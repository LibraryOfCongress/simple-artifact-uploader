package gov.loc.repository;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import gov.loc.repository.extension.UploadPluginExtension;
import gov.loc.repository.task.UploadTask;

public class ArtifactoryPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {
    project.getExtensions().create("artifactory", UploadPluginExtension.class); //define the artifactory closure
    project.getTasks().create("upload", UploadTask.class); //define the upload task
  }
}
