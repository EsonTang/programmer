/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.statusbar.phone;

import static android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.service.media.CameraPrewarmService;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
/// M: Add for OP customization. @{
import com.android.keyguard.EmergencyButton;
/// @}
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.PreviewInflater;

/// M: Add for OP customization. @{
import com.mediatek.keyguard.Plugin.KeyguardPluginFactory;
import com.mediatek.keyguard.ext.IEmergencyButtonExt;
/// @}

/*PRIZE-PowerExtendMode-wangxianzhen-2015-08-24-start bugid 4116*/
import android.os.PowerManager;
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-08-24-end bugid 4116*/
/*PRIZE-import-liufan-2017-03-29-start*/
import com.android.keyguard.EmergencyButton;
import android.content.res.ColorStateList;
/*PRIZE-import-liufan-2017-03-29-end*/
/*PRIZE-add for haokan-liufan-2017-10-25-start*/
import android.widget.ImageView;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import com.android.systemui.statusbar.StatusBarState;
/*PRIZE-add for haokan-liufan-2017-10-25-end*/
/*prize add by xiarui 2018-03-21 start*/
import android.os.Handler;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.provider.Settings;
/*prize add by xiarui 2018-03-21 end*/
import android.os.SystemProperties;
/**
 * Implementation for the bottom area of the Keyguard, including camera/phone affordance and status
 * text.
 */
