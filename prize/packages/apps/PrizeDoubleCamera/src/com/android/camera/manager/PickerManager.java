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

import android.content.res.TypedArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.camera.CameraActivity;
import com.android.camera.Log;
import com.android.camera.ModeChecker;
import com.android.camera.R;
import com.android.camera.Util;
import com.android.camera.ui.PickerButton;
import com.android.camera.ui.PickerButton.Listener;
import com.android.camera.ui.RotateImageView;
import com.mediatek.camera.setting.preference.IconListPreference;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.setting.SettingConstants;
import com.prize.setting.NavigationBarUtils;
import com.android.prize.FlashMenu;
import com.android.prize.FlashMenu.FlashMenuOnListerern;
import com.android.prize.HdrMenu;


public class PickerManager extends ViewManager implements Listener,
        CameraActivity.OnPreferenceReadyListener, CameraActivity.OnParametersReadyListener,
        CameraActivity.OnLowPowerListener{
    private static final String TAG = "PickerManager";
    
    public interface PickerListener {
        boolean onSlowMotionPicked(String turnon);

        boolean onHdrPicked(String value);

        boolean onGesturePicked(String value);

        boolean onSmilePicked(String value);

        boolean onCameraPicked(int camerId);

        boolean onFlashPicked(String flashMode);

        boolean onStereoPicked(boolean stereoType);

        boolean onModePicked(int mode, String value, ListPreference preference);
        /*arc add start*/
        boolean onPicSelfiePicked(String value);
        /*arc add end*/
    }

    public interface OnFlashListerern{
		public void onShowFlashMenu();
		public void onHideFlashMenu();
		public void onCompleteAnimation(boolean isShow);
	}
 
    private OnFlashListerern mOnFlashListerern;
    
    private PickerButton mSlowMotion;
    private PickerButton mGestureShot;
    private PickerButton mHdr;
    private PickerButton mSmileShot;
    private PickerButton mFlashPicker;
    private PickerButton mCameraPicker;
    private PickerButton mStereoPicker;
    /*arc add start*/
    private PickerButton mPicSelfie;
    /*arc add end*/
    private FlashMenu mFlashMenu;
    private PickerListener mListener;
    private boolean mPreferenceReady;
    private CameraActivity mContext;

    private static final int PICKER_BUTTON_NUM = 8;
    public static final int BUTTON_SMILE_SHOT = 0;
    public static final int BUTTON_HDR = 1;
    public static final int BUTTON_FLASH = 2;
    public static final int BUTTON_CAMERA = 3;
    public static final int BUTTON_STEREO = 4;
    public static final int BUTTON_SLOW_MOTION = 5;
    public static final int BUTTON_GESTURE_SHOT = 6;
    /*arc add start*/
    public static final int BUTTON_PICSELFIE = 7;
    /*arc add end*/
    private PickerButton[] mPickerButtons = new PickerButton[PICKER_BUTTON_NUM];

    private static final int MAX_NUM_OF_SHOWEN = 4;
    private int[] mButtonPriority = { BUTTON_SLOW_MOTION, BUTTON_HDR, BUTTON_FLASH, BUTTON_CAMERA,
            BUTTON_STEREO, BUTTON_GESTURE_SHOT, BUTTON_SMILE_SHOT, BUTTON_PICSELFIE };
    private boolean mDefineOrder = false;
    private static boolean[] sShownStatusRecorder = new boolean[PICKER_BUTTON_NUM];
    static {
        sShownStatusRecorder[BUTTON_SLOW_MOTION] = false;
        sShownStatusRecorder[BUTTON_HDR] = false;
        sShownStatusRecorder[BUTTON_FLASH] = false;
        sShownStatusRecorder[BUTTON_CAMERA] = false;
        sShownStatusRecorder[BUTTON_STEREO] = false;
        sShownStatusRecorder[BUTTON_GESTURE_SHOT] = true;
        sShownStatusRecorder[BUTTON_SMILE_SHOT] = true;
        sShownStatusRecorder[BUTTON_PICSELFIE] = false;
    }

    public PickerManager(CameraActivity context) {
        super(context,VIEW_LAYER_TOP);
        mContext = context;
        setFileter(false);
        context.addOnPreferenceReadyListener(this);
        context.addOnParametersReadyListener(this);
        /*prize-xuchunming-20180503-bugid:56870-start*/
        context.addOnLowPowerListeners(this);
        /*prize-xuchunming-20180503-bugid:56870-end*/
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.onscreen_pickers);

        mSlowMotion = (PickerButton) view.findViewById(R.id.onscreen_slow_motion_picker);
        mGestureShot = (PickerButton) view.findViewById(R.id.onscreen_gesture_shot_picker);
        mSmileShot = (PickerButton) view.findViewById(R.id.onscreen_smile_shot_picker);
        mHdr = (PickerButton) view.findViewById(R.id.onscreen_hdr_picker);
        mFlashPicker = (PickerButton) view.findViewById(R.id.onscreen_flash_picker);
        mCameraPicker = (PickerButton) view.findViewById(R.id.onscreen_camera_picker);
        mStereoPicker = (PickerButton) view.findViewById(R.id.onscreen_stereo3d_picker);
        /*arc add start*/
        mPicSelfie = (PickerButton) view.findViewById(R.id.onscreen_picselfie_picker);
        /*arc add end*/
        mCameraPicker.setAnimationEnabled(false, false);
        mPickerButtons[BUTTON_SLOW_MOTION] = mSlowMotion;
        mPickerButtons[BUTTON_GESTURE_SHOT] = mGestureShot;
        mPickerButtons[BUTTON_SMILE_SHOT] = mSmileShot;
        mPickerButtons[BUTTON_HDR] = mHdr;
        mPickerButtons[BUTTON_FLASH] = mFlashPicker;
        mPickerButtons[BUTTON_CAMERA] = mCameraPicker;
        mPickerButtons[BUTTON_STEREO] = mStereoPicker;
        /*arc add start*/
        mPickerButtons[BUTTON_PICSELFIE] = mPicSelfie;
        /*arc add end*/
        
        applyListeners();
        return view;
    }

    private void applyListeners() {
        if (mSlowMotion != null) {
            mSlowMotion.setListener(this);
        }
        if (mGestureShot != null) {
            mGestureShot.setListener(this);
        }
        if (mSmileShot != null) {
            mSmileShot.setListener(this);
        }
        if (mHdr != null) {
            mHdr.setListener(this);
        }
        /*arc add start*/
        if (mPicSelfie != null) {
        	mPicSelfie.setListener(this);
        }
        /*arc add end*/
        if (mFlashMenu != null) {
            mFlashMenu.setListener(this);
        }else  if (mFlashPicker != null) {
        	mFlashPicker.setListener(this);
        }
        if (mCameraPicker != null) {
            mCameraPicker.setListener(this);
        }
        if (mStereoPicker != null) {
            mStereoPicker.setListener(this);
        }
    }

    private void clearListeners() {
        if (mSlowMotion != null) {
            mSlowMotion.setListener(null);
        }
        if (mGestureShot != null) {
            mGestureShot.setListener(null);
        }
        if (mSmileShot != null) {
            mSmileShot.setListener(null);
        }
        if (mHdr != null) {
            mHdr.setListener(null);
        }
        /*arc add start*/
        if (mPicSelfie != null) {
        	mPicSelfie.setListener(null);
        }
        /*arc add end*/
        if (mFlashMenu != null) {
            mFlashMenu.setListener(null);
        }else  if (mFlashPicker != null) {
        	mFlashPicker.setListener(null);
        }
        if (mCameraPicker != null) {
            mCameraPicker.setListener(null);
        }
        if (mStereoPicker != null) {
            mStereoPicker.setListener(null);
        }
    }

    public void setListener(PickerListener listener) {
        mListener = listener;
    }

    @Override
    public void onPreferenceReady() {
        Log.i(TAG, "onPreferenceReady()");
        mPreferenceReady = true;
    }

    @Override
    public void onCameraParameterReady() {
        Log.i(TAG, "onCameraParameterReady(), mDefineOrder:" + mDefineOrder + "" +
                ", mPreferenceReady:" + mPreferenceReady);
        if (!mPreferenceReady) {
            return;
        }

        // the max number of button shown on PickerManager UI is 4, Slow motion,
        // hdr, flash, dual camera,
        // stereo camera have high priority, gesture, smile have low priority,
        // but gesture's priority is
        // higher than smile, if the order of button is definite, do not
        // redefine again.
        if (!mDefineOrder) {
            int count = 0;
            for (int i = 0; i < mButtonPriority.length; i++) {
                ListPreference pref = null;
                boolean visible = false;
                int buttonIndex = mButtonPriority[i];
                switch (buttonIndex) {
                case BUTTON_SLOW_MOTION:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_SLOW_MOTION);
                    break;
                case BUTTON_HDR:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_HDR);
                    break;
                case BUTTON_FLASH:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_FLASH);
                    break;
                case BUTTON_CAMERA:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_DUAL_CAMERA);
                    visible = ModeChecker.getCameraPickerVisible(getContext(),getContext().getCameraId());
                    if (visible) {
                        count++;
                        if (pref != null) {
                            pref.showInSetting(false);
                        }
                    }
                    pref = null;
                    break;
                case BUTTON_STEREO:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_STEREO_MODE);
                    visible = ModeChecker.getStereoPickerVisibile(getContext());
                    if (visible) {
                        count++;
                        if (pref != null) {
                            pref.showInSetting(false);
                        }

                    }
                    pref = null;
                    break;
                case BUTTON_GESTURE_SHOT:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_GESTURE_SHOT);
                    break;
                case BUTTON_SMILE_SHOT:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_SMILE_SHOT);
                    break;
                case BUTTON_PICSELFIE:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE);
                    if (pref != null) {
                        pref.showInSetting(false);
                    }
                    break;
                default:
                    break;
                }

                if (pref != null && pref.getEntries() != null
                        && pref.getEntries().length > 1) {
                    pref.showInSetting(false);
                    count++;
                    if (BUTTON_GESTURE_SHOT == buttonIndex) {
                        sShownStatusRecorder[BUTTON_GESTURE_SHOT] = false;
                    } else if (BUTTON_SMILE_SHOT == buttonIndex) {
                        sShownStatusRecorder[BUTTON_SMILE_SHOT] = false;
                    }
                }

                Log.i(TAG, "count:" + count + ", buttonIndex:" + buttonIndex);
                if (count >= MAX_NUM_OF_SHOWEN) {
                    break;
                }
            }
            mDefineOrder = true;
        } else {
            for (int i = 0; i < mButtonPriority.length; i++) {
                ListPreference pref = null;
                int buttonIndex = mButtonPriority[i];
                switch (buttonIndex) {
                case BUTTON_SLOW_MOTION:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_SLOW_MOTION);
                    break;
                case BUTTON_HDR:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_HDR);
                    break;
                case BUTTON_FLASH:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_FLASH);
                    break;
                case BUTTON_CAMERA:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_DUAL_CAMERA);
                    break;
                case BUTTON_STEREO:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_STEREO_MODE);
                    break;
                case BUTTON_GESTURE_SHOT:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_GESTURE_SHOT);
                    break;
                case BUTTON_SMILE_SHOT:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_SMILE_SHOT);
                    break;
                case BUTTON_PICSELFIE:
                    pref = (IconListPreference) getContext().getListPreference(
                            SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE);
                    break;
                default:
                    break;
                }
                if (pref != null) {
                    pref.showInSetting(sShownStatusRecorder[buttonIndex]);
                }
            }
        }

        refresh();
    }

    @Override
    public void hide() {
        if (mContext.getCurrentMode() == ModePicker.MODE_VIDEO
                && "on".equals(mContext.getISettingCtrl().getSettingValue(
                        SettingConstants.KEY_HDR))) {
            for (int i = PICKER_BUTTON_NUM - 1; i >= 0; i--) {
                if (mPickerButtons[i] == mHdr) {
                    mPickerButtons[i].setEnabled(true);
                    mPickerButtons[i].setClickable(false);
                    mPickerButtons[i].setVisibility(View.VISIBLE);
                    super.fadeIn();
                } else {
                	if (mPickerButtons[i] == mFlashPicker) {
                		if(mFlashMenu != null){
                			mFlashMenu.hideMenu();
                    		Util.fadeOut(mFlashMenu.getContian());
                		}
                	}
                    Util.fadeOut(mPickerButtons[i]);
                }
            }
        } else {
        	if(mFlashMenu != null){
        		mFlashMenu.hideMenu();
        	}
            super.hide();
        }
    }

    @Override
    public boolean onPicked(PickerButton button, ListPreference pref, String newValue) {
        boolean picked = false;
        String key = pref.getKey();
        if (mListener != null) {
            int index = -1;
            for (int i = 0; i < PICKER_BUTTON_NUM; i++) {
                if (button.equals(mPickerButtons[i])) {
                    index = i;
                    break;
                }
            }

            switch (index) {
            case BUTTON_SLOW_MOTION:
                picked = mListener.onSlowMotionPicked(newValue);
                break;
            case BUTTON_GESTURE_SHOT:
                button.setValue(newValue);
                picked = mListener.onGesturePicked(newValue);
                break;
            case BUTTON_SMILE_SHOT:
                button.setValue(newValue);
                picked = mListener.onSmilePicked(newValue);
                break;
            case BUTTON_HDR:
                button.setValue(newValue);
                /*prize-xuchunming-20160907-bugid:21212-start*/
                button.setEnabled(false);
                /*prize-xuchunming-20160907-bugid:21212-end*/
                picked = mListener.onHdrPicked(newValue);
                /*prize-xuchunming-20160907-bugid:21212-start*/
                button.setEnabled(true);
                /*prize-xuchunming-20160907-bugid:21212-end*/
                break;
            case BUTTON_FLASH:
                picked = mListener.onFlashPicked(newValue);
                break;
            case BUTTON_CAMERA:
                picked = mListener.onCameraPicked(Integer.parseInt(newValue));
                if(mFlashMenu != null){
                	mFlashMenu.hideMenu();
                }
                break;
            case BUTTON_STEREO:
                picked = mListener.onStereoPicked("1".endsWith(newValue) ? true : false);
                break;
            /*arc add start*/
            case BUTTON_PICSELFIE:
                picked = mListener.onPicSelfiePicked(newValue);
                break;
            /*arc add end*/
            default:
                break;
            }

        }
        Log.i(TAG, "onPicked(" + key + ", " + newValue + ") mListener=" + mListener + " return "
                + picked);
        return picked;
    }

    public void setCameraId(int cameraId) {
        if (mCameraPicker != null) {
            mCameraPicker.setValue("" + cameraId);
        }
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh(), mPreferenceReady:" + mPreferenceReady);
        if (!mPreferenceReady) {
            return;
        }

        mSlowMotion.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_SLOW_MOTION));
        mGestureShot.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_GESTURE_SHOT));
        mSmileShot.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_SMILE_SHOT));
        mHdr.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_HDR));
        /*arc add start*/
        mPicSelfie.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_ARC_PICSELFIE_ENABLE));
        /*arc add end*/
        if(isEnableFlashMenu() == true){
        	if(mFlashMenu == null && getContainView() != null){
        		initFlashMenu(getContainView());
        	}
        }else{
        	mFlashMenu = null;
        }
        clearListeners();
        applyListeners();
        
        if(mFlashMenu != null){
        	mFlashMenu.initialize((IconListPreference) getContext().getListPreference(
                    SettingConstants.ROW_SETTING_FLASH));
        }else if(mFlashPicker != null) {
        	mFlashPicker.initialize((IconListPreference) getContext().getListPreference(
                    SettingConstants.ROW_SETTING_FLASH));
        }
        mCameraPicker.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_DUAL_CAMERA));
        mStereoPicker.initialize((IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_STEREO_MODE));

        if (mSlowMotion != null) {
            mSlowMotion.refresh();
        }
        if (mGestureShot != null) {
            mGestureShot.refresh();
        }
        if (mSmileShot != null) {
            mSmileShot.refresh();
        }
        if (mFlashMenu != null) {
        	mFlashMenu.updateView();
        }else if(mFlashPicker != null) {
        	mFlashPicker.refresh();
        }
        if (mCameraPicker != null) {
           /* boolean visible = ModeChecker.getCameraPickerVisible(getContext(),getContext().getCameraId());
            if (visible) {*/
                mCameraPicker.refresh();
            /*} else {
                mCameraPicker.setVisibility(View.GONE);
            }*/
        }
        if (mStereoPicker != null) {
            boolean visible = ModeChecker.getStereoPickerVisibile(getContext());
            if (visible) {
                mStereoPicker.refresh();
            } else {
                mStereoPicker.setVisibility(View.GONE);
            }
        }
        if (mHdr != null) {
        	/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
        	if(isFlashMenuShow() == false ){
        		boolean visible = ModeChecker.getModePickerVisible(getContext(),getContext().getCameraId(),ModePicker.MODE_HDR);
        		if(visible){
        			mHdr.refresh();
        		}else{
        			mHdr.setVisibility(View.GONE);
        		}
        		
        	}
        	/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
        }
        /*arc add start*/
        if (mPicSelfie != null) {
        	if(isFlashMenuShow() == false ){
        		mPicSelfie.refresh();
        	}
        }
        /*arc add end*/

        /*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171003-start*/
        onChangeNavigationBar(NavigationBarUtils.isShowNavigationBar(getContext()));
        /*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171003-end*/
    }

    @Override
    protected void onRelease() {
        super.onRelease();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mSlowMotion != null) {
            mSlowMotion.setEnabled(enabled);
            mSlowMotion.setClickable(enabled);
        }
        if (mGestureShot != null) {
            mGestureShot.setEnabled(enabled);
            mGestureShot.setClickable(enabled);
        }
        if (mSmileShot != null) {
            mSmileShot.setEnabled(enabled);
            mSmileShot.setClickable(enabled);
        }
        if (mFlashMenu != null) {
            mFlashMenu.setEnabled(enabled);
            mFlashMenu.setClickable(enabled);
        }else if(mFlashPicker != null) {
        	mFlashPicker.setEnabled(enabled);
        	mFlashPicker.setClickable(enabled);
        }
        if (mCameraPicker != null) {
            mCameraPicker.setEnabled(enabled);
            mCameraPicker.setClickable(enabled);
        }
        if (mStereoPicker != null) {
            mStereoPicker.setEnabled(enabled);
            mStereoPicker.setClickable(enabled);
        }
        if (mHdr != null) {
            mHdr.setEnabled(enabled);
            mHdr.setClickable(enabled);
        }
        /*arc add start*/
        if (mPicSelfie != null) {
        	mPicSelfie.setEnabled(enabled);
        	mPicSelfie.setClickable(enabled);
        }
        /*arc add end*/
    }

    /**
     * Force to enable the picker button indicated by the input key.
     * @param key The key used to indicate the picker button.
     */
    public void forceEnable(String key) {
        if (SettingConstants.KEY_HDR.equals(key)) {
            mHdr.forceEnable();
        }
    }

    /**
     * Do not to force enable the picker button indicated by the input key.
     * @param key The key used to indicate the picker button.
     */
    public void cancelForcedEnable(String key) {
        if (SettingConstants.KEY_HDR.equals(key)) {
            mHdr.cancelForcedEnable();
        }
    }
    
    public void setOnFlashListerern(OnFlashListerern mOnFlashListerern){
    	this.mOnFlashListerern = mOnFlashListerern;
    	
    }
	public void initFlashMenu(View view){
		mFlashMenu = (FlashMenu)view.findViewById(R.id.flash_menu);
        mFlashMenu.initView((ViewGroup)view.findViewById(R.id.falsh_contain),mFlashPicker);
        mFlashMenu.setFlashMenuOnListerern(new FlashMenuOnListerern() {

			@Override
			public void onShowFlashMenu() {
				// TODO Auto-generated method stub
				if(mOnFlashListerern != null){
					mOnFlashListerern.onShowFlashMenu();
				}
                Log.i(TAG, "onShowFlashMenu");
				hidePicker(BUTTON_HDR);
				/*prize-xuchunming-201804026-add spotlight-start*/ 
				mContext.onSpotLightVisibleChange(false);
				/*prize-xuchunming-201804026-add spotlight-end*/ 
			}

			@Override
			public void onHideFlashMenu() {
				// TODO Auto-generated method stub
				if(mOnFlashListerern != null){
					mOnFlashListerern.onHideFlashMenu();
				}
                Log.i(TAG, "onHideFlashMenu");
			}

			@Override
			public void onCompleteAnimation(boolean isShow) {
				Log.i(TAG, "onCompleteAnimation isShow=" + isShow + " mode=" + mContext.getCurrentMode());
				/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
				if(isShow == false && getContext().isSwitchingCamera() == false && (mContext.getCurrentMode() == ModePicker.MODE_PHOTO ||  mContext.getCurrentMode() == ModePicker.MODE_WATERMARK) && getContext().getCameraId() == 0){
					mHdr.refresh();
				}
				 /*arc add start*/
				if(isShow == false && getContext().isSwitchingCamera() == false && mContext.getCurrentMode() == ModePicker.MODE_PHOTO  && getContext().getCameraId() == 1){
					mPicSelfie.refresh();
				}
				/*arc add end*/
				/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
				if(mOnFlashListerern != null){
					mOnFlashListerern.onCompleteAnimation(isShow);
				}
				/*prize-xuchunming-201804026-add spotlight-start*/ 
				if(isShow == false) {
					mContext.onSpotLightVisibleChange(true);
				}
				/*prize-xuchunming-201804026-add spotlight-end*/ 
			}
		});
	}
	
	public void hidePicker(int buttonIndex){
		if(mPickerButtons[buttonIndex] != null){
			mPickerButtons[buttonIndex].hide();
		}
	}

	public void showPicker(int buttonIndex){
		if(mPickerButtons[buttonIndex] != null){
			mPickerButtons[buttonIndex].show();
		}
	}
	
	public void hideFlashMenu() {
		// TODO Auto-generated method stub
		if(mFlashMenu != null){
			mFlashMenu.hideMenu();
		}
	}
	
	public void hideAllFlashItem() {
		// TODO Auto-generated method stub
		if(mFlashMenu != null){
			mFlashMenu.hideMenu();
		}
	}
	
	public boolean isFlashMenuShow(){
		if(mFlashMenu != null){
			return mFlashMenu.isShow();
		}
		return false;
	}
	
	public boolean isEnableFlashMenu(){
		if(getContext().getListPreference(SettingConstants.ROW_SETTING_FLASH) != null && getContext().getListPreference(SettingConstants.ROW_SETTING_FLASH).getEntries().length > 2){
			return true;
		}else{
			return false;
		}
	}

	/*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171001-start*/
    public void onChangeNavigationBar(boolean isShow){
        Log.d(TAG,"isShow: "+isShow);
        if(mCameraPicker == null) {
        	 Log.w(TAG,"mCameraPicker == null, do not onChangeNavigationBar ");
        	return;
        }
        if(isShow == true){
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraPicker.getLayoutParams();
            layoutParams.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.camera_picker_marginBottom);
            mCameraPicker.setLayoutParams(layoutParams);

        }else{
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCameraPicker.getLayoutParams();
            layoutParams.bottomMargin = (int) mContext.getResources().getDimension(R.dimen.camera_picker_marginBottom) -
                                        NavigationBarUtils.getNavigationBarHeight(mContext) * 5/16;
            mCameraPicker.setLayoutParams(layoutParams);
        }

    }
    /*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171001-end*/

    /*prize-xuchunming-20180503-bugid:56870-start*/
	@Override
	public void onLowPower(boolean isLowPower) {
		// TODO Auto-generated method stub
		lowPowerFlash(isLowPower);
	}

	private void lowPowerFlash(boolean isLowPower) {
		// TODO Auto-generated method stub
		IconListPreference mPreference = (IconListPreference) getContext().getListPreference(
                SettingConstants.ROW_SETTING_FLASH);
		Log.d(TAG,"lowPowerFlash:"+isLowPower);
		if(mPreference != null) {
			TypedArray array;
			if(isLowPower == true) {
				array = getContext().getResources().obtainTypedArray(R.array.camera_flashmode_icons_lowpower);
			}else {
				array = getContext().getResources().obtainTypedArray(R.array.camera_flashmode_icons);
			}
	        int n = array.length();
	        int ids[] = new int[n];
	        for (int i = 0; i < n; ++i) {
	            ids[i] = array.getResourceId(i, 0);
	        }
	        array.recycle();
			mPreference.setIconIds(ids);
		}	
		
		if (mFlashMenu != null) {
			if(isLowPower == true) {
				mFlashMenu.setFlashOff(); 
			}
			mFlashMenu.updateView();
        }else if(mFlashPicker != null) {
        	mFlashPicker.refresh();
        }
	}
	/*prize-xuchunming-20180503-bugid:56870-end*/
}
