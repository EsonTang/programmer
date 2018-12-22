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
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.MTAUtil;
import com.prize.app.util.PrizeStatUtil;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.SearchActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

/**
 * 类描述：搜索结果横向推广item布局
 *
 * @author 龙宝修
 * @version 1.0
 */
public class AdvertisingView extends LinearLayout {
    private ImageView game_iv;
    private TextView game_name_tv;
    private TextView game_size_tv;
    private AnimDownloadProgressButton game_download_btn;
    private AppsItemBean gameBean;
    private DownDialog mDownDialog;

    public AdvertisingView(Context context) {
        this(context, null);
    }

    public AdvertisingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = inflate(context, R.layout.advertising_view, this);
        findViewById(view);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameBean == null || TextUtils.isEmpty(gameBean.id))
                    return;
                MTAUtil.onSearchADAppClick(gameBean.name);
                UIUtils.gotoAppDetail(gameBean, gameBean.id, (Activity) getContext());
            }
        });
    }


    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    /**
     * 刷新item的下载按钮状态
     * @param packageName 包名
     */
    public void refreshBtnState(String packageName) {
        if (gameBean != null && !TextUtils.isEmpty(gameBean.packageName) && packageName.equals(gameBean.packageName) && game_download_btn != null) {
            game_download_btn.invalidate();
        }

    }

    private void findViewById(View view) {
        game_iv = (ImageView) view.findViewById(R.id.game_iv);
        game_name_tv = (TextView) view.findViewById(R.id.game_name_tv);
        game_size_tv = (TextView) view.findViewById(R.id.game_size_tv);
        game_download_btn = (AnimDownloadProgressButton) view.findViewById(R.id.game_download_btn);
        game_download_btn.enabelDefaultPress(true);
        game_download_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameBean == null) return;
                final int state = AIDLUtils.getGameAppState(
                        gameBean.packageName, gameBean.id + "",
                        gameBean.versionCode);
                switch (state) {
                    case AppManagerCenter.APP_STATE_UNEXIST:
                    case AppManagerCenter.APP_STATE_UPDATE:
                    case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
                        if (ClientInfo.getAPNType(getContext()) == ClientInfo.NONET) {
                            ToastUtils.showToast(R.string.nonet_connect);
                            return;
                        }
                }
                if (BaseApplication.isDownloadWIFIOnly()
                        && ClientInfo.getAPNType(getContext()) != ClientInfo.WIFI) {
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
                                            if (getContext() instanceof SearchActivity) {
                                                PrizeStatUtil.onSearchResultItemClick(gameBean.id, gameBean.packageName, gameBean.name, ((SearchActivity) getContext()).getKeyWord(), false);
                                            }
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            ((AnimDownloadProgressButton) v).onClick();
                            break;
                    }

                } else {
                    ((AnimDownloadProgressButton) v).onClick();
                }
            }
        });
    }

    public void setData(AppsItemBean item) {
        gameBean = item;
        String url = TextUtils.isEmpty(item.largeIcon) ? item.iconUrl : item.largeIcon;
        ImageLoader.getInstance().displayImage(url, game_iv, UILimageUtil.getUILoptions());
        game_name_tv.setText(item.name);
        if (null != item.downloadTimesFormat) {
            String user = item.downloadTimesFormat.replace("次", "人");
            game_size_tv.setText(mContext.getString(
                    R.string.person_use, user));
        }
        game_download_btn.setGameInfo(item);
    }

}
