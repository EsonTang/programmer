/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p>
 * 内容摘要：
 * 当前版本：
 * 作	者：
 * 完成日期：
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 * ...
 * 修改记录：
 * 修改日期：
 * 版 本 号：
 * 修 改 人：
 * 修改内容：
 *********************************************/

package com.prize.appcenter.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.AppDetailParentFgm;
import com.prize.appcenter.fragment.base.BaseLoadActivity;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.datamgr.AppDetailDataManager;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.DetailDownloadProgressButton;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类描述：app详情
 *
 * @author 作者 huanglingjun
 * @version 版本
 */
@SuppressLint("NewApi")
public class AppDetailActivity extends BaseLoadActivity implements
        OnClickListener, DataManagerCallBack, ServiceConnection {
    private final String TAG = "AppDetailActivity";

    private RelativeLayout mRelayout;
    private AppDetailDataManager dataManager;
    private AppDetailData detailData;
    private DetailDownloadProgressButton mDownloadBtn;
    private AppDetailParentFgm detailParentFgm;
    private String appId;
    private AppsItemBean itemBean;
    private AppsItemBean itemData;
    private View divide_line;
    private IUIDownLoadListenerImp listener = null;
//    private TextView mTitle;

    private String mUserId;
    private String from = null;
    private String packageName = null;
    private DownDialog mDownDialog;
    private FrameLayout container_wait;
    private FrameLayout container;
    private FrameLayout reload_id;
    //    private Bundle mBundle;
//    private RelativeLayout action_bar;
    private ServiceToken mToken;
    /**
     * appId 参数 为String型 added by fanjunchen
     */
    public final static String P_APPID = "appid";
    public final static String FROM = "from";
    private final String isDowmloadNowKey = "isDowmloadNowKey";
    private String keyWord;
    private String url;
    private String pageInfo;
    private boolean isDowmloadNow = false;
    private Handler mHandler = new MyHander(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (!BaseApplication.isThird) {
            WindowMangerUtils.initStateBar(window, this);
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "AppDetailActivity-onCreate-savedInstanceState=" + (savedInstanceState == null));
        }
        if (savedInstanceState != null) {
            UIUtils.addActivity(this);
        }
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new MyIUIDownLoadCallBack(this));
        mToken = AIDLUtils.bindToService(this, this);
        if (!BaseApplication.isThird) {
            setContentView(R.layout.activity_appdetail);
        } else {
            setContentView(R.layout.activity_appdetail_third);
        }
        //Overdraw 的处理移除不必要的background
        getWindow().setBackgroundDrawable(null);
