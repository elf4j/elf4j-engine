package elf4j.engine.logging.pattern.element;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import elf4j.engine.logging.LogEvent;
import org.junit.jupiter.api.Test;

class ClassElementTest {

  @Test
  void fromCreatesClassElementForValidPattern() {
    ClassPattern classPattern = ClassPattern.from("CLASS");
    assertNotNull(classPattern);
    assertTrue(classPattern.nameSpacePattern().requiresCallerDetail());
  }

  @Test
  void fromThrowsExceptionForInvalidPattern() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ClassPattern.from("INVALID"));
    assertTrue(exception.getMessage().contains("Unexpected predefined pattern element"));
  }

  @Test
  void renderDelegatesToNameSpaceElement() {
    NameSpacePattern nameSpacePattern = mock(NameSpacePattern.class);
    ClassPattern classPattern = new ClassPattern(nameSpacePattern);
    LogEvent logEvent = mock(LogEvent.class);
    StringBuilder target = new StringBuilder();

    // BDD style
    willDoNothing().given(nameSpacePattern).render(logEvent, target);

    classPattern.render(logEvent, target);

    then(nameSpacePattern).should().render(logEvent, target);
  }

  @Test
  void includeCallerDetailReturnsCorrectValue() {
    NameSpacePattern nameSpacePattern = mock(NameSpacePattern.class);
    given(nameSpacePattern.requiresCallerDetail()).willReturn(true);
    ClassPattern classPattern = new ClassPattern(nameSpacePattern);

    assertTrue(classPattern.requiresCallerDetail());
  }
}
