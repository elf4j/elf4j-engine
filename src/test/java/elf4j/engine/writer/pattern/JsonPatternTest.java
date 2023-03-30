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

package elf4j.engine.writer.pattern;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.LogEntry;
import elf4j.engine.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class JsonPatternTest {
    @Mock LogService stubLogService;
    LogEntry mockLogEntry;
    String mockMessage = "testLogMessage {}";

    @BeforeEach
    void beforeEach() {
        mockLogEntry = LogEntry.builder()
                .nativeLogger(new NativeLogger("testLoggerName", Level.ERROR, stubLogService))
                .callerThread(LogEntry.ThreadInformation.builder()
                        .name(Thread.currentThread().getName())
                        .id(Thread.currentThread().getId())
                        .build())
                .callerFrame(LogEntry.StackTraceFrame.builder()
                        .className(JsonPattern.class.getName())
                        .methodName("testMethod")
                        .lineNumber(42)
                        .fileName("testFileName")
                        .build())
                .message(mockMessage)
                .arguments(new Object[] { "testArg1" })
                .exception(new Exception("testExceptionMessage"))
                .build();
    }

    @Nested
    class from {
        @Test
        void noPatternOptionDefaults() {
            JsonPattern jsonPattern = JsonPattern.from("json");

            assertFalse(jsonPattern.includeCallerThread());
            assertFalse(jsonPattern.includeCallerDetail());
        }

        @Test
        void includeCallerOption() {
            JsonPattern jsonPattern = JsonPattern.from("json:caller-detail");

            assertFalse(jsonPattern.includeCallerThread());
            assertTrue(jsonPattern.includeCallerDetail());
        }

        @Test
        void includeThreadOption() {
            JsonPattern jsonPattern = JsonPattern.from("json:caller-thread");

            assertTrue(jsonPattern.includeCallerThread());
            assertFalse(jsonPattern.includeCallerDetail());
        }

        @Test
        void includeCallerAndThreadOptions() {
            JsonPattern jsonPattern = JsonPattern.from("json:caller-thread,caller-detail");

            assertTrue(jsonPattern.includeCallerThread());
            assertTrue(jsonPattern.includeCallerDetail());
        }
    }

    @Nested
    class render {
        JsonPattern jsonPattern = JsonPattern.from("json");

        @Test
        void resolveMessage() {
            StringBuilder layout = new StringBuilder();

            jsonPattern.renderTo(mockLogEntry, layout);
            String rendered = layout.toString();

            assertFalse(rendered.contains("testLogMessage {}"));
            assertTrue(rendered.contains(mockLogEntry.getResolvedMessage()));
        }
    }
}