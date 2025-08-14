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

import static elf4j.engine.logging.pattern.ElementType.CLASS;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.ElementType;
import elf4j.engine.logging.pattern.PatternElement;
import lombok.Value;

public @Value class ClassElement implements PatternElement {
  NameSpaceElement nameSpaceElement;

  public static ClassElement from(String patternElement) {
    if (CLASS != ElementType.from(patternElement)) {
      throw new IllegalArgumentException(
          "Unexpected predefined pattern element: %s".formatted(patternElement));
    }
    return new ClassElement(NameSpaceElement.from(patternElement, CLASS));
  }

  @Override
  public boolean includeCallerDetail() {
    return nameSpaceElement.includeCallerDetail();
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    nameSpaceElement.render(logEvent, target);
  }
}
