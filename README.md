# elf4j-engine

![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-engine?label=Maven%20Central)

A Java log engine implementing the [ELF4J](https://github.com/elf4j/elf4j) (Easy Logging Facade for Java ) API.

Naturally, this engine is packaged by [elf4j-provider](https://github.com/elf4j/elf4j-provider) via the
Java [Service Provider Framework](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html) mechanism to
make a complete and native logging _service provider_ of the [ELF4J](https://github.com/elf4j/) logging facade.

See [elf4j-provider](https://github.com/elf4j/elf4j-provider) for the engine's features and usage descriptions.

Meanwhile, this is also a stand-alone log engine, designed to be adaptable for servicing other Java logging APIs. For
example, it is a log engine for the [SLF4J](https://www.slf4j.org/) API, as
in [slf4j-elf4j](https://github.com/elf4j/slf4j-elf4j); or for the Java Platform
Logging ([JPL](https://openjdk.org/jeps/264)) API introduced since Java 9, as
in [jpl-elf4j](https://github.com/elf4j/jpl-elf4j).
