# SwayDB.java 

[![Maven Central](https://img.shields.io/maven-central/v/io.swaydb/swaydb-java.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.swaydb%22%20AND%20a%3A%22swaydb-java%22)
[![Build Status](https://travis-ci.com/simerplaha/SwayDB.java.svg?branch=master)](https://travis-ci.com/simerplaha/SwayDB.java)
[![codecov.io](http://codecov.io/github/simerplaha/SwayDB.java/coverage.svg?branch=master)](http://codecov.io/github/simerplaha/SwayDB.java?branch=master)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=SwayDB.java&metric=sqale_rating)](https://sonarcloud.io/dashboard/index/SwayDB.java)
[![Scrutinizer](https://img.shields.io/scrutinizer/g/simerplaha/SwayDB.java.svg)](https://scrutinizer-ci.com/g/simerplaha/SwayDB.java/)

[![Join the chat at https://gitter.im/SwayDB-chat/Lobby](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/SwayDB-chat/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Java wrapper for [SwayDB](https://github.com/simerplaha/SwayDB).

Requirements
============

Java 1.8 and later.

## Installation

Include the following in your `pom.xml` for Maven:

```
<dependencies>
  <dependency>
    <groupId>io.swaydb</groupId>
    <artifactId>swaydb-java</artifactId>
    <version>0.8-beta.8.5</version>
  </dependency>
  ...
</dependencies>
```

Gradle:

```groovy
compile 'io.swaydb:swaydb-java:0.8-beta.8.5'
```

### Usage

```java
import java.util.AbstractMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class QuickStartTest {

    @Test
    public void quickStart() {
        // Create a memory database
        swaydb.java.Map<Integer, String> map = swaydb.java.memory.Map.create(Integer.class, String.class);

        map.put(1, "one");
        map.get(1);
        map.remove(1);

        // write 100 key-values atomically
        map.put(IntStream.rangeClosed(1, 100)
            .mapToObj(index -> new AbstractMap.SimpleEntry<>(index, String.valueOf(index)))
            .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue())));

        // Iteration: fetch all key-values withing range 10 to 90,
        // update values and atomically write updated key-values
        map
            .from(10)
            .takeWhile(item -> item.getKey() <= 90)
            .map(item -> new AbstractMap.SimpleEntry<>(item.getKey(), item.getValue() + "_updated"))
            .materialize().foreach(map::put);
    }
}
```
