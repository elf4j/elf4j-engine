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

import elf4j.Logger;
import elf4j.engine.NativeLogger;
import elf4j.engine.logging.config.ConfigurationProperties;
import elf4j.engine.logging.config.LoggerOutputMinimumLevelThreshold;
import elf4j.engine.logging.util.StackTraces;
import elf4j.engine.logging.writer.CompositeWriter;
import elf4j.engine.logging.writer.LogWriter;
import elf4j.util.UtilLogger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/** Processing a logging request by converting it into an event for async processing. */
@Value
public class EventingLogHandler implements LogHandler {
  private static final Logger LOGGER = UtilLogger.WARN;

  boolean noop;

  @Nullable LogWriter logWriter;

  @Nullable LoggerOutputMinimumLevelThreshold loggerOutputMinimumLevelThreshold;

  @EqualsAndHashCode.Exclude
  Map<NativeLogger.LoggerId, Boolean> loggerEnablements = new ConcurrentHashMap<>();

  /**
   * Constructor for the EventingLogHandler class.
   *
   * @param configurationProperties parsed configuration for the logger service
   */
  public EventingLogHandler(ConfigurationProperties configurationProperties) {
    if (configurationProperties.isAbsent() || configurationProperties.isTrue("noop")) {
      noop = true;
      LOGGER.warn("No-op per configuration %s".formatted(configurationProperties));
      logWriter = null;
      loggerOutputMinimumLevelThreshold = null;
      return;
    }
    noop = false;
    logWriter = CompositeWriter.from(configurationProperties);
    loggerOutputMinimumLevelThreshold =
        LoggerOutputMinimumLevelThreshold.from(configurationProperties);
  }

  /**
   * Checks if a logger is enabled.
   *
   * @return true if the logger is enabled, false otherwise
   */
  @Override
  public boolean isEnabled(NativeLogger.LoggerId loggerId) {
    if (noop) {
      return false;
    }
    assert loggerOutputMinimumLevelThreshold != null;
    assert logWriter != null;
    return loggerEnablements.computeIfAbsent(
        loggerId,
        k -> k.level()
                    .compareTo(
                        loggerOutputMinimumLevelThreshold.getMinimumThresholdLevel(k.loggerName()))
                >= 0
            && k.level().compareTo(logWriter.getMinimumThresholdLevel()) >= 0);
  }

  @Override
  public void log(
      NativeLogger.LoggerId loggerId,
      Set<String> logServiceClassNames,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments) {
    if (!isEnabled(loggerId)) {
      return;
    }
    assert logWriter != null;
    Thread callerThread = Thread.currentThread();
    logWriter.write(LogEvent.builder()
        .callerThread(new LogEvent.ThreadValue(callerThread.getName(), callerThread.threadId()))
        .level(loggerId.level())
        .throwable(throwable)
        .message(message)
        .arguments(arguments)
        .loggerName(loggerId.loggerName())
        .callerFrame(
            logWriter.requiresCallerDetail()
                ? LogEvent.StackFrameValue.from(
                    StackTraces.earliestCallerOfAny(logServiceClassNames))
                : null)
        .build());
  }
}
