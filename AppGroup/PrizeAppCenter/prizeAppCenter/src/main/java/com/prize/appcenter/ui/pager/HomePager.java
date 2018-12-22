package com.prize.appcenter.ui.pager;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.LocationInfo;
import com.prize.app.beans.RecomandSearchWords;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.NetSourceListener;
import com.prize.app.net.datasource.base.AppData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.net.datasource.home.HomeDataSource;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.callback.NetConnectedListener;
import com.prize.appcenter.callback.OnDrawerListener;
import com.prize.appcenter.callback.UpdateWatchedManager;
import com.prize.appcenter.ui.adapter.HomeGalleryFlowAdapter;
import com.prize.appcenter.ui.adapter.HomePagerListAdapter;
import com.prize.appcenter.ui.adapter.NoticeAdapter;
import com.prize.appcenter.ui.datamgr.HomeDataManager;
import com.prize.appcenter.ui.dialog.ADDialog;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowIndicator;
import com.prize.appcenter.ui.widget.GalleryFlow;
import com.prize.appcenter.ui.widget.swipelayout.SwipeRefreshLayoutView;
import com.prize.appcenter.ui.widget.swipelayout.SwipeRefreshLayoutView.OnRefreshListener;
import com.prize.statistics.model.ExposureBean;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * *
 * 首页精品
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class HomePager extends BasePager implements OnItemClickListener,
        NetConnectedListener {
    private static final String TAG = "HomePager";
    private static final int REFRESH = 1;
    private static final int HIDE_BROARD_LLYT = REFRESH + 1;
    private static final int SCROLL = HIDE_BROARD_LLYT + 1;
    /**
     * 滚动区
     */
    private GalleryFlow galleryFlow = null;
//    /**
//     * 滚动区
//     */
//    private MZBannerView mMZBannerView = null;
    /**
     * 推荐应用游戏列表listView
     **/
    private ListView gameListView;
    /**
     * 列表adapter
     **/
    private HomePagerListAdapter adapter;
    /**
     * 图片滚动间隔
     */
    private int delayMillis = 3 * 1000;
    /**
     * 自动滚动
     */
    private boolean isAutoScroll = false;
    //    // 初始化推荐列表 初始个数
//    private final int DEFAULT_RECOMMAND_NUMBER = 60;
    private List<CarParentBean> filterListfocus = new ArrayList<CarParentBean>();
    private int remainderFootList = 0;

    /**
     * 无更多内容加载
     */
    private View noLoading = null;
    private View loading = null;
    private boolean hasFootView;
    private int mFirstVisibleItem;
    private int lastVisiblePosition;
    private boolean isCanLoadMore = true;
    private boolean isFootViewNoMore = true;
    private HomeDataManager homeDataManager = null;
    private FlowIndicator flowIndicator;
    private boolean isRequestOk = false;
    private SwipeRefreshLayoutView swipeLayout;
    private ImageView coner_Iv;
    private boolean isHasMoreData = true;
    //小广播
    private RelativeLayout broadcastLayout;
    private ImageView broadcastIv;
    private TextView broadcastTv;
    private ArrayList<RecomandSearchWords> words;
    private NoticeAdapter mNoticeAdapter;

    private String broadcastKey = "broadcastKey";
    private String adDialogKey = "adDialogKey";
    private String cornerKey = "cornerKey";
    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private List<ExposureBean> mExposure360 = new ArrayList<>();
    private boolean isFirstStatistics = true;
    private long time = 0L;


    /**
     * 图片滚动任务
     */
    private Handler flowHandler = new MyHander(this);

    /**
     * 设置是否自动滚动
     */
    public void setAutoScroll(boolean auto) {
        isAutoScroll = auto;
        if (adapter != null) {
            adapter.setIsActivity(auto);
        }

    }


    public HomePager(MainActivity context) {
        super(context);
        setNeedAddWaitingView(true);
        adapter = new HomePagerListAdapter(activity);
        if (homeDataManager == null) {
            homeDataManager = new HomeDataManager(this, "0");
        }
        UpdateWatchedManager.registNetConnectedListener(this);
    }

    public void onActivityCreated() {
    }

    @Override
    public void scrollToTop() {
        if (gameListView != null) {
            gameListView.setSelection(0);
        }
    }

    private ViewStub mViewstub;

    /**
     * 广告轮播adapter
     */
    private HomeGalleryFlowAdapter adsAdapter = null; // 广告区
    public View onCreateView() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View root = inflater.inflate(R.layout.home_page, rootView, false);
        gameListView = (ListView) root.findViewById(android.R.id.list);
        View headerView = inflater.inflate(R.layout.home_page_header, gameListView, false);
        galleryFlow = (GalleryFlow) headerView
                .findViewById(R.id.recommand_galleryflow);
        adsAdapter = new HomeGalleryFlowAdapter(activity);
        galleryFlow.setAdapter(adsAdapter);
        galleryFlow.setSelection(60 * 10);
        galleryFlow.setFocusable(false);
        galleryFlow.setFocusableInTouchMode(false);
        galleryFlow.setVerticalFadingEdgeEnabled(false);


//        mMZBannerView = (MZBannerView) headerView
//                .findViewById(R.id.recommand_galleryflow);
        flowIndicator = (FlowIndicator) headerView
                .findViewById(R.id.flowIndicator);
//        mMZBannerView.setBannerPageClickListener(new MZBannerView.BannerPageClickListener() {
//            @Override
//            public void onPageClick(View view, int position) {
//                if (JLog.isDebug) {
//                    JLog.i(TAG, "setBannerPageClickListener-ads=null?" + (ads == null));
//                    if (ads != null) {
//                        JLog.i(TAG, "ads.size()=" + ads.size() + "--position=" + position);
//                    }
//                    if (position < ads.size()) {
//                        JLog.i(TAG, "ads.get(position)=" + ads.get(position));
//                    }
//                }
//                if (ads != null && position < ads.size() && ads.get(position) != null) {
//                    MTAUtil.onClickPageRecommendBanner(position + 1 + "");
//                    HomeAdBean galleryItem = ads.get(position);// TopicItemBean
//                    UIUtils.processGoIntent(galleryItem, activity);
//                }
//            }
//        });


        GridView recommand_notice_gv = (GridView) headerView.findViewById(R.id.recommand_notice_gv);
        mNoticeAdapter = new NoticeAdapter(activity);
        recommand_notice_gv.setAdapter(mNoticeAdapter);
        recommand_notice_gv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mNoticeAdapter != null && mNoticeAdapter.getItem(position) != null) {
                    UIUtils.onClickNavbarsItem(mNoticeAdapter.getItem(position), activity);
                    MTAUtil.onClickPageRecommendEntrance(mNoticeAdapter.getItem(position).title);
                }
            }
        });
        //小广播
        mViewstub = (ViewStub) headerView.findViewById(R.id.mViewstub);
