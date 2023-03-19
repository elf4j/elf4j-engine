# elf4j-impl-core

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-impl-core.svg?label=Maven%20Central)](https://central.sonatype.com/search?smo=true&q=pkg%253Amaven%252Fio.github.elf4j%252Felf4j-impl-core)

The Java portion of [elf4j-impl](https://github.com/elf4j/elf4j-impl), where this will be packaged via the
Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism to
make a native logging service provider implementation of the [ELF4J](https://github.com/elf4j/) (Easy Logging Facade for
Java) SPI. See [elf4j-impl](https://github.com/elf4j/elf4j-impl) for description.

Although directly implementing the [ELF4J](https://github.com/elf4j/elf4j) API, this is a stand-alone logging engine. It
is designed to be adaptable to service other logging API, for example, as
a [logging engine for SLF4J](https://github.com/elf4j/slf4j-elf4j).