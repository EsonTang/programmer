/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2014. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */
package com.android.camera.manager;

import java.io.ByteArrayOutputStream;

import android.view.SurfaceControl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout.LayoutParams;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.camera.CameraActivity;
import com.android.camera.FeatureSwitcher;
import com.android.camera.Log;
import com.android.camera.ModeChecker;
import com.android.camera.R;
import com.android.camera.Util;
import com.android.camera.actor.PhotoActor;
import com.android.camera.bridge.CameraAppUiImpl;
import com.android.camera.bridge.CameraDeviceCtrl.PreviewCallbackListen;
import com.android.camera.ui.ModePickerScrollView;
import com.android.camera.ui.RotateImageView;
import com.android.prize.BlurPic;
import com.android.prize.BokehLongTimeInfo;
import com.mediatek.camera.mode.gyfacebeauty.GyBokehMode;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraAppUi.GestureListener;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingUtils;
import com.mediatek.camera.setting.preference.ListPreference;
import com.prize.setting.NavigationBarUtils;
import com.prize.ui.CenterHorizontalScroll;
import com.prize.ui.HorizontalScrollLayoutAdapter;
import com.prize.ui.HorizontalScrollerLayout.OnItemChangeListener;
import com.prize.ui.SelectHorizontalScrollerLayout;
import com.prize.ui.SelectHorizontalScrollerLayout.OnItemClickListener;


public class ModePicker extends ViewManager{
    private static final String TAG = "ModePicker";

    private ListPreference mModePreference;

    public interface OnModeChangedListener {
        void onModeChanged(int newMode);
    }

    // can not change this sequence
    // Before MODE_VIDEO is "capture mode" for UI,switch "capture mode"
    // remaining view should not show
    public static final int MODE_PHOTO = 0;
    public static final int MODE_HDR = 1;
    public static final int MODE_FACE_BEAUTY = 2;
    public static final int MODE_PANORAMA = 3;
    public static final int MODE_ASD = 4;
    public static final int MODE_PHOTO_PIP = 5;
    public static final int MODE_STEREO_CAMERA = 6;
    public static final int MODE_VIDEO = 7;
    public static final int MODE_VIDEO_PIP = 8;
    public static final int MODE_WATERMARK = 9;
    public static final int MODE_PREVIDEO = 10;
    public static final int MODE_DOUBLECAMERA = 11;

	public static final int MODE_FACEART = 12;//gangyun tech add
    public static final int MODE_BOKEH = 13;//gangyun tech add
    /*arc add start*/
    public static final int MODE_LOWLIGHT_SHOT = 14;
    public static final int MODE_PICTURE_ZOOM = 15;
    /*arc add end*/
    public static final int MODE_SHORT_PREVIDEO = 16;
    public static final int MODE_PORTRAIT = 17;
    public static final int MODE_NUM_ALL = 18;
    public static final int OFFSET = 100;
    private static final int OFFSET_STEREO_PREVIEW = OFFSET;
    private static final int OFFSET_STEREO_SINGLE = OFFSET * 2;

    public static final int MODE_PHOTO_3D = OFFSET_STEREO_PREVIEW + MODE_PHOTO;
    public static final int MODE_VIDEO_3D = OFFSET_STEREO_PREVIEW + MODE_VIDEO;

    public static final int MODE_PHOTO_SGINLE_3D = OFFSET_STEREO_SINGLE + MODE_PHOTO;
    public static final int MODE_PANORAMA_SINGLE_3D = OFFSET_STEREO_SINGLE + MODE_PANORAMA;

    private int mCurrentMode = MODE_PHOTO;
    private int mPreCurrentMode = MODE_PHOTO;
    private OnModeChangedListener mModeChangeListener;
    private OnScreenToast mModeToast;
    
    private static final int MSG_MODE_CHANGED = 1;
    
    private static final int MODE_STATE_CHANG_START = 0;
    private static final int MODE_STATE_CHANGING = 2;
    private static final int MODE_STATE_CHANG_END = 3;
    private static final int MSG_HIDE_SURFACE_COVER = 4;
    private static final int MSG_MODE_CHANGING = 5;
    
    
    private int modeState= MODE_STATE_CHANG_START;
   
    private int screenWidth;
    private int screenheight;

	/*prize-add-adjust the animation effect-xiaoping-20171017-start*/
	private static final float SCALLTOBIG_PIVOTYVALUE = 0.25f;
	private static final float SCALLTOSMALL_PIVOTYVALUE = 0.4f;
	/*prize-add-adjust the animation effect-xiaoping-20171017-end*/