//        mMZBannerView.setDuration(2000);
//        mMZBannerView.addPageChangeLisnter(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                if (flowIndicator != null && mMZBannerView != null)
//                    flowIndicator.setSeletion(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

        galleryFlow.setOnItemClickListener(this);
        galleryFlow.setOnTouchListener(new View.OnTouchListener() {
            /** 处理当用户点中图片时，不进行滚动 */
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_CANCEL:
                        flowHandler.sendEmptyMessageDelayed(SCROLL, delayMillis);
                        swipeLayout.setEnabled(true);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        flowHandler.removeMessages(SCROLL);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        swipeLayout.setEnabled(false);
                        break;
                }
                return false;
            }
        });

        galleryFlow.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (flowIndicator != null && adsAdapter != null)
                    flowIndicator.setSeletion(adsAdapter.getItemIndex(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        coner_Iv = (ImageView) root.findViewById(R.id.coner_Iv);
        // 必须在setAdapter之前执行addHeaderView方法，否则会出现以下异常
        //   java.lang.IllegalStateException: Cannot add header view to list -- setAdapter has already been called. 三方VIvo手机（android4.4之前）出现过这报错   longbaoxiu 20161213
        if (null != headerView && gameListView.getHeaderViewsCount() <= 0) {
            gameListView.addHeaderView(headerView);
        }

        if (adapter == null) {
            adapter = new HomePagerListAdapter(activity);
        }
        adapter.setOnDrawerListener(new OnDrawerListener() {
            @Override
            public void onDataBack(List<AppsItemBean> data, int position) {
                if (JLog.isDebug) {
                    JLog.i("HomePager", "onDataBack--data.size=" + data.size());
                }
                List<ExposureBean> mExposureBeans = new ArrayList<>();
                List<ExposureBean> mNewExposureBeans = new ArrayList<>();
                ExposureBean newbean;
                ExposureBean bean;
                for (AppsItemBean itenmBean : data) {
                    if (isNeedStatic) {
                        newbean = CommonUtils.formNewPagerExposure(itenmBean, Constants.HOME_GUI, Constants.DRAWER);
                        if (newbean != null && !mNewExposureBeans.contains(newbean)) {
                            mNewExposureBeans.add(newbean);
                        }
                    }
                    bean = CommonUtils.formatSearchHeadExposure(Constants.HOME_GUI, Constants.DRAWER, itenmBean.id, itenmBean.name, itenmBean.backParams);
                    if (bean != null && !mExposureBeans.contains(bean)) {
                        mExposureBeans.add(bean);
                    }

                }
                if (JLog.isDebug) {
                    JLog.i(TAG, "onDataBack-360曝光mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onDataBack-抽屉mNewExposureBeans=" + mNewExposureBeans);

                }
                if (isNeedStatic) {
                    PrizeStatUtil.startNewUploadExposure(mNewExposureBeans);
                }
                mNewExposureBeans.clear();
                AIDLUtils.uploadDataNow(mExposureBeans);
                mExposureBeans.clear();
            }
        });
        gameListView.setAdapter(adapter);
        noLoading = inflater.inflate(R.layout.footer_nomore_show, gameListView, false);
        TextView cautionTv = (TextView) noLoading.findViewById(R.id.caution_tv);
        cautionTv.setText(activity.getString(R.string.reach_botton_see_otherpage));
        loading = inflater.inflate(R.layout.footer_loading_small, gameListView, false);

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
                        MTAUtil.onclickhomePositionlist(position);
                        MTAUtil.onUMclickhomePositionlist(position);
                        MTAUtil.onclickhomelist(bean.mAppItemBean.name, bean.mAppItemBean.packageName);
                        MTAUtil.onUMclickhomelist(bean.mAppItemBean.name, bean.mAppItemBean.packageName);
                    }
                    if (bean != null && bean.type.equals("focus")) {
                        if (bean.focus != null) {
                            if (bean.focus.type.equals(Constants.APP)) {
                                UIUtils.gotoAppDetail(bean.focus.cid, activity);
                                MTAUtil.onclickhomesingpic(bean.focus.title);
                                MTAUtil.onHomePageFocusClick(bean.focus.positon);
                            } else if (bean.focus.type.equals(Constants.WEB)) {
                                Intent intent = new Intent(activity,
                                        WebViewActivity.class);
                                intent.putExtra(WebViewActivity.P_URL, bean.focus.value);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                                MTAUtil.onclickhomesingpic(bean.focus.title);
                                MTAUtil.onHomePageFocusClick(bean.focus.positon);
                            }
                        }
                    }
                }
            }
        });
        gameListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
        swipeLayout = (SwipeRefreshLayoutView) root
                .findViewById(R.id.swipeRefreshLayout);
        // 顶部刷新的样式
        swipeLayout.setColorScheme(android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light);
        swipeLayout.setBackgroundColor(activity.getResources().getColor(
                R.color.white));
        swipeLayout.setProgressViewOffset(false, 0, 100);
        swipeLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (ClientInfo.networkType == ClientInfo.NONET) {
                    ToastUtils.showToast(R.string.net_error);
                    swipeLayout.setRefreshing(false);
                } else {
                    if (!isCanLoadMore) {
                        swipeLayout.setRefreshing(false);
                        return;
                    }
                    BaseApplication.cancelPendingRequests(TAG);
                    doHeadRequest();
                    flowHandler.sendEmptyMessageDelayed(REFRESH, 10 * 1000);
                }
            }
        });
        return root;
    }

    private OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.setIsActivity(true);
