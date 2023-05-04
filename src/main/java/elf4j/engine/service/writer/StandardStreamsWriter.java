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
import elf4j.engine.service.pattern.PatternGroup;
import elf4j.engine.service.util.PropertiesUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 */
@Builder
@ToString
public class StandardStreamsWriter implements LogWriter {
    private static final String DEFAULT_MINIMUM_LEVEL = "trace";
    private static final String DEFAULT_PATTERN = "{timestamp} {level} {class} - {message}";
    private static final String DEFAULT_WRITER_OUT_STREAM = "stdout";
    private final Level minimumLevel;
    private final LogPattern logPattern;
    private final StandardOutput standardOutput;

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
        byte[] bytes = target.append(System.lineSeparator()).toString().getBytes(StandardCharsets.UTF_8);
        standardOutput.write(bytes);
    }

    @Override
    public boolean includeCallerDetail() {
        return logPattern.includeCallerDetail();
    }

    @Override
    public boolean includeCallerThread() {
        return logPattern.includeCallerThread();
    }

    /**
     *
     */
    public static class StandardStreamsWriterType implements LogWriterType {
        private static StandardStreamsWriter getDefaultWriter(LogServiceConfiguration logServiceConfiguration) {
            Properties properties = logServiceConfiguration.getProperties();
            return StandardStreamsWriter.builder()
                    .minimumLevel(Level.valueOf(properties.getProperty("level", DEFAULT_MINIMUM_LEVEL)
                            .trim()
                            .toUpperCase()))
                    .logPattern(PatternGroup.from(properties.getProperty("pattern", DEFAULT_PATTERN)))
                    .standardOutput(logServiceConfiguration.getStandardOutput())
                    .build();
        }

        private static String getWriterConfiguredOrDefault(String name,
                Map<String, String> writerConfiguration,
                Properties properties,
                String defaultValue) {
            return writerConfiguration.getOrDefault(name, properties.getProperty(name, defaultValue));
        }

        @Override
        public List<LogWriter> getLogWriters(LogServiceConfiguration logServiceConfiguration) {
            List<LogWriter> standardStreamsWriters = new ArrayList<>();
            Properties properties = logServiceConfiguration.getProperties();
            List<Map<String, String>> writerConfigurations =
                    PropertiesUtils.getPropertiesGroupOfType("standard", logServiceConfiguration.getProperties());
            if (writerConfigurations.isEmpty()) {
                standardStreamsWriters.add(getDefaultWriter(logServiceConfiguration));
                return standardStreamsWriters;
            }
            return writerConfigurations.stream()
                    .map(writerConfiguration -> StandardStreamsWriter.builder()
                            .minimumLevel(Level.valueOf(getWriterConfiguredOrDefault("level",
                                    writerConfiguration,
                                    properties,
                                    DEFAULT_MINIMUM_LEVEL).trim().toUpperCase()))
                            .logPattern(PatternGroup.from(getWriterConfiguredOrDefault("pattern",
                                    writerConfiguration,
                                    properties,
                                    DEFAULT_PATTERN)))
                            .standardOutput(logServiceConfiguration.getStandardOutput())
                            .build())
                    .collect(Collectors.toList());
        }
    }
}