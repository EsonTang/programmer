package com.prize.prizethemecenter.activity;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.AppsItemBean;
import com.prize.prizethemecenter.fragment.PromptDialogFragment;
import com.prize.prizethemecenter.manage.AppManagerCenter;
import com.prize.prizethemecenter.ui.actionbar.ActionBarNoTabActivity;
import com.prize.prizethemecenter.ui.utils.StateBarUtils;
import com.prize.prizethemecenter.ui.utils.ToastUtils;
import com.prize.prizethemecenter.ui.utils.WindowMangerUtils;
import com.prize.prizethemecenter.ui.widget.UpdateSelfDialog;
import com.tencent.android.tpush.XGPushManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.io.File;

/**
 * Created by pengy on 2016/8/31.
 * setting
 */
public class SettingActivity extends ActionBarNoTabActivity implements View.OnClickListener {

    private static final String TAG = "SettingActivity";
    /**
     * 自动换主题
     **/
    private Switch theme_change_auto_sw;
    /**
     * 流量下载
     **/
    private Switch traffic_download_auto_sw;
    /**
     * 允许通知
     **/
    private Switch receiver_notification_sw;
    /**
     * 清除缓存
     **/
    private RelativeLayout clear_cache_set_RL;
    private RelativeLayout mVersionRl;

    private PromptDialogFragment df;

    private TextView cache_size;

    private Handler mHandler;

    private TextView versionId;

    private TextView payUseIDTV;

    private Callback.Cancelable reqHandler;

    private DownloadManager downloadManager;

    private UpdateSelfDialog mUpdateSelfDialog;
    private boolean isNeedTip = false;
    private AppsItemBean mAppsItemBean;
    private boolean isRequestOk = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.setting_layout);
        StateBarUtils.changeStatus(getWindow());
        setTitle(R.string.setting);
        initView();
        setListener();
        init();
        mHandler = new Handler(Looper.getMainLooper(), callback);
        mHandler.post(mBackgroundRunnable);// 将线程post到Handler中
    }

    public void initView() {
//        theme_change_auto_sw = (Switch) findViewById(R.id.theme_change_auto_sw);
        traffic_download_auto_sw = (Switch) findViewById(R.id.traffic_download_auto_sw);
        receiver_notification_sw = (Switch) findViewById(R.id.receiver_notification_sw);
        clear_cache_set_RL = (RelativeLayout) findViewById(R.id.clear_cache_set_RL);
        mVersionRl = (RelativeLayout) findViewById(R.id.version_rl);
        cache_size = (TextView) findViewById(R.id.cache_size);
        versionId = (TextView) findViewById(R.id.version_Id);
        findViewById(R.id.bar_search).setVisibility(View.GONE);
//         payUseIDTV = (TextView) findViewById(R.id.pay_use_id_TV);

        //设置易接支付用户ID
//        APaymentUnity.getUserId(this);
//        long userId = APaymentUnity.getUserId(this);
//        String showext = String.valueOf(userId);
//        payUseIDTV.setText("用户ID："+showText);

        isPush();
    }

    private void isPush() {
        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.SWITCH_PUSH_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            receiver_notification_sw.setChecked(false);
        } else {
            receiver_notification_sw.setChecked(true);
        }
    }

    private void setListener() {
        clear_cache_set_RL.setOnClickListener(this);
        mVersionRl.setOnClickListener(this);
//        theme_change_auto_sw.setOnCheckedChangeListener(onCheckedChangeListener);
        traffic_download_auto_sw.setOnCheckedChangeListener(onCheckedChangeListener);
        receiver_notification_sw.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    private void init() {
        // 自动换主题
//        String auto_change_theme = DataStoreUtils
//                .readLocalInfo(DataStoreUtils.AUTO_CHANGE_THEME);
//        if (DataStoreUtils.CHECK_OFF.equals(auto_change_theme)) {
//            theme_change_auto_sw.setChecked(false);
//        } else {
//            theme_change_auto_sw.setChecked(true);
//        }

        // 流量下载
        String traffic_download = DataStoreUtils
                .readLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD);
        if (DataStoreUtils.CHECK_OFF.equals(traffic_download)) {
            traffic_download_auto_sw.setChecked(false);
        } else {
            traffic_download_auto_sw.setChecked(true);
        }

        // 允许通知
        String receive_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(receive_notification)) {
            receiver_notification_sw.setChecked(false);
        } else {
            receiver_notification_sw.setChecked(true);
        }

        versionId.setText(getString(R.string.set_about_version,
                ClientInfo.getInstance().appVersionCode));
    }

    CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
