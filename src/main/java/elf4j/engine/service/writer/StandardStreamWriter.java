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
import elf4j.engine.service.LogEvent;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.pattern.LogPattern;
import elf4j.engine.service.pattern.PatternElement;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.concurrent.ThreadSafe;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
@Builder
@ToString
public class StandardStreamWriter implements LogWriter {
    private static final String DEFAULT_MINIMUM_LEVEL = "trace";
    private static final String DEFAULT_PATTERN = "{timestamp} {level} {class} - {message}";
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    private static final String LINE_FEED = System.lineSeparator();
    private final StandardOutput standardOutput = new FileStreamStandardOutput();
    private final Level minimumLevel;
    private final PatternElement logPattern;
    private final OutStreamType outStreamType;

    @Override
    public Level getMinimumOutputLevel() {
        return minimumLevel;
    }

    @Override
    public void write(@NonNull LogEvent logEvent) {
        if (logEvent.getNativeLogger().getLevel().compareTo(this.minimumLevel) < 0) {
            return;
        }
        StringBuilder target = new StringBuilder();
        logPattern.render(logEvent, target);
        byte[] bytes = target.append(LINE_FEED).toString().getBytes(StandardCharsets.UTF_8);
        if (outStreamType == OutStreamType.STDERR) {
            standardOutput.err(bytes);
        } else {
            standardOutput.out(bytes);
        }
    }

    @Override
    public boolean includeCallerDetail() {
        return logPattern.includeCallerDetail();
    }

    enum OutStreamType {
        STDOUT,
        STDERR
    }

    /**
     *
     */
    @ThreadSafe
    public interface StandardOutput {
        /**
         * @param bytes
         *         to be written to the out stream
         */
        void out(byte[] bytes);

        /**
         * @param bytes
         *         to be written to the out stream
         */
        void err(byte[] bytes);
    }

    /**
     *
     */
    @ToString
    public static class FileStreamStandardOutput implements StandardOutput {
        private final OutputStream stdout = new FileOutputStream(FileDescriptor.out);
        private final OutputStream stderr = new FileOutputStream(FileDescriptor.err);
        private final Lock lock = new ReentrantLock();

        @Override
        public void out(byte[] bytes) {
            write(bytes, stdout);
        }

        @Override
        public void err(byte[] bytes) {
            write(bytes, stderr);
        }

        private void write(byte[] bytes, @NonNull OutputStream outputStream) {
            lock.lock();
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     *
     */
    static class Type implements LogWriterType {
        private static StandardStreamWriter getDefaultWriter(@NonNull LogServiceConfiguration logServiceConfiguration) {
            Properties properties = logServiceConfiguration.getProperties();
            return StandardStreamWriter.builder()
                    .minimumLevel(Level.valueOf(properties.getProperty("level", DEFAULT_MINIMUM_LEVEL)
                            .trim()
                            .toUpperCase()))
                    .logPattern(LogPattern.from(properties.getProperty("pattern", DEFAULT_PATTERN)))
                    .outStreamType(OutStreamType.valueOf(properties.getProperty("stream",
                            DEFAULT_OUT_STREAM_TYPE.name()).trim().toUpperCase()))
                    .build();
        }

        @Override
        public List<LogWriter> getLogWriters(@NonNull LogServiceConfiguration logServiceConfiguration) {
            return Collections.singletonList(getDefaultWriter(logServiceConfiguration));
        }
    }
}
