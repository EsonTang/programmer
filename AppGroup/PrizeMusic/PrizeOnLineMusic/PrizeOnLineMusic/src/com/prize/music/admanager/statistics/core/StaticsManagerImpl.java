package com.prize.music.admanager.statistics.core;


import java.util.Properties;

import com.prize.music.admanager.presenter.JLog;
import com.prize.music.admanager.statistics.db.utils.DataConstruct;
import com.prize.music.admanager.statistics.listener.DateObserver;
import com.prize.music.admanager.statistics.listener.ISaveLister;
import com.prize.music.admanager.statistics.listener.NetworkObserver;
import com.prize.music.admanager.statistics.listener.ScreenObserver;
import com.prize.music.admanager.statistics.model.TcNote;

import android.content.Context;

/**
 * @创建者 longbaoxiu
 * @创建者 2016/12/17.15:13
 * @描述
 */

public class StaticsManagerImpl implements StaticsManager {
    /**
     * context
     */
    private        Context         mContext;
    private static NetworkObserver paObserverPresenter;
    private static DateObserver    mPrizeDateObserver;
    private static ScreenObserver  mScreenObserver;


    public StaticsManagerImpl(Context mContext) {
        this.mContext = mContext;
        final long time = System.currentTimeMillis();
        paObserverPresenter = new NetworkObserver(mContext, new NetworkObserver.INetworkListener() {
            @Override
            public void onNetworkConnected(final Context aContext) {
                if ((System.currentTimeMillis() - time) < 5000)
                    return;
                if (!isNeedUpload(aContext))
                    return;
                StaSingleThreadExecutor.getInstance().submit(new Thread() {
                    @Override
                    public void run() {
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeNetworkObserver正在执行。。。");
                        StatSdk.getInstance(aContext).uploadAllEven();
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeNetworkObserver结束。。。");
                    }
                });

            }

            @Override
            public void onNetworkUnConnected(Context aContext) {

            }
        });
        paObserverPresenter.start();
        mPrizeDateObserver = new DateObserver(mContext, new DateObserver.IDatekListener() {
            @Override
            public void onDateChange(final Context aContext) {
                if (!isNeedUpload(aContext))
                    return;
                StaSingleThreadExecutor.getInstance().execute(new Thread() {
                    @Override
                    public void run() {
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeDateObserver正在执行。。。");
                        StatSdk.getInstance(aContext).getServeTime(aContext);
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeDateObserver结束。。。");
                    }
                });
            }
        });
        mPrizeDateObserver.start();
        mScreenObserver = new ScreenObserver(mContext, new ScreenObserver.IScreenOffListener() {
            @Override
            public void onIScreenOff(final Context aContext) {
                if (!isNeedUpload(aContext)) {
                    return;
                }
                StaSingleThreadExecutor.getInstance().execute(new Thread() {
                    @Override
                    public void run() {
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeScreenObserver正在执行。。。");
                        StatSdk.getInstance(aContext).uploadAllEven();
                        JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeScreenObserver执行完成。。。");
                    }
                });
            }

        });
        mScreenObserver.start();
    }

    @Override
    public void onEventParameter(Properties p, final Context mContext) {
        DataConstruct.onEvent(p, mContext, new ISaveLister() {
            @Override
            public void saveOk(final TcNote note) {
                if (isNeedUpload(mContext)) {
                    StaSingleThreadExecutor.getInstance().execute(new Thread() {
                        @Override
                        public void run() {
                            JLog.i("PRIZE2016", Thread.currentThread().getName() + "----saveOk正在执行。。。");
                            StatSdk.getInstance(mContext).uploadSingleEven(note);
                            JLog.i("PRIZE2016", Thread.currentThread().getName() + "---saveOk完成。。。");
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onInitEvent(String eventName) {
        DataConstruct.initEvent(eventName);
    }

    /**
     * 多进程情况下，只有主进程才触发上传事件
     *
     * @param context
     * @return
     */
    private boolean isNeedUpload(Context context) {
        return true;
//        int pid = android.os.Process.myPid();
//        String processName = "";
//        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
//            if (appProcess.pid == pid) {
//                processName = appProcess.processName;
//                break;
//            }
//        }
//        String packageName = context.getPackageName() + ":remote";
//        JLog.i("PRIZE2016", "isNeedUpload-appProcess.processName=" + processName + "--packageName=" + packageName);
//
//        if (processName.equals(packageName)) {
//            return true;
//        } else {
//            return false;
//        }

    }

    //    class TaskThread extends Thread{
    //        private boolean isAll;
    //        private TcNote note;
    //
    //        public TaskThread( boolean isAll,TcNote note){
    //            this.isAll = isAll;
    //            this.note = note;
    //        }
    //
    //        @Override
    //        public void run() {
    //            if(isAll){
    //                JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeScreenObserver正在执行。。。");
    ////                        PrizeStatSdk.getInstance(aContext).uploadAllEven();
    //                PrizeStatSdk.getInstance(mContext).uploadSingleEven(true,null);
    //                JLog.i("PRIZE2016", Thread.currentThread().getName() + "--PrizeScreenObserver执行完成。。。");
    //            }else{
    //                JLog.i("PRIZE2016", Thread.currentThread().getName() + "----saveOk正在执行。。。");
    //                PrizeStatSdk.getInstance(mContext).uploadSingleEven(false,note);
    //                JLog.i("PRIZE2016", Thread.currentThread().getName() + "---saveOk完成。。。");
    //            }
    //        }
    //    }
}
