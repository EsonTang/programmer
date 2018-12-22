package com.prize.appcenter.ui.pager;

import android.os.Build;
import android.os.Bundle;
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

//import android.support.v4.widget.SwipeRefreshLayout;
//import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

/**
 * *
 * 应用或者游戏的-榜单pager
 *
 * @author 黄昌国
 * @version V1.1
 */
public class AppCategoryRankingPager extends BasePager {
    private String TAG = "AppCategoryRankingPager";
    private RankingDataManager dataManager;

    private GameRankingAdapter adapter;
    private ListView listView;
    private int mFirstVisibleItem;
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private List<ExposureBean> mExposure360 = new ArrayList<>();
    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private boolean isFirstStatistics = true;
    // 加载更多
    private View loading = null;
    private boolean hasFootView = false;

    // 无更多内容加载
    private View noLoading = null;
    private boolean hasFootViewNoMore;

    // 当前可见的最后位置
    private int lastVisiblePosition;
    private boolean isLoadingMore = true;

    private int currentTitle;// 当前的标题项
    private View selfView;

    private long time = 0L;

    private String widget;

    public AppCategoryRankingPager(RootActivity activity, boolean isGame) {
        super(activity);
        this.isPopular = isGame;
        widget = "app";
        if (this.isPopular) {
            TAG = TAG + isGame;
            widget = "game";
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
            adapter = new GameRankingAdapter(activity,Constants.RANK_GUI,widget);
            View view = inflater.inflate(R.layout.subrank_page_layout, rootView, false);
            listView = (ListView) view.findViewById(android.R.id.list);

            View headerView = new ImageView(activity);
            ListView.LayoutParams params = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 14.0f));
            headerView.setLayoutParams(params);
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
                if (adapter.getItem(position) != null) {

                    UIUtils.gotoAppDetail(adapter.getItem(position),
                            adapter.getItem(position).id, activity);
                    if (position >= 0 && position <= 14) {
                        if (isPopular) {
                            MTAUtil.onClickGameRank(position + 1);
                        } else {
                            MTAUtil.onClickAppRank(position + 1);
                        }
                        MTAUtil.onClickCommomGameRank(position + 1, getPageName());
                    }
                }
            }
        });

        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
    }


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
//            if (!isLoadingMore || swipeLayout.isRefreshing()) {
//                return;
//            }
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
            ExposureBean newbean;
            ExposureBean bean360;
            if (isFirstStatistics && lastVisiblePosition > 0) {
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
            mFirstVisibleItem = firstVisibleItem;
            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
            AIDLUtils.uploadDataNow(mExposure360);
            mExposure360.clear();
        }
    };

    /**
     * 取消 下载监听, Activity OnDestroy 时调用
     */
    private void resetDownLoadHandler() {
//        AIDLUtils.unregisterCallback(listener);
//        if (listener != null) {
//            listener.setmCallBack(null);
//            listener = null;

//        }
//        flowHandler.removeCallbacksAndMessages(null);
    }

//    /**
//     * 设置刷新handler,Activity OnResume 时调用
//     */
//    public void setDownlaodRefreshHandle() {
//        AIDLUtils.registerCallback(listener);
//    }

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

