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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class LevelRepository {
    private static final Level DEFAULT_LOGGER_MINIMUM_LEVEL = Level.TRACE;
    final Map<String, Level> loggerMinimumLevels = new HashMap<>();

    /**
     * @param properties configuration source of all minimum output levels for loggers
     */
    public LevelRepository(Properties properties) {
        properties.stringPropertyNames().forEach(name -> {
            if (name.trim().startsWith("level")) {
                String[] nameSegments = name.split("@");
                switch (nameSegments.length) {
                    case 1:
                        loggerMinimumLevels.put("",
                                Level.valueOf(properties.getProperty("level").trim().toUpperCase()));
                        break;
                    case 2:
                        loggerMinimumLevels.put(nameSegments[1].trim(),
                                Level.valueOf(properties.getProperty(name).trim().toUpperCase()));
                        break;
                    default:
                        throw new IllegalArgumentException("level key: " + name);
                }
            }
        });
    }

    /**
     * @param nativeLogger to search for configured minimum output level
     * @return configured min output level for the specified logger
     */
    public Level getLoggerMinimumLevel(NativeLogger nativeLogger) {
        String callerClassName = nativeLogger.getName();
        int rootPackageLength = callerClassName.indexOf('.');
        if (rootPackageLength == -1) {
            rootPackageLength = callerClassName.length();
        }
        while (callerClassName.length() >= rootPackageLength) {
            if (loggerMinimumLevels.containsKey(callerClassName)) {
                return loggerMinimumLevels.get(callerClassName);
            }
            if (callerClassName.length() == rootPackageLength) {
                break;
            }
            int end = callerClassName.lastIndexOf('.');
            if (end == -1) {
                end = callerClassName.length();
            }
            callerClassName = callerClassName.substring(0, end);
        }
        Level configuredRootLevel = loggerMinimumLevels.get("");
        return configuredRootLevel == null ? DEFAULT_LOGGER_MINIMUM_LEVEL : configuredRootLevel;
    }
}
