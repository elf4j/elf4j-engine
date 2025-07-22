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

package elf4j.engine.logging;

import elf4j.engine.NativeLogger;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** The LogHandler processes the logging operations delegated from the Logger. */
public interface LogHandler {
  /**
   * @return true if the logger instance's severity level is at or above the configured threshold
   *     for the specified logger id, false otherwise
   */
  boolean isEnabled(NativeLogger.LoggerId loggerId);

  /**
   * Service a log operation at the specified level, for the specified caller class
   *
   * @param loggerId the logger id to look up configurations for
   * @param logServiceClassNames the runtime concrete implementation class name(s) of the log
   *     service API. In elf4j-engine, they are those of the {@link NativeLogger} class that is an
   *     implementation of the service interface API, as well as the {@link elf4j.Logger} interface
   *     because its default methods also implement the service interface API and are called
   *     directly by the client code.
   * @param throwable to log
   * @param message to log, can have argument placeholders to be replaced by the values of the
   *     specified arguments
   * @param arguments arguments whose values will replace the placeholders in the specified message
   */
  void log(
      NativeLogger.LoggerId loggerId,
      Set<String> logServiceClassNames,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments);
}
