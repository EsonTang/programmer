package com.prize.appcenter.ui.pager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
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
import com.prize.app.beans.HomeAdBean;
import com.prize.app.constants.Constants;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.bean.GameGatherData;
import com.prize.appcenter.callback.ItemStateChangeCallBack;
import com.prize.appcenter.callback.OnDrawerListener;
import com.prize.appcenter.ui.adapter.AppGamePagerAdapter;
import com.prize.appcenter.ui.datamgr.HomeDataManager;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.GameListHeaderView;
import com.prize.appcenter.ui.widget.swipelayout.SwipeRefreshLayoutView.OnRefreshListener;
import com.prize.appcenter.ui.widget.swipelayout.VpSwipeRefreshLayout;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏选项
 *
 * @author 黄昌国
 * @version V1.1 2016.6.15
 */
public class GameTypePagerGifts extends BasePager {
    private String TAG = "GameTypePagerGifts";
    /**
     * 推荐应用游戏列表
     **/
    private ListView gameListView;
    private AppGamePagerAdapter adapter;
    private static final int REFRESH = 1;

    // 无更多内容加载
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;
    private int lastVisiblePosition;
    private boolean isLoadMore = true;
    private boolean isFootViewNoMore = true;
    private HomeDataManager homeDataManager = null;
    private VpSwipeRefreshLayout swipeLayout;
    private boolean isCanLoadMore = true;
    private Callback.Cancelable mCancelable;
    private GameListHeaderView mGameHeaderView;

    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private int mFirstVisibleItem;
    private boolean isFirstStatistics = false;
    private BroadcastReceiver mBroadcastReceiver;
    private long time = 0L;

    public GameTypePagerGifts(RootActivity activity, boolean isGame) {
        super(activity);
        this.isPopular = isGame;
        setNeedAddWaitingView(true);

    }

    public void onActivityCreated() {
    }

    @Override
    public void scrollToTop() {
        if (gameListView != null) {
            gameListView.setSelection(0);
        }
    }

    /**
     * 初始化界面
     */
    public View onCreateView() {
        if (activity == null)
            return null;
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.app_category_page_layout, rootView, false);
        gameListView = (ListView) root.findViewById(android.R.id.list);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, gameListView, false);
        TextView cautionTv = (TextView) noLoading.findViewById(R.id.caution_tv);
        cautionTv.setText(activity.getString(R.string.reach_botton_see_otherpage));
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        if (isPopular) {
            mGameHeaderView = new GameListHeaderView(activity);
            gameListView.addHeaderView(mGameHeaderView);
            mGameHeaderView.setmItemSelectCallBack(new ItemStateChangeCallBack() {//精彩游戏滚动、、大家都在玩滚动
                @Override
                public void OnItemSelect(List<ExposureBean> beans, boolean isWonderful) {
                    if (beans != null && beans.size() > 0) {
                        if (JLog.isDebug) {
                            JLog.i(TAG, "mExposureBeans-setmItemSelectCallBack=" + beans);
                        }
                        PrizeStatUtil.startNewUploadExposure(beans);
                    }

                }
            });
        }

        if (adapter == null) {
            adapter = new AppGamePagerAdapter(activity, this.isPopular);
        }

        adapter.setOnDrawerListener(new OnDrawerListener() {
            @Override
            public void onDataBack(List<AppsItemBean> data, int position) {

                List<ExposureBean> mExposureBeans = new ArrayList<>();
                for (AppsItemBean itenmBean : data) {
                    ExposureBean bean = CommonUtils.formatSearchHeadExposure(Constants.GAME_GUI, Constants.DRAWER, itenmBean.id, itenmBean.name, itenmBean.backParams);
                    if (bean != null && !mExposureBeans.contains(bean)) {
                        mExposureBeans.add(bean);
                    }
                }
                if (JLog.isDebug) {
                    JLog.i(TAG, "onDataBack=mExposureBeans=" + mExposureBeans.size());
                }
//                PrizeStatUtil.startUploadExposure(mExposureBeans);
                AIDLUtils.uploadDataNow(mExposureBeans);
                mExposureBeans.clear();
            }
        });


