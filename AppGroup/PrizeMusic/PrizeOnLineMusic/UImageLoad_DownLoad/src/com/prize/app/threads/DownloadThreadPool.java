package com.prize.app.threads;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadThreadPool {
	private static LinkedBlockingQueue<Runnable> queue;
	private static ThreadPoolExecutor downloadExecutor;

	public static ThreadPoolExecutor getDownloadThreadExe() {
		if (null == downloadExecutor) {
			queue = new LinkedBlockingQueue<Runnable>();
			downloadExecutor = new ThreadPoolExecutor(1, 10, 180,
					TimeUnit.SECONDS, queue);
		}

		return downloadExecutor;
	}
}
