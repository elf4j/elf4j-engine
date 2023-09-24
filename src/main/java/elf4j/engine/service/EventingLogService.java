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

import elf4j.engine.NativeLogger;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.configuration.RefreshableLogServiceConfiguration;
import lombok.NonNull;

/**
 * converts a log request into an event for async processing
 */
public class EventingLogService implements LogService {

    private final LogServiceConfiguration logServiceConfiguration;

    /**
     *
     */
    public EventingLogService() {
        this(new RefreshableLogServiceConfiguration());
    }

    EventingLogService(LogServiceConfiguration logServiceConfiguration) {
        this.logServiceConfiguration = logServiceConfiguration;
    }

    @Override
    public boolean includeCallerDetail() {
        return this.logServiceConfiguration.getLogServiceWriter().includeCallerDetail();
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        return logServiceConfiguration.isEnabled(nativeLogger);
    }

    @Override
    public void log(@NonNull NativeLogger nativeLogger,
            @NonNull Class<?> serviceInterfaceClass,
            Throwable throwable,
            Object message,
            Object[] arguments) {
        if (!logServiceConfiguration.isEnabled(nativeLogger)) {
            return;
        }
        LogEvent.LogEventBuilder logEventBuilder = LogEvent.builder()
                .nativeLogger(nativeLogger)
                .throwable(throwable)
                .message(message)
                .arguments(arguments);
        if (this.includeCallerDetail()) {
            logEventBuilder.callerStack(new Throwable().getStackTrace()).serviceInterfaceClass(serviceInterfaceClass);
        }
        Thread callerThread = Thread.currentThread();
        logEventBuilder.callerThread(new LogEvent.ThreadValue(callerThread.getName(), callerThread.getId()));
        this.logServiceConfiguration.getLogEventProcessor().process(logEventBuilder.build());
    }
}
