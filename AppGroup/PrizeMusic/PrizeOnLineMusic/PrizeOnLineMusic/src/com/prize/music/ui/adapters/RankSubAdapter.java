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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.music.R;
import com.prize.onlinemusibean.SongDetailInfo;

/**
 * sub排行adapter
 **
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class RankSubAdapter extends BaseAdapter {
	private Context context;// RecomendRankBean
	private List<SongDetailInfo> items = new ArrayList<SongDetailInfo>();

	public RankSubAdapter(Context activity) {
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
		return items.size();
	}

	@Override
	public SongDetailInfo getItem(int position) {
		if (position < 0 || items.isEmpty() || position >= items.size()) {
			return null;
		}
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
					R.layout.item_sub_rank_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.song_info = (TextView) convertView
					.findViewById(R.id.song_info);
			viewHolder.song_number = (TextView) convertView
					.findViewById(R.id.song_number);
			viewHolder.singer_info = (TextView) convertView
					.findViewById(R.id.singer_info);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final SongDetailInfo bean = getItem(position);
		viewHolder.song_info.setText(bean.singers);
		viewHolder.singer_info.setText("-" + bean.song_name);
		if (position <= 2) {
			viewHolder.song_number.setText(String.valueOf(position + 1));
			switch (position) {
			case 0:
				viewHolder.song_number
						.setTextColor(Color.parseColor("#fe0100"));
				break;
			case 1:
				viewHolder.song_number
						.setTextColor(Color.parseColor("#ff6500"));
				break;
			case 2:
				viewHolder.song_number
						.setTextColor(Color.parseColor("#ffc300"));
				break;

			}
		}
		return convertView;
	}

	static class ViewHolder {
		TextView song_info;
		TextView singer_info;
		TextView song_number;

	}

}
