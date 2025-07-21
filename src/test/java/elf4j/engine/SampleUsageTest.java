/*
 * MIT License
 *
 * Copyright (c) 2023 Qingtian Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;

import elf4j.Level;
import elf4j.Logger;
import elf4j.engine.logging.util.ShutdownUnitTestExtension;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ShutdownUnitTestExtension.class)
class SampleUsageTest {
  static Logger logger = Logger.instance();

  @Nested
  class plainText {
    @Test
    void declarationsAndLevels() {
      logger.log(
          "Logger instance is thread-safe so it can be declared and used as a local, instance, or static variable; for best performance, static variables are preferred, and local variables are not recommended");
      logger.log(
          "Default severity level is decided by this logging provider implementation: {}",
          logger.getLevel());
      Logger trace = logger.atTrace();
      trace.log("Explicit severity level is specified by user i.e. TRACE");
      Logger.instance().atTrace().log("Same explicit level TRACE, calling static access API in method local scope is not recommended for performance");
      logger.atDebug().log("Severity level is DEBUG");
      logger.atInfo().log("Severity level is INFO");
      trace.atWarn().log("Severity level is WARN, not TRACE");
      logger.atError().log("Severity level is ERROR");
      Logger.instance()
          .atDebug()
          .atError()
          .atTrace()
          .atWarn()
          .atInfo()
          .log("Not a practical example but the severity level is INFO");
    }

    @Test
    void convenienceShorthands() {
      var message = "Test log message";
      logger.atInfo().log(message + " can be shorthanded");
      logger.info(message);
      var debugLogger = logger.atDebug();
      assertTrue(
          debugLogger.isDebugEnabled(),
          "Assuming minimum severity level configured is TRACE, thus DEBUG level should be enabled");
      if (debugLogger.isDebugEnabled()) {
        debugLogger.debug(
            "The DEBUG level log message '{}' will print here as the default minimum writer threshold level is {}",
            message,
            Level.TRACE);
      }
    }
  }

  @Nested
  class textWithArguments {
    Logger info = logger.atInfo();

    @Test
    void lazyAndEagerArgumentsCanBeMixed() {
      info.log("Message can have any number of arguments of {} type", Object.class.getTypeName());
      info.log(
          "Lazy arguments, of {} type, whose values may be {} can be mixed with eager arguments of non-Supplier types",
          Supplier.class.getTypeName(),
          (Supplier<?>) () -> "expensive to compute");
      info.atWarn()
          .log(
              "The Supplier downcast is mandatory per lambda syntax because arguments are declared as generic Object rather than functional interface");
    }
  }

  @Nested
  class throwable {
    @Test
    void asTheFirstArgument() {
      Exception exception = new Exception("Exception message");
      logger.atError().log(exception);
      logger.atError().log(exception, "Optional log message");
      logger.atInfo().log(
          exception,
          "Exception is always the first argument to a logging method. The {} log message and following arguments work the same way {}.",
          "optional",
          (Supplier<?>) () -> "as usual");
    }
  }
}
