package com.prize.appcenter.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.dialog.ShareDialog;
import com.prize.appcenter.ui.dialog.UpdateSelfDialog;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

/**
 * *
 * 关于
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class AboutActivity extends ActionBarNoTabActivity {
    private TextView about_iv;
    private UpdateSelfDialog mUpdateSelfDialog;
    private RelativeLayout share_to_friend_RL;
    private ImageView bottom_line_id;
    private boolean isNeedTip = false;
    private Cancelable cancelable;
    private ShareDialog mShareDialog;
    private boolean isRequestOk = false;
    private AppsItemBean mAppsItemBean;
    private int clickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        WindowMangerUtils.changeStatus(getWindow());
        setTitle(R.string.set_about);
        if (getIntent() != null) {
            from = getIntent().getStringExtra(Constants.FROM);
        }
        RelativeLayout about_RL = (RelativeLayout) findViewById(R.id.about_RL);
        TextView about = (TextView) findViewById(R.id.setting_about);
        about_iv = (TextView) findViewById(R.id.about_iv);
        share_to_friend_RL = (RelativeLayout) findViewById(R.id.share_to_friend_RL);
        bottom_line_id = (ImageView) findViewById(R.id.bottom_line_id);
        ImageView icon_id = (ImageView) findViewById(R.id.icon_id);
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                mAppsItemBean = (AppsItemBean) getIntent().getExtras().get(
                        "bean");
            }
        }
        if (mAppsItemBean == null) {
            checkUpdate();
        } else {
            isRequestOk = true;
            if (AppManagerCenter.appIsNeedUpate(mAppsItemBean.packageName,
                    mAppsItemBean.versionCode)) {
                PreferencesUtils.putString(AboutActivity.this,
                        Constants.APP_MD5, mAppsItemBean.apkMd5);
                about_iv.setVisibility(View.VISIBLE);
            }
        }
        about.setText(getString(R.string.set_about_jolo,
                ClientInfo.getInstance().appVersionCode));
        about_RL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFastDoubleClick()) {
                    return;
                }
                isNeedTip = true;
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(AboutActivity.this
                            .getString(R.string.toast_net_error));
                    return;
                }
                if (mAppsItemBean != null) {
                    if (AppManagerCenter.appIsNeedUpate(
                            mAppsItemBean.packageName,
                            mAppsItemBean.versionCode)) {
                        PreferencesUtils.putString(AboutActivity.this,
                                Constants.APP_MD5, mAppsItemBean.apkMd5);
                        about_iv.setVisibility(View.VISIBLE);
                        if (isNeedTip && AboutActivity.this.isResumed()) {
                            displayDialog();
                        }
                    } else {
                        about_iv.setVisibility(View.GONE);
                        if (isNeedTip) {
                            ToastUtils.showToast(R.string.is_newest);
                        }
                    }
                } else {
                    checkUpdate();
                }

            }
        });
        isShowShareDialog();
        icon_id.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                long time = System.currentTimeMillis();
                if (lastClickTime != 0) {
                    long timeD = time - lastClickTime;
                    if (timeD > 1000) {
                        clickCount = 0;
                    }
                }
                clickCount++;
                if (clickCount == 4) {
                    if (BaseApplication.isNewSign) {
                        ToastUtils.showToast(AboutActivity.this.getString(R.string.channel) + ClientInfo.getInstance().channel + "-isNewSign");
                    } else {
                        ToastUtils.showToast(AboutActivity.this.getString(R.string.channel) + ClientInfo.getInstance().channel);

                    }
                    clickCount = 0;
                }
                lastClickTime = time;
            }
        });

        icon_id.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                go2WebView();
                return false;
            }
        });
    }


    private long lastClickTime;

    /**
     * 如果是连续点击则返回true
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

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return "AboutActivity";
    }

    @Override
    protected void initActionBar() {
        findViewById(R.id.action_bar_feedback).setVisibility(View.INVISIBLE);
        super.initActionBar();
    }

    private void checkUpdate() {
        if (isRequestOk) {
            displayDialog();
            return;
        }
        String mUrl = Constants.SYSTEM_UPGRADE_URL;
        if (BaseApplication.isThird || BaseApplication.isOeder) {
            mUrl = Constants.THIRD_UPGRADE_URL;
        }
        RequestParams entity = new RequestParams(mUrl);
        entity.setConnectTimeout(30 * 1000);
        cancelable = XExtends.http().post(entity, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
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
                            PreferencesUtils.putString(AboutActivity.this,
                                    Constants.APP_MD5, mAppsItemBean.apkMd5);
                            about_iv.setVisibility(View.VISIBLE);
                            if (isNeedTip && AboutActivity.this.isResumed()) {
                                displayDialog();
                            }
                        } else {
                            about_iv.setVisibility(View.GONE);
                            if (isNeedTip) {
                                ToastUtils.showToast(R.string.is_newest);
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
        });
    }

    private void displayDialog() {
        if (mAppsItemBean == null)
            return;
        if (mUpdateSelfDialog == null) {
            mUpdateSelfDialog = new UpdateSelfDialog(AboutActivity.this,
                    R.style.add_dialog,
                    ClientInfo.getInstance().appVersionCode, getResources()
                    .getString(R.string.new_version_name,
                            mAppsItemBean.versionName), mAppsItemBean.apkSizeFormat,
                    mAppsItemBean.updateInfo);
            mUpdateSelfDialog.setBean(mAppsItemBean);
        }
        if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
            mUpdateSelfDialog.show();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* observer download change **/
        // getContentResolver().registerContentObserver(
        // DownloadManagerPro.CONTENT_URI, true, downloadObserver);
        // updateView();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
        // getContentResolver().unregisterContentObserver(downloadObserver);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (cancelable != null) {
            cancelable.cancel();
        }
    }

    /**
     * 是否显示分享
     */
    private void isShowShareDialog() {
        if (BaseApplication.isThird) {
            share_to_friend_RL.setVisibility(View.VISIBLE);
            bottom_line_id.setVisibility(View.VISIBLE);
            share_to_friend_RL.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mShareDialog = new ShareDialog(AboutActivity.this,
                            AlertDialog.THEME_TRADITIONAL);
                    Window window = mShareDialog.getWindow();
                    if (window == null) return;
                    window.setGravity(Gravity.BOTTOM);
                    window.setWindowAnimations(R.style.popwindow_anim_style);
                    mShareDialog.show();

                }
            });
        }
    }


    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            try {
                UIUtils.gotoActivity(MainActivity.class, AboutActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onBackPressed();
    }

    private void go2WebView() {
        Intent intent = new Intent(this, WebViewActivity.class);
//        intent.putExtra(WebViewActivity.P_URL, "http://192.168.1.192:8080/index.html#/lotteryTest/9");
        intent.putExtra(WebViewActivity.P_URL, Constants.GIS_URL + "/web/test");
        startActivity(intent);
    }
}
