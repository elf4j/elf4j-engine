package elf4j.engine.logging.pattern;

import elf4j.engine.logging.pattern.element.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PatternElements {

  public static final String DELIMITER_PATTERN_ELEMENT = ":";
  public static final String DELIMITER_DISPLAY_OPTION = ",";

  private PatternElements() {}

  static String getPatternElementName(String patternElement) {
    return patternElement.split(DELIMITER_PATTERN_ELEMENT, 2)[0].strip();
  }

  /**
   * @param patternElement entire text of an individual pattern element, including pattern element
   *     name and possibly options
   * @return the option portion of the pattern element text if present; otherwise, empty Optional
   */
  public static Optional<List<String>> getPatternElementDisplayOptions(String patternElement) {
    String[] elements = patternElement.split(DELIMITER_PATTERN_ELEMENT, 2);
    return elements.length == 1
        ? Optional.empty()
        : Optional.of(Arrays.stream(elements[1].trim().split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .toList());
  }

  static PatternElement parsePredefinedPatternELement(String predefinedPatternElement) {
    return Arrays.stream(PredefinedPatternElementType.values())
        .filter(type -> type.isTargetTypeOf(predefinedPatternElement))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Predefined pattern element: '" + predefinedPatternElement + "'"))
        .parsePatternElement(predefinedPatternElement);
  }

  public static Set<String> upperCaseAlphaNumericOnly(Set<String> in) {
    return in.stream().map(PatternElements::upperCaseAlphaNumericOnly).collect(Collectors.toSet());
  }

  private static String upperCaseAlphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
  }

  enum PredefinedPatternElementType {
    TIMESTAMP {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return TimestampElement.from(patternElement);
      }
    },

    LEVEL {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return LevelElement.from(patternElement);
      }
    },

    THREAD {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return ThreadElement.from(patternElement);
      }
    },

    CLASS {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return ClassElement.from(patternElement);
      }
    },

    METHOD {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return new MethodElement();
      }
    },
    FILENAME {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return new FileNameElement();
      }
    },
    LINE_NUMBER {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return new LineNumberElement();
      }
    },

    MESSAGE {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return new MessageAndExceptionElement();
      }
    },

    JSON {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return JsonElement.from(patternElement);
      }
    },
    SYS_PROP {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return SystemPropertyElement.from(patternElement);
      }
    },
    SYS_ENV {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return SystemEnvironmentElement.from(patternElement);
      }
    },
    CONTEXT {
      @Override
      PatternElement parsePatternElement(String patternElement) {
        return ContextElement.from(patternElement);
      }
    };

    /**
     * @param patternElement text to translate
     * @return pattern element object of the specified text
     */
    abstract PatternElement parsePatternElement(String patternElement);

    /**
     * @param patternElement text configuration of an individual pattern element
     * @return true if this pattern element type is the target type of the specified pattern element
     *     text
     */
    private boolean isTargetTypeOf(String patternElement) {
      return upperCaseAlphaNumericOnly(name())
          .equals(upperCaseAlphaNumericOnly(getPatternElementName(patternElement)));
    }
  }
}
