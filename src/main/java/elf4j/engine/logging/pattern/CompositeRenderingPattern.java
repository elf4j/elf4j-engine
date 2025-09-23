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
import elf4j.engine.logging.pattern.element.ElementPatterns;
import java.util.ArrayList;
import java.util.List;

/**
 * A composite pattern that consists of a list of individual pattern elements to render a complete
 * log message.
 *
 * @param patternElements the list of individual pattern elements that make up the composite log
 *     pattern
 */
public record CompositeRenderingPattern(List<RenderingPattern> patternElements)
    implements RenderingPattern {

  /**
   * Parses the specified pattern string and constructs a LogPattern object. The text segments
   * enclosed in curly braces are parsed as predefined pattern elements, other text segments are
   * treated as literal/verbatim text.
   *
   * @param pattern the pattern string to parse. It is the configuration pattern text for the
   *     complete log message. E.g. "{timestamp} [{thread}] {level} {logger} - {message}"
   * @return the constructed LogPattern object
   * @throws IllegalArgumentException if the pattern string is blank
   */
  public static CompositeRenderingPattern from(String pattern) {
    if (pattern.trim().isEmpty()) {
      throw new IllegalArgumentException("Unexpected blank pattern");
    }
    List<RenderingPattern> elements = new ArrayList<>();
    final int length = pattern.length();
    int elementStart = 0;
    while (elementStart < length) {
      String element;
      int elementEnd;
      if (pattern.charAt(elementStart) == '{') {
        elementEnd = pattern.indexOf('}', elementStart);
        if (elementEnd != -1) {
          element = pattern.substring(elementStart + 1, elementEnd);
          elementStart = elementEnd + 1;
        } else {
          element = pattern.substring(elementStart);
          elementStart = length;
        }
      } else {
        elementEnd = pattern.indexOf('{', elementStart);
        if (elementEnd != -1) {
          element = pattern.substring(elementStart, elementEnd);
          elementStart = elementEnd;
        } else {
          element = pattern.substring(elementStart);
          elementStart = length;
        }
      }
      elements.add(ElementPatterns.parseElementPattern(element));
    }
    return new CompositeRenderingPattern(elements);
  }

  /**
   * Checks if the log should include caller detail such as method, line number, etc.
   *
   * @return true if any of the pattern elements include caller detail, false otherwise
   */
  @Override
  public boolean requiresCallerDetail() {
    return patternElements.stream().anyMatch(RenderingPattern::requiresCallerDetail);
  }

  /**
   * Renders the log event and appends it to the specified StringBuilder target.
   *
   * <p>Although thread-safe as with any PatternElement operations, walking over the entire List of
   * render elements does not have to be atomic (i.e. synchronization/locking is not needed during
   * the walk). As long as each caller thread has its own copies of the specified logEvent and
   * render target, different threads can traverse the same List of render elements at the same time
   * while each thread populating its own copy of render target. The target will not be flushed to
   * the final log destination (e.g. the STDOUT stream or a log file) during the walk until after
   * the target is fully populated by all the render elements.
   *
   * <p>Different logEvents of the same caller thread must be sent to this method one at a time in
   * sequence, which is naturally the case from the caller client. The same calling sequence is
   * preserved during the log processing via the <a
   * href="https://q3769.github.io/conseq4j/">conseq4j API</a>
   *
   * @param logEvent the log event to render
   * @param target the StringBuilder to append the rendered log event to
   */
  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    for (RenderingPattern element : patternElements) {
      element.render(logEvent, target);
    }
  }
}
