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

package elf4j.engine.service;

import elf4j.util.IeLogger;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.ToString;

/**
 * The NativeLogServiceManager class is responsible for managing the log service. It provides methods for registering
 * and deregistering Refreshable and Stoppable instances, refreshing the configuration, and shutting down the service.
 */
@ToString
public enum NativeLogServiceManager {
    /** The singleton instance of the NativeLogServiceManager. */
    INSTANCE;

    private final Set<Refreshable> refreshables = new HashSet<>();
    private final Set<Stoppable> stoppables = new HashSet<>();

    @ToString.Exclude
    private final Lock lock = new ReentrantLock();

    /**
     * Registers a Refreshable instance with the NativeLogServiceManager.
     *
     * @param refreshable added to be accessible for management
     */
    public void register(Refreshable refreshable) {
        lockAndRun(() -> refreshables.add(refreshable));
        IeLogger.INFO.log("Registered Refreshable {} in {}", refreshable, this);
    }

    /**
     * Registers a Stoppable instance with the NativeLogServiceManager.
     *
     * @param stoppable added to be accessible for management
     */
    public void register(Stoppable stoppable) {
        lockAndRun(() -> stoppables.add(stoppable));
        IeLogger.INFO.log("Registered Stoppable {} in {}", stoppable, this);
    }

    /** reloads properties source for each refreshable */
    public void refresh() {
        IeLogger.INFO.log("Refreshing elf4j service by {} via reloading properties", this);
        lockAndRun(() -> {
            shutdown();
            refreshables.forEach(Refreshable::refresh);
        });
        IeLogger.INFO.log("Refreshed elf4j service by {} via reloading properties", this);
    }

    /**
     * Refreshes the configuration for each registered Refreshable instance.
     *
     * @param properties if non-null, replaces current configuration with the specified properties, instead of reloading
     *     from the original properties source; otherwise, reloads the original properties source for each refreshable.
     */
    public void refresh(Properties properties) {
        IeLogger.INFO.log("Refreshing elf4j service by {} with properties {}", this, properties);
        lockAndRun(() -> {
            shutdown();
            refreshables.forEach(refreshable -> refreshable.refresh(properties));
        });
        IeLogger.INFO.log("Refreshed elf4j service by {} with properties {}", this, properties);
    }

    /** Stops all registered Stoppable instances and clears the set of registered Stoppable instances. */
    public void shutdown() {
        IeLogger.INFO.log("Start shutting down elf4j service by {}", this);
        lockAndRun(() -> {
            stoppables.forEach(Stoppable::stop);
            stoppables.clear();
        });
        IeLogger.INFO.log("End shutting down elf4j service by {}", this);
    }

    /**
     * Returns a thread that orderly stops the entire log service. As an alternative to calling {@link #shutdown()}, the
     * returned thread can be registered as a JVM shutdown hook.
     *
     * @return a thread that orderly stops the entire log service. As an alternative to calling {@link #shutdown()}, the
     *     returned thread can be registered as a JVM shutdown hook.
     */
    @NonNull public Thread getShutdownHookThread() {
        return new Thread(this::shutdown);
    }

    /**
     * Deregisters the specified Refreshable instance from the NativeLogServiceManager.
     *
     * @param refreshable to be deregistered
     */
    public void deregister(Refreshable refreshable) {
        lockAndRun(() -> refreshables.remove(refreshable));
        IeLogger.INFO.log("De-registered Refreshable {}", refreshable);
    }

    private void lockAndRun(@NonNull Runnable runnable) {
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }

    /** The Refreshable interface defines the contract for components that can refresh their configuration. */
    public interface Refreshable {
        /**
         * Refreshes the configuration of the component.
         *
         * @param properties used to refresh the logging configuration. If <code>null</code>, only properties reloaded
         *     from the configuration file will be used. Otherwise, the specified properties will replace all current
         *     properties and configuration file is ignored.
         */
        void refresh(@Nullable Properties properties);

        /** reloads from original source of properties */
        void refresh();
    }

    /** The Stoppable interface defines the contract for components that can be stopped. */
    public interface Stoppable {
        /** Stops the component. */
        void stop();
    }
}
