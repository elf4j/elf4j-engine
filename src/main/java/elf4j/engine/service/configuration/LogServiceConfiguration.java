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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;

/** The type Log service configuration. */
@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public class LogServiceConfiguration {
    @Nullable private final Properties properties;

    private LogServiceConfiguration(@Nullable Properties properties) {
        this.properties = properties;
    }

    /**
     * By loading log service configuration.
     *
     * @return the log service configuration
     */
    public static @NonNull LogServiceConfiguration byLoading() {
        IeLogger.INFO.log("Configuring by loading properties");
        return new LogServiceConfiguration(new PropertiesFileLoader().load());
    }

    /**
     * By setting log service configuration.
     *
     * @param properties the properties
     * @return the log service configuration
     */
    public static @NonNull LogServiceConfiguration bySetting(Properties properties) {
        IeLogger.INFO.log("Configuring by setting properties: {}", properties);
        return new LogServiceConfiguration(properties);
    }

    /**
     * Is absent boolean.
     *
     * @return the boolean
     */
    public boolean isAbsent() {
        return properties == null;
    }

    /**
     * Takes only digits from the value to form a sequence, and tries to parse the sequence as an {@link Integer}
     *
     * @param name full key in properties
     * @return Integer value of the specified name in the given properties, null if named entry missing or the
     *     corresponding value contains no digit
     */
    @Nullable public Integer getAsInteger(String name) {
        String value = getProperties().getProperty(name);
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        int i = Integer.parseInt(digits);
        return value.startsWith("-") ? -i : i;
    }

    /**
     * Gets int or default.
     *
     * @param name full key in properties
     * @param defaultValue the default value to return if the delegate method {@link #getAsInteger} returns null
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
     * Gets child properties.
     *
     * @param prefix key prefix to search for
     * @return all properties entries whose original keys start with the specified prefix. The prefix is removed from
     *     the keys of the returned map.
     * @see #getPropertiesGroupOfType(String)
     */
    public Map<String, String> getChildProperties(String prefix) {
        if (isAbsent()) {
            return Collections.emptyMap();
        }
        final String start = prefix + '.';
        return getProperties().stringPropertyNames().stream()
                .filter(name -> name.trim().startsWith(start))
                .collect(Collectors.toMap(
                        name -> name.substring(start.length()).trim(),
                        name -> getProperties().getProperty(name).trim()));
    }

    /**
     * Gets properties group of type.
     *
     * @param type the value whose keys are each used as a parent key prefix of a child properties map
     * @return a child properties map group of which every member is a properties map having a common parent key prefix
     *     of the specified type
     * @see #getChildProperties(String)
     */
    public List<Map<String, String>> getPropertiesGroupOfType(String type) {
        if (isAbsent()) {
            return Collections.emptyList();
        }
        return getProperties().stringPropertyNames().stream()
                .filter(name -> getProperties().getProperty(name).trim().equals(type))
                .map(this::getChildProperties)
                .collect(Collectors.toList());
    }

    /**
     * Gets properties.
     *
     * @return the properties
     */
    @Nonnull
    public Properties getProperties() {
        if (isAbsent()) {
            throw new IllegalStateException("No elf4j configuration present");
        }
        return Objects.requireNonNull(properties);
    }

    /**
     * Checks if a named property exists and has a true value.
     *
     * @param name the name to check
     * @return true only when the named property exists, and has a true value
     */
    public boolean isTrue(String name) {
        return Boolean.parseBoolean(getProperties().getProperty(name));
    }

    /** The type Properties file loader. */
    static class PropertiesFileLoader {
        /** */
        static final String ELF4J_PROPERTIES_LOCATION = "elf4j.properties.location";

        private static final String[] DEFAULT_PROPERTIES_LOCATIONS =
                new String[] {"/elf4j-test.properties", "/elf4j.properties"};

        /** @return configuration properties loaded from either the default or specified location */
        @Nullable public Properties load() {
            Properties properties = new Properties();
            InputStream propertiesInputStream;
            final String customPropertiesLocation = System.getProperty(ELF4J_PROPERTIES_LOCATION);
            if (customPropertiesLocation == null) {
                propertiesInputStream = fromDefaultPropertiesLocation();
                if (propertiesInputStream == null) {
                    IeLogger.WARN.log("No configuration file located!");
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
                        "Error loading properties stream from location: "
                                + (customPropertiesLocation == null ? "default location" : customPropertiesLocation),
                        e);
            }
            IeLogger.INFO.log("Loaded properties: {}", properties);
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
