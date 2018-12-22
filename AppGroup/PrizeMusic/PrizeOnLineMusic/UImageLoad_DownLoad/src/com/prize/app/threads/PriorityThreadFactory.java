package com.prize.app.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: PriorityThreadFactory
 * @Description: A thread factory that creates threads with a given thread
 *               priority.
 * @author prize
 */
public class PriorityThreadFactory implements ThreadFactory {

	private final int mPriority;
	private final AtomicInteger mNumber = new AtomicInteger();
	private final String mName;

	public PriorityThreadFactory(String name, int priority) {
		mName = name;
		mPriority = priority;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, mName + '-' + mNumber.getAndIncrement()) {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(mPriority);
				super.run();
			}
		};
	}
}
