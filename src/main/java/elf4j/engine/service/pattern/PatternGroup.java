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
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Composite of individual patterns forming the entire layout pattern
 */
@Value
public class PatternGroup implements LogPattern {
    List<LogPattern> patterns;

    /**
     * @param pattern
     *         entire layout pattern text from configuration
     * @return composite pattern object for the entire final log message output layout
     */
    @Nonnull
    public static PatternGroup from(@NonNull String pattern) {
        return new PatternGroup(PatternType.parsePatterns(pattern));
    }

    @Override
    public boolean includeCallerDetail() {
        return patterns.stream().anyMatch(LogPattern::includeCallerDetail);
    }

    @Override
    public boolean includeCallerThread() {
        return patterns.stream().anyMatch(LogPattern::includeCallerThread);
    }

    @Override
    public void render(LogEvent logEvent, StringBuilder target) {
        for (LogPattern pattern : patterns) {
            pattern.render(logEvent, target);
        }
    }
}
