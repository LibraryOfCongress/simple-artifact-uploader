package gov.loc.repository.hash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class HasherTest extends Assert {
  @Rule
  public TemporaryFolder folder= new TemporaryFolder();
  private File testFile;
  
  @Before
  public void setup() throws IOException{
    testFile = folder.newFile();
    Files.write(Paths.get(testFile.toURI()), "Hello World!".getBytes());
  }
  
  @Test
  public void testHasherMD5() throws IOException, NoSuchAlgorithmException{
    InputStream inputStream = Files.newInputStream(Paths.get(testFile.toURI()), StandardOpenOption.READ);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Hasher.hash(inputStream, messageDigest);
  }
  
  @Test
  public void testHasherSHA1() throws IOException, NoSuchAlgorithmException{
    InputStream inputStream = Files.newInputStream(Paths.get(testFile.toURI()), StandardOpenOption.READ);
    MessageDigest messageDigest = MessageDigest.getInstance("MD5");
    Hasher.hash(inputStream, messageDigest);
  }
}
