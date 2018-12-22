package com.prize.music.admanager.statistics.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaSingleThreadExecutor {
//	private static SynchronousQueue<Runnable> queue;
	private static ExecutorService downloadExecutor;

	public static ExecutorService getInstance() {
//		if (null == downloadExecutor) {
//			queue = new SynchronousQueue<Runnable>();
//			downloadExecutor = new ThreadPoolExecutor(1, 1, 180,
//					TimeUnit.SECONDS, queue);
//
//		}
		if(downloadExecutor==null){
			synchronized (StaSingleThreadExecutor.class) {
				if(downloadExecutor==null){
					downloadExecutor=Executors.newSingleThreadExecutor();
				}
			}
		}
		return downloadExecutor;
	}

}
