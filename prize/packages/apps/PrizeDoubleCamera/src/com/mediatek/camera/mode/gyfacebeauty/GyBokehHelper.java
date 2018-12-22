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

package com.mediatek.camera.mode.gyfacebeauty;

import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import com.android.camera.R;
import com.android.camera.bridge.CameraAppUiImpl;
import com.android.camera.manager.CombinViewManager;
import com.mediatek.camera.AdditionManager;
import com.mediatek.camera.ICameraAddition;
import com.mediatek.camera.ICameraContext;
import com.mediatek.camera.mode.CameraMode;
import android.graphics.ImageFormat;
import com.mediatek.camera.platform.ICameraAppUi.SpecViewType;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice.AutoFocusMvCallback;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice.cFbOriginalCallback;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IFocusManager;
import com.mediatek.camera.platform.IFileSaver.FILE_TYPE;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.util.Log;
import com.mediatek.camera.util.Util;
import android.content.Context;
import android.app.Activity;
import junit.framework.Assert;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

import java.io.IOException;
import com.mediatek.camera.platform.ICameraDeviceManager;
import com.mediatek.camera.platform.ICameraDeviceManager.ICameraDevice;
import android.hardware.Camera.Size;
import com.android.camera.CameraActivity;
import android.hardware.Camera.PreviewCallback;
import com.android.camera.CameraManager;
import android.opengl.GLSurfaceView;
import com.android.camera.ui.PickerButton;
import com.android.camera.ui.PreviewSurfaceView;
import com.android.camera.ui.PickerButton.Listener;
import com.android.prize.DoubleCameraFocusIndicator;
import com.android.prize.IBuzzyStrategy;
import com.android.prize.DoubleCameraFocusIndicator.OnSeekBarChangeListener;
import com.gangyun.camera.gangyunCameraAperture;
import android.widget.RelativeLayout;
import android.widget.FrameLayout;
import android.widget.SeekBar;


import com.gangyun.camera.GYImage;
import com.gangyun.camera.GYSurfaceView;
import android.graphics.Matrix;
import android.widget.TextView;


public class GyBokehHelper implements gangyunCameraAperture.OnGyProgressChangedListener{
    private static final String TAG = "GYLog GyBokehHelper";
    private CameraActivity mContext;
    private Parameters mParameters;
    private Handler mHandler;
	private static GyBokehHelper mGyBokehHelper;
    private GYImage mGYImage;
    private GYSurfaceView mGLSurfaceView;
	private TextView mCloseTv;
    private FrameLayout mCurSurfaceViewLayout;
	private int gyPreviewW=0,gyPreviewH=0,gyx=0,gyy=0,gylevel=80,gyradius=40,gynBoderPower=50,gylevelMax=100;
	private int mFrameWidth, mFrameHeight;
	private boolean isOpen = false;
	private DoubleCameraFocusIndicator mDoubleCameraFocusIndicator;
	private boolean isHide = false;

	private gangyunCameraAperture gyCameraAperture = null;

	
	/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
    private PickerButton mBgFuzzySwitch = null;
    private FrameLayout mBgFuzzySwitchLayout = null;
    private boolean isBgFuzzySwitchOn = true;
    private IBuzzyStrategy mBuzzyStrategy;
    private BgFuzzySwitchListeren mBgFuzzySwitchListeren;
    public  interface BgFuzzySwitchListeren{
    	public void onBgFuzzySwitch(boolean isOpen);
    }
    private boolean mIsCoverSecondaryCamera = false;
    
    /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
    public GyBokehHelper(CameraActivity context) {
    	Log.d(TAG, "new GyBokehHelper :"+context);
        mContext = context;
    }
	
	public static GyBokehHelper getInstance(CameraActivity context){		
		 Log.i(TAG, "getInstance ");
		if (mGyBokehHelper == null){
			mGyBokehHelper = new GyBokehHelper(context);
		}/*else{
			mGyBokehHelper.setGyBokehContext(context);
		}*/
		return mGyBokehHelper;
	}
	
	/*public void setGyBokehContext(CameraActivity context){
		Log.d(TAG, "setGyBokehContext :"+context);
		mContext = context;
	}*/
	
	public static void gyBokehrelease(){
		Log.i(TAG, " gyBokehrelease ");
		mGyBokehHelper = null;
	}
	