//                    mMZBannerView.start();
                    if (mFirstVisibleItem < 0)//此时可见头布局
                        break;
                    CarParentBean bean;
                    for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                        if (JLog.isDebug) {
                            JLog.i(TAG, "onScrollStateChanged位置i=" + i);
                        }
                        if (positions.contains(i)) continue;
                        if (Constants.TYPE_APP_LIST != adapter.getItemViewType(i)) continue;
                        bean = adapter.getItem(i);
                        if (bean != null) {
                            if (isNeedStatic) {
                                mExposureBeans = CommonUtils.formNewPagerExposure(mExposureBeans, Constants.HOME_GUI, bean, adapter.getItemViewType(i));
                            }
                            mExposure360 = CommonUtils.form360PagerExposure(mExposure360, Constants.HOME_GUI, bean, adapter.getItemViewType(i));
                            positions.add(i);
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-mExposure360=" + mExposure360);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposureBeans.size()=" + mExposureBeans.size());
                    }
                    AIDLUtils.uploadDataNow(mExposure360);
                    mExposure360.clear();
                    PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                    mExposureBeans.clear();
                    break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    adapter.setIsActivity(true);
//                    mMZBannerView.start();
                    break;
                case OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    adapter.setIsActivity(false);
//                    mMZBannerView.pause();
                    break;
            }
            if (swipeLayout.isRefreshing() || !isCanLoadMore)

            {
                return;
            }
            if (lastVisiblePosition >= gameListView.getCount() - 1
                    && isCanLoadMore) {
                isCanLoadMore = false;
                if (homeDataManager.hasNextPage() && isHasMoreData) {
                    addFootView();
                    homeDataManager.getRecommandList(TAG);
                } else {
                    addFootViewNoMore();
                    isCanLoadMore = true;
                }
            }


        }

        @Override
        public void onScroll(AbsListView listView, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = gameListView.getLastVisiblePosition();
            if (JLog.isDebug) {
                JLog.i(TAG, "onScroll位置isFirstStatistics=" + isFirstStatistics + "--lastVisiblePosition=" + lastVisiblePosition);
            }
            if (isFirstStatistics && lastVisiblePosition > 0) {
                CarParentBean bean;
                for (int i = mFirstVisibleItem; i < lastVisiblePosition; i++) {
                    if (positions.contains(i)) continue;
                    if (Constants.TYPE_APP_LIST != adapter.getItemViewType(i)) continue;
                    bean = adapter.getItem(i);
                    if (bean != null) {
                        if (isNeedStatic) {
                            mExposureBeans = CommonUtils.formNewPagerExposure(mExposureBeans, Constants.HOME_GUI, bean, adapter.getItemViewType(i));
                        }
                        mExposure360 = CommonUtils.form360PagerExposure(mExposure360, Constants.HOME_GUI, bean, adapter.getItemViewType(i));
                        positions.add(i);
                    }
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i(TAG, "onScroll-去重前mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onScroll-去重前360mExposureBeans=" + mExposure360);
                    JLog.i(TAG, "onScroll-mExposureBeans.size=" + mExposureBeans.size());
                }
            }
            mFirstVisibleItem = firstVisibleItem;
            AIDLUtils.uploadDataNow(mExposure360);
            mExposure360.clear();
            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
        }

    };


    /**
     * 初始化数据
     */
    private void initData() {
        initGameListData(true);
        getData();
    }

    /**
     * 从数据库获取数据
     *
     * @param isInit 是否是初始化
     */
    private void initGameListData(boolean isInit) {
        new DBAsyncTask(this, isInit).execute();
    }


    public ArrayList<RecomandSearchWords> getWords() {
        return words;
    }

    private ArrayList<HomeAdBean> ads = new ArrayList<HomeAdBean>();

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        hideWaiting();
        removeFootViewNoMore();
        if (swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(false);
        }
        switch (what) {
            case HomeDataManager.WHAT_FAILED_LIST:
                isCanLoadMore = true;
                removeFootView();
                break;

            case HomeDataManager.WHAT_SUCESS_LIST:
                isCanLoadMore = true;
                if (null == obj) {
                    return;

                }
                PrizeAppsCardData data = (PrizeAppsCardData) obj;
                if (homeDataManager.isFirstPage() && data != null && data.summary != null) {
                    AppData adss = data.summary;
                    if (adss.wordkvs != null) {
                        activity.setStrs(adss.wordkvs);
                    }
                    ads = adss.ads;
                    mNoticeAdapter.setData(adss.navbars);
                    processBroadCastState(adss.broadcasts, true, false);
                    processBroadCastState(adss.corners, false, false);
                    processBroadCastState(adss.interstitial, false, true);
                    flowIndicator.setVisibility(View.VISIBLE);
//                    if (mMZHolderCreator == null) {
//                        mMZHolderCreator = new MZHolderCreator<BannerViewHolder>() {
//                            @Override
//                            public BannerViewHolder createViewHolder() {
//                                return new BannerViewHolder();
//                            }
//                        };
//                    }
//                    if (mMZBannerView != null) {
//                        mMZBannerView.setPages(ads, mMZHolderCreator);
//                        mMZBannerView.setIndicatorVisible(false);
//                        mMZBannerView.setDelayedTime(delayMillis);
//                        mMZBannerView.start();
//                    }

                    adsAdapter.setData(adss.ads);

                    delayMillis = adss.ads.get(0).seconds * 1000;
                    flowIndicator.setCount(adss.ads.size());
                    filterListfocus = CommonUtils.filterFocus(data.focus);
                    positions.clear();
                }
                isRequestOk = true;
                if (!homeDataManager.hasNextPage()) {
                    addFootViewNoMore();
                }
                ArrayList<CarParentBean> datas;

                ArrayList<CarParentBean> filterList = CommonUtils.filterInstalledAnd2CarParentBean(data.apps, true);
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
                    filterListfocus = filterListfocus.subList(needFocus, filterListfocus.size());
                /*结尾的list数据个数*/
                    remainderFootList = filterList.size() % (5 * needFocus);
                }
                if (homeDataManager.isFirstPage()) {
                    adapter.setData(datas);
                    isFirstStatistics = true;
//                    if (gameListView != null) {
//                        gameListView.setSelection(0);
//                    }
                } else {
                    adapter.addData(datas);
                }
                removeFootView();
                break;
            case NetSourceListener.WHAT_NETERR:
                if (adapter != null && adapter.getCount() <= 0) {
                    loadingFailed(new ReloadFunction() {

                        @Override
                        public void reload() {
                            getData();
                        }
                    });
                } else {
                    ToastUtils.showToast(R.string.net_error);
                }
                removeFootView();
                isCanLoadMore = true;
                break;
        }
    }

    /**
     * 网络请求远程数据
     */
    private void getData() {
        if (homeDataManager == null) {
            homeDataManager = new HomeDataManager(this, "0");
        }
        homeDataManager.getRecommandList(TAG);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        int viewId = parent.getId();
        switch (viewId) {
            case R.id.recommand_galleryflow:
                MTAUtil.onClickPageRecommendBanner((adsAdapter
                        .getItemIndex(position) + 1) + "");
                HomeAdBean galleryItem = adsAdapter.getItem(position);// TopicItemBean
                UIUtils.processGoIntent(galleryItem, activity);
                break;
        }
    }


    @Override
    public void loadData() {
        if (0 == adapter.getCount()) {
            showWaiting();
            initData();
        } else {
            hideWaiting();
            gameListView.setVisibility(View.VISIBLE);
        }
        if (!flowHandler.hasMessages(SCROLL)) {
            flowHandler.sendEmptyMessageDelayed(SCROLL, 100);
        }
        isAutoScroll = true;
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
        isHasMoreData = false;
    }

    /**
     * 添加加载更多
     */
    private void addFootView() {
        if (hasFootView) {
            return;
        }
        gameListView.addFooterView(loading);
        hasFootView = true;
    }

    /**
     * 移除加载更多
     */
    private void removeFootView() {
        if (hasFootView && (null != gameListView)) {
            gameListView.removeFooterView(loading);
            hasFootView = false;
        }
    }

    @Override
    public void onDestroy() {
        BaseApplication.cancelPendingRequests(TAG);
        if (null != adapter) {
            adapter.removeDownLoadHandler();
        }
        UpdateWatchedManager.unregistNetConnectedListener(this);
        if (homeDataManager != null) {
            homeDataManager.setNullListener();
        }
        if (flowHandler != null) {
            flowHandler.removeCallbacksAndMessages(null);
        }
//        if (mMZBannerView != null) {
//            mMZBannerView.release();
//        }
        if (filterListfocus != null) {
            filterListfocus.clear();
        }
        if (words != null) {
            words.clear();
        }
        if (mExposureBeans != null && mExposureBeans.size() > 0) {
//            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            mExposureBeans.clear();
        }
        if (positions != null && positions.size() > 0) {
//            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
            positions.clear();
//            mExposureBeans = null;
        }
    }

    @Override
    public String getPageName() {
        return activity.getResources().getString(R.string.quality_goods);
    }

    @Override
    public void onNetConnected() {
        if (isRequestOk) {
            return;
        }
        if (isAutoScroll) {
            getData();

        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mMZBannerView != null) {
//            mMZBannerView.start();
//        }
        if (adapter != null) {
            adapter.setIsActivity(true);
            adapter.setDownloadRefreshHandle();
            adapter.notifyDataSetChanged();
//            calcOnResume();
        }
        if (ClientInfo.networkType == ClientInfo.NONET) {
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
//        if (mMZBannerView != null) {
//            mMZBannerView.pause();
//        }
        if (adapter != null) {
            adapter.setIsActivity(false);
        }
//        if (mExposureBeans != null && mExposureBeans.size() > 0) {
//            PrizeStatUtil.startNewUploadExposure(mExposureBeans);
//        }
        swipeLayout.setRefreshing(false);
        super.onPause();
    }

    private void doHeadRequest() {
        ((MainActivity) activity).isHaveDialogShow = false;
        homeDataManager.reSetPagerIndex(0);// 此时请求第一页
        isCanLoadMore = false;
        homeDataManager.getRecommandList(TAG);
        if (filterListfocus != null) {
            filterListfocus.clear();
        }
        remainderFootList = 0;
    }

    private void removeFootViewNoMore() {
        if (!isFootViewNoMore) {
            gameListView.removeFooterView(noLoading);
            isFootViewNoMore = true;
        }
        isHasMoreData = true;
    }

    private ADDialog mADDialog;

    /**
     * 是否显示插屏广告
     *
     * @param bean HomeAdBean
     */
    private void disPlayAd(final HomeAdBean bean) {
        if (bean == null && mADDialog != null && mADDialog.isShowing()) {
            mADDialog.dismiss();
            return;
        }
        if (bean == null || activity == null || mADDialog != null && mADDialog.isShowing() || ((MainActivity) activity).isHaveDialogShow || !isNeedShowView(adDialogKey, bean.id)) {
            return;
        }
        mADDialog = new ADDialog(activity, R.style.ad_dialog);
        mADDialog.setCancelable(false);
        mADDialog.setmOnButtonClic(new ADDialog.OnButtonClic() {
            @Override
            public void onClick(int which) {
                switch (which) {
                    case ADDialog.CANCEL:
                        MTAUtil.onHomeADClick("关闭插屏广告");
                        break;
                    case ADDialog.SURE:
                        UIUtils.processHomeGoIntent(bean, activity, "interstitial");
                        MTAUtil.onHomeADClick(bean.title + "_" + bean.id);
                        break;
                }
                DataStoreUtils.saveLocalInfo(adDialogKey, bean.id);
            }
        });
        mADDialog.setHomeBean(bean);
//        mADDialog.setImageUrl(bean.imageUrl, bean.title + "_" + bean.id);
    }

    /**
     * 小广播
     *
     * @param broadcasts  ArrayList<HomeAdBean>
     * @param isBroadCast 是否是小广播，还有一个是首页右下角的小图标
     */
    private void processBroadCastState(final ArrayList<HomeAdBean> broadcasts, boolean isBroadCast, boolean isAD) {
        //当数据为空时，隐藏
        if (broadcasts == null || broadcasts.size() <= 0) {
            if (isAD) {
                disPlayAd(null);
            } else {
                if (isBroadCast) {
                    showBroadCast(null);
                } else {
                    showConner(null);
                }
            }
            return;
        }

        long currentTime = System.currentTimeMillis();
        for (HomeAdBean bean : broadcasts) {
            //Date或者String转化为时间戳
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                long startTime = format.parse(bean.startTime).getTime();
                long endTime = format.parse(bean.endTime).getTime();
                if (startTime <= currentTime && currentTime <= endTime) {
                    if (isAD) {
                        disPlayAd(bean);
                    } else {
                        if (isBroadCast) {
                            showBroadCast(bean);
                        } else {
                            showConner(bean);
                        }
                    }
                    return;
                } else {
                    if (isAD) {
                        disPlayAd(null);
                    } else {
                        if (isBroadCast) {
                            showBroadCast(null);
                        } else {
                            showConner(null);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 是否显示小广播
     *
     * @param bean HomeAdBean
     */
    private void showBroadCast(final HomeAdBean bean) {
        if (bean != null && Arrays.asList(Constants.BROADCAST_AD_TYPES).contains(bean.adType) && isNeedShowView(broadcastKey, bean.id)) {
            if (broadcastLayout == null) {
                broadcastLayout = (RelativeLayout) mViewstub.inflate();
                broadcastIv = (ImageView) broadcastLayout.findViewById(R.id.broadcast_iv);
                broadcastTv = (TextView) broadcastLayout.findViewById(R.id.broadcast_tv);
            }
            broadcastLayout.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(bean.imageUrl, broadcastIv, UILimageUtil.getBroadcastLoptions());
            broadcastTv.setText(bean.description);
            broadcastLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    UIUtils.processHomeGoIntent(bean, activity, "broadcast");
                    MTAUtil.onHomeBroadcastClicked(activity);
                    DataStoreUtils.saveLocalInfo(broadcastKey, bean.id);
                    if (flowHandler != null) {
                        flowHandler.sendEmptyMessageDelayed(HIDE_BROARD_LLYT, 300);
                    }
                }
            });

            if (isNeedStatic && (Constants.BROADCAST_AD_TYPES[6].equals(bean.adType) || Constants.BROADCAST_AD_TYPES[7].equals(bean.adType))) {
                if (bean.app != null && !TextUtils.isEmpty(bean.app.packageName)) {
                    ExposureBean Pbean = CommonUtils.formNewPagerExposure(bean.app, Constants.HOME_GUI, "broadcast");
                    List<ExposureBean> temp = new ArrayList<>();
                    temp.add(Pbean);
                    PrizeStatUtil.startNewUploadExposure(temp);
                    temp.clear();
                }
            }


        } else {
            if (broadcastLayout != null) {
                broadcastLayout.setVisibility(View.GONE);
            }
        }
    }

    private boolean isNeedShowView(String key, String id) {
        String value = DataStoreUtils.readLocalInfo(key);
        return TextUtils.isEmpty(value) || !id.equals(value);
    }

    private DownDialog mDownDialog;

    /**
     * 是否显示小角标
     *
     * @param mCorner HomeAdBean
     */
    private void showConner(final HomeAdBean mCorner) {
        if (coner_Iv == null)
            return;
        if (mCorner == null) {
            coner_Iv.setVisibility(View.GONE);
            return;
        }
        if (isNeedStatic && (Constants.BROADCAST_AD_TYPES[6].equals(mCorner.adType) || Constants.BROADCAST_AD_TYPES[7].equals(mCorner.adType))) {
            if (mCorner.app != null && !TextUtils.isEmpty(mCorner.app.packageName)) {
                ExposureBean bean = CommonUtils.formNewPagerExposure(mCorner.app, Constants.HOME_GUI, "corner");
                List<ExposureBean> temp = new ArrayList<>();
                temp.add(bean);
                PrizeStatUtil.startNewUploadExposure(temp);
                temp.clear();
            }
        }
        if (mCorner != null && !TextUtils.isEmpty(mCorner.imageUrl) && Arrays.asList(Constants.BROADCAST_AD_TYPES).contains(mCorner.adType) && isNeedShowView(cornerKey, mCorner.id)) {
            coner_Iv.setVisibility(View.VISIBLE);
            coner_Iv.setFocusable(true);
            coner_Iv.setClickable(true);
            if (Constants.BROADCAST_AD_TYPES[6].equals(mCorner.adType)) {
                if (mCorner.app != null && !TextUtils.isEmpty(mCorner.app.packageName)) {
                    mCorner.app = CommonUtils.formatAppPageInfo(mCorner.app, Constants.HOME_GUI, "corner", 0);
                }
            }
            Glide.with(activity.getApplicationContext()).load(mCorner.imageUrl).into(coner_Iv);
            coner_Iv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (Constants.BROADCAST_AD_TYPES[6].equals(mCorner.adType)) {
                        if (mCorner.app != null && !TextUtils.isEmpty(mCorner.app.packageName)) {

                            final int state = AIDLUtils.getGameAppState(
                                    mCorner.app.packageName, mCorner.app.id,
                                    mCorner.app.versionCode);
                            if (state == AppManagerCenter.APP_STATE_UNEXIST
                                    || state == AppManagerCenter.APP_STATE_UPDATE || state == AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE) {
                                if (ClientInfo.getAPNType(activity.getApplicationContext()) == ClientInfo.NONET) {
                                    ToastUtils.showToast(R.string.net_error);
                                    return;
                                }
                                if (BaseApplication.isDownloadWIFIOnly()
                                        && ClientInfo.getAPNType(activity) != ClientInfo.WIFI) {
                                    if (mDownDialog == null) {
                                        mDownDialog = new DownDialog(activity,
                                                R.style.add_dialog);
                                    }
                                    mDownDialog.show();
                                    mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                                        @Override
                                        public void onClick(int which) {
                                            dismissDialog();
                                            switch (which) {
                                                case 0:
                                                    break;
                                                case 1:
                                                    startAnimation(state, coner_Iv);
                                                    UIUtils.downloadApp(mCorner.app);
                                                    break;
                                            }
                                        }
                                    });
                                } else {
                                    startAnimation(state, coner_Iv);
                                    UIUtils.downloadApp(mCorner.app);
                                }
                            } else if (state == AppManagerCenter.APP_STATE_DOWNLOADING || state == AppManagerCenter.APP_STATE_INSTALLING) {
                                UIUtils.gotoAppDetail(mCorner.app, mCorner.app.id, activity);
                            } else if (state == AppManagerCenter.APP_STATE_INSTALLED) {
                                UIUtils.startSingleGame(mCorner.app.packageName);
                            }
                        }

                    } else {
                        UIUtils.processHomeGoIntent(mCorner, activity, "corner");
                    }
                    coner_Iv.setVisibility(View.GONE);
                    MTAUtil.onHomeConerClicked(activity);
                    DataStoreUtils.saveLocalInfo(cornerKey, mCorner.id);
                }
            });
        } else {
            coner_Iv.setVisibility(View.GONE);
        }
    }

    public void startAnimation(int state, ImageView imgeView) {
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(imgeView);
            }
        }
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

