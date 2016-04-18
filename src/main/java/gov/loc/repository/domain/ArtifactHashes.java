package gov.loc.repository.domain;

public class ArtifactHashes {
  final public String sha1;
  final public String md5;
  public ArtifactHashes(String sha1, String md5){
    this.sha1 = sha1;
    this.md5 = md5;
  }
}
