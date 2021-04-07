Contributing
==========
Download the source code with

    $ git clone https://github.com/PureGero/JustPlots.git

A folder will be created called `JustPlots` with the source code inside. You can
open this folder with your favourite IDE (eg Eclipse or IntelliJ) and begin
editing.

Compiling
=========
Compile the source with gradle:

    $ mvn

The plugin jar will be found in `target`. Enjoy!

JustPlots as a dependency
=========================
[![Clojars Project](https://img.shields.io/clojars/v/net.justminecraft.plots/justplots.svg)](https://clojars.org/net.justminecraft.plots/justplots)

Add the following into your build.gradle:

```
repositories {
  maven {
    url "https://repo.clojars.org/"
  }
}

dependencies {
  compile "net.justminecraft.plots:justplots:0.9.6"
}
```

Or in your pom.xml:

```
<repositories>
    <repository>
        <id>justplots-repo</id>
        <url>https://repo.clojars.org/</url>
    </repository>
</repositories>
<dependencies>
    <dependency>
        <groupId>net.justminecraft.plots</groupId>
        <artifactId>justplots</artifactId>
        <version>0.9.6</version>
    </dependency>
</dependencies>
```