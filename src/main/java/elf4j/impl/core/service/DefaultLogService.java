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

package elf4j.impl.core.service;

import elf4j.impl.core.NativeLogger;
import elf4j.impl.core.configuration.LoggingConfiguration;
import elf4j.impl.core.util.StackTraceUtils;
import lombok.EqualsAndHashCode;

import java.util.Objects;

/**
 *
 */
@EqualsAndHashCode
public class DefaultLogService implements LogService {
    private final LoggingConfiguration loggingConfiguration;
    private final WriterThreadProvider writerThreadProvider;

    /**
     * @param loggingConfiguration configuration for min logging output level and log writers
     * @param writerThreadProvider provides the async writer thread
     */
    public DefaultLogService(LoggingConfiguration loggingConfiguration, WriterThreadProvider writerThreadProvider) {
        this.loggingConfiguration = loggingConfiguration;
        this.writerThreadProvider = writerThreadProvider;
    }

    @Override
    public boolean includeCallerDetail() {
        return this.loggingConfiguration.getLogServiceWriter().includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return this.loggingConfiguration.getLogServiceWriter().includeCallerThread();
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        return loggingConfiguration.isEnabled(nativeLogger);
    }

    @Override
    public void log(NativeLogger nativeLogger,
            Class<?> loggingServiceInterface,
            Throwable exception,
            Object message,
            Object[] args) {
        if (!loggingConfiguration.isEnabled(nativeLogger)) {
            return;
        }
        LogEntry.LogEntryBuilder logEntryBuilder =
                LogEntry.builder().nativeLogger(nativeLogger).exception(exception).message(message).arguments(args);
        if (this.includeCallerDetail()) {
            logEntryBuilder.callerFrame(StackTraceUtils.callerOf(Objects.requireNonNull(loggingServiceInterface)));
        }
        if (this.includeCallerThread()) {
            Thread callerThread = Thread.currentThread();
            logEntryBuilder.callerThread(new LogEntry.ThreadInformation(callerThread.getName(), callerThread.getId()));
        }
        LogEntry logEntry = logEntryBuilder.build();
        writerThreadProvider.getWriterThread()
                .execute(() -> loggingConfiguration.getLogServiceWriter().write(logEntry));
    }
}
