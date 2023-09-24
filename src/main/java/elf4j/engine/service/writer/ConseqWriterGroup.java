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

import conseq4j.execute.ConseqExecutor;
import elf4j.Level;
import elf4j.engine.service.LogEvent;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.Stoppable;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.PropertiesUtils;
import elf4j.util.IeLogger;
import lombok.NonNull;
import lombok.ToString;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * In general, log events are asynchronously written/rendered in parallel by multiple concurrent threads. However,
 * events issued by the same caller application thread are rendered sequentially with the {@link ConseqExecutor} API.
 * Thus, logs by different caller threads may arrive at the final destination (e.g. system Console or a log file) in any
 * order; meanwhile, logs from the same caller thread will arrive sequentially in the same order as they are called in
 * the orginal thread.
 */
@ToString
public class ConseqWriterGroup implements LogWriter, Stoppable {
    private static final int DEFAULT_CONSEQ_CONCURRENCY = Runtime.getRuntime().availableProcessors();
    private final List<LogWriter> writers;
    private final ConseqExecutor conseqExecutor;
    private Level minimumLevel;
    @ToString.Exclude private Boolean includeCallerDetail;

    private ConseqWriterGroup(List<LogWriter> writers, ConseqExecutor conseqExecutor) {
        this.writers = writers;
        this.conseqExecutor = conseqExecutor;
        LogServiceManager.INSTANCE.registerStop(this);
    }

    /**
     * @param logServiceConfiguration
     *         entire configuration
     * @return the composite writer containing all writers configured in the specified properties
     */
    @NonNull
    public static ConseqWriterGroup from(LogServiceConfiguration logServiceConfiguration) {
        List<TypedLogWriterFactory> typedLogWriterFactories =
                new ArrayList<>(getTypedLogWriterFactories(logServiceConfiguration));
        if (typedLogWriterFactories.isEmpty()) {
            typedLogWriterFactories.add(new StandardStreamsWriter.StandardStreamsWriterFactory());
        }
        List<LogWriter> logWriters = typedLogWriterFactories.stream()
                .flatMap(t -> t.getLogWriters(logServiceConfiguration).stream())
                .collect(Collectors.toList());
        IeLogger.INFO.log("{} service writer(s): {}", logWriters.size(), logWriters);
        Properties properties = logServiceConfiguration.getProperties();
        return new ConseqWriterGroup(logWriters, ConseqExecutor.instance(getConcurrency(properties)));
    }

    private static int getConcurrency(Properties properties) {
        int concurrency = PropertiesUtils.getIntOrDefault("concurrency", properties, DEFAULT_CONSEQ_CONCURRENCY);
        IeLogger.INFO.log("Concurrency: {}", concurrency);
        if (concurrency < 1) {
            IeLogger.ERROR.log("Unexpected concurrency: {}, cannot be less than 1", concurrency);
            throw new IllegalArgumentException("concurrency: " + concurrency);
        }
        return concurrency;
    }

    private static List<TypedLogWriterFactory> getTypedLogWriterFactories(@NonNull LogServiceConfiguration logServiceConfiguration) {
        String writerTypes = logServiceConfiguration.getProperties().getProperty("writer.types");
        if (writerTypes == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(writerTypes.split(",")).map(String::trim).map(fqcn -> {
            try {
                return (TypedLogWriterFactory) Class.forName(fqcn).getDeclaredConstructor().newInstance();
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
    public void write(LogEvent logEvent) {
        conseqExecutor.execute(() -> writers.parallelStream().forEach(writer -> writer.write(logEvent)),
                logEvent.getCallerThread().getId());
    }

    @Override
    public boolean includeCallerDetail() {
        if (includeCallerDetail == null) {
            includeCallerDetail = writers.stream().anyMatch(LogWriter::includeCallerDetail);
        }
        return includeCallerDetail;
    }

    @Override
    public void stop() {
        IeLogger.INFO.log("Stopping {}", this);
        conseqExecutor.shutdown();
    }

    @Override
    public boolean isStopped() {
        return conseqExecutor.isTerminated();
    }
}
