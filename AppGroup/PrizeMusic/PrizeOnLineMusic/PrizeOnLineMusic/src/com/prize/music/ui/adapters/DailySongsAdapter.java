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
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.R;
import com.prize.onlinemusibean.CollectBean;

/**
 * 歌单列表
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class DailySongsAdapter extends BaseAdapter {
	private Context context;// RecomendRankBean
	private ArrayList<CollectBean> items = new ArrayList<CollectBean>();

	public DailySongsAdapter(Context activity) {
		this.context = activity;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
	}

	@Override
	public CollectBean getItem(int position) {

		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	public void setData(ArrayList<CollectBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<CollectBean> data) {
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
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_collect_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.rank_logo = (ImageView) convertView
					.findViewById(R.id.rank_logo);
			viewHolder.collectName_Tv = (TextView) convertView
					.findViewById(R.id.collectName_Tv);
			viewHolder.play_count_Tv = (TextView) convertView
					.findViewById(R.id.play_count_Tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final CollectBean bean = getItem(position);
		viewHolder.collectName_Tv.setText(bean.collect_name);
		viewHolder.play_count_Tv.setText(String.valueOf(bean.play_count));
		ImageLoader.getInstance().displayImage(bean.collect_logo,
				viewHolder.rank_logo, UILimageUtil.getTwoOneZeroDpLoptions(),
				null);

		return convertView;
	}

	static class ViewHolder {
		// 榜单排行图标
		ImageView rank_logo;
		// 榜单排行名称
		TextView collectName_Tv;
		TextView play_count_Tv;

	}
}
