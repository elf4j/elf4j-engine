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

package elf4j.engine.logging.pattern.predefined;

import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.PatternElement;
import elf4j.engine.logging.pattern.PredefinedPatternElementType;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;

public record TimestampElement(DateTimeFormatter dateTimeFormatter, TimeZoneOption timeZoneOption)
    implements PatternElement {
  public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

  /**
   * @param patternElement text pattern element to convert
   * @return converted pattern element object
   */
  public static TimestampElement from(String patternElement) {
    Optional<List<String>> elementDisplayOption =
        PredefinedPatternElementType.getPatternElementDisplayOptions(patternElement);
    if (elementDisplayOption.isEmpty()) {
      return new TimestampElement(DEFAULT_DATE_TIME_FORMAT, TimeZoneOption.DEFAULT);
    }
    List<String> timestampOptions = elementDisplayOption.get();
    return new TimestampElement(
        DateTimeFormatter.ofPattern(timestampOptions.getFirst()),
        getTimeZoneOption(timestampOptions));
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
    if (o == null || getClass() != o.getClass()) return false;
    return ComparisonCopy.from(this).equals(ComparisonCopy.from((TimestampElement) o));
  }

  @Override
  public int hashCode() {
    return Objects.hash(ComparisonCopy.from(this));
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

  @Builder
  private record ComparisonCopy(String dateTimeFormatter, TimeZoneOption timeZoneOption) {
    public static ComparisonCopy from(TimestampElement timestampElement) {
      return ComparisonCopy.builder()
          .dateTimeFormatter(Objects.toString(timestampElement.dateTimeFormatter))
          .timeZoneOption(timestampElement.timeZoneOption)
          .build();
    }
  }
}
