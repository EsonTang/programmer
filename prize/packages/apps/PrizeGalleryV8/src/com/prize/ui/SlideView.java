package com.prize.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.android.gallery3d.R;
import com.prize.util.LogTools;
/**
 * 
 **
 * method：adjust progress,volume,light View
 * @author author
 * @version version
 */
public class SlideView extends FrameLayout {

    protected static final String TAG = "SlideView";

    private static final int BOTTOM_PADDING_IN_DP = 14;

    private static final float WIDTH_HEIGHT_RATIO = 1.41F;

    private static final float HEIGHT_RATIO = 1.5F;

    private static final float VOLUMEBRIGHT_RATIO = 0.8F;

    private static final float VOLUME_RATIO = 0.2252F;

    private int mTopShowWidth;

    private int mTopShowHeight; 

    private int mWidth;

    private int mHeight;

    private ProgressBar mBrightnessPb;

    private ProgressBar mVolumeBrightnessPb;

    private ImageView mVolumeIm;

    private float mDensity;

    private int mVolumeBrightnessWidth;

    private int mVolumeBrightnessHeight;
    

    private int mVolumeBrightnessBottomPadding;

    private int mTopHightWithCenter;

    private boolean mIsVolume;
 
    private static final int BRIGHTNESS_SHOW_PROGRESS = 65;

    private static final int BRIGHTNESS_REAL_PROGRESS = 140;
    

    private static final int VOLUME_LEVEL_ZERO = 0;

    private static final int VOLUME_LEVEL_LOW = 50;

    private static final int VOLUME_LEVEL_HIGH = 100;

    private static final int HEIGHT_VPLUME_BRIGHTNESS_IN_DP = 7;

    private static final int DEFAULT_HEIGHT_WIDTH = 70;

    private int mDefaultHeightWidth;
    
    
    public SlideView(Context context) {
        
        super(context);

        LayoutInflater.from(context).inflate(R.layout.slide_view, this);
        
        mBrightnessPb = (ProgressBar) findViewById(R.id.pb_brightness);
        mVolumeBrightnessPb = (ProgressBar) findViewById(R.id.pb_volume_brightness);
        mVolumeIm = (ImageView) findViewById(R.id.im_volume);
        setBackgroundResource(R.drawable.slide_bg);
        

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mDensity = metrics.density;
        

        mVolumeBrightnessBottomPadding = (int) (BOTTOM_PADDING_IN_DP * mDensity);
        mVolumeBrightnessHeight = (int) (mDensity * HEIGHT_VPLUME_BRIGHTNESS_IN_DP);
        
        mDefaultHeightWidth = (int) (DEFAULT_HEIGHT_WIDTH * mDensity);
    }
    

    private int progressToBrightnessProgress(int progress) {
        return 50 + (progress - 50) * BRIGHTNESS_SHOW_PROGRESS / BRIGHTNESS_REAL_PROGRESS;
    }

    public void setProgress(boolean isVolume, int progress) {
        LogTools.i(TAG, "setProgress-------------isVolume=" + isVolume + " progress=" + progress);
        mIsVolume = isVolume;
        int mProgress = progress;
        mVolumeBrightnessPb.setProgress(mProgress);
        if (mIsVolume) { // volume
            mBrightnessPb.setVisibility(View.GONE);
            mVolumeIm.setVisibility(View.VISIBLE);
            if (mProgress <= VOLUME_LEVEL_ZERO) { // zero volume
                mVolumeIm.setBackgroundResource(R.drawable.sl_volume_zero);
            } else if (mProgress <= VOLUME_LEVEL_LOW) { 
                mVolumeIm.setBackgroundResource(R.drawable.sl_volume_low);
            } else if (mProgress < VOLUME_LEVEL_HIGH) { 
                mVolumeIm.setBackgroundResource(R.drawable.sl_volume_middle);
            } else { 
                mVolumeIm.setBackgroundResource(R.drawable.sl_volume_high);
            }
        } else { 
            mVolumeIm.setVisibility(View.GONE);
            mBrightnessPb.setVisibility(View.VISIBLE);
            if (mProgress <= 0 || mProgress >= 100) { 
                mBrightnessPb.setProgress(mProgress);
            } else {
                mBrightnessPb.setProgress(progressToBrightnessProgress(mProgress));
            }
        }
        requestLayout();
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View topView;
        if (mIsVolume) {
            topView = mVolumeIm;
        } else {
            topView = mBrightnessPb;
        }

        topView.layout((right - left - mTopShowWidth) / 2, (mTopHightWithCenter - mTopShowHeight) / 2, (right - left - mTopShowWidth) / 2 + mTopShowWidth, (mTopHightWithCenter - mTopShowHeight) / 2 + mTopShowHeight);
        LogTools.i(TAG, "onLayout() mVolumeBrightnessPb.getMeasuredHeight()=" + mVolumeBrightnessPb.getMeasuredHeight());
         mVolumeBrightnessPb.layout((mWidth - mVolumeBrightnessWidth) / 2, mHeight - mVolumeBrightnessBottomPadding - mVolumeBrightnessHeight, (mWidth - mVolumeBrightnessWidth) / 2 + mVolumeBrightnessWidth, mHeight - mVolumeBrightnessBottomPadding);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	measureChildren(widthMeasureSpec, heightMeasureSpec);
    	mTopShowWidth = mDefaultHeightWidth;//mVolumeIm.getMeasuredWidth();
        mTopShowHeight = mDefaultHeightWidth;//mVolumeIm.getMeasuredHeight();
    	mHeight = (int) (mTopShowHeight * HEIGHT_RATIO + mVolumeBrightnessHeight);
    	mWidth = (int) (mHeight * WIDTH_HEIGHT_RATIO);
    	mVolumeBrightnessWidth = (int) (mWidth * VOLUMEBRIGHT_RATIO);
    	mTopHightWithCenter = (int) (mTopShowHeight + mHeight * VOLUME_RATIO);
        setMeasuredDimension(mWidth, mHeight);
    }

}

