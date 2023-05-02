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

import lombok.NonNull;
import org.awaitility.Awaitility;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class BufferingWriterThread implements WriterThread {
    private static final int DEFAULT_FRONT_BUFFER_CAPACITY = 262144;
    private final ExecutorService executorService;

    /**
     * @param bufferCapacity
     *         async work queue capacity for log entry tasks
     */
    public BufferingWriterThread(Integer bufferCapacity) {
        bufferCapacity = bufferCapacity == null ? DEFAULT_FRONT_BUFFER_CAPACITY : bufferCapacity;
        //        this.executorService = new ThreadPoolExecutor(1,
        //                1,
        //                0L,
        //                TimeUnit.MILLISECONDS,
        //                new ArrayBlockingQueue<>(bufferCapacity == 0 ? DEFAULT_FRONT_BUFFER_CAPACITY : bufferCapacity),
        //                r -> new Thread(r, "elf4j-engine-writer-thread"),
        //                new BufferOverloadHandler());
        this.executorService = new Ex(1,
                1,
                0,
                TimeUnit.MILLISECONDS,
                bufferCapacity == 0 ? new SynchronousQueue<>() : new ArrayBlockingQueue<>(bufferCapacity),
                new WarningBufferOverloadHandler(bufferCapacity));
        LogServiceManager.INSTANCE.registerStop(this);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        this.executorService.execute(command);
    }

    @Override
    public void stop() {
        this.executorService.shutdown();
        Awaitility.with().timeout(30, TimeUnit.MINUTES).await().until(this.executorService::isTerminated);
    }

    static class Ex extends ThreadPoolExecutor {
        AtomicInteger waterMark = new AtomicInteger();

        public Ex(int corePoolSize,
                int maximumPoolSize,
                long keepAliveTime,
                TimeUnit unit,
                BlockingQueue<Runnable> workQueue,
                RejectedExecutionHandler rejectedExecutionHandler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, rejectedExecutionHandler);
        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            //            super.beforeExecute(t, r);
            //            System.err.println("f " + waterMark.updateAndGet(w -> Math.max(w, this.getQueue().size())));
        }
    }

    static class WarningBufferOverloadHandler extends BufferOverloadHandler {
        final int capacity;

        WarningBufferOverloadHandler(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //            InternalLogger.INSTANCE.log(Level.WARN,
            //                    "Dispatch rate lower than logging rate, buffer overloaded with queue size " + executor.getQueue()
            //                            .size());
            super.rejectedExecution(r, executor);
        }
    }
}
