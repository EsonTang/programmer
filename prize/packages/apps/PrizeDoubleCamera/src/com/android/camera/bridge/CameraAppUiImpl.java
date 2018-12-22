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
package com.android.camera.bridge;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.android.camera.CameraActivity;
import com.android.camera.FeatureSwitcher;
import com.android.camera.FileSaver;
import com.android.camera.R;
import com.android.camera.SaveRequest;
import com.android.camera.Storage;
import com.android.camera.manager.CombinViewManager;
import com.android.camera.manager.CombinViewManager.CombinListener;
import com.android.camera.manager.EffectViewManager.EffectListener;
import com.android.camera.manager.GridManager;
import com.android.camera.manager.IndicatorManager;
import com.android.camera.manager.InfoManager;
import com.android.camera.manager.ModePicker;
import com.android.camera.manager.ModePicker.OnModeChangedListener;
import com.android.camera.manager.OnScreenHint;
import com.android.camera.manager.PickerManager;
import com.android.camera.manager.PickerManager.OnFlashListerern;
import com.android.camera.manager.PickerManager.PickerListener;
import com.android.camera.manager.EffectViewManager;
import com.android.camera.manager.RemainingManager;
import com.android.camera.manager.ReviewManager;
import com.android.camera.manager.RotateDialog;
import com.android.camera.manager.RotateProgress;
import com.android.camera.manager.SettingManager;
import com.android.camera.manager.SettingManager.SettingListener;
import com.android.camera.manager.ShutterManager;
import com.android.camera.manager.SubSettingManager;
import com.android.camera.manager.ThumbnailViewManager;
import com.android.camera.manager.ViewManager;
import com.android.camera.manager.ZoomManager;
import com.android.camera.ui.FaceBeautyEntryView;
import com.android.camera.ui.ShutterButton;
import com.android.camera.ui.ShutterButton.OnShutterButtonListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.AvoidXfermode.Mode;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.mediatek.camera.ICameraMode.CameraModeType;
import com.mediatek.camera.platform.ICameraAppUi;
import com.mediatek.camera.platform.ICameraAppUi.GestureListener;
import com.mediatek.camera.platform.ICameraView;
import com.mediatek.camera.platform.IModuleCtrl;
import com.mediatek.camera.ISettingCtrl;
import com.mediatek.camera.setting.SettingConstants;
import com.mediatek.camera.setting.preference.ListPreference;
import com.mediatek.camera.util.Log;

 //gangyun tech add begin
import com.android.camera.manager.GyBeautyManager; 
import com.android.camera.manager.GyBokehManager;
import com.prize.ui.ToastMessageView;

import static com.android.internal.R.id.time;
//gangyun tech add end
 
public class CameraAppUiImpl implements ICameraAppUi,FileSaver.FileSaverStartListener {
    private static final String TAG = "CameraAppUiImpl";

    private static final int MSG_SHOW_ONSCREEN_INDICATOR = 1000;
    private static final int MSG_DELAY_SHOW_ONSCREEN_INDICATOR = 1001;
    private static final int MSG_SHOW_REMAINING = 1002;
    private static final int MSG_ANIMATION_STOP = 1003;
    private static final int DELAY_MSG_SHOW_ONSCREEN_TIME = 1 * 1000;
    private static final int DELAY_MSG_SHOW_ONSCREEN_VIEW = 3 * 1000;
    private static final int MSG_RESTOREVIEWSTATE = 1004;
    private static final int MSG_HIDE_INFOMANAGER = 1005;
    /**
     * tag for whether auto back to VFB mode. if true means last time leave out
     * VFB mode by the preview not have face in 5s,so next time when detected
     * face will automatic into VFB mode
     */
    private boolean mIsNeedBackToVFBMode = false;

    /**
     * use for tag current whether in camera preview or in Gallery
     */
    private boolean mIsInCameraPreview = true;

    /**
     * used for tag for video shutter button can click,if WFD is connected,
     * this tag will be set to false
     */
    private boolean mIsVideoShutterButtonEanble = true;

    private final CameraActivity mCameraActivity;
    private ShutterManager mShutterManager;
    private ModePicker mModePicker;
    private ThumbnailViewManager mThumbnailManager;
    private PickerManager mPickerManager;
    private IndicatorManager mIndicatorManager;
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/  
    private GridManager mGridManager;
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/  
    private RemainingManager mRemainingManager;
    private SettingManager mSettingManager;
    private InfoManager mInfoManager;
    private ZoomManager mZoomManager;
    private MainHandler mMainHandler;
    private RotateProgress mRotateProgress;
    private ReviewManager mReviewManager;
    private RotateDialog mRotateDialog;
    private CombinViewManager mCombinViewManager;
    //gangyun tech add begin
    private GyBeautyManager mGyBeautyManager; 
    private GyBokehManager mGyBokehManager; 
    //gangyun tech add end
    private FaceBeautyEntryView mFaceBeautyEntryView;
    private SubSettingManager mSubSettingManager; // For tablet
    private ViewState mCurrentViewState = ViewState.VIEW_STATE_NORMAL;
    private ViewState mRestoreViewState = ViewState.VIEW_STATE_NORMAL;
    private OnScreenHint mRotateToast;
    private List<ViewManager> mViewManagers = new CopyOnWriteArrayList<ViewManager>();
    private HashMap<CommonUiType, ICameraView> mCameraViewArray =
            new HashMap<CommonUiType, ICameraView>();

    private ViewGroup mViewLayerBottom;
    private ViewGroup mViewLayerNormal;
    private ViewGroup mViewLayerTop;
    private ViewGroup mViewLayerShutter;
    private ViewGroup mViewLayerSetting;
    private ViewGroup mViewLayerOverlay;
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
    private ViewGroup mViewLayerGrid; // Grid Line
    /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
	private ViewGroup mViewLayerThumbnail;
	private ViewGroup mViewLayerSurfaceCover;
    // restore old surface view window's alpha value

    private static final float TRANSPARENT = 0.0f;
    private float     mOldSurfaceViewAlphaValue = 1.0f;

    private EffectListenerImpl mEffectListener = new EffectListenerImpl();
    private CombinListenerImpl mCombinListener = new CombinListenerImpl();
    private static final int MSG_HIDE_INFO = 100;
    private ToastMessageView mICameraView;
    public CameraAppUiImpl(CameraActivity context) {
        Log.i(TAG, "[CameraAppUiImpl] constructor... ");
        mCameraActivity = context;
        /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-start*/
        mICameraView = (ToastMessageView) getCameraView(SpecViewType.ADDITION_TOAST_MESSAGE);
        int orientation = mCameraActivity.getOrientationCompensation();
        mICameraView.onOrientationChanged(orientation);
        /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-end*/
        mMainHandler = new MainHandler(context.getMainLooper());
    }

