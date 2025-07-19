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

package elf4j.engine.logging.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

@ToString(doNotUseGetters = true)
@EqualsAndHashCode
public final class ConfigurationProperties {
  private static final Logger LOGGER = Logger.getLogger(ConfigurationProperties.class.getName());

  @Nullable private final Properties properties;

  private ConfigurationProperties(@Nullable Properties properties) {
    this.properties = properties;
  }

  /**
   * By loading log service configuration.
   *
   * @return the log service configuration
   */
  public static ConfigurationProperties byLoading() {
    LOGGER.info("Configuring by loading properties");
    return new ConfigurationProperties(new PropertiesFileLoader().load());
  }

  /**
   * By setting log service configuration.
   *
   * @param properties the properties
   * @return the log service configuration
   */
  public static ConfigurationProperties bySetting(@Nullable Properties properties) {
    LOGGER.info("Configuring by setting properties: %s".formatted(properties));
    return new ConfigurationProperties(properties);
  }

  public boolean isAbsent() {
    return properties == null;
  }

  /**
   * Takes only digits from the value to form a sequence, and tries to parse the sequence as an
   * {@link Integer}
   *
   * @param name full key in properties
   * @return Integer value of the specified name in the given properties, null if named entry
   *     missing or the corresponding value contains no digit
   */
  public @Nullable Integer getAsInteger(String name) {
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
   * Gets properties.
   *
   * @return the properties
   */
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
          LOGGER.warning("No configuration file located!");
          return null;
        }
      } else {
        propertiesInputStream = getClass().getResourceAsStream(customPropertiesLocation);
        if (propertiesInputStream == null) {
          throw new IllegalArgumentException(
              "Null resource stream from specified properties location: "
                  + customPropertiesLocation);
        }
      }
      try {
        properties.load(propertiesInputStream);
      } catch (IOException e) {
        throw new UncheckedIOException(
            "Error loading properties stream from location: "
                + (customPropertiesLocation == null
                    ? "default location"
                    : customPropertiesLocation),
            e);
      }
      LOGGER.info("Loaded properties: %s".formatted(properties));
      return properties;
    }

    private @Nullable InputStream fromDefaultPropertiesLocation() {
      return Arrays.stream(DEFAULT_PROPERTIES_LOCATIONS)
          .map(location -> getClass().getResourceAsStream(location))
          .filter(Objects::nonNull)
          .findFirst()
          .orElse(null);
    }
  }
}
