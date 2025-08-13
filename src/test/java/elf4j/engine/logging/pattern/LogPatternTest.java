package elf4j.engine.logging.pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.element.VerbatimElement;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogPatternTest {

  @Nested
  class parsePattern {
    @Test
    void whenNoInterpretingElementIsRecognized() {
      assertThat(LogPattern.from("{testUnrecognizedPredefined}").getPatternElements())
          .hasSize(1)
          .element(0)
          .isInstanceOf(VerbatimElement.class);
    }
  }

  @Nested
  class render {
    @Mock
    PatternElement mockPattern;

    @Mock
    PatternElement mockPattern2;

    @Mock
    LogEvent stubLogEvent;

    @BeforeEach
    void setUp() {
      patternGroupEntry = new LogPattern(Arrays.asList(mockPattern2, mockPattern));
    }

    LogPattern patternGroupEntry;

    @Test
    void dispatchAll() {
      StringBuilder stringBuilder = new StringBuilder();

      patternGroupEntry.render(stubLogEvent, stringBuilder);

      InOrder inOrder = inOrder(mockPattern, mockPattern2);
      then(mockPattern2).should(inOrder).render(stubLogEvent, stringBuilder);
      then(mockPattern).should(inOrder).render(stubLogEvent, stringBuilder);
    }
  }
}
