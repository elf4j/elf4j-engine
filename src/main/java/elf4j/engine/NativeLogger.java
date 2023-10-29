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

package elf4j.engine;

import elf4j.Level;
import elf4j.Logger;
import elf4j.engine.service.LogService;
import javax.annotation.concurrent.ThreadSafe;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * Any instance of this class is thread-safe; it can be safely used as static, instance, or local variables. However,
 * instances returned by the static factory method {@link Logger#instance()} are more expensive to create; it is
 * recommended to use them as static variables. Other instances are less expensive; they are fit to be used as any kind
 * of variables.
 */
@ThreadSafe
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NativeLogger implements Logger {
    /**
     * Name of this logger's declaring class - the logging service client class that first requested this logger
     * instance via the {@link Logger#instance()} service access method. The declaring class is usually the same as the
     * "caller class" - the client class that calls the service interface methods such as {@link Logger#log(Object)}.
     * <p>
     * In rare and not-recommended scenarios, the declaring class can be different from the caller class: e.g. the
     * declaring class could pass a reference of this logger instance out to a different/caller class. Once set, though,
     * the value of this field will never change even when the declaring class is different from the caller class.
     * <p>
     * To reduce the frequency of having to walk the call stack in order to locate the caller class, this native ELF4J
     * implementation assumes the declaring and caller class to be one and the same. Thus, for logging output that
     * requires only the caller class name, this field will be used in liu of checking the stack trace; i.e. the stack
     * trace walking is needed only when more caller details (e.g. method name, file name, line number) are required.
     */
    @EqualsAndHashCode.Include
    private final @NonNull String declaringClassName;

    private final @NonNull Level level;
    private final @NonNull NativeLoggerFactory nativeLoggerFactory;

    /**
     * Constructor only meant to be used by {@link NativeLoggerFactory} and this class itself
     *
     * @param declaringClassName name of the declaring class that requested this instance via the
     * {@link Logger#instance()} method
     * @param level severity level of this logger instance
     * @param nativeLoggerFactory log service access point from this instance, not reloadable
     */
    public NativeLogger(
            @NonNull String declaringClassName,
            @NonNull Level level,
            @NonNull NativeLoggerFactory nativeLoggerFactory) {
        this.declaringClassName = declaringClassName;
        this.level = level;
        this.nativeLoggerFactory = nativeLoggerFactory;
    }

    @Override
    public NativeLogger atLevel(Level level) {
        return this.level == level ? this : this.nativeLoggerFactory.getLogger(level, this.declaringClassName);
    }

    @Override
    public @NonNull Level getLevel() {
        return this.level;
    }

    @Override
    public boolean isEnabled() {
        return getLogService().isEnabled(this);
    }

    @Override
    public void log(Object message) {
        this.service(null, message, null);
    }

    @Override
    public void log(String message, Object... arguments) {
        this.service(null, message, arguments);
    }

    @Override
    public void log(Throwable throwable) {
        this.service(throwable, null, null);
    }

    @Override
    public void log(Throwable throwable, Object message) {
        this.service(throwable, message, null);
    }

    @Override
    public void log(Throwable throwable, String message, Object... arguments) {
        this.service(throwable, message, arguments);
    }

    /**
     * @return directly callable log service, useful for other logging frameworks to use this engine
     */
    public LogService getLogService() {
        return this.nativeLoggerFactory.getLogService();
    }

    /**
     * @return declaring/caller class of this logger instance
     */
    public @NonNull String getDeclaringClassName() {
        return this.declaringClassName;
    }

    private void service(Throwable throwable, Object message, Object[] arguments) {
        getLogService().log(this, NativeLogger.class, throwable, message, arguments);
    }
}
