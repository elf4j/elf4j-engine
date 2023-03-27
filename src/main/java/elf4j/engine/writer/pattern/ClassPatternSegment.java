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

/**
 *
 */
@Value
public class ClassPatternSegment implements LogPattern {
    private static final DisplayOption DEFAULT_DISPLAY_OPTION = DisplayOption.FULL;
    @NonNull DisplayOption classDisplayOption;

    /**
     * @param patternSegment
     *         text patternSegment to convert
     * @return converted patternSegment object
     */
    @Nonnull
    public static ClassPatternSegment from(@NonNull String patternSegment) {
        if (!PatternSegmentType.CLASS.isTargetTypeOf(patternSegment)) {
            throw new IllegalArgumentException("patternSegment: " + patternSegment);
        }
        return new ClassPatternSegment(PatternSegmentType.getPatternSegmentOption(patternSegment)
                .map(displayOption -> DisplayOption.valueOf(displayOption.toUpperCase()))
                .orElse(DEFAULT_DISPLAY_OPTION));
    }

    @Override
    public boolean includeCallerDetail() {
        return true;
    }

    @Override
    public boolean includeCallerThread() {
        return false;
    }

    @Override
    public void render(@NonNull LogEntry logEntry, StringBuilder target) {
        String fullName = logEntry.getCallerClassName();
        switch (classDisplayOption) {
            case FULL:
                target.append(fullName);
                return;
            case SIMPLE:
                target.append(fullName.substring(fullName.lastIndexOf('.') + 1));
                return;
            case COMPRESSED: {
                String[] tokens = fullName.split("\\.");
                String simpleName = tokens[tokens.length - 1];
                for (int i = 0; i < tokens.length - 1; i++) {
                    target.append(tokens[i].charAt(0)).append('.');
                }
                target.append(simpleName);
                return;
            }
            default:
                throw new IllegalArgumentException("class display option: " + classDisplayOption);
        }
    }

    enum DisplayOption {
        FULL, SIMPLE, COMPRESSED
    }
}
