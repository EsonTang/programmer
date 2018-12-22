package com.prize.gallery3d.video;

import android.app.Activity;
import android.content.Context;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import com.android.gallery3d.app.MovieControllerOverlay;
import com.android.gallery3d.ui.GestureRecognizer;

import com.prize.ui.OptionTools;
import com.prize.util.DensityUtil;
import com.prize.util.LogTools;

public class VideoZoomController implements GestureRecognizer.Listener {

    private static final String TAG = "Gallery2/VideoZoomController";

    private Context mContext;

    private View mVideoRoot;
    private View mOverlay;

    private final GestureRecognizer mGestureRecognizer;
    /** Type of record**/
    private int mGestureSlide = GESTURE_SLIDE_NONE;
    /** NO ADJUST**/
    private static final int GESTURE_SLIDE_NONE = -1;
    /** ADJUST progress**/
    public static final int GESTURE_SLIDE_PROGRESS = 0;
    /** adjust volume**/
    public static final int GESTURE_SLIDE_VOLUME = 1;
    /** adjust light**/
    public static final int GESTURE_SLIDE_BRIGHTNESS = 2;
    /** The step size of the sliding time is set, and the sliding time is avoided, and the change is too fast.**/
    private static final float STEP_PROGRESS = 2f;
    /**The step size of the sliding time is set, and the sliding time is avoided, and the change is too fast.**/
    private static final float STEP_VOLUME = 2f;
    /**The step size of the sliding time is set, and the sliding time is avoided, and the change is too fast.**/
    private static final float STEP_BRIGHTNESS = 2f;
    /** onDown after£¬frist do onScroll**/
    private boolean mIsFirstScroll;


    public VideoZoomController(Context context, View videoroot, View overlay) {
    	
    	LogTools.i(TAG, "new VideoZoomController()");
        mContext = context;
        mOverlay = overlay; //MovieControllerOverlay instance.
        mVideoRoot = videoroot;

        mGestureRecognizer = new GestureRecognizer(mContext, this);

        mVideoRoot/*.getRootView()*/.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	LogTools.i(TAG, "onTouch()");
            	mGestureRecognizer.onTouchEvent(event);
            	return true;
            }
        });
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        LogTools.i(TAG, "onSingleTapUp");
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(float x, float y) {
        LogTools.i(TAG, "onSingleTapConfirmed");
		if(((MovieControllerOverlay) mOverlay).isHidden()){
			((MovieControllerOverlay) mOverlay).show();
		}else{
			((MovieControllerOverlay) mOverlay).hide();
		}
        return true;
    }

    @Override
    public boolean onDoubleTap(float x, float y) {
        return true;
    }

    @Override
    public boolean onScroll(float dx, float dy, float totalX, float totalY) {
    	// lock,not used
    	LogTools.i(TAG, " onScroll()");
        if (isLock()) {
            return true;
        }
    	if (OptionTools.isOptionVideo()) {
    		LogTools.i(TAG, " onScroll mIsFirstScroll " + mIsFirstScroll + " mGestureSlide" + mGestureSlide);
            if (mIsFirstScroll) {
                if (Math.abs(dx) >= Math.abs(dy)) {
                    mGestureSlide = GESTURE_SLIDE_PROGRESS;
                }
                onSlideStart(mGestureSlide);
            } else {
                if (mGestureSlide == GESTURE_SLIDE_VOLUME) { //  adjust  volume
                    int pxStepVolume = DensityUtil.dip2px(mContext, STEP_VOLUME);
                    LogTools.i(TAG, " onScroll pxStepVolume " + pxStepVolume + " dy" + dy);
                    if (dy >= pxStepVolume){ 
                        onSlideMove(mGestureSlide, true);
                    } else if (dy <= -pxStepVolume) {
                        onSlideMove(mGestureSlide, false);
                    }    
                    
                } else if (mGestureSlide == GESTURE_SLIDE_BRIGHTNESS) { //  adjust  brightness
                    int pxStepBrightness = DensityUtil.dip2px(mContext, STEP_BRIGHTNESS);
                    LogTools.i(TAG, " onScroll pxStepBrightness " + pxStepBrightness + " dy" + dy);
                    if (dy >= pxStepBrightness) {
                        onSlideMove(mGestureSlide, true);
                    } else if (dy <= -pxStepBrightness) {
                        onSlideMove(mGestureSlide, false);
                    }
                    
                } else if (mGestureSlide == GESTURE_SLIDE_PROGRESS) { // adjust progress
                    int pxStepProgress = DensityUtil.dip2px(mContext, STEP_PROGRESS);
                    LogTools.i(TAG, " onScroll pxStepProgress " + pxStepProgress + " dx" + dx);
                    if (dx >= pxStepProgress) {
                        onSlideMove(mGestureSlide, false);
                    } else if (dx <= -pxStepProgress) {
                        onSlideMove(mGestureSlide, true);
                    }    
                }
            }
            mIsFirstScroll = false;
        }
        return true;
    }
    
    /**
     * 
     * method Touch screen to adjust brightness, volume, progress
     * @param 
     * @param 
     * @return 
     * @see
     */
    private void onSlideMove(int type, boolean inc) {
        ((MovieControllerOverlay) mOverlay).onSlideMove(type, inc);
    }
    
    /**
     * 
     * method start adjust brightness, volume, progress
     * @param 
     * @return 
     * @see
     */
    private void onSlideStart(int type) {
        ((MovieControllerOverlay) mOverlay).onSlideStart(type);
    }
    

    private void onSlideEnd(int type) {
        ((MovieControllerOverlay) mOverlay).onSlideEnd(type);
    }

    private boolean isLock() {
    	return ((MovieControllerOverlay) mOverlay).isLock();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScaleBegin(float focusX, float focusY) {
        return true;
    }

    @Override
    public boolean onScale(float focusX, float focusY, float scale) {
        return true;
    }

    @Override
    public void onDown(float x, float y) {
        LogTools.i(TAG, "onDown");
        mIsFirstScroll = true;
        Display disp = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        int windowWidth = disp.getWidth();
        
        if (x > windowWidth / 2) {
            mGestureSlide = GESTURE_SLIDE_VOLUME;
        } else {
            mGestureSlide = GESTURE_SLIDE_BRIGHTNESS;
        }
        LogTools.i(TAG, " onScroll windowWidth= " + windowWidth);
        onDownEvent();
    }

    @Override
    public void onUp() {
        LogTools.i(TAG, "onUp");
        if (mGestureSlide != GESTURE_SLIDE_NONE && !isLock()) { // MediaPlayer seekTo
            onSlideEnd(mGestureSlide);
        }
    }
    
    public void onDownEvent(){
    	
    }

	@Override
	public void onScaleEnd(float focusX, float focusY) {
		
	}
}
