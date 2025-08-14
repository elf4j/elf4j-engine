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

package elf4j.engine.logging.pattern.element;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.ElementType;
import elf4j.engine.logging.pattern.PatternElement;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Value;

public @Value @Builder(access = lombok.AccessLevel.PRIVATE) class TimestampElement
    implements PatternElement {
  public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

  DateTimeFormatter dateTimeFormatter;
  TimeZoneOption timeZoneOption;

  /**
   * @param patternElement text pattern element to convert
   * @return converted pattern element object
   */
  public static TimestampElement from(String patternElement) {
    if (ElementType.TIMESTAMP != ElementType.from(patternElement)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", patternElement));
    }
    List<String> elementDisplayOption = ElementType.getElementDisplayOptions(patternElement);
    if (elementDisplayOption.isEmpty()) {
      return TimestampElement.builder()
          .dateTimeFormatter(DEFAULT_DATE_TIME_FORMAT)
          .timeZoneOption(TimeZoneOption.DEFAULT)
          .build();
    }
    return TimestampElement.builder()
        .dateTimeFormatter(DateTimeFormatter.ofPattern(elementDisplayOption.getFirst()))
        .timeZoneOption(getTimeZoneOption(elementDisplayOption))
        .build();
  }

  private static TimeZoneOption getTimeZoneOption(List<String> formatOptions) {
    if (formatOptions.size() == 1) {
      return TimeZoneOption.DEFAULT;
    }
    return TimeZoneOption.from(formatOptions.get(1));
  }

  /**
   * {@link DateTimeFormatter} uses the same equals method as {@link Object#equals(Object)}. That
   * has the undesired effect that two formatter instances of exactly the same configurations are
   * considered not equal as they are two different instances. This method attempts to override that
   * behavior.
   *
   * @param o the reference object with which to compare.
   * @return true if timeZoneOption and dateTimeFormatter of the two instances are considered "the
   *     same"
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TimestampElement that)) return false;
    OffsetDateTime now = OffsetDateTime.now();
    return timeZoneOption == that.timeZoneOption
        && Objects.equals(dateTimeFormatter.format(now), that.dateTimeFormatter.format(now));
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeZoneOption);
  }

  @Override
  public boolean includeCallerDetail() {
    return false;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    dateTimeFormatter.formatTo(
        logEvent
            .getTimestamp()
            .atZone(timeZoneOption == TimeZoneOption.UTC ? ZoneOffset.UTC : ZoneId.systemDefault()),
        target);
  }

  enum TimeZoneOption {
    UTC,
    DEFAULT;

    public static TimeZoneOption from(String timeZoneOption) {
      return Arrays.stream(values())
          .filter(v -> v.name().equalsIgnoreCase(timeZoneOption))
          .findFirst()
          .orElseThrow(() ->
              new IllegalArgumentException("Unknown time zone option: %s. Valid options are: %s"
                  .formatted(timeZoneOption, Arrays.toString(TimeZoneOption.values()))));
    }
  }
}