    public void createCommonView() {
        mShutterManager = new ShutterManager(mCameraActivity);
        mInfoManager = new InfoManager(mCameraActivity);
        mRotateProgress = new RotateProgress(mCameraActivity);
        mRemainingManager = new RemainingManager(mCameraActivity);
        mPickerManager = new PickerManager(mCameraActivity);
        mIndicatorManager = new IndicatorManager(mCameraActivity);
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        mGridManager = new GridManager(mCameraActivity);
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
        mReviewManager = new ReviewManager(mCameraActivity);
        mRotateDialog = new RotateDialog(mCameraActivity);
        mZoomManager = new ZoomManager(mCameraActivity);
        mThumbnailManager = new ThumbnailViewManager(mCameraActivity);
        if (FeatureSwitcher.isVfbEnable()) {
            mFaceBeautyEntryView = new FaceBeautyEntryView(mCameraActivity); //add for FB entry
        }

        mSettingManager = new SettingManager(mCameraActivity);
        
        mCombinViewManager = new CombinViewManager(mCameraActivity, mCombinListener);
        //gangyun tech add begin 
        if(FeatureSwitcher.isGyBeautySupported())
      	{
	         mGyBeautyManager = new GyBeautyManager(mCameraActivity);
      	}
	    if(FeatureSwitcher.isGyBokehSupported())
      	{
	         mGyBokehManager = new GyBokehManager(mCameraActivity);
      	}
        //gangyun tech add end 

        // For tablet
        if (FeatureSwitcher.isSubSettingEnabled()) {
            mSubSettingManager = new SubSettingManager(mCameraActivity);
        }
        
        mPickerManager.setOnFlashListerern(new OnFlashListerern() {
			
			@Override
			public void onShowFlashMenu() {
				// TODO Auto-generated method stub
				mSettingManager.hide();
                /*prize-add-Hide the filter button-xiaoping-20171003-start*/
                mCombinViewManager.hide();
                /*prize-Hide the filter button-xiaoping-20171003-end*/

			}
			
			@Override
			public void onHideFlashMenu() {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onCompleteAnimation(boolean isShow) {
				// TODO Auto-generated method stub
				if(isShow == false){
					mSettingManager.show();
                    /*prize-add-Hide the filter button-xiaoping-20171003-start*/
                    mCombinViewManager.show();
                    /*prize-add-Hide the filter button-xiaoping-20171003-end*/
				}
			}
		});
    }

    public void initializeCommonView() {
        mModePicker = mCameraActivity.getModePicker();

        mCameraViewArray.put(CommonUiType.SHUTTER, new CameraViewImpl(mShutterManager));
        mCameraViewArray.put(CommonUiType.MODE_PICKER, new CameraViewImpl(mModePicker));
        mCameraViewArray.put(CommonUiType.THUMBNAIL, new CameraViewImpl(mThumbnailManager));
        mCameraViewArray.put(CommonUiType.PICKER, new CameraViewImpl(mPickerManager));
        mCameraViewArray.put(CommonUiType.INDICATOR, new CameraViewImpl(mIndicatorManager));
        mCameraViewArray.put(CommonUiType.REMAINING, new CameraViewImpl(mRemainingManager));
        mCameraViewArray.put(CommonUiType.INFO, new CameraViewImpl(mInfoManager));
        mCameraViewArray.put(CommonUiType.REVIEW, new CameraViewImpl(mReviewManager));
        mCameraViewArray.put(CommonUiType.ROTATE_PROGRESS, new CameraViewImpl(mRotateProgress));
        mCameraViewArray.put(CommonUiType.ROTATE_DIALOG, new CameraViewImpl(mRotateDialog));
        mCameraViewArray.put(CommonUiType.ZOOM, new CameraViewImpl(mZoomManager));
        mCameraViewArray.put(CommonUiType.SETTING, new CameraViewImpl(mSettingManager));
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        mCameraViewArray.put(CommonUiType.GRID, new CameraViewImpl(mGridManager));
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
        if (mFaceBeautyEntryView != null) {
            mCameraViewArray.put(CommonUiType.FACE_BEAUTY_ENTRY, new CameraViewImpl(
                    mFaceBeautyEntryView));
        }
    }

    public void initializeAfterPreview() {
    	/*prize-xuchunming-20160919-add double camera activity boot-start*/
    	mModePicker.setDefaultMode(mCameraActivity.getCurrentMode());
    	/*prize-xuchunming-20160919-add double camera activity boot-end*/
        mModePicker.setCurrentMode(mCameraActivity.getCurrentMode());
    }
    @Override
    public ICameraView getCameraView(CommonUiType type) {
        return mCameraViewArray.get(type);
    }

    @Override
    public ImageView getVideoShutter() {
        return mShutterManager.getVideoShutter();
    }

    @Override
    public ImageView getPhotoShutter() {
        return mShutterManager.getPhotoShutter();
    }

    @Override
    public void switchShutterType(ShutterButtonType type) {
        switch (type) {
        case SHUTTER_TYPE_PHOTO_VIDEO:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_PHOTO_VIDEO);
            break;

        case SHUTTER_TYPE_PHOTO:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_PHOTO);
            break;

        case SHUTTER_TYPE_VIDEO:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_VIDEO);
            break;

