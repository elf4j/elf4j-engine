package elf4j.engine.util;

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

            assertTrue(PropertiesUtils.getChildProperties("writer1", properties).isEmpty());

            properties.setProperty("writer", "fooValue");

            assertTrue(PropertiesUtils.getChildProperties("writer1", properties).isEmpty());
        }

        @Test
        void allChildren() {
            Properties properties = new Properties();
            properties.setProperty("writer1", "standard");
            properties.setProperty("writer1.level", "info");
            properties.setProperty("writer1.pattern", "{json}");

            Map<String, String> childProperties = PropertiesUtils.getChildProperties("writer1", properties);

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

            Map<String, String> childProperties = PropertiesUtils.getChildProperties("writer1", properties);

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

            assertTrue(PropertiesUtils.getPropertiesGroupOfType("fileWriters", properties).isEmpty());
        }

        @Test
        void existingButNoFurtherConfigurations() {
            Properties properties = new Properties();
            properties.setProperty("writer", "standard");
            properties.setProperty("writer2", "standard");

            List<Map<String, String>> declaredWriter = PropertiesUtils.getPropertiesGroupOfType("standard", properties);

            assertEquals(2, declaredWriter.size());
            assertTrue(declaredWriter.get(0).isEmpty());
            assertTrue(declaredWriter.get(1).isEmpty());
        }
    }
}