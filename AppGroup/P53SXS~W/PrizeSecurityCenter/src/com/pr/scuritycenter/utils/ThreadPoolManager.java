package com.pr.scuritycenter.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * open the thread pool
 * @author bxinhao
 * @version V1.0O
 */
public class ThreadPoolManager {
	private ExecutorService service;

	private ThreadPoolManager() {
		int num = Runtime.getRuntime().availableProcessors();
		service = Executors.newFixedThreadPool(num * 2);
	}

	private static ThreadPoolManager manager;

	public static ThreadPoolManager getInstance() {
		if (manager == null) {
			manager = new ThreadPoolManager();
		}
		return manager;
	}

	public void addTask(Runnable runnable) {
		service.submit(runnable);
	}

}
