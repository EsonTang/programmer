/*******************************************
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

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.AppSyncAssistantRestoreAdapter;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.PrizeCommButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 类描述：同步助手-恢复应用到本地
 *
 * @author huangchangguo
 *
 * @version 版本1.9 2016.7.21
 */
public class AppSyncAssistantrestoreActivity extends ActionBarNoTabActivity
        implements Observer {
    private final String TAG = "AppSyncAssistantrestoreActivity";
    private RecyclerView mRecyclerView;
    private TextView mRecyclerHeader;
    private PrizeCommButton mOnkeyDown;
    private Context mContext;
    private String userId;
    private ArrayList<AppsItemBean> mDatas = new ArrayList<>();
    private AppSyncAssistantRestoreAdapter mRestoreAdapter;
    private String mCalTatolSize;
    private DownDialog mDownDialog;
    private ArrayList<AppsItemBean> updateDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 设置启动加载页
        // setNeedAddWaitingView(true);
        mContext = getApplicationContext();

        setContentView(R.layout.activity_sync_restore_recyclerview);
        updateDatas = getIntent().getParcelableArrayListExtra("bean");
        WindowMangerUtils.changeStatus(getWindow());

        setTitle(this.getString(R.string.app_sync_restore));
        userId = getIntent().getStringExtra("userId");
        // 初始化布局
        initView();
        mToken = AIDLUtils.bindToService(this, this);
        initData();

    }

    private void initView() {

        mRecyclerView = (RecyclerView) findViewById(R.id.sync_restore_recyclerview);
        mRecyclerHeader = (TextView) findViewById(R.id.sync_restore_header_tv);
        mOnkeyDown = (PrizeCommButton) findViewById(R.id.sync_restore_oneKey_down_btn);
        mOnkeyDown.enabelDefaultPress(true);
        // 提高性能
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                mContext);

        mRecyclerView.setLayoutManager(layoutManager);

    }

    private void initData() {

        mRestoreAdapter = new AppSyncAssistantRestoreAdapter(this);

        mRecyclerView.setAdapter(mRestoreAdapter);
        mDatas.addAll(updateDatas);
        // AppSyncAssistantActivity.mFilterRestoreApp = null;
        // TODO

        mRestoreAdapter.addData(mDatas);
        mRecyclerHeader.setText("可恢复" + mDatas.size() + "款应用");

        mCalTatolSize = calTatolSize(mDatas);
        // 多了一个M单位
        if (mCalTatolSize.equals(".00B")) {
            mOnkeyDown
                    .setCurrentText(getString(R.string.sync_assistant_restore_onkey_down));
        } else {
            StringBuilder contentSize = new StringBuilder(
                    getString(R.string.sync_assistant_restore_onkey_down))
                    .append("(").append(mCalTatolSize).append(")");

            mOnkeyDown.setCurrentText(contentSize.toString());
        }

        //

        // 点击下载全部选中的App
        mOnkeyDown.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (updateDatas == null && updateDatas.size() < 0) {
                    return;
                }
                JLog.i(TAG, "mCalTatolSize------" + mCalTatolSize);
                if (mCalTatolSize.equals(".00B")) {
                    ToastUtils
                            .showToast(getString(R.string.sync_restore_not_checked_apps));
                    return;
                }

                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.nonet_connect);
                } else {
                    if (ClientInfo.networkType == ClientInfo.WIFI) {
                        startDownLoad();
                        int noInstalledAppCount = 0;
                        for (int i = 0; i < updateDatas.size(); i++) {
                            AppsItemBean itemBean = updateDatas.get(i);
                            if (itemBean != null) {
                                int state = AIDLUtils.getGameAppState(
                                        itemBean.packageName, itemBean.id,
                                        itemBean.versionCode);
                                switch (state) {
                                    case AppManagerCenter.APP_STATE_DOWNLOADING:
                                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                                    case AppManagerCenter.APP_STATE_UNEXIST:
                                    case AppManagerCenter.APP_STATE_UPDATE:
                                        noInstalledAppCount++;
                                        break;
                                }
                            }
                        }
                        if (noInstalledAppCount > 0) {
                            ToastUtils
                                    .showToast(getString(R.string.sync_restore_download));
                        }
                        finish();
                    } else {
                        mDownDialog = new DownDialog(
                                AppSyncAssistantrestoreActivity.this,
                                R.style.add_dialog);
                        mDownDialog.show();
                        mDownDialog.setmOnButtonClic(new OnButtonClic() {

                            @Override
                            public void onClick(int which) {

                                switch (which) {
                                    case 0:
                                        mDownDialog.dismiss();
                                        break;
                                    case 1:
                                        // 确定则开始下载

                                        startDownLoad();

                                        mDownDialog.dismiss();

                                        ToastUtils
                                                .showToast(getString(R.string.sync_restore_download));
                                        finish();
                                        break;
                                }
                            }
                        });

                    }
                }
            }
        });

    }

    // 计算大小
    private String calTatolSize(List<AppsItemBean> AppsData) {
        long total = 0;
        if (AppsData == null)
            return "";
        int size = AppsData.size();
        if (size <= 0)
            return "";
        for (int i = 0; i < size; i++) {
            if (AppsData.get(i).isCheck) {
                if (AppsData.get(i).apkSize == null) {
                    continue;
                }
                long apkSize = Long.parseLong(AppsData.get(i).apkSize);
                total += apkSize;
            }
        }
        return CommonUtils.formatSize(total, "#.00");
    }

    private void startDownLoad() {

        if (updateDatas == null)
            return;

        int size = updateDatas.size();

        int count = 0;
        for (int i = 0; i < size; i++) {
            AppsItemBean gameBean = updateDatas.get(i);
            // 只下载被选中的
            if (gameBean.isCheck) {

                int state = AIDLUtils.getGameAppState(gameBean.packageName,
                        String.valueOf(gameBean.id), gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        UIUtils.downloadApp(gameBean);
                        break;
                    case AppManagerCenter.APP_STATE_DOWNLOADED:
                        if (!BaseApplication.isThird) {
                            UIUtils.downloadApp(gameBean);
                            break;
                        }
                    case AppManagerCenter.APP_STATE_DOWNLOADING:
                    case AppManagerCenter.APP_STATE_WAIT:
                        break;
                    default:
                        count++;
                        if (count == size) {
                            ToastUtils.showToast(R.string.all_app_isdowned);
                        }
                        break;
                }
            }
        }

    }

    /**
     * 观察者update：恢复的App被选中的情况，实时刷新，显示被选中app的总大小
     */

    @Override
    public void update(Observable observable, Object data) {

        updateDatas = mRestoreAdapter.getAllData();

        mCalTatolSize = calTatolSize(updateDatas);

        if (mCalTatolSize.equals(".00B")) {
            mOnkeyDown
                    .setCurrentText(getString(R.string.sync_assistant_restore_onkey_down));
        } else {
            StringBuilder contentSize = new StringBuilder(
                    getString(R.string.sync_assistant_restore_onkey_down))
                    .append("(").append(mCalTatolSize).append(")");

            mOnkeyDown.setCurrentText(contentSize.toString());
        }
    }

    // -----------------------------------------------通用类，无需修改--------------------------------------------------//
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
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
                    case R.id.bar_title:
                        onBackPressed();
                        break;
                    case R.id.action_bar_search:
                        UIUtils.goSearchActivity(AppSyncAssistantrestoreActivity.this);
                        break;
                    case R.id.action_go_downQueen:
                        UIUtils.gotoActivity(AppDownLoadQueenActivity.class,
                                AppSyncAssistantrestoreActivity.this);
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
        title.setText(R.string.app_sync_restore);
        title.setOnClickListener(onClickListener);
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public String getActivityName() {
        return this.getClass().getSimpleName();
    }

    @Override
    protected void onDestroy() {

        AIDLUtils.unbindFromService(mToken);

        super.onDestroy();
    }

}
