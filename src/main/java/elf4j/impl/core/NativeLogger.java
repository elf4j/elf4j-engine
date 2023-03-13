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

package elf4j.impl.core;

import elf4j.Level;
import elf4j.Logger;
import elf4j.impl.core.service.LogService;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.concurrent.ThreadSafe;

/**
 *
 */
@ThreadSafe
@Value
public class NativeLogger implements Logger {
    /**
     * Taken from the same name of this logger's "owner class" - the logging service client class that created this
     * logger instance via the service access API. The owner class is usually also the same as the "caller class" - the
     * logging service client class that calls the logging service API methods of this instance. In rare and
     * unrecommended scenarios, the owner class can be different from the caller class i.e. the owner class could pass a
     * reference of this logger instance out to a different/caller class. Once set, though, the name of the logger will
     * not change even when the owner class is different from the caller class.
     */
    @NonNull String name;
    @NonNull Level level;
    @EqualsAndHashCode.Exclude @NonNull LogService logService;

    /**
     * @param name       logger name, same as that of the owner class that created this logger instance
     * @param level      severity level of this logger instance
     * @param logService service delegate to do the logging
     */
    public NativeLogger(@NonNull String name, @NonNull Level level, @NonNull LogService logService) {
        this.name = name;
        this.level = level;
        this.logService = logService;
    }

    @Override
    public NativeLogger atDebug() {
        return atLevel(Level.DEBUG);
    }

    @Override
    public NativeLogger atError() {
        return atLevel(Level.ERROR);
    }

    @Override
    public NativeLogger atInfo() {
        return atLevel(Level.INFO);
    }

    @Override
    public NativeLogger atTrace() {
        return atLevel(Level.TRACE);
    }

    @Override
    public NativeLogger atWarn() {
        return atLevel(Level.WARN);
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        return this.logService.isEnabled(this);
    }

    @Override
    public void log(Object message) {
        this.service(null, message, null);
    }

    @Override
    public void log(String message, Object... args) {
        this.service(null, message, args);
    }

    @Override
    public void log(Throwable t) {
        this.service(t, null, null);
    }

    @Override
    public void log(Throwable t, Object message) {
        this.service(t, message, null);
    }

    @Override
    public void log(Throwable t, String message, Object... args) {
        this.service(t, message, args);
    }

    /**
     * @param level of the returned logger instance
     * @return logger instance of the same name, with the specified level
     */
    public NativeLogger atLevel(Level level) {
        return this.level == level ? this : new NativeLogger(this.name, level, logService);
    }

    private void service(Throwable exception, Object message, Object[] args) {
        this.logService.log(this, exception, message, args);
    }
}
