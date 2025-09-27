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

package elf4j.engine.logging.pattern;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.PerformanceSensitive;
import elf4j.engine.logging.writer.LogEventWriter;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Based on the specifications of a log pattern, a rendering pattern extracts the specified content
 * from the log event, formats and renders such content into the specified text target.
 */
@ThreadSafe
public interface RenderingPattern extends PerformanceSensitive {

  /**
   * Extracts the content of particular interest to this log pattern instance from the specified log
   * event, and appends the result to the specified target aggregator of the final log message.
   *
   * @param logEvent entire log content data source to render
   * @param target logging text aggregator of the final log message
   * @apiNote This method only mutates the specified target based on the content of the specified
   *     logEvent, and does not flush the target to its final logging destination (e.g. the STDOUT
   *     stream or a log file). Shipping the completely rendered target message to the final
   *     destination (e.g. the STDOUT stream, a log file, or aggregation vendors like
   *     Newrelic/Datadog) is the {@link LogEventWriter}'s responsibility.
   */
  void render(LogEvent logEvent, StringBuilder target);
}
