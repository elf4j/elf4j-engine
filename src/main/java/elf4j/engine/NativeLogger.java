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
import elf4j.engine.logging.LogHandler;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * Implemented as an unmodifiable value class. Once fully configured, it's instances are thread-safe
 * and can be safely used as static, instance, or local variables. However, the static factory
 * method {@link Logger#instance()} is more performance-wise expensive to call; it is not
 * recommended for creating local Logger variables. Instances factory methods such as
 * {@link NativeLogger#atLevel(Level)} or {@link Logger#atDebug()}, on the other hand, are
 * inexpensive; they can be used to get any type Logger variables as needed.
 */
@Value
@ThreadSafe
public class NativeLogger implements Logger {
  /**
   * This loggerName field stores the fully qualified class name of the "caller class". The log
   * service properties such as the log level output threshold are configured based on this logger
   * name.
   *
   * <p>In general, there are two types of caller classes of the log service:
   *
   * <ol>
   *   <li>One is the caller class (type-1) of the "service access API" to obtain (gain "access" to)
   *       a reference to the "service class" on which to issue service requests.
   *   <li>The other is the caller class (type-2) that issues log service requests by calling the
   *       "service interface API".
   * </ol>
   *
   * Strictly, this logger name field is the fully-qualified name of the former (type-1) caller
   * class. The value of this field is immutable once an instance of this logger class is
   * constructed. This is the "logger name" that should being printing in the final log message, and
   * may or may not be the type-2 "caller class name" of the same log message.
   *
   * <p>In most cases, the type-1 caller class to the service access API is the same as the type-2
   * caller class to the service interface API. The exceptional case where the caller classes are
   * different would be: The type-1 service access caller class obtains a reference to the log
   * service class ({@code NativeLogger}) instance; then instead of using the reference to issue
   * service calls, it passes the reference out to a different type-2 caller class that subsequently
   * calls the service interface API.
   *
   * <p>In elf4j facade API, the {@link Logger} interface is both the service access API and the
   * service interface API. The sole service access API is the {@link Logger#instance()}) static
   * factory method; and the service interface API includes all instance methods in the same
   * {@link Logger} interface. The service access API is for the client (type-1 caller class) to
   * gain access to a reference of the service interface API implementation. The service interface
   * API is for the client (type-2 caller class) to issue subsequent log service requests.
   *
   * <p>Unlike the service access API which is a static method, the service interface API is defined
   * as instance methods. That means, at runtime, a type-1 service access API caller will be
   * directly calling the {@code Logger} interface; but a type-2 service interface API caller will
   * be calling an instance of the service implementation class, i.e. this {@code NativeLogger}
   * class rather than the {@code Logger} interface itself. That is important to note when trying to
   * detect the runtime type-2 service interface API caller class to print in the final log message.
   *
   * <p>Compared to this immutable logger name (same as the type-1 service access API caller class
   * name), it is performance-wise more expensive to obtain the type-2 service interface API caller
   * class information - including its class name, method name, file name, and file line number -
   * which can be dynamic at run-time and different from the type-1 access API caller class. If
   * performance is of concern, use caution when including such run-time caller details in the
   * output log pattern.
   */
  String loggerName;

  Level level;

  /**
   * The state of this field may change while the log service configuration is in progress. Once
   * fully configured, however, the state doesn't change at runtime, and the instance is
   * thread-safe.
   */
  NativeLoggerFactory nativeLoggerFactory;

  /**
   * Constructs a new instance of the NativeLogger class specifically dedicated to service the
   * specified caller class and at the desired log level.
   *
   * @param loggerName The caller class of this log service call
   * @param level The severity level of this logger instance, matching the desired level of the
   *     logging operation.
   * @param nativeLoggerFactory The log service access point to initialize Logger instances
   */
  NativeLogger(String loggerName, Level level, NativeLoggerFactory nativeLoggerFactory) {
    this.loggerName = loggerName;
    this.level = level;
    this.nativeLoggerFactory = nativeLoggerFactory;
  }

  @Override
  public NativeLogger atLevel(Level level) {
    return this.level == level ? this : this.nativeLoggerFactory.getLogger(level, loggerName);
  }

  @Override
  public Level getLevel() {
    return this.level;
  }

  @Override
  public boolean isEnabled() {
    return getLogHandler().isEnabled(level, loggerName);
  }

  @Override
  public void log(Object message) {
    this.handle(null, message, null);
  }

  @Override
  public void log(String message, Object... arguments) {
    this.handle(null, message, arguments);
  }

  @Override
  public void log(Throwable throwable) {
    this.handle(throwable, null, null);
  }

  @Override
  public void log(Throwable throwable, Object message) {
    this.handle(throwable, message, null);
  }

  @Override
  public void log(Throwable throwable, String message, Object... arguments) {
    this.handle(throwable, message, arguments);
  }

  /**
   * Returns the LogHandler associated with this logger, can be used by other logging frameworks to
   * leverage the underlying logging engine.
   *
   * @return directly accessible log handler
   */
  public LogHandler getLogHandler() {
    return this.nativeLoggerFactory.getLogHandler();
  }

  private void handle(
      @Nullable Throwable throwable, @Nullable Object message, Object @Nullable [] arguments) {
    getLogHandler().log(level, loggerName, throwable, message, arguments);
  }
}
