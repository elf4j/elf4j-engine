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
import elf4j.engine.service.LogEntry;
import lombok.NonNull;
import lombok.ToString;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 *
 */
@ToString
public class WriterGroup implements LogWriter {
    private final List<LogWriter> writers;
    private Level minimumLevel;
    @ToString.Exclude private Boolean includeCallerDetail;
    @ToString.Exclude private Boolean includeCallerThread;

    private WriterGroup(List<LogWriter> writers) {
        this.writers = writers;
    }

    /**
     * @param properties
     *         configuration of all the writers
     * @return the composite writer containing all writers configured in the specified properties
     */
    public static WriterGroup from(@NonNull Properties properties) {
        return new WriterGroup(WriterType.parseAllWriters(properties));
    }

    @Override
    public Level getMinimumOutputLevel() {
        if (minimumLevel == null) {
            minimumLevel = Level.values()[writers.stream()
                    .mapToInt(writer -> writer.getMinimumOutputLevel().ordinal())
                    .min()
                    .orElseThrow(NoSuchElementException::new)];
        }
        return minimumLevel;
    }

    @Override
    public void write(LogEntry logEntry) {
        writers.forEach(writer -> writer.write(logEntry));
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
     * @return number of writers in group
     */
    public int size() {
        return writers.size();
    }
}
