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

package elf4j.engine.logging.util;

import elf4j.Logger;
import elf4j.engine.NativeLogger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * The StackTraces class provides utility methods for working with stack traces. It provides methods
 * for getting the caller of a specified class and for getting the stack trace of a throwable as a
 * string.
 */
public class StackTraces {
  // Private constructor to prevent instantiation of utility class
  private StackTraces() {}

  /**
   * Returns the immediate caller frame of the specified callee class.
   *
   * @param calleeClassName whose caller is being searched for
   * @return immediate caller frame of the specified callee class
   */
  public static StackTraceElement callerFrameOf(String calleeClassName) {
    return getExternalCallerFrame(calleeClassName, new Throwable().getStackTrace());
  }

  /**
   * Returns the caller frame of the specified callee class in the given stack trace.
   *
   * @param calleeClassName whose caller is being searched for
   * @param stackTrace to walk in search for the caller
   * @return the caller frame in the stack trace
   */
  private static StackTraceElement getExternalCallerFrame(
      String calleeClassName, StackTraceElement[] stackTrace) {
    boolean calleeFrameFound = false;
    for (StackTraceElement currentFrame : stackTrace) {
      String currentFrameClassName = currentFrame.getClassName();
      if (!calleeFrameFound && currentFrameClassName.equals(calleeClassName)) {
        calleeFrameFound = true;
        continue;
      }
      if (calleeFrameFound
          && !currentFrameClassName.equals(calleeClassName)
          && !currentFrameClassName.equals(Logger.class.getName())
          && !currentFrameClassName.equals(NativeLogger.class.getName())) {
        return currentFrame;
      }
    }
    throw new NoSuchElementException(String.format(
        "External caller of '%s' not found in call stack %s",
        calleeClassName, Arrays.toString(stackTrace)));
  }

  /**
   * Returns the stack trace of the specified throwable as a string.
   *
   * @param throwable to extract stack trace text from
   * @return stack trace buffer as the specified throwable prints it
   */
  public static StringBuffer getTraceAsBuffer(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
      throwable.printStackTrace(printWriter);
      return stringWriter.getBuffer();
    }
  }
}
