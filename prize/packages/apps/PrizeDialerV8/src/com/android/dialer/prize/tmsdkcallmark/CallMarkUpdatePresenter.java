package com.android.dialer.prize.tmsdkcallmark;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CallMarkUpdatePresenter {

    private static final String TAG = "PrizeCallMarkUpdatePresenter";

    private UpdateManager mUpdateManager;
    private CheckResult mCheckResults;

    private static CallMarkUpdatePresenter mCallMarkUpdatePresenter;

    private CallMarkUpdatePresenter() {
    }

    public static CallMarkUpdatePresenter getInstance() {
        if (null == mCallMarkUpdatePresenter) {
            mCallMarkUpdatePresenter = new CallMarkUpdatePresenter();
        }
        return mCallMarkUpdatePresenter;
    }

    private void init() {
        if (null == mUpdateManager) {
            mUpdateManager = ManagerCreatorC.getManager(UpdateManager.class);
        }
    }

    public void check() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long flags = UpdateConfig.UPDATA_FLAG_NUM_MARK;

                //此接口不允许多用户同一时间更新，需要把时间打散，把随机种子时间作为参数传入，如果没有定时更新，传-1
                long randomTime = 2500;//务必重视次参数，否则影响使用
                init();
                mUpdateManager.check(flags, new ICheckListener() {
                    @Override
                    public void onCheckEvent(int arg0) {
                        Log.d(TAG, "check  network error, errorcode : " + arg0);
                    }

                    @Override
                    public void onCheckStarted() {

                    }

                    @Override
                    public void onCheckCanceled() {

                    }

                    @Override
                    public void onCheckFinished(CheckResult result) {
                        mCheckResults = result;
                        if (result != null) {
                            for (UpdateInfo info : result.mUpdateInfoList) {
                                Log.d(TAG, "check  updateinfo filename : " + info.fileName + ",  filesize : " + info.downSize + ",  url : " + info.url);
                            }
                        }
                        update();
                    }
                }, randomTime);
            }
        }).start();
    }

    public void update() {
        if (mCheckResults != null && mCheckResults.mUpdateInfoList != null
                && mCheckResults.mUpdateInfoList.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (null == mCheckResults) return;
                    init();
                    mUpdateManager.update(mCheckResults.mUpdateInfoList, new IUpdateListener() {
                        //更新
                        @Override
                        public void onProgressChanged(UpdateInfo arg0, int arg1) {
                            Log.d(TAG, "update  onProgressChanged, arg1 : " + arg1);
                        }

                        @Override
                        public void onUpdateEvent(UpdateInfo arg0, int arg1) {
                            Log.d(TAG, "update  network error, errorcode : " + arg1);
                        }

                        @Override
                        public void onUpdateFinished() {
                            Log.d(TAG, "update  onUpdateFinished");
                        }

                        @Override
                        public void onUpdateStarted() {
                            Log.d(TAG, "update  onUpdateStarted");
                        }

                        @SuppressWarnings("unused")
                        public void onUpdateCanceled() {
                            Log.d(TAG, "update  onUpdateCanceled");
                        }
                    });
                }
            }).start();
        }
    }

}
