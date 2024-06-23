package elf4j.engine.service.configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ByClassNameSpaceTest {
  @Nested
  class compare {
    LoggerOutputLevelThreshold.ByClassNameSpace byClassNameSpace =
        new LoggerOutputLevelThreshold.ByClassNameSpace();

    @Test
    void whenMorePackageLevels_thenGoesFirst() {
      assertTrue(byClassNameSpace.compare("p1", "") < 0);
      assertTrue(byClassNameSpace.compare("p1.ClassName", "ClassName") < 0);
      assertTrue(byClassNameSpace.compare("p1.ClassName", "ClassNameLooooooooooooooooooooog") < 0);
      assertTrue(byClassNameSpace.compare("p1.ClassName", "p1") < 0);
      assertTrue(byClassNameSpace.compare("p1.p2", "p1") < 0);
    }

    @Test
    void whenSamePackageLevelsAndLengths_thenDictionaryOrder() {
      assertTrue(byClassNameSpace.compare("p1.ClassName", "p2.ClassName") < 0);
      assertTrue(byClassNameSpace.compare("p.ClassName1", "p.ClassName2") < 0);
      assertTrue(byClassNameSpace.compare("p1", "p2") < 0);
    }

    @Test
    void whenSameLevels_thenLongerNameSpaceGoesFirstAndDefaultSpaceAlwaysLast() {
      assertTrue(byClassNameSpace.compare("p1.ClassNameLL", "p2.ClassNameL") < 0);
      assertTrue(byClassNameSpace.compare("ClassName", "") < 0);
      assertTrue(byClassNameSpace.compare("p1", "") < 0);
    }
  }
}
