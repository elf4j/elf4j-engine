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

import static elf4j.engine.logging.writer.StandardStreamWriter.OutStreamType.STDOUT;

import elf4j.Level;
import elf4j.engine.logging.LogEvent;
import elf4j.engine.logging.pattern.PatternElement;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.logging.Logger;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * A log writer implementation that writes log events to the standard output or standard error
 * stream. The log pattern, threshold output level, and target stream (stdout or stderr) can be
 * configured.
 */
@Value
@ToString
public class StandardStreamWriter implements LogWriter {
  static final String DEFAULT_THRESHOLD_OUTPUT_LEVEL = "trace";
  static final String DEFAULT_PATTERN = "{timestamp} {level} {class} - {message}";
  static final OutStreamType DEFAULT_OUT_STREAM_TYPE = STDOUT;
  static final String LINE_FEED = System.lineSeparator();

  @EqualsAndHashCode.Exclude
  StandardOutput standardOutput;

  Level minimumThresholdLevel;
  PatternElement logPattern;
  OutStreamType outStreamType;

  @Builder
  private StandardStreamWriter(
      Level minimumThresholdLevel, PatternElement logPattern, OutStreamType outStreamType) {
    this.minimumThresholdLevel = minimumThresholdLevel;
    this.logPattern = logPattern;
    this.outStreamType = outStreamType;
    this.standardOutput = new StandardOutput(this.outStreamType);
  }

  /**
   * Returns the threshold output level for this log writer.
   *
   * @return the threshold output level
   */
  @Override
  public Level getMinimumThresholdLevel() {
    return minimumThresholdLevel;
  }

  /**
   * Writes the given log event to the configured output stream (stdout or stderr) if the log
   * event's level is greater than or equal to the configured threshold output level.
   *
   * @param logEvent the log event to write
   */
  @Override
  public void write(LogEvent logEvent) {
    if (logEvent.getLevel().compareTo(this.minimumThresholdLevel) < 0) {
      return;
    }
    StringBuilder target = new StringBuilder();
    logPattern.render(logEvent, target);
    byte[] bytes = target.append(LINE_FEED).toString().getBytes(StandardCharsets.UTF_8);
    standardOutput.flushOut(bytes);
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

  /**
   * Implementation of the StandardOutput interface that writes to the standard output and standard
   * error streams using FileOutputStream and synchronizes access using a ReentrantLock.
   */
  private static final class StandardOutput {
    private static final Logger LOGGER = Logger.getLogger(StandardOutput.class.getName());
    private static final Lock OUTPUT_LOCK = new java.util.concurrent.locks.ReentrantLock(true);
    private final OutputStream outputStream;

    public StandardOutput(OutStreamType outStreamType) {
      this.outputStream = switch (outStreamType) {
        case STDOUT -> System.out;
        case STDERR -> System.err;
      };
    }

    /**
     * This method is supposed to be called once and only once per each entirely complete log
     * message.
     *
     * @implNote To avoid "virtual thread pinning", this method is locking on a global lock instead
     *     of {@code synchronized} on {@code System.out} or {@code System.err}. This is to arrange
     *     the desired atomicity of the {@code write}-and-{@code flush} operation. Within the
     *     elf4j-engine, such locking atomicity ensures the flush of each log entry is
     *     self-initiated immediately after the entry's bytes are written (buffered). However, as
     *     the lock is (intentionally) not on the {@code System.out} or {@code System.err}, this
     *     does not prevent the logs from interleaving with content/bytes flushed by other/outside
     *     processes targeting the same STDOUT/STDERR stream. That means this log engine should not
     *     be used together with any other logging provider at the same time.
     * @param bytes to write to the specified standard stream
     */
    public void flushOut(byte[] bytes) {
      OUTPUT_LOCK.lock();
      try {
        outputStream.write(bytes);
        outputStream.flush();
      } catch (IOException e) {
        LOGGER.log(
            java.util.logging.Level.SEVERE,
            "Failed to write bytes[] of length %s to %s".formatted(bytes.length, outputStream),
            e);
      } finally {
        OUTPUT_LOCK.unlock();
      }
    }
  }
}
