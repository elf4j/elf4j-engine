package elf4j.engine.logging;

/**
 * The LogHandlerFactory interface provides methods for getting the log handler. Capable of
 * reconfiguring the log handler with the specified properties at runtime.
 */
public interface LogHandlerFactory {
  /**
   * Gets the log service.
   *
   * @return the log service
   */
  LogHandler getLogHandler();
}
