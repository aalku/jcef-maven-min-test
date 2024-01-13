package org.aalku.demo.jcef;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

@Component
public class ClockService implements DisposableBean {
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void runNextSecond(Runnable task) {
		scheduler.schedule(task, 1000 - System.currentTimeMillis() % 1000L, TimeUnit.MILLISECONDS);
	}

	@Override
	public void destroy() throws Exception {
		scheduler.shutdownNow();
	}
	
}
