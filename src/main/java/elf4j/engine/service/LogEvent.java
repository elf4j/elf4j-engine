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

package elf4j.engine.service;

import elf4j.engine.NativeLogger;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;

/** Source data to be rendered to a final log message */
@Value
@Builder
public class LogEvent {
  private static final int ADDITIONAL_STRING_BUILDER_CAPACITY = 32;

  NativeLogger nativeLogger;

  ThreadValue callerThread;

  Instant timestamp = Instant.now();

  @Nullable Object message;

  Object @Nullable [] arguments;

  @Nullable Throwable throwable;

  @Nullable Class<?> serviceInterfaceClass;

  @Nullable StackFrameValue callerFrame;

  private static CharSequence resolve(@Nullable Object message, Object @Nullable [] arguments) {
    String suppliedMessage = Objects.toString(supply(message), "");
    if (arguments == null || arguments.length == 0) {
      return suppliedMessage;
    }
    int messageLength = suppliedMessage.length();
    StringBuilder resolved = new StringBuilder(messageLength + ADDITIONAL_STRING_BUILDER_CAPACITY);
    int i = 0;
    int j = 0;
    while (i < messageLength) {
      char character = suppliedMessage.charAt(i);
      if (character == '{'
          && ((i + 1) < messageLength && suppliedMessage.charAt(i + 1) == '}')
          && j < arguments.length) {
        resolved.append(supply(arguments[j++]));
        i += 2;
      } else {
        resolved.append(character);
        i += 1;
      }
    }
    return resolved;
  }

  private static @Nullable Object supply(@Nullable Object o) {
    return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
  }

  /**
   * Returns the name of the application client class calling the logging method of this logger
   * instance.
   *
   * @return the name of the caller class
   */
  public String getCallerClassName() {
    return callerFrame != null ? callerFrame.getClassName() : nativeLogger.getDeclaringClassName();
  }

  /**
   * Returns the log message text with all placeholder arguments resolved and replaced by final
   * values.
   *
   * @return the resolved log message
   */
  public CharSequence getResolvedMessage() {
    return resolve(this.message, this.arguments);
  }

  /** Represents a value representing a call stack element. */
  @Value
  @Builder
  public static class StackFrameValue {
    String className;
    String methodName;
    int lineNumber;

    @Nullable String fileName;

    /**
     * Creates a StackFrameValue instance from a StackTraceElement.
     *
     * @param stackTraceElement call stack element
     * @return log render-able value representing the call stack element
     */
    public static StackFrameValue from(StackTraceElement stackTraceElement) {
      return LogEvent.StackFrameValue.builder()
          .fileName(stackTraceElement.getFileName())
          .className(stackTraceElement.getClassName())
          .methodName(stackTraceElement.getMethodName())
          .lineNumber(stackTraceElement.getLineNumber())
          .build();
    }
  }

  /** Represents the value of a thread. */
  public record ThreadValue(String name, long id) {}
}
