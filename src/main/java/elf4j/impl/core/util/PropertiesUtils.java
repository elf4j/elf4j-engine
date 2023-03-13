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

package elf4j.impl.core.util;

import java.util.*;

/**
 *
 */
public class PropertiesUtils {

    private PropertiesUtils() {
    }

    /**
     * @param prefix     all properties whose keys start with this prefix are to be searched for
     * @param properties search source
     * @return all properties whose keys start with the specified prefix
     */
    public static Map<String, String> getChildProperties(String prefix, Properties properties) {
        Map<String, String> childProperties = new HashMap<>();
        String parentPrefix = prefix + '.';
        properties.stringPropertyNames()
                .stream()
                .filter(name -> name.trim().startsWith(parentPrefix))
                .forEach(name -> childProperties.put(name.substring(name.indexOf('.') + 1).trim(),
                        properties.getProperty(name).trim()));
        return childProperties;
    }

    /**
     * @param type       the properties value whose key is used as parent prefix
     * @param properties search source
     * @return properties group each member of which is a set of properties having a unique key prefix of the specified
     *         type
     */
    public static List<Map<String, String>> getPropertiesGroupOfType(String type, Properties properties) {
        List<String> typeKeys = new ArrayList<>();
        properties.stringPropertyNames()
                .stream()
                .filter(name -> properties.getProperty(name).trim().equals(type))
                .forEach(typeKeys::add);
        List<Map<String, String>> propertiesGroup = new ArrayList<>();
        Collections.sort(typeKeys);
        typeKeys.forEach(k -> propertiesGroup.add(getChildProperties(k, properties)));
        return propertiesGroup;
    }
}