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

import com.prize.music.R;
import com.prize.onlinemusibean.AutoTipsBean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MatchSearchAdapter extends BaseAdapter {
	private Context mCtx;
	private ArrayList<AutoTipsBean> datas = new ArrayList<AutoTipsBean>();

	public MatchSearchAdapter(Context mCtx) {
		this.mCtx = mCtx;
	}

	public void setData(ArrayList<AutoTipsBean> datas) {
//		if (datas == null || datas.size() <= 0)
//			return;
////		this.datas = datas;
//		this.datas.addAll(datas);
//		notifyDataSetChanged();
		if (datas != null) {
			this.datas = datas;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		return datas.size();
	}

	@Override
	public AutoTipsBean getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mCtx).inflate(
					R.layout.search_match_item, null);
			holder.matchType = (TextView) convertView
					.findViewById(R.id.match_type);
			holder.matchItem = (TextView) convertView
					.findViewById(R.id.match_item);
			holder.play_Btn = (ImageView) convertView
					.findViewById(R.id.play_Btn);
			holder.icon_fly = (ImageView) convertView
					.findViewById(R.id.icon_fly);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		AutoTipsBean autoTips = datas.get(position);
		if (autoTips == null) {
			return convertView;
		}
		holder.play_Btn.setVisibility(View.INVISIBLE);
		if("艺人".equals(autoTips.type)){
			holder.matchType.setText(R.string.search_singer);
		}else{
			holder.matchType.setText(autoTips.type);
		}
		if("歌曲".equals(autoTips.type)){
			holder.play_Btn.setVisibility(View.VISIBLE);
		}
		holder.matchItem.setText(autoTips.tip);
		return convertView;
	}

	static class ViewHolder {
		TextView matchType;
		TextView matchItem;
		ImageView play_Btn;
		ImageView icon_fly;
	}
}
