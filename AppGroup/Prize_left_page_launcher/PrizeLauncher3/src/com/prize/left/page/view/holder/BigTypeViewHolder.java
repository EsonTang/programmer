package com.prize.left.page.view.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.launcher3.R;
/**
 * 所有卡片大类的Holder
 */
public class BigTypeViewHolder extends RecyclerView.ViewHolder {
	
	public TextView titleTxt;

	private Context mCtx;
	
	public ImageView imgFlag;
	
	public BigTypeViewHolder(View v) {
		super(v);
		mCtx = v.getContext();
		titleTxt = (TextView) v.findViewById(R.id.txt_name);
		
		imgFlag = (ImageView) v.findViewById(R.id.img_flag);
	}
}
