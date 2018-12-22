package com.prize.app.threads;

import java.util.concurrent.atomic.AtomicInteger;

public class PriorityRunnable implements Runnable, Comparable<PriorityRunnable> {
	private static int DEFAULT_DECREASE = 10000;
	private static int basePriority = Integer.MAX_VALUE - DEFAULT_DECREASE;

	private static final AtomicInteger priorityFactory = new AtomicInteger();
	private int base = 0;
	private int priority = 1;

	public PriorityRunnable() {
		base = basePriority;
		priority = priorityFactory.incrementAndGet();
	}

	public static void decreaseBase() {
		basePriority -= DEFAULT_DECREASE;
	}

	public synchronized int getPriority() {
		return base + (priority % DEFAULT_DECREASE);
	}

	public int compare(PriorityRunnable o1, PriorityRunnable o2) {
		int r1 = o1.getPriority();
		int r2 = o2.getPriority();
		return r1 > r2 ? 1 : (r1 == r2 ? 0 : -1);
	}

	@Override
	public int compareTo(PriorityRunnable another) {
		int r1 = getPriority();
		int r2 = another.getPriority();
		return r1 > r2 ? 1 : (r1 == r2 ? 0 : -1);
	}

	@Override
	public void run() {
	}

}
