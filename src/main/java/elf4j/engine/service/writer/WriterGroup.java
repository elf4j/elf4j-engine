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
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.util.InternalLogger;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

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
     * @param logServiceConfiguration
     *         entire configuration
     * @return the composite writer containing all writers configured in the specified properties
     */
    @NonNull
    public static WriterGroup from(LogServiceConfiguration logServiceConfiguration) {
        List<LogWriterType> logWriterTypes = new ArrayList<>(getLogWriterTypes(logServiceConfiguration));
        if (logWriterTypes.isEmpty()) {
            logWriterTypes.add(new StandardStreamsWriter.StandardStreamsWriterType());
        }
        List<LogWriter> logWriters = logWriterTypes.stream()
                .flatMap(t -> t.getLogWriters(logServiceConfiguration).stream())
                .collect(Collectors.toList());
        InternalLogger.INSTANCE.log(Level.INFO,
                "Configured " + logWriters.size() + " service writer(s): " + logWriters);
        return new WriterGroup(logWriters);
    }

    private static List<LogWriterType> getLogWriterTypes(LogServiceConfiguration logServiceConfiguration) {
        String writerTypes = logServiceConfiguration.getProperties().getProperty("writer.types");
        if (writerTypes == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(writerTypes.split(",")).map(String::trim).map(fqcn -> {
            try {
                return (LogWriterType) Class.forName(fqcn).getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new IllegalArgumentException(fqcn, e);
            }
        }).collect(Collectors.toList());
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
}
