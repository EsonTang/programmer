
package com.android.settings.deviceinfo;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.InstrumentedFragment;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.PrizeSettingsPreferenceFragment;
import com.android.settings.Utils;
import com.mediatek.settings.deviceinfo.UsbSettingsExts;
import android.app.Dialog;
import android.content.ContentResolver;
import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.provider.Settings;
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.app.ActivityManager;
public class UsbSettings extends PrizeSettingsPreferenceFragment implements OnPreferenceChangeListener {

    private UsbManager mUsbManager;
    private boolean mUsbAccessoryMode;
    private UsbBackend mBackend;
    private UsbSettingsExts mUsbExts;
	private CompoundButton switchButton;
	private Dialog mAdbDialog;
	private CheckBox localCheckBox;	
	final int msgAdb =1;	
    final int delayMs=800;
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			getActivity().finish();
		}
		
	};
    public static final int[] DEFAULT_MODES = {
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_NONE,
        UsbBackend.MODE_POWER_SOURCE | UsbBackend.MODE_DATA_NONE,
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_MTP,
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_PTP,
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_MIDI,
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_MASS_STORAGE,
        UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_BICR
    };
	@Override
    protected int getMetricsCategory() {
        return MetricsEvent.FUELGAUGE_POWER_USAGE_SUMMARY;
    }
	@Override
	public void onSaveInstanceState(Bundle outState) {		
        super.onSaveInstanceState(outState);
    }
	private boolean getAdbFlag(){
        boolean result = false;
		int mADB = 0;
		mADB = Settings.Global.getInt(getActivity().getContentResolver(),
									Settings.Global.ADB_ENABLED, 0);		
		if(switchButton.isChecked()&&(mADB==1)){
           result = true;
		}
		return result;
	}	
    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
               mUsbAccessoryMode = intent.getBooleanExtra(UsbManager.USB_FUNCTION_ACCESSORY, false);
            }
            mUsbExts.dealWithBroadcastEvent(intent);
            if (mUsbExts.isNeedExit()) {	
				if(!getActivity().isFinishing()){
				    if(getAdbFlag()){
					   mHandler.removeMessages(msgAdb);
					   mHandler.sendEmptyMessageDelayed(msgAdb, delayMs);
					}else{
					   getActivity().finish();
					}
				}                 
			} else if (mUsbExts.isNeedUpdate()) {
            }
        }
    };

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }

		addPreferencesFromResource(R.xml.usb_settings_prize_xiaxuefeng);
		root = getPreferenceScreen();
        root = mUsbExts.addUsbSettingsItem(this);

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            mUsbExts.updateEnableStatus(false);
        }
		setLayoutResource(root);
		return root;
	}
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		
        mUsbManager = (UsbManager)getActivity().getSystemService(Context.USB_SERVICE);
        mUsbExts = new UsbSettingsExts();
        mBackend = new UsbBackend(getActivity());
		
		createPreferenceHierarchy();
		setHeaderView(R.layout.usb_settings_head);		
		setFooterView(R.layout.usb_settings_foot);	
		
		localCheckBox = (CheckBox) getFooterView().findViewById(R.id.not_ask_again);
		if (SystemProperties.get("persist.sys.usb_charge", "0").equals(
					"1")) {
			localCheckBox.setEnabled(false);
			localCheckBox.setButtonDrawable(R.drawable.btn_check_disable);
		} 
		if (SystemProperties.get("persist.sys.usb_askagain", "false").equals(
					"true"))
			localCheckBox.setChecked(true);
			
			localCheckBox
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						public void onCheckedChanged(
								CompoundButton paramCompoundButton,
								boolean paramBoolean) {
							if (paramBoolean) {
								SystemProperties.set(
										"persist.sys.usb_askagain", "true");
							} else {
								SystemProperties.set(
										"persist.sys.usb_askagain","false");
							}
								
						}
					});
			final ContentResolver cr = getActivity().getContentResolver();
			boolean isChecked = (mAdbDialog != null && mAdbDialog.isShowing()) ? true
				: (Settings.Global.getInt(cr, Settings.Global.ADB_ENABLED, 0) != 0);
			switchButton = (CompoundButton) getFooterView().findViewById(R.id.mycheckbox);
			switchButton.setChecked(isChecked);
			switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {				
				@Override
				public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
					if (arg1) {
						Settings.Global.putInt(getActivity().getContentResolver(),
								Settings.Global.ADB_ENABLED, 1);
					} else {
						Settings.Global.putInt(getActivity().getContentResolver(),
								Settings.Global.ADB_ENABLED, 0);
					}
				}
			});	
			//initUsbMode();
	
    }

    @Override
    public void onPause() {
        super.onPause();
		mHandler.removeMessages(msgAdb);
        getActivity().unregisterReceiver(mStateReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        initUsbMode();
        getActivity().registerReceiver(mStateReceiver,
                mUsbExts.getIntentFilter());
    }

    private void updateToggles(String function) {
        mUsbExts.updateCheckedStatus(function);

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            mUsbExts.updateEnableStatus(false);
        } else if (!mUsbAccessoryMode) {
            mUsbExts.updateEnableStatus(true);
        } else {
            mUsbExts.updateEnableStatus(false);
        }
        mUsbExts.setCurrentFunction(function);
    }

    private int getMode(String func){
        int mode = DEFAULT_MODES[0];
        switch(func){
            case UsbManager.USB_FUNCTION_MTP:
                mode = DEFAULT_MODES[2];
                break;
            case UsbManager.USB_FUNCTION_PTP:
                mode = DEFAULT_MODES[3];
                break;
            default:
                break;
        }
        return mode;
    }
    public String getUsbFunction(int mode) {
        String function = UsbManager.USB_FUNCTION_NONE;
        switch(mode){
            case UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_MTP:
                function = UsbManager.USB_FUNCTION_MTP;
                break;
            case UsbBackend.MODE_POWER_SINK | UsbBackend.MODE_DATA_PTP:
                function = UsbManager.USB_FUNCTION_PTP;
                break;
            default:
                break;
        }
        return function;
    }
    private boolean initUsbMode(){
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }
        int current = mBackend.getCurrentMode();
		Log.e("liup","current = " + current);
        updateToggles(getUsbFunction(current));

        mUsbExts.setNeedUpdate(false);

        if (getUsbFunction(current).equals(UsbManager.USB_FUNCTION_NONE)) {
        	localCheckBox.setEnabled(false);
        	SystemProperties.set(
					"persist.sys.usb_charge", "1");
        	localCheckBox.setButtonDrawable(R.drawable.btn_check_disable);
        } else {
        	SystemProperties.set(
					"persist.sys.usb_charge", "0");
        	localCheckBox.setEnabled(true);
        	localCheckBox.setButtonDrawable(R.drawable.checkbox_checked_style);
        }

        return true;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }

        String function = mUsbExts.getFunction(preference);
         if (!ActivityManager.isUserAMonkey()) {
            mBackend.setMode(getMode(function));
         }
        updateToggles(function);

        mUsbExts.setNeedUpdate(false);

        if (preference == mUsbExts.getCharge()) {
        	localCheckBox.setEnabled(false);
        	SystemProperties.set(
					"persist.sys.usb_charge", "1");
        	localCheckBox.setButtonDrawable(R.drawable.btn_check_disable);
        } else {
        	SystemProperties.set(
					"persist.sys.usb_charge", "0");
        	localCheckBox.setEnabled(true);
        	localCheckBox.setButtonDrawable(R.drawable.checkbox_checked_style);
        }

        return true;
	}
	private void setLayoutResource(Preference preference) {
        if (preference instanceof PreferenceScreen) {
            PreferenceScreen ps = (PreferenceScreen) preference;
            ps.setLayoutResource(R.layout.preference_screen);
            int cnt = ps.getPreferenceCount();
            for (int i = 0; i < cnt; ++i) {
                Preference p = ps.getPreference(i);
                setLayoutResource(p);
            }
        }  else {
            preference.setLayoutResource(R.layout.preference_prize);
        }
    }
	
	@Override
	public View onCreateView(LayoutInflater paramLayoutInflater,
			ViewGroup paramViewGroup, Bundle paramBundle) {
		return super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
	}
}
