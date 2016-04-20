package gov.loc.repository.domain;

import java.util.Objects;

/**
 * Data class for storing the MD5 and SHA1 checksum of an artifact
 */
public class ArtifactHashes {
  final public String sha1;
  final public String md5;
  
  public ArtifactHashes(String sha1, String md5){
    this.sha1 = sha1;
    this.md5 = md5;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sha1, md5);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj){
      return true;
    }
    if (obj == null){
      return false;
    }
    if (!(obj instanceof ArtifactHashes)){
      return false;
    }
    ArtifactHashes other = (ArtifactHashes) obj;
    return Objects.equals(sha1, other.sha1) && Objects.equals(md5, other.md5);
  }
  
  
}
