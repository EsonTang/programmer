/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.app.net.datasource.base.AppUninstallData;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.bean.AppInfo;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.AppUninstallListViewAdapter;
import com.prize.appcenter.ui.util.AppUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 类描述：应用卸载应用静默卸载和第三方卸载
 *
 * @author huangchangguo
 * @version 版本1.7
 */
public class AppUninstallActivity extends ActionBarNoTabActivity {

    private RelativeLayout mDdefaultLlyt;
    // private DownLoadedObserver downLoadedResolver;
    private ListView mListView;
    private AppUninstallListViewAdapter mAdapter;
    // private ArrayList<AppInfo> mSystemInfos;
    private ArrayList<AppInfo> mUserInfos;
    private List<AppInfo> appInfos;
    private AppUninstallData data;
    private final String TAG = "AppUninstallActivity";
    private AppUninstallReceiver mReceiver;
    private View noLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 设置启动加载页
        setNeedAddWaitingView(true);

        setContentView(R.layout.app_uninstall_layout);

        WindowMangerUtils.changeStatus(getWindow());

        setTitle(this.getString(R.string.app_uninstall));
        // 初始化布局
        initView();

        initData();

        initReceiver();
    }

    private void initReceiver() {
        // 注册移除监听
        IntentFilter filterRemove = new IntentFilter();
        filterRemove.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filterRemove.addDataScheme("package");

        mReceiver = new AppUninstallReceiver();
        registerReceiver(mReceiver, filterRemove);

    }

    private void initView() {
        // 无卸载应用默认页面
        mDdefaultLlyt = (RelativeLayout) findViewById(R.id.app_uninstall_defalutRlyt_id);

        mListView = (ListView) findViewById(R.id.app_uninstall_list_id);
        noLoading = LayoutInflater.from(this).inflate(
                R.layout.footer_nomore_show, null);

    }

    private void initData() {

        final String packageName = this.getPackageName();

        new Thread() {

            public void run() {

                synchronized (AppUninstallActivity.class) {

                    appInfos = AppUtil.getAllApp(AppUninstallActivity.this.getApplicationContext());
                    mUserInfos = new ArrayList<AppInfo>();

                    // 开始分类所有程序
                    for (AppInfo appInfo : appInfos) {
                        // 如果是系统程序，就放到系统集合中
                        // if (appInfo.isSystemApp) {
                        // mSystemInfos.add(appInfo);
                        // } else {
                        // 反之，就是用户程序
                        // 自己是第三方包的话，不存入appInfo
                        if (appInfo.mPackageName.equals(packageName)) {
                            continue;
                        } else {
                            mUserInfos.add(appInfo);
                        }

                        // }
                    }
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        mAdapter = new AppUninstallListViewAdapter(
                                AppUninstallActivity.this, mUserInfos);
                        mListView.setAdapter(mAdapter);

                        hideWaiting();

                        if (mUserInfos.size() <= 0) {
                            isShowDefaultView(true);
                        } else {
                            if (mUserInfos.size() >= 7) {
                                mListView.addFooterView(noLoading);
                            }
                        }
                    }
                });

            }

        }.start();

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

    public void isShowDefaultView(boolean isShowDefaultView) {
        if (isShowDefaultView) {
            mListView.setVisibility(View.GONE);
            mDdefaultLlyt.setVisibility(View.VISIBLE);
        } else {
            mDdefaultLlyt.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initActionBar() {

        enableSlideLayout(false);
        findViewById(R.id.action_bar_tab).setVisibility(View.VISIBLE);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    // 返回
                    case R.id.back_IBtn:
                        onBackPressed();
                        break;
                    case R.id.action_bar_search:
                        UIUtils.goSearchActivity(AppUninstallActivity.this);
                        break;
                    case R.id.action_go_downQueen:
                        UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
                                AppUninstallActivity.this);
                        break;
                }
            }
        };

        // 增加点击返回的灵敏度
        findViewById(R.id.action_go_downQueen).setOnClickListener(
                onClickListener);
        findViewById(R.id.back_IBtn).setOnClickListener(onClickListener);
        findViewById(R.id.action_bar_search)
                .setOnClickListener(onClickListener);
        TextView title = (TextView) findViewById(R.id.bar_title);
        title.setText(R.string.app_uninstall);
        title.setOnClickListener(onClickListener);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

        // TODO Auto-generated method stub

    }

    @Override
    public String getActivityName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void onDestroy() {
        if (mReceiver != null) {

            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        super.onDestroy();
    }

    /**
     * 广播监听卸载完成刷新Listview
     *
     * @author Administrator
     */
    private class AppUninstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // package:com.itheima.mobilesafe
            Uri uri = intent.getData();
            String data = uri.toString();
            JLog.e(TAG, data);
            String packageName = data.substring(data.indexOf(":") + 1);
            if (mUserInfos == null) {
                return;
            }
            Iterator<AppInfo> iterator = mUserInfos.iterator();
            while (iterator.hasNext()) {
                AppInfo appInfo = (AppInfo) iterator.next();
                if (packageName.equals(appInfo.mPackageName)) {
                    iterator.remove();
                    // 更新UI
                    if (mAdapter == null)
                        return;
                    mAdapter.notifyDataSetChanged();
                    // 设置展示无内容的界面
                    if (mUserInfos.size() <= 0) {
                        isShowDefaultView(true);
                    }
                    if (mUserInfos.size() < 7
                            && mListView.getFooterViewsCount() >= 1) {
                        mListView.removeFooterView(noLoading);
                    }
                    break;
                }

            }

        }
    }

    /**
     * 广播监听安装完成刷新Listview
     *
     * @author Administrator
     *
     */
    // private class AppInstallReceiver extends BroadcastReceiver {
    //
    // @Override
    // public void onReceive(Context context, Intent intent) {
    // // package:com.prizeappcenter
    // Uri uri = intent.getData();
    //
    // String data = uri.toString();
    // String packageName = data.substring(data.indexOf(":") + 1);
    // AppInfo appInfoAdded = AppUtil.getAppInfoByPackageName(
    // getApplicationContext(), packageName);
    //
    // mUserInfos.add(appInfoAdded);
    // // 更新UI
    // // 设置展示无内容的界面
    // mAdapter.notifyDataSetChanged();
    // // // 去除重复
    //
    // }
    // }

}
