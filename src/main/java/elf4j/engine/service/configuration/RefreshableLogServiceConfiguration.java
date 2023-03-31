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

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.writer.LogWriter;
import elf4j.util.InternalLogger;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
@ToString
public class RefreshableLogServiceConfiguration implements LogServiceConfiguration, Refreshable {
    private final Map<NativeLogger, Boolean> loggerConfigurationCache = new ConcurrentHashMap<>();
    private final PropertiesLoader propertiesLoader;
    private boolean noop;
    private CallerLevelRepository callerLevelRepository;
    private WriterRepository writerRepository;

    /**
     *
     */
    public RefreshableLogServiceConfiguration() {
        this.propertiesLoader = new PropertiesLoader();
        setRepositories(this.propertiesLoader.load());
        LogServiceManager.INSTANCE.register(this);
    }

    RefreshableLogServiceConfiguration(CallerLevelRepository callerLevelRepository, WriterRepository writerRepository) {
        this.propertiesLoader = new PropertiesLoader();
        this.callerLevelRepository = callerLevelRepository;
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
        setRepositories(properties != null ? properties : this.propertiesLoader.load());
        this.loggerConfigurationCache.clear();
    }

    private boolean loadLoggerConfigurationCache(NativeLogger nativeLogger) {
        Level callerMinimumOutputLevel = callerLevelRepository.getMinimumOutputLevel(nativeLogger);
        Level writerMinimumOutputLevel = writerRepository.getLogServiceWriter().getMinimumOutputLevel();
        Level loggerLevel = nativeLogger.getLevel();
        return loggerLevel.compareTo(callerMinimumOutputLevel) >= 0
                && loggerLevel.compareTo(writerMinimumOutputLevel) >= 0;
    }

    private void setRepositories(@Nullable Properties properties) {
        InternalLogger.INSTANCE.log(Level.INFO, "Configuration properties: " + properties);
        this.noop = properties == null || Boolean.parseBoolean(properties.getProperty("noop"));
        if (this.noop) {
            InternalLogger.INSTANCE.log(Level.WARN, "No-op per configuration");
        }
        this.callerLevelRepository = CallerLevelRepository.from(properties);
        this.writerRepository = WriterRepository.from(properties);
    }
}
