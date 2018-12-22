package com.prize.prizethemecenter.ui.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @更新人: $Author: bxh $
 * @更新时间: $Date: 2016年9月8日14:50:37
 * @更新描述: TODO
 */
public class ThreadPoolProxy {
    private ThreadPoolExecutor mExecutor;
    private int mCorePoolSize = 3;    // 核心线程数
    private int mMaximumPoolSize = 5;    // 最大线程数
    private long mKeepAliveTime = 5000; // 存活时间

    public ThreadPoolProxy(int coreSize, int maxSize, long keepAlive) {
        this.mCorePoolSize = coreSize;
        this.mMaximumPoolSize = maxSize;
        this.mKeepAliveTime = keepAlive;
    }

    /**
     * 执行任务
     *
     * @param task
     */
    public void execute(Runnable task) {
        if (task == null) {
            return;
        }

        checkPool();

        mExecutor.execute(task);
    }

    /**
     * 执行任务
     *
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        if (task == null) {
            return null;
        }

        checkPool();

        return mExecutor.submit(task);
    }

    /**
     * 移除任务
     *
     * @param task
     */
    public void remove(Runnable task) {
        if (mExecutor != null) {
            mExecutor.getQueue().remove(task);
        }
    }

    private void checkPool() {
        if (mExecutor == null || mExecutor.isShutdown()) {


            TimeUnit unit = TimeUnit.MILLISECONDS;// 时间单位--> 存活时间
            BlockingQueue<Runnable> workQueue = null;// 任务队列

            // workQueue = new ArrayBlockingQueue<Runnable>(3);// capacity :
            // // 任务队列的大小最大为多少

            workQueue = new LinkedBlockingQueue<Runnable>();// 不固定大小

            // workQueue = new SynchronousQueue<Runnable>();

            ThreadFactory threadFactory = Executors.defaultThreadFactory();// 线程工厂
            RejectedExecutionHandler handler = null;// 错误捕获器
            handler = new ThreadPoolExecutor.DiscardPolicy();// 空实现

            mExecutor = new ThreadPoolExecutor(mCorePoolSize,
                    mMaximumPoolSize,
                    mKeepAliveTime,
                    unit,
                    workQueue,
                    threadFactory,
                    handler);

        }
    }


}
