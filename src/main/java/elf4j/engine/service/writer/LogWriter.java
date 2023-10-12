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
import elf4j.engine.service.PerformanceSensitive;
import elf4j.engine.service.configuration.LogServiceConfiguration;

import javax.annotation.concurrent.ThreadSafe;
import java.util.List;

/**
 * Implementation should be thread-safe
 */
@ThreadSafe
public interface LogWriter extends PerformanceSensitive {

    /**
     * @return the minimum output level of this writer
     */
    Level getMinimumOutputLevel();

    /**
     * @param logEvent
     *         the log data entry to write out
     */
    void write(LogEvent logEvent);

    /**
     *
     */
    interface LogWriterType {
        /**
         * @param logServiceConfiguration
         *         entire configuration
         * @return all log writers of the enclosing writer type from the given configuration
         */
        List<LogWriter> getLogWriters(LogServiceConfiguration logServiceConfiguration);
    }
}
