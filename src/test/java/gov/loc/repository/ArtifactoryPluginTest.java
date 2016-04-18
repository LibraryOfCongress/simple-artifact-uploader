package gov.loc.repository;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Test;

import gov.loc.repository.task.UploadTask;

public class ArtifactoryPluginTest extends Assert {

  @Test
  public void testApplyByClass(){
    Project project = ProjectBuilder.builder().build();
    project.getPluginManager().apply(ArtifactoryPlugin.class);
    
    assertTrue(project.getTasks().getByName("upload") instanceof UploadTask);
  }
  
  @Test
  public void testApplyByName(){
    Project project = ProjectBuilder.builder().build();
    project.getPlugins().apply("gov.loc.repository.artifactory");

    assertTrue(project.getTasks().getByName("upload") instanceof UploadTask);
  }
}
