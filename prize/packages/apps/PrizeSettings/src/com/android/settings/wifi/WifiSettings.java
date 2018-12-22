/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.LinkAddress;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemProperties;

import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.LinkifyUtils;
import com.android.settings.PrizeWifiPreference;
import com.android.settings.ProxySelector;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.location.ScanningSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.widget.SwitchBar;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPoint.AccessPointListener;
import com.android.settingslib.wifi.AccessPointPreference;
import com.android.settingslib.wifi.WifiStatusTracker;
import com.android.settingslib.wifi.WifiTracker;

import com.mediatek.settings.FeatureOption;
import com.mediatek.wifi.WifiSettingsExt;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;

import android.database.ContentObserver;
import android.os.Handler;
import android.widget.AdapterView;
import android.widget.ListView;
/**
 * Two types of UI are provided here.
 *
 * The first is for "usual Settings", appearing as any other Setup fragment.
 *
 * The second is for Setup Wizard, with a simplified interface that hides the action bar
 * and menus.
 */
public class WifiSettings extends RestrictedSettingsFragment
        implements Indexable, WifiTracker.WifiListener, AccessPointListener,
        WifiDialog.WifiDialogListener ,LongPressAccessPointPreference.SelectedAccessPoint,AdapterView.OnItemClickListener{

    private static final String TAG = "WifiSettings";

    /* package */ static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
    private static final int MENU_ID_SCAN = Menu.FIRST + 5;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
    private static final int MENU_ID_FORGET = Menu.FIRST + 7;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 8;
    private static final int MENU_ID_WRITE_NFC = Menu.FIRST + 9;
    private static final int MENU_ID_CONFIGURE = Menu.FIRST + 10;

    public static final int WIFI_DIALOG_ID = 1;
    /* package */ static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int WRITE_NFC_DIALOG_ID = 6;

    // Instance state keys
    private static final String SAVE_DIALOG_MODE = "dialog_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
    private static final String SAVED_WIFI_NFC_DIALOG_STATE = "wifi_nfc_dlg_state";

    protected WifiManager mWifiManager;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;

    private WifiEnabler mWifiEnabler;
    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;

    private WifiDialog mDialog;
    private WriteWifiConfigToNfcDialog mWifiToNfcDialog;

    private ProgressBar mProgressHeader;

    // this boolean extra specifies whether to disable the Next button when not connected. Used by
    // account creation outside of setup wizard.
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";
    // This string extra specifies a network to open the connect dialog on, so the user can enter
    // network credentials.  This is used by quick settings for secured networks.
    private static final String EXTRA_START_CONNECT_SSID = "wifi_start_connect_ssid";

    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;

    // Save the dialog details
    private int mDialogMode;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;
    private Bundle mWifiNfcDialogSavedState;

    private WifiTracker mWifiTracker;
    private String mOpenSsid;

    private HandlerThread mBgThread;

    private AccessPointPreference.UserBadgeCache mUserBadgeCache;
    private PrizeWifiPreference mAddPreference;
    private PrizeWifiPreference mAdvancedPreference;
    /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
    private PrizeWifiPreference mSmartWifiPreference;
    private boolean isSmartWifi;
    /* prize-add-by-lijimeng-for smart wifi-20180601-end*/
    private MenuItem mScanMenuItem;
	private SwitchBar mSwitchBar;
    /* End of "used in Wifi Setup context" */

    /// M: add no WAPI certification action  @{
    private final IntentFilter mFilter;
    private final BroadcastReceiver mReceiver;
    /// @}
    /// M: add mtk feature
    private WifiSettingsExt mWifiSettingsExt;
    private  StaticIpConfiguration staticIpConfiguration;
    private IpAssignment mIpAssignment = IpAssignment.UNASSIGNED;
    private ProxySettings mProxySettings = ProxySettings.UNASSIGNED;
    private ProxyInfo mHttpProxy = null;

    private String [] mProxyData;
    private String [] mIpSetting ;
	private List<Integer> mList = new ArrayList<Integer>();
	private PrizeAlertDialog mPrizeAlertDialog;
    public WifiSettings() {
        super(DISALLOW_CONFIG_WIFI);

        /// M: add no WAPI certification action  @{
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NO_CERTIFICATION_ACTION);
        mFilter.addAction(PrizeWifiDetailsPage.PRIZE_WIFI_DETAILS_PAGE);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        /// @}
    }

    /// M: show error message @{
    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "handleEvent(), action = " + action);
        if (WifiManager.NO_CERTIFICATION_ACTION.equals(action)) {
            String apSSID = "";
            if (mSelectedAccessPoint != null) {
                apSSID = "[" + mSelectedAccessPoint.getSsidStr() + "] ";
            }
            Log.i(TAG, "Receive  no certification broadcast for AP " + apSSID);
            String message = getResources().getString(R.string.wifi_no_cert_for_wapi) + apSSID;
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
		if (PrizeWifiDetailsPage.PRIZE_WIFI_DETAILS_PAGE.equals(action)) {
			if(mSelectedAccessPoint != null && mSelectedAccessPoint.isSaved()){
				WifiConfiguration config = mSelectedAccessPoint.getConfig();
				if(config != null && intent != null){
					setIpAndProxy(intent);
					config.setIpConfiguration(new IpConfiguration(mIpAssignment, mProxySettings, staticIpConfiguration, mHttpProxy));
					staticIpConfiguration = null;
					mWifiManager.save(config, mSaveListener);
				}
			}
		}
    }
    // @}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();
        if (activity != null) {
            mProgressHeader = (ProgressBar) setPinnedHeaderView(R.layout.wifi_progress_header);
        }
		mSwitchBar = (SwitchBar) view.findViewById(R.id.prize_switch_bar);
    }
	 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);
        
        RecyclerView listView = getListView();
		listView.setNestedScrollingEnabled(false);
		listView.setFocusable(false);
        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.wifi_settings);
        // Add by zhudaopeng at 2016-11-18 Start
        mAdvancedPreference = new PrizeWifiPreference(getContext());
        mAdvancedPreference.setTitle(R.string.wifi_menu_advanced);
        // Add by zhudaopeng at 2016-11-18 End
        /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
        mSmartWifiPreference = new PrizeWifiPreference(getContext());
        mSmartWifiPreference.setTitle(R.string.prize_smart_wifi_title);
        mSmartWifiPreference.setSummary(R.string.prize_smart_wifi_summary);
        isSmartWifi = SystemProperties.getInt("ro.prize_smart_wifi_switch",0) == 1;
         /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
        mAddPreference = new PrizeWifiPreference(getContext());
        mAddPreference.setIcon(R.drawable.ic_menu_add_inset);
        mAddPreference.setTitle(R.string.wifi_add_network);

        mUserBadgeCache = new AccessPointPreference.UserBadgeCache(getPackageManager());

        mBgThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBgThread.start();

        mWifiSettingsExt = new WifiSettingsExt(getActivity());
        mWifiSettingsExt.onCreate();
        mProxyData = getActivity().getResources().getStringArray(R.array.wifi_proxy_settings);
        mIpSetting = getActivity().getResources().getStringArray(R.array.wifi_ip_settings);
    }

    @Override
    public void onDestroy() {
        mBgThread.quit();
        mWifiSettingsExt.unregisterPriorityObserver(getContentResolver());
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mWifiTracker =
                new WifiTracker(getActivity(), this, mBgThread.getLooper(), true, true, false);
        mWifiManager = mWifiTracker.getManager();

        mConnectListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                                R.string.wifi_failed_connect_message,
                                                Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        mSaveListener = new WifiManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    /// M: update priority after modify AP config @{
                                    mWifiSettingsExt.updatePriority();
                                    /// @}
                                }
                                @Override
                                public void onFailure(int reason) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        Toast.makeText(activity,
                                            R.string.wifi_failed_save_message,
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };

        mForgetListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                       /// M: update priority after connnect AP @{
                                       mWifiSettingsExt.updatePriority();
                                       /// @}
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                               R.string.wifi_failed_forget_message,
                                               Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        if (savedInstanceState != null) {
            mDialogMode = savedInstanceState.getInt(SAVE_DIALOG_MODE);
            if (savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
                mAccessPointSavedState =
                    savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
            }

            if (savedInstanceState.containsKey(SAVED_WIFI_NFC_DIALOG_STATE)) {
                mWifiNfcDialogSavedState =
                    savedInstanceState.getBundle(SAVED_WIFI_NFC_DIALOG_STATE);
            }
        }

        // if we're supposed to enable/disable the Next button based on our current connection
        // state, start it off in the right state
        Intent intent = getActivity().getIntent();
        mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);

        if (mEnableNextOnConnection) {
            if (hasNextButton()) {
                final ConnectivityManager connectivity = (ConnectivityManager)
                        getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo info = connectivity.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI);
                    changeNextButtonState(info.isConnected());
                }
            }
        }

        registerForContextMenu(getListView());

        setHasOptionsMenu(true);

        ///M: add mtk feature
        mWifiSettingsExt.onActivityCreated(this, mWifiManager);

        if (intent.hasExtra(EXTRA_START_CONNECT_SSID)) {
            mOpenSsid = intent.getStringExtra(EXTRA_START_CONNECT_SSID);
            onAccessPointsChanged();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mWifiEnabler != null) {
            mWifiEnabler.teardownSwitchBar();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // On/off switch is hidden for Setup Wizard (returns null)
        mWifiEnabler = createWifiEnabler();
    }

    /**
     * @return new WifiEnabler or null (as overridden by WifiSettingsForSetupWizard)
     */
    /* package */ WifiEnabler createWifiEnabler() {
        final SettingsActivity activity = (SettingsActivity) getActivity();
        //return new WifiEnabler(activity, activity.getSwitchBar());
		if(mSwitchBar != null){
			 return new WifiEnabler(activity, mSwitchBar);
		}
        return null;
    }

    @Override
    public void onResume() {
        final Activity activity = getActivity();
        super.onResume();
        removePreference("dummy");
        if (mWifiEnabler != null) {
            mWifiEnabler.resume(activity);
        }

        mWifiTracker.startTracking();
        activity.invalidateOptionsMenu();

        /// M: register no WAPI certification action receiver
        activity.registerReceiver(mReceiver, mFilter);
        mWifiSettingsExt.onResume();
		// getActivity().getContentResolver().registerContentObserver(
                    // Settings.Global.getUriFor(Settings.Global.AIRPLANE_MODE_ON),
                    // false, mAirplaneMode);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }

        /// M: unregister no WAPI certification action receiver
        getActivity().unregisterReceiver(mReceiver);
		//getActivity().getContentResolver().unregisterContentObserver(mAirplaneMode);
        mWifiTracker.stopTracking();
		if(mPrizeAlertDialog != null){
			mPrizeAlertDialog.dismiss();
		}
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the user is not allowed to configure wifi, do not show the menu.
        if (isUiRestricted()) return;

        addOptionsMenuItems(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * @param menu
     */
    void addOptionsMenuItems(Menu menu) {
        final boolean wifiIsEnabled = mWifiTracker.isWifiEnabled();
        mScanMenuItem = menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.menu_stats_refresh);
        mScanMenuItem.setEnabled(wifiIsEnabled)
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        // Modify by zhudaopeng at 2016-11-18 Start
        // menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
        //         .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        // menu.add(Menu.NONE, MENU_ID_CONFIGURE, 0, R.string.wifi_menu_configure)
        //        .setIcon(R.drawable.ic_settings_24dp)
        //        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        // Modify by zhudaopeng at 2016-11-18 End
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.WIFI;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If the dialog is showing, save its state.
        if (mDialog != null && mDialog.isShowing()) {
            outState.putInt(SAVE_DIALOG_MODE, mDialogMode);
            if (mDlgAccessPoint != null) {
                mAccessPointSavedState = new Bundle();
                mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, mAccessPointSavedState);
            }
        }

        if (mWifiToNfcDialog != null && mWifiToNfcDialog.isShowing()) {
            Bundle savedState = new Bundle();
            mWifiToNfcDialog.saveState(savedState);
            outState.putBundle(SAVED_WIFI_NFC_DIALOG_STATE, savedState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If the user is not allowed to configure wifi, do not handle menu selections.
        if (isUiRestricted()) return false;

        switch (item.getItemId()) {
            case MENU_ID_WPS_PBC:
                showDialog(WPS_PBC_DIALOG_ID);
                return true;
                /*
            case MENU_ID_P2P:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            WifiP2pSettings.class.getCanonicalName(),
                            null,
                            R.string.wifi_p2p_settings_title, null,
                            this, 0);
                } else {
                    startFragment(this, WifiP2pSettings.class.getCanonicalName(),
                            R.string.wifi_p2p_settings_title, -1, null);
                }
                return true;
                */
            case MENU_ID_WPS_PIN:
                showDialog(WPS_PIN_DIALOG_ID);
                return true;
            case MENU_ID_SCAN:
                MetricsLogger.action(getActivity(), MetricsEvent.ACTION_WIFI_FORCE_SCAN);
                mWifiTracker.forceScan();
                return true;
            case MENU_ID_ADVANCED:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            AdvancedWifiSettings.class.getCanonicalName(), null,
                            R.string.wifi_advanced_titlebar, null, this, 0);
                } else {
                    startFragment(this, AdvancedWifiSettings.class.getCanonicalName(),
                            R.string.wifi_advanced_titlebar, -1 /* Do not request a results */,
                            null);
                }
                return true;
            case MENU_ID_CONFIGURE:
                if (getActivity() instanceof SettingsActivity) {
                    ((SettingsActivity) getActivity()).startPreferencePanel(
                            ConfigureWifiSettings.class.getCanonicalName(), null,
                            R.string.wifi_configure_titlebar, null, this, 0);
                } else {
                    startFragment(this, ConfigureWifiSettings.class.getCanonicalName(),
                            R.string.wifi_configure_titlebar, -1 /* Do not request a results */,
                            null);
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
            Preference preference = (Preference) view.getTag();
            if (preference instanceof LongPressAccessPointPreference) {
				final Activity mActivity = getActivity();
				// if(mActivity != null && mActivity instanceof SettingsActivity){
					// SettingsActivity mSettingsActivity = (SettingsActivity)mActivity;
					// mWifiTracker.stopTracking();
					// mSettingsActivity.prizeSetCurrentFragment(this);
				// }
				mList.clear();
                mSelectedAccessPoint =
                        ((LongPressAccessPointPreference) preference).getAccessPoint();
              //  menu.setHeaderTitle(mSelectedAccessPoint.getSsid());
                if (mSelectedAccessPoint.isConnectable()) {
                   // menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
				   mList.add(R.string.wifi_menu_connect);
                }

                WifiConfiguration config = mSelectedAccessPoint.getConfig();
                // Some configs are ineditable
                if (isEditabilityLockedDown(getActivity(), config)) {
					showPrizeDialog();
                    return;
                }

                if (mSelectedAccessPoint.isSaved() || mSelectedAccessPoint.isEphemeral()) {
                    // Allow forgetting a network if either the network is saved or ephemerally
                    // connected. (In the latter case, "forget" blacklists the network so it won't
                    // be used again, ephemerally).
                   // menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
					mList.add(R.string.wifi_menu_forget);
                }

                ///M: add mtk feature
                mWifiSettingsExt.onCreateContextMenu(menu, mSelectedAccessPoint.getDetailedState(),
                        mSelectedAccessPoint);

                if (mSelectedAccessPoint.isSaved()) {
                   // menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
				   mList.add(R.string.wifi_menu_modify);
                    // M: add NfcAdapter judge for ALPS02293624
                    try {
                        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
                        if (nfcAdapter != null && nfcAdapter.isEnabled() &&
                            nfcAdapter.getModeFlag(NfcAdapter.MODE_READER) == NfcAdapter.FLAG_ON &&
                            mSelectedAccessPoint.getSecurity() != AccessPoint.SECURITY_NONE) {
                            // Only allow writing of NFC tags for password-protected networks.
                            // menu.add(Menu.NONE, MENU_ID_WRITE_NFC,
                                // 0, R.string.wifi_menu_write_to_nfc);
								mList.add(R.string.wifi_menu_write_to_nfc);
                        }
                    } catch (UnsupportedOperationException ex) {
                        Log.d(TAG, "this device doesn't support NFC");
                    }
                }
				showPrizeDialog();
            }
    }

	@Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		 if (mSelectedAccessPoint == null) {
            return;
        }
        switch (mList.get(position)) {
            case R.string.wifi_menu_connect: {
                if (mSelectedAccessPoint.isSaved()) {
                    connect(mSelectedAccessPoint.getConfig());
                } else if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig());
                } else {
                    showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                }
				break;
            }
            case R.string.wifi_menu_forget: {
                forget();
				break;
            }
            case R.string.wifi_menu_modify: {
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_MODIFY);
				break;
            }
            case R.string.wifi_menu_write_to_nfc:
                showDialog(WRITE_NFC_DIALOG_ID);
				break;
        }
		if(mPrizeAlertDialog != null){
			mPrizeAlertDialog.dismiss();
		}
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                if (mSelectedAccessPoint.isSaved()) {
                    connect(mSelectedAccessPoint.getConfig());
                } else if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks */
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig());
                } else {
                    showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                forget();
                return true;
            }
            case MENU_ID_MODIFY: {
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_MODIFY);
                return true;
            }
            case MENU_ID_WRITE_NFC:
                showDialog(WRITE_NFC_DIALOG_ID);
                return true;

        }
        ///M: add mtk feature
        if (mWifiSettingsExt != null && mSelectedAccessPoint != null) {
            return mWifiSettingsExt.onContextItemSelected(item, mSelectedAccessPoint.getConfig());
        }
        return super.onContextItemSelected(item);
    }
	
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof LongPressAccessPointPreference) {
            mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            if (mSelectedAccessPoint == null) {
                return false;
            }
            /** Bypass dialog for unsecured, unsaved, and inactive networks */
            if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE &&
                    !mSelectedAccessPoint.isSaved() && !mSelectedAccessPoint.isActive()) {
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig());
            } else if (mSelectedAccessPoint.isSaved()){
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_VIEW);
            } else {
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
            }
        } else if (preference == mAddPreference) {
            onAddNetworkPressed();
        // Add by zhudaopeng at 2016-11-18 Start
        }  else if (preference == mAdvancedPreference) {
        	((SettingsActivity) getActivity()).startPreferencePanel(
                    ConfigureWifiSettings.class.getCanonicalName(), null,
                    R.string.wifi_configure_titlebar, null, this, 0);
       /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
        }else if (preference == mSmartWifiPreference) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                    PrizeSmartWifi.class.getCanonicalName(), null,
                    R.string.prize_smart_wifi_title, null, this, 0);
        /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
        }else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    private void showDialog(AccessPoint accessPoint, int dialogMode) {
        if (accessPoint != null) {
            WifiConfiguration config = accessPoint.getConfig();
            if (isEditabilityLockedDown(getActivity(), config) && accessPoint.isActive()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(),
                        RestrictedLockUtils.getDeviceOwner(getActivity()));
                return;
            }
        }

        if (mDialog != null) {
            removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
        }

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDialogMode = dialogMode;

        showDialog(WIFI_DIALOG_ID);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                AccessPoint ap = mDlgAccessPoint; // For manual launch
                if (ap == null) { // For re-launch from saved state
                    if (mAccessPointSavedState != null) {
                        ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                        // For repeated orientation changes
                        mDlgAccessPoint = ap;
                        // Reset the saved access point data
                        mAccessPointSavedState = null;
                    }
                }
                // If it's null, fine, it's for Add Network
                mSelectedAccessPoint = ap;
                /// M: add mtk feature @{
                if (mSelectedAccessPoint != null) {
                    mWifiSettingsExt.recordPriority(mSelectedAccessPoint.getConfig());
                }
                /// @}
                mDialog = new WifiDialog(getActivity(), this, ap, mDialogMode,
                        /* no hide submit/connect */ false);
                return mDialog;
            case WPS_PBC_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.PBC);
            case WPS_PIN_DIALOG_ID:
                return new WpsDialog(getActivity(), WpsInfo.DISPLAY);
            case WRITE_NFC_DIALOG_ID:
                if (mSelectedAccessPoint != null) {
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(
                            getActivity(), mSelectedAccessPoint.getConfig().networkId,
                            mSelectedAccessPoint.getSecurity(),
                            mWifiManager);
                } else if (mWifiNfcDialogSavedState != null) {
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(
                            getActivity(), mWifiNfcDialogSavedState, mWifiManager);
                }

                return mWifiToNfcDialog;
        }
        return super.onCreateDialog(dialogId);
    }

    /**
     * Shows the latest access points available with supplemental information like
     * the strength of network and the security for it.
     */
    @Override
    public void onAccessPointsChanged() {
        // Safeguard from some delayed event handling
        if (getActivity() == null) return;
        if (isUiRestricted()) {
            if (!isUiRestrictedByOnlyAdmin()) {
                addMessagePreference(R.string.wifi_empty_list_user_restricted);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                // AccessPoints are automatically sorted with TreeSet.
                final Collection<AccessPoint> accessPoints =
                        mWifiTracker.getAccessPoints();
                // getPreferenceScreen().removeAll();
                mWifiSettingsExt.emptyCategory(getPreferenceScreen());

                boolean hasAvailableAccessPoints = false;
                /// M: add ap to screen @{
                Log.d(TAG, "accessPoints.size() = "  + accessPoints.size());
                // Modify by zhudaopeng at 2016-11-18 Start
                int index;
                if(isSmartWifi){
                    index = 2;
                }else{
                    index = 1;
                }
                // Modify by zhudaopeng at 2016-11-18 End
                cacheRemoveAllPrefs(getPreferenceScreen());
                for (AccessPoint accessPoint : accessPoints) {
                    // Ignore access points that are out of range.
                    if (accessPoint.getLevel() != -1) {
                        String key = accessPoint.getBssid();
                        hasAvailableAccessPoints = true;
                        LongPressAccessPointPreference pref = (LongPressAccessPointPreference)
                                getCachedPreference(key);
                        if (pref != null) {
                            pref.setOrder(index++);

                            /// M: modify for mtk feature @{
                            // getPreferenceScreen().addPreference(pref);
                            mWifiSettingsExt.addPreference(getPreferenceScreen(),
                                pref, accessPoint.getConfig() != null);
                            /// @}
                            continue;
                        }
                        LongPressAccessPointPreference
                                preference = new LongPressAccessPointPreference(accessPoint,
                                getPrefContext(), mUserBadgeCache, false, this,this);
                        preference.setKey(key);
                        preference.setOrder(index++);

                        if (mOpenSsid != null && mOpenSsid.equals(accessPoint.getSsidStr())
                                && !accessPoint.isSaved()
                                && accessPoint.getSecurity() != AccessPoint.SECURITY_NONE) {
                            onPreferenceTreeClick(preference);
                            mOpenSsid = null;
                        }
                        // getPreferenceScreen().addPreference(preference);
                        mWifiSettingsExt.addPreference(getPreferenceScreen(),
                                preference, accessPoint.getConfig() != null);
                        accessPoint.setListener(this);
                    }
                }
                removeCachedPrefs(getPreferenceScreen());
                /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
                if(isSmartWifi){
                    mSmartWifiPreference.setOrder(0);
                    getPreferenceScreen().addPreference(mSmartWifiPreference);
                    mAdvancedPreference.setOrder(1);
                    getPreferenceScreen().addPreference(mAdvancedPreference);
                }else{
                    // Add by zhudaopeng at 2016-11-18 Start
                    mAdvancedPreference.setOrder(0);
                    getPreferenceScreen().addPreference(mAdvancedPreference);
                    // Add by zhudaopeng at 2016-11-18 End
                }
                /* prize-add-by-lijimeng-for smart wifi-20180601-end*/
                if (!hasAvailableAccessPoints) {
                    setProgressBarVisible(true);
                    Preference pref = new Preference(getContext()) {
                        @Override
                        public void onBindViewHolder(PreferenceViewHolder holder) {
                            super.onBindViewHolder(holder);
                            // Show a line on each side of add network.
                            holder.setDividerAllowedBelow(true);
                        }
                    };
                    // Modify by zhudaopeng at 2016-11-18 Start
                    pref.setSelectable(false);
                    pref.setSummary(R.string.wifi_empty_list_wifi_on);
                     /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
                    if(isSmartWifi){
                        pref.setOrder(2);
                        mAddPreference.setOrder(3);
                    }else{
                        pref.setOrder(1);
                        mAddPreference.setOrder(2);
                    }
                     /* prize-add-by-lijimeng-for smart wifi-20180601-end*/
                    getPreferenceScreen().addPreference(pref);
                    getPreferenceScreen().addPreference(mAddPreference);
                    // Modify by zhudaopeng at 2016-11-18 Start
                } else {
                    mAddPreference.setOrder(index++);
                    getPreferenceScreen().addPreference(mAddPreference);
                    setProgressBarVisible(false);
                }
                if (mScanMenuItem != null) {
                    mScanMenuItem.setEnabled(true);
                }
                /// M : mtk features
                mWifiSettingsExt.refreshCategory(getPreferenceScreen());
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                // getPreferenceScreen().removeAll();
                mWifiSettingsExt.emptyScreen(getPreferenceScreen());
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(R.string.wifi_stopping);
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                setProgressBarVisible(false);
                if (mScanMenuItem != null) {
                    mScanMenuItem.setEnabled(false);
                }
                break;
        }
    }

    private void setOffMessage() {
        if (isUiRestricted()) {
            if (!isUiRestrictedByOnlyAdmin()) {
                addMessagePreference(R.string.wifi_empty_list_user_restricted);
            }
            getPreferenceScreen().removeAll();
            return;
        }

        TextView emptyTextView = getEmptyTextView();
        if (emptyTextView == null) {
            return;
        }

        final CharSequence briefText = getText(R.string.wifi_empty_list_wifi_off);

        // Don't use WifiManager.isScanAlwaysAvailable() to check the Wi-Fi scanning mode. Instead,
        // read the system settings directly. Because when the device is in Airplane mode, even if
        // Wi-Fi scanning mode is on, WifiManager.isScanAlwaysAvailable() still returns "off".
        final ContentResolver resolver = getActivity().getContentResolver();
        final boolean wifiScanningMode = Settings.Global.getInt(
                resolver, Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1;
        if (!wifiScanningMode) {
            // Show only the brief text if the user is not allowed to configure scanning settings,
            // or the scanning mode has been turned off.
            emptyTextView.setText(briefText, BufferType.SPANNABLE);
        } else {
            // Append the description of scanning settings with link.
            final StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(briefText);
            contentBuilder.append("\n\n");
            contentBuilder.append(getText(R.string.wifi_scan_notify_text));
            LinkifyUtils.linkify(emptyTextView, contentBuilder, new LinkifyUtils.OnClickListener() {
                @Override
                public void onClick() {
                    final SettingsActivity activity =
                            (SettingsActivity) WifiSettings.this.getActivity();
                    activity.startPreferencePanel(ScanningSettings.class.getName(), null,
                            R.string.location_scanning_screen_title, null, null, 0);
                }
            });
        }
        // Embolden and enlarge the brief description anyway.
        Spannable boldSpan = (Spannable) emptyTextView.getText();
        boldSpan.setSpan(
                new TextAppearanceSpan(getActivity(), R.style.prize_empty_text_style), 0,
                briefText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // getPreferenceScreen().removeAll();
        mWifiSettingsExt.emptyScreen(getPreferenceScreen());
    }

    private void addMessagePreference(int messageId) {
        TextView emptyTextView = getEmptyTextView();
        if (emptyTextView != null){
			emptyTextView.setText(messageId);
		} 
        /// M: modify for mtk feature @{
        // getPreferenceScreen().removeAll();
        mWifiSettingsExt.emptyScreen(getPreferenceScreen());
        /// @}
    }

    protected void setProgressBarVisible(boolean visible) {
        if (mProgressHeader != null) {
            mProgressHeader.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onWifiStateChanged(int state) {
        /// M: Safeguard from some delayed event handling @{
        if (getActivity() == null) {
            return;
        }
        /// @}

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setOffMessage();
                setProgressBarVisible(false);
                if (mScanMenuItem != null) {
                    mScanMenuItem.setEnabled(false);
                }
                break;
        }
    }

    @Override
    public void onConnectedChanged() {
        /// M: update priority after connnect AP @{
        if (mWifiTracker.isConnected()) {
            mWifiSettingsExt.updatePriority();
        }
        /// @}
        changeNextButtonState(mWifiTracker.isConnected());
    }

    /**
     * Renames/replaces "Next" button when appropriate. "Next" button usually exists in
     * Wifi setup screens, not in usual wifi settings screen.
     *
     * @param enabled true when the device is connected to a wifi network.
     */
    private void changeNextButtonState(boolean enabled) {
        if (mEnableNextOnConnection && hasNextButton()) {
            getNextButton().setEnabled(enabled);
        }
    }

    @Override
    public void onForget(WifiDialog dialog) {
        forget();
    }

    @Override
    public void onSubmit(WifiDialog dialog) {
        if (mDialog != null) {
            submit(mDialog.getController());
        }
    }

    /* package */ void submit(WifiConfigController configController) {

        final WifiConfiguration config = configController.getConfig();
        Log.d(TAG, "submit, config = " + config);

        /* prize-add-by-lijimeng-for smart wifi-20180601-start*/
        if (mSelectedAccessPoint != null &&
            mSelectedAccessPoint.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
            Activity activity = getActivity();
            if (activity != null) {
                Intent intent = new Intent();
                intent.setAction("android.prize.wifi.POOR_LINK_FORCE_RECONNECT");
                activity.sendBroadcast(intent);
                return;
            }
        }
        /* prize-add-by-lijimeng-for smart wifi-20180601-end*/

        /// M: add mtk feature
        if (mSelectedAccessPoint != null) {
            mWifiSettingsExt.submit(config, mSelectedAccessPoint,
                mSelectedAccessPoint.getNetworkInfo() != null ?
                mSelectedAccessPoint.getNetworkInfo().getDetailedState() : null);
        }

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.isSaved()) {
                connect(mSelectedAccessPoint.getConfig());
            }
        } else if (configController.getMode() == WifiConfigUiBase.MODE_MODIFY) {
            mWifiManager.save(config, mSaveListener);
        } else {
            mWifiManager.save(config, mSaveListener);
            if (mSelectedAccessPoint != null) { // Not an "Add network"
                connect(config);
            }
        }

        mWifiTracker.resumeScanning();
    }

    /* package */ void forget() {
        MetricsLogger.action(getActivity(), MetricsEvent.ACTION_WIFI_FORGET);
        if (!mSelectedAccessPoint.isSaved()) {
            if (mSelectedAccessPoint.getNetworkInfo() != null &&
                    mSelectedAccessPoint.getNetworkInfo().getState() != State.DISCONNECTED) {
                // Network is active but has no network ID - must be ephemeral.
                mWifiManager.disableEphemeralNetwork(
                        AccessPoint.convertToQuotedString(mSelectedAccessPoint.getSsidStr()));
            } else {
                // Should not happen, but a monkey seems to trigger it
                Log.e(TAG, "Failed to forget invalid network " + mSelectedAccessPoint.getConfig());
                return;
            }
        } else {
            mWifiManager.forget(mSelectedAccessPoint.getConfig().networkId, mForgetListener);
        }

        mWifiTracker.resumeScanning();

        // We need to rename/replace "Next" button in wifi setup context.
        changeNextButtonState(false);

        /// M: since we lost a configured AP, left ones priority need to be refreshed
        mWifiSettingsExt.updatePriority();
    }

    protected void connect(final WifiConfiguration config) {
        MetricsLogger.action(getActivity(), MetricsEvent.ACTION_WIFI_CONNECT);
        mWifiManager.connect(config, mConnectListener);
    }

    protected void connect(final int networkId) {
        MetricsLogger.action(getActivity(), MetricsEvent.ACTION_WIFI_CONNECT);
        mWifiManager.connect(networkId, mConnectListener);
    }

    /**
     * Called when "add network" button is pressed.
     */
    /* package */ void onAddNetworkPressed() {
        MetricsLogger.action(getActivity(), MetricsEvent.ACTION_WIFI_ADD_NETWORK);
        // No exact access point is selected.
        mSelectedAccessPoint = null;
        showDialog(null, WifiConfigUiBase.MODE_CONNECT);
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_wifi;
    }

    @Override
    public void onAccessPointChanged(AccessPoint accessPoint) {
        ((LongPressAccessPointPreference) accessPoint.getTag()).refresh();
    }

    @Override
    public void onLevelChanged(AccessPoint accessPoint) {
        ((LongPressAccessPointPreference) accessPoint.getTag()).onLevelChanged();
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> result = new ArrayList<>();
                final Resources res = context.getResources();

                // Add fragment title
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.wifi_settings);
                data.screenTitle = res.getString(R.string.wifi_settings);
                data.keywords = res.getString(R.string.keywords_wifi);
                result.add(data);

                // Add saved Wi-Fi access points
                final Collection<AccessPoint> accessPoints =
                        WifiTracker.getCurrentAccessPoints(context, true, false, false);
                for (AccessPoint accessPoint : accessPoints) {
                    data = new SearchIndexableRaw(context);
                    data.title = accessPoint.getSsidStr();
                    data.screenTitle = res.getString(R.string.wifi_settings);
                    data.enabled = enabled;
                    result.add(data);
                }

                return result;
            }
        };

    /**
     * Returns true if the config is not editable through Settings.
     * @param context Context of caller
     * @param config The WiFi config.
     * @return true if the config is not editable through Settings.
     */
    static boolean isEditabilityLockedDown(Context context, WifiConfiguration config) {
        return !canModifyNetwork(context, config);
    }

    /**
     * This method is a stripped version of WifiConfigStore.canModifyNetwork.
     * TODO: refactor to have only one method.
     * @param context Context of caller
     * @param config The WiFi config.
     * @return true if Settings can modify the config.
     */
    static boolean canModifyNetwork(Context context, WifiConfiguration config) {
        if (config == null) {
            return true;
        }

        final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        // Check if device has DPM capability. If it has and dpm is still null, then we
        // treat this case with suspicion and bail out.
        final PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN) && dpm == null) {
            return false;
        }

        boolean isConfigEligibleForLockdown = false;
        if (dpm != null) {
            final ComponentName deviceOwner = dpm.getDeviceOwnerComponentOnAnyUser();
            if (deviceOwner != null) {
                final int deviceOwnerUserId = dpm.getDeviceOwnerUserId();
                try {
                    final int deviceOwnerUid = pm.getPackageUidAsUser(deviceOwner.getPackageName(),
                            deviceOwnerUserId);
                    isConfigEligibleForLockdown = deviceOwnerUid == config.creatorUid;
                } catch (NameNotFoundException e) {
                    // don't care
                }
            }
        }
        if (!isConfigEligibleForLockdown) {
            return true;
        }

        final ContentResolver resolver = context.getContentResolver();
        final boolean isLockdownFeatureEnabled = Settings.Global.getInt(resolver,
                Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN, 0) != 0;
        return !isLockdownFeatureEnabled;
    }

    @Override
    public void setSelectedAccessPoint(AccessPoint accessPoint) {
        mSelectedAccessPoint = accessPoint;
    }

    private static class SummaryProvider extends BroadcastReceiver
            implements SummaryLoader.SummaryProvider {

        private final Context mContext;
        private final WifiManager mWifiManager;
        private final WifiStatusTracker mWifiTracker;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
            mWifiManager = context.getSystemService(WifiManager.class);
            mWifiTracker = new WifiStatusTracker(mWifiManager);
        }

        private CharSequence getSummary() {
            if (!mWifiTracker.enabled) {
                return mContext.getString(R.string.wifi_disabled_generic);
            }
            if (!mWifiTracker.connected) {
                return mContext.getString(R.string.disconnected);
            }
            return mWifiTracker.ssid;
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
                mSummaryLoader.registerReceiver(this, filter);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mWifiTracker.handleBroadcast(intent);
            mSummaryLoader.setSummary(this, getSummary());
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == PrizeWifiDetailsPage.REMOVE_NETWORK){
            forget();
        }
		if(resultCode == PrizeWifiDetailsPage.SHOW_EAP_DIALOG){
			if(mSelectedAccessPoint != null && !mSelectedAccessPoint.isSaved()){
				mDialog = new WifiDialog(getActivity(), this, mSelectedAccessPoint, mDialogMode,false);
				mDialog.show();
			}else if(mSelectedAccessPoint != null && mSelectedAccessPoint.isSaved()){
				connect(mSelectedAccessPoint.getConfig());
			}
			
		}
        if(resultCode == PrizeWifiDetailsPage.ADD_NETWORK_STATUS0){
            if(data!= null){
               setIpAndProxy(data);
			   String password = data.getStringExtra("wifiPassword");
               savePassword(password);
            }
			// else if(mSelectedAccessPoint.getSecurity() == 0){
				// savePassword(null );
			// }

        }
        if(resultCode == PrizeWifiDetailsPage.ADD_NETWORK_STATUS1){
            if(data!= null){
				String password = data.getStringExtra("wifiPassword");
                setIpAndProxy(data);
                savePassword(password); 
            }
        }
        if(resultCode == PrizeWifiDetailsPage.CONNECT_NETWORK){
            connect(mSelectedAccessPoint.getConfig());
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void savePassword(String mPasswordView){

        WifiConfiguration config = mSelectedAccessPoint.getConfig();
        if(config == null){
            config = new WifiConfiguration();
        }
         if (!mSelectedAccessPoint.isSaved()) {
            config.SSID = AccessPoint.convertToQuotedString(mSelectedAccessPoint.getSsidStr());
        } else {
            config.networkId = mSelectedAccessPoint.getConfig().networkId;
        }

      int mAccessPointSecurity =  mSelectedAccessPoint.getSecurity();
	    if(mPasswordView == null && mAccessPointSecurity != 0){
            return;
        }
        switch (mAccessPointSecurity) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;

            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (mPasswordView.length() != 0) {
                    int length = mPasswordView.length();
                    // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                    if ((length == 10 || length == 26 || length == 58)
                            && mPasswordView.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = mPasswordView;
                    } else {
                        config.wepKeys[0] = '"' + mPasswordView + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (mPasswordView.length() != 0) {
                    if (mPasswordView.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = mPasswordView;
                    } else {
                        config.preSharedKey = '"' + mPasswordView + '"';
                    }
                }
                break;

            case AccessPoint.SECURITY_EAP:
                config.allowedKeyManagement.set(KeyMgmt.WPA_EAP);
                config.allowedKeyManagement.set(KeyMgmt.IEEE8021X);
                    if (mPasswordView.length() > 0) {
                        config.enterpriseConfig.setPassword(mPasswordView);
                    }

                break;
            default:
                break;
        }
        config.setIpConfiguration(new IpConfiguration(mIpAssignment, mProxySettings, staticIpConfiguration, mHttpProxy));
		 // if (mSelectedAccessPoint != null) {
            // mWifiSettingsExt.submit(config, mSelectedAccessPoint,
                    // mSelectedAccessPoint.getNetworkInfo() != null ?
                            // mSelectedAccessPoint.getNetworkInfo().getDetailedState() : null);
        // }
			staticIpConfiguration = null;
            mWifiManager.save(config, mSaveListener);
			if (mSelectedAccessPoint != null) {
				connect(config);
			}
    }
    private int  setIpSettings(String ipAddressEdiText,String lengthEdiText,String routerEdiText,String domain1EdiText,String domain2EdiText){
         staticIpConfiguration = new StaticIpConfiguration();

        String ipAddr = ipAddressEdiText;
        if (TextUtils.isEmpty(ipAddr)) return R.string.wifi_ip_settings_invalid_ip_address;

        Inet4Address inetAddr = getIPv4Address(ipAddr);
        if (inetAddr == null || inetAddr.equals(Inet4Address.ANY)) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        int networkPrefixLength = -1;
        try {
            networkPrefixLength = Integer.parseInt(lengthEdiText);
            if (networkPrefixLength < 0 || networkPrefixLength > 32) {
                return R.string.wifi_ip_settings_invalid_network_prefix_length;
            }
            staticIpConfiguration.ipAddress = new LinkAddress(inetAddr, networkPrefixLength);
        } catch (NumberFormatException e) {
            // Set the hint as default after user types in ip address
        } catch (IllegalArgumentException e) {
            return R.string.wifi_ip_settings_invalid_ip_address;
        }

        String gateway = routerEdiText;
        if (TextUtils.isEmpty(gateway)) {
            try {
                //Extract a default gateway from IP address
                InetAddress netPart = NetworkUtils.getNetworkPart(inetAddr, networkPrefixLength);
                byte[] addr = netPart.getAddress();
                addr[addr.length - 1] = 1;

            } catch (RuntimeException ee) {
            }
        } else {
            InetAddress gatewayAddr = getIPv4Address(gateway);
            if (gatewayAddr == null) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            if (gatewayAddr.isMulticastAddress()) {
                return R.string.wifi_ip_settings_invalid_gateway;
            }
            staticIpConfiguration.gateway = gatewayAddr;
        }

        String dns = domain1EdiText;
        InetAddress dnsAddr = null;

        if (TextUtils.isEmpty(dns)) {
            //If everything else is valid, provide hint as a default option
        } else {
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }

        if (domain2EdiText!=null && domain2EdiText.length() > 0) {
            dns =domain2EdiText;
            dnsAddr = getIPv4Address(dns);
            if (dnsAddr == null) {
                return R.string.wifi_ip_settings_invalid_dns;
            }
            staticIpConfiguration.dnsServers.add(dnsAddr);
        }
        return 0;
    }

    private Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }

    private boolean setProxy(String hostName,String portName,String urlString) {
			mHttpProxy = null;
            mProxySettings = ProxySettings.STATIC;
            String host = hostName;
            String portStr = portName;
            String exclusionList = urlString;
            int port = 0;
            int result = 0;
			if(host == null){
				return false;
			}
            try {
                port = Integer.parseInt(portStr);
                result = ProxySelector.validate(host, portStr, exclusionList);
            } catch (NumberFormatException e) {
                result = R.string.proxy_error_invalid_port;
            }
            if (result == 0) {
                mHttpProxy = new ProxyInfo(host, port, exclusionList);
            } else {
                return false;
            }
        return true;
    }
    private boolean setProxy2(String text) {
		mHttpProxy = null;
        mProxySettings = ProxySettings.PAC;
        String uriSequence = text;
        if (TextUtils.isEmpty(uriSequence)) {
            return false;
        }
        Uri uri = Uri.parse(uriSequence);
        if (uri == null) {
            return false;
        }
        mHttpProxy = new ProxyInfo(uri);
        return true;
    }
	 @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
		activity.setTheme(R.style.WifiTheme);
    }
	
	private void setIpAndProxy(Intent data){
        String summary = data.getStringExtra("ip_summary");
        if(summary != null && summary.equals(mIpSetting[0])){
            mIpAssignment = IpAssignment.DHCP;
        }else if(summary != null && summary.equals(mIpSetting[1])){
            String ipAddress = data.getStringExtra("ip_address");
            String router = data.getStringExtra("router");
            String length = data.getStringExtra("length");
            String domian1 = data.getStringExtra("domian1");
            String domian2 = data.getStringExtra("domian2");
            if(ipAddress != null){
				setIpSettings(ipAddress,length,router,domian1,domian2);
                mIpAssignment = IpAssignment.STATIC;
             }
         }
         String proxySummary = data.getStringExtra("proxy_summary");
         if(proxySummary != null && proxySummary.equals(mProxyData[0])){
             mProxySettings = ProxySettings.NONE;
             mHttpProxy = null;
         }else if(proxySummary != null && proxySummary.equals(mProxyData[1])){
            String host = data.getStringExtra("host");
            String port = data.getStringExtra("port");
            String url = data.getStringExtra("url");
			if(url == null){
				url = new String();
			}
            setProxy(host,port,url);
         }else if(proxySummary != null && proxySummary.equals(mProxyData[2])){
             String pac = data.getStringExtra("pac");
             setProxy2(pac);
		}
	}
	private ContentObserver mAirplaneMode = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {
           final boolean enabled = Settings.Global.getInt(getActivity().getContentResolver(),Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
		   if(mSwitchBar != null){
			   if(mSwitchBar.isChecked() && enabled){
				   mSwitchBar.setChecked(false);
			   }
			   mSwitchBar.setEnabled(!enabled);
		   }
       }
    };

	public WifiTracker getWifiTracker(){
		return mWifiTracker;
	}
	private void showPrizeDialog(){
		mPrizeAlertDialog = new PrizeAlertDialog(getActivity(),mList);
		ListView listView = mPrizeAlertDialog.getListView();
		if(listView != null){
			listView.setOnItemClickListener(this);
		}
		TextView wifiName = mPrizeAlertDialog.getTextView();
		if(wifiName != null){
			wifiName.setText(mSelectedAccessPoint.getSsid());
		}
		mPrizeAlertDialog.show();
	}
}
