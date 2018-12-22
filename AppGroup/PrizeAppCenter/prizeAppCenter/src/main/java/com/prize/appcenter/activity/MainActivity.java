package com.prize.appcenter.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.service.LocationService;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.LocationInfo;
import com.prize.app.beans.RecomandSearchWords;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadState;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PushTimeBean;
import com.prize.app.threads.PriorityRunnable;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.HttpUtils;
import com.prize.app.util.HttpUtils.RequestPIDCallBack;
import com.prize.app.util.JLog;
import com.prize.app.util.MD5Util;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.callback.IUpdateWatcherEtds;
import com.prize.appcenter.callback.IUpdateWatcherEtds.IUpdateWatcherEtdsCallBack;
import com.prize.appcenter.callback.NetConnectedListener;
import com.prize.appcenter.callback.UpdateWatchedManager;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.receiver.UserBroadcast;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.prize.appcenter.ui.actionBar.ActionBarTabActivity;
import com.prize.appcenter.ui.animation.ParabolaView;
import com.prize.appcenter.ui.dialog.ShortCutCautionDialog;
import com.prize.appcenter.ui.dialog.UpdateSelfDialog;
import com.prize.appcenter.ui.pager.AppTypePagerGifts;
import com.prize.appcenter.ui.pager.BasePager;
import com.prize.appcenter.ui.pager.CommRankPager;
import com.prize.appcenter.ui.pager.GameTypePagerGifts;
import com.prize.appcenter.ui.pager.HomePager;
import com.prize.appcenter.ui.pager.PersonalCenterPager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.AppUpdateCache;
import com.prize.appcenter.ui.util.ShortcutUtil;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.util.UpdateDataUtils;
import com.prize.appcenter.ui.widget.CustomViewPager;
import com.prize.appcenter.ui.widget.indicator.IconTextPagerAdapter;
import com.prize.appcenter.ui.widget.indicator.TabTextImagePageIndicator;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;


/**
 * 主界面MainActivity
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class MainActivity extends ActionBarTabActivity implements
        NetConnectedListener {
    private final static String TAG = "MainActivity";
    public static MainActivity thisActivity = null;
    private static final int[] ICONS = {R.string.quality_goods, R.string.brand, R.string.apps,
            R.string.game, R.string.mine};
    private static final int[] ICONSRES = {R.drawable.tab_home_selector, R.drawable.tab_rank_selector,
            R.drawable.tab_apptype_selector, R.drawable.tab_gametype_selector,
            R.drawable.tab_mine_selector};

    private static final long PRESS_BACK_TIME = 2 * 1000;
    /**
     * 上次按返回的时间
     */
    private long lastPressBackTime = 0;

    /***
     * pageID
     */
    private static final int HOME_PAGER_ID = 0;
    private static final int TAB_RANK_ID = 1;
    private static final int APP_CATEGORY_PAGER_ID = 2;
    private static final int GAME_ONLINE_PAGE_ID = 3;
    private static final int MINE_PAGER_ID = 4;

    private HomePager homePage;
    private GameTypePagerGifts onlinepage;

    private PersonalCenterPager mPersonalCenter;

    private AppTypePagerGifts mAppCategoryPager;

    private CustomViewPager viewPager;
    private TabTextImagePageIndicator mIndicator;
    private RelativeLayout download_queue_Rlyt;
    private int currentPage;
    private AppsItemBean bean;
    private BasePager[] pagers = new BasePager[ICONS.length];
    private Cancelable reqHandler;
    private DownloadManager downloadManager;
    private LinearLayout mTitleLlyt;
    private TextView searchKey_Tv;
    private TextView updateNum_Iv;
    /**
     * 更新提示小红点
     */
    private TextView caution_update_Iv;
    private ParabolaView parabolaView;
    private static final int UPLOAD_WHAT = 1;
    private static final int UPDATA_CALLBACK = UPLOAD_WHAT + 1;
    private static final int DOWNLOAD_TASK = UPDATA_CALLBACK + 1;
    private static final int SHORTCUT_TASK = DOWNLOAD_TASK + 1;
    private static final int SCROLL = 100;
    /**
     * 自动滚动
     */
    private boolean isAutoScroll = true;
    /**
     * 推荐词滚动任务
     */
    private ArrayList<RecomandSearchWords> strs;
    private int mNumber = 0;
    private ImageView download_queue_default;
    private IUIDownLoadListenerImp refreshHanle;
    private IUpdateWatcherEtds wather = null;
    private UpdateSelfDialog mUpdateSelfDialog;
    private UpdateSelfDialog mSystemDialog;
    private boolean isShow = true;
    private boolean newMsgsFlag = false;
    private LocationService locationService;
    private BDLocationListener mListener;
    public boolean isHaveDialogShow = false;
    private Handler UIHandler = new MyHander(this);
    private final String pushdisplay_t = "pushDisPlay_Time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isNeedRegister = false;
        super.onCreate(savedInstanceState);
        if (!BaseApplication.isThird) {
            WindowMangerUtils.initStateBar(getWindow(), this);
        }
        thisActivity = this;

        DataStoreUtils.saveLocalInfo("thisActivity", "thisActivity");
        if (PreferencesUtils.getLong(this, pushdisplay_t, 0) == 0) {
            PreferencesUtils.putLong(this, pushdisplay_t, System.currentTimeMillis());
        }
        if (!BaseApplication.isThird) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_third);
        }
        //Overdraw 的处理移除不必要的background
