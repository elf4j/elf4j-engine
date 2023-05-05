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

import elf4j.Level;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.Stoppable;
import elf4j.engine.service.util.MoreAwaitility;
import elf4j.util.InternalLogger;
import lombok.ToString;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@ToString
public class BufferedStandardOutput implements StandardOutput, Stoppable {
    private static final int DEFAULT_BACK_BUFFER_CAPACITY = 256;
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    private final OutStreamType outStreamType;
    private final BlockingQueue<byte[]> buffer;
    private final PollingBytesWriter pollingBytesWriter;
    private boolean stopped;

    /**
     * @param stream
     *         standard out stream type, stdout or stderr, default to stdout
     * @param bufferCapacity
     *         buffer capacity queued on standard out stream
     */
    public BufferedStandardOutput(String stream, Integer bufferCapacity) {
        this.outStreamType = stream == null ? DEFAULT_OUT_STREAM_TYPE : OutStreamType.valueOf(stream.toUpperCase());
        bufferCapacity = bufferCapacity == null ? DEFAULT_BACK_BUFFER_CAPACITY : bufferCapacity;
        InternalLogger.INSTANCE.log(Level.INFO, "Standard stream buffer capacity: " + bufferCapacity);
        this.buffer = bufferCapacity == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(bufferCapacity);
        this.stopped = false;
        pollingBytesWriter = new PollingBytesWriter(bufferCapacity);
        new Thread(pollingBytesWriter).start();
        LogServiceManager.INSTANCE.registerStop(this);
    }

    @Override
    public void stop() {
        MoreAwaitility.suspend(Duration.ofMillis(100));
        ConditionFactory await = Awaitility.with().timeout(30, TimeUnit.MINUTES).await();
        await.until(this.buffer::isEmpty);
        await.until(this.pollingBytesWriter::isBufferEmpty);
        this.stopped = true;
    }

    @Override
    public void write(byte[] bytes) {
        try {
            buffer.put(bytes);
        } catch (InterruptedException e) {
            InternalLogger.INSTANCE.log(Level.ERROR,
                    e,
                    "Thread interrupted while enqueuing bytes of '" + new String(bytes, StandardCharsets.UTF_8)
                            + "' to standard output buffer " + buffer);
            Thread.currentThread().interrupt();
        }
    }

    enum OutStreamType {
        STDOUT, STDERR
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
                List<byte[]> pollBatch = new LinkedList<>();
                buffer.drainTo(pollBatch, batchSize);
                synchronized (byteArrayOutputStream) {
                    byteArrayOutputStream.reset();
                    pollBatch.forEach(bytes -> {
                        try {
                            byteArrayOutputStream.write(bytes);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                    try {
                        byteArrayOutputStream.writeTo(outStreamType == OutStreamType.STDERR ? System.err : System.out);
                        byteArrayOutputStream.flush();
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
