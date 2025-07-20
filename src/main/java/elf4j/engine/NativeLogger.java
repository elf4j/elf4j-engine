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
import javax.annotation.concurrent.ThreadSafe;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * Implemented as an unmodifiable value class. Once fully configured, it's instances are thread-safe
 * and can be safely used as static, instance, or local variables. However, getting an instance of
 * this logger class via the static factory method {@link Logger#instance()} is more
 * performance-wise expensive to call; it is recommended using it on static variables, and not local
 * variables. Instances factory methods such as {@link NativeLogger#atLevel(Level)} or
 * {@link Logger#atDebug()}, on the other hand, are inexpensive; they can be used to get any type
 * Logger variables as needed.
 */
@ThreadSafe
@Value
public class NativeLogger implements Logger {
  private static final Class<NativeLogger> LOG_SERVICE_CLASS = NativeLogger.class;

  LoggerId loggerId;
  LogHandlerFactory logHandlerFactory;

  /**
   * Constructs a new instance of the NativeLogger class specifically dedicated to service the
   * specified caller class and at the desired log level.
   *
   * @param loggerId the logger id to look up configurations for
   * @param logHandlerFactory the log handler factory to use for this logger instance
   */
  NativeLogger(LoggerId loggerId, LogHandlerFactory logHandlerFactory) {
    this.loggerId = loggerId;
    this.logHandlerFactory = logHandlerFactory;
  }

  @Override
  public NativeLogger atLevel(Level level) {
    return loggerId.level() == level
        ? this
        : new NativeLogger(loggerId.toBuilder().level(level).build(), logHandlerFactory);
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
    this.process(null, message, null);
  }

  @Override
  public void log(String message, Object... arguments) {
    this.process(null, message, arguments);
  }

  @Override
  public void log(Throwable throwable) {
    this.process(throwable, null, null);
  }

  @Override
  public void log(Throwable throwable, Object message) {
    this.process(throwable, message, null);
  }

  @Override
  public void log(Throwable throwable, String message, Object... arguments) {
    this.process(throwable, message, arguments);
  }

  private void process(
      @Nullable Throwable throwable, @Nullable Object message, Object @Nullable [] arguments) {
    handle(LOG_SERVICE_CLASS, throwable, message, arguments);
  }

  /**
   * Public API other logging frameworks can use.
   *
   * @param logServiceClass the concrete runtime implementation class the log framework API provides
   *     for the client code to issue log service requests. In this case, it is always this
   *     {@link NativeLogger} class rather than the {@link Logger} interface.
   * @param throwable to log
   * @param message to log
   * @param arguments to log
   */
  public void handle(
      Class<?> logServiceClass,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments) {
    logHandlerFactory.getLogHandler().log(logServiceClass, loggerId, throwable, message, arguments);
  }

  /**
   * Although the logger's ID includes both the name and severity level of the logger, only the
   * logger name is used to configure the logger's minimum output level. Only when the logger's
   * severity level is equal or greater than the configured minimum level, will this logger's
   * messages eventually print out.
   *
   * @param loggerName This loggerName field stores the fully qualified class name of the "caller
   *     class". The minimum output threshold level is configured based on this logger name.
   *     <p>In general, there are two types of client "caller classes" of the log service:
   *     <ol>
   *       <li>One is the caller class (type-1) of the "service access API". The purpose of this
   *           call is to obtain (gain "access" to) a reference to the "service class", where
   *           subsequent log service requests can be issued.
   *       <li>The other is the caller class (type-2) of the "service interface API". The purpose of
   *           this caller is to issue log service requests to "service class" which is the concrete
   *           implementation of the service interface API.
   *     </ol>
   *     Strictly, this logger name field is the fully-qualified name of the former (type-1) caller
   *     class. This field will end up being the "logger" value printed in the final log message,
   *     and may or may not be the "class" value (i.e. the type-2 caller class) of the same log
   *     message.
   *     <p>In most cases, the type-1 caller class to the service access API is the same as the
   *     type-2 caller class to the service interface API. The exceptional case where the caller
   *     classes are different would be: The type-1 service access API caller class obtains a
   *     reference to the log service class ({@code NativeLogger}) instance; then instead of using
   *     the reference to issue service calls, it passes the reference out to a different type-2
   *     caller class that subsequently calls the service interface API.
   *     <p>In the final log message pattern, "logger" is the class name of the type-1 client
   *     caller, "class" is the class name of the type-2 client caller.
   *     <p>In elf4j facade API, the {@link Logger} interface is both the service access API and the
   *     service interface API. The sole service access API is the {@link Logger#instance()}) static
   *     factory method; and the service interface API includes all instance methods in the same
   *     {@link Logger} interface. The service access API is for the client (type-1 caller class) to
   *     gain access to a reference of the service interface API implementation. The service
   *     interface API is for the client (type-2 caller class) to issue subsequent log service
   *     requests.
   *     <p>Unlike the service access API which is a static method, the service interface API is
   *     defined as instance methods. That means, at runtime, a type-1 caller will be directly
   *     calling the {@code Logger} interface; but a type-2 caller will be calling an instance of
   *     the concrete implementation class, i.e. the {@code NativeLogger} class rather than the
   *     {@code Logger} interface itself. That is important to note when trying to detect the
   *     runtime type-2 caller class to print as "class" in the final log message.
   *     <p>Compared to this immutable logger name (the type-1 caller class name), it is
   *     performance-wise more expensive to obtain the type-2 caller class information - including
   *     its class name, method name, file name, and file line number - which can be dynamic at
   *     run-time and different from the type-1 caller class. If performance is of concern, use
   *     caution when including such run-time caller details in the output log pattern.
   * @param level the severity level of this logger instance
   */
  @Builder(toBuilder = true)
  public record LoggerId(String loggerName, Level level) {}
}
