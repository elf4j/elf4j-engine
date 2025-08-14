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

package elf4j.engine.logging.pattern.element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import elf4j.Level;
import elf4j.engine.logging.LogEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerbatimElementTest {

  @Test
  void fromCreatesVerbatimElement() {
    String text = "Hello World";

    VerbatimElement element = VerbatimElement.from(text);

    assertEquals(text, element.getText());
  }

  @Test
  void fromHandlesEmptyString() {
    String text = "";

    VerbatimElement element = VerbatimElement.from(text);

    assertEquals(text, element.getText());
  }

  @Test
  void fromHandlesSpecialCharacters() {
    String text = "Special chars: {}[]()!@#$%^&*";

    VerbatimElement element = VerbatimElement.from(text);

    assertEquals(text, element.getText());
  }

  @Test
  void includeCallerDetailReturnsFalse() {
    VerbatimElement element = VerbatimElement.from("test");

    assertFalse(element.includeCallerDetail());
  }

  @Test
  void renderAppendsTextToTarget() {
    String text = "Test message";
    VerbatimElement element = VerbatimElement.from(text);
    StringBuilder target = new StringBuilder();
    LogEvent logEvent = createMockLogEvent();

    element.render(logEvent, target);

    assertEquals(text, target.toString());
  }

  @Test
  void renderAppendsToExistingContent() {
    String existingContent = "Existing: ";
    String text = "New content";
    VerbatimElement element = VerbatimElement.from(text);
    StringBuilder target = new StringBuilder(existingContent);
    LogEvent logEvent = createMockLogEvent();

    element.render(logEvent, target);

    assertEquals(existingContent + text, target.toString());
  }

  private LogEvent createMockLogEvent() {
    return LogEvent.builder()
        .loggerName("test.Logger")
        .level(Level.INFO)
        .callerThread(new LogEvent.ThreadValue("main", 1L))
        .callerFrame(LogEvent.StackFrameValue.from(mock(StackWalker.StackFrame.class)))
        .message("test message")
        .build();
  }

  @Test
  void fromTreatsOtherElementTypeAsVerbatim() {
    String text = "timestamp:test-timestamp-pattern";

    VerbatimElement element = VerbatimElement.from(text);

    assertEquals(text, element.getText());
  }
}
