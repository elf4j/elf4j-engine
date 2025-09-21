package elf4j.engine.logging.util;

import elf4j.Logger;
import elf4j.engine.logging.NativeLogServiceManager;
import elf4j.util.UtilLogger;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

public class Elf4jEngineTestExecutionListener implements TestExecutionListener {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static boolean shutdownHookRegistered = false;

  @Override
  public void testPlanExecutionStarted(TestPlan testPlan) {
    if (!shutdownHookRegistered) {
      LOGGER.info("Registering elf4j-engine shutdown hook before any planned test...");
      shutdownHookRegistered = true;
      Runtime.getRuntime()
          .addShutdownHook(NativeLogServiceManager.INSTANCE.getShutdownHookThread());
      return;
    }
    LOGGER.info("Unit test shutdown hook already registered");
  }

  @Override
  public void testPlanExecutionFinished(TestPlan testPlan) {
    long delay = 2000;
    LOGGER.info("All planned tests finished, delaying test engine exit: delay={} ms", delay);
    try {
      Thread.sleep(delay); // 2s delay before engine exits
    } catch (InterruptedException e) {
      LOGGER.warn("Interrupted while delaying test engine exit", e);
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    }
  }
}
