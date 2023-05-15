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

package elf4j.engine.service.writer;

import elf4j.engine.service.configuration.LogServiceConfiguration;
import lombok.NonNull;
import lombok.ToString;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 *
 */
@ToString
public class DirectStandardOutput implements StandardOutput {
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    @NonNull private final OutStreamType outStreamType;

    /**
     * @param outStreamType
     *         standard out stream type, stdout or stderr, default to stdout
     */
    private DirectStandardOutput(@NonNull OutStreamType outStreamType) {
        this.outStreamType = outStreamType;
    }

    /**
     * @param logServiceConfiguration
     *         entire service configuration @return the {@link DirectStandardOutput} per the specified configuration
     * @return output per specified configuration
     */
    public static @NonNull DirectStandardOutput from(@NonNull LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        String stream = properties.getProperty("stream");
        return new DirectStandardOutput(
                stream == null ? DEFAULT_OUT_STREAM_TYPE : OutStreamType.valueOf(stream.toUpperCase()));
    }

    @Override
    public void write(byte[] bytes) {
        try {
            if (this.outStreamType == OutStreamType.STDERR) {
                System.err.write(bytes);
            } else {
                System.out.write(bytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    enum OutStreamType {
        STDOUT,
        STDERR
    }
}