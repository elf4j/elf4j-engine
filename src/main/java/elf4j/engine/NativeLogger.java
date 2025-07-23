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

import elf4j.Level;
import elf4j.Logger;
import elf4j.engine.logging.LogHandlerFactory;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Implemented as an unmodifiable value class. Once fully configured, its instances are thread-safe
 * and can be used as static, instance, or local variables. However, an instance from the static
 * factory method {@link Logger#instance()} is more performance-wise expensive; it is recommended
 * for static variables, and not local variables. By contrast, an instance from an instance factory
 * method such as {@link NativeLogger#atLevel(Level)} or {@link Logger#atDebug()} is inexpensive to
 * obtain; it can be used for variables of any scope.
 */
@ThreadSafe
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NativeLogger implements Logger {
  /**
   * {@link Logger} is also an "implementation" of the log service interface API because of the
   * default methods.
   */
  private static final Set<String> LOG_SERVICE_CLASS_NAMES =
      Set.of(NativeLogger.class.getName(), Logger.class.getName());

  static final String DEFAULT_THROWABLE_MESSAGE = "";

  @Getter
  @EqualsAndHashCode.Include
  private final LoggerId loggerId;

  private final LogHandlerFactory logHandlerFactory;

  /**
   * Constructs a new instance of the NativeLogger class specifically dedicated to service the
   * specified caller class and at the desired severity level.
   *
   * @param loggerId the logger loggerId to look up configurations for
   * @param logHandlerFactory the access API to the log handler service
   */
  NativeLogger(LoggerId loggerId, LogHandlerFactory logHandlerFactory) {
    this.loggerId = loggerId;
    this.logHandlerFactory = logHandlerFactory;
  }

  @Override
  public NativeLogger atLevel(Level level) {
    return loggerId.level == level
        ? this
        : new NativeLogger(new LoggerId(loggerId.loggerName, level), logHandlerFactory);
  }

  @Override
  public Level getLevel() {
    return loggerId.level;
  }

  @Override
  public boolean isEnabled() {
    return logHandlerFactory.getLogHandler().isEnabled(loggerId);
  }

  @Override
  public void log(Object message) {
    process(LOG_SERVICE_CLASS_NAMES, null, message, null);
  }

  @Override
  public void log(String message, Object... arguments) {
    process(LOG_SERVICE_CLASS_NAMES, null, message, arguments);
  }

  @Override
  public void log(Throwable throwable) {
    process(LOG_SERVICE_CLASS_NAMES, throwable, DEFAULT_THROWABLE_MESSAGE, null);
  }

  @Override
  public void log(Throwable throwable, Object message) {
    process(LOG_SERVICE_CLASS_NAMES, throwable, message, null);
  }

  @Override
  public void log(Throwable throwable, String message, Object... arguments) {
    process(LOG_SERVICE_CLASS_NAMES, throwable, message, arguments);
  }

  /**
   * Public API in addition to the [Logger] interface
   *
   * @param logServiceClassNames the concrete runtime implementation class name(s) that the logging
   *     framework provides for the client code to call the service interface API at runtime. In
   *     this case, it contains this [NativeLogger] class which implements the service interface
   *     API, as well as the [Logger] interface itself because its default methods are also
   *     implementations directly called by the client code.
   * @param throwable to log
   * @param message to log
   * @param arguments to log
   * @apiNote Used by elf4j-engine internally, not meant for direct usage by client code. Made
   *     public for potential internal usage of other logging frameworks.
   */
  public void process(
      Set<String> logServiceClassNames,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments) {
    logHandlerFactory
        .getLogHandler()
        .log(loggerId, logServiceClassNames, throwable, message, arguments);
  }

  /**
   * Although the logger's ID includes both the name and severity level of the logger, only the
   * logger name is used to configure the logger's minimum output level. Only when the logger's
   * severity level is equal or greater than the configured minimum level, will this logger's
   * messages eventually print out.
   *
   * <p>In general, there are two types of client "caller classes" of the log service:
   *
   * <p>1. One (type-1) is the caller class of the "service access API", calling to obtain (gain
   * "access" to) a "service class" reference on which subsequent log service requests can be
   * issued.
   *
   * <p>2. The other (type-2) is the caller class of the "service interface API", calling to issue
   * log service requests to the "service class" which is the concrete implementation of the service
   * interface API.
   *
   * <p>In the final log message, "logger" is the name of the type-1 caller class; "class" is the
   * name of the type-2 caller class.
   *
   * <p>In this implementation, the logger name is the fully-qualified name of the type-1 caller
   * class, so it will end up being the "logger" value of the final log message. Meanwhile, it may
   * not be the "class" value (i.e. the type-2 caller class) of the same log message.
   *
   * <p>Most commonly, though, the type-1 caller class to the service access API is the same as the
   * type-2 caller class to the service interface API. The exceptional case where the caller classes
   * are different would be: The type-1 caller class first calls the service access class/API to get
   * a reference to the service interface API/class instance; then instead of using the reference to
   * call the log services, it passes the reference out to a (different) type-2 caller class that
   * subsequently calls the service interface API.
   *
   * <p>In the elf4j facade API, the [Logger] interface is both the service access API and the
   * service interface API. The sole service access API is the static factory method
   * [Logger#instance()], and the service interface API includes all instance methods in the same
   * [Logger] interface. That means, at runtime, a type-1 caller will be directly calling the
   * `Logger` interface; but a type-2 caller will be calling an instance of the concrete
   * implementation class, i.e. either the `NativeLogger` class, or the `Logger` interface (default
   * methods). That is important to note when trying to detect the runtime type-2 caller class to
   * print as "class" in the final log message.
   *
   * <p>Compared to the immutable type-1 caller class name (i.e. the logger name), it is
   * performance-wise more expensive to obtain the type-2 caller class information - including its
   * class name, method name, file name, and file line number - which can be dynamic at run-time and
   * different from the type-1 caller class. If performance is of concern, use caution when
   * including such run-time caller details in the output log pattern.
   *
   * @param loggerName This field stores the fully qualified name of the client code "caller class"
   *     to the service access API. The minimum output threshold level is configured based on this
   *     logger name.
   * @param level the severity level of this logger instance
   * @implNote The logger name in the id determines the minimum threshold output level configured
   *     for all Logger instances of the same name. Given such configured threshold level, the
   *     instance's id (name and level combined) determines if messages from this Logger instance
   *     will ultimately print: Only when the instance level in the id is equal or greater than the
   *     threshold configured for that logger name, will the message print.
   */
  public record LoggerId(String loggerName, Level level) {}
}
