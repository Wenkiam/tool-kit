package com.wenky.log.trace.propagation;


/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ThreadLocalCurrentTraceContext extends CurrentTraceContext {

    public static CurrentTraceContext create() {
        return new Builder(DEFAULT).build();
    }

    public static CurrentTraceContext.Builder newBuilder() {
        return new Builder(DEFAULT);
    }

    static final class Builder extends CurrentTraceContext.Builder {
        private  final ThreadLocal<TraceContext> local;
        @Override
        public CurrentTraceContext build() {
            return new ThreadLocalCurrentTraceContext(this, local);
        }


        Builder(ThreadLocal<TraceContext> local) {
            this.local = local;
        }
    }

    static final ThreadLocal<TraceContext> DEFAULT = new ThreadLocal<>();

    @SuppressWarnings("ThreadLocalUsage") // intentional: to support multiple Tracer instances
    final ThreadLocal<TraceContext> local;

    ThreadLocalCurrentTraceContext(
            CurrentTraceContext.Builder builder,
            ThreadLocal<TraceContext> local
    ) {
        super(builder);
        if (local == null) {
            throw new NullPointerException("local == null");
        }
        this.local = local;
    }

    @Override
    public TraceContext get() {
        return local.get();
    }

    @Override
    public boolean isInTraceContext() {
        return local.get() != null;
    }

    @Override
    public Scope newScope(TraceContext currentSpan) {
        TraceContext previous = local.get();
        local.set(currentSpan);
        return decorateScope(currentSpan, ()->{
            if (previous == null) {
                local.remove();
            }
            else {
                local.set(previous);
            }
        });
    }
}
