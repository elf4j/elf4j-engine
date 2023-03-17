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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 *
 */
enum PatternType {
    /**
     *
     */
    TIMESTAMP {
        @Override
        LogPattern translate(String pattern) {
            return TimestampPattern.from(pattern);
        }

        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }
    },
    /**
     *
     */
    LEVEL {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return LevelPattern.from(pattern);
        }
    },
    /**
     *
     */
    THREAD {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return ThreadPattern.from(pattern);
        }
    },
    /**
     *
     */
    CLASS {
        @Override
        boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return ClassPattern.from(pattern);
        }
    },
    /**
     *
     */
    METHOD {
        @Override
        boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return MethodPattern.from(pattern);
        }
    },
    /**
     *
     */
    MESSAGE {
        @Override
        boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return MessageAndExceptionPattern.from(pattern);
        }
    },
    /**
     *
     */
    JSON {
        @Override
        boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return JsonPattern.from(pattern);
        }
    },
    /**
     *
     */
    VERBATIM {
        @Override
        boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern translate(String pattern) {
            return VerbatimPattern.from(pattern);
        }
    };
    private static final EnumSet<PatternType> PREDEFINED_PATTERN_TYPES = EnumSet.complementOf(EnumSet.of(VERBATIM));

    /**
     * @param pattern entire layout pattern text of a writer, including one or more individual pattern segments
     * @return ordered list of individual patterns forming the entire layout pattern of the writer
     */
    static List<LogPattern> parsePatternGroup(String pattern) {
        List<LogPattern> logPatterns = new ArrayList<>();
        int length = pattern.length();
        int i = 0;
        while (i < length) {
            String iPattern;
            char character = pattern.charAt(i);
            int iEnd = pattern.indexOf('}', i);
            if (character == '{' && iEnd != -1) {
                iPattern = pattern.substring(i + 1, iEnd);
                i = iEnd + 1;
            } else {
                if (iEnd == -1) {
                    iEnd = length;
                } else {
                    iEnd = pattern.indexOf('{', i);
                    if (iEnd == -1) {
                        iEnd = length;
                    }
                }
                iPattern = pattern.substring(i, iEnd);
                i = iEnd;
            }
            logPatterns.add(parsePattern(iPattern));
        }
        return logPatterns;
    }

    private static boolean isPatternOfType(PatternType patternType, String pattern) {
        if (patternType == VERBATIM) {
            return PREDEFINED_PATTERN_TYPES.stream().noneMatch(type -> type.isTargetTypeOf(pattern));
        }
        return patternType.name().equalsIgnoreCase(pattern.split(":", 2)[0].trim());
    }

    private static LogPattern parsePattern(String pattern) {
        return EnumSet.allOf(PatternType.class)
                .stream()
                .filter(type -> type.isTargetTypeOf(pattern))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("pattern: '" + pattern + "' not parsable"))
                .translate(pattern);
    }

    /**
     * @param pattern text configuration of an individual pattern segment
     * @return true if this pattern type is the target type of the specified pattern text
     */
    abstract boolean isTargetTypeOf(String pattern);

    abstract LogPattern translate(String pattern);
}
