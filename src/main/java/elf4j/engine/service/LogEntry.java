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

package elf4j.engine.service;

import elf4j.engine.NativeLogger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 *
 */
@Value
@Builder
public class LogEntry {
    private static final int ADDITIONAL_STRING_BUILDER_CAPACITY = 32;
    @NonNull NativeLogger nativeLogger;
    @EqualsAndHashCode.Exclude Instant timestamp = Instant.now();
    @Nullable Object message;
    @Nullable Object[] arguments;
    @Nullable Throwable exception;
    @Nullable StackTraceFrame callerFrame;
    @Nullable ThreadInformation callerThread;

    private static @NonNull CharSequence resolve(Object msg, Object[] arguments) {
        String message = Objects.toString(supply(msg), "");
        if (arguments == null || arguments.length == 0) {
            return message;
        }
        int messageLength = message.length();
        StringBuilder resolved = new StringBuilder(messageLength + ADDITIONAL_STRING_BUILDER_CAPACITY);
        int i = 0;
        int j = 0;
        while (i < messageLength) {
            char character = message.charAt(i);
            if (character == '{' && ((i + 1) < messageLength && message.charAt(i + 1) == '}') && j < arguments.length) {
                resolved.append(supply(arguments[j++]));
                i += 2;
            } else {
                resolved.append(character);
                i += 1;
            }
        }
        return resolved;
    }

    private static Object supply(Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    /**
     * @return the name of the application client class calling the logging method of this logger instance
     */
    public String getCallerClassName() {
        return callerFrame == null ? nativeLogger.getOwnerClassName() : callerFrame.getClassName();
    }

    /**
     * @return log message text with all placeholder arguments resolved and replaced by final values
     */
    public CharSequence getResolvedMessage() {
        return resolve(this.message, this.arguments);
    }

    /**
     *
     */
    @Value
    @Builder
    public static class StackTraceFrame {
        @NonNull String className;
        @NonNull String methodName;
        int lineNumber;
        @Nullable String fileName;
    }

    /**
     *
     */
    @Value
    @Builder
    public static class ThreadInformation {
        @NonNull String name;
        long id;
    }
}
