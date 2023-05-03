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
import org.awaitility.Awaitility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class BufferedStandardOutput implements StandardOutput {
    private static final int DEFAULT_BACK_BUFFER_CAPACITY = 256;
    private final OutStreamType outStreamType;
    private final BlockingQueue<byte[]> buffer;
    private boolean stopped;

    /**
     * @param stream
     *         standard out stream type, stdout or stderr, default to stdout
     * @param bufferCapacity
     *         buffer capacity queued on standard out stream
     */
    public BufferedStandardOutput(String stream, Integer bufferCapacity) {
        this.outStreamType = stream == null ? OutStreamType.STDOUT : OutStreamType.valueOf(stream.toUpperCase());
        bufferCapacity = bufferCapacity == null ? DEFAULT_BACK_BUFFER_CAPACITY : bufferCapacity;
        this.buffer = bufferCapacity == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(bufferCapacity);
        this.stopped = false;
        new Thread(new PollingBytesWriter()).start();
        LogServiceManager.INSTANCE.registerStop(this);
    }

    @Override
    public void stop() {
        Awaitility.with().timeout(30, TimeUnit.MINUTES).await().until(this.buffer::isEmpty);
        this.stopped = true;
    }

    @Override
    public void write(byte[] bytes) {
        try {
            buffer.put(bytes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    enum OutStreamType {
        STDOUT, STDERR
    }

    private class PollingBytesWriter implements Runnable {
        @Override
        public void run() {
            while (!stopped) {
                List<byte[]> poll = new LinkedList<>();
                buffer.drainTo(poll);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(poll.size() * 2048);
                poll.forEach(bytes -> {
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
}
