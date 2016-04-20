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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
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
 * The class that actually does the uploading
 */
public class UploadTask extends DefaultTask{
  private static final Logger logger = Logging.getLogger(UploadTask.class);
  private static final String API = "api/storage";
  private HttpClient client;
  private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
  
  public UploadTask(){
    this.setDescription("If a project generates an artifact, upload it to artifactory");
    
    client = HttpClients.custom()
        .setDefaultCredentialsProvider(credentialsProvider)
        .build();
  }

  @TaskAction
  public void uploadArtifacts(){
    UploadPluginExtension extension = getProject().getExtensions().findByType(UploadPluginExtension.class);
    if (extension == null) {
        extension = new UploadPluginExtension();
    }
    
    FileCollection artifacts = getProject().getConfigurations().getByName("archives").getAllArtifacts().getFiles();
    
    for(File artifact : artifacts){
      try{
        ArtifactHashes hashes = calculateHashes(artifact);
        if(hashesDiffer(hashes, extension.getRepository(), extension.getFolder(), artifact.getName(), extension.getUrl())){
          upload(extension, artifact);
        }
        else{
          logger.quiet("Skipping upload since checksums match for {}", artifact);
        }
      }
      catch(Exception e){
        throw new GradleException("Failed to upload artifact " + artifact, e);
      }
    }
  }
  
  protected ArtifactHashes calculateHashes(File artifact) throws NoSuchAlgorithmException, IOException{
    logger.debug("Calulating hash for {}", artifact);
    
    MessageDigest md5MessageDigest = MessageDigest.getInstance("MD5");
    MessageDigest sha1MessageDigest = MessageDigest.getInstance("SHA1");
    
    String md5 = calculateHash(artifact, md5MessageDigest);
    logger.debug("MD5 hash for {} is {}", artifact, md5);
    
    String sha1 = calculateHash(artifact, sha1MessageDigest);
    logger.debug("SHA1 hash for {} is {}", artifact, sha1);
    
    return new ArtifactHashes(sha1, md5);
  }

  protected String calculateHash(File file, MessageDigest messageDigest) throws IOException{
    InputStream stream = Files.newInputStream(Paths.get(file.toURI()), StandardOpenOption.READ);
    return Hasher.hash(stream, messageDigest);
  }

  protected boolean hashesDiffer(ArtifactHashes calculatedHashes, String repo, String folder, String artifactName, String artifactoryUrl) throws ClientProtocolException, IOException{
    logger.debug("Seeing if our calculated hash is different than the last uploaded artifact");
    StringBuilder url = new StringBuilder();
    url.append(artifactoryUrl).append("/").append(API).append("/").append(repo).append("/").append(folder).append("/").append(artifactName);
    
    HttpGet request = new HttpGet(url.toString());
    HttpResponse response = client.execute(request);
    
    if(response.getStatusLine().getStatusCode() != 200){
      logger.debug("got {} so assuming that artifact doesn't exist, so of course it differs", response);
      return true;
    }
    
    ArtifactHashes artifactoryHashes = getHashes(response);
    
    return !Objects.equals(artifactoryHashes.sha1, calculatedHashes.sha1) || !Objects.equals(artifactoryHashes.md5, calculatedHashes.md5);
  }

  protected ArtifactHashes getHashes(HttpResponse response) throws ParseException, IOException{
    logger.debug("Getting MD5 and SHA1 hashes from response");
    String json = EntityUtils.toString(response.getEntity());
    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    JsonObject checkSums = jsonObject.getAsJsonObject("checksums");
    String sha1 = checkSums.get("sha1").getAsString();
    String md5 = checkSums.get("md5").getAsString();
    
    return new ArtifactHashes(sha1, md5);
  }
  
  protected void upload(UploadPluginExtension ext, File artifact) throws ClientProtocolException, IOException{
    HttpEntity entity = MultipartEntityBuilder.create().addBinaryBody(artifact.getName(), artifact, ContentType.create("application/octet-stream"), artifact.getName()).build();
    
    StringBuilder url = new StringBuilder();
    url.append(ext.getUrl()).append("/").append(ext.getRepository()).append("/").append(ext.getFolder()).append("/").append(artifact.getName());
    logger.debug("Uploading {} to {}", artifact.getName(), url.toString());
    
    credentialsProvider.setCredentials(
        new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
        new UsernamePasswordCredentials(ext.getUsername(), ext.getPassword()));
    
    HttpPut request = new HttpPut(url.toString());
    request.setEntity(entity);

    HttpResponse response = client.execute(request);

    if(response.getStatusLine().getStatusCode() != 201){
      throw new GradleException("Unable to upload artifact. Response from artifactory was " + response.getStatusLine());
    }
    
    logger.debug("Uploaded {} successfully!", artifact.getName());
  }

  /**
   * Just for unit testing!
   * 
   * @param client the object actually doing the http request
   */
  protected void setClient(HttpClient client) {
    this.client = client;
  }
}
