package com.wenky.log.trace.async;

import com.wenky.log.trace.TraceScope;
import com.wenky.log.trace.Tracer;
import com.wenky.log.trace.Tracing;
import com.wenky.log.trace.propagation.TraceContext;

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
        final TraceContext context;
        final Tracer tracer;
        TraceRunnable(Runnable delegate) {
            tracer = Tracing.current().tracer();
            context = tracer.getOrCreateContext().clone();
            this.delegate = delegate instanceof TraceRunnable ?
                    ((TraceRunnable) delegate).delegate : delegate;
        }

        @Override
        public void run() {
            try (TraceScope ignore = tracer.newScope(context)){
                delegate.run();
            }
        }
    }

    static class TraceCallable<V> implements Callable<V>{
        final Callable<V> delegate;
        final TraceContext context;
        final Tracer tracer;
        TraceCallable(Callable<V> delegate) {
            tracer = Tracing.current().tracer();
            context = tracer.getOrCreateContext();
            this.delegate = delegate instanceof TraceCallable ?
                    ((TraceCallable<V>) delegate).delegate : delegate;
        }

        @Override
        public V call() throws Exception {
            try (TraceScope ignore = tracer.newScope(context)){
                return delegate.call();
            }
        }
    }
}
