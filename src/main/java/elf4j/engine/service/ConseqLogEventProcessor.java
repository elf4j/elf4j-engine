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
import elf4j.Level;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.PropertiesUtils;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.InternalLogger;
import lombok.NonNull;
import org.awaitility.Awaitility;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Log events are asynchronously processed, optionally by multiple concurrent threads. However, events issued by the
 * same caller application thread are processed sequentially with the {@link ConseqExecutor} API. Thus, logs by
 * different caller threads may arrive at the final destination (e.g. system Console or a log file) in any order;
 * meanwhile, logs from the same caller thread will arrive sequentially in the same order as they are called in the
 * orginal thread.
 */
public class ConseqLogEventProcessor implements LogEventProcessor {
    private static final int DEFAULT_FRONT_BUFFER_CAPACITY = 256;
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
    public static ConseqLogEventProcessor from(LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        Integer workQueueCapacity = getWorkQueueCapacity(properties);
        InternalLogger.INSTANCE.log(Level.INFO, "Log event work queue capacity: " + workQueueCapacity);
        Integer concurrency = getConcurrency(properties);
        InternalLogger.INSTANCE.log(Level.INFO, "Log process concurrency: " + concurrency);
        SequentialExecutor conseqExecutor =
                new ConseqExecutor.Builder().concurrency(concurrency).workQueueCapacity(workQueueCapacity).build();
        return new ConseqLogEventProcessor(logServiceConfiguration.getLogServiceWriter(), conseqExecutor);
    }

    @NonNull
    private static Integer getConcurrency(Properties properties) {
        Integer concurrency = PropertiesUtils.getAsInteger("concurrency", properties);
        concurrency = concurrency == null ? DEFAULT_CONCURRENCY : concurrency;
        if (concurrency < 1) {
            throw new IllegalArgumentException("Unexpected concurrency: " + concurrency + ", cannot be less than 1");
        }
        return concurrency;
    }

    @NonNull
    private static Integer getWorkQueueCapacity(Properties properties) {
        Integer workQueueCapacity = PropertiesUtils.getAsInteger("buffer.front", properties);
        workQueueCapacity = workQueueCapacity == null ? DEFAULT_FRONT_BUFFER_CAPACITY : workQueueCapacity;
        if (workQueueCapacity < 0) {
            throw new IllegalArgumentException("Unexpected buffer.front: " + workQueueCapacity);
        }
        if (workQueueCapacity == 0) {
            workQueueCapacity = 1;
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
        Awaitility.with().timeout(30, TimeUnit.MINUTES).await().until(this.conseqExecutor::isTerminated);
    }
}
