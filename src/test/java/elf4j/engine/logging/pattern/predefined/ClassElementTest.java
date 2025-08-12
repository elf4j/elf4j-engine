package elf4j.engine.logging.pattern.predefined;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import elf4j.engine.logging.LogEvent;
import org.junit.jupiter.api.Test;

class ClassElementTest {

  @Test
  void fromCreatesClassElementForValidPattern() {
    ClassElement classElement = ClassElement.from("CLASS");
    assertNotNull(classElement);
    assertTrue(classElement.nameSpaceElement().includeCallerDetail());
  }

  @Test
  void fromThrowsExceptionForInvalidPattern() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> ClassElement.from("INVALID"));
    assertTrue(exception.getMessage().contains("Unexpected predefined pattern element"));
  }

  @Test
  void renderDelegatesToNameSpaceElement() {
    NameSpaceElement nameSpaceElement = mock(NameSpaceElement.class);
    ClassElement classElement = new ClassElement(nameSpaceElement);
    LogEvent logEvent = mock(LogEvent.class);
    StringBuilder target = new StringBuilder();

    // BDD style
    willDoNothing().given(nameSpaceElement).render(logEvent, target);

    classElement.render(logEvent, target);

    then(nameSpaceElement).should().render(logEvent, target);
  }

  @Test
  void includeCallerDetailReturnsCorrectValue() {
    NameSpaceElement nameSpaceElement = mock(NameSpaceElement.class);
    given(nameSpaceElement.includeCallerDetail()).willReturn(true);
    ClassElement classElement = new ClassElement(nameSpaceElement);

    assertTrue(classElement.includeCallerDetail());
  }
}
