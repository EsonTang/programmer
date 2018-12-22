package com.goodix.fpsetting;

import java.util.List;

import com.goodix.model.FpFunctionDescription;
import com.goodix.util.FpFunctionSPUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FpFunctionListViewAdapter extends BaseAdapter {

	private Context mContext;
	private List<FpFunctionDescription> mList;
	private LayoutInflater mInflater;
	private FpFunctionSPUtil mSharePUtil;

	public FpFunctionListViewAdapter() {
		this(null,null);
	}

	public FpFunctionListViewAdapter(Context context, List<FpFunctionDescription> list) {
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

		final FpFunctionDescription info = mList.get(position);
		holder.mFunctionName.setText(info.getmShowName());
		holder.mFunctionInstruction.setText(info.getmShowInstruction());
		mSharePUtil = new FpFunctionSPUtil(mContext);
		Boolean isOpenFunction = mSharePUtil.getFunctionStatus(info.getmDbColumnName());

		if (isOpenFunction) {
			holder.mFunctionSwitch.setBackgroundResource(R.drawable.app_lock_open);
		} else {
			holder.mFunctionSwitch.setBackgroundResource(R.drawable.app_lock_close);
		}

		final ViewHolder mHolder = (ViewHolder)convertView.getTag();
		holder.mFunctionSwitch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String currentFunctionName = info.getmDbColumnName();
				Boolean isOpenFunction = mSharePUtil.getFunctionStatus(currentFunctionName);
				if (isOpenFunction) {
					mSharePUtil.setFunctionStatus(currentFunctionName, false);
					mHolder.mFunctionSwitch.setBackgroundResource(R.drawable.app_lock_close);
				} else {
					mSharePUtil.setFunctionStatus(currentFunctionName, true);
					mHolder.mFunctionSwitch.setBackgroundResource(R.drawable.app_lock_open);
				}
			}
		});

		return convertView;
	}

	private class ViewHolder{
		TextView mFunctionName;
		TextView mFunctionInstruction;
		ImageView mFunctionSwitch;
	}

}
