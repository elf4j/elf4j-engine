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

package elf4j.engine.logging.pattern.predefined;

import com.google.common.collect.Iterables;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.PatternElement;
import elf4j.engine.logging.pattern.PredefinedPatternElementType;
import java.util.Arrays;
import java.util.Objects;

public record ThreadElement(DisplayOption threadDisplayOption) implements PatternElement {
  /**
   * @param patternElement text pattern element to convert
   * @return the thread pattern element converted from the specified text
   */
  public static ThreadElement from(String patternElement) {
    return new ThreadElement(
        PredefinedPatternElementType.getPatternElementDisplayOptions(patternElement)
            .map(Iterables::getOnlyElement)
            .map(DisplayOption::from)
            .orElse(DisplayOption.NAME));
  }

  @Override
  public boolean includeCallerDetail() {
    return false;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    LogEvent.ThreadValue callerThread = Objects.requireNonNull(logEvent.getCallerThread());
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
