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

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 *
 */
public class FilePropertiesLoader implements PropertiesLoader {
    /**
     *
     */
    public static final String ELF4J_PROPERTIES_LOCATION = "elf4j.properties.location";
    private static final String[] DEFAULT_PROPERTIES_LOCATIONS =
            new String[] { "/elf4j-test.properties", "/elf4j.properties" };

    /**
     * @return configuration properties loaded from either the default or specified location
     */
    @Override
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
