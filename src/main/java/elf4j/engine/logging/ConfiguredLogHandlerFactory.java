/*
 * MIT License
 *
 * Copyright (c) 2025 Qingtian Wang
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
package elf4j.engine.logging;

import elf4j.engine.logging.config.ConfigurationProperties;
import java.util.Properties;
import org.jspecify.annotations.Nullable;

/**
 * The ConfiguredLogHandlerFactory class implements the LogHandlerFactory interface and provides a
 * concrete implementation for getting the log service, reloading the log service, and resetting the
 * log service with the specified properties.
 */
public class ConfiguredLogHandlerFactory
    implements LogHandlerFactory, NativeLogServiceManager.Refreshable {
  private final Class<?> logServiceClass;
  private LogHandler logHandler;

  /** Constructor for the ConfiguredLogHandlerFactory class. */
  public ConfiguredLogHandlerFactory(Class<?> logServiceClass) {
    this.logServiceClass = logServiceClass;
    logHandler = new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceClass);
    NativeLogServiceManager.INSTANCE.register(this);
  }

  /**
   * Gets the log service.
   *
   * @return the log service
   */
  @Override
  public LogHandler getLogHandler() {
    return logHandler;
  }

  /** Reloads the log service. */
  private void reload() {
    logHandler = new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceClass);
  }

  /**
   * Resets the log service with the specified properties.
   *
   * @param properties the new properties for the log service
   */
  private void reset(@Nullable Properties properties) {
    logHandler =
        new EventingLogHandler(ConfigurationProperties.bySetting(properties), logServiceClass);
  }

  @Override
  public void refresh(@Nullable Properties properties) {
    reset(properties);
  }

  @Override
  public void refresh() {
    reload();
  }
}