	private void setGyContext(CameraActivity context){
	    Log.i(TAG, "setGyContext");
		mContext = context;
		mGYImage = new GYImage(mContext);
	    /*prize-xuchunming-20161202-start*/
	    //gyCameraAperture =  (gangyunCameraAperture)mContext.findViewById(R.id.gyCameraAperture);
		/*prize-xuchunming-20161202-end*/
	
	}


	public boolean isBokenOpen(){
		if(mGYImage != null)
			return true;
		else
			return false;

	}
	public void gyBokehShow(){
	  Log.i(TAG, "gyBokehShow isOpen:"+isOpen);
	  isHide = false;
	  if(isOpen ){	  	
	  	  return ;
	  }
	  isOpen = true;
	  
	  refreshView();
      setGyContext(mContext);
	  mGYImage.SetScanType(3);
	  int mmode = mContext.getCameraDevice().getParameters().getPreviewFormat();
	  int yuvmode = 0;
	  if(mmode == ImageFormat.YV12){
			yuvmode = 3;
	  }
	  else if(mmode == ImageFormat.NV21){
			yuvmode = 2;
	  }
	  Log.i(TAG, "getPreviewFormat=" + mmode+"yuvmode:"+yuvmode);
	  mGYImage.SetCameraYUVMode(yuvmode);
	  Log.i(TAG, "gyshow 11");
	
		mContext.getCameraDevice().setPreviewDisplayAsync(null);
		//xucm
		//((com.android.camera.actor.PhotoActor)mContext.getCameraActor()).stopPreview();
		initBlurParameters();
		mGYImage.setGLSurfaceView(mGLSurfaceView);
		mGYImage.SetCameraID(0);
		mGYImage.SetDegree(mContext.getDisplayOrientation());

		mGYImage.setUpCamera(mContext.getCameraDevice().getCamera().getInstance(), mContext.getDisplayOrientation(), false, false, mContext.getModePicker(), mBuzzyStrategy);
		//xucm
		//((com.android.camera.actor.PhotoActor)mContext.getCameraActor()).restartPreview(false);
			
		mGLSurfaceView.setAspectRatio((double)gyPreviewW/(double)gyPreviewH);
		mGLSurfaceView.onResume();
		mGYImage.requestRender();
		
}
	
public void gyBokehSetPreviewCallback(){
	if (mGYImage != null && mContext.getCameraDevice() != null && mContext.getCameraDevice().getCamera() != null && mContext.getCameraDevice().getCamera().getInstance() != null){
	    mGYImage.setGyImagePreviewCallback(mContext.getCameraDevice().getCamera().getInstance());
	}
}

public void showApertureView(int x,int y){
	Log.i(TAG, "showApertureView x:"+x + " y:"+y);
    if(isHide){
       return;
	}
    if(gyCameraAperture != null){
		gyCameraAperture.gyShowView();
		RelativeLayout.LayoutParams ps =(RelativeLayout.LayoutParams)gyCameraAperture.getLayoutParams();
		int h =gyCameraAperture.getHeight();
		int w = gyCameraAperture.getWidth();
		ps.setMargins((int)x -w/2,	(int)y-h/2, 0, 0);

		int[] rules = ps.getRules();
		rules[RelativeLayout.CENTER_IN_PARENT] =0;
		gyCameraAperture.requestLayout();
    }
}

public void gyBokehHide(){
	Log.i(TAG, "gyBokehHide");
	isHide = true;
	if(gyCameraAperture != null){
		gyCameraAperture.gyViewHide();
	}
	detachSurfaceViewLayout();		
	if(mDoubleCameraFocusIndicator != null){
		mDoubleCameraFocusIndicator.hide();
	}
	removeBgFuzzySwitch();
	   
	//mContext.getPreviewSurfaceView().setVisibility(android.view.View.VISIBLE);
	mContext.getFocusManager().getFocusLayout().setVisibility(View.VISIBLE);
	
}


public void gyBokehclose(boolean needStartPrview){
	Log.i(TAG, "gyBokehclose needStartPrview:"+needStartPrview + " isOpen:"+isOpen + " mGLSurfaceView:"+mGLSurfaceView);
	if(mGLSurfaceView != null && isOpen){
		mGLSurfaceView.onPause();
		mGYImage.gyImagestop();
		if (mContext.getCameraDevice() != null && mContext.getCameraDevice().getCamera() != null){
			   //mContext.getCameraDevice().setPreviewDisplayAsync(mContext.getPreviewSurfaceView().getHolder());
			   try{
					mContext.getCameraDevice().getCamera().getInstance().setPreviewTexture(null);
			   }catch(IOException e){
				   //
			   } 
		}
	    gyBokehHide();
		isOpen = false;
		/*if (needStartPrview && mContext.getCameraActor() instanceof com.android.camera.actor.PhotoActor){
		   if (mContext.getCameraDevice() != null && mContext.getCameraDevice().getCamera() != null){
			   mContext.getCameraDevice().setPreviewDisplayAsync(mSurfaceView.getHolder());
			   try{
					mContext.getCameraDevice().getCamera().getInstance().setPreviewTexture(null);
			   }catch(IOException e){
				   //
			   } 
			   ((com.android.camera.actor.PhotoActor)mContext.getCameraActor()).restartPreview(true);
		   }
		}
		else{
			if (mContext.getCameraDevice() != null && mContext.getCameraDevice().getCamera() != null){
				mContext.getCameraDevice().getCamera().getInstance().setPreviewCallback(null);

			}

		}*/
		/*if (needStartPrview){
			mContext.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGLSurfaceView.setVisibility(android.view.View.GONE);			 				
				    mSurfaceView.setVisibility(android.view.View.VISIBLE);
				
				}
			});
		}*/
   }
   if(needStartPrview == true){
		 resetSettingItem();
   }
  
}

/*prize-xuchunming-20161202-start*/
public void onSingleTapUp(View v, int x, int y){ //add one param :View;
/*prize-xuchunming-20161202-end*/
	Log.i(TAG, "onSingleTapUp x:"+x+" y:"+y);
	if(isBgFuzzySwitchOn == true){
		gyx = x;
		gyy = y;
		if(mGLSurfaceView != null && mGYImage != null){
			mGYImage.setPos(x, y);
		}
		/*prize-xuchunming-20161202-start*/
		if(mDoubleCameraFocusIndicator != null){
			mDoubleCameraFocusIndicator.onSinlgeTapUp(v, x, y);
		}
		/*prize-xuchunming-20161202-end*/
	}

}

public int getRadiu(){
	 return gyradius;
}

public int getPower(){
	return gylevel;
}

public int getBorderPower(){
	return gynBoderPower;
}

public int getPosX(){
	return gyx;
}

public int getPosY(){
	return gyy;
}

public int getFrameWidth(){
	return mFrameWidth;
}

public int getFrameHeight(){
	return mFrameHeight;
}


@Override
public void onGyProgressChanged(int arg1) {
	// TODO Auto-generated method stub
	Log.i(TAG, "onGyProgressChanged arg1:"+arg1);
   if(mGYImage != null){
   	   gylevel = arg1;
	   mGYImage.setParameter(gyradius,gylevel,gynBoderPower);
   }
		
}

