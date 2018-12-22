/*
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/

package com.prize.appcenter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.prize.app.beans.ClientInfo;
import com.prize.app.net.datasource.base.AppDetailData;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.DetailApp;
import com.prize.app.util.BlurPic;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.WindowMangerUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.AppCommentActivity;
import com.prize.appcenter.activity.AppDetailActivity;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.FlowLayout;
import com.prize.appcenter.ui.widget.GifView;
import com.prize.appcenter.ui.widget.NotifyingScrollView;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/**
 * 类描述：详情fragment
 *
 * @author huanglingjun
 * @version 版本
 */
public class AppDetailParentFgm extends MyHeaderFragment {
    private static final String TAG = "AppDetailParentFgm";
    private ImageView mAppIcon;
    private TextView mDownLoadTimes;
    private TextView mAppSize;
    private TextView mAd;
    private TextView mAppName;
    private TextView mScore;
    private AppDetailFgm appDetailFgm;
    private View view;
    private FragmentManager mChildFragmentManage;
    private AppDetailData appData;
    private NotifyingScrollView mNotifyingScrollView;
    private FrameLayout container_waitView;
    private FrameLayout containerView;
    private FrameLayout reloadView;
    private ImageView haveAdIcon;
    private FlowLayout ourtag_container;
    private WeakReference<AppDetailActivity> mActivities = null;
    private Handler mHandler;
    private View headView;
    /**
     * 背景模糊
     */
    private ImageView headImg_conver;
    /***
     * 渐变背景
     */
    private ImageView Img_conver;

    /**
     * 背景大图
     */
    private ImageView mBannerIcon;


