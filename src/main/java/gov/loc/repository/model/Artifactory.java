package gov.loc.repository.model;

import org.gradle.model.Managed;

@Managed
public interface Artifactory {
//the folder to upload to, like gov/loc/repository/simple-artifact-uploader
  void setFolder(String folder);
  String getFolder();
  
  //which repository to upload into. Like snapshots, releases, etc
  void setRepository(String repository);
  String getRepository();
  
  //The base url of artifactory, includes port
  void setUrl(String url);
  String getUrl();
  
  //for authentication of restful api calls
  void setUsername(String username);
  String getUsername();
  
  //for authentication of restful api calls
  void setPassword(String password);
  String getPassword();
}