        case SHUTTER_TYPE_OK_CANCEL:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_OK_CANCEL);
            break;

        case SHUTTER_TYPE_CANCEL:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_CANCEL);
            break;

        case SHUTTER_TYPE_CANCEL_VIDEO:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_CANCEL_VIDEO);
            break;

        case SHUTTER_TYPE_SLOW_VIDEO:
            mShutterManager.switchShutter(ShutterManager.SHUTTER_TYPE_SLOW_VIDEO);
            break;

        default:
            break;
        }
    }

    public ShutterButtonType getShutterType() {
        int type = mShutterManager.getShutterType();
        switch (type) {
        case ShutterManager.SHUTTER_TYPE_PHOTO_VIDEO:
            return ShutterButtonType.SHUTTER_TYPE_PHOTO_VIDEO;

        case ShutterManager.SHUTTER_TYPE_PHOTO:
            return ShutterButtonType.SHUTTER_TYPE_PHOTO;

        case ShutterManager.SHUTTER_TYPE_VIDEO:
            return ShutterButtonType.SHUTTER_TYPE_VIDEO;

        case ShutterManager.SHUTTER_TYPE_OK_CANCEL:
            return ShutterButtonType.SHUTTER_TYPE_OK_CANCEL;

        case ShutterManager.SHUTTER_TYPE_CANCEL:
            return ShutterButtonType.SHUTTER_TYPE_CANCEL;

        case ShutterManager.SHUTTER_TYPE_CANCEL_VIDEO:
            return ShutterButtonType.SHUTTER_TYPE_CANCEL_VIDEO;

        case ShutterManager.SHUTTER_TYPE_SLOW_VIDEO:
            return ShutterButtonType.SHUTTER_TYPE_SLOW_VIDEO;

        default:
            Log.w(TAG, "[getShutterType] illegal type:" + type);
            return null;
        }
    }

    @Override
    public void setCurrentMode(CameraModeType mode) {
        Log.i(TAG, "[setCurrentMode] mode = " + mode + ",curMode:" + mModePicker.getCurrentMode());

        int modePickerMode = getModePickerMode(mode);
        if (modePickerMode == mModePicker.getCurrentMode()) {
            return;
        }
        //disable change mode when onModechange
        mModePicker.setEnabled(false);
        mModePicker.setCurrentMode(modePickerMode);
        mModePicker.setEnabled(true);
    }

    @Override
    public void setThumbnailRefreshInterval(int ms) {
        mThumbnailManager.setRefreshInterval(ms);
    }

    @Override
    public void forceThumbnailUpdate() {
        mThumbnailManager.forceUpdate();
    }

    @Override
    public Uri getThumbnailUri() {
       return mThumbnailManager.getThumbnailUri();
    }

    @Override
    public String getThumbnailMimeType() {
        return mThumbnailManager.getThumbnailMimeType();
    }

    @Override
    public void setCameraId(int cameraId) {
        mPickerManager.setCameraId(cameraId);
    }

    @Override
    public void updatePreference() {
        mPickerManager.onPreferenceReady();
    }

    public void updateSnapShotUIView(boolean enabled) {
        if(mCameraActivity.isNonePickIntent() == true){
    		mCameraActivity.showBorder(enabled);
            mZoomManager.setEnabled(!enabled);
            mShutterManager.getPhotoShutter().setEnabled(!enabled);
    	}
    }

    public void showRemainHint() {
        mRemainingManager.showHint();
    }

    public void clearRemainAvaliableSpace() {
        mRemainingManager.clearAvaliableSpace();
    }

    public boolean showRemainIfNeed() {
        return mRemainingManager.showIfNeed();
    }

    public void notifyParametersReady() {
        Log.i(TAG, "[notifyParametersReady]");
        if (ViewState.VIEW_STATE_RECORDING == getViewState()
                || ViewState.VIEW_STATE_PRE_RECORDING == getViewState()) {
            return;
        }
	//gangyun tech add begin 
	     if(mGyBeautyManager != null)
	     {
	         if( mModePicker.getCurrentMode() == ModePicker.MODE_FACEART && mCurrentViewState != ViewState.VIEW_STATE_SETTING){
	            mGyBeautyManager.show(); 
	         }else{
                mGyBeautyManager.hide(); 
	         }
	     }
		 if(mGyBokehManager != null)
		 {
	        if( mModePicker.getCurrentMode() == ModePicker.MODE_BOKEH && mCurrentViewState != ViewState.VIEW_STATE_SETTING){
	          mGyBokehManager.show(); 
	        }else{
                mGyBokehManager.hide(); 
	         }
		 }
	//gangyun tech add end  
    }

    @Override
    public long updateRemainStorage() {
       return mRemainingManager.updateStorage();
    }

    public void setSettingCtrl(ISettingCtrl settingCtrl) {
        mSettingManager.setSettingController(settingCtrl);
        mShutterManager.setSettingController(settingCtrl);

        if (FeatureSwitcher.isSubSettingEnabled()) {
            mSubSettingManager.setSettingController(settingCtrl);
        }
    }

    public void showText(CharSequence text) {
        mInfoManager.showText(text);
    }

    public boolean collapseSetting(boolean force) {
        return mSettingManager.collapse(force);
    }

    public boolean collapseSubSetting(boolean force) {
        return mSubSettingManager.collapse(force);
    }


    public boolean performSettingClick() {
        return mSettingManager.handleMenuEvent();
    }

    public boolean isSettingShowing() {
        return mSettingManager.isShowSettingContainer();
    }

    public void setSettingListener(SettingListener listener) {
        mSettingManager.setListener(listener);
    }

    public void setSubSettingListener(SettingListener listener) {
        mSubSettingManager.setListener(listener);
    }

    @Override
    public void setReviewCompensation(int compensation) {
        mReviewManager.setOrientationCompensation(compensation);
    }

    @Override
    public void changeZoomForQuality() {
        mZoomManager.changeZoomForQuality();
    }

    @Override
    public void onDetectedSceneMode(int scene, boolean suggestedHdr) {
        mIndicatorManager.onDetectedSceneMode(scene);
        /*if (suggestedHdr) {
            mPickerManager.forceEnable(SettingConstants.KEY_HDR);
            showToast(mCameraActivity.getString(R.string.asd_hdr_guide));
        } else {
            mPickerManager.cancelForcedEnable(SettingConstants.KEY_HDR);
            hideToast();
        }*/
    }

    @Override
    public void restoreSceneMode() {
        mPickerManager.cancelForcedEnable(SettingConstants.KEY_HDR);
        hideToast();
        mIndicatorManager.restoreSceneMode();
    }
    @Override
    public void setSwipeEnabled(boolean enabled) {
    }

    @Override
    public void setGestureListener(GestureListener listener) {
        mCameraActivity.setGestureListener(listener);
    }

    @Override
    public ViewGroup getBottomViewLayer() {
        return mViewLayerBottom;
    }

    @Override
    public ViewGroup getNormalViewLayer() {
        return mViewLayerNormal;
    }

    @Override
    public ViewGroup getSurfaceCoverViewLayer() {
        return mViewLayerSurfaceCover;
    }
    @Override
    public void changeBackToVFBModeStatues(boolean isNeed) {
        Log.i(TAG, "[changeBackToVFBModeStatues] isNeed = " + isNeed);
        mIsNeedBackToVFBMode = isNeed;
    }

    @Override
    public boolean isNeedBackToVFBMode() {
        return mIsNeedBackToVFBMode;
    }

    @Override
    public void updateFaceBeatuyEntryViewVisible(boolean visible) {
        Log.i(TAG, "[updateFaceBeatuyEntryViewVisible] visible = " + visible
                + ",mIsInCameraPreview = " + mIsInCameraPreview + ",mIsNeedBackToVFBMode = "
                + mIsNeedBackToVFBMode);
        if (mFaceBeautyEntryView != null) {
            if (visible && mIsInCameraPreview) {
                /**
                 * Case One: when into Camera->Gallery,but current the FD is
                 * also running, so this case not need to show the FB relative
                 * UI.
                 *
                 * Case Two: when last time leave VFB mode by automatic[the
                 * preview have no face in 5s], so the tag:mIsNeedBackToVFBMode
                 * will be set true, and now the preview have face, in this case
                 * not need show the FB entry view,directly into VFB mode
                 */
                if (mIsNeedBackToVFBMode) {
                    changeBackToVFBModeStatues(false);
                    setCurrentMode(CameraModeType.EXT_MODE_FACE_BEAUTY);
                } else {
                    mFaceBeautyEntryView.show();
                }
            } else {
                mFaceBeautyEntryView.hide();
            }
        }
    }

    @Override
    public void showRemaining() {
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        //doShowRemaining(false);
        mMainHandler.obtainMessage(MSG_SHOW_REMAINING, false).sendToTarget();
    }

    public void refreshModeRelated() {
        // should update ModePicker mode firstly.
        mModePicker.refresh();
        mPickerManager.refresh();
        mShutterManager.refresh();
        mCombinViewManager.refresh();
    }

    @Override
    public void setReviewListener(OnClickListener retatekListener, OnClickListener playListener) {
        mReviewManager.setReviewListener(retatekListener, playListener);
    }

    @Override
    public void showReview(String filePath, FileDescriptor fd) {
        if (null != filePath) {
            setViewState(ViewState.VIEW_STATE_REVIEW);
            mReviewManager.show(filePath);
        } else if (null != fd) {
            setViewState(ViewState.VIEW_STATE_REVIEW);
            mReviewManager.show(fd);
        } else {
            setViewState(ViewState.VIEW_STATE_REVIEW);
            mReviewManager.show();
        }
    }

    @Override
    public void hideReview() {
        mReviewManager.hide();
        restoreViewState();
    }

    @Override
    public void showProgress(String msg) {
        setViewState(ViewState.VIEW_STATE_SAVING);
        mRotateProgress.showProgress(msg);
    }

    @Override
    public void dismissProgress() {
        mRotateProgress.hide();
    }

    @Override
    public boolean isShowingProgress() {
        return mRotateProgress.isShowing();
    }

    @Override
    public void showInfo(final String text) {
        showInfo(text, DELAY_MSG_SHOW_ONSCREEN_VIEW);
    }

    @Override
    public void showInfo(final CharSequence text, int showMs) {
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        doShowInfo(text, showMs);
    }

    @Override
    public void dismissInfo() {
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        mMainHandler.sendEmptyMessage(MSG_SHOW_ONSCREEN_INDICATOR);
    }

    @Override
    public boolean collapseViewManager(boolean force) {
        boolean handle = false;
        // hide dialog if it's showing.
        if (mRotateDialog.isShowing() && !force) {
            mRotateDialog.collapse(force);
            handle = true;
        } else {
            for (ViewManager manager : mViewManagers) {
                handle = manager.collapse(force);
                if (!force && handle) {
                    break; // just collapse one sub list
                }
            }
        }
        Log.d(TAG, "collapseViewManager(" + force + ") return " + handle);
        return handle;
    }

    @Override
    public void restoreViewState() {
        /*prize-add-bug:52745 Fix child thread update UI error-xiaoping-20180314-start*/
        //setViewState(ViewState.VIEW_STATE_NORMAL);
        mMainHandler.sendEmptyMessage(MSG_RESTOREVIEWSTATE);
        /*prize-add-bug:52745 Fix child thread update UI error-xiaoping-20180314-end*/
    }

    @Override
    public void setPhotoShutterEnabled(boolean enable) {
        mShutterManager.setPhotoShutterEnabled(enable);
    }


    @Override
    public void updateVideoShutterStatues(boolean enable) {
        mIsVideoShutterButtonEanble = enable;
    }

    @Override
    public void setVideoShutterEnabled(boolean enable) {
        mShutterManager.setVideoShutterEnabled(enable ? mIsVideoShutterButtonEanble : false);
    }

    @Override
    public void setOkButtonEnabled(boolean enable) {
        mShutterManager.setOkButtonEnabled(enable);

    }

    @Override
    public int getPreviewFrameWidth() {
        return mCameraActivity.getPreviewFrameWidth();
    }

    @Override
    public int getPreviewFrameHeight() {
        return mCameraActivity.getPreviewFrameHeight();
    }

    @Override
    public View getPreviewFrameLayout() {
        return mCameraActivity.getPreviewFrameLayout();
    }

    public void hideAllViews() {
        if (mCameraActivity.isNonePickIntent() && mCameraActivity.getParameters() != null) {
            // if mParameters is not ready, it should not show ModePicker,
            // otherwise JE
            // would pop up when change capture mode
            mModePicker.hide();
            mThumbnailManager.hide();
        }
        mSettingManager.hide();
        mIndicatorManager.hide();
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        mGridManager.hide();
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
        mPickerManager.hide();
        mRemainingManager.hide();
        mCombinViewManager.hide();
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
		//gangyun tech add begin
		if(mGyBeautyManager != null){
	       mGyBeautyManager.hide(); 
		}		
		if(mGyBokehManager != null){
	       mGyBokehManager.hide();
		}
		//gangyun tech add end 
    }
    
    public boolean canShowMode() {
    	return mCombinViewManager.canShowMode();
    }

    @Override
    public void showAllViews() {
        if (mCameraActivity.isNonePickIntent() && mCameraActivity.getParameters() != null) {
            // if mParameters is not ready, it should not show ModePicker,
            // otherwise JE
            // would pop up when change capture mode
            mModePicker.show();
            mThumbnailManager.show();
        }
        mSettingManager.show();
        mIndicatorManager.show();
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        mGridManager.show();
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
        mPickerManager.show();
        mRemainingManager.show();
        //gangyun tech add begin 
	    if(mGyBeautyManager != null && mModePicker.getCurrentMode() == ModePicker.MODE_FACEART){
	         mGyBeautyManager.show(); 
	    }
	    
	 if(mGyBokehManager != null && mModePicker.getCurrentMode() == ModePicker.MODE_BOKEH){
	         mGyBokehManager.show(); 
	 }
       	//gangyun tech add end  
        mCombinViewManager.show();
    }

    @Override
    public void setViewState(ViewState state) {
        Log.i(TAG, "[setViewState],mCurrentViewState:" + mCurrentViewState + ",newState:" + state);
        Log.d(TAG, android.util.Log.getStackTraceString(new Throwable()));
        if (mCurrentViewState == state) {
            return;
        }

        if (mSettingManager.isShowSettingContainer()) {
            state = ViewState.VIEW_STATE_SETTING;
        } else if (FeatureSwitcher.isSubSettingEnabled()
                && mSubSettingManager.isShowSettingContainer()) {
            state = ViewState.VIEW_STATE_SUB_SETTING;
        }

        if (state == ViewState.VIEW_STATE_CAMERA_CLOSED) {
            mRestoreViewState = mCurrentViewState;
        }

        if (mCurrentViewState == ViewState.VIEW_STATE_CAMERA_CLOSED
                && state != ViewState.VIEW_STATE_CAMERA_OPENED) {
            Log.d(TAG, "[setViewState] set restore view state mRestoreViewState:"
                    + mRestoreViewState + ",state:" + state);
            if(!(mRestoreViewState == ViewState.VIEW_STATE_REVIEW  && mCameraActivity.getCurrentMode() == ModePicker.MODE_VIDEO)){
            	 mRestoreViewState = state;
            }
            return;
        }

        if (state == ViewState.VIEW_STATE_CAMERA_OPENED) {
            mCurrentViewState = mRestoreViewState;
            Log.d(TAG, "[setViewState] view state:" + mCurrentViewState);
        } else {
            mCurrentViewState = state;
        }

        switch (mCurrentViewState) {
        case VIEW_STATE_NORMAL:
            setViewManagerVisible(true);
            setViewManagerEnable(true);
            mShutterManager.setEnabled(true);
            mShutterManager.setVideoShutterEnabled(mIsVideoShutterButtonEanble);
            mSettingManager.setFileter(false);
            mSettingManager.setAnimationEnabled(true, true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.setFileter(false);
                mSubSettingManager.setAnimationEnabled(true, true);
            }
           /* if (!mCameraActivity.isNonePickIntent()
                    && mCameraActivity.isCameraOpened()) {*/
                mPickerManager.show();
           // }
            if (!mMainHandler.hasMessages(MSG_SHOW_ONSCREEN_INDICATOR)) {
                showIndicator(0);
            } else {
                Log.d(TAG, "[setViewState]mMainHandler has message MSG_SHOW_ONSCREEN_INDICATOR");
            }
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
			mGridManager.show();
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
			mCombinViewManager.show();
			mCombinViewManager.onShowMode();
			mPickerManager.hideFlashMenu();
			
			 //gangyun tech add begin 
		     if(mGyBeautyManager != null && mModePicker.getCurrentMode() == ModePicker.MODE_FACEART){
		         mGyBeautyManager.show(); 
		     }
	     if(mGyBokehManager != null && mModePicker.getCurrentMode() == ModePicker.MODE_BOKEH){
	         mGyBokehManager.show(); 
	     }
	    	 //gangyun tech add end  
            break;

        case VIEW_STATE_CAPTURE:
            mModePicker.hideToast();
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            setViewManagerEnable(false);
            mShutterManager.setEnabled(false);
            break;

        case VIEW_STATE_PRE_RECORDING:
            mModePicker.setEnabled(false);
            mPickerManager.setEnabled(false);
            mShutterManager.setEnabled(false);
            mSettingManager.setEnabled(false);
			//gangyun tech add begin
			if(mGyBeautyManager != null){
	            mGyBeautyManager.setEnabled(false); 
			}
			if(mGyBokehManager != null){
	            mGyBokehManager.setEnabled(false);
			}
			//gangyun tech add end
            mModePicker.hide();
            mPickerManager.hide();
            mSettingManager.hide();
			//gangyun tech add begin
			if(mGyBeautyManager != null){
	           mGyBeautyManager.hide();
			}
			if(mGyBokehManager != null){
	           mGyBokehManager.hide();
			}
			//gangyun tech add end
			/*prize-xuchunming-2018040417-bugid:53082-start*/
			mThumbnailManager.hide();
			setViewManagerVisible(false);
            setViewManagerEnable(false);
			/*prize-xuchunming-2018040417-bugid:53082-end*/
            break;
        case VIEW_STATE_RECORDING:
            mModePicker.hideToast();
            mShutterManager.setEnabled(true);
            // when WFD Connected,then VR,will found the VideoShutter button
            // will be set to enable TODO will check this whether need
            mShutterManager.setVideoShutterEnabled(mIsVideoShutterButtonEanble);
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            setViewManagerVisible(false);
            setViewManagerEnable(false);
            mZoomManager.setEnabled(true);
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
            mGridManager.show();
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
            /*prize-xuchunming-2018040417-bugid:53082-start*/
            //mThumbnailManager.hide();
            /*prize-xuchunming-2018040417-bugid:53082-end*/
            break;

        case VIEW_STATE_SETTING:
            mModePicker.hide();
            mThumbnailManager.hide();
            mPickerManager.hide();
		    //gangyun tech add begin
		    if(mGyBeautyManager!=null){
	        	mGyBeautyManager.hide(); 
		    }
			if(mGyBokehManager!=null){
	            mGyBokehManager.hide();
			}
			//gangyun tech add end
            setViewManagerEnable(false);
            mSettingManager.setEnabled(true);
            mIndicatorManager.refresh();
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
            mGridManager.refresh();
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
            //when is show setting ,need hide the FaceBeauty view
            if (mFaceBeautyEntryView != null) {
                mFaceBeautyEntryView.hide();
            }
            mShutterManager.setEnabled(false);
            break;

        case VIEW_STATE_SUB_SETTING:
            mModePicker.hide();
            mThumbnailManager.hide();
            mPickerManager.hide();
            setViewManagerEnable(false);
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.setEnabled(true);
            }
            break;

        case VIEW_STATE_FOCUSING:
            setViewManagerEnable(false);
            mShutterManager.setEnabled(false);
            break;
        case VIEW_STATE_MVFOCUSING:
            setViewManagerEnable(false);
            mShutterManager.setEnabled(false);
            break;
        case VIEW_STATE_SAVING:
            mModePicker.hideToast();
            mShutterManager.setEnabled(false);
            setViewManagerVisible(false);
            setViewManagerEnable(false);
            break;

        case VIEW_STATE_REVIEW:
            mShutterManager.setEnabled(true);
            setViewManagerVisible(false);
            setViewManagerEnable(false);
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
            mGridManager.show();
			/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
            break;

        case VIEW_STATE_PICKING:
            mShutterManager.setEnabled(true);
            setViewManagerVisible(false);
            setViewManagerEnable(false);
            //prize-xucm-2015-12-3-show gridline
            mGridManager.show();
            break;
        case VIEW_STATE_CONTINUOUS_CAPTURE:
            mModePicker.hideToast();
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            //setViewManagerVisible(false);
            setViewManagerEnable(false);
            mShutterManager.setVideoShutterEnabled(false);
            break;

        case VIEW_STATE_CAMERA_CLOSED:
            setViewManagerEnable(false);
            mShutterManager.setEnabled(false);
            // Hide indicator for not show indicator before remaining show.
            mIndicatorManager.hide();
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
            mGridManager.hide();
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
            // when switch camera ,need hide the FaceBeauty view
            if (mFaceBeautyEntryView != null) {
                mFaceBeautyEntryView.hide();
            }
            mMainHandler.removeMessages(MSG_SHOW_REMAINING);
            break;

        case VIEW_STATE_LOMOEFFECT_SETTING:
            mModePicker.hide();
            mThumbnailManager.hide();
            mPickerManager.hide();
            setViewManagerEnable(false);
            mSettingManager.hide();
            mIndicatorManager.refresh();
            mShutterManager.setEnabled(false);
            mShutterManager.hide();
            hideToast();
            if (mFaceBeautyEntryView != null) {
                mFaceBeautyEntryView.hide();
            }
            break;

        case VIEW_STATE_HIDE_ALL_VIEW:
            hideAllViews();
            break;
        case VIEW_STATE_SHOW_BLUR:
        	hideInfoManager();
            setViewManagerEnable(false);
            mShutterManager.setEnabled(false);
            mGridManager.hide();
            mCameraActivity.getCameraActor().onModeBlueChange();
            break;
        case VIEW_STATE_PANORAMA_CAPTURE:
        	mModePicker.hideToast();
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            setViewManagerVisible(false);
            setViewManagerEnable(false);
            mShutterManager.setVideoShutterEnabled(false);
            break;
        default:
            break;
        }
    }

    public ViewState getViewState() {
        return mCurrentViewState;
    }

    @Override
    public ICameraView getCameraView(SpecViewType type) {
        return ViewFactory.getInstance().createViewManager(mCameraActivity, type);
    }

    @Override
    public void showToastForShort(int stringId) {
        String message = mCameraActivity.getString(stringId);
        Log.d(TAG, "[showToastForShort](" + message + ")");
        showToastForShort(message);
    }

    public void showToastForShort(String message) {
        Log.d(TAG, "showToast(" + message + ")");
        if (message != null && mCameraActivity.isFullScreen()) {
            if (mRotateToast == null) {
                mRotateToast = OnScreenHint.makeText(mCameraActivity, message);
            } else {
                mRotateToast.setText(message);
            }
            mRotateToast.showToastForShort();
        }
    }

    @Override
    public void setVideoShutterMask(boolean enable) {
        mShutterManager.setVideoShutterMask(enable);
    };

    @Override
    public boolean isNormalViewState() {
        return (mCurrentViewState == ViewState.VIEW_STATE_NORMAL);
    }

    @Override
    public void setCamcorderProfile(CamcorderProfile profile) {
        mRemainingManager.setCamcorderProfile(profile);
    }

    @Override
    public void showToast(int stringId) {
        String message = mCameraActivity.getString(stringId);
        showToast(message);
    }

    @Override
    public void showToast(String message) {
        Log.d(TAG, "showToast(" + message + ")");
        if (message != null && mCameraActivity.isFullScreen()) {
            if (mRotateToast == null) {
                mRotateToast = OnScreenHint.makeText(mCameraActivity, message);
            } else {
                mRotateToast.setText(message);
            }
            mRotateToast.showToast();
        }
    }

    public void hideInfoManager() {
        if (mInfoManager != null) {
            mInfoManager.hide();
        }
    }

    @Override
    public void showRemainingAways() {
        Log.i(TAG, "[showRemainingAways]");
        /*prize-modify-bugid:58427 prompt information does not disappear-xiaoping-20180521-start*/
        if (mInfoManager != null && mInfoManager.isShowing()) {
			mMainHandler.sendEmptyMessage(MSG_HIDE_INFOMANAGER);
		}
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        /*prize-modify-bugid:58427 prompt information does not disappear-xiaoping-20180521-end*/
        //doShowRemaining(true);
        mMainHandler.obtainMessage(MSG_SHOW_REMAINING, true).sendToTarget();
    }

    public void applayViewCallbacks() {
        mShutterManager.setShutterListener(mPhotoShutterListener, mVideoShutterListener,
                mCameraActivity.getCameraActor().getOkListener(), mCameraActivity.getCameraActor()
                        .getCancelListener());
    }

    public void clearViewCallbacks() {
        mShutterManager.setShutterListener(null, null, null, null);
    }

    public void setModeChangeListener(OnModeChangedListener listener) {
        mModePicker.setListener(listener);
    }

    public void setThumbnailFileSaver(FileSaver saver) {
        mThumbnailManager.addFileSaver(saver);
    }

    public void resetSettings() {
        mSettingManager.resetSettings();
        mCombinViewManager.reStore();
    }
    public void showSetting() {
        mSettingManager.showSetting();
    }
	
	/*prize add Storage path selection function wanzhijuan 2016-10-19 start*/
    public void refreshSetting() {
    	mSettingManager.refresh();
    }
	/*prize add Storage path selection function wanzhijuan 2016-10-19 end*/
	
    @Override
    public void hideToast() {
        Log.d(TAG, "[hideToast]");
        if (mRotateToast != null) {
            mRotateToast.cancel();
        }
    }

    @Override
    public void showAlertDialog(String title, String msg, String button1Text, final Runnable r1,
            String button2Text, final Runnable r2) {
        mRotateDialog.showAlertDialog(title, msg, button1Text, r1, button2Text, r2);
    }

    public void showIndicator(int delayMs) {
        Log.d(TAG, "[showIndicator] (" + delayMs + ")");
        mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        if (delayMs > 0) {
            mMainHandler.sendEmptyMessageDelayed(MSG_SHOW_ONSCREEN_INDICATOR, delayMs);
        } else {
            mMainHandler.sendEmptyMessage(MSG_SHOW_ONSCREEN_INDICATOR);
        }
    }

    public boolean addViewManager(ViewManager viewManager) {
        if (!mViewManagers.contains(viewManager)) {
            return mViewManagers.add(viewManager);
        }
        return false;
    }

    public boolean removeViewManager(ViewManager viewManager) {
        return mViewManagers.remove(viewManager);
    }

    public void dismissAlertDialog() {
        mRotateDialog.hide();
    }

    public void addFileSaver(FileSaver saver) {
        mThumbnailManager.addFileSaver(saver);
        if (saver != null) {
            /*prize-add-thumbnail generation monitor-xiaoping-20171020-start*/
            saver.addSaveStartListener(this);
            /*prize-add-thumbnail generation monitor-xiaoping-20171020-end*/
        }
    }

    public void removeFileSaver(FileSaver saver) {
        mThumbnailManager.removeFileSaver(saver);
        removeFileSaver(saver);
    }

    public void initializeViewGroup() {
        mViewLayerBottom = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_bottom);
        mViewLayerNormal = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_normal);
        mViewLayerTop = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_top);
        mViewLayerShutter = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_shutter);
        mViewLayerSetting = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_setting);
        mViewLayerOverlay = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_overlay);
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        mViewLayerGrid = (ViewGroup) mCameraActivity.findViewById(R.id.view_grid);
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
		mViewLayerThumbnail = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_thumbnial);
		mViewLayerSurfaceCover = (ViewGroup) mCameraActivity.findViewById(R.id.view_layer_surfaceviewcover);
    }

    public void removeAllView() {
        if (mViewLayerBottom != null) {
            mViewLayerBottom.removeAllViews();
        }
        if (mViewLayerNormal != null) {
            mViewLayerNormal.removeAllViews();
        }
        if (mViewLayerShutter != null) {
            mViewLayerShutter.removeAllViews();
        }
        if (mViewLayerSetting != null) {
            mViewLayerSetting.removeAllViews();
        }
        if (mViewLayerOverlay != null) {
            mViewLayerOverlay.removeAllViews();
        }
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        if (mViewLayerGrid != null) {
        	mViewLayerGrid.removeAllViews();
        }
        /*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
		
		if (mViewLayerThumbnail != null) {
        	mViewLayerThumbnail.removeAllViews();
        }
		
		if (mViewLayerSurfaceCover != null) {
			mViewLayerSurfaceCover.removeAllViews();
        }
    }

    public void addView(View view, int layer) {
        ViewGroup group = getViewLayer(layer);
        if (group != null) {
            group.addView(view);
        }
    }

    public void removeView(View view, int layer) {
        ViewGroup group = getViewLayer(layer);
        if (group != null) {
            group.removeView(view);
        }
    }

    public View inflate(int layoutId, int layer) {
        // mViewLayerNormal, mViewLayerBottom and mViewLayerTop are same
        // ViewGroup.
        // Here just use one to inflate child view.
        return mCameraActivity.getLayoutInflater().inflate(layoutId, getViewLayer(layer), false);
    }

    public void resetZoom() {
        mZoomManager.resetZoom();
    }

    public void setZoomParameter() {
        mZoomManager.setZoomParameter();
    }
    
    public void zoom(float scale) {
        /*prize-add-bugid:44003 two fingers zooming too fast-xiaoping-20171202-start*/
        if (scale > 1 ) {
            scale = (float) 1.02;
        } else {
            scale = (float) 0.98;
        }
        /*prize-add-bugid:44003 two fingers zooming too fast-xiaoping-20171202-end*/
        mZoomManager.onScale(0, 0, scale);
    }

    public void checkViewManagerConfiguration() {
        for (ViewManager manager : mViewManagers) {
            manager.checkConfiguration();
        }
    }

    public void onConfigurationChanged() {
        for (ViewManager manager : mViewManagers) {
            manager.reInflate();
        }
        //because onScreenHint is not extends view manager,
        //so when camera -> Gallery ,rotate device,back to camera
        //this time the screen hint need re-inflate
        //but current screen Hint not the interface of inflate,
        //so just set the mRotateToast to null,when next time need show
        //a toast,will re-inflate
        mRotateToast = null;
    }

    public void setCurrentMode(int mode) {
        mModePicker.setCurrentMode(mode);
    }

    public void setPickerListener(PickerListener listener) {
        mPickerManager.setListener(listener);
    }

    /**
     * Update surface view's window alpha.
     *
     * <p>In camera application, we draw transparent buffer to GLSurfaceView,
     * set it's window's alpha to 0.0f to tell HWC not compose it to reduce the loading of HWC.<p>
     *
     * <p>When slide to gallery, recover GLSurfaceView's old alpha value.</p>
     *
     * <p>Note: this method must be called in main thread.</p>
     *
     * @param surfaceView update this surface view window's alpha value
     * @param transparent true set alpha to 0.0f, false recover old alpha value
     */
    public void updateSurfaceViewAlphaValue(SurfaceView surfaceView, boolean transparent) {
        if (surfaceView != null) {
            float currentAlphaValue = surfaceView.getWindowAlpha();
            // non-transparent ---> transparent, launch camera or gallery --> camera
            if (transparent && (Math.abs(currentAlphaValue - TRANSPARENT) > 0.0001)) {
                mOldSurfaceViewAlphaValue = currentAlphaValue;
                surfaceView.setWindowAlpha(TRANSPARENT);
                Log.i(TAG, "updateSurfaceViewAlphaValue to transparent" +
                        " mOldSurfaceViewAlphaValue = " + mOldSurfaceViewAlphaValue);
            }
            // transparent ----> non-transparent, camera --> gallery
            if (!transparent && (Math.abs(currentAlphaValue - TRANSPARENT) < 0.0001)) {
                surfaceView.setWindowAlpha(mOldSurfaceViewAlphaValue);
                Log.i(TAG, "updateSurfaceViewAlphaValue recover to old alpha value" +
                        " mOldSurfaceViewAlphaValue = " + mOldSurfaceViewAlphaValue);
            }
        }
    }

    private OnShutterButtonListener mPhotoShutterListener = new OnShutterButtonListener() {

        @Override
        public void onShutterButtonLongPressed(ShutterButton button) {
            Log.i(TAG, "[photo.onShutterButtonLongPressed] (" + button + ")");
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            OnShutterButtonListener listener = mCameraActivity.getCameraActor()
                    .getPhotoShutterButtonListener();
            if (listener != null) {
                listener.onShutterButtonLongPressed(button);
            }
        }

        @Override
        public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
            Log.i(TAG, "[photo.onShutterButtonFocus] (" + button + ", " + pressed + ")");
            mSettingManager.cancleHideAnimation();
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            OnShutterButtonListener listener = mCameraActivity.getCameraActor()
                    .getPhotoShutterButtonListener();
            if (listener != null) {
                listener.onShutterButtonFocus(button, pressed);
            }
        }

        @Override
        public void onShutterButtonClick(ShutterButton button) {
            Log.i(TAG,
                    "[photo.onShutterButtonClick](" + button + ")isFullScreen()="
                            + mCameraActivity.isFullScreen());

            if (mCameraActivity.isFullScreen()) {
                mSettingManager.collapse(true);
                // For tablet
                if (FeatureSwitcher.isSubSettingEnabled()) {
                    mSubSettingManager.collapse(true);
                }
                OnShutterButtonListener listener = mCameraActivity.getCameraActor()
                        .getPhotoShutterButtonListener();
                if (listener != null) {
                    listener.onShutterButtonClick(button);
                }
            }
        }
    };

    private OnShutterButtonListener mVideoShutterListener = new OnShutterButtonListener() {

        @Override
        public void onShutterButtonLongPressed(ShutterButton button) {
            Log.i(TAG, "[video.onShutterButtonLongPressed] (" + button + ")");
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            OnShutterButtonListener listener = mCameraActivity.getCameraActor()
                    .getVideoShutterButtonListener();
            if (listener != null) {
                listener.onShutterButtonLongPressed(button);
            }
        }

        @Override
        public void onShutterButtonFocus(ShutterButton button, boolean pressed) {
            Log.i(TAG, "[Video.onShutterButtonFocus] (" + button + ", " + pressed + ")");
            if (pressed && mCameraActivity.isFullScreen()) {
                setSwipeEnabled(false);
            }
            mSettingManager.cancleHideAnimation();
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            OnShutterButtonListener listener = mCameraActivity.getCameraActor()
                    .getVideoShutterButtonListener();
            if (listener != null && mCameraActivity.isCameraOpened()) {
                listener.onShutterButtonFocus(button, pressed);
            }
        }

        @Override
        public void onShutterButtonClick(final ShutterButton button) {
            Log.i(TAG,
                    "[Video.onShutterButtonClick] (" + button + ") isFullScreen()="
                            + mCameraActivity.isFullScreen() + ",isCameraOpened = "
                            + mCameraActivity.isCameraOpened()  + ",Camera State = "
                            + mCameraActivity.getCameraState());
            // when click lomo effect icon and quickly click video shutter
            // button, the lomo effect will be show
            // when recording
            if (mCurrentViewState == ViewState.VIEW_STATE_LOMOEFFECT_SETTING) {
                return;
            }
            mSettingManager.collapse(true);
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.collapse(true);
            }
            // make sure the preview is ready when call it.
            if (mCameraActivity.isFullScreen()
                    && mCameraActivity.isCameraOpened()
                    && (mCameraActivity.getCameraState() == CameraActivity.STATE_IDLE ||
                    mCameraActivity.getCameraState() == CameraActivity.STATE_FOCUSING)) {
                OnShutterButtonListener listener = mCameraActivity
                        .getCameraActor().getVideoShutterButtonListener();
                int mode = mCameraActivity.getCameraActor().getMode();
                if (listener != null) {
                	/*prize-xuchunming-2018040417-bugid:53082-start*/
                	setViewState(ViewState.VIEW_STATE_PRE_RECORDING);
                	/*prize-xuchunming-2018040417-bugid:53082-end*/
                    listener.onShutterButtonClick(button);
                } else if (mModePicker.getModeIndex(mode) != ModePicker.MODE_VIDEO) {
                    if (Storage.getLeftSpace() > 0) {
                        if (mModePicker.getModeIndex(mode) == ModePicker.MODE_PHOTO_PIP) {
                         // new video pip mode
                            mModePicker.setCurrentMode(ModePicker.MODE_VIDEO_PIP);
                        } else {
                         // new video mode
                            mModePicker.setCurrentMode(ModePicker.MODE_VIDEO);
                        }
                        setViewState(ViewState.VIEW_STATE_PRE_RECORDING);
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                OnShutterButtonListener listener = mCameraActivity
                                        .getCameraActor().getVideoShutterButtonListener();
                                if (listener != null) {
                                    listener.onShutterButtonClick(button);
                                } else {
                                    Log.i(TAG, "error video shutter listener is null");
                                }
                            }
                        });
                    } else {
                        // should set enabld true because onShutterButtonFocus set it false
                        setSwipeEnabled(true);
                    }
                }
            }
        }
    };

    private void doShowIndicator() {
        Log.d(TAG, "[doShowIndicator]");

        mInfoManager.hide();
        mRemainingManager.hide();
        mCameraActivity.getCameraActor().onHideInfo();
        // if Effects is showing,not need show the picker button
        if (mCurrentViewState == ViewState.VIEW_STATE_NORMAL
                && (!mCameraActivity.isVideoMode() || !mCameraActivity.isNonePickIntent())
                && mCameraActivity.isCameraOpened()) {
            //mPickerManager.show();
        }
        if (mCurrentViewState != ViewState.VIEW_STATE_SAVING) {
            mIndicatorManager.show();
        }
    }

    /*prize-add-take pictures of animation-xiaoping-20170929-start*/
    public void hideCaptureAnimation() {
        if (mShutterManager.getCaptureAnimation() != null && !mShutterManager.isCaptureAnimationStop()) {
            mShutterManager.hideCaptureAnimation();
        }
    }

    public void showPhotoShutter() {
        if (mShutterManager != null) {
            mShutterManager.showPhotoShutter();
        }
    }

    /*prize-add-thumbnail generation monitor-xiaoping-20171020-start*/
    @Override
    public void onFileSaveStart(SaveRequest request) {
        Log.d(TAG,"[onFileSaveStart]");
        if (mShutterManager.getCaptureAnimation() != null && !mShutterManager.isCaptureAnimationStop()) {
            mMainHandler.sendEmptyMessageDelayed(MSG_ANIMATION_STOP,50);
        }
    }
    /*prize-add-thumbnail generation monitor-xiaoping-20171020-end*/
    /*prize-add-take pictures of animation-xiaoping-20170929-end*/


    private final class MainHandler extends Handler {

        public MainHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "msg id=" + msg.what);
            switch (msg.what) {
            case MSG_SHOW_ONSCREEN_INDICATOR:
                doShowIndicator();
                break;

            case MSG_DELAY_SHOW_ONSCREEN_INDICATOR:
                mRemainingManager.hide();
                mInfoManager.showText((CharSequence) msg.obj);
                showIndicator(msg.arg1);
                break;

            case MSG_SHOW_REMAINING:
                doShowRemaining((Boolean) msg.obj);
                break;
			case MSG_ANIMATION_STOP:
				mShutterManager.stopAnimation();
				break;
			/* prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-start */
			case MSG_HIDE_INFO:
				if (mICameraView != null) {
					mICameraView.hide();
				}
				break;
			/* prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-end */
			/* prize-add-bug:52745 Fix child thread update UI error-xiaoping-20180314-start*/
			case MSG_RESTOREVIEWSTATE:
				setViewState(ViewState.VIEW_STATE_NORMAL);
				break;
			/* prize-add-bug:52745 Fix child thread update UI error-xiaoping-20180314-end*/
			case MSG_HIDE_INFOMANAGER:
		    /*prize-modify-bugid:58427 prompt information does not disappear-xiaoping-20180521-start*/
		        if (mInfoManager != null && mInfoManager.isShowing()) {
					mInfoManager.hide();
				}
		        break;
		    /*prize-modify-bugid:58427 prompt information does not disappear-xiaoping-20180521-end*/
            default:
                break;
            }
        }
    }

    private void setViewManagerVisible(boolean visible) {
        if (visible) {
            if (mCameraActivity.isNonePickIntent() && mCameraActivity.getParameters() != null) {
                // if mParameters is not ready, it should not show ModePicker,
                // otherwise JE
                // would pop up when change capture mode
                mModePicker.show();
                mThumbnailManager.show();
            }
            mShutterManager.show();
            mSettingManager.show();
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.show();
            }

            mCombinViewManager.show();
        } else {
            mModePicker.hide();
            mPickerManager.hide();
            mSettingManager.hide();
            // For tablet
            if (FeatureSwitcher.isSubSettingEnabled()) {
                mSubSettingManager.hide();
            }
            if (mFaceBeautyEntryView != null) {
                mFaceBeautyEntryView.hide();
            }
            mCombinViewManager.hide();
        }
    }

    private void setViewManagerEnable(boolean enabled) {
        if (mCameraActivity.isNonePickIntent()) {
            if (!mCameraActivity.isModeChanged()) {
                mModePicker.setEnabled(enabled);
            }
            mThumbnailManager.setEnabled(enabled);
        }
        mSettingManager.setEnabled(enabled);
        mPickerManager.setEnabled(enabled);
        mZoomManager.setEnabled(enabled);
        if (mFaceBeautyEntryView != null) {
            mFaceBeautyEntryView.setEnabled(enabled);
        }
        // For tablet
        if (FeatureSwitcher.isSubSettingEnabled()) {
            mSubSettingManager.setEnabled(enabled);
        }
    }

    private void doShowInfo(final CharSequence text, final int showMs) {
        Log.d(TAG, "doShowInfo(" + text + ", " + showMs + ")");
        mCameraActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorManager.hide();
                //mPickerManager.hide();
                mRemainingManager.hide();
                mCameraActivity.getCameraActor().onShowInfo();
                mInfoManager.showText(text);
                mMainHandler.sendEmptyMessageDelayed(MSG_HIDE_INFO, showMs);
                if (showMs != -1) {
                    showIndicator(showMs);
                }
            }
        });
    }
    
    private void doShowInfo(final CharSequence text, final int showMs, final int topMargin) {
        Log.d(TAG, "doShowInfo(" + text + ", " + showMs + ")"+",topMargin: "+topMargin);
        mCameraActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIndicatorManager.hide();
                //mPickerManager.hide();
                mRemainingManager.hide();
                mCameraActivity.getCameraActor().onShowInfo();
                /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-start*/
                if (topMargin <= mCameraActivity.getResources().getDimension(R.dimen.camera_pano_mosaic_surface_height)) {
                    mInfoManager.showText(text, topMargin);
                } else {
                    mICameraView.updateText(0,topMargin, text);
                    mMainHandler.sendEmptyMessageDelayed(MSG_HIDE_INFO, showMs);
                }
                if (showMs != -1) {
                    showIndicator(showMs);
                }
                /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-end*/
            }
        });
    }

    private void doShowRemaining(final boolean showAways) {
        Log.d(TAG, "[doShowRemaining](" + showAways + ")");
        boolean remainingShown = false;
        if (showAways) {
            remainingShown = mRemainingManager.showAways();
        } else {
            remainingShown = mRemainingManager.showIfNeed();
        }
        if (remainingShown) {
            mIndicatorManager.hide();
            mInfoManager.hide();
            if (getViewState() == ViewState.VIEW_STATE_NORMAL
                    && (!mCameraActivity.isVideoMode() || !mCameraActivity.isNonePickIntent())) {
                mPickerManager.show();
            }
            showIndicator(DELAY_MSG_SHOW_ONSCREEN_VIEW);
        }
    }

    private ViewGroup getViewLayer(int layer) {
        Log.i(TAG, "[getViewLayer] layer:" + layer);
        ViewGroup viewLayer = null;
        switch (layer) {
        case ViewManager.VIEW_LAYER_BOTTOM:
            viewLayer = mViewLayerBottom;
            break;
        case ViewManager.VIEW_LAYER_NORMAL:
            viewLayer = mViewLayerNormal;
            break;
        case ViewManager.VIEW_LAYER_TOP:
            viewLayer = mViewLayerTop;
            break;
        case ViewManager.VIEW_LAYER_SHUTTER:
            viewLayer = mViewLayerShutter;
            break;
        case ViewManager.VIEW_LAYER_SETTING:
            viewLayer = mViewLayerSetting;
            break;
        case ViewManager.VIEW_LAYER_OVERLAY:
            viewLayer = mViewLayerOverlay;
            break;
            /*PRIZE-add setting items-wanzhijuan-2016-05-03-start*/ 
        case ViewManager.VIEW_LAYER_GRID:
        	viewLayer = mViewLayerGrid;
        	break;
        	/*PRIZE-add setting items-wanzhijuan-2016-05-03-end*/ 
		case ViewManager.VIEW_LAYER_THUMBNAIL:
            viewLayer = mViewLayerThumbnail;
            break;
		case ViewManager.VIEW_LAYER_SURFACECOVER:
            viewLayer = mViewLayerSurfaceCover;
            break;
        default:
            throw new RuntimeException("Wrong layer:" + layer);
        }
        return viewLayer;
    }

    private boolean isEffectConditionSatisfied() {
        boolean isSatisfied = false;
        ListPreference pref = mCameraActivity.getISettingCtrl()
                .getListPreference(SettingConstants.KEY_COLOR_EFFECT);
        if (pref != null && pref.isEnabled()
                && mCurrentViewState != ViewState.VIEW_STATE_SETTING
                && FeatureSwitcher.isLomoEffectEnabled()
                && mCameraActivity.isNonePickIntent()
                && ModePicker.MODE_VIDEO != mCameraActivity.getCurrentMode()) {
            isSatisfied = true;
        }
        return isSatisfied;
    }

    private class EffectListenerImpl implements EffectListener {
        @Override
        public boolean onClick() {
            mCameraActivity.getModuleManager().onEffectClick();
            return true;
        }
    }
    
    private void closeCombinMode(int mode) {
    	if (mode == CombinViewManager.COMBIN_EFFECT) {
    		mCameraActivity.getModuleManager().onEffectClose();
    	} else if (mode == CombinViewManager.COMBIN_WATERMARK) {
    		mOnModeChangedListener.onModeChanged(ModePicker.MODE_PHOTO);
    	} else if (mode == CombinViewManager.COMBIN_GIF) {
    		
    	} else if (mode == CombinViewManager.COMBIN_GYBOKEH) {
            /*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
            setCurrentMode(ModePicker.MODE_PHOTO);
            /*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/
        }
    }
    
    private void openCombinMode(int mode) {
    	if (mode == CombinViewManager.COMBIN_EFFECT) {
    		if (isNormalViewState()) {
                mEffectListener.onClick();
                mModePicker.hide();
            }
    	} else if (mode == CombinViewManager.COMBIN_WATERMARK) {
    		mOnModeChangedListener.onModeChanged(ModePicker.MODE_WATERMARK);
    		mModePicker.hide();
    	} else if (mode == CombinViewManager.COMBIN_GIF) {
    		mModePicker.show();
    	} else if (mode == CombinViewManager.COMBIN_GYBOKEH) {
            /*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-start*/
            mOnModeChangedListener.onModeChanged(ModePicker.MODE_BOKEH);
            mModePicker.hide();
            /*prize-add-gybokeh mode moved to the second menu-xiaoping-20171114-end*/
        }else {
    		mModePicker.show();
    	}
    }
    
    public void closeCombinView(int mode) {
		mCombinViewManager.closeCombinView(mode);
	}
    
    public void closeCombinView() {
		mCombinViewManager.reStore();
	}
    
    private class CombinListenerImpl implements CombinListener {
        @Override
        public boolean onCombinModeChange(int lastMode, int newMode) {
            // TODO wzj
        	closeCombinMode(lastMode);
        	openCombinMode(newMode);
            /* prize-add-set value of arc_front_superzoom -xiaoping-20180313-start */
            Log.d(TAG, "lastMode: " + lastMode + ", newMode: " + newMode);
            if (FeatureSwitcher.isArcsoftFcamSuperZoomSupported()) {
				if (mCameraActivity.getCameraId() == 1 && newMode == CombinViewManager.COMBIN_NORMAL
						&& !mCameraActivity.isPicselfieOpen()) {
					mCameraActivity.getISettingCtrl().onSettingChanged(SettingConstants.KEY_ARC_FCAM_SUPERZOOM_ENABLE,
							"on");
				} else {
					mCameraActivity.getISettingCtrl().onSettingChanged(SettingConstants.KEY_ARC_FCAM_SUPERZOOM_ENABLE,
							"off");
				}
				mCameraActivity.applyParametersToServer();
			}
			/* prize-add-set value of arc_front_superzoom -xiaoping-20180313-end */
            return true;
        }
    }

    private int getModePickerMode(CameraModeType mode) {
        switch (mode) {
        case EXT_MODE_PHOTO:
            return ModePicker.MODE_PHOTO;

        case EXT_MODE_FACE_BEAUTY:
            return ModePicker.MODE_FACE_BEAUTY;

        case EXT_MODE_PANORAMA:
            return ModePicker.MODE_PANORAMA;

        case EXT_MODE_VIDEO:
            return ModePicker.MODE_VIDEO;

        case EXT_MODE_VIDEO_PIP:
            return ModePicker.MODE_VIDEO_PIP;
       
        case EXT_MODE_DOUBLECAMERA:
            return ModePicker.MODE_DOUBLECAMERA;

        //gangyun tech add begin
	    case EXT_MODE_GY_BEAUTY:
           return ModePicker.MODE_FACEART;       
	
		case EXT_MODE_GY_BOKEH:
           return ModePicker.MODE_BOKEH;
        //gangyun tech add end 
		case EXT_MODE_SHORT_PREVIDEO:
			return ModePicker.MODE_SHORT_PREVIDEO;
		case EXT_MODE_PORTRAIT:
			return ModePicker.MODE_PORTRAIT;
        default:
            break;
        }
        return ModePicker.MODE_PHOTO;
    }

    private class CameraViewImpl implements ICameraView {

        private ViewManager mViewManager;

        public CameraViewImpl(ViewManager viewManager) {
            mViewManager = viewManager;
        }

        @Override
        public void init(Activity activity, ICameraAppUi cameraAppUi, IModuleCtrl moduleCtrl) {
        }

        @Override
        public void show() {
            mViewManager.show();
        }

        @Override
        public void hide() {
            mViewManager.hide();
        }

        @Override
        public void uninit() {
            // do nothing

        }

        @Override
        public void reset() {
            // do nothing
        }

        @Override
        public void reInflate() {
            // do nothing
        }

        @Override
        public boolean isShowing() {
            return mViewManager.isShowing();
        }

        @Override
        public boolean isEnabled() {
            return mViewManager.isEnabled();
        }

        @Override
        public int getViewHeight() {
            return mViewManager.getViewHeight();
        }

        @Override
        public int getViewWidth() {
            return mViewManager.getViewWidth();
        }

        @Override
        public boolean update(int type, Object... args) {
            return true;
        }

        @Override
        public void refresh() {
            mViewManager.refresh();
        }

        @Override
        public void setEnabled(boolean enabled) {
            mViewManager.setEnabled(enabled);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            //do nothing.
        }

        @Override
        public void setListener(Object obj) {
            // do nothing

        }
    }
    public ShutterManager getShutterManager(){
    	return mShutterManager;
    }
    /*PRIZE-12383-wanzhijuan-2016-03-02-start*/
	public void onSizeChanged(int width, int height) {
		if (mGridManager != null) {
			mGridManager.onSizeChanged(width, height);
		}
	}
	/*PRIZE-12383-wanzhijuan-2016-03-02-end*/
	
	/*prize Video status does not show grid lines wanzhijuan 2016-5-31 start*/
	public void onViewStateChanged(boolean isVideo) {
		if (mGridManager != null) {
			//mGridManager.onViewStateChanged(isVideo);
		}
	}
	/*prize Video status does not show grid lines wanzhijuan 2016-5-31 end*/
	
	public boolean isPreVideo() {
		return (mModePicker.getCurrentMode() == ModePicker.MODE_PREVIDEO || mModePicker.getCurrentMode() == ModePicker.MODE_SHORT_PREVIDEO);
	}
	
	public void onModeChange(int newMode, int oldMode){
		/*prize-xuchunming-20161104-add bgfuzzy switch-start*/
		onModeChange(newMode);
		/*prize-xuchunming-20161104-add bgfuzzy switch-end*/
    	mCombinViewManager.onModePickerChange(newMode, oldMode);
    	mPickerManager.hideFlashMenu();
    	/*prize-add-bugid:57586 Switch mode hide toast-xiaoping-20180510-start*/
    	if (mICameraView != null && mICameraView.isShowing()) {
    		mICameraView.hide();
		}
    	/*prize-add-bugid:57586 Switch mode hide toast-xiaoping-20180510-end*/
    }

	
	public void onModeChange(int newMode){
		if(mCameraActivity.isNonePickIntent() == false){
			return;
		}
    	switch(newMode){
            case ModePicker.MODE_SHORT_PREVIDEO:
	    	case ModePicker.MODE_PREVIDEO:
	    		onViewStateChanged(true);
	    		if(mShutterManager.getPhotoShutter() != null){
                	mShutterManager.getPhotoShutter().setImageResource(R.drawable.btn_photo);
                    LayoutParams lp = (LayoutParams) mShutterManager.getPhotoShutter().getLayoutParams();
            		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            		mShutterManager.getPhotoShutter().setLayoutParams(lp);
            		mShutterManager.getPhotoShutter().setVisibility(View.INVISIBLE);
                }
	    		
	    		if(mShutterManager.getVideoShutter() != null){
	    			mShutterManager.getVideoShutter().setVisibility(View.VISIBLE);
	    		}
	    		break;
	    	case ModePicker.MODE_PHOTO:
	    	case ModePicker.MODE_PORTRAIT:	
			/*arc add start*/
	    	case ModePicker.MODE_LOWLIGHT_SHOT:
	    	case ModePicker.MODE_PICTURE_ZOOM:
	    	/*arc add end*/	
	    		onViewStateChanged(false);
	    		mThumbnailManager.show();
                if(mShutterManager.getPhotoShutter() != null){
                	mShutterManager.getPhotoShutter().setImageResource(R.drawable.btn_photo);
                    LayoutParams lp = (LayoutParams) mShutterManager.getPhotoShutter().getLayoutParams();
            		lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            		mShutterManager.getPhotoShutter().setLayoutParams(lp);
            		mShutterManager.getPhotoShutter().setVisibility(View.VISIBLE);
                }
                
                if(mShutterManager.getVideoShutter() != null){
                	mShutterManager.getVideoShutter().setVisibility(View.INVISIBLE);
                }
	    		break;
	    	case ModePicker.MODE_FACE_BEAUTY:
	    	case ModePicker.MODE_PANORAMA:
	    	case ModePicker.MODE_DOUBLECAMERA:
	    	case ModePicker.MODE_BOKEH:
	    		onViewStateChanged(false);
	    		
	    		if(mShutterManager.getPhotoShutter() != null){
	    			mShutterManager.getPhotoShutter().setVisibility(View.VISIBLE);
	    		}
	    		if(mShutterManager.getVideoShutter() != null){
	    			mShutterManager.getVideoShutter().setVisibility(View.INVISIBLE);
	    		}
	    		break;
    	}
    }
	
	private OnModeChangedListener mOnModeChangedListener;
	public void setModeChangedListener(OnModeChangedListener modeChangedListener) {
		mOnModeChangedListener = modeChangedListener;
	}
	
	public boolean isFlashMenuShowing() {
		// TODO Auto-generated method stub
		if(mPickerManager != null){
			return mPickerManager.isFlashMenuShow();
		}
		return false;
	}
	public void switchCamera(int cameraId){
        /*prize-add-bug:43567 reset the preview magnification of switchCamera-xiaoping-20171130-start*/
        resetZoom();
        /*prize-add-bug:43567 reset the preview magnification of switchCamera-xiaoping-20171130-end*/
        if(cameraId == 1){
			mPickerManager.hideAllFlashItem();
		}
		closeCombinView();
	}

	public void onChangeNavigationBar(boolean isShow) {
		mSettingManager.onChangeNavigationBar(isShow);
		mShutterManager.onChangeNavigationBar(isShow);
		mThumbnailManager.onChangeNavigationBar(isShow);
		mCombinViewManager.onChangeNavigationBar(isShow);
        /*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171003-start*/
        mPickerManager.onChangeNavigationBar(isShow);
        /*prize-add-take the picture button to adapt to the navigation bar-xiaoping-20171003-end*/
		mModePicker.onChangeNavigationBar(isShow);
		mCameraActivity.getCameraActor().onNavigatChange(isShow);
	}

	@Override
	public void showInfo(CharSequence text, int showMs, int topMargin) {
		// TODO Auto-generated method stub
		mMainHandler.removeMessages(MSG_SHOW_ONSCREEN_INDICATOR);
        /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-start*/
        showMs = 2*1000;
        /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-end*/
		doShowInfo(text, showMs, topMargin);
	}

    /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-start*/
    public void setOrientation(int orention) {
        Log.d(TAG,"orention: "+orention);
        if (mICameraView != null) {
            mICameraView.onOrientationChanged(orention);
            mICameraView.refresh();
        }
        /*prize-add-bug:46073 refresh ui when the phone changes direction-xiaoping-20180109-start */
       if (mInfoManager != null) {
            mInfoManager.onOrientationChanged(orention);
            mInfoManager.refresh();
        }
       /*prize-add-bug:46073 refresh ui when the phone changes direction-xiaoping-20180109-end */
        
        
    }
    /*prize-modify-bugid:40009 Horizontal state prompt-xiaoping-20171012-end*/

    /*prize-add-bugid:43077 load animation only take pictures-xiaoping-20171113-start*/
    public void loadCaptureAnimation() {
        if (mModePicker.getCurrentMode() == ModePicker.MODE_LOWLIGHT_SHOT || mModePicker.getCurrentMode() == ModePicker.MODE_PICTURE_ZOOM
                || (mCameraActivity.getParameters().get(SettingConstants.KEY_ARC_PICSELFIE_ENABLE) != null && mCameraActivity.getParameters().get(SettingConstants.KEY_ARC_PICSELFIE_ENABLE).equals("1"))
                || (mCameraActivity.getParameters().get(SettingConstants.KEY_ARC_HDR_ENABLE) != null && mCameraActivity.getParameters().get(SettingConstants.KEY_ARC_HDR_ENABLE).equals("1"))) {
            mShutterManager.loadAnimation();
        }
    }
    /*prize-add-bugid:43077 load animation only take pictures-xiaoping-20171113-end*/

    /*prize-xuchunming-20180411-bugid:50825-start*/
    public void updateThumbnail(Bitmap bitmap) {
    	mThumbnailManager.updateThumbnail(bitmap);
    }
    /*prize-xuchunming-20180411-bugid:50825-end*/
}
