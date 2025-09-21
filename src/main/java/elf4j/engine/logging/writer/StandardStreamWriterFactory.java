package elf4j.engine.logging.writer;

import elf4j.engine.logging.configuration.ConfigurationProperties;
import elf4j.engine.logging.pattern.LogPattern;
import java.util.Properties;

/**
 * Produces the default StandardStreamWriter based on the provided ConfigurationProperties.
 *
 * <p>To work with elf4j-engine, any implementation of the {@link LogWriterFactory} interface must
 * have an accessible no-arg constructor
 */
final class StandardStreamWriterFactory implements LogWriterFactory {

  public StandardStreamWriterFactory() { // no-arg constructor required
  }

  @Override
  public LogWriter getLogWriter(Properties configurationProperties) {
    return new StandardStreamWriter(
        LogPattern.from(configurationProperties.getProperty(
            ConfigurationProperties.PATTERN, StandardStreamWriter.DEFAULT_PATTERN)),
        StandardStreamWriter.OutStreamType.valueOf(configurationProperties
            .getProperty(
                ConfigurationProperties.STREAM, StandardStreamWriter.DEFAULT_OUT_STREAM_TYPE.name())
            .trim()
            .toUpperCase()));
  }
}
