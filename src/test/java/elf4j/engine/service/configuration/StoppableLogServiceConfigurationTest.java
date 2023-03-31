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

package elf4j.engine.service.configuration;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.engine.service.LogService;
import elf4j.engine.service.writer.LogWriter;
import elf4j.engine.service.writer.StandardStreamsWriter;
import elf4j.engine.service.writer.WriterGroup;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class StoppableLogServiceConfigurationTest {
    @Mock CallerLevelRepository mockCallerLevelRepository;
    @Mock WriterRepository mockWriterRepository;
    @Mock LogWriter stubLogWriter;
    @Mock LogService mockLogService;

    @Nested
    class isEnabled {
        @Test
        void cacheLoadFromReposOnlyOnce() {
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration(mockCallerLevelRepository, mockWriterRepository);
            NativeLogger nativeLogger = new NativeLogger("test.owner.class.Name", Level.OFF, mockLogService);
            given(mockWriterRepository.getLogServiceWriter()).willReturn(stubLogWriter);
            given(stubLogWriter.getMinimumOutputLevel()).willReturn(Level.TRACE);
            given(mockCallerLevelRepository.getMinimumOutputLevel(nativeLogger)).willReturn(Level.TRACE);

            refreshableLogServiceConfiguration.isEnabled(nativeLogger);

            assertSame(stubLogWriter, mockWriterRepository.getLogServiceWriter());
            then(mockCallerLevelRepository).should().getMinimumOutputLevel(nativeLogger);

            refreshableLogServiceConfiguration.isEnabled(nativeLogger);

            then(mockWriterRepository).shouldHaveNoMoreInteractions();
            then(mockCallerLevelRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class refresh {
        @Test
        void reload() {
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration();

            refreshableLogServiceConfiguration.refresh(null);

            assertTrue(refreshableLogServiceConfiguration.getLogServiceWriter() instanceof WriterGroup);
        }

        @Test
        void replace() {
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration();

            refreshableLogServiceConfiguration.refresh(new Properties());

            assertTrue(refreshableLogServiceConfiguration.getLogServiceWriter() instanceof StandardStreamsWriter);
        }
    }
}