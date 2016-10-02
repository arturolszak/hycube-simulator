package net.hycube.simulator.environment;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.hycube.environment.ScheduledTask;
import net.hycube.environment.TimeProvider;

public class AcceleratedSystemTimeProvider implements TimeProvider {

	
	public static final int DEFAULT_SCHEDULER_THREAD_POOL_SIZE = 1;
	
	
	protected ScheduledThreadPoolExecutor schedExecService;
	protected double timeAcceleration;
	protected boolean discarded;
	

	public AcceleratedSystemTimeProvider() {
		this(DEFAULT_SCHEDULER_THREAD_POOL_SIZE);
	}
	
	public AcceleratedSystemTimeProvider(double timeAcceleration) {
		this(DEFAULT_SCHEDULER_THREAD_POOL_SIZE, timeAcceleration);	
	}
	
	public AcceleratedSystemTimeProvider(int schedulerThreadPoolSize, double timeAcceleration) {
		schedExecService = new ScheduledThreadPoolExecutor(schedulerThreadPoolSize);
		this.timeAcceleration = timeAcceleration;
		discarded = false;	
	}
	
	
	public boolean isDiscarded() {
		return discarded;
	}
	
	
	@Override
	public long getCurrentTime() {
		
		return (long) (timeAcceleration * System.currentTimeMillis());
		
	}

	@Override
	public void schedule(ScheduledTask scheduledTask) {
		schedule(scheduledTask, (long) (timeAcceleration * scheduledTask.getExecutionTime()));
		
	}

	@Override
	public void schedule(ScheduledTask scheduledTask, long executionTime) {
		
		long currTime = (long) (timeAcceleration * System.currentTimeMillis());
		long delay = executionTime - currTime;
		if (delay < 0) delay = 0;
		
		scheduleWithDelay(scheduledTask, delay);
		
	}

	
	@Override
	public void scheduleWithDelay(ScheduledTask scheduledTask, long delay) {
		schedExecService.schedule(scheduledTask, delay, TimeUnit.MILLISECONDS);
		
	}

	
	@Override
	public void discard() {
		schedExecService.shutdownNow();
		discarded = true;
		
	}
	
	
	
}
