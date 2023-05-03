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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

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
    public void refreshAll() {
        refreshables.forEach(Refreshable::refresh);
    }

    /**
     * @param properties
     *         if non-null, replaces current configuration with the specified properties, instead of reloading from the
     *         original properties source; otherwise, reloads the original properties source for each refreshable.
     */
    public void refreshAll(Properties properties) {
        refreshables.forEach(refreshable -> refreshable.refresh(properties));
    }

    /**
     *
     */
    public void stopAll() {
        stopService();
        stopOutput();
    }

    private void stopOutput() {
        stoppables.stream()
                .filter(s -> !(s instanceof LogServiceDispatchingThread))
                .parallel()
                .forEach(Stoppable::stop);
    }

    private void stopService() {
        stoppables.stream().filter(LogServiceDispatchingThread.class::isInstance).parallel().forEach(Stoppable::stop);
    }
}