public class KeyguardBottomAreaView extends FrameLayout implements View.OnClickListener,
        UnlockMethodCache.OnUnlockMethodChangedListener,
        AccessibilityController.AccessibilityStateChangedCallback, View.OnLongClickListener {

    final static String TAG = "PhoneStatusBar/KeyguardBottomAreaView";
    final static String FACEID_TAG = "KeyguardBottom-FaceId";

    public static final String CAMERA_LAUNCH_SOURCE_AFFORDANCE = "lockscreen_affordance";
    public static final String CAMERA_LAUNCH_SOURCE_WIGGLE = "wiggle_gesture";
    public static final String CAMERA_LAUNCH_SOURCE_POWER_DOUBLE_TAP = "power_double_tap";

    public static final String EXTRA_CAMERA_LAUNCH_SOURCE
            = "com.android.systemui.camera_launch_source";

    private static final Intent SECURE_CAMERA_INTENT =
            new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    public static final Intent INSECURE_CAMERA_INTENT =
            new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
    private static final Intent PHONE_INTENT = new Intent(Intent.ACTION_DIAL);
    private static final int DOZE_ANIMATION_STAGGER_DELAY = 48;
    private static final int DOZE_ANIMATION_ELEMENT_DURATION = 250;

    private KeyguardAffordanceView mCameraImageView;
    private KeyguardAffordanceView mLeftAffordanceView;
    private LockIcon mLockIcon;
    private TextView mIndicationText;
    private ViewGroup mPreviewContainer;

    private View mLeftPreview;
    private View mCameraPreview;

    private ActivityStarter mActivityStarter;
    private UnlockMethodCache mUnlockMethodCache;
    private LockPatternUtils mLockPatternUtils;
    private FlashlightController mFlashlightController;
    private PreviewInflater mPreviewInflater;
    private KeyguardIndicationController mIndicationController;
    private AccessibilityController mAccessibilityController;
    private PhoneStatusBar mPhoneStatusBar;
    private KeyguardAffordanceHelper mAffordanceHelper;

    private boolean mUserSetupComplete;
    private boolean mPrewarmBound;
    private Messenger mPrewarmMessenger;

    /**prize add by xiarui 2017-11-29 start**/
    private ViewGroup mFaceTipLayout;
    private ImageView mFaceIconView;
    private TextView mFaceTipInfo;
    /**prize add by xiarui 2017-11-29 end**/

    private final ServiceConnection mPrewarmConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPrewarmMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPrewarmMessenger = null;
        }
    };

    private boolean mLeftIsVoiceAssist;
    private AssistManager mAssistManager;

    /// M: Add for OP customization. @{
    private EmergencyButton mEmergencyButton;
    private IEmergencyButtonExt mEmergencyButtonExt;
    /// @}

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        /// M: Add for OP customization. @{
        mEmergencyButtonExt = KeyguardPluginFactory.getEmergencyButtonExt(context);
        /// @}
    }

    private AccessibilityDelegate mAccessibilityDelegate = new AccessibilityDelegate() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            String label = null;
            if (host == mLockIcon) {
                label = getResources().getString(R.string.unlock_label);
            } else if (host == mCameraImageView) {
                label = getResources().getString(R.string.camera_label);
            } else if (host == mLeftAffordanceView) {
                if (mLeftIsVoiceAssist) {
                    label = getResources().getString(R.string.voice_assist_label);
                } else {
                    label = getResources().getString(R.string.phone_label);
                }
            }
            info.addAction(new AccessibilityAction(ACTION_CLICK, label));
        }

        @Override
        public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == ACTION_CLICK) {
                if (host == mLockIcon) {
                    mPhoneStatusBar.animateCollapsePanels(
                            CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL, true /* force */);
                    return true;
                } else if (host == mCameraImageView) {
                    launchCamera(CAMERA_LAUNCH_SOURCE_AFFORDANCE);
                    return true;
                } else if (host == mLeftAffordanceView) {
                    launchLeftAffordance();
                    return true;
                }
            }
            return super.performAccessibilityAction(host, action, args);
        }
    };

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockPatternUtils = new LockPatternUtils(mContext);
        mPreviewContainer = (ViewGroup) findViewById(R.id.preview_container);
        mCameraImageView = (KeyguardAffordanceView) findViewById(R.id.camera_button);
        mLeftAffordanceView = (KeyguardAffordanceView) findViewById(R.id.left_button);
        mLockIcon = (LockIcon) findViewById(R.id.lock_icon);
        mIndicationText = (TextView) findViewById(R.id.keyguard_indication_text);
        /// M: Add for OP customization. @{
        mEmergencyButton = (EmergencyButton)
                findViewById(R.id.notification_keyguard_emergency_call_button);
        /// @}

        /*PRIZE-add for haokan-liufan-2017-10-25-start*/
        mArrowForHaoKanImgView = (ImageView) findViewById(R.id.keyguard_bottom_arrow_haokan);
        /*PRIZE-add for haokan-liufan-2017-10-25-end*/
        watchForCameraPolicyChanges();
        updateCameraVisibility();
        /**prize add by xiarui 2017-11-29 start**/
        if (PrizeOption.PRIZE_FACE_ID) {
            mFaceTipLayout = (ViewGroup) findViewById(R.id.prize_face_tip);
            mFaceTipInfo = (TextView) findViewById(R.id.prize_face_tip_info);
            mFaceIconView = (ImageView) findViewById(R.id.prize_face_iconview);
            mFaceIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "click face icon view");
                    mFaceTipInfo.setText("");
                    sendPowerKeyScreenOnBroadcast(0);
                    sendPowerKeyScreenOnBroadcast(1);
                }
            });
            //registerFaceIdReceiver();
            updateFaceTipVisibility(-1, isShowFaceIcon(mContext));
        }
        /**prize add by xiarui 2017-11-29 end**/
        mUnlockMethodCache = UnlockMethodCache.getInstance(getContext());
        mUnlockMethodCache.addListener(this);
        mLockIcon.update();
        setClipChildren(false);
        setClipToPadding(false);
        mPreviewInflater = new PreviewInflater(mContext, new LockPatternUtils(mContext));
        inflateCameraPreview();
        mLockIcon.setOnClickListener(this);
        mLockIcon.setOnLongClickListener(this);
        mCameraImageView.setOnClickListener(this);
        mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
        refreshView();
    }
	
	 /*prize-add by lihuangyuan,for faceid-2017-10-26-start*/
    private void sendPowerKeyScreenOnBroadcast(int screenonoff)
    {
		/*Intent intent = new Intent("com.prize.faceid.service");
		mContext.sendBroadcast(intent);*/
		 String faceCompany = "sensetime";
		 ComponentName cn ;
	
	    if(faceCompany.equals("sensetime")){
			cn = new ComponentName("com.prize.faceunlock", "com.sensetime.faceunlock.service.PrizeFaceDetectService");

	   }else{
			cn = new ComponentName("com.android.settings", "com.android.settings.face.FaceShowService");
	   }
        Intent intent = new Intent();
        intent.putExtra("screen_onoff", screenonoff);
        intent.setComponent(cn);
        /*intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK  | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent);*/
        mContext.startService(intent);
    }
	/*prize-add by lihuangyuan,for faceid-2017-10-26-end*/

    /*PRIZE-add for haokan-liufan-2017-10-25-start*/
    private ImageView mArrowForHaoKanImgView;
    private boolean isShowArrowImgView = true;
    private AnimationSet animationSet;
    private void showArrowImgViewWithAnimation(){
        stopShowArrowImgViewAnim();
        if(PrizeOption.PRIZE_LOCKSCREEN_INVERSE){
            if(textColor == 1){
                mArrowForHaoKanImgView.setImageResource(R.drawable.keyguard_bottom_arrow_haokan_w);
            }else{
                mArrowForHaoKanImgView.setImageResource(R.drawable.keyguard_bottom_arrow_haokan_b);
            }
        } else {
            mArrowForHaoKanImgView.setImageResource(R.drawable.keyguard_bottom_arrow_haokan_w);
        }
        mArrowForHaoKanImgView.setVisibility(View.VISIBLE);
        int repeatCount = 2;
        long durtion = 1500;
        animationSet = new AnimationSet(true);
        //scale
        ScaleAnimation scaleAnim =new ScaleAnimation(1.05f, 1f, 1.05f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(durtion);
        scaleAnim.setRepeatCount(repeatCount);
        animationSet.addAnimation(scaleAnim);
        
        //translation
        TranslateAnimation translateAnim = new TranslateAnimation(0, 0, 0, -30);
        translateAnim.setDuration(durtion);
        translateAnim.setRepeatCount(repeatCount);
        animationSet.addAnimation(translateAnim);
        
        //alpha
        AlphaAnimation alphaAnim = new AlphaAnimation(1, 0.01f);
        alphaAnim.setDuration(durtion);
        alphaAnim.setRepeatCount(repeatCount);
        animationSet.addAnimation(alphaAnim);
        
        animationSet.setInterpolator(new LinearInterpolator());
        animationSet.setDuration(durtion);
        animationSet.setFillAfter(true);
        animationSet.setRepeatMode(Animation.RESTART);
        //animationSet.setStartOffset(500);
        mArrowForHaoKanImgView.startAnimation(animationSet);
    }
    
    public void setShowArrawImgViewFlag(boolean isShow){
        isShowArrowImgView = isShow;
    }
    
    public void decideShowArrowImgView(){
        boolean isNeedShow = false;
        if(//!NotificationPanelView.USE_VLIFE && !NotificationPanelView.USE_ZOOKING && 
            mPhoneStatusBar != null){
            //if(!(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode())){
                boolean isUseHaoKan = mPhoneStatusBar.isOpenMagazine();
                if(mPhoneStatusBar.isUseHaoKan() && isUseHaoKan){
                    if(PrizeOption.PRIZE_SYSTEMUI_HAOKAN_SCREENVIEW){
                        if(NotificationPanelView.IS_ShowNotification_WhenShowHaoKan){
                            isNeedShow = true;
                        }
                    }
                }
            //}
        }
        NotificationPanelView.debugMagazine("decideShowArrowImgView isNeedShow : "+isNeedShow);
        if(isNeedShow){
            if(isShowArrowImgView){
                isShowArrowImgView = false;
                showArrowImgViewWithAnimation();
            }
        }else{
            stopShowArrowImgViewAnim();
        }
    }

    public void stopShowArrowImgViewAnim(){
        if(animationSet != null){
            mArrowForHaoKanImgView.clearAnimation();
            animationSet.cancel();
            animationSet = null;
        }
        mArrowForHaoKanImgView.setVisibility(View.GONE);
    }

    public void stopFaceId(){
        sendPowerKeyScreenOnBroadcast(0);
        if (PrizeOption.PRIZE_FACE_ID) {
            mFaceTipInfo.setAlpha(1f);
            mFaceTipInfo.setText(R.string.face_id_tip_0);
            mFaceIconView.setEnabled(true);
        }
    }
    /*PRIZE-add for haokan-liufan-2017-10-25-end*/
    public void refreshView(){
            mPreviewContainer.setVisibility(View.VISIBLE);
            /*--Prize--add by yuhao   close magazine lock screen --2017-2-23--start--*/
            if(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
                Log.i("guofan","refreshView isSuperSaverMode mCameraImageView setVisibility GONE");
                NotificationPanelView.debugMagazine("KeyguardBottomAreaView refreshView 2 isUseHaoKan() : " + isUseHaoKan());
                mCameraImageView.setVisibility(View.GONE);
                if(isUseHaoKan() || (mPhoneStatusBar != null && mPhoneStatusBar.getBarState() == StatusBarState.SHADE)){
                    mLockIcon.setVisibility(View.GONE);
                    mLeftAffordanceView.setVisibility(View.GONE);
                }else{
                    mLockIcon.setVisibility(View.VISIBLE);
                    mLeftAffordanceView.setVisibility(View.VISIBLE);
                }
                /*-prize-modify by lihuangyuan,show lock and phone icon-2017-03-29-end-*/
                mIndicationText.setVisibility(View.VISIBLE);
            }else{
                Log.i("guofan","refreshView mLockIcon visible");
                NotificationPanelView.debugMagazine("KeyguardBottomAreaView refreshView 3 isUseHaoKan() : " + isUseHaoKan());
                mCameraImageView.setVisibility(View.VISIBLE);
                /*PRIZE-add for haokan-liufan-2017-10-16-start*/
                if(isUseHaoKan() || (mPhoneStatusBar != null && mPhoneStatusBar.getBarState() == StatusBarState.SHADE)) {
                    mLockIcon.setVisibility(View.GONE);
                    mLeftAffordanceView.setVisibility(View.GONE);
                    mIndicationText.setVisibility(View.GONE);
                } else {
                    mLockIcon.setVisibility(View.VISIBLE);
                    mLockIcon.resetImageScale();
                    mLeftAffordanceView.setVisibility(View.VISIBLE);
                    mIndicationText.setVisibility(View.VISIBLE);
                }
                /*PRIZE-add for haokan-liufan-2017-10-16-end*/
			}
    }

    /*PRIZE-add for bugid:51190-liufan-2018-02-27-start*/
    public void showIndication(){
        if(!(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()) && isUseHaoKan()){
            return;
        }
        if(mIndicationController != null) {
            String indication = mContext.getResources().getString(R.string.fingerprint_not_recognized);
            if(mIndicationController.getShowIndication().equals(indication)){
                postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        mPhoneStatusBar.onUnlockHintStarted();
                        mPhoneStatusBar.onHintFinished();
                    }
                },1000);
            }else{
                mPhoneStatusBar.onUnlockHintStarted();
                mPhoneStatusBar.onHintFinished();
            }
        }
        mIndicationText.setVisibility(View.VISIBLE);
    }
    /*PRIZE-add for bugid:51190-liufan-2018-02-27-end*/

    //add by haokan-liufan-2016-10-09-start
	private boolean isUseHaoKan(){
		return mPhoneStatusBar != null && mPhoneStatusBar.isUseHaoKan();
	}
	//add by haokan-liufan-2016-10-09-end
	
    private void initAccessibility() {
        mLockIcon.setAccessibilityDelegate(mAccessibilityDelegate);
        mLeftAffordanceView.setAccessibilityDelegate(mAccessibilityDelegate);
        mCameraImageView.setAccessibilityDelegate(mAccessibilityDelegate);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int indicationBottomMargin = getResources().getDimensionPixelSize(
                R.dimen.keyguard_indication_margin_bottom);
        MarginLayoutParams mlp = (MarginLayoutParams) mIndicationText.getLayoutParams();

        ///M: Since we need to add a ECC button below Indication Text,
        ///   we remove this part code due to it will make a
        /// significant/weird space between Indication Text & ECC Button.
        /*
        if (mlp.bottomMargin != indicationBottomMargin) {
            mlp.bottomMargin = indicationBottomMargin;
            mIndicationText.setLayoutParams(mlp);
        }*/

        // Respect font size setting.
        mIndicationText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimensionPixelSize(
                        com.android.internal.R.dimen.text_size_small_material));

        /// M: Fix ALPS01868023, reload resources when locale changed.
        getRightView().setContentDescription(
            getResources().getString(R.string.accessibility_camera_button)) ;
        getLockIcon().setContentDescription(
            getResources().getString(R.string.accessibility_unlock_button)) ;
        getLeftView().setContentDescription(
            getResources().getString(R.string.accessibility_phone_button)) ;

        ViewGroup.LayoutParams lp = mCameraImageView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        mCameraImageView.setLayoutParams(lp);
        mCameraImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_camera_alt_24dp));

        lp = mLockIcon.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        mLockIcon.setLayoutParams(lp);
        mLockIcon.update(true /* force */);

        lp = mLeftAffordanceView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        mLeftAffordanceView.setLayoutParams(lp);
        updateLeftAffordanceIcon();
    }

    public void setActivityStarter(ActivityStarter activityStarter) {
        mActivityStarter = activityStarter;
    }

    public void setFlashlightController(FlashlightController flashlightController) {
        mFlashlightController = flashlightController;
    }

    public void setAccessibilityController(AccessibilityController accessibilityController) {
        mAccessibilityController = accessibilityController;
        mLockIcon.setAccessibilityController(accessibilityController);
        accessibilityController.addStateChangedCallback(this);
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        mPhoneStatusBar = phoneStatusBar;
        updateCameraVisibility(); // in case onFinishInflate() was called too early
    }

    public void setAffordanceHelper(KeyguardAffordanceHelper affordanceHelper) {
        mAffordanceHelper = affordanceHelper;
    }

    public void setUserSetupComplete(boolean userSetupComplete) {
        mUserSetupComplete = userSetupComplete;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    private Intent getCameraIntent() {
        KeyguardUpdateMonitor updateMonitor = KeyguardUpdateMonitor.getInstance(mContext);
        boolean canSkipBouncer = updateMonitor.getUserCanSkipBouncer(
                KeyguardUpdateMonitor.getCurrentUser());
        boolean secure = mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser());
        return (secure && !canSkipBouncer) ? SECURE_CAMERA_INTENT : INSECURE_CAMERA_INTENT;
    }

    /**
     * Resolves the intent to launch the camera application.
     */
    public ResolveInfo resolveCameraIntent() {
        return mContext.getPackageManager().resolveActivityAsUser(getCameraIntent(),
                PackageManager.MATCH_DEFAULT_ONLY,
                KeyguardUpdateMonitor.getCurrentUser());
    }

    private void updateCameraVisibility() {
        if (mCameraImageView == null) {
            // Things are not set up yet; reply hazy, ask again later
            return;
        }
        ResolveInfo resolved = resolveCameraIntent();
        boolean isCameraDisabled =
                (mPhoneStatusBar != null) && !mPhoneStatusBar.isCameraAllowedByAdmin();
        boolean visible = !isCameraDisabled
                && resolved != null
                && getResources().getBoolean(R.bool.config_keyguardShowCameraAffordance)
                && mUserSetupComplete;
		/*PRIZE-PowerExtendMode-wangxianzhen-2015-08-24-start bugid 4116*/
        if(PrizeOption.PRIZE_POWER_EXTEND_MODE && PowerManager.isSuperSaverMode()){
            Log.i(TAG,"PowerExtendMode isSuperSaverMode mCameraImageView setVisibility GONE");
            mCameraImageView.setVisibility(View.GONE);
            //mPhoneImageView.setVisibility(View.GONE);
        }else{
            mCameraImageView.setVisibility(visible ? View.VISIBLE : View.GONE);
            //updatePhoneVisibility();
        }
        /*PRIZE-PowerExtendMode-wangxianzhen-2015-08-24-end bugid 4116*/
    }

    public void updateLeftAffordanceIcon() {
        mLeftIsVoiceAssist = canLaunchVoiceAssist();
        int drawableId;
        int contentDescription;
        boolean visible = mUserSetupComplete;
        if (mLeftIsVoiceAssist) {
            drawableId = R.drawable.ic_mic_26dp;
            contentDescription = R.string.accessibility_voice_assist_button;
        } else {
            visible &= isPhoneVisible();
				drawableId = R.drawable.ic_phone_24dp;

            contentDescription = R.string.accessibility_phone_button;
        }
		/**PRIZE-haokanscreen iteration one-liufan-2016-06-23-start */
        mLeftAffordanceView.setVisibility((mPhoneStatusBar != null && mPhoneStatusBar.isUseHaoKan()) ? View.GONE : 
            visible
            ? View.VISIBLE : View.GONE);
        NotificationPanelView.debugMagazine("mLeftAffordanceView.getVisibility-->"+mLeftAffordanceView.getVisibility());
		/**PRIZE-haokanscreen iteration one-liufan-2016-06-23-end */
        mLeftAffordanceView.setImageDrawable(mContext.getDrawable(drawableId));
        mLeftAffordanceView.setContentDescription(mContext.getString(contentDescription));
    }

    public boolean isLeftVoiceAssist() {
        return mLeftIsVoiceAssist;
    }

    private boolean isPhoneVisible() {
        PackageManager pm = mContext.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
                && pm.resolveActivity(PHONE_INTENT, 0) != null;
    }

    private boolean isCameraDisabledByDpm() {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm != null && mPhoneStatusBar != null) {
            try {
                final int userId = ActivityManagerNative.getDefault().getCurrentUser().id;
                final int disabledFlags = dpm.getKeyguardDisabledFeatures(null, userId);
                final  boolean disabledBecauseKeyguardSecure =
                        (disabledFlags & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0
                                && mPhoneStatusBar.isKeyguardSecure();
                return dpm.getCameraDisabled(null) || disabledBecauseKeyguardSecure;
            } catch (RemoteException e) {
                Log.e(TAG, "Can't get userId", e);
            }
        }
        return false;
    }

    /**prize add by xiarui 2017-11-29 start**/
    private static final int DETECT_DEFAULT = -1; //无效
    private static final int DETECT_SUCCESS = 0; //检测成功
    private static final int DETECT_FAIL_NOUSER = 1; //检测不匹配
    private static final int DETECT_FAIL_NOFACE = 2; //没有检测到人脸
	private static final int DETECT_FAIL_OPENCAMERA= 3; //相机被占用
    private static final int DETECT_FACE_BY_CLICK = 4; //点击图标检测面部

    private boolean isOpenFaceId() {
        boolean isOpen = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
        return isOpen;
    }

    private boolean isHaveFace(Context context) {
        boolean isHaveFace = SystemProperties.get("persist.sys.ishavaface", "no").equals("yes");
		Log.d(FACEID_TAG,"isHaveFace : " + isHaveFace);
		return isHaveFace;
    }

    private boolean isShowFaceIcon(Context context) {
        return isOpenFaceId() && isHaveFace(context);
    }

    private final ContentObserver mFaceIdSwitchObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d(FACEID_TAG,"mFaceIdSwitchObserver - onChange");
            updateFaceTipVisibility(-1, isShowFaceIcon(mContext));
        }
    };

    public void registerFaceIdSwitchObserver() {
        final ContentResolver resolver = getContext().getContentResolver();
        resolver.registerContentObserver(Settings.System.getUriFor(Settings.System.PRIZE_FACEID_SWITCH), false, mFaceIdSwitchObserver);
    }

    public void unregisterFaceIdSwitchObserver() {
        final ContentResolver resolver = getContext().getContentResolver();
        resolver.unregisterContentObserver(mFaceIdSwitchObserver);
    }

    public void registerFaceIdReceiver() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction("com.faceid");
		//filter.addAction(Intent.ACTION_SCREEN_OFF);
		//filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);//set the priority high
        getContext().registerReceiverAsUser(mFaceIDReceiver, UserHandle.ALL, filter, null, null);
    }

    public void unregisterFaceIdReceiver() {
        getContext().unregisterReceiver(mFaceIDReceiver);
    }

    private boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        Log.d(FACEID_TAG, "is screen on:" + isScreenOn);
        return isScreenOn;
    }

    private final BroadcastReceiver mFaceIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.faceid".equals(action)) {
                final int faceResult = intent.getIntExtra("faceid", DETECT_DEFAULT);
                final boolean isShowFaceIcon = isShowFaceIcon(context);
                Log.d(FACEID_TAG, "faceResult = " + faceResult + ", isShowFaceIcon = " + isShowFaceIcon);
                if (isScreenOn(context)) {
                    updateFaceTipVisibility(faceResult, isShowFaceIcon);
                }
            }
        }
    };

    private TranslateAnimation createAnimation() {
        TranslateAnimation animation = new TranslateAnimation(5, -5, 0, 0);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(100);
        animation.setRepeatCount(2);
        animation.setRepeatMode(Animation.RESTART);
        return animation;
    }

    private void updateFaceTipVisibility(final int faceResult, final boolean isShowFaceIcon) {
        if (mFaceTipLayout != null && mFaceTipInfo != null) {
            if (!isShowFaceIcon) {
                mFaceTipLayout.setVisibility(View.GONE);
            } else {
                mFaceTipInfo.setAlpha(1f);
                mFaceTipLayout.setVisibility(View.VISIBLE);
                switch (faceResult) {
                    case DETECT_SUCCESS:
                        mFaceTipInfo.setText("");
                        mFaceIconView.setEnabled(true);
                        break;
                    case DETECT_FAIL_NOUSER:
                        mFaceTipInfo.setText(R.string.face_id_tip_1);
                        mFaceIconView.clearAnimation();
                        mFaceIconView.startAnimation(createAnimation());
                        mFaceIconView.setEnabled(true);
                        break;
                    case DETECT_FAIL_NOFACE:
                        mFaceTipInfo.setText(R.string.face_id_tip_0);
                        mFaceIconView.clearAnimation();
                        mFaceIconView.startAnimation(createAnimation());
                        mFaceIconView.setEnabled(true);
                        break;
					case DETECT_FAIL_OPENCAMERA:
                        mFaceTipInfo.setText(R.string.face_id_tip_2);
                        mFaceIconView.clearAnimation();
                        mFaceIconView.startAnimation(createAnimation());
                        mFaceIconView.setEnabled(true);
                        break;
                    case DETECT_FACE_BY_CLICK:
                        mFaceTipInfo.setText(R.string.face_id_tip_3);
                        mFaceIconView.clearAnimation();
                        mFaceIconView.startAnimation(createAnimation());
                        mFaceIconView.setEnabled(true);
                        break;
                    default:
                        mFaceTipInfo.setText("");
                        mFaceIconView.setEnabled(true);
                        break;
                }
            }
        }
    }
    /**prize add by xiarui 2017-11-29 end**/

    private void watchForCameraPolicyChanges() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(DevicePolicyManager.ACTION_DEVICE_POLICY_MANAGER_STATE_CHANGED);
        getContext().registerReceiverAsUser(mDevicePolicyReceiver,
                UserHandle.ALL, filter, null, null);
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateMonitorCallback);
    }

    @Override
    public void onStateChanged(boolean accessibilityEnabled, boolean touchExplorationEnabled) {
        mCameraImageView.setClickable(touchExplorationEnabled);
        mLeftAffordanceView.setClickable(touchExplorationEnabled);
        mCameraImageView.setFocusable(accessibilityEnabled);
        mLeftAffordanceView.setFocusable(accessibilityEnabled);
        mLockIcon.update();
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraImageView) {
            launchCamera(CAMERA_LAUNCH_SOURCE_AFFORDANCE);
        } else if (v == mLeftAffordanceView) {
            launchLeftAffordance();
        } if (v == mLockIcon) {
            if (!mAccessibilityController.isAccessibilityEnabled()) {
                handleTrustCircleClick();
            } else {
                mPhoneStatusBar.animateCollapsePanels(
                        CommandQueue.FLAG_EXCLUDE_NONE, true /* force */);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        handleTrustCircleClick();
        return true;
    }

    private void handleTrustCircleClick() {
        EventLogTags.writeSysuiLockscreenGesture(
                EventLogConstants.SYSUI_LOCKSCREEN_GESTURE_TAP_LOCK, 0 /* lengthDp - N/A */,
                0 /* velocityDp - N/A */);
        //Modify fot background_reflecting by Luyingying
        mIndicationController.showTransientIndication(
                R.string.keyguard_indication_trust_disabled, 1);
        //modify end
        mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void bindCameraPrewarmService() {
        Intent intent = getCameraIntent();
        ActivityInfo targetInfo = PreviewInflater.getTargetActivityInfo(mContext, intent,
                KeyguardUpdateMonitor.getCurrentUser(), true /* onlyDirectBootAware */);
        if (targetInfo != null && targetInfo.metaData != null) {
            String clazz = targetInfo.metaData.getString(
                    MediaStore.META_DATA_STILL_IMAGE_CAMERA_PREWARM_SERVICE);
            if (clazz != null) {
                Intent serviceIntent = new Intent();
                serviceIntent.setClassName(targetInfo.packageName, clazz);
                serviceIntent.setAction(CameraPrewarmService.ACTION_PREWARM);
                try {
                    if (getContext().bindServiceAsUser(serviceIntent, mPrewarmConnection,
                            Context.BIND_AUTO_CREATE | Context.BIND_FOREGROUND_SERVICE,
                            new UserHandle(UserHandle.USER_CURRENT))) {
                        mPrewarmBound = true;
                    }
                } catch (SecurityException e) {
                    Log.w(TAG, "Unable to bind to prewarm service package=" + targetInfo.packageName
                            + " class=" + clazz, e);
                }
            }
        }
    }

    public void unbindCameraPrewarmService(boolean launched) {
        if (mPrewarmBound) {
            if (mPrewarmMessenger != null && launched) {
                try {
                    mPrewarmMessenger.send(Message.obtain(null /* handler */,
                            CameraPrewarmService.MSG_CAMERA_FIRED));
                } catch (RemoteException e) {
                    Log.w(TAG, "Error sending camera fired message", e);
                }
            }
            mContext.unbindService(mPrewarmConnection);
            mPrewarmBound = false;
        }
    }

    public void launchCamera(String source) {
        final Intent intent = getCameraIntent();
        intent.putExtra(EXTRA_CAMERA_LAUNCH_SOURCE, source);
        boolean wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(
                mContext, intent, KeyguardUpdateMonitor.getCurrentUser());
        if (intent == SECURE_CAMERA_INTENT && !wouldLaunchResolverActivity) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    int result = ActivityManager.START_CANCELED;

                    // Normally an activity will set it's requested rotation
                    // animation on its window. However when launching an activity
                    // causes the orientation to change this is too late. In these cases
                    // the default animation is used. This doesn't look good for
                    // the camera (as it rotates the camera contents out of sync
                    // with physical reality). So, we ask the WindowManager to
                    // force the crossfade animation if an orientation change
                    // happens to occur during the launch.
                    ActivityOptions o = ActivityOptions.makeBasic();
                    o.setRotationAnimationHint(
                            WindowManager.LayoutParams.ROTATION_ANIMATION_SEAMLESS);
                    try {
                        //M: add clear top flag for ALPS02320925
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        result = ActivityManagerNative.getDefault().startActivityAsUser(
                                null, getContext().getBasePackageName(),
                                intent,
                                intent.resolveTypeIfNeeded(getContext().getContentResolver()),
                                null, null, 0, Intent.FLAG_ACTIVITY_NEW_TASK, null, o.toBundle(),
                                UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w(TAG, "Unable to start camera activity", e);
                    }
                    mActivityStarter.preventNextAnimation();
                    final boolean launched = isSuccessfulLaunch(result);
                    post(new Runnable() {
                        @Override
                        public void run() {
                            unbindCameraPrewarmService(launched);
                        }
                    });
                }
            });
        } else {

            // We need to delay starting the activity because ResolverActivity finishes itself if
            // launched behind lockscreen.
            mActivityStarter.startActivity(intent, false /* dismissShade */,
                    new ActivityStarter.Callback() {
                        @Override
                        public void onActivityStarted(int resultCode) {
                            unbindCameraPrewarmService(isSuccessfulLaunch(resultCode));
                        }
                    });
        }
    }

    private static boolean isSuccessfulLaunch(int result) {
        return result == ActivityManager.START_SUCCESS
                || result == ActivityManager.START_DELIVERED_TO_TOP
                || result == ActivityManager.START_TASK_TO_FRONT;
    }

    public void launchLeftAffordance() {

            if (mLeftIsVoiceAssist) {
                launchVoiceAssist();
            } else {
                launchPhone();
            }

    }

    private void launchVoiceAssist() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAssistManager.launchVoiceAssistFromKeyguard();
                mActivityStarter.preventNextAnimation();
            }
        };
        if (mPhoneStatusBar.isKeyguardCurrentlySecure()) {
            AsyncTask.execute(runnable);
        } else {
            mPhoneStatusBar.executeRunnableDismissingKeyguard(runnable, null /* cancelAction */,
                    false /* dismissShade */, false /* afterKeyguardGone */, true /* deferred */);
        }
    }

    private boolean canLaunchVoiceAssist() {
        return mAssistManager.canVoiceAssistBeLaunchedFromKeyguard();
    }

    private void launchPhone() {
        final TelecomManager tm = TelecomManager.from(mContext);
        if (tm.isInCall()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    tm.showInCallScreen(false /* showDialpad */);
                }
            });
        } else {
            mActivityStarter.startActivity(PHONE_INTENT, false /* dismissShade */);
        }
    }
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && visibility == VISIBLE) {
            mLockIcon.update();
            updateCameraVisibility();
        }
    }

    public KeyguardAffordanceView getLeftView() {
        return mLeftAffordanceView;
    }

    public KeyguardAffordanceView getRightView() {
        return mCameraImageView;
    }

    public View getLeftPreview() {
        return mLeftPreview;
    }

    public View getRightPreview() {
        return mCameraPreview;
    }

    public LockIcon getLockIcon() {
        return mLockIcon;
    }

    public View getIndicationView() {
        return mIndicationText;
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onUnlockMethodStateChanged() {
        mLockIcon.update();
        updateCameraVisibility();
    }

    private void inflateCameraPreview() {
        View previewBefore = mCameraPreview;
        boolean visibleBefore = false;
        if (previewBefore != null) {
            mPreviewContainer.removeView(previewBefore);
            visibleBefore = previewBefore.getVisibility() == View.VISIBLE;
        }
        mCameraPreview = mPreviewInflater.inflatePreview(getCameraIntent());
        if (mCameraPreview != null) {
            mPreviewContainer.addView(mCameraPreview);
            mCameraPreview.setVisibility(visibleBefore ? View.VISIBLE : View.INVISIBLE);
        }
        if (mAffordanceHelper != null) {
            mAffordanceHelper.updatePreviews();
        }
    }

    private void updateLeftPreview() {
        View previewBefore = mLeftPreview;
        if (previewBefore != null) {
            mPreviewContainer.removeView(previewBefore);
        }
        if (mLeftIsVoiceAssist) {
            mLeftPreview = mPreviewInflater.inflatePreviewFromService(
                    mAssistManager.getVoiceInteractorComponentName());
        } else {
            mLeftPreview = mPreviewInflater.inflatePreview(PHONE_INTENT);
        }
        if (mLeftPreview != null) {
            mPreviewContainer.addView(mLeftPreview);
            mLeftPreview.setVisibility(View.INVISIBLE);
        }
        if (mAffordanceHelper != null) {
            mAffordanceHelper.updatePreviews();
        }
    }

    public void startFinishDozeAnimation() {
        long delay = 0;
        if (mLeftAffordanceView.getVisibility() == View.VISIBLE) {
            startFinishDozeAnimationElement(mLeftAffordanceView, delay);
            delay += DOZE_ANIMATION_STAGGER_DELAY;
        }
        startFinishDozeAnimationElement(mLockIcon, delay);
        delay += DOZE_ANIMATION_STAGGER_DELAY;
        if (mCameraImageView.getVisibility() == View.VISIBLE) {
            startFinishDozeAnimationElement(mCameraImageView, delay);
        }
        mIndicationText.setAlpha(0f);
        mIndicationText.animate()
                .alpha(1f)
                .setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN)
                .setDuration(NotificationPanelView.DOZE_ANIMATION_DURATION);
    }

    private void startFinishDozeAnimationElement(View element, long delay) {
        element.setAlpha(0f);
        element.setTranslationY(element.getHeight() / 2);
        element.animate()
                .alpha(1f)
                .translationY(0f)
                .setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN)
                .setStartDelay(delay)
                .setDuration(DOZE_ANIMATION_ELEMENT_DURATION);
    }

    private final BroadcastReceiver mDevicePolicyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            post(new Runnable() {
                @Override
                public void run() {
                    updateCameraVisibility();
                }
            });
        }
    };

    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback =
            new KeyguardUpdateMonitorCallback() {
        @Override
        public void onUserSwitchComplete(int userId) {
            updateCameraVisibility();
        }

        @Override
        public void onStartedWakingUp() {
            mLockIcon.setDeviceInteractive(true);
        }

        @Override
        public void onFinishedGoingToSleep(int why) {
            mLockIcon.setDeviceInteractive(false);
        }

        @Override
        public void onScreenTurnedOn() {
            mLockIcon.setScreenOn(true);
        }

        @Override
        public void onScreenTurnedOff() {
            mLockIcon.setScreenOn(false);
        }

        @Override
        public void onKeyguardVisibilityChanged(boolean showing) {
            mLockIcon.update();
        }

        @Override
        public void onFingerprintRunningStateChanged(boolean running) {
            mLockIcon.update();
        }

        @Override
        public void onStrongAuthStateChanged(int userId) {
            mLockIcon.update();
        }

        @Override
        public void onUserUnlocked() {
            inflateCameraPreview();
            updateCameraVisibility();
            updateLeftAffordance();
        }
    };

    public void setKeyguardIndicationController(
            KeyguardIndicationController keyguardIndicationController) {
        mIndicationController = keyguardIndicationController;
    }

    public void setAssistManager(AssistManager assistManager) {
        mAssistManager = assistManager;
        updateLeftAffordance();
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
        updateLeftPreview();
    }

    public void onKeyguardShowingChanged() {
        updateLeftAffordance();
        inflateCameraPreview();
    }

    /// M: Add for OP customization. @{
    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        mEmergencyButtonExt.setEmergencyButtonVisibility(mEmergencyButton, alpha);
    }
    /// @}
    
	/**PRIZE-haokanscreen iteration one-liufan-2016-09-13-start */
    public KeyguardAffordanceView getCameraImageView(){
    	 return mCameraImageView;
    }
	/**PRIZE-haokanscreen iteration one-liufan-2016-09-13-end */
	
	/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-Start*/
	public EmergencyButton getEmergencyButton(){
		return mEmergencyButton;
	}
	public static int textColor = 0;
	public void setTextColor(int color){
		textColor = color;
		if(textColor == 0 ){
			mCameraImageView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));
			mLeftAffordanceView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));
			mEmergencyButton.setTextColor(PhoneStatusBar.LOCK_DARK_COLOR);
			mLockIcon.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));;
			mIndicationText.setTextColor(PhoneStatusBar.LOCK_DARK_COLOR);

            if (PrizeOption.PRIZE_FACE_ID) {
                mFaceIconView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_DARK_COLOR));
                mFaceTipInfo.setTextColor(PhoneStatusBar.LOCK_DARK_COLOR);
            }
		}else{
			mCameraImageView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));
			mLeftAffordanceView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));
			mEmergencyButton.setTextColor(PhoneStatusBar.LOCK_LIGHT_COLOR);
			mLockIcon.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));;
			mIndicationText.setTextColor(PhoneStatusBar.LOCK_LIGHT_COLOR);

            if (PrizeOption.PRIZE_FACE_ID) {
                mFaceIconView.setImageTintList(ColorStateList.valueOf(PhoneStatusBar.LOCK_LIGHT_COLOR));
                mFaceTipInfo.setTextColor(PhoneStatusBar.LOCK_LIGHT_COLOR);
            }
		}
	}
	/*PRIZE-Add for backgroud_reflect-luyingying-2017-09-09-end*/
	
	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-start*/
	public void nofityPowerKeyPower(boolean interactive) {
	    Log.d(FACEID_TAG, "nofityPowerKeyPower");
		final boolean isShowFaceIcon = isShowFaceIcon(getContext());
		 mFaceTipInfo.setText("");
         mFaceTipLayout.setVisibility(isShowFaceIcon ? View.VISIBLE : View.GONE);
    }
	/*prize-xuchunming-20180413:KeyguradBottomAreaView set faceid info at press powerkey-end*/
}
