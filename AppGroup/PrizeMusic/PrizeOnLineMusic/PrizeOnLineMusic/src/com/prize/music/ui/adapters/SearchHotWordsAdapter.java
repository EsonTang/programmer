package com.prize.music.ui.adapters;

import java.util.ArrayList;

import com.prize.music.R;
import com.prize.onlinemusibean.HotWordsResponse.SearchWords;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
/***
 * 关键字热搜
 * @author Administrator
 *
 */
public class SearchHotWordsAdapter extends BaseAdapter{
    
	private Context ctx;
	private ArrayList<SearchWords> datas;
	private int MAX_COUNT = 9;
	public SearchHotWordsAdapter(Activity activity) {
		ctx = activity;
	}

	public void setData(ArrayList<SearchWords> datas) {
		if (datas == null || datas.size() <= 0)
			return;
		this.datas = datas;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (datas == null) {
			return 0;
		}
		if (datas.size() > MAX_COUNT) {
			return MAX_COUNT;
		}
		return datas.size();
	}

	@Override
	public SearchWords getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(ctx).inflate(
					R.layout.item_search_hotwords, null);
			holder.hotwords_Tv = (TextView) convertView
					.findViewById(R.id.hotwords_Tv);
			convertView.setTag(holder);
			convertView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		SearchWords bean = datas.get(position);
		if (!TextUtils.isEmpty(bean.word)) {
			holder.hotwords_Tv.setText(bean.word);
		}
		return convertView;
	}

	static class ViewHolder {
		TextView hotwords_Tv;
	}
}
