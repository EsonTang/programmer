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
import com.prize.app.beans.HomeAdBean;
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
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.callback.OnDrawerListener;
import com.prize.appcenter.fragment.base.BaseFragment;
import com.prize.appcenter.ui.actionBar.ActionBarActivity.ReloadFunction;
import com.prize.appcenter.ui.actionBar.ActionBarNoTabActivity;
import com.prize.appcenter.ui.adapter.SearchListAdapter;
import com.prize.appcenter.ui.datamgr.SearchResultListManager;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ImageUtil;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.AdvertisingView;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.LinearLayoutForDetail;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;
import com.prize.statistics.model.ExposureBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * *
 * 搜索结果
 *
 * @author longbaoxiu
 * @version V1.0
 */
public class SearchResultFragment extends BaseFragment implements OnClickListener {
    //    private String widget = "SearchResultHead";
//    public static String search_Drawer = "Search_Drawer";
    private View root;
    private View headView;
    private ImageView mBannerHeadView;
    private final String TAG = "SearchResultFragment";
    private ActionBarNoTabActivity activity;
    private SearchListAdapter adapter;
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

    private TextView searchresult_Tv;
    private LinearLayout no_match_Llyt;


//    private ExposureBean bannerExposure;

    //    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private List<ExposureBean> mNewHeadExposure = new ArrayList<>();
    private List<ExposureBean> mNewHead360 = new ArrayList<>();
    private int mFirstVisibleItem;
    private boolean isFirstStatistics = true;
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
    /**
     * 大家都在搜索
     ***/
    private LinearLayout container_Llyt;
    private int hight = 0;
    private IUIDownLoadListenerImp listener = null;
    private Handler mHandler = new MyHander(this);
    private AdvertisingView left_AdvertisingView, right_AdvertisingView;
    /*记录已经曝光的位置*****/
    private List<Integer> positions = new ArrayList<>();
    private boolean isNeedStatic = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_search_result, container,
                false);
        headView = inflater.inflate(R.layout.head_search_match, null);
        activity = (ActionBarNoTabActivity) getActivity();
        mBannerHeadView = new ImageView(activity);
        mBannerHeadView.setScaleType(ImageView.ScaleType.FIT_XY);
        isNeedStatic = BaseApplication.isNeedStatic;
        mBannerHeadView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (ClientInfo.getInstance().screenWidth / 2.57)));
        isTag = getArguments().getBoolean("tag", false);
        findViewById();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                mHandler.removeCallbacksAndMessages(null);
                Message msg = Message.obtain();
                msg.what = 0;
                msg.obj = pkgName;
                mHandler.sendMessage(msg);

            }
        });
        AIDLUtils.registerCallback(listener);
        mToken = AIDLUtils.bindToService(activity, this);
        init();
        setListener();
        return root;
    }

    @Override
    protected void findViewById() {
        mListView = (ListView) root.findViewById(android.R.id.list);
        searchresult_Tv = (TextView) root.findViewById(R.id.searchresult_Tv);
        no_match_Llyt = (LinearLayout) root.findViewById(R.id.no_match_Llyt);
        container_Llyt = (LinearLayout) root.findViewById(R.id.container_Llyt);
        mLinerLayout = (LinearLayoutForDetail) headView.findViewById(R.id.child_id);
        left_AdvertisingView = (AdvertisingView) headView.findViewById(R.id.left_AdvertisingView);
        right_AdvertisingView = (AdvertisingView) headView.findViewById(R.id.right_AdvertisingView);
        reloadView = root.findViewById(R.id.reload_Llyt);
        waitView = root.findViewById(R.id.loading_Llyt_id);
        game_download_Rlyt = (RelativeLayout) headView.findViewById(R.id.game_download_Rlyt);
        gameIcon = (ImageView) headView.findViewById(R.id.game_iv);
        gameName = (TextView) headView.findViewById(R.id.game_name_tv);
        gameSize = (TextView) headView.findViewById(R.id.game_size_tv);
        game_brief = (TextView) headView.findViewById(R.id.game_brief);
        searchmsg_Tv = (TextView) headView.findViewById(R.id.searchmsg_Tv);
        tag_container = (FlowLayout) headView.findViewById(R.id.tag_container);
        ourtag_container = (FlowLayout) headView.findViewById(R.id.ourtag_container);
        downloadBtn = (AnimDownloadProgressButton) headView.findViewById(R.id.game_download_btn);
    }

    @Override
    protected void setListener() {
        headView.findViewById(R.id.item_rlyt).setOnClickListener(this);
        headView.findViewById(R.id.child_id).setOnClickListener(this);
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
                    MTAUtil.onSearchResultListClick(realPostion + 1, adapter.getItem(realPostion).name);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("AppsItemBean",
                            adapter.getItem(realPostion));
                    bundle.putString("appid", adapter.getItem(realPostion).id);
                    bundle.putString("from", "search");
                    bundle.putString("keyWord", keyword);
                    PrizeStatUtil.onSearchResultItemClick(adapter.getItem(realPostion).id, adapter.getItem(realPostion).packageName, adapter.getItem(realPostion).name, keyword, true);
                    UIUtils.gotoAppDetailFromSearch(bundle, activity);
                    MTAUtil.onDetailClick(activity, adapter.getItem(realPostion).name, adapter.getItem(realPostion).packageName);
                    if (adapter.getItemViewType(realPostion) == 1) {
                        MTAUtil.onSearchADAppClick(adapter.getItem(realPostion).name);
                    }
                }

            }
        });

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader
                .getInstance(), true, true, mOnScrollListener));
    }

    OnScrollListener mOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            switch (scrollState) {
                case OnScrollListener.SCROLL_STATE_IDLE:
                    adapter.setIsActivity(true);
                    if (mFirstVisibleItem < 0)
                        break;
                    int tempInt = lastVisiblePosition;
                    if (mListView.getHeaderViewsCount() > 0) {
                        tempInt--;
                    }

                    List<ExposureBean> mExposureBeans = new ArrayList<>();
                    List<ExposureBean> mExposure360 = new ArrayList<>();
                    ExposureBean bean360;
                    ExposureBean newBean;
                    for (int i = mFirstVisibleItem; i <= tempInt; i++) {
                        if (positions.contains(i)) {
                            continue;
                        }
                        AppsItemBean appsItemBean = adapter.getItem(i);
                        if (appsItemBean == null) continue;
                        if (isNeedStatic) {
                            newBean = CommonUtils.formNewPagerExposure(appsItemBean, Constants.SEARCH_RESULT_GUI, Constants.LIST);
                            mExposureBeans.add(newBean);
                        }
                        bean360 = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI,
                                Constants.LIST, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                        if (bean360 != null) {
                            mExposure360.add(bean360);
                        }
                        positions.add(i);
                    }

                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrollStateChanged-新版mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrollStateChanged-mExposure360=" + mExposure360);
                    }
                    if (isNeedStatic) {
                        PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                    }
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

            if (lastVisiblePosition >= mListView.getCount() - 1 && isLoadMore) {
                isLoadMore = false;
                if (manager.isListNextPage()) {
                    addFootView();
                    manager.getNextListPage(TAG);
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            lastVisiblePosition = mListView.getLastVisiblePosition();
            mFirstVisibleItem = firstVisibleItem;
            if (isFirstStatistics && lastVisiblePosition > 0) {
                if (mFirstVisibleItem == 0) {//头布局可见
                    if (mListView.getHeaderViewsCount() > 0) {
                        View subHeadView = mListView.getChildAt(0);
                        if (subHeadView != null) {
                            if (subHeadView instanceof RelativeLayout) {//完全匹配时的布局
                                if (mNewHeadExposure.size() > 0) {
                                    if (JLog.isDebug) {
                                        JLog.i(TAG, "onScroll-mNewHeadExposure=" + mNewHeadExposure);
                                    }
                                    if (isNeedStatic) {
                                        PrizeStatUtil.startNewUploadExposure(mNewHeadExposure);
                                    }
                                    mNewHeadExposure.clear();
                                }
                                if (mNewHead360.size() > 0) {
                                    if (JLog.isDebug) {
                                        JLog.i(TAG, "onScroll-mNewHead360=" + mNewHead360);
                                    }
//                                    PrizeStatUtil.startUploadExposure(mNewHeadExposure);
                                    AIDLUtils.uploadDataNow(mNewHead360);
                                    mNewHead360.clear();
                                }

                            }
                        }
                    }
                }
                int tempInt = lastVisiblePosition;
                if (mListView.getHeaderViewsCount() > 0) {
                    tempInt--;
                }
                List<ExposureBean> mExposureBeans = new ArrayList<>();
                List<ExposureBean> mExposure360 = new ArrayList<>();
                ExposureBean bean360;
                ExposureBean newBean;
                for (int i = mFirstVisibleItem; i <= tempInt; i++) {
                    if (positions.contains(i)) {
                        continue;
                    }
                    AppsItemBean appsItemBean = adapter.getItem(i);
                    if (appsItemBean == null) continue;
                    if (isNeedStatic) {
                        newBean = CommonUtils.formNewPagerExposure(appsItemBean, Constants.SEARCH_RESULT_GUI, Constants.LIST);
                        mExposureBeans.add(newBean);
                    }
                    bean360 = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI, Constants.LIST, appsItemBean.id, appsItemBean.name, appsItemBean.backParams);
                    if (bean360 != null) {
                        mExposure360.add(bean360);
                    }
                    positions.add(i);
                }
                isFirstStatistics = false;
                if (JLog.isDebug) {
                    JLog.i(TAG, "onScroll-mExposureBeans=" + mExposureBeans);
                    JLog.i(TAG, "onScroll-mExposure360=" + mExposure360);
                    JLog.i(TAG, "onScroll-positions=" + positions);
                }
                if (isNeedStatic) {
                    PrizeStatUtil.startNewUploadExposure(mExposureBeans);
                }
                mExposureBeans.clear();
//                PrizeStatUtil.startUploadExposure(mExposure360);
                AIDLUtils.uploadDataNow(mExposure360);
                mExposure360.clear();
            }

        }
    };


    @Override
    protected void init() {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View noContent = inflater.inflate(R.layout.footer_nomore_show, null);
        ((TextView) noContent.findViewById(R.id.caution_tv))
                .setText(R.string.no_result_find);
        loading = inflater.inflate(R.layout.footer_loading_small, null);
        keyword = getArguments().getString("keyword");
        adapter = new SearchListAdapter(activity);
        adapter.setDownlaodRefreshHandle();
        mListView.setAdapter(adapter);
        adapter.setOnDrawerListener(new OnDrawerListener() {
            @Override
            public void onDataBack(List<AppsItemBean> data, int position) {
                if (JLog.isDebug) {
                    JLog.i(TAG, "onDataBack-drawerData=" + data.size());
                }
                List<ExposureBean> mExposureBeans = new ArrayList<>();
                List<ExposureBean> mNewExposureBeans = new ArrayList<>();
                ExposureBean newbean;
                ExposureBean bean;
                for (AppsItemBean itenmBean : data) {
                    bean = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI, Constants.DRAWER, itenmBean.id, itenmBean.name, itenmBean.backParams);

                    if (bean != null && !mExposureBeans.contains(bean)) {
                        mExposureBeans.add(bean);
                    }
                    if (isNeedStatic) {
                        newbean = CommonUtils.formNewPagerExposure(itenmBean, Constants.SEARCH_RESULT_GUI, Constants.DRAWER);
                        if (newbean != null && !mNewExposureBeans.contains(newbean)) {
                            mNewExposureBeans.add(newbean);
                        }
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
//                PrizeStatUtil.startUploadExposure(mExposureBeans);
                mExposureBeans.clear();
            }
        });
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
                final PrizeAppsTypeData prizeAppsTypeData = ((PrizeAppsTypeData) obj);
                List<AppsItemBean> data = prizeAppsTypeData.apps;
                mListView.setVisibility(View.VISIBLE);
                if (manager.isFirstPage()) {
                    isFirstStatistics = true;
                    if (mNewHeadExposure != null) {
                        mNewHeadExposure.clear();
                    }
                    if (mNewHead360 != null) {
                        mNewHead360.clear();
                    }
                    positions.clear();
                    headAppItem = data.get(0);
                    headAppItem = CommonUtils.formatAppPageInfo(headAppItem, Constants.SEARCH_RESULT_GUI, Constants.LIST, 1);
                    ExposureBean bean = CommonUtils.formNewPagerExposure(headAppItem, Constants.SEARCH_RESULT_GUI, Constants.LIST);
                    ExposureBean bean360 = CommonUtils.formatSearchHeadExposure(Constants.SEARCH_RESULT_GUI, Constants.LIST, headAppItem.id, headAppItem.name, headAppItem.backParams);
                    if (bean360 != null) {
                        mNewHead360.add(bean360);
                    }
                    if (isNeedStatic && !mNewHeadExposure.contains(bean)) {
                        mNewHeadExposure.add(bean);
                    }
                    if (data == null || data.size() <= 0) {

                    } else {
                        if (prizeAppsTypeData.status != null) {
                            mSearchStatusResBean = prizeAppsTypeData.status;
                            initAllSearch();
                            if (mSearchStatusResBean.type == 1) {//完全匹配
                                if (mListView.getHeaderViewsCount() <= 0) {
                                    if (Build.VERSION.SDK_INT <= 17) {//// FIXME: 2017/1/7  longbaoixu  适配3方4.4以下版本
                                        mListView.setAdapter(null);
                                        mListView.addHeaderView(headView);
                                        mListView.setAdapter(adapter);
                                    } else {
                                        mListView.addHeaderView(headView);
                                    }
                                } else {
                                    mListView.removeHeaderView(mBannerHeadView);
                                    if (mListView.getHeaderViewsCount() <= 0) {
                                        mListView.addHeaderView(headView);
                                    }
                                }
                                if (prizeAppsTypeData.pops != null && !TextUtils.isEmpty(prizeAppsTypeData.pops.type)
                                        && prizeAppsTypeData.pops.type.equals("apps")) {
                                    if (prizeAppsTypeData.pops.apps != null && (prizeAppsTypeData.pops.apps.size()) >= 2) {
                                        headView.findViewById(R.id.game_image_tag).setVisibility(View.VISIBLE);
                                        AppsItemBean leftBean = prizeAppsTypeData.pops.apps.get(0);
                                        leftBean = CommonUtils.formatAppPageInfo(leftBean, Constants.SEARCH_RESULT_GUI, Constants.LIST, 2);
                                        left_AdvertisingView.setData(leftBean);
                                        ExposureBean exposureBean = CommonUtils.formNewPagerExposure(leftBean, Constants.SEARCH_RESULT_GUI, Constants.LIST);
                                        if (!mNewHeadExposure.contains(exposureBean)) {
                                            mNewHeadExposure.add(exposureBean);
                                        }
                                        AppsItemBean rightBean = prizeAppsTypeData.pops.apps.get(1);
                                        rightBean = CommonUtils.formatAppPageInfo(rightBean, Constants.SEARCH_RESULT_GUI, Constants.LIST, 3);
                                        ExposureBean rightExposureBean = CommonUtils.formNewPagerExposure(rightBean, Constants.SEARCH_RESULT_GUI, Constants.LIST);
                                        if (!mNewHeadExposure.contains(rightExposureBean)) {
                                            mNewHeadExposure.add(rightExposureBean);
                                        }
                                        right_AdvertisingView.setData(rightBean);
                                        data.removeAll(prizeAppsTypeData.pops.apps);
                                    } else {
                                        headView.findViewById(R.id.game_image_tag).setVisibility(View.GONE);
                                    }
                                } else {
                                    headView.findViewById(R.id.game_image_tag).setVisibility(View.GONE);
                                }
                                processHeadData();
                                data = data.subList(1, data.size());
                            } else {//不完全匹配
                                if (mListView.getHeaderViewsCount() > 0) {
                                    mListView.removeHeaderView(headView);
                                }
                                if (mListView.getHeaderViewsCount() > 0) {
                                    mListView.removeHeaderView(mBannerHeadView);
                                }
                                if (prizeAppsTypeData.pops != null && !TextUtils.isEmpty(prizeAppsTypeData.pops.type)) {
                                    if (prizeAppsTypeData.pops.type.equals("ads")) {//不完全匹配推广的广告banner
                                        if (prizeAppsTypeData.pops.ads != null) {
                                            final HomeAdBean bannerBean = prizeAppsTypeData.pops.ads;
//                                            bannerExposure = CommonUtils.formatGameHeadExposure(Constants.SEARCH_RESULT_GUI, widget, bannerBean.adType, 1, bannerBean.id, bannerBean.title, null);
                                            ImageLoader.getInstance().displayImage(bannerBean.bigImageUrl, mBannerHeadView, UILimageUtil.getADUILoptions());
                                            if (mListView.getHeaderViewsCount() <= 0) {
                                                mListView.addHeaderView(mBannerHeadView);
                                            }
                                            mBannerHeadView.setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    UIUtils.processGoIntent(bannerBean, activity);
                                                    MTAUtil.onSearchADAppClick(bannerBean.title);
                                                }
                                            });
                                        } else {
                                            mListView.addHeaderView(mBannerHeadView);
                                        }
                                    } else if (prizeAppsTypeData.pops.type.equals("apps")) {//不完全匹配推广的应用
                                        if (prizeAppsTypeData.pops.apps != null && (prizeAppsTypeData.pops.apps.size()) >= 2) {
                                            List<AppsItemBean> tempList = new ArrayList<>();//获取前2位的元素
//                                            List<AppsItemBean> tempList = data.subList(0, 2);//获取前2位的元素
                                            for (int i = 0; i < 2; i++) {
                                                tempList.add(data.get(i));
                                            }
                                            prizeAppsTypeData.pops.apps.removeAll(tempList);
                                            List<AppsItemBean> tempList2 = data.subList(2, data.size());//除去前2位的元素
                                            List<AppsItemBean> tempList3 = tempList2;//除去前2位的元素
                                            ArrayList<AppsItemBean> list = new ArrayList<>();
                                            if (prizeAppsTypeData.pops.apps.size() > 0) {
                                                tempList3.removeAll(prizeAppsTypeData.pops.apps);//从第三个开始过滤推广的，推广出现的优先
                                                list.addAll(tempList3);//位置已经改变了 so..
                                                for (int i = 0; i < prizeAppsTypeData.pops.apps.size(); i++) {
                                                    AppsItemBean bean1 = prizeAppsTypeData.pops.apps.get(i);
                                                    bean1.isAdvertise = true;
                                                    tempList.add(bean1);//推广出现的优先，推广数据加到第三位：既添加到list尾部
                                                }
                                                tempList.addAll(list);
                                                data = tempList;
                                            }
                                        }
                                    }

                                } else {
                                    mListView.removeHeaderView(mBannerHeadView);
                                }
                                headAppItem = null;

                                if (mSearchStatusResBean.type == 2) {//完全不匹配
                                    no_match_Llyt.setVisibility(View.VISIBLE);
                                    searchresult_Tv.setText(mSearchStatusResBean.displayText);
                                }
                            }
                        }
                    }

                    adapter.setData(CommonUtils.filter360Data(data));
                    mListView.setSelection(0);
                    return;
                }
                if (manager.getCurPageIndex() == 2) {//只针对前2页的360的已安装数据去重
                    adapter.addData(CommonUtils.filter360Data(data));
                } else {
                    adapter.addData(data);
                }


                break;
            case NetSourceListener.WHAT_NETERR:
                isLoadMore = true;
                manager.getCurPageIndex();
                if (manager.getCurPageIndex() == 0) {
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
     * 初始化大家都在搜
     */

    private void initAllSearch() {
        if (mSearchStatusResBean.keywords != null && mSearchStatusResBean.keywords.length > 0) {
            container_Llyt.removeAllViews();
            for (int i = 0; i < mSearchStatusResBean.keywords.length; i++) {
                final TextView textView = (TextView) LayoutInflater.from(activity)
                        .inflate(R.layout.item_allsearch, null);
                textView.setText(mSearchStatusResBean.keywords[i]);
                textView.setTag(i);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((SearchActivity) activity).searchView.setTextForEditText(textView.getText().toString());
                        requestData(textView.getText().toString());
                        MTAUtil.onClickSearchResultLabel(((int) view.getTag()) + 1);
                    }
                });

                container_Llyt.addView(textView);
            }
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
        no_match_Llyt.setVisibility(View.GONE);
        removeFootView();
        BaseApplication.cancelPendingRequests(TAG);
        SearchHistoryDao.insert(keyword, java.lang.System.currentTimeMillis());
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
        AIDLUtils.unregisterCallback(listener);
        listener.setmCallBack(null);
        listener = null;
