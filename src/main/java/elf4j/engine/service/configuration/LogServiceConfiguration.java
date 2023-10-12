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

import elf4j.util.IeLogger;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@ToString
@EqualsAndHashCode
public class LogServiceConfiguration {
    @Nullable private final Properties properties;

    /**
     *
     */
    private LogServiceConfiguration() {
        this(new PropertiesFileLoader().load());
    }

    private LogServiceConfiguration(@Nullable Properties properties) {
        this.properties = properties;
    }

    public static @NonNull LogServiceConfiguration byLoading() {
        return new LogServiceConfiguration();
    }

    public static @NonNull LogServiceConfiguration bySetting(Properties properties) {
        IeLogger.INFO.log("Setting configuration: {}", properties);
        return new LogServiceConfiguration(properties);
    }

    public boolean isMissing() {
        return properties == null;
    }

    /**
     * Takes only digits from the value to form a sequence, and tries to parse the sequence as an {@link Integer}
     *
     * @param name
     *         full key in properties
     * @return Integer value of the specified name in the given properties, null if named entry missing or the
     *         corresponding value contains no digit
     */
    @Nullable
    public Integer getAsInteger(String name) {
        if (properties == null) {
            return null;
        }
        String value = properties.getProperty(name);
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        return value.startsWith("-") ? -Integer.parseInt(digits) : Integer.parseInt(digits);
    }

    /**
     * @param name
     *         full key in properties
     * @param defaultValue
     *         the default value to return if the delegate method {@link #getAsInteger} returns null
     * @return result of the delegate method {@link #getAsInteger} or, if that is null, the specified defaultValue
     */
    public int getIntOrDefault(String name, int defaultValue) {
        Integer value = getAsInteger(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * @param prefix
     *         key prefix to search for
     * @return all properties entries whose original keys start with the specified prefix. The prefix is removed from
     *         the keys of the returned entries.
     */
    public Map<String, String> getChildProperties(String prefix) {
        if (properties == null) {
            return Collections.emptyMap();
        }
        final String start = prefix + '.';
        return properties.stringPropertyNames()
                .stream()
                .filter(name -> name.trim().startsWith(start))
                .collect(Collectors.toMap(name -> name.substring(start.length()).trim(),
                        name -> properties.getProperty(name).trim()));
    }

    /**
     * @param type
     *         the properties value whose keys are each used as a parent key prefix
     * @return a group whose every member is a set of properties entries having a common key prefix of the specified
     *         type
     */
    public List<Map<String, String>> getPropertiesGroupOfType(String type) {
        if (properties == null) {
            return Collections.emptyList();
        }
        return properties.stringPropertyNames()
                .stream()
                .filter(name -> properties.getProperty(name).trim().equals(type))
                .map(this::getChildProperties)
                .collect(Collectors.toList());
    }

    @Nullable
    public Properties getProperties() {
        return properties;
    }

    public boolean isTrue(String name) {
        return properties != null && Boolean.parseBoolean(properties.getProperty(name));
    }

    /**
     *
     */
    static class PropertiesFileLoader {
        /**
         *
         */
        static final String ELF4J_PROPERTIES_LOCATION = "elf4j.properties.location";
        private static final String[] DEFAULT_PROPERTIES_LOCATIONS =
                new String[] { "/elf4j-test.properties", "/elf4j.properties" };

        /**
         * @return configuration properties loaded from either the default or specified location
         */
        @Nullable
        public Properties load() {
            Properties properties = new Properties();
            InputStream propertiesInputStream;
            final String customPropertiesLocation = System.getProperty(ELF4J_PROPERTIES_LOCATION);
            if (customPropertiesLocation == null) {
                propertiesInputStream = fromDefaultPropertiesLocation();
                if (propertiesInputStream == null) {
                    IeLogger.WARN.log("No configuration file located");
                    return null;
                }
            } else {
                propertiesInputStream = getClass().getResourceAsStream(customPropertiesLocation);
                if (propertiesInputStream == null) {
                    throw new IllegalArgumentException(
                            "Null resource stream from specified properties location: " + customPropertiesLocation);
                }
            }
            try {
                properties.load(propertiesInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(
                        "Error loading properties stream from location: " + (customPropertiesLocation == null ?
                                "default location" : customPropertiesLocation), e);
            }
            IeLogger.INFO.log("Loaded configuration: {}", properties);
            return properties;
        }

        private InputStream fromDefaultPropertiesLocation() {
            return Arrays.stream(DEFAULT_PROPERTIES_LOCATIONS)
                    .map(location -> getClass().getResourceAsStream(location))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
    }
}
