package elf4j.engine.logging.util;

import elf4j.engine.logging.NativeLogServiceManager;
import java.util.logging.Logger;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class ShutdownUnitTestExtension implements BeforeAllCallback {
  private static final Logger LOGGER = Logger.getLogger(ShutdownUnitTestExtension.class.getName());
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
