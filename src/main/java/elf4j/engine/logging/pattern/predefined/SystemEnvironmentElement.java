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

public record SystemEnvironmentElement(String key) implements PatternElement {
  /**
   * @param patternElement text patternElement to convert
   * @return converted patternElement object
   */
  public static SystemEnvironmentElement from(String patternElement) {
    if (!PredefinedPatternElementType.SYS_ENV.matchesTypeOf(patternElement)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", patternElement));
    }
    return new SystemEnvironmentElement(Iterables.getOnlyElement(
        PredefinedPatternElementType.getPatternElementDisplayOptions(patternElement)));
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    target.append(System.getenv(key));
  }

  @Override
  public boolean includeCallerDetail() {
    return false;
  }
}
