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

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.ModeChecker;
import com.android.camera.R;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ShutterButton.OnShutterButtonListener;
import com.android.prize.FaceBeautyCoverView;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.prize.setting.NavigationBarUtils;
import com.prize.ui.CaptureAnimation;
import com.prize.ui.ShortVideoAnimation;

public class ShutterManager extends ViewManager {
    private static final String TAG = "ShutterManager";

    public static final int SHUTTER_TYPE_PHOTO_VIDEO = 0;
    public static final int SHUTTER_TYPE_PHOTO = 1;
    public static final int SHUTTER_TYPE_VIDEO = 2;
    public static final int SHUTTER_TYPE_OK_CANCEL = 3;
    public static final int SHUTTER_TYPE_CANCEL = 4;
    public static final int SHUTTER_TYPE_CANCEL_VIDEO = 5;
    public static final int SHUTTER_TYPE_SLOW_VIDEO = 6;

    private int mShutterType = SHUTTER_TYPE_PHOTO_VIDEO;
    private ShutterButton mPhotoShutter;
    private ShutterButton mVideoShutter;
    private View mOkButton;
    private View mCancelButton;
    private View mShutterContain;
    private OnShutterButtonListener mPhotoListener;
    private OnShutterButtonListener mVideoListener;
    private OnClickListener mOklistener;
    private OnClickListener mCancelListener;
    private boolean mPhotoShutterEnabled = true;
    private boolean mVideoShutterEnabled = true;
    private boolean mCancelButtonEnabled = true;
    private boolean mOkButtonEnabled = true;
    private boolean mVideoShutterMasked;
    private boolean mFullScreen = true;
    private ISettingCtrl mISettingController;
	private CameraActivity mcontext;//gangyun tech add 
    private AnimationDrawable mAimationDrawable;
    private  boolean isCaptureAnimationStop = false;
    private CaptureAnimation mCaptureAnimation;
    /*prize-add-10s short video -xiaoping-20180505-start*/
    private ShortVideoAnimation mShortVideoAnimation;
    /*prize-add-10s short video -xiaoping-20180505-end*/
    public ShutterManager(CameraActivity context) {
        super(context, VIEW_LAYER_SHUTTER);
        setFileter(false);
		mcontext = context;//gangyun tech add
    }

    @Override
    protected View getView() {
        View view = null;
        int layoutId = R.layout.camera_shutter_photo_video;
        Log.d(TAG,"mShutterType: "+mShutterType);
        switch (mShutterType) {
        case SHUTTER_TYPE_PHOTO_VIDEO:
            layoutId = R.layout.camera_shutter_photo_video;
            break;
        case SHUTTER_TYPE_PHOTO:
            layoutId = R.layout.camera_shutter_photo;
            break;
        case SHUTTER_TYPE_VIDEO:
            layoutId = R.layout.camera_shutter_video;
            break;
        case SHUTTER_TYPE_OK_CANCEL:
            layoutId = R.layout.camera_shutter_ok_cancel;
            break;
        case SHUTTER_TYPE_CANCEL:
            layoutId = R.layout.camera_shutter_cancel;
            break;
        case SHUTTER_TYPE_CANCEL_VIDEO:
            layoutId = R.layout.camera_shutter_cancel_video;
            break;
        case SHUTTER_TYPE_SLOW_VIDEO:
            layoutId = R.layout.camera_shutter_slow_video;
        default:
            break;
        }
        view = inflate(layoutId);
        mPhotoShutter = (ShutterButton) view.findViewById(R.id.shutter_button_photo);
        if (mShutterType == SHUTTER_TYPE_SLOW_VIDEO) {
            mVideoShutter = (ShutterButton) view.findViewById(R.id.shutter_button_slow_video);
        } else {
            mVideoShutter = (ShutterButton) view.findViewById(R.id.shutter_button_video);
        }
        mOkButton = view.findViewById(R.id.btn_done);
        mCancelButton = view.findViewById(R.id.btn_cancel);
        mShutterContain = view.findViewById(R.id.shutterContain);
        mCaptureAnimation = (CaptureAnimation) view.findViewById(R.id.capture_anima);
        /*prize-add-10s short video -xiaoping-20180505-start*/
        mShortVideoAnimation = (ShortVideoAnimation) view.findViewById(R.id.shortvideo_anima);
        /*prize-add-10s short video -xiaoping-20180505-end*/
        Log.d(TAG,"mCaptureAnimation: "+mCaptureAnimation);
        //NavigationBarUtils.adpaterNavigationBar(getContext(), mShutterContain);
        applyListener();
        return view;
    }