//    private MZHolderCreator<BannerViewHolder> mMZHolderCreator;

    private static class DBAsyncTask extends AsyncTask<String, Void, PrizeAppsCardData> {
        WeakReference<HomePager> pagers;
        boolean isInit;

        DBAsyncTask(HomePager pager, boolean isInit) {
            pagers = new WeakReference<HomePager>(pager);
            this.isInit = isInit;
        }

        @Override
        protected PrizeAppsCardData doInBackground(String[] params) {
            String json = HomeDataSource.getListJson();
            if (TextUtils.isEmpty(ClientInfo.getInstance().location)) {
                String location = DataStoreUtils.readLocalInfo("locationKey");
                if (!TextUtils.isEmpty(location)) {
                    LocationInfo info = new Gson().fromJson(location, LocationInfo.class);
                    ClientInfo.getInstance().latitude = info.latitude;
                    ClientInfo.getInstance().longitude = info.lontitude;
                    ClientInfo.getInstance().location = info.address;
                }
            }
            try {
                if (!TextUtils.isEmpty(json)) {
                    return new Gson().fromJson(json,
                            PrizeAppsCardData.class);
                } else {
                    return null;
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PrizeAppsCardData bean) {
            if (bean == null || pagers == null || pagers.get() == null)
                return;
            HomePager pager = pagers.get();
            pager.adapter.clearAll();
            ArrayList<HomeAdBean> adList = new ArrayList<HomeAdBean>();
            if (bean.summary != null) {
                if (bean.summary.words != null) {
                    pager.words = bean.summary.wordkvs;
                    pager.activity.setStrs(pager.words);
                }
                adList = bean.summary.ads;
                pager.mNoticeAdapter.setData(bean.summary.navbars);
            }
//            if (pager.mMZHolderCreator == null) {
//                pager.mMZHolderCreator = new MZHolderCreator<BannerViewHolder>() {
//                    @Override
//                    public BannerViewHolder createViewHolder() {
//                        return new BannerViewHolder();
//                    }
//                };
//            }
//            pager.mMZBannerView.setPages(adList, pager.mMZHolderCreator);
//            pager.mMZBannerView.setIndicatorVisible(false);

            pager.adsAdapter.setData(adList);
            pager.flowIndicator.setCount(adList.size());
            pager.flowIndicator.setVisibility(adList.size() > 0 ? View.VISIBLE
                    : View.GONE);
            ArrayList<CarParentBean> datas;
            List<CarParentBean> filterListfocusT = CommonUtils.filterFocus(bean.focus);
            ArrayList<CarParentBean> filterList = CommonUtils.filterInstalledAnd2CarParentBean(bean.apps, true);

//            if (pager.mTopThreeApps != null && pager.mTopThreeApps.size() > 0) {
//                pager.mTopThreeLayout.setData(pager.mTopThreeApps, TextUtils.isEmpty(pager.mTopThreeTitle) ? bean.summary.listTitle : pager.mTopThreeTitle);
//                pager.adapter.setTopThreeGridView(pager.mTopThreeLayout);
//            }

            if (!isInit) {
                pager.filterListfocus = filterListfocusT;
            }
            if (filterListfocusT.size() == 0 || filterList.size() == 0)
                return;

            if (filterListfocusT.size() * 5 < filterList.size()) {
                pager.addFootViewNoMore();
                datas = CommonUtils.change2CarParentBean2(filterList, filterListfocusT, pager.remainderFootList);
            } else {
                pager.removeFootViewNoMore();
                int needFocus = filterList.size() / 5;
                if (filterList.size() % 5 == 0) {
                    needFocus--;
                }
                datas = CommonUtils.change2CarParentBean(filterList, filterListfocusT.subList(0, needFocus), pager.remainderFootList);
                if (needFocus <= 0) {
                    return;
                }
                if (!isInit) {
                    pager.filterListfocus = filterListfocusT.subList(needFocus, filterListfocusT.size());
                    pager.remainderFootList = filterList.size() % (5 * needFocus);
                }
            }
            pager.adapter.setData(datas);
        }
    }

    private static class MyHander extends Handler {
        private WeakReference<HomePager> mActivities;

        MyHander(HomePager mActivity) {
            this.mActivities = new WeakReference<HomePager>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final HomePager activity = mActivities.get();
            if (activity != null) {
                //执行业务逻辑
                if (msg != null) {
                    switch (msg.what) {
                        case REFRESH:
                            if (activity.swipeLayout.isRefreshing()) {
                                activity.swipeLayout.setRefreshing(false);
                            }
                            break;
                        case HIDE_BROARD_LLYT:
                            if (activity.broadcastLayout != null) {
                                activity.broadcastLayout.setVisibility(View.GONE);
                            }
                            break;
                        case SCROLL: {
                            if (activity.isAutoScroll && (null != activity.galleryFlow)) {
                                activity.galleryFlow.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
                                removeMessages(SCROLL);
                            }
                            sendEmptyMessageDelayed(SCROLL, activity.delayMillis);
                        }
                        default:
                            break;
                    }
                }
            }
        }
    }
}