//                case R.id.theme_change_auto_sw:
//                    ThemeAutoChange(isChecked);
//                    break;
                case R.id.traffic_download_auto_sw:
                    TrafficDownload(isChecked);
                    break;
                case R.id.receiver_notification_sw:
                    ReceiveNotification(isChecked);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     *
     * 推送设置
     *
     * @param isCheck
     *            默认false
     */
    private void pushNotifiacationSwitch(boolean isCheck) {
        // 自动下载更新包设置
        if (!isCheck) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(
                    DataStoreUtils.SWITCH_PUSH_NOTIFICATION,
                    DataStoreUtils.CHECK_OFF);
        } else {
            DataStoreUtils.saveLocalInfo(
                    DataStoreUtils.SWITCH_PUSH_NOTIFICATION,
                    DataStoreUtils.CHECK_ON);
        }
    }
    private void ReceiveNotification(boolean isChecked) {
        if (!isChecked) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION,
                    DataStoreUtils.CHECK_OFF);
            XGPushManager.unregisterPush(getApplicationContext());
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.RECEIVE_NOTIFICATION,
                    DataStoreUtils.CHECK_ON);
            XGPushManager.registerPush(getApplicationContext());
        }
    }

    /**
     *
     * 移动数据下载
     *
     * @param isChecked
     */
    private void TrafficDownload(boolean isChecked) {
        if (!isChecked) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD,
                    DataStoreUtils.CHECK_OFF);
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.TRAFFIC_DOWNLOAD,
                    DataStoreUtils.CHECK_ON);
        }
    }

    protected void ThemeAutoChange(boolean isChecked) {
        if (!isChecked) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_CHANGE_THEME,
                    DataStoreUtils.CHECK_OFF);
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_CHANGE_THEME,
                    DataStoreUtils.CHECK_ON);
