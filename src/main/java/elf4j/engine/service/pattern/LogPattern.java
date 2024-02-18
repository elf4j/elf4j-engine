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

package elf4j.engine.service.pattern;

import elf4j.engine.service.LogEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.NonNull;
import lombok.Value;

/** Composite of individual patterns, intended to form the entire log layout */
@Value
public class LogPattern implements PatternElement {
    List<PatternElement> patternElements;

    /**
     * @param pattern layout pattern text for entire log entry from configuration
     * @return composite pattern object for the entire final log message output layout
     */
    public static @Nonnull LogPattern from(@NonNull String pattern) {
        if (pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Unexpected blank pattern");
        }
        List<PatternElement> elements = new ArrayList<>();
        final int length = pattern.length();
        int i = 0;
        while (i < length) {
            String element;
            int j;
            if (pattern.charAt(i) == '{') {
                j = pattern.indexOf('}', i);
                if (j != -1) {
                    element = pattern.substring(i + 1, j);
                    i = j + 1;
                } else {
                    element = pattern.substring(i);
                    i = length;
                }
                elements.add(PatternElements.parsePredefinedPatternELement(element));
            } else {
                j = pattern.indexOf('{', i);
                if (j != -1) {
                    element = pattern.substring(i, j);
                    i = j;
                } else {
                    element = pattern.substring(i);
                    i = length;
                }
                elements.add(VerbatimElement.from(element));
            }
        }
        return new LogPattern(elements);
    }

    @Override
    public boolean includeCallerDetail() {
        return patternElements.stream().anyMatch(PatternElement::includeCallerDetail);
    }

    @Override
    public void render(LogEvent logEvent, StringBuilder target) {
        for (PatternElement pattern : patternElements) {
            pattern.render(logEvent, target);
        }
    }
}
