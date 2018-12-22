package com.prize.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class HorizontalScrollLayoutAdapter {

	private LayoutInflater mInflater;
	private List<String> mDatas;
	private int mLayoutId;

	public HorizontalScrollLayoutAdapter(Context context, List<String> mDatas, int layoutId) {
		mLayoutId = layoutId;
		mInflater = LayoutInflater.from(context);
		this.mDatas = mDatas;
	}

	public int getCount() {
		return mDatas.size();
	}

	public Object getItem(int position) {
		return mDatas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(mLayoutId, parent, false);
			viewHolder.mText = (TextView) convertView;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.mText.setText(mDatas.get(position));
		viewHolder.mText.setScaleX((float) (1-(Math.abs(position -3)) * 0.07));

		return convertView;
	}

	private class ViewHolder {
		TextView mText;
	}

}
