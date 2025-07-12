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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import elf4j.Level;
import elf4j.engine.logging.LogHandler;
import elf4j.engine.logging.NativeLogServiceManager;
import java.util.Properties;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NativeLogServiceProviderTest {
  @Nested
  class customizedFactory {

    @Mock
    LogHandler logHandler;

    @Mock
    NativeLogServiceProvider.LogHandlerFactory logHandlerFactory;

    NativeLogServiceProvider sut;

    @BeforeEach
    void beforeEach() {
      sut = new NativeLogServiceProvider(
          Level.ERROR, NativeLogServiceProvider.class, logHandlerFactory);
      NativeLogServiceManager.INSTANCE.deregister(sut);
    }

    @Test
    void level() {
      assertEquals(Level.ERROR, sut.logger().getLevel());
    }

    @Test
    void name() {
      assertEquals(this.getClass().getName(), sut.logger().getLoggerName());
    }

    @Test
    void service() {
      given(logHandlerFactory.getLogHandler()).willReturn(logHandler);

      assertSame(logHandler, sut.logger().getLogHandler());
    }

    @Nested
    class refresh {
      NativeLogServiceProvider sut;

      @Mock
      LogHandler logHandler;

      @BeforeEach
      void beforeEach() {
        sut = new NativeLogServiceProvider(
            Level.ERROR, NativeLogServiceProvider.class, new MockLogHandlerFactory(logHandler));
        NativeLogServiceManager.INSTANCE.deregister(sut);
      }

      @Test
      void whenRefreshedBySetting() {
        Properties properties = new Properties();
        NativeLogger nativeLogger = sut.logger();
        LogHandler logHandler = nativeLogger.getLogHandler();

        sut.refresh(properties);

        assertNotSame(nativeLogger.getLogHandler(), logHandler);
      }

      @Test
      void whenRefreshedByLoading() {
        NativeLogger nativeLogger = sut.logger();
        LogHandler logHandler = nativeLogger.getLogHandler();

        sut.refresh();

        assertNotSame(nativeLogger.getLogHandler(), logHandler);
      }

      static class MockLogHandlerFactory implements NativeLogServiceProvider.LogHandlerFactory {
        LogHandler logHandler;

        private MockLogHandlerFactory(LogHandler logHandler) {
          this.logHandler = logHandler;
        }

        @Override
        public LogHandler getLogHandler() {
          return logHandler;
        }

        @Override
        public void reload() {
          logHandler = mock(LogHandler.class);
        }

        @Override
        public void reset(@Nullable Properties properties) {
          logHandler = mock(LogHandler.class);
        }
      }
    }
  }
}
