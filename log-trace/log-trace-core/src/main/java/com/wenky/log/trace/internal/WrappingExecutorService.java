package com.wenky.log.trace.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public abstract class WrappingExecutorService implements ExecutorService {
    protected WrappingExecutorService() {
    }

    protected abstract ExecutorService delegate();

    protected abstract <C> Callable<C> wrap(Callable<C> task);

    protected abstract Runnable wrap(Runnable task);

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate().awaitTermination(timeout, unit);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return delegate().invokeAll(wrap(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                         TimeUnit unit) throws InterruptedException {
        return delegate().invokeAll(wrap(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return delegate().invokeAny(wrap(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate().invokeAny(wrap(tasks), timeout, unit);
    }

    @Override
    public boolean isShutdown() {
        return delegate().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate().isTerminated();
    }

    @Override
    public void shutdown() {
        delegate().shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate().shutdownNow();
    }

    @Override
    public void execute(Runnable task) {
        delegate().execute(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate().submit(wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate().submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate().submit(wrap(task), result);
    }

    <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        ArrayList<Callable<T>> result = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            result.add(wrap(task));
        }
        return result;
    }
}
