
package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.MTAUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

public class SearchAdverListItem extends LinearLayout {
    private ImageView mIcon;
    private ImageView icon_ad_Iv;
    private TextView name;
    //    private TextView size;
    private TextView downLoadCount;
    private AnimDownloadProgressButton downloadBtn;
    private RelativeLayout game_download_Rlyt;
    private TextView game_brief;
    private FlowLayout tag_container;
    private FlowLayout ourtag_container;
    private Activity mContext;
    private DownDialog mDownDialog;
    private LayoutParams param;
    private LayoutParams param2;
    protected OnClickListener mOnClickListener;

    public SearchAdverListItem(Activity context) {
        super(context);
        mContext = context;
        setOrientation(VERTICAL);
        setBackgroundColor(Color.parseColor("#e7e7e7"));
        View view = inflate(context, R.layout.item_search_adver, this);
        findViewById(view);
    }

    public SearchAdverListItem(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.item_search_adver, this);
        findViewById(view);
    }

    private void findViewById(View view) {
        mIcon = (ImageView) view.findViewById(R.id.game_iv);
        icon_ad_Iv = (ImageView) view.findViewById(R.id.icon_ad_Iv);
        name = (TextView) view.findViewById(R.id.game_name_tv);
        game_download_Rlyt = (RelativeLayout) view
                .findViewById(R.id.game_download_Rlyt);
//        size = (TextView) view.findViewById(R.id.game_size_tv);
        downLoadCount = (TextView) view.findViewById(R.id.download_count_tv);
        game_brief = (TextView) view.findViewById(R.id.game_brief);
        tag_container = (FlowLayout) view.findViewById(R.id.tag_container);
        ourtag_container = (FlowLayout) view
                .findViewById(R.id.ourtag_container);
        downloadBtn = (AnimDownloadProgressButton) view
                .findViewById(R.id.game_download_btn);
        setListener();
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);
    }

    private void setListener() {
        mOnClickListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                final AppsItemBean itemBean = (AppsItemBean) v.getTag();
                if (itemBean == null) return;
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
                        MTAUtil.onSearchResultListClick(position + 1, itemBean.name);//传进来数值是从0开始的
                        if (BaseApplication.isDownloadWIFIOnly()
                                && ClientInfo.networkType != ClientInfo.WIFI) {
                            if (mDownDialog == null) {
                                mDownDialog = new DownDialog(mContext, R.style.add_dialog);
                            }
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            UIUtils.downloadApp(itemBean);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
                                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                            }
                                            break;
                                    }
                                }
                            });

                        } else {
                            downloadBtn.onClick();
                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                            }
                        }
                        break;
                    default:
                        downloadBtn.onClick();
                        break;
                }
            }
        };
    }


    private int position = 0;

    /***
     * 设置数据（第2，第3,第4个参数是为了区分MTA统计）
     *
     * @param itemBean AppsItemBean
     * @param position 在list所在位置
     */

    public void setData(final AppsItemBean itemBean, int position) {
        this.position = position;
        if (position == 2) {
            icon_ad_Iv.setVisibility(View.VISIBLE);
        } else {
            icon_ad_Iv.setVisibility(View.GONE);
        }
        name.setLayoutParams(param);
        ourtag_container.setVisibility(View.GONE);
        ourtag_container.removeAllViews();
        LayoutParams params1 = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, 12, 0);

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
        downloadBtn.setOnClickListener(mOnClickListener);
        downloadBtn.enabelDefaultPress(true);
        downloadBtn.setTag(CommonUtils.formatAppPageInfo(itemBean, Constants.SEARCH_RESULT_GUI, Constants.LIST, position + 1));
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, mIcon,
                    UILimageUtil.getUILoptions(), null);
        } else {

            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl, mIcon,
                        UILimageUtil.getUILoptions(), null);
            }
        }

        if (itemBean.name != null) {
            name.setText(itemBean.name);
        }
//        size.setText(itemBean.apkSizeFormat);
//        if (null != itemBean.downloadTimesFormat) {
//            String user = itemBean.downloadTimesFormat.replace("次", "人");
//            downLoadCount.setText(mContext.getString(
//                    R.string.person_use, user));
//        }
        downLoadCount.setText(itemBean.apkSizeFormat);

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
                LayoutParams params = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
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
        downloadBtn.setGameInfo(CommonUtils.formatAppPageInfo(itemBean, Constants.SEARCH_RESULT_GUI, Constants.LIST, position + 1));
    }

    public SearchAdverListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }


}
