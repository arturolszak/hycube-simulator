package net.hycube.simulator.environment;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import net.hycube.environment.ScheduledTask;
import net.hycube.environment.TimeProvider;

public class VirtualTimeProvider implements TimeProvider {

	
	public static int DEFAULT_TIMER_THREAD_POOL_SIZE = 2;
	public static int TASK_QUEUE_INITIAL_CAPACITY = 10;
	
	protected long currTime;
	protected ScheduledExecutorService schedExecService;
	protected PriorityQueue<ScheduledTask> taskQueue;
	protected boolean checkTasks;
	protected boolean active;
	protected boolean discarded;
	protected Object timeLock = new Object();
	

	public VirtualTimeProvider() {
		this(DEFAULT_TIMER_THREAD_POOL_SIZE);
	}
	
	public VirtualTimeProvider(int timerThreadPoolSize) {
		schedExecService = Executors.newScheduledThreadPool(timerThreadPoolSize);
		taskQueue = new PriorityQueue<ScheduledTask>(TASK_QUEUE_INITIAL_CAPACITY, new ScheduledTask.ScheduledTaskComparator());
		active = true;
		discarded = false;
		checkTasks = false;
		
	}
	
	
	public boolean isDiscarded() {
		return discarded;
	}
	
	
	@Override
	public long getCurrentTime() {
		synchronized (timeLock) {
			return currTime;
		}
	}
	
	
	protected long incrementTime() {
		return advanceTime(1);
	}
	
	protected long advanceTime(long value) {
		synchronized (timeLock) {
			currTime = currTime + value;
			
		}
		timeChanged();
		return currTime;
		
	}

	
	@Override
	public void schedule(ScheduledTask scheduledTask) {
		synchronized (timeLock) {
			synchronized (taskQueue) {
				taskQueue.add(scheduledTask);
			}
		}
		wakeupTimerThreads();
	}

	@Override
	public void schedule(ScheduledTask scheduledTask, long executionTime) {
		scheduledTask.setExecutionTime(executionTime);
		schedule(scheduledTask);
		
	}

	
	@Override
	public void scheduleWithDelay(ScheduledTask scheduledTask, long delay) {
		long executionTime;
		synchronized (timeLock) {
			executionTime = currTime + delay;
		}
		scheduledTask.setExecutionTime(executionTime);
		schedule(scheduledTask);
		
	}

	
	protected ScheduledTask checkTasks() {
		//check the execution time of the first inserted task
		synchronized (timeLock) {
			synchronized (taskQueue) {
				ScheduledTask task = taskQueue.peek();
				if (task != null && task.getExecutionTime() <= currTime) {
					return task;
				}
				else return null;
			}
		}
	}
	
	
	
	protected void timeChanged() {
		wakeupTimerThreads();
	}
	
	
	protected void processQueue() {
		synchronized (timeLock) {
			synchronized (taskQueue) {
				if (! checkTasks) {
					//wait for the tasks
					try {
						taskQueue.wait();
					} catch (InterruptedException e) {
						//do nothing
					}
				}
				if (checkTasks) {
					ScheduledTask task = checkTasks();
					if (task != null) {
						//submit new processQueue call to the thread pool
						//!!!...
						
						//and process the task
						task.run();
					}
					else {
						checkTasks = false;
					}
				}
			}
		}
	}
	
	
	protected void wakeupTimerThreads() {
		synchronized (timeLock) {
			synchronized (taskQueue) {
				checkTasks = true;
				taskQueue.notify();
			}
		}
	}
	
	
	@Override
	public void discard() {
		active = false;


		discarded = true;
		
	}
	
	
	
}
