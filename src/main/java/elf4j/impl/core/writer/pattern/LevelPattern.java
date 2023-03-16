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

package elf4j.impl.core.writer.pattern;

import elf4j.impl.core.service.LogEntry;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;

/**
 *
 */
@Value
public class LevelPattern implements LogPattern {
    private static final int DISPLAY_LENGTH_UNSET = -1;
    int displayLength;

    private LevelPattern(int displayLength) {
        this.displayLength = displayLength;
    }

    /**
     * @param pattern to convert
     * @return converted pattern object
     */
    @Nonnull
    public static LevelPattern from(@NonNull String pattern) {
        if (!PatternType.LEVEL.isTargetTypeOf(pattern)) {
            throw new IllegalArgumentException("pattern: " + pattern);
        }
        return new LevelPattern(LogPattern.getPatternOption(pattern)
                .map(Integer::parseInt)
                .orElse(DISPLAY_LENGTH_UNSET));
    }

    @Override
    public boolean includeCallerDetail() {
        return false;
    }

    @Override
    public boolean includeCallerThread() {
        return false;
    }

    @Override
    public void render(LogEntry logEntry, StringBuilder logTextBuilder) {
        String level = logEntry.getNativeLogger().getLevel().name();
        if (displayLength == DISPLAY_LENGTH_UNSET) {
            logTextBuilder.append(level);
            return;
        }
        char[] levelChars = level.toCharArray();
        for (int i = 0; i < displayLength; i++) {
            logTextBuilder.append(i < levelChars.length ? levelChars[i] : ' ');
        }
    }
}
