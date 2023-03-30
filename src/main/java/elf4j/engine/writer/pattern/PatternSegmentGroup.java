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

package elf4j.engine.writer.pattern;

import elf4j.engine.service.LogEntry;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Composite of individual patterns forming the entire layout pattern
 */
@Value
public class PatternSegmentGroup implements LogPattern {
    List<LogPattern> logPatternEntries;

    /**
     * @param pattern
     *         entire layout pattern text from configuration
     * @return composite pattern object for the entire log entry's output layout
     */
    @Nonnull
    public static PatternSegmentGroup from(@NonNull String pattern) {
        return new PatternSegmentGroup(PatternSegmentType.parsePatternSegments(pattern));
    }

    @Override
    public boolean includeCallerDetail() {
        return logPatternEntries.stream().anyMatch(LogPattern::includeCallerDetail);
    }

    @Override
    public boolean includeCallerThread() {
        return logPatternEntries.stream().anyMatch(LogPattern::includeCallerThread);
    }

    @Override
    public void renderTo(LogEntry logEntry, StringBuilder target) {
        for (LogPattern pattern : logPatternEntries) {
            pattern.renderTo(logEntry, target);
        }
    }
}
