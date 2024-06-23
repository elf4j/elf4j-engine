package elf4j.engine.service.pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LogPatternTest {

  @Nested
  class parsePattern {
    @Test
    void whenPredefinedElementIsUnrecognized() {
      assertThrows(
          IllegalArgumentException.class, () -> LogPattern.from("{testUnrecognizedPredefined}"));
    }
  }
}
