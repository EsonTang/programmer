package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.DrawerData;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.GsonParseUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.activity.OnlineGameListActivity;
import com.prize.appcenter.callback.OnNewDownloadListener;
import com.prize.appcenter.receiver.PrizeXutilStringCallBack;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;
import com.prize.custmerxutils.XExtends;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import java.util.List;

public class ListViewItem extends LinearLayout {
    private ImageView mIcon;
    private TextView name;
    private TextView size;
    private AnimDownloadProgressButton downloadBtn;
    private RelativeLayout game_download_Rlyt;
    private TextView game_brief;
    private FlowLayout tag_container;
    private FlowLayout ourtag_container;
    private Activity mContext;
    private DownDialog mDownDialog;
    private LayoutParams param;
    private LayoutParams param2;
    private View.OnClickListener mOnClickListener;
    private RelativeLayout mDrawerContainer; //抽屉布局
    private LinearLayout.LayoutParams params1;

    public ListViewItem(Activity context) {
        super(context);
        mContext = context;
        setOrientation(VERTICAL);
        View view = inflate(context, R.layout.hasgift_listview_item, this);
        findViewById(view);
    }

    public ListViewItem(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.hasgift_listview_item, this);
        findViewById(view);
    }

    private void findViewById(View view) {
        mIcon = (ImageView) view.findViewById(R.id.game_iv);
        name = (TextView) view.findViewById(R.id.game_name_tv);
        game_download_Rlyt = (RelativeLayout) view
                .findViewById(R.id.game_download_Rlyt);
        size = (TextView) view.findViewById(R.id.game_size_tv);
        game_brief = (TextView) view.findViewById(R.id.game_brief);
        tag_container = (FlowLayout) view.findViewById(R.id.tag_container);
        ourtag_container = (FlowLayout) view
                .findViewById(R.id.ourtag_container);
        downloadBtn = (AnimDownloadProgressButton) view
                .findViewById(R.id.game_download_btn);
        setListener();
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        params1 = new LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 8, 0);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT, 1.0f);
        mDrawerContainer = (RelativeLayout) view.findViewById(R.id.drawer_container);
    }

    private void setListener() {
        mOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final AppsItemBean itemBean = (AppsItemBean) v.getTag();
                if (mContext instanceof OnlineGameListActivity) {
                    MTAUtil.onLineGamePosition(position);
                }
                final int state = AIDLUtils.getGameAppState(
                        itemBean.packageName, itemBean.id + "",
                        itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.networkType == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                        if (state == AppManagerCenter.APP_STATE_UNEXIST) {
                            if (mContext instanceof MainActivity) {
                                if (isHome) {
                                    MTAUtil.onclickhomePositionlist(position);
                                    MTAUtil.onUMclickhomePositionlist(position);//
                                    MTAUtil.onclickhomelist(itemBean.name, itemBean.packageName);
                                    MTAUtil.onUMclickhomelist(itemBean.name, itemBean.packageName);
                                } else {
                                    if (isGame) {
                                        MTAUtil.onclickGamePositionlist(position);
                                        MTAUtil.onUMclickGamePositionlist(position);
                                        MTAUtil.onclickGamelist(itemBean.name, itemBean.packageName);
                                        MTAUtil.onUMclickGamelist(itemBean.name, itemBean.packageName);
                                    } else {
                                        MTAUtil.onclickAppPositionlist(position);
                                        MTAUtil.onUMclickAppPositionlist(position);
                                        MTAUtil.onclickApplist(itemBean.name, itemBean.packageName);
                                        MTAUtil.onUMclickApplist(itemBean.name, itemBean.packageName);
                                    }
                                }
                            }
                        }
                        if (BaseApplication.isDownloadWIFIOnly() && ClientInfo.networkType != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mContext, R.style.add_dialog);
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
                                            startAnimation(state);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST && mContext != null && mContext instanceof MainActivity) {
                                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                                if (mDrawerContainer != null && mDrawerContainer.getChildCount() > 0)
                                                    break;
                                                requestDrawerData(itemBean.id, itemBean.name, position);
                                            }
                                            UIUtils.downloadApp(itemBean);
                                            break;
                                    }
                                }
                            });

                        } else {
                            downloadBtn.onClick();
                            startAnimation(state);
                            if (state == AppManagerCenter.APP_STATE_UNEXIST && mContext != null && mContext instanceof MainActivity) {
                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                if (mDrawerContainer != null && mDrawerContainer.getChildCount() > 0)
                                    return;
                                requestDrawerData(itemBean.id, itemBean.name, position);
                            }
                        }

                        break;


                    default://不是需要下载，继续 更新的条件
                        downloadBtn.onClick();
                        break;
                }
            }
        };

    }

    private boolean isGame = false;
    private boolean isHome = false;
    private int position = 0;

    /***
     * 设置数据（第2，第3,第4个参数是为了区分MTA统计）
     *
     * @param itemBean AppsItemBean
     * @param isGame   是否是游戏界面
     * @param isHome   是否是首页
     * @param position 在list所在位置
     */

    public void setData(final AppsItemBean itemBean, boolean isGame, boolean isHome, int position) {
        this.isGame = isGame;
        this.isHome = isHome;
        this.position = position;
        name.setLayoutParams(param);
        ourtag_container.setVisibility(View.GONE);
        ourtag_container.removeAllViews();
//        AppsItemBean itemBean = CommonUtils.formatAppPageInfo(inItemBean, Constants.HOME_GUI, Constants.LIST, position);
        if (TextUtils.isEmpty(itemBean.customTags)) {
            if (!TextUtils.isEmpty(itemBean.ourTag) || itemBean.giftCount > 0) {
                name.setLayoutParams(param2);
                ourtag_container.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(itemBean.ourTag)) {
                    String[] tags = null;
                    if (itemBean.ourTag.contains(",")) {
                        tags = itemBean.ourTag.split(",");

                    } else {
                        tags = new String[]{itemBean.ourTag};
                    }
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 2 ? 2 : size;
                        TextView tagView;
                        for (int i = 0; i < requireLen; i++) {
                            if (TextUtils.isEmpty(tags[i])) {
                                continue;
                            }
                            tagView = (TextView) LayoutInflater.from(mContext)
                                    .inflate(R.layout.item_textview, null);
                            tagView.setText(tags[i]);
                            tagView.setTextColor(this.mContext.getResources()
                                    .getColor(R.color.text_color_009def));
                            tagView.setBackgroundResource(R.drawable.bg_list_tag);
                            tagView.setLayoutParams(params1);
                            ourtag_container.addView(tagView);
                        }
                    }

                }
                if (itemBean.giftCount > 0) {
                    TextView tagView = (TextView) LayoutInflater.from(mContext)
                            .inflate(R.layout.item_textview, null);
                    tagView.setText(R.string.gamedetail_gift_title);
                    tagView.setTextColor(this.mContext.getResources()
                            .getColor(R.color.text_color_ff9732));
                    tagView.setBackgroundResource(R.drawable.bg_list_tag_gift);
                    tagView.setLayoutParams(params1);
                    ourtag_container.addView(tagView);
                }
            }
        } else {
            name.setLayoutParams(param2);
            ourtag_container.setVisibility(View.VISIBLE);
            TextView tagView = (TextView) LayoutInflater.from(this.mContext)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(itemBean.customTags);
            tagView.setGravity(Gravity.CENTER);
            tagView.setTextColor(this.mContext.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
        }


        game_download_Rlyt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                downloadBtn.performClick();

            }
        });
        downloadBtn.enabelDefaultPress(true);
        downloadBtn.setOnClickListener(mOnClickListener);
        downloadBtn.setTag(itemBean);
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, mIcon,
                    UILimageUtil.getUILoptions(), null);
