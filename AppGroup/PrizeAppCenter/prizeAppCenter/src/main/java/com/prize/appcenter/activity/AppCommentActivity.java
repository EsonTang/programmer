/*
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * <p/>
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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.Person;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppCommentData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.DetailApp;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.MainApplication;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.AppCommentAdapter;
import com.prize.appcenter.ui.datamgr.AppDetailDataManager;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.dialog.SubmitCommentDialog;
import com.prize.appcenter.ui.dialog.SubmitCommentDialog.SubmitCommentCallBack;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.DetailDownloadProgressButton;
import com.tencent.stat.StatService;

import java.lang.ref.WeakReference;

/**
 * 类描述：app评论信息
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppCommentActivity extends ActionBarNoTabActivity implements
        DataManagerCallBack, OnClickListener {
    private final String TAG = "AppCommentActivity";
    private TextView mLevel;
    private ListView mListView;
    private RatingBar mRatingBar;
    private AppCommentAdapter adapter;
    private AppDetailDataManager appDetailDataManager;
    private int currentPager = 1;
    private String appId;
    private static final int PAGERSIZE = 20;
    private View loading = null;
    private boolean isLoadMore = true;
    private boolean isFootViewNoMore = true;
    private int pageCount;
    private int pageIndex;
    private int lastVisiblePosition;
    private boolean hasFootView = false;
    private DetailApp appDetailData;
    private AppsItemBean mAppsItemBean;
    private DetailDownloadProgressButton mDownloadBtn;
    private IUIDownLoadListenerImp listener = null;
    private DownDialog mDownDialog;
    private AppsItemBean itemBean;
    private Person mPerson;
    private boolean isComment;
    private RelativeLayout noCommentRlyt;
    private Handler mHandler = new MyHander(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.fragment_appcomment);
        WindowMangerUtils.changeStatus(getWindow());
        Bundle mBundle = getIntent().getBundleExtra("bundle");
        if (mBundle != null) {
            appDetailData = (DetailApp) mBundle.getSerializable("detailData");
            mAppsItemBean = mBundle.getParcelable("appsItemBean");
            if(appDetailData != null) {
                appId = appDetailData.id;
            }else if(mAppsItemBean != null){
                appId = mAppsItemBean.id;
            }
            isComment = mBundle.getBoolean("isComment", false);
        }

        String appid = getIntent().getStringExtra("appId");
        if (appid != null) {
            appId = appid;
            isComment = false;
        }

        super.setTitle(getResources().getString(R.string.game_comment_title));
        mPerson = ((MainApplication) getApplication()).getPerson();
        initView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        StatService.onPause(this);
    }

    /**
     * 方法描述:设置监听
     */
    private void setListener() {
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, scrollListener));
        mDownloadBtn.setOnClickListener(this);
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state,boolean isNewDownload) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessage(0);

            }
        });
        AIDLUtils.registerCallback(listener);
        mToken = AIDLUtils.bindToService(this, this);
        if(appDetailData != null) {
            itemBean = UIUtils.changeToAppItemBean(appDetailData);
        }else if(mAppsItemBean != null){
            itemBean = mAppsItemBean;
        }
    }

    /**
     * 方法描述：初始化控件
     */
    private void initView() {
        loading = LayoutInflater.from(this).inflate(
                R.layout.footer_loading_small, null);

        View headView = this.getLayoutInflater().inflate(
                R.layout.listview_head_comment, null);
        noCommentRlyt = (RelativeLayout) findViewById(R.id.noCommentRlyt_id);
        mLevel = (TextView) headView.findViewById(R.id.level_id);
        mRatingBar = (RatingBar) headView.findViewById(R.id.ratingBar_big_id);

        mListView = (ListView) findViewById(R.id.mListView_id);
        mDownloadBtn = (DetailDownloadProgressButton) findViewById(R.id.detailinfo_download_id);
        adapter = new AppCommentAdapter(this);
        mListView.addHeaderView(headView);
        mListView.setAdapter(adapter);
        mDownloadBtn.enabelDefaultPress(true);
        if(appDetailData != null) {
            mDownloadBtn.setGameInfo(UIUtils.changeToAppItemBean(appDetailData));
        }else if(mAppsItemBean != null){
            mDownloadBtn.setGameInfo(mAppsItemBean);
        }
        getCommentData();
    }

    private void getCommentData() {
        showWaiting();
        if (appDetailDataManager == null) {
            appDetailDataManager = new AppDetailDataManager(this);
        }
        appDetailDataManager
                .getCommentData(appId, currentPager, PAGERSIZE, TAG);
    }

    /**
     *用户提交评论
     * @param appId  应用id
     * @param versionName 版本名称
     * @param starLevel  星级
     * @param content  评论内容
     * @param mobile 手机号
     * @param userId  用户id
     * @param nickName  昵称
     * @param avatarUrl  用户icon
     */
    public void doCommentRequest(String appId, String versionName,
                                 float starLevel, String content, String mobile, int userId,
                                 String nickName, String avatarUrl) {
        appDetailDataManager.doPostComment(appId, versionName, starLevel,
                content, mobile, userId, nickName, avatarUrl, TAG);
    }
    /**
     * 方法描述：初始化数据
     *
     * @param commentData  AppCommentData
     */
    private void initData(AppCommentData commentData) {
        mRatingBar.setRating(commentData.rating);
        mLevel.setText(String.valueOf(commentData.rating));
        mDownloadBtn.setCommentCallBack(new DetailDownloadProgressButton.CommentCallBack() {

            @Override
            public void showCommentDialog() {
                dispalyCommentDialog();
            }
        });
    }

    private OnScrollListener scrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lastVisiblePosition >= mListView.getCount() - 1 && isLoadMore) {
                isLoadMore = false;
                if (hasNext()) {
                    addFootView();
                    appDetailDataManager.getCommentData(appId, currentPager,
                            PAGERSIZE, TAG);
                } else {
                    addFootViewNoMore();
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = mListView.getLastVisiblePosition();
        }
    };

    private boolean reSetDataFlag = false;

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        isLoadMore = true;
        synchronized (AppCommentActivity.class) {
            if (what == NetSourceListener.WHAT_NETERR) {
                if (currentPager == 1) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            appDetailDataManager.getCommentData(appId,
                                    currentPager, PAGERSIZE, TAG);
                        }
                    });
                } else {
                    Toast.makeText(this, this.getString(R.string.net_error),
                            Toast.LENGTH_SHORT).show();
                }
                removeFootView();
                return;
            }

            // 用户评价是否成功
            if (what == AppDetailDataManager.USER_COMMENT_SUCCESS) {
                appDetailDataManager.getCommentData(appId, 1, PAGERSIZE, TAG);

                AppCommentData data = (AppCommentData) obj;
                //提交成功后，后台会根据提交内容返回吐司信息
                if (data.msg != null) {
                    Toast.makeText(this, data.msg,
                            Toast.LENGTH_SHORT).show();
                }
                reSetDataFlag = true;
                return;

            } else if (what == AppDetailDataManager.USER_COMMENT_FAILURE) {
                Toast.makeText(this, this.getString(R.string.submint_failure),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 用户评价成功，刷新评价列表
            if (what == AppDetailDataManager.COMMENT_SUCCESS && reSetDataFlag
                    && obj != null) {
                AppCommentData commentData = (AppCommentData) obj;
                if (commentData.comments != null
                        && commentData.comments.size() > 0) {
                    noCommentRlyt.setVisibility(View.GONE);
                    initData(commentData);
                    adapter.reSetData(commentData.comments);
                    adapter.notifyDataSetChanged();
                    currentPager = 1;
                } else {
                    // noComment.setVisibility(View.VISIBLE);
                    noCommentRlyt.setVisibility(View.VISIBLE);
                    Toast.makeText(this, this.getString(R.string.failure),
                            Toast.LENGTH_SHORT).show();
                }
                reSetDataFlag = false;
                return;
            }

            // 获取评价列表数据
            if (what == AppDetailDataManager.COMMENT_SUCCESS) {
                if (obj == null) {
                    if (currentPager == 1) {
                        loadingFailed(new ReloadFunction() {
                            @Override
                            public void reload() {
                                appDetailDataManager.getCommentData(appId,
                                        currentPager, PAGERSIZE, TAG);
                            }
                        });
                        return;
                    }
                }
                removeFootView();
                AppCommentData commentData = (AppCommentData) obj;
                if (commentData != null) {
                    initData(commentData);
                    pageIndex = commentData.pageIndex;
                    pageCount = commentData.pageCount;
                    if (commentData.comments.size() > 0) {
                        adapter.addData(commentData.comments);
                        currentPager++;
                    } else {
                        noCommentRlyt.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                Toast.makeText(this, this.getString(R.string.failure),
                        Toast.LENGTH_SHORT).show();
                removeFootView();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("outState", appId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public String getActivityName() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 是否有下一页
     *
     * @return boolean
     */
    public boolean hasNext() {
        return pageIndex + 1<=pageCount;
    }

    /**
     * 添加加载更多
     */
    private void addFootView() {
        mListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            // mListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != mListView)) {
            mListView.removeFooterView(loading);
            hasFootView = false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.detailinfo_download_id:
                int state = AIDLUtils.getGameAppState(itemBean.packageName,
                        itemBean.id + "", itemBean.versionCode);
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
                            mDownDialog = new DownDialog(AppCommentActivity.this,
                                    R.style.add_dialog);
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
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            mDownloadBtn.onClick();
                            break;
                    }

                } else {
                    mDownloadBtn.onClick();
                }

                break;
            default:
                break;
        }

    }

    private void dispalyCommentDialog() {
        final String id;
        final String versionName;

        if (appDetailData != null) {
            id = appDetailData.id;
            versionName = appDetailData.versionName;
        }else if(mAppsItemBean != null){
            id = mAppsItemBean.id;
            versionName = mAppsItemBean.versionName;
        }else {
            return;
        }
        SubmitCommentDialog commentDialog = new SubmitCommentDialog(AppCommentActivity.this,
                R.style.add_dialog);
        commentDialog.setCommentCallBack(new SubmitCommentCallBack() {

            @Override
            public void submitCommentClick(String commentContent, float rating) {
                if (mPerson != null) {
                    AppCommentActivity.this.doCommentRequest(id,
                            versionName, rating, commentContent,
                            ClientInfo.getInstance().brand,
                            Integer.parseInt(mPerson.getUserId()),
                            mPerson.getRealName(), mPerson.getAvatar());

                } else {
                    AppCommentActivity.this.doCommentRequest(id,
                            versionName, rating, commentContent,
                            ClientInfo.getInstance().brand, 0, "", "");
                }
            }
        });
        commentDialog.show();
    }

    private void dismissCautionDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appDetailDataManager != null) {
            appDetailDataManager.setNullListener();
        }
        BaseApplication.cancelPendingRequests(TAG);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);

        }
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private boolean isFirst = true;

    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus && isFirst) {
            if (isComment) {
                dispalyCommentDialog();
            }
            isFirst = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        AIDLUtils.registerCallback(listener);
    }

    private static class MyHander extends Handler {
        private WeakReference<AppCommentActivity> mActivities;

        MyHander(AppCommentActivity mActivity) {
            this.mActivities = new WeakReference<AppCommentActivity>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final AppCommentActivity activity = mActivities.get();
            if (activity != null) {
                if (activity.mDownloadBtn != null) {
                    activity.mDownloadBtn.invalidate();
                }
            }
        }
    }
}
