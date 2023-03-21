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
import elf4j.engine.util.InternalLogger;
import elf4j.engine.writer.GroupWriter;
import elf4j.engine.writer.LogWriter;
import elf4j.engine.writer.StandardStreamsWriter;
import lombok.NonNull;

import javax.annotation.Nonnull;
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
    static @Nonnull WriterRepository from(@NonNull Properties properties) {
        GroupWriter groupWriter = GroupWriter.from(properties);
        if (!groupWriter.isEmpty()) {
            InternalLogger.log(Level.INFO,
                    String.format("Service writer %s obtained from configuration %s", groupWriter, properties));
            return new WriterRepository(groupWriter);
        }
        InternalLogger.log(Level.WARN,
                String.format("No writer found in configuration %s, using default service writer %s",
                        properties,
                        DEFAULT_WRITER));
        return new WriterRepository(DEFAULT_WRITER);
    }

    LogWriter getLogServiceWriter() {
        return logServiceWriter;
    }
}
