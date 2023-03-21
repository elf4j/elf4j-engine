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

package elf4j.engine.service;

import elf4j.engine.NativeLogger;
import elf4j.engine.writer.PerformanceSensitive;

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
     * @param nativeLogger          the serviced logger
     * @param serviceInterfaceClass The concrete logging service (logger) implementation class that the client calls
     *                              directly at runtime to issue the log. For the native ELF4J service implementation,
     *                              this is always the {@link NativeLogger} class; may be a different class if this core
     *                              library is used to service other logging API. The real-time caller of this class,
     *                              therefore, is the logging service's "caller class" whose details, such as method and
     *                              line number, may need to be resolved by walking the runtime calling stack trace if
     *                              such details are required per configuration.
     * @param exception             to log
     * @param message               to log, can have argument placeholders
     * @param args                  to replace the placeholders in the message
     */
    void log(NativeLogger nativeLogger,
            Class<?> serviceInterfaceClass,
            Throwable exception,
            Object message,
            Object[] args);
}
