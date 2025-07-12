package elf4j.engine.logging.pattern;

import elf4j.engine.logging.pattern.predefined.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum PredefinedPatternElementType {
  TIMESTAMP {
    @Override
    PatternElement parse(String patternElement) {
      return TimestampElement.from(patternElement);
    }
  },
  LEVEL {
    @Override
    PatternElement parse(String patternElement) {
      return LevelElement.from(patternElement);
    }
  },
  THREAD {
    @Override
    PatternElement parse(String patternElement) {
      return ThreadElement.from(patternElement);
    }
  },
  LOGGER {
    @Override
    PatternElement parse(String patternElement) {
      return NameSpaceElement.from(patternElement, NameSpaceElement.TargetPattern.LOGGER);
    }
  },
  CLASS {
    @Override
    PatternElement parse(String patternElement) {
      return NameSpaceElement.from(patternElement, NameSpaceElement.TargetPattern.CLASS);
    }
  },
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
  LINE_NUMBER {
    @Override
    PatternElement parse(String patternElement) {
      return new LineNumberElement();
    }
  },
  MESSAGE {
    @Override
    PatternElement parse(String patternElement) {
      return new MessageAndExceptionElement();
    }
  },
  JSON {
    @Override
    PatternElement parse(String patternElement) {
      return JsonElement.from(patternElement);
    }
  },
  SYS_PROP {
    @Override
    PatternElement parse(String patternElement) {
      return SystemPropertyElement.from(patternElement);
    }
  },
  SYS_ENV {
    @Override
    PatternElement parse(String patternElement) {
      return SystemEnvironmentElement.from(patternElement);
    }
  },
  CONTEXT {
    @Override
    PatternElement parse(String patternElement) {
      return ContextElement.from(patternElement);
    }
  };

  public static final String DELIMITER_PATTERN_ELEMENT = ":";
  public static final String DELIMITER_DISPLAY_OPTION = ",";

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
        : Optional.of(Arrays.stream(elements[1].split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .toList());
  }

  static PatternElement parsePredefinedPatternELement(String predefinedPatternElement) {
    return PredefinedPatternElementType.from(predefinedPatternElement)
        .parse(predefinedPatternElement);
  }

  public static Set<String> alphaNumericOnly(Set<String> in) {
    return in.stream()
        .map(PredefinedPatternElementType::alphaNumericOnly)
        .collect(Collectors.toSet());
  }

  private static String alphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "");
  }

  /**
   * @param patternElement text to translate
   * @return pattern element object of the specified text
   */
  static PredefinedPatternElementType from(String patternElement) {
    return Arrays.stream(PredefinedPatternElementType.values())
        .filter(type -> alphaNumericOnly(type.name())
            .equalsIgnoreCase(alphaNumericOnly(getPatternElementName(patternElement))))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Unexpected predefined pattern element: '%s'".formatted(patternElement)));
  }

  abstract PatternElement parse(String patternElement);
}
