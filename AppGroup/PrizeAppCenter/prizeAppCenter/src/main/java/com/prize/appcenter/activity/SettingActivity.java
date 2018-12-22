package com.prize.appcenter.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.PromptDialogFragment;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.util.ShortcutUtil;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.cloud.util.Utils;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.io.File;

/**
 * *
 * 设置界面
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SettingActivity extends UpdateActivity implements OnClickListener {
    /* PRIZE-系统版开关声明，第三方版需要注销 huanglingjun 2015-11-25-start */
    /**
     * wifi环境下下载开关
     **/
    private Switch wifiSetIV;
    /**
     * 更新提醒开关
     **/
    private Switch updateNotifySetIV;
    /**
     * 自动升级开关
     **/
    private Switch auto_update_sw;
    /**
     * 推送提醒开关
     **/
    private Switch push_notification_set_btn_iv;
    /**
     * 省流量开关
     **/
    private Switch save_traffic_sw;
    /**
     * 删除安装包开关
     **/
    private Switch pkg_del_sw;
    /**
     * 桌面文件夹开关
     **/
    private Switch folder_set_btn_iv;
    /**
     * 桌面文件夹开关
     **/
    private RelativeLayout folder_set_RL;
    /**
     * 垃圾清理开关
     **/
    private Switch garbage_clean_Sw;
    /* PRIZE-系统版开关声明，第三方版需要注销 huanglingjun 2015-11-25-end */

	/* PRIZE-第三方版开关声明，系统版需要注销 huanglingjun 2015-11-25-start */
    /** wifi环境下下载开关 **/
