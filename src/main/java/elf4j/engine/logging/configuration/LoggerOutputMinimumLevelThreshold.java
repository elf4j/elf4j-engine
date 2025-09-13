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

import elf4j.Level;
import elf4j.Logger;
import elf4j.util.UtilLogger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Manages the threshold output levels for the named logger instances. It allows for overriding the
 * default threshold output level of the root or specific logger instances based on the provided
 * configuration properties.
 */
public record LoggerOutputMinimumLevelThreshold(
    Map<String, Level> loggerMinimumThresholdLevels, List<String> sortedLoggerNameNameSpaces) {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static final String ROOT_LOGGER_NAME = "";
  private static final Level DEFAULT_THRESHOLD_OUTPUT_LEVEL = Level.TRACE;

  public LoggerOutputMinimumLevelThreshold(Map<String, Level> loggerMinimumThresholdLevels) {
    this(
        loggerMinimumThresholdLevels,
        loggerMinimumThresholdLevels.keySet().stream()
            .sorted(new FullyQualifiedClassNameComparator())
            .collect(Collectors.toList()));
    LOGGER.info(
        "%s overriding caller level(s) in %s".formatted(loggerMinimumThresholdLevels.size(), this));
  }

  public static LoggerOutputMinimumLevelThreshold from(
      ConfigurationProperties configurationProperties) {
    Map<String, Level> configuredLevels = new HashMap<>();
    Properties properties = configurationProperties.properties();
    getAsLevel("level", properties)
        .ifPresent(level -> configuredLevels.put(ROOT_LOGGER_NAME, level));
    configuredLevels.putAll(properties.stringPropertyNames().stream()
        .filter(name -> name.trim().startsWith("level@"))
        .filter(name -> !properties.getProperty(name).isBlank())
        .collect(Collectors.toMap(
            name -> name.split("@", 2)[1].trim(),
            name -> getAsLevel(name, properties).orElseThrow(NoSuchElementException::new))));
    return new LoggerOutputMinimumLevelThreshold(configuredLevels);
  }

  private static Optional<Level> getAsLevel(String levelKey, Properties properties) {
    String levelValue = properties.getProperty(levelKey);
    return levelValue == null
        ? Optional.empty()
        : Optional.of(Level.valueOf(levelValue.trim().toUpperCase()));
  }

  public Level getMinimumThresholdLevel(String loggerName) {
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
