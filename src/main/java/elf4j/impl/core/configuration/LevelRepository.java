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

package elf4j.impl.core.configuration;

import elf4j.Level;
import elf4j.impl.core.NativeLogger;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class LevelRepository {
    private static final Level DEFAULT_LOGGER_MINIMUM_LEVEL = Level.TRACE;
    private static final String ROOT_CLASS_NAME_SPACE = "";
    private final Map<String, Level> callerClassNameSpaceMinimumLevels;
    /**
     * Longest first
     */
    private final List<String> sortedCallerClassNameSpaces;

    private LevelRepository(@NonNull Map<String, Level> callerClassNameSpaceMinimumLevels) {
        this.callerClassNameSpaceMinimumLevels = callerClassNameSpaceMinimumLevels;
        this.sortedCallerClassNameSpaces = callerClassNameSpaceMinimumLevels.keySet()
                .stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());
    }

    /**
     * @param properties configuration source of all minimum output levels for loggers
     */
    @NonNull
    public static LevelRepository from(@NonNull Properties properties) {
        Map<String, Level> callerClassNameSpaceMinimumLevels = new HashMap<>();
        getAsLevel("level", properties).ifPresent(level -> callerClassNameSpaceMinimumLevels.put(ROOT_CLASS_NAME_SPACE,
                level));
        callerClassNameSpaceMinimumLevels.putAll(properties.stringPropertyNames()
                .stream()
                .filter(name -> name.trim().startsWith("level@"))
                .collect(Collectors.toMap(name -> name.split("@", 2)[1].trim(),
                        name -> getAsLevel(name, properties).orElseThrow(NoSuchElementException::new))));
        return new LevelRepository(callerClassNameSpaceMinimumLevels);
    }

    private static Optional<Level> getAsLevel(String levelKey, @NonNull Properties properties) {
        String levelValue = properties.getProperty(levelKey);
        return levelValue == null ? Optional.empty() : Optional.of(Level.valueOf(levelValue.trim().toUpperCase()));
    }

    /**
     * @param nativeLogger to search for configured minimum output level
     * @return configured min output level for the specified logger, or the default level if not configured
     */
    public Level getLoggerMinimumLevel(NativeLogger nativeLogger) {
        return this.sortedCallerClassNameSpaces.stream()
                .filter(classNameSpace -> nativeLogger.getOwnerClassName().startsWith(classNameSpace))
                .findFirst()
                .map(this.callerClassNameSpaceMinimumLevels::get)
                .orElse(DEFAULT_LOGGER_MINIMUM_LEVEL);
    }
}
