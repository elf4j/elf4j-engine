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
import elf4j.engine.service.LogService;
import elf4j.engine.service.LogServiceManager;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NativeLoggerFactoryTest {
    @Nested
    class customizedFactory {

        @Mock
        LogService logService;

        @Mock
        NativeLoggerFactory.LogServiceFactory logServiceFactory;

        NativeLoggerFactory sut;

        @BeforeEach
        void beforeEach() {
            sut = new NativeLoggerFactory(Level.ERROR, NativeLoggerFactory.class, logServiceFactory);
            LogServiceManager.INSTANCE.deregister(sut);
        }

        @Test
        void level() {
            assertEquals(Level.ERROR, sut.logger().getLevel());
        }

        @Test
        void name() {
            assertSame(this.getClass().getName(), sut.logger().getOwnerClassName());
        }

        @Test
        void service() {
            given(logServiceFactory.getLogService()).willReturn(logService);

            assertSame(logService, sut.logger().getLogService());
        }

        @Nested
        class refresh {
            NativeLoggerFactory sut;

            @Mock
            LogService logService;

            @BeforeEach
            void beforeEach() {
                sut = new NativeLoggerFactory(
                        Level.ERROR, NativeLoggerFactory.class, new MockLogServiceFactory(logService));
                LogServiceManager.INSTANCE.deregister(sut);
            }

            @Test
            void whenRefreshedBySetting() {
                Properties properties = new Properties();
                NativeLogger nativeLogger = sut.logger();
                LogService logService = nativeLogger.getLogService();

                sut.refresh(properties);

                assertNotSame(nativeLogger.getLogService(), logService);
            }

            @Test
            void whenRefreshedByLoading() {
                NativeLogger nativeLogger = sut.logger();
                LogService logService = nativeLogger.getLogService();

                sut.refresh();

                assertNotSame(nativeLogger.getLogService(), logService);
            }

            class MockLogServiceFactory implements NativeLoggerFactory.LogServiceFactory {
                LogService logService;

                private MockLogServiceFactory(LogService logService) {
                    this.logService = logService;
                }

                @Override
                public LogService getLogService() {
                    return logService;
                }

                @Override
                public void reload() {
                    logService = mock(LogService.class);
                }

                @Override
                public void reset(Properties properties) {
                    logService = mock(LogService.class);
                }
            }
        }
    }
}
