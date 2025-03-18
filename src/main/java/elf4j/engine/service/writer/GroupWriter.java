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

package elf4j.engine.service.writer;

import conseq4j.execute.ConseqExecutor;
import elf4j.Level;
import elf4j.engine.service.LogEvent;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.util.IeLogger;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.ToString;
import org.slf4j.MDC;

/**
 * In general, log events are asynchronously written/rendered in parallel by multiple concurrent
 * threads. However, events issued by the same caller application thread are rendered sequentially
 * with the {@link ConseqExecutor} API. Thus, logs by different caller threads may arrive at the
 * final destination (e.g. system Console or a log file) in any order; meanwhile, logs from the same
 * caller thread will arrive sequentially in the same order as they are called in the original
 * thread.
 */
public class GroupWriter implements LogWriter {
  private static final int DEFAULT_CONCURRENCY = Runtime.getRuntime().availableProcessors();
  private final List<LogWriter> writers;
  private final ConseqExecutor conseqExecutor;
  private Level thresholdOutputLevel;

  @ToString.Exclude
  private Boolean includeCallerDetail;

  private GroupWriter(@NonNull List<LogWriter> writers, ConseqExecutor conseqExecutor) {
    this.writers = writers;
    this.conseqExecutor = conseqExecutor;
    IeLogger.INFO.log("{} service writer(s) in {}", writers.size(), this);
  }

  /**
   * Creates a GroupWriter instance from the provided LogServiceConfiguration.
   *
   * @param logServiceConfiguration entire configuration
   * @return the composite writer containing all writers configured in the specified properties
   */
  @NonNull public static GroupWriter from(LogServiceConfiguration logServiceConfiguration) {
    List<LogWriterType> logWriterTypes =
        new ArrayList<>(getLogWriterTypes(logServiceConfiguration));
    if (logWriterTypes.isEmpty()) {
      logWriterTypes.add(new StandardStreamWriter.Type());
    }
    List<LogWriter> logWriters = logWriterTypes.stream()
        .flatMap(t -> t.getLogWriters(logServiceConfiguration).stream())
        .collect(Collectors.toList());
    return new GroupWriter(
        logWriters, ConseqExecutor.instance(getConcurrency(logServiceConfiguration)));
  }

  private static int getConcurrency(@NonNull LogServiceConfiguration logServiceConfiguration) {
    int concurrency = logServiceConfiguration.getIntOrDefault("concurrency", DEFAULT_CONCURRENCY);
    IeLogger.INFO.log("Concurrency: {}", concurrency);
    if (concurrency < 1) {
      IeLogger.ERROR.log("Unexpected concurrency: {}, cannot be less than 1", concurrency);
      throw new IllegalArgumentException("concurrency: " + concurrency);
    }
    return concurrency;
  }

  private static List<LogWriterType> getLogWriterTypes(
      @NonNull LogServiceConfiguration logServiceConfiguration) {
    if (logServiceConfiguration.isAbsent()) {
      return Collections.emptyList();
    }
    Properties properties = logServiceConfiguration.getProperties();
    String writerTypes = properties.getProperty("writer.types");
    if (writerTypes == null) {
      return Collections.emptyList();
    }
    return Arrays.stream(writerTypes.split(","))
        .map(String::trim)
        .map(fqcn -> {
          try {
            return (LogWriterType) Class.forName(fqcn).getDeclaredConstructor().newInstance();
          } catch (InstantiationException
              | IllegalAccessException
              | InvocationTargetException
              | NoSuchMethodException
              | ClassNotFoundException e) {
            throw new IllegalArgumentException("Error instantiating: " + fqcn, e);
          }
        })
        .collect(Collectors.toList());
  }

  private static @NonNull Runnable withMdcContext(Runnable task) {
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
  public void write(@NonNull LogEvent logEvent) {
    writers.forEach(writer -> conseqExecutor.execute(
        withMdcContext(() -> writer.write(logEvent)), logEvent.getCallerThread().getId()));
  }

  @Override
  public boolean includeCallerDetail() {
    if (includeCallerDetail == null) {
      includeCallerDetail = writers.stream().anyMatch(LogWriter::includeCallerDetail);
    }
    return includeCallerDetail;
  }
}
