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

package com.prize.appcenter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.app.net.datasource.base.AppDetailRecommandData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.DetailApp;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppCommentActivity;
import com.prize.appcenter.activity.AppFeedbackActivity;
import com.prize.appcenter.activity.TagListActivity;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.adapter.AppDetailGridViewAdapter;
import com.prize.appcenter.ui.adapter.AppDetailImgRcycleAdapter;
import com.prize.appcenter.ui.datamgr.AppDetailDataManager;
import com.prize.appcenter.ui.datamgr.DataManagerCallBack;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.AppDetailView;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.StretchyTextView;
import com.prize.custmerxutils.XExtends;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback.Cancelable;
import org.xutils.http.RequestParams;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 类描述：详情子fragment
 *
 * @author 作者 huanglingjun
 * @version 版本
 */
public class AppDetailFgm extends Fragment implements OnClickListener,
        DataManagerCallBack {
    private final String TAG = "AppDetailFgm";
    private AppDetailData mData;
    /**
     * 应用介绍
     *****/
    private StretchyTextView mExpendTvOne;
//    private ExpendTextView mExpendTvOne;
    private GridView allLikeGridView, releaGridView;
    /**
     * 相关推荐
     *****/
    private AppDetailGridViewAdapter releaApps;
    /**
     * 大家都喜欢
     *****/
    private AppDetailGridViewAdapter allLikeAdapter;
    private WeakReference<Activity> mActivities = null;
    private AppsItemBean itemBean;
    private RelativeLayout all_Like_Rylt;
    private TextView relatede_Tv;
    /**
     * app其他信息，权限，开发者等等容器
     ****/
    private AppDetailView mAppDetailView;
    //    private int windowHeight;
    private TextView mUpdateTime;
    private FlowLayout ourtag_container;
    private TextView mVersion;
    private String UNKNOW;
    private View line_tag_down, line, line_app_tag;
    private View imgs_line, divide_line;
    private RelativeLayout our_tag_Rlyt;
    private Cancelable mCancelable;
    private AppDetailImgRcycleAdapter myAdapter;
    private View seprate_class;
    private RecyclerView mRecyclerView;
    private TextView detail_info_id;

    public AppDetailFgm() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mActivities == null && getActivity() != null) {
            mActivities = new WeakReference<Activity>(getActivity());
            UNKNOW = mActivities.get().getResources().getString(R.string.unknow);
        }
        if (JLog.isDebug) {
            JLog.i("AppDetailParentFgm", "AppDetailFgm-onCreateView-savedInstanceState==null?"
                    + (savedInstanceState == null));
        }
//        if (null != view) {
//            ViewGroup parent = (ViewGroup) view.getParent();
//            if (null != parent) {
//                parent.removeView(view);
//            }
//        } else {
        View view = inflater.inflate(R.layout.fragment_appdetail, container,
                false);
        if (getArguments() != null
                && getArguments().getSerializable("AppDetailData") != null) {
            this.mData = (AppDetailData) getArguments().getSerializable("AppDetailData");
        }
        if (this.mData == null) {
            getActivity().finish();
            return view;
        }
        initView(view);
