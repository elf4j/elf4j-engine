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

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.PrettifyOutputStream;
import com.dslplatform.json.runtime.Settings;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.PatternElement;
import elf4j.engine.logging.pattern.PredefinedElementType;
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
import lombok.Builder;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

@Builder
public @Value class JsonElement implements PatternElement {
  private static final String CALLER_DETAIL = "caller-detail";
  private static final String CALLER_THREAD = "caller-thread";
  private static final String PRETTY = "pretty";
  private static final Set<String> DISPLAY_OPTIONS = Arrays.stream(
          new String[] {CALLER_THREAD, CALLER_DETAIL, PRETTY})
      .collect(Collectors.toSet());
  private static final DslJson<Object> DSL_JSON =
      new DslJson<>(Settings.basicSetup().skipDefaultValues(true).includeServiceLoader());
  private static final String UTF_8 = StandardCharsets.UTF_8.toString();
  private static final int JSON_BYTES_INIT_SIZE = 1024;

  boolean includeCallerThread;
  boolean includeCallerDetail;
  boolean prettyPrint;

  private JsonElement(
      boolean includeCallerThread, boolean includeCallerDetail, boolean prettyPrint) {
    this.includeCallerThread = includeCallerThread;
    this.includeCallerDetail = includeCallerDetail;
    this.prettyPrint = prettyPrint;
  }

  /**
   * @param patternElement to convert
   * @return converted patternElement object
   */
  public static JsonElement from(String patternElement) {
    if (!PredefinedElementType.JSON.matchesTypeOf(patternElement)) {
      throw new IllegalArgumentException(
          String.format("Unexpected predefined pattern element: %s", patternElement));
    }
    List<String> displayOptions = PredefinedElementType.getElementDisplayOptions(patternElement);
    if (displayOptions.isEmpty()) {
      return JsonElement.builder().build();
    }
    Set<String> options = Set.copyOf(displayOptions);
    if (!PredefinedElementType.alphaNumericOnly(DISPLAY_OPTIONS)
        .containsAll(PredefinedElementType.alphaNumericOnly(options))) {
      throw new IllegalArgumentException("Invalid JSON display option inside: " + options);
    }
    return JsonElement.builder()
        .includeCallerThread(options.contains(CALLER_THREAD))
        .includeCallerDetail(options.contains(CALLER_DETAIL))
        .prettyPrint(options.contains(PRETTY))
        .build();
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
      target.append(byteArrayOutputStream.toString(UTF_8));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Builder
  @CompiledJson
  record JsonLogEntry(
      OffsetDateTime timestamp,
      String level,
      LogEvent.@Nullable ThreadValue callerThread,
      @Nullable String loggerName,
      LogEvent.@Nullable StackFrameValue callerDetail,
      Map<String, String> context,
      String message,
      @Nullable String exception) {
    static JsonLogEntry from(LogEvent logEvent, JsonElement jsonPattern) {
      return JsonLogEntry.builder()
          .timestamp(OffsetDateTime.ofInstant(logEvent.getTimestamp(), ZoneId.systemDefault()))
          .loggerName(logEvent.getLoggerName())
          .level(logEvent.getLevel().name())
          .callerThread(jsonPattern.includeCallerThread ? logEvent.getCallerThread() : null)
          .callerDetail(
              jsonPattern.includeCallerDetail
                  ? Objects.requireNonNull(logEvent.getCallerFrame())
                  : null)
          .message(logEvent.getResolvedMessage().toString())
          .context(MDC.getCopyOfContextMap())
          .exception(
              logEvent.getThrowable() == null
                  ? null
                  : StackTraces.getTraceAsBuffer(logEvent.getThrowable()).toString())
          .build();
    }
  }
}
