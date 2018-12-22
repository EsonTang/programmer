package com.android.settings.fuelgauge;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;

import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
import com.android.settings.HelpUtils;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.powermaster.CPUModeDetailsActivity;
import com.mediatek.settings.fuelgauge.PowerUsageExts;

import java.util.List;

public class PowerRank extends PreferenceFragment {

	private static final boolean DEBUG = false;

	static final String TAG = "PowerRank";

	private static final String KEY_APP_LIST = "app_list";

	private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";

	private UserManager mUm;

	private BatteryHistoryPreference mHistPref;
	private PreferenceGroup mAppListGroup;
	private String mBatteryLevel;
	private String mBatteryStatus;
	
	private TextView mBackView;

	private TextView mLeftTitle;

	private TextView mMidTitle;

	private TextView mOperationButton;

	private ActionBar mActionBar;

	private RelativeLayout mActionBarView;

	private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

	private static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
	private static final int MAX_ITEMS_TO_LIST = 10;
	private static final int MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP = 10;
	private static final int SECONDS_IN_HOUR = 60 * 60;

	private BatteryStatsHelper mStatsHelper;
	
	private RelativeLayout mBackClickRl;
	private RelativeLayout mOperationClickRl;

	private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "");
			String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)
					&& updateBatteryStatus(intent)) {
				if (!mHandler.hasMessages(MSG_REFRESH_STATS)) {
					mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
				}
			}
		}
	};
	
	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_click_rl:
				mBackView.setPressed(true);
				getActivity().finish();
				break;

			case R.id.operation_click_rl:
				mOperationButton.setPressed(true);
				mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
				break;
			}
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
		mStatsHelper = new BatteryStatsHelper(activity, true);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setActionBarView();
	}
	
	private void setActionBarView(){
		mActionBar = getActivity().getActionBar();
		mActionBar.setElevation(0);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.power_master_action_bar_layout);
		mActionBarView = (RelativeLayout) mActionBar.getCustomView();
		
		mBackClickRl = (RelativeLayout) mActionBarView.findViewById(R.id.back_click_rl);
		mOperationClickRl = (RelativeLayout) mActionBarView.findViewById(R.id.operation_click_rl);
		
		mBackView = (TextView) mActionBarView.findViewById(R.id.back_button);
		mLeftTitle = (TextView) mActionBarView.findViewById(R.id.left_title);
		mMidTitle = (TextView) mActionBarView.findViewById(R.id.mid_title);
		mOperationButton = (TextView) mActionBarView.findViewById(R.id.operation_button);
		
		mBackClickRl.setOnClickListener(mClickListener);
		mOperationClickRl.setOnClickListener(mClickListener);
		
		mMidTitle.setVisibility(View.GONE);
		
		mLeftTitle.setText(getResources().getString(R.string.power_rank_title));
		mOperationButton.setText(getResources().getString(R.string.refresh_title));
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mStatsHelper.create(icicle);
	}
	
	@Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
		addPreferencesFromResource(R.xml.power_master_usage_summary);
		mAppListGroup = (PreferenceGroup) findPreference(KEY_APP_LIST);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		mStatsHelper.clearStats();
	}

	@Override
	public void onResume() {
		super.onResume();
		BatteryStatsHelper.dropFile(getActivity(), BATTERY_HISTORY_FILE);
		updateBatteryStatus(getActivity().registerReceiver(mBatteryInfoReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
		if (mHandler.hasMessages(MSG_REFRESH_STATS)) {
			mHandler.removeMessages(MSG_REFRESH_STATS);
			mStatsHelper.clearStats();
		}
		refreshStats();
	}

	@Override
	public void onPause() {
		BatteryEntry.stopRequestQueue();
		mHandler.removeMessages(BatteryEntry.MSG_UPDATE_NAME_ICON);
		getActivity().unregisterReceiver(mBatteryInfoReceiver);
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		// Log.d(TAG, "onStop() time = "+ mStatsHelper.getBatteryTimeRemaining());
		mHandler.removeMessages(MSG_REFRESH_STATS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Log.d(TAG, "onDestroy() time = "+ mStatsHelper.getBatteryTimeRemaining());
		if (getActivity().isChangingConfigurations()) {
			mStatsHelper.storeState();
			BatteryEntry.clearUidCache();
		}
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if (preference instanceof BatteryHistoryPreference) {
			mStatsHelper.storeStatsHistoryInFile(BATTERY_HISTORY_FILE);
			Bundle args = new Bundle();
			args.putString(BatteryHistoryDetail.EXTRA_STATS, BATTERY_HISTORY_FILE);
			args.putParcelable(BatteryHistoryDetail.EXTRA_BROADCAST,
					mStatsHelper.getBatteryBroadcast());
			SettingsActivity sa = (SettingsActivity) getActivity();
			sa.startPreferencePanel(BatteryHistoryDetail.class.getName(), args,
					R.string.history_details_title, null, null, 0);
			return super.onPreferenceTreeClick(preference);
		}


		if (!(preference instanceof PowerGaugePreference)) {
			return false;
		}
		PowerGaugePreference pgp = (PowerGaugePreference) preference;
		BatteryEntry entry = pgp.getInfo();
		PowerMasterUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(), mStatsHelper,
				mStatsType, entry, true);
		return super.onPreferenceTreeClick(preference);
	}

	private boolean updateBatteryStatus(Intent intent) {
		if (intent != null) {
			String batteryLevel = com.android.settings.Utils.getBatteryPercentage(intent);
			String batteryStatus = com.android.settings.Utils.getBatteryStatus(getResources(),
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
		final PowerProfile powerProfile = mStatsHelper.getPowerProfile();
		final BatteryStats stats = mStatsHelper.getStats();
		final double averagePower = powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		if (averagePower >= MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP) {
			final List<UserHandle> profiles = mUm.getUserProfiles();

			mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);

			final List<BatterySipper> usageList = mStatsHelper.getUsageList();

			final int dischargeAmount = stats != null ? stats.getDischargeAmount(mStatsType) : 0;
			final int numSippers = usageList.size();
			mAppListGroup.removeAll();
			for (int i = 0; i < numSippers; i++) {
				final BatterySipper sipper = usageList.get(i);
				if ((sipper.totalPowerMah * SECONDS_IN_HOUR) < MIN_POWER_THRESHOLD_MILLI_AMP) {
					continue;
				}
				final double percentOfTotal =
						((sipper.totalPowerMah / mStatsHelper.getTotalPower()) * dischargeAmount);
				if (((int) (percentOfTotal + .5)) < 1) {
					continue;
				}
				if (sipper.drainType == BatterySipper.DrainType.OVERCOUNTED) {
					// Don't show over-counted unless it is at least 2/3 the size of
					// the largest real entry, and its percent of total is more significant
					if (sipper.totalPowerMah < ((mStatsHelper.getMaxRealPower()*2)/3)) {
						continue;
					}
					if (percentOfTotal < 10) {
						continue;
					}
					if ("user".equals(Build.TYPE)) {
						continue;
					}
				}
				if (sipper.drainType == BatterySipper.DrainType.UNACCOUNTED) {
					// Don't show over-counted unless it is at least 1/2 the size of
					// the largest real entry, and its percent of total is more significant
					if (sipper.totalPowerMah < (mStatsHelper.getMaxRealPower()/2)) {
						continue;
					}
					if (percentOfTotal < 5) {
						continue;
					}
					if ("user".equals(Build.TYPE)) {
						continue;
					}
				}
				final UserHandle userHandle = new UserHandle(UserHandle.getUserId(sipper.getUid()));
				final BatteryEntry entry = new BatteryEntry(getActivity(), mHandler, mUm, sipper);
				final Drawable badgedIcon = mUm.getBadgedIconForUser(entry.getIcon(),
						userHandle);
				final CharSequence contentDescription = mUm.getBadgedLabelForUser(entry.getLabel(),
						userHandle);
				final PowerGaugePreference pref = new PowerGaugePreference(getActivity(),
						badgedIcon, contentDescription, entry);

				final double percentOfMax = (sipper.totalPowerMah * 100) / mStatsHelper.getMaxPower();
				sipper.percent = percentOfTotal;
				pref.setTitle(entry.getLabel());
				pref.setOrder(i + 1);
				pref.setPercent(percentOfMax, percentOfTotal);
				if (sipper.uidObj != null) {
					pref.setKey(Integer.toString(sipper.uidObj.getUid()));
				}
				mAppListGroup.addPreference(pref);
				if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST + 1)) {
					break;
				}
			}
		}

		BatteryEntry.startRequestQueue();
	}

	static final int MSG_REFRESH_STATS = 100;

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BatteryEntry.MSG_UPDATE_NAME_ICON:
				BatteryEntry entry = (BatteryEntry) msg.obj;
				PowerGaugePreference pgp =
						(PowerGaugePreference) findPreference(
								Integer.toString(entry.sipper.uidObj.getUid()));
				if (pgp != null) {
					final int userId = UserHandle.getUserId(entry.sipper.getUid());
					final UserHandle userHandle = new UserHandle(userId);
					/* prize-modify-by-lijimeng-for bugid 52874-20180319-start*/
					//pgp.setIcon(mUm.getBadgedIconForUser(entry.getIcon(), userHandle));
					/* prize-modify-by-lijimeng-for bugid 52874-20180319-end*/
					pgp.setTitle(entry.name);
				}
				break;
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
}
