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

import java.io.*;
import java.util.Properties;

/**
 *
 */
@ToString
public class BufferedStandardOutput implements StandardOutput {
    private static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
    @NonNull private final OutStreamType outStreamType;
    private final BufferedStandardOutStreams bufferedStandardOutStreams = new BufferedStandardOutStreams();

    /**
     * @param outStreamType
     *         standard out stream type, stdout or stderr, default to stdout
     */
    private BufferedStandardOutput(@NonNull OutStreamType outStreamType) {
        this.outStreamType = outStreamType;
    }

    /**
     * @param logServiceConfiguration
     *         entire service configuration @return the {@link BufferedStandardOutput} per the specified configuration
     * @return output per specified configuration
     */
    public static @NonNull BufferedStandardOutput from(@NonNull LogServiceConfiguration logServiceConfiguration) {
        Properties properties = logServiceConfiguration.getProperties();
        String stream = properties.getProperty("stream");
        return new BufferedStandardOutput(
                stream == null ? DEFAULT_OUT_STREAM_TYPE : OutStreamType.valueOf(stream.toUpperCase()));
    }

    @Override
    public void write(byte[] bytes) {
        if (this.outStreamType == OutStreamType.STDERR) {
            bufferedStandardOutStreams.err(bytes);
        } else {
            bufferedStandardOutStreams.out(bytes);
        }
    }

    enum OutStreamType {
        STDOUT,
        STDERR
    }

    static class BufferedStandardOutStreams {
        final OutputStream bufferedStdOut = new BufferedOutputStream(new FileOutputStream(FileDescriptor.out), 2048);
        final OutputStream bufferedStdErr = new BufferedOutputStream(new FileOutputStream(FileDescriptor.err), 2048);

        BufferedStandardOutStreams() {
        }

        void err(byte[] bytes) {
            synchronized (System.err) {
                try {
                    bufferedStdErr.write(bytes);
                    bufferedStdErr.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        void out(byte[] bytes) {
            synchronized (System.out) {
                try {
                    bufferedStdOut.write(bytes);
                    bufferedStdOut.flush();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }
}