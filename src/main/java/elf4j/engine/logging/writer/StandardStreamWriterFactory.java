package elf4j.engine.logging.writer;

import elf4j.Level;
import elf4j.engine.logging.pattern.LogPattern;
import java.util.Properties;

/** Produces the default StandardStreamWriter based on the provided ConfigurationProperties. */
class StandardStreamWriterFactory implements LogWriterFactory {
  private static StandardStreamWriter getDefaultWriter(Properties configurationProperties) {
    return StandardStreamWriter.builder()
        .thresholdOutputLevel(Level.valueOf(configurationProperties
            .getProperty("level", StandardStreamWriter.DEFAULT_THRESHOLD_OUTPUT_LEVEL)
            .trim()
            .toUpperCase()))
        .logPattern(LogPattern.from(
            configurationProperties.getProperty("pattern", StandardStreamWriter.DEFAULT_PATTERN)))
        .outStreamType(StandardStreamWriter.OutStreamType.valueOf(configurationProperties
            .getProperty("stream", StandardStreamWriter.DEFAULT_OUT_STREAM_TYPE.name())
            .trim()
            .toUpperCase()))
        .build();
  }

  @Override
  public LogWriter getLogWriter(Properties configurationProperties) {
    return getDefaultWriter(configurationProperties);
  }
}
