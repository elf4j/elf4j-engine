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

import lombok.NonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 *
 */
enum PatternSegmentType {
    /**
     *
     */
    TIMESTAMP {
        @Override
        LogPattern translate(String patternSegment) {
            return TimestampPatternSegment.from(patternSegment);
        }

        @Override
        public boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }
    },
    /**
     *
     */
    LEVEL {
        @Override
        public boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return LevelPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    THREAD {
        @Override
        public boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return ThreadPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    CLASS {
        @Override
        boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return ClassPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    METHOD {
        @Override
        boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return MethodPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    MESSAGE {
        @Override
        boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return MessageAndExceptionPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    JSON {
        @Override
        boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return JsonPatternSegment.from(patternSegment);
        }
    },
    /**
     *
     */
    VERBATIM {
        @Override
        boolean isTargetTypeOf(String patternSegment) {
            return isPatternSegmentOfType(this, patternSegment);
        }

        @Override
        LogPattern translate(String patternSegment) {
            return VerbatimPatternSegment.from(patternSegment);
        }
    };
    private static final EnumSet<PatternSegmentType> PREDEFINED_PATTERN_TYPES =
            EnumSet.complementOf(EnumSet.of(VERBATIM));

    /**
     * @param patternSegment entire text of an individual pattern segment, including pattern segment name and possibly
     *                       options
     * @return the option portion of the pattern segment text if present; otherwise, empty Optional
     */
    static Optional<String> getPatternSegmentOption(@NonNull String patternSegment) {
        String[] elements = patternSegment.split(":", 2);
        return elements.length == 1 ? Optional.empty() : Optional.of(elements[1].trim());
    }

    /**
     * @param pattern entire layout pattern text of a writer, including one or more individual pattern segments.
     *                Predefined pattern segments are those between curly braces e.g. {timestamp}, {level}, or {json}
     *                that extract and render specific log data to compose the final log message. Pattern text outside
     *                curly brace pairs are to be rendered verbatim in the final log message.
     * @return ordered list of individual patterns forming the entire layout pattern of the writer
     */
    static List<LogPattern> parsePatternSegments(String pattern) {
        List<LogPattern> logPatterns = new ArrayList<>();
        final int length = pattern.length();
        int i = 0;
        while (i < length) {
            int j;
            String segment;
            if (pattern.charAt(i) == '{') {
                j = pattern.indexOf('}', i);
                segment = (j == -1) ? pattern.substring(i, length) : pattern.substring(i + 1, j);
                i = j + 1;
            } else {
                j = pattern.indexOf('{', i);
                segment = (j == -1) ? pattern.substring(i, length) : pattern.substring(i, j);
                i = j;
            }
            logPatterns.add(parsePatternSegment(segment));
        }
        return logPatterns;
    }

    private static boolean isPatternSegmentOfType(PatternSegmentType patternSegmentType, String patternSegment) {
        if (patternSegmentType == VERBATIM) {
            return PREDEFINED_PATTERN_TYPES.stream().noneMatch(type -> type.isTargetTypeOf(patternSegment));
        }
        return patternSegmentType.name().equalsIgnoreCase(patternSegment.split(":", 2)[0].trim());
    }

    private static LogPattern parsePatternSegment(String patternSegment) {
        return EnumSet.allOf(PatternSegmentType.class)
                .stream()
                .filter(type -> type.isTargetTypeOf(patternSegment))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "pattern segment: '" + patternSegment + "' not parsable"))
                .translate(patternSegment);
    }

    abstract LogPattern translate(String patternSegment);

    /**
     * @param patternSegment text configuration of an individual pattern segment
     * @return true if this pattern segment type is the target type of the specified pattern segment text
     */
    abstract boolean isTargetTypeOf(String patternSegment);
}
