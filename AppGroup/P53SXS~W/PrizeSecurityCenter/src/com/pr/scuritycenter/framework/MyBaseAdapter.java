package com.pr.scuritycenter.framework;

import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * @author wangzhong
 *
 */
public abstract class MyBaseAdapter extends BaseAdapter  {

	protected LayoutInflater layoutInflater;
	public List<? extends Object> listData;

	public MyBaseAdapter(LayoutInflater layoutInflater, List<? extends Object> listData) {
		super();
		this.layoutInflater = layoutInflater;
		this.listData = listData;
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

}
