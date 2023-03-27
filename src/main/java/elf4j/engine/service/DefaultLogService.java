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
import elf4j.engine.configuration.DefaultLogServiceConfiguration;
import elf4j.engine.configuration.LogServiceConfiguration;
import elf4j.engine.util.StackTraceUtils;
import lombok.NonNull;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class DefaultLogService implements LogService {
    private final LogServiceConfiguration logServiceConfiguration;
    private final WriterThread writerThread;

    /**
     *
     */
    public DefaultLogService() {
        this(ServiceConfigurationHolder.INSTANCE, WriterThreadHolder.INSTANCE);
    }

    DefaultLogService(LogServiceConfiguration logServiceConfiguration, WriterThread writerThread) {
        this.logServiceConfiguration = logServiceConfiguration;
        this.writerThread = writerThread;
    }

    static void refreshConfiguration(Properties properties) {
        ServiceConfigurationHolder.INSTANCE.refresh(properties);
    }

    static void shutdown() {
        WriterThreadHolder.INSTANCE.shutdown();
    }

    @Override
    public boolean includeCallerDetail() {
        return this.logServiceConfiguration.getLogServiceWriter().includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return this.logServiceConfiguration.getLogServiceWriter().includeCallerThread();
    }

    @Override
    public boolean isNoop() {
        return logServiceConfiguration.isNoop();
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        return logServiceConfiguration.isEnabled(nativeLogger);
    }

    @Override
    public void log(NativeLogger nativeLogger,
            Class<?> serviceInterfaceClass,
            Throwable exception,
            Object message,
            Object[] args) {
        if (!logServiceConfiguration.isEnabled(nativeLogger)) {
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
        writerThread.execute(() -> logServiceConfiguration.getLogServiceWriter().write(logEntryBuilder.build()));
    }

    /**
     *
     */
    private static class ExecutorServiceWriterThread implements WriterThread {
        private final ExecutorService executorService;

        /**
         * @param executorService service delegate
         */
        public ExecutorServiceWriterThread(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void shutdown() {
            this.executorService.shutdown();
        }

        @Override
        public void execute(@NonNull Runnable command) {
            this.executorService.execute(command);
        }
    }

    private static class ServiceConfigurationHolder {
        private static final LogServiceConfiguration INSTANCE = new DefaultLogServiceConfiguration();
    }

    private static class WriterThreadHolder {
        private static final ExecutorService SINGLE_THREAD_EXECUTOR =
                Executors.newSingleThreadExecutor(r -> new Thread(r, "elf4j-engine-writer-thread"));
        private static final ExecutorServiceWriterThread INSTANCE =
                new ExecutorServiceWriterThread(SINGLE_THREAD_EXECUTOR);
    }
}
