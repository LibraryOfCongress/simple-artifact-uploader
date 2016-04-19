package gov.loc.repository.extension;

/**
 * Used for defining the artifactory closure where you can configure the settings
 */
public class UploadPluginExtension {
  private String folder = ""; //the folder to upload into in artifactory
  private String repository = "libs-release-local"; //the repository to upload into. 
  private String url = "http://artifactory"; //the artifactory instance
  private String username = "";
  private String password = "";
  
  public String getFolder() {
    return folder;
  }
  public void setFolder(String folder) {
    this.folder = folder;
  }
  public String getRepository() {
    return repository;
  }
  public void setRepository(String repository) {
    this.repository = repository;
  }
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }
}
