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
import org.jspecify.annotations.Nullable;

/**
 * Any instance of this class is thread-safe; it can be safely used as static, instance, or local
 * variables. However, instances returned by the static factory method {@link Logger#instance()} are
 * more expensive to create; it is not recommended to use them as local variables. Instances
 * obtained from other (instance factory) methods are less expensive; they can be used in any way as
 * needed.
 *
 * <p>Also, it should always be the same caller class that first calls the log service access API
 * {@link Logger#instance()} to initialize a Logger instance, and then use the instance's service
 * interface API such as the {@link Logger#log(Object)} method to perform log operations. In other
 * words, it is considered a programming error to declare/initiate a logger instance in one class
 * and pass such instance out for another (caller) class to call the instance's log API.
 */
@ThreadSafe
public class NativeLogger implements Logger {
  /**
   * This loggerName field stores the fully qualified class name of the caller that invokes the log
   * "server access API" {@link Logger#instance()}) to create and obtain a reference to this
   * NativeLogger instance. Strictly speaking, it is the name of the caller class to the service
   * access API. It happens in this implementation, it is also the caller class of the "service
   * interface API".
   *
   * <p>In general, there are two types of "caller" classes of the log service:
   *
   * <ol>
   *   <li>one is the caller class of the "service access API" (in this implementation, the access
   *       API is {@link Logger#instance()}) to obtain (gain "access" to) a reference to the
   *       "service interface" (the "logger")
   *   <li>the other is the caller class of the "service interface API" (in this implementation, the
   *       interface API is methods like {@link Logger#log(Object)}) to issue log service requests.
   * </ol>
   *
   * In most cases, though, the "caller class" of the service access API is the same as that of the
   * service interface API. The only exception would be: The service access caller class initializes
   * a log service (i.e. the {@link Logger}) instance; then instead of using the instance to call
   * the service interface API by itself, it passes the Logger reference out to a (different)
   * service interface caller class that calls the service interface API.
   *
   * <p>Compared to the logger name (same as the "access API caller class" name), it is
   * performance-wise more expensive to obtain more detailed caller information - class name of the
   * interface API caller (even when it is different from the access API caller), method name, file
   * name, and file line number. If performance is of concern, caution is recommended when including
   * caller detail in the output log pattern.
   */
  private final String loggerName;

  private final Level level;
  private final NativeLogServiceProvider nativeLogServiceProvider;

  /**
   * Constructs a new instance of the NativeLogger class specifically dedicated to service the
   * specified caller class and at the desired log level.
   *
   * @param loggerName The caller class of this log service call
   * @param level The severity level of this logger instance, matching the desired level of the
   *     logging operation.
   * @param nativeLogServiceProvider The log service access point to initialize Logger instances
   */
  public NativeLogger(
      String loggerName, Level level, NativeLogServiceProvider nativeLogServiceProvider) {
    this.loggerName = loggerName;
    this.level = level;
    this.nativeLogServiceProvider = nativeLogServiceProvider;
  }

  @Override
  public NativeLogger atLevel(Level level) {
    return this.level == level ? this : this.nativeLogServiceProvider.getLogger(level, loggerName);
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
    return this.nativeLogServiceProvider.getLogHandler();
  }

  /**
   * Returns the caller class of the log service API
   *
   * @return declaring/caller class of this logger instance
   */
  public String getLoggerName() {
    return this.loggerName;
  }

  private void handle(
      @Nullable Throwable throwable, @Nullable Object message, Object @Nullable [] arguments) {
    getLogHandler().log(level, loggerName, throwable, message, arguments);
  }
}
