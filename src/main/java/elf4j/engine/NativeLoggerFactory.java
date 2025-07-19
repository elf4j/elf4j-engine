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
import elf4j.engine.logging.ConfiguredLogHandlerFactory;
import elf4j.engine.logging.LogHandler;
import elf4j.engine.logging.LogHandlerFactory;
import elf4j.engine.logging.util.StackTraces;
import elf4j.spi.LoggerFactory;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MdcAdapterInitializer;

/**
 * The implementation class of the elf4j SPI.
 *
 * <p>It also provides methods for refreshing the properties of the log service.
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
public class NativeLoggerFactory implements LoggerFactory {
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
  private final Map<NativeLogger.LoggerId, NativeLogger> nativeLoggers = new ConcurrentHashMap<>();

  /** Default constructor required by {@link ServiceLoader} */
  @SuppressWarnings("unused")
  public NativeLoggerFactory() {
    this(Logger.class, NativeLogger.class);
  }

  /**
   * NativeLoggerFactory constructor with the default logger level
   *
   * @param logServiceAccessClass the concrete implementation of the log service access API. In this
   *     case, since the sole log service access API is the static method {@link Logger#instance()},
   *     the service access class is always the {@code Logger} interface itself.
   * @param logServiceClass the concrete implementation of the log service interface API. In this
   *     case, it is the {@code NativeLogger} class, as opposed to the {@code Logger} interface.
   */
  public NativeLoggerFactory(Class<?> logServiceAccessClass, Class<?> logServiceClass) {
    this(
        DEFAULT_LOGGER_SEVERITY_LEVEL,
        logServiceAccessClass,
        new ConfiguredLogHandlerFactory(logServiceClass));
  }

  /**
   * Constructor for the NativeLoggerFactory class.
   *
   * @param defaultLoggerLevel the default logger level
   * @param logServiceAccessClass the concrete implementation of the log service access API. In this
   *     case, it is always the {@link Logger} interface itself as the access API is a static method
   *     of the interface.
   * @param logHandlerFactory the factory for native log handler. Capable of reconfiguring the
   *     handler at runtime.
   */
  NativeLoggerFactory(
      Level defaultLoggerLevel,
      Class<?> logServiceAccessClass,
      LogHandlerFactory logHandlerFactory) {
    MdcAdapterInitializer.initialize();
    this.defaultLoggerLevel = defaultLoggerLevel;
    this.logServiceAccessClass = logServiceAccessClass;
    this.logHandlerFactory = logHandlerFactory;
  }

  /**
   * More performance-wise expensive logger instance creation as it uses run-time stack trace to
   * locate the client class calling the {@link #logServiceAccessClass}.
   *
   * @return new instance of {@link NativeLogger}
   */
  @Override
  public NativeLogger getLogger() {
    return getLogger(new NativeLogger.LoggerId(
        StackTraces.callerFrameOf(logServiceAccessClass.getName()).getClassName(),
        defaultLoggerLevel));
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
   * Gets a logger with the specified level and (access API caller class) name. Should be
   * performance-wise inexpensive to call.
   *
   * @return the logger service instance
   * @implNote Use caching for speed
   */
  NativeLogger getLogger(NativeLogger.LoggerId loggerId) {
    return nativeLoggers.computeIfAbsent(loggerId, k -> new NativeLogger(loggerId, this));
  }
}
