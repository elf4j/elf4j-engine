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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * Manages the threshold output levels for the named logger instances. It allows for overriding the
 * default threshold output level of the root or specific logger instances based on the provided
 * configuration properties.
 */
@Value
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class LoggerOutputMinimumLevelThreshold {
  private static final Logger LOGGER = UtilLogger.INFO;
  private static final String CONFIGURED_ROOT_LOGGER_NAME_SPACE = "";
  private static final Level DEFAULT_THRESHOLD_OUTPUT_LEVEL = Level.TRACE;

  @EqualsAndHashCode.Include
  Map<String, Level> loggerMinimumThresholdLevels;

  List<String> sortedLoggerNameNameSpaces;

  /**
   * Constructor for the LoggerOutputMinimumLevelThreshold class.
   *
   * @param loggerMinimumThresholdLevels a map of configured levels
   */
  private LoggerOutputMinimumLevelThreshold(Map<String, Level> loggerMinimumThresholdLevels) {
    this.loggerMinimumThresholdLevels = new ConcurrentHashMap<>(loggerMinimumThresholdLevels);
    this.sortedLoggerNameNameSpaces = loggerMinimumThresholdLevels.keySet().stream()
        .sorted(new ByClassNameSpace())
        .collect(Collectors.toList());
    LOGGER.info(
        "%s overriding caller level(s) in %s".formatted(loggerMinimumThresholdLevels.size(), this));
  }

  /**
   * Creates a new LoggerOutputMinimumLevelThreshold instance from a given configuration.
   *
   * @param configurationProperties configuration source of all threshold output levels for caller
   *     classes
   * @return the overriding caller levels
   */
  public static LoggerOutputMinimumLevelThreshold from(
      ConfigurationProperties configurationProperties) {
    Map<String, Level> configuredLevels = new HashMap<>();
    Properties properties = configurationProperties.getProperties();
    getAsLevel("level", properties)
        .ifPresent(level -> configuredLevels.put(CONFIGURED_ROOT_LOGGER_NAME_SPACE, level));
    configuredLevels.putAll(properties.stringPropertyNames().stream()
        .filter(name -> name.trim().startsWith("level@"))
        .collect(Collectors.toMap(
            name -> name.split("@", 2)[1].trim(),
            name -> getAsLevel(name, properties).orElseThrow(NoSuchElementException::new))));
    return new LoggerOutputMinimumLevelThreshold(configuredLevels);
  }

  /**
   * Converts a given level key to a Level instance.
   *
   * @param levelKey the level key
   * @param properties the properties
   * @return an Optional containing the Level instance if the level key is valid, otherwise an empty
   *     Optional
   */
  private static Optional<Level> getAsLevel(String levelKey, Properties properties) {
    String levelValue = properties.getProperty(levelKey);
    return levelValue == null
        ? Optional.empty()
        : Optional.of(Level.valueOf(levelValue.trim().toUpperCase()));
  }

  /**
   * Returns the threshold output level for a given logger.
   *
   * @param loggerName to search for configured threshold output level
   * @return If the threshold level is configured for the specified logger name, return the
   *     configured level. Otherwise, if no threshold level configured, return the default threshold
   *     level.
   */
  public Level getMinimumThresholdLevel(String loggerName) {
    return this.sortedLoggerNameNameSpaces.stream()
        .filter(loggerName::startsWith)
        .findFirst()
        .map(this.loggerMinimumThresholdLevels::get)
        .orElse(DEFAULT_THRESHOLD_OUTPUT_LEVEL);
  }

  /**
   * The ByClassNameSpace class is a comparator for class name spaces. It sorts class name spaces by
   * the number of package levels and then by length and lexicographic order.
   */
  static class ByClassNameSpace implements Comparator<String> {
    /**
     * Returns the number of package levels in a given class name space.
     *
     * @param classNameSpace the class name space
     * @return the number of package levels
     */
    private static int getPackageLevels(String classNameSpace) {
      return classNameSpace.split("\\.").length;
    }

    /**
     * Compares two class name spaces. More specific name space goes first.
     *
     * <p>Note: this comparator imposes orderings that are inconsistent with equals.
     *
     * @param classNameSpace1 can be fqcn or just package name
     * @param classNameSpace2 can be fqcn or just package name
     * @return a negative integer, zero, or a positive integer as the first argument is less than,
     *     equal to, or greater than the second.
     */
    @Override
    public int compare(String classNameSpace1, String classNameSpace2) {
      int packageLevelDifference =
          getPackageLevels(classNameSpace2) - getPackageLevels(classNameSpace1);
      if (packageLevelDifference != 0) return packageLevelDifference;
      return Comparator.comparingInt(String::length)
          .reversed()
          .thenComparing(String::compareTo)
          .compare(classNameSpace1, classNameSpace2);
    }
  }
}
