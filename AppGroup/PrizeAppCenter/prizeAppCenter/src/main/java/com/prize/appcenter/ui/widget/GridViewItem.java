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
import com.prize.app.download.AppManagerCenter;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.MainActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.progressbutton.AnimDownloadProgressButton;

public class GridViewItem extends LinearLayout {
	public CustomImageView itemImg;
	public TextView itemName;
	public AnimDownloadProgressButton mProgressNoGiftButton;
	private DownDialog mDownDialog;

	public GridViewItem(Context context) {
		super(context);
		mContext = context;
		setOrientation(VERTICAL);
		setGravity(Gravity.CENTER);
		View view = inflate(context, R.layout.card_item_topic_view, this);
		findViewById(view);
	}

	public GridViewItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setOrientation(VERTICAL);
		View view = inflate(context, R.layout.card_item_topic_view, this);
		findViewById(view);
	}

	private void findViewById(View view) {
		itemImg = (CustomImageView) view.findViewById(R.id.appItem_img_id);
		itemName = (TextView) view.findViewById(R.id.appItem_name_id);
		mProgressNoGiftButton = (AnimDownloadProgressButton) view.findViewById(R.id.progressButton_id);
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
		

        if(itemBean.name.trim().length()>5){
			itemName.setText(itemBean.name.trim().substring(0,5));
		}else{
			itemName.setText(itemBean.name.trim());
		}
		mProgressNoGiftButton.setGameInfo(itemBean);
		mProgressNoGiftButton.enabelDefaultPress(true);
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
											startAnimation(state);
											UIUtils.downloadApp(itemBean);
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
					startAnimation(state);
				}
			}
		});
	}




	public GridViewItem(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	private void dismissDialog() {
		if (mDownDialog != null && mDownDialog.isShowing()) {
			mDownDialog.dismiss();
			mDownDialog = null;
		}
	}

	public void startAnimation(int state) {
		if (state == AppManagerCenter.APP_STATE_UNEXIST
				|| state == AppManagerCenter.APP_STATE_UPDATE) {
			if (mContext instanceof MainActivity) {
				((MainActivity) mContext).startAnimation(itemImg);
			}
		}
	}
}
