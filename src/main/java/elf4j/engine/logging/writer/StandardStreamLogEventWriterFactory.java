package elf4j.engine.logging.writer;

import elf4j.engine.logging.configuration.ConfigurationProperties;
import elf4j.engine.logging.pattern.CompositeRenderingPattern;
import java.util.Properties;

/**
 * Produces the default StandardStreamWriter based on the provided ConfigurationProperties.
 *
 * <p>To work with elf4j-engine, any implementation of the {@link LogEventWriterFactory} interface
 * must have an accessible no-arg constructor
 */
final class StandardStreamLogEventWriterFactory implements LogEventWriterFactory {
  public StandardStreamLogEventWriterFactory() { // no-arg constructor required
  }

  @Override
  public LogEventWriter getWriter(Properties configurationProperties) {
    return new StandardStreamLogEventWriter(
        CompositeRenderingPattern.from(configurationProperties.getProperty(
            ConfigurationProperties.PATTERN, StandardStreamLogEventWriter.DEFAULT_PATTERN)),
        StandardStreamLogEventWriter.OutStreamType.valueOf(configurationProperties
            .getProperty(
                ConfigurationProperties.STREAM,
                StandardStreamLogEventWriter.DEFAULT_OUT_STREAM_TYPE.name())
            .trim()
            .toUpperCase()));
  }
}
