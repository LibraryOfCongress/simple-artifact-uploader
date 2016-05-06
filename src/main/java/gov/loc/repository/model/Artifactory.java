package gov.loc.repository.model;

import org.gradle.model.Managed;

/**
 * The model for uploading to artifactory.
 */
@Managed
public interface Artifactory {
  /**
   * @param folder the folder to upload to on artifactory. For example gov/loc/repository/simple-artifact-uploader
   */
  void setFolder(String folder);
  /**
   * @return the folder to upload to on artifactory. For example gov/loc/repository/simple-artifact-uploader
   */
  String getFolder();
  
  /**
   * @param repository the repository to upload into. For example snapshots, releases, etc
   */
  void setRepository(String repository);
  /**
   * @return the repository to upload into. For example snapshots, releases, etc
   */
  String getRepository();
  
  /**
   * @param url The base url of artifactory, including the port. For example https://artifactory.com:8080
   */
  void setUrl(String url);
  /**
   * @return The base url of artifactory, including the port. For example https://artifactory.com:8080
   */
  String getUrl();
  
  /**
   * @param username the username to use if uploading is restricted to certain users
   */
  void setUsername(String username);
  /**
   * @return the username to use if uploading is restricted to certain users
   */
  String getUsername();
  
  /**
   * @param password the password of the user if uploading is restricted to certain users
   */
  void setPassword(String password);
  /**
   * @return the password of the user if uploading is restricted to certain users
   */
  String getPassword();
}
