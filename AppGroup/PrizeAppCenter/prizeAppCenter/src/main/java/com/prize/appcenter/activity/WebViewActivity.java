package com.prize.appcenter.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.AppBrefData;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.service.ServiceToken;
import com.prize.appcenter.ui.datamgr.AppWebViewDataManager;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.dialog.JS2AndroidDialog;
import com.prize.appcenter.ui.dialog.WebShareDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.webview.CustomWebView;
import com.prize.appcenter.ui.widget.CornerImageView;
import com.prize.appcenter.ui.widget.ProgressNoGiftButton;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.custmerxutils.XExtends;

import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Stack;

import static com.prize.app.util.HttpUtils.getParams;

/***
 * 内嵌WebView页面
 *
 * @author fanjunchen
 */
public class WebViewActivity extends Activity implements DataManagerCallBack,
        ServiceConnection {

    /**
     * 需要加载的URL(string)
     */
    public static final String P_URL = "p_loadUrl";
    /**
     * 需要加载的应用的URL(string)
     */
    public static final String P_APP_URL = "p_appUrl";
    /**
     * 需要加载的应用的ID(string)
     */
    public static final String P_APP_ID = "p_appId";
    /**
     * 模拟增加进度条进度
     */
    private static final int INCREASE_MSG = 1;
    private static final int REFRESH_BTN = INCREASE_MSG + 1;
    private static WebViewHandler mHandler;
    private final String TAG = "WebViewActivity";
    protected ServiceToken mToken;
    private  Stack<String> urlStack = new Stack<String>();
    private CustomWebView mWebView;
    private ProgressBar mProgressBar;
    private TextView titleView;
    private String mUrl = null;
    private String appId = null;
    private int count = 0;
    private boolean stopExitOut = false;
    /**
     * activity是否结束
     */
    private boolean isDestroy = false;
    private ProgressNoGiftButton mButton;
    private View mBottomView, mErrorView;
    /**
     * 应用icon图标
     */
    private CornerImageView mImgIcon;
    /**
     * 应用icon图标
     */
    private TextView mAppName, mAppOther;
    private RatingBar mRatingBar;
    private AppWebViewDataManager dataManager;
    private AppsItemBean mBean;
    private IUIDownLoadListenerImp mProgressListener;
    private WebShareDialog mShareDialog;
    private String mTitle = null;
    private String from = null;
    private WebShareDialog.IReloadUrl reloadListener = new WebShareDialog.IReloadUrl() {
        @Override
        public void onClickReload(String url) {
            mProgressBar.setProgress(0);
            mWebView.loadUrl(mUrl);
            mWebView.setVisibility(View.VISIBLE);
            mErrorView.setVisibility(View.GONE);
        }
    };
    private DownDialog mDownDialog;
    private JS2AndroidDialog mJS2AndroidDialog;
    private IntentFilter mIntentFilter;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!BaseApplication.isThird) {
            initStatusBar();
        }
        super.onCreate(savedInstanceState);
        mToken = AIDLUtils.bindToService(this, this);
        setContentView(R.layout.activity_web_view);
        WindowMangerUtils.changeStatus(getWindow());
        initView();

        registerLocalBroadcastReceiver();
    }

    protected void initStatusBar() {
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(
                    R.color.status_color));
        }
    }

    /***
     * 初始化控件
     */
    private void initView() {

        Intent it = getIntent();

        if (it != null) {
            mUrl = it.getStringExtra(P_URL);

            if (TextUtils.isEmpty(mUrl)) {
                finish();
                return;
            }

            appId = it.getStringExtra(P_APP_ID);
            from = it.getStringExtra("from");
        }
        JLog.i(TAG, "=initView==>mUrl==" + mUrl);
        mHandler = new WebViewHandler(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        titleView = (TextView) findViewById(R.id.tv_title);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);

        mImgIcon = (CornerImageView) findViewById(R.id.img_app);

        mAppName = (TextView) findViewById(R.id.txt_appName);
        mAppOther = (TextView) findViewById(R.id.txt_appSize);

        mWebView = (CustomWebView) findViewById(R.id.web_view);

        mBottomView = findViewById(R.id.bottom_lay);

        mErrorView = findViewById(R.id.error_lay);

        mButton = (ProgressNoGiftButton) findViewById(R.id.btn_download);

        // 打开JS可用
        setJsEnabled(true);

        WebChromeClient webChrome = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                JLog.i(TAG, "===>onReceivedTitle==" + title);
                mTitle = title;
                titleView.setText(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (newProgress > 80) {
                    if (newProgress > 93) {
                        if (mHandler != null) {
                            mHandler.removeMessages(INCREASE_MSG);
                        }
                        mProgressBar.setProgress(0);
                        mWebView.setLoadState(true);
                    } else
                        mProgressBar.setProgress(newProgress);
                }
            }

        };
        mWebView.setWebChromeClient(webChrome);

        WebViewClient client = new WebViewClient() {
            private String preUrl;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                JLog.i(TAG, "===>shouldOverrideUrlLoading==" + url);
                stopExitOut = false;
                //if (count >= 0) {
                    if (urlStack.contains(urlStack))
                        urlStack.push(url);
//                    if (!url.equals(urlStack))
//                        urlStack.push(url);
                //}
                preUrl = url;
                //count++;
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                JLog.i(TAG, "===>onPageFinished==");
                // mHandler.stopAdd();
                if (mHandler != null) {
                    mHandler.removeMessages(INCREASE_MSG);
                }
                mProgressBar.setProgress(0);
                // mProgressBar.setVisibility(View.INVISIBLE);
                mWebView.setLoadState(true);
                super.onPageFinished(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                JLog.i(TAG, "===>onPageStarted==");
                mWebView.setLoadState(false);
                if (TextUtils.isEmpty(mTitle))
                    titleView.setText(getString(R.string.loading));
                // mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.VISIBLE);
                // 虚拟加载进度条
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(INCREASE_MSG);
                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

                super.onReceivedError(view, errorCode, description, failingUrl);
                mWebView.setLoadState(false);
                mWebView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                // 加载出错后的处理
            }
        };
        mWebView.setWebViewClient(client);

        // mWebView.loadUrl(mUrl);

        new JudgeUrl().execute();

        dataManager = new AppWebViewDataManager(this);
        if (!TextUtils.isEmpty(appId))
            dataManager.getNetData(appId, "", TAG);


        initApp();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setJsEnabled(boolean isEnabled) {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(isEnabled);
        if (isEnabled) {
            mWebView.addJavascriptInterface(new WebAppInterface(this),
                    "jsObj");//"jsObj"不能改变，否则无法与H5交互

        }
        if (JLog.isDebug) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDefaultTextEncodingName("utf-8");
        mWebView.requestFocusFromTouch();
    }

    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