    public void onChangeNavigationBar(boolean isShow){
    	if(isShow == true){
    		NavigationBarUtils.adpaterNavigationBar(getContext(), mShutterContain);
    	}else{
    		NavigationBarUtils.adpaterNavigationBar(getContext(), mShutterContain,0);
    	}
    	
    }
    @Override
    protected void onRelease() {
        if (mPhotoShutter != null) {
            mPhotoShutter.setOnShutterButtonListener(null);
        }
        if (mVideoShutter != null) {
            mVideoShutter.setOnShutterButtonListener(null);
        }
        if (mOkButton != null) {
            mOkButton.setOnClickListener(null);
        }
        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(null);
        }
        mPhotoShutter = null;
        mVideoShutter = null;
        mOkButton = null;
        mCancelButton = null;
    }

    public void setSettingController(ISettingCtrl settingController) {
        mISettingController = settingController;
    }

    private void applyListener() {
        if (mPhotoShutter != null) {
            mPhotoShutter.setOnShutterButtonListener(mPhotoListener);
        }
        if (mVideoShutter != null) {
            mVideoShutter.setOnShutterButtonListener(mVideoListener);
        }
        if (mOkButton != null) {
            mOkButton.setOnClickListener(mOklistener);
        }
        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(mCancelListener);
        }
        Log.d(TAG, "applyListener() mPhotoShutter=(" + mPhotoShutter + ", " + mPhotoListener
                + "), mVideoShutter=(" + mVideoShutter + ", " + mVideoListener + "), mOkButton=("
                + mOkButton + ", " + mOklistener + "), mCancelButton=(" + mCancelButton + ", "
                + mCancelListener + ")");
    }

    public void setShutterListener(OnShutterButtonListener photoListener,
            OnShutterButtonListener videoListener, OnClickListener okListener,
            OnClickListener cancelListener) {
        mPhotoListener = photoListener;
        mVideoListener = videoListener;
        mOklistener = okListener;
        mCancelListener = cancelListener;
        applyListener();
    }

    public void switchShutter(int type) {
        Log.i(TAG, "switchShutterType(" + type + ") mShutterType=" + mShutterType);
        if (mShutterType != type) {
            mShutterType = type;
            reInflate();
        }
    }

    public int getShutterType() {
        return mShutterType;
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh() mPhotoShutterEnabled=" + mPhotoShutterEnabled + ", mFullScreen="
                + mFullScreen + ", isEnabled()=" + isEnabled());
        if (mVideoShutter != null) {
            boolean visible = ModeChecker.getModePickerVisible(getContext(), getContext()
                    .getCameraId(), ModePicker.MODE_VIDEO);
			
			//gangyun tech add begin
			if(mcontext.getCurrentMode() == ModePicker.MODE_BOKEH ){
				visible = false;
			}
			//gangyun tech add end
			
            boolean enabled = mVideoShutterEnabled && isEnabled() && mFullScreen && visible;
                    //&& !getContext().getWfdManagerLocal().isWfdEnabled();
            mVideoShutter.setEnabled(enabled);
            mVideoShutter.setClickable(enabled);
            boolean isSlowMotionOn = false;
            if (mISettingController != null) {
                isSlowMotionOn = "on".equals(mISettingController
                        .getSettingValue(SettingConstants.KEY_SLOW_MOTION));
            }

            if (mVideoShutterMasked) {
                if (isSlowMotionOn) {
                    mVideoShutter.setImageResource(R.drawable.btn_slow_video_mask);
                } else {
                    mVideoShutter.setImageResource(R.drawable.btn_video_mask);
                    if(mPhotoShutter != null){
                    	mPhotoShutter.setImageResource(R.drawable.btn_photo_mask);
                        LayoutParams lp = (LayoutParams)mPhotoShutter.getLayoutParams();
                 		lp.addRule(RelativeLayout.CENTER_IN_PARENT, 0);
                 		mPhotoShutter.setLayoutParams(lp);
                 		mPhotoShutter.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (isSlowMotionOn) {
                    mVideoShutter.setImageResource(R.drawable.btn_slow_video);
                } else {
                    mVideoShutter.setImageResource(R.drawable.btn_video);
                }
            }
        }
        if (mPhotoShutter != null) {
            boolean enabled = mPhotoShutterEnabled && isEnabled() && mFullScreen;
            mPhotoShutter.setEnabled(enabled);
            mPhotoShutter.setClickable(enabled);
        }
        if (mOkButton != null) {
            boolean enabled = mOkButtonEnabled && isEnabled() && mFullScreen;
            mOkButton.setEnabled(enabled);
            mOkButton.setClickable(enabled);
        }
        if (mCancelButton != null) {
            boolean enabled = mCancelButtonEnabled && isEnabled() && mFullScreen;
            mCancelButton.setEnabled(enabled);
            mCancelButton.setClickable(enabled);
        }
        onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
    }

    public ShutterButton getPhotoShutter() {
        return mPhotoShutter;
    }

	public ShutterButton getVideoShutter() {
        return mVideoShutter;
    }

    public View getOkShutter() {
        return mOkButton;
    }

    public boolean performPhotoShutter() {
        boolean performed = false;
        if (mPhotoShutter != null && mPhotoShutter.isEnabled()) {
            mPhotoShutter.performClick();
            performed = true;
        }
        Log.d(TAG, "performPhotoShutter() mPhotoShutter=" + mPhotoShutter + ", return "
                + mPhotoShutter);
        return performed;
    }

    public void setPhotoShutterEnabled(boolean enabled) {
        Log.d(TAG, "setPhotoShutterEnabled(" + enabled + ")");
        mPhotoShutterEnabled = enabled;
        refresh();
    }

    public boolean isPhotoShutterEnabled() {
        Log.v(TAG, "isPhotoShutterEnabled() return " + mPhotoShutterEnabled);
        return mPhotoShutterEnabled;
    }

    public void setVideoShutterEnabled(boolean enabled) {
        Log.d(TAG, "setVideoShutterEnabled(" + enabled + ")");
        mVideoShutterEnabled = enabled;
        refresh();
    }

    public boolean isVideoShutterEnabled() {
        Log.d(TAG, "isVideoShutterEnabled() return " + mVideoShutterEnabled);
        return mVideoShutterEnabled;
    }

    public void setCancelButtonEnabled(boolean enabled) {
        Log.d(TAG, "setCancelButtonEnabled(" + enabled + ")");
        mCancelButtonEnabled = enabled;
        refresh();
    }

    public void setOkButtonEnabled(boolean enabled) {
        Log.d(TAG, "setOkButtonEnabled(" + enabled + ")");
        mOkButtonEnabled = enabled;
        refresh();
    }

    public boolean isCancelButtonEnabled() {
        Log.d(TAG, "isCancelButtonEnabled() return " + mCancelButtonEnabled);
        return mCancelButtonEnabled;
    }

    public void setVideoShutterMask(boolean mask) {
        Log.d(TAG, "setVideoShutterMask(" + mask + ")");
        mVideoShutterMasked = mask;
        refresh();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refresh();
    }

    /*prize-add-take pictures of animation-xiaoping-20170929-start*/
    public void loadAnimation() {
        Log.d(TAG,"loadAnimation,mCaptureAnimation: "+mCaptureAnimation);
        if (mCaptureAnimation != null) {
            mPhotoShutter.setVisibility(View.GONE);
            mCaptureAnimation.setVisibility(View.VISIBLE);
            mCaptureAnimation.start();
            isCaptureAnimationStop = false;
        }
    }

    public void stopAnimation() {
        Log.d(TAG,"stopAnimation,mCaptureAnimation: "+mCaptureAnimation);
        if (mCaptureAnimation != null) {
            mCaptureAnimation.stop();
            mCaptureAnimation.setVisibility(View.GONE);
            mPhotoShutter.setVisibility(View.VISIBLE);
            isCaptureAnimationStop = true;

        }
    }
    public CaptureAnimation getCaptureAnimation () {
        return mCaptureAnimation;
    }

    public void hideCaptureAnimation() {
        if (mCaptureAnimation != null && mCaptureAnimation.getVisibility() == View.VISIBLE) {
            mCaptureAnimation.stop();
        }
    }

    public void showPhotoShutter() {
        if (mCaptureAnimation != null && mPhotoShutter != null) {
            mCaptureAnimation.setVisibility(View.GONE);
            mPhotoShutter.setVisibility(View.VISIBLE);
        }
    }
    public boolean isCaptureAnimationStop() {
        return isCaptureAnimationStop;
    }
    /*prize-add-take pictures of animation-xiaoping-20170929-end*/

    /*prize-add-10s short video -xiaoping-20180505-start*/
    public void loadShortVideoAnimation() {
        if (mShortVideoAnimation != null) {
            mShortVideoAnimation.setVisibility(View.VISIBLE);
            mShortVideoAnimation.start();
        }
    }

    public void stopShortVideoAnimation() {
        if (mShortVideoAnimation != null) {
            mShortVideoAnimation.stop();
            mShortVideoAnimation.setVisibility(View.GONE);
            mVideoShutter.setVisibility(View.VISIBLE);

        }
    }

    public void pauseShortVideoAnimation() {
        if (mShortVideoAnimation != null) {
            mShortVideoAnimation.pause();

        }
    }

    public boolean isShortVideoAnimationVisible() {
        boolean isShortVideoAnimationShow = false;
        if (mShortVideoAnimation != null) {
            isShortVideoAnimationShow =  mShortVideoAnimation.getVisibility() == View.VISIBLE;
        }
        Log.i(TAG,"isShortVideoAnimationShow: "+isShortVideoAnimationShow);
        return isShortVideoAnimationShow;
    }
    /*prize-add-10s short video -xiaoping-20180505-end*/
}