    /*prize-xuchunming-20161223-show info when operation long time in bokehmode-start*/
    private BokehLongTimeInfo mBokehLongTimeInfo;
    /*prize-xuchunming-20161223-show info when operation long time in bokehmode-end*/
    
    /*prize-xuchunming-20180307-bugid:50956-start*/
    private boolean isWaitCameraSwitch;
    /*prize-xuchunming-20180307-bugid:50956-end*/
   
    /*prize-xuchunming-20180403-bugid:54203-start*/
    private boolean mIsShowCoverAnimat;
    /*prize-xuchunming-20180403-bugid:54203-end*/
    private Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch(msg.what){
    		case MSG_HIDE_SURFACE_COVER:
    			hideSurfaceCover();
    			break;
    		}
    		 
    	}
    };

    private static final int[] MODE_ORDER = {
    	MODE_PICTURE_ZOOM,  //arc add 
    	MODE_PREVIDEO,
    	MODE_SHORT_PREVIDEO,
        MODE_PHOTO, 
        MODE_FACE_BEAUTY,
		/*MODE_BOKEH,*/
		MODE_PORTRAIT,
		MODE_LOWLIGHT_SHOT,
			MODE_PANORAMA}; //arc add
    
    private static final int[] MODE_ORDER_FRONT = {
    	MODE_PREVIDEO,
    	MODE_SHORT_PREVIDEO,
        MODE_PHOTO, 
        MODE_FACE_BEAUTY,
		MODE_PORTRAIT,
        /*MODE_PANORAMA*/}; //arc add 
  
    private static final int MODE_TITLEID_FRONT[]  = {
    	R.string.pref_gesture_video,
    	R.string.pref_gesture_short_video,
    	R.string.pref_gesture_photo, 
    	R.string.pref_face_beauty_mode_title,
    	R.string.pref_gesture_pref_portrait,
    	/*R.string.pano_dialog_title*/}; //arc add
    
    private static final int MODE_TITLEID_BACK[]  = {
    	R.string.pref_picture_zoom_mode_title, //arc add 
    	R.string.pref_gesture_video,
    	R.string.pref_gesture_short_video,
    	R.string.pref_gesture_photo, 
    	R.string.pref_face_beauty_mode_title,
    	R.string.pref_gesture_pref_portrait,
		/*R.string.pref_camera_gybokeh_title,*/
		R.string.pref_lowlight_shot_mode_title,
			R.string.pano_dialog_title}; //arc add
    

    private int step;
    
    private ViewGroup screenShotLayout;
    
    private ImageView screenShotImg;
    
    private SelectHorizontalScrollerLayout mChsView;
    
    private List<String> mModeTile;
    
    private List<String> mFilterModeTile;
    
    private List<Integer> mModeValue;
    
    private float lastPreviewHeight;
    
    private float mLastBlurBottomMarg;
    
    private float mBlurBottomMarg;
    
    private OnItemClickListener mOnItemChangeListener = new OnItemClickListener() {

		@Override
		public void onItemClick(int pos) {
			// TODO Auto-generated method stub
			onClick(pos);
		}
		
		
	};
    
	private GestureListener mGestureListener = new GestureListener() {
		
		@Override
		public boolean onUp() {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onSingleTapUp(float x, float y) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onSingleTapConfirmed(float x, float y) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onScroll(float dx, float dy, float totalX, float totalY, MotionEvent e2) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onScroll dx:"+dx+",dy:"+dy+",totalX:"+totalX+",totalY:"+totalY+"");
			if(isShowing() == true && isEnabled() == true && e2.getPointerCount() < 2){
				if(modeState == MODE_STATE_CHANG_START && Math.abs(totalX) > step){
					if(mChsView.isLayoutRtl() == true){
						changMode(totalX < 0 ? false :true);
					}else{
						changMode(totalX > 0 ? false :true);
					}
					
					return true;
				}
			}
			return false;
		}
		
		@Override
		public boolean onScaleBegin(float focusX, float focusY) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onScale(float focusX, float focusY, float scale) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onLongPress(float x, float y) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onDown(float x, float y, int width, int height) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean onDoubleTap(float x, float y) {
			// TODO Auto-generated method stub
			return false;
		}
	};
   
   
    
    public ModePicker(CameraActivity context) {
        super(context,VIEW_LAYER_TOP);
        initScreenWidth(context);
        step = screenWidth / 15;
        mModeTile = new ArrayList<String>();
        mFilterModeTile = new ArrayList<String>();
        mModeValue = new ArrayList<Integer>();
        
        if (FeatureSwitcher.isGangyunBokehEnable() == false){
        	mFilterModeTile.add(getContext().getString(MODE_TITLEID_BACK[4]));
        }
        /*prize-xuchunming-20171123-arcsoft switch-start*/
        if (FeatureSwitcher.isArcsoftSupperZoomSupported() == false){
        	mFilterModeTile.add(getContext().getString(MODE_TITLEID_BACK[0]));
        }
        if (FeatureSwitcher.isArcsoftNightShotSupported() == false){
        	mFilterModeTile.add(getContext().getString(MODE_TITLEID_BACK[6]));
        }

		if (FeatureSwitcher.isShortVideoModeSupported() == false) {
			mFilterModeTile.add(getContext().getString(MODE_TITLEID_BACK[2]));
		}
        if (FeatureSwitcher.isPortraitModeSupported() == false) {
            mFilterModeTile.add(getContext().getString(MODE_TITLEID_BACK[5]));
        }
		/*prize-xuchunming-20171123-arcsoft switch-end*/
        for(int i = 0; i < MODE_TITLEID_BACK.length; i++){
        	if(mFilterModeTile.contains(getContext().getString(MODE_TITLEID_BACK[i])) == false){
        		mModeTile.add(getContext().getString(MODE_TITLEID_BACK[i]));
           	 	mModeValue.add(MODE_ORDER[i]);
        	}
        }
        setFileter(false);
       
        /*prize-xuchunming-20161223-show info when operation long time in bokehmode-start*/
        mBokehLongTimeInfo = new BokehLongTimeInfo(getContext());
        mBokehLongTimeInfo.startTime();
        /*prize-xuchunming-20161223-show info when operation long time in bokehmode-end*/
    }

    public int getCurrentMode() {
        return mCurrentMode;
    }

    private void setRealMode(int mode) {
        Log.d(TAG, "setRealMode(" + mode + ") mCurrentMode=" + mCurrentMode);

        if (mode == MODE_PHOTO_PIP) {
        }
        // in photo mode, if the hdr, asd, smile shot, gesture shot is on, we
        // should set the current mode is hdr or asd or smile shot. in hdr, asd,
        // smile shot, gesture shot mode if its values is off in
        // sharepreference,
        // we should set the current mode
        // as photo mode
       /* if (mode == MODE_PHOTO || mode == MODE_HDR || mode == MODE_ASD) {
            mode = getRealMode(mModePreference);
        }*/

        if (mCurrentMode != mode) {
        	mPreCurrentMode = mCurrentMode;
            mCurrentMode = mode;
            highlightCurrentMode();
            modeState = MODE_STATE_CHANGING;
            if(isNeedBlur()){
            	showSurfaceCover();
            }else{
            	notifyModeChanged();
            }
            if (mModeToast != null) {
                mModeToast.cancel();
            }
            
            /*prize-xuchunming-20161223-show info when operation long time in bokehmode-start*/
            if(mode == MODE_BOKEH){
            	mBokehLongTimeInfo.resumeTime();
            }else{
            	mBokehLongTimeInfo.pausTime();
            }
            /*prize-xuchunming-20161223-show info when operation long time in bokehmode-end*/
            
        } else {
            // if mode do not change, we should reset ModePicker view enabled
            setEnabled(true);
        }
    }

    public void setCurrentMode(int mode) {
        int realmode = getModeIndex(mode);
        if (getContext().isStereoMode()) {
            if (FeatureSwitcher.isStereoSingle3d()) {
                realmode += OFFSET_STEREO_SINGLE;
            } else {
                realmode += OFFSET_STEREO_PREVIEW;
            }
        }
        Log.i(TAG, "setCurrentMode(" + mode + ") realmode=" + realmode);
        setRealMode(realmode);
    }

    private void highlightCurrentMode() {
    	if(mCurrentMode != MODE_BOKEH && mChsView != null && getModeOrderIndex(mCurrentMode) != -1){
    		mChsView.setSelectIndex(getModeOrderIndex(mCurrentMode));
    	} else if (mCurrentMode == MODE_BOKEH) {
			/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
			mChsView.setSelect(getModeOrderIndex(MODE_PHOTO));
			/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
		}
    }

    public int getModeIndex(int mode) {
        int index = mode % OFFSET;
        Log.d(TAG, "getModeIndex(" + mode + ") return " + index);
        return index;
    }

    public void setListener(OnModeChangedListener l) {
        mModeChangeListener = l;
    }

    @Override
    protected View getView() {
        clearListener();
        View view = inflate(R.layout.mode_menu);
        mChsView = (SelectHorizontalScrollerLayout) view.findViewById(R.id.mode_scrollview);
        HorizontalScrollLayoutAdapter adapter = new HorizontalScrollLayoutAdapter(getContext(), mModeTile, R.layout.photo_mode_item);
        mChsView.setAdapter(adapter);
        applyListener();
        return view;
    }

    @Override
    public void show() {
    	// TODO Auto-generated method stub
    	if(getContext().isNonePickIntent() == true && getContext().getCameraAppUI().canShowMode()){
    		super.show();
    	}
    }
	private void applyListener() {
    	mChsView.setOnItemClickListener(mOnItemChangeListener);
    }

    private void clearListener() {
    	if(mChsView != null){
    		mChsView.setOnItemClickListener(null);
    	}
    }

    public void onClick(int position) {
        setEnabled(false);
        if (getContext().isFullScreen()) {
            setCurrentMode(mModeValue.get(position));
            Log.i(TAG, "onClick,isCameraOpened:" + getContext().isCameraOpened());
            /*prize-xuchunming-20161010-bugid:22263-start*/
            /*if (getContext().isCameraOpened()) {
                setEnabled(true);
            }*/
            /*prize-xuchunming-20161010-bugid:22263-end*/
        } else {
            // if the is not full screen, we should reset PickMode view enable
        	/*prize-xuchunming-20161010-bugid:22263-start*/
        	// setEnabled(true);
        	/*prize-xuchunming-20161010-bugid:22263-end*/
        }
    }

    public void hideToast() {
        Log.i(TAG, "hideToast(), mModeToast:" + mModeToast);
        if (mModeToast != null) {
            mModeToast.hideToast();
        }
    }

    private void notifyModeChanged() {
        if (mModeChangeListener != null) {
            mModeChangeListener.onModeChanged(getCurrentMode());
        }
        
        if(isNeedBlur()){
        	 if(getContext().isSwitchingCamera()){
                 Log.i(TAG, "notifyModeChanged(), delay hideSurfaceCover");
                 /*prize-xuchunming-20180307-bugid:50956-start*/
                 isWaitCameraSwitch = true;
                 /*prize-xuchunming-20180307-bugid:50956-end*/
        		 return;
        	 }
            Log.i(TAG, "notifyModeChanged(), hideSurfaceCover send Handler msg");
        	 mHandler.removeMessages(MSG_HIDE_SURFACE_COVER);
        	 mHandler.sendEmptyMessage(MSG_HIDE_SURFACE_COVER);
        	
        }else{
            Log.i(TAG, "notifyModeChanged(), hideSurfaceCover no send Handler msg");
            modeState = MODE_STATE_CHANG_START;
        }
    }

   
    @Override
    protected void onRefresh() {
    	// TODO Auto-generated method stub
    	super.onRefresh();
    	Log.d(TAG, "onRefresh() mCurrentMode=" + mCurrentMode);
        highlightCurrentMode();
        onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
        if (FeatureSwitcher.isArcsoftFcamSuperZoomSupported()) {
			/* prize-add-set value of arc_front_superzoom -xiaoping-20180313-start */
			if (getContext().getCameraId() == 1 && mCurrentMode == ModePicker.MODE_PHOTO
					&& !getContext().isPicselfieOpen()) {
				getContext().getISettingCtrl().onSettingChanged(SettingConstants.KEY_ARC_FCAM_SUPERZOOM_ENABLE, "on");
			} else {
				getContext().getISettingCtrl().onSettingChanged(SettingConstants.KEY_ARC_FCAM_SUPERZOOM_ENABLE, "off");
			}
			getContext().applyParametersToServer();
			/* prize-add-set value of arc_front_superzoom -xiaoping-20180313-end */
		}
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (mChsView != null) {
        	mChsView.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        mModeToast = null;
    }

   
    public void setModePreference(ListPreference pref) {
        mModePreference = pref;
    }
    
    public GestureListener getGestureListener(){
    	return mGestureListener;
    }
    
    public int getModeOrderIndex(int mode){
		/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
		if (mode == MODE_BOKEH) {
			return mModeValue.indexOf(MODE_PHOTO);
		/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/
		} else {
			return mModeValue.indexOf(mode) < 0 ? -1:mModeValue.indexOf(mode);
		}
    }
    
    public void initScreenWidth(CameraActivity mContext){
    	DisplayMetrics metric = new DisplayMetrics();  
    	mContext. getWindowManager().getDefaultDisplay().getRealMetrics(metric); 
    	screenWidth = metric.widthPixels;
		screenheight = metric.heightPixels;
    }
    
    public void changMode(boolean isNextMode) {
		// TODO Auto-generated method stub
    	int modeIndex; 
		if(isNextMode == true){
			modeIndex = getModeOrderIndex(mCurrentMode) + 1;
		}else{
			modeIndex = getModeOrderIndex(mCurrentMode) - 1;
		}
    	if(mChsView != null && (modeIndex >= mChsView.getCount() || modeIndex < 0)){
    		return ;
    	}
    	setCurrentMode(mModeValue.get(modeIndex));
	}
    
	class BlurAsyncTask extends AsyncTask<Bitmap, Bitmap, Bitmap>{

		protected Bitmap doInBackground(Bitmap... params) {
			// TODO Auto-generated method stub
			return BlurPic.blurScale(params[0]);
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			showSurfaceCover(result);
		}
	}
	

	public Bitmap takeScreenShot()
	{
		Bitmap mBitmap = SurfaceControl.screenshot(screenWidth, screenheight);
		/*prize-xuchunming-20180518-bugid:58341-start*/
		if(mBitmap == null) {
			return null;
		}
		/*prize-xuchunming-20180518-bugid:58341-end*/
		/*prize-modify-the screen is abnormal on takepicture at facebeauty mode-xiaoping-20171018-start*/
		int top = getContext().getPreviewSurfaceView().getBottom() - getContext().getPreviewFrameHeight();
		/*prize-modify-the screen is abnormal on takepicture at facebeauty mode-xiaoping-20171018-end*/
		int bottom = getContext().getPreviewFrameHeight();
		if(((mPreCurrentMode == MODE_PREVIDEO || mPreCurrentMode == MODE_SHORT_PREVIDEO) && modeState == MODE_STATE_CHANGING) && screenheight <= getContext().getPreviewFrameHeight()){
			bottom = bottom - (int)getContext().getResources().getDimension(R.dimen.shutter_group_height);
		}
		Log.i(TAG,"top: "+top+",bottom: "+bottom+",screenWidth: "+screenWidth+",screenheight: "+screenheight);
		return Bitmap.createBitmap(mBitmap, 0, top >= 0 ? top : 0, screenWidth, bottom < screenheight ? bottom : screenheight, null, false);
	}

	public void showSurfaceCover(Bitmap result){
		lastPreviewHeight = getContext().getPreviewFrameHeight();
		if (screenShotLayout == null) {
    		screenShotLayout = (ViewGroup) getContext().inflate(R.layout.surfaceview_screenshot, VIEW_LAYER_SURFACECOVER);
    		screenShotImg = (ImageView) screenShotLayout.findViewById(R.id.sf_screenshot);
    		if (screenShotLayout.getParent() == null) {
				getContext().addView(screenShotLayout, VIEW_LAYER_SURFACECOVER);
			}
    	}
		
		if(screenShotImg != null){
            /*prize-add-bugid:57540 adaptation switching mode animation for 19:9 full screen-xiaoping-20180511-start*/
            if (Math.abs(((double)getContext().getPreviewFrameHeight()/getContext().getPreviewFrameWidth() - SettingUtils.getFullScreenRatio())) <= Util.ASPECT_TOLERANCE) { //19:9
            	adapter199display();
            	mLastBlurBottomMarg = 0;
            }else if (Math.abs(((double)getContext().getPreviewFrameHeight()/getContext().getPreviewFrameWidth() - SettingUtils.getFullScreenRatio())) <= Util.ASPECT_TOLERANCE_16_9){
                adapter169display();
                mLastBlurBottomMarg = 0;
			} else {
                adapter43display();
                mLastBlurBottomMarg = getContext().getResources().getDimension(R.dimen.shutter_group_height);
            }
            /*prize-add-bugid:57540 adaptation switching mode animation for 19:9 full screen-xiaoping-20180511-end*/
            screenShotImg.setImageBitmap(result);
            Log.d(TAG, "showSurfaceCover showSurfaceCover");
			Animation mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.surfacecover_show);
			mAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					Log.d(TAG, "showSurfaceCover onAnimationStart screenShotLayout = " + screenShotLayout);					
					screenShotLayout.setAlpha(1.0f);
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					Log.d(TAG, "showSurfaceCover onAnimationEnd");
					/*prize-xuchunming-20180403-bugid:54203-start*/
					mIsShowCoverAnimat = false;
					/*prize-xuchunming-20180403-bugid:54203-end*/
					screenShotLayout.setBackgroundResource(R.color.black);
					notifyModeChanged();
				}
			});
			/*prize-xuchunming-20180403-bugid:54203-start*/
			mIsShowCoverAnimat = true;
			/*prize-xuchunming-20180403-bugid:54203-end*/
			screenShotImg.startAnimation(mAnimation);
		}
	}
	
	public void hideSurfaceCover(){
		/*prize-xuchunming-20161109-bugid:24293-start*/
		if(screenShotImg == null){
			Log.d(TAG, "hideSurfaceCover screenShotImg == null return");
			modeState = MODE_STATE_CHANG_START;
			return;
		}
		/*prize-xuchunming-20161109-bugid:24293-end*/
		Animation mAnimation = getScalAnimation() ;
		if(mAnimation == null){
			mAnimation = (AnimationSet)AnimationUtils.loadAnimation(getContext(), R.anim.surfacecover_hide);
			screenShotLayout.setBackgroundResource(R.color.alpht);
		}
		
        Log.d(TAG, "hideSurfaceCover hideSurfaceCover");
		mAnimation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				Log.d(TAG, "hideSurfaceCover onAnimationStart");
				getContext().getCameraActor().onModeBlueDone();
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Log.d(TAG, "hideSurfaceCover onAnimationEnd screenShotLayout = " + screenShotLayout);
				screenShotLayout.setAlpha(0.0f);
				screenShotLayout.setBackgroundResource(R.color.alpht);
				modeState = MODE_STATE_CHANG_START;
				if(getContext().getCameraAppUI().getViewState() != ViewState.VIEW_STATE_RECORDING){
					getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_NORMAL);
				}
				
				setEnabled(true);
			}
		});
		
		if(screenShotImg != null){
			screenShotImg.startAnimation(mAnimation);
		}
	}
	
	
	private ScaleAnimation getScalAnimation() {
		// TODO Auto-generated method stub
		Log.d(TAG,"ScaleAnimation,getPreviewFrameHeight: "+getContext().getPreviewFrameHeight()+",lastPreviewHeight: "+lastPreviewHeight+",mBlurBottomMarg: "+mBlurBottomMarg);
		if(getContext().getPreviewFrameHeight() != (int)lastPreviewHeight){
			/**
			 * Scale big:
			 *    lastPreviewHeight*pivotXValue*(getPreviewFrameHeight()/lastPreviewHeight) = lastBlurBottomMarg + lastPreviewHeight*pivotXValue;
			 *    pivotXValue = lastBlurBottomMarg / (lastPreviewHeight*(getPreviewFrameHeight()/lastPreviewHeight) -1));
			 * 
			 * Scale small:
			 *    lastPreviewHeight*pivotXValue*(getPreviewFrameHeight()/lastPreviewHeight) = lastPreviewHeight*pivotXValue - lastBlurBottomMarg;
			 *    pivotXValue = lastBlurBottomMarg / (lastPreviewHeight*(getPreviewFrameHeight()/lastPreviewHeight) + 1));
			 * 
			 */
			float pivotXValue;
			float pivotYValue;
			float toY;
			
			if (Math.abs((getContext().getPreviewSurfaceView().getAspectRatio() - SettingUtils.getFullScreenRatio())) <= Util.ASPECT_TOLERANCE){ // != 16:9
				mBlurBottomMarg = getContext().getResources().getDimension(R.dimen.shutter_group_height);
			}else{
				mBlurBottomMarg = Math.abs(getContext().getResources().getDimension(R.dimen.shutter_group_height) - mLastBlurBottomMarg) ;
			}
			if(getContext().getPreviewFrameHeight() > lastPreviewHeight){ //Scale big:
				toY = getContext().getPreviewFrameHeight() / lastPreviewHeight;
				pivotXValue = mBlurBottomMarg / (lastPreviewHeight*(toY -1));
				/*prize-add-adjust the animation effect-xiaoping-20170927-start*/
				pivotYValue = SCALLTOBIG_PIVOTYVALUE;
			}else{ // Scale small:
				toY = getContext().getPreviewFrameHeight() / lastPreviewHeight;
				pivotXValue = mBlurBottomMarg / (lastPreviewHeight*(1-toY));
				pivotYValue = SCALLTOSMALL_PIVOTYVALUE;
			}

			ScaleAnimation mScaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, toY,
		             Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,pivotYValue);
			/*prize-add-adjust the animation effect-xiaoping-20170927-end*/
			mScaleAnimation.setDuration(200);
			mScaleAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
			return mScaleAnimation;
		}
		return null;
	}

	public void adapter43display(){
    	LayoutParams lp = (LayoutParams) screenShotImg.getLayoutParams();
		lp.width = getContext().getPreviewFrameWidth();
		lp.height = getContext().getPreviewFrameHeight();
		if(mPreCurrentMode == MODE_BOKEH && ((PhotoActor)((CameraActivity)getContext()).getCameraActor()).getICameraMode() instanceof GyBokehMode){
			GyBokehMode bokehMode = (GyBokehMode)((PhotoActor)((CameraActivity)getContext()).getCameraActor()).getICameraMode();
			lp.bottomMargin = ((FrameLayout.LayoutParams)bokehMode.getGlSurfaceView().getLayoutParams()).bottomMargin;
		}else{
			lp.bottomMargin = ((FrameLayout.LayoutParams)getContext().getPreviewSurfaceView().getLayoutParams()).bottomMargin;
		}
		lp.topMargin = 0;
		Log.d(TAG, "adapter43display,lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",mPreCurrentMode:"+mPreCurrentMode);
    	screenShotImg.setLayoutParams(lp);
    }
	
	public void adapter169display(){
    	LayoutParams lp = (LayoutParams) screenShotImg.getLayoutParams();
		lp.width = getContext().getPreviewFrameWidth();
		lp.height = getContext().getPreviewFrameHeight();
		lp.bottomMargin = 0;
		lp.topMargin = (int)getContext().getResources().getDimension(R.dimen.preview_surfaceview_margin);
        Log.d(TAG, "adapter169display,lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",mPreCurrentMode:"+mPreCurrentMode);
		screenShotImg.setLayoutParams(lp);
    }

    /*prize-add-bugid:57540 adaptation switching mode animation for 19:9 full screen-20180511-start*/
    public void adapter199display(){
        LayoutParams lp = (LayoutParams) screenShotImg.getLayoutParams();
        lp.width = getContext().getPreviewFrameWidth();
        lp.height = getContext().getPreviewFrameHeight();
        lp.bottomMargin = 0;
        lp.topMargin = 0;
        Log.d(TAG, "adapter199display,lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",mPreCurrentMode:"+mPreCurrentMode);
        screenShotImg.setLayoutParams(lp);
    }
    /*prize-add-bugid:57540 adaptation switching mode animation for 19:9 full screen-20180511-end*/

	public boolean isNeedBlur(){
        boolean isNeedBlur = true;
		if(getContext().isRestoring() == true ){
            isNeedBlur = false;
        }
		
		if(mCurrentMode == MODE_VIDEO){
            isNeedBlur = false;
		}
		
		if((mCurrentMode == MODE_PREVIDEO || mCurrentMode == MODE_SHORT_PREVIDEO) && mPreCurrentMode == MODE_VIDEO){
            isNeedBlur = false;
		}
        Log.d(TAG, "isNeedBlur : " + isNeedBlur);
		return isNeedBlur;
	}
	
	public void showSurfaceCover(){
		modeState = MODE_STATE_CHANGING;
		getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_SHOW_BLUR);
		BlurAsyncTask mBlurAsyncTask = new BlurAsyncTask();
    	mBlurAsyncTask.execute(takeScreenShot());
	}
	
	/*prize-xuchunming-adjust layout at 18:9 project-start*/
    public void onChangeNavigationBar(boolean isShow){/*
		if(mChsView != null){
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)mChsView.getLayoutParams();
		    if(isShow == true){
		    	if(lp.bottomMargin != (int)getContext().getResources().getDimension(R.dimen.shutter_group_height)){
		    		lp.bottomMargin = (int)getContext().getResources().getDimension(R.dimen.shutter_group_height);
			    	mChsView.setLayoutParams(lp);
			    	mChsView.setBackgroundResource(R.color.shutter_video_background);
		    	}
		    }else{
		    	if(lp.bottomMargin != (int)getContext().getResources().getDimension(R.dimen.shutter_group_height) - lp.height){
		    		lp.bottomMargin = (int)getContext().getResources().getDimension(R.dimen.shutter_group_height) - lp.height;
			    	mChsView.setLayoutParams(lp);
			    	mChsView.setBackgroundResource(R.color.alpht);
		    	}
		    }
		}
	*/}
    /*prize-xuchunming-adjust layout at 18:9 project-end*/
	
	public void switchCamera(int cameraid){

		/*prize-add-bug:40356、40685 The other application calls the camera ,camera mode display exception-xiaoping-20171016-start*/
		if (mModeTile != null) {
			if ((mModeTile.size() == MODE_ORDER.length && cameraid == 0) || (mModeTile.size() == MODE_ORDER_FRONT.length && cameraid == 1) ) {
				return;
			}
		}
		/*prize-add-bug:40356、40685 The other application calls the camera ,camera mode display exception-xiaoping-20171016-end*/
		
		if(mModeTile == null){
			mModeTile = new ArrayList<String>();
			mModeValue = new ArrayList<Integer>();
		}else{
			mModeTile.clear();
			mModeValue.clear();
		}
		
		int titleId[];
		int valueId[]; //arc add 
		if(cameraid == 0){
			titleId = MODE_TITLEID_BACK;
			valueId = MODE_ORDER; //arc add 
		}else{
			titleId = MODE_TITLEID_FRONT;
			valueId = MODE_ORDER_FRONT; //arc add 
		}
		
		for(int i = 0; i < titleId.length; i++){
			if(mFilterModeTile.contains(getContext().getString(titleId[i])) == false){
				mModeTile.add(getContext().getString(titleId[i]));
	        	mModeValue.add(valueId[i]); //arc add 
        	}
        	 
        }
		
		if(mChsView != null){
			HorizontalScrollLayoutAdapter adapter = new HorizontalScrollLayoutAdapter(getContext(), mModeTile, R.layout.photo_mode_item);
	        mChsView.setAdapter(adapter);
		}
	}
	
    /*prize-xuchunming-20160919-add double camera activity boot-start*/
	public void setDefaultMode(int mode){
		mCurrentMode = mode;
	}
	/*prize-xuchunming-20160919-add double camera activity boot-end*/

	public PreviewCallbackListen getPreviewCallbackListen() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Bitmap getCameraPreviewData(){
		return takeScreenShot();
	}
	
	
	/*prize-xuchunming-20161105-hide modechange blur effect when app exit-start*/
	public void onStop() {
		// TODO Auto-generated method stub
		/*prize-xuchunming-20170809-bugid:36748-start*/
		if(screenShotLayout != null && screenShotLayout.getAlpha() == 1){
		/*prize-xuchunming-20170809-bugid:36748-end*/
			if(screenShotLayout != null){
				Log.d(TAG, "onStop screenShotLayout = " + screenShotLayout);
				screenShotLayout.setAlpha(0.0f);
				screenShotLayout.setBackgroundResource(R.color.alpht);
			}
			modeState = MODE_STATE_CHANG_START;
			getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_NORMAL);
			setEnabled(true);
		}
		
		/*prize-xuchunming-20161223-show info when operation long time in bokehmode-start*/
		mBokehLongTimeInfo.pausTime();
		/*prize-xuchunming-20161223-show info when operation long time in bokehmode-end*/
		
	}
	/*prize-xuchunming-20161105-hide modechange blur effect when app exit-end*/
	
	
	/*prize-xuchunming-20161223-show info when operation long time in bokehmode-start*/
	public void onResume(){
		if(mCurrentMode == MODE_BOKEH){
			mBokehLongTimeInfo.resumeTime();
		}
		
	}
	
	public void onDestory(){
		mBokehLongTimeInfo.stopTime();
	}
	/*prize-xuchunming-20161223-show info when operation long time in bokehmode-end*/
	
	/*prize-xuchunming-20171216-bugid:44558-start*/
	public void onpause() {
		if(getContext().getCameraAppUI().getViewState() == ViewState.VIEW_STATE_MVFOCUSING) {
			Log.d(TAG, "force set VIEW_STATE_NORMAL when CurrentViewState == VIEW_STATE_MVFOCUSING");
			getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_NORMAL);
		}
	}
	/*prize-xuchunming-20171216-bugid:44558-end*/
	
	/*prize-xuchunming-20180307-bugid:50956-start*/
	public void setWaitCameraSwitch(boolean isWaitCameraSwitch) {
		this.isWaitCameraSwitch = isWaitCameraSwitch;
	}
	
	public boolean isWaitCameraSwitch() {
		return isWaitCameraSwitch;	
	}
	/*prize-xuchunming-20180307-bugid:50956-end*/
	
	
	/*prize-xuchunming-20180403-bugid:54203-start*/
	public boolean isShowCoverAnimat() {
		return mIsShowCoverAnimat;
	}
	/*prize-xuchunming-20180403-bugid:54203-end*/
}
