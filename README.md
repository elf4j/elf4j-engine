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
   class Scratch {
      static Logger logger = Logger.instance();
   
      public static void main(String[] args) {
         logger.log("Hello, world!");
         logger.atInfo().log("It's a beautiful day");
         Logger info = logger.atInfo();
         info.log("... no matter on what level you say it");
         Logger warn = info.atWarn();
         warn.log("Houston, we do not have {} but let's do {}", "a problem", (Supplier) () -> "a drill");
         Throwable exception = new Exception("This is a drill");
         warn.atError().log(exception);
         logger.atInfo()
                 .log(exception,
                         "i.e. Throwable always comes {}, then the {} message and arguments work {}",
                         "first",
                         "optional",
                         (Supplier) () -> "as usual");
         Logger.instance()
                 .atInfo()
                 .atError()
                 .atWarn()
                 .atTrace()
                 .atDebug()
                 .log("Not a practical example but now the severity level is DEBUG");
      }
   }
   ```
   The `Logger` instance is thread-safe, affording flexible usage.
3. Run that application, the follow output will appear in stdout:
   ```
   2023-04-04T09:56:41.688-05:00 INFO Main - Hello, world!
   2023-04-04T09:56:41.693-05:00 TRACE Main - It's a beautiful day
   2023-04-04T09:56:41.693-05:00 INFO Main - ... no matter on what level you say it
   2023-04-04T09:56:41.693-05:00 WARN Main - Houston, we do not have a problem but let's do a drill
   2023-04-04T09:56:41.693-05:00 ERROR Main - 
   java.lang.Exception: This is a drill
       at elf4j.engine.Main.main(Main.java:45)
   
   2023-04-04T09:56:41.693-05:00 INFO Main - i.e. Throwable always comes first, then the following optional message and arguments work as usual
   java.lang.Exception: This is a drill
       at elf4j.engine.Main.main(Main.java:45)
   
   2023-04-04T09:56:41.694-05:00 DEBUG Main - Not a practical example but now the severity level is DEBUG
   ```
   The output is always asynchronous and won't block the application's normal workflow.
4. The output format patterns can be configured by using a Properties file named `elf4j.properties`, placed in the root
   of the classpath.

   e.g. with the `elf4j.properties` file:

   ```properties
   pattern={timestamp} {level:5} {class:full}#{method}(L{linenumber}@{filename}) - {message}
   ```

   The output is:

   ```
   2023-04-04T09:55:04.857-05:00 INFO  elf4j.engine.Main#main(L39@Main.java) - Hello, world!
   2023-04-04T09:55:04.864-05:00 TRACE elf4j.engine.Main#main(L40@Main.java) - It's a beautiful day
   2023-04-04T09:55:04.864-05:00 INFO  elf4j.engine.Main#main(L42@Main.java) - ... no matter on what level you say it
   2023-04-04T09:55:04.864-05:00 WARN  elf4j.engine.Main#main(L44@Main.java) - Houston, we do not have a problem but let's do a drill
   2023-04-04T09:55:04.865-05:00 ERROR elf4j.engine.Main#main(L46@Main.java) - 
   java.lang.Exception: This is a drill
       at elf4j.engine.Main.main(Main.java:45)
   
   2023-04-04T09:55:04.865-05:00 INFO  elf4j.engine.Main#main(L48@Main.java) - i.e. Throwable always comes first, then the following optional message and arguments work as usual
   java.lang.Exception: This is a drill
       at elf4j.engine.Main.main(Main.java:45)
   
   2023-04-04T09:55:04.865-05:00 DEBUG elf4j.engine.Main#main(L59@Main.java) - Not a practical example but now the severity level is DEBUG
   ```

   With the `elf4j.properties` file:

   ```properties
   pattern={json}
   ```

   The output becomes:

   ```
   {"timestamp":"2023-10-07T20:11:44.0345848-05:00","message":"Hello, world!","level":"INFO","callerClass":"elf4j.engine.Main"}
   {"timestamp":"2023-10-07T20:11:44.0375856-05:00","message":"It's a beautiful day","level":"TRACE","callerClass":"elf4j.engine.Main"}
   {"timestamp":"2023-10-07T20:11:44.038585-05:00","message":"... no matter on what level you say it","level":"INFO","callerClass":"elf4j.engine.Main"}
   {"timestamp":"2023-10-07T20:11:44.038585-05:00","message":"Houston, we do not have a problem but let's do a drill","level":"WARN","callerClass":"elf4j.engine.Main"}
   {"timestamp":"2023-10-07T20:11:44.038585-05:00","message":"","level":"ERROR","callerClass":"elf4j.engine.Main","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Main.main(Main.java:43)\r\n"}
   {"timestamp":"2023-10-07T20:11:44.038585-05:00","message":"i.e. Throwable always comes first, then the following optional message and arguments work as usual","level":"INFO","callerClass":"elf4j.engine.Main","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Main.main(Main.java:43)\r\n"}
   {"timestamp":"2023-10-07T20:11:44.038585-05:00","message":"Not a practical example but now the severity level is DEBUG","level":"DEBUG","callerClass":"elf4j.engine.Main"}
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
