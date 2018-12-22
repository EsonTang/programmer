package com.prize.weather.framework;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class MyBaseAdapter extends BaseAdapter {
	
	protected LayoutInflater layoutInflater;
	public List<? extends Object> list;
	
	public MyBaseAdapter(LayoutInflater layoutInflater,
			List<? extends Object> list) {
		super();
		this.layoutInflater = layoutInflater;
		this.list = list;
	}
	
	public MyBaseAdapter(Context mContext,
			List<? extends Object> list) {
		super();
		this.layoutInflater = LayoutInflater.from(mContext);
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
	
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
	
}
