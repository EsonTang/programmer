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
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.WelfareBean;
import com.prize.app.constants.Constants;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.GiftDownloadProgressButton;


/**
 * 类描述：游戏页（玩家福利每个item布局）
 *
 * @author 聂礼刚
 * @version 1.0
 */
public class ItemWelfareView extends RelativeLayout {
    private Activity mContext;
    private TextView title;
    private TextView gameName;
    private ImageView cornerImg;
    private ImageView bigImg;
    private ImageView gameImg;
    private TextView poins;

    // 下载按钮
    private GiftDownloadProgressButton downloadBtn;

    private DownDialog mDownDialog;
    private WelfareBean mWelfareBean;


    public ItemWelfareView(Activity context) {
        super(context);
        mContext = context;
        setBackgroundResource(R.drawable.welfare_item_selector);
        View view = inflate(context, R.layout.welfare_item, this);
        findViewById(view);
    }


    private void findViewById(View view) {
        title = (TextView) view.findViewById(R.id.title);
        gameName = (TextView) view
                .findViewById(R.id.game_name_tv);
        cornerImg = (ImageView) view.findViewById(R.id.welfare_corner);
        bigImg = (ImageView) view.findViewById(R.id.game_big_image);
        gameImg = (ImageView) view.findViewById(R.id.game_iv);
        poins = (TextView) view.findViewById(R.id.points);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);

        downloadBtn = (GiftDownloadProgressButton) view.findViewById(R.id.game_download_btn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GiftDownloadProgressButton downloadBtn = (GiftDownloadProgressButton) v;
                final int state = AIDLUtils.getGameAppState(
                        mWelfareBean.app.packageName, mWelfareBean.app.id,
                        mWelfareBean.app.versionCode);
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
                            mDownDialog = new DownDialog(mContext, R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new DownDialog.OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            //startAnimation(state);
                                            UIUtils.downloadApp(mWelfareBean.app);
                                            break;
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
                    //startAnimation(state);
                }
            }
        });
        downloadBtn.enabelDefaultPress(true);
        downloadBtn.setOnWelfareReceivedListener(new GiftDownloadProgressButton.OnWelfareReceivedListener() {
            @Override
            public void onReceived() {
                poins.setVisibility(GONE);
            }
        });
    }

    public void setData(WelfareBean item) {
        if (item == null)
            return;

        mWelfareBean = item;
        downloadBtn.setGameInfo(mWelfareBean);
        title.setText(mWelfareBean.title);
        ImageLoader.getInstance().displayImage(mWelfareBean.imageUrl, bigImg,
                UILimageUtil.getUILoptions(R.drawable.bg_ad), null);
        String url = TextUtils.isEmpty(mWelfareBean.app.largeIcon) ? mWelfareBean.app.iconUrl : mWelfareBean.app.largeIcon;
        ImageLoader.getInstance().displayImage(url, gameImg, UILimageUtil.getUILoptions(), null);

        if ("gift".equals(item.type)) {
            cornerImg.setImageResource(R.drawable.welfare_gift_corner);
            if (TextUtils.isEmpty(mWelfareBean.giftText)) {
                gameName.setText(mWelfareBean.app.name);
            } else {
                String pointStr = mWelfareBean.app.name + "<font color=#ff7f14>" + mWelfareBean.giftText + "</font>";
                gameName.setText(Html.fromHtml(pointStr));
            }
        } else if ("point".equals(item.type)) {
            cornerImg.setImageResource(R.drawable.welfare_score_corner);
            gameName.setText(mWelfareBean.app.name);
            if (!CommonUtils.IsGotWelfare(mContext, Constants.KEY_WELFARE_GOT + item.app.packageName)) {
                String pointStr = "<font color=#ff7f14>+" + mWelfareBean.points + "</font>";
                poins.setText(Html.fromHtml(pointStr));
            } else {
                poins.setText("");
            }
        }
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public View getDownLoadView() {
        return downloadBtn;
    }
}
