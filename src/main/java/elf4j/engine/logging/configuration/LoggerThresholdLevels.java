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

package elf4j.engine.logging.configuration;

import static elf4j.engine.logging.configuration.ConfigurationProperties.LEVEL;
import static elf4j.engine.logging.configuration.ConfigurationProperties.LEVEL_NAME_DELIMITER;

import elf4j.Level;
import elf4j.Logger;
import elf4j.util.UtilLogger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Manages the threshold output levels for the named logger instances. It allows for overriding the
 * default threshold output level of the root or specific logger instances based on the provided
 * configuration properties.
 */
public record LoggerThresholdLevels(
    Map<String, Level> loggerMinimumThresholdLevels, List<String> sortedLoggerNameNameSpaces) {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static final String ROOT_LOGGER_NAME = "";
  private static final Level DEFAULT_THRESHOLD_OUTPUT_LEVEL = Level.TRACE;

  public LoggerThresholdLevels(Map<String, Level> loggerMinimumThresholdLevels) {
    this(
        loggerMinimumThresholdLevels,
        loggerMinimumThresholdLevels.keySet().stream()
            .sorted(new FullyQualifiedClassNameComparator())
            .collect(Collectors.toList()));
    LOGGER.info("Specified %s logger minimum threshold level(s) in %s"
        .formatted(loggerMinimumThresholdLevels.size(), this));
  }

  public static LoggerThresholdLevels from(ConfigurationProperties configurationProperties) {
    Map<String, Level> thresholdLevelsByNameSpace = new HashMap<>();
    Properties properties = configurationProperties.properties();
    getAsThresholdLevel(LEVEL, properties)
        .ifPresent(level -> thresholdLevelsByNameSpace.put(ROOT_LOGGER_NAME, level));
    thresholdLevelsByNameSpace.putAll(properties.stringPropertyNames().stream()
        .filter(name -> name.trim().startsWith(LEVEL + LEVEL_NAME_DELIMITER))
        .filter(name -> !properties.getProperty(name).isBlank())
        .collect(Collectors.toMap(
            name -> name.split(LEVEL_NAME_DELIMITER, 2)[1].strip(),
            name -> getAsThresholdLevel(name, properties)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Value of configured threshold level cannot be empty")))));
    return new LoggerThresholdLevels(thresholdLevelsByNameSpace);
  }

  private static Optional<Level> getAsThresholdLevel(String name, Properties properties) {
    String thresholdLevelValue = properties.getProperty(name);
    return thresholdLevelValue == null
        ? Optional.empty()
        : Optional.of(Level.valueOf(thresholdLevelValue.strip().toUpperCase()));
  }

  public Level getLoggerThresholdLevel(String loggerName) {
    return this.sortedLoggerNameNameSpaces.stream()
        .filter(loggerName::startsWith)
        .findFirst()
        .map(this.loggerMinimumThresholdLevels::get)
        .orElse(DEFAULT_THRESHOLD_OUTPUT_LEVEL);
  }

  static class FullyQualifiedClassNameComparator implements Comparator<String> {
    private static int getPackageLevels(String fqcn) {
      return fqcn.split("\\.").length;
    }

    static final Comparator<String> BY_LENGTH_REVERSED =
        Comparator.comparingInt(String::length).reversed();

    /**
     * Compares two class name spaces. More specific name space goes first.
     *
     * @param fqcn1 can be fqcn or just package name
     * @param fqcn2 can be fqcn or just package name
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     *     equal to, or greater than the second.
     * @implNote This comparator imposes ordering that is inconsistent with
     *     {@link String#equals(Object)}.
     */
    @Override
    public int compare(String fqcn1, String fqcn2) {
      int byPackageLevel = Comparator.comparingInt(
              FullyQualifiedClassNameComparator::getPackageLevels)
          .reversed()
          .compare(fqcn1, fqcn2);
      if (byPackageLevel != 0) return byPackageLevel;
      int byLength = BY_LENGTH_REVERSED.compare(fqcn1, fqcn2);
      if (byLength != 0) return byLength;
      return fqcn1.compareTo(fqcn2);
    }
  }
}
