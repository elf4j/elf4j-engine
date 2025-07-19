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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.logging.config.ConfigurationProperties;
import elf4j.engine.logging.writer.LogWriter;
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
class EventingLogHandlerTest {
  @AfterAll
  static void afterAll() {
    NativeLogServiceManager.INSTANCE.restart();
  }

  @Nested
  class isEnabled {
    @Test
    void whenInvokingLog() {
      EventingLogHandler logService =
          spy(new EventingLogHandler(ConfigurationProperties.bySetting(null), NativeLogger.class));
      String loggerName = this.getClass().getName();

      logService.log(Level.INFO, loggerName, null, null, null);

      then(logService).should().isEnabled(Level.INFO, loggerName);
    }
  }

  @Nested
  class log {

    @Test
    void callWriter() {
      LogHandler logHandler = new EventingLogHandler(
          ConfigurationProperties.bySetting(new Properties()), NativeLogger.class);
      LogWriter mockLogWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(logHandler, "logWriter", mockLogWriter);
      given(mockLogWriter.getMinimumThresholdLevel()).willReturn(Level.INFO);

      logHandler.log(Level.INFO, this.getClass().getName(), null, null, null);

      then(mockLogWriter).should().write(any(LogEvent.class));
    }

    @Test
    void whenCallerDetailRequired() {
      LogHandler sut = new EventingLogHandler(
          ConfigurationProperties.bySetting(new Properties()), EventingLogHandler.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      given(logWriter.includeCallerDetail()).willReturn(true);
      given(logWriter.getMinimumThresholdLevel()).willReturn(Level.INFO);
      ArgumentCaptor<LogEvent> logEvent = ArgumentCaptor.forClass(LogEvent.class);

      sut.log(Level.INFO, this.getClass().getName(), null, null, null);

      then(logWriter).should().write(logEvent.capture());
      assertEquals(
          Thread.currentThread().getName(),
          Objects.requireNonNull(logEvent.getValue().getCallerThread()).name());
      assertEquals(
          Thread.currentThread().threadId(),
          logEvent.getValue().getCallerThread().id());
      assertNotNull(logEvent.getValue().getCallerFrame());
    }

    @Test
    void whenCallerDetailNotRequired() {
      LogHandler sut = new EventingLogHandler(
          ConfigurationProperties.bySetting(new Properties()), NativeLogger.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      given(logWriter.includeCallerDetail()).willReturn(false);
      given(logWriter.getMinimumThresholdLevel()).willReturn(Level.INFO);
      ArgumentCaptor<LogEvent> logEvent = ArgumentCaptor.forClass(LogEvent.class);

      sut.log(Level.INFO, this.getClass().getName(), null, null, null);

      then(logWriter).should().write(logEvent.capture());
      assertEquals(
          Thread.currentThread().getName(),
          Objects.requireNonNull(logEvent.getValue().getCallerThread()).name());
      assertEquals(
          Thread.currentThread().threadId(),
          logEvent.getValue().getCallerThread().id());
      assertNull(logEvent.getValue().getCallerFrame());
    }

    @Test
    void onlyLogWhenEnabled() {
      LogHandler sut = new EventingLogHandler(
          ConfigurationProperties.bySetting(new Properties()), NativeLogger.class);
      LogWriter logWriter = mock(LogWriter.class);
      ReflectionTestUtils.setField(sut, "logWriter", logWriter);
      given(logWriter.getMinimumThresholdLevel()).willReturn(Level.INFO);

      sut.log(Level.TRACE, this.getClass().getName(), null, null, null);

      then(logWriter).should(never()).write(any(LogEvent.class));
    }
  }
}
