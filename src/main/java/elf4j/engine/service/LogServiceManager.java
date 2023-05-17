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

import elf4j.engine.service.configuration.Refreshable;
import lombok.NonNull;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public enum LogServiceManager {
    /**
     *
     */
    INSTANCE;

    private final Set<Refreshable> refreshables = new HashSet<>();
    private final Set<Stoppable> stoppables = new HashSet<>();
    private final ConditionFactory await = Awaitility.with().timeout(Duration.ofMinutes(10));

    private static boolean allStopped(@NonNull Collection<Stoppable> stoppables) {
        return stoppables.stream().allMatch(Stoppable::isStopped);
    }

    /**
     * @param refreshable
     *         added to be accessible for management
     */
    public void registerRefresh(Refreshable refreshable) {
        this.refreshables.add(refreshable);
    }

    /**
     * @param stoppable
     *         added to be accessible for management
     */
    public void registerStop(Stoppable stoppable) {
        this.stoppables.add(stoppable);
    }

    /**
     * reloads properties source for each refreshable
     */
    public void refresh() {
        refreshables.forEach(Refreshable::refresh);
    }

    /**
     * @param properties
     *         if non-null, replaces current configuration with the specified properties, instead of reloading from the
     *         original properties source; otherwise, reloads the original properties source for each refreshable.
     */
    public void refresh(Properties properties) {
        refreshables.forEach(refreshable -> refreshable.refresh(properties));
    }

    /**
     *
     */
    public void stop() {
        stopIntake();
        stopOutput();
    }

    /**
     * @return a thread that orderly stops the entire log service. As an alternative to calling the {@link #stop()}, the
     *         returned thread can be registered as a JVM shutdown hook.
     */
    @NonNull
    public Thread getShutdownHookThread() {
        return new Thread(this::stop);
    }

    private void stopIntake() {
        stopType(Stoppable.Intake.class);
    }

    private void stopOutput() {
        stopType(Stoppable.Output.class);
    }

    private void stopType(Class<? extends Stoppable> targetType) {
        List<Stoppable> stopTargets = stoppables.stream().filter(targetType::isInstance).collect(Collectors.toList());
        stopTargets.stream().parallel().forEach(Stoppable::stop);
        this.await.until(() -> allStopped(stopTargets));
    }
}
