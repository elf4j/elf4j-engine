package elf4j.engine.logging.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StackTracesTest {

  @Nested
  class getCallerFrame {
    @Test
    void whenCalleeClassIsNotFoundInCallStack() {
      assertThrows(
          NoSuchElementException.class,
          () -> StackTraces.callerFrameOf(NotInCallstack.class.getName()));
    }

    static class NotInCallstack {}
  }
}
