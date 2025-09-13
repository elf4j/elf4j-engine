package elf4j.engine.logging.pattern.element;

import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum ElementPatternType {
  TIMESTAMP {
    @Override
    TimestampPattern parse(String elementPattern) {
      return TimestampPattern.from(elementPattern);
    }
  },
  LEVEL {
    @Override
    LevelPattern parse(String elementPattern) {
      return LevelPattern.from(elementPattern);
    }
  },
  THREAD {
    @Override
    ThreadPattern parse(String elementPattern) {
      return ThreadPattern.from(elementPattern);
    }
  },
  LOGGER {
    @Override
    LoggerPattern parse(String elementPattern) {
      return LoggerPattern.from(elementPattern);
    }
  },
  CLASS {
    @Override
    ClassPattern parse(String elementPattern) {
      return ClassPattern.from(elementPattern);
    }
  },
  METHOD {
    @Override
    MethodPattern parse(String elementPattern) {
      return MethodPattern.from(elementPattern);
    }
  },
  FILENAME {
    @Override
    FileNamePattern parse(String elementPattern) {
      return FileNamePattern.from(elementPattern);
    }
  },
  LINE_NUMBER {
    @Override
    LineNumberPattern parse(String elementPattern) {
      return LineNumberPattern.from(elementPattern);
    }
  },
  MESSAGE {
    @Override
    MessageAndExceptionPattern parse(String elementPattern) {
      return MessageAndExceptionPattern.from(elementPattern);
    }
  },
  JSON {
    @Override
    JsonPattern parse(String elementPattern) {
      return JsonPattern.from(elementPattern);
    }
  },
  SYS_PROP {
    @Override
    SystemPropertyPattern parse(String elementPattern) {
      return SystemPropertyPattern.from(elementPattern);
    }
  },
  SYS_ENV {
    @Override
    SystemEnvironmentPattern parse(String elementPattern) {
      return SystemEnvironmentPattern.from(elementPattern);
    }
  },
  CONTEXT {
    @Override
    ContextPattern parse(String elementPattern) {
      return ContextPattern.from(elementPattern);
    }
  },
  VERBATIM {
    @Override
    VerbatimPattern parse(String elementPattern) {
      return VerbatimPattern.from(elementPattern);
    }
  };

  public static final String DELIMITER_PATTERN_ELEMENT = ":";
  public static final String DELIMITER_DISPLAY_OPTION = ",";

  public static ElementPatternType from(String elementPattern) {
    String elementType = elementPattern.split(DELIMITER_PATTERN_ELEMENT, 2)[0].strip();
    return Arrays.stream(values())
        .filter(v -> alphaNumericOnly(v.name()).equalsIgnoreCase(alphaNumericOnly(elementType)))
        .findFirst()
        .orElse(VERBATIM);
  }

  public static List<String> getElementDisplayOptions(String elementPattern) {
    String[] elements = elementPattern.split(DELIMITER_PATTERN_ELEMENT, 2);
    return elements.length < 2 || elements[1].isBlank()
        ? List.of()
        : Arrays.stream(elements[1].split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .filter(s -> !s.isBlank())
            .toList();
  }

  public static RenderingPattern parseElement(String elementPattern) {
    return ElementPatternType.from(elementPattern).parse(elementPattern);
  }

  public static Set<String> uniqueAlphaNumericOnly(Collection<String> in) {
    return in.stream()
        .map(ElementPatternType::alphaNumericOnly)
        .collect(Collectors.toUnmodifiableSet());
  }

  public static String alphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "");
  }

  abstract RenderingPattern parse(String elementPattern);
}
