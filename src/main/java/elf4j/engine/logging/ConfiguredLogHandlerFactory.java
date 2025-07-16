package elf4j.engine.logging;

import elf4j.engine.logging.config.ConfigurationProperties;
import java.util.Properties;
import org.jspecify.annotations.Nullable;

/**
 * The ConfiguredLogHandlerFactory class implements the LogHandlerFactory interface and provides a
 * concrete implementation for getting the log service, reloading the log service, and resetting the
 * log service with the specified properties.
 */
public class ConfiguredLogHandlerFactory
    implements LogHandlerFactory, NativeLogServiceManager.Refreshable {
  private final Class<?> logServiceInterfaceClass;
  private LogHandler logHandler;

  /** Constructor for the ConfiguredLogHandlerFactory class. */
  public ConfiguredLogHandlerFactory(Class<?> logServiceClass) {
    this.logServiceInterfaceClass = logServiceClass;
    logHandler = new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceClass);
    NativeLogServiceManager.INSTANCE.register(this);
  }

  /**
   * Gets the log service.
   *
   * @return the log service
   */
  @Override
  public LogHandler getLogHandler() {
    return logHandler;
  }

  /** Reloads the log service. */
  private void reload() {
    logHandler =
        new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceInterfaceClass);
  }

  /**
   * Resets the log service with the specified properties.
   *
   * @param properties the new properties for the log service
   */
  private void reset(@Nullable Properties properties) {
    logHandler = new EventingLogHandler(
        ConfigurationProperties.bySetting(properties), logServiceInterfaceClass);
  }

  @Override
  public void refresh(@Nullable Properties properties) {
    reset(properties);
  }

  @Override
  public void refresh() {
    reload();
  }
}
