package elf4j.engine.logging.pattern.element;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import elf4j.engine.logging.LogEvent;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimestampPatternTest {

  @ParameterizedTest
  @ValueSource(strings = {"timestamp", "timestamp:"})
  void fromCreatesDefaultTimestampElementWhenPatternIsEmpty(String patternElement) {
    TimestampPattern element = TimestampPattern.from(patternElement);
    assertEquals(TimestampPattern.DEFAULT_DATE_TIME_FORMAT, element.dateTimeFormatter());
    assertEquals(TimestampPattern.TimeZoneOption.DEFAULT, element.timeZoneOption());
  }

  @Test
  void fromCreatesTimestampElementWithCustomFormatAndDefaultTimeZone() {
    TimestampPattern element = TimestampPattern.from("timestamp:uuuu-MM-dd");
    var now = OffsetDateTime.now();
    assertEquals(
        DateTimeFormatter.ofPattern("uuuu-MM-dd").format(now),
        element.dateTimeFormatter().format(now));
    assertEquals(TimestampPattern.TimeZoneOption.DEFAULT, element.timeZoneOption());
  }

  @Test
  void fromCreatesTimestampElementWithCustomFormatAndUTC() {
    TimestampPattern element = TimestampPattern.from("timestamp:uuuu-MM-dd HH:mm:ss.SSSXXX,UTC");
    var now = OffsetDateTime.now();
    assertEquals(
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSXXX").format(now),
        element.dateTimeFormatter().format(now));
    assertEquals(TimestampPattern.TimeZoneOption.UTC, element.timeZoneOption());
  }

  @Test
  void fromThrowsExceptionForInvalidTimeZoneOption() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> TimestampPattern.from("timestamp:uuuu-MM-dd,INVALID"));
    assertTrue(exception.getMessage().contains("Unknown time zone option"));
  }

  @Test
  void equalsReturnsTrueForEquivalentTimestampElements() {
    TimestampPattern element1 = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampPattern.TimeZoneOption.DEFAULT);
    TimestampPattern element2 = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampPattern.TimeZoneOption.DEFAULT);
    assertEquals(element1, element2);
  }

  @Test
  void equalsReturnsFalseForDifferentTimeZoneOptions() {
    TimestampPattern element1 = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampPattern.TimeZoneOption.DEFAULT);
    TimestampPattern element2 = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampPattern.TimeZoneOption.UTC);
    assertNotEquals(element1, element2);
  }

  @Test
  void renderFormatsLogEventTimestampCorrectlyWithDefaultTimeZone() {
    LogEvent logEvent = mock(LogEvent.class);
    var timestamp = Instant.now();
    when(logEvent.getTimestamp()).thenReturn(timestamp);

    TimestampPattern element = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSXXX"),
        TimestampPattern.TimeZoneOption.DEFAULT);
    StringBuilder target = new StringBuilder();
    element.render(logEvent, target);

    assertEquals(
        timestamp
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSXXX")),
        target.toString());
  }

  @Test
  void renderFormatsLogEventTimestampCorrectlyWithUTC() {
    LogEvent logEvent = mock(LogEvent.class);
    var timestamp = Instant.now();
    when(logEvent.getTimestamp()).thenReturn(timestamp);

    TimestampPattern element = new TimestampPattern(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampPattern.TimeZoneOption.UTC);
    StringBuilder target = new StringBuilder();
    element.render(logEvent, target);

    assertEquals(
        timestamp.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("uuuu-MM-dd")),
        target.toString());
  }
}
