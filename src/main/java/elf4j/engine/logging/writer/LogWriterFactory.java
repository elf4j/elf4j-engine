package elf4j.engine.logging.writer;

import java.util.Properties;

public interface LogWriterFactory {
  /**
   * Returns a list of log writers of the enclosing writer type based on the provided logging
   * configuration.
   *
   * @param configurationProperties the entire log service configuration
   * @return all log writers of the enclosing writer type from the given configuration
   */
  LogWriter getLogWriter(Properties configurationProperties);
}
