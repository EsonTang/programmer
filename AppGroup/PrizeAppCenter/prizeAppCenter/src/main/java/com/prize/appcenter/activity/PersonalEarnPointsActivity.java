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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.constants.Constants;
import com.prize.app.database.dao.XutilsDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppPointsPageData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PointsConfigData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.PrizeStatUtil;
import com.prize.app.util.Verification;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.actionBar.ActionBarPointsActivity;
import com.prize.appcenter.ui.adapter.ryclvadapter.PersonalEarnPointsRycVAdapter;
import com.prize.appcenter.ui.recyclerview.EndlessRecyclerOnScrollListener;
import com.prize.appcenter.ui.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.prize.appcenter.ui.recyclerview.LoadingFooter;
import com.prize.appcenter.ui.recyclerview.RecyclerViewStateUtils;
import com.prize.appcenter.ui.recyclerview.RecyclerViewUtils;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.PointsLotteryUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.custmerxutils.XExtends;
import com.prize.statistics.model.ExposureBean;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Title:    个人中心-赚取积分
 * Desc:    2.0积分系统-赚取积分
 * Version:    应用市场2.0
 * Created by huangchangguo
 * on   2016/8/15  15:38
 * <p/>
 * Update Description:  更新描述
 * Updater:   更新者
 * Update Time:   更新时间
 */

public class PersonalEarnPointsActivity extends ActionBarPointsActivity {
    public static final String TAG = "PersonalEarnPointsActivity";
    private TextView mPointsMall, mPointsDesc;
    private ImageView mHeaderBanner;
    private RecyclerView mEarnPointsRyclv;
    private Context mContext;
    private AppPointsPageData mAppPointsPageData;
    private LinearLayout mHeader;
    //实时的索引
    private int currentIndex = 1;
    private ArrayList<AppsItemBean> itemDatas = new ArrayList<AppsItemBean>();
    private HeaderAndFooterRecyclerViewAdapter mHeaderAndFooterRecyclerViewAdapter = null;
    private PreviewHandler mHandler = new PreviewHandler(this);
    private PersonalEarnPointsRycVAdapter mEarnPointsRycVAdapter;
    private Callback.Cancelable mCancel;
    private Callback.Cancelable mConfigCancel;
    private RelativeLayout mDdefaultLlyt;
    private List<ExposureBean> mExposureBeans = new ArrayList<>();
    private List<ExposureBean> mNewExposureBeans = new ArrayList<>();
    private LinearLayoutManager mLayoutManager;
    private int mFirstVisibleItem;
    private int mLastVisiblePosition;
    private boolean isFirstCome = true;
    private boolean isNeedStatic = false;

    private static class PreviewHandler extends Handler {
        private WeakReference<PersonalEarnPointsActivity> ref;

