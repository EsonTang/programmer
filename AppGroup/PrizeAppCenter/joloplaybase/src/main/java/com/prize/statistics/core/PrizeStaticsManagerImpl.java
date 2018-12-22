package com.prize.statistics.core;

import android.app.ActivityManager;
import android.content.Context;

import com.prize.app.util.JLog;
import com.prize.statistics.db.utils.DataConstruct;
import com.prize.statistics.listener.HomeKeyEventBroadcastReceiver;
import com.prize.statistics.listener.ISaveLister;
import com.prize.statistics.listener.PrizeDateObserver;
import com.prize.statistics.listener.PrizeNetworkObserver;
import com.prize.statistics.listener.PrizeScreenObserver;
import com.prize.statistics.model.ExposureBean;
import com.prize.statistics.model.TcNote;

import java.util.List;
import java.util.Properties;

/**
 * longbaoxiu
 *  2016/12/17.15:13
 *
 */

public class PrizeStaticsManagerImpl implements StaticsManager {

    PrizeStaticsManagerImpl(final Context mContext) {
        final long time = System.currentTimeMillis();
        PrizeNetworkObserver paObserverPresenter = new PrizeNetworkObserver(mContext, new PrizeNetworkObserver.INetworkListener() {
            @Override
            public void onNetworkConnected(final Context aContext) {
                if ((System.currentTimeMillis() - time) < 5000)
                    return;
                if (!isNeedUpload(aContext))
                    return;
                StaSingleThreadExecutor.getInstance().submit(new Thread() {
                    @Override
                    public void run() {
                        PrizeStatSdk.getInstance(aContext).uploadAllExposureEven();
                        PrizeStatSdk.getInstance(aContext).uploadNewAllExposureEven();
                        PrizeStatSdk.getInstance(aContext).uploadNewDownEven();
                        PrizeStatSdk.getInstance(aContext).uploadDownEven();
                        PrizeStatSdk.getInstance(aContext).uploadAllEven();
                    }
                });

            }

            @Override
            public void onNetworkUnConnected(Context aContext) {

            }
        });
        paObserverPresenter.start();
        PrizeDateObserver mPrizeDateObserver = new PrizeDateObserver(mContext, new PrizeDateObserver.IDatekListener() {
            @Override
            public void onDateChange(final Context aContext) {
                if (!isNeedUpload(aContext))
                    return;
                StaSingleThreadExecutor.getInstance().execute(new Thread() {
                    @Override
                    public void run() {
                        PrizeStatSdk.getInstance(aContext).getServeTime();
                    }
                });
            }
        });
        mPrizeDateObserver.start();
        PrizeScreenObserver mPrizeScreenObserver = new PrizeScreenObserver(mContext, new PrizeScreenObserver.IScreenOffListener() {
            @Override
            public void onIScreenOff(final Context aContext) {
                if (!isNeedUpload(aContext)) {
                    return;
                }
                StaSingleThreadExecutor.getInstance().execute(new Thread() {
                    @Override
                    public void run() {
                        PrizeStatSdk.getInstance(aContext).uploadAllExposureEven();
                        PrizeStatSdk.getInstance(aContext).uploadNewAllExposureEven();
                        PrizeStatSdk.getInstance(aContext).uploadNewDownEven();
                        PrizeStatSdk.getInstance(aContext).uploadDownEven();
                        PrizeStatSdk.getInstance(aContext).uploadAllEven();
                    }
                });
            }

        });
        mPrizeScreenObserver.start();
        HomeKeyEventBroadcastReceiver mHomeKeyEventBroadcastReceiver = new HomeKeyEventBroadcastReceiver(mContext, new HomeKeyEventBroadcastReceiver.HomeKeyListener() {
            @Override
            public void onHomeKeyShortPressed() {
                JLog.i("PrizeStaticsManagerImpl", "onHomeKeyShortPressed");
                if (!isNeedUpload(mContext)) {
                    return;
                }
                StaSingleThreadExecutor.getInstance().execute(new Thread() {
                    @Override
                    public void run() {
                        PrizeStatSdk.getInstance(mContext).uploadAllExposureEven();
                        PrizeStatSdk.getInstance(mContext).uploadDownEven();
                        PrizeStatSdk.getInstance(mContext).uploadNewAllExposureEven();
                        PrizeStatSdk.getInstance(mContext).uploadNewDownEven();
                        PrizeStatSdk.getInstance(mContext).uploadAllEven();
                    }
                });
            }

            @Override
            public void onHomeKeyLongPressed() {
                JLog.i("PrizeStaticsManagerImpl", "onHomeKeyLongPressed");
            }
        });
        mHomeKeyEventBroadcastReceiver.start();
    }

    @Override
    public void onEventParameter(Properties p, final Context mContext,final boolean upLoadNow) {
        DataConstruct.onEvent(p, new ISaveLister() {
            @Override
            public void saveOk(final TcNote note) {
                if (upLoadNow&&isNeedUpload(mContext)) {
                    StaSingleThreadExecutor.getInstance().execute(new Thread() {
                        @Override
                        public void run() {
                            PrizeStatSdk.getInstance(mContext).upload360ClickEven(note);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDownEventParameter(Properties p, final Context mContext) {
        DataConstruct.onEvent(p, new ISaveLister() {
            @Override
            public void saveOk(final TcNote note) {
                if (isNeedUpload(mContext)) {
                    StaSingleThreadExecutor.getInstance().execute(new Thread() {
                        @Override
                        public void run() {
                            PrizeStatSdk.getInstance(mContext).uploadSingleDownEven(note);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onExposureParameter(List<ExposureBean> p, final Context mContext,final boolean upLoadNow) {
        DataConstruct.onExposure(p, new ISaveLister() {
            @Override
            public void saveOk(final TcNote note) {
                if (upLoadNow&&isNeedUpload(mContext)) {
                    StaSingleThreadExecutor.getInstance().execute(new Thread() {
                        @Override
                        public void run() {
                            PrizeStatSdk.getInstance(mContext).uploadSingleExposureEven(note);
                        }
                    });
                }
            }

        });
    }

    @Override
    public void onNewDown(ExposureBean p, final Context mContext) {
        DataConstruct.onNewDown(p, null);
    }
//        {
//            @Override
//            public void saveOk(final TcNote note) {
////                if (isNeedUpload(mContext)) {
////                    StaSingleThreadExecutor.getInstance().execute(new Thread() {
////                        @Override
////                        public void run() {
////                        }
////                    });
////                }
//            }
//
//        });
//    }

    @Override
    public void onInitEvent(String eventName) {
        DataConstruct.initEvent(eventName);
    }

    @Override
    public void onInitExposureEvent(String eventName) {
        DataConstruct.initExposureEvent(eventName);
    }


    /**
     * 多进程情况下，只有主进程才触发上传事件
     *
     * @param context Context
     * @return boolean
     */
    private boolean isNeedUpload(Context context) {
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager == null || mActivityManager.getRunningAppProcesses() == null)
            return false;
        try {
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    processName = appProcess.processName;
                    break;
                }
            }
            String packageName = context.getPackageName() + ":remote";
            if (JLog.isDebug) {
                JLog.i("PRIZE2016", "PrizeStaticsManagerImpl-isNeedUpload-appProcess.processName=" + processName + "--packageName=" + packageName);
            }
            return processName.equals(packageName);
        } catch (NullPointerException e) {
            e.printStackTrace();

        }
        return false;
    }

}
