# Simple Artifact Uploader
The plugin providied by artifactory involves a lot of configuration. This plugin was created to simplify uploading gradle built artifacts to artifactory. It uses artifactory's rest api to do the actual work.

[![Master Branch Build Status](https://travis-ci.org/LibraryOfCongress/simple-artifact-uploader.svg?branch=master)](https://travis-ci.org/LibraryOfCongress/simple-artifact-uploader)

## License
[![cc0](http://i.creativecommons.org/p/zero/1.0/88x31.png)](http://creativecommons.org/publicdomain/zero/1.0/)

Note: By contributing to this project, you agree to license your work under the
same terms as those that govern this project's distribution.

## Install
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

## Usage
### Configuring the plugin
``` groovy
artifactory{
  folder "foo"
  url = "http://<artifactoryServer>:<PORT>/artifactory" //the url of artifactory
  repository = "rdc-snapshots" //which repository to upload to
  username = "${artifactory_username}" //the user to authenticate with. Property should be located in your private gradle properties file
  password = "${artifactory_password}" //password of the user. Property should be located in your private gradle properties file 
}
```
### Adding more artifacts to be uploaded
``` groovy
task myJar(type: Jar)

artifacts {
    archives myJar //register the output of the myJar task as an artifact. All registered artifacts are automatically uploaded.
}
```

## Help
* For more information about where to put your gradle.properties file see [here](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties)

## Development
* Version number
  * 1.X is for development using the current gradle model
  * 2.X is for development using the new [gradle model and rules](https://docs.gradle.org/2.13/userguide/pt06.html)
* Master branch will be used for publishing to plugins.gradle.org

## Contribute
Please send us your pull requests! See the [roadmap](#roadmap) or issues for areas where you can help us improve

## Roadmap
- [ ] extend config for controlling other aspects of the rest api requests (timeout, number of tries, etc)
- [ ] extend config to controll which folder individual artifacts are uploaded to
- [ ] extend config to allow upload to multiple artifactory servers
- [ ] upload artifact with pregenerated checksums instead of having artifactory do it later (causes checksum check to always fail. See [issue #3](https://github.com/LibraryOfCongress/simple-artifact-uploader/issues/3) ) 
