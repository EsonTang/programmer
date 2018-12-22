package com.android.camera.ui;

import com.android.camera.Log;
import com.android.camera.Util;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.mediatek.camera.setting.SettingUtils;
import com.prize.setting.NavigationBarUtils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.android.camera.R;

public class PreviewSurfaceView extends SurfaceView {
    private double                      mAspectRatio = 0.0;
    private static final String         TAG = "PreviewSurfaceView";
    private int                         mPreviewWidth = 0;
    private int                         mPreviewHeight = 0;
    private boolean                     mIsNeedLockSizeChange = false;
    private static final int SHRINK_SIZE = 2; // surface size must be multiple of 2
	private int mScreenPixHeight;
	private int mScreenPixWidth;

    public PreviewSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getScreenPix();
    }

    // Add onMeasure is for full screen crop preview
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int previewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int previewHeight = MeasureSpec.getSize(heightMeasureSpec);
        Log.i(TAG, "onMeasure preview is ( " + previewWidth + " ,  " + previewHeight + " )");
        boolean widthLonger = previewWidth > previewHeight;
        int longSide = (widthLonger ? previewWidth : previewHeight);
        int shortSide = (widthLonger ? previewHeight : previewWidth);
        if (mAspectRatio > 0) {
            double fullScreenRatio = SettingUtils.getFullScreenRatio();
            /*prize-add-18:9 full screen-xiaoping-20180423-start*/
            Log.d(TAG,"mAspectRatio: "+mAspectRatio+",fullScreenRatio: "+fullScreenRatio);
            if (mAspectRatio == 2.0) {
                longSide = /*Math.round((float) (shortSide * mAspectRatio) / 2) * 2*/MeasureSpec.getSize(heightMeasureSpec);
                adapter199display();
            /*prize-add-18:9 full screen-xiaoping-20180423-end*/
            } else if (Math.abs((mAspectRatio - fullScreenRatio)) <= Util.ASPECT_TOLERANCE) {
                // full screen preview case
                Log.i(TAG, "19 :9 full screen case");
                /*prize-xuchunming-surfaceview width real show at 18:9 project-start*/
                //if (longSide < shortSide * mAspectRatio) {
                    longSide = /*Math.round((float) (shortSide * mAspectRatio) / 2) * 2*/MeasureSpec.getSize(heightMeasureSpec);
               /* } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }*/
                /*prize-xuchunming-surfaceview width real show at 18:9 project-end*/
                adapter199display();
            } else if ((Math.abs((mAspectRatio - fullScreenRatio)) <= Util.ASPECT_TOLERANCE_16_9)) {
                Log.i(TAG, "16:9 case");
                longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                adapter169display();
            } else {
                // standard (4:3) preview case
                Log.i(TAG, "4:3 case");
                if (longSide > shortSide * mAspectRatio) {
                    longSide = Math.round((float) (shortSide * mAspectRatio) / 2) * 2;
                } else {
                    shortSide = Math.round((float) (longSide / mAspectRatio) / 2) * 2;
                }
                adapter43display(mScreenPixHeight, longSide);
            }
        }
        if (widthLonger) {
            previewWidth = longSide;
            previewHeight = shortSide;
        } else {
            previewWidth = shortSide;
            previewHeight = longSide;
        }
        /* if (mIsNeedLockSizeChange) {
            previewWidth = SHRINK_SIZE;
            previewHeight = SHRINK_SIZE;
        }
        if (!mIsNeedLockSizeChange && widthLonger == false) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
        }*/

        boolean originalPreviewIsLandscape = (mPreviewWidth > mPreviewHeight);
        int orientation = getContext().getResources().getConfiguration().orientation;
        boolean configurationIsLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);

        // if configuration is changed, swap to view's configuration
        if (originalPreviewIsLandscape != configurationIsLandscape) {
            int originalPreviewWidht = previewWidth;
            previewWidth = previewHeight;
            previewHeight = originalPreviewWidht;
        }

        Log.i(TAG, "originalPreviewIsLandscape = " + originalPreviewIsLandscape
                + ",configurationIsLandscape = " + configurationIsLandscape + ",mPreviewWidth = "
                + mPreviewWidth + ",mPreviewHeight = " + mPreviewHeight);

        setMeasuredDimension(previewWidth, previewHeight);
        Log.i(TAG, "After onMeasure  aspectRatio = " + mAspectRatio + " previewWidth = "
                + previewWidth + " previewHeight = " + previewHeight);
    }

    /**
     * set new preview aspect ratio to notify SurfaceView onMeasure again
     * Note: this method must be called on UI Thread
     * @param aspectRatio
     * @return true: layout will change; false: layout will not change
     */
    public boolean setAspectRatio(double aspectRatio) {
        Log.i(TAG, "setAspectRatio aspectRatio = " + aspectRatio);
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
            requestLayout();
            return true;
        }
        return false;
    }

    /**
     * hide surface view by setting size to 1x1
     * when swap to gallery complete, should hide surface view
     * Note: this method must be called on UI Thread
     */
    public void shrink() {
        if (mIsNeedLockSizeChange) {
            return;
        }
        mIsNeedLockSizeChange = true;
        setVisibility(View.INVISIBLE);
        //setLayoutSize(SHRINK_SIZE, SHRINK_SIZE);
    }

    /**restore surface view's size
     * when swap from gallery to camera, should expand surface view immediately
     * Note: this method must be called on UI Thread
     */
    public void expand() {
        Log.i(TAG, "expand preview (" + mPreviewWidth + " , " + mPreviewHeight + ")");
        if (!mIsNeedLockSizeChange) {
            return;
        }
        boolean originalPreviewIsLandscape = (mPreviewWidth > mPreviewHeight);
        double orientation = getContext().getResources().getConfiguration().orientation;
        boolean configurationIsLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        // if configuration is changed, swap to view's configuration
        if (originalPreviewIsLandscape != configurationIsLandscape) {
            int originalPreviewWidht = mPreviewWidth;
            mPreviewWidth = mPreviewHeight;
            mPreviewHeight = originalPreviewWidht;
        }
        mIsNeedLockSizeChange = false;
        setVisibility(View.VISIBLE);
        //setLayoutSize(mPreviewWidth, mPreviewHeight);
    }

    private void setLayoutSize(int width, int height) {
        Log.i(TAG, "setLayoutSize mPreviewWidth = " + mPreviewWidth
                + " width = " + width
                + " mPreviewHeight = " + mPreviewHeight
                + " height = " + height);
        if (width <= 0 || height <= 0 || mPreviewWidth <= 0 || mPreviewWidth <= 0) {
            return;
        }
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) getLayoutParams();
        if (p.width != width || p.height != height) {
            p.width = width;
            p.height = height;
            p.setMargins(mPreviewWidth - width, mPreviewHeight - height, 0, 0);
            setLayoutParams(p);
        }
    }
    public void adapter43display(int ScreenHeight, int displayHeight){
    	Log.d("xucm", "ScreenHeight:"+ScreenHeight+",displayHeight:"+displayHeight);
    	FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
    	p.bottomMargin = (int)getContext().getResources().getDimension(R.dimen.shutter_group_height) - (ScreenHeight - displayHeight) / 2;
    	p.topMargin = 0;
    	if(p.bottomMargin < 0){
    		p.bottomMargin = 0;
    	}
    	setLayoutParams(p);
    }
    
    public void getScreenPix() {
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);

		mScreenPixWidth = metric.widthPixels;
		mScreenPixHeight = metric.heightPixels;
	}

    /*prize-xuchunming-20160524-bugid:16429-start*/
    public void adapter169display(){
        Log.d(TAG,"adapter169display");
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
        p.bottomMargin = 0;
        p.topMargin = (int)getResources().getDimension(R.dimen.preview_surfaceview_margin);
        setLayoutParams(p);
    }
    /*prize-xuchunming-20160524-bugid:16429-end*/

    /*prize-xuchunming-20160524-bugid:16429-start*/
    public void adapter199display(){
    	Log.d(TAG,"adapter199display");
    	FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)getLayoutParams();
    	p.bottomMargin = 0;
    	p.topMargin = 0;
    	setLayoutParams(p);
    }
    /*prize-xuchunming-20160524-bugid:16429-end*/
    
    public double getAspectRatio(){
    	return mAspectRatio;
    }
    
    public int getScreenPixHeight(){
    	return mScreenPixHeight;
    }


}
