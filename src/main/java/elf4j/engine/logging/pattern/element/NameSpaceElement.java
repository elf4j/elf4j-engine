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
import elf4j.engine.logging.pattern.ElementType;
import elf4j.engine.logging.pattern.PatternElement;
import java.util.Objects;
import lombok.Value;

@Value
class NameSpaceElement implements PatternElement {
  private static final DisplayOption DEFAULT_DISPLAY_OPTION = DisplayOption.FULL;

  ElementType targetElementType;
  DisplayOption displayOption;

  /**
   * @param patternElement text patternElement to convert
   * @return converted patternElement object
   */
  public static NameSpaceElement from(String patternElement, ElementType targetElementType) {
    ElementType type = ElementType.from(patternElement);
    if (type != ElementType.CLASS && type != ElementType.LOGGER) {
      throw new IllegalArgumentException(
          "Unexpected predefined pattern element: %s".formatted(patternElement));
    }
    return new NameSpaceElement(
        targetElementType,
        ElementType.getElementDisplayOptions(patternElement).stream()
            .collect(MoreCollectors.toOptional())
            .map(name -> DisplayOption.valueOf(name.toUpperCase()))
            .orElse(DEFAULT_DISPLAY_OPTION));
  }

  @Override
  public boolean includeCallerDetail() {
    return switch (targetElementType) {
      case CLASS -> true;
      case LOGGER -> false;
      default ->
        throw new IllegalArgumentException(
            "Unexpected name space element type: " + targetElementType);
    };
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    String fullName =
        switch (targetElementType) {
          case LOGGER -> logEvent.getLoggerName();
          case CLASS -> Objects.requireNonNull(logEvent.getCallerFrame()).getClassName();
          default ->
            throw new IllegalStateException(
                "Unexpected name space element type: " + targetElementType);
        };
    switch (displayOption) {
      case FULL -> target.append(fullName);
      case SIMPLE -> target.append(fullName.substring(fullName.lastIndexOf('.') + 1));
      case COMPRESSED -> target.append(getCompressedName(fullName));
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
