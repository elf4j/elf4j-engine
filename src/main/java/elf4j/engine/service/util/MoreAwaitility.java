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

import elf4j.Level;
import elf4j.util.InternalLogger;
import lombok.NonNull;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class MoreAwaitility {
    private MoreAwaitility() {
    }

    /**
     * @param duration
     *         to suspend the current thread for
     */
    public static void suspend(@NonNull Duration duration) {
        suspend(duration, null);
    }

    /**
     * @param duration
     *         to suspend the current thread for
     * @param message
     *         intended for internal logging which goes out to stderr
     */
    public static void suspend(@NonNull Duration duration, String message) {
        if (message != null) {
            InternalLogger.INSTANCE.log(Level.INFO,
                    message + " - suspending current thread " + Thread.currentThread() + " for " + duration);
        }
        AtomicBoolean resume = new AtomicBoolean(false);
        ScheduledExecutorService delayer = Executors.newSingleThreadScheduledExecutor();
        delayer.schedule(() -> resume.set(true), duration.toMillis(), TimeUnit.MILLISECONDS);
        delayer.shutdown();
        Awaitility.with().pollInterval(duration.dividedBy(10)).await().untilTrue(resume);
    }
}