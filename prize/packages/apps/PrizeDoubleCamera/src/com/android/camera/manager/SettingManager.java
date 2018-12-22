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

import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.android.camera.CameraActivity;
import com.android.camera.FeatureSwitcher;
import com.android.camera.Log;
import com.android.camera.R;
import com.android.camera.Util;
import com.android.camera.ui.RotateImageView;
import com.android.camera.ui.SettingListLayout;

import com.mediatek.camera.platform.ICameraAppUi.CommonUiType;
import com.mediatek.camera.platform.ICameraAppUi.ViewState;
import com.mediatek.camera.setting.preference.CameraPreference;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.setting.preference.PreferenceGroup;
import com.mediatek.camera.setting.SettingConstants;
import com.prize.setting.IViewType;
import com.prize.setting.NavigationBarUtils;
import com.prize.setting.SettingTool;
import com.android.camera.ui.SettingListLayout;
import com.prize.setting.TitlePreference;

import java.util.ArrayList;
import java.util.List;

public class SettingManager extends ViewManager implements SettingListLayout.Listener, CameraActivity.OnPreferenceReadyListener,CameraActivity.OnParametersReadyListener{
    private static final String TAG = "SettingManager";

    public interface SettingListener {
        void onSharedPreferenceChanged(ListPreference preference);
        void onRestorePreferencesClicked();
        void onSettingContainerShowing(boolean show);
        void onVoiceCommandChanged(int index);
        void onStereoCameraPreferenceChanged(ListPreference preference, int type);
    }

    protected static final int SETTING_PAGE_LAYER = VIEW_LAYER_SETTING;
    private static final String TAB_INDICATOR_KEY_PREVIEW = "preview";
    private static final String TAB_INDICATOR_KEY_COMMON = "common";
    private static final String TAB_INDICATOR_KEY_CAMERA = "camera";
    private static final String TAB_INDICATOR_KEY_VIDEO = "video";

    protected static final int MSG_REMOVE_SETTING = 0;
    protected static final int DELAY_MSG_REMOVE_SETTING_MS = 3000; // delay
                                                                   // remove
                                                                   // setting
    
    protected ViewGroup mSettingLayout;
    protected SettingListLayout mSettingListLayout;
    protected RotateImageView mIndicator;
    protected boolean mShowingContainer;
    private boolean mIsStereoFeatureSwitch;
    protected ISettingCtrl mSettingController;
    protected SettingListener mListener;
    private Animation mFadeIn;
    private Animation mFadeOut;
    private ListPreference mPreference;
    private boolean mCancleHideAnimation = false;
    private CameraActivity mContext;
    
