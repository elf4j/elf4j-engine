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

package elf4j.impl.core.configuration;

import elf4j.Level;
import elf4j.impl.core.NativeLogger;
import elf4j.impl.core.service.LogService;
import elf4j.impl.core.writer.LogWriter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DefaultServiceConfigurationTest {
    @Mock LevelRepository mockLevelRepository;
    @Mock WriterRepository mockWriterRepository;
    @Mock LogWriter stubLogWriter;
    @Mock LogService mockLogService;

    @Nested
    class isEnabled {
        @Test
        void cacheLoadFromReposOnlyOnce() {
            DefaultServiceConfiguration defaultLoggingConfiguration =
                    new DefaultServiceConfiguration(mockLevelRepository, mockWriterRepository);
            NativeLogger nativeLogger = new NativeLogger("test.owner.class.Name", Level.OFF, mockLogService);
            given(mockWriterRepository.getLogServiceWriter()).willReturn(stubLogWriter);
            given(stubLogWriter.getMinimumLevel()).willReturn(Level.TRACE);
            given(mockLevelRepository.getLoggerMinimumLevel(nativeLogger)).willReturn(Level.TRACE);

            defaultLoggingConfiguration.isEnabled(nativeLogger);

            then(mockWriterRepository).should().getLogServiceWriter();
            then(mockLevelRepository).should().getLoggerMinimumLevel(nativeLogger);

            defaultLoggingConfiguration.isEnabled(nativeLogger);

            then(mockWriterRepository).shouldHaveNoMoreInteractions();
            then(mockLevelRepository).shouldHaveNoMoreInteractions();
        }
    }
}