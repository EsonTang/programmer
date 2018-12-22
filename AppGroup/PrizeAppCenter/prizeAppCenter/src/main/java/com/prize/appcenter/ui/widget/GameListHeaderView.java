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

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.HomeAdBean;
import com.prize.app.beans.Person;
import com.prize.app.beans.WelfareBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppHeadCategories;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.JLog;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.CategoryAppGameListActivity;
import com.prize.appcenter.activity.CommonCategoryActivity;
import com.prize.appcenter.activity.GiftCenterActivity;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.NewGameListActivity;
import com.prize.appcenter.activity.OnlineGameListActivity;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.activity.TopicDetailActivity;
import com.prize.appcenter.activity.WebViewActivity;
import com.prize.appcenter.bean.GameCommentBean;
import com.prize.appcenter.bean.GameGatherData;
import com.prize.appcenter.callback.ItemStateChangeCallBack;
import com.prize.appcenter.ui.adapter.AppGameNoticeAdapter;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;
import com.prize.cloud.activity.LoginActivityNew;
import com.prize.cloud.util.CloudIntent;
import com.prize.statistics.model.ExposureBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * 类描述：游戏（头部布局:精彩游戏 玩家福利，新游尝鲜）
 *
 * @author 聂礼刚
 * @version 1.0
 */
public class GameListHeaderView extends LinearLayout {
    private RootActivity mContext;
    private Person mPerson;
    private GameGatherData mGameGatherData;
    /**
     * 精彩游戏容器
     **/
    private LinearLayout mWonderfulContainer;
    /**
     * 玩家福利容器
     **/
    private LinearLayout mWelfareContainer;
    private CycleGalleryViewPager mCommentsGallery;
    private Handler mHandler;
    private IUIDownLoadListenerImp listener = null;
    private SpacingTextView mWelfareTitle, mCommentsTitle;
    private ImageView mLeftEntry, mRightTopEntry;  // 第二部分三个分类入口
    private TextView mWelfareLink;
    private LinearLayout mWelfareLoginLl;
    private TextView mSeeMore;
    private LocalBroadcastManager mLocalBroadcastManager;
    private LocalBroadcastReceiver mLocalBroadcastReceiver;
    private ShapeDrawable mTopShapeDrawable;
    private ShapeDrawable mBottomShapeDrawable;

    /**
     * 当前页是否处于显示状态
     */
    private boolean isActivity = true; // 默认true
    private AppGameNoticeAdapter mNoticeAdapter;
    /**
     * 精彩游戏曝光数据
     **/
    private List<ExposureBean> wonderfulExposureBeans = new ArrayList<>();
    /**
     * 实际精彩游戏曝光数据id
     **/
    private List<String> realwonderIds = new ArrayList<>();
    /**
     * 玩家福利曝光数据
     **/
    private List<ExposureBean> welfareExposureBeans = new ArrayList<>();
    /**
     * 玩家福利曝光数据Id
     **/
    private List<String> realwelfareIds = new ArrayList<>();
    /**
     * 新游尝鲜曝光数据
     **/
    private List<ExposureBean> gamecommentExposureBeans = new ArrayList<>();
    /**
     * 实际新游尝鲜曝光数据id
     **/
    private List<String> realcommentIds = new ArrayList<>();


    public void setIsActivity(boolean state) {
        isActivity = state;
    }

    public GameListHeaderView(RootActivity context) {
        super(context);
        mContext = context;
        if (mContext == null)
            return;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        View view = inflate(context, R.layout.game_list_header, this);
        findViewById(view);
    }


