/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wenky.log.trace.async;

import java.util.concurrent.*;

import static com.wenky.log.trace.async.TaskWrapper.wrap;



/**
 * @author zhongwenjian
 */
public class TraceScheduledExecutorService extends TraceExecutorService
		implements ScheduledExecutorService {

	public TraceScheduledExecutorService(final ExecutorService delegate) {
		super(delegate);
	}

	private ScheduledExecutorService getScheduledExecutorService() {
		return (ScheduledExecutorService) this.delegate;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return getScheduledExecutorService().schedule(wrap(command),
				delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay,
			TimeUnit unit) {
		return getScheduledExecutorService().schedule(wrap(callable),
				delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
			long period, TimeUnit unit) {
		return getScheduledExecutorService().scheduleAtFixedRate(wrap(command),
				initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
			long delay, TimeUnit unit) {
		return getScheduledExecutorService().scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
	}

}
