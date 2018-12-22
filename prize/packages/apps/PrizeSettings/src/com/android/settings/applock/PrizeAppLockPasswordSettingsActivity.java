package com.android.settings.applock;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SettingsActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.hardware.fingerprint.FingerprintManager;
import android.content.Context;
/**
 * Created by wangzhong on 2016/7/13.
 */
public class PrizeAppLockPasswordSettingsActivity extends SettingsActivity {
     static final String TAG = "PrizeAppLock";
    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, getFragmentClass().getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeAppLockPasswordSettingsFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

    /* package */ Class<? extends Fragment> getFragmentClass() {
        return PrizeAppLockPasswordSettingsFragment.class;
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        CharSequence msg = getText(R.string.prize_applock_cipher_operation_title);
        setTitle(msg);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PrizeAppLockPasswordSettingsFragment extends SettingsPreferenceFragment 
		implements OnPreferenceChangeListener
   {
        public static final String KEY_USEFINGER_SWITCH = "applock_usefinger_switch";
	 public static final String KEY_PIN_PASSWORD = "applock_number_password";
	 public static final String KEY_COMPLEX_PASSWORD = "applock_complex_password";
	 public static final String KEY_PATTERN_PASSWORD = "applock_pattern_password";

	 public static final String SYSTEM_SETTING_KEY_USEFINGER = "applock_usefinger";
        @Override
        protected int getMetricsCategory() {
            return MetricsEvent.CHOOSE_LOCK_PASSWORD;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prize_applock_changepassword);
	     SwitchPreference usefinger = (SwitchPreference)findPreference(KEY_USEFINGER_SWITCH);
	     int ischeck = Settings.System.getInt(getActivity().getContentResolver(),SYSTEM_SETTING_KEY_USEFINGER,0);
            Log.d(TAG,"applock_userfinger:"+ischeck);
	     usefinger.setChecked(ischeck==1?true:false);
	     usefinger.setOnPreferenceChangeListener(this);
	     FingerprintManager fmgr = (FingerprintManager)getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
	     boolean ishasfingerprint = fmgr.hasEnrolledFingerprints();
	     if(!ishasfingerprint)
	     {
	         removePreference(KEY_USEFINGER_SWITCH);
	     }
        }

        @Override
    	 public boolean onPreferenceTreeClick(Preference preference) {
    	      final String key = preference.getKey();
             if (KEY_PIN_PASSWORD.equals(key)) 
	      {
	          startApplockNumSetting();
             }
	       else if(KEY_COMPLEX_PASSWORD.equals(key))
	       {
	       	startApplockComplexSetting();
	       }
		else if(KEY_PATTERN_PASSWORD.equals(key))
	       {
	       	startApplockPatternSetting();
	       }
    	      return true;
        }
	 @Override
	public boolean onPreferenceChange(Preference preference, Object value) 
	{
		if(preference == null) return true;
		String key = preference.getKey();
	  	if(key == null)return true;
		if(key.equals(KEY_USEFINGER_SWITCH))
		{
			setApplockUseFinger((Boolean)value);
			return true;
		}
		return false;
	 }
	 public void setApplockUseFinger(boolean isuse)
	 {
	 	Log.d(TAG,"setApplockUseFinger:"+isuse);
	 	Settings.System.putInt(getActivity().getContentResolver(),SYSTEM_SETTING_KEY_USEFINGER,isuse?1:0);
	 }
	 public void startApplockPatternSetting()
	 {
	 	Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            PrizeApplockChoosePatternPassword.class.getName());
              startActivity(intent);
	 }
	 public void startApplockComplexSetting()
	 {
	 	Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            PrizeApplockChooseComplexPassword.class.getName());
              startActivity(intent);
	 }
	 public void startApplockNumSetting()
	 {
	 	Intent intent = new Intent();
                    intent.setClassName("com.android.settings",
                            PrizeAppLockChooseNumPassword.class.getName());
                    startActivity(intent);
	 }
    }

}
