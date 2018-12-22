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

package com.mediatek.camera.mode.facebeauty;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.FrameLayout.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.R;
import com.android.prize.FaceBeautyCoverView;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.SettingUtils;
import com.mediatek.camera.ui.CameraView;
import com.mediatek.camera.util.Log;
import com.mediatek.camera.util.Util;
import com.prize.setting.NavigationBarUtils;
/*prize-xuchunming-201804026-add spotlight-start*/ 
import com.prize.ui.SpotLight;
import com.android.camera.FeatureSwitcher;
import com.android.camera.ui.RotateImageView;
/*prize-xuchunming-201804026-add spotlight-end*/ 
/**
 * The FaceBeautyView.java equals former :FaceBeautyIndicatorManager.java
 * 
 */
public class FaceBeautyView extends CameraView implements OnClickListener, OnSeekBarChangeListener{
    private static final String TAG = "FaceBeautyView";
    
    
    /**
     * when FaceBeautyMode send MSG show the FB icon
     * but if current is in setting change, FaceBeautyMode will receive a parameters Ready MSG
     * so will notify view show.but this case is in the setting,not need show the view
     * if the msg:ON_CAMERA_PARAMETERS_READY split to ON_CAMERA_PARAMETERS_READY and ON_CAMERA_PARAMETERS_CHANGE
     * and the change MSG used for setting change,so can not use mIsShowSetting
     */
    private boolean mIsShowSetting = false;
    private boolean mIsInPictureTakenProgress = false;
    
    /**
     * this tag is used for judge whether in camera preview
     * for example:camera -> Gallery->play video,now when play the video,camera will
     * execute onPause(),and when the finished play,camera will onResume,so this time
     * FaceBeautyMode will receive onCameraOpen and onCameraParameters Ready MSG,so will
     * notify FaceBeautyView ,but this view will show the VFB UI,so in this case[not in Camera preview]
     * not need show the UI
     * if FaceBeautyView not show the UI,so this not use
     */
    private boolean mIsInCameraPreview = true;
    
   

    private ICameraAppUi mICameraAppUi;
    private IModuleCtrl mIModuleCtrl;
    
    private FaceBeautyInfo mFaceBeautyInfo;
    private View mView;
    
    /*prize-xuchunming-20160519-add fotonation face beauty-start*/
    private CameraActivity mContext;
    
    private RelativeLayout beautiful_layout;
	private RelativeLayout beautiful_contain;
	
	private LinearLayout compositeBeautiful;
	private LinearLayout smoothingBeautiful;
	private LinearLayout slimmingBeautiful;
	private LinearLayout catchlightBeautiful;
	private LinearLayout eyesEnlargementBeautiful;
	
	private String currentKey;
	private int currentValue;
	    
	private static final double SMOTTINGMAXVALUE = 200;
	private static final double SLIMMINGMAXVALUE = 96;//128;
	private static final double TONINRMAXVALUE = 123;
	private static final double EYESENLARGVALUE = 250;
	private String defaultValue;
	    
	private double step;
	private SeekBar mSeekBar;
	
    private View focuseView;
    /*xucm-20160122-bugid:12679-end*/
	/*prize-xuchunming-20160519-add fotonation face beauty-end*/

    private ImageView mCameraLayoutCover;
	private AnimationDrawable mSaveWaitingAnim;
    private int mScreenPixHeight;
	private int mScreenPixWidth;
	private FaceBeautyCoverView mFaceBeautyCoverView;
	/*prize-xuchunming-201804026-add spotlight-start*/ 
	private SpotLight mSpotLight;
	/*prize-xuchunming-201804026-add spotlight-end*/ 
    public FaceBeautyView(Activity mActivity) {
        super(mActivity);
        mContext = (CameraActivity)mActivity;
        /*prize-xuchunming-201804026-add spotlight-start*/
        if(FeatureSwitcher.isSuperSpotLight() == true) {
        	mSpotLight = new SpotLight(mActivity);
        }
        /*prize-xuchunming-201804026-add spotlight-end*/
        getScreenPix();
        Log.i(TAG, "[FaceBeautyView]constructor...");
    }

