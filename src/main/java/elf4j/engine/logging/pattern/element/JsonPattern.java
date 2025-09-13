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

import static elf4j.engine.logging.pattern.element.ElementPatternType.alphaNumericOnly;
import static elf4j.engine.logging.pattern.element.ElementPatternType.uniqueAlphaNumericOnly;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.PrettifyOutputStream;
import com.dslplatform.json.runtime.Settings;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.RenderingPattern;
import elf4j.engine.logging.util.StackTraces;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

public record JsonPattern(
    boolean includeCallerThread, boolean includeCallerDetail, boolean prettyPrint)
    implements RenderingPattern {
  private static final String CALLER_DETAIL = "caller-detail";
  private static final String CALLER_THREAD = "caller-thread";
  private static final String PRETTY = "pretty";
  private static final Set<String> DISPLAY_OPTIONS = Arrays.stream(
          new String[] {CALLER_THREAD, CALLER_DETAIL, PRETTY})
      .collect(Collectors.toUnmodifiableSet());
  private static final DslJson<Object> DSL_JSON =
      new DslJson<>(Settings.basicSetup().skipDefaultValues(true).includeServiceLoader());
  private static final int JSON_BYTES_INIT_SIZE = 1024;

  public static JsonPattern from(String elementPattern) {
    if (ElementPatternType.JSON != ElementPatternType.from(elementPattern)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", elementPattern));
    }
    List<String> displayOptions = ElementPatternType.getElementDisplayOptions(elementPattern);
    if (displayOptions.isEmpty()) {
      return new JsonPattern(false, false, false);
    }
    Set<String> uniqueOptions = uniqueAlphaNumericOnly(displayOptions);
    if (uniqueOptions.size() != displayOptions.size()) {
      throw new IllegalArgumentException("Duplicate JSON display option inside: " + displayOptions);
    }
    if (!uniqueAlphaNumericOnly(DISPLAY_OPTIONS).containsAll(uniqueOptions)) {
      throw new IllegalArgumentException("Invalid JSON display option inside: " + displayOptions);
    }
    return new JsonPattern(
        uniqueOptions.contains(alphaNumericOnly(CALLER_THREAD)),
        uniqueOptions.contains(alphaNumericOnly(CALLER_DETAIL)),
        uniqueOptions.contains(alphaNumericOnly(PRETTY)));
  }

  @Override
  public boolean includeCallerDetail() {
    return this.includeCallerDetail;
  }

  @Override
  public void render(LogEvent logEvent, StringBuilder target) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(JSON_BYTES_INIT_SIZE);
    try (OutputStream outputStream =
        prettyPrint ? new PrettifyOutputStream(byteArrayOutputStream) : byteArrayOutputStream) {
      DSL_JSON.serialize(JsonLogEntry.from(logEvent, this), outputStream);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    target.append(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
  }

  @CompiledJson
  public record JsonLogEntry(
      OffsetDateTime timestamp,
      String level,
      LogEvent.@Nullable ThreadValue callerThread,
      @Nullable String loggerName,
      LogEvent.@Nullable StackFrameValue callerDetail,
      Map<String, String> context,
      String message,
      @Nullable String exception) {

    static JsonLogEntry from(LogEvent logEvent, JsonPattern jsonPattern) {
      return new JsonLogEntry(
          OffsetDateTime.ofInstant(logEvent.getTimestamp(), ZoneId.systemDefault()),
          logEvent.getLevel().name(),
          jsonPattern.includeCallerThread ? logEvent.getCallerThread() : null,
          logEvent.getLoggerName(),
          jsonPattern.includeCallerDetail
              ? Objects.requireNonNull(logEvent.getCallerFrame())
              : null,
          MDC.getCopyOfContextMap(),
          logEvent.getResolvedMessage().toString(),
          logEvent.getThrowable() == null
              ? null
              : StackTraces.getTraceAsBuffer(logEvent.getThrowable()).toString());
    }
  }
}
