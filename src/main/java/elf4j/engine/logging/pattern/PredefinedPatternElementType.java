package elf4j.engine.logging.pattern;

import elf4j.engine.logging.pattern.predefined.*;
import java.util.Arrays;
import java.util.List;
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
      return LoggerElement.from(patternElement);
    }
  },
  CLASS {
    @Override
    PatternElement parse(String patternElement) {
      return ClassElement.from(patternElement);
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

  static String getPatternElementType(String patternElement) {
    return patternElement.split(DELIMITER_PATTERN_ELEMENT, 2)[0].strip();
  }

  public static List<String> getPatternElementDisplayOptions(String patternElement) {
    String[] elements = patternElement.split(DELIMITER_PATTERN_ELEMENT, 2);
    return elements.length == 1 || elements[1].isBlank()
        ? List.of()
        : Arrays.stream(elements[1].split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .toList();
  }

  static PatternElement parsePredefinedPatternElement(String predefinedPatternElement) {
    return PredefinedPatternElementType.from(predefinedPatternElement)
        .parse(predefinedPatternElement);
  }

  public static Set<String> alphaNumericOnly(Set<String> in) {
    return in.stream()
        .map(PredefinedPatternElementType::alphaNumericOnly)
        .collect(Collectors.toSet());
  }

  public static String alphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "");
  }

  static PredefinedPatternElementType from(String patternElement) {
    return Arrays.stream(PredefinedPatternElementType.values())
        .filter(type -> type.matchesTypeOf(patternElement))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Unexpected predefined pattern element: '%s'".formatted(patternElement)));
  }

  abstract PatternElement parse(String patternElement);

  public boolean matchesTypeOf(String patternElement) {
    return alphaNumericOnly(this.name())
        .equalsIgnoreCase(alphaNumericOnly(getPatternElementType(patternElement)));
  }
}
