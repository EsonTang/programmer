package com.prize.music.ui.adapters;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumBean;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/***
 * 推荐歌手类型分类的item 适配器
 * @author Administrator
 *
 */
public class SearchAlbumAdapter extends BaseAdapter{
    
	private Context ctx;
	private ArrayList<AlbumBean> datas = new ArrayList<AlbumBean>();
	public SearchAlbumAdapter(Activity activity) {
		ctx = activity;
	}

	public void setData(ArrayList<AlbumBean> datas) {
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
	public AlbumBean getItem(int position) {
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
					R.layout.search_album_item, null);
			viewHolder.singeritemImg = (ImageView) convertView
					.findViewById(R.id.singerItem_img_id);
			viewHolder.singerName = (TextView) convertView
					.findViewById(R.id.singer_name_id);
			viewHolder.albumName = (TextView) convertView
					.findViewById(R.id.album_name_id);
			convertView.setTag(viewHolder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final AlbumBean data = datas.get(position);
		if (data == null) {
			return convertView;
		}
		if (!TextUtils.isEmpty(data.album_logo)) {
			ImageLoader.getInstance().displayImage(data.album_logo,
					viewHolder.singeritemImg, UILimageUtil.getTwoOneZeroDpLoptions(), null);
		}
		viewHolder.singeritemImg.setTag(data);
		viewHolder.singerName.setText(data.artist_name);
		viewHolder.albumName.setText(data.album_name);
		
		return convertView;
	}

	class ViewHolder {
		public ImageView singeritemImg;
		public TextView singerName;
		public TextView albumName;
	}

}
