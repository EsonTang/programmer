package com.prize.music.ui.adapters.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.prize.music.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PopupWindowAdapter extends BaseAdapter {

	private List<String> mPlayListNames;
	private LayoutInflater mInflater;
	private List<Map<String, Object>> mLists;

	public PopupWindowAdapter(List<Map<String, Object>> mLists, Context context) {
		super();
		this.mLists = mLists;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mLists.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHlder viewHlder = null;
		if (convertView == null) {
			viewHlder = new ViewHlder();
			convertView = mInflater.inflate(R.layout.popupwindow_options_item,
					null);
			viewHlder.title = (TextView) convertView
					.findViewById(R.id.item_text_tv);
			convertView.setTag(viewHlder);
		} else {
			viewHlder = (ViewHlder) convertView.getTag();
		}
		viewHlder.title.setText((String) mLists.get(position).get("name"));
		return convertView;
	}

	private class ViewHlder {
		public TextView title;
	}
}
