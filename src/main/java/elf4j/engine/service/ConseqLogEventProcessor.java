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

import conseq4j.execute.ConseqExecutor;
import conseq4j.execute.SequentialExecutor;
import conseq4j.util.MoreRejectedExecutionHandlers;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.PropertiesUtils;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.IeLogger;
import lombok.NonNull;

import java.util.Properties;

/**
 * Log events are asynchronously processed, optionally by multiple concurrent threads. However, events issued by the
 * same caller application thread are processed sequentially with the {@link ConseqExecutor} API. Thus, logs by
 * different caller threads may arrive at the final destination (e.g. system Console or a log file) in any order;
 * meanwhile, logs from the same caller thread will arrive sequentially in the same order as they are called in the
 * orginal thread.
 */
public class ConseqLogEventProcessor implements LogEventProcessor {
    private static final int DEFAULT_FRONT_BUFFER_CAPACITY = Integer.MAX_VALUE;
    private static final int DEFAULT_CONCURRENCY = Runtime.getRuntime().availableProcessors();
    private final LogWriter logWriter;
    private final SequentialExecutor conseqExecutor;

    private ConseqLogEventProcessor(LogWriter logWriter, SequentialExecutor conseqExecutor) {
        this.logWriter = logWriter;
        this.conseqExecutor = conseqExecutor;
        LogServiceManager.INSTANCE.registerStop(this);
    }

    /**
     * @param logServiceConfiguration
     *         entire configuration
     * @return conseq executor
     */
    @NonNull
    public static ConseqLogEventProcessor from(@NonNull LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        int workQueueCapacity = getWorkQueueCapacity(properties);
        IeLogger.INFO.log("Buffer front: {}", workQueueCapacity);
        int concurrency = getConcurrency(properties);
        IeLogger.INFO.log("Concurrency: {}", concurrency);
        SequentialExecutor conseqExecutor = new ConseqExecutor.Builder().concurrency(concurrency)
                .rejectedExecutionHandler(MoreRejectedExecutionHandlers.blockingRetryPolicy())
                .workQueueCapacity(workQueueCapacity)
                .build();
        return new ConseqLogEventProcessor(logServiceConfiguration.getLogServiceWriter(), conseqExecutor);
    }

    private static int getConcurrency(Properties properties) {
        int concurrency = PropertiesUtils.getIntOrDefault("concurrency", properties, DEFAULT_CONCURRENCY);
        if (concurrency < 1) {
            IeLogger.ERROR.log("Unexpected concurrency: {}, cannot be less than 1", concurrency);
            throw new IllegalArgumentException("concurrency: " + concurrency);
        }
        return concurrency;
    }

    private static int getWorkQueueCapacity(Properties properties) {
        int workQueueCapacity =
                PropertiesUtils.getIntOrDefault("buffer.front", properties, DEFAULT_FRONT_BUFFER_CAPACITY);
        if (workQueueCapacity < 1) {
            IeLogger.ERROR.log("Unexpected buffer.front: {}, cannot be less than 1", workQueueCapacity);
            throw new IllegalArgumentException("buffer.front: " + workQueueCapacity);
        }
        return workQueueCapacity;
    }

    @Override
    public void process(LogEvent logEvent) {
        conseqExecutor.execute(() -> logWriter.write(logEvent), logEvent.getCallerThread().getId());
    }

    @Override
    public void stop() {
        this.conseqExecutor.shutdown();
    }

    @Override
    public boolean isStopped() {
        return this.conseqExecutor.isTerminated();
    }
}
