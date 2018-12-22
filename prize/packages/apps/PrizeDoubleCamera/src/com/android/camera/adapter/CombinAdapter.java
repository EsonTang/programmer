package com.android.camera.adapter;

import java.util.ArrayList;

import com.android.camera.ui.RotateImageView;
import com.android.camera.CameraActivity;
import com.android.camera.ModeChecker;
import com.android.camera.R;
import com.mediatek.camera.util.Util;

import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import com.android.camera.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;



public class CombinAdapter extends BaseAdapter{
	
    // can not change this sequence
    // Before MODE_VIDEO is "capture mode" for UI,switch "capture mode"
    // remaining view should not show
  
	private static final String TAG = "PrizeModePickerAdapter";
	private CameraActivity mContext;
    private int[] modeIconsNormal;
    private int[] modeIconsHight;
    private int[] modeTitl;
    private int currentSelet;

    public CombinAdapter(CameraActivity mContext,int[] modeIconsNormal,int[] modeIconsHight,int[] modeTitl){
		this.mContext = mContext;
		this.modeIconsNormal = modeIconsNormal;
		this.modeIconsHight = modeIconsHight;
		this.modeTitl = modeTitl;
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return modeTitl.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d(TAG, "getView:"+position);
		
		ViewHolder mViewHolder = new ViewHolder();
		
		if(convertView==null){
			convertView = LayoutInflater.from(mContext.getBaseContext()).inflate(R.layout.combin_contain_item, null);
			mViewHolder.mRotateImageView =(RotateImageView) convertView.findViewById(R.id.mRotateImageView);
			convertView.setTag(mViewHolder);
		}else{
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		
		if(currentSelet == position){
			mViewHolder.mRotateImageView.setImageResource(modeIconsHight[position]);
			mViewHolder.mRotateImageView.setTag(true);
		}else{
			mViewHolder.mRotateImageView.setImageResource(modeIconsNormal[position]);
			mViewHolder.mRotateImageView.setTag(false);
		}
		
			
		mViewHolder.mRotateImageView.setContentDescription(mContext.getBaseContext().getString(modeTitl[position]));

		return convertView;	
	}	
	
	
	public class ViewHolder{
		public RotateImageView mRotateImageView;
	}


	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setCurrentSelet(int position){
		currentSelet = position;
	}
	
}
