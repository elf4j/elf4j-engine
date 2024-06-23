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

package elf4j.engine.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import elf4j.engine.service.writer.LogWriter;
import java.util.Objects;
import java.util.Properties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EventingNativeLoggerServiceTest {
  @AfterAll
  static void afterAll() {
    NativeLogServiceManager.INSTANCE.refresh();
  }

  @Nested
  class isEnabled {
    @Test
    void whenInvokingLog() {
      EventingNativeLoggerService logService =
          spy(new EventingNativeLoggerService(LogServiceConfiguration.bySetting(null)));
      NativeLogger stubLogger = mock(NativeLogger.class);

      logService.log(stubLogger, this.getClass(), null, null, null);

      then(logService).should().isEnabled(stubLogger);
    }
  }

  @Nested
  class log {

    @Test
    void callWriter() {
      NativeLoggerService nativeLoggerService =
          new EventingNativeLoggerService(LogServiceConfiguration.bySetting(new Properties()));
      LogWriter mockLogWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(nativeLoggerService, "logWriter", mockLogWriter);
      NativeLogger stubLogger = mock(NativeLogger.class);
      given(mockLogWriter.getThresholdOutputLevel()).willReturn(Level.INFO);
      given(stubLogger.getLevel()).willReturn(Level.INFO);

      nativeLoggerService.log(stubLogger, this.getClass(), null, null, null);

      then(mockLogWriter).should().write(any(LogEvent.class));
    }

    @Test
    void whenCallerDetailRequired() {
      NativeLoggerService sut =
          new EventingNativeLoggerService(LogServiceConfiguration.bySetting(new Properties()));
      NativeLogger nativeLogger = mock(NativeLogger.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      given(logWriter.includeCallerDetail()).willReturn(true);
      given(nativeLogger.getLevel()).willReturn(Level.INFO);
      given(logWriter.getThresholdOutputLevel()).willReturn(Level.INFO);
      ArgumentCaptor<LogEvent> logEvent = ArgumentCaptor.forClass(LogEvent.class);

      sut.log(nativeLogger, this.getClass(), null, null, null);

      then(logWriter).should().write(logEvent.capture());
      assertEquals(
          Thread.currentThread().getName(),
          Objects.requireNonNull(logEvent.getValue().getCallerThread()).getName());
      assertEquals(
          Thread.currentThread().getId(), logEvent.getValue().getCallerThread().getId());
      assertNotNull(logEvent.getValue().getCallerFrame());
    }

    @Test
    void whenCallerDetailNotRequired() {
      NativeLoggerService sut =
          new EventingNativeLoggerService(LogServiceConfiguration.bySetting(new Properties()));
      NativeLogger nativeLogger = mock(NativeLogger.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      given(logWriter.includeCallerDetail()).willReturn(false);
      given(nativeLogger.getLevel()).willReturn(Level.INFO);
      given(logWriter.getThresholdOutputLevel()).willReturn(Level.INFO);
      ArgumentCaptor<LogEvent> logEvent = ArgumentCaptor.forClass(LogEvent.class);

      sut.log(nativeLogger, this.getClass(), null, null, null);

      then(logWriter).should().write(logEvent.capture());
      assertEquals(
          Thread.currentThread().getName(),
          Objects.requireNonNull(logEvent.getValue().getCallerThread()).getName());
      assertEquals(
          Thread.currentThread().getId(), logEvent.getValue().getCallerThread().getId());
      assertNull(logEvent.getValue().getCallerFrame());
    }

    @Test
    void onlyLogWhenEnabled() {
      NativeLoggerService sut =
          new EventingNativeLoggerService(LogServiceConfiguration.bySetting(new Properties()));
      NativeLogger nativeLogger = mock(NativeLogger.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      Level loggerLevel = Level.TRACE;
      given(nativeLogger.getLevel()).willReturn(loggerLevel);
      Level writerLevel = Level.INFO;
      given(logWriter.getThresholdOutputLevel()).willReturn(writerLevel);

      sut.log(nativeLogger, this.getClass(), null, null, null);

      assert loggerLevel.compareTo(writerLevel) < 0;
      then(logWriter).should(never()).write(any(LogEvent.class));
    }
  }
}
