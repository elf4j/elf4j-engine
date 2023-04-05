# elf4j-engine

[![Maven Central](https://img.shields.io/maven-central/v/io.github.elf4j/elf4j-engine.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.elf4j%22%20AND%20a:%22elf4j-engine%22)

Asynchronous Java log engine implementing the [ELF4J](https://github.com/elf4j/elf4j) (Easy Logging Facade for Java )
API.

Naturally, this engine is packaged by [elf4j-provider](https://github.com/elf4j/elf4j-provider) to
make a native logging _service provider_ of the [ELF4J](https://github.com/elf4j/) logging facade.

Meanwhile, this is also a stand-alone log engine, designed to be adaptable for servicing other Java logging APIs. For
example, it is a log engine for the [SLF4J](https://www.slf4j.org/) API, as
in [slf4j-elf4j](https://github.com/elf4j/slf4j-elf4j); it is also a log engine for the Java Platform
Logging ([JPL](https://openjdk.org/jeps/264)) API introduced since Java 9, as
in [jpl-elf4j](https://github.com/elf4j/jpl-elf4j).

## Getting Started...

1. Install

   For log service client applications, the elf4j-engine is wrapped as a runtime dependency of elf4j-provider. See
   installation details [here](https://github.com/elf4j/elf4j-provider#installation), and use the latest versions from
   Maven Central.
2. Use it for logging in the application:
   ```java
   import elf4j.Logger;
   
   import java.util.function.Supplier;
   
   class Scratch {
      static Logger logger = Logger.instance();
   
      public static void main(String[] args) {
         logger.log("Hello, world!");
         logger.atInfo().log("It's a beautiful day");
         Logger info = logger.atInfo();
         info.log("... no matter how you say it");
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
   writer=standard
   writer.pattern={timestamp} {level:5} {class:full}#{method}(L{linenumber}@{filename}) - {message}
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
   writer=standard
   writer.pattern={json}
   ```

   The output becomes:

   ```
   {"timestamp":"2023-04-04T09:58:18.6232798-05:00","level":"INFO","callerClass":"elf4j.engine.Main","message":"Hello, world!"}
   {"timestamp":"2023-04-04T09:58:18.6542784-05:00","level":"TRACE","callerClass":"elf4j.engine.Main","message":"It\u0027s a beautiful day"}
   {"timestamp":"2023-04-04T09:58:18.6542784-05:00","level":"INFO","callerClass":"elf4j.engine.Main","message":"... no matter on what level you say it"}
   {"timestamp":"2023-04-04T09:58:18.6542784-05:00","level":"WARN","callerClass":"elf4j.engine.Main","message":"Houston, we do not have a problem but let\u0027s do a drill"}
   {"timestamp":"2023-04-04T09:58:18.6542784-05:00","level":"ERROR","callerClass":"elf4j.engine.Main","message":"","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Main.main(Main.java:45)\r\n"}
   {"timestamp":"2023-04-04T09:58:18.655279-05:00","level":"INFO","callerClass":"elf4j.engine.Main","message":"i.e. Throwable always comes first, then the following optional message and arguments work as usual","exception":"java.lang.Exception: This is a drill\r\n\tat elf4j.engine.Main.main(Main.java:45)\r\n"}
   {"timestamp":"2023-04-04T09:58:18.655279-05:00","level":"DEBUG","callerClass":"elf4j.engine.Main","message":"Not a practical example but now the severity level is DEBUG"}
   ```

   The JSON pattern can be configured to pretty-print format, and/or mixed with other patterns.

## Features, Usage, and Configuration Details

For using ELF4J as a logging facade API, see
ELF4J's [API description](https://github.com/elf4j/elf4j#log-service-interface-and-access-api)
and [sample usage](https://github.com/elf4j/elf4j#use-it---for-log-service-api-clients).

For details of using this as a runtime log engine, see [elf4j-provider](https://github.com/elf4j/elf4j-provider)
for [features](https://github.com/elf4j/elf4j-provider#features)
and [configuration](https://github.com/elf4j/elf4j-provider#configuration).

## "The Performance Talk"

It's less about how fast you can fill up the log file/repository, and more about how fast you can relieve the
application from logging duty back to its normal workflow.
