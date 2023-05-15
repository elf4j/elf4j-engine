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
import elf4j.engine.service.ConseqLogEventProcessor;
import elf4j.engine.service.LogEventProcessor;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.writer.DirectStandardOutput;
import elf4j.engine.service.writer.CooperatingWriterGroup;
import elf4j.engine.service.writer.LogWriter;
import elf4j.engine.service.writer.StandardOutput;
import elf4j.util.IeLogger;
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
    private final PropertiesLoader propertiesLoader;
    private Properties properties;
    private boolean noop;
    private CallerLevels callerLevels;
    private Map<NativeLogger, Boolean> loggerEnablementCache;
    private StandardOutput standardOutput;
    private LogWriter logServiceWriter;
    private LogEventProcessor logEventProcessor;

    /**
     *
     */
    public RefreshableLogServiceConfiguration() {
        this(new FilePropertiesLoader());
    }

    RefreshableLogServiceConfiguration(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        this.properties = this.propertiesLoader.load();
        parse(this.properties);
        LogServiceManager.INSTANCE.registerRefresh(this);
    }

    @Override
    public Properties getProperties() {
        return this.properties;
    }

    @Override
    public LogWriter getLogServiceWriter() {
        return this.logServiceWriter;
    }

    @Override
    public boolean isEnabled(NativeLogger nativeLogger) {
        if (this.noop) {
            return false;
        }
        return this.loggerEnablementCache.computeIfAbsent(nativeLogger, this::loadLoggerConfigurationCache);
    }

    @Override
    public StandardOutput getStandardOutput() {
        return this.standardOutput;
    }

    public LogEventProcessor getLogEventProcessor() {
        return this.logEventProcessor;
    }

    @Override
    public void refresh(@Nullable Properties properties) {
        this.properties = properties == null ? this.propertiesLoader.load() : properties;
        parse(this.properties);
    }

    private boolean loadLoggerConfigurationCache(NativeLogger nativeLogger) {
        Level callerMinimumOutputLevel = callerLevels.getCallerMinimumOutputLevel(nativeLogger);
        Level writerMinimumOutputLevel = logServiceWriter.getMinimumOutputLevel();
        Level loggerLevel = nativeLogger.getLevel();
        return loggerLevel.compareTo(callerMinimumOutputLevel) >= 0
                && loggerLevel.compareTo(writerMinimumOutputLevel) >= 0;
    }

    private void parse(@Nullable Properties properties) {
        IeLogger.INFO.log("Configuration properties: {}", properties);
        if (properties == null) {
            IeLogger.WARN.log("No-op as in no configuration");
            this.noop = true;
            return;
        }
        this.noop = Boolean.parseBoolean(properties.getProperty("noop"));
        if (this.noop) {
            IeLogger.WARN.log("No-op as configured");
            return;
        }
        this.callerLevels = CallerLevels.from(properties);
        this.loggerEnablementCache = new ConcurrentHashMap<>();
        this.standardOutput = DirectStandardOutput.from(this);
        this.logServiceWriter = CooperatingWriterGroup.from(this);
        this.logEventProcessor = ConseqLogEventProcessor.from(this);
    }
}
