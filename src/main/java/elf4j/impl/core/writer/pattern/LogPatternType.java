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
import java.util.Objects;

/**
 *
 */
public enum LogPatternType {
    /**
     *
     */
    TIMESTAMP {
        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? TimestampPattern.from(pattern) : null;
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
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? LevelPattern.from(pattern) : null;
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
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? ThreadPattern.from(pattern) : null;
        }
    },
    /**
     *
     */
    CLASS {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? ClassPattern.from(pattern) : null;
        }
    },
    /**
     *
     */
    METHOD {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? MethodPattern.from(pattern) : null;
        }
    },
    /**
     *
     */
    MESSAGE {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? MessageAndExceptionPattern.from(pattern) : null;
        }
    },
    /**
     *
     */
    JSON {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? JsonPattern.from(pattern) : null;
        }
    },
    /**
     *
     */
    VERBATIM {
        @Override
        public boolean isTargetTypeOf(String pattern) {
            return isPatternOfType(this, pattern);
        }

        @Override
        LogPattern parsePattern(String pattern) {
            return this.isTargetTypeOf(pattern) ? VerbatimPattern.from(pattern) : null;
        }
    };

    /**
     * @param pattern entire layout pattern text of a writer, including one or more individual pattern segments
     * @return ordered list of individual patterns forming the entire layout pattern of the writer
     */
    public static List<LogPattern> parseAllPatternsOrThrow(String pattern) {
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
            logPatterns.add(LogPatternType.parsePatternOrThrow(iPattern));
        }
        return logPatterns;
    }

    private static boolean isPatternOfType(LogPatternType targetPatternType, String pattern) {
        if (targetPatternType == VERBATIM) {
            return EnumSet.complementOf(EnumSet.of(VERBATIM)).stream().noneMatch(type -> type.isTargetTypeOf(pattern));
        }
        return targetPatternType.name().equalsIgnoreCase(pattern.split(":", 2)[0].trim());
    }

    private static LogPattern parsePatternOrThrow(String pattern) {
        return EnumSet.allOf(LogPatternType.class)
                .stream()
                .map(type -> type.parsePattern(pattern))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("pattern: '" + pattern + "' not parsable"));
    }

    /**
     * @param pattern text configuration of an individual pattern segment
     * @return true if this pattern type is the target type of the specified pattern text
     */
    public abstract boolean isTargetTypeOf(String pattern);

    abstract LogPattern parsePattern(String pattern);
}
