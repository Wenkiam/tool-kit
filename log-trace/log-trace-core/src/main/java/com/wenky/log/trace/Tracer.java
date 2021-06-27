package com.wenky.log.trace;

import com.wenky.log.trace.internal.Platform;
import com.wenky.log.trace.propagation.CurrentTraceContext;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.propagation.TraceContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class Tracer {

    final CurrentTraceContext currentTraceContext;

    final Propagation.Factory propagationFactory;

    public Tracer(CurrentTraceContext currentTraceContext, Propagation.Factory propagationFactory) {
        this.currentTraceContext = currentTraceContext;
        this.propagationFactory = propagationFactory;
    }

    public ContextScope newScope(){
        TraceContext context = currentTraceContext.get();
        if (context == null){
            context = propagationFactory.decorate(new TraceContext(nextId()));
        }
        CurrentTraceContext.Scope scope = currentTraceContext.newScope(context);
        return new ContextScope(context, scope);
    }

    public ContextScope newScope(TraceContext context){
        CurrentTraceContext.Scope scope = currentTraceContext.newScope(context);
        return new ContextScope(context, scope);
    }

    public ContextScope newScope(Map<String, String> extra){
        TraceContext context = currentTraceContext.get();
        if (context == null){
            context = propagationFactory.decorate(new TraceContext(nextId(), extra));
        }
        CurrentTraceContext.Scope scope = currentTraceContext.newScope(context);
        return new ContextScope(context, scope);
    }

    long nextId() {
        long nextId = Platform.get().randomLong();
        while (nextId == 0L) {
            nextId = Platform.get().randomLong();
        }
        return nextId;
    }

}
