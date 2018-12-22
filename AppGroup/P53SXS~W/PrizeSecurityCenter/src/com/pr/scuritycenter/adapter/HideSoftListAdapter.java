package com.pr.scuritycenter.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pr.scuritycenter.R;

@SuppressLint({ "ViewHolder", "InflateParams" })
public class HideSoftListAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> scanList = new ArrayList<String>();
	private ViewHolder mViewHolder;

	public HideSoftListAdapter(Context context) {
		super();
		this.mContext = context;
		getList();
	}

	@Override
	public int getCount() {
		return scanList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup arg2) {
		Log.d("secure", "getView......................  view = " + view);
		View itemView = LayoutInflater.from(mContext).inflate(R.layout.hide_soft_item, null);
		mViewHolder = new ViewHolder();
		mViewHolder.scanType = (TextView) itemView.findViewById(R.id.hide_soft_list_itemname);
		mViewHolder.scanType.setText(mContext.getResources().getString(R.string.scan_result_jre));
		mViewHolder.icon = (ImageView) itemView.findViewById(R.id.hide_soft_list_itemicon);
		mViewHolder.scanType.setText(scanList.get(position));
		mViewHolder.icon.setImageResource(R.drawable.scan_result_safe);
		return itemView;
	}

	private class ViewHolder {
		ImageView icon;
		TextView scanType;
	}

	private void getList() {
		scanList.add(mContext.getResources()
				.getString(R.string.scan_result_jre));
		scanList.add(mContext.getResources().getString(R.string.scan_result_ne));
		scanList.add(mContext.getResources().getString(R.string.scan_result_pe));
		scanList.add(mContext.getResources().getString(R.string.scan_result_se));
	}

}
