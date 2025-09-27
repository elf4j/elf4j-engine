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

package elf4j.engine.logging.pattern.element;

import com.google.common.collect.MoreCollectors;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.Arrays;
import java.util.Objects;

record ThreadPattern(DisplayOption threadDisplayOption) implements RenderingPattern {
  /**
   * @param elementPattern text pattern element to convert. E.g. "{thread}", "{Thread:id}", or
   *     "{THREAD:name}", excluding the surrounding braces
   * @return the thread pattern element converted from the specified text
   */
  static ThreadPattern from(String elementPattern) {
    if (PatternElementType.THREAD != PatternElementType.from(elementPattern)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", elementPattern));
    }
    return new ThreadPattern(
        ElementPatterns.getElementPatternDisplayOptions(elementPattern).stream()
            .collect(MoreCollectors.toOptional())
            .map(DisplayOption::from)
            .orElse(DisplayOption.NAME));
  }

  @Override
  public boolean requiresCallerDetail() {
    return false;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    LogEvent.CallerThreadValue callerThread = Objects.requireNonNull(logEvent.callerThread());
    target.append(
        threadDisplayOption == DisplayOption.ID ? callerThread.id() : callerThread.name());
  }

  enum DisplayOption {
    ID,
    NAME;

    public static DisplayOption from(String displayOption) {
      return Arrays.stream(values())
          .filter(o -> o.name().equalsIgnoreCase(displayOption))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              "Unknown thread display option: %s. Valid options are: %s"
                  .formatted(displayOption, Arrays.toString(values()))));
    }
  }
}