  private void initBlurParameters(){
  	   Log.i(TAG, "initBlurParameters");
	   if (mContext.getParameters() != null){
		   Size mgyPreviewSize = mContext.getParameters().getPreviewSize();		   
		   gyPreviewW = mgyPreviewSize.width;
		   gyPreviewH = mgyPreviewSize.height;
		   mFrameWidth =  mContext.getPreviewFrameWidth();
		   mFrameHeight = mContext.getPreviewFrameHeight();		   
		   
		   gyx = mFrameWidth/2;
		   gyy = mFrameHeight/2;
		   mGYImage.setPos(gyx, gyy);
		   /*prize-xuchunming-20161202-start*/
		   int progress = Integer.valueOf(mContext.getListPreference(SettingConstants.KEY_APERTURE).getValue());
		   setGylevel(progress);
		   setGyradius(progress);
		   if(isBgFuzzySwitchOn == true){
		   /*prize-xuchunming-20161202-end*/
			   mGYImage.setParameter(gyradius,gylevel,gynBoderPower);
		   /*prize-xuchunming-20161202-start*/
		   }else{
			   mGYImage.setParameter(100,0,0);
		   }
		   /*prize-xuchunming-20161202-end*/
		   Log.i(TAG, "mGLSurfaceView is w:"+gyPreviewW+" h:"+gyPreviewH+" FrameWidth:"+ mFrameWidth + " FrameHeight:" + mFrameHeight);
	   }
  }

