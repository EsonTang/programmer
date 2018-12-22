package com.android.settings.powermaster;

import java.util.List;

import com.android.settings.Utils;
import com.android.settings.fuelgauge.BatteryEntry;
import com.android.settings.widget.ToggleSwitch;
import com.android.settings.widget.ToggleSwitch.OnBeforeCheckedChangeListener;

import android.os.BatteryStats;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
import android.content.DialogInterface;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.settings.R;

import android.os.SystemProperties;

import com.mediatek.common.prizeoption.PrizeOption;

import android.widget.ImageButton;
public class PowerMasterMainFragment extends Fragment implements OnPreferenceChangeListener {

	private static final boolean DEBUG = false;

	static final String TAG = "PowerMasterMainFragment";

	public static final String KEY_PERFORMANCE_AND_POWER = "performance_and_power";
	private static final String PROPERTY_POWER_MODE= "persist.sys.power.mode";
	private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";

	private static final String BLOCK_WAKE_UP = "prize_intercept_wakeup_alarm_state";

	private UserManager mUm;

	private Activity mActivity;

	private String mBatteryLevel;
	private String mBatteryStatus;

	private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

	private static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
	private static final int MAX_ITEMS_TO_LIST = 10;
	private static final int MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP = 10;
	private static final int SECONDS_IN_HOUR = 60 * 60;

	private BatteryStatsHelper mStatsHelper;

