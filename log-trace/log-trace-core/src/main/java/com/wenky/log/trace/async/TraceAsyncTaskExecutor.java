package com.wenky.log.trace.async;

import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static com.wenky.log.trace.async.TaskWrapper.wrap;


/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class TraceAsyncTaskExecutor implements AsyncTaskExecutor {


    private final AsyncTaskExecutor delegate;



    public TraceAsyncTaskExecutor(AsyncTaskExecutor delegate) {
        this.delegate = delegate;
    }

    @Override
    public void execute(Runnable task, long startTimeout) {
        delegate.execute(wrap(task), startTimeout);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public void execute(Runnable task) {
        delegate.execute(wrap(task));
    }


}
