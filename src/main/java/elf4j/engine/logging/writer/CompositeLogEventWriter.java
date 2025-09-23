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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.awaitility.Awaitility.await;

import conseq4j.execute.ConseqExecutor;
import elf4j.Logger;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.NativeLogServiceManager;
import elf4j.engine.logging.configuration.ConfigurationProperties;
import elf4j.util.UtilLogger;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

/**
 * @implNote Log events are usually asynchronously written/rendered in parallel by multiple
 *     concurrent threads. However, events issued by the same caller application thread are rendered
 *     sequentially with the {@link ConseqExecutor} API. Thus, logs by different caller threads may
 *     arrive at the final destination (e.g. system Console or a log file) in any order; meanwhile,
 *     logs from the same caller thread will arrive sequentially in the same order as they are
 *     called by such thread.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class CompositeLogEventWriter implements LogEventWriter, NativeLogServiceManager.Stoppable {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static final LogEventWriterFactory DEFAULT_WRITER_FACTORY =
      new StandardStreamLogEventWriterFactory();

  /** Composed writers are created based on configuration properties. */
  @EqualsAndHashCode.Include
  private final List<LogEventWriter> writers;

  /**
   * The async executor's concurrency is based on configuration properties. If omitted, the default
   * concurrency is determined by the <a href="https://q3769.github.io/conseq4j">conseq4j API</a>
   */
  private final ConseqExecutor conseqExecutor;

  /**
   * {@code true} if any of the configured log writer's log pattern requires run-time caller detail,
   * cached after deriving from the writers.
   */
  private @Nullable Boolean includeCallerDetail;

  private CompositeLogEventWriter(List<LogEventWriter> writers, ConseqExecutor conseqExecutor) {
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
  public static CompositeLogEventWriter from(ConfigurationProperties configurationProperties) {
    List<LogEventWriterFactory> configuredWriterFactories =
        new ArrayList<>(getLogWriterFactories(configurationProperties));
    if (configuredWriterFactories.isEmpty()) {
      configuredWriterFactories.add(DEFAULT_WRITER_FACTORY);
    }
    List<LogEventWriter> logEventWriters = configuredWriterFactories.stream()
        .map(logEventWriterFactory ->
            logEventWriterFactory.getWriter(configurationProperties.properties()))
        .toList();
    return new CompositeLogEventWriter(
        logEventWriters,
        Optional.ofNullable(
                configurationProperties.getAsInteger(ConfigurationProperties.CONCURRENCY))
            .map(ConseqExecutor::instance)
            .orElse(ConseqExecutor.instance()));
  }

  private static List<LogEventWriterFactory> getLogWriterFactories(
      ConfigurationProperties configurationProperties) {
    if (configurationProperties.isAbsent()) {
      return Collections.emptyList();
    }
    Properties properties = configurationProperties.properties();
    String writerFactoryClassNames =
        properties.getProperty(ConfigurationProperties.WRITER_FACTORIES);
    if (writerFactoryClassNames == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(writerFactoryClassNames.split(","))
        .map(String::strip)
        .filter(strip -> !isNullOrEmpty(strip))
        .map(fqcn -> {
          try {
            return (LogEventWriterFactory)
                Class.forName(fqcn).getDeclaredConstructor().newInstance();
          } catch (InstantiationException
              | IllegalAccessException
              | InvocationTargetException
              | NoSuchMethodException
              | ClassNotFoundException e) {
            LOGGER.error(
                "Unable to construct log writer factory: fqcn='%s' - it must have a no-arg constructor and of type %s"
                    .formatted(fqcn, LogEventWriterFactory.class),
                e);
            throw new IllegalArgumentException(
                "Error instantiating writer class '%s'".formatted(fqcn), e);
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
  public void write(LogEvent logEvent) {
    writers.forEach(writer -> conseqExecutor.execute(
        withMdcContext(() -> writer.write(logEvent)), logEvent.callerThread().id()));
  }

  @Override
  public boolean requiresCallerDetail() {
    if (includeCallerDetail == null) {
      includeCallerDetail = writers.stream().anyMatch(LogEventWriter::requiresCallerDetail);
    }
    return includeCallerDetail;
  }

  @Override
  public void stop() {
    if (conseqExecutor.isTerminated()) {
      return;
    }
    LOGGER.info("Stopping %s".formatted(this));
    conseqExecutor.shutdown();
    Duration timeout = Duration.ofSeconds(30);
    try (conseqExecutor) {
      await().atMost(timeout).until(conseqExecutor::isTerminated);
      LOGGER.info("Stopped %s".formatted(this));
    } catch (Exception e) {
      LOGGER.warn(
          "Writer executor %s still not terminated after %s".formatted(conseqExecutor, timeout), e);
    }
  }
}