//        }

        return view;
    }

    /**
     * 是否是新风格的界面
     */
    private boolean isNewStyle = false;
    private int bgColor;

    /***
     * 初始化控件
     *
     * @param view  View
     */
    private void initView(View view) {
        imgs_line = view.findViewById(R.id.imgs_line);
        line_app_tag = view.findViewById(R.id.line_app_tag);
        seprate_class = view.findViewById(R.id.seprate_class);
        divide_line = view.findViewById(R.id.divide_detailline);
        our_tag_Rlyt = (RelativeLayout) view.findViewById(R.id.our_tag_Rlyt);
        line_tag_down = view.findViewById(R.id.line_tag_down);
        line = view.findViewById(R.id.line);
        relatede_Tv = (TextView) view.findViewById(R.id.relatede_Tv);
        all_Like_Rylt = (RelativeLayout) view.findViewById(R.id.all_Like_Rylt);
        mExpendTvOne = (StretchyTextView) view
                .findViewById(R.id.expendTextView_one);
        ourtag_container = (FlowLayout) view
                .findViewById(R.id.ourtag_container);

        mUpdateTime = (TextView) view.findViewById(R.id.updateTimeTv_id);
        mVersion = (TextView) view.findViewById(R.id.versionTv_id);

        TextView report_Tv = (TextView) view.findViewById(R.id.report_Tv);
        report_Tv.setOnClickListener(this);

        releaGridView = (GridView) view.findViewById(R.id.gridView_hot_id);
        allLikeGridView = (GridView) view.findViewById(R.id.allLikeGridView);
        mAppDetailView = (AppDetailView) view.findViewById(R.id.appDetailView);
//        mLinerLayout = (LinearLayout) view.findViewById(R.id.child_id);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        // 提高性能
        int windowHeight = ClientInfo.getInstance().screenHeight;
        int h;
        if (windowHeight >= 2000) {
            h = 478 * 2;
        } else {
            h = mActivities.get().getResources().getInteger(R.integer.image_height);
        }
        LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, h);
        params.topMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 15.0f);
        mRecyclerView.setLayoutParams(params);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mActivities.get());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
