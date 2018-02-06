# Terminator
[![Build Status](https://travis-ci.org/taivokasper/Terminator.svg?branch=master)](https://travis-ci.org/taivokasper/Terminator)
[ ![Download](https://api.bintray.com/packages/taivokasper/maven/Terminator/images/download.svg) ](https://bintray.com/taivokasper/maven/Terminator/_latestVersion)

Terminator is a small Java library for easing the process of shutting down multiple executor services in parallel as fast as possible without any new threads.

I have learned that often times Java executor services are not shut down, waste too much time shutting down or shutdown logic is not [implemented correctly](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html)
which may result in unexpected behaviour during graceful process termination, hence the reason for this library.

## Usage samples
* Treat executor services as equal and terminate all with a single timeout
```java
EqualTerminator equalTerminator = TerminatorFactory.createEqualTerminator();

equalTerminator.addItem(Executors.newSingleThreadExecutor());
equalTerminator.addItem(Executors.newCachedThreadPool());
equalTerminator.addItem(Executors.newFixedThreadPool(3));

// Now terminate all executor services in total of 5 seconds
equalTerminator.terminate(5, SECONDS);
```

* For edge cases where executor services are not equal and some take longer to stop their work
```java
UnequalTerminator unequalTerminator = TerminatorFactory.createUnequalTerminator();

unequalTerminator.addItem(Executors.newSingleThreadExecutor(), 5, SECONDS);
unequalTerminator.addItem(Executors.newCachedThreadPool(), 15, SECONDS);
unequalTerminator.addItem(Executors.newFixedThreadPool(1), 1, SECONDS);

// Now terminate all executor services in total of 15 seconds. The total comes from whatever is the largest timeout.
// Shutdowns and termination will be performed in ascending timeout order
unequalTerminator.terminate();
```

## Download
* Gradle
```
repositories {
  maven {
    url  "https://dl.bintray.com/taivokasper/maven"
  }
}
dependencies {
  compile 'com.github.taivokasper:terminator:0.1'
}
```
* Maven
```
<repositories>
  <repository>
    <id>taivo-bintray</id>
    <name>Taivo's bintray repository</name>
    <url>https://dl.bintray.com/taivokasper/maven</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.taivokasper</groupId>
  <artifactId>terminator</artifactId>
  <version>0.1</version>
</dependency>
```