    public void setCurrentMode(String key) {
		// TODO Auto-generated method stub
    	Log.d(TAG, "setCurrentMode key:"+key+",cameraid:"+((CameraActivity)getContext()).getCameraId());
		if(key.equals(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL)){
			setCompositeMode();
		}else if(key.equals(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL)){
			setEyesEnlargementMode();
		}else if(key.equals(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL)){
			setSlimmingMode();
		}else if(key.equals(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL)){
			setSmoothingMode();
		}else if(key.equals(SettingConstants.KEY_FN_FB_TONING_LEVEL)){
			setTonigMode();
		}else{
			Log.d(TAG, "setFaceMode key is invaild");
		}
		setUiHightlight(focuseView);
	}

	@Override
    public void init(Activity activity, ICameraAppUi cameraAppUi, IModuleCtrl moduleCtrl) {
        Log.i(TAG, "[init]...");
        mICameraAppUi = cameraAppUi;
        mIModuleCtrl = moduleCtrl;
        setOrientation(mIModuleCtrl.getOrientationCompensation());
		mView = inflate(R.layout.facebeauty_indicator);
        initUi(mView);
        applyListeners();
        mFaceBeautyInfo = new FaceBeautyInfo(activity, mIModuleCtrl);
        /*prize-xuchunming-201804026-add spotlight-start*/
        if(mSpotLight != null) {
        	mSpotLight.initView((ViewGroup)mView);
        }
        /*prize-xuchunming-201804026-add spotlight-end*/
    }

    @Override
    public void hide() {
        Log.i(TAG, "[hide]...");
        closeFaceBeauty();
        hideCameraLayoutCover();
        super.hide();
    }

    @Override
    protected View getView() {
        Log.i(TAG, "[getView].view = " + mView);
        return mView;

    }

