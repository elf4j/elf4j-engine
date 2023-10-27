package elf4j.engine.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import elf4j.Logger;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LogServiceManagerTest {

    @AfterAll
    static void cleanUp() {
        LogServiceManager.INSTANCE.refresh();
    }

    @Nested
    class shutdown {
        @Test
        void whenLoggingAfterShutdown() {
            elf4j.Logger logger = Logger.instance();
            logger.log("before shutdown");

            LogServiceManager.INSTANCE.shutdown();

            assertThrows(RejectedExecutionException.class, () -> logger.log("after shutdown"));
        }
    }

    @Nested
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
        void whenForcingToNoop() {
            Logger logger = Logger.instance();
            logger.log("before noop set true, this is showing up in system console");

            LogServiceManager.INSTANCE.refresh(null);

            logger.log("after noop set true, this is not showing in system console");
        }

        @Test
        void whenSetWithDifferentPropertiesThanLoaded() {
            Logger withLoadedProperties = Logger.instance();
            withLoadedProperties.log(
                    "before refresh, {} is to print with withLoadedProperties properties configuration",
                    withLoadedProperties);

            LogServiceManager.INSTANCE.refresh(new Properties());

            Logger withSetProperties = Logger.instance();
            assertSame(withLoadedProperties, withSetProperties);
            withSetProperties.log(
                    "after refresh, the same logger instance {} is to print with newly set properties configuration",
                    withSetProperties);
        }
    }
}
