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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The StackTraces class provides utility methods for working with stack traces. It provides methods
 * for getting the caller of a specified class and for getting the stack trace of a throwable as a
 * string.
 */
public class StackTraces {
  // Private constructor to prevent instantiation of utility class
  private StackTraces() {}

  /**
   * Returns the earliest caller frame on any of the specified callee classes
   *
   * @param calleeClassNames whose caller is being searched for
   * @return the earliest caller frame on any the specified callee class
   */
  public static StackWalker.StackFrame earliestCallerOfAny(Set<String> calleeClassNames) {
    var stackFrames =
        StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(Stream::toList);
    for (var i = stackFrames.size() - 1; i >= 0; i--) {
      if (calleeClassNames.contains(stackFrames.get(i).getClassName())) {
        return stackFrames.get(i + 1);
      }
    }
    throw new NoSuchElementException(String.format(
        "No caller found: calleeClassNames='%s', stackFrames=%s", calleeClassNames, stackFrames));
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