    public GameListHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameListHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void findViewById(View view) {
        mWelfareTitle = (SpacingTextView) view.findViewById(R.id.welfare_title);
        mCommentsTitle = (SpacingTextView) view.findViewById(R.id.comments_title);
        mWonderfulContainer = (LinearLayout) view.findViewById(R.id.child_id);
        PrizeHorizontalScrollView horizatal_id = (PrizeHorizontalScrollView) view.findViewById(R.id.horizatal_id);
        mLeftEntry = (ImageView) view.findViewById(R.id.left_entry);
        mRightTopEntry = (ImageView) view.findViewById(R.id.right_top_entry);
        mWelfareLink = (TextView) view.findViewById(R.id.welfare_login_link);
        mWelfareLoginLl = (LinearLayout) view.findViewById(R.id.welfare_login_ll);
        GridView recommand_notice_gv = (GridView) view.findViewById(R.id.recommand_notice_gv);
        mWelfareContainer = (LinearLayout) view.findViewById(R.id.welfare_container);
        mCommentsGallery = (CycleGalleryViewPager) view.findViewById(R.id.comments_wgallery);
        mSeeMore = (TextView) view.findViewById(R.id.see_more);
        horizatal_id.setOnScrollChangedListener(new PrizeHorizontalScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int scrollType) {
                if (mWonderfulContainer != null && PrizeHorizontalScrollView.IDLE == scrollType) {
                    //此时去计算
                    if (mItemSelectCallBack != null) {
                        mItemSelectCallBack.OnItemSelect(getWonderfulExposureBeans(), true);
                    }
                }
            }
        });
        if (mNoticeAdapter == null) {
            mNoticeAdapter = new AppGameNoticeAdapter(mContext);
        }
        recommand_notice_gv.setAdapter(mNoticeAdapter);
        recommand_notice_gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mNoticeAdapter != null && mNoticeAdapter.getItem(position) != null) {
                    AppHeadCategories bean = mNoticeAdapter.getItem(position);
                    Intent intent = new Intent(mContext, CategoryAppGameListActivity.class);
                    if (!"0".equals(bean.catId)) {
                        intent.putExtra(CategoryAppGameListActivity.selectPos, bean.cIdpos);
                        intent.putExtra(CategoryAppGameListActivity.parentID, bean.catId);
                        intent.putExtra(CategoryAppGameListActivity.typeName, bean.pCatName);
                        intent.putExtra(CategoryAppGameListActivity.subtypeName, bean.catName);
                        intent.putExtra(CategoryAppGameListActivity.SUBTYPEID, bean.catId);
                        intent.putExtra(CategoryAppGameListActivity.tags, bean.tags);
                        intent.putExtra(CategoryAppGameListActivity.isGameKey, true);
                    } else {
                        intent = new Intent(mContext, CommonCategoryActivity.class);
                    }
                    intent.putExtra("isPopular", true);
                    mContext.startActivity(intent);
                    MTAUtil.onClickGamePageHeadEntrance(bean.showText);
                }
            }
        });

        setListener();
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public boolean setDownloadRefreshHandle() {
        if (listener == null) {
            listener = IUIDownLoadListenerImp.getInstance();
            listener.setmCallBack(new MyIUIDownLoadCallBack(this));
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return AIDLUtils.registerCallback(listener);
    }

    /**
     * 判断新游尝鲜是否可见
     *
     * @return boolean
     */
    public boolean isCommentsGalleryViewVisible() {
        return mCommentsGallery != null && CommonUtils.isVisibleGamepageView(mCommentsGallery);
    }


    /**
     * 判断精彩游戏是否可见
     *
     * @return boolean
     */
    public boolean ismWonderfulViewVisible() {
        return mWonderfulContainer != null && CommonUtils.isVisibleGamepageView(mWonderfulContainer);
    }

    private PagerAdapter mPagerAdapter;

    private void initAdapter(final GameGatherData data) {
        if (mPagerAdapter == null) {
            mPagerAdapter = new PagerAdapter() {
                @Override
                public int getCount() {
                    return data.gamecomment.comments.size();
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return view == object;
                }

                @Override
                public Object instantiateItem(ViewGroup container, final int position) {
                    View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_comments, container, false);

                    final ImageView gameIcon = (ImageView) convertView
                            .findViewById(R.id.game_iv);
                    TextView gameName = (TextView) convertView
                            .findViewById(R.id.game_name_tv);
                    final AnimDownloadProgressButton downloadBtn = (AnimDownloadProgressButton) convertView
                            .findViewById(R.id.game_download_btn);
                    downloadBtn.enabelDefaultPress(true);
                    RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.ratingBar_id);
                    TextView comment = (TextView) convertView.findViewById(R.id.comment_id);
                    TextView model = (TextView) convertView.findViewById(R.id.model_id);
                    RelativeLayout topLayout = (RelativeLayout) convertView.findViewById(R.id.top_id);
                    RelativeLayout bottomLayout = (RelativeLayout) convertView.findViewById(R.id.bottom_id);
                    GameCommentBean bean = data.gamecomment.comments.get(position);

                    bean.app = CommonUtils.formatAppPageInfo(bean.app, Constants.GAME_GUI, "gather", position + 1);
                    final AppsItemBean gameBean = bean.app;

                    if (!TextUtils.isEmpty(gameBean.largeIcon)) {
                        ImageLoader.getInstance().displayImage(gameBean.largeIcon,
                                gameIcon, UILimageUtil.getUILoptions(), null);
                    } else {
                        if (gameBean.iconUrl != null) {
                            ImageLoader.getInstance()
                                    .displayImage(gameBean.iconUrl, gameIcon,
                                            UILimageUtil.getUILoptions(), null);
                        }
                    }

                    if (gameBean.name != null) {
                        gameName.setText(gameBean.name);
                    }

                    downloadBtn.setGameInfo(gameBean);
                    comment.setText(bean.comment);
                    model.setText(bean.model);
                    ratingBar.setRating(Float.valueOf(gameBean.rating));

                    mTopShapeDrawable.getPaint().setColor(Color.parseColor("#ecf3f4"));
                    mBottomShapeDrawable.getPaint().setColor(Color.parseColor("#ecf3f4"));
                    mBottomShapeDrawable.getPaint().setAlpha(153);

                    topLayout.setBackground(mTopShapeDrawable);
                    bottomLayout.setBackground(mBottomShapeDrawable);
                    container.addView(convertView);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (gameBean == null || gameBean.id == null)
                                return;
                            MTAUtil.onNewGameCardClick(position + 1);
                            UIUtils.gotoAppDetail(gameBean, gameBean.id, mContext);
                        }

                    });

                    downloadBtn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final int state = AIDLUtils.getGameAppState(
                                    gameBean.packageName, gameBean.id + "",
                                    gameBean.versionCode);
                            switch (state) {
                                case AppManagerCenter.APP_STATE_UNEXIST:
                                case AppManagerCenter.APP_STATE_UPDATE:
                                case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

                                    if (ClientInfo.getAPNType(mContext) == ClientInfo.NONET) {
                                        ToastUtils.showToast(R.string.nonet_connect);
                                        return;
                                    }
                            }
                            if (BaseApplication.isDownloadWIFIOnly()
                                    && ClientInfo.getAPNType(mContext) != ClientInfo.WIFI) {
                                switch (state) {
                                    case AppManagerCenter.APP_STATE_UNEXIST:
                                    case AppManagerCenter.APP_STATE_UPDATE:
                                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                                        mDownDialog = new DownDialog(mContext,
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
                                                        startAnimation(state, gameIcon);
                                                        UIUtils.downloadApp(gameBean);
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
                            if (ClientInfo.networkType == ClientInfo.WIFI) {
                                startAnimation(state, gameIcon);
                            }
                        }
                    });
                    return convertView;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }

                @Override
                public float getPageWidth(int position) {
                    return 0.7f;//建议值为0.6~1.0之间
                }
            };

            mCommentsGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (mItemSelectCallBack != null) {
                        mItemSelectCallBack.OnItemSelect(getGamecommentExposureBeans(), false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            mCommentsGallery.setNarrowFactor(0.92f);

            mSeeMore.setText(getResources().getString(R.string.see_more) + " >");
            mSeeMore.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, NewGameListActivity.class);
                    mContext.startActivity(intent);
                    mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    MTAUtil.onMoreNewGameCardClick();
                }
            });

            mCommentsGallery.setAdapter(mPagerAdapter);

        } else {
            mPagerAdapter.notifyDataSetChanged();
        }
    }

    public void setData(final GameGatherData data) {
        realwonderIds.clear();
        realcommentIds.clear();
        realwelfareIds.clear();
        mGameGatherData = data;
        mNoticeAdapter.setData(data.categorys);
        addWelfareItem();
        if (data.wonderful.apps != null) {
            data.wonderful.apps = CommonUtils.filterWonderfulData(data.wonderful.apps);
            addWonderfulCard(data.wonderful.apps.size());
        }

        if (data.ads != null && data.ads.size() >= 2) {
            if (data.ads.get(0).imageUrl == null || data.ads.get(1).imageUrl == null)
                return;
            ImageLoader.getInstance().displayImage(data.ads.get(0).imageUrl, mLeftEntry, UILimageUtil.getUILoptions(R.drawable.bg_ad));
            ImageLoader.getInstance().displayImage(data.ads.get(1).imageUrl, mRightTopEntry, UILimageUtil.getUILoptions(R.drawable.bg_ad));
            mLeftEntry.setTag(data.ads.get(0));
            mRightTopEntry.setTag(data.ads.get(1));
        }

        if (data.gamecomment != null && data.gamecomment.comments != null && data.gamecomment.comments.size() > 0) {//新游尝鲜模块
            mCommentsTitle.setText(data.gamecomment.title);
            mCommentsTitle.setLetterSpacing(10);
            float Radius = 10;
            float[] topRadii = {Radius, Radius, Radius, Radius, 0, 0, 0, 0};
            float[] bottomeRadii = {0, 0, 0, 0, Radius, Radius, Radius, Radius};
            mTopShapeDrawable = new ShapeDrawable(new RoundRectShape(topRadii, null, null));
            mBottomShapeDrawable = new ShapeDrawable(new RoundRectShape(bottomeRadii, null, null));
            initAdapter(data);
//            mCommentsGallery.setAdapter(new PagerAdapter() {
//                @Override
//                public int getCount() {
//                    return data.gamecomment.comments.size();
//                }
//
//                @Override
//                public boolean isViewFromObject(View view, Object object) {
//                    return view == object;
//                }
//
//                @Override
//                public Object instantiateItem(ViewGroup container, final int position) {
//                    View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_comments, container, false);
//
//                    final ImageView gameIcon = (ImageView) convertView
//                            .findViewById(R.id.game_iv);
//                    TextView gameName = (TextView) convertView
//                            .findViewById(R.id.game_name_tv);
//                    final AnimDownloadProgressButton downloadBtn = (AnimDownloadProgressButton) convertView
//                            .findViewById(R.id.game_download_btn);
//                    downloadBtn.enabelDefaultPress(true);
//                    RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.ratingBar_id);
//                    TextView comment = (TextView) convertView.findViewById(R.id.comment_id);
//                    TextView model = (TextView) convertView.findViewById(R.id.model_id);
//                    RelativeLayout topLayout = (RelativeLayout) convertView.findViewById(R.id.top_id);
//                    RelativeLayout bottomLayout = (RelativeLayout) convertView.findViewById(R.id.bottom_id);
//                    GameCommentBean bean = data.gamecomment.comments.get(position);
//
//                    bean.app = CommonUtils.formatAppPageInfo(bean.app, Constants.GAME_GUI, "gather", position + 1);
//                    final AppsItemBean gameBean = bean.app;
//
//                    if (!TextUtils.isEmpty(gameBean.largeIcon)) {
//                        ImageLoader.getInstance().displayImage(gameBean.largeIcon,
//                                gameIcon, UILimageUtil.getUILoptions(), null);
//                    } else {
//                        if (gameBean.iconUrl != null) {
//                            ImageLoader.getInstance()
//                                    .displayImage(gameBean.iconUrl, gameIcon,
//                                            UILimageUtil.getUILoptions(), null);
//                        }
//                    }
//
//                    if (gameBean.name != null) {
//                        gameName.setText(gameBean.name);
//                    }
//
//                    downloadBtn.setGameInfo(gameBean);
//                    comment.setText(bean.comment);
//                    model.setText(bean.model);
//                    ratingBar.setRating(Float.valueOf(gameBean.rating));
//
//                    mTopShapeDrawable.getPaint().setColor(Color.parseColor("#ecf3f4"));
//                    mBottomShapeDrawable.getPaint().setColor(Color.parseColor("#ecf3f4"));
//                    mBottomShapeDrawable.getPaint().setAlpha(153);
//
//                    topLayout.setBackground(mTopShapeDrawable);
//                    bottomLayout.setBackground(mBottomShapeDrawable);
//                    container.addView(convertView);
//                    convertView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (gameBean == null || gameBean.id == null)
//                                return;
//                            MTAUtil.onNewGameCardClick(position + 1);
//                            UIUtils.gotoAppDetail(gameBean, gameBean.id, mContext);
//                        }
//
//                    });
//
//                    downloadBtn.setOnClickListener(new OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            final int state = AIDLUtils.getGameAppState(
//                                    gameBean.packageName, gameBean.id + "",
//                                    gameBean.versionCode);
//                            switch (state) {
//                                case AppManagerCenter.APP_STATE_UNEXIST:
//                                case AppManagerCenter.APP_STATE_UPDATE:
//                                case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
//
//                                    if (ClientInfo.getAPNType(mContext) == ClientInfo.NONET) {
//                                        ToastUtils.showToast(R.string.nonet_connect);
//                                        return;
//                                    }
//                            }
//                            if (BaseApplication.isDownloadWIFIOnly()
//                                    && ClientInfo.getAPNType(mContext) != ClientInfo.WIFI) {
//                                switch (state) {
//                                    case AppManagerCenter.APP_STATE_UNEXIST:
//                                    case AppManagerCenter.APP_STATE_UPDATE:
//                                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
//                                        mDownDialog = new DownDialog(mContext,
//                                                R.style.add_dialog);
//                                        mDownDialog.show();
//                                        mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {
//
//                                            @Override
//                                            public void onClick(int which) {
//                                                dismissDialog();
//                                                switch (which) {
//                                                    case 0:
//                                                        break;
//                                                    case 1:
//                                                        startAnimation(state, gameIcon);
//                                                        UIUtils.downloadApp(gameBean);
//                                                }
//                                            }
//                                        });
//                                        break;
//                                    default:
//                                        downloadBtn.onClick();
//                                        break;
//                                }
//
//                            } else {
//                                downloadBtn.onClick();
//                            }
//                            if (ClientInfo.networkType == ClientInfo.WIFI) {
//                                startAnimation(state, gameIcon);
//                            }
//                        }
//                    });
//                    return convertView;
//                }
//
//                @Override
//                public void destroyItem(ViewGroup container, int position, Object object) {
//                    container.removeView((View) object);
//                }
//
//                @Override
//                public float getPageWidth(int position) {
//                    return 0.7f;//建议值为0.6~1.0之间
//                }
//            });
//            mCommentsGallery.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                @Override
//                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//                }
//
//                @Override
//                public void onPageSelected(int position) {
//                    if (mItemSelectCallBack != null) {
//                        mItemSelectCallBack.OnItemSelect(getGamecommentExposureBeans(), false);
//                    }
//                }
//
//                @Override
//                public void onPageScrollStateChanged(int state) {
//
//                }
//            });
//            mCommentsGallery.setNarrowFactor(0.92f);
//
//            mSeeMore.setText(getResources().getString(R.string.see_more) + " >");
//            mSeeMore.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(mContext, NewGameListActivity.class);
//                    mContext.startActivity(intent);
//                    mContext.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//                    MTAUtil.onMoreNewGameCardClick();
//                }
//            });
            if (gamecommentExposureBeans != null) {
                gamecommentExposureBeans.clear();
            }
            for (int i = 0; i < data.gamecomment.comments.size(); i++) {
                AppsItemBean bean = data.gamecomment.comments.get(i).app;
                ExposureBean exposureBean = CommonUtils.formNewPagerExposure(bean, Constants.GAME_GUI, "welfare");
                if (!gamecommentExposureBeans.contains(exposureBean)) {
                    gamecommentExposureBeans.add(exposureBean);
                }
            }
        }

    }

    public void clearCacheData() {
        if (welfareExposureBeans != null) {
            welfareExposureBeans.clear();
        }
        if (wonderfulExposureBeans != null) {
            wonderfulExposureBeans.clear();
        }
        if (gamecommentExposureBeans != null) {
            gamecommentExposureBeans.clear();
        }
        if (realwonderIds != null) {
            realwonderIds.clear();
        }
        if (realwelfareIds != null) {
            realwelfareIds.clear();
        }

        if (realcommentIds != null) {
            realcommentIds.clear();
        }
    }

    public void setListener() {

        if (mHandler == null) {
            mHandler = new Handler();
        }
        if (listener == null) {
            listener = IUIDownLoadListenerImp.getInstance();
            listener.setmCallBack(new MyIUIDownLoadCallBack(this));
        }
        AIDLUtils.registerCallback(listener);

        OnClickListener mEntryClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeAdBean homeAdBean = (HomeAdBean) v.getTag();
                onItemClick(homeAdBean);
                if (!TextUtils.isEmpty(homeAdBean.title)) {
                    MTAUtil.onCatEntryClick(homeAdBean.title);
                }
            }
        };

        mLeftEntry.setOnClickListener(mEntryClickListener);
        mRightTopEntry.setOnClickListener(mEntryClickListener);
    }

    /**
     * 刷新精彩游戏
     */
    public void refreshWonderful() {
        if (mGameGatherData != null && mGameGatherData.wonderful != null
                && mGameGatherData.wonderful.apps != null && mGameGatherData.wonderful.apps.size() > 0) {
            int subViewCount = mWonderfulContainer.getChildCount();
            if (subViewCount > 0) {
                for (int i = 0; i < subViewCount; i++) {
                    ItemWonderfulView view = (ItemWonderfulView) mWonderfulContainer.getChildAt(i);
                    view.setState(mGameGatherData.wonderful.apps.get(i));
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerLocalBroadcastReceiver(); //注册登录广播
        if (mHandler == null) {
            mHandler = new Handler();
        }
        if (listener == null) {
            listener = IUIDownLoadListenerImp.getInstance();
            listener.setmCallBack(new MyIUIDownLoadCallBack(this));
        }
        AIDLUtils.registerCallback(listener);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unRegisterLocalBroadcastReceiver();
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {
            listener.setmCallBack(null);
            listener = null;
        }
    }

    /**
     * 获取精彩游戏曝光数据
     **/
    public List<ExposureBean> getWonderfulExposureBeans() {
        List<ExposureBean> realwonderfulExposure = new ArrayList<>();
        realwonderfulExposure.clear();
        int subViewCount = mWonderfulContainer.getChildCount();
        if (subViewCount > 0) {
            for (int i = 0; i < subViewCount; i++) {
                if (i >= wonderfulExposureBeans.size()) return realwonderfulExposure;
                ItemWonderfulView view = (ItemWonderfulView) mWonderfulContainer.getChildAt(i);
                if (CommonUtils.getHorizontalScrollViewVisible(view)) {
                    ExposureBean bean = wonderfulExposureBeans.get(i);
                    if (realwonderIds.contains(bean.appId)) {
                        continue;
                    }
                    realwonderfulExposure.add(bean);
                    realwonderIds.add(bean.appId);
                }
            }
        }
        return realwonderfulExposure;
    }

    /**
     * 获取玩家福利曝光数据
     **/
    public List<ExposureBean> getWelfareExposureBeans() {
        List<ExposureBean> list = new ArrayList<>();
        int count = mWelfareContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            if (CommonUtils.isVisibleGamepageView(mWelfareContainer.getChildAt(i))) {
                ExposureBean bean = welfareExposureBeans.get(i);
                if (realwelfareIds.contains(bean.appId)) continue;
                list.add(bean);
                realwelfareIds.add(bean.appId);
            }
        }
        return list;
    }

//    /**
//     * 判断玩家福利界面是否可见
//     *
//     * @return boolean
//     */
//    public boolean ismWelfareViewVisible() {
//        return mWelfareContainer != null && CommonUtils.isVisibleGamepageView(mWelfareContainer);
//    }

    /**
     * 获取新游尝鲜曝光数据
     **/
    public List<ExposureBean> getGamecommentExposureBeans() {
        List<ExposureBean> realcommentExposure = new ArrayList<>();
        realcommentExposure.clear();
        int position = mCommentsGallery.getCurrentItem();
        if (gamecommentExposureBeans == null || gamecommentExposureBeans.size() <= 0) {
            return realcommentExposure;
        }
        int size = gamecommentExposureBeans.size();
        if (JLog.isDebug) {
            JLog.i("GameListHeaderView", "getGamecommentExposureBeans-size=" + size + "--position=" + position);
        }
        if (!realcommentIds.contains(gamecommentExposureBeans.get((position + size - 1) % size).appId)) {
            realcommentExposure.add(gamecommentExposureBeans.get((position + size - 1) % size));
            realcommentIds.add(gamecommentExposureBeans.get((position + size - 1) % size).appId);
        }
        if (!realcommentIds.contains(gamecommentExposureBeans.get((position + size) % size).appId)) {
            realcommentExposure.add(gamecommentExposureBeans.get((position + size) % size));
            realcommentIds.add(gamecommentExposureBeans.get((position + size) % size).appId);
        }
        if (!realcommentIds.contains(gamecommentExposureBeans.get((position + size + 1) % size).appId)) {
            realcommentExposure.add(gamecommentExposureBeans.get((position + size + 1) % size));
            realcommentIds.add(gamecommentExposureBeans.get((position + size + 1) % size).appId);
        }
        if (JLog.isDebug) {
            JLog.i("GameListHeaderView", "getGamecommentExposureBeans-realcommentExposure=" + realcommentExposure);
        }
        return realcommentExposure;
    }

    /**
     * 精彩游戏
     *
     * @param size 精彩游戏的size
     */
    private void addWonderfulCard(int size) {
        LayoutParams params = new LayoutParams(
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 90),
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 140));
        params.setMargins(0, 0, 20, 0);
        mWonderfulContainer.removeAllViews();
        if (wonderfulExposureBeans != null) {
            wonderfulExposureBeans.clear();
        }
        for (int i = 0; i < size; i++) {
            ItemWonderfulView gridViewItem = new ItemWonderfulView(mContext);
            AppsItemBean bean = mGameGatherData.wonderful.apps.get(i);
            bean = CommonUtils.formatAppPageInfo(bean, Constants.GAME_GUI, "gather", i + 1);
            gridViewItem.setData(bean);
            ExposureBean exposureBean = CommonUtils.formNewPagerExposure(bean, Constants.GAME_GUI, "gather");
            if (!wonderfulExposureBeans.contains(exposureBean)) {
                wonderfulExposureBeans.add(exposureBean);
            }
            int topBgColorId = mContext.getResources().getIdentifier("wonderful_top_bg" + i % 5, "color", mContext.getPackageName());
            int bottomBgColorId = mContext.getResources().getIdentifier("wonderful_bottom_bg" + i % 5, "color", mContext.getPackageName());
            gridViewItem.setBackgroundColor(topBgColorId, bottomBgColorId);

            gridViewItem.setLayoutParams(params);
            gridViewItem.setTag(i);
            gridViewItem.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (CommonUtils.isFastDoubleClick())
                        return;
                    int position = (int) v.getTag();
                    if (mGameGatherData.wonderful.apps.get(position) != null) {
                        AppsItemBean bean = mGameGatherData.wonderful.apps.get(position);
                        UIUtils.gotoAppDetail(bean, bean.id, mContext);
                        MTAUtil.onDetailClick(mContext, bean.name,
                                bean.packageName);
                        MTAUtil.onWonderfuClick(position + 1);
                    }
                }
            });
            mWonderfulContainer.addView(gridViewItem);
        }
    }


    /**
     * 设置玩家福利数据
     */
    private void addWelfareItem() {
        if (mGameGatherData == null || mGameGatherData.gamewelfare == null || mGameGatherData.gamewelfare.welfares == null)
            return;
        if (mPerson == null) {
            mPerson = CommonUtils.queryUserPerson(mContext);
        }
        mWelfareTitle.setText(mGameGatherData.gamewelfare.title);
        mWelfareTitle.setLetterSpacing(10);
        if (mPerson == null) {
            mWelfareLoginLl.setVisibility(VISIBLE);

            mWelfareLink.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, LoginActivityNew.class);
                    mContext.startActivity(intent);
                    mContext.overridePendingTransition(R.anim.fade_in,
                            R.anim.fade_out);
                }
            });
        } else {
            mWelfareLoginLl.setVisibility(GONE);
        }

        final List<WelfareBean> welfareBeenList = CommonUtils.filterWelfareData2(mContext, mGameGatherData.gamewelfare.welfares);

        mWelfareContainer.removeAllViews();
        if (welfareExposureBeans != null) {
            welfareExposureBeans.clear();
        }
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 80));
        for (int i = 0; i < welfareBeenList.size(); i++) {
            ItemWelfareView welfareView = new ItemWelfareView(mContext);
            final WelfareBean welfareBean = welfareBeenList.get(i);
            AppsItemBean bean = welfareBean.app;
            welfareBean.app = CommonUtils.formatAppPageInfo(bean, Constants.GAME_GUI, "welfare", i + 1);
            ExposureBean exposureBean = CommonUtils.formNewPagerExposure(welfareBean.app, Constants.GAME_GUI, "welfare");
            if (!welfareExposureBeans.contains(exposureBean)) {
                welfareExposureBeans.add(exposureBean);
            }
            if (i > 0) {
                params.setMargins(0, 10, 0, 0);
            }

            welfareView.setLayoutParams(params);
            welfareView.setTag(i);
            welfareView.setData(welfareBean);
            welfareView.setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (CommonUtils.isFastDoubleClick())
                        return;
                    int position = (int) v.getTag();
                    if (welfareBeenList.get(position).app != null) {
                        AppsItemBean bean = welfareBean.app;
                        UIUtils.gotoAppDetail(bean, bean.id, mContext);
                        MTAUtil.onDetailClick(mContext, bean.name,
                                bean.packageName);
                        MTAUtil.onWelfareClick(position + 1);
                    }
                }
            });

            mWelfareContainer.addView(welfareView);
        }
    }

    private static class LocalBroadcastReceiver extends BroadcastReceiver {
        WeakReference<GameListHeaderView> mGameListHeaderView = null;
        WeakReference<Activity> mActivity = null;

        public LocalBroadcastReceiver(Activity mContext, GameListHeaderView instance) {
            this.mActivity = new WeakReference<Activity>(mContext);
            this.mGameListHeaderView = new WeakReference<GameListHeaderView>(instance);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            GameListHeaderView instance = this.mGameListHeaderView.get();
            final Activity mContext = this.mActivity.get();
            if (instance == null || mContext == null || intent.getAction() == null)
                return;
            switch (intent.getAction()) {
                case Constants.ACTION_LOGIN_SUCCESS:
                    instance.mPerson = CommonUtils.queryUserPerson(mContext);
                    instance.mWelfareLoginLl.setVisibility(GONE);

                    break;

                case CloudIntent.ACTION_LOGOUT:
                    instance.mWelfareLoginLl.setVisibility(VISIBLE);
                    instance.mWelfareLink.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, LoginActivityNew.class);
                            mContext.startActivity(intent);
                            mContext.overridePendingTransition(R.anim.fade_in,
                                    R.anim.fade_out);
                        }
                    });

                    break;
            }
        }
    }

    /**
     * 注册本地广播接收者
     */
    private void registerLocalBroadcastReceiver() {
        IntentFilter loginFilter = new IntentFilter(Constants.ACTION_LOGIN_SUCCESS);
        IntentFilter logoutFilter = new IntentFilter(CloudIntent.ACTION_LOGOUT);
        mLocalBroadcastReceiver = new LocalBroadcastReceiver(mContext, this);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, loginFilter);
        mContext.registerReceiver(mLocalBroadcastReceiver, logoutFilter);
    }

    /**
     * 取消本地广播的注册
     */
    private void unRegisterLocalBroadcastReceiver() {
        if (mLocalBroadcastManager != null) {
            if (mLocalBroadcastReceiver != null) {
                mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
                mContext.unregisterReceiver(mLocalBroadcastReceiver);
            }
        }
    }

    private DownDialog mDownDialog;

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public void startAnimation(int state, ImageView imgeView) {
        Activity activity = this.mContext;
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).startAnimation(imgeView);
            }
        }
    }

    private static class MyIUIDownLoadCallBack implements IUIDownLoadListenerImp.IUIDownLoadCallBack {
        WeakReference<GameListHeaderView> mActivities = null;

        public MyIUIDownLoadCallBack(GameListHeaderView intance) {
            mActivities = new WeakReference<GameListHeaderView>(intance);
        }

        @Override
        public void callBack(final String pkgName, int state, boolean isNewDownload) {
            if (JLog.isDebug) {
                JLog.i("long2017", "GameListHeaderView-callBack-mActivity=" + mActivities);

            }
            if (mActivities == null) {
                return;
            }
            final GameListHeaderView intance = mActivities.get();
            if (JLog.isDebug) {
                JLog.i("long2017", "GameListHeaderView-callBack-intance=" + intance);

            }
            if (intance == null) {
                return;
            }
            if (JLog.isDebug) {
                JLog.i("long2017", "GameListHeaderView-callBack-intance.isActivity=" + intance.isActivity);

            }
            if (!intance.isActivity) {
                return;
            }
            intance.mHandler.post(new Runnable() {

                @Override
                public void run() {
                    if (intance.mGameGatherData != null && intance.mGameGatherData.wonderful != null
                            && intance.mGameGatherData.wonderful.apps != null && intance.mGameGatherData.wonderful.apps.size() > 0) {
                        int subViewCount = intance.mWonderfulContainer.getChildCount();
                        if (subViewCount > 0) {
                            for (int i = 0; i < subViewCount; i++) {
                                ItemWonderfulView view = (ItemWonderfulView) intance.mWonderfulContainer.getChildAt(i);
                                AppsItemBean bean = intance.mGameGatherData.wonderful.apps.get(i);
                                if (bean.packageName.equals(pkgName)) {
                                    view.setState(intance.mGameGatherData.wonderful.apps.get(i));
                                }
                            }
                        }
                    }

                    if (intance.mGameGatherData != null && intance.mGameGatherData.gamewelfare != null
                            && intance.mGameGatherData.gamewelfare.welfares != null && intance.mGameGatherData.gamewelfare.welfares.size() > 0) {
                        int subViewCount = intance.mWelfareContainer.getChildCount();
                        if (subViewCount > 0) {
                            for (int i = 0; i < subViewCount; i++) {
                                ItemWelfareView view = (ItemWelfareView) intance.mWelfareContainer.getChildAt(i);
                                WelfareBean bean = intance.mGameGatherData.gamewelfare.welfares.get(i);
                                if (bean.app.packageName.equals(pkgName)) {
                                    view.getDownLoadView().invalidate();
                                }
                            }
                        }
                    }
                    if (JLog.isDebug) {
                        JLog.i("long2017", "GameListHeaderView-onRefreshUI-mGameGatherData=" + intance.mGameGatherData);

                    }
                    if (JLog.isDebug) {
                        if (intance.mGameGatherData != null && intance.mGameGatherData.gamecomment != null)
                            JLog.i("long2017", "GameListHeaderView-onRefreshUI-mGameGatherData.gamecomment=" + intance.mGameGatherData.gamecomment);

                    }
                    if (JLog.isDebug) {
                        if (intance.mGameGatherData != null && intance.mGameGatherData.gamecomment != null && intance.mGameGatherData.gamecomment.comments != null)
                            JLog.i("long2017", "GameListHeaderView-onRefreshUI-mGameGatherData.gamecomment.comments.size()=" + intance.mGameGatherData.gamecomment.comments.size());

                    }
                    if (intance.mGameGatherData != null && intance.mGameGatherData.gamecomment != null
                            && intance.mGameGatherData.gamecomment.comments != null && intance.mGameGatherData.gamecomment.comments.size() > 0) {
                        int subViewCount = intance.mCommentsGallery.getChildCount();
                        if (subViewCount > 0) {
                            for (int i = 0; i < subViewCount; i++) {
                                View view = intance.mCommentsGallery.getChildAt(i);
                                View subView = view.findViewById(R.id.game_download_btn);
                                if (subView != null) {
                                    subView.postInvalidate();
                                }
                            }
                        }
                    }
                }
            });
        }
    }


    public void onItemClick(HomeAdBean homeAdBean) {
        if (homeAdBean == null || TextUtils.isEmpty(homeAdBean.adType)) return;
        Intent intent = new Intent(mContext, TopicDetailActivity.class);
        switch (homeAdBean.adType) {//adType=net_game
            case "topic":
                com.prize.app.beans.TopicItemBean bean = new com.prize.app.beans.TopicItemBean();
                bean.description = homeAdBean.description;
                bean.title = homeAdBean.title;
                bean.imageUrl = homeAdBean.imageUrl;
                bean.id = homeAdBean.associateId;
                Bundle b = new Bundle();
                b.putSerializable("bean", bean);
                intent.putExtras(b);
                mContext.startActivity(intent);
                break;
            case "net_game":
                intent = new Intent(mContext, OnlineGameListActivity.class);
                intent.putExtra("title", homeAdBean.title);
                mContext.startActivity(intent);
                break;
            case "web":
                if (!TextUtils.isEmpty(homeAdBean.url)) {
                    intent = new Intent(mContext, WebViewActivity.class);
                    intent.putExtra(WebViewActivity.P_URL, homeAdBean.url);
                    intent.putExtra(WebViewActivity.P_APP_ID,
                            homeAdBean.associateId);
                    mContext.startActivity(intent);
                    return;
                }
                break;
            case "giftcenter":// 礼包（2.2版本）
                intent = new Intent(mContext, GiftCenterActivity.class);
                intent.putExtra(GiftCenterActivity.TOPICIDKEY, homeAdBean.associateId);
                mContext.startActivity(intent);
                break;
        }
    }

    private ItemStateChangeCallBack mItemSelectCallBack;

    public void setmItemSelectCallBack(ItemStateChangeCallBack mItemSelectCallBack) {
        this.mItemSelectCallBack = mItemSelectCallBack;
    }
}
