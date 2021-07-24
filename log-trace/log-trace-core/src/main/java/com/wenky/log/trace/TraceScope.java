package com.wenky.log.trace;

import com.wenky.log.trace.propagation.CurrentTraceContext;
import com.wenky.log.trace.propagation.TraceContext;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class TraceScope implements AutoCloseable{
    final TraceContext context;
    final CurrentTraceContext.Scope scope;

    public TraceScope(TraceContext context, CurrentTraceContext.Scope scope) {
        this.context = context;
        this.scope = scope;
    }

    public TraceContext context(){
        return context;
    }

    @Override
    public void close(){
        scope.close();
    }
}
