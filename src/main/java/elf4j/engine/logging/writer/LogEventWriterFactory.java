package elf4j.engine.logging.writer;

import java.util.Properties;

public interface LogEventWriterFactory {
  /**
   * @param configurationProperties the entire log service configuration
   * @return a log event writer instance based on the provided configuration
   */
  LogEventWriter getWriter(Properties configurationProperties);
}
