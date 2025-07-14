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
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Any instance of this class is thread-safe; it can be safely used as static, instance, or local
 * variables. However, instances returned by the static factory method {@link Logger#instance()} are
 * more expensive to create; it is not recommended to use them as local variables. Instances
 * obtained from other (instance factory) methods are less expensive; they can be used in any way as
 * needed.
 */
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
   *   <li>One is the caller class of the "service access API" to obtain (gain "access" to) a
   *       reference to the "service class" on which to issue service requests. The concrete
   *       implementation of the service access API is called the "provider class".
   *   <li>The other is the caller class of the "service interface API" to issue log service
   *       requests. The concrete implementation of the service interface API is called the "service
   *       class".
   * </ol>
   *
   * <p>In case of the elf4j API, the {@link Logger} interface is both the service access API and
   * the service interface API. The sole access API is the {@link Logger#instance()}) static method;
   * the service interface API includes instance methods like {@link Logger#log(Object)}). The
   * service access API (provider class) is for the log service client/caller to gain access to a
   * reference of the service interface API (service class). A service interface API caller (often
   * the same caller of the service access API) can then use the reference to issue subsequent
   * service requests to the runtime instance of the service class.
   *
   * <p>Unlike the service access API which is a static method on the {@code Logger} interface, all
   * methods of the service interface API are instance methods. That means, at runtime, the service
   * interface API caller will be calling the instance of this {@code NativeLogger} service class
   * (rather than the {@code Logger} interface itself) for all log service requests. That is
   * important to note when trying to detect the runtime service interface API caller class.
   *
   * <p>In most cases, the "caller class" of the service access API is the same as that of the
   * service interface API. The only exception where the caller classes are different would be: The
   * service access caller class obtains a reference to the log service class ({@code NativeLogger})
   * instance; then instead of using the reference to call the service interface API, it passes the
   * reference out to a different caller class that subsequently calls the service interface API.
   *
   * <p>Compared to the statically stored logger name (same as the "service access API caller class"
   * name), it is performance-wise more expensive to obtain the (run-time) "service interface API
   * caller class" information - class name (can be different from the access API caller), method
   * name, file name, and file line number. If performance is of concern, use caution when including
   * such run-time caller details in the output log pattern.
   */
  @Getter
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

  private void handle(
      @Nullable Throwable throwable, @Nullable Object message, Object @Nullable [] arguments) {
    getLogHandler().log(level, loggerName, throwable, message, arguments);
  }
}
