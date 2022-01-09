# Release Hints

See http://central.sonatype.org/pages/apache-maven.html for more infos about the sonatype release process.
See https://maven.apache.org/plugins/maven-gpg-plugin/usage.html for more infos.

## required ~/.m2/settings.xml

```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/settings/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    ...
    <server>
      <id>sonatype</id>
      <username>yourusername</username>
      <password>{your password encrypted with mvn --encrypt-password yourpassword}</password>
    </server>
    ...
  </servers>

</settings>
```

## release artifact into staging repo
```
mvn release:prepare release:perform --batch-mode -Darguments='-Dgpg.keyname=A77B975B -Dgpg.passphrase=thepassphrase'
```

## list staging repos
```
mvn nexus-staging:rc-list | grep iogithubedilib
```

## close staging repo
```
mvn nexus-staging:rc-close -DstagingRepositoryId=iogithubedilib-xxxx
```

## abort staging process
```
mvn nexus-staging:drop -DstagingRepositoryId=iogithubedilib-xxxx
```

## release artifact from staging repo to public
```
mvn nexus-staging:release -DstagingRepositoryId=iogithubedilib-xxxx
```
