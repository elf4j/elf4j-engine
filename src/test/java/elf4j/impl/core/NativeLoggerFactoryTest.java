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

package elf4j.impl.core;

import elf4j.Level;
import elf4j.impl.core.configuration.LoggingConfiguration;
import elf4j.impl.core.service.DefaultLogService;
import elf4j.impl.core.service.WriterThreadProvider;
import elf4j.impl.core.util.StackTraceUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@ExtendWith(MockitoExtension.class)
class NativeLoggerFactoryTest {
    @Mock LoggingConfiguration mockLoggingConfiguration;
    @Mock WriterThreadProvider mockWriterThreadProvider;

    @Nested
    class customizedFactory {
        @Test
        void level() {
            Class<?> stubLoggerAccessInterface = NativeLoggerFactory.class;
            NativeLoggerFactory nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR,
                    stubLoggerAccessInterface,
                    mockLoggingConfiguration,
                    mockWriterThreadProvider);

            assertEquals(Level.ERROR, nativeLoggerFactory.logger().getLevel());
        }

        @Test
        void loggerClass() {
            Class<?> stubLoggerAccessInterface = NativeLoggerFactory.class;
            NativeLoggerFactory nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR,
                    stubLoggerAccessInterface,
                    mockLoggingConfiguration,
                    mockWriterThreadProvider);

            assertEquals(new DefaultLogService(mockLoggingConfiguration, mockWriterThreadProvider),
                    nativeLoggerFactory.logger().getLogService());
        }

        @Test
        void name() {
            Class<?> mockLoggerInterface = this.getClass();
            NativeLoggerFactory nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR,
                    mockLoggerInterface,
                    mockLoggingConfiguration,
                    mockWriterThreadProvider);

            NativeLogger logger = nativeLoggerFactory.logger();

            assertSame(StackTraceUtils.callerOf(mockLoggerInterface).getClassName(), logger.getName());
        }

        @Test
        void service() {
            Class<?> stubLoggerAccessInterface = NativeLoggerFactory.class;
            NativeLoggerFactory nativeLoggerFactory = new NativeLoggerFactory(Level.ERROR,
                    stubLoggerAccessInterface,
                    mockLoggingConfiguration,
                    mockWriterThreadProvider);

            assertEquals(new DefaultLogService(mockLoggingConfiguration, mockWriterThreadProvider),
                    nativeLoggerFactory.logger().getLogService());
        }
    }
}