package gov.loc.repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ArtifactoryPluginTest extends Assert {
  private static final String PLUGIN_TASK_NAME = "uploadToArtifactory";
  
  @Rule
  public final TemporaryFolder testProjectDir = new TemporaryFolder();
  private File buildFile;

  @Before
  public void setup() throws IOException {
    buildFile = testProjectDir.newFile("build.gradle");
  }
  
  @Ignore //TODO figure out a way to nicely test this without having to specify username and password...
  @Test
  public void testIntegration() throws IOException{
    String buildFileContent = 
        "plugins {\n" + 
        "  id 'java' \n" + 
        "  id 'gov.loc.repository.simple-artifact-uploader'\n" + 
        "}\n" + 
        "\n" + 
        "model {\n" + 
        "  artifactory {\n" + 
        "    folder = 'simple-artifact-uploader'\n" + 
        "    url = 'http://artifactory:8081/artifactory'\n" + 
        "    repository = 'plugins-snapshots-local'\n" +
        "  }\n" + 
        "}\n" + 
        "\n" + 
        "jar{\n" + 
        "  archiveName = 'integration-test.jar'\n" + 
        "}";
    writeFile(buildFile, buildFileContent);

    BuildResult result = GradleRunner.create().withPluginClasspath()
        .withProjectDir(testProjectDir.getRoot())
        .withArguments(PLUGIN_TASK_NAME, "--stacktrace")
        .build();

    assertEquals(result.task(":" + PLUGIN_TASK_NAME).getOutcome(), TaskOutcome.SUCCESS);
  }

  @Test
  public void testCreateUploadToArtifactoryTask() throws IOException {
    String buildFileContent = 
        " plugins {\n" +
        "            id 'gov.loc.repository.simple-artifact-uploader'\n" +
        "}";
    writeFile(buildFile, buildFileContent);

    BuildResult result = GradleRunner.create().withPluginClasspath()
        .withProjectDir(testProjectDir.getRoot())
        .withArguments("tasks")
        .build();

    assertTrue(result.getOutput().contains(PLUGIN_TASK_NAME));
  }

  private void writeFile(File destination, String content) throws IOException {
    BufferedWriter output = null;
    try {
      output = new BufferedWriter(new FileWriter(destination));
      output.write(content);
    } finally {
      if (output != null) {
        output.close();
      }
    }
  }
}
