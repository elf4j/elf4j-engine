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

package elf4j.engine.service.pattern;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.NativeLoggerFactory;
import elf4j.engine.service.LogEvent;
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
    @Mock NativeLoggerFactory mockNativeLoggerFactory;
    LogEvent mockLogEvent;
    String mockMessage = "testLogMessage {}";

    @BeforeEach
    void beforeEach() {
        mockLogEvent = LogEvent.builder()
                .nativeLogger(new NativeLogger("testLoggerName", Level.ERROR, mockNativeLoggerFactory))
                .callerThread(LogEvent.ThreadValue.builder()
                        .name(Thread.currentThread().getName())
                        .id(Thread.currentThread().getId())
                        .build())
                .callerStack(new Throwable().getStackTrace())
                .serviceInterfaceClass(this.getClass())
                .message(mockMessage)
                .arguments(new Object[] { "testArg1" })
                .throwable(new Exception("testExceptionMessage"))
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

            jsonPattern.render(mockLogEvent, layout);
            String rendered = layout.toString();

            assertFalse(rendered.contains("testLogMessage {}"));
            assertTrue(rendered.contains(mockLogEvent.getResolvedMessage()));
        }
    }
}