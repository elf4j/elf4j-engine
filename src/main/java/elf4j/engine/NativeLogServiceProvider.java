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

import static java.util.stream.Collectors.toMap;

import elf4j.Level;
import elf4j.Logger;
import elf4j.engine.service.EventingNativeLoggerService;
import elf4j.engine.service.NativeLogServiceManager;
import elf4j.engine.service.NativeLoggerService;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.util.StackTraces;
import elf4j.spi.LogServiceProvider;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.NonNull;
import org.slf4j.MdcAdapterInitializer;

/**
 * The NativeLogServiceProvider class implements the LogServiceProvider and
 * NativeLogServiceManager.Refreshable interfaces. It is responsible for managing and providing
 * NativeLogger instances for logging purposes. It also provides methods for refreshing the
 * properties of the log service.
 *
 * <p>This class contains a map of native loggers, categorized by their level, and a reference to
 * the class or interface that the API client calls first to get a logger instance. The client
 * caller class of this class will be the declaring class of the logger instances this factory
 * produces.
 *
 * <p>For this native implementation, the service access class is the {@link Logger} interface
 * itself as the client calls the static factory method {@link Logger#instance()} first to get a
 * logger instance. If this library is used as the engine of another logging API, then this access
 * class would be the class in that API that the client calls first to get a logger instance of that
 * API.
 */
public class NativeLogServiceProvider
    implements LogServiceProvider, NativeLogServiceManager.Refreshable {
  private static final Level DEFAULT_LOGGER_SEVERITY_LEVEL = Level.INFO;
  /** Made injectable for extensions other than this native ELF4J implementation */
  @NonNull private final Level defaultLoggerLevel;

  /** A map of native loggers, categorized by their level. */
  private final Map<Level, Map<String, NativeLogger>> nativeLoggers =
      EnumSet.allOf(Level.class).stream()
          .collect(toMap(Function.identity(), level -> new ConcurrentHashMap<>()));
  /**
   * The class or interface that the API client calls first to get a logger instance. The client
   * caller class of this class will be the declaring class of the logger instances this factory
   * produces.
   *
   * <p>For this native implementation, the service access class is the {@link Logger} interface
   * itself as the client calls the static factory method {@link Logger#instance()} first to get a
   * logger instance. If this library is used as the engine of another logging API, then this access
   * class would be the class in that API that the client calls first to get a logger instance of
   * that API.
   */
  @NonNull private final Class<?> serviceAccessClass;

  @NonNull private final NativeLogServiceProvider.NativeLoggerServiceFactory nativeLoggerServiceFactory;

  /** Default constructor required by {@link java.util.ServiceLoader} */
  @SuppressWarnings("unused")
  public NativeLogServiceProvider() {
    this(Logger.class);
  }

  /**
   * NativeLogServiceProvider constructor with the default logger level and the service access
   * class.
   *
   * @param serviceAccessClass the class or interface that the API client application calls first to
   *     a logger instance
   */
  public NativeLogServiceProvider(@NonNull Class<?> serviceAccessClass) {
    this(
        DEFAULT_LOGGER_SEVERITY_LEVEL,
        serviceAccessClass,
        new ConfiguredNativeLoggerServiceFactory());
  }

  /**
   * Constructor for the NativeLogServiceProvider class.
   *
   * @param defaultLoggerLevel the default logger level
   * @param serviceAccessClass the class or interface that the API client application calls first to
   *     a logger instance
   * @param nativeLoggerServiceFactory the factory for creating native logger services
   */
  NativeLogServiceProvider(
      @NonNull Level defaultLoggerLevel,
      @NonNull Class<?> serviceAccessClass,
      @NonNull NativeLogServiceProvider.NativeLoggerServiceFactory nativeLoggerServiceFactory) {
    MdcAdapterInitializer.initialize();
    this.defaultLoggerLevel = defaultLoggerLevel;
    this.serviceAccessClass = serviceAccessClass;
    this.nativeLoggerServiceFactory = nativeLoggerServiceFactory;
    NativeLogServiceManager.INSTANCE.register(this);
  }

  /**
   * More expensive logger instance creation as it uses stack trace to locate the client class
   * (declaring class) requesting the Logger instance.
   *
   * @return new instance of {@link NativeLogger}
   */
  @Override
  public NativeLogger logger() {
    return getLogger(
        defaultLoggerLevel, StackTraces.callerOf(serviceAccessClass).getClassName());
  }

  /**
   * Refreshes the properties of the log service.
   *
   * @param properties the new properties for the log service
   */
  @Override
  public void refresh(@Nullable Properties properties) {
    nativeLoggerServiceFactory.reset(properties);
  }

  /** Reloads the log service. */
  @Override
  public void refresh() {
    nativeLoggerServiceFactory.reload();
  }

  /**
   * Gets the log service.
   *
   * @return the log service
   */
  @NonNull NativeLoggerService getLogService() {
    return nativeLoggerServiceFactory.getLogService();
  }

  /**
   * Gets a logger with the specified level and declaring class name.
   *
   * @param level the level of the logger
   * @param declaringClassName the name of the declaring class
   * @return the logger
   */
  NativeLogger getLogger(Level level, String declaringClassName) {
    return nativeLoggers
        .get(level)
        .computeIfAbsent(declaringClassName, k -> new NativeLogger(k, level, this));
  }

  /**
   * The NativeLoggerServiceFactory interface provides methods for getting the log service,
   * reloading the log service, and resetting the log service with the specified properties.
   */
  interface NativeLoggerServiceFactory {
    /**
     * Gets the log service.
     *
     * @return the log service
     */
    NativeLoggerService getLogService();

    /** Reloads the log service. */
    void reload();

    /**
     * Resets the log service with the specified properties.
     *
     * @param properties the new properties for the log service
     */
    void reset(Properties properties);
  }

  /**
   * The ConfiguredNativeLoggerServiceFactory class implements the NativeLoggerServiceFactory
   * interface and provides a concrete implementation for getting the log service, reloading the log
   * service, and resetting the log service with the specified properties.
   */
  static class ConfiguredNativeLoggerServiceFactory implements NativeLoggerServiceFactory {
    private NativeLoggerService nativeLoggerService;

    /** Constructor for the ConfiguredNativeLoggerServiceFactory class. */
    private ConfiguredNativeLoggerServiceFactory() {
      nativeLoggerService = new EventingNativeLoggerService(LogServiceConfiguration.byLoading());
    }

    /**
     * Gets the log service.
     *
     * @return the log service
     */
    @Override
    public NativeLoggerService getLogService() {
      return nativeLoggerService;
    }

    /** Reloads the log service. */
    @Override
    public void reload() {
      nativeLoggerService = new EventingNativeLoggerService(LogServiceConfiguration.byLoading());
    }

    /**
     * Resets the log service with the specified properties.
     *
     * @param properties the new properties for the log service
     */
    @Override
    public void reset(Properties properties) {
      nativeLoggerService =
          new EventingNativeLoggerService(LogServiceConfiguration.bySetting(properties));
    }
  }
}
