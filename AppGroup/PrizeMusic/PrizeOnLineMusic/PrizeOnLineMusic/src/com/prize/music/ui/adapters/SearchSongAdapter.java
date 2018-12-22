package com.prize.music.ui.adapters;

import java.util.ArrayList;

import com.prize.app.beans.ClientInfo;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.download.DownloadHelper;
import com.prize.app.download.UIDownLoadListener;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.helpers.DownLoadUtils;
import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.SongsBean;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
/***
 * 搜索结果歌曲
 * @author pengyang
 *
 */
public class SearchSongAdapter extends BaseAdapter{
    
	private Context ctx;
	private ArrayList<SongDetailInfo> datas = new ArrayList<SongDetailInfo>();
	private UIDownLoadListener listener = null;
	public SearchSongAdapter(Activity activity) {
		ctx = activity;
		listener = new UIDownLoadListener() {
			@Override
			protected void onErrorCode(int song_Id, int errorCode) {
			}

			@Override
			protected void onFinish(int song_Id) {
				notifyDataSetChanged();
			}

			@Override
			public void onRefreshUI(int song_Id) {
			}

		};
	}

	public void setData( ArrayList<SongDetailInfo> datas) {
		if (datas == null || datas.size() <= 0)
			return;
//		this.datas = datas;
		this.datas.addAll(datas);
		notifyDataSetChanged();
	}
	
	public void clearData() {
		if (this.datas != null) {
			datas.clear();
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		return datas.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.search_song_item, null);
			viewHolder.songName = (TextView) convertView
					.findViewById(R.id.song_name_tv);
			viewHolder.singerName = (TextView) convertView
					.findViewById(R.id.singer_name_tv);
			viewHolder.downloadImg = (ImageButton) convertView
					.findViewById(R.id.download_img);
			viewHolder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			convertView.setTag(viewHolder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SongDetailInfo data = datas.get(position);
		if (data == null) {
			return convertView;
		}
		viewHolder.songName.setText(data.song_name);
		viewHolder.singerName.setText(data.artist_name);
		
//		if (data.permission != null) {
//			if (!data.permission.available) {
//				viewHolder.downloadImg.setVisibility(View.INVISIBLE);
//			} else {
//				viewHolder.downloadImg.setVisibility(View.VISIBLE);
//			}
//		}
		
		if (DownloadHelper.isFileExists(data)) {
			viewHolder.downloadImg.setEnabled(false);
		} else {
			viewHolder.downloadImg.setEnabled(true);
		}
		
		viewHolder.downloadImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (ClientInfo.networkType == ClientInfo.NONET) {
					ToastUtils.showToast(R.string.net_error);
					return;
				}
				DownLoadUtils.downloadMusic(data);
				ToastUtils.showToast(R.string.add_download_queue_ok);
			}
		});
		return convertView;
	}

	class ViewHolder {
		public TextView songName;
		public TextView singerName;
		public ImageButton downloadImg;
		ImageView icon_fly;
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

}