        private PreviewHandler(PersonalEarnPointsActivity activity) {
            ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final PersonalEarnPointsActivity activity = ref.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            switch (msg.what) {
                case 0:
                    if (activity.mAppPointsPageData == null) {
                        return;
                    }
                    activity.currentIndex++;
                    if (activity.currentIndex <= activity.mAppPointsPageData.pageCount) {
                        //保证请求的是下一页的数据
                        RecyclerViewStateUtils.setFooterViewState(activity.mEarnPointsRyclv, LoadingFooter.State.Normal);
                    } else {
                        RecyclerViewStateUtils.setFooterViewState(activity, activity.mEarnPointsRyclv
                                , Constants.PAGE_SIZE, LoadingFooter.State.TheEnd, null);
                    }

                    activity.mEarnPointsRycVAdapter.addData(activity.itemDatas);
                    activity.mHeaderAndFooterRecyclerViewAdapter.notifyDataSetChanged();
                    if (activity.currentIndex > activity.mAppPointsPageData.pageCount && activity.mEarnPointsRycVAdapter.getItemCount() <= 0) {
                        activity.mDdefaultLlyt.setVisibility(View.VISIBLE);
                        activity.mEarnPointsRyclv.setVisibility(View.GONE);
                    } else {
                        activity.mDdefaultLlyt.setVisibility(View.GONE);
                        activity.mEarnPointsRyclv.setVisibility(View.VISIBLE);
                    }
                    break;
                //加载更多
                case 1:
                    RecyclerViewStateUtils.setFooterViewState(activity, activity.mEarnPointsRyclv, Constants.PAGE_SIZE, LoadingFooter.State.NetWorkError, activity.mFooterClick);

                    break;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedAddWaitingView(true);
        setContentView(R.layout.activity_personal_earn_points);
        WindowMangerUtils.changeStatus(getWindow());
        mContext = getApplicationContext();

        setTitle(getString(R.string.has_gift));
        initView();
        initListener();
        initData();
    }


    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mHeader = (LinearLayout) inflater.inflate(R.layout.activity_personal_earn_points_header, null);
        mDdefaultLlyt = (RelativeLayout) findViewById(R.id.defalutRlyt_id);
        mPointsMall = (TextView) mHeader.findViewById(R.id.points_mall_tv);
        mPointsDesc = (TextView) mHeader.findViewById(R.id.points_header_desc_tv);
        mHeaderBanner = (ImageView) mHeader.findViewById(R.id.earn_points_header_banner_iv);
        mEarnPointsRyclv = (RecyclerView) findViewById(R.id.earn_points_recyclerview);

        // 提高性能
        mEarnPointsRyclv.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);

        mEarnPointsRyclv.setLayoutManager(mLayoutManager);
        if (getIntent() != null) {
            from = getIntent().getStringExtra(Constants.FROM);
        }
        isNeedStatic = JLog.isDebug || !TextUtils.isEmpty(CommonUtils.getNewTid());
    }

    private void initData() {

        XutilsDAO.deletEarnPointsBeanData(System.currentTimeMillis() - 60 * 24 * 60 * 60 * 1000);
        mHeaderAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(mEarnPointsRycVAdapter);
        mEarnPointsRyclv.setAdapter(mHeaderAndFooterRecyclerViewAdapter);
        //添加头布局
        RecyclerViewUtils.setHeaderView(mEarnPointsRyclv, mHeader);
        requestPointsRule();
        requestData();
    }


    private void initListener() {
        //跳转到积分商城
        mPointsMall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PersonalPointsMallActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        mEarnPointsRyclv.addOnScrollListener(mOnScrollListener);
        mEarnPointsRycVAdapter = new PersonalEarnPointsRycVAdapter(this);
        mEarnPointsRycVAdapter.setDownlaodRefreshHandle();
        mToken = AIDLUtils.bindToService(this, this);
    }


