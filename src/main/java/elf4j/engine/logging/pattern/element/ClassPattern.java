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

import static elf4j.engine.logging.pattern.element.PatternElementType.CLASS;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.RenderingPattern;

record ClassPattern(NameSpacePattern nameSpacePattern) implements RenderingPattern {
  /**
   * @param elementPattern the pattern element string, e.g. "{class}", "{class:full}",
   *     "{class:compressed}", "{class:simple}", excluding the surrounding braces
   * @return the corresponding {@link ClassPattern} instance
   */
  static ClassPattern from(String elementPattern) {
    if (CLASS != PatternElementType.from(elementPattern)) {
      throw new IllegalArgumentException(
          "Unexpected predefined pattern element: %s".formatted(elementPattern));
    }
    return new ClassPattern(NameSpacePattern.from(elementPattern, CLASS));
  }

  @Override
  public boolean requiresCallerDetail() {
    return nameSpacePattern.requiresCallerDetail();
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    nameSpacePattern.render(logEvent, target);
  }
}
