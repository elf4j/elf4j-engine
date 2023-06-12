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
import elf4j.util.IeLogger;
import lombok.ToString;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
@ToString
public class FileStreamStandardOutput implements StandardOutput, Stoppable.Output {
    private final OutputStream stdout = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out));
    private final OutputStream stderr = new BufferedOutputStream(new FileOutputStream(FileDescriptor.err));
    private final Lock lock = new ReentrantLock();
    private boolean stopped;

    /**
     *
     */
    public FileStreamStandardOutput() {
        LogServiceManager.INSTANCE.registerStop(this);
    }

    @Override
    public void out(byte[] bytes) {
        lock.lock();
        try {
            writeOut(bytes);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void err(byte[] bytes) {
        lock.lock();
        try {
            writeErr(bytes);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        IeLogger.INFO.log("Stopping {}", this);
        try (Closeable stopTarget1 = stdout; Closeable stopTarget2 = stderr) {
            stopped = true;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    private void writeErr(byte[] bytes) {
        try {
            stderr.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeOut(byte[] bytes) {
        try {
            stdout.write(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