//        adapter.setmItemSelectCallBack(new ItemStateChangeCallBack() {
//            @Override
//            public void OnItemSelect(List<ExposureBean> beans, boolean isWonderfulGame) {
//                rankExposure = beans;
//                if (JLog.isDebug) {
//                    JLog.i(TAG, "setmItemSelectCallBack-rankExposure=" + rankExposure);
//                }
//                for (int i = 0; i < beans.size(); i++) {
//                    ExposureBean bean = beans.get(i);
//                    if (mExposureBeans != null && !mExposureBeans.contains(bean)) {
//                        mExposureBeans.add(bean);
//                    }
//                }
//            }
//        });
        gameListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                if (adapter.getItem(position - 1) != null) {
                    CarParentBean bean = adapter
                            .getItem(position - 1);
                    if (bean != null && bean.type.equals("apps")) {
                        UIUtils.gotoAppDetail(bean.mAppItemBean, bean.mAppItemBean.id, activity);
                        MTAUtil.onDetailClick(activity, bean.mAppItemBean.name, bean.mAppItemBean.packageName);
                        if (isPopular) {
                            MTAUtil.onclickGamePositionlist(position);
                            MTAUtil.onUMclickGamePositionlist(position);
                            MTAUtil.onclickGamelist(bean.mAppItemBean.name, bean.mAppItemBean.packageName);
                            MTAUtil.onUMclickGamelist(bean.mAppItemBean.name, bean.mAppItemBean.packageName);
                        }
                    }
                    if (bean != null && bean.type.equals("focus")) {
                        if (bean.focus != null) {
                            if (bean.focus.type.equals(Constants.APP)) {
                                UIUtils.gotoAppDetail(bean.focus.cid + "", activity);
                                if (isPopular) {
                                    MTAUtil.onGamePageFocusClick(bean.focus.positon);
                                    MTAUtil.onclickgamesingpic(bean.focus.title);
                                }
                            } else if (bean.focus.type.equals(Constants.WEB)) {
                                Intent intent = new Intent(activity,
                                        WebViewActivity.class);
                                intent.putExtra(WebViewActivity.P_URL, bean.focus.value);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                                if (isPopular) {
                                    MTAUtil.onGamePageFocusClick(bean.focus.positon);
                                    MTAUtil.onclickgamesingpic(bean.focus.title);
                                }
                            }
                        }
                    }
                }
            }
        });

        gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));

        swipeLayout = (VpSwipeRefreshLayout) root
                .findViewById(R.id.swipeRefreshLayout);
        // 顶部刷新的样式
        swipeLayout.setColorScheme(android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light);