    private RelativeLayout head_Rlyt;
    private AppsItemBean mAppsItemBean;
    private TextView mTitle;
    private RelativeLayout action_bar_no_tab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Fragment fragment = getChildFragmentManager().findFragmentByTag(AppDetailFgm.class.getSimpleName());
            if (fragment != null&&fragment instanceof  AppDetailFgm) {
                appDetailFgm = (AppDetailFgm) fragment;
            }
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "AppDetailParentFgm-onCreateView-appDetailFgm==null?" + (appDetailFgm == null));
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        setOnHeaderScrollChangedListener(new OnHeaderScrollChangedListener() {
            @Override
            public void onHeaderScrollChanged(float progress, int height,
                                              int scroll) {
                if (activity != null) {
                    if (Math.abs(progress) > 1f)
                        progress = 1f;
                    if (appDetailFgm != null) {
                        appDetailFgm.startLoadImage();
                    }
                }
                if (head_Rlyt != null) {
                    ViewHelper.setTranslationY(head_Rlyt, -(height * progress) / 2);
                    if (Build.VERSION.SDK_INT >= 16) {
                        headImg_conver.setImageAlpha((int) (progress * 255));
                    } else {
                        headImg_conver.setAlpha((int) progress * 255);
                    }
                    mTitle.setAlpha(Math.abs(progress));
                    if (!isNewStyle) {
                        if (action_bar_no_tab.getBackground() == null) {
                            action_bar_no_tab.setBackgroundResource(R.drawable.actionbar_bg);
                        }
                        action_bar_no_tab.getBackground().mutate().setAlpha((int) (progress * 255));
                    }
                }
            }
        });
        if (activity != null) {
            mActivities = new WeakReference<AppDetailActivity>((AppDetailActivity) activity);
        }
    }


    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        Bundle mBundle = getArguments();
        if (mBundle != null) {
            mAppsItemBean = mBundle.getParcelable("AppsItemBean");
        }
        if (JLog.isDebug) {
            JLog.i(TAG, "onCreateContentView-view=" + view
                    + "--mAppsItemBean=" + mAppsItemBean);
        }
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_detail_parent, container,
                    false);
            mBannerIcon = (ImageView) view.findViewById(R.id.headImg_id);
            headImg_conver = (ImageView) view.findViewById(R.id.headImg_conver);
            Img_conver = (ImageView) view.findViewById(R.id.Img_conver);
            head_Rlyt = (RelativeLayout) view.findViewById(R.id.head_Rlyt);
            action_bar_no_tab = (RelativeLayout) view.findViewById(R.id.action_bar_no_tab);
            headView = view.findViewById(R.id.headLayout_two);
            mNotifyingScrollView = (NotifyingScrollView) view.findViewById(R.id.parentScrollView_id);
            initHeadView(headView);
            initHeaderBannerView();
            container_waitView = (FrameLayout) view
                    .findViewById(R.id.container_wait);
            containerView = (FrameLayout) view
                    .findViewById(R.id.fragment_detail_id);
            reloadView = (FrameLayout) view.findViewById(R.id.reload_id);
            initLoadView();
            showWaiting();
        }
        return view;
    }

    private void initHeadView(View headView) {
        ourtag_container = (FlowLayout) headView
                .findViewById(R.id.ourtag_container);
        mAppIcon = (ImageView) headView.findViewById(R.id.app_icon_id);
        mDownLoadTimes = (TextView) headView
                .findViewById(R.id.downloadTimes_id);
        mAppSize = (TextView) headView.findViewById(R.id.appSize_id);
        mAppName = (TextView) headView.findViewById(R.id.name_id);
        mScore = (TextView) headView.findViewById(R.id.score_id);

        mAd = (TextView) headView.findViewById(R.id.ad_id);
        haveAdIcon = (ImageView) headView.findViewById(R.id.have_ad_icon_id);
        initActionBar();
//        mBannerIcon.setOnClickListener(this);
        if (mAppsItemBean != null) {
            initHeadData(mAppsItemBean);
        }

    }

    private void initHeadData(AppsItemBean itemData) {
        Activity mActivity = mActivities.get();
        if (mActivity == null)
            return;
        mAppName.setText(itemData.name);
        String user = itemData.downloadTimesFormat.replace("次", "人");
        mDownLoadTimes.setText(mActivity.getString(
                R.string.person_use, user));

        mAppSize.setText(itemData.apkSizeFormat);
        if (!TextUtils.isEmpty(itemData.rating)) {
            mScore.setText(itemData.rating + mActivity.getResources().getString(R.string.scrol));
        }

        if (itemData.isAd > 0) {
            haveAdIcon.setBackgroundResource(R.drawable.detail_have_ad);
            mAd.setText(mActivity.getString(R.string.app_have_ad));
            mAd.setTextColor(mActivity.getResources().getColor(
                    R.color.text_color_ff594e));
        } else {
            mAd.setText(mActivity.getString(R.string.app_no_ad));
            mAd.setTextColor(mActivity.getResources().getColor(
                    R.color.text_color_24aa42));
        }

        ourtag_container.removeAllViews();
        LinearLayout.LayoutParams params1 = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 8, 0);
        if (TextUtils.isEmpty(itemData.customTags)) {
            if (!TextUtils.isEmpty(itemData.ourTag)) {
                ourtag_container.setVisibility(View.VISIBLE);
                String[] tags = null;
                if (itemData.ourTag.contains(",")) {
                    tags = itemData.ourTag.split(",");

                } else {
                    tags = new String[]{itemData.ourTag};
                }
                if (tags != null && tags.length > 0) {
                    int size = tags.length;
                    int requireLen = size > 5 ? 5 : size;
                    TextView tagView;
                    for (int i = 0; i < requireLen; i++) {
                        tagView = (TextView) LayoutInflater.from(mActivity)
                                .inflate(R.layout.item_textview,
                                        null);
                        tagView.setTextColor(mActivity.getResources()
                                .getColor(R.color.text_color_009def));
                        if (TextUtils.isEmpty(tags[i])) {
                            continue;
                        }
                        tagView.setText(tags[i]);
                        tagView.setBackgroundResource(R.drawable.bg_list_tag);
                        tagView.setLayoutParams(params1);
                        ourtag_container.addView(tagView);
                    }
                }
            }
        } else {
            TextView tagView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(itemData.customTags);
            tagView.setTextColor(mActivity.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setGravity(Gravity.CENTER);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
            ourtag_container.setVisibility(View.VISIBLE);
        }


    }

    //    private Bitmap loadedImage;
    private boolean isNewStyle = false;

    private void initHeadData(DetailApp detailData) {
        Activity mActivity = mActivities.get();
        if (mActivity == null)
            return;
        if (detailData.istyle == 1 && detailData.style != null &&
                !TextUtils.isEmpty(detailData.style.backgroundColor)
                        & !TextUtils.isEmpty(detailData.style.backgroundUrl)) {
            isNewStyle = true;
            mTitle.setTextColor(Color.WHITE);
            headView.setVisibility(View.GONE);
            ViewStub mViewstub = (ViewStub) view.findViewById(R.id.mViewstub);
            View llView = mViewstub.inflate();
            llView.setVisibility(View.VISIBLE);
            mHandler = new MyHandler(this);
            TextView name_id = (TextView) llView.findViewById(R.id.name_id);
            FlowLayout ourtag_container = (FlowLayout) llView.findViewById(R.id.ourtag_container);
            initNewStyleTag(ourtag_container, detailData, mActivity);
            TextView app_description_Tv = (TextView) llView.findViewById(R.id.app_description_Tv);
            ImageView newstyle_icon = (ImageView) llView.findViewById(R.id.newstyle_icon);
            TextView downloadTimes_id = (TextView) llView.findViewById(R.id.downloadTimes_id);
            TextView score_id = (TextView) llView.findViewById(R.id.score_id);
            TextView ad_id = (TextView) llView.findViewById(R.id.ad_id);
            ImageView have_ad_icon_id = (ImageView) llView.findViewById(R.id.have_ad_icon_id);
            TextView appSize_id = (TextView) llView.findViewById(R.id.appSize_id);
            FrameLayout parent_FRly = (FrameLayout) view.findViewById(R.id.parent_FRly);
            ImageLoader.getInstance().displayImage(detailData.style.backgroundUrl, mBannerIcon, UILimageUtil.getNoImageUILoptions(), new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    AppDetailParentFgm.this.loadedImage = loadedImage;
                    new BlurTask(AppDetailParentFgm.this).execute(loadedImage);
                    if (loadedImage != null) {
                        int width = loadedImage.getWidth();
                        int height = loadedImage.getHeight();
                        float a = (float) height / (float) width;
                        BigDecimal b = new BigDecimal(a);
                        Double rate = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (JLog.isDebug) {
                            JLog.i(TAG, "width=" + width + "--height=" + height + "--a=" + a
                                    + "-rate=" + rate + "--view.getWidth()=" + view.getWidth()
                                    + "--view.getHeight()=" + view.getHeight());
                        }
                        if (mHandler != null) {
                            Message msg = Message.obtain();
                            msg.what = 2;
                            msg.obj = rate;
                            mHandler.sendMessage(msg);
                        }
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
            if (!TextUtils.isEmpty(detailData.brief)) {
                app_description_Tv.setText(detailData.brief);
            } else {
                app_description_Tv.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(TextUtils.isEmpty(detailData.largeIcon) ?
                            detailData.iconUrl : detailData.largeIcon, newstyle_icon,
                    UILimageUtil.getUILoptions(R.drawable.default_icon));
            name_id.setText(detailData.name);
            downloadTimes_id.setText(detailData.downloadTimesFormat);
            if (detailData.downloadTimesFormat != null) {
                String user = detailData.downloadTimesFormat.replace("次", "人");
                downloadTimes_id.setText(mActivity.getString(R.string.person_use,
                        user));
            }
            if (!TextUtils.isEmpty(detailData.rating)) {
                score_id.setText(detailData.rating + mActivity.getResources().getString(R.string.scrol));
            }

            if (detailData.isAd > 0) {
                have_ad_icon_id.setBackgroundResource(R.drawable.detail_have_ad);
                ad_id.setText(mActivity.getString(R.string.app_have_ad));
                ad_id.setTextColor(mActivity.getResources().getColor(
                        R.color.text_color_ff594e));
            } else {
                ad_id.setText(mActivity.getString(R.string.app_no_ad));
                ad_id.setTextColor(mActivity.getResources().getColor(
                        R.color.text_color_24aa42));
            }
            appSize_id.setText(detailData.apkSizeFormat);
            int bgColor;
            try {
                bgColor = Color.parseColor("#" + detailData.style.backgroundColor);
            } catch (Exception e) {
                bgColor = Color.parseColor("#42cf78");//防止后台配置出错时，默认浅绿色
            }

            GradientDrawable drawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{0, bgColor});
            parent_FRly.setBackgroundColor(bgColor);
            Img_conver.setBackground(drawable);

            action_bar_back.getBackground().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//            action_goToComment.getBackground().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            action_bar_search.getBackground().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

//            action_bar_back.setImageResource(R.drawable.action_bar_back_white_normal);
            action_goToComment.setImageResource(R.drawable.icon_comment_white_nomal);
//            action_bar_search.setImageResource(R.drawable.action_bar_search_white_nomal);

        } else {
            mBannerIcon.setVisibility(View.GONE);
            WindowMangerUtils.changeStatus(mActivity.getWindow());
            mAppName.setText(detailData.name);
            String user = detailData.downloadTimesFormat.replace("次", "人");
            mDownLoadTimes.setText(mActivity.getString(
                    R.string.person_use, user));

            mAppSize.setText(detailData.apkSizeFormat);
            mScore.setText(detailData.rating + mActivity.getResources().getString(R.string.scrol));

            if (detailData.isAd > 0) {
                haveAdIcon.setBackgroundResource(R.drawable.detail_have_ad);
                mAd.setText(mActivity.getString(R.string.app_have_ad));
                mAd.setTextColor(mActivity.getResources().getColor(
                        R.color.text_color_ff594e));
            } else {
                mAd.setText(mActivity.getString(R.string.app_no_ad));
                mAd.setTextColor(mActivity.getResources().getColor(
                        R.color.text_color_24aa42));
            }
            ImageLoader.getInstance().displayImage(TextUtils.isEmpty(detailData.largeIcon) ?
                            detailData.iconUrl : detailData.largeIcon,
                    mAppIcon, UILimageUtil.getDeatailLoptions(), null);

            ourtag_container.removeAllViews();
            LinearLayout.LayoutParams params1 = new LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(0, 0, 8, 0);
            params1.setMargins(0, 0, 8, 0);
            if (TextUtils.isEmpty(detailData.customTags)) {
                if (!TextUtils.isEmpty(detailData.ourTag)) {
                    ourtag_container.setVisibility(View.VISIBLE);
                    String[] tags = null;
                    if (detailData.ourTag.contains(",")) {
                        tags = detailData.ourTag.split(",");

                    } else {
                        tags = new String[]{detailData.ourTag};
                    }
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 5 ? 5 : size;
                        TextView tagView;
                        for (int i = 0; i < requireLen; i++) {
                            tagView = (TextView) LayoutInflater.from(mActivity)
                                    .inflate(R.layout.item_textview,
                                            null);
                            tagView.setTextColor(mActivity.getResources()
                                    .getColor(R.color.text_color_009def));
                            if (TextUtils.isEmpty(tags[i])) {
                                continue;
                            }
                            tagView.setText(tags[i]);
                            tagView.setBackgroundResource(R.drawable.bg_list_tag);
                            tagView.setLayoutParams(params1);
                            ourtag_container.addView(tagView);
                        }
                    }
                }
            } else {
                TextView tagView = (TextView) LayoutInflater.from(mActivity)
                        .inflate(R.layout.item_textview, null);
                tagView.setText(detailData.customTags);
                tagView.setTextColor(mActivity.getResources().getColor(
                        R.color.text_color_009def));
                tagView.setBackgroundResource(R.drawable.icon_customertag);
                tagView.setGravity(Gravity.CENTER);
                tagView.setLayoutParams(params1);
                ourtag_container.addView(tagView);
                ourtag_container.setVisibility(View.VISIBLE);
            }
        }

    }

    /***
     * 初始化数据
     *
     * @param appDetailData
     *            AppDetailData
     */
    public void initData(AppDetailData appDetailData) {
        if (JLog.isDebug) {
            JLog.i(TAG, "AppDetailParentFgm-initData-start");
        }
        Activity mActivity = mActivities.get();
        if (mActivity == null)
            return;
        mTitle.setText(appDetailData.app.name);
        mTitle.setAlpha(0f);
        appData = appDetailData;
        DetailApp detailData = appData.app;
        if (detailData == null)
            return;
//        if (mAppsItemBean == null) {
        initHeadData(detailData);
//        }
        initDetailFragment(appData);
        if (JLog.isDebug) {
            JLog.i(TAG, "AppDetailParentFgm-initData--end");
        }

    }


    private void initDetailFragment(final AppDetailData appData) {
        hideWaiting();
        if (appDetailFgm == null) {
            Activity mActivity = mActivities.get();
            if (mActivity == null)
                return;
            if (appDetailFgm == null) {
                appDetailFgm = new AppDetailFgm();
            }
            Bundle args = new Bundle();
            args.putSerializable("AppDetailData", appData);
            appDetailFgm.setArguments(args);
        }
        mChildFragmentManage = getChildFragmentManager();
        if (mChildFragmentManage != null && !appDetailFgm.isAdded()) {
            try {
                mChildFragmentManage.beginTransaction()
                        .add(R.id.fragment_detail_id, appDetailFgm, AppDetailFgm.class.getSimpleName())
                        .commit();
            } catch (IllegalStateException e) {
                JLog.i(TAG,"initDetailFragment-e="+e.getMessage());
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }

        }

    }

    @Override
    public View onCreateHeaderView() {
        return mBannerIcon;
    }

    @Override
    public void onDestroy() {
        if (mActivities != null) {
            mActivities.clear();
//            mActivities = null;
        }
//        appDetailFgm = null;
//        mChildFragmentManage = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
//        if (mAsyncTask != null) {
//            mAsyncTask.cancel(true);
//        }
        super.onDestroy();
    }

    public void showReloadView() {
        if (JLog.isDebug) {
            JLog.i(TAG, "showReloadView");
        }
        loadingFailed(new ReloadFunction() {

            @Override
            public void reload() {
                AppDetailActivity mActivity = mActivities.get();
                if (mActivity == null)
                    return;
                mActivity.doRequest();
            }
        });
    }

    private void initLoadView() {
        Activity mActivity = mActivities.get();
        if (mActivity == null)
            return;
        View waitView = LayoutInflater.from(mActivity).inflate(
                R.layout.fragment_detail_waiting, null);
        GifView gifWaitingView = (GifView) waitView
                .findViewById(R.id.gif_waiting);
        View reload_View = LayoutInflater.from(mActivity).inflate(
                R.layout.detail_relaod_layout, null);
        ImageView reloadIcon = (ImageView) reload_View
                .findViewById(R.id.loadFailure_icon_id);
        LinearLayout.LayoutParams params = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        params.setMargins(0, 100, 0, 0);
        reloadIcon.setLayoutParams(params);
        gifWaitingView.setLayoutParams(params);
        container_waitView.addView(waitView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        reloadView.addView(reload_View, FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        initAllView(container_waitView, containerView, reloadView);
    }

    /**
     * 滚动到底
     */
    public void scrollBotton() {
        if (JLog.isDebug) {
            JLog.i("PrizeStatUtil", "AppDetailParentFgm-scrollBotton-Y=" + (mNotifyingScrollView.getChildAt(0).getHeight() - ClientInfo.getInstance().screenHeight - 100));
        }
        if (mNotifyingScrollView != null) {
            mNotifyingScrollView.scrollTo(0, mNotifyingScrollView.getChildAt(0).getHeight() - ClientInfo.getInstance().screenHeight - 100);
        }
    }


    private static class MyHandler extends Handler {
        private final WeakReference<AppDetailParentFgm> mPaletteImageViewWeakReference;

        MyHandler(AppDetailParentFgm paletteImageView) {
            mPaletteImageViewWeakReference = new WeakReference<AppDetailParentFgm>(paletteImageView);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mPaletteImageViewWeakReference.get() != null) {
                AppDetailParentFgm paletteImageView = mPaletteImageViewWeakReference.get();

                switch (msg.what) {
                    case 1:
                        paletteImageView.headImg_conver.setImageBitmap(paletteImageView.gaussBg);
                        if (Build.VERSION.SDK_INT >= 16) {
                            paletteImageView.headImg_conver.setImageAlpha(25);
                        } else {
                            paletteImageView.headImg_conver.setAlpha(0);
                        }
                        break;
                    case 2:
                        if (msg.obj != null) {
                            double rate = (double) msg.obj;
                            RelativeLayout.LayoutParams mBannerIcon_p = (RelativeLayout.LayoutParams) paletteImageView.mBannerIcon.getLayoutParams();
                            mBannerIcon_p.height = (int) (ClientInfo.getInstance().screenWidth * rate);

                            if (JLog.isDebug) {
                                JLog.i(TAG, "mBannerIcon_p.height=" + mBannerIcon_p.height
                                        + "--rate=" + rate);
                            }
                            RelativeLayout.LayoutParams headImg_conver_p = (RelativeLayout.LayoutParams) paletteImageView.headImg_conver.getLayoutParams();
                            headImg_conver_p.height = (int) (ClientInfo.getInstance().screenWidth * rate);

                            RelativeLayout.LayoutParams Img_conver_p = (RelativeLayout.LayoutParams) paletteImageView.Img_conver.getLayoutParams();
                            Img_conver_p.height = (int) ((int) (ClientInfo.getInstance().screenWidth * rate) + Math.ceil(DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 0.5f)));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private Bitmap gaussBg;

    private static class BlurTask extends AsyncTask<Bitmap, Void, Void> {
        private final WeakReference<AppDetailParentFgm> mPaletteImageViewWeakReference;

        BlurTask(AppDetailParentFgm paletteImageView) {
            mPaletteImageViewWeakReference = new WeakReference<AppDetailParentFgm>(paletteImageView);
        }

        @Override
        protected Void doInBackground(Bitmap... bitmaps) {
            if (mPaletteImageViewWeakReference.get() != null) {
                AppDetailParentFgm paletteImageView = mPaletteImageViewWeakReference.get();
                paletteImageView.gaussBg = BlurPic.blurScale(bitmaps[0], 30);
                paletteImageView.mHandler.sendEmptyMessage(1);
            }
            return null;
        }


    }

    public void goToComment() {
        if (appData == null || appData.app == null) {
            return;
        }
        Intent intent = new Intent(mActivities.get(), AppCommentActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("detailData", appData.app);
        bundle.putBoolean("isComment", false);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    ImageButton action_bar_back, action_goToComment, action_bar_search;

    /**
     * 方法描述：初始化actionbar
     */

    protected void initActionBar() {
        action_bar_back = (ImageButton) view.findViewById(R.id.action_bar_back);
        action_goToComment = (ImageButton) view.findViewById(R.id.action_goToComment);
        action_bar_search = (ImageButton) view.findViewById(R.id.action_bar_search);
        mTitle = (TextView) view.findViewById(R.id.app_title_Tv);
        action_goToComment.setOnClickListener(onClickListener);
        mTitle.setOnClickListener(onClickListener);
        action_bar_search.setOnClickListener(onClickListener);
        action_bar_back.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.action_bar_back:
                case R.id.app_title_Tv:
                    mActivities.get().finish();
                    break;
                case R.id.action_goToComment:
                    goToComment();
                    break;
                case R.id.action_bar_search:
                    UIUtils.goSearchActivity(mActivities.get());
                    break;
            }
        }
    };

    /**
     * 设置新版的tag
     *
     * @param ourtag_container FlowLayout
     * @param detailData       DetailApp
     * @param mActivity        Activity
     */
    private void initNewStyleTag(FlowLayout ourtag_container, DetailApp detailData, Activity mActivity) {
        ourtag_container.removeAllViews();
        LinearLayout.LayoutParams params1 = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 8, 0);
        if (TextUtils.isEmpty(detailData.customTags)) {
            if (!TextUtils.isEmpty(detailData.ourTag)) {
                ourtag_container.setVisibility(View.VISIBLE);
                String[] tags = null;
                if (detailData.ourTag.contains(",")) {
                    tags = detailData.ourTag.split(",");

                } else {
                    tags = new String[]{detailData.ourTag};
                }
                if (tags != null && tags.length > 0) {
                    int size = tags.length;
                    int requireLen = size > 5 ? 5 : size;
                    TextView tagView;
                    for (int i = 0; i < requireLen; i++) {
                        tagView = (TextView) LayoutInflater.from(mActivity)
                                .inflate(R.layout.item_textview,
                                        null);
                        tagView.setTextColor(mActivity.getResources()
                                .getColor(R.color.text_color_009def));
                        if (TextUtils.isEmpty(tags[i])) {
                            continue;
                        }
                        tagView.setText(tags[i]);
                        tagView.setBackgroundResource(R.drawable.bg_list_tag);
                        tagView.setLayoutParams(params1);
                        ourtag_container.addView(tagView);
                    }
                }
            }
        } else {
            TextView tagView = (TextView) LayoutInflater.from(mActivity)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(detailData.customTags);
            tagView.setTextColor(mActivity.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setGravity(Gravity.CENTER);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
            ourtag_container.setVisibility(View.VISIBLE);
        }
    }

}
