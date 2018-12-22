/*******************************************
 * 版权所有©2015,深圳市铂睿智恒科技有限公司
 * 
 * 内容摘要：处理设置中的智能辅助菜单
 * 当前版本：V1.0
 * 作    者：黄典俊
 * 完成日期：2015-03-21
 *
 * 修改记录
 * 修改日期：2015-03-25
 * 版 本 号：V1.0
 * 修 改 人：钟卫林
 * 修改内容：新增智能辅助菜单项
 *********************************************/
package com.android.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.content.ComponentName;

import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import android.provider.Settings;
import com.android.settings.R;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.mediatek.common.prizeoption.PrizeOption;

/// add new menu to search db liup 20160622 start
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import java.util.List;
import android.content.Context;
import java.util.ArrayList;
import android.provider.SearchIndexableResource;
/// add new menu to search db liup 20160622 end
//-prize-add-yangming-2106-8-12-start-/
import android.os.UserHandle;
import android.util.Log;
//-prize-add-yangming-2106-8-12-end-/
/*-prize-add-Messenger-yangming-2016_12_22-start-*/
import android.content.ServiceConnection; 
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
/*-prize-add-Messenger-yangming-2016_12_22-end-*/
public class PrizeIntelligentSettings extends SettingsPreferenceFragment implements
		Preference.OnPreferenceChangeListener, OnPreferenceClickListener ,Indexable{///add Indexable liup 20160622
	private static final String TAG = "prize";
	private static final String KEY_RED_PACKET_HELPER = "red_packet_helper";
	private static final String KEY_SLEEP_GESTURE = "sleep_gesture";
	private static final String KEY_FLIP_SILENT = "flip_silent_preference";
	private static final String KEY_SMART_DIALING = "smart_dialing_preference";
	private static final String KEY_SMART_ANSWER_CALL = "smart_answer_call_preference";
	private static final String KEY_SLIDE_SCREENSHOT = "slide_screenshot_preference";
	private static final String KEY_POCKET_MODE = "pocket_mode_preference";
    private static final String KEY_ANTIFAKE_TOUCH = "antifake_touch_preference";
    private static final String KEY_NON_TOUCH_OPERATION = "non_touch_operation";
	private static final String KEY_DOUBLE_CLICK_SLEEP = "dblclick_sleep_preference";
    private static final String KEY_LOCKSCREEN_OPEN_TORCH = "lockscreen_open_torch_preference";
	/*PRIZE-OneHandMode-liyu-2016-01-04-start*/
	private static final String KEY_ONE_HAND_MODE = "one_hand_mode";
	/*PRIZE-OneHandMode-liyu-2016-01-04-end*/

	private static final String SYSTEM_CATEGORY = "system_category";
	private static final String PERSONALISE_CATEGORY = "personalise_category";
	//-prize-add-yangming-2106-8-12-start-/		
	private static final String PRIZE_FLOAT_WINDOW =  "prize_float_window_preference";
	//-prize-add-yangming-2106-8-12-end-/
    // PRIZE BEGIN
    // ID : PRIZE_BARRAGE_WINDOW
    // DESCRIPTION : Add barrage preference
    // AUTHOR : yueliu
    private static final String PRIZE_BARRAGE_WINDOW =  "prize_barrage_window_preference";
	private SwitchPreference mPrizeBarrageWindow;
    // PRIZE END
	private static final String PRIZE_NEW_FLOAT_WINDOW =  "prize_new_float_window_preference";
	private static final String KEY_APP_INST_SETTINGS =  "app_inst_settings";
	private static final String KEY_SLIDE_SPLIT_SCREEN = "prize_slide_split_screen__preference";
	private Preference mAppInstSettingsPref;
	
	private PreferenceCategory mSystemsCategory;
	private PreferenceCategory mPersonaliseCategory;
	private Preference mRedPcketHelperPref; // red packet
	private Preference mSleepGesturePref; // 黑屏手势
	private SwitchPreference mFlipSilentPref; // 翻转静音
	private SwitchPreference mSmartDialingPref;// 智能拨打
	private SwitchPreference mSmartAnswerCallPref;// 智能接听
	private SwitchPreference mSlideScreenshotPref;// 三指截屏
	private SwitchPreference mPocketModePref;// 口袋模式
    private SwitchPreference mAntifakeTouchPref;// 防误触
	private SwitchPreference mDbclickSleepPref;// sleep
    private SwitchPreference mOpenTorchPref;// torch
    private Preference mNonTouchOperationPref;//隔空操作
	/*PRIZE-OneHandMode-liyu-2016-01-04-start*/
	private Preference mOneHandModePref;//单手模式
	/*PRIZE-OneHandMode-liyu-2016-01-04-end*/
	//-prize-add-yangming-2106-8-12-start-/
	private SwitchPreference mPrizeFloatWindow;
	//-prize-add-yangming-2106-8-12-end-/
	private Preference mNewPrizeFloatWindow;//
	private SwitchPreference mPrizeSliptScreen;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.intelligent_settings);
		Log.v(TAG, "*******PrizeIntelligentSettings********");
		Log.e("test", "onstart");
		initializeAllPreferences();
		
		Log.d("ClassName", "ClassName = "+getActivity().getComponentName().getClassName());
	}

	@Override
	public void onResume() {
		super.onResume();
		//-prize-add-yangming-2106-8-12-start-/
		final boolean bPrizeFloatWindow = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_FLOAT_WINDOW,0) == 1;
		mPrizeFloatWindow.setChecked(bPrizeFloatWindow);
		//-prize-add-yangming-2106-8-12-end-/	
        // PRIZE BEGIN
        // ID : PRIZE_BARRAGE_WINDOW
        // DESCRIPTION : Add barrage preference
        // AUTHOR : yueliu
		final boolean prizeBarrageWindow = Settings.System.getInt(getContentResolver(),Settings.System.PRIZE_BARRAGE_WINDOW,0) == 1;
		mPrizeBarrageWindow.setChecked(prizeBarrageWindow);
        // PRIZE END
		
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected int getMetricsCategory() {
        return MetricsEvent.PRIVACY;
    }
	/**
	 * @Todo init-PrizeIntelligentSettings-UI
	 * @author zhongweilin
	 */
	private void initializeAllPreferences() {
		mSystemsCategory = (PreferenceCategory) findPreference(SYSTEM_CATEGORY);
		mPersonaliseCategory = (PreferenceCategory) findPreference(PERSONALISE_CATEGORY);
		
		mAppInstSettingsPref=(Preference) findPreference(KEY_APP_INST_SETTINGS);
		if(!PrizeOption.PRIZE_APP_MULTI_INSTANCES){
			mPersonaliseCategory.removePreference(mAppInstSettingsPref);
		}
		
		mRedPcketHelperPref = (Preference) findPreference(KEY_RED_PACKET_HELPER);

		mPersonaliseCategory.removePreference(mRedPcketHelperPref);

		
		if (!PrizeOption.PRIZE_SLEEP_GESTURE) {
			mSleepGesturePref = (Preference) findPreference(KEY_SLEEP_GESTURE);
			getPreferenceScreen().removePreference(mSleepGesturePref);
		}
		
		// flipSilent
		mFlipSilentPref = (SwitchPreference) findPreference(KEY_FLIP_SILENT);
		int flipSilent = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_FLIP_SILENT, 0);
		Log.v(TAG, "******flipSilent = " + flipSilent + "********");
		if (flipSilent == 1) {
			mFlipSilentPref.setChecked(true);
		} else {
			mFlipSilentPref.setChecked(false);
		}
		mFlipSilentPref.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_FLIP_SILENT) {
			getPreferenceScreen().removePreference(mFlipSilentPref);
		}
		// smart dialing
		mSmartDialingPref = (SwitchPreference) findPreference(KEY_SMART_DIALING);
		int smartDialing = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_SMART_DIALING, 0);
		if (smartDialing == 1) {
			mSmartDialingPref.setChecked(true);
		} else {
			mSmartDialingPref.setChecked(false);
		}
		mSmartDialingPref.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_SMART_DIALING) {
			getPreferenceScreen().removePreference(mSmartDialingPref);
		}

		// smart answer call
		mSmartAnswerCallPref = (SwitchPreference) findPreference(KEY_SMART_ANSWER_CALL);
		int smartAnswerCall = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_SMART_ANSWER_CALL, 0);
		if (smartAnswerCall == 1) {
			mSmartAnswerCallPref.setChecked(true);
		} else {
			mSmartAnswerCallPref.setChecked(false);
		}
		mSmartAnswerCallPref.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_SMART_ANSWER_CALL) {
			getPreferenceScreen().removePreference(mSmartAnswerCallPref);
		}

		// slide screenshot
		mSlideScreenshotPref = (SwitchPreference) findPreference(KEY_SLIDE_SCREENSHOT);
		int slideScreenshot = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_SLIDE_SCREENSHOT, 0);
		if (slideScreenshot == 1) {
			mSlideScreenshotPref.setChecked(true);
		} else {
			mSlideScreenshotPref.setChecked(false);
		}
		mSlideScreenshotPref.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_SLIDE_SCREENSHOT) {
			getPreferenceScreen().removePreference(mSlideScreenshotPref);
		}
		// split screen
		mPrizeSliptScreen = (SwitchPreference) findPreference(KEY_SLIDE_SPLIT_SCREEN);
		int prizeSplitScreen = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_SLIDE_SPLIT_SCREEN, 0);
		if (prizeSplitScreen == 1) {
			mPrizeSliptScreen.setChecked(true);
		} else {
			mPrizeSliptScreen.setChecked(false);
		}
		mPrizeSliptScreen.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_SPLIT_SCREEN) {
			getPreferenceScreen().removePreference(mPrizeSliptScreen);
		}
		// PocketMode
		mPocketModePref = (SwitchPreference) findPreference(KEY_POCKET_MODE);
		int pocketMode = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_POCKET_MODE, 0);
		if (pocketMode == 1) {
			mPocketModePref.setChecked(true);
		} else {
			mPocketModePref.setChecked(false);
		}
		mPocketModePref.setOnPreferenceChangeListener(this);
		if (!PrizeOption.PRIZE_POCKET_MODE) {
			getPreferenceScreen().removePreference(mPocketModePref);
		}
        
        //AntifakeTouch
        mAntifakeTouchPref = (SwitchPreference) findPreference(KEY_ANTIFAKE_TOUCH);
        int antifakeTouch = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_ANTIFAKE_TOUCH, 0);
        if (antifakeTouch == 1) {
            mAntifakeTouchPref.setChecked(true);
        } else {
            mAntifakeTouchPref.setChecked(false);
        }
        mAntifakeTouchPref.setOnPreferenceChangeListener(this);
        if (!PrizeOption.PRIZE_ANTIFAKE_TOUCH) {
            getPreferenceScreen().removePreference(mAntifakeTouchPref);
        }
		
		/*PRIZE-OneHandMode-liyu-2016-01-04-start*/
		mOneHandModePref = (Preference) findPreference(KEY_ONE_HAND_MODE);
		getPreferenceScreen().removePreference(mOneHandModePref);

        //Non-touch Operation
		mNonTouchOperationPref = (Preference) findPreference(KEY_NON_TOUCH_OPERATION);
        if (!PrizeOption.PRIZE_NON_TOUCH_OPERATION) {
            getPreferenceScreen().removePreference(mNonTouchOperationPref);
        }

        /* Reposition nav bar back key feature & Dynamically hiding nav bar feature &. 
         * Nav bar related to mBack key feature &  Dynamically changing Recents function feature.
         *   prize-linkh-20161115 */
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        boolean showNav = false;
        try {
            showNav = windowManagerService.hasNavigationBar();
        } catch(Exception e) {
        }

        if(showNav) {
            if(!PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR && !PrizeOption.PRIZE_REPOSITION_BACK_KEY 
                && !PrizeOption.PRIZE_TREAT_RECENTS_AS_MENU
                && !PrizeOption.PRIZE_NAVBAR_COLOR_CUST
                && !PrizeOption.PRIZE_SUPPORT_NAV_BAR_FOR_MBACK_DEVICE) {
                Preference pref = (Preference) findPreference("navigation_bar");
                if(pref != null) {
                    //getPreferenceScreen().removePreference(pref);
                    mPersonaliseCategory.removePreference(pref);
                }
            }
        } else {
            Preference pref = (Preference) findPreference("navigation_bar");
            if(pref != null) {
                //getPreferenceScreen().removePreference(pref);
                mPersonaliseCategory.removePreference(pref);
            }            
        } //END....

		// double click sleep
		mDbclickSleepPref = (SwitchPreference) findPreference(KEY_DOUBLE_CLICK_SLEEP);
		int dbclicksleep = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_DBLCLICK_SLEEP, 0);
		if (dbclicksleep == 1) {
			mDbclickSleepPref.setChecked(true);
		} else {
			mDbclickSleepPref.setChecked(false);
		}
		mDbclickSleepPref.setOnPreferenceChangeListener(this);
		
		 if(showNav) {
            getPreferenceScreen().removePreference(mDbclickSleepPref);
        }
		// open torch
		mOpenTorchPref = (SwitchPreference) findPreference(KEY_LOCKSCREEN_OPEN_TORCH);
		int lockscreenopentorch = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_LOCKSCREEN_OPEN_TORCH, 0);
		if (lockscreenopentorch == 1) {
			mOpenTorchPref.setChecked(true);
		} else {
			mOpenTorchPref.setChecked(false);
		}
		mOpenTorchPref.setOnPreferenceChangeListener(this);
		 if(showNav) {
            getPreferenceScreen().removePreference(mOpenTorchPref);
        }
		//-prize-add-yangming-2106-8-12-start-/
		mPrizeFloatWindow = (SwitchPreference) findPreference(PRIZE_FLOAT_WINDOW);
		int lockPrizeFloatWindow = Settings.System.getInt(getContentResolver(),
				Settings.System.PRIZE_FLOAT_WINDOW, 0);
		if (lockPrizeFloatWindow == 1) {
			mPrizeFloatWindow.setChecked(true);
		} else {
			mPrizeFloatWindow.setChecked(false);
		}
		mPrizeFloatWindow.setOnPreferenceChangeListener(this);
		if(!PrizeOption.PRIZE_FLOAT_WINDOW){
		    //getPreferenceScreen().removePreference(mPrizeFloatWindow);
			mPersonaliseCategory.removePreference(mPrizeFloatWindow);
	    }
		Log.e("test", "initializeAllPreferences");
		//-prize-add-yangming-2106-8-12-end-/
        // PRIZE BEGIN
        // ID : PRIZE_BARRAGE_WINDOW
        // DESCRIPTION : Add barrage preference
        // AUTHOR : yueliu
        mPrizeBarrageWindow = (SwitchPreference) findPreference(PRIZE_BARRAGE_WINDOW);
        int lockPrizeBarrageWindow = Settings.System.getInt(getContentResolver(),
                Settings.System.PRIZE_BARRAGE_WINDOW, 0);
        if (lockPrizeBarrageWindow == 1) {
            mPrizeBarrageWindow.setChecked(true);
        } else {
            mPrizeBarrageWindow.setChecked(false);
        }
        mPrizeBarrageWindow.setOnPreferenceChangeListener(this);
        if(!PrizeOption.PRIZE_BARRAGE_WINDOW){
            mPersonaliseCategory.removePreference(mPrizeBarrageWindow);
        }
        Log.e("barrage", "[PrizeIntelligentSettings] initializeAllPreferences");
        // PRIZE END
		mNewPrizeFloatWindow = (Preference) findPreference(PRIZE_NEW_FLOAT_WINDOW);
		mNewPrizeFloatWindow.setOnPreferenceClickListener(this);
		if(!PrizeOption.PRIZE_NEW_FLOAT_WINDOW){
			mPersonaliseCategory.removePreference(mNewPrizeFloatWindow);
	    }
        //end......
    }

	/**
	 * @Todo onclick-PreferenceChange
	 * @author zhongweilin
	 */
	@Override
	public boolean onPreferenceChange(Preference preference, Object objValue) {
		Log.v(TAG, "*********onPreferenceChange********");
		final String key = preference.getKey();
		// flipSilent
		if (preference == mFlipSilentPref) {
			boolean flipSilentValue = (Boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_FLIP_SILENT, flipSilentValue ? 1 : 0);
			mFlipSilentPref.setChecked(flipSilentValue);
		}

		// smart dialing
		if (preference == mSmartDialingPref) {
			boolean smartDialingValue = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_SMART_DIALING, smartDialingValue ? 1
							: 0);
			mSmartDialingPref.setChecked(smartDialingValue);
		}

		// smart answer call
		if (preference == mSmartAnswerCallPref) {
			boolean smartAnswerCallValue = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_SMART_ANSWER_CALL,
					smartAnswerCallValue ? 1 : 0);
			mSmartAnswerCallPref.setChecked(smartAnswerCallValue);
		}

		// slide screenshot
		if (preference == mSlideScreenshotPref) {
			boolean slideScreenshotValue = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_SLIDE_SCREENSHOT,
					slideScreenshotValue ? 1 : 0);
			mSlideScreenshotPref.setChecked(slideScreenshotValue);
		}
		// split screen
		if (preference == mPrizeSliptScreen) {
			boolean isChecked = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_SLIDE_SPLIT_SCREEN,
					isChecked ? 1 : 0);
			mPrizeSliptScreen.setChecked(isChecked);
		}
		// dbclicksleep
		if (preference == mDbclickSleepPref) {
			boolean dbclicksleepValue = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_DBLCLICK_SLEEP,
					dbclicksleepValue ? 1 : 0);
			mDbclickSleepPref.setChecked(dbclicksleepValue);
		}
		// lockscreen torch
		if (preference == mOpenTorchPref) {
			boolean opentorchValue = (boolean) objValue;
			Settings.System.putInt(getContentResolver(),
					Settings.System.PRIZE_LOCKSCREEN_OPEN_TORCH,
					opentorchValue ? 1 : 0);
			mOpenTorchPref.setChecked(opentorchValue);
		}
        // PocketMode
        if (preference == mPocketModePref) {
            boolean pocketModeValue = (boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_POCKET_MODE,
                    pocketModeValue ? 1 : 0);
            mPocketModePref.setChecked(pocketModeValue);
        }
        //AntifakeTouch
        if (preference == mAntifakeTouchPref) {
            boolean antifakeTouchValue = (boolean) objValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIZE_ANTIFAKE_TOUCH,
                    antifakeTouchValue ? 1 : 0);
            mAntifakeTouchPref.setChecked(antifakeTouchValue);  
        }
		//-prize-add-yangming-2106-8-12-start-ADD/
		if ( preference == mPrizeFloatWindow ) {
			boolean isFloatWindow = (boolean) objValue;
			mPrizeFloatWindow.setChecked(isFloatWindow);  
			handlePrizeFloatWindowPreferenceClick();
			Log.e("test", "click");
        } 
		//-prize-add-yangming-2106-8-12-end-/
        // PRIZE BEGIN
        // ID : PRIZE_BARRAGE_WINDOW
        // DESCRIPTION : Add barrage preference
        // AUTHOR : yueliu
        if ( preference == mPrizeBarrageWindow ) {
            boolean isBarrageWindow = (boolean) objValue;
            mPrizeBarrageWindow.setChecked(isBarrageWindow);
            handlePrizeBarrageWindowPreferenceClick();
            Log.e("barrage", "[PrizeIntelligentSettings] click PrizeBarrageWindow, isBarrageWindow is " + isBarrageWindow);
        }
        // PRIZE END
		return true;
	}

	/**
	 * @Todo onclick-PreferenceClick
	 * @author zhongweilin
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.v(TAG, "*********onPreferenceClick********");
		if ( preference == mNewPrizeFloatWindow ) {
			Intent intent = new Intent();
       	    ComponentName comp = new ComponentName("com.android.prizefloatwindow", "com.android.prizefloatwindow.LauncherActivity");
        	intent.setComponent(comp);
        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	getActivity().startActivity(intent);
        } 
		return true;
	}
	/// add new menu to search db liup 20160622 start
	public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {

			@Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
                ArrayList<SearchIndexableResource> indexables = new ArrayList<SearchIndexableResource>();

                SearchIndexableResource indexable = new SearchIndexableResource(context);
                indexable.xmlResId = R.xml.intelligent_settings;
                indexables.add(indexable);

                return indexables;
            }
			
			 @Override
            public List<String> getNonIndexableKeys(Context context) {
                final List<String> keys = new ArrayList<String>();

				keys.add(KEY_RED_PACKET_HELPER);

				if (!PrizeOption.PRIZE_SLEEP_GESTURE) {
					keys.add(KEY_SLEEP_GESTURE);
				}
				if (!PrizeOption.PRIZE_SMART_DIALING) {
					keys.add(KEY_SMART_DIALING);
				}
				if (!PrizeOption.PRIZE_SMART_ANSWER_CALL) {
					keys.add(KEY_SMART_ANSWER_CALL);
				}
				keys.add(KEY_APP_INST_SETTINGS);
				keys.add(KEY_LOCKSCREEN_OPEN_TORCH);
				keys.add(KEY_DOUBLE_CLICK_SLEEP);
                return keys;
            }
        };
	// add new menu to search db liup 20160622 end
    /*-prize-add-yangming-2016-8-12-start-*/   
	private void handlePrizeFloatWindowPreferenceClick() {
		Settings.System.putInt(getContentResolver(),Settings.System.PRIZE_FLOAT_WINDOW,mPrizeFloatWindow.isChecked() ? 1 : 0);
		/*-- prize-change-Messenger-yangming-2016_12_22-start*/
		/*String ACTION_FLOATWINDOW = "android.intent.action.PRIZE_FLOAT_WINDOW";
		Intent iFloatWindow = new Intent(ACTION_FLOATWINDOW);
		getActivity().sendBroadcastAsUser(iFloatWindow,UserHandle.ALL);*/ 
		
		Intent intent = new Intent();
		intent.setAction("com.android.floatwindow.IMyFloatWindowService");
		intent.setPackage("com.android.floatwindow");
		getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	//static final int MSG_GET_isWindowShowing = 1;
	static final int MSG_GET_startFloatWindowService = 2;
	static final int MSG_GET_stopFloatWindowService = 3;
    Messenger mService = null;
    //boolean mBound;
	private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            Log.i("test", "settings mService = " + mService + "");
            //mBound = true;
            boolean isShow = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_FLOAT_WINDOW, 0) == 1;
            if (isShow){
    			Message msg = Message.obtain(null,MSG_GET_startFloatWindowService,0,0);
    			try {
    				mService.send(msg);
    				getActivity().unbindService(mConnection);
    			} catch (Exception e) {
    				Log.i("test", "start send error! " + e + "");
    			}
    		}else{
    			Message msg = Message.obtain(null,MSG_GET_stopFloatWindowService,0,0);
    			try {
    				mService.send(msg);
    				getActivity().unbindService(mConnection);
    			} catch (Exception e) {
    				Log.i("test", "stop send error! " + e + "");
    			}
    		}
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            //mBound = false;
            Log.i("test", "onServiceDisconnected");
        }
    };
	    /*-prize-change-Messenger-yangming-2016_12_22-end-*/
	/*-prize-add-yangming-2016-8-12-end-*/
    // PRIZE BEGIN
    // ID : PRIZE_BARRAGE_WINDOW
    // DESCRIPTION : Add barrage preference
    // AUTHOR : yueliu
	private void handlePrizeBarrageWindowPreferenceClick(){
        Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_BARRAGE_WINDOW, mPrizeBarrageWindow.isChecked() ? 1 : 0);

        Intent intent = new Intent();
        intent.setAction("android.intent.action.PRIZE_BARRAGE_WINDOW");
        intent.setPackage("com.prize.barragewindow");
        getActivity().sendBroadcast(intent);
    }
	// PRIZE END
}
