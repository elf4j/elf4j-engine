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

import elf4j.impl.core.util.PropertiesUtils;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public enum LogWriterType {
    /**
     *
     */
    StandardStreams {
        @Override
        Set<LogWriter> parseLogWriters(Properties properties) {
            return PropertiesUtils.getPropertiesGroupOfType("stream", properties)
                    .stream()
                    .map(consoleWriterConfiguration -> StandardStreamsWriter.from(consoleWriterConfiguration,
                            properties.getProperty("stream.type")))
                    .collect(Collectors.toSet());
        }
    };

    /**
     * @param properties configuration source
     * @return all writers parsed from the specified properties
     */
    public static Set<LogWriter> parseAllLogWriters(Properties properties) {
        return EnumSet.allOf(LogWriterType.class)
                .stream()
                .flatMap(type -> type.parseLogWriters(properties).stream())
                .collect(Collectors.toSet());
    }

    abstract Set<LogWriter> parseLogWriters(Properties properties);
}
