package elf4j.engine.logging.pattern;

import elf4j.engine.logging.LogEvent;
import java.util.NoSuchElementException;
import org.slf4j.MDC;

/**
 * The ContextElement class implements the PatternElement interface and represents a context element
 * in a log pattern. It provides methods for checking if the log should include caller detail, for
 * creating a new instance from a pattern segment, and for rendering the log event.
 */
record ContextElement(String key) implements PatternElement {
  /**
   * Constructor for the ContextElement class.
   *
   * @param key whose value will be printed out from the thread context
   */
  ContextElement {}

  /**
   * Checks if the log should include caller detail such as method, line number, etc.
   *
   * @return false as the context element does not include caller detail
   */
  @Override
  public boolean includeCallerDetail() {
    return false;
  }

  /**
   * Creates a new ContextElement instance from a given pattern segment.
   *
   * @param patternSegment the pattern text to config the context logging
   * @return the element that can render context log
   * @throws NoSuchElementException if no key is configured in the 'context' pattern element
   */
  public static ContextElement from(String patternSegment) {
    return new ContextElement(PatternElements.getPatternElementDisplayOption(patternSegment)
        .orElseThrow(
            () -> new NoSuchElementException("No key configured in 'context' pattern element")));
  }

  /**
   * Renders the log event and appends it to the specified StringBuilder.
   *
   * @param logEvent entire log content data source to render
   * @param target logging text aggregator of the final log message
   */
  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    String value = MDC.get(key);
    target.append(value == null ? "" : value);
  }
}
