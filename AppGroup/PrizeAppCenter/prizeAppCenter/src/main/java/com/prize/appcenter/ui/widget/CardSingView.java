/*******************************************
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

package com.prize.appcenter.ui.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.net.datasource.home.CarParentBean;
import com.prize.app.util.DisplayUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

/**
 * 类描述：首页单个应用card
 *
 * @author 龙宝修
 * @version 1.0
 */
public class CardSingView extends LinearLayout {
    private DownDialog mDownDialog;
    protected LayoutParams param;
    protected LayoutParams param2;
    private ImageView mIcon;
    private ImageView game_iv;
    private TextView game_name_tv;
    private TextView download_count_tv;
    private TextView game_size_tv;
    private FlowLayout ourtag_container;
    private AnimDownloadProgressButton game_download_btn;

    public CardSingView(Activity context) {
        super(context);
        mContext = context;
        param = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        param2 = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, 1.0f);
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(VERTICAL);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        int padding = (int) DisplayUtil.forMatSpAndDp(TypedValue.COMPLEX_UNIT_DIP, 13);
        setPadding(padding, 0, padding, 0);
        View view = inflate(context, R.layout.card_singview, this);
        findViewById(view);
    }

    public CardSingView(Activity context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = inflate(context, R.layout.card_singview, this);
        findViewById(view);
    }

    private void findViewById(View view) {
        mIcon = (ImageView) findViewById(R.id.card_listview_icon_id);
        game_iv = (ImageView) findViewById(R.id.game_iv);
        game_name_tv = (TextView) findViewById(R.id.game_name_tv);
        download_count_tv = (TextView) findViewById(R.id.download_count_tv);
        game_size_tv = (TextView) findViewById(R.id.game_size_tv);
        ourtag_container = (FlowLayout) findViewById(R.id.ourtag_container);
        game_download_btn = (AnimDownloadProgressButton) findViewById(R.id.game_download_btn);
    }

    private AppsItemBean gameBean;

    public void setData(CarParentBean bean) {
        if (bean == null) return;
        mIcon.setDrawingCacheEnabled(true);
        ImageLoader.getInstance().displayImage(bean.focus.imageUrl, mIcon, UILimageUtil
                .getUILoptions(R.drawable.topic_icon_background), null);
        if (bean.focus == null || bean.focus.app == null)
            return;
        gameBean = bean.focus.app;
        if (TextUtils.isEmpty(gameBean.name)) {
            return;
        }
        if (bean == null) return;
        String imgUrl = TextUtils.isEmpty(gameBean.largeIcon) ? gameBean.iconUrl : gameBean.largeIcon;
        ImageLoader.getInstance().displayImage(imgUrl, game_iv, UILimageUtil
                .getNoLoadLoptions());
        game_size_tv.setText(gameBean.apkSizeFormat);
        if (null != gameBean.downloadTimesFormat) {
            String user = gameBean.downloadTimesFormat.replace("次", "人");
            download_count_tv.setText(mContext.getString(
                    R.string.person_use, user));
        }

        game_name_tv.setLayoutParams(param);
        game_name_tv.setText(gameBean.name);
        ourtag_container.setVisibility(View.GONE);
        ourtag_container.removeAllViews();
        if (TextUtils.isEmpty(gameBean.customTags)) {
            if (!TextUtils.isEmpty(gameBean.ourTag) || gameBean.giftCount > 0) {
                LinearLayout.LayoutParams params1 = new LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                game_name_tv.setLayoutParams(param2);
                ourtag_container.setVisibility(View.VISIBLE);
                if (!TextUtils.isEmpty(gameBean.ourTag)) {
                    String[] tags = null;
                    if (gameBean.ourTag.contains(",")) {
                        tags = gameBean.ourTag.split(",");
                    } else {
                        tags = new String[]{gameBean.ourTag};
                    }
                    if (tags != null && tags.length > 0) {
                        params1.setMargins(0, 0, 8, 0);
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
                if (gameBean.giftCount > 0) {
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
            game_name_tv.setLayoutParams(param2);
            ourtag_container.setVisibility(View.VISIBLE);
            TextView tagView = (TextView) LayoutInflater.from(this.mContext)
                    .inflate(R.layout.item_textview, null);
            tagView.setText(gameBean.customTags);
            tagView.setTextColor(this.mContext.getResources().getColor(
                    R.color.text_color_009def));
            tagView.setBackgroundResource(R.drawable.icon_customertag);
            LinearLayout.LayoutParams params1 = new LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(0, 0, 8, 0);
            tagView.setLayoutParams(params1);
            ourtag_container.addView(tagView);
        }

        game_download_btn.setGameInfo(gameBean);
        game_download_btn.enabelDefaultPress(true);
        game_download_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int state = AIDLUtils.getGameAppState(
                        gameBean.packageName, gameBean.id + "",
                        gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:

                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
                    switch (state) {
                        case AppManagerCenter.APP_STATE_UNEXIST:
                        case AppManagerCenter.APP_STATE_UPDATE:
                        case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                            mDownDialog = new DownDialog(getContext(),
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
                                            UIUtils.downloadApp(gameBean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            game_download_btn.onClick();
                            break;
                    }

                } else {
                    game_download_btn.onClick();
                }
            }
        });
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public AppsItemBean getAppBean() {
        return gameBean;
    }

}
