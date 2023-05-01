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

package elf4j.engine.service.configuration;

import elf4j.engine.NativeLogger;
import elf4j.engine.service.WriterThread;
import elf4j.engine.service.writer.BufferedStandardOutput;
import elf4j.engine.service.writer.LogWriter;

/**
 *
 */
public interface LogServiceConfiguration {
    /**
     * @return the top level (group) writer for the log service, may contain multiple individual writers.
     */
    LogWriter getLogServiceWriter();

    /**
     * @param nativeLogger
     *         the logger to check for enablement against configuration
     * @return true if the specified logger's level is at or above the configured minimum output level of both the
     *         writer and that configured for the logger's caller/owner class; otherwise, false.
     */
    boolean isEnabled(NativeLogger nativeLogger);

    /**
     * @return async executor for log entry tasks
     */
    WriterThread getWriterThread();

    /**
     * @return buffered standard out stream writer
     */
    BufferedStandardOutput getSBufferedStandardOutput();
}
