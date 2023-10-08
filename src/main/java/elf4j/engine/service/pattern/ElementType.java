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
enum ElementType {
    /**
     *
     */
    TIMESTAMP {
        @Override
        PatternElement parse(String patternElement) {
            return TimestampElement.from(patternElement);
        }
    },
    /**
     *
     */
    LEVEL {
        @Override
        PatternElement parse(String patternElement) {
            return LevelElement.from(patternElement);
        }
    },
    /**
     *
     */
    THREAD {
        @Override
        PatternElement parse(String patternElement) {
            return ThreadElement.from(patternElement);
        }
    },
    /**
     *
     */
    CLASS {
        @Override
        PatternElement parse(String patternElement) {
            return ClassElement.from(patternElement);
        }
    },
    /**
     *
     */
    METHOD {
        @Override
        PatternElement parse(String patternElement) {
            return MethodElement.from(patternElement);
        }
    },
    FILENAME {
        @Override
        PatternElement parse(String patternElement) {
            return FileNameElement.from(patternElement);
        }
    },
    LINENUMBER {
        @Override
        PatternElement parse(String patternElement) {
            return LineNumberElement.from(patternElement);
        }
    },

    /**
     *
     */
    MESSAGE {
        @Override
        PatternElement parse(String patternElement) {
            return MessageAndExceptionElement.from(patternElement);
        }
    },
    /**
     *
     */
    JSON {
        @Override
        PatternElement parse(String patternElement) {
            return JsonElement.from(patternElement);
        }
    },
    SYSPROP {
        @Override
        PatternElement parse(String patternElement) {
            return SystemPropertyElement.from(patternElement);
        }
    },
    SYSENV {
        @Override
        PatternElement parse(String patternElement) {
            return SystemEnvironmentElement.from(patternElement);
        }
    },
    /**
     *
     */
    VERBATIM {
        @Override
        PatternElement parse(String patternElement) {
            return VerbatimElement.from(patternElement);
        }
    };
    private static final EnumSet<ElementType> PREDEFINED_TYPES = EnumSet.complementOf(EnumSet.of(VERBATIM));

    /**
     * @param patternElement
     *         entire text of an individual pattern element, including pattern element name and possibly options
     * @return the option portion of the pattern element text if present; otherwise, empty Optional
     */
    static Optional<String> getPatternDisplayOption(@NonNull String patternElement) {
        String[] elements = patternElement.split(":", 2);
        return elements.length == 1 ? Optional.empty() : Optional.of(elements[1].trim());
    }

    /**
     * @param pattern
     *         entire layout pattern text of a writer, including one or more individual pattern elements. Predefined
     *         pattern element texts in curly braces - e.g. {timestamp}, {level}, or {json} - will be parsed into
     *         pattern element objects who extract and render specific log data to form the final log message. Undefined
     *         pattern texts, in or outside curly braces, are to be rendered verbatim in the final log message.
     * @return ordered list of individual patterns forming the entire layout pattern of the writer
     */
    static @NonNull List<PatternElement> parsePattern(@NonNull String pattern) {
        if (pattern.trim().isEmpty()) {
            throw new IllegalArgumentException("Unexpected blank pattern");
        }
        List<PatternElement> patternElements = new ArrayList<>();
        final int length = pattern.length();
        int i = 0;
        while (i < length) {
            String element;
            int j;
            if (pattern.charAt(i) == '{') {
                j = pattern.indexOf('}', i);
                if (j != -1) {
                    element = pattern.substring(i + 1, j);
                    i = j + 1;
                } else {
                    element = pattern.substring(i);
                    i = length;
                }
                patternElements.add(parsePredefinedPatternELement(element));
            } else {
                j = pattern.indexOf('{', i);
                if (j != -1) {
                    element = pattern.substring(i, j);
                    i = j;
                } else {
                    element = pattern.substring(i);
                    i = length;
                }
                patternElements.add(VERBATIM.parse(element));
            }
        }
        return patternElements;
    }

    private static String getPatternElementName(String patternElement) {
        return patternElement.split(":", 2)[0].trim();
    }

    private static PatternElement parsePredefinedPatternELement(String predefinedPatternElement) {
        return PREDEFINED_TYPES.stream()
                .filter(type -> type.isTargetTypeOf(predefinedPatternElement))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Predefined pattern element: '" + predefinedPatternElement + "'"))
                .parse(predefinedPatternElement);
    }

    /**
     * @param patternElement
     *         text to translate
     * @return pattern element object of the specified text
     */
    abstract PatternElement parse(String patternElement);

    /**
     * @param patternElement
     *         text configuration of an individual pattern element
     * @return true if this pattern element type is the target type of the specified pattern element text
     */
    private boolean isTargetTypeOf(String patternElement) {
        if (this == VERBATIM) {
            return PREDEFINED_TYPES.stream().noneMatch(type -> type.isTargetTypeOf(patternElement));
        }
        return name().equalsIgnoreCase(getPatternElementName(patternElement));
    }
}
