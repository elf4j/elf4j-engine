package elf4j.engine.logging.pattern.element;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.CompositeRenderingPattern;
import elf4j.engine.logging.pattern.RenderingPattern;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompositeRenderingPatternTest {

  @Nested
  class parsePattern {
    @Test
    void whenNoInterpretingElementIsRecognized() {
      Assertions.assertThat(
              CompositeRenderingPattern.from("{testUnrecognizedPredefined}").patternElements())
          .hasSize(1)
          .element(0)
          .isInstanceOf(VerbatimPattern.class);
    }
  }

  @Nested
  class render {
    @Mock
    RenderingPattern mockPattern;

    @Mock
    RenderingPattern mockPattern2;

    @Mock
    LogEvent stubLogEvent;

    @BeforeEach
    void setUp() {
      patternGroupEntry = new CompositeRenderingPattern(Arrays.asList(mockPattern2, mockPattern));
    }

    CompositeRenderingPattern patternGroupEntry;

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
