/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.server.policy;

import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.policy.EmergencyAffordanceManager;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.android.internal.R;
import com.android.internal.widget.LockPatternUtils;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/*prize-shutdown menu-liuweiquan-20161025-start*/
import android.os.SystemProperties;
/*prize-shutdown menu-liuweiquan-20161025-end*/

/*PRIZE-PowerExtendMode-wangxianzhen-2015-04-14-start*/
import android.content.ContentResolver;
import android.os.PowerManager;
import com.mediatek.common.prizeoption.PrizeOption;
/*PRIZE-PowerExtendMode-wangxianzhen-2015-04-14-end*/

 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/
import android.graphics.Color;
import android.view.SurfaceControl;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.graphics.Rect;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.LinearLayout;
 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/

/**
 * Helper to show the global actions dialog.  Each item is an {@link Action} that
 * may show depending on whether the keyguard is showing, and whether the device
 * is provisioned.
 */
class GlobalActions implements DialogInterface.OnDismissListener, DialogInterface.OnClickListener  {

    private static final String TAG = "GlobalActions";

    private static final boolean SHOW_SILENT_TOGGLE = true;

    /* Valid settings for global actions keys.
     * see config.xml config_globalActionList */
    private static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    private static final String GLOBAL_ACTION_KEY_BUGREPORT = "bugreport";
    private static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    private static final String GLOBAL_ACTION_KEY_USERS = "users";
    private static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    private static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";
    private static final String GLOBAL_ACTION_KEY_VOICEASSIST = "voiceassist";
    private static final String GLOBAL_ACTION_KEY_ASSIST = "assist";
    private static final String GLOBAL_ACTION_KEY_RESTART = "restart";
	/* PRIZE-reboot-xiaxuefeng-2015-3-25-start */
	private static final String GLOBAL_ACTION_KEY_REBOOT = "reboot";
	/* PRIZE-reboot-xiaxuefeng-2015-3-25-end */

    private final Context mContext;
    private final WindowManagerFuncs mWindowManagerFuncs;
    private final AudioManager mAudioManager;
    private final IDreamManager mDreamManager;

    private ArrayList<Action> mItems;

	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
	private static final String GLOBAL_ACTION_KEY_SUPER_LESS_POWER = "superlesspower";
    private Action mPowerExtendModeAction;
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
    private Action mSilentModeAction;
    private ToggleAction mAirplaneModeOn;

    private MyAdapter mAdapter;

    private boolean mKeyguardShowing = false;
    private boolean mDeviceProvisioned = false;
    private ToggleAction.State mAirplaneState = ToggleAction.State.Off;
    private boolean mIsWaitingForEcmExit = false;
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private final boolean mShowSilentToggle;
    private final EmergencyAffordanceManager mEmergencyAffordanceManager;
 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/
    //private GlobalActionsDialog mDialog;
	private ShutdownDialog mDialog;
	private static final String SHUTDOWN = "windowManagerFuncsShutdown";
	private static final String REBOOT = "windowManagerFuncsReboot";
	private static final int SCALE_VAL = 8;
	public static int mBitmapPixels;
	public static boolean NavbarisShow;
 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/

