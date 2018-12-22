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

package com.prize.appcenter.fragment;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.SearchStatusResBean;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.SearchHistoryDao;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.fragment.base.BaseFragment;
import com.prize.appcenter.ui.actionBar.ActionBarActivity.ReloadFunction;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.GameListAdapter;
import com.prize.appcenter.ui.datamgr.SearchResultListManager;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.LinearLayoutForDetail;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;
import com.prize.statistics.model.ExposureBean;
import com.tencent.stat.StatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * *
 * 搜索结果
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class TagResultFragment extends BaseFragment {
    private View root, headView;
    private final String TAG = "TagResultFragment";
    private ActionBarNoTabActivity activity;
    private GameListAdapter adapter;
    private ListView mListView;
    private SearchResultListManager manager;
    private int lastVisiblePosition;
    private boolean isLoadMore = true;
    private View loading = null;
    private boolean hasFootView;
    private boolean isTag;
    private View reloadView;
    private View waitView = null;
    private String keyword;


    /**
     * start-头部view控件
     **/
    private LinearLayoutForDetail mLinerLayout;
    // 游戏图标
    private ImageView gameIcon;
    // 游戏名称
    private TextView gameName;
    /**
     * 搜索返回信息
     ***/
    private TextView searchmsg_Tv;
    // 游戏大小
    private TextView gameSize;
    // 下载按钮
    private AnimDownloadProgressButton downloadBtn;
    /**
     * 游戏介绍
     */
    private TextView game_brief;
    private FlowLayout tag_container;
    private FlowLayout ourtag_container;

    private RelativeLayout game_download_Rlyt;
    /**
     * end-头部view控件
     **/

    private SearchStatusResBean mSearchStatusResBean;
    private AppsItemBean headAppItem;
    private int hight = 0;
    private IUIDownLoadListenerImp listener = null;
    /*记录已经曝光的位置*****/
    private List<Integer> position_360 = new ArrayList<>();


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (downloadBtn != null && msg != null && msg.what == 0) {
                downloadBtn.invalidate();

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_tag_result, container,
                false);
        headView = inflater.inflate(R.layout.head_search_match, null);
        activity = (ActionBarNoTabActivity) getActivity();
        isTag = getArguments().getBoolean("tag", false);
        findViewById();
        init();
        setListener();
        return root;
    }

    @Override
    protected void findViewById() {
        mListView = (ListView) root.findViewById(android.R.id.list);
        mLinerLayout = (LinearLayoutForDetail) headView.findViewById(R.id.child_id);
        reloadView = root.findViewById(R.id.reload_Llyt);
        waitView = root.findViewById(R.id.loading_Llyt_id);
        game_download_Rlyt = (RelativeLayout) headView
                .findViewById(R.id.game_download_Rlyt);
        gameIcon = (ImageView) headView
                .findViewById(R.id.game_iv);
        gameName = (TextView) headView
                .findViewById(R.id.game_name_tv);
        gameSize = (TextView) headView
                .findViewById(R.id.game_size_tv);
        game_brief = (TextView) headView
                .findViewById(R.id.game_brief);
        searchmsg_Tv = (TextView) headView
                .findViewById(R.id.searchmsg_Tv);
        tag_container = (FlowLayout) headView
                .findViewById(R.id.tag_container);
        ourtag_container = (FlowLayout) headView
                .findViewById(R.id.ourtag_container);
        downloadBtn = (AnimDownloadProgressButton) headView
                .findViewById(R.id.game_download_btn);
    }

    @Override
    protected void setListener() {
        headView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (headAppItem == null || TextUtils.isEmpty(headAppItem.id))
                    return;
                UIUtils.gotoAppDetail(headAppItem, headAppItem.id, activity);
                MTAUtil.onDetailClick(activity,
                        headAppItem.name,
                        headAppItem.packageName);
            }
        });
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                int realPostion = position;
                if (mListView.getHeaderViewsCount() > 0) {
                    realPostion = position - 1;
                }
                if (adapter.getItem(realPostion) != null && activity != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("AppsItemBean",
                            adapter.getItem(realPostion));
                    bundle.putString("appid", adapter.getItem(realPostion).id);
                    bundle.putString("from", "search");
                    bundle.putString("keyWord", keyword);
                    UIUtils.gotoAppDetailFromSearch(bundle, activity);
                    MTAUtil.onDetailClick(activity,
                            adapter.getItem(realPostion).name,
                            adapter.getItem(realPostion).packageName);
                }

            }
        });

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
    }

    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private int mFirstVisibleItem;
    private boolean isFirstStatistics = true;
    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (lastVisiblePosition >= mListView.getCount() - 1 && isLoadMore) {
                isLoadMore = false;
                if (manager.isListNextPage()) {
                    addFootView();
                    manager.getNextListPage(TAG);
                }
            }

            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    if (mFirstVisibleItem < 0)
                        break;
                    for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                        if (position_360.contains(i)) continue;
                        AppsItemBean appsItemBean = adapter.getItem(i);
                        if (appsItemBean != null && !TextUtils.isEmpty(appsItemBean.backParams)) {
                            ExposureBean bean360 = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI, Constants.LIST, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                            mExposureBeans.add(bean360);
                            position_360.add(i);
                        }
                    }
