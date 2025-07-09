package elf4j.engine.logging;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import elf4j.Logger;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.*;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class LogHandlerManagerTest {
  @BeforeEach
  void setup() {
    NativeLogServiceManager.INSTANCE.restart();
  }

  @AfterAll
  static void cleanUp() {
    NativeLogServiceManager.INSTANCE.restart();
  }

  @Nested
  @Order(Integer.MAX_VALUE) // run shutdown the last
  class shutdown {

    @Test
    void whenLoggingAfterShutdown() {
      elf4j.Logger logger = Logger.instance();
      logger.log("before shutdown");

      NativeLogServiceManager.INSTANCE.shutdown();

      assertThrows(RejectedExecutionException.class, () -> logger.log("after shutdown"));
    }
  }

  @Nested
  @Order(100)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class refresh {
    //        private final PrintStream standardOut = System.out;
    //        private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    //
    //        @BeforeEach
    //        public void setUp() {
    //            System.setOut(new PrintStream(outputStreamCaptor));
    //        }
    //
    //        @AfterEach
    //        public void tearDown() {
    //            System.setOut(standardOut);
    //        }

    @Test
    @Order(20)
    void whenForcingToNoop() {
      Logger logger = Logger.instance();
      logger.log("before noop set true, this is showing up in system console");

      NativeLogServiceManager.INSTANCE.restart(null);

      logger.log("after noop set true, this is not showing in system console");
    }

    @Test
    @Order(10)
    void whenSetWithDifferentPropertiesThanLoaded() {
      Logger logger = Logger.instance();
      logger.log(
          "before refresh, {} is to print with configuration properties loaded from configuration file",
          logger);

      Properties properties = new Properties();
      NativeLogServiceManager.INSTANCE.restart(properties);

      Logger configurationPropertiesChanged = Logger.instance();
      assertSame(logger, configurationPropertiesChanged);
      configurationPropertiesChanged.log(
          "after refresh, the same logger instance {} is to print with newly set configuration properties {}",
          configurationPropertiesChanged,
          properties);
    }
  }
}
