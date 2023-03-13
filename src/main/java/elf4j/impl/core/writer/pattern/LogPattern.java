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

package elf4j.impl.core.writer.pattern;

import elf4j.impl.core.service.LogEntry;
import elf4j.impl.core.writer.PerformanceSensitive;
import lombok.NonNull;

import java.util.Optional;

/**
 *
 */
public interface LogPattern extends PerformanceSensitive {
    /**
     * @param pattern entire pattern text of an individual pattern segment, including pattern name and possibly options
     * @return the option portion of the pattern text if present; otherwise, empty Optional
     */
    static Optional<String> getPatternOption(@NonNull String pattern) {
        String[] elements = pattern.split(":", 2);
        return elements.length == 1 ? Optional.empty() : Optional.of(elements[1].trim());
    }

    /**
     * From the log entry, renders the data of interest particular to this log pattern instance, and appends the result
     * to the text builder
     *
     * @param logEntry       the overall log data entry to render
     * @param logTextBuilder the overall logging text aggregator
     */
    void render(LogEntry logEntry, StringBuilder logTextBuilder);
}
