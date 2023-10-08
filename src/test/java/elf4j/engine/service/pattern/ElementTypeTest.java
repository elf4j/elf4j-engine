package elf4j.engine.service.pattern;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ElementTypeTest {

    @Nested
    class parsePattern {
        @Test
        void whenPredefinedElementIsUnrecognized() {
            assertThrows(IllegalArgumentException.class,
                    () -> ElementType.parsePattern("{testUnrecognizedPredefined}"));
        }
    }
}
