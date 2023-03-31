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
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MoreAwaitility {
    private static final ScheduledExecutorService delayer =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1, r -> {
                Thread daemonThread = new Thread(r, "more-awaitility-util-thread" + UUID.randomUUID());
                daemonThread.setDaemon(true);
                return daemonThread;
            });

    public static void block(@NonNull Duration duration) {
        block(duration, null);
    }

    public static void block(@NonNull Duration duration, String message) {
        if (message != null) {
            System.out.println(message + " - blocking for " + duration);
        }
        AtomicBoolean resume = new AtomicBoolean(false);
        delayer.schedule(() -> resume.set(true), duration.toMillis(), TimeUnit.MILLISECONDS);
        Awaitility.await().untilTrue(resume);
    }
}
