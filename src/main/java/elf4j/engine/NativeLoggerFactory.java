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
import elf4j.engine.logging.LogHandlerFactory;
import elf4j.engine.logging.RefreshableLogHandlerFactory;
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
 */
public class NativeLoggerFactory implements LoggerFactory {
  public static final Level DEFAULT_LOG_SEVERITY_LEVEL = Level.INFO;

  /**
   * The default class or interface that the API client calls first to get a {@link Logger}
   * instance, providing the "service access API" in the {@link ServiceLoader} framework. The
   * client caller class of this class is the declaring class of the logger instances this factory
   * produces.
   *
   * @implNote In the default case, it is always the {@link Logger} interface itself because, at
   *     runtime, the log service client calls the static factory method {@link Logger#instance()}
   *     first to get (gain "access" to) a reference to the log service interface.
   */
  public static final Set<Class<?>> ELF4J_SERVICE_ACCESS_CLASSES = Set.of(Logger.class);

  /**
   * The default concrete implementation class(es) of the log service API that the client calls at
   * runtime.
   *
   * @implNote In the default case, it includes not only the {@link NativeLogger} class but also the
   *     {@link Logger} interface itself as its default methods are directly called by the client at
   *     runtime.
   */
  public static final Set<Class<?>> ELF4J_SERVICE_CLASSES =
      Set.of(NativeLogger.class, Logger.class);

  /** Made injectable for extensions other than this native ELF4J implementation */
  private final Level defaultLogSeverityLevel;

  /**
   * The class or interface that the API client calls first to get a {@link Logger} instance. The
   * client caller class of this class is the declaring class of the logger instances this factory
   * produces.
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
    this(ELF4J_SERVICE_ACCESS_CLASSES, ELF4J_SERVICE_CLASSES);
  }

  /**
   * NativeLoggerFactory constructor with the default logger severity level
   *
   * @param logServiceAccessClasses the concrete implementation of the log service access API. In
   *     the default case, since the sole log service access API is the static method
   *     {@link Logger#instance()}, the service access class is always the {@code Logger} interface
   *     itself.
   * @param logServiceClasses the concrete implementation class(es) of the log service API that the
   *     client calls at runtime. In the default case, it includes not only the {@link NativeLogger}
   *     class but also the {@link Logger} interface itself as its default methods are directly
   *     called by the client at runtime.
   * @apiNote This constructor can be used by other logging frameworks trying to use this as its own
   *     log engine. The specified log service access class is whatever class the other framework
   *     uses to provide access/factory method(s) for the client code to obtain/gain a reference to
   *     call the log service operations.
   */
  public NativeLoggerFactory(
      Set<Class<?>> logServiceAccessClasses, Set<Class<?>> logServiceClasses) {
    this(
        logServiceAccessClasses,
        new RefreshableLogHandlerFactory(logServiceClasses),
        DEFAULT_LOG_SEVERITY_LEVEL);
  }

  /**
   * Constructor for the NativeLoggerFactory class.
   *
   * @param logServiceAccessClasses the concrete implementation of the log service access API. In
   *     this case, it is always the {@link Logger} interface itself as the access API is a static
   *     method of the interface.
   * @param logHandlerFactory the factory for native log handler. Capable of reconfiguring the
   *     handler at runtime.
   * @param defaultLogSeverityLevel the default severity level this factory will produce logger
   *     instances with
   */
  private NativeLoggerFactory(
      Set<Class<?>> logServiceAccessClasses,
      LogHandlerFactory logHandlerFactory,
      Level defaultLogSeverityLevel) {
    this.logServiceAccessClassNames = logServiceAccessClasses.stream()
        .map(Class::getName)
        .collect(Collectors.toUnmodifiableSet());
    this.logHandlerFactory = logHandlerFactory;
    this.defaultLogSeverityLevel = defaultLogSeverityLevel;
    MdcAdapterInitializer.initialize();
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
            defaultLogSeverityLevel),
        logHandlerFactory);
  }
}