//     private ToggleButton wifiSetIV;
//     /** 更新提醒开关 **/
//     private ToggleButton updateNotifySetIV;
//     /** 自动升级开关 **/
//     private ToggleButton auto_update_sw;
//     /** 推送提醒开关 **/
//     private ToggleButton push_notification_set_btn_iv;
//     /** 省流量开关 **/
//     private ToggleButton save_traffic_sw;
//     /** 删除安装包开关 **/
//     // private Switch pkg_del_sw;
//     private ToggleButton pkg_del_sw;
//     /** 桌面文件夹开关 **/
//     private ToggleButton folder_set_btn_iv;
//     /** 垃圾清理开关 **/
//     private ToggleButton garbage_clean_Sw;

	/* PRIZE-第三方版开关声明，系统版需要注销 huanglingjun 2015-11-25-end */

    /**
     * 静默安装开关
     **/
    private Switch install_silent_sw;
    private RelativeLayout about_RL;
    private RelativeLayout clear_cache_set_RL;
    private PromptDialogFragment df;
    private PromptDialogFragment mDialog;
    private TextView cache_size;
    private Handler mHandler;
    private TextView about_iv;
    private TextView pkg_path;
    /**
     * Xutils请求后 返回 ，可以用于取消网络请求
     **/
    private Cancelable reqHandler;
    private AppsItemBean bean;
    private TextView mLoginOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BaseApplication.isThird) {
            setContentView(R.layout.activity_setting);
        } else {
            setContentView(R.layout.activity_setting_thrid);
        }
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.setting);
        findViewById();
        setListener();
        initView();
        checkUpdate();
        mHandler = new Handler(Looper.getMainLooper(), callback);
        mHandler.post(mBackgroundRunnable);// 将线程post到Handler中
    }

    private void checkUpdate() {
        String mUrl;
        if (BaseApplication.isThird || BaseApplication.isOeder) {
            mUrl = Constants.THIRD_UPGRADE_URL;
        } else {
            mUrl = Constants.SYSTEM_UPGRADE_URL;
        }
        RequestParams params = new RequestParams(mUrl);
        reqHandler = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {
                        JSONObject o = new JSONObject(obj.getString("data"));
                        bean = new Gson().fromJson(o.getString("app"),
                                AppsItemBean.class);
                        if (AppManagerCenter.appIsNeedUpate(bean.packageName,
                                bean.versionCode)) {
                            about_iv.setVisibility(View.VISIBLE);
                            PreferencesUtils.putString(SettingActivity.this,
                                    Constants.APP_MD5, bean.apkMd5);
                        } else {
                            about_iv.setVisibility(View.GONE);
                        }
                    } else {
                        try {
                            String msg = obj.getString("msg");
                            if (!TextUtils.isEmpty(msg)) {
                                ToastUtils.showToast(msg);
                                JLog.i("SettingActivity", "msg=" + msg);
                            }
                            about_iv.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {

                    e.printStackTrace();

                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                about_iv.setVisibility(View.GONE);

            }

        });

    }

    private Callback callback = new Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if (msg != null && msg.obj != null && msg.what == 1) {
                String size = (String) msg.obj.toString();
                cache_size.setText(size);

                return true;
            } else {
                changeState();
            }
            return false;
        }

    };

    private void changeState() {
        cache_size.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.clear_cache_set_tv))
                .setTextColor(getResources()
                        .getColor(R.color.text_color_737373));
        clear_cache_set_RL.setEnabled(false);
        clear_cache_set_RL.setClickable(false);
    }

    private void findViewById() {
        cache_size = (TextView) findViewById(R.id.cache_size);
        clear_cache_set_RL = (RelativeLayout) findViewById(R.id.clear_cache_set_RL);
        about_RL = (RelativeLayout) findViewById(R.id.about_RL);

        if (!BaseApplication.isThird) {
            /* PRIZE-系统版使用，三方注销-longbaoxiu-2016-06-18)-start */

            push_notification_set_btn_iv = (Switch) findViewById(R.id.push_notification_set_btn_iv);
            folder_set_RL = (RelativeLayout) findViewById(R.id.folder_set_RL);
            auto_update_sw = (Switch) this.findViewById(R.id.auto_update_sw);
            install_silent_sw = (Switch) this
                    .findViewById(R.id.install_silent_sw);
            save_traffic_sw = (Switch) this.findViewById(R.id.save_traffic_sw);
            wifiSetIV = (Switch) this.findViewById(R.id.wifi_set_btn_iv);
            updateNotifySetIV = (Switch) this
                    .findViewById(R.id.update_notify_set_btn_iv);
            pkg_del_sw = (Switch) this.findViewById(R.id.package_set_del_sw);
            folder_set_btn_iv = (Switch) this
                    .findViewById(R.id.folder_set_btn_iv);
            garbage_clean_Sw = (Switch) this
                    .findViewById(R.id.garbage_clean_Sw);
            pkg_path = (TextView) findViewById(R.id.package_path_content);
            /* PRIZE-系统版使用，三方注销--longbaoxiu-2016-06-18)-end */
        } else {
            /* PRIZE-三方版使用，系统版注销-huanglingjun 2015-11-25-start */

//             push_notification_set_btn_iv = (ToggleButton)
//             findViewById(R.id.push_notification_set_btn_iv);
//             auto_update_sw = (ToggleButton) this
//             .findViewById(R.id.auto_update_sw);
//             pkg_del_sw = (ToggleButton) this
//             .findViewById(R.id.package_set_del_sw);
//             save_traffic_sw = (ToggleButton) this
//             .findViewById(R.id.save_traffic_sw);
//             wifiSetIV = (ToggleButton)
//             this.findViewById(R.id.wifi_set_btn_iv);
//             updateNotifySetIV = (ToggleButton) this
//             .findViewById(R.id.update_notify_set_btn_iv);
//             pkg_path = (TextView) findViewById(R.id.package_path_content);
//             folder_set_btn_iv = (ToggleButton)
//             this.findViewById(R.id.folder_set_btn_iv);
//            garbage_clean_Sw = (ToggleButton)
//             this.findViewById(R.id.garbage_clean_Sw);

			/* PRIZE-三方版使用，系统版注销-huanglingjun 2015-11-25-end */
        }
        about_iv = (TextView) findViewById(R.id.about_iv);
        mLoginOutBtn = (TextView) findViewById(R.id.login_out_Btn);
    }

    private void setListener() {
        folder_set_RL.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendIconToLuancher();

//                if (ShortcutUtil.hasShortcut(SettingActivity.this)) {
//                    ToastUtils.showToast("桌面上已存在快捷键");
//                } else {
//                    ShortcutUtil.createShortCut(SettingActivity.this, "必备软件");
//                }
            }
        });
        mLoginOutBtn.setOnClickListener(this);
        about_RL.setOnClickListener(this);
        clear_cache_set_RL.setOnClickListener(this);
        wifiSetIV.setOnCheckedChangeListener(settingClickListener);
        updateNotifySetIV.setOnCheckedChangeListener(settingClickListener);
        auto_update_sw.setOnCheckedChangeListener(settingClickListener);
        garbage_clean_Sw.setOnCheckedChangeListener(settingClickListener);
        save_traffic_sw.setOnCheckedChangeListener(settingClickListener);
        folder_set_btn_iv.setOnCheckedChangeListener(settingClickListener);
        push_notification_set_btn_iv
                .setOnCheckedChangeListener(settingClickListener);
        pkg_del_sw.setOnCheckedChangeListener(settingClickListener);
        if (!BaseApplication.isThird) {
            install_silent_sw.setOnCheckedChangeListener(settingClickListener);
        }
    }

    /**
     * 当仅wifi下载开关进行切换的时候，切换下载状态
     *
     * @param isOnlyWifi ：true: 仅WIFI时下载； false:所有网络都可以下载
     */
    private void switchDownloadsByWifiOnly(boolean isOnlyWifi) {
        // 判断网络状态
        int netType = ClientInfo.networkType;

        if (isOnlyWifi && (netType != ClientInfo.WIFI)) {
            // 仅WIFI下载，且不是WIFI
            AppManagerCenter.pauseAllDownload();
        } else if (!isOnlyWifi
                && ((netType == ClientInfo.MOBILE_2G) || (netType == ClientInfo.MOBILE_3G))) {
            // 仅WIFI下载开关关闭且2g/3g网络下，通知下载。如果WIFI是打开的，什么都不要做
            // AppManagerCenter.continueAllDownload();
        }
    }

    private OnCheckedChangeListener settingClickListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            int viewId = buttonView.getId();
            switch (viewId) {
                case R.id.wifi_set_btn_iv:// 设置wifi下载
                    wifiSettingSwitch(isChecked);
                    break;
                case R.id.update_notify_set_btn_iv:// 设置更新提醒
                    updateNotificationSwitch(isChecked);
                    break;
                case R.id.auto_update_sw:// 设置自动升级
                    autoLoadUpdatePkgSwitch(isChecked);
                    break;
                case R.id.install_silent_sw:// 设置静默安装，默认开启
                    installSilentSwitch(isChecked);
                    break;
                case R.id.save_traffic_sw:// 省流量，默认关闭
                    saveTrafficSwitch(isChecked);
                    break;
                case R.id.package_set_del_sw:
                    delPkgSwitch(isChecked);
                    break;
                case R.id.push_notification_set_btn_iv:// 推送通知，默认开启
                    pushNotifiacationSwitch(isChecked);
                    break;
                case R.id.garbage_clean_Sw:// 垃圾清理 2.5版本 add
                    pushGarbageCleanSwitch(isChecked);
                    break;
                case R.id.folder_set_btn_iv:// 添加桌面文件夹，默认开启
                    // pushNotifiacationSwitch(isChecked);
                    folderSettingSwitch(isChecked);
                    break;
            }

        }

    };

    /**
     * 编辑后退出提示框
     */
    private View.OnClickListener mLogoutPromptListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mDialog.dismissAllowingStateLoss();
            Utils.logout(SettingActivity.this);
            mLoginOutBtn.setVisibility(View.GONE);
        }
    };

    private void folderSettingSwitch(boolean isChecked) {
        // 自动下载更新包设置
//        if (!isChecked) {// 默认ON，故判断OFF
//            DataStoreUtils.saveLocalInfo(
//                    DataStoreUtils.SWITCH_ADD_LUANCHER_FOLDER,
//                    DataStoreUtils.CHECK_OFF);
//            // if (ShortcutUtil.hasShortcut(this, "精选应用")) {
//            ShortcutUtil.delShortCut(this, "必备软件", FolderActivity.class);
//            // }
//        } else {
//            DataStoreUtils.saveLocalInfo(
//                    DataStoreUtils.SWITCH_ADD_LUANCHER_FOLDER,
//                    DataStoreUtils.CHECK_ON);
        sendIconToLuancher();
//        }
    }

    private void sendIconToLuancher() {
        // new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
//        Bitmap[] bitmaps = new Bitmap[4];
//        ArrayList<PackageInfo> appPackage = CommonUtils
//                .getThirdPackageInfoList();
//        if (appPackage != null && appPackage.size() > 0) {
//            for (int i = 0; i < appPackage.size(); i++) {
//                PackageInfo packageInfo = appPackage.get(i);
//                if (packageInfo != null) {
////                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//                    Drawable drawable = packageInfo.applicationInfo
//                            .loadIcon(getPackageManager());
//                    Bitmap bitmap = BitmapUtils.drawableToBitmap(drawable);
//                    bitmaps[i] = bitmap;
//                    if (i == 3)
//                        break;
//                }
//            }
//            Bitmap bitmapBg = BitmapFactory.decodeResource(this.getResources(),
//                    R.drawable.ic_applauncher);
//            Bitmap folderBitmap = BitmapUtils.doodleicons(bitmapBg, bitmaps, 5);
//            if (!ShortcutUtil.hasShortcut(this, "精选应用")) {
        if (!ShortcutUtil.hasShortcut(this)) {
            ShortcutUtil.createShortCut(this, "必备软件");
        } else {
            ToastUtils.showToast("该快捷键已经创建");
        }
//        }
        // }
        // }).start();
    }

    private void wifiSettingSwitch(boolean isChecked) {
        // wifi设置
        if (isChecked) {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY,
                    DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE);
            // 开启仅WIFI下载，2g/3g情况下不能下载
            // wifiSetIV.setImageResource(R.drawable.action_marker_open);
            switchDownloadsByWifiOnly(true);
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY,
                    DataStoreUtils.DOWNLOAD_WIFI_ONLY_UNABLE);
            // wifiSetIV.setImageResource(R.drawable.action_marker_close);
            // 关闭仅WIFI下载，2g/3g情况下允许下载。
            switchDownloadsByWifiOnly(false);
        }
    }

    private void updateNotificationSwitch(boolean isChecked) {
        // 软件更新提示设置
        if (isChecked) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER,
                    DataStoreUtils.CHECK_ON);
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER,
                    DataStoreUtils.CHECK_OFF);
        }
    }

    private void installSilentSwitch(boolean isCheck) {
        // 自动下载更新包设置
        if (!isCheck) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_INSTALL_SILENT,
                    DataStoreUtils.CHECK_OFF);
            autoLoadUpdatePkgSwitch(false); // 关闭自动升级
            auto_update_sw.setChecked(false);
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_INSTALL_SILENT,
                    DataStoreUtils.CHECK_ON);
        }
    }

    /**
     * 省流量设置
     *
     * @param isCheck 默认false
     */
    private void saveTrafficSwitch(boolean isCheck) {
        // 自动下载更新包设置
        if (!isCheck) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_SAVETRAFFIC, "");
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.SWITCH_SAVETRAFFIC,
                    DataStoreUtils.CHECK_ON);
        }
    }

    /**
     * 安装设置
     *
     * @param isChecked 默认false
     */
    protected void delPkgSwitch(boolean isChecked) {
        // TODO 安装后删除安装包
        if (!isChecked) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_DEL_PKG, "");
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_DEL_PKG,
                    DataStoreUtils.CHECK_ON);
        }
    }

    /**
     * 省流量设置
     *
     * @param isCheck 默认false
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

    /**
     * 垃圾清理通知控制
     *
     * @param isCheck 默认true
     */
    private void pushGarbageCleanSwitch(boolean isCheck) {
        // 自动下载更新包设置
        if (!isCheck) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(
                    DataStoreUtils.SWITCH_PUSH_GARBAGE_NOTIFICATION,
                    DataStoreUtils.CHECK_OFF);
        } else {
            DataStoreUtils.saveLocalInfo(
                    DataStoreUtils.SWITCH_PUSH_GARBAGE_NOTIFICATION,
                    DataStoreUtils.CHECK_ON);
        }
    }

    private void autoLoadUpdatePkgSwitch(boolean isCheck) {
        // 自动下载更新包设置
        if (isCheck) {// 默认ON，故判断OFF
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG,
                    DataStoreUtils.CHECK_ON);
            /*
             * installSilentSwitch(isCheck); // 打开静默安装
			 * install_silent_sw.setChecked(true);
			 */
        } else {
            DataStoreUtils.saveLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG, DataStoreUtils.CHECK_OFF);
            // DownloadTaskMgr.getInstance().pauseAllBackgroundDownload();
        }
    }

    private void initView() {
        // wifi是否下载
        if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
            mLoginOutBtn.setVisibility(View.VISIBLE);
        }
        String wifiSettingString = DataStoreUtils
                .readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
        if (wifiSettingString.equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_UNABLE)) {
            wifiSetIV.setChecked(false);
        } else {
            wifiSetIV.setChecked(true);
        }

        // 软件更新提示设置
        String updateNotifySetting = DataStoreUtils
                .readLocalInfo(DataStoreUtils.GAME_UPDATES_REMINDER);
        if (DataStoreUtils.CHECK_OFF.equals(updateNotifySetting)) {
            updateNotifySetIV.setChecked(false);
        } else {
            updateNotifySetIV.setChecked(true);
        }

        // 自动wifi升级更新包设置 默认开启
        String autoloadSetting = DataStoreUtils
                .readLocalInfo(DataStoreUtils.AUTO_LOAD_UPDATE_PKG);
        if (DataStoreUtils.CHECK_OFF.equals(autoloadSetting)) {
            auto_update_sw.setChecked(false);
        } else {
            auto_update_sw.setChecked(true);
        }
        // 垃圾清理开关
        String garbage_clean_Setting = DataStoreUtils
                .readLocalInfo(DataStoreUtils.SWITCH_PUSH_GARBAGE_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(garbage_clean_Setting)) {
            garbage_clean_Sw.setChecked(false);
        } else {
            garbage_clean_Sw.setChecked(true);
        }

        if (!BaseApplication.isThird) {
            // 静默安装默认开启
            String install_silent = DataStoreUtils
                    .readLocalInfo(DataStoreUtils.SWITCH_INSTALL_SILENT);
            if (DataStoreUtils.CHECK_OFF.equals(install_silent)) {
                install_silent_sw.setChecked(false);
            } else {
                install_silent_sw.setChecked(true);
            }
        }

        // 接收推送默认开启
        String push_notification = DataStoreUtils
                .readLocalInfo(DataStoreUtils.SWITCH_PUSH_NOTIFICATION);
        if (DataStoreUtils.CHECK_OFF.equals(push_notification)) {
            push_notification_set_btn_iv.setChecked(false);
        } else {
            push_notification_set_btn_iv.setChecked(true);
        }
        // 省流量设置，默认关闭
        String switch_savetraffic = DataStoreUtils
                .readLocalInfo(DataStoreUtils.SWITCH_SAVETRAFFIC);
        if (!TextUtils.isEmpty(switch_savetraffic)
                && DataStoreUtils.CHECK_ON.equals(switch_savetraffic)) {
            save_traffic_sw.setChecked(true);
        } else {
            save_traffic_sw.setChecked(false);
        }
        // huanglingjun 2015-11-25
        if (BaseApplication.isThird) {
            // 安装后删除安装包 默认关闭
            String delPackage = DataStoreUtils
                    .readLocalInfo(DataStoreUtils.AUTO_DEL_PKG);
            if (!TextUtils.isEmpty(delPackage)
                    && DataStoreUtils.CHECK_ON.equals(delPackage)) {
                // 第三方版必须打开
                pkg_del_sw.setChecked(true);
            } else {
                pkg_del_sw.setChecked(false);
            }
            // 安装路径
            pkg_path.setText(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/prizeAppCenter/download");
        }
//        int launcherCode = AppManagerCenter.getAppVersionCode("com.android.launcher3");
//        Log.i("MainActivity", "launcherCode=" + launcherCode);
//        if (launcherCode <= 35) {
            folder_set_RL.setVisibility(View.GONE);
            findViewById(R.id.shortcut_line).setVisibility(View.GONE);
//        }
    }

    @Override
    public String getActivityName() {
        return "SettingActivity";
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    @Override
    public void onClick(View v) {
        int key = v.getId();
        switch (key) {
            case R.id.feedback_RL:
                UIUtils.gotoActivity(FeedbackExActivity.class, SettingActivity.this);
                break;

            case R.id.about_RL:
                Intent intent = new Intent(SettingActivity.this,
                        AboutActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle b = new Bundle();
                if (bean != null) {
                    b.putParcelable("bean", bean);
                }
                intent.putExtras(b);
                startActivity(intent);
//                ShortcutUtil.delShortCut(this,"精选应用",FolderActivity.class);
                break;
            case R.id.clear_cache_set_RL:// 清理缓存
                if (df == null || !df.isAdded()) {
                    df = PromptDialogFragment.newInstance(null, null, null, null,
                            mDeletePromptListener);
                }
                try {
                    if (df != null && !df.isAdded()) {
                        df.show(getSupportFragmentManager(), "loginDialog");
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.login_out_Btn:

                if (mDialog == null || !mDialog.isAdded()) {
                    mDialog = PromptDialogFragment
                            .newInstance(
                                    getResources().getString(
                                            R.string.caution),
                                    getResources().getString(
                                            R.string.login_out_tips),
                                    getString(R.string.alert_button_yes),
                                    null, mLogoutPromptListener);
                }
                try {
                    if (mDialog != null && !mDialog.isAdded()) {
                        mDialog.show(getSupportFragmentManager(), "sureDialog");
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                break;
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

    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundRunnable != null && mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (reqHandler != null) {
            reqHandler.cancel();
        }
    }


    private class AsyncLoader_GuessInfo extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            File appCacheDir = StorageUtils
                    .getCacheDirectory(getApplicationContext());
            return appCacheDir.exists() && FileUtils.recursionDeleteFile(appCacheDir);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                changeState();
                ToastUtils.showToast(R.string.cache_clear_ok);
            }
        }

    }

}