//        getWindow().setBackgroundDrawable(null);
        if (!BaseApplication.isThird) {
            WindowMangerUtils.changeStatus(getWindow());
        }
        mToken = AIDLUtils.bindToService(this, this);
        refreshHanle = IUIDownLoadListenerImp.getInstance();
        refreshHanle.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(String arg0, int state, boolean isNewDownload) {
                if (!isAutoScroll) {
                    return;
                }
                if (state == DownloadState.STATE_DOWNLOAD_WAIT && !isNewDownload) {
                    return;
                }
                switch (state) {
                    case DownloadState.STATE_DOWNLOAD_WAIT:
                    case DownloadState.STATE_DOWNLOAD_SUCESS:
                    case DownloadState.STATE_DOWNLOAD_ERROR:
                    case DownloadState.STATE_DOWNLOAD_INSTALLED:
                    case DownloadState.STATE_DOWNLOAD_CANCEL:
                        changeFloatViewStates();
                        break;

                }

            }

        });
        wather = IUpdateWatcherEtds.getInstance();
        wather.setmCallBack(new IUpdateWatcherEtdsCallBack() {

            @Override
            public void update(int number, List<String> imgs,
                               List<AppsItemBean> listItem) {
                Message msg = Message.obtain();
                msg.what = UPDATA_CALLBACK;
                msg.arg1 = number;
                mNumber = number;
                msg.obj = listItem;
                UIHandler.removeMessages(UPDATA_CALLBACK);
                UIHandler.sendMessage(msg);
            }
        });

        findViewById();
        init();
        setOnClicListener();
        sendPushCast();
        checkNewVersion();
        requestUUID();
        setUpWindowTrisience();
        DataStoreUtils.saveLocalInfo("ENTERTIME", System.currentTimeMillis() + "");
    }

    private int history_versionCode;

    private void requestUUID() {
        Log.i(TAG, "TID=" + PreferencesUtils.getKEY_TID());
        Log.i(TAG, "versionCode=" + ClientInfo.getInstance().appVersionCode);
        history_versionCode = PreferencesUtils.getInt(this, DataStoreUtils.HISTORY_CODE);
        if (history_versionCode != ClientInfo.getInstance().appVersion) {
            //创建快捷键的逻辑
//            if(AppManagerCenter.getAppVersionCode("com.android.launcher3")>35){
//                new ShortCutTask(this).execute();
//            }
            PreferencesUtils.putInt(this, DataStoreUtils.HISTORY_CODE, ClientInfo.getInstance().appVersion);
        }
        if (TextUtils.isEmpty(PreferencesUtils.getKEY_TID())) {
            HttpUtils.getPidFromServer(new RequestPIDCallBack() {
                @Override
                public void requestOk(String pid) {
                    HttpUtils.getUuidFromServer(pid, new HttpUtils.RequestUuidCallBack() {
                        @Override
                        public void onSaveUuidOk(String uuid) {
                            UIUtils.registerXG(MainActivity.this);
                            AIDLUtils.registSelfPush();
                        }
                    });
                }
            });

        } else {
            UIUtils.registerXG(this);
        }

    }

    private static class ShortCutTask extends AsyncTask<String, Void, Void> {
        private final WeakReference<MainActivity> weakReference;

        ShortCutTask(MainActivity paletteImageView) {
            weakReference = new WeakReference<MainActivity>(paletteImageView);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (weakReference.get() != null) {
                MainActivity paletteImageView = weakReference.get();
                boolean result = ShortcutUtil.hasShortcut(paletteImageView.getApplicationContext());
                Message msg = Message.obtain();
                msg.what = SHORTCUT_TASK;
                msg.arg1 = result ? 1 : 0;
                if (params.length > 0) {
                    msg.arg2 = 88;
                }
                paletteImageView.UIHandler.sendMessage(msg);
            }
            return null;
        }


    }

    private boolean isFromShortCick = false;

    private void init() {
        mIndicator.setVisibility(View.VISIBLE);
        if (processIntent(getIntent())) {
            currentPage = getIntent().getIntExtra("position", 0);
        }
        if (getIntent() != null && getIntent().getStringExtra("key") != null) {
            isFromShortCick = true;
            sendBroadcast(new Intent(UserBroadcast.SHORTCUT_ACTION));
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "init-currentPage=" + currentPage);
        }
        initTabs();
        mIndicator.setViewPager(viewPager);
        mIndicator.setCurrentItem(currentPage);
        requestUpdate();
        getPreloadApp();
    }

    private void initTabs() {
        String newTid = CommonUtils.getNewTid();
        BaseApplication.isNeedStatic = JLog.isDebug || !TextUtils.isEmpty(newTid);
        Log.i(TAG, "NewTid=" + newTid);
        MainAdapter adapter = new MainAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);
    }

    private void findViewById() {
        download_queue_Rlyt = (RelativeLayout) findViewById(R.id.download_queue_Rlyt);
        caution_update_Iv = (TextView) findViewById(R.id.caution_update_Iv);
        mTitleLlyt = (LinearLayout) findViewById(R.id.linearLayout);
        searchKey_Tv = (TextView) findViewById(R.id.searchKey_Tv);
        updateNum_Iv = (TextView) findViewById(R.id.updateNum_Tv);
        viewPager = (CustomViewPager) findViewById(R.id.pager);
        mIndicator = (TabTextImagePageIndicator) findViewById(R.id.indicator);
        parabolaView = (ParabolaView) findViewById(R.id.parabolaView1);
        download_queue_default = (ImageView) findViewById(R.id.download_queue_line);
        viewPager.setScanScroll(false);
    }

    @SuppressLint("NewApi")
    private void setUpWindowTrisience() {
        if (VERSION.SDK_INT >= VERSION_CODES.L) {
            getWindow().setAllowEnterTransitionOverlap(true);
        }
    }

    /**
     * 请求更新数据
     */
    private void requestUpdate() {
        Intent intent = new Intent(this, PrizeAppCenterService.class);
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 3);
        startService(intent);
    }


    private void requestLocation() {
        String scanTime = DataStoreUtils
                .readLocalInfo(DataStoreUtils.TIME_FOR_BD);
        long scanTimeValue = 0L;
        if (!TextUtils.isEmpty(scanTime) && !scanTime.equals(DataStoreUtils.DEFAULT_VALUE)) {
            scanTimeValue = Long.parseLong(scanTime);
        }
        if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo("locationKey")) || ((System.currentTimeMillis() - scanTimeValue > 1000 * 3600 * 24 * 7))) {
            if (locationService == null) {
                locationService = new LocationService(getApplicationContext());
            }
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
            if (mListener == null) {
                mListener = new BDLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation location) {
                        if (locationService != null) {
                            locationService.stop(); //停止定位服务
                        }
//                        if (JLog.isDebug) {
//                            JLog.i("MainActivity", "onReceiveLocation-location--" + location.getLocType());
//                        }
                        if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                            LocationInfo info = new LocationInfo();
                            info.latitude = location.getLatitude();
                            info.lontitude = location.getLongitude();
                            info.address = location.getAddrStr();
                            DataStoreUtils
                                    .saveLocalInfo(DataStoreUtils.TIME_FOR_BD, String.valueOf(System.currentTimeMillis()));
                            DataStoreUtils.saveLocalInfo("locationKey", new Gson().toJson(info));
//                            if (JLog.isDebug) {
//                                JLog.i("MainActivity", "onReceiveLocation---" + info);
//                            }
                            ClientInfo.getInstance().latitude = info.latitude;
                            ClientInfo.getInstance().longitude = info.lontitude;
                            ClientInfo.getInstance().location = info.address;
                        }
                    }

                    public void onConnectHotSpotMessage(String s, int i) {
                    }
                };
            }
            locationService.registerListener(mListener);
            locationService.start();
        }
    }

    /**
     * 检测新版本
     */
    private void checkNewVersion() {
        String mUrl = Constants.SYSTEM_UPGRADE_URL;
        if (BaseApplication.isThird || BaseApplication.isOeder) {
            mUrl = Constants.THIRD_UPGRADE_URL;
        }
        RequestParams params = new RequestParams(mUrl);
        reqHandler = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {

                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 0) {

                        JSONObject o = new JSONObject(obj.getString("data"));
                        bean = GsonParseUtils.parseSingleBean(o.getString("app"),
                                AppsItemBean.class);
                        if (AppManagerCenter.appIsNeedUpate(bean.packageName,
                                bean.versionCode)) {
                            PreferencesUtils.putString(MainActivity.this,
                                    Constants.APP_MD5, bean.apkMd5);
                            if (downloadManager == null) {
                                downloadManager = (DownloadManager) MainActivity.this
                                        .getSystemService(Context.DOWNLOAD_SERVICE);
                            }
                            if ((new File(Constants.APKFILEPATH)).exists()) {
                                queryDownloadStatus();
                            } else {
                                String date = DataStoreUtils.readLocalInfo("DATE");
                                if (!TextUtils.isEmpty(date) && CommonUtils.isToday(date)) {
                                    if (bean != null && ClientInfo.networkType != ClientInfo.NONET) {
                                        UIHandler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                UpdateDataUtils.downloadApk(downloadManager, bean, getApplicationContext());
                                            }
                                        }, 3000);
                                    }
                                    return;
                                }
                                displayUpdateCaution();
                            }

                        } else {
                            File file = new File(Constants.APKFILEPATH);
                            if (file.exists()) {
                                file.delete();
                            }
                            PreferencesUtils.putString(MainActivity.this, Constants.APP_MD5, "");
                            getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_DATA=?", new String[]{Constants.APKFILEPATH});
                        }
