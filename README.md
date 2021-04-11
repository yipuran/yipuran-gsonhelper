# yipuran-gsonhelper
Google gson use library

## Dependency
https://github.com/google/gson


## Document
Extract doc/yipuran-gsonhelper-doc.zip and see the Javadoc
or [Wiki Page](../../wiki)

## Setup pom.xml
```
<repositories>
   <repository>
      <id>yipuran-gsonhelper</id>
      <url>https://raw.github.com/yipuran/yipuran-gsonhelper/mvn-repo</url>
   </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.yipuran.gsonhelper</groupId>
        <artifactId>yipuran-gsonhelper</artifactId>
        <version>4.23</version>
    </dependency>
</dependencies>
```


## Setup gradle
```
repositories {
    mavenCentral()
    maven { url 'https://raw.github.com/yipuran/yipuran-gsonhelper/mvn-repo'  }
}

dependencied {
    compile 'org.yipuran.core:yipuran-gsonhelper:4.23'
}
```
