/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.Context;
import android.os.SystemClock;
import com.android.camera.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.android.camera.manager.ModePicker;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingDataBase;
import com.prize.ui.ScaleGestureDetector;

// This class aggregates three gesture detectors: GestureDetector,
// ScaleGestureDetector, and DownUpDetector.
public class GestureRecognizer {
    @SuppressWarnings("unused")
    private static final String TAG = "GestureRecognizer";

    public interface Listener {
        boolean onSingleTapUp(float x, float y);
        boolean onSingleTapConfirmed(float x, float y);
        void onLongPress(float x, float y);
        boolean onDoubleTap(float x, float y);
        boolean onScroll(float dx, float dy, float totalX, float totalY, MotionEvent e2);
        boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY);
        boolean onScaleBegin(float focusX, float focusY);
        boolean onScale(float focusX, float focusY, float scale);
        void onScaleEnd();
        void onDown(float x, float y);
        void onUp();
    }

    private final GestureDetector mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;
    private final DownUpDetector mDownUpDetector;
    /*    private final Listener mListener;*/
    private Listener mListener;
    private boolean mListenerAvaliable;
    private Context mContext;

    public GestureRecognizer(Context context, Listener listener) {
        mListener = listener;
        Log.i(TAG, "GestureRecognizer");
        mGestureDetector = new GestureDetector(context, new MyGestureListener(),
                null, true /* ignoreMultitouch */);
        mScaleDetector = new ScaleGestureDetector(
                context, new MyScaleListener());
        mDownUpDetector = new DownUpDetector(new MyDownUpListener());
        mListenerAvaliable = true;
        mContext = context;
    }

    public void onTouchEvent(MotionEvent event) {
        Log.i("remove", "Gesture onTouchEvent");
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        mDownUpDetector.onTouchEvent(event);
    }

    public boolean isDown() {
        return mDownUpDetector.isDown();
    }

    public void cancelScale() {
        long now = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(
                now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mScaleDetector.onTouchEvent(cancelEvent);
        cancelEvent.recycle();
    }
    private class MyGestureListener
                extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i(TAG, "MyGestureListener onSingleTapUp");
            if (!mListenerAvaliable)
                return true;
            /*prize-add-click focus on 19: 9 full screen -xiaoping-20180504-start*/
            if (mContext instanceof CameraActivity) {
            	/*prize-modify-do not display focus view from 19:9 full screen-xiaoping-20180509-start*/
                if (((CameraActivity)mContext).getCurrentMode() != ModePicker.MODE_PICTURE_ZOOM
                		&& ((CameraActivity)mContext).getCurrentMode() != ModePicker.MODE_BOKEH
                		&& ((CameraActivity)mContext).getCurrentMode() != ModePicker.MODE_PREVIDEO	//add for bugid:63157
                		&& ((CameraActivity)mContext).getCurrentMode() != ModePicker.MODE_SHORT_PREVIDEO //add for bugid:63157
                		&& ((CameraActivity)mContext).getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO) != null 
                		&& ((CameraActivity)mContext).getISettingCtrl().getSettingValue(SettingConstants.KEY_PICTURE_RATIO).equals(SettingDataBase.PICTURE_RATIO_19_9)) {
                    Log.i(TAG,"onSingleTapUp at 19:9 full screen");
                    return mListener.onSingleTapUp(e.getX(), e.getRawY());
                }
            }
            /*prize-add-click focus on 19: 9 full screen -xiaoping-20180504-end*/
            return mListener.onSingleTapUp(e.getX(), e.getY());
        }
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!mListenerAvaliable)
                return true;
            return mListener.onDoubleTap(e.getX(), e.getY());
        }

        @Override
        public boolean onScroll(
                MotionEvent e1, MotionEvent e2, float dx, float dy) {
        	if(e1 == null || e2 == null) {
        		Log.w(TAG, "onScroll drop , MotionEvent is null");
        	}
        	Log.d(TAG, "onScroll e1.getX():"+e1.getX()+",e2.getX():"+e2.getX()+",dx:"+dx+",e2.getPointerCount():"+e2.getPointerCount());
            if (!mListenerAvaliable)
                return true;
            return mListener.onScroll(
                    dx, dy, e2.getX() - e1.getX(), e2.getY() - e1.getY(), e2);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
        	if(e1 == null || e2 == null) {
        		Log.w(TAG, "onScroll drop , MotionEvent is null");
        	}
        	Log.d(TAG, "onScroll e1.getX():"+e1.getX()+",e2.getX():"+e2.getX()+",velocityX:"+velocityX);
            if (!mListenerAvaliable)
                return true;
            return mListener.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mListenerAvaliable)
                return true;
            return mListener.onSingleTapConfirmed(e.getX(), e.getY());
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i(TAG, "MyGestureListener onLongPress");
            mListener.onLongPress(e.getX(), e.getY());
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
        	// TODO Auto-generated method stub
        	Log.i(TAG, "MyGestureListener onDown");
        	return super.onDown(e);
        }
    }

    private class MyScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if (!mListenerAvaliable)
                return true;
            return mListener.onScaleBegin(
                    detector.getFocusX(), detector.getFocusY());
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!mListenerAvaliable)
                return true;
            return mListener.onScale(detector.getFocusX(),
                    detector.getFocusY(), detector.getScaleFactor());
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (!mListenerAvaliable)
                return;
            mListener.onScaleEnd();
        }
    }
    private class MyDownUpListener implements DownUpDetector.DownUpListener {
        @Override
        public void onDown(MotionEvent e) {
        	Log.i(TAG, "MyDownUpListener onDown");
            if (!mListenerAvaliable)
                return;
            mListener.onDown(e.getX(), e.getY());
        }
        @Override
        public void onUp(MotionEvent e) {
        	Log.i(TAG, "MyDownUpListener onUp");
            if (!mListenerAvaliable)
                return;
            mListener.onUp();
        }
    }
    public void setAvaliable(boolean avaliable) {
        mListenerAvaliable = avaliable;
    }
    public Listener setGestureListener(Listener listener) {
        Listener old = mListener;
        mListener = listener;
        return old;
    }
}
