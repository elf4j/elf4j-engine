package elf4j.engine.logging.pattern.element;

import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Parser and utility methods for predefined pattern elements used in log message patterns. */
public class ElementPatterns {
  private static final String DELIMITER_PATTERN_ELEMENT = ":";
  private static final String DELIMITER_DISPLAY_OPTION = ",";

  private ElementPatterns() {}

  static String alphaNumericOnly(String in) {
    return in.replaceAll("[^a-zA-Z0-9]", "");
  }

  static Set<String> uniqueAlphaNumericOnly(Collection<String> in) {
    return in.stream()
        .map(ElementPatterns::alphaNumericOnly)
        .collect(Collectors.toUnmodifiableSet());
  }

  static String getElementPatternName(final String elementPattern) {
    return elementPattern.split(DELIMITER_PATTERN_ELEMENT, 2)[0].strip();
  }

  static List<String> getElementPatternDisplayOptions(String elementPattern) {
    String[] elements = elementPattern.split(DELIMITER_PATTERN_ELEMENT, 2);
    return elements.length < 2 || elements[1].isBlank()
        ? List.of()
        : Arrays.stream(elements[1].split(DELIMITER_DISPLAY_OPTION))
            .map(String::strip)
            .filter(s -> !s.isBlank())
            .toList();
  }

  /**
   * Parses the specified pattern element string and constructs the corresponding RenderingPattern
   * object.
   *
   * @param elementPattern the pattern element string to parse.
   * @return the constructed RenderingPattern object for the specified pattern element
   */
  public static RenderingPattern parseElementPattern(String elementPattern) {
    return PatternElementType.from(elementPattern).parseElement(elementPattern);
  }
}
