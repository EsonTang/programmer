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

package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.prize.app.beans.ClientInfo;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * 已下载的歌曲
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DownLoadingSongsAdapter extends BaseAdapter {
	private ArrayList<SongDetailInfo> items = new ArrayList<SongDetailInfo>();

	private Context context;
	private UIDownLoadListener listener = null;
	/** 当前页是否处于显示状态 */
	private boolean isActivity = true; // 默认true
	private GameDAO dao;

	public void setIsActivity(boolean state) {
		isActivity = state;
	}

	public DownLoadingSongsAdapter(Context activity) {
		this.context = activity;
		listener = new UIDownLoadListener() {
			@Override
			protected void onErrorCode(int song_Id, int errorCode) {
				notifyDataSetChanged();
			}

			@Override
			protected void onUpdateProgress(int song_Id) {
				if (isActivity) {
					notifyDataSetChanged();
				}
			}

			@Override
			protected void onFinish(int song_Id) {
				notifyDataSetChanged();
			}

			@Override
			public void onRefreshUI(int song_Id) {
			}

			@Override
			protected void onStart(int song_Id) {
				notifyDataSetChanged();
			}
		};
		dao = GameDAO.getInstance();
	}

	@Override
	public int getCount() {
		return items == null ? 0 : items.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<SongDetailInfo> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<SongDetailInfo> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	/**
	 */
	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_downloading_song_layout, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.song_name_Tv = (TextView) convertView
					.findViewById(R.id.song_name_Tv);
			viewHolder.downSize_Tv = (TextView) convertView
					.findViewById(R.id.downSize_Tv);
			viewHolder.down_pause_Tv = (TextView) convertView
					.findViewById(R.id.down_pause_Tv);
			viewHolder.radius_Tv = (TextView) convertView
					.findViewById(R.id.radius_Tv);
			viewHolder.totalSize_Tv = (TextView) convertView
					.findViewById(R.id.totalSize_Tv);
			viewHolder.download_progressbar = (ProgressBar) convertView
					.findViewById(R.id.download_progressbar);
			viewHolder.down_wait_Tv = (TextView) convertView
					.findViewById(R.id.down_wait_Tv);
			viewHolder.relativeLayout1 = (LinearLayout) convertView
					.findViewById(R.id.relativeLayout1);
			viewHolder.container = (LinearLayout) convertView
					.findViewById(R.id.container);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SongDetailInfo bean = getItem(position);
		viewHolder.song_name_Tv.setText(bean.song_name);
		long totalSize = 0;
		if (dao != null) {
			totalSize = dao.getDownloadSize(String.valueOf(bean.song_id));
		}
		int progress = AppManagerCenter.getDownloadProgress(String
				.valueOf(bean.song_id));
		viewHolder.download_progressbar.setProgress(progress);
		if (totalSize > 0) {
			String downloadSize = CommonUtils
					.paresAppSize((long) (totalSize * (progress / 100f)));
			viewHolder.downSize_Tv.setText(downloadSize + "MB");
			viewHolder.totalSize_Tv.setText("/"
					+ CommonUtils.paresAppSize(totalSize) + "MB");
		}
		final int state = AppManagerCenter.getGameAppState(bean);
		JLog.i("DownLoadingSongsAdapter", "--state=" + state
				+ "--4098:暂停-4097：在下载--4099：等待中");
		viewHolder.relativeLayout1.setVisibility(View.VISIBLE);
		viewHolder.down_wait_Tv.setVisibility(View.VISIBLE);
		viewHolder.down_pause_Tv.setVisibility(View.VISIBLE);
		refreshView(viewHolder.relativeLayout1, viewHolder.down_wait_Tv,
				viewHolder.down_pause_Tv, viewHolder.radius_Tv, state,
				position, bean, progress);
		return convertView;
	}

	static class ViewHolder {
		TextView song_name_Tv;
		// TextView down_faile_Tv;
		TextView down_wait_Tv;
		TextView down_pause_Tv;

		LinearLayout relativeLayout1;
		ProgressBar download_progressbar;
		TextView totalSize_Tv;
		TextView downSize_Tv;
		TextView radius_Tv;
		LinearLayout container;

	}

	/**
	 * 取消 下载监听, Activity OnDestroy 时调用
	 */
	public void removeDownLoadHandler() {
		AppManagerCenter.removeDownloadRefreshHandle(listener);
	}

	/**
	 * 设置刷新handler,Activity OnResume 时调用
	 */
	public void setDownlaodRefreshHandle() {
		AppManagerCenter.setDownloadRefreshHandle(listener);
	}

	/**
	 * 
	 */
	public void removeIndexObject(int postion) {
		if (items != null && items.size() > 0 && items.size() > postion
				&& postion >= 0) {
			items.remove(postion);

		}
		notifyDataSetChanged();
	}

	void refreshView(LinearLayout relativeLayout1, TextView down_wait_Tv,
			TextView down_pause_Tv, TextView radius_Tv, int state,
			int position, SongDetailInfo bean, int progress) {
		if (state == AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE) {
			relativeLayout1.setVisibility(View.GONE);
			down_wait_Tv.setVisibility(View.GONE);
			down_pause_Tv.setVisibility(View.VISIBLE);
		} else if (state == AppManagerCenter.APP_STATE_WAIT) {
			relativeLayout1.setVisibility(View.GONE);
			down_wait_Tv.setVisibility(View.VISIBLE);
			down_pause_Tv.setVisibility(View.GONE);
		} else if (state == AppManagerCenter.APP_STATE_DOWNLOADING) {
			relativeLayout1.setVisibility(View.VISIBLE);
			down_wait_Tv.setVisibility(View.GONE);
			down_pause_Tv.setVisibility(View.GONE);
			// 刷新下载速度
			int radiu = AppManagerCenter.getDownloadSpeed(String
					.valueOf(bean.song_id));
			if (radiu >= 1000) {
				radius_Tv.setText(String.format("%1$.2f", radiu / (1024f))
						+ "MB/s");

			} else if (radiu > 0 && radiu < 1000) {
				radius_Tv.setText(radiu + "KB/s");
			}

		} else {
			down_wait_Tv.setVisibility(View.GONE);
			relativeLayout1.setVisibility(View.GONE);
			down_pause_Tv.setVisibility(View.GONE);
		}
	}

	public void onItemClick(int position) {
		if (position < 0 || position >= items.size()) {
			return;
		}
		SongDetailInfo bean = items.get(position);
		if (bean == null)
			return;
		final int state = AppManagerCenter.getGameAppState(bean);
		switch (state) {
		case AppManagerCenter.APP_STATE_DOWNLOAD_PAUSE:
			if (ClientInfo.networkType == ClientInfo.NONET) {
				ToastUtils.showToast(R.string.net_error);
				return;
			}
			DownLoadUtils.downloadMusic(bean);
			break;
		case AppManagerCenter.APP_STATE_DOWNLOADING:
			AppManagerCenter.pauseDownload(bean, true);
			break;

		default:
			break;
		}
		notifyDataSetChanged();
	}

}
