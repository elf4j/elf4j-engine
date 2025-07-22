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
import elf4j.engine.logging.LogHandlerFactory;
import elf4j.engine.logging.util.StackTraces;
import elf4j.spi.LoggerFactory;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
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
  private static final Set<Class<?>> ELF4J_SERVICE_ACCESS_CLASSES = Set.of(Logger.class);

  /** Made injectable for extensions other than this native ELF4J implementation */
  private final Level defaultLoggerLevel;

  /**
   * The class or interface that the API client calls first to get a {@link Logger} instance. The
   * client caller class of this class will be the declaring class of the logger instances this
   * factory produces.
   *
   * @implNote The log service access class(es) (providing the "service access API" in the
   *     {@link ServiceLoader} framework). In this case, it is always the {@link Logger} interface
   *     itself because, at runtime, the log service client calls the static factory method
   *     {@link Logger#instance()} first to get (gain "access" to) a reference to the log service
   *     interface.
   */
  private final Set<String> logServiceAccessClassNames;

  private final LogHandlerFactory logHandlerFactory;

  /**
   * Default constructor required by {@link ServiceLoader}
   *
   * @apiNote This no-arg constructor is required by the {@link ServiceLoader} framework when
   *     working as an elf4j service provider. It is not meant to be used by the client code or
   *     other logging API trying to use this as a generic log engine.
   */
  @SuppressWarnings("unused")
  public NativeLoggerFactory() {
    this(ELF4J_SERVICE_ACCESS_CLASSES);
  }

  /**
   * NativeLoggerFactory constructor with the default logger level
   *
   * @param logServiceAccessClasses the concrete implementation of the log service access API. In
   *     this case, since the sole log service access API is the static method
   *     {@link Logger#instance()}, the service access class is always the {@code Logger} interface
   *     itself.
   * @apiNote This constructor can be used by other logging frameworks trying to use this as its own
   *     log engine. The specified log service access class is whatever class the other framework
   *     uses to provide access/factory method(s) for the client code to obtain/gain a reference to
   *     call the log service operations.
   */
  public NativeLoggerFactory(Set<Class<?>> logServiceAccessClasses) {
    this(logServiceAccessClasses, DEFAULT_LOGGER_SEVERITY_LEVEL, new ConfiguredLogHandlerFactory());
  }

  /**
   * Constructor for the NativeLoggerFactory class.
   *
   * @param logServiceAccessClasses the concrete implementation of the log service access API. In
   *     this case, it is always the {@link Logger} interface itself as the access API is a static
   *     method of the interface.
   * @param defaultLoggerLevel the default severity level this factory will produce logger instances
   *     with
   * @param logHandlerFactory the factory for native log handler. Capable of reconfiguring the
   *     handler at runtime.
   */
  private NativeLoggerFactory(
      Set<Class<?>> logServiceAccessClasses,
      Level defaultLoggerLevel,
      LogHandlerFactory logHandlerFactory) {
    MdcAdapterInitializer.initialize();
    this.logServiceAccessClassNames =
        logServiceAccessClasses.stream().map(Class::getName).collect(Collectors.toSet());
    this.defaultLoggerLevel = defaultLoggerLevel;
    this.logHandlerFactory = logHandlerFactory;
  }

  /**
   * More performance-wise expensive logger instance creation as it uses run-time stack trace to
   * locate the client class calling the {@link #logServiceAccessClassNames}.
   *
   * @return new instance of {@link NativeLogger}
   */
  @Override
  public NativeLogger getLogger() {
    return new NativeLogger(
        new NativeLogger.LoggerId(
            StackTraces.earliestCallerOfAny(logServiceAccessClassNames).getClassName(),
            defaultLoggerLevel),
        logHandlerFactory);
  }
}