    protected Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage(" + msg + ")");
            switch (msg.what) {
            case MSG_REMOVE_SETTING:
                // If we removeView and addView frequently, drawing cache may be
                // wrong.
                // Here avoid do this action frequently to workaround that
                // issue.
                if (mSettingLayout != null && mSettingLayout.getParent() != null) {
                    getContext().removeView(mSettingLayout, SETTING_PAGE_LAYER);
                }
                break;
            default:
                break;
            }
        };
    };

    public SettingManager(CameraActivity context) {
        super(context,VIEW_LAYER_SETTING);
        context.addOnPreferenceReadyListener(this);
        /*prize-xuchunming-20161104-add bgfuzzy switch-start*/
        context.addOnParametersReadyListener(this);
        /*prize-xuchunming-20161104-add bgfuzzy switch-end*/
        mContext = context;
    }

    @Override
    protected View getView() {
        View view = inflate(R.layout.setting_indicator);
        mIndicator = (RotateImageView) view.findViewById(R.id.setting_indicator);
        mIndicator.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				handleMenuEvent();
			}
		});
        return view;
    }
    
    @Override
    public void show() {
    	// TODO Auto-generated method stub
    	if(getContext().isNonePickIntent() == true && getContext().getCameraAppUI().isFlashMenuShowing() == false){
    		super.show();
    	}
    	
    }
    
    @Override
    public void onRefresh() {
        Log.i(TAG, "onRefresh() isShowing()=" + isShowing() + ", mShowingContainer="
                + mShowingContainer);
        if (mShowingContainer) { // just apply checker when
        	mSettingListLayout.reloadPreference();                                       // showing settings
        }
    }

    @Override
    public void hide() {
        collapse(true);
        super.hide();
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        releaseSettingResource();
    }

    @Override
    public boolean collapse(boolean force) { // force back key
        boolean collapsechild = false;
        if (mShowingContainer) {
        	if (!collapseChild() || force) {
        		collapse();
        	}
            collapsechild = true;
        }
        Log.i(TAG, "collapse(" + force + ") mShowingContainer=" + mShowingContainer + ", return "
                + collapsechild);
        return collapsechild;
    }
    
    private void collapse() {
    	if (mShowingContainer) {
    		collapseChild();
        	hideSetting();
        }
    }
    
    private boolean collapseChild() {
    	if (mSettingListLayout != null) {
    		return mSettingListLayout.collapseChild();
    	}
    	return false;
    }
    
    @Override
    public void onOrientationChanged(int orientation) {
        super.onOrientationChanged(orientation);
        adjustView(orientation, true);
    }
    
    private void adjustView(int orientation, boolean isAnim) {
    	Util.setOrientation(mSettingLayout, orientation, isAnim);
        navigationBar();
    }

    public void superOrientationChanged(int orientation) {
        super.onOrientationChanged(orientation);
    }

    @Override
    public void onRestorePreferencesClicked() {
        Log.i(TAG, "onRestorePreferencesClicked() mShowingContainer=" + mShowingContainer);
        if (mListener != null && mShowingContainer) {
            mListener.onRestorePreferencesClicked();
        }
    }

    @Override
    public void onSettingChanged(SettingListLayout settingList, ListPreference preference) {
        Log.i(TAG, "onSettingChanged(" + settingList + ")");
        if (mListener != null) {
        	/*prize-xuchunming-20160310-solve monkey error-start*/
        	try {
        		mListener.onSharedPreferenceChanged(preference);
        	} catch(Exception e){
        		Log.d(TAG, "onSettingChanged error"+e.getStackTrace());
        	}
        	/*prize-xuchunming-20160310-solve monkey error-end*/
            mPreference = preference;
        }
        refresh();
    }

    @Override
    public void onStereoCameraSettingChanged(SettingListLayout settingList, ListPreference preference, int index, boolean showing) {
        Log.i(TAG, "onStereo3dSettingChanged(" + settingList + ")" + ", type = " + index);
        if (mListener != null) {
            mIsStereoFeatureSwitch = true;
            mListener.onStereoCameraPreferenceChanged(preference, index);
            mPreference = preference;
        }
        if (getContext().getCurrentMode() == ModePicker.MODE_STEREO_CAMERA
                || (getContext().getCurrentMode() != ModePicker.MODE_STEREO_CAMERA && index == 2)) {
            refresh();
            return;
        }
        
        if (mShowingContainer && mSettingLayout != null) {
            mMainHandler.removeMessages(MSG_REMOVE_SETTING);
            mSettingLayout.setVisibility(View.GONE);
                    getContext().getCameraAppUI().restoreViewState();
                    mIndicator.setImageResource(R.drawable.ic_setting_normal);
            mMainHandler.sendEmptyMessageDelayed(MSG_REMOVE_SETTING,
                    DELAY_MSG_REMOVE_SETTING_MS);
        }
        
        setChildrenClickable(false);
        if (getContext().isFullScreen()) {
                mMainHandler.removeMessages(MSG_REMOVE_SETTING);
                initializeSettings();
                refresh();
                mSettingLayout.setVisibility(View.VISIBLE);
                if (mSettingLayout.getParent() == null) {
                    getContext().addView(mSettingLayout, SETTING_PAGE_LAYER);
                }
                startFadeInAnimation(mSettingLayout);
                getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_SETTING);
                mIndicator.setImageResource(R.drawable.ic_setting_pressed);
            setChildrenClickable(true);
        }
    }

    @Override
    public void onPreferenceReady() {
        releaseSettingResource();
    }
    
    public void setListener(SettingListener listener) {
        mListener = listener;
    }

    public void setSettingController(ISettingCtrl settingController) {
        mSettingController = settingController;
    }

    public boolean handleMenuEvent() {
        boolean handle = false;
        Log.i(TAG, "handleMenuEvent() isEnabled()=" + isEnabled() + ", isShowing()=" + isShowing()
                + ", mIndicator=" + mIndicator + ", return " + handle+",mShowingContainer:"+mShowingContainer);
        if (!mShowingContainer && isEnabled() && isShowing()) {
            showSetting();
        }
        handle = true;
        return handle;
    }

    protected void releaseSettingResource() {
        Log.i(TAG, "releaseSettingResource()");
        if (mIsStereoFeatureSwitch) {
            mIsStereoFeatureSwitch = false;
            Log.i(TAG, "releaseSettingResource is stereo feature, no need release");
            return;
        }
        /*collapse();
        if (mSettingLayout != null) {
            mSettingLayout = null;
        }*/
    }

    public void showSetting() {
        Log.i(TAG, "showSetting() mShowingContainer=" + mShowingContainer
                + ", getContext().isFullScreen()=" + getContext().isFullScreen());
        if (getContext().isFullScreen() && isAllowShowSetting()) {
            if (!mShowingContainer && getContext().getCameraAppUI().isNormalViewState()) {
                mMainHandler.removeMessages(MSG_REMOVE_SETTING);
                mShowingContainer = true;
                mListener.onSettingContainerShowing(mShowingContainer);
                initializeSettings();
                refresh();
                mSettingLayout.setVisibility(View.VISIBLE);
                if (mSettingLayout.getParent() == null) {
                    getContext().addView(mSettingLayout, SETTING_PAGE_LAYER);
                }
                getContext().getCameraAppUI().setViewState(ViewState.VIEW_STATE_SETTING);
                startFadeInAnimation(mSettingLayout);
                mIndicator.setImageResource(R.drawable.ic_setting_pressed);
            }
            setChildrenClickable(true);
        }
    }
    
    
     /**
     * limit show setting when some cases
     * @param null
     * @return boolean true allow show setting,false limit show setting
     */
    public boolean isAllowShowSetting() {
		
    	if(getContext().getSelfTimeManager().isSelfTimerCounting()){
    		return false;
    	}
		return true;
	}

	public void resetSettings(){
        /*if (mSettingLayout != null && mSettingLayout.getParent() != null) {
            getContext().removeView(mSettingLayout, SETTING_PAGE_LAYER);
        }
        mSettingLayout = null;*/
    	initializeSettings();
    }
    
    private void initializeSettings() {
        if (mSettingLayout == null && mSettingController.getPreferenceGroup() != null) {
            mSettingLayout = (ViewGroup) getContext().inflate(R.layout.setting_container_prize,
                    SETTING_PAGE_LAYER);
//            mTabHost = (TabHost) mSettingLayout.findViewById(R.id.tab_title);
//            mTabHost.setup();
            
            // For tablet
            /*int settingKeys[] = SettingConstants.SETTING_GROUP_COMMON_FOR_TAB;
            if (FeatureSwitcher.isSubSettingEnabled()) {
                settingKeys = SettingConstants.SETTING_GROUP_MAIN_COMMON_FOR_TAB;
            } else if (FeatureSwitcher.isLomoEffectEnabled() && getContext().isNonePickIntent()) {
                settingKeys = SettingConstants.SETTING_GROUP_COMMON_FOR_LOMOEFFECT;
            }
            List<Holder> list = new ArrayList<Holder>();
            if (getContext().isNonePickIntent() || getContext().isStereoMode()) {
                if (FeatureSwitcher.isPrioritizePreviewSize()) {
                    list.add(new Holder(TAB_INDICATOR_KEY_PREVIEW,
                            R.drawable.ic_tab_common_setting,
                            SettingConstants.SETTING_GROUP_COMMON_FOR_TAB_PREVIEW));
                    list.add(new Holder(TAB_INDICATOR_KEY_COMMON, R.drawable.ic_tab_common_setting,
                            settingKeys));
                    list.add(new Holder(TAB_INDICATOR_KEY_CAMERA, R.drawable.ic_tab_camera_setting,
                            SettingConstants.SETTING_GROUP_CAMERA_FOR_TAB_NO_PREVIEW));
                    list.add(new Holder(TAB_INDICATOR_KEY_VIDEO, R.drawable.ic_tab_video_setting,
                            SettingConstants.SETTING_GROUP_VIDEO_FOR_TAB_NO_PREVIEW));
                } else if (getContext().isStereoMode()) {
                    list.add(new Holder(TAB_INDICATOR_KEY_COMMON, R.drawable.ic_tab_common_setting,
                            settingKeys));
                    list.add(new Holder(TAB_INDICATOR_KEY_CAMERA, R.drawable.ic_tab_camera_setting,
                            SettingConstants.SETTING_GROUP_CAMERA_3D_FOR_TAB));
                    list.add(new Holder(TAB_INDICATOR_KEY_VIDEO, R.drawable.ic_tab_video_setting,
                            SettingConstants.SETTING_GROUP_VIDEO_FOR_TAB));
                } else {
                    list.add(new Holder(TAB_INDICATOR_KEY_COMMON, R.drawable.ic_tab_common_setting,
                            settingKeys));
                    list.add(new Holder(TAB_INDICATOR_KEY_CAMERA, R.drawable.ic_tab_camera_setting,
                            SettingConstants.SETTING_GROUP_CAMERA_FOR_TAB));
                    list.add(new Holder(TAB_INDICATOR_KEY_VIDEO, R.drawable.ic_tab_video_setting,
                            SettingConstants.SETTING_GROUP_VIDEO_FOR_TAB));
                }
            } else { // pick case has no video quality
                if (FeatureSwitcher.isPrioritizePreviewSize()) {
                    if (getContext().isImageCaptureIntent()) {
                        list.add(new Holder(TAB_INDICATOR_KEY_PREVIEW,
                                R.drawable.ic_tab_common_setting,
                                SettingConstants.SETTING_GROUP_COMMON_FOR_TAB_PREVIEW));
                        list.add(new Holder(TAB_INDICATOR_KEY_COMMON,
                                R.drawable.ic_tab_common_setting, settingKeys));
                        list.add(new Holder(TAB_INDICATOR_KEY_CAMERA,
                                R.drawable.ic_tab_camera_setting,
                                SettingConstants.SETTING_GROUP_CAMERA_FOR_TAB_NO_PREVIEW));
                    } else {
                        list.add(new Holder(TAB_INDICATOR_KEY_COMMON,
                                R.drawable.ic_tab_common_setting, settingKeys));
                        list.add(new Holder(TAB_INDICATOR_KEY_VIDEO,
                                R.drawable.ic_tab_video_setting,
                                SettingConstants.SETTING_GROUP_VIDEO_FOR_TAB_NO_PREVIEW));
                    }
                } else {
                    list.add(new Holder(TAB_INDICATOR_KEY_COMMON, R.drawable.ic_tab_common_setting,
                            settingKeys));
                    if (getContext().isImageCaptureIntent()) {
                        list.add(new Holder(TAB_INDICATOR_KEY_CAMERA,
                                R.drawable.ic_tab_camera_setting,
                                SettingConstants.SETTING_GROUP_CAMERA_FOR_TAB));
                    } else {
                        list.add(new Holder(TAB_INDICATOR_KEY_VIDEO,
                                R.drawable.ic_tab_video_setting,
                                SettingConstants.SETTING_GROUP_VIDEO_FOR_TAB_NO_PREVIEW));
                    }
                }
            }*/
            
            mSettingListLayout = (SettingListLayout) mSettingLayout.findViewById(R.id.view_setting_list);
            mSettingListLayout.setSettingChangedListener(this);
            ((ImageView) mSettingLayout.findViewById(R.id.im_back)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					collapse();
				}
			});
            
            ((ImageView) mSettingLayout.findViewById(R.id.im_reset)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onRestorePreferencesClicked();
				}
			});
            
            /*int size = list.size();
            for (int i = 0; i < 1; i++) {
                Holder holder = list.get(i);
                mSettingListLayout.initialize(getListPreferences(holder.mSettingKeys, i == 0));
            }*/
        }
        mSettingListLayout.initialize(SettingTool.getViewTypes(getContext(), mSettingController));
        adjustView(getOrientation(), false);
    }
    
	/*prize bug 14674 Camera settings select the interface, the virtual key to display the white can not see wanzhijuan 2016-4-28 start*/
    private void navigationBar() {
    	//xucm
    	if (mSettingLayout != null) {
    		NavigationBarUtils.checkNavigationBar(getContext(), mSettingLayout.findViewById(R.id.container), getOrientation());
    	}
    }
	/*prize bug 14674 Camera settings select the interface, the virtual key to display the white can not see wanzhijuan 2016-4-28 end*/
    
    private ArrayList<ListPreference> getListPreferences(int[] keys, boolean addrestore) {
        ArrayList<ListPreference> listItems = new ArrayList<ListPreference>();
        for (int i = 0; i < keys.length; i++) {
            String key = SettingConstants.getSettingKey(keys[i]);
            ListPreference pref = mSettingController.getListPreference(key);
            if (pref != null && pref.isShowInSetting()) {
                if (SettingConstants.KEY_VIDEO_QUALITY.equals(key)) {
                    if (!("on".equals(mSettingController
                            .getSettingValue(SettingConstants.KEY_SLOW_MOTION)))) {
                        listItems.add(pref);
                    }
                } else {
                    listItems.add(pref);
                }
                
            }
        }
        
        return listItems;
    }
    
    public void cancleHideAnimation() {
        //mCancleHideAnimation = true;
    }
    
    public void hideSetting() {
        Log.i(TAG, "hideSetting() mShowingContainer=" + mShowingContainer + ", mSettingLayout="
                + mSettingLayout);
        setChildrenClickable(false);
        if (mShowingContainer && mSettingLayout != null) {
            mMainHandler.removeMessages(MSG_REMOVE_SETTING);
            if (!mCancleHideAnimation) {
                startFadeOutAnimation(mSettingLayout);
            }
            mSettingLayout.setVisibility(View.GONE);
            mShowingContainer = false;
            //because into setting,ViewState will set mode picker false
            getContext().getCameraAppUI().getCameraView(CommonUiType.MODE_PICKER).setEnabled(true);
            /*prize-xuchunming-201804026-add spotlight-start*/ 
            //mListener.onSettingContainerShowing(mShowingContainer);
            /*prize-xuchunming-201804026-add spotlight-end*/ 
            mIndicator.setImageResource(R.drawable.ic_setting_normal);
            mMainHandler.sendEmptyMessageDelayed(MSG_REMOVE_SETTING, DELAY_MSG_REMOVE_SETTING_MS);
        }
        mCancleHideAnimation = false;
    }

    protected void setChildrenClickable(boolean clickable) {
        Log.i(TAG, "setChildrenClickable(" + clickable + ") ");
        PreferenceGroup group = mSettingController.getPreferenceGroup();
        if (group != null) {
            int len = group.size();
            for (int i = 0; i < len; i++) {
                CameraPreference pref = group.get(i);
                if (pref instanceof ListPreference) {
                    ((ListPreference) pref).setClickable(clickable);
                }
            }
        }
    }

    protected void startFadeInAnimation(View view) {
        int orientation = getOrientation();
        if (NavigationBarUtils.checkDeviceHasNavigationBar(getContext())) {
        	orientation = 0;
        }
        if (orientation == 270) {
        	mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.activity_open_in_anim_270);
        } else if (orientation == 180) {
        	mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.activity_open_in_anim_180);
        } else if (orientation == 90) {
        	mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.activity_open_in_anim_90);
        } else {
        	mFadeIn = AnimationUtils.loadAnimation(getContext(), R.anim.activity_open_in_anim_0);
        }
        if (view != null && mFadeIn != null) {
            view.startAnimation(mFadeIn);
        }
    }

    protected void startFadeOutAnimation(View view) {
        int orientation = getOrientation();
        if (NavigationBarUtils.checkDeviceHasNavigationBar(getContext())) {
        	orientation = 0;
        }
        if (orientation == 270) {
        	mFadeOut = AnimationUtils.loadAnimation(getContext(),
                    R.anim.activity_close_out_anim_270);
        } else if (orientation == 180) {
        	mFadeOut = AnimationUtils.loadAnimation(getContext(),
                    R.anim.activity_close_out_anim_180);
        } else if (orientation == 90) {
        	mFadeOut = AnimationUtils.loadAnimation(getContext(),
                    R.anim.activity_close_out_anim_90);
        } else {
        	mFadeOut = AnimationUtils.loadAnimation(getContext(),
                    R.anim.activity_close_out_anim_0);
        }
        
        mFadeOut.setAnimationListener(new AnimationListener() {
			
			public void onAnimationStart(Animation animation) {
				
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationRepeat(Animation animation) {
				
				// TODO Auto-generated method stub
				
			}
			
			public void onAnimationEnd(Animation animation) {
				
				// TODO Auto-generated method stub
				/*prize-xuchunming-201804026-add spotlight-start*/ 
				mListener.onSettingContainerShowing(mShowingContainer);
				/*prize-xuchunming-201804026-add spotlight-end*/ 
				getContext().getCameraAppUI().restoreViewState();
			}
		});
        if (view != null && mFadeOut != null) {
            view.startAnimation(mFadeOut);
        }
    }

    private class Holder {
        String mIndicatorKey;
        int mIndicatorIconRes;
        int[] mSettingKeys;

        public Holder(String key, int res, int[] keys) {
            mIndicatorKey = key;
            mIndicatorIconRes = res;
            mSettingKeys = keys;
        }
    }

    public boolean isShowSettingContainer() {
        return mShowingContainer;
    }
    
    @Override
    public void onVoiceCommandChanged(int index) {
        if (mListener != null) {
            mListener.onVoiceCommandChanged(index);
        }
    }

	public void onChangeNavigationBar(boolean isShow) {
		if (mShowingContainer) {
			if (mSettingLayout != null) {
	    		NavigationBarUtils.checkNavigationBar(getContext(), mSettingLayout.findViewById(R.id.container), isShow);
	    		mSettingListLayout.onChangeNavigationBar(isShow);
	    	}
		}
	}
	
	/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
	public void setSettingIconCenterLayout(boolean isCenterLayout){
		if(mIndicator != null){
			RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mIndicator.getLayoutParams();
			if(isCenterLayout){
				lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			}else{
				lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
			}
			mIndicator.setLayoutParams(lp);
		}
	}

	@Override
	public void onCameraParameterReady() {
		// TODO Auto-generated method stub
		if(mContext.getCurrentMode() == ModePicker.MODE_DOUBLECAMERA || mContext.getCurrentMode() == ModePicker.MODE_BOKEH){
        	setSettingIconCenterLayout(true);
        }else{
        	setSettingIconCenterLayout(false);
        }
	}
	/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
}
