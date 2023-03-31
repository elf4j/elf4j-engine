# elf4j-engine

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-engine.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-engine%22)

Asynchronous Java log engine implementing the [ELF4J](https://github.com/elf4j/elf4j) (Easy Logging Facade for Java ) API.

Naturally, this engine is packaged by [elf4j-provider](https://github.com/elf4j/elf4j-provider) via the
Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism to
make a complete and native logging _service provider_ of the [ELF4J](https://github.com/elf4j/) logging facade.

Meanwhile, this is also a stand-alone log engine, designed to be adaptable for servicing other Java logging APIs. For
example, it is a log engine for the [SLF4J](https://www.slf4j.org/) API, as
in [slf4j-elf4j](https://github.com/elf4j/slf4j-elf4j); it is also a log engine for the Java Platform
Logging ([JPL](https://openjdk.org/jeps/264)) API introduced since Java 9, as
in [jpl-elf4j](https://github.com/elf4j/jpl-elf4j).

## Features and Configuration

For using ELF4J as a logging facade API, see
ELF4J's [API description](https://github.com/elf4j/elf4j#log-service-interface-and-access-api)
and [sample usage](https://github.com/elf4j/elf4j#use-it---for-log-service-api-clients).

For more details of using this as the runtime log engine, see [elf4j-provider](https://github.com/elf4j/elf4j-provider)
on [features](https://github.com/elf4j/elf4j-provider#features)
and [configuration](https://github.com/elf4j/elf4j-provider#configuration).