//    /**
//     * 榜单数据的显示
//     */
//    private void initRankData() {
//        if (headViewData == null || headViewData.size() < 3)
//            return;
//        gameBean = headViewData.get(0);
//        gameBean2 = headViewData.get(1);
//        gameBean3 = headViewData.get(2);
//        if (!TextUtils.isEmpty(gameBean.largeIcon)) {
//            ImageLoader.getInstance().displayImage(gameBean.largeIcon, numOne_Iv,
//                    UILimageUtil.getUILoptions(), null);
//        } else {
//            if ((gameBean.iconUrl != null)) {
//                ImageLoader.getInstance().displayImage(gameBean.iconUrl,
//                        numOne_Iv, UILimageUtil.getUILoptions(), null);
//            }
//        }
//
//        if (!TextUtils.isEmpty(gameBean2.largeIcon)) {
//            ImageLoader.getInstance().displayImage(gameBean2.largeIcon,
//                    numTwo_Iv, UILimageUtil.getUILoptions(), null);
//        } else {
//            if ((gameBean2.iconUrl != null)) {
//                ImageLoader.getInstance().displayImage(gameBean2.iconUrl,
//                        numTwo_Iv, UILimageUtil.getUILoptions(), null);
//            }
//        }
//
//        if (!TextUtils.isEmpty(gameBean3.largeIcon)) {
//            ImageLoader.getInstance().displayImage(gameBean3.largeIcon,
//                    numThree_Iv, UILimageUtil.getUILoptions(), null);
//        } else {
//            if ((gameBean3.iconUrl != null)) {
//                ImageLoader.getInstance().displayImage(gameBean3.iconUrl,
//                        numThree_Iv, UILimageUtil.getUILoptions(), null);
//            }
//        }
//        numOne_title_Tv.setText(gameBean.name);
//        numTwo_title_Tv.setText(gameBean2.name);
//        numThree_title_Tv.setText(gameBean3.name);
//        if (null != gameBean.downloadTimesFormat) {
//            String user = gameBean.downloadTimesFormat.replace("次", "人");
//            numOne_use_Tv.setText(activity.getString(
//                    R.string.person_use, user));
//        }
//        if (null != gameBean2.downloadTimesFormat) {
//            String user = gameBean2.downloadTimesFormat.replace("次", "人");
//            numTwo_use_Tv.setText(activity.getString(
//                    R.string.person_use, user));
//        }
//        if (null != gameBean3.downloadTimesFormat) {
//            String user = gameBean3.downloadTimesFormat.replace("次", "人");
//            numThree_use_Tv.setText(activity.getString(
//                    R.string.person_use, user));
//        }
//
//        numOne_Pbtn.setGameInfo(gameBean);
//        numTwo_Pbtn.setGameInfo(gameBean2);
//        numThree_Pbtn.setGameInfo(gameBean3);
//        numOne_Pbtn.setOnClickListener(this);
//        numTwo_Pbtn.setOnClickListener(this);
//        numThree_Pbtn.setOnClickListener(this);
//        numThree_Iv.setOnClickListener(this);
//        numTwo_Iv.setOnClickListener(this);
//        numOne_Iv.setOnClickListener(this);
//    }

    public void loadData() {

        if (dataManager == null) {
            if (isPopular) {
                dataManager = new RankingDataManager(this, "2", isPopular);

            } else {
                dataManager = new RankingDataManager(this, "1", isPopular);
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

    private void removeFootViewNoMore() {
        if (hasFootViewNoMore) {
            listView.removeFooterView(noLoading);
            hasFootViewNoMore = false;
        }
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
//        if (flowHandler != null) {
//            flowHandler.removeCallbacksAndMessages(null);
//        }
        resetDownLoadHandler();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
//        if (swipeLayout.isRefreshing()) {
//            swipeLayout.setRefreshing(false);
//        }
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


    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("currentTitle", currentTitle);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        currentTitle = savedInstanceState.getInt("currentTitle", 0);
    }

    @Override
    public String getPageName() {
        // return "GameRankingPager";
        return isPopular ? activity.getResources().getString(R.string.game_rank) : activity.getResources().getString(R.string.app_rank);
    }

    @Override
    public void onPause() {
        if (adapter != null) {
            adapter.setIsActivity(false);
        }
//        isActivity = false;
        super.onPause();
    }

    @Override
    public void onResume() {
        if (adapter != null) {
            adapter.setIsActivity(true);
            adapter.setDownlaodRefreshHandle();
            adapter.notifyDataSetChanged();
        }
//        if (swipeLayout.isRefreshing()) {
//            swipeLayout.setRefreshing(false);
//        }
//        isActivity = true;
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
        super.onResume();
    }

    private void doHeadRequest() {
        if (dataManager != null) {
            dataManager.reSetPagerIndex(0);
            isLoadingMore = false;
            dataManager.getRankingListData(TAG);
        }
    }


//        Map<String, String> param = new HashMap<String, String>();
//        if (isPopular) {
//            param.put("rankType", "2");
//
//        } else {
//            param.put("rankType", "1");
//
//        }
//        param.put("pageIndex", "1");
//        param.put("pageSize", "20");
//        String url = Constants.GIS_URL + "/rank/ranklist";
//        HttpUtils.doHeadRequest(HttpUtils.getUrl(param, url), this);
//        isLoadingMore = true;
//}

//    @Override
//    public void onResponseHeaders(Map<String, String> headers) {
//        // TODO Auto-generated method stub
////        String last_modify = headers.get(Constants.LAST_MODIFY);
////        if (!TextUtils.isEmpty(last_modify)) {
////            // long current_modifytime = Long.parseLong(last_modify);
////            long old_modifytime = UpdateCach.getInstance().getLastModifyTime(
////                    TAG);
////            JLog.e("huang", "current_modifytime=" + last_modify
////                    + "old_modifytime=" + old_modifytime);
////            if (Long.parseLong(last_modify) != old_modifytime) {
////                UpdateCach.getInstance().setlastModifyTime(TAG, last_modify);
////                dataManager.reSetPagerIndex(0);
////                isLoadingMore = false;
////                dataManager.getRankingListData(TAG);
////            } else {
//////                swipeLayout.setRefreshing(false);
////                String json = UpdateCach.getInstance().getJsonData(TAG);
////                if (!TextUtils.isEmpty(json)) {
////                    PrizeAppsTypeData data = new Gson().fromJson(json,
////                            PrizeAppsTypeData.class);
////                    dataManager.reSetPagerIndex(1);
////                    refreshData(data);
////                }
////            }
////        } else {
//////            swipeLayout.setRefreshing(false);
////        }
////        removeFootViewNoMore();
//    }

    private void refreshData(PrizeAppsTypeData data) {
        if (data != null) {
//            ArrayList<AppsItemBean> gameList = CommonUtils.filterResData(data.apps, dataManager.isFirstPage(), 5);
//            if (dataManager.isFirstPage()) {
//                headViewData.clear();
//                listViewData.clear();
//                for (int i = 0; i < gameList.size(); i++) {
//                    if (i < 3) {
//                        headViewData.add(gameList.get(i));
//                    } else {
//                        listViewData.add(gameList.get(i));
//                    }
//                }
//            }
//            initRankData();
            removeFootView();
            isLoadingMore = false;
            if (adapter != null) {
                if (dataManager.isFirstPage()) {
                    adapter.setData(data.apps, getPageName());
                    isFirstStatistics=true;
                    positions.clear();
                } else {
                    adapter.addData(data.apps);
                }
            }
        }
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.numOne_Pbtn:
//                beginDownload(gameBean, numOne_Pbtn);
//                break;
//            case R.id.numTwo_Pbtn:
//                beginDownload(gameBean2, numTwo_Pbtn);
//                break;
//            case R.id.numThree_Pbtn:
//                beginDownload(gameBean3, numThree_Pbtn);
//                break;
//            case R.id.numOne_Iv:
//                if (CommonUtils.isFastDoubleClick())
//                    return;
//                if (gameBean != null) {
//                    UIUtils.gotoAppDetail(gameBean, gameBean.id, activity);
//                    if (isPopular) {
//                        MTAUtil.onClickGameRank(0);
//                    } else {
//                        MTAUtil.onClickAppRank(0);
//                    }
//                }
//                break;
//            case R.id.numTwo_Iv:
//                if (CommonUtils.isFastDoubleClick())
//                    return;
//                if (gameBean2 != null) {
//                    UIUtils.gotoAppDetail(gameBean2, gameBean2.id,
//                            activity);
//                    if (isPopular) {
//                        MTAUtil.onClickGameRank(1);
//                    } else {
//                        MTAUtil.onClickAppRank(1);
//                    }
//                }
//                break;
//            case R.id.numThree_Iv:
//                if (CommonUtils.isFastDoubleClick())
//                    return;
//                if (gameBean3 != null) {
//                    UIUtils.gotoAppDetail(gameBean3, gameBean3.id,
//                            activity);
//                    if (isPopular) {
//                        MTAUtil.onClickGameRank(2);
//                    } else {
//                        MTAUtil.onClickAppRank(2);
//                    }
//                }
//                break;
//            default:
//                break;
//        }
//    }
//
//    private DownDialog mDownDialog;
//
//    private void beginDownload(final AppsItemBean bean, ProgressButtonSingleGame button) {
//        final int state = AIDLUtils.getGameAppState(
//                gameBean.packageName, gameBean.id + "",
//                gameBean.versionCode);
//        switch (state) {
//            case AppManagerCenter.APP_STATE_UNEXIST:
//            case AppManagerCenter.APP_STATE_UPDATE:
//            case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
//
//                if (ClientInfo.getAPNType(activity) == ClientInfo.NONET) {
//                    ToastUtils.showToast(R.string.nonet_connect);
//                    return;
//                }
//        }
//        if (BaseApplication.isDownloadWIFIOnly()
//                && ClientInfo.getAPNType(activity) != ClientInfo.WIFI) {
//            switch (state) {
//                case AppManagerCenter.APP_STATE_UNEXIST:
//                case AppManagerCenter.APP_STATE_UPDATE:
//                case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
//                    mDownDialog = new DownDialog(activity,
//                            R.style.add_dialog);
//                    mDownDialog.show();
//                    mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {
//
//                        @Override
//                        public void onClick(int which) {
//                            dismissDialog();
//                            switch (which) {
//                                case 0:
//                                    break;
//                                case 1:
//                                    UIUtils.downloadApp(bean);
//                                    break;
//                            }
//                        }
//                    });
//                    break;
//                default:
//                    button.onClick();
//                    break;
//            }
//
//        } else {
//            button.onClick();
//        }
//    }
//
//    private void dismissDialog() {
//        if (mDownDialog != null && mDownDialog.isShowing()) {
//            mDownDialog.dismiss();
//            mDownDialog = null;
//        }
//    }
}
