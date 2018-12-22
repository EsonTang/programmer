package com.android.camera.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

public class PrizeWaterMarkShowApdater  extends PagerAdapter{
	
	private Context mContext;
	
	private ArrayList<View> mListWaterViewShow ;
	
	private int length = 0;
	
	public PrizeWaterMarkShowApdater(Context mContext,ArrayList<View> mListWaterViewShow,final int length){
		this.mContext = mContext;
		this.mListWaterViewShow = mListWaterViewShow;
		this.length = length;
	}
	
	public void setListWaterViewShow(ArrayList<View> mListWaterViewShow){
		this.mListWaterViewShow = mListWaterViewShow;
	}
	
	public ArrayList<View> getListWaterViewShow(){
		return this.mListWaterViewShow;
	} 

	@Override
	public Object instantiateItem(ViewGroup  container, int position) {
        position %= mListWaterViewShow.size();
        if (position<0){
            position = mListWaterViewShow.size() + position;
        }
        View view = mListWaterViewShow.get(position);
        ViewParent vp = view.getParent();
        if (vp != null){
            ViewGroup parent = (ViewGroup)vp;
            parent.removeView(view);
        }
        container.addView(view);  
        return view;  
	}

	@Override
	public int getCount() {
		return Integer.MAX_VALUE; 
	}

	@Override  
    public void destroyItem(ViewGroup container, int position, Object object) {  
    } 
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
