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

package elf4j.impl.core.service;

import elf4j.impl.core.NativeLogger;
import elf4j.impl.core.writer.PerformanceSensitive;

/**
 *
 */
public interface LogService extends PerformanceSensitive {
    /**
     * @param nativeLogger to check for enablement
     * @return true if the logger's level is at or above configured minimum
     */
    boolean isEnabled(NativeLogger nativeLogger);

    /**
     * @param nativeLogger            the serviced logger
     * @param loggingServiceInterface runtime logging service class that the client calls directly to issue the log. For
     *                                native ELF4J service, it is always the {@link NativeLogger} class; may be a
     *                                different class if this core library is used to service other logging API.
     * @param exception               to log
     * @param message                 to log, can have argument placeholders
     * @param args                    to replace the placeholders in the message
     */
    void log(NativeLogger nativeLogger,
            Class<?> loggingServiceInterface,
            Throwable exception,
            Object message,
            Object[] args);
}
