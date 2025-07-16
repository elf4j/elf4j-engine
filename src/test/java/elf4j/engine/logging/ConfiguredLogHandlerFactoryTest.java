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

import elf4j.engine.NativeLogger;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ConfiguredLogHandlerFactoryTest {
  @Test
  void service() {
    ConfiguredLogHandlerFactory sut = new ConfiguredLogHandlerFactory(NativeLogger.class);
    NativeLogServiceManager.INSTANCE.deregister(sut);
    assertNotNull(sut.getLogHandler());
  }

  @Nested
  class refresh {
    ConfiguredLogHandlerFactory sut;

    @BeforeEach
    void beforeEach() {
      sut = new ConfiguredLogHandlerFactory(NativeLogger.class);
      NativeLogServiceManager.INSTANCE.deregister(sut);
    }

    @Test
    void whenRefreshedBySetting() {
      LogHandler old = sut.getLogHandler();
      Properties properties = new Properties();

      sut.refresh(properties);

      LogHandler reset = sut.getLogHandler();
      assertNotSame(old, reset, "refresh() should always results in a new LogHandler instance");
      assertNotEquals(
          old,
          reset,
          "Should not be equal as the reset LogHandler using different configuration properties");
    }

    @Test
    void whenRefreshedByLoading() {
      LogHandler old = sut.getLogHandler();

      sut.refresh();

      LogHandler reloaded = sut.getLogHandler();
      assertNotSame(old, reloaded, "refresh() should always results in a new LogHandler instance");
      assertEquals(
          old,
          reloaded,
          "Should be equal as reloaded LogHandler using the same default configuration properties");
    }
  }
}
