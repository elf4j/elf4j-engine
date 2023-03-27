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

package elf4j.engine.writer;

import elf4j.Level;
import elf4j.engine.service.LogEntry;
import elf4j.engine.writer.pattern.LogPattern;
import elf4j.engine.writer.pattern.PatternSegmentGroup;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 *
 */
@ToString
public class StandardStreamsWriter implements LogWriter {
    private static final Level DEFAULT_MINIMUM_LEVEL = Level.TRACE;
    private static final OutStreamType DEFAULT_WRITER_OUT_STREAM = OutStreamType.STDOUT;
    private static final String DEFAULT_PATTERN =
            "{timestamp:yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ} {level} {class} - {message}";
    private final LogPattern logPattern;
    private final Level minimumLevel;
    private final OutStreamType outStreamType;

    private StandardStreamsWriter(Level minimumLevel, PatternSegmentGroup logPattern, OutStreamType outStreamType) {
        this.logPattern = logPattern;
        this.minimumLevel = minimumLevel;
        this.outStreamType = outStreamType;
    }

    /**
     * @return default writer
     */
    public static @Nonnull StandardStreamsWriter defaultWriter() {
        return new StandardStreamsWriter(DEFAULT_MINIMUM_LEVEL,
                PatternSegmentGroup.from(DEFAULT_PATTERN),
                DEFAULT_WRITER_OUT_STREAM);
    }

    /**
     * @param configuration        properties map to make a console writer
     * @param defaultOutStreamType override of global default out stream type for standard writers. Writer-specific
     *                             type, if present, takes precedence over global level. If no out stream type
     *                             configured on either global or writer level, default to stdout.
     * @return console writer per the specified configuration
     */
    public static @NonNull StandardStreamsWriter from(@NonNull Map<String, String> configuration,
            @Nullable String defaultOutStreamType) {
        String level = configuration.get("level");
        String pattern = configuration.get("pattern");
        String writerOutStreamType = configuration.get("stream");
        if (writerOutStreamType == null) {
            writerOutStreamType = defaultOutStreamType;
        }
        if (writerOutStreamType == null) {
            writerOutStreamType = DEFAULT_WRITER_OUT_STREAM.name();
        }
        return new StandardStreamsWriter(level == null ? DEFAULT_MINIMUM_LEVEL : Level.valueOf(level.toUpperCase()),
                PatternSegmentGroup.from(pattern == null ? DEFAULT_PATTERN : pattern),
                OutStreamType.valueOf(writerOutStreamType.trim().toUpperCase()));
    }

    @Override
    public Level getMinimumOutputLevel() {
        return minimumLevel;
    }

    @Override
    public void write(@NonNull LogEntry logEntry) {
        if (logEntry.getNativeLogger().getLevel().compareTo(this.minimumLevel) < 0) {
            return;
        }
        StringBuilder target = new StringBuilder();
        logPattern.render(logEntry, target);
        switch (this.outStreamType) {
            case STDOUT:
                BufferedStandardOutputStream.flushOut(target);
                return;
            case STDERR:
                BufferedStandardOutputStream.flushErr(target);
                return;
            case AUTO:
                if (logEntry.getNativeLogger().getLevel().compareTo(Level.WARN) < 0) {
                    BufferedStandardOutputStream.flushOut(target);
                } else {
                    BufferedStandardOutputStream.flushErr(target);
                }
                return;
            default:
                throw new IllegalArgumentException("Unsupported out stream type: " + this.outStreamType);
        }
    }

    @Override
    public boolean includeCallerDetail() {
        return logPattern.includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return logPattern.includeCallerThread();
    }

    enum OutStreamType {
        STDOUT, STDERR, AUTO
    }

    private static class BufferedStandardOutputStream {
        private static final PrintStream ERR = new PrintStream(new BufferedOutputStream(System.err), false);
        private static final PrintStream OUT = new PrintStream(new BufferedOutputStream(System.out), false);

        static synchronized void flushErr(Object o) {
            ERR.println(o);
            ERR.flush();
        }

        static synchronized void flushOut(Object o) {
            OUT.println(o);
            OUT.flush();
        }
    }
}