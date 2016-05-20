package gov.loc.repository.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.loc.repository.domain.ArtifactHashes;
import gov.loc.repository.extension.UploadPluginExtension;
import gov.loc.repository.hash.Hasher;

/**
 * The class that actually does the uploading to artifactory
 */
public class UploadTask extends DefaultTask {
  private static final Logger logger = Logging.getLogger(UploadTask.class);
  private static final String API = "api/storage";
  private transient HttpClient client;
  private transient final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

  public UploadTask() {
    this.setDescription("If a project generates an artifact, upload it to artifactory");

    client = HttpClients.custom()
        .setDefaultCredentialsProvider(credentialsProvider)
        .build();
  }

  @TaskAction
  public void uploadArtifacts() {
    UploadPluginExtension extension = getProject().getExtensions().findByType(UploadPluginExtension.class);
    final FileCollection artifacts = getProject().getConfigurations().getByName("archives").getAllArtifacts().getFiles();

    for (final File artifact : artifacts) {
      try {
        final ArtifactHashes hashes = calculateHashes(artifact);
        final ArtifactHashes artifactoryHashes = getArtifactoryHashes(extension, artifact.getName());
        if (hashesDiffer(hashes, artifactoryHashes)) {
          upload(extension, artifact);
        } else {
          logger.quiet("Skipping upload since checksums match for {}", artifact);
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        throw new GradleException("Failed to upload artifact " + artifact, e);
      }
    }
  }

  protected ArtifactHashes calculateHashes(final File artifact) throws NoSuchAlgorithmException, IOException {
    logger.debug("Calulating hash for {}", artifact);

    final MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
    final MessageDigest sha1MessageDigest = MessageDigest.getInstance("SHA1");

    final String md5 = calculateHash(artifact, md5MessageDigest);
    logger.debug("MD5 hash for {} is {}", artifact, md5);

    final String sha1 = calculateHash(artifact, sha1MessageDigest);
    logger.debug("SHA1 hash for {} is {}", artifact, sha1);

    return new ArtifactHashes(sha1, md5);
  }

  protected String calculateHash(final File file, final MessageDigest messageDigest) throws IOException {
    final InputStream stream = Files.newInputStream(Paths.get(file.toURI()), StandardOpenOption.READ);
    return Hasher.hash(stream, messageDigest);
  }

  protected boolean hashesDiffer(final ArtifactHashes calculatedHashes, final ArtifactHashes artifactoryHashes) throws ClientProtocolException, IOException {
    logger.debug("Seeing if our calculated hash is different than the last uploaded artifact");
    logger.debug("artifactory sha1 [{}] differs from calculated [{}] ? {}", artifactoryHashes.sha1, calculatedHashes.sha1, Objects.equals(artifactoryHashes.sha1, calculatedHashes.sha1));
    logger.debug("artifactory md5 [{}] differs from calculated [{}] ? {}", artifactoryHashes.md5, calculatedHashes.md5, Objects.equals(artifactoryHashes.md5, calculatedHashes.md5));
    
    return !Objects.equals(artifactoryHashes.sha1, calculatedHashes.sha1)
        || !Objects.equals(artifactoryHashes.md5, calculatedHashes.md5);
  }
  
  protected ArtifactHashes getArtifactoryHashes(final UploadPluginExtension ext, final String artifactName) throws ClientProtocolException, IOException{
    final StringBuilder url = new StringBuilder();
    url.append(ext.getUrl()).append('/').append(API).append('/').append(ext.getRepository()).append('/').append(ext.getFolder()).append('/').append(artifactName);
    logger.debug("Getting artifact information from url {}", url.toString());

    final HttpGet request = new HttpGet(url.toString());
    final HttpResponse response = client.execute(request);
    
    if (response.getStatusLine().getStatusCode() != 200) {
      logger.debug("got {} so assuming that artifact doesn't exist, so of course it differs", response);
      EntityUtils.consume(response.getEntity());
      return new ArtifactHashes("", "");
    }

    final ArtifactHashes artifactoryHashes = getHashes(response);
    EntityUtils.consume(response.getEntity());
    return artifactoryHashes;
  }

  protected ArtifactHashes getHashes(final HttpResponse response) throws ParseException, IOException {
    logger.debug("Getting MD5 and SHA1 hashes from response");
    final String json = EntityUtils.toString(response.getEntity());
    final JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    final JsonObject checkSums = jsonObject.getAsJsonObject("checksums");
    final String sha1 = checkSums.get("sha1").getAsString();
    final String md5 = checkSums.get("md5").getAsString();

    return new ArtifactHashes(sha1, md5);
  }

  protected void upload(final UploadPluginExtension ext, final File artifact) throws ClientProtocolException, IOException {
    final HttpEntity entity = new FileEntity(artifact);

    final StringBuilder url = new StringBuilder();
    url.append(ext.getUrl()).append('/').append(ext.getRepository()).append('/').append(ext.getFolder()).append('/').append(artifact.getName());
    logger.debug("Uploading {} to {}", artifact.getName(), url.toString());

    credentialsProvider.setCredentials(
        new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
        new UsernamePasswordCredentials(ext.getUsername(), ext.getPassword()));

    final HttpPut request = new HttpPut(url.toString());
    request.setEntity(entity);

    final HttpResponse response = client.execute(request);

    if (response.getStatusLine().getStatusCode() != 201) {
      EntityUtils.consume(response.getEntity());
      throw new GradleException("Unable to upload artifact. Response from artifactory was " + response.getStatusLine());
    }

    EntityUtils.consume(response.getEntity());
    logger.debug("Uploaded {} successfully!", artifact.getName());
  }

  /**
   * Just for unit testing!
   * 
   * @param client
   *          the object actually doing the http request
   */
  protected void setClient(final HttpClient client) {
    this.client = client;
  }
}