//        mRecyclerView.addItemDecoration(new SpaceItemDecoration((int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 27.0f)));
        myAdapter = new AppDetailImgRcycleAdapter(mActivities.get());
        int resultColor;
        if (mData != null && mData.app != null && mData.app.style != null
                && !TextUtils.isEmpty(mData.app.style.backgroundColor)
                && !TextUtils.isEmpty(mData.app.style.backgroundUrl)) {
            try {
                if (!mData.app.style.backgroundColor.contains("#")) {
                    mData.app.style.backgroundColor = "#" + mData.app.style.backgroundColor;
                }
                bgColor = Color.parseColor(mData.app.style.backgroundColor);
                isNewStyle = true;
            } catch (Exception e) {
                bgColor = Color.parseColor("#009def");//防止后台配置出错时，默认浅绿色
            }
            resultColor = CommonUtils.getModifyHueColor(bgColor);
            if (null == allLikeAdapter) {
                allLikeAdapter = new AppDetailGridViewAdapter(mActivities.get());
                allLikeAdapter.setBgColor(resultColor);
            }
            if (null == releaApps) {
                releaApps = new AppDetailGridViewAdapter(mActivities.get());
                releaApps.setBgColor(resultColor);
            }
            if (JLog.isDebug) {
                JLog.i(TAG, "设置举报按钮反色");
            }

            LayoutParams likeParam = (LayoutParams) all_Like_Rylt.getLayoutParams();
            likeParam.topMargin = 0;
//            likeParam.topMargin= (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP,5f);
//            all_Like_Rylt.setLayoutParams(likeParam);

            //http://blog.51cto.com/6169621/1618580
            report_Tv.getBackground().mutate().setColorFilter(resultColor, PorterDuff.Mode.SRC_ATOP);//设置举报按钮的颜色
            int mColor = Color.parseColor("#66FFFFFF");
            //设置其他信息的字体和按钮的颜色
            mAppDetailView.setDescripColor(Color.WHITE);
            mAppDetailView.setTitleColor(mColor);
            mAppDetailView.setMoreColorDrawables(resultColor);

            mUpdateTime.setTextColor(mColor);
            mVersion.setTextColor(mColor);
            TextView version_id = (TextView) view.findViewById(R.id.version_id);
            version_id.setTextColor(mColor);
            TextView updateTime_id = (TextView) view.findViewById(R.id.updateTime_id);
            updateTime_id.setTextColor(mColor);

            int fiveMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 5f);
            LayoutParams relatede_P = (LayoutParams) relatede_Tv.getLayoutParams();
            relatede_P.topMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 13f);

            LayoutParams mExpendTvOne_P = (LayoutParams) mExpendTvOne.getLayoutParams();
            mExpendTvOne_P.topMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 13f);

            LayoutParams our_tag_Rlyt_P = (LayoutParams) our_tag_Rlyt.getLayoutParams();
            our_tag_Rlyt_P.topMargin = fiveMargin;

            relatede_Tv.setTextColor(Color.WHITE);
            //反转TextView左边的icon图标为转换好的颜色
            Drawable[] drawables = relatede_Tv.getCompoundDrawables();
            Drawable d1 = drawables[0].mutate();
            d1.setColorFilter(resultColor, PorterDuff.Mode.SRC_ATOP);
            relatede_Tv.setCompoundDrawables(d1, null, null, null);


            TextView app_developerTv_id = (TextView) view.findViewById(R.id.app_developerTv_id);
            detail_info_id = (TextView) view.findViewById(R.id.detail_info_id);
            TextView app_tag_id = (TextView) view.findViewById(R.id.app_tag_id);

            RelativeLayout.LayoutParams allLikeparam = (RelativeLayout.LayoutParams) app_developerTv_id.getLayoutParams();
            allLikeparam.topMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 19f);
            app_developerTv_id.setTextColor(Color.WHITE);

            RelativeLayout.LayoutParams detail_info_p = (RelativeLayout.LayoutParams) detail_info_id.getLayoutParams();
            detail_info_p.topMargin = fiveMargin;

            app_developerTv_id.setTextColor(Color.WHITE);

            app_developerTv_id.setCompoundDrawables(d1, null, null, null);
            detail_info_id.setCompoundDrawables(d1, null, null, null);
            app_tag_id.setCompoundDrawables(d1, null, null, null);

            detail_info_id.setTextColor(Color.WHITE);
            app_tag_id.setTextColor(Color.WHITE);

            mExpendTvOne.setTitleColorDrawables(Color.WHITE, d1);
            mExpendTvOne.setDescripColor(mColor);
            mExpendTvOne.setColorFilter(resultColor);
        }

        mRecyclerView.setAdapter(myAdapter);
        if (null == allLikeAdapter) {
            allLikeAdapter = new AppDetailGridViewAdapter(mActivities.get());
        }
        if (null == releaApps) {
            releaApps = new AppDetailGridViewAdapter(mActivities.get());
        }

        initData();
        initGift(view);
        initHorizontalScrollView();
    }

    /**
     * 初始化礼包信息
     */
    private void initGift(View view) {
        final Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        if (mData != null) {
            TextView giftTitle = (TextView) view.findViewById(R.id.giftTitle);
            View v = (View) giftTitle.getParent();
            if (mData.gifts == null || mData.gifts.size() <= 0) {
                v.setVisibility(View.GONE);
                imgs_line.setVisibility(View.VISIBLE);
            } else {
                imgs_line.setVisibility(View.GONE);
                if (isNewStyle) {
                    v.setBackgroundColor(Color.TRANSPARENT);
                    LinearLayout.LayoutParams v_p = (LayoutParams) v.getLayoutParams();
                    v_p.height = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 58);
                }
            }
            if (mData.gifts != null && mData.gifts.size() > 0) {
                giftTitle.setText(mCtx.getString(R.string.appdetail_gift_num, mData.gifts.get(0).title, mData.gifts.size()));
                if (isNewStyle) {
                    Drawable drawable = mCtx.getDrawable(R.drawable.detail_gift_newicon);
                    if (drawable != null) {
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        giftTitle.setCompoundDrawables(drawable, null, null, null);
                    }
                    TextView join_Iv = (TextView) view.findViewById(R.id.join_Iv);
                    join_Iv.setBackground(mCtx.getResources().getDrawable(R.drawable.icon_newjoin_sl));
                    join_Iv.setTextColor(Color.WHITE);
                    View giftLine = v.findViewById(R.id.divide_line);
                    giftLine.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
                    v.findViewById(R.id.divide_gift_bottom).setBackgroundColor(Color.TRANSPARENT);

                    LinearLayout.LayoutParams mRecyclerView_p = (LayoutParams) mRecyclerView.getLayoutParams();
                    mRecyclerView_p.topMargin = 0;//减少大图与礼包的间距

//                    RelativeLayout.LayoutParams giftLine_p = (RelativeLayout.LayoutParams) giftLine.getLayoutParams();
//                    giftLine_p.bottomMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 5f);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) join_Iv.getLayoutParams();
                    params.width = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 55);
                    params.height = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 24);
                    giftTitle.setTextColor(Color.WHITE);
                }
                v.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        UIUtils.gotoGameGiftDetai(mCtx, mData.gifts.get(0).appId, 0);

                    }
                });

                view.findViewById(R.id.join_Iv).setOnClickListener(
                        new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                UIUtils.gotoGameGiftDetai(mCtx,
                                        mData.gifts.get(0).appId, 0);

                            }
                        });
                v.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        UIUtils.gotoGameGiftDetai(mCtx, mData.gifts.get(0).appId, 0);

                    }
                });
            }
        }
        JLog.i(TAG, "isNewStyle=" + isNewStyle);
        if (isNewStyle) {
            imgs_line.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
            divide_line.setBackgroundColor(Color.TRANSPARENT);
            divide_line.setVisibility(View.INVISIBLE);
            line_tag_down.setBackgroundColor(Color.TRANSPARENT);
            line.setBackgroundColor(Color.TRANSPARENT);
            seprate_class.setBackgroundColor(Color.TRANSPARENT);
            line_app_tag.setBackgroundColor(Color.TRANSPARENT);


        }
    }


    /**
     * 方法描述：初始化图片展示区
     */
    private void initHorizontalScrollView() {
        final Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        if (mData == null || mData.app == null
                || mData.app.screenshotsUrl == null)
            return;
        final String[] paths = mData.app.screenshotsUrl.split("\\,");
        myAdapter.setPaths(paths, isNewStyle);
    }

    /**
     * 填充数据，填充更新时间，版本，标签，其他信息等等；同时请求相关推荐和大家也喜欢列表数据
     */
    private void initData() {
        if (JLog.isDebug) {
            JLog.i("AppDetailParentFgm", "AppDetailFgm-initData--start");
        }
        Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        if (mData == null || mData.app == null)
            return;
        requestrecommandData();
        if (!TextUtils.isEmpty(mData.app.updateTime)) {
            mUpdateTime.setText(mData.app.updateTime);
        } else {
            mUpdateTime.setText(UNKNOW);
        }
        if (!TextUtils.isEmpty(mData.app.versionName)) {
            mVersion.setText(mData.app.versionName);
        } else {
            mVersion.setText(UNKNOW);
        }

        DetailApp appData = mData.app;
        initTag(appData);
        itemBean = UIUtils.changeToAppItemBean(mData.app);
        mExpendTvOne.setContentDesc(appData.description);
//        mExpendTvOne.setContentDesc(mCtx.getString(R.string.app_introduce),
//                appData.description);
        mAppDetailView.setData(mData.app, true);
        if (JLog.isDebug) {
            JLog.i("AppDetailParentFgm", "AppDetailFgm-initData--end");
        }
    }

    /**
     * 返回相关推荐和大家也喜欢列表数据
     */
    private void requestrecommandData() {
        int subCatId = mData.app.subCatId;
        String appId = mData.app.id;
        String appName = mData.app.name;
        if (subCatId <= 0) {
            processNoCommentData();
            return;
        }
        RequestParams entity = new RequestParams(Constants.GIS_URL + "/category/like");
        entity.addBodyParameter("subCatId", String.valueOf(subCatId));
        entity.addBodyParameter("appId", appId);
        entity.addBodyParameter("appName", appName);
//        entity.setConnectTimeout(30000);
        mCancelable = XExtends.http().post(entity, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {// AppDetailRecommandData
                try {
                    JSONObject o = new JSONObject(result);
                    if (o.getInt("code") == 0) {
                        AppDetailRecommandData data = new Gson()
                                .fromJson(o.getString("data"), AppDetailRecommandData.class);
                        processRecommandData(data);
                    } else {
                        processNoCommentData();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                processNoCommentData();
            }

        });

    }

    /**
     * 处理相关推荐&大家也喜欢 应用列表信息
     *
     * @param data AppDetailRecommandData
     */
    protected void processRecommandData(AppDetailRecommandData data) {
        final Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        if (releaApps == null) {
            releaApps = new AppDetailGridViewAdapter(getActivity());
        }
        List<AppsItemBean> list = CommonUtils.filterDetailData(
                this.mData.app.packageName, data.relatedApps);
        releaGridView.setAdapter(releaApps);
        releaApps.setDownlaodRefreshHandle();
        releaGridView.setFocusable(false);
        releaApps.setData(list);

        releaGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (CommonUtils.isFastDoubleClick())
                    return;
                UIUtils.gotoAppDetail(releaApps.getItem(position),
                        releaApps.getItem(position).id, mCtx);
                MTAUtil.onClickRela_AllLike(true);
                MTAUtil.onDetailClick(mCtx, releaApps.getItem(position).name,
                        releaApps.getItem(position).packageName);
            }
        });

        if (allLikeAdapter == null) {
            allLikeAdapter = new AppDetailGridViewAdapter(getActivity());
        }
        List<AppsItemBean> likeApps = CommonUtils.filterDetailData(
                this.mData.app.packageName, data.likeApps);
        allLikeGridView.setAdapter(allLikeAdapter);
        allLikeAdapter.setData(likeApps);
        allLikeAdapter.setDownlaodRefreshHandle();
        allLikeGridView.setFocusable(false);
        allLikeGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (CommonUtils.isFastDoubleClick())
                    return;
                UIUtils.gotoAppDetail(allLikeAdapter.getItem(position),
                        allLikeAdapter.getItem(position).id,
                        mCtx);
                onBtnClick("detail", allLikeAdapter.getItem(position).name);
                MTAUtil.onClickRela_AllLike(false);
            }
        });
    }

    public void onBtnClick(String btn_id, String appName) {
        Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        // 统计按钮被点击次数，统计对象：OK按钮
        Properties prop = new Properties();
        prop.setProperty("name", appName);
        StatService.trackCustomKVEvent(mCtx, btn_id, prop);
    }

    @Override
    public void onClick(View v) {
        Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        int id = v.getId();
        Intent intent;
        switch (id) {
            case R.id.more_comment_id:
                intent = new Intent(mCtx, AppCommentActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("detailData", mData.app);
                bundle.putBoolean("isComment", false);
                intent.putExtra("bundle", bundle);
                mCtx.startActivity(intent);
                mCtx.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                break;
            case R.id.report_Tv:
                intent = new Intent(mCtx, AppFeedbackActivity.class);
                intent.putExtra(AppFeedbackActivity.APP_NAME, mData.app.name);
                intent.putExtra(AppFeedbackActivity.APP_VERSIONNAME,
                        mData.app.versionName);
                intent.putExtra(AppFeedbackActivity.APP_PKG, mData.app.packageName);
                intent.putExtra(AppFeedbackActivity.APP_ID, mData.app.id);
                mCtx.startActivity(intent);
                mCtx.overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                break;
            case R.id.commentEdit_id:
                int netType = ClientInfo.getAPNType(mCtx);
                if (netType == ClientInfo.NONET) {
                    ToastUtils.showToast(mCtx.getResources().getString(
                            R.string.net_error));
                    return;
                }
                int state = AppManagerCenter.getGameAppState(itemBean.packageName,
                        itemBean.id, itemBean.versionCode);
                if (state == AppManagerCenter.APP_STATE_INSTALLED
                        || state == AppManagerCenter.APP_STATE_UPDATE) {
                    intent = new Intent(mCtx, AppCommentActivity.class);
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("detailData", mData.app);
                    bundle2.putBoolean("isComment", true);
                    intent.putExtra("bundle", bundle2);
                    mCtx.startActivity(intent);
                    mCtx.overridePendingTransition(R.anim.fade_in,
                            R.anim.fade_out);
                } else {
                    ToastUtils.showToast(mCtx.getResources().getString(
                            R.string.please_download_app));
                }
                break;
            default:
                break;
        }

    }

    @Override
    public void onDestroy() {
        if (null != releaApps) {
            releaApps.removeDownLoadHandler();
        }
        if (null != allLikeAdapter) {
            allLikeAdapter.removeDownLoadHandler();
        }
        if (mCancelable != null) {
            mCancelable.cancel();
        }
        if (ourtag_container != null) {
            ourtag_container.removeAllViews();
            ourtag_container = null;
        }
        if (mData != null) {
            mData = null;
        }
        BaseApplication.cancelPendingRequests(TAG);
        super.onDestroy();
    }

    @Override
    public void onBack(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case AppDetailDataManager.GIFT_SUCCESS:
                if (obj == null)
                    return;
                break;
            case AppDetailDataManager.GIFT_FAILURE:
                break;
            case AppDetailDataManager.COMMENT_SUCCESS:
                if (obj == null)
                    return;
                break;
            default:
                break;
        }
    }

    /**
     * 初始化标签数据
     *
     * @param detailData DetailApp
     */
    private void initTag(DetailApp detailData) {
        final Activity mCtx = mActivities.get();
        if (mCtx == null)
            return;
        if (TextUtils.isEmpty(detailData.tag)
                && TextUtils.isEmpty(detailData.categoryName)) {
            our_tag_Rlyt.setVisibility(View.GONE);
            return;
        }
        OnClickListener mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mCtx, TagListActivity.class);
                intent.putExtra("tag", ((TextView) v).getText().toString()
                        .trim());
                mCtx.startActivity(intent);
            }
        };
        our_tag_Rlyt.setVisibility(View.VISIBLE);
        ourtag_container.removeAllViews();
        LayoutParams params1 = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 22, 0);
        List<String> listTags = new ArrayList<String>();
        if (!TextUtils.isEmpty(detailData.categoryName)) {
            listTags.add(detailData.categoryName);
        }
        if (!TextUtils.isEmpty(detailData.tag)) {
            final String[] tags = detailData.tag.split(" ");
            for (String tag : tags) {
                if (TextUtils.isEmpty(tag) || TextUtils.isEmpty(tag.trim())) continue;
                if (TextUtils.isEmpty(detailData.categoryName)
                        || !detailData.categoryName.equals(tag)) {
                    listTags.add(tag);
                }

            }
        }
        int length = listTags.size() > 5 ? 5 : listTags.size();
        for (int i = 0; i < length; i++) {
            TextView tagView = (TextView) LayoutInflater.from(mCtx).inflate(
                    R.layout.textview, null);
            if (i == 1) {
                tagView.setBackgroundResource(R.drawable.detail_tag_two_bg);
            }
            if (i == 2) {
                tagView.setBackgroundResource(R.drawable.detail_tag_three_bg);
            }
            if (i == 3) {
                tagView.setBackgroundResource(R.drawable.detail_tag_four_bg);
            }
            if (i == 4) {
                tagView.setBackgroundResource(R.drawable.detail_tag_five_bg);
            }
            if (i == 5) {
                tagView.setBackgroundResource(R.drawable.detail_tag_six_bg);

            }
            if (isNewStyle) {
                tagView.setTextColor(bgColor);
            }
            tagView.setText(listTags.get(i));
            tagView.setTag(i);
            tagView.setOnClickListener(mOnClickListener);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
        }
    }

    /**
     * 处理没有推荐数据的情况
     */
    private void processNoCommentData() {
        relatede_Tv.setVisibility(View.GONE);
        all_Like_Rylt.setVisibility(View.GONE);
        allLikeGridView.setVisibility(View.GONE);
        releaGridView.setVisibility(View.GONE);
        line_tag_down.setVisibility(View.GONE);
        if (isNewStyle) {
            RelativeLayout.LayoutParams detail_info_p = (RelativeLayout.LayoutParams) detail_info_id.getLayoutParams();
            detail_info_p.topMargin = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 25f);
        }
    }

    private boolean iswelfareExposureAdd = false;
    private boolean isallLikeExposureAdd = false;

    /**
     * 可见才加载推荐的icon
     */
    public void startLoadImage() {
        if (!iswelfareExposureAdd && CommonUtils.isViewVisibleForDetail(releaGridView)) {
            releaApps.setNeedLoadImg(true);
            releaApps.notifyDataSetChange();
            iswelfareExposureAdd = true;
        }
        if (!isallLikeExposureAdd && CommonUtils.isViewVisibleForDetail(allLikeGridView)) {
            allLikeAdapter.setNeedLoadImg(true);
            allLikeAdapter.notifyDataSetChange();
            isallLikeExposureAdd = true;
        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//    }
}
