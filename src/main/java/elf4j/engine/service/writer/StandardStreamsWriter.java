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
import elf4j.engine.service.BoundedBufferWriterThread;
import elf4j.engine.service.LogEntry;
import elf4j.engine.service.WriterThread;
import elf4j.engine.service.pattern.LogPattern;
import elf4j.engine.service.pattern.PatternGroup;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;

import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
@Builder
@ToString
public class StandardStreamsWriter implements LogWriter {
    private static final String DEFAULT_MINIMUM_LEVEL = "trace";
    private static final String DEFAULT_PATTERN = "{timestamp} {level} {class} - {message}";
    private static final String DEFAULT_WRITER_OUT_STREAM = "stdout";
    private static final String DEFAULT_BUFFER_CAPACITY = "250_000";
    private final Level minimumLevel;
    private final LogPattern logPattern;
    private final OutStreamType outStreamType;
    private final WriterThread writerThread;

    /**
     * @param writerConfiguration
     *         properties map to make a standard-stream writer
     * @param properties
     *         entire logging configuration for global properties lookup
     * @return a single standard-stream writer per the specified writerConfiguration
     */
    public static @NonNull StandardStreamsWriter from(@NonNull Map<String, String> writerConfiguration,
            @NonNull Properties properties) {
        return StandardStreamsWriter.builder()
                .minimumLevel(Level.valueOf(getWriterConfiguredOrDefault("level",
                        writerConfiguration,
                        properties,
                        DEFAULT_MINIMUM_LEVEL).trim().toUpperCase()))
                .logPattern(PatternGroup.from(getWriterConfiguredOrDefault("pattern",
                        writerConfiguration,
                        properties,
                        DEFAULT_PATTERN)))
                .outStreamType(OutStreamType.valueOf(getWriterConfiguredOrDefault("stream",
                        writerConfiguration,
                        properties,
                        DEFAULT_WRITER_OUT_STREAM).trim().toUpperCase()))
                .writerThread(new BoundedBufferWriterThread(Integer.parseInt(getWriterConfiguredOrDefault("buffer",
                        writerConfiguration,
                        properties,
                        DEFAULT_BUFFER_CAPACITY).replace("_", "").replace(",", ""))))
                .build();
    }

    /**
     * @param properties
     *         entire configuration properties
     * @return default writer when no specific writer is configured
     */
    public static StandardStreamsWriter defaultWriter(Properties properties) {
        return StandardStreamsWriter.builder()
                .minimumLevel(Level.valueOf(properties.getProperty("level", DEFAULT_MINIMUM_LEVEL)
                        .trim()
                        .toUpperCase()))
                .logPattern(PatternGroup.from(properties.getProperty("pattern", DEFAULT_PATTERN)))
                .outStreamType(OutStreamType.valueOf(properties.getProperty("stream", DEFAULT_WRITER_OUT_STREAM)
                        .trim()
                        .toUpperCase()))
                .writerThread(new BoundedBufferWriterThread(Integer.parseInt(properties.getProperty("buffer",
                        DEFAULT_BUFFER_CAPACITY).replace("_", "").replace(",", ""))))
                .build();
    }

    private static String getWriterConfiguredOrDefault(String name,
            Map<String, String> writerConfiguration,
            Properties properties,
            String defaultValue) {
        return writerConfiguration.getOrDefault(name, properties.getProperty(name, defaultValue));
    }

    @Override
    public Level getMinimumOutputLevel() {
        return minimumLevel;
    }

    @Override
    public void write(@NonNull LogEntry logEntry) {
        this.writerThread.execute(() -> doWrite(logEntry));
    }

    @Override
    public boolean includeCallerDetail() {
        return logPattern.includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return logPattern.includeCallerThread();
    }

    private void doWrite(@NonNull LogEntry logEntry) {
        if (logEntry.getNativeLogger().getLevel().compareTo(this.minimumLevel) < 0) {
            return;
        }
        StringBuilder target = new StringBuilder();
        logPattern.renderTo(logEntry, target);
        switch (this.outStreamType) {
            case STDOUT:
                AtomicOutput.flushOut(target);
                return;
            case STDERR:
                AtomicOutput.flushErr(target);
                return;
            case AUTO:
                if (logEntry.getNativeLogger().getLevel().compareTo(Level.WARN) < 0) {
                    AtomicOutput.flushOut(target);
                } else {
                    AtomicOutput.flushErr(target);
                }
                return;
            default:
                throw new IllegalArgumentException("Unsupported out stream type: " + this.outStreamType);
        }
    }

    enum OutStreamType {
        STDOUT, STDERR, AUTO
    }

    private static class AtomicOutput {
        static synchronized void flushErr(Object o) {
            PrintStream stderr = System.err;
            stderr.println(o);
            /* explicit flush in case default standard stream is changed */
            stderr.flush();
        }

        static synchronized void flushOut(Object o) {
            PrintStream stdout = System.out;
            stdout.println(o);
            /* explicit flush in case default standard stream is changed */
            stdout.flush();
        }
    }
}