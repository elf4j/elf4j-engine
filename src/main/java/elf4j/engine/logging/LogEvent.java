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

package elf4j.engine.logging;

import elf4j.Level;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

/** Source data to be rendered to a final log message */
public record LogEvent(
    Instant timestamp,
    String loggerName,
    Level level,
    @Nullable Throwable throwable,
    @Nullable Object message,
    Object @Nullable [] arguments,
    CallerThreadValue callerThread,
    LogEvent.@Nullable CallerFrameValue callerFrame) {
  private static final int INIT_ARG_LENGTH = 32;

  @Builder
  public LogEvent(
      String loggerName,
      Level level,
      @Nullable Throwable throwable,
      @Nullable Object message,
      Object @Nullable [] arguments,
      CallerThreadValue callerThread,
      LogEvent.@Nullable CallerFrameValue callerFrame) {
    this(
        Instant.now(), loggerName, level, throwable, message, arguments, callerThread, callerFrame);
  }

  private static CharSequence resolve(
      @Nullable final Object message, final Object @Nullable [] arguments) {
    String suppliedMessage = Objects.toString(supply(message));
    if (message == null || arguments == null || arguments.length == 0) {
      return suppliedMessage;
    }
    StringBuilder resolvedMessage = new StringBuilder(suppliedMessage.length() + INIT_ARG_LENGTH);
    int messageIndex = 0;
    int argumentIndex = 0;
    while (messageIndex < suppliedMessage.length()) {
      if (atPlaceHolder(messageIndex, suppliedMessage) && !exceedsBound(argumentIndex, arguments)) {
        resolvedMessage.append(supply(arguments[argumentIndex]));
        argumentIndex += 1;
        messageIndex += 2;
      } else {
        resolvedMessage.append(suppliedMessage.charAt(messageIndex));
        messageIndex += 1;
      }
    }
    return resolvedMessage;
  }

  private static boolean atPlaceHolder(final int index, final String message) {
    if (exceedsLength(index + 1, message)) {
      return false;
    }
    return '{' == message.charAt(index) && '}' == message.charAt(index + 1);
  }

  private static boolean exceedsLength(final int index, final String message) {
    return index >= message.length();
  }

  private static boolean exceedsBound(final int index, final Object[] arguments) {
    return index >= arguments.length;
  }

  private static @Nullable Object supply(@Nullable Object o) {
    return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
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

  /** A renderable value representing a call stack element. */
  public record CallerFrameValue(
      String className, String methodName, int lineNumber, @Nullable String fileName) {
    public static CallerFrameValue from(StackWalker.StackFrame stackFrame) {
      return new CallerFrameValue(
          stackFrame.getClassName(),
          stackFrame.getMethodName(),
          stackFrame.getLineNumber(),
          stackFrame.getFileName());
    }
  }

  /** Represents the value of a thread. */
  public record CallerThreadValue(String name, long id) {}
}
