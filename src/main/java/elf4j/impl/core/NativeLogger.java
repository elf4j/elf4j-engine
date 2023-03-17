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
 * Instances of this class are thread-safe, and can be used as class, instance, or local variables. It is recommended,
 * however, to use class variables for instances that are returned by the static factory method
 * {@link Logger#instance()}, as those instances are more expensive to create. Other instances returned by the
 * fluent-style factory methods, such as {@link Logger#atError()}, are inexpensive to create and can be used (and
 * discarded) in-line or as convenient.
 */
@ThreadSafe
@Value
public class NativeLogger implements Logger {
    /**
     * Name of this logger's "owner class" - the logging service client class that first requested for this logger
     * instance via the {@link Logger#instance()} service access method. The owner class is usually the same as the
     * "caller class" - the client class that calls the service interface methods, such as {@link Logger#log(Object)}.
     * <p></p>
     * In rare and not-recommended scenarios, the owner class can be different from the caller class: e.g. the owner
     * class could pass a reference of this logger instance out to a different/caller class. Once set, though, the value
     * of this field will never change even when the owner class is different from the caller.
     * <p></p>
     * In general, this native ELF4J implementation assumes owner and caller class to be the same.
     */
    @NonNull String ownerClassName;
    @NonNull Level level;
    @EqualsAndHashCode.Exclude @NonNull LogService logService;

    /**
     * Constructor only meant to be used by {@link NativeLoggerFactory} and this class itself
     *
     * @param ownerClassName name of the owner class that requested this instance via the {@link Logger#instance()}
     *                       method
     * @param level          severity level of this logger instance
     * @param logService     service delegate to do the logging
     */
    public NativeLogger(@NonNull String ownerClassName, @NonNull Level level, @NonNull LogService logService) {
        this.ownerClassName = ownerClassName;
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
        return this.level == level ? this : new NativeLogger(this.ownerClassName, level, logService);
    }

    private void service(Throwable exception, Object message, Object[] args) {
        this.logService.log(this, NativeLogger.class, exception, message, args);
    }
}
