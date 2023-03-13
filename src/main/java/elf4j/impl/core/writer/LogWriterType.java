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

import java.util.*;

/**
 *
 */
public enum LogWriterType {
    /**
     *
     */
    CONSOLE {
        @Override
        List<LogWriter> parseLogWriters(Properties properties) {
            List<Map<String, String>> configurationGroup =
                    PropertiesUtils.getPropertiesGroupOfType("console", properties);
            List<LogWriter> consoleWriters = new ArrayList<>();
            configurationGroup.forEach(configuration -> consoleWriters.add(ConsoleWriter.from(configuration,
                    properties.getProperty("console.out.stream"))));
            return consoleWriters;
        }
    };

    /**
     * @param properties configuration source
     * @return all writers parsed from the specified properties
     */
    public static List<LogWriter> parseAllLogWriters(Properties properties) {
        List<LogWriter> logWriters = new ArrayList<>();
        EnumSet.allOf(LogWriterType.class).forEach(type -> logWriters.addAll(type.parseLogWriters(properties)));
        return logWriters;
    }

    abstract List<LogWriter> parseLogWriters(Properties properties);
}
