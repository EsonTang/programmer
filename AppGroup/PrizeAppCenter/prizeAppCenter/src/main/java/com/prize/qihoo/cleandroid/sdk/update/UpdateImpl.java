
package com.prize.qihoo.cleandroid.sdk.update;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.SDKEnv;
import com.prize.qihoo.cleandroid.sdk.SharedPrefUtils;
import com.qihoo.antivirus.update.AppEnv;
import com.qihoo.antivirus.update.UpdateCommand;
import com.qihoo.cleandroid.sdk.utils.ClearModuleUtils;
import com.qihoo.cleandroid.sdk.utils.ClearSDKException;
import com.qihoo360.mobilesafe.opti.env.clear.SDKUpdateEnv;
import com.qihoo360.mobilesafe.opti.i.plugins.IUpdate.UpdateCallback;
import com.qihoo360.mobilesafe.opti.i.processclear.IProcessCleaner;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class UpdateImpl {
    public static final boolean DEBUG = SDKEnv.DEBUG;

    private static final String TAG = DEBUG ? "UpdateImpl" : UpdateImpl.class.getSimpleName();

    // notify id for update
    public static final int NOTIFY_UPDATE_PROG_ID = 360;

    public static final int NOTIFY_UPDATE_INFO_ID = 361;

    // 无新版本
    private static final int MSG_SHOW_UPDATE_NO_NEWVERSION = 4;

    // 显示包升级弹窗
    private static final int MSG_SHOW_UPDATE_PACKAGE_INFO = 5;

    // 联网错误
    private static final int MSG_SHOW_UPDATE_ERR = 6;

    // 引导用户进行Google Play评价
    private static final int MSG_WHAT_GOTO_MARKET_IF_NEED = 7;

    // 显示包安装弹窗
    private static final int MSG_SHOW_UPDATE_PACKAGE_INSTALL = 8;

    // 更新包升级进度
    private static final int MSG_SHOW_UPDATE_APP_PROGRESS = 9;

    // 更新数据升级进度
    private static final int MSG_SHOW_UPDATE_DATA_PROGRESS = 10;

    private static final String MSG_ERR_TYPE = "error_type";

    private static final String MSG_ERR_CODE = "error_code";

    public static final String ACTION_UPDATE_TRIGGER = "update_trigger";

    public static final String EXTRA_UI_UPDATE_TYPE = "ui_update_type";

    public static final String EXTRA_NOTIFY_PROGRESS_VALUE = "ui_prog_value";

    public static final String EXTRA_NOTIFY_PROGRESS_TYPE = "ui_prog_type";

    public static final String EXTRA_NOTIFY_ERROR_TYPE = "ui_err_type";

    public static final String EXTRA_NOTIFY_ERROR_CODE = "ui_err_code";

    // wifi-on,wifi-off
    public static final String EXTRA_ACTION_UPDATE_TYPE = "update_type";

    public static final String UPDATE_TYPE_WIFI_ON = "wifi_on";

    public static final String UPDATE_TYPE_WIFI_OFF = "wifi_off";

    public static final String UPDATE_TYPE_SCREEN_ON = "screen_on";

    public static final String ACTION_UPDATE_NOTIFY = "update_notify";

    public static final String ACTION_FORCE_CLOSE_MAIN_ACTIVITY = "update_close_ui";

    // ui notify message type
    public static final int UI_NOTIFY_DEFAULT = 0;

    // check update
    public static final int UI_NOTIFY_CHECK_UPDATE = 1;

    // update progress
    public static final int UI_NOTIFY_UPDATE_PROGRESS = 2;

    // update find error
    public static final int UI_NOTIFY_UPDATE_ERROR = 3;

    // show app update
    public static final int UI_NOTIFY_SHOW_UPDATE_INFO = 4;

    // not new version update
    public static final int UI_NOTIFY_NO_NEW_VERSION = 5;

    // close update screen window
    public static final int UI_NOTIFY_CLOSE_UPDATE_SCREEN = 6;

    // error type define
    // patch apk failed
    public static final int UI_NOTIFY_ERROR_PATCH = 10;

    // normal update error
    public static final int UI_NOTIFY_ERROR_UPDATE = 11;

    // update service error
    public static final int UI_NOTIFY_ERROR_SERVICE = 12;

    // progress type define
    public static final int UI_NOTIFY_PROGRESS_DATA = 13;

    public static final int UI_NOTIFY_PROGRESS_APK = 14;

    // last update time
    public static final String SP_LAST_CHECK_UPDATE_TIME = "update_last_time";

    // add by conyzhou begin:
    // update condition 8 hours trigger
    // add by conyzhou end:
    private static final int intervalUpdateTime = 8 * 1000 * 60 * 60;

    /** 自动更新清理数据库的时间间隔：24小时内只一次 */
    public static final long AUTO_UPDAE_CLEAR_DB_INTERVAL = 24 * 60 * 60 * 1000;

    /** 当前版本信息 */
    private String mPackageVersion;

    private Context mContext = null;

    // add tag for multi call for update
    private boolean mUpdatingState = false;

    // add notification progress support
    // private NotificationProgress mNotifyProgress = null;

    /**
     * 后台检查标志，后台升级检查时有24小弹窗逻辑
     */
    private boolean mAutoCheckUpdateState = true;

    private final Handler mHandler = new MyHandler(this, Looper.getMainLooper());

    private static class MyHandler extends Handler {
        private final WeakReference<UpdateImpl> mOuter;

        MyHandler(UpdateImpl obj, Looper looper) {
            super(looper);
            mOuter = new WeakReference<UpdateImpl>(obj);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            UpdateImpl obj = mOuter.get();
            if (obj == null) {
                return;
            }

            switch (msg.what) {
                case MSG_SHOW_UPDATE_NO_NEWVERSION:
                    // obj.notifyNoNewVersion(obj.mAutoCheckUpdateState);
                    // update ini get success remove statistics
                    // obj.resetClientLog();
                    // obj.onFinalUpdate();
                    break;
                case MSG_SHOW_UPDATE_PACKAGE_INSTALL:
                    // package download to local
                    // obj.handlePackageUpdateMsg(msg, true);
                    // obj.onFinalUpdate();
                    break;
                case MSG_SHOW_UPDATE_PACKAGE_INFO:
                    // package update info not download to local
                    // obj.handlePackageUpdateMsg(msg, false);
                    // obj.resetClientLog();
                    // if (obj.mAutoCheckUpdateState &&
                    // obj.isAutoDownloadOnWifi()) {
                    // // post message for auto update message
                    // for (int i = 0; i < 3; i++) {
                    // try {
                    // Thread.sleep(1000);
                    // } catch (InterruptedException e) {
                    // e.printStackTrace();
                    // }
                    // if (DEBUG) {
                    // Log.i(TAG, "try download app updaterunning=" +
                    // isUpdateRunning(obj.mContext));
                    // }
                    // if (!isUpdateRunning(obj.mContext)) {
                    // obj.beginUpdateApp(true, false);
                    // break;
                    // }
                    // }
                    // } else {
                    // obj.onFinalUpdate();
                    // }
                    break;
                case MSG_SHOW_UPDATE_ERR:
                    // obj.handleErrorMsg(msg);
                    break;
                case MSG_WHAT_GOTO_MARKET_IF_NEED:
                    // obj.showGoogleMarketVote();
                    break;
                case MSG_SHOW_UPDATE_APP_PROGRESS:
                    // obj.handleUpdateProgress(msg, UI_NOTIFY_PROGRESS_APK);
                    break;
                case MSG_SHOW_UPDATE_DATA_PROGRESS:
                    // obj.handleUpdateProgress(msg, UI_NOTIFY_PROGRESS_DATA);
                    break;
                default:
                    break;
            }
        }
    };

    // update receiver action here
    BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String product = intent.getStringExtra(AppEnv.EXTRA_BROADCAST_PRODUCT);
                if (DEBUG) {
                    Log.v(TAG, "onReceive:" + product + " " + action);
                }
                // 检查是否是清理SDK升级

                if (SDKEnv.sIsMultilang) {
                    if (!SDKUpdateEnv.PRODUCT_MULTILANG.equals(product)) {
                        return;
                    }
                } else {
                    if (!SDKUpdateEnv.PRODUCT_CN.equals(product)) {
                        return;
                    }
                }

                if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_INSTALL_NOTICE)) {
                    // send message for show apk download success
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_PACKAGE_INSTALL);
                    Bundle bundle = msg.getData();
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE));
                    if (DEBUG) {
                        Log.i(TAG,
                                "onReceive ACTION_INSTALL_NOTICE EXTRA_APP_PATH = " + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH) + " EXTRA_APP_VERSION = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION) + " EXTRA_APP_FORCE_UPDATE = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE) + " updating=" + mUpdatingState);
                    }
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_NOTICE)) {
                    // send message for show apk update ui
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_PACKAGE_INFO);
                    Bundle bundle = msg.getData();
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATCH_SIZE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATCH_SIZE));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_SIZE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_SIZE));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE));
                    if (DEBUG) {
                        Log.i(TAG,
                                "onReceive ACTION_UPDATE_OVER EXTRA_APP_PATCH_SIZE = " + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATCH_SIZE) + " EXTRA_APP_SIZE = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_SIZE) + " EXTRA_APP_VERSION = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION) + " EXTRA_APP_FORCE_UPDATE = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE) + " updating=" + mUpdatingState);
                    }
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_OVER)) {
                    // send message for show apk download success
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_PACKAGE_INSTALL);
                    Bundle bundle = msg.getData();
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_DESCRIPTION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION));
                    bundle.putString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE));
                    if (DEBUG) {
                        Log.i(TAG,
                                "onReceive ACTION_UPDATE_OVER EXTRA_APP_PATH = " + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_PATH) + " EXTRA_APP_VERSION = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_VERSION) + " EXTRA_APP_FORCE_UPDATE = "
                                        + bundle.getString(com.qihoo.antivirus.update.AppEnv.EXTRA_APP_FORCE_UPDATE) + " updating=" + mUpdatingState);
                    }
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_CHECK_OVER)) {
                    // send message for show apk download success
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_NO_NEWVERSION);
                    if (DEBUG) {
                        Log.i(TAG, "onReceive MSG_SHOW_UPDATE_NO_NEWVERSION");
                    }
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_PATCH_FILE_NOTIFY)) {
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_PATCH_FILE_NOTIFY action" + action);
                    }

                    // String vdat_filename =
                    // intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_TARGET_NAME);
                    // String patch_filename =
                    // intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_PATCH_NAME);
                    // int flags =
                    // intent.getIntExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_FLAG,
                    // -1);
                    int version = intent.getIntExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_VERSION, -1);
                    int patch_type = intent.getIntExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_PATCH_TYPE, -1);
                    if (patch_type != 0 && patch_type != 1) {
                        return;
                    }
                    if (version == -1) {
                        return;
                    }
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_ERROR)) {

                    String errorCode = intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_ERROR_CODE);
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_ERROR error = " + errorCode + " updating=" + mUpdatingState);
                    }

                    boolean retryDownload = false;
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_ERR);
                    Bundle bundle = msg.getData();
                    bundle.putString(MSG_ERR_CODE, intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_ERROR_CODE));
                    bundle.putInt(MSG_ERR_TYPE, (retryDownload) ? UI_NOTIFY_ERROR_UPDATE : UI_NOTIFY_ERROR_SERVICE);
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                    // 差量包合并失败
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_APK_PATCH_ERROR)) {
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_APK_PATCH_ERROR action" + action + " updating=" + mUpdatingState);
                    }
                    // send error message cancel upate ui
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_ERR);
                    Bundle bundle = msg.getData();
                    bundle.putInt(MSG_ERR_TYPE, UI_NOTIFY_ERROR_PATCH);
                    mUpdatingState = false;
                    mHandler.sendMessage(msg);
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATED_FILE_NOTIFY)) {
                    String filePath = intent.getStringExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_VDATA_TARGET_NAME);
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_UPDATED_FILE_NOTIFY filePath = " + filePath);
                    }
                    File file = new File(filePath);
                    String fileName = file.getName();
                    long assetstime = UpdateUtils.getBundleTimestamp(mContext, fileName);
                    UpdateUtils.setFileTimestamp(mContext, fileName, assetstime + 1);

                    // 重新加载数据文件
                    if (fileName.equals("o_c_spf.dat")) {
                        IProcessCleaner processCleaner = ClearModuleUtils.getProcessCleanerImpl(mContext);
                        if (processCleaner != null) {
                            processCleaner.init(mContext, false);
                            processCleaner.updateConfigure();
                            if (DEBUG) {
                                Log.i(TAG, "reload configure o_c_spf.dat");
                            }
                        }
                    }
                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_APP_PROGRESS)) {
                    // manual update send update progress messsage
                    long currntSize = intent.getLongExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_CURRENT, 0);
                    long totalSize = intent.getLongExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_TOTAL, 0);
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_APP_PROGRESS current = " + currntSize + " total = " + totalSize + " autoupdate=" + mAutoCheckUpdateState);
                    }
                    // send error message cancel upate ui
                    Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_APP_PROGRESS);
                    Bundle bundle = msg.getData();
                    bundle.putLong(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_CURRENT, currntSize);
                    bundle.putLong(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_TOTAL, totalSize);
                    mHandler.sendMessage(msg);

                } else if (action.equals(com.qihoo.antivirus.update.AppEnv.ACTION_DATA_FILE_PROGRESS)) {
                    // manual update send update progress messsage
                    long currntSize = intent.getLongExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_CURRENT, 0);
                    long totalSize = intent.getLongExtra(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_TOTAL, 0);
                    if (DEBUG) {
                        Log.i(TAG, "onReceive ACTION_DATA_FILE_PROGRESS current = " + currntSize + " total = " + totalSize + " autoupdate=" + mAutoCheckUpdateState);
                    }
                    if (!mAutoCheckUpdateState) {
                        // send error message cancel upate ui
                        Message msg = mHandler.obtainMessage(MSG_SHOW_UPDATE_DATA_PROGRESS);
                        Bundle bundle = msg.getData();
                        bundle.putLong(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_CURRENT, currntSize);
                        bundle.putLong(com.qihoo.antivirus.update.AppEnv.EXTRA_PROGRESS_TOTAL, totalSize);
                        mHandler.sendMessage(msg);
                    }
                } else {
                    if (DEBUG) {
                        Log.i(TAG, "onReceive unhandle action" + action);
                    }
                }
            }
        }
    };

    private final UpdateCallback mUpdateCallback;

    public UpdateImpl(Context context, UpdateCallback updateCallback) {
        mContext = context;
        mUpdateCallback = updateCallback;
        if (DEBUG) {
            Log.i(TAG, "UpdateImpl ctor register update receiver");
        }
        registerUpdateReceiver();
    }

    // add by conyzhou begin:
    // stop update service process when not update package
    // add by conyzhou end:
    public void forceStopUpdate() {
        if (DEBUG) {
            Log.d(TAG, "[update]forceStopUpdate=" + mAutoCheckUpdateState + " mUpdatingState=" + mUpdatingState);
        }
        UpdateCommand.stopUpdate(mContext, SDKEnv.sIsMultilang ? SDKUpdateEnv.PRODUCT_MULTILANG : SDKUpdateEnv.PRODUCT_CN, AppEnv.UPDATE_CANCEL);
    }

    // add by conyzhou begin:
    // add update support used v5
    // add by conyzhou end:
    private void registerUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        // add by conyzhou begin:
        // register unused receiver
        // add by conyzhou end:
        // 准备下载数据文件
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_DATA_FILE_DOWNLOAD_INI);
        // 开始下载数据文件
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_DATA_FILE_DOWNLOAD_BEGIN);
        // 数据文件下载完成
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_DATA_FILE_DOWNLOAD_END);
        // 数据文件下载进度
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_DATA_FILE_PROGRESS);
        // 包文件下载进度
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_APP_PROGRESS);
        // 联网失败，重试中
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_CONNECT_RETRY);
        // 升级失败
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_ERROR);
        // 差量包合并失败
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_APK_PATCH_ERROR);

        // 有新版本升级（未下载到本地）
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_NOTICE);
        // 有新版本升级(已经下载到本地)
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_INSTALL_NOTICE);
        // 数据文件更新完成
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATED_FILE_NOTIFY);
        // 数据文件需要合并
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_PATCH_FILE_NOTIFY);
        // 包文件下载完成
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_OVER);
        // 升级检查完毕(只有数据文件升级没有包文件升级)
        filter.addAction(com.qihoo.antivirus.update.AppEnv.ACTION_UPDATE_CHECK_OVER);

        if (null != mContext && null != mUpdateReceiver) {
            if (DEBUG) {
                Log.i(TAG, "registerUpdateReceiver");
            }
            mContext.registerReceiver(mUpdateReceiver, filter, SDKEnv.UPDATE_PERMISSION, null);
        }
    }

    private void unRegisterUpdateReceiver() {
        if (null != mContext && null != mUpdateReceiver) {
            if (DEBUG) {
                Log.i(TAG, "unRegisterUpdateReceiver");
            }
            try {
                mContext.unregisterReceiver(mUpdateReceiver);
            } catch (Exception e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    // if need begin update query now 8 hours
    public static boolean isNeedUpdateNow(Context context, boolean needUpdate) {
        // hour or half hour not check update
        try {
            Calendar rightNow = Calendar.getInstance();
            int m = rightNow.get(Calendar.MINUTE);
            if ((58 <= m) || (m < 3) || ((28 <= m) && (m < 33))) {
                if (DEBUG) {
                    Log.d(TAG, "isNeedUpdateNow not update at:" + m);
                }
                return false;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.e(TAG, "isNeedUpdateNow error=" + e.getMessage(), e);
            }
        }
        // 8 hours check update url
        String s = SharedPrefUtils.getString(context, SP_LAST_CHECK_UPDATE_TIME, "");
        long nowTime = System.currentTimeMillis();
        long lastCheckUpdateTime = 0;
        boolean firstUpdate = TextUtils.isEmpty(s);
        try {
            lastCheckUpdateTime = s != null ? Long.parseLong(s) : 0;
        } catch (Exception e) {
            lastCheckUpdateTime = nowTime;
        }
        long deltaTime = (nowTime > lastCheckUpdateTime) ? (nowTime - lastCheckUpdateTime) : (lastCheckUpdateTime - nowTime);
        if (DEBUG) {
            Log.i(TAG, "isNeedUpdateNow delta= " + deltaTime + " firstUpdate=" + firstUpdate + " showAlert=" + (deltaTime > intervalUpdateTime) + " needUpdate=" + needUpdate);
        }
        if (deltaTime > intervalUpdateTime || firstUpdate) {
            // if(deltaTime>200 || firstUpdate){
            // update check last time
            if (DEBUG) {
                Log.d(TAG, "isNeedUpdateNow updateLastTime=" + nowTime + " firstUpdate=" + firstUpdate);
            }
            if (needUpdate) {
                SharedPrefUtils.setString(context, SP_LAST_CHECK_UPDATE_TIME, String.valueOf(nowTime));
            }
            return true;
        }
        return false;
    }

    public void stopUpdate() {
        if (DEBUG) {
            Log.d(TAG, "stopUpdate mUpdatingState=" + mUpdatingState + " autoUpdate=" + mAutoCheckUpdateState);
        }
        UpdateCommand.stopUpdate(mContext, SDKEnv.sIsMultilang ? SDKUpdateEnv.PRODUCT_MULTILANG : SDKUpdateEnv.PRODUCT_CN, AppEnv.UPDATE_CANCEL);
        // send stop message to update screen
        // unregister reciever
        mUpdatingState = false;
        unRegisterUpdateReceiver();
    }

    // start update
    /**
     * 如果是静默升级，SDK内部将会进行如下检查： SDK会检查是否联网，手动升级不会检查是否联网。
     * SDK会检查升级时间，如果本次升级时间距离上次升级时间的间隔小于8小时，禁止本次静默升级。
     * SDK会检查升级时间，如果本次升级时间在整点或者半点的5分钟区间内，禁止本次静默升级。
     *
     * @param autoUpdate 是否静默升级
     */
    public void beginUpdate(boolean autoUpdate) {
        if (DEBUG) {
            Log.i(TAG, "beginUpdate autoUpdate=" + autoUpdate);
        }
        mAutoCheckUpdateState = autoUpdate;
        if (mUpdatingState) {
            // if update with menu show updating warning dialog
            if (DEBUG) {
                Log.d(TAG, "beginUpdate mUpdatingState=" + mUpdatingState + " autoUpdate=" + autoUpdate);
            }
            return;
        }
        mUpdatingState = true;

        // 清理SDK固定参数 sdkid=2，product=sdkclear
        HashMap<String, String> updateParam = new HashMap<String, String>();
        updateParam.put("sdkid", SDKUpdateEnv.ID);
        updateParam.put("cid", ClearModuleUtils.getXmlConfigValue(mContext, "cid_config", "sdk", "cid"));
        updateParam.put(com.qihoo.antivirus.update.AppEnv.UPDATE_REQ_PRODUCT, SDKEnv.sIsMultilang ? SDKUpdateEnv.PRODUCT_MULTILANG : SDKUpdateEnv.PRODUCT_CN);
        updateParam.put(com.qihoo.antivirus.update.AppEnv.UPDATE_REQ_PERMISSION, SDKEnv.UPDATE_PERMISSION);
        if (SDKEnv.sIsMultilang) {
            updateParam.put(com.qihoo.antivirus.update.AppEnv.UPDATE_REQ_SERVER, SDKUpdateEnv.SERVER_URL_MULTILANG);
        }

        // add support for error version test
        if (mPackageVersion == null) {
            try {
                mPackageVersion = ClearModuleUtils.getClearModulel(mContext).getSDKVersionName();
            } catch (ClearSDKException e) {
                e.printStackTrace();
            }
        }

        String strAppVersion = mPackageVersion;
        if (DEBUG) {
            strAppVersion = SharedPrefUtils.getString(mContext, "appver", "");
            if (TextUtils.isEmpty(strAppVersion)) {
                strAppVersion = mPackageVersion;
            }
            Log.i(TAG, "beginUpdate hackversion=" + strAppVersion + " autoUpdate=" + mAutoCheckUpdateState + " updateParam:" + updateParam);
        }

        int result = UpdateCommand.startUpdate(mContext, com.qihoo.antivirus.update.AppEnv.UPDATE_TYPE_USERCLICK, strAppVersion,
                updateParam);

        if (result == com.qihoo.antivirus.update.AppEnv.OK) {
            if (DEBUG) {
                Log.d(TAG, "beginUpdate startUpdate");
            }
            if (mUpdateCallback != null) {
                // 数据更新接口调用成功
                mUpdateCallback.onFinished(1);
            }
        } else if (result == com.qihoo.antivirus.update.AppEnv.NO_NETWORK) {
            mUpdatingState = false;
            // if update with menu show updating warning dialog
            if (DEBUG) {
                Log.d(TAG, "beginUpdate startUpdate=NO_NETWORK");
            }
        } else if (result == com.qihoo.antivirus.update.AppEnv.UPDATING) {
            // if update with menu show updating warning dialog
            mUpdatingState = false;
            if (DEBUG) {
                Log.d(TAG, "beginUpdate startUpdate=UPDATING");
            }
        } else if (result == com.qihoo.antivirus.update.AppEnv.PARAM_ERROR) {
            mUpdatingState = false;
            if (DEBUG) {
                Log.d(TAG, "beginUpdate startUpdate=UPDATING");
            }
        }
    }

    public static boolean isUpdateRunning(Context context) {
        if (DEBUG) {
            Log.i(TAG, "isUpdateRunning running=" + isUpdateProcessRunning(context));
        }
        return isUpdateProcessRunning(context);
    }

    public static boolean isUpdateProcessRunning(Context c) {
        String my = c.getApplicationInfo().packageName + ":update";

        ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> list = manager.getRunningAppProcesses();
        for (RunningAppProcessInfo p : list) {
            String processName = p.processName;
            if (processName.indexOf(my) != -1) {
                return true;
            }
        }
        return false;
    }
}
