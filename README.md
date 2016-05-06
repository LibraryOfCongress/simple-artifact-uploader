# artifactory-plugin
The plugin providied by artifactory involves a lot of configuration. This plugin was created to simplify uploading gradle built artifacts to artifactory. It uses artifactory's rest api to do the actual work.

## License
[![cc0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

Note: By contributing to this project, you agree to license your work under the
same terms as those that govern this project's distribution.

## Adding to your build script
see https://plugins.gradle.org/plugin/gov.loc.repository.artifactory
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.gov.loc.repository:simple-artifact-uploader:1.0"
  }
}

apply plugin: "gov.loc.repository.simple-artifact-uploader"
```
or if you are using gradle version 2.1 or newer
```
plugins {
  id "gov.loc.repository.simple-artifact-uploader" version "1.0"
}
```

## Example use in gradle build script
``` groovy
artifactory{
  folder "foo"
  url = "http://<artifactoryServer>:<PORT>/artifactory" //the url of artifactory
  repository = "rdc-snapshots" //which repository to upload to
  username = "${artifactory_username}" //the user to authenticate with. Property should be located in your private gradle properties file
  password = "${artifactory_password}" //password of the user. Property should be located in your private gradle properties file 
}
```
for more information about where to put your gradle.properties file see [here](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)

##Adding more artifacts to be uploaded
``` groovy
task myJar(type: Jar)

artifacts {
    archives myJar //register the output of the myJar task as an artifact. All registered artifacts are automatically uploaded.
}
```
