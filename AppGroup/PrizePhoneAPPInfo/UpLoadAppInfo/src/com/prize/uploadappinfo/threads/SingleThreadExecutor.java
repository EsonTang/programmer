package com.prize.uploadappinfo.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadExecutor {

	/*** 实例 */
	private static SingleThreadExecutor instance = null;

	/*** 单线程 */
	private ExecutorService executor = null;

	private SingleThreadExecutor() {
	}

	/**
	 * 去实例
	 * 
	 * @return
	 */
	public static SingleThreadExecutor getInstance() {
		synchronized (SingleThreadExecutor.class) {
			if (instance == null) {
				instance = new SingleThreadExecutor();
				PriorityThreadFactory threadFactory = new PriorityThreadFactory(
						"Single-thread",
						android.os.Process.THREAD_PRIORITY_BACKGROUND);

				instance.executor = Executors.newFixedThreadPool(1,
						threadFactory);
			}
			return instance;
		}
	}

	/**
	 * 执行
	 * 
	 * @param task
	 */
	public void execute(Runnable task) {
		instance.executor.execute(task);
	}
}
