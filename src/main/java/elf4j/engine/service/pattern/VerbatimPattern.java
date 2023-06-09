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

/**
 *
 */
@Value
public class VerbatimPattern implements LogPattern {
    @NonNull String text;

    /**
     * @param patternSegment
     *         text pattern segment to convert
     * @return converted pattern segment object
     */
    @Nonnull
    public static VerbatimPattern from(String patternSegment) {
        if (!PatternType.VERBATIM.isTargetTypeOf(patternSegment)) {
            throw new IllegalArgumentException(String.format(
                    "patternSegment '%s' looks to be targeted at another known patternSegment type than %s",
                    patternSegment,
                    PatternType.VERBATIM));
        }
        return new VerbatimPattern(patternSegment);
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
    public void render(LogEvent logEvent, @NonNull StringBuilder target) {
        target.append(text);
    }
}
