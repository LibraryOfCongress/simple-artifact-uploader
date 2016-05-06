package gov.loc.repository.actions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import gov.loc.repository.domain.ArtifactHashes;
import gov.loc.repository.model.Artifactory;

public class UploadActionTest extends Assert {
  @Rule
  public final TemporaryFolder folder = new TemporaryFolder();
  
  private UploadAction sut;
  private HttpClient mockClient;
  private Artifactory mockConfig;
  
  @Before
  public void setup() throws IOException{
    mockClient = Mockito.mock(HttpClient.class);
    mockConfig = Mockito.mock(Artifactory.class);
    
    Set<File> artifacts = new HashSet<>();
    File artifact = folder.newFile();
    artifact.createNewFile();
    artifacts.add(artifact);
    
    sut = new UploadAction(mockConfig, artifacts);
    sut.setClient(mockClient);
  }
  
  @Test
  public void testExecute() throws Exception{
    Mockito.when(mockConfig.getUsername()).thenReturn("");
    Mockito.when(mockConfig.getPassword()).thenReturn("");
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockClient.execute(Mockito.any())).thenReturn(mockResponse);
    StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
    Mockito.when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(400); //the artifact doesn't already exist
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(201); //upload was successful
    
    sut.execute(null);
  }
  
  @Test
  public void testGetHashes() throws IOException{
    String artifactoryJson = "{\n" + 
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
    InputStream mockContent = new ByteArrayInputStream(artifactoryJson.getBytes(StandardCharsets.UTF_8));
    
    HttpEntity mockEntity = Mockito.mock(HttpEntity.class);
    Mockito.when(mockEntity.getContent()).thenReturn(mockContent);
    
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockResponse.getEntity()).thenReturn(mockEntity);
    
    ArtifactHashes expectedHashes = new ArtifactHashes("3bc73ab2766e277a9fdba25daca73ee963f92756", "126965f55e9cdce6c8b9cdf0260712dd");
    ArtifactHashes actualHashes = sut.getHashes(mockResponse);
    assertEquals(expectedHashes, actualHashes);
  }
}