//		case R.id.btn_refresh:
//			mWebView.loadUrl(mUrl);
//			break;
            case R.id.btn_more:
                // 从下方弹出对话框
                if (null == mShareDialog)
                    mShareDialog = new WebShareDialog(this,
                            AlertDialog.THEME_TRADITIONAL);
                mShareDialog.setListener(reloadListener);
                mShareDialog.setUrl(mUrl);
                Window window = mShareDialog.getWindow();
                window.setGravity(Gravity.BOTTOM);
                window.setWindowAnimations(R.style.popwindow_anim_style);
                mShareDialog.show();
                break;
            case R.id.btn_back:
            /*
             * if (urlStack.size() > 0) { mWebView.goBack(); urlStack.pop(); }
			 * else
			 */

                onBackPressed();
                break;
            case R.id.btn_download:
                int state = AIDLUtils.getGameAppState(mBean.packageName,
                        String.valueOf(mBean.id), mBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:

                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                            mDownDialog = new DownDialog(this, R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(mBean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            mButton.onClick();
                            break;
                    }

                } else {
                    mButton.onClick();
                }
                break;
            case R.id.bottom_lay:
                if (!TextUtils.isEmpty(appId)) {
                    Intent aIt = new Intent(this, AppDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(AppDetailActivity.P_APPID, appId);
                    aIt.putExtra("bundle", bundle);
                    aIt.putExtra(AppDetailActivity.P_APPID, appId);
                    startActivity(aIt);
                    aIt = null;
                }
                break;
            case R.id.btn_reload:
                if (mHandler != null) {
                    mHandler.resetTimes();
                }
                mProgressBar.setProgress(0);
                mWebView.loadUrl(mUrl);
                mWebView.setVisibility(View.VISIBLE);
                mErrorView.setVisibility(View.GONE);
                break;
            case R.id.btn_set_net:
                Intent aIt = new Intent(
                        android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                startActivity(aIt);
                aIt = null;
                break;
        }
    }

    /***
     * 模拟增加进度
     */
    void increaseProgress() {
        int progress = mProgressBar.getProgress();
        if (progress > 80)
            return;

        progress += 10;
        if (progress > 80)
            progress = 80;
        mProgressBar.setProgress(progress);
    }


    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub
        if (isDestroy)
            return;
        switch (what) {
            case AppWebViewDataManager.GET_FAILURE:
                Toast.makeText(this, getString(R.string.failure),
                        Toast.LENGTH_SHORT).show();
                break;
            case AppWebViewDataManager.GET_SUCCESS:
                if (null == obj || ((AppBrefData) obj).app == null) {
                    ToastUtils.showToast(R.string.app_isnot_exist);
                    return;
                }
                if (mButton != null) {
                    AppBrefData bean = (AppBrefData) obj;
                    mBean = bean.app;
                    mButton.setGameInfo(mBean);
                    // 显示出bean信息
                    mBottomView.setVisibility(View.VISIBLE);
                    mWebView.setTargetView(mBottomView);
                    initApp();
                }
                break;
        }
    }

    /***
     * 初始化应用控件数据
     */
    private void initApp() {
        if (mBean != null) {

            String str = mBean.apkSizeFormat;
            if (null != mBean.downloadTimesFormat) {
                if (null == str)
                    str = getString(R.string.person_use,
                            mBean.downloadTimesFormat.replace("次", "人"));
                else
                    str = getString(R.string.person_use,
                            mBean.downloadTimesFormat.replace("次", "人")) + " " + str;
            }
            mAppOther.setText(str);

            mAppName.setText(mBean.name);

            mButton.setGameInfo(mBean);

            if (!TextUtils.isEmpty(mBean.largeIcon)) {
                ImageLoader.getInstance().displayImage(mBean.largeIcon, mImgIcon,
                        UILimageUtil.getUILoptions(), null);
            } else if (!TextUtils.isEmpty(mBean.iconUrl)) {
                ImageLoader.getInstance().displayImage(mBean.iconUrl, mImgIcon,
                        UILimageUtil.getUILoptions(), null);
            }
            try {
                mRatingBar.setRating((Float.parseFloat(mBean.rating)));
            } catch (Exception e) {
            }
        }
        // 刷新下载的进度获取 需要定时获取
        mProgressListener = IUIDownLoadListenerImp.getInstance();
        mProgressListener.setmCallBack(mIUIDownLoadCallBack);
//        AIDLUtils.registerCallback(mProgressListener);
    }

    private IUIDownLoadCallBack mIUIDownLoadCallBack = new IUIDownLoadCallBack() {
        @Override
        public void callBack(String pkgName, int state,boolean isNewDownload) {
            if (mHandler == null)
                return;
            Message msg = Message.obtain();
            msg.what = REFRESH_BTN;
            msg.obj = pkgName;
            msg.arg1 = state;
            mHandler.sendMessage(msg);
        }
    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AIDLUtils.registerCallback(mProgressListener);

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    private void processH5Data(final AppsItemBean homeAdBean, String type) {
        switch (type) {
            case "detail":
                UIUtils.gotoAppDetail(homeAdBean.id, WebViewActivity.this);
                break;
            case "download":
            case "continue":
                if (ClientInfo.getAPNType(getApplicationContext()) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    return;
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    if(mDownDialog==null){
                        mDownDialog = new DownDialog(WebViewActivity.this,
                                R.style.add_dialog);
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG,"dismissDialog--mDownDialog.show()="+mDownDialog+"--");

                    }
                    mDownDialog.show();
                    mDownDialog.setmOnButtonClic(new OnButtonClic() {

                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    if (homeAdBean != null && homeAdBean.packageName != null)
                                        UIUtils.downloadApp(CommonUtils.formatAppPageInfo(homeAdBean,"h5_page",Constants.LIST,0));
                                    break;
                            }
                        }
                    });

                } else {
                    if (homeAdBean != null && homeAdBean.packageName != null)
                        UIUtils.downloadApp(CommonUtils.formatAppPageInfo(homeAdBean,"h5_page",Constants.LIST,0));
                }

                break;
            case "update":
                if (ClientInfo.getAPNType(getApplicationContext()) == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    return;
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    mDownDialog = new DownDialog(WebViewActivity.this,
                            R.style.add_dialog);
                    mDownDialog.show();
                    mDownDialog.setmOnButtonClic(new OnButtonClic() {

                        @Override
                        public void onClick(int which) {
                            dismissDialog();
                            switch (which) {
                                case 0:
                                    break;
                                case 1:
                                    if (homeAdBean != null && homeAdBean.packageName != null)
                                        UIUtils.downloadApp(homeAdBean);
                                    break;
                            }
                        }
                    });

                } else {
                    if (homeAdBean != null && homeAdBean.packageName != null)
                        UIUtils.downloadApp(homeAdBean);
                }

                break;
            case "pause":
                AIDLUtils.pauseDownload(homeAdBean, true);
                break;
            case "install":
                if (BaseApplication.isThird) {
                    AppManagerCenter.installGameApk(homeAdBean);
                }

                break;
            case "open":
                if (AppManagerCenter.isAppExist(homeAdBean.packageName)) {
                    UIUtils.startSingleGame(homeAdBean.packageName);
                }
                break;

        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mWebView != null) {
                    mWebView.loadUrl("javascript: downloadBtnClickCallBack()");//
                }
            }
        });

    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    private void dismissDelTaskDialog() {
        if (mJS2AndroidDialog != null && mJS2AndroidDialog.isShowing()) {
            mJS2AndroidDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null) {
            mWebView.loadUrl("javascript: javaCallJsExitOutSwitch()");
            try {
                Thread.sleep(300);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }else{
            stopExitOut = false;
        }
        JLog.i(TAG, "stopExitOut=" + stopExitOut+"--from="+from);
        if (stopExitOut) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWebView != null) {
                        mWebView.loadUrl("javascript: javaCallJsToast()");
                    }
                }
            });
        } else {
            JLog.i(TAG, "else=" + stopExitOut+"--from="+from);
            stopExitOut = false;
            if (urlStack.size() > 0) {
                mWebView.goBack();
                urlStack.pop();
                return;
            }
            if (!TextUtils.isEmpty(from) && "push".equals(from)) {
                UIUtils.gotoActivity(MainActivity.class, WebViewActivity.this);
            }
            finish();
            super.onBackPressed();
        }
    }

    /**
     * 注册本地广播接收者
     */
    private void registerLocalBroadcastReceiver() {
        mIntentFilter = new IntentFilter(Constants.ACTION_LOGIN_SUCCESS);
        mLocalBroadcastReceiver = new LocalBroadcastReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, mIntentFilter);
    }

    /**
     * 取消本地广播的注册
     */
    private void unRegisterLocalBroadcastReceiver() {
        if (mLocalBroadcastManager != null) {
            if (mLocalBroadcastReceiver != null) {
                mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
            }
        }
    }

    /**
     * @param url String
     * @param params String
     * @param type String
     */
    private void requestServerByAsync(String url, String params, final String type) {
        RequestParams reqParams = new RequestParams(Constants.GIS_URL + url);
        Map<String, String> map = getParams(params);
        for (String key : map.keySet()) {
            reqParams.addBodyParameter(key, map.get(key));
        }
        XExtends.http().post(reqParams, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWebView != null) {
                            mWebView.loadUrl("javascript: requestServerByAsyncCallBack('" + type + "','success','" + result + "')");
                        }
                    }
                });
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWebView != null) {
                            mWebView.loadUrl("javascript: requestServerByAsyncCallBack('" + type + "','error','')");//downloadBtnClickCallBack()
                        }
                    }
                });
            }
        });

    }

    class WebViewHandler extends Handler {
        WeakReference<WebViewActivity> ref = null;

        private int times = 0;

        public WebViewHandler(WebViewActivity act) {
            ref = new WeakReference<WebViewActivity>(act);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case INCREASE_MSG:
                    if (null == ref)
                        return;
                    times++;
                    WebViewActivity a = ref.get();
                    if (a != null) {
                        a.increaseProgress();
                    }
                    if (times < 9) {
                        removeMessages(INCREASE_MSG);
                        sendEmptyMessageDelayed(INCREASE_MSG, 120);
                    }
                    break;
                case REFRESH_BTN:
                    if (msg.obj == null)
                        return;
                    final String pkgName = (String) msg.obj;
                    final int state = msg.arg1;
                    final float progress = AIDLUtils.getDownloadProgress(pkgName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mWebView != null) {
                                mWebView.loadUrl("javascript: javaCallJsChangeStatus('" + pkgName + "'," + state + "," + progress + ")");
                            }
                        }
                    });
                    JLog.i(TAG, "pkgName=" + pkgName + "---state=" + state);
                    if (mBean != null && pkgName != null && pkgName.equals(mBean.packageName)) {
                        mButton.setGameInfo(mBean);
                    }
                    break;
            }
        }

        public void resetTimes() {
            times = 0;
        }

        public void stopAdd() {
            times = 9;
        }
    }

    class JsObject {
        @JavascriptInterface
        public String toString() {
            return "donotInject";
        }
    }

    /***
     * 判断URL是否为有效连接
     *
     * @author fanjunchen
     */
    class JudgeUrl extends AsyncTask<Void, Void, Boolean> {
        @Override
        public void onPreExecute() {
            if (TextUtils.isEmpty(mTitle))
                titleView.setText(getString(R.string.loading));
            mProgressBar.setProgress(0);
            mProgressBar.setVisibility(View.VISIBLE);
            // 虚拟加载进度条
            if (mHandler != null) {
                mHandler.resetTimes();
                mHandler.removeMessages(INCREASE_MSG);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                URL aURL = new URL(mUrl);
                HttpURLConnection conn = (HttpURLConnection) aURL
                        .openConnection();
                conn.setConnectTimeout(10 * 1000);
                conn.setReadTimeout(10 * 1000);
                conn.connect();
                int status = conn.getResponseCode();
                JLog.i(TAG, "===>getResponseCode==" + status);
                if (status == HttpURLConnection.HTTP_OK || ClientInfo.networkType != ClientInfo.NONET) {
                    return true;
                }
                conn.disconnect();
                conn = null;
            } catch (Exception e) {
                e.printStackTrace();
                JLog.i(TAG, "===>" + e.getMessage());
            }
            return false;
        }

        @Override
        public void onPostExecute(Boolean result) {
            if (result) {
                if (mWebView != null)
                    mWebView.loadUrl(mUrl);
            } else {
                mProgressBar.setProgress(0);
                mProgressBar.setVisibility(View.INVISIBLE);
                if (TextUtils.isEmpty(mTitle))
                    titleView.setText(getString(R.string.load_fail));
                if (mWebView != null && mErrorView != null) {
                    mErrorView.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 自定义的Android代码和JavaScript代码之间的桥梁类
     *
     * @author 1
     */
    public class WebAppInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void jsCallHandleAppAction(final String param, String type) {////JS调用
            JLog.i(TAG, "param=" + param);
            AppsItemBean homeAdBean = GsonParseUtils.parseSingleBean(param, AppsItemBean.class);
            if (homeAdBean == null || homeAdBean.packageName == null)
                return;
            processH5Data(homeAdBean, type);
        }

        @JavascriptInterface
        public int HtmlCallJavaCheckApp
                (String packageName, String gameCode, int versionCode) {//
            return AIDLUtils.getGameAppState(packageName, gameCode, versionCode);
        }

        @JavascriptInterface
        public void HtmlCallJavaOpenApp(String packageName) {//打开应用
            if (AppManagerCenter.isAppExist(packageName)) {
                UIUtils.startSingleGame(packageName);
            }
        }

        @JavascriptInterface
        public String jsCallGetUserLoginInfo() {//获取用户信息
            Person person = CommonUtils.queryUserPerson(getApplicationContext());
            return new Gson().toJson(person);
        }

        @JavascriptInterface
        public void jsCallAlertAToast(String value) {//Toast
            ToastUtils.showToast(value);
        }

        @JavascriptInterface
        public int jsCallGetAppstoreVersionCode() {///** 获得用户应用市场的版本号
            return ClientInfo.getInstance().appVersion;
        }

        @JavascriptInterface
        public void jsCallJumpToAppstoreUpdate() {///**跳转致应用升级关于页面
            UIUtils.gotoActivity(AboutActivity.class, WebViewActivity.this);
        }

        @JavascriptInterface
        public String jsCallRequestServer(String url, String params) {//同步请求网络
            return HttpUtils.jsCallRequestServer(url, params);

        }

        @JavascriptInterface
        public void jsCallRequestServerByAsync(String url, String params, String type) {//
            requestServerByAsync(url, params, type);

        }

        @JavascriptInterface
        public String jsCallInitStatusApps(String appsJson) {//
            return CommonUtils.jsCallInitStatusApps(appsJson);

        }

        @JavascriptInterface
        public void jsCallJumpToLogin() {//跳转到云账号登录界面
            UIUtils.gotoActivity(LoginActivityNew.class, WebViewActivity.this);

        }

        @JavascriptInterface
        public boolean jsCallCheckLogin() {//检测是否登录
            return !TextUtils.isEmpty(CommonUtils.queryUserId());

        }

        @JavascriptInterface
        public float jsCallGetAppProcess(String packageName) {//
            return AIDLUtils.getDownloadProgress(packageName);

        }

        @JavascriptInterface
        public boolean jsCallIsSystemAppstore() {//
            return !BaseApplication.isThird;
        }

        @JavascriptInterface
        public void jsCallTransferParam(String param) {//2.5add
            if ("stopExitOut".equals(param)) {
                stopExitOut = !stopExitOut;
            } else {
                stopExitOut = false;
            }
        }


        /**
         * @param text     服务端返回的提示语
         * @param callback 服务端返回的方法名称
         */
        @JavascriptInterface
        public void jsCallConfirmText(String text, final String callback) {//
            if (mJS2AndroidDialog == null) {
                mJS2AndroidDialog = new JS2AndroidDialog(WebViewActivity.this, R.style.add_dialog);
            }
            mJS2AndroidDialog.show();
            mJS2AndroidDialog.setContent(text);
            mJS2AndroidDialog.setmOnButtonClic(new JS2AndroidDialog.OnButtonClic() {
                @Override
                public void onClick(int which) {
                    dismissDelTaskDialog();
                    switch (which) {
                        case 1:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mWebView != null) {
                                        mWebView.loadUrl("javascript:  " + callback);
                                    }
                                }
                            });

                            break;
                    }
                }
            });
        }
    }

    private class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!TextUtils.isEmpty(CommonUtils.queryUserId())) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mWebView != null) {
                            mWebView.loadUrl("javascript: loginSuccessCallBack()");
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterLocalBroadcastReceiver();
        BaseApplication.cancelPendingRequests(TAG);
        AIDLUtils.unregisterCallback(mProgressListener);
        if (mProgressListener != null) {
            mProgressListener.setmCallBack(null);
            mProgressListener = null;
            mIUIDownLoadCallBack=null;
        }
        AIDLUtils.unbindFromService(mToken);
        if (mWebView != null) {
            RelativeLayout a = (RelativeLayout) mWebView.getParent();
            if (a != null)
                a.removeView(mWebView);
            mWebView.destroy();
        }
        urlStack = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler=null;

        }
        isDestroy = true;
        reloadListener = null;

    }
}
