package com.wenky.log.trace;

import com.wenky.log.trace.propagation.CurrentTraceContext;
import com.wenky.log.trace.propagation.TraceContext;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ContextScope {
    final TraceContext context;
    final CurrentTraceContext.Scope scope;

    public ContextScope(TraceContext context, CurrentTraceContext.Scope scope) {
        this.context = context;
        this.scope = scope;
    }

    public TraceContext context(){
        return context;
    }

    public void finish(){
        scope.close();
    }
}
