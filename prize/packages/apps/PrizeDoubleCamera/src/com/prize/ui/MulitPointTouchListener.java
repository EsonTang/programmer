package com.prize.ui;

import com.android.camera.CameraActivity;

import android.content.Context;
import android.graphics.PointF;
import android.util.FloatMath;
import com.android.camera.Log;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public class MulitPointTouchListener implements OnTouchListener {

    private static final String TAG = "MulitPoint";
    /*prize-xuchuming-20180427-bugid:55041-start*/
    private Context mContext;
    /*prize-xuchuming-20180427-bugid:55041-end*/

    // We can be in one of these 3 states
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // Remember some things for zooming
    PointF mid = new PointF();
    float oldDist = 1f;
    private float xInScreen;

    private float yInScreen;

    private float xInView;

    private float yInView;
    
    public interface ViewTouchListener {
    	void singleClick(MotionEvent e);
    	void scale(float scale);
    }
    
    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;
    private boolean mIsDoubleTapping;
    private int mDoubleTapSlopSquare;
    private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();
    
    private ViewTouchListener mViewTouchListener;
    private CenterHorizontalScroll mChildView;
    
    
    public void setViewTouchListener(ViewTouchListener listener) {
    	mViewTouchListener = listener;
    }
    
    private void cancel() {
        mIsDoubleTapping = false;
        mode = NONE;
    }
    
    private void singleTapConfirmed(MotionEvent ev) {
    	if (mViewTouchListener != null) {
    		mViewTouchListener.singleClick(ev);
    	}
    }
    
    private void scaleView(float scale) {
    	if (mViewTouchListener != null) {
    		mViewTouchListener.scale(scale);
    	}
    }
    
    public MulitPointTouchListener(Context context, CenterHorizontalScroll childView) {
    	final ViewConfiguration configuration = ViewConfiguration.get(context);
    	final int doubleTapSlop = configuration.getScaledDoubleTapSlop();
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
        mChildView = childView;
        /*prize-xuchuming-20180427-bugid:55041-start*/
        mContext = context;
        /*prize-xuchuming-20180427-bugid:55041-end*/
	}
    
    public MulitPointTouchListener(Context context) {
    	this(context, null);
    	/*prize-xuchuming-20180427-bugid:55041-start*/
        mContext = context;
        /*prize-xuchuming-20180427-bugid:55041-end*/
	}

	@Override
    public boolean onTouch(View v, MotionEvent event) {

        // Dump touch event to log
		int margintop = 0;
        dumpEvent(event);
        if (((CameraActivity)mContext).getPreviewSurfaceView() != null) {
        	margintop = ((CameraActivity)mContext).getPreviewSurfaceView().getTop();
		}
        /*prize-xuchuming-20180427-bugid:55041-start*/
        if(mContext instanceof CameraActivity) {
        		/*prize-modify-inconsistent display of click position and focus area-xiaoping-20180626-start*/
        		if (((CameraActivity)mContext).getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO) != null 
        				&& ((CameraActivity)mContext).getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO).equals(SettingDataBase.PICTURE_RATIO_4_3)) {
					event.setLocation(event.getX(),(float) (event.getRawY() -margintop ));
				}
        		 Log.i(TAG, "getX: "+event.getX()+",getY: "+event.getY()+",getRawX: "+event.getRawX()+",getRawY: "+event.getRawY()+",top: "+margintop);
        		 /*prize-modify-inconsistent display of click position and focus area-xiaoping-20180626-end*/
        	 ((CameraActivity)mContext).getGestureRecognizer().onTouchEvent(event);
        }
        /*prize-xuchuming-20180427-bugid:55041-end*/
        // Handle touch events here...
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:

            xInView = event.getX();
            yInView = event.getY();
            xInScreen = event.getRawX();
            yInScreen = event.getRawY();
            
            if (mCurrentDownEvent != null) {
                mCurrentDownEvent.recycle();
            }
            mCurrentDownEvent = MotionEvent.obtain(event);
            if (mChildView != null) {
            	mChildView.handleMotionEvent(event);
            }
            Log.i(TAG, "onTouch() -------------down");
            break;
        case MotionEvent.ACTION_POINTER_DOWN: // 多指按下，标记为缩放状态。并隐藏FloatMovieControllerOverlay，防止误操作
            oldDist = spacing(event);
            xInView = (event.getX(0) + event.getX(1)) / 2;
            yInView = (event.getY(0) + event.getY(1)) / 2;
            mode = ZOOM;
            Log.i(TAG, "onTouch() -------------POINTER_DOWN");
            break;
        case MotionEvent.ACTION_UP: //
        	Log.i(TAG, "onTouch() -------------UP mode=" + mode);
            if (mIsDoubleTapping) {
            	
            } else if (mode == DRAG) { // 
            	if (mChildView != null) {
                	mChildView.handleMotionEvent(event);
                }
            } else if (mode == NONE) { // 
            	singleTapConfirmed(mCurrentDownEvent);
            }
            mode = NONE;
            mIsDoubleTapping = false;
            if (mPreviousUpEvent != null) {
                mPreviousUpEvent.recycle();
            }
            // Hold the event we obtained above - listeners may have changed the original.
            mPreviousUpEvent = MotionEvent.obtain(event);
            break;
        case MotionEvent.ACTION_POINTER_UP:
        	Log.i(TAG, "onTouch() -------------POINTER_UP mode=" + mode);
            break;
        case MotionEvent.ACTION_MOVE:
        	Log.i(TAG, "onTouch() -------------MOVE mode=" + mode);
            if (mIsDoubleTapping) {
            	break;
            }
            if (mode == DRAG) { // 
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                if (mChildView != null) {
                	mChildView.handleMotionEvent(event);
                }
            } else if (mode == ZOOM) { // 
                if (event.getPointerCount() > 1) { // 
                    float newDist = spacing(event);
                    float scale = newDist / oldDist;
                    scaleView(scale); 
                }
            } else { 
            	int x = (int) event.getX();
                int y = (int) event.getY();
                if (Math.abs(x - (int) xInView) < 10  && Math.abs(y - (int) yInView) < 10) {
                	
                } else {
                	mode = DRAG;
                	if (mChildView != null) {
                    	mChildView.handleMotionEvent(event);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            cancel();
            if (mode == DRAG) { // 为移动状态时，要检测移动到的边界，越过边界要调整
            	if (mChildView != null) {
                	mChildView.handleMotionEvent(event);
                }
            }
            break;
        }

        return true; // indicate event was handled
    }

    private void dumpEvent(MotionEvent event) {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
                || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(
                    action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }
        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++) {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }
        sb.append("]");
        Log.i(TAG, sb.toString());
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
}
