package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

public class DownloadFooterGridViewItem extends LinearLayout {
    private Context mContext;
    public CustomImageView itemImg;
    public TextView itemName;
    public TextView itemSize;
    public AnimDownloadProgressButton itemButton;
    private DownDialog mDownDialog;
    private AppsItemBean appsItemBean;
    private IUIDownLoadListenerImp mListener;
    private Handler mHandler = new Handler();

    public DownloadFooterGridViewItem(Context context) {
        super(context);
        mContext = context;
        inflate(context, R.layout.app_item_view_download, this);
        findViewById();
    }

    public DownloadFooterGridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflate(context, R.layout.app_item_view_download, this);
        findViewById();
    }

    private void findViewById() {
        itemImg = (CustomImageView) findViewById(R.id.appItem_img_id);
        itemName = (TextView) findViewById(R.id.appItem_name_id);
        itemSize = (TextView) findViewById(R.id.appItem_size_id);
        itemButton = (AnimDownloadProgressButton) findViewById(R.id.progressButton_id);
        setListener();
    }

    public void setData(final AppsItemBean itemBean) {
        if (itemBean == null)
            return;
        appsItemBean = itemBean;
        if (!TextUtils.isEmpty(itemBean.largeIcon)) {
            ImageLoader.getInstance().displayImage(itemBean.largeIcon, itemImg,
                    UILimageUtil.getUILoptions(), null);
        } else {

            if (itemBean.iconUrl != null) {
                ImageLoader.getInstance().displayImage(itemBean.iconUrl,
                        itemImg, UILimageUtil.getUILoptions(), null);
            }
        }
//        if(itemBean.name.trim().length()>5){
//            itemName.setText(itemBean.name.trim().substring(0,4)+"...");
//        }else{
        itemName.setText(itemBean.name.trim());
//        }
        if (null != itemBean.downloadTimesFormat) {
            String user = itemBean.downloadTimesFormat.replace("次", "人");
            itemSize.setText(BaseApplication.curContext.getString(
                    R.string.person_use, user));
        }
        itemButton.setGameInfo(itemBean);
    }

    private void setListener() {

        mListener = IUIDownLoadListenerImp.getInstance();
        mListener.setmCallBack(new IUIDownLoadListenerImp.IUIDownLoadCallBack() {

            @Override
            public void callBack(String pkgName, int state, boolean isNewDownload) {
                if (itemButton == null)
                    return;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        itemButton.invalidate();
                    }
                });

            }
        });
        AIDLUtils.registerCallback(mListener);
        itemButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (appsItemBean == null)
                    return;
                final int state = AIDLUtils.getGameAppState(
                        appsItemBean.packageName, appsItemBean.id + "",
                        appsItemBean.versionCode);
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
                            mDownDialog = new DownDialog(mContext,
                                    R.style.add_dialog);
                            mDownDialog.show();
                            mDownDialog.setmOnButtonClic(new OnButtonClic() {

                                @Override
                                public void onClick(int which) {
                                    dismissDialog();
                                    switch (which) {
                                        case 0:
                                            break;
                                        case 1:
                                            //startAnimation(state);
                                            UIUtils.downloadApp(appsItemBean);
                                            break;
                                    }
                                }
                            });
                            break;
                        default:
                            itemButton.onClick();
                            break;
                    }

                } else {
                    itemButton.onClick();
                }

            }
        });
        itemButton.enabelDefaultPress(true);
    }

    //	public void startAnimation(int state) {
    //		if (state == AppManagerCenter.APP_STATE_UNEXIST
    //				|| state == AppManagerCenter.APP_STATE_UPDATE) {
    //			if (mContext instanceof MainActivity) {
    //				((MainActivity) mContext).startAnimation(itemImg);
    //			}
    //		}
    //	}

    private void dismissDialog() {
        if (mDownDialog != null && mDownDialog.isShowing()) {
            mDownDialog.dismiss();
            mDownDialog = null;
        }
    }

    public DownloadFooterGridViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    /**
     * 设置刷新handler,Activity OnResume 时调用
     */
    public void setDownlaodRefreshHandle() {
        AIDLUtils.registerCallback(mListener);
    }


    public void unBindregisterCallback() {
        AIDLUtils.unregisterCallback(mListener);
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);

        }
        if (mListener != null) {
            mListener.setmCallBack(null);
            mListener = null;
        }
    }
}
