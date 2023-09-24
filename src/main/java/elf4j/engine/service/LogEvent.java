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
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Source data to be rendered to a final log message
 */
@Value
@Builder
public class LogEvent {
    private static final int ADDITIONAL_STRING_BUILDER_CAPACITY = 32;
    @NonNull NativeLogger nativeLogger;
    @NonNull ThreadValue callerThread;
    @NonNull Instant timestamp = Instant.now();
    @Nullable Object message;
    @Nullable Object[] arguments;
    @Nullable Throwable throwable;
    @Nullable Class<?> serviceInterfaceClass;
    @Nullable StackTraceElement callerFrame;

    private static @NonNull CharSequence resolve(Object message, Object[] arguments) {
        String suppliedMessage = Objects.toString(supply(message), "");
        if (arguments == null || arguments.length == 0) {
            return suppliedMessage;
        }
        int messageLength = suppliedMessage.length();
        StringBuilder resolved = new StringBuilder(messageLength + ADDITIONAL_STRING_BUILDER_CAPACITY);
        int i = 0;
        int j = 0;
        while (i < messageLength) {
            char character = suppliedMessage.charAt(i);
            if (character == '{' && ((i + 1) < messageLength && suppliedMessage.charAt(i + 1) == '}')
                    && j < arguments.length) {
                resolved.append(supply(arguments[j++]));
                i += 2;
            } else {
                resolved.append(character);
                i += 1;
            }
        }
        return resolved;
    }

    private static @Nullable Object supply(@Nullable Object o) {
        return o instanceof Supplier<?> ? ((Supplier<?>) o).get() : o;
    }

    /**
     * @return the name of the application client class calling the logging method of this logger instance
     */
    public String getCallerClassName() {
        return callerFrame != null ? callerFrame.getClassName() : nativeLogger.getOwnerClassName();
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
    public static class StackFrameValue {
        @NonNull String className;
        @NonNull String methodName;
        int lineNumber;
        @Nullable String fileName;

        public static StackFrameValue from(StackTraceElement stackTraceElement) {
            return LogEvent.StackFrameValue.builder()
                    .fileName(stackTraceElement.getFileName())
                    .className(stackTraceElement.getClassName())
                    .methodName(stackTraceElement.getMethodName())
                    .lineNumber(stackTraceElement.getLineNumber())
                    .build();
        }
    }

    /**
     *
     */
    @Value
    public static class ThreadValue {
        @NonNull String name;
        long id;
    }
}
