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

package elf4j.engine.service;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.configuration.LoggerOutputLevelThreshold;
import elf4j.engine.service.util.StackTraces;
import elf4j.engine.service.writer.GroupWriter;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.IeLogger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;

/**
 * The EventingNativeLoggerService class implements the NativeLoggerService interface and is
 * responsible for converting a log request into an event for async processing. It provides methods
 * for checking if the log should include caller detail, for checking if a logger is enabled, and
 * for logging a log event.
 */
public class EventingNativeLoggerService implements NativeLoggerService {
  private final boolean noop;
  private final LogWriter logWriter;
  private final LoggerOutputLevelThreshold loggerOutputLevelThreshold;
  private final Map<NativeLogger, Boolean> loggerEnabled = new ConcurrentHashMap<>();

  /**
   * Constructor for the EventingNativeLoggerService class.
   *
   * @param logServiceConfiguration parsed configuration for the logger service
   */
  public EventingNativeLoggerService(@NonNull LogServiceConfiguration logServiceConfiguration) {
    if (logServiceConfiguration.isAbsent() || logServiceConfiguration.isTrue("noop")) {
      noop = true;
      IeLogger.WARN.log("No-op per configuration {}", logServiceConfiguration);
      logWriter = null;
      loggerOutputLevelThreshold = null;
      return;
    }
    noop = false;
    logWriter = GroupWriter.from(logServiceConfiguration);
    loggerOutputLevelThreshold = LoggerOutputLevelThreshold.from(logServiceConfiguration);
  }

  /**
   * Checks if the log should include caller detail such as method, line number, etc.
   *
   * @return false as the context element does not include caller detail
   */
  @Override
  public boolean includeCallerDetail() {
    return logWriter.includeCallerDetail();
  }

  /**
   * Checks if a logger is enabled.
   *
   * @param nativeLogger the logger to check
   * @return true if the logger is enabled, false otherwise
   */
  @Override
  public boolean isEnabled(NativeLogger nativeLogger) {
    if (noop) {
      return false;
    }
    return loggerEnabled.computeIfAbsent(nativeLogger, logger -> {
      Level level = logger.getLevel();
      return level.compareTo(loggerOutputLevelThreshold.getThresholdOutputLevel(logger)) >= 0
          && level.compareTo(logWriter.getThresholdOutputLevel()) >= 0;
    });
  }

  /**
   * Logs a log event.
   *
   * @param nativeLogger the logger to use
   * @param serviceInterfaceClass the class of the service interface
   * @param throwable the throwable to log
   * @param message the message to log
   * @param arguments the arguments to the message
   */
  @Override
  public void log(
      @NonNull NativeLogger nativeLogger,
      @NonNull Class<?> serviceInterfaceClass,
      Throwable throwable,
      Object message,
      Object[] arguments) {
    if (!this.isEnabled(nativeLogger)) {
      return;
    }
    Thread callerThread = Thread.currentThread();
    logWriter.write(LogEvent.builder()
        .callerThread(new LogEvent.ThreadValue(callerThread.getName(), callerThread.threadId()))
        .nativeLogger(nativeLogger)
        .throwable(throwable)
        .message(message)
        .arguments(arguments)
        .serviceInterfaceClass(serviceInterfaceClass)
        .callerFrame(
            includeCallerDetail()
                ? LogEvent.StackFrameValue.from(StackTraces.getCallerFrame(
                    serviceInterfaceClass, new Throwable().getStackTrace()))
                : null)
        .build());
  }
}
