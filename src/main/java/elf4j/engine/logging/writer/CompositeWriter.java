/*
 * MIT License
 *
 * Copyright (c) 2023 Qingtian Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package elf4j.engine.logging.writer;

import conseq4j.execute.ConseqExecutor;
import elf4j.Level;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.NativeLogServiceManager;
import elf4j.engine.logging.config.ConfigurationProperties;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

/**
 * In general, log events are asynchronously written/rendered in parallel by multiple concurrent
 * threads. However, events issued by the same caller application thread are rendered sequentially
 * with the {@link ConseqExecutor} API. Thus, logs by different caller threads may arrive at the
 * final destination (e.g. system Console or a log file) in any order; meanwhile, logs from the same
 * caller thread will arrive sequentially in the same order as they are called in the original
 * thread.
 */
@ToString
public class CompositeWriter implements LogWriter, NativeLogServiceManager.Stoppable {
  private static final Logger LOGGER = Logger.getLogger(CompositeWriter.class.getName());
  private final List<LogWriter> writers;
  private final ConseqExecutor conseqExecutor;
  private @Nullable Level thresholdOutputLevel;

  @ToString.Exclude
  private @Nullable Boolean includeCallerDetail;

  private CompositeWriter(List<LogWriter> writers, ConseqExecutor conseqExecutor) {
    this.writers = writers;
    this.conseqExecutor = conseqExecutor;
    LOGGER.info("%s service writer(s) in %s".formatted(writers.size(), this));
    NativeLogServiceManager.INSTANCE.register(this);
  }

  /**
   * Creates a CompositeWriter instance from the provided ConfigurationProperties.
   *
   * @param configurationProperties entire configuration
   * @return the composite writer containing all writers configured in the specified properties
   */
  public static CompositeWriter from(ConfigurationProperties configurationProperties) {
    List<LogWriterFactory> configuredLogWriters =
        new ArrayList<>(getLogWriterFactories(configurationProperties));
    if (configuredLogWriters.isEmpty()) {
      configuredLogWriters.add(new StandardStreamWriterFactory());
    }
    List<LogWriter> logWriters = configuredLogWriters.stream()
        .map(t -> t.getLogWriter(configurationProperties.getProperties()))
        .toList();
    return new CompositeWriter(
        logWriters,
        Optional.ofNullable(configurationProperties.getAsInteger("concurrency"))
            .map(ConseqExecutor::instance)
            .orElse(ConseqExecutor.instance()));
  }

  private static List<LogWriterFactory> getLogWriterFactories(
      ConfigurationProperties configurationProperties) {
    if (configurationProperties.isAbsent()) {
      return Collections.emptyList();
    }
    Properties properties = configurationProperties.getProperties();
    String writerTypes = properties.getProperty("writer.factories");
    if (writerTypes == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(writerTypes.split(","))
        .map(String::trim)
        .map(fqcn -> {
          try {
            return (LogWriterFactory)
                Class.forName(fqcn).getDeclaredConstructor().newInstance();
          } catch (InstantiationException
              | IllegalAccessException
              | InvocationTargetException
              | NoSuchMethodException
              | ClassNotFoundException e) {
            LOGGER.severe(
                "Unable to construct log writer factory: fqcn=%s - it must have a no-arg constructor and of type %s"
                    .formatted(fqcn, LogWriterFactory.class));
            throw new IllegalArgumentException("Error instantiating: %s".formatted(fqcn), e);
          }
        })
        .collect(Collectors.toList());
  }

  private static Runnable withMdcContext(Runnable task) {
    Map<String, String> callerContext = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> workerContext = switchContextTo(callerContext);
      MDC.setContextMap(callerContext);
      try {
        task.run();
      } finally {
        MDC.setContextMap(workerContext);
      }
    };
  }

  private static Map<String, String> switchContextTo(Map<String, String> targetContext) {
    Map<String, String> replaced = MDC.getCopyOfContextMap();
    MDC.setContextMap(targetContext);
    return replaced;
  }

  @Override
  public Level getThresholdOutputLevel() {
    if (thresholdOutputLevel == null) {
      thresholdOutputLevel = Level.values()[
          writers.stream()
              .mapToInt(writer -> writer.getThresholdOutputLevel().ordinal())
              .min()
              .orElseThrow(NoSuchElementException::new)];
    }
    return thresholdOutputLevel;
  }

  @Override
  public void write(LogEvent logEvent) {
    writers.forEach(writer -> conseqExecutor.execute(
        withMdcContext(() -> writer.write(logEvent)), logEvent.getCallerThread().id()));
  }

  @Override
  public boolean includeCallerDetail() {
    if (includeCallerDetail == null) {
      includeCallerDetail = writers.stream().anyMatch(LogWriter::includeCallerDetail);
    }
    return includeCallerDetail;
  }

  @Override
  public void stop() {
    if (conseqExecutor.isTerminated()) {
      return;
    }
    LOGGER.info("Stopping %s".formatted(this));
    conseqExecutor.close();
  }
}
