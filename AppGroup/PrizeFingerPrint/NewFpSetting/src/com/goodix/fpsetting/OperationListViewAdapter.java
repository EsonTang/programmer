package com.goodix.fpsetting;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OperationListViewAdapter extends BaseAdapter {

	private Context mContext;
	private List<String> mList;
	private LayoutInflater mInflater;

	public OperationListViewAdapter() {
		this(null,null);
	}

	public OperationListViewAdapter(Context context, List<String> list) {
		super();
		if(null != context && null != list){
			mContext = context;
			mList = list;
			mInflater = LayoutInflater.from(context);
		}
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(null == convertView){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.operation_item, null);
			holder.opeationClickArea = (TextView) convertView.findViewById(R.id.opeation_click_area);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		String content = getItem(position);
		holder.opeationClickArea.setText(content);
		return convertView;
	}

	private class ViewHolder{
		TextView opeationClickArea;
	}

}
