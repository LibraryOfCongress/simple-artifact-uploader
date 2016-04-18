package gov.loc.repository.task;

import java.io.File;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import gov.loc.repository.ArtifactoryPlugin;

public class UploadTaskTest extends Assert {
  private UploadTask sut;
  private Project project;
  private HttpClient mockClient;
  
  @Before
  public void setup(){
    project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(ArtifactoryPlugin.class);
    project.getPluginManager().apply("java");
    
    sut = (UploadTask) project.getTasks().getByName("upload");
    mockClient = Mockito.mock(HttpClient.class);
    sut.setClient(mockClient);
  }
  
  @Test
  public void testUploadArtifacts() throws IOException{
    FileCollection artifacts = project.getConfigurations().getByName("archives").getAllArtifacts().getFiles();
    for(File artifact : artifacts){
      artifact.getParentFile().mkdirs();
      artifact.createNewFile();
    }
    
    HttpResponse mockResponse = Mockito.mock(HttpResponse.class);
    Mockito.when(mockClient.execute(Mockito.any())).thenReturn(mockResponse);
    StatusLine mockStatusLine = Mockito.mock(StatusLine.class);
    Mockito.when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(400); //the artifact doesn't already exist
    Mockito.when(mockStatusLine.getStatusCode()).thenReturn(201); //upload was successful
    
    sut.uploadArtifacts(); 
  }
}
