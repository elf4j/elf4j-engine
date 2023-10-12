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
import elf4j.engine.service.LogServiceManager;
import org.junit.jupiter.api.AfterAll;
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
    @Mock LogService logService;
    NativeLogger sut;

    @AfterAll
    static void afterAll() {
        LogServiceManager.INSTANCE.refresh();
    }

    @BeforeEach
    void beforeEach() {
        NativeLoggerFactory nativeLoggerFactory = new NativeLoggerFactory(TRACE, NativeLoggerTest.class, logService);
        sut = new NativeLogger(NativeLoggerTest.class.getName(), INFO, nativeLoggerFactory);
        LogServiceManager.INSTANCE.deregister(nativeLoggerFactory);
    }

    @Nested
    class atLevels {
        @Test
        void instanceForDifferentLevel() {
            NativeLogger info = (NativeLogger) sut.atInfo();
            NativeLogger warn = (NativeLogger) info.atWarn();

            assertNotSame(warn, info);
            assertEquals(info.getOwnerClassName(), warn.getOwnerClassName());
            assertEquals(WARN, warn.getLevel());
        }

        @Test
        void instanceForSameLevel() {
            NativeLogger trace = (NativeLogger) sut.atTrace();
            NativeLogger debug = (NativeLogger) sut.atDebug();
            NativeLogger info = (NativeLogger) sut.atInfo();
            NativeLogger warn = (NativeLogger) sut.atWarn();
            NativeLogger error = (NativeLogger) sut.atError();

            assertSame(trace, trace.atTrace());
            assertSame(debug, debug.atDebug());
            assertSame(info, info.atInfo());
            assertSame(warn, warn.atWarn());
            assertSame(error, error.atError());
        }
    }

    @Nested
    class isEnabled {
        @Test
        void delegateToService() {
            sut.isEnabled();

            then(logService).should().isEnabled(sut);
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
            sut.log(exception);

            then(logService).should().log(same(sut), same(NativeLogger.class), same(exception), isNull(), isNull());
        }

        @Test
        void exceptionWithMessage() {
            sut.log(exception, plainTextMessage);

            then(logService).should()
                    .log(same(sut), same(NativeLogger.class), same(exception), same(plainTextMessage), isNull());
        }

        @Test
        void exceptionWithMessageAndArgs() {
            sut.log(exception, textMessageWithArgHolders, args);

            then(logService).should()
                    .log(same(sut),
                            same(NativeLogger.class),
                            same(exception),
                            same(textMessageWithArgHolders),
                            same(args));
        }

        @Test
        void messageWithArguments() {
            sut.log(textMessageWithArgHolders, args);

            then(logService).should()
                    .log(same(sut), same(NativeLogger.class), isNull(), same(textMessageWithArgHolders), same(args));
        }

        @Test
        void plainText() {
            sut.log(plainTextMessage);

            then(logService).should()
                    .log(same(sut), same(NativeLogger.class), isNull(), same(plainTextMessage), isNull());
        }
    }
}
