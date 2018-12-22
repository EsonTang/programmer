package com.prize.smartcleaner;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import com.prize.smartcleaner.utils.LogUtils;

import com.prize.smartcleaner.utils.PrizeClearUtil;

/**
 * Created by xiarui on 2018/1/18.
 */

public class BootCompleteReceiver extends BroadcastReceiver {

    private Context mContext;
    public static final String TAG = PrizeClearSystemService.TAG;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext = context;
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            LogUtils.d(TAG, "BOOT COMPLETE BROADCAST RECEIVER!!");
            startClearSystemService();

            new Thread(new initFilterDataRunnable()).start();

            LogUtils.d(TAG, "supportMorningClear = " + PrizeClearSystemService.mSupportMorningClean);

            if (PrizeClearSystemService.mSupportMorningClean) {
                PrizeClearUtil.setMorningClearAlarm(context);
            }
        }
    }

    private void startClearSystemService() {
        LogUtils.d(TAG, "------startClearSystemService------");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.prize.smartcleaner", "com.prize.smartcleaner.PrizeClearSystemService"));
        mContext.startServiceAsUser(intent, UserHandle.CURRENT);
    }

    class initFilterDataRunnable implements Runnable {
        @Override
        public void run() {
            PrizeClearFilterManager.getInstance().initSysClearAppFilterList(mContext);
        }
    }
}
