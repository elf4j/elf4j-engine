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
import elf4j.engine.service.util.StackTraceUtils;
import elf4j.engine.service.writer.ConseqWriterGroup;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.IeLogger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;

/**
 * converts a log request into an event for async processing
 */
public class EventingLogService implements LogService {
    private final boolean noop;
    private final LogWriter logWriter;
    private final LoggerOutputLevelThreshold loggerOutputLevelThreshold;
    private final Map<NativeLogger, Boolean> loggerEnabled = new ConcurrentHashMap<>();

    public EventingLogService(@NonNull LogServiceConfiguration logServiceConfiguration) {
        if (logServiceConfiguration.isAbsent() || logServiceConfiguration.isTrue("noop")) {
            noop = true;
            IeLogger.WARN.log("No-op per configuration {}", logServiceConfiguration);
            logWriter = null;
            loggerOutputLevelThreshold = null;
            return;
        }
        noop = false;
        logWriter = ConseqWriterGroup.from(logServiceConfiguration);
        loggerOutputLevelThreshold = LoggerOutputLevelThreshold.from(logServiceConfiguration);
    }

    @Override
    public boolean includeCallerDetail() {
        return logWriter.includeCallerDetail();
    }

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
                .callerThread(new LogEvent.ThreadValue(callerThread.getName(), callerThread.getId()))
                .nativeLogger(nativeLogger)
                .throwable(throwable)
                .message(message)
                .arguments(arguments)
                .serviceInterfaceClass(serviceInterfaceClass)
                .callerFrame(
                        includeCallerDetail()
                                ? LogEvent.StackFrameValue.from(StackTraceUtils.getCallerFrame(
                                        serviceInterfaceClass, new Throwable().getStackTrace()))
                                : null)
                .build());
    }
}
