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
import elf4j.impl.core.configuration.DefaultServiceConfiguration;
import elf4j.impl.core.configuration.ServiceConfiguration;
import elf4j.impl.core.util.StackTraceUtils;
import lombok.EqualsAndHashCode;

import java.util.Objects;
import java.util.Properties;

/**
 *
 */
@EqualsAndHashCode
public class DefaultLogService implements LogService {
    private final ServiceConfiguration serviceConfiguration;
    @EqualsAndHashCode.Exclude private final WriterThreadProvider writerThreadProvider;

    /**
     *
     */
    public DefaultLogService() {
        this(ServiceConfigurationHolder.INSTANCE, new WriterThreadProvider());
    }

    DefaultLogService(ServiceConfiguration serviceConfiguration, WriterThreadProvider writerThreadProvider) {
        this.serviceConfiguration = serviceConfiguration;
        this.writerThreadProvider = writerThreadProvider;
    }

    static void refreshConfiguration(Properties properties) {
        ServiceConfigurationHolder.INSTANCE.refresh(properties);
    }

    @Override
    public boolean includeCallerDetail() {
        return this.serviceConfiguration.getLogServiceWriter().includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return this.serviceConfiguration.getLogServiceWriter().includeCallerThread();
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        return serviceConfiguration.isEnabled(nativeLogger);
    }

    @Override
    public void log(NativeLogger nativeLogger,
            Class<?> serviceInterfaceClass,
            Throwable exception,
            Object message,
            Object[] args) {
        if (!serviceConfiguration.isEnabled(nativeLogger)) {
            return;
        }
        LogEntry.LogEntryBuilder logEntryBuilder =
                LogEntry.builder().nativeLogger(nativeLogger).exception(exception).message(message).arguments(args);
        if (this.includeCallerDetail()) {
            logEntryBuilder.callerFrame(StackTraceUtils.callerOf(Objects.requireNonNull(serviceInterfaceClass)));
        }
        if (this.includeCallerThread()) {
            Thread callerThread = Thread.currentThread();
            logEntryBuilder.callerThread(new LogEntry.ThreadInformation(callerThread.getName(), callerThread.getId()));
        }
        writerThreadProvider.getWriterThread()
                .execute(() -> serviceConfiguration.getLogServiceWriter().write(logEntryBuilder.build()));
    }

    private static class ServiceConfigurationHolder {
        private static final ServiceConfiguration INSTANCE = new DefaultServiceConfiguration();
    }
}
