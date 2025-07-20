package elf4j.engine.logging.util;

import elf4j.Logger;
import elf4j.engine.logging.NativeLogServiceManager;
import elf4j.util.UtilLogger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class ShutdownUnitTestExtension implements BeforeAllCallback {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static boolean shutdownHookRegistered = false;

  @Override
  public void beforeAll(ExtensionContext context) {
    if (!shutdownHookRegistered) {
      LOGGER.info("Registering unit test shutdown hook...");
      shutdownHookRegistered = true;
      Runtime.getRuntime()
          .addShutdownHook(NativeLogServiceManager.INSTANCE.getShutdownHookThread());
      return;
    }
    LOGGER.info("Unit test shutdown hook already registered");
  }
}
