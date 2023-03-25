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

import elf4j.engine.util.PropertiesUtils;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public enum WriterType {

    /**
     * Type of writer that only output to standard streams
     */
    STANDARD {
        @Override
        Set<LogWriter> parseWriters(@NonNull Properties properties) {
            String defaultOutStreamTypeOverride = properties.getProperty("standard.stream");
            return PropertiesUtils.getPropertiesGroupOfType(STANDARD.name().toLowerCase(), properties)
                    .stream()
                    .map(writerConfiguration -> StandardStreamsWriter.from(writerConfiguration,
                            defaultOutStreamTypeOverride))
                    .collect(Collectors.toSet());
        }
    };

    private static final EnumSet<WriterType> WRITER_TYPES = EnumSet.allOf(WriterType.class);

    /**
     * @param properties configuration source
     * @return all writers parsed from the specified properties
     */
    public static Set<LogWriter> parseAllWriters(@NonNull Properties properties) {
        return WRITER_TYPES.stream()
                .flatMap(type -> type.parseWriters(properties).stream())
                .collect(Collectors.toSet());
    }

    abstract Set<LogWriter> parseWriters(Properties properties);
}