//        swipeLayout.setBackgroundColor(context.getResources().getColor(
//                R.color.white));
        swipeLayout.setProgressViewOffset(false, 0, 100);
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    swipeLayout.setRefreshing(false);
                } else {
                    if (!isLoadMore) {
                        swipeLayout.setRefreshing(false);
                        return;
                    }
                    BaseApplication.cancelPendingRequests(TAG);
                    doHeadRequest();
                    flowHandler.sendEmptyMessageDelayed(REFRESH, 10 * 1000);
                }
            }
        });


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.prize.pageOne".equals(intent.getAction())) {
                    scrollToTop();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.prize.pageOne");
        LocalBroadcastManager.getInstance(activity.getApplicationContext()).registerReceiver(mBroadcastReceiver, filter);

        return root;
    }

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.setIsActivity(true);
                    if (!isNeedStatic||mFirstVisibleItem < 0)//此时可见头布局
                        break;
                    if (mFirstVisibleItem == 0) {
                        if (mGameHeaderView == null)
                            return;
                        List<ExposureBean> realwonderfulExposure = mGameHeaderView.getWonderfulExposureBeans();
                        if (mGameHeaderView.ismWonderfulViewVisible() && !mExposureBeans.containsAll(realwonderfulExposure)) {
                            mExposureBeans.addAll(realwonderfulExposure);
                        }
                        mExposureBeans.addAll(mGameHeaderView.getWelfareExposureBeans());
                        if (mGameHeaderView.isCommentsGalleryViewVisible()) {
                            List<ExposureBean> realcommentExposure = mGameHeaderView.getGamecommentExposureBeans();
                            mExposureBeans.addAll(realcommentExposure);
                        }
                    }
                    CarParentBean bean;
                    for (int i = mFirstVisibleItem; i <= lastVisiblePosition; i++) {
                        if (positions.contains(i)) continue;
                        if (Constants.TYPE_APP_LIST != adapter.getItemViewType(i - 1)) continue;
                        bean = adapter.getItem(i - 1);
                        if (bean != null) {
                            mExposureBeans = CommonUtils.formNewPagerExposure(mExposureBeans, Constants.GAME_GUI, bean, adapter.getItemViewType(i - 1));
                            positions.add(i);
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-positions=" + positions);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans.size()=" + mExposureBeans.size());
                    }
                    PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                    mExposureBeans.clear();
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.setIsActivity(true);
                    break;
                case OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    adapter.setIsActivity(false);
                    break;
            }
            if (!isLoadMore || swipeLayout.isRefreshing()) {
                return;
            }
            if (lastVisiblePosition >= gameListView.getCount() - 1
                    && isLoadMore) {
                isLoadMore = false;
                if (homeDataManager.hasNextPage() && isCanLoadMore) {
                    addFootView();
                    homeDataManager.getRecommandList(TAG);
                } else {
                    addFootViewNoMore();
                    isLoadMore = true;
                }
            }


        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = gameListView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