//                        File file = new File(Constants.APK_OlD_PATH);
//                        if (file.exists()) {
//                            file.delete();
//                        }
//                        getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_DATA=?", new String[]{Constants.APK_OlD_PATH});

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

    private void setOnClicListener() {
        UpdateWatchedManager.registNetConnectedListener(this);
        download_queue_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        AppDownLoadQueenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                MTAUtil.onClickNavDownloadManage();


//                UIUtils.testGo(MainActivity.this);
//                Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), null, "_DATA=?", new String[]{Constants.APK_OlD_PATH}, null);
//                Log.i(TAG, "cursor:"+cursor);
//                if (cursor != null) {
//                    try {
//                        if (cursor.moveToFirst()) {
//                            int conut=cursor.getColumnCount();
//                            for(int i=0;i<conut;i++){
//                                Log.i(TAG,"ColumnName["+i+"]="+ cursor.getColumnName(i));
//                                Log.i(TAG, cursor.getString(i)==null?"kong":cursor.getString(i));
//
//                            }
//                        }
//                    }catch (Exception e){
//                        Log.i(TAG, "e:"+e);
//                    }finally {
//                        if(cursor!=null){
//                            cursor.close();
//                        }
//                    }
//                }
////                getContentResolver().delete(MediaStore.Files.getContentUri("external"), "_DATA=?", new String[]{Constants.APK_OlD_PATH});
            }
        });
        searchKey_Tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        SearchActivity.class);
                if (currentLocation >= 0 && strs != null && !strs.isEmpty() && currentLocation < strs.size()) {
                    intent.putExtra(SearchActivity.STR, strs.get(currentLocation).value);
                }
                startActivity(intent);
                MTAUtil.onClickNavSearch();
            }
        });
        mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                //释放所有视频
                JCVideoPlayer.releaseAllVideos();
                PriorityRunnable.decreaseBase();
                // 友盟统计
                if (currentPage != position) {
                    pagers[currentPage].onPause(); // 前一页pause
                    pagers[position].onResume();
                    if (currentPage == MINE_PAGER_ID) {
                        WindowMangerUtils.changeStatus(getWindow());
                    }
                }
                currentPage = position;

                homePage.setAutoScroll(position == HOME_PAGER_ID);
                mAppCategoryPager.setAutoScroll(position == APP_CATEGORY_PAGER_ID);
                onlinepage.setAutoScroll(position == GAME_ONLINE_PAGE_ID);
                pagers[position].loadData();
                responseChangeTab(position);
                if (MINE_PAGER_ID == position) {
                    MTAUtil.onClickNavUserPage();
                    WindowMangerUtils.changeStatusWhite(getWindow());
                }
                MTAUtil.onClickMenu(pagers[currentPage].getPageName());

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

        mIndicator.setOnTabReselectedListener(mOnTabReselectedListener);
    }

    private TabTextImagePageIndicator.OnTabReselectedListener mOnTabReselectedListener = new TabTextImagePageIndicator.OnTabReselectedListener() {
        @Override
        public void onTabReselected(int position) {
            if (currentPage == position) {
                pagers[currentPage].scrollToTop();
            }
        }
    };

    /**
     * 响应tab切换时title栏的状态
     *
     * @param position tab位置
     */
    private void responseChangeTab(int position) {
        if (mTitleLlyt == null) {
            return;
        }

        /*
         * 我的里面隐藏tab
         */
        if (position == MINE_PAGER_ID) {
            mTitleLlyt.setVisibility(View.GONE);
        } else {
            mTitleLlyt.setVisibility(View.VISIBLE);
        }
        /*
         * 是否显示小红点
         */
        if (position != MINE_PAGER_ID && (mNumber > 0 || newMsgsFlag)) {
            caution_update_Iv.setVisibility(View.VISIBLE);
        } else {
            caution_update_Iv.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        pagers[currentPage].onResume();
        pagers[currentPage].setAutoScroll(true);
        isAutoScroll = true;
        changeFloatViewStates();
        displayDialog();
        UIUtils.clearAllActivity();
        if (!isFromShortCick && history_versionCode == ClientInfo.getInstance().appVersion) {
            long lastT = PreferencesUtils.getLong(this, pushdisplay_t, 0);
            if (lastT != 0 && System.currentTimeMillis() - lastT >= 7 * 24 * 60 * 60 * 1000) {//大于等于一周
//                if (AppManagerCenter.getAppVersionCode("com.android.launcher3") > 35) {
//                    new ShortCutTask(this).execute("push");
//                }
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        //释放所有视频
        JCVideoPlayer.releaseAllVideos();
        pagers[currentPage].onPause();
        pagers[currentPage].setAutoScroll(false);
        isAutoScroll = false;

        super.onPause();
    }

    private class MainAdapter extends PagerAdapter implements IconTextPagerAdapter {

        MainAdapter() {
            // 首页
            homePage = new HomePager(MainActivity.this);
            homePage.getView(); // 需要初始化，原因：page 可以跳转
            // home.setPager(viewPager);
            pagers[HOME_PAGER_ID] = homePage;

            if (currentPage == HOME_PAGER_ID) {
                homePage.loadData();
            }
//            if (!processIntent(getIntent())) {
//                homePage.loadData();
//            }
            pagers[TAB_RANK_ID] = new CommRankPager(MainActivity.this);
            pagers[TAB_RANK_ID].getView(); // 需要初始化，原因：page 可以跳转
            if (currentPage == TAB_RANK_ID) {
                pagers[TAB_RANK_ID].onResume();
            }

            // 应用页
            mAppCategoryPager = new AppTypePagerGifts(MainActivity.this, false);
            pagers[APP_CATEGORY_PAGER_ID] = mAppCategoryPager;
            pagers[APP_CATEGORY_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转

            // 游戏
            onlinepage = new GameTypePagerGifts(MainActivity.this, true);
            pagers[GAME_ONLINE_PAGE_ID] = onlinepage;
            pagers[GAME_ONLINE_PAGE_ID].getView(); // 需要初始化，原因：page 可以跳转
            if (currentPage == GAME_ONLINE_PAGE_ID) {
                pagers[GAME_ONLINE_PAGE_ID].loadData();
            }
//            if (processIntent(getIntent())) {
//                int position = getIntent().getIntExtra("position", 0);
//                if (position == GAME_ONLINE_PAGE_ID) {
//                    onlinepage.loadData();
//                }
//            }
            // 我的
            mPersonalCenter = new PersonalCenterPager(MainActivity.this);
            pagers[MINE_PAGER_ID] = mPersonalCenter;
            pagers[MINE_PAGER_ID].getView(); // 需要初始化，原因：page 可以跳转
            mPersonalCenter.setListener(new PersonalCenterPager.OnMessageCheckedListener() {
                @Override
                public void onMessageCheckFinish(boolean flag) {
                    newMsgsFlag = flag;
                }
            });

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(pagers[position].getView());
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            container.addView(pagers[position].getView());
            return pagers[position].getView();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            int id = ICONS[position % ICONS.length];
            return getString(id);
        }

        @Override
        public int getCount() {
            return ICONS.length;
        }

        @Override
        public int getIconResId(int index) {
            return ICONSRES[index];
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            default:
                break;
        }
    }


    @Override
    /*
     *  实现按两次Back键退出
     */
    public void onBackPressed() {
        if (JCVideoPlayer.backPress()) {
            return;
        }
        long currentPressBackTime = System.currentTimeMillis();
        if (currentPressBackTime - lastPressBackTime < PRESS_BACK_TIME) {
            finish();
        } else {
            ToastUtils.showToast(R.string.toast_exit, Gravity.BOTTOM);
        }
        lastPressBackTime = currentPressBackTime;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        UIUtils.gotoActivity(SettingActivity.class, MainActivity.this);
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", currentPage);
        if (pagers[currentPage] != null) {
            pagers[currentPage].onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        currentPage = savedInstanceState.getInt("position", 0);
        if (pagers[currentPage] != null) {
            pagers[currentPage].onRestoreInstanceState(savedInstanceState);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public String getActivityName() {
        return null; // 返回null ，因为需要统计page友盟统计
    }

    @Override
    protected void initActionBar() {
        enableSlideLayout(false);
    }

    /**
     * 查询下载任务个数
     */
    private void changeFloatViewStates() {
        if (!isAutoScroll) {
            return;
        }
        int size = AIDLUtils.getDownloadAppList().size();
        Message msg = Message.obtain();
        msg.what = DOWNLOAD_TASK;
        msg.arg1 = size;
        if (UIHandler == null)
            return;
        UIHandler.removeMessages(DOWNLOAD_TASK);
        UIHandler.sendMessage(msg);
    }

    /**
     * 发送取消提醒数字广播
     */
    private void sendPushCast() {
        // 软件更新提示设置
        CommonUtils.sendCautionBroadcast(this, 0);
    }


    @Override
    public void onNetConnected() {
        if (AppUpdateCache.getInstance().getCache() != null && AppUpdateCache.getInstance().getCache().size() > 0)
            return;
        requestUpdate();

    }

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(PreferencesUtils.getLong(this,
                Constants.KEY_NAME_DOWNLOAD_ID));
        Cursor c = downloadManager.query(query);
        if (c.moveToFirst()) {
            int status = c.getInt(c
                    .getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    // 正在下载，不做任何事情
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    // 完成
                    displayDialog();
                    break;
                case DownloadManager.STATUS_FAILED:
                    // 清除已下载的内容，重新下载
                    downloadManager.remove(PreferencesUtils.getLong(this,
                            Constants.KEY_NAME_DOWNLOAD_ID));
                    PreferencesUtils.putLong(this, Constants.KEY_NAME_DOWNLOAD_ID,
                            -1);
                    break;
            }
        }
    }


    public void setStrs(ArrayList<RecomandSearchWords> strs) {
        this.strs = strs;
        refresh();
        if (!UIHandler.hasMessages(SCROLL)) {
            UIHandler.sendEmptyMessageDelayed(SCROLL, 100);
        }
    }

    private int currentLocation = -1;

    public void refresh() {
        if (searchKey_Tv != null && strs != null && strs.size() > 0) {
            currentLocation = (int) (Math.random() * strs.size());
            if (currentLocation >= strs.size()) {
                return;
            }
            searchKey_Tv.setText(strs.get(currentLocation).key);
        }

    }

    /**
     * 下载飞入动画
     *
     * @param view ImageView
     */
    public void startAnimation(ImageView view) {
        parabolaView.setAnimationPara(view, download_queue_default);
        parabolaView.showMovie();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (JLog.isDebug) {
            JLog.i(TAG, "onNewIntent-intent=" + intent);
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("key"))) {
            isFromShortCick = true;
            sendBroadcast(new Intent(UserBroadcast.SHORTCUT_ACTION));
        }
        if (intent.getAction() != null && mIndicator != null) {
            mIndicator.setCurrentItem(2);
        }
        if (mIndicator != null && intent.getIntExtra("position", -1) == 0) {
            mIndicator.setCurrentItem(0);
        }
        if (mIndicator != null && intent.getIntExtra("position", -1) == 1) {
            mIndicator.setCurrentItem(1);
        }
        if (mIndicator != null && intent.getIntExtra("position", -1) == 3) {
            mIndicator.setCurrentItem(3);
            Intent GoIntent = new Intent("com.prize.pageOne");//跳转到游戏page首页
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(GoIntent);
        }
        super.onNewIntent(intent);
    }

    /**
     * 判断是否来自桌面启动 或者从积分商城跳转过来到第3个 item
     *
     * @param intent Intent
     * @return boolean
     */
    public boolean processIntent(Intent intent) {
        return intent != null && intent.getAction() != null
                && intent.getAction().equals(Intent.ACTION_MAIN) || (intent != null && intent.getIntExtra("position", 0) != 0);
    }


    /**
     * 三方版本提示安装包已经下载好
     */
    private void displayDialog() {
        if (bean == null || !BaseApplication.isThird || !isResumed() || !isShow)
            return;
        try {
            if (AppManagerCenter.appIsNeedUpate(bean.packageName,
                    bean.versionCode)) {
                File file = new File(Constants.APKFILEPATH);
                if (file.exists()) {
                    PackageInfo localPackageInfo = getPackageManager()
                            .getPackageArchiveInfo(Constants.APKFILEPATH, PackageManager.GET_ACTIVITIES);
                    if ((localPackageInfo != null)
                            && (bean.packageName
                            .equals(localPackageInfo.packageName))
                            && (this.bean.versionCode == localPackageInfo.versionCode)
                            && (MD5Util.Md5Check(Constants.APKFILEPATH,
                            bean.apkMd5))) {
                        if (mUpdateSelfDialog == null) {
                            mUpdateSelfDialog = new UpdateSelfDialog(
                                    MainActivity.this, R.style.add_dialog,
                                    ClientInfo.getInstance().appVersionCode,
                                    getResources().getString(
                                            R.string.new_version_name,
                                            bean.versionName), bean.apkSizeFormat, bean.updateInfo);
                            mUpdateSelfDialog.setBean(bean);
                        }
                        if (mUpdateSelfDialog != null
                                && !mUpdateSelfDialog.isShowing()) {
                            mUpdateSelfDialog.show();
                            isShow = false;
                        }
                    } else {
                        file.delete();
                    }
                }
            } else {
                isShow = false;
            }
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AIDLUtils.registObserver(wather);
        AIDLUtils.registerCallback(refreshHanle);
        AIDLUtils.pauseAllBackgroudDownload(MainActivity.this);
        pagers[currentPage].onResume();
        mPersonalCenter.registerListener();
        changeFloatViewStates();
        AIDLUtils.registSelfPush();
    }

    @Override
    public void finish() {
        if (JLog.isDebug) {
            JLog.i(TAG, "finish()");
        }
        thisActivity = null;
        DataStoreUtils.saveLocalInfo("thisActivity", "");
        overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
        super.finish();
        RootActivity.exitActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateWatchedManager.unregistNetConnectedListener(this);
        AIDLUtils.unregisterCallback(refreshHanle);
        AIDLUtils.unregistObserver(wather);
        wather.setmCallBack(null);
        wather = null;
        refreshHanle.setmCallBack(null);
        refreshHanle = null;
        if (reqHandler != null) {
            reqHandler.cancel();
        }
        /*
         * 排行榜替换
         */
        pagers[HOME_PAGER_ID].onDestroy();
        pagers[TAB_RANK_ID].onDestroy();
        pagers[APP_CATEGORY_PAGER_ID].onDestroy();
        pagers[GAME_ONLINE_PAGE_ID].onDestroy();
        pagers[MINE_PAGER_ID].onDestroy();
        if (UIHandler != null) {
            UIHandler.removeCallbacksAndMessages(null);
        }
        BaseApplication.cancelPendingRequests(TAG);
        if (strs != null) {
            strs.clear();
        }
        AIDLUtils.unbindFromService(mToken);
        mToken = null;
//        UpdateCach.getInstance().cleanCache();
        ImageLoader.getInstance().clearMemoryCache();
        Glide.get(this).clearMemory();
        AppUpdateCache.getInstance().clearCache();
        if (strs != null) {
            strs.clear();
            strs = null;
        }
        DataStoreUtils.saveLocalInfo("ENTERTIME", "");
        if (locationService != null && mListener != null) {
            locationService.unregisterListener(mListener); //注销掉监听
            locationService.stop(); //停止定位服务
        }
    }

    /**
     * 弹出升级提醒对话框
     */
    private void displayUpdateCaution() {
        if (bean == null || !isResumed() || isHaveDialogShow) {
            if (bean != null && ClientInfo.networkType != ClientInfo.NONET) {
                UIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UpdateDataUtils.downloadApk(downloadManager, bean, MainActivity.this.getApplicationContext());
                    }
                }, 3000);
            }
            return;
        }
        if (mSystemDialog == null) {
            mSystemDialog = new UpdateSelfDialog(MainActivity.this,
                    R.style.add_dialog,
                    ClientInfo.getInstance().appVersionCode, getResources()
                    .getString(R.string.new_version_name,
                            bean.versionName), bean.apkSizeFormat,
                    bean.updateInfo);
            mSystemDialog.setBean(bean);
        }
        if (mSystemDialog != null && !mSystemDialog.isShowing()) {
            if (!isHaveDialogShow) {
                mSystemDialog.show();
                isHaveDialogShow = true;
            }
        }
        DataStoreUtils.saveLocalInfo("DATE", System.currentTimeMillis() + "");

    }


    /**
     * 获取服务器推送设置信息
     */
    private void getPreloadApp() {
        if (BaseApplication.isThird)
            return;
        if (TextUtils.isEmpty(DataStoreUtils.readLocalInfo(Constants.PREALOADS)) || TextUtils.isEmpty(DataStoreUtils.readLocalInfo(Constants.SHIELDPACKAGES)) || !CommonUtils.isToday(DataStoreUtils.readLocalInfo("APPGETTIME"))) {
            if (ClientInfo.networkType == ClientInfo.NONET)
                return;
            String url = Constants.GIS_URL + "/push/setting";
            RequestParams reqParams = new RequestParams(url);
            XExtends.http().post(reqParams, new PrizeXutilStringCallBack<String>() {

                @Override
                public void onSuccess(String result) {
                    JSONObject obj;
                    try {
                        obj = new JSONObject(result);
                        int code = obj.optInt("code");
                        if (code == 0) {
                            PushTimeBean pushTimeBean = new Gson().fromJson(
                                    obj.optString("data"), PushTimeBean.class);
                            if (pushTimeBean != null && !TextUtils.isEmpty(pushTimeBean.shieldPackages)) {
                                DataStoreUtils.saveLocalInfo(Constants.SHIELDPACKAGES, pushTimeBean.shieldPackages);
                                DataStoreUtils.saveLocalInfo(Constants.PREALOADS, pushTimeBean.prealoads);
                                DataStoreUtils.saveLocalInfo("APPGETTIME", System.currentTimeMillis() + "");
                                //上传已经激活的应用
                                Intent intent = new Intent(MainActivity.this, PrizeAppCenterService.class);
                                intent.putExtra(PrizeAppCenterService.OPT_TYPE, 10);
                                intent.putExtra(Constants.PREALOADS, pushTimeBean.prealoads);
                                MainActivity.this.startService(intent);
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationService != null && mListener != null) {
            locationService.unregisterListener(mListener); //注销掉监听
            locationService.stop(); //停止定位服务
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestLocation();
    }

    private static class MyHander extends Handler {
        private WeakReference<MainActivity> mActivities;

        MyHander(MainActivity mActivity) {
            this.mActivities = new WeakReference<MainActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final MainActivity activity = mActivities.get();
            if (activity != null) {
                switch (msg.what) {
                    case SCROLL:
                        if (activity.isAutoScroll && (null != activity.searchKey_Tv)) {
                            activity.refresh();
                            removeMessages(SCROLL);
                        }
                        sendEmptyMessageDelayed(SCROLL, 2000);
                        break;

                    case UPDATA_CALLBACK:// 回调可以更新的app
                        List<AppsItemBean> data = (List<AppsItemBean>) msg.obj;
                        AppUpdateCache.getInstance().saveCache(data);
                        if (activity.caution_update_Iv != null) {
                            activity.caution_update_Iv.setVisibility((msg.arg1 > 0 || activity.newMsgsFlag)
                                    && activity.currentPage != MINE_PAGER_ID ? View.VISIBLE
                                    : View.GONE);
                        }
                        break;
                    case DOWNLOAD_TASK:// 回调下载任务
                        int size = msg.arg1;
                        if (activity.download_queue_default == null || activity.updateNum_Iv == null)
                            return;
                        if (size > 0) {
                            activity.download_queue_default.setVisibility(View.GONE);
                            activity.updateNum_Iv.setVisibility(View.VISIBLE);
                        } else {
                            activity.download_queue_default.setVisibility(View.VISIBLE);
                            activity.updateNum_Iv.setVisibility(View.GONE);
                        }
                        break;
                    case SHORTCUT_TASK:// 回调快捷键任务
                        int state = msg.arg1;
                        if (state == 0) {
                            if (msg.arg2 == 88) {
                                activity.processNotification();
                            } else {
                                //弹窗
                                if (!activity.isHaveDialogShow) {
                                    ShortCutCautionDialog mShortCutCautionDialog = new ShortCutCautionDialog(activity, R.style.add_dialog);
                                    mShortCutCautionDialog.show();
                                }
                                activity.isHaveDialogShow = true;

                            }
                        }
                        break;
                    default:

                        break;

                }
            }
        }
    }

    private void processNotification() {
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.putExtra("key", "shortcutNotification");
        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 1000, openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this);
        if (!CommonUtils.isScreenLocked(MainActivity.this)) {
            builder.setFullScreenIntent(null, true);
        }
        RemoteViews views = new RemoteViews(MainActivity.this.getPackageName(),
                R.layout.notification_xg_app);

        views.setImageViewResource(R.id.big_Iv, R.drawable.push_icon);
        views.setTextViewText(R.id.title_tv, "热门应用下载更方便");
        views.setTextViewText(R.id.content_tv, "小伙伴们都在用，立即体验→");

        Notification notification = builder.build();
        notification.contentView = views;
        notification.contentIntent = contentIntent;
        notification.icon = R.drawable.push_icon;
        notification.defaults = Notification.DEFAULT_ALL;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if (notificationManager != null) {
            notificationManager.notify(1000, notification);
            PreferencesUtils.putLong(this, pushdisplay_t, System.currentTimeMillis());
        }

    }


}