package com.prize.prizenavigation;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.prizenavigation.adapter.NaviFragmentStatePagerAdapter;
import com.prize.prizenavigation.bean.AppsItemBean;
import com.prize.prizenavigation.bean.ClientInfo;
import com.prize.prizenavigation.bean.NaviDatas;
import com.prize.prizenavigation.fragment.NaviFragment;
import com.prize.prizenavigation.manager.NaviDatasManager;
import com.prize.prizenavigation.receiver.NavigationNetReceiver;
import com.prize.prizenavigation.utils.Constants;
import com.prize.prizenavigation.utils.DataStoreUtils;
import com.prize.prizenavigation.utils.FileUtils;
import com.prize.prizenavigation.utils.IConstants;
import com.prize.prizenavigation.utils.LogUtil;
import com.prize.prizenavigation.utils.MD5Util;
import com.prize.prizenavigation.utils.PermissionUtils;
import com.prize.prizenavigation.utils.PreferencesUtils;
import com.prize.prizenavigation.utils.ToastUtils;
import com.prize.prizenavigation.utils.UIUtils;
import com.prize.prizenavigation.utils.UpdateDataUtils;
import com.prize.prizenavigation.utils.WindowMangerUtils;
import com.prize.prizenavigation.view.TitleListPopWindow;
import com.prize.prizenavigation.view.UpdateSelfDialog;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fm.jiecao.jcvideoplayer_lib.JCVideoPlayer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, TitleListPopWindow.OnPopupWindowClickListener, NaviDatasManager.NaviDatasCallback {

    private ViewPager mViewPager;
    /**
     * 标题栏
     */
    private Toolbar mToolbar;
    /**
     * toolbar帧布局
     */
    private FrameLayout mToolbarFl;

    private LinearLayout mLLNetError;
    /**
     * 列表
     */
    private ImageView mImgList;
    /**
     * 分享
     */
    private ImageView mImgShare;
    /**
     * 位置
     */
    private TextView textViewpos;
    /**
     * TitleListPopWindow
     */
    private TitleListPopWindow mTitlelistPop;
    /**
     * 内容链表
     */
    private List<NaviDatas.ListBean> datas = new ArrayList<NaviDatas.ListBean>();
    /**
     * 碎片链表
     */
    private List<NaviFragment> fragmentList = new ArrayList<NaviFragment>();
    /**
     * viewpager适配器
     */
    private NaviFragmentStatePagerAdapter mNaviAdapter;
    /**
     * 碎片管理者
     */
    private FragmentManager fragmentManager;
    private int pos;
    /**
     * 数据获取管理者
     */
    private NaviDatasManager datasManager;
    /**
     * 网络状态变化接受者
     */
    private NavigationNetReceiver mNetStateReceiver;

    /**
     * 最后一次点击时间
     */
    private long lastClickTime = 0;
    private int mClick = 0;

    private Callback.Cancelable reqHandler;
    private AppsItemBean bean;
    private DownloadManager downloadManager;
    private Handler mHander;
    private UpdateSelfDialog mUpdateSelfDialog;
    private boolean isShow = true;
    private TextView toolbartv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowMangerUtils.initStateBar(getWindow(), this);
        setContentView(R.layout.activity_main);
        WindowMangerUtils.changeStatus(getWindow());

        initView();

        loadDatas();

        checkNewVersion();

        registerNetworkReceiver();

    }

    /**
     * //初始化控件
     */
    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.main_tb);
        mToolbarFl = (FrameLayout) findViewById(R.id.toolbar_fl);
        mImgList = (ImageView) mToolbarFl.findViewById(R.id.toolbar_list_iv);
        mImgShare = (ImageView) mToolbarFl.findViewById(R.id.toolbar_share_iv);
        mLLNetError = (LinearLayout) findViewById(R.id.main_net_error_ll);
        textViewpos = (TextView) findViewById(R.id.vp_center_pos);

        toolbartv = (TextView) mToolbarFl.findViewById(R.id.toolbar_title);
        toolbartv.setOnClickListener(this);

        mToolbar.setTitle(null);
        mToolbar.setNavigationIcon(null);
        mToolbar.setSubtitle(null);
        mImgList.setOnClickListener(this);
        mImgShare.setOnClickListener(this);
        mLLNetError.setOnClickListener(this);
        setSupportActionBar(mToolbar);

        mViewPager = (ViewPager) findViewById(R.id.main_vp);
        mViewPager.addOnPageChangeListener(this);
        // 若设置了该属性 则viewpager会缓存指定数量的Fragment
        mViewPager.setOffscreenPageLimit(IConstants.VIEWPAGER_OFF_SCREEN_PAGE_LIMIT);
        fragmentManager = getSupportFragmentManager();
        mNaviAdapter = new NaviFragmentStatePagerAdapter(fragmentManager);

        mTitlelistPop = new TitleListPopWindow(NavigationApplication.getContext());
        mTitlelistPop.setOnPopupWindowClickListener(this);

        datasManager = NaviDatasManager.getInstance();
    }

    /**
     * 数据加载
     */
    private void loadDatas() {
        //bug 52507
        PermissionUtils.requestPermissions(this,200,
                new String[]{Manifest.permission.READ_PHONE_STATE}, new  PermissionUtils.OnPermissionListener(){

            @Override
            public void onPermissionGranted() {

                datasManager.getNaviDatas(MainActivity.this);

            }

            @Override
            public void onPermissionDenied(String[] deniedPermissions) {

                ToastUtils.showToast("无权限!");
                loadDatas();
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(MainActivity.this,requestCode,permissions,grantResults);
    }

    /**
     * toolbar点击事件
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.toolbar_list_iv:
//                ToastUtils.showToast("点击了列表");
                if (ClientInfo.networkType == 0 && (datas == null || datas.size() <= 0)) {
                    ToastUtils.showErrorToast(R.string.net_error);
                    return;
                }
                if (mTitlelistPop != null && mImgList != null) {
                    if (mTitlelistPop.isShowing()) {
                        mImgList.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_iv_list_selector));
                        mTitlelistPop.dismiss();
                    } else {
                        mImgList.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_iv_back_selector));
                        mTitlelistPop.showAsDropDown(mToolbar);
                    }
                }

                break;
            case R.id.toolbar_share_iv:
//                ToastUtils.showToast("点击了分享");
                LogUtil.i("lk", "点击了分享");
                //如果当前列表为打开状态 先关闭
                if (mTitlelistPop != null && mImgList != null) {
                    if (mTitlelistPop.isShowing()) {
                        mImgList.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_iv_list_selector));
                        mTitlelistPop.dismiss();
                    }
                }
                try {
                    if (ClientInfo.networkType == 0) {
                        ToastUtils.showErrorToast(R.string.net_error);
                        return;
                    }
                    if (!UIUtils.isFastClick(1500)) {
                        String id = datas.get(pos).getId();
                        String name = datas.get(pos).getTitle();
                        String content = datas.get(pos).getContent();
                        FileUtils.screenShot(this, id, name);
                        UIUtils.shareImage(this, content, name, FileUtils.getDownloadDir() + name + id + ".png");
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
                break;
            case R.id.toolbar_title:
                long time = System.currentTimeMillis();
                if (time - lastClickTime < 1000) {
                    mClick++;
                    if (mClick == 3) {
                        ToastUtils.showOneToast(
                                "当前版本号"
                                        + ClientInfo.getInstance().appVersionCode
                                        + "-"
                                        + ClientInfo.getInstance().appVersion
                        );
                        mClick = 0;
                    }
                }
                lastClickTime = time;

                break;
            case R.id.main_net_error_ll:
                if (datas != null && datas.size() > 0) {
                    //数据不为空则刷新
                    mNaviAdapter.refreshAllFragment(datas);
                    mTitlelistPop.changeData(datas);
                } else if (ClientInfo.networkType != ClientInfo.NONET && datasManager != null) {
                    //第一次进入数据为空则请求
                    datasManager.getNaviDatas(this);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(mNetStateReceiver);
        this.setContentView(R.layout.empty_view);
        datas = null;
        datasManager = null;
    }

    /**
     * viewpager滑动监听事件 start
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        LogUtil.i("vp", "onPageScrolled" + position);
    }

    @Override
    public void onPageSelected(int position) {
        LogUtil.i("vp", "onPageSelected" + position);
//        ToastUtils.showToast("当前位置" + position);
        pos = position;
        if (datas != null && datas.size() > 0) {
            StringBuilder sbPos = new StringBuilder();
            sbPos.append(datas.get(pos).getPagenum()).append("/").append(datas.size());
            textViewpos.setText(sbPos);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        LogUtil.i("vp", "onPageScrollStateChanged" + state);

    }

    /**viewpager滑动监听事件 end*/

    /**
     * listview列表点击事件
     *
     * @param position 位置
     */
    @Override
    public void onPopupWindowItemClick(int position) {
//        ToastUtils.showToast("点击了位置" + position);
        mViewPager.setCurrentItem(position, false);
        mImgList.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_iv_list_selector));
    }

    /**
     * 获取数据成功回调
     *
     * @param datasList
     * @param total
     */
    @Override
    public void onSuccess(List<NaviDatas.ListBean> datasList, int total) {
        if (datasList != null && datasList.size() > 0 && total > 0 && datasList.size() == total) {
            datas = datasList;
            mNaviAdapter.init(datas);
            mViewPager.setAdapter(mNaviAdapter);
            mTitlelistPop.changeData(datas);

            StringBuilder sbPos = new StringBuilder();
            sbPos.append(datas.get(pos).getPagenum()).append("/").append(datas.size());
            textViewpos.setText(sbPos);

            if (mLLNetError != null)
                mLLNetError.setVisibility(View.GONE);
        } else {
            if (mLLNetError != null)
                mLLNetError.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取数据失败回调
     *
     * @param e
     * @param datasList
     */
    @Override
    public void onFail(Exception e, List<NaviDatas.ListBean> datasList) {
        LogUtil.e("lk", "NaviDatasManager.getNaviDatas()" + e.toString());
        ToastUtils.showErrorToast(R.string.net_error);
        //网络错误并且数据为空
        if (mLLNetError != null) {
            mLLNetError.setVisibility(View.VISIBLE);
        } else
            {
            mLLNetError.setVisibility(View.GONE);
        }
    }


    /**
     * 监听网络状态改变 及时更新数据
     */
    private void registerNetworkReceiver() {
        mNetStateReceiver = new NavigationNetReceiver();
        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetStateReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        //37358 【提示V1.1】点击左上角展开按钮进入列表后，按back键会直接退出提示（附视频，100%）
        if (mTitlelistPop != null && mImgList != null) {
            if (mTitlelistPop.isShowing()) {
                mImgList.setImageDrawable(getResources().getDrawable(R.drawable.toolbar_iv_list_selector));
                mTitlelistPop.dismiss();
                return;
            }
        }


        if (JCVideoPlayer.backPress()) {
            return;
        }

        super.onBackPressed();
    }

    /**
     * 检测新版本
     *
     * @return void
     */
    public void checkNewVersion() {
        String mUrl = Constants.SYSTEM_UPGRADE_URL;
        RequestParams params = new RequestParams(mUrl);

        reqHandler = x.http().post(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject obj = new JSONObject(result);
                    if (obj.getInt("code") == 00000) {
                        JSONObject o = new JSONObject(obj.getString("data"));
                        bean = new Gson().fromJson(o.getString("app"),
                                AppsItemBean.class);
                        if (appIsNeedUpate(bean.packageName,
                                bean.versionCode)) {
                            LogUtil.i("lk", "appIsNeedUpate");
                            PreferencesUtils.putString(MainActivity.this,
                                    Constants.APP_MD5, bean.apkMd5);
                            if (downloadManager == null) {
                                downloadManager = (DownloadManager) MainActivity.this
                                        .getSystemService(Context.DOWNLOAD_SERVICE);
                            }
                            if ((new File(Constants.APKFILEPATH)).exists()) {
                                queryDownloadStatus();
                            } else {
                                if (ClientInfo.networkType != ClientInfo.NONET) {
                                    String wifiSettingString = DataStoreUtils
                                            .readLocalInfo(DataStoreUtils.DOWNLOAD_WIFI_ONLY);
                                    if (ClientInfo.networkType != ClientInfo.WIFI) {
                                        if (wifiSettingString
                                                .equals(DataStoreUtils.DOWNLOAD_WIFI_ONLY_ENABLE)) {
                                            return;
                                        }
                                    }
                                    if (mHander == null) {
                                        mHander = new Handler();
                                    }
                                    mHander.postDelayed(new Runnable() {
                                        public void run() {
                                            UpdateDataUtils.downloadApk(
                                                    downloadManager, bean,
                                                    MainActivity.this);
                                        }
                                    }, 3000);
                                }
                            }
                        } else {
                            File file = new File(Constants.APKFILEPATH);
                            if (file.exists()) {
                                file.delete();
                            }
                            PreferencesUtils.putString(MainActivity.this,
                                    Constants.APP_MD5, "");
                            getContentResolver().delete(
                                    MediaStore.Files.getContentUri("external"),
                                    "_DATA=?",
                                    new String[]{Constants.APKFILEPATH});
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

    /**
     * 判断是否要更新版本(getPackageArchiveInfo)，根据versionCode来判断
     *
     * @param packageName
     * @param versionCode
     * @return
     */
    public static boolean appIsNeedUpate(String packageName, int versionCode) {
        try {
            ApplicationInfo applicationInfo = NavigationApplication.getContext()
                    .getPackageManager().getApplicationInfo(packageName, 0);

            if (!isNewMethod()) {
                return applicationInfo.versionCode < versionCode;
            }
            PackageInfo packageInfo = NavigationApplication.getContext()
                    .getPackageManager().getPackageArchiveInfo(
                            applicationInfo.publicSourceDir, 0);
            LogUtil.i("lk", "versioncode=" + packageInfo.versionName + packageInfo.versionCode);
            if (packageInfo != null) {
                return packageInfo.versionCode < versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return false;
    }

    public static boolean isNewMethod() {
        return "1".equals(SystemProperties.get("ro.prize_app_update_appcenter", "0"));
    }

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(PreferencesUtils.getLong(this,
                Constants.KEY_NAME_DOWNLOAD_ID));
        LogUtil.i("lk", "downloadid == " + PreferencesUtils.getLong(this,
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
                    LogUtil.i("lk", "STATUS_SUCCESSFUL");
                    // 完成
                    displayDialog();
                    break;
                case DownloadManager.STATUS_FAILED:
                    // 清除已下载的内容，重新下载
                    LogUtil.i("lk", "STATUS_FAILED");
                    downloadManager.remove(PreferencesUtils.getLong(this,
                            Constants.KEY_NAME_DOWNLOAD_ID));
                    PreferencesUtils.putLong(this, Constants.KEY_NAME_DOWNLOAD_ID,
                            -1);
                    break;
            }
        }
    }

    /**
     * 更新弹框
     */
    private void displayDialog() {
//		JLog.i("hu","bean=="+bean.downloadUrlCdn + "--isResumed()=="+isResumed()+ "--isShow()=="+isShow);
        if (bean == null || !isResumed() || !isShow)
            return;
        try {
            if (appIsNeedUpate(bean.packageName,
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
                                            bean.versionName), bean.updateInfo);
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
        } catch (Resources.NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
