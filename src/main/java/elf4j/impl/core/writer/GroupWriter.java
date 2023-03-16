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

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

/**
 *
 */
public class GroupWriter implements LogWriter {
    private final Set<LogWriter> writers;
    private Level minimumLevel;
    private Boolean includeCallerDetail;
    private Boolean includeCallerThread;

    private GroupWriter(Set<LogWriter> writers) {
        this.writers = writers;
    }

    /**
     * @param properties configuration of all the writers
     * @return the composite writer containing all writers configured in the specified properties
     */
    public static GroupWriter from(Properties properties) {
        return new GroupWriter(LogWriterType.parseAllLogWriters(properties));
    }

    @Override
    public Level getMinimumLevel() {
        if (minimumLevel == null) {
            minimumLevel = Level.values()[writers.stream()
                    .mapToInt(writer -> writer.getMinimumLevel().ordinal())
                    .min()
                    .orElseThrow(NoSuchElementException::new)];
        }
        return minimumLevel;
    }

    @Override
    public void write(LogEntry logEntry) {
        writers.parallelStream().forEach(writer -> writer.write(logEntry));
    }

    @Override
    public boolean includeCallerDetail() {
        if (includeCallerDetail == null) {
            includeCallerDetail = writers.stream().anyMatch(LogWriter::includeCallerDetail);
        }
        return includeCallerDetail;
    }

    @Override
    public boolean includeCallerThread() {
        if (includeCallerThread == null) {
            includeCallerThread = writers.stream().anyMatch(LogWriter::includeCallerThread);
        }
        return includeCallerThread;
    }

    /**
     * @return true if no writer is configured
     */
    public boolean isEmpty() {
        return writers.isEmpty();
    }
}
