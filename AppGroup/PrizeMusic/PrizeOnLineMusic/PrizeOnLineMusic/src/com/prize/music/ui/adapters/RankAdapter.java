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
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.app.constants.Constants;
import com.prize.music.helpers.utils.LogUtils;
import com.prize.music.helpers.utils.RankUtils;
import com.prize.music.helpers.utils.UILimageUtil;
import com.prize.music.helpers.utils.UiUtils;
import com.prize.music.ui.fragments.DetailFragment;
import com.prize.music.views.ListViewForScrollView;
import com.prize.music.R;
import com.prize.onlinemusibean.RecomendRankBean;

/**
 * 排行adapter
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RankAdapter extends BaseAdapter {
	private ArrayList<RecomendRankBean> items = new ArrayList<RecomendRankBean>();

	private FragmentActivity context;// RecomendRankBean

	public RankAdapter(FragmentActivity activity) {
		this.context = activity;
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
	}

	@Override
	public RecomendRankBean getItem(int position) {
		if (position < 0 || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {

		// TODO Auto-generated method stub
		return position;
	}

	public void setData(ArrayList<RecomendRankBean> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	public void addData(ArrayList<RecomendRankBean> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		RankSubAdapter adapter;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_rank_layout, parent,false);
			viewHolder = new ViewHolder();
			adapter = new RankSubAdapter(context);
			viewHolder.rank_logo = (ImageView) convertView
					.findViewById(R.id.rank_logo);
			viewHolder.rank_Tv = (TextView) convertView
					.findViewById(R.id.rank_Tv);
			viewHolder.mListView = (ListViewForScrollView) convertView
					.findViewById(R.id.mListView);
			convertView.setTag(viewHolder);
			convertView.setTag(R.id.id_adapter, adapter);
			if (convertView != null) {
				convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
			adapter = (RankSubAdapter) convertView.getTag(R.id.id_adapter);
		}
		final RecomendRankBean bean = getItem(position);
		if (!RankUtils.setImagAndText(viewHolder.rank_logo, viewHolder.rank_Tv,
				bean.title)) {
			ImageLoader.getInstance().displayImage(bean.logo,
					viewHolder.rank_logo,
					UILimageUtil.getTwoOneZeroDpLoptions(), null);
			viewHolder.rank_Tv.setText(bean.title);
		}
		viewHolder.mListView.setClickable(false);
		viewHolder.mListView.setPressed(false);
		viewHolder.mListView.setEnabled(false);
		viewHolder.mListView.setAdapter(adapter);
		adapter.setData(bean.songs.size() > 3 ? bean.songs.subList(0, 3)
				: bean.songs);

		return convertView;
	}

	static class ViewHolder {
		// 榜单排行图标
		ImageView rank_logo;
		// 榜单排行名称
		TextView rank_Tv;

		ListViewForScrollView mListView;
	}

	public void onItemClick(int position) {
		if (getItem(position) != null) {
			UiUtils.gotoMoreDaily(context, getItem(position).type,
					Constants.KEY_RANK);
		}
	}
}
