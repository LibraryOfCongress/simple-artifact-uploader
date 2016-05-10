package gov.loc.repository;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import gov.loc.repository.extension.UploadPluginExtension;
import gov.loc.repository.task.UploadTask;

/**
 * The class that extends gradle by adding the artifactory closure and the upload task
 */
public class SimpleArtifactUploaderPlugin implements Plugin<Project>{
  private static final Logger logger = Logging.getLogger(SimpleArtifactUploaderPlugin.class);
  
  @Override
  public void apply(Project project) {
    UploadPluginExtension extension = project.getExtensions().create("artifactory", UploadPluginExtension.class); //define the artifactory closure
    extension.setFolder(project.getGroup() + "/" + project.getName() + "/" + project.getVersion());
    
    UploadTask upload = project.getTasks().create("uploadToArtifactory", UploadTask.class); //define the upload task
    
    //make sure tasks that generate artifacts happen first
    for(Task task : project.getTasks()){
      if(task instanceof AbstractArchiveTask){
        logger.debug("Adding dependsOn {} for uploadToArtifactory", task.getName());
        upload.dependsOn(task);
      }
    }
  }
}
