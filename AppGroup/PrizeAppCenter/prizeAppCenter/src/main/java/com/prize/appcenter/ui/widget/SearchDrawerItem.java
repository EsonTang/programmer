package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
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
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

/**
 * 首页头布局的横向3个和抽屉布局共用
 */
public class SearchDrawerItem extends LinearLayout {
    public CustomImageView itemImg;
    public TextView itemName;
    private TextView game_size_tv;
    public AnimDownloadProgressButton mProgressNoGiftButton;
    private DownDialog mDownDialog;
    private int position;

    /**
     * 构造方法
     *
     * @param context  上下文
     * @param position 列表依附的位置
     */
    public SearchDrawerItem(Context context, int position) {
        super(context);
        mContext = context;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        View view = inflate(context, R.layout.search_drawer_itemlayout, this);
        this.position = position;
        findViewById(view);
    }

    private void findViewById(View view) {
        itemImg = (CustomImageView) view.findViewById(R.id.game_iv);
        itemName = (TextView) view.findViewById(R.id.game_name_tv);
        game_size_tv = (TextView) view.findViewById(R.id.game_size_tv);
        mProgressNoGiftButton = (AnimDownloadProgressButton) view.findViewById(R.id.game_download_btn);
    }

    public void setData(final AppsItemBean itemBean) {
        if (itemBean == null)
            return;
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, itemImg,
                    UILimageUtil.getUILoptions(), null);
        } else {
            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        itemImg, UILimageUtil.getUILoptions(), null);
            }
        }
        game_size_tv.setText(itemBean.apkSizeFormat);
        itemName.setText(itemBean.name.trim());
        mProgressNoGiftButton.enabelDefaultPress(true);
        mProgressNoGiftButton.setGameInfo(CommonUtils.formatAppPageInfo(itemBean, Constants.SEARCH_RESULT_GUI, Constants.DRAWER, position));
        mProgressNoGiftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimDownloadProgressButton downloadBtn = (AnimDownloadProgressButton) v;
                final int state = AIDLUtils.getGameAppState(
                        itemBean.packageName, itemBean.id,
                        itemBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        MTAUtil.onSearchDrawerClick(position);
                        if (ClientInfo.getAPNType(BaseApplication.curContext) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                        if (BaseApplication.isDownloadWIFIOnly()
                                && ClientInfo.getAPNType(BaseApplication.curContext) != ClientInfo.WIFI) {
//                            mDownDialog = new DownDialog(mContext, R.style.add_dialog);
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
                                            UIUtils.downloadApp(itemBean);
                                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                                            }
                                            break;
                                    }
                                }
                            });
                        } else {
                            downloadBtn.onClick();
                            if (state == AppManagerCenter.APP_STATE_UNEXIST) {
//                                PrizeStatUtil.onClickBackParams(itemBean.backParams, itemBean.name, itemBean.packageName);
                                AIDLUtils.upload360ClickDataNow(itemBean.backParams, itemBean.name, itemBean.packageName);
                            }
                        }

                        break;
                    default:
                        downloadBtn.onClick();
                        break;
                }
            }
        });
    }

    public SearchDrawerItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }
}
