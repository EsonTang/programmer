package com.prize.statistics.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaSingleThreadExecutor {
//	private static SynchronousQueue<Runnable> queue;
	private static ExecutorService downloadExecutor;

	public static ExecutorService getInstance() {
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
