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

import elf4j.engine.service.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static elf4j.Level.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class NativeLoggerTest {
    @Mock LogService mockLogService;

    NativeLoggerFactory mockNativeLoggerFactory;

    NativeLogger nativeLogger;

    @BeforeEach
    void init() {
        mockNativeLoggerFactory = new NativeLoggerFactory(INFO, NativeLogger.class, mockLogService);
        nativeLogger = new NativeLogger(this.getClass().getName(), TRACE, mockNativeLoggerFactory);
    }

    @Nested
    class atLevels {
        @Test
        void instanceForDifferentLevel() {
            NativeLogger info = (NativeLogger) nativeLogger.atInfo();
            NativeLogger warn = (NativeLogger) info.atWarn();

            assertNotSame(warn, info);
            assertEquals(info.getOwnerClassName(), warn.getOwnerClassName());
            assertEquals(WARN, warn.getLevel());
        }

        @Test
        void instanceForSameLevel() {
            NativeLogger trace = (NativeLogger) nativeLogger.atTrace();
            NativeLogger debug = (NativeLogger) nativeLogger.atDebug();
            NativeLogger info = (NativeLogger) nativeLogger.atInfo();
            NativeLogger warn = (NativeLogger) nativeLogger.atWarn();
            NativeLogger error = (NativeLogger) nativeLogger.atError();

            assertSame(trace, trace.atTrace());
            assertSame(debug, debug.atDebug());
            assertSame(info, info.atInfo());
            assertSame(warn, warn.atWarn());
            assertSame(error, error.atError());
        }
    }

    @Nested
    class enabled {
        @Test
        void delegateToService() {
            nativeLogger.isEnabled();

            then(mockLogService).should().isEnabled(nativeLogger);
        }
    }

    @Nested
    class logDelegateToService {
        String plainTextMessage = "plainTextMessage";
        String textMessageWithArgHolders = "textMessage with 2 task holders of values {} and {}";

        Object[] args = new Object[] { "1stArgOfObjectType", (Supplier) () -> "2ndArgOfSupplierType" };

        Throwable exception = new Exception("Test exception message");

        @Test
        void exception() {
            nativeLogger.log(exception);

            then(mockLogService).should()
                    .log(same(nativeLogger), same(NativeLogger.class), same(exception), isNull(), isNull());
        }

        @Test
        void exceptionWithMessage() {
            nativeLogger.log(exception, plainTextMessage);

            then(mockLogService).should()
                    .log(same(nativeLogger),
                            same(NativeLogger.class),
                            same(exception),
                            same(plainTextMessage),
                            isNull());
        }

        @Test
        void exceptionWithMessageAndArgs() {
            nativeLogger.log(exception, textMessageWithArgHolders, args);

            then(mockLogService).should()
                    .log(same(nativeLogger),
                            same(NativeLogger.class),
                            same(exception),
                            same(textMessageWithArgHolders),
                            same(args));
        }

        @Test
        void messageWithArguments() {
            nativeLogger.log(textMessageWithArgHolders, args);

            then(mockLogService).should()
                    .log(same(nativeLogger),
                            same(NativeLogger.class),
                            isNull(),
                            same(textMessageWithArgHolders),
                            same(args));
        }

        @Test
        void plainText() {
            nativeLogger.log(plainTextMessage);

            then(mockLogService).should()
                    .log(same(nativeLogger), same(NativeLogger.class), isNull(), same(plainTextMessage), isNull());
        }
    }
}