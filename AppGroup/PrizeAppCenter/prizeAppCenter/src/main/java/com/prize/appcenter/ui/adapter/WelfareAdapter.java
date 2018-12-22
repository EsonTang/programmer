package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.download.IUIDownLoadListenerImp.IUIDownLoadCallBack;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.app.util.PreferencesUtils;
import com.prize.appcenter.R;
import com.prize.app.beans.WelfareBean;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ProgressNoGiftButton;

import java.util.List;

/**
 * 
 ** 
 * 游戏页玩家福利adapter
 * 
 * @author nieligang
 * @version V1.0
 */
public class WelfareAdapter extends BaseAdapter {

	private IUIDownLoadListenerImp listener = null;
	private AppsItemBean appItem;
	private List<WelfareBean> mDatas;
	private Context mContext;
	private boolean mIsLogin;
	protected Handler mHandler;
	private ListView mListView;
	private DownDialog mDownDialog;


	public WelfareAdapter(Context context, List<WelfareBean> items,  boolean isLogin) {
		mDatas = items;
		mContext = context;
		mHandler = new Handler();
		mIsLogin = isLogin;

		listener = IUIDownLoadListenerImp.getInstance();
		listener.setmCallBack(new IUIDownLoadCallBack() {

			@Override
			public void callBack(final String pkgName, int state,boolean isNewDownload) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						updateView(pkgName);
						
					}
				});
			}
		});
	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AIDLUtils.unregisterCallback(listener);
		mHandler.removeCallbacksAndMessages(null);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AIDLUtils.registerCallback(listener);
	}

	@Override
	public int getCount() {
		if (mDatas == null) {
			return 0;
		}
		if (mDatas.size() > 3) {
			return 3;
		}
		return mDatas.size();
	}

	@Override
	public WelfareBean getItem(int position) {
		return mDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		mListView = (ListView) parent;

		final WelfareBean welfareBean = mDatas.get(position);
		boolean welfareGot = PreferencesUtils.getBoolean(mContext, welfareBean.app.packageName, false);
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.welfare_item, null);
			viewHolder.title = (TextView) convertView.findViewById(R.id.title);
			viewHolder.gameName = (TextView) convertView
					.findViewById(R.id.game_name_tv);
			viewHolder.cornerImg = (ImageView) convertView.findViewById(R.id.welfare_corner);
			viewHolder.bigImg = (ImageView) convertView.findViewById(R.id.game_big_image);
			viewHolder.gameImg = (ImageView) convertView.findViewById(R.id.game_iv);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

			viewHolder.downloadBtn = (ProgressNoGiftButton) convertView
					.findViewById(R.id.game_download_btn);
			viewHolder.downloadBtn
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							ProgressNoGiftButton downloadBtn = (ProgressNoGiftButton) v;
							final int state = AIDLUtils.getGameAppState(
									welfareBean.app.packageName, welfareBean.app.id,
									welfareBean.app.versionCode);
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
														UIUtils.downloadApp(welfareBean.app);
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

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.downloadBtn.setGameInfo(welfareBean.app);
		viewHolder.title.setText(welfareBean.title);
		ImageLoader.getInstance().displayImage(welfareBean.imageUrl, viewHolder.bigImg,
				UILimageUtil.getUILoptions(), null);
		ImageLoader.getInstance().displayImage(welfareBean.app.largeIcon, viewHolder.gameImg,
				UILimageUtil.getUILoptions(), null);

		if(welfareBean.app.giftCount > 0){
			if(!welfareGot) {
				viewHolder.cornerImg.setImageResource(R.drawable.welfare_gift_corner);
			}
			viewHolder.gameName.setText(welfareBean.giftText);
		}else {
			if(!welfareGot) {
				viewHolder.cornerImg.setImageResource(R.drawable.welfare_score_corner);
			}
			viewHolder.gameName.setText(welfareBean.app.name);
		}

		return convertView;

	}

	private class ViewHolder {
		public TextView title;
		public TextView gameName;
		public ImageView cornerImg;
		public ImageView bigImg;
		public ImageView gameImg;

		// 下载按钮
		ProgressNoGiftButton downloadBtn;
	}

	private void dismissDialog() {
		if (mDownDialog != null && mDownDialog.isShowing()) {
			mDownDialog.dismiss();
			mDownDialog = null;
		}
	}

	private void updateView(String packageName) {
		if (mListView == null)
			return;
		//得到第一个可显示控件的位置，
		int visiblePosition = mListView.getFirstVisiblePosition();
		int headerViewsCount = mListView.getHeaderViewsCount();
		int LastVisiblePosition = mListView.getLastVisiblePosition();

		for (int i = visiblePosition-headerViewsCount; i <= LastVisiblePosition-headerViewsCount; i++) {
			WelfareBean bean = mDatas.get(i);
			if(bean.app.packageName.equals(packageName)) {
				mListView.getChildAt(i).findViewById(R.id.game_download_btn).invalidate();
			}
		}
	}
}
