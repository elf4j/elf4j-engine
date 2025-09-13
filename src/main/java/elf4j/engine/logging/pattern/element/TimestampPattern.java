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
import elf4j.engine.logging.pattern.RenderingPattern;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

record TimestampPattern(DateTimeFormatter dateTimeFormatter, TimeZoneOption timeZoneOption)
    implements RenderingPattern {
  static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * @param elementPattern text pattern element to convert
   * @return converted pattern element object
   */
  static TimestampPattern from(String elementPattern) {
    if (PatternElementType.TIMESTAMP != PatternElementType.from(elementPattern)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", elementPattern));
    }
    List<String> elementDisplayOption =
        ElementPatterns.getElementPatternDisplayOptions(elementPattern);
    if (elementDisplayOption.isEmpty()) {
      return new TimestampPattern(DEFAULT_DATE_TIME_FORMAT, TimeZoneOption.DEFAULT);
    }
    return new TimestampPattern(
        DateTimeFormatter.ofPattern(elementDisplayOption.getFirst()),
        getTimeZoneOption(elementDisplayOption));
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
    if (!(o
        instanceof
        TimestampPattern(
            DateTimeFormatter thatDateTimeFormatter,
            TimeZoneOption thatTimeZoneOption))) return false;
    OffsetDateTime now = OffsetDateTime.now();
    return timeZoneOption == thatTimeZoneOption
        && Objects.equals(dateTimeFormatter.format(now), thatDateTimeFormatter.format(now));
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeZoneOption);
  }

  @Override
  public boolean requiresCallerDetail() {
    return false;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    dateTimeFormatter.formatTo(
        logEvent
            .timestamp()
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
