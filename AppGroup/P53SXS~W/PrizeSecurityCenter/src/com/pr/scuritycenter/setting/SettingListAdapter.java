package com.pr.scuritycenter.setting;

import java.util.List;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pr.scuritycenter.R;
import com.pr.scuritycenter.framework.MyBaseAdapter;

/**
 * 
 * @author wangzhong
 *
 */
public class SettingListAdapter extends MyBaseAdapter {

	public SettingListAdapter(LayoutInflater layoutInflater,
			List<? extends Object> listData) {
		super(layoutInflater, listData);
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = layoutInflater.inflate(R.layout.setting_list_item, null);
		TextView tv_title = (TextView) convertView.findViewById(R.id.tv_title);
		TextView tv_des = (TextView) convertView.findViewById(R.id.tv_des);
		
		String title = (String) getItem(position);
		
		tv_title.setText(title);
		if (position == 3) {
			tv_des.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}

}