//            ImageLoader.getInstance().displayImage("http://cdnimages.img-cn-hangzhou.aliyuncs.com/appstore/appinfo/icons/1514880918628.jpg", mIcon,
//                    UILimageUtil.getUILoptions(), null);
        } else {
            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl, mIcon,
                        UILimageUtil.getUILoptions(), null);
            }
        }
        if (itemBean.name != null) {
            name.setText(itemBean.name);
        }
        size.setText(itemBean.apkSizeFormat);
        if (!TextUtils.isEmpty(itemBean.brief)) {
            game_brief.setVisibility(View.VISIBLE);
            game_brief.setText(itemBean.brief);
            tag_container.setVisibility(View.GONE);
            game_brief.setCompoundDrawablePadding(0);
            game_brief.setTextColor(mContext.getResources().getColor(
                    R.color.text_color_6c6c6c));
        } else {
            if (!TextUtils.isEmpty(itemBean.categoryName)
                    || !TextUtils.isEmpty(itemBean.tag)) {
                game_brief.setVisibility(View.GONE);
                // 添加标签
                tag_container.setVisibility(View.VISIBLE);
                tag_container.removeAllViews();
                LinearLayout.LayoutParams params = new LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                int rightMargin = this.mContext.getResources()
                        .getDimensionPixelSize(R.dimen.flow_rightMargin);
                params.setMargins(0, rightMargin, 12, 0);
                TextView tagView1 = (TextView) LayoutInflater.from(mContext)
                        .inflate(R.layout.item_textview, null);
                tagView1.setText(itemBean.categoryName);
                tagView1.setLayoutParams(params);
                tag_container.addView(tagView1);
                if (!TextUtils.isEmpty(itemBean.tag)) {
                    String[] tags = itemBean.tag.split(" ");
                    if (tags != null && tags.length > 0) {
                        int size = tags.length;
                        int requireLen = size > 3 ? 3 : size;
                        for (int i = 0; i < requireLen; i++) {
                            if (!TextUtils.isEmpty(itemBean.categoryName)
                                    && itemBean.categoryName.equals(tags[i])) {
                                continue;
                            }
                            TextView tagView = (TextView) LayoutInflater.from(
                                    mContext).inflate(R.layout.item_textview,
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
        downloadBtn.setGameInfo(itemBean);
    }

    public ListViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void startAnimation(int state) {
        if (state == AppManagerCenter.APP_STATE_UNEXIST
                || state == AppManagerCenter.APP_STATE_UPDATE) {
            if (mContext instanceof MainActivity) {
                ((MainActivity) mContext).startAnimation(mIcon);
            }
        }
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    /**
     * 请求抽屉数据
     *
     * @param appId   应用id
     * @param appName 包名
     */
    private void requestDrawerData(final String appId, final String appName, final int position) {
        RequestParams params = new RequestParams(Constants.GIS_URL + "/category/recommendList");
        params.addBodyParameter("appId", appId);
        Callback.Cancelable mCancelable = XExtends.http().post(params, new PrizeXutilStringCallBack<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject o = new JSONObject(result);
                    int code = o.getInt("code");
                    if (0 == code) {
                        String res = o.getString("data");
                        DrawerData data = GsonParseUtils.parseSingleBean(res, DrawerData.class);
                        if (data != null) {
                            List<AppsItemBean> drawerData = CommonUtils.getDrawerData(data);
                            if (mListener != null) {
                                mListener.onDownLoad(appId, appName, drawerData, position);
                                if (isHome) {
                                    MTAUtil.homeDrawerViewShow();
                                } else {
                                    if (isGame) {
                                        MTAUtil.gameDrawerViewShow();
                                    } else {
                                        MTAUtil.appDrawerViewShow();
                                    }
                                }
                            }
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

        });

        if (mListener != null) {
            mListener.onRequestDrawerData(mCancelable);
        }

    }


    public RelativeLayout getDrawerContainer() {
        return mDrawerContainer;
    }


    private OnNewDownloadListener mListener;

    public void setNewDownloadListener(OnNewDownloadListener listener) {
        mListener = listener;
    }
}
