package com.prize.prizethemecenter.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.prize.prizethemecenter.R;
import com.prize.prizethemecenter.bean.AutoTipsData.AutoTip;

import java.util.ArrayList;

/**
 * 适配器
 * @author pengy
 * 
 */
public class AutoTipsAdapter extends BaseAdapter {
	private ArrayList<AutoTip> items = new ArrayList<>();
	protected Context context;

	public AutoTipsAdapter(Context activity) {
		this.context = activity;
	}

	public void addData(ArrayList<AutoTip> data) {
		if (data != null) {
			items.addAll(data);
		}
		notifyDataSetChanged();
	}
	public void setData(ArrayList<AutoTip> data) {
		if (data == null || data.size() <= 0)
			return;
		this.items = data;
		notifyDataSetChanged();
	}

	public void clearAll() {
		if (items != null) {
			items.clear();
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public AutoTip getItem(int position) {
		if (position < 0 || items.isEmpty() || position >= items.size()) {
			return null;
		}
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.search_history_item_layout, null);
			viewHolder = new ViewHolder();
			viewHolder.history_item_tv = (TextView) convertView
					.findViewById(R.id.history_item_tv);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		AutoTip autoTip = items.get(position);
		viewHolder.history_item_tv.setText(autoTip.suggestion);

		return convertView;
	}

	static class ViewHolder {
		TextView history_item_tv;
	}
}
