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

import u.aly.co;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.prize.app.constants.Constants;
import com.prize.app.util.JLog;
import com.prize.app.util.ToastUtils;
import com.prize.music.activities.ToAlbumDetailActivity;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.widgets.AlbumItemView;
import com.prize.music.R;
import com.prize.onlinemusibean.AlbumBean;

/**
 * 歌手详情页 歌单adapter
 * 
 * @author pengyang
 */
public class AlbumListAdapter extends BaseAdapter {
	private ArrayList<AlbumBean> items = new ArrayList<AlbumBean>();

	private FragmentActivity context;

	public AlbumListAdapter(FragmentActivity activity) {
		this.context = activity;
	}

	@Override
	public int getCount() {
		if (items.size() % 3 == 0) {
			return items.size() / 3;
		}
		return items.size() / 3 + 1;
	}

	@Override
	public AlbumBean getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<AlbumBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<AlbumBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	// @Override
	// public int getItemViewType(int position) {
	// return items.get(position).type;
	// }

	@Override
	public long getItemId(int position) {
		return position;
	}

	// @Override
	// public int getViewTypeCount() {
	// return 3;
	// }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_album_list_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.view_col1 = (AlbumItemView) convertView
					.findViewById(R.id.view_col1);
			viewHolder.view_col2 = (AlbumItemView) convertView
					.findViewById(R.id.view_col2);
			viewHolder.view_col3 = (AlbumItemView) convertView
					.findViewById(R.id.view_col3);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		JLog.i("hu", "items.size=="+items.size());
		// one 
		final AlbumBean bean1 = items.get(position * 3);
//		JLog.i("hu", position * 3+1+"=="+bean1.album_name);
		viewHolder.view_col1.setAlbumItemBean(bean1);
		viewHolder.view_col1.setTag(bean1);
		viewHolder.view_col1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UiUtils.JumpToAlbumDetail(context, bean1.album_id);
			}
		});
        
		// Two
		if ((position * 3 + 1) < items.size()) {
			viewHolder.view_col2.setVisibility(View.VISIBLE);
			final AlbumBean bean2 = items.get(position * 3 + 1);
//			JLog.i("hu", position * 3 + 1+1+"=="+bean2.album_name+"---item.size=="+items.size());
			viewHolder.view_col2.setAlbumItemBean(bean2);
			viewHolder.view_col2.setTag(bean2);
			viewHolder.view_col2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					UiUtils.JumpToAlbumDetail(context, bean2.album_id);
				}
			});
		} else {
			viewHolder.view_col2.defaultTransState();
			viewHolder.view_col2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
				}
				
			});
			viewHolder.view_col2.setVisibility(View.INVISIBLE);
//		    JLog.i("hu", position * 3 + 1+1+"==null");
		 }
        
		// Three
		if ((position * 3 + 2) < items.size()) {
			viewHolder.view_col3.setVisibility(View.VISIBLE);
			final AlbumBean bean3 = items.get(position * 3 + 2);
//			JLog.i("hu", position * 3 + 2+1+"=="+bean3.album_name+ "---item.size=="+items.size());
			viewHolder.view_col3.setAlbumItemBean(bean3);
			viewHolder.view_col3.setTag(bean3);
			
			
			
			viewHolder.view_col3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					UiUtils.JumpToAlbumDetail(context, bean3.album_id);
				}
			});
		}
		else {
			viewHolder.view_col3.defaultTransState();
			viewHolder.view_col3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					
				}
				
			});
			viewHolder.view_col3.setVisibility(View.INVISIBLE);
//			JLog.i("hu", position * 3 + 2+1+"==null");
		}

		return convertView;
	}

	static class ViewHolder {
		AlbumItemView view_col1;
		AlbumItemView view_col2;
		AlbumItemView view_col3;
	}
}
