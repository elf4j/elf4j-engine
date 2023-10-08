package elf4j.engine.service.pattern;

import lombok.NonNull;

import java.util.EnumSet;
import java.util.Optional;

class PatternElements {
    private static final EnumSet<PredefinedPatternElement> PREDEFINED_PATTERN_ELEMENT_TYPES =
            EnumSet.allOf(PredefinedPatternElement.class);

    private PatternElements() {
    }

    /**
     * @param patternElement
     *         entire text of an individual pattern element, including pattern element name and possibly options
     * @return the option portion of the pattern element text if present; otherwise, empty Optional
     */
    static Optional<String> getPatternElementDisplayOption(@NonNull String patternElement) {
        String[] elements = patternElement.split(":", 2);
        return elements.length == 1 ? Optional.empty() : Optional.of(elements[1].trim());
    }

    static @NonNull String getPatternElementName(@NonNull String patternElement) {
        return patternElement.split(":", 2)[0].trim();
    }

    static PatternElement parsePredefinedPatternELement(String predefinedPatternElement) {
        return PREDEFINED_PATTERN_ELEMENT_TYPES.stream()
                .filter(type -> type.isTargetTypeOf(predefinedPatternElement))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Predefined pattern element: '" + predefinedPatternElement + "'"))
                .parse(predefinedPatternElement);
    }

    /**
     *
     */
    enum PredefinedPatternElement {
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
                return new MethodElement();
            }
        },
        FILENAME {
            @Override
            PatternElement parse(String patternElement) {
                return new FileNameElement();
            }
        },
        LINENUMBER {
            @Override
            PatternElement parse(String patternElement) {
                return new LineNumberElement();
            }
        },

        /**
         *
         */
        MESSAGE {
            @Override
            PatternElement parse(String patternElement) {
                return new MessageAndExceptionElement();
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
        };

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
            return name().equalsIgnoreCase(getPatternElementName(patternElement));
        }
    }
}
