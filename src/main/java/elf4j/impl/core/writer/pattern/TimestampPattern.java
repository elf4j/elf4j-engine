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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 *
 */
@Value
public class TimestampPattern implements LogPattern {
    private static final DateTimeFormatter DEFAULT_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    private static final ZoneId DEFAULT_TIMESTAMP_ZONE = ZoneId.systemDefault();
    DateTimeFormatter dateTimeFormatter;

    /**
     * @param pattern text pattern to convert
     * @return converted pattern object
     */
    @Nonnull
    public static TimestampPattern from(@NonNull String pattern) {
        if (!PatternType.TIMESTAMP.isTargetTypeOf(pattern)) {
            throw new IllegalArgumentException("pattern: " + pattern);
        }
        DateTimeFormatter dateTimeFormatter = LogPattern.getPatternOption(pattern)
                .map(DateTimeFormatter::ofPattern)
                .orElse(DEFAULT_TIMESTAMP_FORMATTER);
        if (dateTimeFormatter.getZone() == null) {
            dateTimeFormatter = dateTimeFormatter.withZone(DEFAULT_TIMESTAMP_ZONE);
        }
        return new TimestampPattern(dateTimeFormatter);
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
        logTextBuilder.append(dateTimeFormatter.format(logEntry.getTimestamp()));
    }
}
