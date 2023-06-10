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
import elf4j.util.IeLogger;
import lombok.NonNull;
import lombok.ToString;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
@ToString
public class FileStreamStandardOutput implements StandardOutput {
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    @NonNull private final OutStreamType outStreamType;
    private final StandardOutputStreams standardOutputStreams = new StandardOutputStreams();

    /**
     * @param outStreamType
     *         standard out stream type, stdout or stderr, default to stdout
     */
    private FileStreamStandardOutput(@NonNull OutStreamType outStreamType) {
        this.outStreamType = outStreamType;
    }

    /**
     * @param logServiceConfiguration
     *         entire service configuration @return the {@link FileStreamStandardOutput} per the specified
     *         configuration
     * @return parsed output per specified configuration
     */
    public static @NonNull FileStreamStandardOutput from(@NonNull LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        String stream = properties.getProperty("stream");
        return new FileStreamStandardOutput(
                stream == null ? DEFAULT_OUT_STREAM_TYPE : OutStreamType.valueOf(stream.toUpperCase()));
    }

    @Override
    public void write(byte[] bytes) {
        if (this.outStreamType == OutStreamType.STDERR) {
            standardOutputStreams.err(bytes);
        } else {
            standardOutputStreams.out(bytes);
        }
    }

    enum OutStreamType {
        STDOUT,
        STDERR
    }

    static class StandardOutputStreams implements Stoppable.Output {
        final OutputStream stdout = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out));
        final OutputStream stderr = new BufferedOutputStream(new FileOutputStream(FileDescriptor.err));

        final Lock lock = new ReentrantLock();
        boolean stopped;

        StandardOutputStreams() {
            LogServiceManager.INSTANCE.registerStop(this);
        }

        @Override
        public void stop() {
            IeLogger.INFO.log("Stopping {}", this);
            try (AutoCloseable out = stdout; AutoCloseable err = stderr) {
                this.stopped = true;
            } catch (Exception e) {
                IeLogger.WARN.log(e, "Error closing {} or {}", stdout, stderr);
                throw new IllegalStateException(e);
            }
        }

        @Override
        public boolean isStopped() {
            return this.stopped;
        }

        void err(byte[] bytes) {
            lock.lock();
            try {
                writeErr(bytes);
            } finally {
                lock.unlock();
            }
        }

        void out(byte[] bytes) {
            lock.lock();
            try {
                writeOut(bytes);
            } finally {
                lock.unlock();
            }
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
}