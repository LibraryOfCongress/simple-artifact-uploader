package gov.loc.repository.domain;

import org.junit.Assert;
import org.junit.Test;

public class ArtifactHashesTest extends Assert {
  private final ArtifactHashes sut = new ArtifactHashes("sha1", "md5");
  private final ArtifactHashes same = new ArtifactHashes("sha1", "md5");
  private final ArtifactHashes onlyMD5 = new ArtifactHashes("sha1-different", "md5");
  private final ArtifactHashes onlySHA1 = new ArtifactHashes("sha1", "md5-different");
  private final ArtifactHashes different = new ArtifactHashes("foo", "bar");

  @Test
  public void testEqualsReturnsTrueWhenSameObject(){
    assertEquals(sut, sut);
  }
  
  @Test
  public void testEqualsReturnsTrueWhenSameValues(){
    assertEquals(sut, same);
  }
  
  @Test
  public void testEqualsReturnsFalseWhenOnlyMD5Matches(){
    assertNotEquals(sut, onlyMD5);
  }
  
  @Test
  public void testEqualsReturnsFalseWhenOnlySHA1Matches(){
    assertNotEquals(sut, onlySHA1);
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNeitherValueMatches(){
    assertNotEquals(sut, different);
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNotSameType(){
    assertNotEquals(sut, new Object());
  }
  
  @Test
  public void testEqualsReturnsFalseWhenNull(){
    assertNotEquals(sut, null);
  }
  
  @Test
  public void testHashcodeProducesSameHashForEquivalentObjects(){
    assertEquals(sut.hashCode(), same.hashCode());
  }
}
