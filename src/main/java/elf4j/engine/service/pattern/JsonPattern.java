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

package elf4j.engine.service.pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import elf4j.engine.service.LogEntry;
import elf4j.engine.service.util.StackTraceUtils;
import lombok.Builder;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@Value
@Builder
public class JsonPattern implements LogPattern {
    private static final String CALLER_DETAIL = "caller-detail";
    private static final String CALLER_THREAD = "caller-thread";
    private static final String PRETTY = "pretty";
    private static final Set<String> DISPLAY_OPTIONS =
            Arrays.stream(new String[] { CALLER_THREAD, CALLER_DETAIL, PRETTY }).collect(Collectors.toSet());
    boolean includeCallerThread;
    boolean includeCallerDetail;
    @ToString.Exclude Gson gson;

    /**
     * @param patternSegment
     *         to convert
     * @return converted patternSegment object
     */
    public static JsonPattern from(@NonNull String patternSegment) {
        if (!PatternType.JSON.isTargetTypeOf(patternSegment)) {
            throw new IllegalArgumentException("patternSegment: " + patternSegment);
        }
        Optional<String> displayOption = PatternType.getPatternDisplayOption(patternSegment);
        if (!displayOption.isPresent()) {
            return JsonPattern.builder().includeCallerThread(false).includeCallerDetail(false).gson(new Gson()).build();
        }
        Set<String> options =
                Arrays.stream(displayOption.get().split(",")).map(String::trim).collect(Collectors.toSet());
        if (!DISPLAY_OPTIONS.containsAll(options)) {
            throw new IllegalArgumentException("Invalid JSON display option inside: " + options);
        }
        return JsonPattern.builder()
                .includeCallerThread(options.contains(CALLER_THREAD))
                .includeCallerDetail(options.contains(CALLER_DETAIL))
                .gson(options.contains(PRETTY) ? new GsonBuilder().setPrettyPrinting().create() : new Gson())
                .build();
    }

    @Override
    public boolean includeCallerDetail() {
        return this.includeCallerDetail;
    }

    @Override
    public boolean includeCallerThread() {
        return this.includeCallerThread;
    }

    @Override
    public void renderTo(LogEntry logEntry, StringBuilder target) {
        gson.toJson(JsonLogEntry.from(logEntry, this), target);
    }

    @Value
    @Builder
    static class JsonLogEntry {
        static final DateTimeFormatter DATE_TIME_FORMATTER =
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());
        CharSequence timestamp;
        String level;
        LogEntry.ThreadValue callerThread;
        String callerClass;
        LogEntry.StackFrameValue callerDetail;
        CharSequence message;
        CharSequence exception;

        static JsonLogEntry from(@NonNull LogEntry logEntry, @NonNull JsonPattern jsonPattern) {
            StringBuilder timestamp = new StringBuilder(35);
            DATE_TIME_FORMATTER.formatTo(logEntry.getTimestamp(), timestamp);
            return JsonLogEntry.builder()
                    .timestamp(timestamp)
                    .callerClass(jsonPattern.includeCallerDetail ? null : logEntry.getCallerClassName())
                    .level(logEntry.getNativeLogger().getLevel().name())
                    .callerThread(jsonPattern.includeCallerThread ? logEntry.getCallerThread() : null)
                    .callerDetail(jsonPattern.includeCallerDetail ? logEntry.getCallerDetail() : null)
                    .message(logEntry.getResolvedMessage())
                    .exception(logEntry.getException() == null ? null :
                            StackTraceUtils.getTraceAsBuffer(logEntry.getException()))
                    .build();
        }
    }
}
