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
    Level level,
    String loggerName,
    ThreadValue callerThread,
    Instant timestamp,
    @Nullable Object message,
    Object @Nullable [] arguments,
    @Nullable Throwable throwable,
    @Nullable StackFrameValue callerFrame) {
  private static final int INIT_ARG_LENGTH = 32;

  @Builder
  public LogEvent(
      Level level,
      String loggerName,
      ThreadValue callerThread,
      @Nullable Object message,
      Object @Nullable [] arguments,
      @Nullable Throwable throwable,
      @Nullable StackFrameValue callerFrame) {
    this(
        level, loggerName, callerThread, Instant.now(), message, arguments, throwable, callerFrame);
  }

  private static CharSequence resolve(
      @Nullable final Object message, final Object @Nullable [] arguments) {
    String suppliedMessage = Objects.toString(supply(message));
    if (message == null || arguments == null || arguments.length == 0) {
      return suppliedMessage;
    }
    int messageLength = suppliedMessage.length();
    StringBuilder resolvedMessage = new StringBuilder(messageLength + INIT_ARG_LENGTH);
    int iMessage = 0;
    int iArguments = 0;
    while (iMessage < messageLength) {
      char character = suppliedMessage.charAt(iMessage);
      if (character == '{'
          && ((iMessage + 1) < messageLength && suppliedMessage.charAt(iMessage + 1) == '}')
          && iArguments < arguments.length) {
        resolvedMessage.append(supply(arguments[iArguments++]));
        iMessage += 2;
      } else {
        resolvedMessage.append(character);
        iMessage += 1;
      }
    }
    return resolvedMessage;
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

  /** Represents a value representing a call stack element. */
  public record StackFrameValue(
      String className, String methodName, int lineNumber, @Nullable String fileName) {
    /**
     * Creates a StackFrameValue instance from a StackTraceElement.
     *
     * @param stackFrame call stack element
     * @return log render-able value representing the call stack element
     */
    public static StackFrameValue from(StackWalker.StackFrame stackFrame) {
      return new StackFrameValue(
          stackFrame.getClassName(),
          stackFrame.getMethodName(),
          stackFrame.getLineNumber(),
          stackFrame.getFileName());
    }
  }

  /** Represents the value of a thread. */
  public record ThreadValue(String name, long id) {}
}