//                    PrizeStatUtil.startUploadExposure(mExposureBeans);
                    AIDLUtils.uploadDataNow(mExposureBeans);
                    mExposureBeans.clear();
                    break;
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = mListView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
            if (isFirstStatistics && lastVisiblePosition > 0) {
                AppsItemBean appsItemBean;
                for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                    if (position_360.contains(i)) continue;
                    appsItemBean = adapter.getItem(i);
                    if (appsItemBean != null && !TextUtils.isEmpty(appsItemBean.backParams)) {
                        ExposureBean bean360 = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI, Constants.LIST, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                        mExposureBeans.add(bean360);
                        position_360.add(i);
                    }
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i("PrizeStatUtil", "onScroll-mExposureBeans=" + mExposureBeans);
                }
//                PrizeStatUtil.startUploadExposure(mExposureBeans);
                AIDLUtils.uploadDataNow(mExposureBeans);
                mExposureBeans.clear();
            }

            mFirstVisibleItem = firstVisibleItem;
        }
    };

    public void onBtnClick(String appName) {
        // 统计按钮被点击次数，统计对象：OK按钮
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        StatService.trackCustomKVEvent(activity, "button_click", prop);
    }

    @Override
    protected void init() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View noContent = inflater.inflate(R.layout.footer_nomore_show, null);
        ((TextView) noContent.findViewById(R.id.caution_tv))
                .setText(R.string.no_result_find);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        keyword = getArguments().getString("keyword");
        adapter = new GameListAdapter(activity,null,null);
        adapter.setDownlaodRefreshHandle();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessage(0);

            }
        });
        AIDLUtils.registerCallback(listener);

        mToken = AIDLUtils.bindToService(activity, this);
        mListView.setAdapter(adapter);
        hight = (int) getResources().getDimension(R.dimen.hight_search_imag);
        if (!TextUtils.isEmpty(keyword)) {
            requestData(keyword);
        }
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        activity.hideWaiting();
        hideWaiting();
        switch (what) {
            case SearchResultListManager.SUCCESS:
                isLoadMore = true;
                removeFootView();
                List<AppsItemBean> data = ((PrizeAppsTypeData) obj).apps;

                mListView.setVisibility(View.VISIBLE);
                if (manager.isFirstPage()) {
                    if (data == null || data.size() <= 0) {
                    } else {
                        if (((PrizeAppsTypeData) obj).status != null) {
                            mSearchStatusResBean = ((PrizeAppsTypeData) obj).status;
                            if (mSearchStatusResBean.type == 1) {
                                if (mListView.getHeaderViewsCount() <= 0) {
                                    mListView.addHeaderView(headView);
                                }
                                headAppItem = data.get(0);
                                processHeadData();
                                data = data.subList(1, data.size());
                            } else {
                                if (mListView.getHeaderViewsCount() > 0) {
                                    mListView.removeHeaderView(headView);
                                }
                                headAppItem = null;
                            }
                        }
                    }
                    adapter.setData(data);
                    mListView.setSelection(0);
                    return;
                }
                adapter.addData(data);

                break;
            case NetSourceListener.WHAT_NETERR:
                isLoadMore = true;
                if (manager.isFirstPage()) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            showWaiting();
                            requestData(keyword);
                        }

                    });
                } else {
                    removeFootView();
                    ToastUtils.showToast(R.string.net_error);
                }
                break;
        }

    }

    /**
     * 请求搜索结果
     *
     * @param keyword 关键字
     */
    public void requestData(String keyword) {
        if (isTag) {
            activity.showWaiting();
        }
        this.keyword = keyword;
        if (manager == null) {
            manager = new SearchResultListManager(this, keyword);
        } else {
            manager.setQuery(keyword);
        }
        removeFootView();
        BaseApplication.cancelPendingRequests(TAG);
        SearchHistoryDao.insert(keyword, System.currentTimeMillis());
        manager.getNewData(TAG);
        if (!isTag) {
            if (manager.isFirstPage()) {
                showWaiting();
            }
        }
    }


    /**
     * 取消加载更多
     */
    private void removeFootView() {
        if (hasFootView) {
            // modify by huanglingjun 2015-12-3
            // api4.4以下footview如果显示不出来，remove会报错
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mListView.removeFooterView(loading);
            }
            hasFootView = false;
        }

    }


    /**
     * 加载更多
     */
    private void addFootView() {
        if (loading != null && mListView != null) {
            mListView.addFooterView(loading);
        }
        hasFootView = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.setNullListener();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (adapter != null) {
            adapter.removeDownLoadHandler();
        }
        if (mExposureBeans != null && mExposureBeans.size() > 0) {
//            PrizeStatUtil.startUploadExposure(mExposureBeans);
            mExposureBeans.clear();
        }
        if (position_360 != null && position_360.size() > 0) {
//            PrizeStatUtil.startUploadExposure(mExposureBeans);
            position_360.clear();
        }
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onResume() {
        if (adapter != null) {
            adapter.setIsActivity(true);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (adapter != null) {
            adapter.setIsActivity(false);
        }
        super.onPause();
    }

    /**
     * 隐藏等待框
     */
    public void hideWaiting() {
        if (waitView == null)
            return;
        waitView.setVisibility(View.GONE);
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(true);
        reloadView.setVisibility(View.GONE);
        // content_Llyt.setVisibility(View.VISIBLE);

    }

    /**
     * 加载失败
     */
    public void loadingFailed(final ReloadFunction reload) {
        waitView.setVisibility(View.GONE);
        reloadView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        LinearLayout reloadLinearLayout = (LinearLayout) reloadView
                .findViewById(R.id.reload_Llyt);
        if (reloadLinearLayout != null) {
            reloadLinearLayout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    reload.reload();
                }
            });
        }

    }

    /**
     * 显示等待框
     */
    public void showWaiting() {
        if (waitView == null)
            return;
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        gifWaitingView.setPaused(false);
        waitView.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        reloadView.setVisibility(View.GONE);
    }


    private void processHeadData() {
        if (headAppItem == null)
            return;
        searchmsg_Tv.setText(mSearchStatusResBean.displayText);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        gameName.setLayoutParams(param);
        ourtag_container.setVisibility(View.GONE);
        ourtag_container.removeAllViews();
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        params1.setMargins(0, 0, 12, 0);

        if (TextUtils.isEmpty(headAppItem.customTags)) {
            if (!TextUtils.isEmpty(headAppItem.ourTag) || headAppItem.giftCount > 0) {
                gameName.setLayoutParams(param2);
                ourtag_container.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(headAppItem.ourTag)) {
                    String[] tags;
                    if (headAppItem.ourTag.contains(",")) {
                        tags = headAppItem.ourTag.split(",");

                    } else {
                        tags = new String[]{headAppItem.ourTag};
                    }
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 2 ? 2 : size;
                        TextView tagView;
                        for (int i = 0; i < requireLen; i++) {
                            if (TextUtils.isEmpty(tags[i])) {
                                continue;
                            }
                            tagView = (TextView) LayoutInflater.from(this.activity)
                                    .inflate(R.layout.item_textview, null);
                            tagView.setText(tags[i]);
                            tagView.setTextColor(this.activity.getResources()
                                    .getColor(R.color.text_color_009def));
                            tagView.setBackgroundResource(R.drawable.bg_list_tag);
                            tagView.setLayoutParams(params1);
                            ourtag_container.addView(tagView);
                        }
                    }

                }
                if (headAppItem.giftCount > 0) {
                    TextView tagView = (TextView) LayoutInflater.from(this.activity)
                            .inflate(R.layout.item_textview, null);
                    tagView.setText(R.string.gamedetail_gift_title);
                    tagView.setTextColor(this.activity.getResources()
                            .getColor(R.color.text_color_ff9732));
                    tagView.setBackgroundResource(R.drawable.bg_list_tag_gift);
                    tagView.setLayoutParams(params1);
                    ourtag_container.addView(tagView);
                }
            }
        } else {
            gameName.setLayoutParams(param2);
            ourtag_container.setVisibility(View.VISIBLE);
            TextView tagView = (TextView) LayoutInflater.from(this.activity)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(headAppItem.customTags);
            tagView.setTextColor(this.activity.getResources().getColor(
                    R.color.text_color_customer));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
        }
        downloadBtn.setGameInfo(headAppItem);
        downloadBtn.enabelDefaultPress(true);
        game_download_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadBtn.performClick();

            }
        });
        downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (headAppItem == null)
                    return;
                final int state = AIDLUtils.getGameAppState(
                        headAppItem.packageName, headAppItem.id + "",
                        headAppItem.versionCode);
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
                            mDownDialog = new DownDialog(activity,
                                    R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(headAppItem);
                                            PrizeStatUtil.onSearchResultItemClick(headAppItem.id, headAppItem.packageName, headAppItem.name, keyword, true);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            downloadBtn.onClick();
                            break;
                    }

                } else {
                    downloadBtn.onClick();
                }
            }
        });
        if (!TextUtils.isEmpty(headAppItem.largeIcon)) {
            ImageLoader.getInstance().displayImage(headAppItem.largeIcon,
                    gameIcon, UILimageUtil.getUILoptions(), null);
        } else {

            if (headAppItem.iconUrl != null) {
                ImageLoader.getInstance()
                        .displayImage(headAppItem.iconUrl, gameIcon,
                                UILimageUtil.getUILoptions(), null);
            }
        }

        if (headAppItem.name != null) {
            gameName.setText(headAppItem.name);
        }
        gameSize.setText(headAppItem.apkSizeFormat);
        if (!TextUtils.isEmpty(headAppItem.brief)) {
            game_brief.setVisibility(View.VISIBLE);
            game_brief.setText(headAppItem.brief);
            tag_container.setVisibility(View.GONE);
            game_brief.setCompoundDrawablePadding(0);
            game_brief.setTextColor(activity.getResources()
                    .getColor(R.color.text_color_6c6c6c));
        } else {
            if (!TextUtils.isEmpty(headAppItem.categoryName)
                    || !TextUtils.isEmpty(headAppItem.tag)) {
                game_brief.setVisibility(View.GONE);
                // 添加标签
                tag_container.setVisibility(View.VISIBLE);
                tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
//                int rightMargin = activity.getResources()
//                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, activity.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin), 12, 0);
                TextView tagView1 = (TextView) LayoutInflater.from(activity)
                        .inflate(R.layout.item_textview, null);
                if (!TextUtils.isEmpty(headAppItem.categoryName)) {
                    tagView1.setText(headAppItem.categoryName);
                    tagView1.setLayoutParams(params);
                    tag_container.addView(tagView1);

                }
                if (!TextUtils.isEmpty(headAppItem.tag)) {
                    String[] tags = headAppItem.tag.split(" ");
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 3 ? 3 : size;
                        for (int i = 0; i < requireLen; i++) {
                            if (!TextUtils.isEmpty(headAppItem.categoryName)
                                    && headAppItem.categoryName.equals(tags[i])) {
                                continue;
                            }
                            TextView tagView = (TextView) LayoutInflater.from(
                                    activity).inflate(R.layout.item_textview,
                                    null);
                            tagView.setText(tags[i]);
                            tagView.setLayoutParams(params);
                            tag_container.addView(tagView);
                        }
                    }
                }
            } else {
                game_brief.setVisibility(View.VISIBLE);
                game_brief.setText("");
                tag_container.setVisibility(View.GONE);
            }

        }
        initHorizontalScrollView();
    }


    private DownDialog mDownDialog;
    protected Drawable drawable;
    private Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    private void initHorizontalScrollView() {
        if (activity == null)
            return;
        mLinerLayout.removeAllViews();
        if (mSearchStatusResBean == null || TextUtils.isEmpty(mSearchStatusResBean.screenshotsUrl))
            return;
        final String[] paths = mSearchStatusResBean.screenshotsUrl.split("\\,");
        int leftMargin = 12;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, hight);
        params.weight = 1;
        int length = paths.length > 3 ? 3 : paths.length;
        for (int i = 0; i < length; i++) {
            final ImageView image = (ImageView) LayoutInflater.from(activity)
                    .inflate(R.layout.imageview, null);

            if (i == 0) {
                params.leftMargin = 0;
            } else {
                params.leftMargin = leftMargin;
            }
            image.setLayoutParams(params);
            image.setDrawingCacheEnabled(true);
            image.setTag(i);
            image.setFocusable(true);
            image.requestLayout();
            ImageLoader.getInstance().displayImage(paths[i], image, UILimageUtil
                            .getUILoptions(R.drawable.detail_big_icon_defualt),
                    new ImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {

                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {

                        }

                        @Override
                        public void onLoadingComplete(String imageUri,
                                                      View view, Bitmap loadedImage) {
                            if (loadedImage == null) {
                                return;
                            }
                            int width = loadedImage.getWidth();
                            int height = loadedImage.getHeight();
                            if (loadedImage != null && image != null
                                    && width > height) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                Bitmap bitmap = Bitmap.createBitmap(
                                        loadedImage, 0, 0, width, height,
                                        matrix, true);
                                image.setImageBitmap(bitmap);
                            }

                        }

                        @Override
                        public void onLoadingCancelled(String imageUri,
                                                       View view) {

                        }
                    });
            mLinerLayout.addView(image);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (adapter != null) {
            adapter.setDownlaodRefreshHandle();
        }
        AIDLUtils.registerCallback(listener);
    }
}
