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
import elf4j.engine.util.StackTraceUtils;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;

/**
 *
 */
@Value
public class MessageAndExceptionPatternSegment implements LogPattern {
    /**
     * @param patternSegment text segment to convert
     * @return converted patternSegment object
     */
    @Nonnull
    public static MessageAndExceptionPatternSegment from(String patternSegment) {
        if (!PatternSegmentType.MESSAGE.isTargetTypeOf(patternSegment)) {
            throw new IllegalArgumentException("patternSegment text: " + patternSegment);
        }
        return new MessageAndExceptionPatternSegment();
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
    public void render(@NonNull LogEntry logEntry, @NonNull StringBuilder logTextBuilder) {
        logTextBuilder.append(logEntry.getResolvedMessage());
        Throwable t = logEntry.getException();
        if (t == null) {
            return;
        }
        logTextBuilder.append(System.lineSeparator()).append(StackTraceUtils.getTraceAsBuffer(t));
    }
}