//        if (mExposureBeans != null && mExposureBeans.size() > 0) {
//            PrizeStatUtil.startUploadExposure(mExposureBeans);
//            mExposureBeans.clear();
//        }
        AIDLUtils.unbindFromService(mToken);
    }

    @Override
    public void onResume() {
        if (adapter != null) {
            adapter.setIsActivity(true);
//            calcOnResume();
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
                    String[] tags = null;
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
                        if (BaseApplication.isDownloadWIFIOnly()
                                && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(activity, R.style.add_dialog);
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
                                            if (headAppItem != null) {
                                                UIUtils.downloadApp(headAppItem);
                                                if (state == AppManagerCenter.APP_STATE_UNEXIST) {
                                                    PrizeStatUtil.onSearchResultItemClick(headAppItem.id, headAppItem.packageName, headAppItem.name, keyword, true);
                                                }
                                            }
                                            break;
                                    }
                                }
                            });

                        } else {
                            downloadBtn.onClick();
                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
                                AIDLUtils.upload360ClickDataNow(headAppItem.backParams, headAppItem.name, headAppItem.packageName);
                            }
                        }
                        break;
                    default:
                        downloadBtn.onClick();
                        break;
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

        if (headAppItem.name != null){
            gameName.setText(headAppItem.name);
        }
        gameSize.setText(headAppItem.apkSizeFormat);
        if (!TextUtils.isEmpty(headAppItem.brief)){
            game_brief.setVisibility(View.VISIBLE);
            game_brief.setText(headAppItem.brief);
            tag_container.setVisibility(View.GONE);
            game_brief.setCompoundDrawablePadding(0);
            game_brief.setTextColor(activity.getResources()
                    .getColor(R.color.text_color_6c6c6c));
        } else{
            if (!TextUtils.isEmpty(headAppItem.categoryName)
                    || !TextUtils.isEmpty(headAppItem.tag)) {
                game_brief.setVisibility(View.GONE);
                // 添加标签
                tag_container.setVisibility(View.VISIBLE);
                tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                int rightMargin = activity.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, rightMargin, 12, 0);
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
                            if (loadedImage != null && image != null && width > height) {
                                Bitmap bitmap = ImageUtil.adjustPhotoRotation(loadedImage, 90);
                                if (bitmap != null) {
                                    image.setImageBitmap(bitmap);
                                }
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

    @Override
    public void onClick(View v) {
        if (headAppItem == null || TextUtils.isEmpty(headAppItem.id))
            return;
        UIUtils.gotoAppDetail(headAppItem, headAppItem.id, activity);
        MTAUtil.onDetailClick(activity,
                headAppItem.name,
                headAppItem.packageName);
        PrizeStatUtil.onSearchResultItemClick(headAppItem.id, headAppItem.packageName, headAppItem.name, keyword, true);
    }

    private static class MyHander extends Handler {
        private WeakReference<SearchResultFragment> mActivities;

        MyHander(SearchResultFragment mActivity) {
            this.mActivities = new WeakReference<SearchResultFragment>(mActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivities == null || mActivities.get() == null) return;
            final SearchResultFragment activity = mActivities.get();
            if (activity != null) {
                //执行业务逻辑
                if (msg != null) {
                    if (activity.downloadBtn != null && msg != null && msg.what == 0) {
                        activity.downloadBtn.invalidate();
                        if (msg.obj != null) {
                            activity.left_AdvertisingView.refreshBtnState((String) msg.obj);
                            activity.right_AdvertisingView.refreshBtnState((String) msg.obj);
                        }
                    }
                }
            }
        }
    }

}
