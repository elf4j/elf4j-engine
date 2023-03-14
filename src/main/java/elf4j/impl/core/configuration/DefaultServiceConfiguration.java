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

package elf4j.impl.core.configuration;

import elf4j.Level;
import elf4j.impl.core.NativeLogger;
import elf4j.impl.core.writer.LogWriter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class DefaultServiceConfiguration implements ServiceConfiguration {
    private final Map<NativeLogger, Boolean> loggerConfigurationCache = new ConcurrentHashMap<>();
    private final PropertiesLoader propertiesLoader;
    private boolean noop;
    private LevelRepository levelRepository;
    private WriterRepository writerRepository;

    /**
     *
     */
    public DefaultServiceConfiguration() {
        this(new PropertiesLoader());
    }

    DefaultServiceConfiguration(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        setRepositories(propertiesLoader.load());
    }

    DefaultServiceConfiguration(LevelRepository levelRepository, WriterRepository writerRepository) {
        this(new PropertiesLoader());
        this.levelRepository = levelRepository;
        this.writerRepository = writerRepository;
    }

    @Override
    public LogWriter getLogServiceWriter() {
        return writerRepository.getLogServiceWriter();
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        if (this.noop) {
            return false;
        }
        return this.loggerConfigurationCache.computeIfAbsent(nativeLogger, this::loadLoggerConfigurationCache);
    }

    @Override
    public void refresh(@Nullable Properties properties) {
        Properties refreshed = this.propertiesLoader.load();
        if (properties != null) {
            refreshed.putAll(properties);
        }
        setRepositories(refreshed);
        this.loggerConfigurationCache.clear();
    }

    private boolean loadLoggerConfigurationCache(NativeLogger nativeLogger) {
        Level loggerMinimumLevel = levelRepository.getLoggerMinimumLevel(nativeLogger);
        Level logServiceWriterMinimumLevel = writerRepository.getLogServiceWriter().getMinimumLevel();
        int effectiveMinimumLevelOrdinal =
                Math.max(loggerMinimumLevel.ordinal(), logServiceWriterMinimumLevel.ordinal());
        return nativeLogger.getLevel().ordinal() >= effectiveMinimumLevelOrdinal;
    }

    private void setRepositories(@NonNull Properties properties) {
        this.noop = Boolean.parseBoolean(properties.getProperty("noop"));
        if (this.noop) {
            System.err.println("ELF4J status: No-op per configuration");
        }
        this.levelRepository = new LevelRepository(properties);
        this.writerRepository = new WriterRepository(properties);
    }
}
