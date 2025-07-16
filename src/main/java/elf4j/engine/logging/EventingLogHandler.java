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

package elf4j.engine.logging;

import elf4j.Level;
import elf4j.engine.logging.config.ConfigurationProperties;
import elf4j.engine.logging.config.LoggerOutputLevelThreshold;
import elf4j.engine.logging.util.StackTraces;
import elf4j.engine.logging.writer.CompositeWriter;
import elf4j.engine.logging.writer.LogWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/**
 * The EventingLogHandler class implements the LogHandler interface and is responsible for
 * converting a log request into an event for async processing. It provides methods for checking if
 * the log should include caller detail, for checking if a logger is enabled, and for logging a log
 * event.
 */
@Value
@EqualsAndHashCode
public class EventingLogHandler implements LogHandler {
  private static final Logger LOGGER = Logger.getLogger(EventingLogHandler.class.getName());

  Class<?> logServiceClass;
  boolean noop;

  @Nullable LogWriter logWriter;

  @Nullable LoggerOutputLevelThreshold loggerOutputLevelThreshold;

  @EqualsAndHashCode.Exclude
  Map<String, Boolean> loggerNameEnablements = new ConcurrentHashMap<>();

  /**
   * Constructor for the EventingLogHandler class.
   *
   * @param configurationProperties parsed configuration for the logger service
   */
  public EventingLogHandler(
      ConfigurationProperties configurationProperties, Class<?> logServiceClass) {
    this.logServiceClass = logServiceClass;
    if (configurationProperties.isAbsent() || configurationProperties.isTrue("noop")) {
      noop = true;
      LOGGER.warning("No-op per configuration %s".formatted(configurationProperties));
      logWriter = null;
      loggerOutputLevelThreshold = null;
      return;
    }
    noop = false;
    logWriter = CompositeWriter.from(configurationProperties);
    loggerOutputLevelThreshold = LoggerOutputLevelThreshold.from(configurationProperties);
  }

  /**
   * Checks if the log should include caller detail such as method, line number, etc.
   *
   * @return false as the context element does not include caller detail
   */
  @Override
  public boolean includeCallerDetail() {
    assert !noop;
    assert logWriter != null;
    return logWriter.includeCallerDetail();
  }

  /**
   * Checks if a logger is enabled.
   *
   * @param level the desired level of this particular log invocation
   * @param loggerName whose threshold level is to be checked
   * @return true if the logger is enabled, false otherwise
   */
  @Override
  public boolean isEnabled(Level level, String loggerName) {
    if (noop) {
      return false;
    }
    assert loggerOutputLevelThreshold != null;
    assert logWriter != null;
    return loggerNameEnablements.computeIfAbsent(loggerName, k -> {
      if (level.compareTo(loggerOutputLevelThreshold.getThresholdOutputLevel(k)) < 0) return false;
      return level.compareTo(logWriter.getThresholdOutputLevel()) >= 0;
    });
  }

  @Override
  public void log(
      Level level,
      String loggerName,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments) {
    if (!this.isEnabled(level, loggerName)) {
      return;
    }
    assert logWriter != null;
    Thread callerThread = Thread.currentThread();
    logWriter.write(LogEvent.builder()
        .callerThread(new LogEvent.ThreadValue(callerThread.getName(), callerThread.threadId()))
        .level(level)
        .throwable(throwable)
        .message(message)
        .arguments(arguments)
        .loggerName(loggerName)
        .callerFrame(
            includeCallerDetail()
                ? LogEvent.StackFrameValue.from(
                    StackTraces.callerFrameOf(logServiceClass.getName()))
                : null)
        .build());
  }
}
