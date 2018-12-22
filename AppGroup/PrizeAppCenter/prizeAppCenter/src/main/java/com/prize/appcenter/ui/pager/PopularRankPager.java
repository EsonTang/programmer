package com.prize.appcenter.ui.pager;

import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsTypeData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.adapter.GameRankingAdapter;
import com.prize.appcenter.ui.datamgr.RankingDataManager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.statistics.model.ExposureBean;

import java.util.ArrayList;
import java.util.List;


/**
 * *
 * 流行榜和新品榜
 */
public class PopularRankPager extends BasePager {
    private String TAG = "PopularRankPager";
    private RankingDataManager dataManager;

    private GameRankingAdapter adapter;
    private ListView listView;

    // 加载更多
    private View loading = null;
    private boolean hasFootView = false;

    // 无更多内容加载
    private View noLoading = null;
    private boolean hasFootViewNoMore;

    // 当前可见的最后位置
    private int lastVisiblePosition;
    private int mFirstVisibleItem;
    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private List<ExposureBean> mExposure360 = new ArrayList<>();
    private boolean isLoadingMore = true;
    private String widget;
    private View selfView;
    private boolean isFirstStatistics = true;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.setIsActivity(true);
                    if (mFirstVisibleItem < 0)//此时可见头布局
                        break;
                    ExposureBean newbean;
                    ExposureBean bean360;
                    for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                        if (positions.contains(i)) continue;
                        AppsItemBean bean = adapter.getItem(i);
                        if (bean == null) continue;
                        if (isNeedStatic) {
                            newbean = CommonUtils.formNewPagerExposure(bean, Constants.RANK_GUI, widget);
                            if (newbean != null) {
                                mExposureBeans.add(newbean);
                            }
                        }
                        bean360 = CommonUtils.formatSearchHeadExposure(Constants.RANK_GUI, widget, bean.id, bean.name, bean.backParams);
                        if (bean360 != null) {
                            mExposure360.add(bean360);
                        }
                        positions.add(i);
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-positions=" + positions);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans.size()=" + mExposureBeans.size());
                    }
                    PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                    mExposureBeans.clear();
                    AIDLUtils.uploadDataNow(mExposure360);
                    mExposure360.clear();
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.setIsActivity(true);
                    break;
                case OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    adapter.setIsActivity(false);
                    break;
            }
            if (!isLoadingMore) {
                return;
            }
            if (lastVisiblePosition >= adapter.getCount() - 1 && isLoadingMore) {
                isLoadingMore = false;
                // 分页显示
                if (dataManager.isListNextPage()) {
                    addFootView();
                    dataManager.getRankingListData(TAG);
                } else {
                    isLoadingMore = true;
                    if (!hasFootViewNoMore) {
                        addFootViewNoMore();
                    }
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = listView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
            ExposureBean newbean;
            ExposureBean bean360;
            if (isFirstStatistics && lastVisiblePosition > 0) {//有头部
                for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                    if (positions.contains(i)) continue;
                    AppsItemBean bean = adapter.getItem(i);
                    if (bean == null) continue;
                    if (isNeedStatic) {
                        newbean = CommonUtils.formNewPagerExposure(bean, Constants.RANK_GUI, widget);
                        if (newbean != null && !mExposureBeans.contains(newbean)) {
                            mExposureBeans.add(newbean);
                        }
                    }
                    bean360 = CommonUtils.formatSearchHeadExposure(Constants.RANK_GUI, widget, bean.id, bean.name, bean.backParams);
                    if (bean360 != null) {
                        mExposure360.add(bean360);
                    }

                    positions.add(i);
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i(TAG, "onScroll-去重前mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onScroll-mExposureBeans.size=" + mExposureBeans.size());
                }
            }
            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
            AIDLUtils.uploadDataNow(mExposure360);
            mExposure360.clear();
        }
    };

    /**
     * @param activity  RootActivity
     * @param isPopular true 为流行榜 false：新品榜
     */
    public PopularRankPager(RootActivity activity, boolean isPopular) {
        super(activity);
        this.isPopular = isPopular;
        widget = "new";
        if (this.isPopular) {
            TAG = TAG + isPopular;
            widget = "popular";
        }
        setNeedAddWaitingView(true);
    }


    @Override
    public void scrollToTop() {
        if (listView != null) {
            listView.setSelection(0);
        }
    }

    public View onCreateView() {
        if (null == selfView) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            adapter = new GameRankingAdapter(activity, Constants.RANK_GUI, widget);
            View view = inflater.inflate(R.layout.subrank_page_layout, rootView, false);
            View headerView = new ImageView(activity);
            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 14.0f));
            headerView.setLayoutParams(params);
            listView = (ListView) view.findViewById(android.R.id.list);
            listView.addHeaderView(headerView);
            selfView = view;
        }

        initTab();
        setlisetener();
        return selfView;
    }

    private void setlisetener() {

        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                position = position - listView.getHeaderViewsCount();
                if (position < 0 || CommonUtils.isFastDoubleClick())
                    return;
                if (adapter != null && position < adapter.getCount() && adapter.getItem(position) != null) {
                    UIUtils.gotoAppDetail(adapter.getItem(position),
                            adapter.getItem(position).id, activity);
                    if (position >= 0 && position <= 14) {
                        MTAUtil.onClickCommomGameRank(position + 1, getPageName());
                        MTAUtil.onPopularOrNewRank(isPopular, position + 1);
                    }
                }
            }
        });

        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
    }

    public void onActivityCreated() {
    }


    /**
     * 初始化TabHost
     */

    private void initTab() {
        loading = LayoutInflater.from(activity).inflate(
                R.layout.footer_loading_small, listView, false);
        noLoading = LayoutInflater.from(activity).inflate(
                R.layout.footer_no_loading, listView, false);
        if (Build.VERSION.SDK_INT <= 17) {
            addFootView();
        }
        listView.setAdapter(adapter);
    }

    public void loadData() {

        if (dataManager == null) {
            if (isPopular) {
                dataManager = new RankingDataManager(this, "8", true);

            } else {
                dataManager = new RankingDataManager(this, "4", false);
            }
        }
        if (0 == adapter.getCount()) {
            showWaiting();
            dataManager.getRankingListData(TAG);
        } else {
            hideWaiting();
        }
    }

    /**
     * 取消加载更多
     */
    private void removeFootView() {
        if (hasFootView) {
            listView.removeFooterView(loading);
            hasFootView = false;
        }

    }

    /**
     * 加载更多
     */
    private void addFootView() {
        listView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 添加无更多加载
     */
    private void addFootViewNoMore() {
        listView.addFooterView(noLoading, null, false);
        hasFootViewNoMore = true;
    }

    public void onDestroy() {
        BaseApplication.cancelPendingRequests(TAG);
        if (dataManager != null) {
            dataManager.setNullListener();
        }
        if (adapter != null) {
            adapter.resetDownLoadHandler();
        }
        if (positions != null) {
            positions.clear();
        }
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        PrizeAppsTypeData data;
        switch (what) {
            case RankingDataManager.APP_DOWN_SUCCESS_RANKING_LIST:// 请求成共返回
                data = (PrizeAppsTypeData) obj;
                refreshData(data);
                isLoadingMore = true;

                break;
            case RankingDataManager.GAME_FAILE_RANGKING_LIST:
                removeFootView();
                if (null != adapter && adapter.getCount() == 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            loadData();
                        }
                    });
                } else {
                    ToastUtils.showToast(R.string.net_error);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
                isLoadingMore = true;
                break;
            case NetSourceListener.WHAT_NETERR:
                hideWaiting();
                if (null != adapter && adapter.getCount() == 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            loadData();
                        }
                    });
                } else {
                    ToastUtils.showToast(R.string.net_error);
                }
                isLoadingMore = true;
                removeFootView();
                break;
        }
    }


    @Override
    public String getPageName() {
        return isPopular ? activity.getString(R.string.popular_rank) : activity.getString(R.string.new_product_rank);
    }

    @Override
    public void onPause() {

        if (adapter != null) {
            adapter.setIsActivity(false);
        }
        super.onPause();
    }

    private long time = 0L;

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setIsActivity(true);
            adapter.setDownlaodRefreshHandle();
            adapter.notifyDataSetChanged();
        }
        listView.setFocusable(false);
        if (time == 0) {
            time = System.currentTimeMillis();
        } else {
            if ((System.currentTimeMillis() - time) > Constants.REFRESH_TIME) {//间隔大于5分钟则刷新
                if (ClientInfo.networkType == ClientInfo.NONET) return;
                doHeadRequest();
                time = System.currentTimeMillis();
            }
        }

    }

    private void doHeadRequest() {
        if (dataManager != null) {
            dataManager.reSetPagerIndex(0);
            isLoadingMore = false;
            dataManager.getRankingListData(TAG);
        }
    }

    private void refreshData(PrizeAppsTypeData data) {
        if (data != null) {
//            ArrayList<AppsItemBean> gameList = CommonUtils.filterResData(data.apps, dataManager.isFirstPage(), 5);
            removeFootView();
            isLoadingMore = false;
            if (adapter != null) {
                if (JLog.isDebug) {
                    JLog.i(TAG, "refreshData-dataManager.isFirstPage()=" + dataManager.isFirstPage());
                }
                if (dataManager.isFirstPage()) {
                    adapter.setData(data.apps, getPageName());
                    positions.clear();
                    isFirstStatistics = true;
                } else {
                    adapter.addData(data.apps);
                }
            }
        }
    }

}
