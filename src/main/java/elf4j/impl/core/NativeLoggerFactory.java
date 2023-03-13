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
import elf4j.impl.core.configuration.DefaultLoggingConfiguration;
import elf4j.impl.core.configuration.LoggingConfiguration;
import elf4j.impl.core.service.DefaultLogService;
import elf4j.impl.core.service.LogService;
import elf4j.impl.core.service.WriterThreadProvider;
import elf4j.impl.core.util.StackTraceUtils;
import elf4j.spi.LoggerFactory;
import lombok.NonNull;

import java.util.Properties;

/**
 *
 */
public class NativeLoggerFactory implements LoggerFactory {
    private static final Class<Logger> DEFAULT_ACCESS_INTERFACE = Logger.class;
    private static final Level DEFAULT_LOGGER_SEVERITY_LEVEL = Level.TRACE;
    private static final Class<?> DEFAULT_SERVICE_INTERFACE = NativeLogger.class;
    @NonNull private final Level defaultLoggerLevel;
    @NonNull private final Class<?> accessInterface;
    @NonNull private final LogService logService;

    /**
     * Default constructor required by {@link java.util.ServiceLoader}
     */
    public NativeLoggerFactory() {
        this(DEFAULT_ACCESS_INTERFACE, DEFAULT_SERVICE_INTERFACE);
    }

    /**
     * @param accessInterface  the class that the API client uses to obtain access to a logger instance
     * @param serviceInterface the class that the API client uses to invoke logging service
     */
    public NativeLoggerFactory(@NonNull Class<?> accessInterface, @NonNull Class<?> serviceInterface) {
        this(DEFAULT_LOGGER_SEVERITY_LEVEL,
                accessInterface,
                serviceInterface,
                ConfigurationInstanceHolder.INSTANCE,
                new WriterThreadProvider());
    }

    NativeLoggerFactory(@NonNull Level defaultLoggerLevel,
            @NonNull Class<?> accessInterface,
            @NonNull Class<?> serviceInterface,
            @NonNull LoggingConfiguration loggingConfiguration,
            @NonNull WriterThreadProvider writerThreadProvider) {
        this.defaultLoggerLevel = defaultLoggerLevel;
        this.accessInterface = accessInterface;
        this.logService = new DefaultLogService(serviceInterface, loggingConfiguration, writerThreadProvider);
    }

    /**
     *
     */
    public static void refreshConfiguration() {
        refreshConfiguration(null);
    }

    /**
     * @param properties used to override those reloaded from configuration file
     */
    public static void refreshConfiguration(Properties properties) {
        ConfigurationInstanceHolder.INSTANCE.refresh(properties);
    }

    @Override
    public NativeLogger logger() {
        return new NativeLogger(StackTraceUtils.callerOf(this.accessInterface).getClassName(),
                defaultLoggerLevel,
                logService);
    }

    private static class ConfigurationInstanceHolder {
        private static final LoggingConfiguration INSTANCE = new DefaultLoggingConfiguration();
    }
}
