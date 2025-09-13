package elf4j.engine.logging.pattern.element;

import com.google.common.collect.Iterables;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.NoSuchElementException;
import org.slf4j.MDC;

/**
 * The ContextElement class implements the PatternElement interface and represents a context element
 * in a log pattern. It provides methods for checking if the log should include caller detail, for
 * creating a new instance from a pattern element, and for rendering the log event.
 */
public record ContextPattern(String key) implements RenderingPattern {
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
   * Creates a new ContextElement instance from the element pattern to print thread scoped context.
   *
   * @param elementPattern the context pattern text without the enclosing braces, i.e. In the log
   *     configuration pattern {@code {time} {level} {context} - {message}}, the
   *     {@code elementPattern} passed in this method is expected to be {@code context}
   * @return the element that can render context log
   * @throws NoSuchElementException if no key is configured in the 'context' pattern element
   */
  public static ContextPattern from(String elementPattern) {
    if (ElementPatternType.CONTEXT != ElementPatternType.from(elementPattern)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", elementPattern));
    }
    return new ContextPattern(
        Iterables.getOnlyElement(ElementPatternType.getElementDisplayOptions(elementPattern)));
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
