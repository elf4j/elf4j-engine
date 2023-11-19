package elf4j.engine.service.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StackTraceUtilsTest {

    @Nested
    class getCallerFrame {
        @Test
        void whenCalleeClassIsNotFoundInCallStack() {
            assertThrows(
                    NoSuchElementException.class,
                    () -> StackTraceUtils.getCallerFrame(NotInCallstack.class, new Throwable().getStackTrace()));
        }

        class NotInCallstack {}
    }
}