    @Override
    public void show() {
        Log.i(TAG, "[show]...,mIsShowSetting = " + mIsShowSetting + ",mIsInCameraPreview = "
                + mIsInCameraPreview+",isShutterState():");
        if (!mIsShowSetting && mIsInCameraPreview) {
        	Log.i(TAG, "super.show()");
        	if(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_MODE) != null){
        		/*prize-xuchunming-20180523-add shortcuts-start*/
        		mCameraLayoutCover = (ImageView)mICameraAppUi.getSurfaceCoverViewLayer().findViewById(R.id.camera_layout_cover);
        		//prize-add-for fb animation cover-20170318-pengcancan-start
        		mFaceBeautyCoverView = (FaceBeautyCoverView) mICameraAppUi.getSurfaceCoverViewLayer().findViewById(R.id.face_beauty_cover);
        		fbAdapter169display();
        		//prize-add-for fb animation cover-20170318-pengcancan-end
        		/*prize-xuchunming-20180523-add shortcuts-end*/
        		setCurrentMode(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_MODE).getValue());
        		onProgressChanged(mSeekBar,mSeekBar.getProgress(),false);
        		super.show();
        		/*prize-xuchunming-201804026-add spotlight-start*/
        		if(mSpotLight != null) {
        			mSpotLight.show();
        		}
        		/*prize-xuchunming-201804026-add spotlight-end*/
        		super.show();
                update(FaceBeautyMode.ORIENTATION_CHANGED, mIModuleCtrl.getOrientationCompensation());
                onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
        	}
        }
    }

    @Override
    public void setListener(Object obj) {
    }

    @Override
    public boolean update(int type, Object... args) {
        if (FaceBeautyMode.INFO_FACE_DETECTED != type && FaceBeautyMode.ORIENTATION_CHANGED != type) {
            Log.i(TAG, "[update] type = " + type);
        }
        boolean value = false;
        switch (type) {

        case FaceBeautyMode.ON_CAMERA_CLOSED:
            // when back to camera, the auto back to photoMode not need
        	/*prize-xuchunming-20180509-bugid:57453-start*/
        	//closeFaceBeauty();
        	hide();
        	/*prize-xuchunming-20180509-bugid:57453-end*/
            break;

        case FaceBeautyMode.ON_CAMERA_PARAMETERS_READY:
            //xuchunming
        	openFaceBeauty();
        	/*prize-xuchunming-201804026-add spotlight-start*/
        	if(mSpotLight != null) {
        		mSpotLight.show();
        	}
        	/*prize-xuchunming-201804026-add spotlight-end*/
            break;

        case FaceBeautyMode.INFO_FACE_DETECTED:
            updateUI((Integer) args[0]);
            break;

        case FaceBeautyMode.ORIENTATION_CHANGED:
            Util.setOrientation(mView, (Integer) args[0], true);
            if (mFaceBeautyInfo != null) {
                mFaceBeautyInfo.onOrientationChanged((Integer) args[0]);
            }
            break;

        case FaceBeautyMode.ON_FULL_SCREEN_CHANGED:
            mIsInCameraPreview = (Boolean) args[0];
            Log.i(TAG, "ON_FULL_SCREEN_CHANGED, mIsInCameraPreview = " + mIsInCameraPreview);
            if (mIsInCameraPreview) {
                show();
            } else {
               
                hide();
            }
            break;

        case FaceBeautyMode.ON_BACK_PRESSED:
            
            break;

        case FaceBeautyMode.HIDE_EFFECTS_ITEM:
           
            break;

        case FaceBeautyMode.ON_SETTING_BUTTON_CLICK:
            mIsShowSetting = (Boolean) args[0];

            Log.i(TAG, "ON_SETTING_BUTTON_CLICK,mIsShowSetting =  " + mIsShowSetting);

            if (mIsShowSetting) {
                hide();
            } else {
                show();
            }
            break;

        case FaceBeautyMode.ON_LEAVE_FACE_BEAUTY_MODE:
            hide();
           
            break;
            
        case FaceBeautyMode.REMVOE_BACK_TO_NORMAL:
            // this case also need reset the automatic back to VFB mode
            break;

        case FaceBeautyMode.ON_SELFTIMER_CAPTUEING:
            Log.i(TAG, "[ON_SELFTIMER_CAPTUEING] args[0] = "
                    + (Boolean) args[0] + ", mIsInPictureTakenProgress = "
                    + mIsInPictureTakenProgress);
            if ((Boolean) args[0]) {
                super.hide();
            } else {
                super.show();
            }
            break;

        case FaceBeautyMode.IN_PICTURE_TAKEN_PROGRESS:
            mIsInPictureTakenProgress = (Boolean) args[0];
            Log.i(TAG, "mIsInPictureTakenProgress = " + mIsInPictureTakenProgress);
            break;
        case FaceBeautyMode.ON_SPOTLIGHT_VISIBLE:
            boolean isSpotLightVisible = (Boolean) args[0];
            Log.i(TAG, "isSpotLightVisible =  " + isSpotLightVisible);
            /*prize-xuchunming-201804026-add spotlight-start*/
            if(mSpotLight != null) {
	            if (isSpotLightVisible) {
	                mSpotLight.setSpotLightVisible(View.VISIBLE);
	            } else {
	            	mSpotLight.setSpotLightVisible(View.GONE);
	            }
            }
            /*prize-xuchunming-201804026-add spotlight-end*/
            break;
        default:
            break;
        }

        return value;
    }

    @Override
    public int getViewHeight() {
        return 0;
    }

    @Override
    public int getViewWidth() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean isShowing() {
        return false;
    }

  

    private void applyListeners() {
    
    }


    private void hideToast() {
        Log.d(TAG, "[hideToast()]");
        if (mFaceBeautyInfo != null) {
            mFaceBeautyInfo.hideToast();
        }
    }
    
    private void updateUI(int length) {
     
    }
    
    /*prize-xuchunming-20160519-add fotonation face beauty-start*/
    public void initUi(View view) { 
		
		// TODO Auto-generated method stub
		beautiful_layout = (RelativeLayout)view.findViewById(R.id.beautiful_layout);
		beautiful_contain = (RelativeLayout)view.findViewById(R.id.beautiful_contain);
		compositeBeautiful = (LinearLayout)view.findViewById(R.id.composite_beautiful);
		smoothingBeautiful = (LinearLayout)view.findViewById(R.id.smoothing_beautiful);
		slimmingBeautiful = (LinearLayout)view.findViewById(R.id.slimming_beautiful);
		catchlightBeautiful = (LinearLayout)view.findViewById(R.id.catchlight_beautiful);
		eyesEnlargementBeautiful = (LinearLayout)view.findViewById(R.id.eyes_enlargement_beautiful);
		
		compositeBeautiful.setOnClickListener(this);
		smoothingBeautiful.setOnClickListener(this);
		slimmingBeautiful.setOnClickListener(this);
		catchlightBeautiful.setOnClickListener(this);
		eyesEnlargementBeautiful.setOnClickListener(this);
		
		mSeekBar = (SeekBar) view.findViewById(R.id.beautifu_seekbar);
		mSeekBar.setOnSeekBarChangeListener(this);
		/*prize-xuchunming-20180523-add shortcuts-start*/
		/*mCameraLayoutCover = (ImageView)mICameraAppUi.getSurfaceCoverViewLayer().findViewById(R.id.camera_layout_cover);
		 
		//prize-add-for fb animation cover-20170318-pengcancan-start
		mFaceBeautyCoverView = (FaceBeautyCoverView) mICameraAppUi.getSurfaceCoverViewLayer().findViewById(R.id.face_beauty_cover);
		fbAdapter169display();
		//prize-add-for fb animation cover-20170318-pengcancan-end*/
		/*prize-xuchunming-20180523-add shortcuts-end*/
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		Log.d(TAG,"onProgressChanged progress:"+progress);
		if(currentKey.equals(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL)){
			setComposite(progress);
		}else{
			mContext.getISettingCtrl().onSettingChanged(currentKey, String.valueOf((int)(progress*step)));
		}
		mContext.applyParametersToServer();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onStopTrackingTouch currentKey:"+currentKey+",currentvalue:"+seekBar.getProgress()*step);
		mContext.getISettingCtrl().getListPreference(currentKey).setValue(String.valueOf(seekBar.getProgress()));
		if(currentKey.equals(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL)){
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_TONING_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL).setValue(String.valueOf(seekBar.getProgress()));
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				switch(v.getId()){
				case R.id.composite_beautiful:
					 setCompositeMode();
					break;
				case R.id.smoothing_beautiful:
					 setSmoothingMode();
					 break;
				case R.id.slimming_beautiful:
					 setSlimmingMode();
					 break;
				case R.id.catchlight_beautiful:
					 setTonigMode();
					 break;
				case R.id.eyes_enlargement_beautiful:
					 setEyesEnlargementMode();
					 break;
				
				}
				setUiHightlight(v);
	}
	
	public void setUiHightlight(View v) {
		
		// TODO Auto-generated method stub
		if(v != null){
			clearSeletor();
			setSeletor(((ViewGroup)v));
			refleshSeakbar();
			mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_MODE).setValue(currentKey);
		}
	}

	private void setSeletor(ViewGroup v) {
		// TODO Auto-generated method stub
		v.getChildAt(0).setSelected(true);
		((TextView)v.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.main_color));
		
	}

	public void refleshSeakbar(){
		Log.d(TAG, "reflesh seakbar:"+currentValue);
		mSeekBar.setProgress(currentValue);
		
	}
	
	public void clearSeletor(){
		compositeBeautiful.getChildAt(0).setSelected(false);
		smoothingBeautiful.getChildAt(0).setSelected(false);
		slimmingBeautiful.getChildAt(0).setSelected(false);
		catchlightBeautiful.getChildAt(0).setSelected(false);
		eyesEnlargementBeautiful.getChildAt(0).setSelected(false);
		
		((TextView)compositeBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
		((TextView)smoothingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
		((TextView)slimmingBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
		((TextView)catchlightBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
		((TextView)eyesEnlargementBeautiful.getChildAt(1)).setTextColor(mActivity.getResources().getColor(R.color.beautiful_text_color_normal));
	}
	
	public void setEyesEnlargementMode() {
		
		// TODO Auto-generated method stub
		currentKey = SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL;
		currentValue  = Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL).getValue());
		focuseView = eyesEnlargementBeautiful;
		step = EYESENLARGVALUE/100;
	}

	public void setTonigMode() {
		
		// TODO Auto-generated method stub
		currentKey = SettingConstants.KEY_FN_FB_TONING_LEVEL;
		currentValue  = Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_TONING_LEVEL).getValue());
		focuseView = catchlightBeautiful;
		step = TONINRMAXVALUE/100;
	}

	public void setSlimmingMode() {
		
		// TODO Auto-generated method stub
		currentKey = SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL;
		currentValue  = Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL).getValue());
		focuseView = slimmingBeautiful;
		step = SLIMMINGMAXVALUE/100;
	}

	public void setSmoothingMode() {
		
		// TODO Auto-generated method stub
		currentKey = SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL;
		currentValue  = Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL).getValue());
		focuseView = smoothingBeautiful;
		step = SMOTTINGMAXVALUE/100;
	}

	public void setCompositeMode() {
		
		// TODO Auto-generated method stub
		currentKey = SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL;
		currentValue  = Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL).getValue());
		focuseView = compositeBeautiful;
		step = SMOTTINGMAXVALUE/100;
	}
	
	
	public String getModeValue(String key,int value){
		double step = 0;
		if(key.equals(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL)){
			step = SMOTTINGMAXVALUE/100;
		}else if(key.equals(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL)){
			step = SLIMMINGMAXVALUE/100;
		}else if(key.equals(SettingConstants.KEY_FN_FB_TONING_LEVEL)){
			step = TONINRMAXVALUE/100;
		}else if(key.equals(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL)){
			step = EYESENLARGVALUE/100;
		}else if(key.equals(SettingConstants.KEY_FN_FB_COMPOSITE_LEVEL)){
			step = SMOTTINGMAXVALUE/100;
		}
		value = (int) (value*step);
		return String.valueOf(value);
	}
	
	public void setComposite(int valus){
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_ENABLE,"1");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL,valus));
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL,valus));
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_TONING_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_TONING_LEVEL,valus));
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL,valus));
	}
	
	public void openFaceBeauty(){
		    /*prize-xuchunming-20180523-add shortcuts-start*/
			if(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL) == null) {
				Log.w(TAG, "openFaceBeauty fail , ListPreference KEY_FN_FB_SLIMMIMG_LEVEL is null");
				return;
			}
			/*prize-xuchunming-20180523-add shortcuts-end*/
			mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_ENABLE,"1");
			mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL,Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL).getValue())));
			mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL,Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL).getValue())));
			mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_TONING_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_TONING_LEVEL,Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_TONING_LEVEL).getValue())));
			mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL, getModeValue(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL,Integer.valueOf(mContext.getISettingCtrl().getListPreference(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL).getValue())));
			mContext.applyParametersToServer();
	}
	
	public void closeFaceBeauty(){
		Log.d(TAG, "+++++++++closeFaceBeauty++++++++++");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_ENABLE,"0");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SLIMMIMG_LEVEL, "0");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_SMOOTHING_LEVEL, "0");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_TONING_LEVEL, "0");
		mContext.getISettingCtrl().onSettingChanged(SettingConstants.KEY_FN_FB_EYEENLARGE_LEVEL, "0");
		/*prize-xuchunming-201804026-add spotlight-start*/
		if(mSpotLight != null) {
			mSpotLight.closeSpotLight();
		}
		/*prize-xuchunming-201804026-add spotlight-end*/
		mContext.applyParametersToServer();
	}
	
	/*prize-xuchunming-adjust layout at 18:9 project-start*/
	 public void onChangeNavigationBar(boolean isShow){/*
	    if(isShow == true){
	    	NavigationBarUtils.adpaterNavigationBar(getContext(), beautiful_contain,(int)getContext().getResources().getDimension(R.dimen.shutter_group_height)+(int)getContext().getResources().getDimension(R.dimen.center_item_height));
	    }else{
	    	NavigationBarUtils.adpaterNavigationBar(getContext(), beautiful_contain,(int)getContext().getResources().getDimension(R.dimen.shutter_group_height));
	    }
	*/}	
	 /*prize-xuchunming-adjust layout at 18:9 project-end*/
	
	public void showCameraLayoutCover(){
		 if (Math.abs(((double)((CameraActivity)getContext()).getPreviewFrameHeight()/((CameraActivity)getContext()).getPreviewFrameWidth() - SettingUtils.getFullScreenRatio())) <= Util.ASPECT_TOLERANCE) { //16:9
         	adapter169display();
         }else{
         	adapter43display();
		 }
		mCameraLayoutCover.setImageBitmap(((CameraActivity)getContext()).getModePicker().getCameraPreviewData());
		mCameraLayoutCover.setVisibility(View.VISIBLE);
		
	}

	//prize-add-for fb animation cover-20170318-pengcancan-start
	public void startFbCoverAnimation(){
		if (mFaceBeautyCoverView != null) {
			mFaceBeautyCoverView.setVisibility(View.VISIBLE);
			mFaceBeautyCoverView.startAnimation();
		}
	}
	//prize-add-for fb animation cover-20170318-pengcancan-end
	
	public void hideCameraLayoutCover(){
		Log.i("pengcancan","[hideCameraLayoutCover]",new Throwable("hideCameraLayoutCover"));
		if(mCameraLayoutCover != null){
			mCameraLayoutCover.setVisibility(View.GONE);
		}
		//prize-add-for fb animation cover-20170318-pengcancan-start
		if (mFaceBeautyCoverView != null && mFaceBeautyCoverView.getVisibility() == View.VISIBLE){
			mFaceBeautyCoverView.stopAnimation();
			mFaceBeautyCoverView.setVisibility(View.GONE);
		}
		//prize-add-for fb animation cover-20170318-pengcancan-end
	}
	
	public boolean isBeautyLayoutCoverShow(){
		return mCameraLayoutCover.getVisibility() == View.VISIBLE ||  mFaceBeautyCoverView .getVisibility() == View.VISIBLE ? true : false;
	}
	public void adapter43display(){
		LayoutParams lp = (LayoutParams) mCameraLayoutCover.getLayoutParams();
		lp.width = ((CameraActivity)getContext()).getPreviewFrameWidth();
		lp.height = ((CameraActivity)getContext()).getPreviewFrameHeight();
		lp.bottomMargin = ((LayoutParams)((CameraActivity)getContext()).getPreviewSurfaceView().getLayoutParams()).bottomMargin;
		mCameraLayoutCover.setLayoutParams(lp);
		Log.d(TAG, "adapter43display，lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",topMargin: "+lp.topMargin);
	}
	
	public void adapter169display(){
		LayoutParams lp = (LayoutParams) mCameraLayoutCover.getLayoutParams();
		/*prize-modify-the screen is abnormal on takepicture at facebeauty mode-xiaoping-20171018-start*/
		/*lp.width = mScreenPixWidth;
		lp.height = mScreenPixHeight;
		lp.bottomMargin = 0;*/
		lp.width = ((CameraActivity)getContext()).getPreviewFrameWidth();
		lp.height = ((CameraActivity)getContext()).getPreviewFrameHeight();
		lp.bottomMargin = ((LayoutParams)((CameraActivity)getContext()).getPreviewSurfaceView().getLayoutParams()).bottomMargin;
		/*prize-modify-the screen is abnormal on takepicture at facebeauty mode-xiaoping-20171018-end*/
		mCameraLayoutCover.setLayoutParams(lp);
		Log.d(TAG, "adapter169display，lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",topMargin: "+lp.topMargin);
	}

	//prize-add-for fb animation cover-20170318-pengcancan-start
	public void fbAdapter169display(){
		LayoutParams lp = (LayoutParams) mFaceBeautyCoverView.getLayoutParams();
		lp.width = mScreenPixWidth;
		lp.height = mScreenPixHeight;
		lp.bottomMargin = 0;
		Log.d(TAG, "fbAdapter169display，lp.width:"+lp.width+",lp.height:"+lp.height+",lp.bottomMargin:"+lp.bottomMargin+",topMargin: "+lp.topMargin);
		mFaceBeautyCoverView.setLayoutParams(lp);
    }
	//prize-add-for fb animation cover-20170318-pengcancan-end
	
	
	public void getScreenPix() {
		DisplayMetrics metric = new DisplayMetrics();
		WindowManager wm = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getRealMetrics(metric);

		mScreenPixWidth = metric.widthPixels;
		mScreenPixHeight = metric.heightPixels;
	}
    
}
