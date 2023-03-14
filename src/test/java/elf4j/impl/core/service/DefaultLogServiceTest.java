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

package elf4j.impl.core.service;

import elf4j.Level;
import elf4j.impl.core.NativeLogger;
import elf4j.impl.core.configuration.ServiceConfiguration;
import elf4j.impl.core.writer.LogWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class DefaultLogServiceTest {
    @Mock ServiceConfiguration mockServiceConfiguration;

    @Mock WriterThreadProvider mockWriterThreadProvider;
    NativeLogger nativeLogger;
    DefaultLogService logService;

    static class StubSyncExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }

    @Nested
    class isEnabled {
        @BeforeEach
        void init() {
            logService = new DefaultLogService(mockServiceConfiguration, mockWriterThreadProvider);
            nativeLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
        }

        @Test
        void delegateToConfiguration() {
            logService.isEnabled(nativeLogger);

            then(mockServiceConfiguration).should().isEnabled(nativeLogger);
        }
    }

    @Nested
    class log {
        @Mock LogWriter mockLogWriter;
        @Captor ArgumentCaptor<LogEntry> captorLogEntry;
        @Mock private ExecutorService mockExecutorService;

        @BeforeEach
        void init() {
            logService = new DefaultLogService(mockServiceConfiguration, mockWriterThreadProvider);
            nativeLogger = new NativeLogger(this.getClass().getName(), Level.TRACE, logService);
        }

        @Test
        void async() {
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockWriterThreadProvider.getWriterThread()).willReturn(mockExecutorService);

            logService.log(nativeLogger, this.getClass(), null, null, null);

            then(mockExecutorService).should().execute(any(Runnable.class));
        }

        @Test
        void callThreadInfoEager() {
            given(mockLogWriter.includeCallerThread()).willReturn(true);
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockWriterThreadProvider.getWriterThread()).willReturn(new StubSyncExecutor());

            logService.log(nativeLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertEquals(Thread.currentThread().getName(), captorLogEntry.getValue().getCallerThread().getName());
            assertEquals(Thread.currentThread().getId(), captorLogEntry.getValue().getCallerThread().getId());
        }

        @Test
        void callThreadInfoLazy() {
            given(mockLogWriter.includeCallerThread()).willReturn(false);
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockWriterThreadProvider.getWriterThread()).willReturn(new StubSyncExecutor());

            logService.log(nativeLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertNull(captorLogEntry.getValue().getCallerThread());
        }

        @Test
        void exceptionIndicatesAttemptOfCallFrame() {
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockLogWriter.includeCallerDetail()).willReturn(true);
            given(mockWriterThreadProvider.getWriterThread()).willReturn(new StubSyncExecutor());

            logService.log(nativeLogger, this.getClass(), null, null, null);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            assertNotNull(captorLogEntry.getValue().getCallerFrame());
        }

        @Test
        void noCallFrameRequired() {
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(true);
            given(mockLogWriter.includeCallerDetail()).willReturn(false);
            given(mockServiceConfiguration.getLogServiceWriter()).willReturn(mockLogWriter);
            given(mockWriterThreadProvider.getWriterThread()).willReturn(new StubSyncExecutor());
            Exception exception = new Exception();
            String message = "test message {}";
            Object[] args = { "test arg" };

            logService.log(nativeLogger, this.getClass(), exception, message, args);

            then(mockLogWriter).should().write(captorLogEntry.capture());
            LogEntry logEntry = captorLogEntry.getValue();
            assertNull(logEntry.getCallerFrame());
            assertSame(exception, logEntry.getException());
            assertSame(message, logEntry.getMessage());
            assertSame(args, logEntry.getArguments());
        }

        @Test
        void onlyLogWhenEnabled() {
            given(mockServiceConfiguration.isEnabled(any(NativeLogger.class))).willReturn(false);

            logService.log(nativeLogger, this.getClass(), null, null, null);

            then(mockServiceConfiguration).should(never()).getLogServiceWriter();
        }
    }
}