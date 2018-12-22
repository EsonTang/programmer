/*******************************************
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
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.base.PrizeAppsCardData;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DisplayUtil;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.callback.ItemStateChangeCallBack;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.statistics.model.ExposureBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 类描述：应用游戏card（榜单）
 *
 * @author huanglingjun
 * @version 1.0
 */
public class CardRankView extends RelativeLayout {
    private Activity mContext;
    private LinearLayout mContainer;
    private ImageView bg_Iv;
    private TextView title_Tv;
    private TextView go_Tv;
    private OnClickListener mOnClickListener;
    private LinearLayout.LayoutParams params1;
    private Handler mHandler;
    private IUIDownLoadListenerImp listener = null;
    private PrizeAppsCardData.FocusBean mItem;
    private boolean isGame = false;

    public CardRankView(Activity context, boolean isGame) {
        super(context);
        mContext = context;
        this.isGame = isGame;
        View view = inflate(context, R.layout.card_rankview, this);
        findViewById(view);
        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = (int) v.getTag();
                AppsItemBean bean = mItem.apps.get(index);
                UIUtils.gotoAppDetail(bean, bean.id, mContext);
                if (mItem != null && !TextUtils.isEmpty(mItem.cid) && Integer.parseInt(mItem.cid) == 2) {
                    MTAUtil.onClickGameRank(index);
                    MTAUtil.onGamePageFocusClick(mItem.positon);
                } else {
                    MTAUtil.onClickAppRank(index);
                    MTAUtil.onAppPageFocusClick(mItem.positon);
                }
            }
        };
        params1 = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 138));
        params1.setMargins(0, 0, 12, 0);
        mHandler = new Handler();
        listener = IUIDownLoadListenerImp.getInstance();
        listener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {
            @Override
            public void callBack(final String pkgName, int position, boolean isNewDownload) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        if (mItem != null && mItem.apps != null && mItem.apps.size() > 0) {
                            int subViewCount = mContainer.getChildCount();
                            if (subViewCount > 0) {
                                for (int i = 0; i < subViewCount; i++) {
                                    ItemCardRankView view = (ItemCardRankView) mContainer.getChildAt(i);
                                    try {
                                        AppsItemBean bean = mItem.apps.get((Integer) view.findViewById(R.id.game_iv).getTag());
                                        if (bean.packageName.equals(pkgName)) {
                                            view.setData(mItem.apps.get(i));
                                            view.findViewById(R.id.game_iv).setOnClickListener(mOnClickListener);
                                            view.findViewById(R.id.game_iv).setTag(i);
                                        }

                                    }catch (IndexOutOfBoundsException e){
                                        e.printStackTrace();
                                        break;

                                    }
                                }
                            }
                        }
                    }
                });
            }

        });
        AIDLUtils.registerCallback(listener);
        go_Tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.prize.torank");
                boolean isGame = (mItem != null && !TextUtils.isEmpty(mItem.cid) && Integer.parseInt(mItem.cid) == 2);
                intent.putExtra("isPopular", isGame);
                LocalBroadcastManager.getInstance(mContext.getApplicationContext()).sendBroadcast(intent);

                if (isGame) {
                    MTAUtil.onGamePageFocusClick(mItem.positon);
                } else {
                    MTAUtil.onAppPageFocusClick(mItem.positon);
                }
            }
        });
    }

    private ItemStateChangeCallBack mItemSelectCallBack;

    public void setmItemSelectCallBack(ItemStateChangeCallBack mItemSelectCallBack) {
        this.mItemSelectCallBack = mItemSelectCallBack;
    }

    /**
     * 实际榜单曝光数据
     **/
    private List<ExposureBean> realRankExposure = new ArrayList<>();

    private void findViewById(View view) {
        mContainer = (LinearLayout) view.findViewById(R.id.child_id);
        bg_Iv = (ImageView) view.findViewById(R.id.bg_Iv);
        title_Tv = (TextView) view.findViewById(R.id.title_Tv);
        go_Tv = (TextView) view.findViewById(R.id.go_Tv);
        PrizeHorizontalScrollView horizatal_id = (PrizeHorizontalScrollView) view.findViewById(R.id.horizatal_id);
        horizatal_id.setOnScrollChangedListener(new PrizeHorizontalScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int scrollType) {
                if (mContainer != null && PrizeHorizontalScrollView.IDLE == scrollType) {
                    //此时去计算
                    if (mItemSelectCallBack != null) {
                        mItemSelectCallBack.OnItemSelect(getrealRankExposure(), isGame);
                    }
                }
            }
        });
    }

    /**
     * 获取榜单可见曝光数据
     **/
    private List<ExposureBean> getrealRankExposure() {
        realRankExposure.clear();
        realRankExposure.add(allRankExposure.get(0));
        int subViewCount = mContainer.getChildCount();
        if (subViewCount > 0) {
            for (int i = 0; i < subViewCount; i++) {//此处要获取标题ExposureBean
                ItemCardRankView view = (ItemCardRankView) mContainer.getChildAt(i);
                if (CommonUtils.getHorizontalScrollViewVisible(view)) {
                    ExposureBean bean = allRankExposure.get(i + 1);
                    realRankExposure.add(bean);
                }
            }
        }
        return realRankExposure;
    }

    private List<ExposureBean> allRankExposure = new ArrayList<>();

    public void setData(PrizeAppsCardData.FocusBean item, int position) {
        mItem = item;
        ImageLoader.getInstance().displayImage(item.imageUrl, bg_Iv, UILimageUtil.getUILoptions(R.drawable.bg_ad));
        if (item == null || item.apps == null) {
            return;
        }

        ExposureBean bean1 = new ExposureBean();
        if (isGame) {
            bean1.gui = Constants.GAME_GUI;
        } else {
            bean1.gui = Constants.APP_GUI;
        }
        bean1.widget = Constants.LIST;
//        bean1.position = String.valueOf(position);
        bean1.type = "focus";
        bean1.datas = String.valueOf(item.id);
        bean1.title = item.title;
        if (!allRankExposure.contains(bean1)) {
            allRankExposure.add(bean1);
        }
        title_Tv.setText(item.title);
        int size = item.apps.size();
        int subViewCount = mContainer.getChildCount();
        if (subViewCount > 0) {
            for (int i = 0; (i < subViewCount) && (i < size); i++) {
                ItemCardRankView view = (ItemCardRankView) mContainer.getChildAt(i);
                AppsItemBean itemBean = item.apps.get(i);
                view.setData(itemBean);
                view.findViewById(R.id.game_iv).setOnClickListener(mOnClickListener);
                view.findViewById(R.id.game_iv).setTag(i);
            }
        } else {

            float containerWidth = ClientInfo.getInstance().screenWidth - (DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 102));
            double count = (double) containerWidth / (DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 76) + 12);
            int realCount = (int) Math.ceil(count);
            realRankExposure.add(allRankExposure.get(0));
            for (int i = 0; i < size; i++) {
                ItemCardRankView view = new ItemCardRankView(mContext);
                AppsItemBean bean = item.apps.get(i);
//                if (isGame) {
//                    bean = CommonUtils.formatAppPageInfo(bean, Constants.GAME_GUI, Constants.FOCUS, position);
//                } else {
//                    bean = CommonUtils.formatAppPageInfo(bean, Constants.APP_GUI, Constants.FOCUS, position);
//                }
                view.setData(bean);
                ExposureBean beans = new ExposureBean();
                if (isGame) {
                    beans.gui = Constants.GAME_GUI;
                } else {
                    beans.gui = Constants.APP_GUI;
                }
                beans.widget = Constants.LIST;
                beans.type = "app";
                beans.datas = bean.id;
                beans.title = bean.name;
                if (!TextUtils.isEmpty(bean.backParams)) {
                    beans.backParams = bean.backParams;
                }
//                beans.child_position = String.valueOf(i);
//                beans.position = String.valueOf(position);
                beans.parent_type = "focus";
                beans.parent_datas = String.valueOf(item.id);
                if (!allRankExposure.contains(beans)) {
                    allRankExposure.add(beans);
                }
                if (i < realCount) {
                    if (!realRankExposure.contains(beans)) {
                        realRankExposure.add(beans);
                    }
                }

                view.setLayoutParams(params1);
                view.findViewById(R.id.game_iv).setOnClickListener(mOnClickListener);
                view.findViewById(R.id.game_iv).setTag(i);
                mContainer.addView(view);
            }
            if (mItemSelectCallBack != null) {
                mItemSelectCallBack.OnItemSelect(realRankExposure, isGame);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AIDLUtils.unregisterCallback(listener);
        if (listener != null) {
            listener.setmCallBack(null);
            listener = null;

        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