//            if (JLog.isDebug) {
//                JLog.i(TAG, "onScroll-isFirstStatistics=" + isFirstStatistics
//                        + "-lastVisiblePosition=" + lastVisiblePosition);
//            }
            if (isNeedStatic&&isFirstStatistics && lastVisiblePosition >= 0) {
                if (mFirstVisibleItem == 0) {
                    List<ExposureBean> realwonderfulExposure = mGameHeaderView.getWonderfulExposureBeans();
                    if (mGameHeaderView.ismWonderfulViewVisible() && !mExposureBeans.containsAll(realwonderfulExposure)) {
                        mExposureBeans.addAll(realwonderfulExposure);
                    }
                    mExposureBeans.addAll(mGameHeaderView.getWelfareExposureBeans());
                    if (mGameHeaderView.isCommentsGalleryViewVisible()) {
                        List<ExposureBean> realcommentExposure = mGameHeaderView.getGamecommentExposureBeans();
                        mExposureBeans.addAll(realcommentExposure);
                    }
                }
                //头布局可见
                CarParentBean bean;
                for (int i = mFirstVisibleItem; i <= lastVisiblePosition; i++) {
                    if (positions.contains(i)) continue;
                    if (Constants.TYPE_APP_LIST != adapter.getItemViewType(i - 1)) continue;
                    bean = adapter.getItem(i - 1);
                    if (bean != null) {
                        mExposureBeans = CommonUtils.formNewPagerExposure(mExposureBeans, Constants.GAME_GUI, bean, adapter.getItemViewType(i - 1));
                        positions.add(i);
                    }
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
        }
    };

    /**
     * 取消加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != gameListView)) {
            gameListView.removeFooterView(loading);
            hasFootView = false;
        }

    }

    /**
     * 加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        gameListView.addFooterView(loading);
        hasFootView = true;
    }

    public void loadData() {
        JLog.i(TAG, "loadData()");
        if (adapter == null) {
            adapter = new AppGamePagerAdapter(activity, this.isPopular);
        }
        if (homeDataManager == null) {
            homeDataManager = new HomeDataManager(this, "2");
        }
        if (0 == adapter.getCount()) {
            showWaiting();
            gameListView.setAdapter(adapter);
            requestData();//请求头部数据
        } else {
            hideWaiting();
        }

    }

    /**
     * 添加无更多加载布局
     */
    private void addFootViewNoMore() {
        if (isFootViewNoMore) {
            removeFootView();
            gameListView.addFooterView(noLoading, null, false);
            isFootViewNoMore = false;
        }
        isCanLoadMore = false;
    }


    private List<CarParentBean> filterListfocus = new ArrayList<CarParentBean>();
    private int remainderFootList = 0;

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        removeFootViewNoMore();
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        switch (what) {
            case HomeDataManager.WHAT_FAILED_NOTICE:
            case HomeDataManager.WHAT_FAILED_RECOMMAND:
                break;
            case HomeDataManager.WHAT_FAILED_LIST:
                isLoadMore = true;
                break;
            case HomeDataManager.WHAT_SUCESS_RECOMMAND:
                break;
            case HomeDataManager.WHAT_SUCESS_LIST:
                isLoadMore = true;
                if (null == obj) {
                    return;
                }
                PrizeAppsCardData data = (PrizeAppsCardData) obj;
                if (homeDataManager.isFirstPage() && data != null && data.summary != null) {
                    AppData adss = data.summary;
                    ArrayList<HomeAdBean> ads = adss.ads;
                    filterListfocus = CommonUtils.filterFocus(data.focus);
                }
                if (!homeDataManager.hasNextPage()) {
                    addFootViewNoMore();
                }
                ArrayList<CarParentBean> datas;
                ArrayList<CarParentBean> filterList;
                filterList = CommonUtils.filterInstalledAnd2CarParentBean(data.apps, threeListpkg);
                JLog.i(TAG, "onBack-homeDataManager.isFirstPage()=" + homeDataManager.isFirstPage() + "--filterList.size()=" + filterList.size() + "--filterListfocus=" + filterListfocus.size());
                if (filterListfocus.size() * 5 < filterList.size()) {
                    addFootViewNoMore();
                    datas = CommonUtils.change2CarParentBean2(filterList, filterListfocus, remainderFootList);
                } else {
                    int needFocus = filterList.size() / 5;
                    if (filterList.size() % 5 == 0) {
                        needFocus--;
                    }
                    if (needFocus <= 0)
                        return;
                    datas = CommonUtils.change2CarParentBean(filterList, filterListfocus.subList(0, needFocus), remainderFootList);
                    /*结尾的list数据个数*/
                    remainderFootList = filterList.size() % (5 * needFocus);
                    filterListfocus = filterListfocus.subList(needFocus, filterListfocus.size());
                }
                if (homeDataManager.isFirstPage()) {
                    adapter.setData(datas);
                    isFirstStatistics = true;
                    positions.clear();
                } else {
                    adapter.addData(datas);
                }

                removeFootView();
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
                isLoadMore = true;
                removeFootView();
                break;
        }
    }

    /**
     * 精彩游戏前三个
     */
    private List<String> threeListpkg = new ArrayList<>();

    private void requestData() {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/recommand/gamegather");
        mCancelable = XExtends.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {//SingGameResData
                hideWaiting();
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        GameGatherData data = new Gson().fromJson(res, GameGatherData.class);
                        if (data != null) {
                            mGameHeaderView.setData(data);
                            List<AppsItemBean> threeList = CommonUtils.filterWonderfulData(data.wonderful.apps);
                            threeListpkg.clear();
                            for (int i = 0; i < threeList.size(); i++) {
                                threeListpkg.add(threeList.get(i).packageName);
                                if (threeListpkg.size() >= 4)
                                    break;
                            }
                            homeDataManager.reSetPagerIndex(0);
                            homeDataManager.getRecommandList(TAG);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (JLog.isDebug) {
                        JLog.i("PRIZE2017", "requestData-JSONException=" + e.getMessage());
                    }
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            requestData();
                        }

                    });
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (JLog.isDebug) {
                    JLog.i("PRIZE2017", "requestData-onError=" + ex.getMessage());
                }
                loadingFailed(new ReloadFunction() {

                    @Override
                    public void reload() {
                        requestData();
                    }

                });
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
     * 图片滚动任务
     */
    private Handler flowHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH:
                    if (swipeLayout.isRefreshing()) {
                        swipeLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        BaseApplication.cancelPendingRequests(TAG);

        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(activity.getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
        }
        if (homeDataManager != null) {
            homeDataManager.setNullListener();
        }
        if (adapter != null) {
            adapter.removeDownLoadHandler();
        }
        if (flowHandler != null) {
            flowHandler.removeCallbacksAndMessages(null);
        }
        if (mGameHeaderView != null) {
            mGameHeaderView.clearCacheData();
            mGameHeaderView = null;
        }
        if (positions != null) {
            positions.clear();
        }
        if (mExposureBeans != null && mExposureBeans.size() > 0) {
            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
        }
    }

    @Override
    public String getPageName() {
        return activity.getResources().getString(R.string.game);
    }

    @Override
    public void onResume() {
        JLog.i(TAG, "onResume");
        super.onResume();
        if (adapter != null) {
            adapter.setDownloadRefreshHandle();
            adapter.setIsActivity(true);
            adapter.notifyDataSetChanged();
//            calcOnResume();
        }
        if (mGameHeaderView != null) {
            mGameHeaderView.setIsActivity(true);
            mGameHeaderView.setDownloadRefreshHandle();
            mGameHeaderView.refreshWonderful();
        }
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
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

    @Override
    public void onPause() {
        if (adapter != null) {
            adapter.setIsActivity(false);
        }
        if (mGameHeaderView != null) {
            mGameHeaderView.setIsActivity(false);
        }
        if (swipeLayout.isRefreshing()) {

            swipeLayout.setRefreshing(false);
        }
//        if (mExposureBeans != null && mExposureBeans.size() > 0) {
//            PrizeStatUtil.startUploadExposure(mExposureBeans);
//            mExposureBeans.clear();
//        }
        super.onPause();
    }

    private void doHeadRequest() {
        homeDataManager.reSetPagerIndex(0);// 此时请求第一页
        isLoadMore = false;
        filterListfocus.clear();
        remainderFootList = 0;
        if (isPopular) {
            requestData();
        }
    }

//    /**
//     * 判断后台数据是否改变，改变则重新请求网络，没改变则从内存缓存中拿数据刷新一遍
//     * 2.8版本取消。因为后台每次返回结果都是要刷新
//     */
//    @Override
//    public void onResponseHeaders(Map<String, String> headers) {
//        String last_modify = headers.get(Constants.LAST_MODIFY);
//        if (!TextUtils.isEmpty(last_modify)) {
//            long old_modifytime = UpdateCach.getInstance().getLastModifyTime(TAG);
//            if (Long.parseLong(last_modify) != old_modifytime) {
//                UpdateCach.getInstance().setlastModifyTime(TAG, last_modify);
//                homeDataManager.reSetPagerIndex(0);// 此时请求第一页
//                isLoadMore = false;
//                filterListfocus.clear();
//                remainderFootList = 0;
//                if (isPopular) {
//                    requestData();
//                }
//            } else {
//                swipeLayout.setRefreshing(false);
//            }
//        } else {
//            swipeLayout.setRefreshing(false);
//        }
//    }

    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            gameListView.removeFooterView(noLoading);
            isFootViewNoMore = true;
        }
        isCanLoadMore = true;
    }


}
