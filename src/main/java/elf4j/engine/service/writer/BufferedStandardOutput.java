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

package elf4j.engine.service.writer;

import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.Stoppable;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.PropertiesUtils;
import elf4j.util.IeLogger;
import lombok.ToString;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

/**
 *
 */
@ToString
public class BufferedStandardOutput implements StandardOutput, Stoppable {
    private static final int DEFAULT_BACK_BUFFER_CAPACITY = Integer.MAX_VALUE;
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    private final OutStreamType outStreamType;
    private final BlockingQueue<byte[]> buffer;
    private final PollingBytesWriter pollingBytesWriter;
    private boolean stopped;

    /**
     * @param outStreamType
     *         standard out stream type, stdout or stderr, default to stdout
     * @param bufferCapacity
     *         buffer capacity queued on standard out stream
     */
    private BufferedStandardOutput(OutStreamType outStreamType, int bufferCapacity) {
        this.outStreamType = outStreamType;
        this.buffer = new LinkedBlockingQueue<>(bufferCapacity);
        this.pollingBytesWriter = new PollingBytesWriter(bufferCapacity);
        LogServiceManager.INSTANCE.registerStop(this);
        this.stopped = false;
        new Thread(pollingBytesWriter).start();
    }

    /**
     * @param logServiceConfiguration
     *         entire service configuration
     * @return the {@link BufferedStandardOutput} per the specified configuration
     */
    public static BufferedStandardOutput from(LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        return new BufferedStandardOutput(getOutStreamType(properties), getBufferCapacity(properties));
    }

    private static int getBufferCapacity(Properties properties) {
        int bufferCapacity = PropertiesUtils.getIntOrDefault("buffer.back", properties, DEFAULT_BACK_BUFFER_CAPACITY);
        IeLogger.INFO.log("Buffer back: {}", bufferCapacity);
        if (bufferCapacity < 1) {
            IeLogger.ERROR.log("Unexpected buffer.back: {}, cannot be less than 1", bufferCapacity);
            throw new IllegalArgumentException("buffer.back: " + bufferCapacity);
        }
        return bufferCapacity;
    }

    private static OutStreamType getOutStreamType(Properties properties) {
        String stream = properties.getProperty("stream");
        return stream == null ? DEFAULT_OUT_STREAM_TYPE : OutStreamType.valueOf(stream.toUpperCase());
    }

    @Override
    public void stop() {
        long longMinutes = 10;
        ExecutorService shutdownThread = Executors.newSingleThreadExecutor();
        shutdownThread.execute(() -> {
            ConditionFactory await = Awaitility.with().timeout(longMinutes, TimeUnit.MINUTES).await();
            await.until(this.buffer::isEmpty);
            await.until(this.pollingBytesWriter::isBufferEmpty);
            this.stopped = true;
        });
        shutdownThread.shutdown();
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    @Override
    public void write(byte[] bytes) {
        try {
            buffer.put(bytes);
        } catch (InterruptedException e) {
            IeLogger.ERROR.log(e,
                    "Thread interrupted while enqueuing bytes of '{}' to standard output buffer {}",
                    new String(bytes, StandardCharsets.UTF_8),
                    buffer);
            Thread.currentThread().interrupt();
        }
    }

    enum OutStreamType {
        STDOUT,
        STDERR
    }

    private class PollingBytesWriter implements Runnable {
        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        private final int batchSize;

        private PollingBytesWriter(int batchSize) {
            if (batchSize < 0) {
                throw new IllegalArgumentException();
            }
            this.batchSize = Math.max(1, batchSize);
        }

        @Override
        public void run() {
            while (!stopped) {
                synchronized (byteArrayOutputStream) {
                    byteArrayOutputStream.reset();
                    List<byte[]> pollBatch = new LinkedList<>();
                    buffer.drainTo(pollBatch, batchSize);
                    if (pollBatch.isEmpty()) {
                        continue;
                    }
                    pollBatch.forEach(bytes -> {
                        try {
                            byteArrayOutputStream.write(bytes);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    try {
                        byteArrayOutputStream.writeTo(outStreamType == OutStreamType.STDERR ? System.err : System.out);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }
        }

        public boolean isBufferEmpty() {
            return this.byteArrayOutputStream.size() == 0;
        }
    }
}
