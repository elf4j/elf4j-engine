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

import elf4j.engine.service.writer.LogWriter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RefreshableLogServiceConfigurationTest {
    @Mock PropertiesLoader mockPropertiesLoader;

    @Nested
    class isEnabled {

        @Test
        void cacheLoadFromReposOnlyOnce() {
            given(mockPropertiesLoader.load()).willReturn(new Properties());
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration(mockPropertiesLoader);

            LogWriter logServiceWriter = refreshableLogServiceConfiguration.getLogServiceWriter();
            LogWriter logServiceWriter1 = refreshableLogServiceConfiguration.getLogServiceWriter();

            assertSame(logServiceWriter, logServiceWriter1);
            then(mockPropertiesLoader).should(times(1)).load();
        }
    }

    @Nested
    class refresh {

        @Test
        void reload() {
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration(mockPropertiesLoader);

            refreshableLogServiceConfiguration.refresh(null);

            then(mockPropertiesLoader).should(times(2)).load();
        }

        @Test
        void replace() {
            RefreshableLogServiceConfiguration refreshableLogServiceConfiguration =
                    new RefreshableLogServiceConfiguration(mockPropertiesLoader);

            refreshableLogServiceConfiguration.refresh(new Properties());

            then(mockPropertiesLoader).should(times(1)).load();
        }
    }
}