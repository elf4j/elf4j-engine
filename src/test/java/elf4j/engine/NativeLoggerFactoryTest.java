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

import elf4j.Level;
import elf4j.engine.service.EventingLogService;
import elf4j.engine.service.LogService;
import elf4j.engine.service.LogServiceManager;
import elf4j.engine.service.configuration.LogServiceConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NativeLoggerFactoryTest {
    @AfterAll
    static void afterAll() {
        LogServiceManager.INSTANCE.refresh();
    }

    @Nested
    class customizedFactory {

        @Mock LogService mockLogService;
        NativeLoggerFactory nativeLoggerFactory;

        @BeforeEach
        void afterEach() {
            nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR, NativeLoggerFactory.class, mockLogService);
            LogServiceManager.INSTANCE.deregister(nativeLoggerFactory);
        }

        @Test
        void level() {
            assertEquals(Level.ERROR, nativeLoggerFactory.logger().getLevel());
        }

        @Test
        void name() {
            assertSame(this.getClass().getName(), nativeLoggerFactory.logger().getOwnerClassName());
        }

        @Test
        void service() {
            assertSame(mockLogService, nativeLoggerFactory.logger().getLogService());
        }

        @Nested
        class refresh {
            NativeLoggerFactory nativeLoggerFactory;

            @BeforeEach
            void beforeEach() {
                nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR,
                        NativeLoggerFactory.class,
                        new EventingLogService(LogServiceConfiguration.bySetting(new Properties())));
                LogServiceManager.INSTANCE.deregister(nativeLoggerFactory);
            }

            @Test
            void whenRefreshedBySetting() {
                Properties properties = new Properties();
                NativeLogger nativeLogger = nativeLoggerFactory.logger();
                LogService logService = nativeLogger.getLogService();

                nativeLoggerFactory.refresh(properties);

                assertNotSame(nativeLogger.getLogService(), logService);
            }

            @Test
            void whenRefreshedByLoading() {
                NativeLogger nativeLogger = nativeLoggerFactory.logger();
                LogService logService = nativeLogger.getLogService();

                nativeLoggerFactory.refresh();

                assertNotSame(nativeLogger.getLogService(), logService);
            }
        }
    }
}
