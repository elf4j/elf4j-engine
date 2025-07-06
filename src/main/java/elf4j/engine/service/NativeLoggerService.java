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

package elf4j.engine.service;

import elf4j.engine.NativeLogger;
import org.jspecify.annotations.Nullable;

/**
 * The NativeLoggerService interface is a part of the ELF4J logging service. It provides methods for
 * checking if a logger is enabled and for logging a message with the specified logger, service
 * interface class, throwable, message, and arguments.
 */
public interface NativeLoggerService extends PerformanceSensitive {
  /**
   * Checks if the logger's level is at or above the configured threshold.
   *
   * @param nativeLogger the logger to check for enablement
   * @return true if the logger's level is at or above the configured threshold, false otherwise
   */
  boolean isEnabled(NativeLogger nativeLogger);

  /**
   * Logs a message with the specified logger, service interface class, throwable, message, and
   * arguments.
   *
   * @param nativeLogger the serviced logger
   * @param serviceInterfaceClass The concrete logging service (logger) implementation class that
   *     the client calls directly at runtime to make log requests. For the native ELF4J service
   *     implementation, this is always the {@link NativeLogger} class; may be a different class if
   *     this core library is used to service other logging API. i.e. the real-time caller of this
   *     class is the logging service's "caller class" whose details (such as method and line
   *     number) if required by configuration, may need to be resolved by walking the runtime
   *     calling stack trace.
   * @param throwable to log
   * @param message to log, can have argument placeholders to be replaced by the values of the
   *     specified arguments
   * @param arguments arguments whose values will replace the placeholders in the specified message
   */
  void log(
      NativeLogger nativeLogger,
      Class<?> serviceInterfaceClass,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments);
}
