package com.android.launcher3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQLSingleThreadExcutor {

	/*** 实例 */
	private static SQLSingleThreadExcutor instance = null;

	/*** 单线程 */
	private ExecutorService executor = null;

	private SQLSingleThreadExcutor() {
	}

	/**
	 * 去实例
	 * 
	 * @return
	 */
	public static SQLSingleThreadExcutor getInstance() {
		synchronized (SQLSingleThreadExcutor.class) {
			if (instance == null) {
				instance = new SQLSingleThreadExcutor();
				PriorityThreadFactory threadFactory = new PriorityThreadFactory(
						"SQL-Single-thread",
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
