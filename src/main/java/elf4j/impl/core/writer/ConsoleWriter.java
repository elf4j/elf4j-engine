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

package elf4j.impl.core.writer;

import elf4j.Level;
import elf4j.impl.core.service.LogEntry;
import elf4j.impl.core.writer.pattern.GroupLogPattern;
import elf4j.impl.core.writer.pattern.LogPattern;

import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.PrintStream;
import java.util.Map;

/**
 *
 */
public class ConsoleWriter implements LogWriter {
    private static final Level DEFAULT_MINIMUM_LEVEL = Level.TRACE;
    private static final OutStreamType DEFAULT_OUT_STREAM = OutStreamType.STDOUT;
    private static final String DEFAULT_PATTERN =
            "{timestamp:yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ} {level} {class} - {message}";
    private static final PrintStream ERR = new PrintStream(new BufferedOutputStream(System.err));
    private static final PrintStream OUT = new PrintStream(new BufferedOutputStream(System.out));
    private final LogPattern logPattern;
    private final Level minimumLevel;
    private final OutStreamType outStreamType;

    private ConsoleWriter(Level minimumLevel, GroupLogPattern logPattern, OutStreamType outStreamType) {
        this.logPattern = logPattern;
        this.minimumLevel = minimumLevel;
        this.outStreamType = outStreamType;
    }

    /**
     * @return default writer
     */
    public static ConsoleWriter defaultWriter() {
        return new ConsoleWriter(DEFAULT_MINIMUM_LEVEL, GroupLogPattern.from(DEFAULT_PATTERN), DEFAULT_OUT_STREAM);
    }

    /**
     * @param configuration properties map to make a console writer
     * @param outStream     out stream type, either stdout or stderr
     * @return console writer per the specified configuration
     */
    public static ConsoleWriter from(Map<String, String> configuration, @Nullable String outStream) {
        String level = configuration.get("level");
        String pattern = configuration.get("pattern");
        return new ConsoleWriter(level == null ? DEFAULT_MINIMUM_LEVEL : Level.valueOf(level.toUpperCase()),
                GroupLogPattern.from(pattern == null ? DEFAULT_PATTERN : pattern),
                outStream == null ? DEFAULT_OUT_STREAM : OutStreamType.valueOf(outStream.trim().toUpperCase()));
    }

    private static void flushErr(StringBuilder logTextBuilder) {
        ERR.println(logTextBuilder);
        ERR.flush();
    }

    private static void flushOut(StringBuilder logTextBuilder) {
        OUT.println(logTextBuilder);
        OUT.flush();
    }

    @Override
    public Level getMinimumLevel() {
        return minimumLevel;
    }

    @Override
    public void write(LogEntry logEntry) {
        if (this.minimumLevel.ordinal() > logEntry.getNativeLogger().getLevel().ordinal()) {
            return;
        }
        StringBuilder logTextBuilder = new StringBuilder();
        logPattern.render(logEntry, logTextBuilder);
        switch (this.outStreamType) {
            case STDOUT:
                flushOut(logTextBuilder);
                return;
            case STDERR:
                flushErr(logTextBuilder);
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
        STDOUT,
        STDERR
    }
}
