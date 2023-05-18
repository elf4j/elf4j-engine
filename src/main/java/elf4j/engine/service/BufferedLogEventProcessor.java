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

import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.MoreExecutors;
import elf4j.engine.service.util.PropertiesUtils;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.IeLogger;
import lombok.NonNull;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 *
 */
public class BufferedLogEventProcessor implements LogEventProcessor {
    private static final int DEFAULT_FRONT_BUFFER_CAPACITY = Integer.MAX_VALUE;
    private final LogWriter logWriter;
    private final ExecutorService bufferedExecutor;

    private BufferedLogEventProcessor(LogWriter logWriter, ExecutorService bufferedExecutor) {
        this.logWriter = logWriter;
        this.bufferedExecutor = bufferedExecutor;
        LogServiceManager.INSTANCE.registerStop(this);
    }

    /**
     * @param logServiceConfiguration
     *         entire configuration
     * @return conseq executor
     */
    @NonNull
    public static BufferedLogEventProcessor from(@NonNull LogServiceConfiguration logServiceConfiguration) {
        return new BufferedLogEventProcessor(logServiceConfiguration.getLogServiceWriter(),
                MoreExecutors.newSingleThreadBlockingRetryExecutor(getWorkQueueCapacity(logServiceConfiguration.getProperties())));
    }

    private static int getWorkQueueCapacity(Properties properties) {
        int bufferCapacity = PropertiesUtils.getIntOrDefault("buffer", properties, DEFAULT_FRONT_BUFFER_CAPACITY);
        IeLogger.INFO.log("Buffer: {}", bufferCapacity);
        if (bufferCapacity < 1) {
            IeLogger.ERROR.log("Unexpected buffer: {}, cannot be less than 1", bufferCapacity);
            throw new IllegalArgumentException("buffer: " + bufferCapacity);
        }
        return bufferCapacity;
    }

    @Override
    public void process(LogEvent logEvent) {
        bufferedExecutor.execute(() -> logWriter.write(logEvent));
    }

    @Override
    public void stop() {
        this.bufferedExecutor.shutdown();
    }

    @Override
    public boolean isStopped() {
        return this.bufferedExecutor.isTerminated();
    }
}
