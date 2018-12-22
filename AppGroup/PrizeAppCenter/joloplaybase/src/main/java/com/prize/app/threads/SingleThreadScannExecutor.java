package com.prize.app.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadScannExecutor {

    /*** 实例 */
    private static SingleThreadScannExecutor instance = null;

    /*** 单线程 */
    private ExecutorService executor = null;

    private SingleThreadScannExecutor() {
    }

    /**
     * 去实例
     *
     * @return
     */
    public static SingleThreadScannExecutor getInstance() {
        synchronized (SingleThreadScannExecutor.class) {
            if (instance == null) {
                instance = new SingleThreadScannExecutor();
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

    public void cancleAllTask() {
        if (executor != null&&isShutdown()) {
            executor.shutdown();
        }
    }

    private boolean isShutdown() {
        if (executor != null) {
            return executor.isShutdown();
        }
        return  false;
    }
}
