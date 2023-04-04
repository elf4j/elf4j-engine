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

import lombok.NonNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 *
 */
enum PatternType {
    /**
     *
     */
    TIMESTAMP {
        @Override
        LogPattern translate(String patternSegment) {
            return TimestampPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    LEVEL {
        @Override
        LogPattern translate(String patternSegment) {
            return LevelPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    THREAD {
        @Override
        LogPattern translate(String patternSegment) {
            return ThreadPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    CLASS {
        @Override
        LogPattern translate(String patternSegment) {
            return ClassPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    METHOD {
        @Override
        LogPattern translate(String patternSegment) {
            return MethodPattern.from(patternSegment);
        }
    }, FILENAME {
        @Override
        LogPattern translate(String patternSegment) {
            return FileNamePattern.from(patternSegment);
        }
    }, LINENUMBER {
        @Override
        LogPattern translate(String patternSegment) {
            return LineNumberPattern.from(patternSegment);
        }
    },

    /**
     *
     */
    MESSAGE {
        @Override
        LogPattern translate(String patternSegment) {
            return MessageAndExceptionPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    JSON {
        @Override
        LogPattern translate(String patternSegment) {
            return JsonPattern.from(patternSegment);
        }
    }, SYSPROP {
        @Override
        LogPattern translate(String patternSegment) {
            return SystemPropertyPattern.from(patternSegment);
        }
    }, SYSENV {
        @Override
        LogPattern translate(String patternSegment) {
            return SystemEnvironmentPattern.from(patternSegment);
        }
    },
    /**
     *
     */
    VERBATIM {
        @Override
        LogPattern translate(String patternSegment) {
            return VerbatimPattern.from(patternSegment);
        }
    };
    private static final EnumSet<PatternType> PATTERN_TYPES = EnumSet.allOf(PatternType.class);
    private static final EnumSet<PatternType> PREDEFINED_PATTERN_TYPES = EnumSet.complementOf(EnumSet.of(VERBATIM));

    /**
     * @param patternSegment
     *         entire text of an individual pattern segment, including pattern segment name and possibly options
     * @return the option portion of the pattern segment text if present; otherwise, empty Optional
     */
    static Optional<String> getPatternDisplayOption(@NonNull String patternSegment) {
        String[] elements = patternSegment.split(":", 2);
        return elements.length == 1 ? Optional.empty() : Optional.of(elements[1].trim());
    }

    /**
     * @param pattern
     *         entire layout pattern text of a writer, including one or more individual pattern segments. Predefined
     *         pattern segment texts in curly braces - e.g. {timestamp}, {level}, or {json} - will be parsed into
     *         pattern segment objects who extract and render specific log data to form the final log message. Undefined
     *         pattern texts, in or outside curly braces, are to be rendered verbatim in the final log message.
     * @return ordered list of individual patterns forming the entire layout pattern of the writer
     */
    static @NonNull List<LogPattern> parsePatterns(@NonNull String pattern) {
        if (pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Unexpected blank pattern");
        }
        List<LogPattern> logPatterns = new ArrayList<>();
        final int length = pattern.length();
        int i = 0;
        while (i < length) {
            String segment;
            int j;
            if (pattern.charAt(i) == '{') {
                j = pattern.indexOf('}', i);
                if (j != -1) {
                    segment = pattern.substring(i + 1, j);
                    i = j + 1;
                } else {
                    segment = pattern.substring(i);
                    i = length;
                }
            } else {
                j = pattern.indexOf('{', i);
                if (j != -1) {
                    segment = pattern.substring(i, j);
                    i = j;
                } else {
                    segment = pattern.substring(i);
                    i = length;
                }
            }
            logPatterns.add(parsePattern(segment));
        }
        return logPatterns;
    }

    private static LogPattern parsePattern(String patternSegment) {
        return PATTERN_TYPES.stream()
                .filter(type -> type.isTargetTypeOf(patternSegment))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("pattern segment: '" + patternSegment + "'"))
                .translate(patternSegment);
    }

    /**
     * @param patternSegment
     *         text to translate
     * @return pattern segment object of the specified text
     */
    abstract LogPattern translate(String patternSegment);

    /**
     * @param patternSegment
     *         text configuration of an individual pattern segment
     * @return true if this pattern segment type is the target type of the specified pattern segment text
     */
    boolean isTargetTypeOf(String patternSegment) {
        if (this == VERBATIM) {
            return PREDEFINED_PATTERN_TYPES.stream().noneMatch(type -> type.isTargetTypeOf(patternSegment));
        }
        return name().equalsIgnoreCase(patternSegment.split(":", 2)[0].trim());
    }
}
