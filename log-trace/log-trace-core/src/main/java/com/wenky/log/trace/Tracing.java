package com.wenky.log.trace;


import com.wenky.log.trace.propagation.B3Propagation;
import com.wenky.log.trace.propagation.CurrentTraceContext;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.propagation.TraceContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public abstract class Tracing implements Closeable {

    static final AtomicReference<Tracing> CURRENT = new AtomicReference<>();

    public abstract Tracer tracer();

    public Propagation<String> propagation(){
        return propagationFactory().create(Propagation.KeyFactory.STRING);
    }

    public abstract Propagation.Factory propagationFactory();

    public abstract CurrentTraceContext currentTraceContext();

    public static Tracing current(){
        return CURRENT.get();
    }

    public static boolean isInTraceContext(){
        return Optional.ofNullable(CURRENT.get())
                .map(Tracing::currentTraceContext)
                .map(CurrentTraceContext::isInTraceContext)
                .orElse(false);
    }
    public static Builder newBuilder() {
        return new Builder();
    }
    public static Tracer currentTracer(){
        Tracing tracing = current();
        return tracing == null ? null : tracing.tracer();
    }

    public static final class Builder {
        CurrentTraceContext currentTraceContext;
        Propagation.Factory propagationFactory = B3Propagation.FACTORY;

        public Builder currentTraceContext(CurrentTraceContext currentTraceContext){
            this.currentTraceContext = currentTraceContext;
            return this;
        }

        public Builder propagationFactory(Propagation.Factory factory){
            this.propagationFactory = factory;
            return this;
        }
        Builder(){

        }
        public Tracing build(){
            return new Default(this);
        }
    }

    static final class Default extends Tracing {

        final Tracer tracer;

        final Propagation.Factory propagationFactory;

        final CurrentTraceContext currentTraceContext;

        Default(Builder builder){
            this.tracer = new Tracer(builder.currentTraceContext, builder.propagationFactory);
            this.propagationFactory = builder.propagationFactory;
            this.currentTraceContext = builder.currentTraceContext;
            CURRENT.compareAndSet(null, this);
        }

        @Override
        public Tracer tracer() {
            return tracer;
        }

        @Override
        public Propagation.Factory propagationFactory() {
            return propagationFactory;
        }

        @Override
        public CurrentTraceContext currentTraceContext() {
            return tracer.currentTraceContext;
        }

        @Override
        public void close() throws IOException {
            CURRENT.compareAndSet(this, null);
        }
    }

    public static TraceContext currentContext(){
        Tracing tracing = CURRENT.get();
        if (tracing == null){
            return null;
        }
        CurrentTraceContext currentTraceContext = tracing.currentTraceContext();
        return currentTraceContext.get();
    }

    public static void trace(Runnable runnable){
        Tracing tracing = CURRENT.get();
        if (tracing == null){
            runnable.run();
            return;
        }
        Tracer tracer = tracing.tracer();
        try (TraceScope ignored = tracer.newScope()) {
            runnable.run();
        }
    }

    public static <T> T trace(Callable<T> callable) throws Exception {
        Tracing tracing = CURRENT.get();
        if (tracing == null){
            return callable.call();
        }
        Tracer tracer = tracing.tracer();
        try (TraceScope ignored = tracer.newScope()) {
            return callable.call();
        }
    }

}
