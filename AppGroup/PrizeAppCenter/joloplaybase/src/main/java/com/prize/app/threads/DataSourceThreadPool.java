package com.prize.app.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 **
 * 网络请求线程池
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DataSourceThreadPool {

	private ExecutorService executor;

	private final int MAX_NUM_POOL_SIZE = 3;

	private static DataSourceThreadPool instance;

	private DataSourceThreadPool() {
		PriorityThreadFactory threadFactory = new PriorityThreadFactory(
				"http-data", android.os.Process.THREAD_PRIORITY_BACKGROUND);
		executor = Executors.newFixedThreadPool(MAX_NUM_POOL_SIZE,
				threadFactory);
	}

	public static DataSourceThreadPool getInstance() {
		if (instance == null) {
			instance = new DataSourceThreadPool();
		}
		return instance;
	}

	public void execute(Runnable task) {
		if (executor != null && !executor.isShutdown()) {
			executor.execute(task);
		}
	}

	public void stop() {
		if (executor != null) {
			executor.shutdown();
			executor = null;
		}
	}

}
