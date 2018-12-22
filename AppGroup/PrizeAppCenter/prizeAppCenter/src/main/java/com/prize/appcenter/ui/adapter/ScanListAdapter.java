package com.prize.appcenter.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.prize.appcenter.R;

import java.util.ArrayList;
import java.util.List;

public class ScanListAdapter extends BaseAdapter {
	private Context mContext;
	private List<String> mStringList = new ArrayList<String>();

	public ScanListAdapter(Context context) {
		this.mContext = context;
		
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_safe_clear));
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_weixin_clear));
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_qq_clear));
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_trash_cache));
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_trash_apk));
		mStringList.add(mContext.getResources().getString(R.string.clear_sdk_trash_bigfile));
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mStringList.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return mStringList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder = null;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.clear_tash_scan_list_item, null);
			holder = new Holder();
			holder.title_tv = (TextView) convertView.findViewById(R.id.item_title);
			holder.running_im = (ImageView) convertView.findViewById(R.id.running_im);
			convertView.setTag(holder);
		}
		holder = (Holder) convertView.getTag();
		
		holder.title_tv.setText(getItem(position));
		holder.running_im.setImageResource(R.drawable.clear_running_anim);
		AnimationDrawable animationDrawable = (AnimationDrawable) holder.running_im.getDrawable();
		animationDrawable.start();
		
		return convertView;
	}
	
	public class Holder{
		TextView title_tv;
		ImageView running_im;
	}

}
