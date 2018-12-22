package com.goodix.fpsetting;

import java.util.List;

import com.goodix.model.FpFunctionDescription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FpManagerListViewAdapter extends BaseAdapter {

	private Context mContext;
	private List<FpFunctionDescription> mList;
	private LayoutInflater mInflater;

	public FpManagerListViewAdapter() {
		super();
	}

	public FpManagerListViewAdapter(Context context, List<FpFunctionDescription> list) {
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
	public FpFunctionDescription getItem(int position) {
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
			convertView = mInflater.inflate(R.layout.fp_function_item, null);
			holder.mFunctionName = (TextView) convertView.findViewById(R.id.name);
			holder.mFunctionInstruction = (TextView) convertView.findViewById(R.id.instruction);
			holder.mFunctionSwitch = (ImageView) convertView.findViewById(R.id.function_switch);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}

		final FpFunctionDescription description = mList.get(position);
		holder.mFunctionName.setText(description.getmShowName());
		holder.mFunctionInstruction.setText(description.getmShowInstruction());
		holder.mFunctionSwitch.setVisibility(View.GONE);
		return convertView;
	}

	private class ViewHolder{
		TextView mFunctionName;
		TextView mFunctionInstruction;
		ImageView mFunctionSwitch;
	}

}
