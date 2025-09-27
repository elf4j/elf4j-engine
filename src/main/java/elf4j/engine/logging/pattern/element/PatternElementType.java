package elf4j.engine.logging.pattern.element;

import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Types of predefined pattern elements recognized inside a log pattern configuration. Each type is
 * associated with a function that can convert a text pattern element into the corresponding
 * RenderingPattern instance.
 */
enum PatternElementType {
  TIMESTAMP(TimestampPattern::from),
  LEVEL(LevelPattern::from),
  THREAD(ThreadPattern::from),
  LOGGER(LoggerPattern::from),
  CLASS(ClassPattern::from),
  METHOD(MethodPattern::from),
  FILENAME(FileNamePattern::from),
  LINE_NUMBER(LineNumberPattern::from),
  MESSAGE(MessageAndExceptionPattern::from),
  JSON(JsonPattern::from),
  SYS_PROP(SystemPropertyPattern::from),
  SYS_ENV(SystemEnvironmentPattern::from),
  CONTEXT(ContextPattern::from),
  VERBATIM(VerbatimPattern::from);

  private final Function<String, ? extends RenderingPattern> elementPatternParser;

  PatternElementType(final Function<String, ? extends RenderingPattern> elementPatternParser) {
    this.elementPatternParser = elementPatternParser;
  }

  static PatternElementType from(String elementPattern) {
    return Arrays.stream(values())
        .filter(patternElementType -> ElementPatterns.alphaNumericOnly(patternElementType.name())
            .equalsIgnoreCase(ElementPatterns.alphaNumericOnly(
                ElementPatterns.getElementPatternName(elementPattern))))
        .findFirst()
        .orElse(VERBATIM);
  }

  RenderingPattern parseElement(final String elementPattern) {
    return elementPatternParser.apply(elementPattern);
  }
}
