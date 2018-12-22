package com.goodix.service;

import java.util.ArrayList;

import com.goodix.util.Fingerprint;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import  com.goodix.fpsetting.R;

public class FingerAdapter extends BaseAdapter {

	private final static String TAG = "FingerAdapter";
	private ArrayList<Fingerprint>  mfingerlist;
	@SuppressWarnings("unused")
	private Context mContext;
	private LayoutInflater mInflater;
	public static String BACKGROUND_COLOR_ENABLED = "#FFFFFF";
	public static String BACKGROUND_COLOR_DISABLED = "#CCCCCC";
	public static String BACKGROUND_COLOR_ONTOUCH = "#CCCCCC";
	//	public static boolean isTouch = true;
	public FingerAdapter(Context context, ArrayList<Fingerprint>mFingerList) {
		mContext = context;
		mfingerlist = (ArrayList<Fingerprint>) mFingerList;
		mInflater = LayoutInflater.from(context);
	}

	public void setFingerList(ArrayList<Fingerprint> mlist) {
		mfingerlist = mlist;
	}

	@Override
	public int getCount() {
		if(null != mfingerlist){
			return mfingerlist.size();
		}else{
			return 0;
		}
	}

	@Override
	public Object getItem(int pos) {
		return mfingerlist.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(null == convertView){
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.finger_item, null);
			holder.fingerView = (TextView) convertView.findViewById(R.id.finger_textview);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		String fp_name = mfingerlist.get(position).getName();
		holder.fingerView.setText(fp_name);
		return convertView;
	}

	private class ViewHolder{
		TextView fingerView;
	}
}

