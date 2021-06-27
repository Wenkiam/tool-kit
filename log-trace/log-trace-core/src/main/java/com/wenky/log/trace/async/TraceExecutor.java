package com.wenky.log.trace.async;

import com.wenky.log.trace.Tracing;

import java.util.concurrent.Executor;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class TraceExecutor implements Executor {

    private final TraceExecutor delegate;

    public TraceExecutor(TraceExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(TaskWrapper.wrap(command));
    }
}