	private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)
					&& updateBatteryStatus(intent)) {
				mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
			}
		}
	};

	private View mFragmentView;

	private ScrollView mScrollView;

	private TextView mBatteryPercentage;

	private TextView mBatteryRemainTime;

	private LinearLayout mFastFunctionLl;

	private LinearLayout mNormalModeLl;

	private LinearLayout mNoopsycheSaveLl;

	private LinearLayout mSuperPowerSaveLl;

	private LinearLayout mPowerRankDetailLl;

	private PrizeDynamicWaveView mWaveView;

	private LinearLayout mFastFunctionTitleLl;

	private TextView mFastFunctionTitle;

	private TextView mFastFunctionSub;

	private CheckBox mFastFunctionCheckBox;

	private LinearLayout mNormalModeTitleLl;

	private TextView mNormalModeSub;

	private TextView mNormalModeTitle;

	private CheckBox mNormalModeCheckBox;

	private LinearLayout mNoopsycheSaveTitleLl;

	private TextView mNoopsycheSaveTitle;

	private TextView mNoopsycheSaveSub;

	private CheckBox mNoopsycheSaveBox;

	private LinearLayout mSuperPowerSaveTitleLl;

	private TextView mSuperPowerSaveTitle;

	private TextView mSuperPowerSaveSub;

	private CheckBox mSuperPowerSaveBox;

	private TextView mPowerRankDetailTitle;

	private LinearLayout mBlockThirdAppWakeUpLl;
	
	private LinearLayout mBlockThirdAppLl;

	private LinearLayout mBlockThirdAppWakeUpTitleLl;

	private TextView mBlockThirdAppWakeUpTitle;

	private TextView mBlockThirdAppWakeUpSub;

	private ToggleSwitch mBlockThirdAppWakeUpSwitchBar;

	private int mPowerModelType;

	private PowerManager mPm;

	private Intent mBatteryBroadcast;

	private int mBatteryPercent;

	private BatteryStats mStats;

	private ContentResolver mContentResolver;

	private Dialog mDialog;
	
	private boolean mIsBlockThirdAppWakeUp = true;

	private OnCheckedChangeListener mCheckListener = new CompoundButton.OnCheckedChangeListener(){ 
		@Override 
		public void onCheckedChanged(CompoundButton buttonView, 
				boolean isChecked) { 
			Log.d(TAG, "onCheckedChanged()   isChecked = "+isChecked);
			final ViewParent mButtonView = buttonView.getParent();
			
			if(((View)mButtonView).getId() == R.id.block_third_app_wake_up_ll){
				if(mIsBlockThirdAppWakeUp && !isChecked){
					mDialog = DialogUtils.getDialog(mActivity, mActivity.getResources().getString(R.string.notice), 
							mActivity.getResources().getString(R.string.power_close_block_third_app_wake_up_notice), 
							new OnClickListener() {
						@Override
						public void onClick(View v) {
							mBlockThirdAppWakeUpSwitchBar.setChecked(true);
							mDialog.dismiss();
						}
					}, 
							new OnClickListener() {
						@Override
						public void onClick(View v) {
							/* prize -modify-bugid 33108 Dialog can not dismiss-lijimeng-20170426-start*/
							mIsBlockThirdAppWakeUp = false;
							mBlockThirdAppWakeUpSwitchBar.setChecked(false);
							//mIsBlockThirdAppWakeUp = false;
							/* prize -modify-bugid 33108 Dialog can not dismiss-lijimeng-20170426-end*/
							Settings.System.putInt(mContentResolver, BLOCK_WAKE_UP,0);
							mDialog.dismiss();
						}
					});
					mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							boolean isOpenedBolockWakeUp = Settings.System.getInt(mContentResolver, BLOCK_WAKE_UP, 1) == 1;
							boolean isChecked = mBlockThirdAppWakeUpSwitchBar.isChecked();
							if(isOpenedBolockWakeUp != isChecked){
								mBlockThirdAppWakeUpSwitchBar.setChecked(isOpenedBolockWakeUp);
							}
						}
					});
				}else if((!mIsBlockThirdAppWakeUp && isChecked)){
					mBlockThirdAppWakeUpSwitchBar.setChecked(true);
					mIsBlockThirdAppWakeUp = true;
					Settings.System.putInt(mContentResolver, BLOCK_WAKE_UP,1);
				}
				return;
			}
			final boolean mIsChecked = isChecked;
			if(mIsChecked){ 
				switch (((View)mButtonView).getId()) {
				case R.id.fast_function_ll:
					mPowerModelType = 0;
					break;
				case R.id.normal_mode_ll:
					mPowerModelType = 1;
					break;
				case R.id.noopsyche_save_ll:
					mPowerModelType = 2;
					break;
				}

				setCheckBoxStatus();
				final Context mContext = getActivity();
				boolean mIsSameMode = isSameMode(mPowerModelType);
				if(!mIsSameMode){
					new Thread(){
						public void run() {
							switch (((View)mButtonView).getId()) {
							case R.id.fast_function_ll:
								Log.d(TAG, "onCheckedChanged()   Check Fast Function");
								CPUModeUtils.setCpuModeType(mContext, 0);

								//mPm.powerHint(PowerManager.POWER_HINT_PERFORMANCE_BOOST, 0);
								SystemProperties.set(PROPERTY_POWER_MODE, String.valueOf(0));

								mPowerModelType = 0;
								break;

							case R.id.normal_mode_ll:
								Log.d(TAG, "onCheckedChanged()   Check Normal Mode");
								CPUModeUtils.setCpuModeType(mContext, 1);

								//mPm.powerHint(PowerManager.POWER_HINT_BALANCE, 0);
								SystemProperties.set(PROPERTY_POWER_MODE, String.valueOf(1));

								mPowerModelType = 1;
								break;
							case R.id.noopsyche_save_ll:
								Log.d(TAG, "onCheckedChanged()   Check Noopsyche Save");
								CPUModeUtils.setCpuModeType(mContext, 2);
								CPUModeUtils.setNoopsycheSaveMode(mContext);

								//mPm.powerHint(PowerManager.POWER_HINT_BALANCE, 0);
								SystemProperties.set(PROPERTY_POWER_MODE, String.valueOf(1));

								mPowerModelType = 2;
								break;
							}
							if(mPowerModelType ==0 || mPowerModelType == 1){
								CPUModeUtils.resumeOrignalState(mContext);
							}
							mHandler.sendEmptyMessage(MSG_REFRESH_STATS);
						};
					}.start();
				}
			}
		} 
	};

	private OnClickListener mTitleListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getActivity(),CPUModeDetailsActivity.class);
			switch (((View)v.getParent()).getId()) {
			case R.id.fast_function_ll:
				mFastFunctionLl.setPressed(true);
				mPowerModelType = 0;
				break;
			case R.id.normal_mode_ll:
				mNormalModeLl.setPressed(true);
				mPowerModelType = 1;
				break;
			case R.id.noopsyche_save_ll:
				mPowerModelType = 2;
				mNoopsycheSaveLl.setPressed(true);
				break;
			default:
				if(v.getId() == R.id.super_power_save_ll){
					intent = null;
					mSuperPowerSaveLl.setPressed(true);
					intoSuperPowerMode();
				} else if(v.getId() == R.id.power_rank_ll){
					mPowerRankDetailLl.setPressed(true);
					intent = new Intent();
//					SAVE_KEY_SHOW_HOME_AS_UP
					intent.putExtra(":settings:show_home_as_up", true);
					intent.setAction("android.intent.action.POWER_USAGE_SUMMARY");
//					intent.setAction("android.intent.action.POWER_RANK");
				}
				break;
			}
			if(intent == null){
				return;
			}
			try {
				intent.putExtra(PowerMasterUtil.CPU_MODE_TYPE, mPowerModelType);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, e.getMessage());
			}
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
		mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
		mStatsHelper = new BatteryStatsHelper(activity, true);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mStatsHelper.create(icicle);

		mPm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/* prize-modify-by-lijimeng-for liuhai -20180705-start*/
		if(PrizeOption.PRIZE_NOTCH_SCREEN){
			mFragmentView = inflater.inflate(R.layout.prize_power_master_main_fragment, null);
		}else{
			mFragmentView = inflater.inflate(R.layout.prize_power_master_main_fragment, null);
		}
		/* prize-modify-by-lijimeng-for liuhai -20180705-end*/
		initViews();
		return mFragmentView;
	}

	private void initViews(){
		mScrollView = (ScrollView) mFragmentView.findViewById(R.id.scroll_view);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
		//mBatteryPercentage = (TextView) mFragmentView.findViewById(R.id.power_master_battery_percentage);
		//mBatteryRemainTime = (TextView) mFragmentView.findViewById(R.id.power_master_battery_remain_time);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
		mFastFunctionLl = (LinearLayout) mFragmentView.findViewById(R.id.fast_function_ll);
		mNormalModeLl = (LinearLayout) mFragmentView.findViewById(R.id.normal_mode_ll);
		mNoopsycheSaveLl = (LinearLayout) mFragmentView.findViewById(R.id.noopsyche_save_ll);
		mSuperPowerSaveLl = (LinearLayout) mFragmentView.findViewById(R.id.super_power_save_ll);
		mPowerRankDetailLl = (LinearLayout) mFragmentView.findViewById(R.id.power_rank_ll);
		mBlockThirdAppWakeUpLl = (LinearLayout) mFragmentView.findViewById(R.id.block_third_app_wake_up_ll);

		mWaveView = (PrizeDynamicWaveView) mFragmentView.findViewById(R.id.wava_view);

		mFastFunctionTitleLl = (LinearLayout) mFastFunctionLl.findViewById(R.id.title_ll);
		mFastFunctionTitle = (TextView) mFastFunctionLl.findViewById(R.id.tv_power_master_operation_item_title);
		mFastFunctionSub = (TextView) mFastFunctionLl.findViewById(R.id.tv_power_master_operation_item_sub);
		mFastFunctionCheckBox = (CheckBox) mFastFunctionLl.findViewById(R.id.cb_power_master_operation_item_checkbox);

		mNormalModeTitleLl = (LinearLayout) mNormalModeLl.findViewById(R.id.title_ll);
		mNormalModeTitle = (TextView) mNormalModeLl.findViewById(R.id.tv_power_master_operation_item_title);
		mNormalModeSub = (TextView) mNormalModeLl.findViewById(R.id.tv_power_master_operation_item_sub);
		mNormalModeCheckBox = (CheckBox) mNormalModeLl.findViewById(R.id.cb_power_master_operation_item_checkbox);

		mNoopsycheSaveTitleLl = (LinearLayout) mNoopsycheSaveLl.findViewById(R.id.title_ll);
		mNoopsycheSaveTitle = (TextView) mNoopsycheSaveLl.findViewById(R.id.tv_power_master_operation_item_title);
		mNoopsycheSaveSub = (TextView) mNoopsycheSaveLl.findViewById(R.id.tv_power_master_operation_item_sub);
		mNoopsycheSaveBox = (CheckBox) mNoopsycheSaveLl.findViewById(R.id.cb_power_master_operation_item_checkbox);

		mSuperPowerSaveTitleLl = (LinearLayout) mSuperPowerSaveLl.findViewById(R.id.title_ll);
		mSuperPowerSaveTitle = (TextView) mSuperPowerSaveLl.findViewById(R.id.tv_power_master_operation_item_title);
		mSuperPowerSaveSub = (TextView) mSuperPowerSaveLl.findViewById(R.id.tv_power_master_operation_item_sub);
		mSuperPowerSaveBox = (CheckBox) mSuperPowerSaveLl.findViewById(R.id.cb_power_master_operation_item_checkbox);

		mPowerRankDetailTitle = (TextView) mPowerRankDetailLl.findViewById(R.id.power_master_operation_item_title);

		mBlockThirdAppLl = (LinearLayout) mBlockThirdAppWakeUpLl.findViewById(R.id.block_third_app_ll);
		mBlockThirdAppWakeUpTitleLl = (LinearLayout) mBlockThirdAppWakeUpLl.findViewById(R.id.title_ll);
		mBlockThirdAppWakeUpTitle = (TextView) mBlockThirdAppWakeUpLl.findViewById(R.id.tv_power_master_operation_item_title);
		mBlockThirdAppWakeUpSub = (TextView) mBlockThirdAppWakeUpLl.findViewById(R.id.tv_power_master_operation_item_sub);
		mBlockThirdAppWakeUpSwitchBar = (ToggleSwitch) mBlockThirdAppWakeUpLl.findViewById(R.id.cb_power_master_operation_item_checkbox);
		
		ImageButton mImageButton = (ImageButton)mFragmentView.findViewById(R.id.prize_back_arrow);
		mImageButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
		if(!PrizeOption.PRIZE_INTERCEPT_WAKEUP_ALARMS){
			mBlockThirdAppLl.setVisibility(View.GONE);
		}

		mFastFunctionTitle.setText(R.string.fast_function_title);
		mFastFunctionSub.setText(R.string.cpu_performance_optimization);

		mNormalModeTitle.setText(R.string.normal_mode_title);
		mNormalModeSub.setText(R.string.keep_daily_usage_status);

		mNoopsycheSaveTitle.setText(R.string.noopsych_save_title);
		mNoopsycheSaveSub.setText(R.string.adjustment_of_cpu_frequency_brightness);

		mSuperPowerSaveTitle.setText(R.string.super_power_save_title);
		mSuperPowerSaveSub.setText(R.string.only_enable_phone_msg_alarmclocks);

		mPowerRankDetailTitle.setText(R.string.power_rank_title);

		mBlockThirdAppWakeUpTitle.setText(R.string.power_block_third_app_wake_up);
		mBlockThirdAppWakeUpSub.setText(R.string.power_open_block_third_app_wake_up_details);

		mFastFunctionTitleLl.setOnClickListener(mTitleListener);
		mNormalModeTitleLl.setOnClickListener(mTitleListener);
		mNoopsycheSaveTitleLl.setOnClickListener(mTitleListener);
		mSuperPowerSaveLl.setOnClickListener(mTitleListener);
		mPowerRankDetailLl.setOnClickListener(mTitleListener);

		mFastFunctionCheckBox.setOnCheckedChangeListener(mCheckListener);
		mNormalModeCheckBox.setOnCheckedChangeListener(mCheckListener);
		mNoopsycheSaveBox.setOnCheckedChangeListener(mCheckListener);
		mBlockThirdAppWakeUpSwitchBar.setOnCheckedChangeListener(mCheckListener);
		mSuperPowerSaveBox.setClickable(false);
	}

	@Override
	public void onStart() {
		super.onStart();
		mContentResolver = getActivity().getContentResolver();
		mStatsHelper.clearStats();
	}

	@Override
	public void onResume() {
		super.onResume();

		String mode = SystemProperties.get(PROPERTY_POWER_MODE);
		if(mode == null || mode.length() == 0){
			mPowerModelType = 1;
			//mPm.powerHint(PowerManager.POWER_HINT_BALANCE, 0);
		}else {
			mPowerModelType = Integer.parseInt(mode);

			int mSaveModeType = CPUModeUtils.getCpuModeType(getActivity());
			if(mPowerModelType == 1 && mSaveModeType == 2){
				mPowerModelType = 2;
			}
		}

		initPowerMode(mPowerModelType);

		boolean isOpenedBolockWakeUp = Settings.System.getInt(mContentResolver, BLOCK_WAKE_UP, 1) == 1;
		mIsBlockThirdAppWakeUp = isOpenedBolockWakeUp;
		if(isOpenedBolockWakeUp && !mBlockThirdAppWakeUpSwitchBar.isChecked()){
			mBlockThirdAppWakeUpSwitchBar.setChecked(true);
			mIsBlockThirdAppWakeUp = true;
		} else if(!isOpenedBolockWakeUp && mBlockThirdAppWakeUpSwitchBar.isChecked()){
			mBlockThirdAppWakeUpSwitchBar.setChecked(false);
			mIsBlockThirdAppWakeUp = false;
		}

		CPUModeUtils.setCpuModeType(getActivity(), mPowerModelType);
		Log.d(TAG, "mPowerModelType = "+mPowerModelType);

		mScrollView.smoothScrollTo(0, 0);
		setCheckBoxStatus();
		BatteryStatsHelper.dropFile(getActivity(), BATTERY_HISTORY_FILE);
		updateBatteryStatus(getActivity().registerReceiver(mBatteryInfoReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
		if (mHandler.hasMessages(MSG_REFRESH_STATS)) {
			mHandler.removeMessages(MSG_REFRESH_STATS);
			mStatsHelper.clearStats();
		}
		refreshStats();
	}

	private void initPowerMode(int type){
		switch (type) {
		case 0:
			mFastFunctionCheckBox.setChecked(true);
			break;
		case 1:
			mNormalModeCheckBox.setChecked(true);
			break;
		case 2:
			mNoopsycheSaveBox.setChecked(true);
			break;

		default:
			mFastFunctionCheckBox.setChecked(true);
			break;
		}
	}

	@Override
	public void onPause() {
		BatteryEntry.stopRequestQueue();
		getActivity().unregisterReceiver(mBatteryInfoReceiver);
		if(mDialog != null){
			mDialog.dismiss();
		}
		super.onPause();
	}

	public void onWindowFocusChanged(boolean hasFocus){
		Log.d(TAG, "hasFocus = " +hasFocus);
		if(hasFocus){
			new Thread(){
				public void run() {
					CPUModeUtils.saveOrignalState(getActivity());
				};
			}.start();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeMessages(MSG_REFRESH_STATS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isChangingConfigurations()) {
			mStatsHelper.storeState();
			BatteryEntry.clearUidCache();
		}
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (KEY_PERFORMANCE_AND_POWER.equals(preference.getKey())) {
			Log.d(TAG, "onPreferenceChange KEY_PERFORMANCE_AND_POWER ");
			mPowerModelType = Integer.parseInt(newValue.toString());
			refreshStats();
			return true;
		}
		return false;
	}

	private boolean updateBatteryStatus(Intent intent) {
		if (intent != null) {
			String batteryLevel = com.android.settings.Utils.getBatteryPercentage(intent);
			String batteryStatus = com.android.settings.Utils.getBatteryStatus(getActivity().getResources(),
					intent);
			if (!batteryLevel.equals(mBatteryLevel) || !batteryStatus.equals(mBatteryStatus)) {
				mBatteryLevel = batteryLevel;
				mBatteryStatus = batteryStatus;
				return true;
			}
		}
		return false;
	}

	private void refreshStats() {
		mStats = BatteryStatsHelper.statsFromFile(getActivity(), BATTERY_HISTORY_FILE);
		final PowerProfile powerProfile = mStatsHelper.getPowerProfile();
		powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);

		final List<UserHandle> profiles = mUm.getUserProfiles();
		mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);

		final long elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;
		mStats.computeBatteryRealtime(elapsedRealtimeUs,BatteryStats.STATS_SINCE_CHARGED);

		mBatteryBroadcast = mStatsHelper.getBatteryBroadcast();
		mBatteryPercent = com.android.settings.Utils.getBatteryLevel(mBatteryBroadcast);
		//String batteryPercentString = Utils.formatPercentage(mBatteryPercent);

		String mChargeLabelString = null;
		int theStatus = mBatteryBroadcast.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
		Log.d("BatteryStatus", "Status = "+theStatus);
		if (theStatus == 0) {
			final long drainTime = mStats.computeBatteryTimeRemaining(elapsedRealtimeUs);
			if (drainTime > 0) {
				String timeString = Formatter.formatShortElapsedTime(getActivity(),drainTime / 1000);
				mChargeLabelString = getActivity().getResources().getString(R.string.power_remain_time, timeString);
			} else {
				mChargeLabelString = "";
			}
		} else {
			final long chargeTime = mStats.computeChargeTimeRemaining(elapsedRealtimeUs);
			final String statusLabel = com.android.settings.Utils.getBatteryStatus(getActivity().getResources(),
					mBatteryBroadcast);
			final int status = mBatteryBroadcast.getIntExtra(BatteryManager.EXTRA_STATUS,
					BatteryManager.BATTERY_STATUS_UNKNOWN);
			if (chargeTime > 0 && status != BatteryManager.BATTERY_STATUS_FULL) {
				String timeString = Formatter.formatShortElapsedTime(getActivity(),chargeTime / 1000);
				mChargeLabelString = getActivity().getResources().getString(R.string.power_master_charge_duration, timeString);
			} else {
				mChargeLabelString = getActivity().getResources().getString(R.string.power_master_charging, statusLabel);
			}
		}

		mWaveView.setYWave(mBatteryPercent);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
		//mBatteryPercentage.setText(String.valueOf(mBatteryPercent));
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
		//mBatteryRemainTime.setText(mChargeLabelString);

		BatteryEntry.startRequestQueue();
		BatteryStatsHelper.dropFile(getActivity(), BATTERY_HISTORY_FILE);
	}

	static final int MSG_REFRESH_STATS = 100;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BatteryEntry.MSG_REPORT_FULLY_DRAWN:
				Activity activity = getActivity();
				if (activity != null) {
					activity.reportFullyDrawn();
				}
				break;
			case MSG_REFRESH_STATS:
				mStatsHelper.clearStats();
				refreshStats();
			}
			super.handleMessage(msg);
		}
	};

	private boolean isSameMode(int mModeType){
		Log.d(TAG, "mModeType = "+mModeType);
		Log.d(TAG, "SaveModeType = "+CPUModeUtils.getModeType(getActivity()));
		return mModeType == CPUModeUtils.getModeType(getActivity());
	}

	private void intoSuperPowerMode(){
		((PowerManager) getActivity().getSystemService(Context.POWER_SERVICE)).switchSuperSaverMode(true);
	}

	private void setCheckBoxStatus(){
		switch (mPowerModelType) {
		case 0:
			mFastFunctionCheckBox.setClickable(false);

			mNormalModeCheckBox.setChecked(false);
			mNoopsycheSaveBox.setChecked(false);
			mSuperPowerSaveBox.setChecked(false);

			mNormalModeCheckBox.setClickable(true);
			mNoopsycheSaveBox.setClickable(true);
			break;
		case 1:
			mNormalModeCheckBox.setClickable(false);

			mFastFunctionCheckBox.setChecked(false);
			mNoopsycheSaveBox.setChecked(false);
			mSuperPowerSaveBox.setChecked(false);

			mFastFunctionCheckBox.setClickable(true);
			mNoopsycheSaveBox.setClickable(true);
			break;
		case 2:
			mNoopsycheSaveBox.setClickable(false);

			mFastFunctionCheckBox.setChecked(false);
			mNormalModeCheckBox.setChecked(false);
			mSuperPowerSaveBox.setChecked(false);

			mFastFunctionCheckBox.setClickable(true);
			mNormalModeCheckBox.setClickable(true);
			break;

		case 3:
			mSuperPowerSaveBox.setChecked(true);
			mSuperPowerSaveBox.setClickable(false);

			mFastFunctionCheckBox.setChecked(false);
			mNormalModeCheckBox.setChecked(false);
			mNoopsycheSaveBox.setChecked(false);

			mFastFunctionCheckBox.setClickable(true);
			mNormalModeCheckBox.setClickable(true);
			mNoopsycheSaveBox.setClickable(true);
			break;

		default:
			mFastFunctionCheckBox.setChecked(true);
			mFastFunctionCheckBox.setClickable(false);

			mNormalModeCheckBox.setChecked(false);
			mNoopsycheSaveBox.setChecked(false);
			mSuperPowerSaveBox.setChecked(false);

			mNormalModeCheckBox.setClickable(true);
			mNoopsycheSaveBox.setClickable(true);
			break;
		}
	}
	
}