
package com.prize.qihoo.cleandroid.sdk.plugins;

import android.content.Context;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.SDKEnv;
import com.prize.qihoo.cleandroid.sdk.update.UpdateImpl;
import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo360.mobilesafe.opti.env.clear.CloudQueryEnv;
import com.qihoo360.mobilesafe.opti.i.cloudquery.ICloudQuery;
import com.qihoo360.mobilesafe.opti.i.plugins.IUpdate;

/**
 * res/xml/clear_sdk_config.xml 中 clear_sdk_update=on ，必须有这个实现类，否则清理SDK无法使用；
 */
public class ClearSDKUpdateImpl implements IUpdate {
    public static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "ClearSDKUpdateImpl" : ClearSDKUpdateImpl.class.getSimpleName();

    private final Context mContext;

    private final UpdateImpl mUpdateImpl;

    private UpdateCallback mCallUpdateCallback;

    private long lastUpdate = 0;

    private boolean isUpdateing;

    public ClearSDKUpdateImpl(Context context) {
        mContext = context;
        mUpdateImpl = new UpdateImpl(mContext, mUpdateCallback);
    }

    @Override
    public void doUpdate(final UpdateCallback updateCallback) {
        if (DEBUG) {
            Log.d(TAG, "doUpdate start");
        }
        // 调用间隔大于5s
        if ((System.currentTimeMillis() - lastUpdate) < 5000) {
            if (SDKEnv.DEBUG) {
                Log.w(TAG, " just call return");
            }
            return;
        }

        if (isUpdateing) {
            if (SDKEnv.DEBUG) {
                Log.w(TAG, " isUpdateing return");
            }
            return;
        }

        lastUpdate = System.currentTimeMillis();
        isUpdateing = true;
        new Thread(new Runnable() {

            @Override
            public void run() {
                mCallUpdateCallback = updateCallback;
                // 数据升级
                mUpdateImpl.beginUpdate(true);
                // 云查询
                ICloudQuery cloudQuery = ClearModuleUtils.getCloudQueryImpl(mContext);
                final int resultCode = cloudQuery.cloudQuery(CloudQueryEnv.TYPE_PATH, null, null);
                if (SDKEnv.DEBUG) {
                    Log.i(TAG, " cloudQuery resultCode:" + resultCode);
                }
                isUpdateing = false;
                if (DEBUG) {
                    Log.d(TAG, "doUpdate end");
                }
            }
        }).start();
    }

    private final UpdateCallback mUpdateCallback = new UpdateCallback() {

        @Override
        public void onFinished(int resultCode) {
            if (mCallUpdateCallback != null) {
                if (SDKEnv.DEBUG) {
                    Log.i(TAG, " update resultCode:" + resultCode);
                }
                mCallUpdateCallback.onFinished(resultCode);
            }
        }
    };
}
