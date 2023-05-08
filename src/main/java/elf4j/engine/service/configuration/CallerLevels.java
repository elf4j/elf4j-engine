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

package elf4j.engine.service.configuration;

import elf4j.Level;
import elf4j.engine.NativeLogger;
import elf4j.util.InternalLogger;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 *
 */
public class CallerLevels {
    private static final Level DEFAULT_CALLER_MINIMUM_OUTPUT_LEVEL = Level.TRACE;
    private static final String ROOT_CLASS_NAME_SPACE = "";
    private final Map<String, Level> configuredLevels;
    /**
     * Longest first
     */
    private final List<String> sortedCallerClassNameSpaces;

    private CallerLevels(@NonNull Map<String, Level> configuredLevels) {
        this.configuredLevels = new ConcurrentHashMap<>(configuredLevels);
        this.sortedCallerClassNameSpaces = configuredLevels.keySet()
                .stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());
    }

    /**
     * @param properties
     *         configuration source of all minimum output levels for caller classes
     */
    @NonNull
    static CallerLevels from(@NonNull Properties properties) {
        Map<String, Level> configuredLevels = new HashMap<>();
        getAsLevel("level", properties).ifPresent(level -> configuredLevels.put(ROOT_CLASS_NAME_SPACE, level));
        configuredLevels.putAll(properties.stringPropertyNames()
                .stream()
                .filter(name -> name.trim().startsWith("level@"))
                .collect(Collectors.toMap(name -> name.split("@", 2)[1].trim(),
                        name -> getAsLevel(name, properties).orElseThrow(NoSuchElementException::new))));
        InternalLogger.INSTANCE.log(Level.INFO,
                "Configured " + configuredLevels.size() + " caller output level(s): " + configuredLevels);
        return new CallerLevels(configuredLevels);
    }

    private static Optional<Level> getAsLevel(String levelKey, @NonNull Properties properties) {
        String levelValue = properties.getProperty(levelKey);
        return levelValue == null ? Optional.empty() : Optional.of(Level.valueOf(levelValue.trim().toUpperCase()));
    }

    /**
     * Assuming the owner and caller class of the specified logger is the same
     *
     * @param nativeLogger
     *         to search for configured minimum output level
     * @return configured min output level for the specified logger's caller/owner class, or the default level if not
     *         configured
     */
    public Level getCallerMinimumOutputLevel(NativeLogger nativeLogger) {
        return this.sortedCallerClassNameSpaces.stream()
                .filter(classNameSpace -> nativeLogger.getOwnerClassName().startsWith(classNameSpace))
                .findFirst()
                .map(this.configuredLevels::get)
                .orElse(DEFAULT_CALLER_MINIMUM_OUTPUT_LEVEL);
    }
}
