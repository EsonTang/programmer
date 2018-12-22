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

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.GiftPkgItemBean;
import com.prize.app.beans.Person;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.gamegift.SingeGameGiftData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PreferencesUtils;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.MainApplication.LoginDataCallBack;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.SingleGameGiftListAdapter;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.CornerImageView;
import com.prize.appcenter.ui.widget.progressbutton.DetailDownloadProgressButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;

/**
 * 礼包详情
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class GameGiftDetailActivity extends ActionBarNoTabActivity {
    private final String TAG = "GameGiftDetailActivity";
    private View headerView;
    private CornerImageView mCornerImageView;
    private DetailDownloadProgressButton mProgressButton;
    private TextView game_name_tv;
    private SingleGameGiftListAdapter mSingleGameGiftListAdapter;
    private ListView appListView;
    private String appId = "";
    private Cancelable reqHandler;
    private int currentIndex = 1;
    private TextView numGift_Tv;
    private String mUserId;
    private IUIDownLoadListenerImp listener = null;
    private AppsItemBean bean;
    private int lastVisiblePosition;
    private SingeGameGiftData data;
    private boolean isFootViewNoMore = true;
    private int expandPos = -1;
    private DownDialog mDownDialog;

    // 无更多内容加载
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;

    protected Handler mHandler = new MyHander(this);
//    {
//        public void handleMessage(android.os.Message msg) {
//            if (bean != null && bean.packageName.equals(msg.obj)) {
//                if (mProgressButton != null && msg.what == 0) {
//                    mProgressButton.invalidate();
//                }
//            }
//        }
//
//        ;
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_gift_detail);
        LayoutInflater inflater = LayoutInflater.from(this);
        headerView = inflater.inflate(R.layout.giftdetail_header, null);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, null);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        WindowMangerUtils.changeStatus(getWindow());
        findViewById();
        if (savedInstanceState != null) {
            UIUtils.addActivity(this);
        }
        mSingleGameGiftListAdapter = new SingleGameGiftListAdapter(this);
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                mHandler.removeCallbacksAndMessages(null);
                Message msg = Message.obtain();
                msg.obj = pkgName;
                msg.what = 0;
                mHandler.sendMessage(msg);

            }
        });
        mToken = AIDLUtils.bindToService(this, this);
        processIntent();
        setTitle(R.string.gift_detail);
        setListener();
        init();
        requestData();
    }

    private void processIntent() {
        appId = getIntent().getStringExtra("appId");
        expandPos = getIntent().getIntExtra("position", 0);// mSingleGameGiftListAdapter.onItemClick
    }

    private void init() {
        mSingleGameGiftListAdapter.onItemClick(expandPos);
        appListView.setAdapter(mSingleGameGiftListAdapter);
        queryUserId();
        ((MainApplication) getApplication())
                .setLoginCallBack(new LoginDataCallBack() {

                    @Override
                    public void setPerson(Person person) {
                        queryUserId();
                    }
                });
    }

    protected void queryUserId() {
        mUserId = CommonUtils.queryUserId();
    }

    private void requestData() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/gift/special");
        if (!TextUtils.isEmpty(mUserId)) {
            params.addBodyParameter("userId", mUserId);
        }
        params.addBodyParameter("appId", appId);
        params.addBodyParameter("pageSize", String.valueOf(Constants.PAGE_SIZE));
        params.addBodyParameter("pageIndex", String.valueOf(currentIndex));

        reqHandler = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {

            @Override
            public void onSuccess(String result) {
                hideWaiting();
                JLog.i(TAG, result);
                currentIndex++;
                try {
                    String response = new JSONObject(result).getString("data");
                    data = new Gson().fromJson(response,
                            SingeGameGiftData.class);
                    if (data != null) {
                        processData();
                    }
                } catch (JSONException e) {
                    hideWaiting();
                    e.printStackTrace();

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                if (mSingleGameGiftListAdapter != null
                        && mSingleGameGiftListAdapter.getCount() <= 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }
                    });
                }
                removeFootView();

            }
        });

    }

    private void processData() {
        if (data.app == null) {
            return;
        }
        processReceivedPref();
        mSingleGameGiftListAdapter.addData(data.gifts);
        mSingleGameGiftListAdapter.setmBean(data.app);
        if (data.pageCount < currentIndex && currentIndex != 2) {
            addFootViewNoMore();
        }
        bean = data.app;
        String uri = bean.iconUrl;
        if (!TextUtils.isEmpty(bean.largeIcon)) {
            uri = bean.largeIcon;
        }
        ImageLoader.getInstance().displayImage(uri, mCornerImageView,
                UILimageUtil.getUILoptions());
        mProgressButton.enabelDefaultPress(true);
        mProgressButton.setGameInfo(bean);
        game_name_tv.setText(bean.name);
        StringBuilder source = new StringBuilder();
        source.append(getString(R.string.total));
        source.append(String.valueOf(data.pageItemCount));
        source.append(getString(R.string.unit_gift));
        numGift_Tv.setText(source.toString());
    }

    private void processReceivedPref() {
        boolean receivedAll = true;
        for (GiftPkgItemBean bean : data.gifts) {
            if (bean.giftStatus == 2 && bean.giftType == 1 && bean.activationCode == null) {
                receivedAll = false;
                break;
            }
        }
        if (receivedAll) {
            PreferencesUtils.putBoolean(this, Constants.KEY_WELFARE_GOT_GIFT + data.app.packageName, true);
        } else {
            PreferencesUtils.putBoolean(this, Constants.KEY_WELFARE_GOT_GIFT + data.app.packageName, false);

        }
    }

    private void findViewById() {
        appListView = (ListView) findViewById(android.R.id.list);
        mCornerImageView = (CornerImageView) headerView
                .findViewById(R.id.game_iv);
        mProgressButton = (DetailDownloadProgressButton) findViewById(R.id.detailinfo_download_id);
        game_name_tv = (TextView) headerView.findViewById(R.id.game_name_tv);
        numGift_Tv = (TextView) headerView.findViewById(R.id.numGift_Tv);
        appListView.addHeaderView(headerView);
        appListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setListener() {
        headerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (data != null && data.app != null) {
                    UIUtils.gotoAppDetail(data.app, appId,
                            GameGiftDetailActivity.this);
                    MTAUtil.onDetailClick(GameGiftDetailActivity.this,
                            data.app.name, data.app.packageName);
                }
            }
        });
        OnScrollListener scrollListener = new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (data == null)
                    return;
                if (lastVisiblePosition >= appListView.getCount() - 1
                        && currentIndex < data.pageCount) {
                    addFootView();
                    requestData();
                } else {
                    if (currentIndex != 2) {
                        addFootViewNoMore();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                lastVisiblePosition = appListView.getLastVisiblePosition();
            }
        };
        appListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, scrollListener));

        appListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mSingleGameGiftListAdapter.onItemClick(position - 1);

            }
        });

        mProgressButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bean == null) {
                    return;
                }
                int state = AIDLUtils.getGameAppState(bean.packageName,
                        bean.id + "", bean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

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
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(GameGiftDetailActivity.this,
                                    R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissCautionDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(bean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            mProgressButton.onClick();
                            break;
                    }

                } else {
                    mProgressButton.onClick();
                }

            }
        });
    }

    @Override
    public String getActivityName() {

        return "GameGiftDetailActivity";
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        if (reqHandler != null) {
            reqHandler.cancel();
        }
        if (mSingleGameGiftListAdapter != null) {
            mSingleGameGiftListAdapter.removeDownLoadHandler();
        }
        AIDLUtils.unbindFromService(mToken);
        UIUtils.removeActivity(this);
        ((MainApplication) getApplication()).setLoginCallBack(null);
    }

    /**
     * 添加加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        appListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != appListView)) {
            appListView.removeFooterView(loading);
            hasFootView = false;
        }
    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            appListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AIDLUtils.registerCallback(listener);
        mSingleGameGiftListAdapter.setDownlaodRefreshHandle();
    }

    private void dismissCautionDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
        }
    }

    private static class MyHander extends Handler {
        private WeakReference<GameGiftDetailActivity> mActivities;

        MyHander(GameGiftDetailActivity mActivity) {
            this.mActivities = new WeakReference<GameGiftDetailActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final GameGiftDetailActivity activity = mActivities.get();
            if (activity != null) {
                if (activity.bean != null && activity.bean.packageName.equals(msg.obj)) {
                    if (activity.mProgressButton != null && msg.what == 0) {
                        activity.mProgressButton.invalidate();
                    }
                }

            }
        }
    }
}