//            Intent intent = new Intent(this, AutoChangeThemeService.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.setAction(AutoChangeThemeService.AUTO_CHANGE_THEME_ACTION);
//            startService(intent);
        }
    }

    /**
     * 删除提示对话框
     */
    private View.OnClickListener mDeletePromptListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            df.dismissAllowingStateLoss();
            new AsyncLoader_GuessInfo().execute();
        }
    };

    class AsyncLoader_GuessInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            File appCacheDir = StorageUtils
                    .getCacheDirectory(getApplicationContext());
            if (!appCacheDir.exists()) {
                return false;
            }
            return FileUtils.recursionDeleteFile(appCacheDir);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                changeState();
                ToastUtils.showToast(R.string.cache_clear_ok);
            }
        }
    }

    private void changeState() {
        cache_size.setVisibility(View.GONE);
        clear_cache_set_RL.setEnabled(false);
        clear_cache_set_RL.setClickable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_cache_set_RL:
                if (df == null || !df.isAdded()) {
                    df = PromptDialogFragment.newInstance(getString(R.string.clean_cache),
                            getString(R.string.clean_now),
                            getString(R.string.clean), null,
                            mDeletePromptListener);
                }
                if (df != null && !df.isAdded()) {
                    df.show(getSupportFragmentManager(), "loginDialog");
                }
                break;
            case R.id.receiver_notification_sw:

                break;
            case R.id.version_rl:
                checkNewVersion();
                break;
            default:
                break;
        }
    }

    private long lastClickTime;
    /**
     * @Description:[如果是连续点击则返回true]
     * @return
     */
    private boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;

        return false;
    }
    /**
     * 检测新版本
     * @return void
     */
    public void checkNewVersion() {
        if (isFastDoubleClick()) {
            return;
        }
        isNeedTip = true;
        if (ClientInfo.networkType == ClientInfo.NONET) {
            ToastUtils.showToast(SettingActivity.this
                    .getString(R.string.toast_net_error));
            return;
        }
        if (mAppsItemBean != null) {
            if (AppManagerCenter.appIsNeedUpate(
                    mAppsItemBean.packageName,
                    mAppsItemBean.versionCode)) {
                PreferencesUtils.putString(SettingActivity.this,
                        Constants.APP_MD5, mAppsItemBean.apkMd5);
                if (isNeedTip && SettingActivity.this.isResumed()) {
                    displayDialog();
                }
            } else {
                if (isNeedTip) {
                    ToastUtils.showToast(R.string.verision_is_newest);
                }
            }
        } else {
            checkUpdate();
        }
    }

    private void checkUpdate() {
        if (isRequestOk) {
            displayDialog();
            return;
        }
        String mUrl  = Constants.SYSTEM_UPGRADE_URL;
        RequestParams entity = new RequestParams(mUrl);
        entity.setConnectTimeout(30 * 1000);
        reqHandler = XExtends.http().post(entity, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                JLog.i(TAG, "result--->" + result);
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {
                        isRequestOk = true;
                        JSONObject o = new JSONObject(obj.getString("data"));
                        mAppsItemBean = new Gson().fromJson(o.getString("app"),
                                AppsItemBean.class);
                        if (AppManagerCenter.appIsNeedUpate(
                                mAppsItemBean.packageName,
                                mAppsItemBean.versionCode)) {
                            PreferencesUtils.putString(SettingActivity.this,
                                    Constants.APP_MD5, mAppsItemBean.apkMd5);
                            if (isNeedTip && SettingActivity.this.isResumed()) {
                                displayDialog();
                            }
                        } else {
                            if (isNeedTip) {
                                ToastUtils.showToast(R.string.verision_is_newest);
                            }
                        }
                    } else {
                        try {
                            String msg = obj.getString("msg");
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.showToast(msg);
                            }

                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                } catch (JSONException e) {

                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

        });
    }

    private void displayDialog() {
        if (mAppsItemBean == null)
            return;
        if (mUpdateSelfDialog == null) {
            mUpdateSelfDialog = new UpdateSelfDialog(SettingActivity.this, R.style.add_dialog, ClientInfo.getInstance().appVersionCode,getResources().getString(R.string.new_version_name,
                            mAppsItemBean.versionName),mAppsItemBean.updateInfo);
            mUpdateSelfDialog.setBean(mAppsItemBean);
        }
        if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
            mUpdateSelfDialog.show();

        }
    }



    Runnable mBackgroundRunnable = new Runnable() {

        @Override
        public void run() {
            // String path = FileUtils.getImageCachePath();
            // File dataDir = new File(new File(
            // Environment.getExternalStorageDirectory(), "Android"),
            // "data");
            // File appCacheDir = new File(new File(dataDir, getApplication()
            // .getPackageName()), "cache");

            File appCacheDir = StorageUtils
                    .getCacheDirectory(getApplicationContext());
            double size = 0;
            if (appCacheDir.exists()) {
                size = FileUtils.getDirSize(appCacheDir);
            }
            // if (path != null) {
            // size = FileUtils.getDirSize(new File(path));
            // }
            Message msg = Message.obtain();
            if (size == 0.0) {
                msg.what = 0;
                mHandler.sendMessage(msg);
                return;
            }
            StringBuilder content = new StringBuilder("(");
            content.append(String.format("%1$.2f", size) + "M)");

            // cache_size.setText(content);
            msg.obj = content;
            msg.what = 1;
            mHandler.sendMessage(msg);
        }

    };

    Handler.Callback callback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if (msg != null && msg.obj != null && msg.what == 1) {
                String size = (String) msg.obj.toString();
                cache_size.setText(size);
                return true;
            }  else {
                changeState();
            }
            return false;
        }
    };

    @Override
    public String getActivityName() {
        return null;
    }

    @Override
    protected void onDestroy() {
//        unbindService();
        if (reqHandler != null) {
            reqHandler.cancel();
        }
        super.onDestroy();
    }
}
