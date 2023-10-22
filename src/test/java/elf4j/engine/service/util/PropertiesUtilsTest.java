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

package elf4j.engine.service.util;

import elf4j.engine.service.configuration.LogServiceConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesUtilsTest {

    @Nested
    class getChildProperties {
        @Test
        void noSuchPrefix() {
            Properties properties = new Properties();

            assertTrue(LogServiceConfiguration.bySetting(properties)
                    .getChildProperties("writer1")
                    .isEmpty());

            properties.setProperty("writerA", "fooValue");

            assertTrue(LogServiceConfiguration.bySetting(properties)
                    .getChildProperties("writer1")
                    .isEmpty());
        }

        @Test
        void allChildren() {
            Properties properties = new Properties();
            properties.setProperty("writer1", "standard");
            properties.setProperty("writer1.level", "info");
            properties.setProperty("writer1.pattern", "{json}");

            Map<String, String> childProperties =
                    LogServiceConfiguration.bySetting(properties).getChildProperties("writer1");

            assertEquals(2, childProperties.size());
            assertEquals("info", childProperties.get("level"));
            assertEquals("{json}", childProperties.get("pattern"));
        }

        @Test
        void onlyChildren() {
            Properties properties = new Properties();
            properties.setProperty("writer", "standard.id.foo");
            properties.setProperty("writer1", "standard");
            properties.setProperty("writer1.level", "info");
            properties.setProperty("writer1.pattern", "{json}");

            Map<String, String> childProperties =
                    LogServiceConfiguration.bySetting(properties).getChildProperties("writer1");

            assertFalse(childProperties.containsKey("writer"));
            assertFalse(childProperties.containsKey("writer1"));
        }
    }

    @Nested
    class getPropertiesGroupOfType {
        @Test
        void nonExistingType() {
            Properties properties = new Properties();
            properties.setProperty("writer2", "standard");

            assertTrue(LogServiceConfiguration.bySetting(properties)
                    .getPropertiesGroupOfType("fileWriters")
                    .isEmpty());
        }

        @Test
        void existingButNoFurtherConfigurations() {
            Properties properties = new Properties();
            properties.setProperty("writer", "standard");
            properties.setProperty("writer2", "standard");

            List<Map<String, String>> declaredWriter =
                    LogServiceConfiguration.bySetting(properties).getPropertiesGroupOfType("standard");

            assertEquals(2, declaredWriter.size());
            assertTrue(declaredWriter.get(0).isEmpty());
            assertTrue(declaredWriter.get(1).isEmpty());
        }
    }
}
