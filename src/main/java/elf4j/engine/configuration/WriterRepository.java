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

package elf4j.engine.configuration;

import elf4j.Level;
import elf4j.engine.writer.LogWriter;
import elf4j.engine.writer.StandardStreamsWriter;
import elf4j.engine.writer.WriterGroup;
import elf4j.util.InternalLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Properties;

/**
 *
 */
public class WriterRepository {
    private static final LogWriter DEFAULT_WRITER = StandardStreamsWriter.defaultWriter();
    private final LogWriter logServiceWriter;

    private WriterRepository(LogWriter logServiceWriter) {
        this.logServiceWriter = logServiceWriter;
    }

    /**
     * @param properties configuration from which to build the writer repo
     */
    static @Nonnull WriterRepository from(@Nullable Properties properties) {
        if (properties == null) {
            InternalLogger.INSTANCE.log(Level.INFO, "No configuration, taking default writer");
            return new WriterRepository(DEFAULT_WRITER);
        }
        WriterGroup writerGroup = WriterGroup.from(properties);
        if (writerGroup.size() > 0) {
            InternalLogger.INSTANCE.log(Level.INFO, "Configured writers: " + writerGroup.size());
            return new WriterRepository(writerGroup);
        }
        InternalLogger.INSTANCE.log(Level.WARN, "No writer configured, falling back to default writer");
        return new WriterRepository(DEFAULT_WRITER);
    }

    LogWriter getLogServiceWriter() {
        return logServiceWriter;
    }
}
