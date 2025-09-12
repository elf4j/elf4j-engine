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

class TimestampElementTest {

  @ParameterizedTest
  @ValueSource(strings = {"timestamp", "timestamp:"})
  void fromCreatesDefaultTimestampElementWhenPatternIsEmpty(String patternElement) {
    TimestampElement element = TimestampElement.from(patternElement);
    assertEquals(TimestampElement.DEFAULT_DATE_TIME_FORMAT, element.dateTimeFormatter());
    assertEquals(TimestampElement.TimeZoneOption.DEFAULT, element.timeZoneOption());
  }

  @Test
  void fromCreatesTimestampElementWithCustomFormatAndDefaultTimeZone() {
    TimestampElement element = TimestampElement.from("timestamp:uuuu-MM-dd");
    var now = OffsetDateTime.now();
    assertEquals(
        DateTimeFormatter.ofPattern("uuuu-MM-dd").format(now),
        element.dateTimeFormatter().format(now));
    assertEquals(TimestampElement.TimeZoneOption.DEFAULT, element.timeZoneOption());
  }

  @Test
  void fromCreatesTimestampElementWithCustomFormatAndUTC() {
    TimestampElement element = TimestampElement.from("timestamp:uuuu-MM-dd HH:mm:ss.SSSXXX,UTC");
    var now = OffsetDateTime.now();
    assertEquals(
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSXXX").format(now),
        element.dateTimeFormatter().format(now));
    assertEquals(TimestampElement.TimeZoneOption.UTC, element.timeZoneOption());
  }

  @Test
  void fromThrowsExceptionForInvalidTimeZoneOption() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> TimestampElement.from("timestamp:uuuu-MM-dd,INVALID"));
    assertTrue(exception.getMessage().contains("Unknown time zone option"));
  }

  @Test
  void equalsReturnsTrueForEquivalentTimestampElements() {
    TimestampElement element1 = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampElement.TimeZoneOption.DEFAULT);
    TimestampElement element2 = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampElement.TimeZoneOption.DEFAULT);
    assertEquals(element1, element2);
  }

  @Test
  void equalsReturnsFalseForDifferentTimeZoneOptions() {
    TimestampElement element1 = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampElement.TimeZoneOption.DEFAULT);
    TimestampElement element2 = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampElement.TimeZoneOption.UTC);
    assertNotEquals(element1, element2);
  }

  @Test
  void renderFormatsLogEventTimestampCorrectlyWithDefaultTimeZone() {
    LogEvent logEvent = mock(LogEvent.class);
    var timestamp = Instant.now();
    when(logEvent.getTimestamp()).thenReturn(timestamp);

    TimestampElement element = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSSXXX"),
        TimestampElement.TimeZoneOption.DEFAULT);
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

    TimestampElement element = new TimestampElement(
        DateTimeFormatter.ofPattern("uuuu-MM-dd"), TimestampElement.TimeZoneOption.UTC);
    StringBuilder target = new StringBuilder();
    element.render(logEvent, target);

    assertEquals(
        timestamp.atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("uuuu-MM-dd")),
        target.toString());
  }
}