  /*prize-xuchunming-20161202-start*/
  public void initDoubleCameraFocusIndicator(final CameraActivity mContext){
	    mDoubleCameraFocusIndicator = (DoubleCameraFocusIndicator) mContext
				.findViewById(R.id.double_camera_rotate_layout);
	    mDoubleCameraFocusIndicator.initUi();
		mDoubleCameraFocusIndicator.setOrientation(90, false);
		mDoubleCameraFocusIndicator.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
					if(mIsCoverSecondaryCamera == true){
						Log.i(TAG, "cancel set gy-bokeh-radius/level value when secondary isCovered");
						return;
					}
					
					if (mContext.getParameters() != null) {
						Log.i(TAG, "set gy-bokeh-radius: " + progress);
						setGylevel(progress);
						setGyradius(progress);
						mGYImage.setParameter(gyradius,gylevel,gynBoderPower);
					} else {
						Log.i(TAG, "set gy-bokeh-radius error ");
					}
		
			}
		});
		
		if(isBgFuzzySwitchOn == true){
			mContext.getFocusManager().getFocusLayout().setVisibility(View.GONE);
			mDoubleCameraFocusIndicator.startShow();
		}
  	}
  
    private int progressMapping(int index){
		if(index > 0){
			if(index < 10){
				return index*4;
			}else{
				return (int) (40 + (index-10)/1.5);
			}
		}
		return 0;
	}
    public void onAutoFocusing() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null && isBgFuzzySwitchOn() == true){
			mDoubleCameraFocusIndicator.onAutoFocusing();
		}
	}

    public void onAutoFocused() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null && isBgFuzzySwitchOn() == true){
			mDoubleCameraFocusIndicator.onAutoFocused();
		}

	}

    public void onMvFocused() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null && isBgFuzzySwitchOn() == true){
			mDoubleCameraFocusIndicator.onMvFocused();
		}
	}

    public void onMvFocusing() {
		// TODO Auto-generated method stub
		if(mDoubleCameraFocusIndicator != null && isBgFuzzySwitchOn() == true){
			mDoubleCameraFocusIndicator.onMvFocusing();
		}
	}

    public void addBgFuzzySwitch(){
		if (mBgFuzzySwitchLayout == null) {
			FrameLayout mViewLayerTop = (FrameLayout) mContext.findViewById(R.id.view_layer_top);
			mBgFuzzySwitchLayout = (FrameLayout) mContext.getLayoutInflater().inflate(R.layout.bg_fuzzy_switch, null);
			mBgFuzzySwitch = (PickerButton) mBgFuzzySwitchLayout.findViewById(R.id.bg_fuzzy_switch);
			IconListPreference pref = (IconListPreference) mContext.getListPreference(SettingConstants.KEY_BG_FUZZY);
			pref.showInSetting(false);
			mBgFuzzySwitch.initialize(pref);
			mBgFuzzySwitch.refresh();
			isBgFuzzySwitchOn = pref.getValue().equals("on") ? true:false;
			mBgFuzzySwitch.setListener(new Listener() {
				
				@Override
				public boolean onPicked(PickerButton button, ListPreference preference,
						String newValue) {
					// TODO Auto-generated method stub
					mBgFuzzySwitch.setClickable(false);
					if(mBgFuzzySwitchListeren != null){
						mBgFuzzySwitchListeren.onBgFuzzySwitch(newValue.equals("on"));
					}
					if(newValue.equals("on")){
						openBlur();
					}else{
						closeBlur();
					}
					mBgFuzzySwitch.setClickable(true);
					return true;
				}
			});
			
			mViewLayerTop.addView(mBgFuzzySwitchLayout);
		}
		
	}
	
	public void removeBgFuzzySwitch(){
		if (mBgFuzzySwitchLayout != null) {
			FrameLayout mViewLayerTop = (FrameLayout) mContext.findViewById(R.id.view_layer_top);
			mViewLayerTop.removeView(mBgFuzzySwitchLayout);
			mBgFuzzySwitchLayout = null;
		}
	}
	
	private void closeBlur() {
		// TODO Auto-generated method stub
		isBgFuzzySwitchOn = false;
		mGYImage.setParameter(100,0,0);
		mDoubleCameraFocusIndicator.hide();
		mContext.getFocusManager().getFocusLayout().setVisibility(View.VISIBLE);
	}

	private void openBlur() {
		// TODO Auto-generated method stub
		isBgFuzzySwitchOn = true;
		mGYImage.setParameter(gyradius,gylevel,gynBoderPower);
		mContext.getFocusManager().getFocusLayout().setVisibility(View.GONE);
		mDoubleCameraFocusIndicator.show();
	}
	
	public boolean isBgFuzzySwitchOn(){
		return isBgFuzzySwitchOn;
	}
	private void setGylevel(int progress){
		gylevel = progressMapping(progress);
	}
	
	private void setGyradius(int progress){
		gyradius = 100 - progress;
		if (gyradius < 30) {
			gyradius = 30;
		}
	}
	
	public void resetSettingItem(){
		IconListPreference pref = (IconListPreference) mContext.getListPreference(SettingConstants.KEY_BG_FUZZY);
		if(pref != null){
			pref.setValue("on");
		}
	}
	/*prize-xuchunming-20161202-end*/
	
	public void setmBuzzyStrategy(IBuzzyStrategy mBuzzyStrategy){
		this.mBuzzyStrategy = mBuzzyStrategy;
	}
	
	/*prize-xuchunming-20161202-start*/
	public void refreshView(){
		
		mContext.detachSurfaceViewLayout();
		mContext.getPreviewSurfaceView().setVisibility(android.view.View.GONE);
		attachSurfaceViewLayout();
		addBgFuzzySwitch();
		initDoubleCameraFocusIndicator(mContext);
	}
	/*prize-xuchunming-20161202-end*/
	
	public void attachSurfaceViewLayout() {
		Log.i(TAG, "[attachSurfaceViewLayout] begin ");

		if (mGLSurfaceView == null) {
			FrameLayout surfaceViewRoot = (FrameLayout) mContext.findViewById(R.id.camera_surfaceview_root);
			mCurSurfaceViewLayout = (FrameLayout) mContext.getLayoutInflater()
					.inflate(R.layout.bokeh_camera_preview_layout, null);
			/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
			mCloseTv = (TextView) mCurSurfaceViewLayout.findViewById(R.id.tv_close_slr);
			mCloseTv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG,"close slr");
					mContext.getCameraAppUI().closeCombinView(CombinViewManager.COMBIN_GYBOKEH);
				}
			});
			/*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/
			mGLSurfaceView = (GYSurfaceView) mCurSurfaceViewLayout.findViewById(R.id.gy_glsurfaceView);
			mGLSurfaceView.setOnTouchListener(new View.OnTouchListener() {  
				  
				public boolean onTouch(View v, MotionEvent event) {  
					// TODO Auto-generated method stub	
					Log.i(TAG, "onTouch");
					mContext.getGestureRecognizer().onTouchEvent(event);
					return true;  
				}  
			}); 
		    mGLSurfaceView.getHolder().addCallback(new Callback() {
				
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					// TODO Auto-generated method stub
					if(mContext.getCameraAppUI() != null){
						((CameraAppUiImpl)mContext.getCameraAppUI()).onSizeChanged(width, height);
					}
					
				}
			});
			
			surfaceViewRoot.addView(mCurSurfaceViewLayout);
		}
		Log.i(TAG, "mSecondaryCamera [attachSurfaceViewLayout] end ");
	}
	
	public void detachSurfaceViewLayout() {
		Log.i(TAG, "[detachSurfaceViewLayout] begin ");

		if (mGLSurfaceView != null) {
			FrameLayout surfaceViewRoot = (FrameLayout) mContext.findViewById(R.id.camera_surfaceview_root);
			surfaceViewRoot.removeView(mCurSurfaceViewLayout);
			mGLSurfaceView = null;
		}
		Log.i(TAG, "[detachSurfaceViewLayout] end ");
	}
	
	public GYSurfaceView getGlSurfaceView(){
		return mGLSurfaceView;
	}
	
	public void setBgFuzzySwitchListeren(BgFuzzySwitchListeren mBgFuzzySwitchListeren){
		this.mBgFuzzySwitchListeren = mBgFuzzySwitchListeren;
	}
	
	public void setIsCoverSecondaryCamera(boolean isCover){
		mIsCoverSecondaryCamera = isCover;
	}
}
