package gov.loc.repository;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.model.Model;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;

import gov.loc.repository.actions.UploadAction;
import gov.loc.repository.model.Artifactory;

/**
 * The class that extends gradle by adding the artifactory closure and the upload task
 */
public class SimpleArtifactUploaderPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {
//    project.getExtensions().create("artifactory", UploadPluginExtension.class); //define the artifactory closure
//    project.getTasks().create("upload", UploadTask.class); //define the upload task
  }
  
  static class Rules extends RuleSource {
    @SuppressWarnings("unused")
    @Model 
    void artifactory(Artifactory artifactory) {}
    
    @Mutate
    public void createUploadTask(ModelMap<Task> tasks, Artifactory artifactoryConfig){
      tasks.create("uploadToArtifactory");
      Task uploadTask = tasks.get("uploadToArtifactory");
      uploadTask.setGroup("publishing");
      uploadTask.setDescription("If a project generates an artifact, upload it to artifactory");
      
      Set<File> artifacts = new HashSet<>();
      
      for(Task task : tasks){
        if(task instanceof AbstractArchiveTask){
          uploadTask.dependsOn(task);
          artifacts.add(((AbstractArchiveTask)task).getArchivePath());
        }
      }
      
      uploadTask.getActions().add(new UploadAction(artifactoryConfig, artifacts));
    }
  }
}
