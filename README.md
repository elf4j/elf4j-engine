[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-engine.svg?label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/io.github.elf4j/elf4j-engine)

# elf4j-engine

An asynchronous Java log engine.

Implementing the [elf4j](https://github.com/elf4j/elf4j) (Easy Logging Facade for Java ) API, this is the log engine
behind [elf4j-provider](https://github.com/elf4j/elf4j-provider) - a native logging _service provider_ of
the [elf4j](https://github.com/elf4j/) logging facade.

A stand-alone log engine in the meantime, it is designed to be adaptable for servicing other Java logging APIs.

* It is a log engine for the [SLF4J](https://www.slf4j.org/) API, as
  in [slf4j-elf4j](https://github.com/elf4j/slf4j-elf4j).
* It is also a log engine for the Java Platform Logging ([JPL](https://openjdk.org/jeps/264)) API introduced since Java
  9, as in [jpl-elf4j](https://github.com/elf4j/jpl-elf4j).

## Getting started...

1. Install

   To use this as a logging service provider in your application, see installation
   details [here](https://github.com/elf4j/elf4j-provider#installation)

2. Use it for logging in the application:
   ```java 
    package elf4j.engine;
    
    import elf4j.Logger;
    import java.util.function.Supplier;
    import org.slf4j.MDC;
    
    public class Scratch {
      static Logger logger = Logger.instance();
    
      public static void main(String[] args) throws InterruptedException {
        MDC.put("ctx-key", "ctx-value");
        logger.log("Hello, world!");
        logger.atTrace().log("It's a beautiful day");
        Logger info = logger.atInfo();
        info.log("... no matter on what level you say it");
        Logger warn = info.atWarn();
        warn.log("Houston, we do not have {} but let's do {}", "a problem", (Supplier) () -> "a drill");
        Throwable exception = new Exception("This is a drill");
        warn.atError().log(exception);
        logger.atInfo().log(exception, "When being logged, the Throwable always comes {}", "first");
        logger.atInfo().log(
            exception, "The log {} and {} work as usual", () -> "message", () -> "arguments");
        Logger.instance()
            .atInfo()
            .atError()
            .atWarn()
            .atTrace()
            .atDebug()
            .log("Not a practical example but now the severity level is DEBUG");
        Thread.sleep(50);
      }
    }
   ```
   The `Logger` instance is thread-safe, affording flexible usage.
3. Run that application, the follow output will appear in stdout:
   ```
    2025-07-12T17:50:38.015-05:00 INFO elf4j.engine.Scratch - Hello, world!
    2025-07-12T17:50:38.030-05:00 TRACE elf4j.engine.Scratch - It's a beautiful day
    2025-07-12T17:50:38.031-05:00 INFO elf4j.engine.Scratch - ... no matter on what level you say it
    2025-07-12T17:50:38.032-05:00 WARN elf4j.engine.Scratch - Houston, we do not have a problem but let's do a drill
    2025-07-12T17:50:38.032-05:00 ERROR elf4j.engine.Scratch - 
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:50:38.032-05:00 INFO elf4j.engine.Scratch - When being logged, the Throwable always comes first
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:50:38.039-05:00 INFO elf4j.Logger - The log message and arguments work as usual
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:50:38.039-05:00 DEBUG elf4j.engine.Scratch - Not a practical example but now the severity level is DEBUG
   ```
   The output is always asynchronous and won't block the application's normal workflow.
4. The output format patterns can be configured by using a Properties file named `elf4j.properties`, placed in the root
   of the classpath.

   e.g. with the `elf4j.properties` file:

   ```properties
   pattern={timestamp} {level:5} {class:simple}#{method}(L{linenumber}@{filename}) - {message}
   ```

   The output is:

   ```
    2025-07-12T17:53:42.627-05:00 INFO  Scratch#main(L37@Scratch.java) - Hello, world!
    2025-07-12T17:53:42.638-05:00 TRACE Scratch#main(L38@Scratch.java) - It's a beautiful day
    2025-07-12T17:53:42.639-05:00 INFO  Scratch#main(L40@Scratch.java) - ... no matter on what level you say it
    2025-07-12T17:53:42.640-05:00 WARN  Scratch#main(L42@Scratch.java) - Houston, we do not have a problem but let's do a drill
    2025-07-12T17:53:42.640-05:00 ERROR Scratch#main(L44@Scratch.java) - 
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:53:42.640-05:00 INFO  Scratch#main(L45@Scratch.java) - When being logged, the Throwable always comes first
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:53:42.641-05:00 INFO  Logger#log(L169@Logger.java) - The log message and arguments work as usual
    java.lang.Exception: This is a drill
    	at elf4j.engine.Scratch.main(Scratch.java:43)
    
    2025-07-12T17:53:42.641-05:00 DEBUG Scratch#main(L54@Scratch.java) - Not a practical example but now the severity level is DEBUG
   ```

   With the `elf4j.properties` file:

   ```properties
   pattern={json}
   ```

   The output becomes:

   ```
    {"timestamp":"2025-07-12T17:55:31.7475327-05:00","level":"INFO","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"Hello, world!"}
    {"timestamp":"2025-07-12T17:55:31.7566835-05:00","level":"TRACE","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"It's a beautiful day"}
    {"timestamp":"2025-07-12T17:55:31.7576926-05:00","level":"INFO","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"... no matter on what level you say it"}
    {"timestamp":"2025-07-12T17:55:31.7576926-05:00","level":"WARN","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"Houston, we do not have a problem but let's do a drill"}
    {"timestamp":"2025-07-12T17:55:31.7576926-05:00","level":"ERROR","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Scratch.main(Scratch.java:43)\r\n"}
    {"timestamp":"2025-07-12T17:55:31.7576926-05:00","level":"INFO","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"When being logged, the Throwable always comes first","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Scratch.main(Scratch.java:43)\r\n"}
    {"timestamp":"2025-07-12T17:55:31.7586935-05:00","level":"INFO","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"The log message and arguments work as usual","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Scratch.main(Scratch.java:43)\r\n"}
    {"timestamp":"2025-07-12T17:55:31.7586935-05:00","level":"DEBUG","loggerName":"elf4j.engine.Scratch","context":{"ctx-key":"ctx-value"},"message":"Not a practical example but now the severity level is DEBUG"}
   ```

   The JSON pattern can be configured to pretty-print format, and/or mixed with other patterns.

## Features, usage, and configuration details

For using elf4j as a logging facade API, see
elf4j's [API description](https://github.com/elf4j/elf4j#log-service-interface-and-access-api)
and [sample usage](https://github.com/elf4j/elf4j#use-it---for-log-service-api-clients).

For details of using this as a runtime log engine, see [elf4j-provider](https://github.com/elf4j/elf4j-provider)
for [features](https://github.com/elf4j/elf4j-provider#features)
and [configuration](https://github.com/elf4j/elf4j-provider#configuration).

## ["The performance talk"](https://github.com/elf4j/elf4j-provider#performance)

It's not how fast you fill up the target log file or repository, it's how fast you relieve the application from logging
duty back to its own business.
