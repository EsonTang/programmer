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
import java.util.List;

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
import com.prize.onlinemusibean.SongDetailInfo;
import com.prize.onlinemusibean.RecomendRankBean;
import com.xiami.sdk.utils.ImageUtil;

/**
 * 今日歌单
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RecommendDailyAdapter extends BaseAdapter {
	private List<SongDetailInfo> items;
	private Context context;

	public RecommendDailyAdapter(Context activity) {
		this.context = activity;
	}

	public void setData(List<SongDetailInfo> data) {
		if (data != null) {
			items = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {

		// TODO Auto-generated method stub
		return items == null ? 0 : items.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {
		if (items == null)
			return null;
		return items.get(position);
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
					R.layout.item_home_daily_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.logo_Iv = (ImageView) convertView
					.findViewById(R.id.logo_Iv);
			// viewHolder.play_Iv = (ImageView) convertView
			// .findViewById(R.id.play_Iv);
			viewHolder.title_Tv = (TextView) convertView
					.findViewById(R.id.title_Tv);
			viewHolder.singer_Tv = (TextView) convertView
					.findViewById(R.id.singer_Tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		// viewHolder.play_Iv.setVisibility(View.GONE);
		final SongDetailInfo bean = getItem(position);
		viewHolder.title_Tv.setText(bean.song_name);
		viewHolder.singer_Tv.setText(bean.singers);
		ImageLoader.getInstance().displayImage(
				ImageUtil.transferImgUrl(bean.album_logo, 220),
				viewHolder.logo_Iv, UILimageUtil.getTwoOneZeroDpLoptions(),
				null);

		return convertView;
	}

	static class ViewHolder {
		// 榜单排行图标
		ImageView logo_Iv;
		// ImageView play_Iv;
		// 榜单排行名称
		TextView title_Tv;
		TextView singer_Tv;

	}

}