    private View.OnClickListener mFooterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            RecyclerViewStateUtils.setFooterViewState(PersonalEarnPointsActivity.this, mEarnPointsRyclv, Constants.PAGE_SIZE, LoadingFooter.State.Loading, null);
            //请求加载数据
            requestData();
            requestPointsRule();
        }
    };
    private EndlessRecyclerOnScrollListener mOnScrollListener = new EndlessRecyclerOnScrollListener() {

        @Override
        public void onLoadNextPage(View view) {
            super.onLoadNextPage(view);

            LoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(mEarnPointsRyclv);
            if (state == LoadingFooter.State.Loading || mAppPointsPageData == null) {
                return;
            }
            if (currentIndex <= mAppPointsPageData.pageCount) {
                // 加载更多
                RecyclerViewStateUtils.setFooterViewState(PersonalEarnPointsActivity.this, mEarnPointsRyclv, 100, LoadingFooter.State.Loading, null);

                requestData();
            } else {
                //没有啦
                JLog.i(TAG, "itemDatas.size()=" + itemDatas.size() + "--currentIndex=" + currentIndex);
                RecyclerViewStateUtils.setFooterViewState(PersonalEarnPointsActivity.this, mEarnPointsRyclv, 100, LoadingFooter.State.TheEnd, null);
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            switch (newState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    if (itemDatas != null && itemDatas.size() > 0) {
                        AppsItemBean bean;
                        ExposureBean exposureBean;
                        ExposureBean newbean;
                        List<ExposureBean> tempOldList = new ArrayList<>();
                        List<ExposureBean> tempNewList = new ArrayList<>();
                        for (int i = mFirstVisibleItem; i < mLastVisiblePosition && i < itemDatas.size(); i++) {
                            bean = itemDatas.get(i);
                            if (bean == null) continue;
                            if (isNeedStatic) {
                                newbean = CommonUtils.formNewPagerExposure(bean, Constants.EARNPOINTS_GUI, Constants.LIST);
                                if (newbean != null && !mNewExposureBeans.contains(newbean)) {
                                    mNewExposureBeans.add(newbean);
                                    tempNewList.add(newbean);
                                }
                            }
                            if (TextUtils.isEmpty(bean.backParams)) continue;//旧版的曝光统计只曝光360的数据
                            exposureBean = CommonUtils.formatSearchHeadExposure(Constants.EARNPOINTS_GUI, Constants.LIST, bean.id, bean.name, bean.backParams);
                            if (exposureBean != null && !mExposureBeans.contains(exposureBean)) {
                                mExposureBeans.add(exposureBean);
                                tempOldList.add(exposureBean);
                            }
                        }
                        if (JLog.isDebug) {
                            JLog.i(TAG, "onScrollStateChanged--需要上报的360曝光tempOldList=" + tempOldList);
                            JLog.i(TAG, "onScrollStateChanged--已经上传的360曝光mExposureBeans=" + mExposureBeans);
                            JLog.i(TAG, "onScrollStateChanged--新版需要上报曝光tempNewList=" + tempNewList);
                            JLog.i(TAG, "onScrollStateChanged--新版已经上报曝光mNewExposureBeans=" + mNewExposureBeans);
                            JLog.i(TAG, "onScrollStateChanged--新版已经上报曝光mNewExposureBeans=" + mNewExposureBeans.size());
                        }
                        if (isNeedStatic) {
                            PrizeStatUtil.startNewUploadExposure(tempNewList);
                        }
                        tempNewList.clear();
                        AIDLUtils.uploadDataNow(tempOldList);
//                        PrizeStatUtil.startUploadExposure(tempOldList);
                        tempOldList.clear();
                    }
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                    break;
                case AbsListView.OnScrollListener.SCROLL_STATE_FLING://是当用户由于之前划动屏幕并抬起手指，屏幕产生惯性滑动时
                    break;
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mLastVisiblePosition = mLayoutManager.findLastVisibleItemPosition();
            mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
            if (isFirstCome) {
                isFirstCome = false;
                if (itemDatas != null && itemDatas.size() > 0) {
                    AppsItemBean bean;
                    ExposureBean exposureBean;
                    ExposureBean newbean;
                    List<ExposureBean> tempOldList = new ArrayList<>();
                    List<ExposureBean> tempNewList = new ArrayList<>();
                    for (int i = mFirstVisibleItem; i < mLastVisiblePosition && i < itemDatas.size(); i++) {
                        bean = itemDatas.get(i);
                        if (bean == null) continue;
                        if (isNeedStatic) {
                            newbean = CommonUtils.formNewPagerExposure(bean, Constants.EARNPOINTS_GUI, Constants.LIST);
                            if (newbean != null && !mNewExposureBeans.contains(newbean)) {
                                mNewExposureBeans.add(newbean);
                                tempNewList.add(newbean);
                            }
                        }
                        if (TextUtils.isEmpty(bean.backParams)) continue;//旧版的曝光统计只曝光360的数据
                        exposureBean = CommonUtils.formatSearchHeadExposure(Constants.EARNPOINTS_GUI, Constants.LIST, bean.id, bean.name, bean.backParams);
                        if (exposureBean != null && !mExposureBeans.contains(exposureBean)) {
                            mExposureBeans.add(exposureBean);
                            tempOldList.add(exposureBean);
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i(TAG, "onScrolled--需要上报的360曝光tempOldList=" + tempOldList);
                        JLog.i(TAG, "onScrolled--已经上传的360曝光mExposureBeans=" + mExposureBeans);
                        JLog.i(TAG, "onScrolled--新版需要上报曝光tempNewList=" + tempNewList);
                        JLog.i(TAG, "onScrolled--新版已经上报曝光mNewExposureBeans=" + mNewExposureBeans);
                    }
                    AIDLUtils.uploadDataNow(tempOldList);
//                    PrizeStatUtil.startUploadExposure(tempOldList);
                    tempOldList.clear();
                    if (isNeedStatic) {
                        PrizeStatUtil.startNewUploadExposure(tempNewList);
                    }
                    tempNewList.clear();
                }
            }
        }
    };

    private void requestData() {
        RequestParams mParams = new RequestParams(Constants.GIS_URL + "/point/applist/v300");
        mParams.addBodyParameter("pageIndex", String.valueOf(currentIndex));
        mParams.addBodyParameter("pageSize", 100 + "");
        String sign = Verification.getInstance().getSign(mParams.getBodyParams());
        mParams.addBodyParameter("sign", sign);
        mCancel = XExtends.http().post(mParams, new PrizeXutilStringCallBack<String>() {


            @Override
            public void onSuccess(String result) {
                hideWaiting();
                try {
                    String response = new JSONObject(result).getString("data");
                    mAppPointsPageData = GsonParseUtils.parseSingleBean(response, AppPointsPageData.class);
                    if (mAppPointsPageData != null && mAppPointsPageData.apps != null) {
                        itemDatas = filterInstalled(mAppPointsPageData.apps);
                        mHandler.sendEmptyMessage(0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    RecyclerViewStateUtils.setFooterViewState(mEarnPointsRyclv, LoadingFooter.State.NetWorkError);

                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                hideWaiting();
                mHandler.sendEmptyMessageDelayed(1, 1000);

            }
        });

    }


    @Override
    public String getActivityName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mEarnPointsRycVAdapter != null) {
            mEarnPointsRycVAdapter.setIsActivity(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mEarnPointsRycVAdapter != null) {
            mEarnPointsRycVAdapter.setIsActivity(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExposureBeans != null && mExposureBeans.size() > 0) {
            mExposureBeans.clear();
        }
        if (mNewExposureBeans != null && mNewExposureBeans.size() > 0) {
//            PrizeStatUtil.startNewUploadExposure(mNewExposureBeans);
            mNewExposureBeans.clear();
        }
        if (mEarnPointsRycVAdapter != null) {
            mEarnPointsRycVAdapter.removeDownLoadHandler();

        }
        if (mHeaderAndFooterRecyclerViewAdapter != null) {
            mHeaderAndFooterRecyclerViewAdapter.unregisterAdapterDataObserver();
        }
        if (mCancel != null) {
            mCancel.cancel();
        }
        if (mConfigCancel != null) {
            mConfigCancel.cancel();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        PointsLotteryUtils.clearHashSet();
        AIDLUtils.unbindFromService(mToken);
    }

    /**
     * Desc: 联网获得积分规则的地址
     * 2.0积分系统
     * Created by huangchangguo
     * Date:  2016/8/16 13:59
     */
    private void requestPointsRule() {
        String url = Constants.GIS_URL + "/point/config";
        RequestParams reqParams = new RequestParams(url);
        reqParams.addBodyParameter("type", "point");
        String sign = Verification.getInstance().getSign(reqParams.getBodyParams());
        reqParams.addBodyParameter("sign", sign);

        mConfigCancel = XExtends.http().post(reqParams,
                new PrizeXutilStringCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (!TextUtils.isEmpty(result)) {
                            try {
                                JSONObject obj = new JSONObject(result);
                                String resp = obj.optString("data");
                                //值始终是不会变的，所以可以存常量里面
                                PointsConfigData pointsConfig = GsonParseUtils.parseSingleBean(resp,
                                        PointsConfigData.class);
                                //给banner图赋值
                                if (mHeaderBanner != null && pointsConfig != null && !TextUtils.isEmpty(pointsConfig.bannerUrl)) {
                                    ImageLoader.getInstance().displayImage(pointsConfig.bannerUrl,
                                            mHeaderBanner, UILimageUtil.getUILoptions(R.drawable.points_mall_item_details_header_banner), null);
                                }
                                //给notice赋值
                                if (mPointsDesc != null && pointsConfig != null && !TextUtils.isEmpty(pointsConfig.notice)) {
                                    mPointsDesc.setText(pointsConfig.notice);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                    }
                }
        );
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mEarnPointsRycVAdapter.setDownlaodRefreshHandle();
    }

//    @Override
//    public void onBackPressed() {
//        if (!TextUtils.isEmpty(from)) {
//            if ("push".equals(from) || "startPage".equals(from)) {
//                try {
//                    UIUtils.gotoMainActivity(PersonalEarnPointsActivity.this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        super.onBackPressed();
//    }

    @Override
    public void finish() {
        UIUtils.gotoMainActivity(this,from);
        super.finish();
    }

    /**
     * 过滤已安装和已经领取积分的应用（只能用于赚取积分）
     *
     * @param listData List<AppsItemBean>
     * @return ArrayList<AppsItemBean>
     */
    private static ArrayList<AppsItemBean> filterInstalled(
            List<AppsItemBean> listData) {
        ArrayList<AppsItemBean> listFilter = new ArrayList<AppsItemBean>();
        List<AppsItemBean> list360 = XutilsDAO.findPoints360Bean();//找寻360的数据
        if (JLog.isDebug) {
            JLog.i(TAG, "filterInstalled-list360=" + list360);
        }
        if (list360 != null && list360.size() > 0) {//先添加点击后，还未领取的应用
            int size = list360.size();
            AppsItemBean item;
            for (int i = 0; i < size; i++) {//过滤掉安装了 又卸载的app
                item = list360.get(i);
                if (item == null) continue;
                int state = AIDLUtils.getGameAppState(item.packageName, item.id, item.versionCode);
                JLog.i(TAG, "filterInstalled-state=" + state);
                if (state == AppManagerCenter.APP_STATE_UNEXIST || (AppManagerCenter.isAppExist(item.packageName) && AppManagerCenter.appIsNeedUpate(item.packageName,
                        item.versionCode))) {
                    XutilsDAO.deletegettedPoints360Bean(item.packageName);
                } else {
                    listFilter.add(item);
                }
            }
        }
        int size = listData.size();
        for (int i = 0; i < size; i++) {
            AppsItemBean item = listData.get(i);
            if (XutilsDAO.isExistsEarnPointsBeanData(item.packageName))
                continue;
            if (!TextUtils.isEmpty(item.packageName) && AppManagerCenter.isAppExist(item.packageName) && !isEarnPointsPage(item.packageName)) {
                continue;
            }
            if (!listFilter.contains(item)) {//后续列表过滤未领取的
                listFilter.add(item);
            }
        }
        return listFilter;
    }

    /**
     * 来自赚取积分界面
     *
     * @param packageName 应用包名
     * @return boolean
     */
    private static boolean isEarnPointsPage(String packageName) {
        String pageInfo = AIDLUtils.getDownloadedAppPageInfo(packageName);
        return (!TextUtils.isEmpty(pageInfo)) && pageInfo.contains(Constants.EARNPOINTS_GUI);
    }

}