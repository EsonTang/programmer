package com.android.settings.powermaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.android.settings.R;

public class CPUModeDetailsAdapter extends BaseAdapter{


	private LayoutInflater mLayoutInflater;
	private String[] mModeNameArr;
	private String[] mModeStatusArr;
	private int mModeType;

	public CPUModeDetailsAdapter(Context context, int modeType,String[] modeNameArr, String[] modeStatusArr) {
		super();
		mLayoutInflater = LayoutInflater.from(context);
		mModeType = modeType;
		mModeNameArr = modeNameArr;
		mModeStatusArr = modeStatusArr;
	}

	@Override
	public int getCount() {
		if(mModeType == 0){
			return 1;
		}else{
			return mModeStatusArr.length;
		}
	}

	@Override
	public String getItem(int position) {
		return mModeStatusArr[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mHolder = null;
		if(convertView == null){
			mHolder = new ViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.prize_power_master_cpu_model_item, null);
			mHolder.mNameView = (TextView)convertView.findViewById(R.id.name);
			mHolder.mStatusView = (TextView)convertView.findViewById(R.id.status);
			convertView.setTag(mHolder);
		}else{
			mHolder = (ViewHolder)convertView.getTag();
		}

		mHolder.mNameView.setText(mModeNameArr[position]);
		mHolder.mStatusView.setText(mModeStatusArr[position]);
		return convertView;
	}

	private class ViewHolder{
		public TextView mNameView;
		public TextView mStatusView;
	}
}
