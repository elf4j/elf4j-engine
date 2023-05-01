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

import elf4j.engine.service.BufferOverloadHandler;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.Stoppable;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class BufferedStandardOutput implements Stoppable {
    private static final int DEFAULT_BACK_BUFFER_CAPACITY = 262144;
    private final ExecutorService executorService;

    /**
     * @param bufferCapacity
     *         buffer capacity queued on standard out stream
     */
    public BufferedStandardOutput(int bufferCapacity) {
        this.executorService = new ThreadPoolExecutor(1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(bufferCapacity == 0 ? DEFAULT_BACK_BUFFER_CAPACITY : bufferCapacity),
                new BufferOverloadHandler());
        LogServiceManager.INSTANCE.register(this);
    }

    @Override
    public void stop() {
        this.executorService.shutdown();
    }

    void flushOut(byte[] bytes) {
        PrintStream stdout = System.out;
        executorService.submit(() -> {
            try {
                stdout.write(bytes);
                /* explicit flush in case default standard stream is changed */
                stdout.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    void flushErr(byte[] bytes) {
        PrintStream stderr = System.err;
        executorService.submit(() -> {
            try {
                stderr.write(bytes);
                /* explicit flush in case default standard stream is changed */
                stderr.flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
