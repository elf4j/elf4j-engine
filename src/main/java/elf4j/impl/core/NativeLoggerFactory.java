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
import elf4j.impl.core.service.DefaultLogService;
import elf4j.impl.core.service.LogService;
import elf4j.impl.core.util.StackTraceUtils;
import elf4j.spi.LoggerFactory;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class NativeLoggerFactory implements LoggerFactory {
    private static final Level DEFAULT_LOGGER_SEVERITY_LEVEL = Level.TRACE;
    private static final Class<Logger> DEFAULT_LOGGING_SERVICE_ACCESS_CLASS = Logger.class;
    @NonNull private final Level defaultLoggerLevel;
    private final Map<String, NativeLogger> nativeLoggers = new HashMap<>();
    /**
     * The class that the API client uses to initiate access and request for a logger instance. The client caller class
     * of this class will be the "owner class" of the logger instances this factory produces.
     * <p></p>
     * For this native implementation, the service access class is the {@link Logger} interface itself as the client
     * calls the static factory method {@link Logger#instance()} to gain access to a logger instance. The client will
     * then use the logger instance to invoke logging services such as the {@link Logger#log} methods.
     * <p></p>
     * If this library is used as the engine of another logging API, then this access class would be the class from that
     * API that the client calls to gain access to that API's logging service.
     */
    @NonNull private final Class<?> serviceAccessClass;
    @NonNull private final LogService logService;

    /**
     * Default constructor required by {@link java.util.ServiceLoader}
     */
    public NativeLoggerFactory() {
        this(DEFAULT_LOGGING_SERVICE_ACCESS_CLASS);
    }

    /**
     * @param serviceAccessClass the class that the API client uses to obtain access to a logger instance
     */
    public NativeLoggerFactory(@NonNull Class<?> serviceAccessClass) {
        this(DEFAULT_LOGGER_SEVERITY_LEVEL, serviceAccessClass, new DefaultLogService());
    }

    NativeLoggerFactory(@NonNull Level defaultLoggerLevel,
            @NonNull Class<?> serviceAccessClass,
            @NonNull LogService logService) {
        this.defaultLoggerLevel = defaultLoggerLevel;
        this.serviceAccessClass = serviceAccessClass;
        this.logService = logService;
    }

    /**
     * A bit heavy as it uses stack trace to locate the client class (owner class) requesting the Logger instance.
     *
     * @return new instance of {@link NativeLogger}
     */
    @Override
    public NativeLogger logger() {
        return this.nativeLoggers.computeIfAbsent(StackTraceUtils.callerOf(this.serviceAccessClass).getClassName(),
                ownerClassName -> new NativeLogger(ownerClassName, defaultLoggerLevel, logService));
    }
}
