package com.android.settings;

import android.os.Bundle;
import android.os.Message;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;



public class PrizeShowAnimationView extends View{

	private static final String TAG = "PrizeShowAnimationView";
	private ArrayList<Integer> mImageId = new ArrayList<Integer>();
	private Integer mCurrDrawIndex = 0;
	private boolean mIsShowAnimation= false;

        
	private Thread  mThread =new Thread(new Runnable(){

		@Override
		public void run() {
			while(mIsShowAnimation){

				if((mImageId.size() > 0)&&(mCurrDrawIndex < mImageId.size())){
					Log.v(TAG,"-----------mCurrDrawIndex="+mCurrDrawIndex+"-----postInvalidate()----");
					postInvalidate();
					try{
						Thread.sleep(50);
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}else{
					Log.v(TAG,"-----------else-----------------");
					try{
						Thread.sleep(50);
					}
					catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		}
	});  
        

        public PrizeShowAnimationView(Context context) {
            super(context);
        }

	public PrizeShowAnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}


        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

		synchronized (mCurrDrawIndex) {
			//Log.v(TAG,"------onDraw()-----");
			Drawable currDrawable = null;
			if(mImageId.size() > 0){
				if (mCurrDrawIndex < mImageId.size()) {
					Log.v(TAG,"-------mCurrDrawIndex="+mCurrDrawIndex+"----imageId="+mImageId.get(mCurrDrawIndex));		
					currDrawable = getResources().getDrawable(mImageId.get(mCurrDrawIndex));
					if (currDrawable != null) {
						currDrawable.setBounds(0, 0, currDrawable.getIntrinsicWidth(), currDrawable.getIntrinsicHeight());
						currDrawable.draw(canvas);
						mCurrDrawIndex=mCurrDrawIndex+1;
					}
					if(mCurrDrawIndex==mImageId.size())
						mIsShowAnimation=false;
				}
			}
		}

        }


	public void setImagesID(ArrayList<Integer> image) {
		mImageId = image;
	}
	public void addImageId(int imageId){
		mImageId.add(imageId);
	}


	public void startShowImage() {
		mIsShowAnimation = true;
		mCurrDrawIndex = 0;
		if (!mThread.isAlive()) {
			mThread.start();
		}
	}

	public void stopShowImage() {
		mIsShowAnimation = false;
		mCurrDrawIndex = 0;
	}


	public boolean isShowAnimation() {

		return mIsShowAnimation;
	}


      
       
}





