package elf4j.engine.util;

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
                Thread daemonThread = new Thread(r, "more-await-util-thread" + UUID.randomUUID());
                daemonThread.setDaemon(true);
                return daemonThread;
            });

    public static void await(@NonNull Duration duration) {
        AtomicBoolean resume = new AtomicBoolean(false);
        delayer.schedule(() -> resume.set(true), duration.toMillis(), TimeUnit.MILLISECONDS);
        Awaitility.await().atLeast(duration).until(resume::get);
    }
}