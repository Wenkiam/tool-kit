package com.wenky.log.trace.propagation;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public abstract class CurrentTraceContext {

    protected CurrentTraceContext() {
        this.scopeDecorators = Collections.emptyList();
    }

    protected CurrentTraceContext(Builder builder) {
        this.scopeDecorators = new ArrayList<>(builder.scopeDecorators);
    }

    public abstract TraceContext get();

    public abstract boolean isInTraceContext();

    public abstract Scope newScope(TraceContext currentSpan);

    final List<ScopeDecorator> scopeDecorators;

    protected Scope decorateScope(TraceContext currentSpan, Scope scope) {
        for (ScopeDecorator scopeDecorator : scopeDecorators) {
            scope = scopeDecorator.decorateScope(currentSpan, scope);
        }
        return scope;
    }
    public abstract static class Builder {
        ArrayList<ScopeDecorator> scopeDecorators = new ArrayList<>();

        /**
         * Implementations call decorators in order to add features like log correlation to a scope.
         *
         * @since 5.2
         */
        public Builder addScopeDecorator(ScopeDecorator scopeDecorator) {
            if (scopeDecorator == null) {
                throw new NullPointerException("scopeDecorator == null");
            }
            this.scopeDecorators.add(scopeDecorator);
            return this;
        }

        public abstract CurrentTraceContext build();
    }
    public interface Scope extends Closeable {

        @Override
        void close();
    }

    public interface ScopeDecorator {
        Scope decorateScope(TraceContext currentSpan, Scope scope);
    }


    public static final class Default extends ThreadLocalCurrentTraceContext {

        static final InheritableThreadLocal<TraceContext> INHERITABLE = new InheritableThreadLocal<>();

        public static CurrentTraceContext create() {
            return ThreadLocalCurrentTraceContext.create();
        }

        public static CurrentTraceContext inheritable() {
            return new Default();
        }

        public static CurrentTraceContext.Builder newBuilder(){
            return new Builder(INHERITABLE);
        }
        Default() {
            super( newBuilder(), INHERITABLE);
        }
    }
}
