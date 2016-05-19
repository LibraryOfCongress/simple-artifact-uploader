package gov.loc.repository.task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.loc.repository.SimpleArtifactUploaderPlugin;
import gov.loc.repository.domain.ArtifactHashes;
import gov.loc.repository.extension.UploadPluginExtension;

public class UploadTaskTest extends Assert {
  private static final String ARTIFACTORY_JSON = "{\n" + 
      "  \"repo\" : \"rdc-releases\",\n" + 
      "  \"path\" : \"/loc-repository/inventory/3.7.0/inventory-3.7.0.jar\",\n" + 
      "  \"created\" : \"2016-03-28T11:19:11.846-04:00\",\n" + 
      "  \"createdBy\" : \"fafu\",\n" + 
      "  \"lastModified\" : \"2016-03-28T11:19:11.716-04:00\",\n" + 
      "  \"modifiedBy\" : \"fafu\",\n" + 
      "  \"lastUpdated\" : \"2016-03-28T11:19:11.716-04:00\",\n" + 
      "  \"downloadUri\" : \"http://140.147.214.66:8081/artifactory/rdc-releases/loc-repository/inventory/3.7.0/inventory-3.7.0.jar\",\n" + 
      "  \"mimeType\" : \"application/java-archive\",\n" + 
      "  \"size\" : \"407528\",\n" + 
      "  \"checksums\" : {\n" + 
      "    \"sha1\" : \"3bc73ab2766e277a9fdba25daca73ee963f92756\",\n" + 
      "    \"md5\" : \"126965f55e9cdce6c8b9cdf0260712dd\"\n" + 
      "  },\n" + 
      "  \"originalChecksums\" : {\n" + 
      "    \"sha1\" : \"3bc73ab2766e277a9fdba25daca73ee963f92756\",\n" + 
      "    \"md5\" : \"126965f55e9cdce6c8b9cdf0260712dd\"\n" + 
      "  },\n" + 
      "  \"uri\" : \"http://140.147.214.66:8081/artifactory/api/storage/rdc-releases/loc-repository/inventory/3.7.0/inventory-3.7.0.jar\"\n" + 
      "}";
  
  private UploadTask sut;
  private Project project;
  private HttpClient mockClient;
  private UploadPluginExtension extension;
  
  @Before
  public void setup(){
    extension = new UploadPluginExtension();
    extension.setFolder("folder");
    extension.setRepository("repository");
    extension.setUrl("url");
    
    project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(SimpleArtifactUploaderPlugin.class);
    project.getPluginManager().apply("java");
    
    sut = (UploadTask) project.getTasks().getByName("uploadToArtifactory");
    mockClient = Mockito.mock(HttpClient.class);
    sut.setClient(mockClient);
  }
  
  @Test
  public void testHashesDiffer() throws ClientProtocolException, IOException{
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockClient.execute(Mockito.any(HttpRequestBase.class))).thenReturn(mockResponse);
    
    StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
    Mockito.when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(200); //the artifact doesn't already exist
    
    HttpEntity httpEntity = Mockito.mock(HttpEntity.class);
    Mockito.when(mockResponse.getEntity()).thenReturn(httpEntity);
    Mockito.when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(ARTIFACTORY_JSON.getBytes(StandardCharsets.UTF_8)));
    
    ArtifactHashes calculatedHashes = new ArtifactHashes("sha1", "md5");
    
    boolean differ = sut.hashesDiffer(calculatedHashes, extension, "artifactName");
    assertTrue(differ);
  }
  
  @Test
  public void testUploadArtifacts() throws IOException{
    FileCollection artifacts = project.getConfigurations().getByName("archives").getAllArtifacts().getFiles();
    for(File artifact : artifacts){
      artifact.getParentFile().mkdirs();
      artifact.createNewFile();
    }
    
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockClient.execute(Mockito.any(HttpRequestBase.class))).thenReturn(mockResponse);
    StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
    Mockito.when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(400); //the artifact doesn't already exist
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(201); //upload was successful
    
    sut.uploadArtifacts(); 
  }
  
  @Test
  public void testGetHashes() throws IOException{
    InputStream mockContent = new ByteArrayInputStream(ARTIFACTORY_JSON.getBytes(StandardCharsets.UTF_8));
    
    HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
    Mockito.when(mockEntity.getContent()).thenReturn(mockContent);
    
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockResponse.getEntity()).thenReturn(mockEntity);
    
    ArtifactHashes expectedHashes = new ArtifactHashes("3bc73ab2766e277a9fdba25daca73ee963f92756", "126965f55e9cdce6c8b9cdf0260712dd");
    ArtifactHashes actualHashes = sut.getHashes(mockResponse);
    assertEquals(expectedHashes, actualHashes);
  }
}
