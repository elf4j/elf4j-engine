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

public record ClassElement(ClassElement.DisplayOption classDisplayOption)
    implements PatternElement {
  private static final DisplayOption DEFAULT_DISPLAY_OPTION = DisplayOption.SIMPLE;

  /**
   * @param patternElement text patternElement to convert
   * @return converted patternElement object
   */
  public static ClassElement from(String patternElement) {
    return new ClassElement(
        PredefinedPatternElementType.getPatternElementDisplayOptions(patternElement)
            .map(Iterables::getOnlyElement)
            .map(o -> DisplayOption.valueOf(o.toUpperCase()))
            .orElse(DEFAULT_DISPLAY_OPTION));
  }

  /**
   * @return <code>false</code> assuming the logger's declaring class is the same as the caller
   *     class. Therefore, unlike the {@link MethodElement}, it does not take a stack trace walk to
   *     locate the caller class - the declaring class is taken instead.
   */
  @Override
  public boolean includeCallerDetail() {
    return false;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    String fullName = logEvent.getCallerClassName();
    switch (classDisplayOption) {
      case FULL -> target.append(fullName);
      case SIMPLE -> target.append(fullName.substring(fullName.lastIndexOf('.') + 1));
      case COMPRESSED -> target.append(getCompressedName(fullName));
      default -> throw new IllegalArgumentException("class display option: " + classDisplayOption);
    }
  }

  private static StringBuilder getCompressedName(String fullName) {
    var compressedName = new StringBuilder();
    var tokens = fullName.split("\\.");
    var simpleName = tokens[tokens.length - 1];
    for (var i = 0; i < tokens.length - 1; i++) {
      compressedName.append(tokens[i].charAt(0)).append('.');
    }
    compressedName.append(simpleName);
    return compressedName;
  }

  enum DisplayOption {
    FULL,
    SIMPLE,
    COMPRESSED
  }
}
