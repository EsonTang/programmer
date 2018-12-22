package com.prize.music.ui.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.onlinemusibean.SongsBean;

/***
 * 搜索结果歌曲
 * 
 * @author pengyang
 *
 */
public class AlbumAdapter extends BaseAdapter {

	private Context ctx;
	private ArrayList<SongsBean> datas = new ArrayList<SongsBean>();

	public AlbumAdapter(Activity activity) {
		ctx = activity;
	}

	public void setData(ArrayList<SongsBean> datas) {
		if (datas == null || datas.size() <= 0)
			return;
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
	public SongsBean getItem(int position) {
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
			viewHolder.downloadImg = (ImageView) convertView
					.findViewById(R.id.download_img);
			convertView.setTag(viewHolder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SongsBean data = datas.get(position);
		if (data == null) {
			return convertView;
		}
		viewHolder.songName.setText(data.song_name);
		viewHolder.singerName.setText(data.artist_name);

		viewHolder.downloadImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}
		});
		return convertView;
	}

	class ViewHolder {
		public TextView songName;
		public TextView singerName;
		public ImageView downloadImg;
	}

}
