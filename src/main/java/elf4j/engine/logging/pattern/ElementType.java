package elf4j.engine.logging.pattern;

import elf4j.engine.logging.pattern.element.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum ElementType {
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
      return MethodElement.from(patternElement);
    }
  },
  FILENAME {
    @Override
    PatternElement parse(String patternElement) {
      return FileNameElement.from(patternElement);
    }
  },
  LINE_NUMBER {
    @Override
    PatternElement parse(String patternElement) {
      return LineNumberElement.from(patternElement);
    }
  },
  MESSAGE {
    @Override
    PatternElement parse(String patternElement) {
      return MessageAndExceptionElement.from(patternElement);
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
  },
  VERBATIM {
    @Override
    PatternElement parse(String patternElement) {
      return VerbatimElement.from(patternElement);
    }
  };

  public static final String DELIMITER_PATTERN_ELEMENT = ":";
  public static final String DELIMITER_DISPLAY_OPTION = ",";

  public static ElementType from(String patternElement) {
    String elementType = patternElement.split(DELIMITER_PATTERN_ELEMENT, 2)[0].strip();
    return Arrays.stream(values())
        .filter(v -> alphaNumericOnly(v.name()).equalsIgnoreCase(alphaNumericOnly(elementType)))
        .findFirst()
        .orElse(VERBATIM);
  }

  public static List<String> getElementDisplayOptions(String patternElement) {
    String[] elements = patternElement.split(DELIMITER_PATTERN_ELEMENT, 2);
    return elements.length < 2 || elements[1].isBlank()
        ? List.of()
        : Arrays.stream(elements[1].split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .filter(s -> !s.isBlank())
            .toList();
  }

  static PatternElement parseElement(String patternElement) {
    return ElementType.from(patternElement).parse(patternElement);
  }

  public static Set<String> uniqueAlphaNumericOnly(Collection<String> in) {
    return in.stream().map(ElementType::alphaNumericOnly).collect(Collectors.toUnmodifiableSet());
  }

  public static String alphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "");
  }

  abstract PatternElement parse(String patternElement);
}
