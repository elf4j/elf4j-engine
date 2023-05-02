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

package elf4j.engine.service.util;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 *
 */
public class PropertiesUtils {

    private PropertiesUtils() {
    }

    /**
     * @param prefix
     *         key prefix to search for
     * @param properties
     *         search source
     * @return all properties entries whose original keys start with the specified prefix. The prefix is removed from
     *         the keys of the returned entries.
     */
    public static Map<String, String> getChildProperties(String prefix, @NonNull Properties properties) {
        final String start = prefix + '.';
        return properties.stringPropertyNames()
                .stream()
                .filter(name -> name.trim().startsWith(start))
                .collect(Collectors.toMap(name -> name.substring(start.length()).trim(),
                        name -> properties.getProperty(name).trim()));
    }

    /**
     * @param type
     *         the properties value whose keys are each used as a parent key prefix
     * @param properties
     *         search source
     * @return a group whose every member is a set of properties entries having a common key prefix of the specified
     *         type
     */
    public static List<Map<String, String>> getPropertiesGroupOfType(String type, @NonNull Properties properties) {
        return properties.stringPropertyNames()
                .stream()
                .filter(name -> properties.getProperty(name).trim().equals(type))
                .map(name -> getChildProperties(name, properties))
                .collect(Collectors.toList());
    }

    /**
     * @param name
     *         full key in properties
     * @param properties
     *         to look up in
     * @return Integer value of the specified name in the given properties, null if named entry missing or the
     *         corresponding value contains no digit
     */
    @Nullable
    public static Integer getAsInteger(String name, @NonNull Properties properties) {
        String value = properties.getProperty(name);
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        return value.startsWith("-") ? -Integer.parseInt(digits) : Integer.parseInt(digits);
    }
}