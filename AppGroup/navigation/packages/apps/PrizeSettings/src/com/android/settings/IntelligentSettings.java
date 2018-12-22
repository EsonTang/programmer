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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import com.android.settings.R;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.logging.MetricsLogger;

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

public class IntelligentSettings extends SettingsPreferenceFragment implements
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
	
	private PreferenceCategory mSystemsCategory;
	private Preference mRedPcketHelperPref; // red packet
	private Preference mSleepGesturePref; // 黑屏手势
	private CheckBoxPreference mFlipSilentPref; // 翻转静音
	private CheckBoxPreference mSmartDialingPref;// 智能拨打
	private CheckBoxPreference mSmartAnswerCallPref;// 智能接听
	private CheckBoxPreference mSlideScreenshotPref;// 三指截屏
	private CheckBoxPreference mPocketModePref;// 口袋模式
    private CheckBoxPreference mAntifakeTouchPref;// 防误触
	private CheckBoxPreference mDbclickSleepPref;// sleep
    private CheckBoxPreference mOpenTorchPref;// torch
    private Preference mNonTouchOperationPref;//隔空操作
	/*PRIZE-OneHandMode-liyu-2016-01-04-start*/
	private Preference mOneHandModePref;//单手模式
	/*PRIZE-OneHandMode-liyu-2016-01-04-end*/

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.intelligent_settings);
		Log.v(TAG, "*******IntelligentSettings********");
		initializeAllPreferences();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	
	protected int getMetricsCategory() {
        return MetricsLogger.PRIVACY;
    }
	/**
	 * @Todo init-IntelligentSettings-UI
	 * @author zhongweilin
	 */
	private void initializeAllPreferences() {
		mSystemsCategory = (PreferenceCategory) findPreference(SYSTEM_CATEGORY);
		
		mRedPcketHelperPref = (Preference) findPreference(KEY_RED_PACKET_HELPER);
		if(!PrizeOption.PRIZE_LUCKY_MONEY_HELPER){
			getPreferenceScreen().removePreference(mRedPcketHelperPref);
		}
		
		if (!PrizeOption.PRIZE_SLEEP_GESTURE) {
			mSleepGesturePref = (Preference) findPreference(KEY_SLEEP_GESTURE);
			getPreferenceScreen().removePreference(mSleepGesturePref);
		}
		
		// flipSilent
		mFlipSilentPref = (CheckBoxPreference) findPreference(KEY_FLIP_SILENT);
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
		mSmartDialingPref = (CheckBoxPreference) findPreference(KEY_SMART_DIALING);
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
		mSmartAnswerCallPref = (CheckBoxPreference) findPreference(KEY_SMART_ANSWER_CALL);
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
		mSlideScreenshotPref = (CheckBoxPreference) findPreference(KEY_SLIDE_SCREENSHOT);
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

		// PocketMode
		mPocketModePref = (CheckBoxPreference) findPreference(KEY_POCKET_MODE);
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
        mAntifakeTouchPref = (CheckBoxPreference) findPreference(KEY_ANTIFAKE_TOUCH);
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
		if(!PrizeOption.PRIZE_ONE_HAND_MODE){
			getPreferenceScreen().removePreference(mOneHandModePref);
		}
        /*PRIZE-OneHandMode-liyu-2016-01-04-end*/
        //Non-touch Operation
		mNonTouchOperationPref = (Preference) findPreference(KEY_NON_TOUCH_OPERATION);
        if (!PrizeOption.PRIZE_NON_TOUCH_OPERATION) {
            getPreferenceScreen().removePreference(mNonTouchOperationPref);
        }

        //add for nav bar. prize-linkh-20150724       
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        boolean showNav = false;
        try {
            showNav = windowManagerService.hasNavigationBar();
        } catch(Exception e) {
        }
        
        boolean supportNavbarStyle = getActivity().getResources().getBoolean(
            com.prize.internal.R.bool.support_navbar_style);        
        if(showNav) {
            if(!PrizeOption.PRIZE_DYNAMICALLY_HIDE_NAVBAR && !supportNavbarStyle) {
                Preference pref = (Preference) findPreference("navigation_bar");
                if(pref != null) {
                    getPreferenceScreen().removePreference(pref);
                }
            }
        } else {
            Preference pref = (Preference) findPreference("navigation_bar");
            if(pref != null) {
                getPreferenceScreen().removePreference(pref);
            }            
        }
		// double click sleep
		mDbclickSleepPref = (CheckBoxPreference) findPreference(KEY_DOUBLE_CLICK_SLEEP);
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
		mOpenTorchPref = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_OPEN_TORCH);
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

		return true;
	}

	/**
	 * @Todo onclick-PreferenceClick
	 * @author zhongweilin
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		Log.v(TAG, "*********onPreferenceClick********");
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
        };
	/// add new menu to search db liup 20160622 end
}
