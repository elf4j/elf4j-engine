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
import org.jspecify.annotations.Nullable;

/** Performs the logging operations delegated from the Logger */
public interface LogHandler {
  /**
   * @return true if the logger instance's severity level is at or above the configured minimum
   *     threshold for the specified logger, false otherwise
   */
  boolean isEnabled(NativeLogger.LoggerId loggerId);

  /**
   * Service a log operation at the specified severity level for the specified logger.
   *
   * @param loggerId the logger ID (containing the logger name and severity) used to resolve
   *     configuration
   * @param throwable to log
   * @param message to log, can have argument placeholders to be replaced by the values of the
   *     specified arguments
   * @param arguments arguments whose values will replace the placeholders in the specified message
   * @apiNote Meant to be called in the same caller thread itself, not a different e.g. asynchronous
   *     processing thread
   */
  void log(
      NativeLogger.LoggerId loggerId,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments);
}
