package gov.loc.repository;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import gov.loc.repository.extension.UploadPluginExtension;
import gov.loc.repository.task.UploadTask;

/**
 * The class that extends gradle by adding the artifactory closure and the upload task
 */
public class SimpleArtifactUploaderPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {
    UploadPluginExtension extension = project.getExtensions().create("artifactory", UploadPluginExtension.class); //define the artifactory closure
    extension.setFolder(project.getGroup() + "/" + project.getName() + "/" + project.getVersion());
    project.getTasks().create("upload", UploadTask.class); //define the upload task
  }
}
