package com.mediatek.camera.mode.watermark;

import com.android.camera.Log;
import com.android.camera.ui.RotateLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class WaterMarkRotateLayout extends RotateLayout{
	private int mMeasureOrientation = -1;
	private int mOverLimmitLeftMargin = -1;
	private int mOverLimmitTopMargin = -1;
	
	private PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo;
	
    public PrizeWaterMarkThumbInfo getPrizeWaterMarkThumbInfo() {
		return mPrizeWaterMarkThumbInfo;
	}
	public void setPrizeWaterMarkThumbInfo(PrizeWaterMarkThumbInfo mPrizeWaterMarkThumbInfo) {
		this.mPrizeWaterMarkThumbInfo = mPrizeWaterMarkThumbInfo;
	}
	public interface OnMeasureListener{
    	public void onRotateMeasure(WaterMarkRotateLayout v);
    }
	OnMeasureListener mMeasureListener;
    
	private static final String TAG = "WaterMarkRotateLayout";
	public WaterMarkRotateLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onLayout(boolean change, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(change, left, top, right, bottom);
		
	}
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		// TODO Auto-generated method stub
		if(mMeasureListener != null && isPortLandChange()) {
			mMeasureListener.onRotateMeasure(this);
		}
		mMeasureOrientation = getOrientation();
		super.onMeasure(widthSpec, heightSpec);
	}
	public void setOnMeasureListener(OnMeasureListener layoutListener) {
		mMeasureListener = layoutListener;
	}
	
	public boolean isPortLandChange() {
		if(Math.abs(mMeasureOrientation-getOrientation()) == 180 || Math.abs(mMeasureOrientation-getOrientation()) == 0) {
			return false;
		}else {
			return true;
		}
			
	}
	public int getOverLimmitLeftMargin() {
		return mOverLimmitLeftMargin;
	}
	public void setOverLimmitLeftMargin(int mOverLimmitLeftMargin) {
		this.mOverLimmitLeftMargin = mOverLimmitLeftMargin;
	}
	public int getOverLimmitTopMargin() {
		return mOverLimmitTopMargin;
	}
	public void setOverLimmitTopMargin(int mOverLimmitTopMargin) {
		this.mOverLimmitTopMargin = mOverLimmitTopMargin;
	}
}