//        WindowMangerUtils.changeStatus(window);
        Intent intent = getIntent();
        Bundle mBundle = null;
        if (intent != null) {
            mBundle = intent.getBundleExtra("bundle");
            if (mBundle != null) {
                itemData = mBundle.getParcelable("AppsItemBean");
                url = mBundle.getString("url");
                pageInfo = mBundle.getString("pageInfo");
                appId = mBundle.getString(P_APPID, null);
                from = mBundle.getString(FROM, null);
                isDowmloadNow = mBundle.getBoolean(isDowmloadNowKey, false);
                JLog.i(TAG, "onCreate-isDowmloadNow=" + isDowmloadNow);
                packageName = mBundle.getString("packageName");
                if (!TextUtils.isEmpty(from) && from.equals("search")) {
                    keyWord = mBundle.getString("keyWord", null);
                }
            }
        }
        if (savedInstanceState != null) {
            android.support.v4.app.Fragment fragment = getSupportFragmentManager().
                    findFragmentByTag(AppDetailParentFgm.class.getSimpleName());
            if (fragment != null&&fragment instanceof  AppDetailParentFgm) {
                detailParentFgm = (AppDetailParentFgm) fragment;
            }
        }
        if (detailParentFgm == null) {
            detailParentFgm = new AppDetailParentFgm();
            detailParentFgm.setArguments(mBundle);
        }
        if (detailParentFgm != null && !detailParentFgm.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, detailParentFgm, AppDetailParentFgm.class.getSimpleName())
                    .commitAllowingStateLoss();
        }

        XGPushClickedResult xgPushClickedResult = XGPushManager
                .onActivityStarted(this);
        if (xgPushClickedResult != null) {
            String pushJson = xgPushClickedResult.getCustomContent();
            if (!TextUtils.isEmpty(pushJson)) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(pushJson);
                    from = jsonObject.optString(FROM);
                    appId = jsonObject.optString(P_APPID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        initView();
        Map<String, String> map = new HashMap<String, String>();
        map.put(Constants.EVT_ENTER_APP_DETAIL_ID, Constants.E_ENTER_APP_DETAIL);
        MobclickAgent.onEventValue(MainApplication.curContext,
                Constants.EVT_ENTER_APP_DETAIL_ID, map, 100);
//        action_bar.post(new Runnable() {
//
//            @Override
//            public void run() {
//                if (detailParentFgm != null) {
//                    detailParentFgm.setActionBarHeight(action_bar.getHeight());
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.onPause(this);
    }

    private void initView() {
        container_wait = (FrameLayout) findViewById(R.id.container_wait);
        divide_line = findViewById(R.id.divide_line);
        container = (FrameLayout) findViewById(R.id.container);
        reload_id = (FrameLayout) findViewById(R.id.reload_id);
        mRelayout = (RelativeLayout) findViewById(R.id.bottom_id);
        mDownloadBtn = (DetailDownloadProgressButton) findViewById(R.id.detailinfo_download_id);
        mDownloadBtn.setOnClickListener(this);
        mDownloadBtn.enabelDefaultPress(true);
        if (itemData == null) {
            initLoadView();
        }

        doRequest();
    }

    private void dismissCautionDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
        }
    }

    /**
     * 方法描述：执行网络请求初始化数据
     */
    public void doRequest() {
        if (itemData == null) {
            showWaiting();
        }
        if (!TextUtils.isEmpty(packageName)) {
            requestDetailByPackName();
            return;
        }
        if (dataManager == null) {
            dataManager = new AppDetailDataManager(this);
        }
        if (TextUtils.isEmpty(mUserId)) {
            dataManager.getNetData(appId, "", TAG);
        } else {
            dataManager.getNetData(appId, mUserId, TAG);
        }
    }

    /**
     * 通过包名请求app信息
     */
    private void requestDetailByPackName() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/appinfo/isapp");
        params.addBodyParameter("packageName", packageName);
        params.addBodyParameter("name", "");
        XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    hideWaiting();
                    JSONObject o = new JSONObject(result);
                    if (o.getInt("code") == 0) {
                        detailData = GsonParseUtils.parseSingleBean(o.getString("data"), AppDetailData.class);
                        itemBean = UIUtils.changeToAppItemBean(detailData.app);
                        if (detailData.app == null) {
                            ToastUtils.showToast(R.string.app_isnot_exist);
                            AppDetailActivity.this.finish();
                            return;
                        }
                        if (detailData != null && detailData.app != null
                                && detailData.app.name != null) {
                            mRelayout.setVisibility(View.VISIBLE);
//                            mTitle.setText(detailData.app.name);
                            mDownloadBtn.setGameInfo(UIUtils
                                    .changeToAppItemBean(detailData.app));
                            detailParentFgm.initData(detailData);

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                loadingFailed(new ReloadFunction() {
                    @Override
                    public void reload() {
                        doRequest();

                    }
                });

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {

            case R.id.detailinfo_download_id:
                if (itemBean == null) {
                    return;
                }
                if (!TextUtils.isEmpty(url)) {
                    itemBean.downloadUrl = url;
                }
                if (itemData != null && !TextUtils.isEmpty(itemData.backParams)) {
                    itemBean.backParams = itemData.backParams;
                }
                final int state = AIDLUtils.getGameAppState(itemBean.packageName,
                        itemBean.id, itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (detailParentFgm != null) {
                                    detailParentFgm.scrollBotton();
                                }
                            }
                        });
                        MTAUtil.onClickAppDetailBtnDown(itemBean.name + "-" + itemBean.packageName);
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }


                        if (BaseApplication.isDownloadWIFIOnly()
                                && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(AppDetailActivity.this, R.style.add_dialog);
                            }
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissCautionDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(itemBean);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                            }
                                            break;
                                    }
                                }
                            });

                        } else {
                            mDownloadBtn.onClick();
                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                            }
                        }
                        break;
                    default:
                        mDownloadBtn.onClick();
                        break;
                }

                break;

        }

    }

    /**
     * 执行完网络请求后返回的数据，进行控件的数据初始化操作
     */
    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        UIUtils.addActivity(this);
        hideWaiting();
        if (BaseApplication.isThird) {
            if (this.isFinishing())
                return;
        } else {
            if (this.isDestroyed())
                return;
        }

        synchronized (AppDetailActivity.class) {
            switch (what) {
                case AppDetailDataManager.DETAIL_SUCCESS:
                    if (obj == null) {
                        if (itemData == null) {
                            loadingFailed(new ReloadFunction() {

                                @Override
                                public void reload() {
                                    doRequest();
                                }
                            });
                        } else {
                            detailParentFgm.showReloadView();
                        }
                        return;
                    }
                    detailData = (AppDetailData) obj;
                    if (detailData.app == null) {
                        ToastUtils.showToast(R.string.app_isnot_exist);
                        AppDetailActivity.this.finish();
                        return;
                    }
                    itemBean = UIUtils.changeToAppItemBean(detailData.app);
                    if (detailData != null && detailData.app != null
                            && detailData.app.name != null) {
                        mRelayout.setVisibility(View.VISIBLE);
//                        mTitle.setText(detailData.app.name);
                        if (itemData != null) {
                            mDownloadBtn.setGameInfo(itemData);
                            if (JLog.isDebug) {
                                JLog.i(TAG, "来自其他传AppsItemBean，前页已曝光页面信息=" + itemData.pageInfo);
                            }
                        } else {
                            if (!TextUtils.isEmpty(pageInfo)) {
                                itemBean = CommonUtils.formatNewAppPageInfo(itemBean, pageInfo);
                                if (JLog.isDebug) {
                                    JLog.i(TAG, "来自其他，传id，详情才曝光页面信息=" + pageInfo);
                                    JLog.i(TAG, "来自其他，传id，更新后页面信息=" + itemBean.pageInfo);
                                }
                                if (BaseApplication.isNeedStatic) {
                                    ExposureBean bean = new Gson().fromJson(pageInfo, ExposureBean.class);
                                    List<ExposureBean> mExposureBeans = new ArrayList<>();
                                    mExposureBeans.add(CommonUtils.formNewPagerExposure(itemBean, bean.gui, bean.widget));
                                    PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                                    mExposureBeans.clear();
                                }
                            }
                            mDownloadBtn.setGameInfo(itemBean);
                        }
                        if (isDowmloadNow) {
                            final int state = AIDLUtils.getGameAppState(
                                    itemBean.packageName, String.valueOf(itemBean.id),
                                    itemBean.versionCode);
                            if (state == AppManagerCenter.APP_STATE_UNEXIST || state == AppManagerCenter.APP_STATE_UPDATE
                                    || state == AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE)
                                mDownloadBtn.performClick();
                        }
                        if (detailData != null && detailData.app != null && detailData.app.style != null
                                && !TextUtils.isEmpty(detailData.app.style.backgroundColor)
                                && !TextUtils.isEmpty(detailData.app.style.backgroundUrl)) {
                            int bgColor;
                            try {
                                bgColor = Color.parseColor("#" + detailData.app.style.backgroundColor);
                            } catch (Exception e) {
                                bgColor = Color.WHITE;//防止后台配置出错时，默认浅绿色
                            }
                            if (JLog.isDebug) {
                                JLog.i("AppDetailParentFgm", "bgColor=" + bgColor
                                        + "--detailData.app.backgroundColor=" + detailData.app.style.backgroundColor);
                            }
                            mRelayout.setBackgroundColor(bgColor);
                            int btnColor = CommonUtils.getModifyBrightColor(bgColor);
                            divide_line.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
//                            int intColor = CommonUtils.getModifyBrightColor(bgColor);
                            mDownloadBtn.setBackgroundColor(btnColor, btnColor);
                        }
                        detailParentFgm.initData(detailData);

                    } else {
                        Toast.makeText(this, this.getString(R.string.failure),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                case AppDetailDataManager.DETAIL_FAILURE:
                    Toast.makeText(this, this.getString(R.string.failure),
                            Toast.LENGTH_SHORT).show();
                    break;
                case NetSourceListener.WHAT_NETERR:
                    if (itemData == null) {
                        loadingFailed(new ReloadFunction() {

                            @Override
                            public void reload() {
                                doRequest();
                            }
                        });
                    } else {
                        detailParentFgm.showReloadView();
                    }
                    break;
                default:
                    break;
            }
        }
    }

//    public FadingActionBarHelper getFadingActionBarHelper() {
//        return mFadingActionBarHelper;
////    }

//    @Override
//    public void onBackPressed() {
//        try {
//            super.onBackPressed();
//        } catch (IllegalStateException ignored) {
//            // There's no way to avoid getting this if saveInstanceState has already been called.
//            finish();
//        }
//        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
//            try {
//                UIUtils.gotoActivity(MainActivity.class, AppDetailActivity.this);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(from) && "push".equals(from)) {
            try {
                UIUtils.gotoActivity(MainActivity.class, AppDetailActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.finish();
    }

//    public void goToComment(View view) {
//        if (detailData == null || detailData.app == null) {
//            return;
//        }
//        Intent intent = new Intent(this, AppCommentActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("detailData", detailData.app);
//        bundle.putBoolean("isComment", false);
//        intent.putExtra("bundle", bundle);
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//    }

    private void initLoadView() {
        View waitView = LayoutInflater.from(this).inflate(
                R.layout.fragment_detail_waiting, null);
        View reload_View = LayoutInflater.from(this).inflate(
                R.layout.detail_relaod_layout, null);
        LinearLayout layoutFailure = (LinearLayout) reload_View
                .findViewById(R.id.loadFailure_Llyt_id);
        LinearLayout layoutloading = (LinearLayout) waitView
                .findViewById(R.id.loading_Llyt_id);

        layoutFailure.setGravity(Gravity.CENTER);
        layoutloading.setGravity(Gravity.CENTER);
        container_wait.addView(waitView, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        reload_id.addView(reload_View, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        initAllView(container_wait, container, reload_id);
    }

//    /**
//     * 方法描述：初始化actionbar
//     */
//
//    protected void initActionBar() {
////        action_bar = (RelativeLayout) findViewById(R.id.action_bar_no_tab);
////        ImageButton action_bar_back = (ImageButton) findViewById(R.id.action_bar_back);
////        ImageButton action_bar_search = (ImageButton) findViewById(R.id.action_bar_search);
////        mTitle = (TextView) findViewById(R.id.app_title_Tv);
////        action_bar_back.setOnClickListener(onClickListener);
////        action_bar_search.setOnClickListener(onClickListener);
////        mTitle.setOnClickListener(onClickListener);
//    }
//
//    OnClickListener onClickListener = new OnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//            int id = v.getId();
//            switch (id) {
//                case R.id.action_bar_back:
//                    onBackPressed();
//                    break;
//                case R.id.app_title_Tv:
//                    onBackPressed();
//                    break;
//                case R.id.action_bar_search:
//                    UIUtils.goSearchActivity(AppDetailActivity.this);
//                    break;
//            }
//        }
//    };


    public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
        AIDLUtils.registerCallback(listener);
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    protected void onStop() {
        super.onStop();
        XGPushManager.onActivityStoped(this);
    }

    @Override
    protected void onDestroy() {
        UIUtils.removeActivity(this);
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        if (dataManager != null) {
            dataManager.setNullListener();
        }
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
//        detailParentFgm = null;
        AIDLUtils.unbindFromService(mToken);
        mToken = null;
        super.onDestroy();
    }

    private static class MyIUIDownLoadCallBack implements IUIDownLoadListenerImp.IUIDownLoadCallBack {
        WeakReference<AppDetailActivity> mActivities = null;

        MyIUIDownLoadCallBack(AppDetailActivity intance) {
            mActivities = new WeakReference<AppDetailActivity>(intance);
        }

        @Override
        public void callBack(String pkgName, int state, boolean isNewDownload) {
            if (mActivities == null) {
                return;
            }
            final AppDetailActivity intance = mActivities.get();
            if (intance == null) {
                return;
            }
            if (intance.mHandler != null) {
                Message msg = Message.obtain();
                msg.obj = pkgName;
                msg.what = 0;
                intance.mHandler.sendMessage(msg);
            }
        }
    }


    private static class MyHander extends Handler {
        private WeakReference<AppDetailActivity> mActivities;

        public MyHander(AppDetailActivity mActivity) {
            this.mActivities = new WeakReference<AppDetailActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final AppDetailActivity activity = mActivities.get();
            if (activity != null) {
                if (activity.itemBean != null && activity.itemBean.packageName.equals(msg.obj)) {
                    if (activity.mDownloadBtn != null) {
                        activity.mDownloadBtn.invalidate();
                    }
                }
            }
        }

    }

}
