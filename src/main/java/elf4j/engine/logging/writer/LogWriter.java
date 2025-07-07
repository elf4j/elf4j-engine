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

package elf4j.engine.logging.writer;

import elf4j.Level;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.PerformanceSensitive;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An interface representing a log writer responsible for writing log events to an output
 * destination. Implementations of this interface should be thread-safe.
 *
 * @see PerformanceSensitive
 */
@ThreadSafe
public interface LogWriter extends PerformanceSensitive {
  /**
   * Returns the threshold output level for this log writer. Log events with a level lower than the
   * threshold will not be written.
   *
   * @return the threshold output level of this writer
   */
  Level getThresholdOutputLevel();

  /**
   * Writes the given log event to the output destination(s) configured for this log writer.
   *
   * @param logEvent the log data entry to write out
   */
  void write(LogEvent logEvent);
}