    /**
     * @param context everything needs a context :(
     */
    public GlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        mContext = context;
        mWindowManagerFuncs = windowManagerFuncs;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mDreamManager = IDreamManager.Stub.asInterface(
                ServiceManager.getService(DreamService.DREAM_SERVICE));

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED);
        /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/
        filter.addAction(SHUTDOWN);
        filter.addAction(REBOOT);
        /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/
        context.registerReceiver(mBroadcastReceiver, filter);
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
        //ConnectivityManager cm = (ConnectivityManager)
        //        context.getSystemService(Context.CONNECTIVITY_SERVICE);		
        //mHasTelephony = cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
        // get notified of phone state changes
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE);
        // mContext.getContentResolver().registerContentObserver(
                // Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON), true,
                // mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mHasVibrator = vibrator != null && vibrator.hasVibrator();

        mShowSilentToggle = SHOW_SILENT_TOGGLE && !mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_useFixedVolume);

        mEmergencyAffordanceManager = new EmergencyAffordanceManager(context);
    }

    /**
     * Show the global actions dialog (creating if necessary)
     * @param keyguardShowing True if keyguard is showing
     */
    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        mKeyguardShowing = keyguardShowing;
        mDeviceProvisioned = isDeviceProvisioned;
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
            // Show delayed, so that the dismiss of the previous dialog completes
            mHandler.sendEmptyMessage(MESSAGE_SHOW);
        } else {
            handleShow();
        }
    }

    private void awakenIfNecessary() {
        if (mDreamManager != null) {
            try {
                if (mDreamManager.isDreaming()) {
                    mDreamManager.awaken();
                }
            } catch (RemoteException e) {
                // we tried
            }
        }
    }

    private void handleShow() {
        awakenIfNecessary();
        /*prize-OS8.0_Shutdown-change-yangming-2017_8_12-start*/
        /*mDialog = createDialog();
        prepareDialog();

        // If we only have 1 item and it's a simple press action, just do this action.
        if (mAdapter.getCount() == 1
                && mAdapter.getItem(0) instanceof SinglePressAction
                && !(mAdapter.getItem(0) instanceof LongPressAction)) {
            ((SinglePressAction) mAdapter.getItem(0)).onPress();
        } else {
            WindowManager.LayoutParams attrs = mDialog.getWindow().getAttributes();
            attrs.setTitle("GlobalActions");
            mDialog.getWindow().setAttributes(attrs);
            mDialog.show();
            mDialog.getWindow().getDecorView().setSystemUiVisibility(View.STATUS_BAR_DISABLE_EXPAND);
        }*/
		
		NavbarisShow = Settings.System.getInt(mContext.getContentResolver(),
                             Settings.System.PRIZE_NAVBAR_STATE, 0) != 0;
		Log.i("dialog","NavbarisShow = " + NavbarisShow );
	    /*if(NavbarisShow){
            Intent navIntent = new Intent("com.prize.nav_bar_control");
            navIntent.putExtra("command", "hide");
            mContext.sendBroadcast(navIntent);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Bitmap mBitmap = big(blurScale(screenShot()));
		            Drawable mDrawable = new BitmapDrawable(mContext.getResources(), mBitmap);
		            mBitmapPixels = getPicturePixel(mBitmap);
		            ShutdownDialog.Builder builder = new ShutdownDialog.Builder(mContext);
		            mDialog = builder.create();
		            mDialog.getWindow().getDecorView().setBackground(mDrawable);
		            mDialog.show();
			    }
		    }, 200);
		}else{
			Bitmap mBitmap = big(blurScale(screenShot()));
		    Drawable mDrawable = new BitmapDrawable(mContext.getResources(), mBitmap);
		    mBitmapPixels = getPicturePixel(mBitmap);
		    ShutdownDialog.Builder builder = new ShutdownDialog.Builder(mContext);
		    mDialog = builder.create();
		    mDialog.getWindow().getDecorView().setBackground(mDrawable);
		    mDialog.show();
		}*/
		if(null != screenShot()){
			Bitmap mBitmap = big(blurScale(screenShot()));
		    Drawable mDrawable = new BitmapDrawable(mContext.getResources(), mBitmap);
			/*prize-change-bugid:44103-yangming-2017_12_7-start*/
			if(mBitmap.getHeight() > 840){
				mBitmapPixels = getPicturePixel(mBitmap,300,400,420,840);
			}else{
				mBitmapPixels = getPicturePixel(mBitmap,530,280,840,670);
			}
			/*prize-change-bugid:44103-yangming-2017_12_7-end*/
		    ShutdownDialog.Builder builder = new ShutdownDialog.Builder(mContext);
		    mDialog = builder.create();
		    mDialog.getWindow().getDecorView().setBackground(mDrawable);
		    mDialog.show();
		}
		/*prize-OS8.0_Shutdown-change-yangming-2017_8_12-end*/
    }
	
    private int getPicturePixel(Bitmap bitmap,int bitmapLeft, int bitmapTop, int bitmapRight, int bitmapBottom) {

		int top = bitmapTop;
		int left = bitmapLeft;
		int right = bitmapRight;
		int bottom = bitmapBottom;
		
		int width = right - left;
		int height = bottom - top;
	    int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, left, top, width, height);
		        long totalY = 0;
        for (int i = 0; i < pixels.length; i++) {
            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16; 
            int green = (clr & 0x0000ff00) >> 8; 
            int blue = clr & 0x000000ff; 
            totalY += (red * 0.299f + green * 0.587f + blue * 0.114f);
        }
        Log.i("dialog", "getPicturePixel  totalY  = " + (int) (totalY / pixels.length) );
        return (int) (totalY / pixels.length);
		
		
    }
 /*prize-OS8.0_Shutdown-change-yangming-2017_8_12-end*/

    /**
     * Create the global actions dialog.
     * @return A new dialog.
     */
    private GlobalActionsDialog createDialog() {
        // Simple toggle style if there's no vibrator, otherwise use a tri-state
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
		if(SystemProperties.get("ro.prize_shut_menu").equals("1")){
			mSilentModeAction = new SilentModeToggleAction();
			mAirplaneModeOn = new ToggleAction(
                com.prize.internal.R.drawable.global_action_airplane_on,
                com.prize.internal.R.drawable.global_action_airplane_off,
                com.android.internal.R.string.global_actions_toggle_airplane_mode,
                com.android.internal.R.string.global_actions_airplane_mode_on_status,
                com.android.internal.R.string.global_actions_airplane_mode_off_status) {

            void onToggle(boolean on) {
				/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
                //if (mHasTelephony && Boolean.parseBoolean(
                //        SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
                //    mIsWaitingForEcmExit = true;
                    // Launch ECM exit dialog
                //    Intent ecmDialogIntent =
                //            new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null);
                //    ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //    mContext.startActivity(ecmDialogIntent);
                //} else {
                    changeAirplaneModeSystemSetting(on);
                //}
				/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
            }

            @Override
            protected void changeStateFromPress(boolean buttonOn) {
				/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
                //if (!mHasTelephony) return;				
                // In ECM mode airplane state cannot be changed
                //if (!(Boolean.parseBoolean(
                //        SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE)))) {
                //    mState = buttonOn ? State.TurningOn : State.TurningOff;
                //    mAirplaneState = mState;
                //}
				/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }

            public boolean isEnabled() {
                boolean isAirplaneModeAvailable = true;
                //FIXME: for build pass, please help to crrect me
                /*try {
                    final ITelephonyEx phoneEx = ITelephonyEx.Stub.asInterface(ServiceManager.checkService("phoneEx"));
                    if (phoneEx != null) {
                        isAirplaneModeAvailable = phoneEx.isAirplanemodeAvailableNow();
                    }
                } catch (RemoteException e) {
                    Log.d(TAG, "isAirplanemodeAvailableNow exception caught");
                }*/
                return (super.isEnabled() && isAirplaneModeAvailable);
            }
        	};
		}else{
			if (!mHasVibrator) {
            	mSilentModeAction = new SilentModeToggleAction();
        	} else {
            	mSilentModeAction = new SilentModeTriStateAction(mContext, mAudioManager, mHandler);
        	}
				mAirplaneModeOn = new ToggleAction(
                	R.drawable.ic_lock_airplane_mode,
                	R.drawable.ic_lock_airplane_mode_off,
                	R.string.global_actions_toggle_airplane_mode,
                	R.string.global_actions_airplane_mode_on_status,
                	R.string.global_actions_airplane_mode_off_status) {
            	void onToggle(boolean on) {
                	if (mHasTelephony && Boolean.parseBoolean(
                        	SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
                    	mIsWaitingForEcmExit = true;
                    	// Launch ECM exit dialog
                    	Intent ecmDialogIntent =
                            	new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null);
                    	ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    	mContext.startActivity(ecmDialogIntent);
                	} else {
                    	changeAirplaneModeSystemSetting(on);
                	}
            	}

            @Override
            protected void changeStateFromPress(boolean buttonOn) {
                if (!mHasTelephony) return;

                // In ECM mode airplane state cannot be changed
                if (!(Boolean.parseBoolean(
                        SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE)))) {
                    mState = buttonOn ? State.TurningOn : State.TurningOff;
                    mAirplaneState = mState;
                }
            }

            public boolean showDuringKeyguard() {
                return true;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
            	public boolean isEnabled() {
                	boolean isAirplaneModeAvailable = true;
                	//FIXME: for build pass, please help to crrect me
                	/*try {
                    	final ITelephonyEx phoneEx = ITelephonyEx.Stub.asInterface(ServiceManager.checkService("phoneEx"));
                    	if (phoneEx != null) {
                        	isAirplaneModeAvailable = phoneEx.isAirplanemodeAvailableNow();
                    	}
                	} catch (RemoteException e) {
                    	Log.d(TAG, "isAirplanemodeAvailableNow exception caught");
                	}*/
                	return (super.isEnabled() && isAirplaneModeAvailable);
            	}
        		};
		}
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
        //onAirplaneModeChanged();

        mItems = new ArrayList<Action>();
        String[] defaultActions = mContext.getResources().getStringArray(
                com.android.internal.R.array.config_globalActionsList);

        ArraySet<String> addedKeys = new ArraySet<String>();
		/* PRIZE-title-xiaxuefeng-2015-3-25-start */
		if (SystemProperties.get("ro.prize_shut_menu").equals("1")){
			TitleAction title = new TitleAction(com.prize.internal.R.string.global_action_title);
			mItems.add(title);
		}		
		/* PRIZE-title-xiaxuefeng-2015-3-25-end */

        /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
        mPowerExtendModeAction = new PowerExtendModeToggleAction();
        refreshPowerExtendMode();
        /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/

        for (int i = 0; i < defaultActions.length; i++) {
            String actionKey = defaultActions[i];
            if (addedKeys.contains(actionKey)) {
                // If we already have added this, don't add it again.
                continue;
            }
            if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                mItems.add(new PowerAction());
			/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
            } else if (SystemProperties.get("ro.prize_shut_menu").equals("1") && GLOBAL_ACTION_KEY_REBOOT.equals(actionKey)){
            	mItems.add(new RebootAction());
			/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
            } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                mItems.add(mAirplaneModeOn);
			/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
            } else if (GLOBAL_ACTION_KEY_SUPER_LESS_POWER.equals(actionKey)) {
				mItems.add(mPowerExtendModeAction);
			/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
			}else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                if (Settings.Global.getInt(mContext.getContentResolver(),
                        Settings.Global.BUGREPORT_IN_POWER_MENU, 0) != 0 && isCurrentUserOwner()) {
                    mItems.add(new BugReportAction());
                }
            } else if (GLOBAL_ACTION_KEY_SILENT.equals(actionKey)) {
                if (mShowSilentToggle) {
                    mItems.add(mSilentModeAction);
                }
            } else if (GLOBAL_ACTION_KEY_USERS.equals(actionKey)) {
                if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                    addUsersToMenu(mItems);
                }
            } else if (GLOBAL_ACTION_KEY_SETTINGS.equals(actionKey)) {
                mItems.add(getSettingsAction());
            } else if (GLOBAL_ACTION_KEY_LOCKDOWN.equals(actionKey)) {
                mItems.add(getLockdownAction());
            } else if (GLOBAL_ACTION_KEY_VOICEASSIST.equals(actionKey)) {
                mItems.add(getVoiceAssistAction());
            } else if (GLOBAL_ACTION_KEY_ASSIST.equals(actionKey)) {
                mItems.add(getAssistAction());
            } else if (GLOBAL_ACTION_KEY_RESTART.equals(actionKey)) {
                mItems.add(new RestartAction());
            } else {
                Log.e(TAG, "Invalid global action key " + actionKey);
            }
            // Add here so we don't add more than one.
            addedKeys.add(actionKey);
        }

        if (mEmergencyAffordanceManager.needsEmergencyAffordance()) {
            mItems.add(getEmergencyAction());
        }

        mAdapter = new MyAdapter();

        AlertParams params = new AlertParams(mContext);
        params.mAdapter = mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = true;

        GlobalActionsDialog dialog = new GlobalActionsDialog(mContext, params);
        dialog.setCanceledOnTouchOutside(false); // Handled by the custom class.

        dialog.getListView().setItemsCanFocus(true);
        dialog.getListView().setLongClickable(true);
        dialog.getListView().setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                            long id) {
                        final Action action = mAdapter.getItem(position);
                        if (action instanceof LongPressAction) {
                            return ((LongPressAction) action).onLongPress();
                        }
                        return false;
                    }
        });
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

        dialog.setOnDismissListener(this);

        return dialog;
    }

    private final class PowerAction extends SinglePressAction implements LongPressAction {
        private PowerAction() {
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
		//super(com.android.internal.R.drawable.ic_lock_power_off,
        //	R.string.global_action_power_off);
			super(com.android.internal.R.drawable.ic_lock_power_off,
            	R.string.global_action_power_off,
				com.prize.internal.R.drawable.global_action_power_off,
                com.android.internal.R.string.global_action_power_off);
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
        }

        @Override
        public boolean onLongPress() {
            UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            if (!um.hasUserRestriction(UserManager.DISALLOW_SAFE_BOOT)) {
                mWindowManagerFuncs.rebootSafeMode(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean showDuringKeyguard() {
            return true;
        }

        @Override
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override
        public void onPress() {
            // shutdown by making sure radio and power are handled accordingly.
            mWindowManagerFuncs.shutdown(false /* confirm */);
        }
    }
private final class RestartAction extends SinglePressAction implements LongPressAction {
        private RestartAction() {
            super(R.drawable.ic_restart, R.string.global_action_restart);
        }

        @Override
        public boolean onLongPress() {
            UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            if (!um.hasUserRestriction(UserManager.DISALLOW_SAFE_BOOT)) {
                mWindowManagerFuncs.rebootSafeMode(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean showDuringKeyguard() {
            return true;
        }

        @Override
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override
        public void onPress() {
            mWindowManagerFuncs.reboot(false /* confirm */);
        }
    }
	
	/* PRIZE-reboot-xiaxuefeng-2015-3-25-start */
    private final class RebootAction extends SinglePressAction implements LongPressAction {
        private RebootAction() {
            super(com.prize.internal.R.drawable.global_action_restart,
                com.prize.internal.R.string.global_action_reboot);
        }

        @Override
        public boolean onLongPress() {
            UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            if (!um.hasUserRestriction(UserManager.DISALLOW_SAFE_BOOT)) {
                mWindowManagerFuncs.rebootSafeMode(true);
                return true;
            }
            return false;
        }

        @Override
        public boolean showDuringKeyguard() {
            return true;
        }

        @Override
        public boolean showBeforeProvisioning() {
            return true;
        }

        @Override
        public void onPress() {
            mWindowManagerFuncs.reboot(false /* confirm */);
        }
    }
	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
    private final class PowerExtendModeAction extends SinglePressAction /*implements LongPressAction*/ {
        private PowerExtendModeAction(int iconID, int textID) {
            super(iconID,textID);
        }

        public void onPress() {
            PowerManager pwManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            // switch super saver mode.
            Log.i(TAG,"PowerExtendMode PowerExtendModeAction PowerManager.isSuperSaverMode() is " + PowerManager.isSuperSaverMode());
            if (PowerManager.isSuperSaverMode()){
                pwManager.switchSuperSaverMode(false);
            }else{
                pwManager.switchSuperSaverMode(true);
            }
        }

        /*@Override
        public boolean onLongPress() {
            mWindowManagerFuncs.rebootSafeMode(true);
            return true;
        }*/

        @Override
        public boolean showDuringKeyguard() {
            return true;
        }

        @Override
        public boolean showBeforeProvisioning() {
            return true;
        }
    }
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/

    private class BugReportAction extends SinglePressAction implements LongPressAction {

        public BugReportAction() {
            super(com.android.internal.R.drawable.ic_lock_bugreport, R.string.bugreport_title);
        }

        @Override
        public void onPress() {
            // don't actually trigger the bugreport if we are running stability
            // tests via monkey
            if (ActivityManager.isUserAMonkey()) {
                return;
            }
            // Add a little delay before executing, to give the
            // dialog a chance to go away before it takes a
            // screenshot.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Take an "interactive" bugreport.
                        MetricsLogger.action(mContext,
                                MetricsEvent.ACTION_BUGREPORT_FROM_POWER_MENU_INTERACTIVE);
                        ActivityManagerNative.getDefault().requestBugReport(
                                ActivityManager.BUGREPORT_OPTION_INTERACTIVE);
                    } catch (RemoteException e) {
                    }
                }
            }, 500);
        }

        @Override
        public boolean onLongPress() {
            // don't actually trigger the bugreport if we are running stability
            // tests via monkey
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                // Take a "full" bugreport.
                MetricsLogger.action(mContext, MetricsEvent.ACTION_BUGREPORT_FROM_POWER_MENU_FULL);
                ActivityManagerNative.getDefault().requestBugReport(
                        ActivityManager.BUGREPORT_OPTION_FULL);
            } catch (RemoteException e) {
            }
            return false;
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        @Override
        public boolean showBeforeProvisioning() {
            return false;
        }

        @Override
        public String getStatus() {
            return mContext.getString(
                    com.android.internal.R.string.bugreport_status,
                    Build.VERSION.RELEASE,
                    Build.ID);
        }
    }

    private Action getSettingsAction() {
        return new SinglePressAction(com.android.internal.R.drawable.ic_settings,
                R.string.global_action_settings) {

            @Override
            public void onPress() {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }

            @Override
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override
            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getEmergencyAction() {
        return new SinglePressAction(com.android.internal.R.drawable.emergency_icon,
                R.string.global_action_emergency) {
            @Override
            public void onPress() {
                mEmergencyAffordanceManager.performEmergencyCall();
            }

            @Override
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override
            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(com.android.internal.R.drawable.ic_action_assist_focused,
                R.string.global_action_assist) {
            @Override
            public void onPress() {
                Intent intent = new Intent(Intent.ACTION_ASSIST);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }

            @Override
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override
            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(com.android.internal.R.drawable.ic_voice_search,
                R.string.global_action_voice_assist) {
            @Override
            public void onPress() {
                Intent intent = new Intent(Intent.ACTION_VOICE_ASSIST);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }

            @Override
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override
            public boolean showBeforeProvisioning() {
                return true;
            }
        };
    }

    private Action getLockdownAction() {
        return new SinglePressAction(com.android.internal.R.drawable.ic_lock_lock,
                R.string.global_action_lockdown) {

            @Override
            public void onPress() {
                new LockPatternUtils(mContext).requireCredentialEntry(UserHandle.USER_ALL);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error while trying to lock device.", e);
                }
            }

            @Override
            public boolean showDuringKeyguard() {
                return true;
            }

            @Override
            public boolean showBeforeProvisioning() {
                return false;
            }
        };
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException re) {
            return null;
        }
    }

    private boolean isCurrentUserOwner() {
        UserInfo currentUser = getCurrentUser();
        return currentUser == null || currentUser.isPrimary();
    }

    private void addUsersToMenu(ArrayList<Action> items) {
        UserManager um = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (final UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean isCurrentUser = currentUser == null
                            ? user.id == 0 : (currentUser.id == user.id);
                    Drawable icon = user.iconPath != null ? Drawable.createFromPath(user.iconPath)
                            : null;
                    SinglePressAction switchToUser = new SinglePressAction(
                            com.android.internal.R.drawable.ic_menu_cc, icon,
                            (user.name != null ? user.name : "Primary")
                            + (isCurrentUser ? " \u2714" : "")) {
                        public void onPress() {
                            try {
                                ActivityManagerNative.getDefault().switchUser(user.id);
                            } catch (RemoteException re) {
                                Log.e(TAG, "Couldn't switch user " + re);
                            }
                        }

                        public boolean showDuringKeyguard() {
                            return true;
                        }

                        public boolean showBeforeProvisioning() {
                            return false;
                        }
                    };
                    items.add(switchToUser);
                }
            }
        }
    }

    private void prepareDialog() {
        refreshSilentMode();
        mAirplaneModeOn.updateState(mAirplaneState);
        mAdapter.notifyDataSetChanged();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (mShowSilentToggle) {
            IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
            mContext.registerReceiver(mRingerModeReceiver, filter);
        }
    }

    private void refreshSilentMode() {
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
		if (SystemProperties.get("ro.prize_shut_menu").equals("1")) {
			final boolean silentModeOn =
                    mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
            ((ToggleAction)mSilentModeAction).updateState(
                    silentModeOn ? ToggleAction.State.On : ToggleAction.State.Off);
		}  else {
			if (!mHasVibrator) {
            	final boolean silentModeOn =
                    	mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
            	((ToggleAction)mSilentModeAction).updateState(
                    	silentModeOn ? ToggleAction.State.On : ToggleAction.State.Off);
        	}
		}
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
    }

	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
    private void refreshPowerExtendMode(){
        if(PrizeOption.PRIZE_POWER_EXTEND_MODE)
        {
            final boolean powerExtendModeOn = PowerManager.isSuperSaverMode();
            ((ToggleAction)mPowerExtendModeAction).updateState(powerExtendModeOn ? ToggleAction.State.On : ToggleAction.State.Off);
        }
    }
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
	
    /** {@inheritDoc} */
    public void onDismiss(DialogInterface dialog) {
        if (mShowSilentToggle) {
            try {
                mContext.unregisterReceiver(mRingerModeReceiver);
            } catch (IllegalArgumentException ie) {
                // ignore this
                Log.w(TAG, ie);
            }
        }
    }

    /** {@inheritDoc} */
    public void onClick(DialogInterface dialog, int which) {
        if (!(mAdapter.getItem(which) instanceof SilentModeTriStateAction)) {
            dialog.dismiss();
        }
        mAdapter.getItem(which).onPress();
    }

    /**
     * The adapter used for the list within the global actions dialog, taking
     * into account whether the keyguard is showing via
     * {@link GlobalActions#mKeyguardShowing} and whether the device is provisioned
     * via {@link GlobalActions#mDeviceProvisioned}.
     */
    private class MyAdapter extends BaseAdapter {

        public int getCount() {
            int count = 0;

            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);

                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                count++;
            }
            return count;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        public Action getItem(int position) {

            int filteredPos = 0;
            for (int i = 0; i < mItems.size(); i++) {
                final Action action = mItems.get(i);
                if (mKeyguardShowing && !action.showDuringKeyguard()) {
                    continue;
                }
                if (!mDeviceProvisioned && !action.showBeforeProvisioning()) {
                    continue;
                }
                if (filteredPos == position) {
                    return action;
                }
                filteredPos++;
            }

            throw new IllegalArgumentException("position " + position
                    + " out of range of showable actions"
                    + ", filtered count=" + getCount()
                    + ", keyguardshowing=" + mKeyguardShowing
                    + ", provisioned=" + mDeviceProvisioned);
        }


        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Action action = getItem(position);
            return action.create(mContext, convertView, parent, LayoutInflater.from(mContext));
        }
    }

    // note: the scheme below made more sense when we were planning on having
    // 8 different things in the global actions dialog.  seems overkill with
    // only 3 items now, but may as well keep this flexible approach so it will
    // be easy should someone decide at the last minute to include something
    // else, such as 'enable wifi', or 'enable bluetooth'

    /**
     * What each item in the global actions dialog must be able to support.
     */
    private interface Action {
        /**
         * @return Text that will be announced when dialog is created.  null
         *     for none.
         */
        CharSequence getLabelForAccessibility(Context context);

        View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater);

        void onPress();

        /**
         * @return whether this action should appear in the dialog when the keygaurd
         *    is showing.
         */
        boolean showDuringKeyguard();

        /**
         * @return whether this action should appear in the dialog before the
         *   device is provisioned.
         */
        boolean showBeforeProvisioning();

        boolean isEnabled();
    }

    /**
     * An action that also supports long press.
     */
    private interface LongPressAction extends Action {
        boolean onLongPress();
    }
	/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
	private class TitleAction implements Action {
		private final int mMessageResId;
		protected TitleAction(int messageResId) {
			mMessageResId = messageResId;
		}
		public boolean isEnabled() {
            return false;
        }
		public View create(
                Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(com.prize.internal.R.layout.global_actions_title, parent, false);
            TextView titleView = (TextView) v.findViewById(com.prize.internal.R.id.shut_menu_title);
            if(titleView!=null){
				titleView.setText(mMessageResId);}
            return v;
        }
		public CharSequence getLabelForAccessibility(Context context) {
			return context.getString(mMessageResId);
		}


        public void onPress() {
			
		}
		
		public String getStatus() {
            return null;
        }
   
        public boolean showDuringKeyguard() {
			return true;
		}

    
        public boolean showBeforeProvisioning() {
			return false;
		}

	}
	/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
 	/**
     * A single press action maintains no state, just responds to a press
     * and takes an action.
     */
    private static abstract class SinglePressAction implements Action {
        private final int mIconResId;
        private final Drawable mIcon;
        private final int mMessageResId;
        private final CharSequence mMessage;

        protected SinglePressAction(int iconResId, int messageResId) {
            mIconResId = iconResId;
            mMessageResId = messageResId;
            mMessage = null;
            mIcon = null;
        }

        protected SinglePressAction(int iconResId, Drawable icon, CharSequence message) {
            mIconResId = iconResId;
            mMessageResId = 0;
            mMessage = message;
            mIcon = icon;
        }

		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
		protected SinglePressAction(int iconResId, int messageResId, int iconResId2, int messageResId2) {
			if (SystemProperties.get("ro.prize_shut_menu").equals("1")) {
				mIconResId = iconResId2;
            	mMessageResId = messageResId2;
            	mMessage = null;
            	mIcon = null;
			} else {
				mIconResId = iconResId;
            	mMessageResId = messageResId;
            	mMessage = null;
            	mIcon = null;
			}
        }
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
        public boolean isEnabled() {
            return true;
        }

        public String getStatus() {
            return null;
        }

        abstract public void onPress();

        public CharSequence getLabelForAccessibility(Context context) {
            if (mMessage != null) {
                return mMessage;
            } else {
                return context.getString(mMessageResId);
            }
        }

        public View create(
                Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
			/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
			View v;
			ImageView icon;
			TextView statusView;
			TextView messageView;
			if (SystemProperties.get("ro.prize_shut_menu").equals("1")) {
				v = inflater.inflate(com.prize.internal.R.layout.global_actions_item_prize_xiaxuefeng, parent, false);
				icon = (ImageView) v.findViewById(com.prize.internal.R.id.shut_menu_icon);
            	messageView = (TextView) v.findViewById(com.prize.internal.R.id.shut_menu_message);
            	statusView = (TextView) v.findViewById(com.prize.internal.R.id.shut_menu_status);
			} else {
				v = inflater.inflate(R.layout.global_actions_item, parent, false);
				icon = (ImageView) v.findViewById(R.id.icon);
            	messageView = (TextView) v.findViewById(R.id.message);
            	statusView = (TextView) v.findViewById(R.id.status);
			}
            /* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
            final String status = getStatus();
            if (!TextUtils.isEmpty(status)) {
                statusView.setText(status);
            } else {
                statusView.setVisibility(View.GONE);
            }
            if (mIcon != null) {
                icon.setImageDrawable(mIcon);
                icon.setScaleType(ScaleType.CENTER_CROP);
            } else if (mIconResId != 0) {
                icon.setImageDrawable(context.getDrawable(mIconResId));
            }
            if (mMessage != null) {
                messageView.setText(mMessage);
            } else {
                messageView.setText(mMessageResId);
            }

            return v;
        }
    }

    /**
     * A toggle action knows whether it is on or off, and displays an icon
     * and status message accordingly.
     */
    private static abstract class ToggleAction implements Action {

        enum State {
            Off(false),
            TurningOn(true),
            TurningOff(true),
            On(false);

            private final boolean inTransition;

            State(boolean intermediate) {
                inTransition = intermediate;
            }

            public boolean inTransition() {
                return inTransition;
            }
        }

        protected State mState = State.Off;

        // prefs
        protected int mEnabledIconResId;
        protected int mDisabledIconResid;
        protected int mMessageResId;
        protected int mEnabledStatusMessageResId;
        protected int mDisabledStatusMessageResId;

        /**
         * @param enabledIconResId The icon for when this action is on.
         * @param disabledIconResid The icon for when this action is off.
         * @param essage The general information message, e.g 'Silent Mode'
         * @param enabledStatusMessageResId The on status message, e.g 'sound disabled'
         * @param disabledStatusMessageResId The off status message, e.g. 'sound enabled'
         */
        public ToggleAction(int enabledIconResId,
                int disabledIconResid,
                int message,
                int enabledStatusMessageResId,
                int disabledStatusMessageResId) {
            mEnabledIconResId = enabledIconResId;
            mDisabledIconResid = disabledIconResid;
            mMessageResId = message;
            mEnabledStatusMessageResId = enabledStatusMessageResId;
            mDisabledStatusMessageResId = disabledStatusMessageResId;
        }
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
		public ToggleAction(int enabledIconResId,
                int disabledIconResid,
				int enabledIconResId2,
				int disabledIconResid2,
                int message,
                int enabledStatusMessageResId,
                int disabledStatusMessageResId) {
			if (SystemProperties.get("ro.prize_shut_menu").equals("1")) {
				 mEnabledIconResId = enabledIconResId2;
            	 mDisabledIconResid = disabledIconResid2;
			} else {
				 mEnabledIconResId = enabledIconResId;
            	 mDisabledIconResid = disabledIconResid;
			}          
            mMessageResId = message;
            mEnabledStatusMessageResId = enabledStatusMessageResId;
            mDisabledStatusMessageResId = disabledStatusMessageResId;
        }
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
        /**
         * Override to make changes to resource IDs just before creating the
         * View.
         */
        void willCreate() {

        }

        @Override
        public CharSequence getLabelForAccessibility(Context context) {
            return context.getString(mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent,
                LayoutInflater inflater) {
            willCreate();

            /* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
			View v;
			ImageView icon;
			TextView statusView;
			TextView messageView;
			if (SystemProperties.get("ro.prize_shut_menu").equals("1")) {
				v = inflater.inflate(com.prize.internal.R.layout.global_actions_item_prize_xiaxuefeng, parent, false);
				icon = (ImageView) v.findViewById(com.prize.internal.R.id.shut_menu_icon);
            	messageView = (TextView) v.findViewById(com.prize.internal.R.id.shut_menu_message);
            	statusView = (TextView) v.findViewById(com.prize.internal.R.id.shut_menu_status);
			} else {
				v = inflater.inflate(R.layout.global_actions_item, parent, false);
				icon = (ImageView) v.findViewById(R.id.icon);
            	messageView = (TextView) v.findViewById(R.id.message);
            	statusView = (TextView) v.findViewById(R.id.status);
			}
            /* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */
            final boolean enabled = isEnabled();

            if (messageView != null) {
                messageView.setText(mMessageResId);
                messageView.setEnabled(enabled);
            }

            boolean on = ((mState == State.On) || (mState == State.TurningOn));
            if (icon != null) {
                icon.setImageDrawable(context.getDrawable(
                        (on ? mEnabledIconResId : mDisabledIconResid)));
                icon.setEnabled(enabled);
            }

            if (statusView != null) {
                statusView.setText(on ? mEnabledStatusMessageResId : mDisabledStatusMessageResId);
                statusView.setVisibility(View.VISIBLE);
                statusView.setEnabled(enabled);
            }
            v.setEnabled(enabled);

            return v;
        }

        public final void onPress() {
            if (mState.inTransition()) {
                Log.w(TAG, "shouldn't be able to toggle when in transition");
                return;
            }

            final boolean nowOn = !(mState == State.On);
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return !mState.inTransition();
        }

        /**
         * Implementations may override this if their state can be in on of the intermediate
         * states until some notification is received (e.g airplane mode is 'turning off' until
         * we know the wireless connections are back online
         * @param buttonOn Whether the button was turned on or off
         */
        protected void changeStateFromPress(boolean buttonOn) {
            mState = buttonOn ? State.On : State.Off;
        }

        abstract void onToggle(boolean on);

        public void updateState(State state) {
            mState = state;
        }
    }
	/*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-start*/
    private class PowerExtendModeToggleAction extends ToggleAction {
        public PowerExtendModeToggleAction() {
            super(com.prize.internal.R.drawable.ic_super_saver_on,
                com.prize.internal.R.drawable.ic_super_saver_quit,
                com.prize.internal.R.string.global_super_saver,
                com.prize.internal.R.string.global_action_super_saver_mode_on_status,
                com.prize.internal.R.string.global_action_super_saver_mode_off_status);
        }
        void onToggle(boolean on) {
            Log.d(TAG, "PowerExtendMode onToggle on=" + on);
            PowerManager pwManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
            Log.i(TAG,"PowerExtendMode PowerManager.isSuperSaverMode() is " + PowerManager.isSuperSaverMode());
            if (on) {
                // switch super saver mode.
                if (PowerManager.isSuperSaverMode()){
                    pwManager.switchSuperSaverMode(false);
                }else{
                    pwManager.switchSuperSaverMode(true);
                }
            }else{
                pwManager.switchSuperSaverMode(false);
            }
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }
    /*PRIZE-PowerExtendMode-wangxianzhen-2015-05-30-end*/
	/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
    private class SilentModeToggleAction extends ToggleAction {
        public SilentModeToggleAction() {
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-start */
				super(R.drawable.ic_audio_vol_mute,
					R.drawable.ic_audio_vol,
					com.prize.internal.R.drawable.global_action_vibrate_on,
                    com.prize.internal.R.drawable.global_action_vibrate_off,
                    com.android.internal.R.string.global_action_toggle_silent_mode,
                    com.android.internal.R.string.global_action_silent_mode_on_status,
                    com.android.internal.R.string.global_action_silent_mode_off_status);

				//super(R.drawable.ic_audio_vol_mute,
                //    R.drawable.ic_audio_vol,
                //    R.string.global_action_toggle_silent_mode,
                //    R.string.global_action_silent_mode_on_status,
                //    R.string.global_action_silent_mode_off_status);
		/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end*/
        }

        void onToggle(boolean on) {
            if (on) {
				if(SystemProperties.get("ro.prize_shut_menu").equals("1")) {
					if (!mHasVibrator) {
						mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
					} else {
						mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
					}					
				} else {
					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				}              
            } else {
                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }
	/* PRIZE-shutmenu-xiaxuefeng-2015-3-25-end */

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {

        private final int[] ITEM_IDS = { R.id.option1, R.id.option2, R.id.option3 };

        private final AudioManager mAudioManager;
        private final Handler mHandler;
        private final Context mContext;

        SilentModeTriStateAction(Context context, AudioManager audioManager, Handler handler) {
            mAudioManager = audioManager;
            mHandler = handler;
            mContext = context;
        }

        private int ringerModeToIndex(int ringerMode) {
            // They just happen to coincide
            return ringerMode;
        }

        private int indexToRingerMode(int index) {
            // They just happen to coincide
            return index;
        }

        @Override
        public CharSequence getLabelForAccessibility(Context context) {
            return null;
        }

        public View create(Context context, View convertView, ViewGroup parent,
                LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.global_actions_silent_mode, parent, false);

            int selectedIndex = ringerModeToIndex(mAudioManager.getRingerMode());
            for (int i = 0; i < 3; i++) {
                View itemView = v.findViewById(ITEM_IDS[i]);
                itemView.setSelected(selectedIndex == i);
                // Set up click handler
                itemView.setTag(i);
                itemView.setOnClickListener(this);
            }
            return v;
        }

        public void onPress() {
        }

        public boolean showDuringKeyguard() {
            return true;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean isEnabled() {
            return true;
        }

        void willCreate() {
        }

        public void onClick(View v) {
            if (!(v.getTag() instanceof Integer)) return;

            int index = (Integer) v.getTag();
            mAudioManager.setRingerMode(indexToRingerMode(index));
            mHandler.sendEmptyMessageDelayed(MESSAGE_DISMISS, DIALOG_DISMISS_DELAY);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    || Intent.ACTION_SCREEN_OFF.equals(action)) {
                /*prize-OS8.0_Shutdown-change-yangming-2017_12_23-start*/
                /*String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(reason)) {
                    mHandler.sendEmptyMessage(MESSAGE_DISMISS);
                }*/
                ShutdownDialog.setDialogDismiss();
                /*prize-OS8.0_Shutdown-change-yangming-2017_12_23-end*/
            } else if (TelephonyIntents.ACTION_EMERGENCY_CALLBACK_MODE_CHANGED.equals(action)) {
                // Airplane mode can be changed after ECM exits if airplane toggle button
                // is pressed during ECM mode
                if (!(intent.getBooleanExtra("PHONE_IN_ECM_STATE", false)) &&
                        mIsWaitingForEcmExit) {
                    mIsWaitingForEcmExit = false;
                    changeAirplaneModeSystemSetting(true);
                }
            }
 /*prize-OS8.0_Shutdown-change-yangming-2017_8_12-start*/
		else if(SHUTDOWN.equals(action)){
			mWindowManagerFuncs.shutdown( false );
		}else if(REBOOT.equals(action)){
			mWindowManagerFuncs.reboot( false );
		}
 /*prize-OS8.0_Shutdown-change-yangming-2017_8_12-end*/
        }
    };

    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
			/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
            //if (!mHasTelephony) return;
            /// M:[ALPS00109833] @{
            //final boolean inAirplaneMode;
            //inAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
            //Log.v(TAG, "Phone State = " + serviceState.getState()
            //        + " gemini = " + SystemProperties.get("ro.mtk_gemini_support").equals("1")
            //        + " inAirplaneMode " + inAirplaneMode);
            /// @}
            //mAirplaneState = inAirplaneMode ? ToggleAction.State.On : ToggleAction.State.Off;
            //mAirplaneModeOn.updateState(mAirplaneState);
            //mAdapter.notifyDataSetChanged();
			/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
        }
    };

    private BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
                mHandler.sendEmptyMessage(MESSAGE_REFRESH);
            }
        }
    };

    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onAirplaneModeChanged();
        }
    };

    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final int MESSAGE_SHOW = 2;
    private static final int DIALOG_DISMISS_DELAY = 300; // ms

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_DISMISS:
                if (mDialog != null) {
                    mDialog.dismiss();
                    mDialog = null;
                }
                break;
            case MESSAGE_REFRESH:
                refreshSilentMode();
                mAdapter.notifyDataSetChanged();
                break;
            case MESSAGE_SHOW:
                handleShow();
                break;
            }
        }
    };

    private void onAirplaneModeChanged() {
        // Let the service state callbacks handle the state.
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
        //if (mHasTelephony) return;
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
        boolean airplaneModeOn = Settings.Global.getInt(
                mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,
                0) == 1;
        mAirplaneState = airplaneModeOn ? ToggleAction.State.On : ToggleAction.State.Off;
        mAirplaneModeOn.updateState(mAirplaneState);
    }

    /**
     * Change the airplane mode system setting
     */
    private void changeAirplaneModeSystemSetting(boolean on) {
        Settings.Global.putInt(
                mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,
                on ? 1 : 0);
        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        intent.putExtra("state", on);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-start*/
        //if (!mHasTelephony) {
            mAirplaneState = on ? ToggleAction.State.On : ToggleAction.State.Off;
        //}
		/*PRIZE-airplane_mode_ecm_remove-xiaxuefeng-2015-6-15-end*/
    }

    private static final class GlobalActionsDialog extends Dialog implements DialogInterface {
        private final Context mContext;
        private final int mWindowTouchSlop;
        private final AlertController mAlert;
        private final MyAdapter mAdapter;

        private EnableAccessibilityController mEnableAccessibilityController;

        private boolean mIntercepted;
        private boolean mCancelOnUp;

        public GlobalActionsDialog(Context context, AlertParams params) {
            super(context, getDialogTheme(context));
            mContext = getContext();
            mAlert = AlertController.create(mContext, this, getWindow());
            mAdapter = (MyAdapter) params.mAdapter;
            mWindowTouchSlop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
            params.apply(mAlert);
        }

        private static int getDialogTheme(Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(com.android.internal.R.attr.alertDialogTheme,
                    outValue, true);
            return outValue.resourceId;
        }

        @Override
        protected void onStart() {
            // If global accessibility gesture can be performed, we will take care
            // of dismissing the dialog on touch outside. This is because the dialog
            // is dismissed on the first down while the global gesture is a long press
            // with two fingers anywhere on the screen.
            if (EnableAccessibilityController.canEnableAccessibilityViaGesture(mContext)) {
                mEnableAccessibilityController = new EnableAccessibilityController(mContext,
                        new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
                super.setCanceledOnTouchOutside(false);
            } else {
                mEnableAccessibilityController = null;
                super.setCanceledOnTouchOutside(true);
            }

            super.onStart();
        }

        @Override
        protected void onStop() {
            if (mEnableAccessibilityController != null) {
                mEnableAccessibilityController.onDestroy();
            }
            super.onStop();
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if (mEnableAccessibilityController != null) {
                final int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    View decor = getWindow().getDecorView();
                    final int eventX = (int) event.getX();
                    final int eventY = (int) event.getY();
                    if (eventX < -mWindowTouchSlop
                            || eventY < -mWindowTouchSlop
                            || eventX >= decor.getWidth() + mWindowTouchSlop
                            || eventY >= decor.getHeight() + mWindowTouchSlop) {
                        mCancelOnUp = true;
                    }
                }
                try {
                    if (!mIntercepted) {
                        mIntercepted = mEnableAccessibilityController.onInterceptTouchEvent(event);
                        if (mIntercepted) {
                            final long now = SystemClock.uptimeMillis();
                            event = MotionEvent.obtain(now, now,
                                    MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
                            event.setSource(InputDevice.SOURCE_TOUCHSCREEN);
                            mCancelOnUp = true;
                        }
                    } else {
                        return mEnableAccessibilityController.onTouchEvent(event);
                    }
                } finally {
                    if (action == MotionEvent.ACTION_UP) {
                        if (mCancelOnUp) {
                            cancel();
                        }
                        mCancelOnUp = false;
                        mIntercepted = false;
                    }
                }
            }
            return super.dispatchTouchEvent(event);
        }

        public ListView getListView() {
            return mAlert.getListView();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAlert.installContent();
        }

        @Override
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                for (int i = 0; i < mAdapter.getCount(); ++i) {
                    CharSequence label =
                            mAdapter.getItem(i).getLabelForAccessibility(getContext());
                    if (label != null) {
                        event.getText().add(label);
                    }
                }
            }
            return super.dispatchPopulateAccessibilityEvent(event);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (mAlert.onKeyDown(keyCode, event)) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (mAlert.onKeyUp(keyCode, event)) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }
 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-start*/
	private Bitmap screenShot(){

        Bitmap screenBitmap = null;
        WindowManager mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = mWindowManager.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        if(dims[0] > dims[1]){
            screenBitmap = SurfaceControl.screenshot((int) dims[1], (int) dims[0]);
            Matrix matrix = new Matrix();
            matrix.reset();
            int rotation = mDisplay.getRotation();
            if(rotation==3){
                matrix.setRotate(90);
            }else{
                matrix.setRotate(-90);
            }
            Bitmap bitmap = screenBitmap;
            screenBitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(), bitmap.getHeight(),matrix, true);
            bitmap.recycle();
            bitmap = null;
        }else{
            screenBitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
        }
        return screenBitmap;
	}
    private Bitmap blurScale(Bitmap bmp){
        float scaleFactor = SCALE_VAL;  //8
        float radius = 8; 
        Bitmap overlay = Bitmap.createBitmap(
                (int) (bmp.getWidth() / scaleFactor),
                (int) (bmp.getHeight() / scaleFactor),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(overlay);
        canvas.scale(1/ scaleFactor, 1/ scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bmp, 0, 0, paint);
        overlay = doBlur(overlay, (int) radius, true);
		Log.i("dialog", "blurScale = " + overlay);
        return  overlay;
    }
    public static Bitmap doBlur(Bitmap sentBitmap, int radius,  
            boolean canReuseInBitmap) {  
        Bitmap bitmap;
        if(canReuseInBitmap){
            bitmap = sentBitmap;
        }else{
            bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
        }
        if(radius < 1){
            return  (null);
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;
        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];
        
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++){
            dv[i] = (i/ divsum);
        }
        
        yw = yi = 0;
        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;
        for (y = 0; y < h; y++) {  
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i<= radius; i++){
                p = pix[yi + Math.min(wm, Math.max(i, 0))];  
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if(i > 0){
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                }else{
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }

            stackpointer = radius;

            for (x = 0; x < w; x++){
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                
                if(y == 0){
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                
                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }

        for(x = 0; x < w; x++){
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++){
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }

            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16)
                        | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                
                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];
                
                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
               stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		Log.i("dialog", "doBlur = " + bitmap);
        return (bitmap);
    }	
    private static Bitmap big(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(8f, 8f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
 /*prize-OS8.0_Shutdown-add-yangming-2017_8_12-end*/
}
