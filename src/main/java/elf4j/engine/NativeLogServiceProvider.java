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
import elf4j.engine.logging.EventingLogHandler;
import elf4j.engine.logging.LogHandler;
import elf4j.engine.logging.NativeLogServiceManager;
import elf4j.engine.logging.config.ConfigurationProperties;
import elf4j.engine.logging.util.StackTraces;
import elf4j.spi.LogServiceProvider;
import java.util.EnumSet;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
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
  private final Level defaultLoggerLevel;

  /**
   * The class or interface that the API client calls first to get a {@link Logger} instance. The
   * client caller class of this class will be the declaring class of the logger instances this
   * factory produces.
   *
   * @implNote The log service access class (providing the "service access API" in the
   *     {@link ServiceLoader} framework). In this case, it is always the {@link Logger} interface
   *     itself because, at runtime, the log service client calls the static factory method
   *     {@link Logger#instance()} first to get (gain "access" to) a reference to the log service
   *     interface.
   */
  private final Class<?> logServiceAccessClass;

  private final LogHandlerFactory logHandlerFactory;

  /** A map of native loggers, categorized by their level. */
  private final Map<Level, Map<String, NativeLogger>> nativeLoggers =
      EnumSet.allOf(Level.class).stream()
          .collect(toMap(Function.identity(), level -> new ConcurrentHashMap<>()));

  /** Default constructor required by {@link ServiceLoader} */
  @SuppressWarnings("unused")
  public NativeLogServiceProvider() {
    this(Logger.class, NativeLogger.class);
  }

  /**
   * NativeLogServiceProvider constructor with the default logger level
   *
   * @param logServiceProviderClass the concrete implementation of the log service access API. In
   *     this case, since the sole log service access API is the static method
   *     {@link Logger#instance()}, the provider class is always the {@code Logger} interface
   *     itself.
   * @param logServiceClass the concrete implementation of the log service interface API. In this
   *     case, it is the {@code NativeLogger} class, as opposed to the {@code Logger} interface. See
   *     the Javadoc of the {@link NativeLogger#loggerName} field.
   */
  public NativeLogServiceProvider(Class<?> logServiceProviderClass, Class<?> logServiceClass) {
    this(
        DEFAULT_LOGGER_SEVERITY_LEVEL,
        logServiceProviderClass,
        new ConfiguredLogHandlerFactory(logServiceClass));
  }

  /**
   * Constructor for the NativeLogServiceProvider class.
   *
   * @param defaultLoggerLevel the default logger level
   * @param logServiceAccessClass the runtime implementation class of the log service access API
   * @param logHandlerFactory the factory for native log handler. Capable of reconfiguring the
   *     handler at runtime.
   */
  NativeLogServiceProvider(
      Level defaultLoggerLevel,
      Class<?> logServiceAccessClass,
      LogHandlerFactory logHandlerFactory) {
    MdcAdapterInitializer.initialize();
    this.defaultLoggerLevel = defaultLoggerLevel;
    this.logServiceAccessClass = logServiceAccessClass;
    this.logHandlerFactory = logHandlerFactory;
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
        defaultLoggerLevel,
        StackTraces.callerFrameOf(logServiceAccessClass.getName()).getClassName());
  }

  /**
   * Refreshes the properties of the log service.
   *
   * @param properties the new properties for the log service
   */
  @Override
  public void refresh(@Nullable Properties properties) {
    logHandlerFactory.reset(properties);
  }

  /** Reloads the log service. */
  @Override
  public void refresh() {
    logHandlerFactory.reload();
  }

  /**
   * Gets the log service.
   *
   * @return the log service
   */
  LogHandler getLogHandler() {
    return logHandlerFactory.getLogHandler();
  }

  /**
   * Gets a logger with the specified level and declaring class name.
   *
   * @param level the level of the logger
   * @param loggerName name of the class calling to obtain a Logger instance
   * @return the logger service instance
   */
  NativeLogger getLogger(Level level, String loggerName) {
    return nativeLoggers
        .get(level)
        .computeIfAbsent(loggerName, k -> new NativeLogger(loggerName, level, this));
  }

  /**
   * The LogHandlerFactory interface provides methods for getting the log handler. Capable of
   * reconfiguring the log handler with the specified properties at runtime.
   */
  interface LogHandlerFactory {
    /**
     * Gets the log service.
     *
     * @return the log service
     */
    LogHandler getLogHandler();

    /** Reloads the log service. */
    void reload();

    /**
     * Resets the log service with the specified properties.
     *
     * @param properties the new properties for the log service
     */
    void reset(@Nullable Properties properties);
  }

  /**
   * The ConfiguredLogHandlerFactory class implements the LogHandlerFactory interface and provides a
   * concrete implementation for getting the log service, reloading the log service, and resetting
   * the log service with the specified properties.
   */
  static class ConfiguredLogHandlerFactory implements LogHandlerFactory {
    private final Class<?> logServiceInterfaceClass;
    private LogHandler logHandler;

    /** Constructor for the ConfiguredLogHandlerFactory class. */
    private ConfiguredLogHandlerFactory(Class<?> logServiceInterfaceClass) {
      this.logServiceInterfaceClass = logServiceInterfaceClass;
      logHandler =
          new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceInterfaceClass);
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
    @Override
    public void reload() {
      logHandler =
          new EventingLogHandler(ConfigurationProperties.byLoading(), logServiceInterfaceClass);
    }

    /**
     * Resets the log service with the specified properties.
     *
     * @param properties the new properties for the log service
     */
    @Override
    public void reset(@Nullable Properties properties) {
      logHandler = new EventingLogHandler(
          ConfigurationProperties.bySetting(properties), logServiceInterfaceClass);
    }
  }
}
