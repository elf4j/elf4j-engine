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

package elf4j.engine.logging.writer;

import elf4j.Level;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.PatternElement;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.concurrent.ThreadSafe;
import lombok.Builder;
import lombok.ToString;

/**
 * A log writer implementation that writes log events to the standard output or standard error
 * stream. The log pattern, threshold output level, and target stream (stdout or stderr) can be
 * configured.
 */
@Builder
@ToString
public class StandardStreamWriter implements LogWriter {
  static final String DEFAULT_THRESHOLD_OUTPUT_LEVEL = "trace";
  static final String DEFAULT_PATTERN = "{timestamp} {level} {class} - {message}";
  static final OutStreamType DEFAULT_OUT_STREAM_TYPE = OutStreamType.STDOUT;
  static final String LINE_FEED = System.lineSeparator();
  private final StandardOutput standardOutput = new FileStreamStandardOutput();
  private final Level thresholdOutputLevel;
  private final PatternElement logPattern;
  private final OutStreamType outStreamType;

  /**
   * Returns the threshold output level for this log writer.
   *
   * @return the threshold output level
   */
  @Override
  public Level getThresholdOutputLevel() {
    return thresholdOutputLevel;
  }

  /**
   * Writes the given log event to the configured output stream (stdout or stderr) if the log
   * event's level is greater than or equal to the configured threshold output level.
   *
   * @param logEvent the log event to write
   */
  @Override
  public void write(LogEvent logEvent) {
    if (logEvent.getLevel().compareTo(this.thresholdOutputLevel) < 0) {
      return;
    }
    StringBuilder target = new StringBuilder();
    logPattern.render(logEvent, target);
    byte[] bytes = target.append(LINE_FEED).toString().getBytes(StandardCharsets.UTF_8);
    switch (outStreamType) {
      case STDOUT -> standardOutput.out(bytes);
      case STDERR -> standardOutput.err(bytes);
    }
  }

  /**
   * Returns whether the log pattern includes caller detail (e.g., source code location).
   *
   * @return true if the log pattern includes caller detail, false otherwise
   */
  @Override
  public boolean includeCallerDetail() {
    return logPattern.includeCallerDetail();
  }

  /** Enum representing the output stream type (stdout or stderr). */
  enum OutStreamType {
    STDOUT,
    STDERR
  }

  /** Interface for writing bytes to the standard output or standard error stream. */
  @ThreadSafe
  public interface StandardOutput {
    /**
     * Writes the given bytes to the standard output stream.
     *
     * @param bytes the bytes to write
     */
    void out(byte[] bytes);

    /**
     * Writes the given bytes to the standard error stream.
     *
     * @param bytes the bytes to write
     */
    void err(byte[] bytes);
  }

  /**
   * Implementation of the StandardOutput interface that writes to the standard output and standard
   * error streams using FileOutputStream and synchronizes access using a ReentrantLock.
   */
  @ToString
  public static class FileStreamStandardOutput implements StandardOutput {
    private final OutputStream stdout = new FileOutputStream(FileDescriptor.out);
    private final OutputStream stderr = new FileOutputStream(FileDescriptor.err);
    private final Lock lock = new ReentrantLock();

    @Override
    public void out(byte[] bytes) {
      write(bytes, stdout);
    }

    @Override
    public void err(byte[] bytes) {
      write(bytes, stderr);
    }

    private void write(byte[] bytes, OutputStream outputStream) {
      lock.lock();
      try {
        outputStream.write(bytes);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } finally {
        lock.unlock();
      }
    }
  }
}
