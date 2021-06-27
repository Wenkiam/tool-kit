package com.wenky.log.trace.async;

import com.wenky.log.trace.Tracing;

import java.util.concurrent.Callable;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
class TaskWrapper {

    static Runnable wrap(Runnable task){
        return new TraceRunnable(task);
    }

    static <T> Callable<T> wrap(Callable<T> task){
        return new TraceCallable<>(task);
    }

    static class TraceRunnable implements Runnable {
        final Runnable delegate;

        TraceRunnable(Runnable delegate) {
            this.delegate = delegate instanceof TraceRunnable ?
                    ((TraceRunnable) delegate).delegate : delegate;
        }

        @Override
        public void run() {
            Tracing.trace(delegate);
        }
    }

    static class TraceCallable<V> implements Callable<V>{
        final Callable<V> delegate;

        TraceCallable(Callable<V> delegate) {
            this.delegate = delegate instanceof TraceCallable ?
                    ((TraceCallable<V>) delegate).delegate : delegate;
        }

        @Override
        public V call() throws Exception {
            return Tracing.trace(delegate);
        }
    }
}
