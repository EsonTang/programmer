package com.prize.appcenter.ui.adapter;

import android.database.DataSetObserver;
import android.os.Handler;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.BaseApplication;
import com.prize.app.beans.ClientInfo;
import com.prize.app.beans.GameListGiftBean;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.IUIDownLoadListenerImp;
import com.prize.app.net.datasource.base.AppsItemBean;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.RootActivity;
import com.prize.appcenter.ui.dialog.DownDialog;
import com.prize.appcenter.ui.dialog.DownDialog.OnButtonClic;
import com.prize.appcenter.ui.util.AIDLUtils;
import com.prize.appcenter.ui.util.ToastUtils;
import com.prize.appcenter.ui.util.UILimageUtil;
import com.prize.appcenter.ui.util.UIUtils;
import com.prize.appcenter.ui.widget.ProgressButton;

import java.util.ArrayList;

/**
 * 礼包adaptger
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class GameGiftListAdapter extends GameListBaseAdapter {
	private ArrayList<GameListGiftBean> items = new ArrayList<GameListGiftBean>();
	private IUIDownLoadListenerImp listener = null;
	protected RootActivity activity;
	/** 当前页是否处于显示状态 */
	private boolean isActivity = true; // 默认true
	private DownDialog mDownDialog;
	private SubGameGiftListAdapter adapter;

	public GameGiftListAdapter(RootActivity activity) {
		super(activity);
		this.activity = activity;
		mHandler = new Handler();
		listener = new IUIDownLoadListenerImp() {

			@Override
			public void onRefreshUI(String pkgName,int position) {
				if (isActivity) {
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();

						}
					});
				}
			}
		};
	}

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	/**
	 * 设置游戏列表集合,注意直接替换数据类型的,故需要注意数据是在UI线程
	 */
	public void setData(ArrayList<GameListGiftBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	/**
	 * 添加新游戏列表到已有集合中
	 */
	public void addData(ArrayList<GameListGiftBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 * 清空游戏列表
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public GameListGiftBean getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(
					R.layout.gift_listview_item, null);
			viewHolder = new ViewHolder();
			viewHolder.gameIcon = (ImageView) convertView
					.findViewById(R.id.game_iv);
			viewHolder.gameName = (TextView) convertView
					.findViewById(R.id.game_name_tv);
			viewHolder.gameSize = (TextView) convertView
					.findViewById(R.id.apk_size_tv);
			viewHolder.numGift_Tv = (TextView) convertView
					.findViewById(R.id.numGift_Tv);
			viewHolder.game_layout = (LinearLayout) convertView
					.findViewById(R.id.game_layout);
			viewHolder.downloadBtn = (ProgressButton) convertView
					.findViewById(R.id.game_download_btn);
			viewHolder.listView = (ListView) convertView
					.findViewById(android.R.id.list);
			convertView.setTag(viewHolder);
			super.getView(position, convertView, parent);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final AppsItemBean gameBean = getItem(position).app;

		viewHolder.downloadBtn.setGameInfo(gameBean);
		viewHolder.game_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				UIUtils.gotoGameGiftDetai(activity,gameBean.id, position);
			}
		});
		viewHolder.gameIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				UIUtils.gotoAppDetail(gameBean,gameBean.id,activity);
			}
		});
		viewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int state = AIDLUtils.getGameAppState(
						gameBean.packageName, gameBean.id + "",
						gameBean.versionCode);
				switch (state) {
				case AppManagerCenter.APP_STATE_UNEXIST:
				case AppManagerCenter.APP_STATE_UPDATE:

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
						mDownDialog = new DownDialog(activity,
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
									UIUtils.downloadApp(gameBean);
									break;
								}
							}
						});
						break;
					default:
						viewHolder.downloadBtn.onClick();
						break;
					}

				} else {
					viewHolder.downloadBtn.onClick();
				}

			}
		});
		if (!TextUtils.isEmpty(gameBean.largeIcon)) {
			ImageLoader.getInstance().displayImage(gameBean.largeIcon,
					viewHolder.gameIcon, UILimageUtil.getUILoptions(), null);
		} else {

			if (gameBean.iconUrl != null) {
				ImageLoader.getInstance()
						.displayImage(gameBean.iconUrl, viewHolder.gameIcon,
								UILimageUtil.getUILoptions(), null);
			}
		}

		if (gameBean.name != null) {
			viewHolder.gameName.setText(gameBean.name);
		}
		viewHolder.gameSize.setText(gameBean.apkSizeFormat);
		StringBuilder source = new StringBuilder();
		source.append("</font>");
		source.append("<font color='#737373'>");
		source.append(this.activity.getString(R.string.total));
		source.append("</font>");
		source.append("<font color='#ff6262'>");
		source.append(String.valueOf(getItem(position).app.giftCount));
		source.append("</font>");
		source.append("<font color='#737373'>");
		source.append(this.activity.getString(R.string.unit_gift));
		source.append("</font>");
		viewHolder.numGift_Tv.setText(Html.fromHtml(source.toString()));
		adapter = new SubGameGiftListAdapter(this.activity);
		adapter.setData(getItem(position).gifts);
		adapter.setmBean(gameBean);
		viewHolder.listView.setAdapter(adapter);
		return convertView;
	}

	private void dismissDialog() {
		if (mDownDialog != null && mDownDialog.isShowing()) {
			mDownDialog.dismiss();
			mDownDialog = null;
		}
	}

	static class ViewHolder {
		// 游戏图标
		ImageView gameIcon;
		// 游戏名称
		TextView gameName;
		// 游戏大小
		TextView gameSize;
		// 游戏礼包数量
		TextView numGift_Tv;
		// 下载按钮
		ProgressButton downloadBtn;
		// 游戏推荐图标
		// ImageView gameCornerIcon;
		// 游戏介绍
		TextView gameComment;
		/** 内测，公测等 */
		TextView gameTagTV;
		/** 评分 */
		ListView listView;
		LinearLayout game_layout;

	}

	// public void onItemClick(int position) {
	// if (position < 0 || position >= items.size()) {
	// return;
	// }
	// AppsItemBean item = items.get(position);
	// if (null != item) {
	// // 跳转到详细界面
	// UIUtils.gotoAppDetail(item.id);
	// }
	// }

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AIDLUtils.unregisterCallback(listener);
		mHandler.removeCallbacksAndMessages(null);
		if (adapter != null) {
			adapter.removeDownLoadHandler();
		}
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AIDLUtils.registerCallback(listener);
		if (adapter != null) {
			adapter.setDownlaodRefreshHandle();
		}
	}

	/**
	 * 充写原因 ViewPager在Android4.0上有兼容性错误
	 * ViewPager在移除View时会调用ListView的unregisterDataSetObserver方法
	 * ，而ListView本身也会调用该方法，所以在第二次调用时就会报“The observer is null”错误。
	 * http://blog.csdn.net/guxiao1201/article/details/8818734
	 */
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		if (observer != null) {
			super.unregisterDataSetObserver(observer);
		}
	}

	// /**
	// * 继续提示对话框
	// */
	// private View.OnClickListener mDeletePromptListener = new
	// View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// JLog.i("long", "mBean=" + mBean + "--df=" + df);
	// df.dismissAllowingStateLoss();
	// if (mBean != null) {
	// UIUtils.downloadApp(mBean);
	// }
	// }
	// };

}
