package com.android.settings.wifi;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;


/**
 * Created by prize on 2018/6/1.
 */

public class PrizeSmartWifi extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_PRIZE_SMART_SELECT_WIFI = "prize_smart_select_wifi";
    private static final String KEY_PRIZE_SUTO_CHANGE_CELLULAR = "prize_auto_change_cellular";
    private SwitchPreference mPrizeSmartWifi;
    private SwitchPreference mPrizeAutoCellular;
    @Override
    protected int getMetricsCategory() {
        return MetricsProto.MetricsEvent.WIFI;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prize_smart_wifi);
        mPrizeSmartWifi = (SwitchPreference)findPreference(KEY_PRIZE_SMART_SELECT_WIFI);
        boolean mIsPrizeSmartWifiOpen = Settings.System.getInt(getContentResolver(),"wifi_switch_enabled",0) == 1;
        if(mIsPrizeSmartWifiOpen){
            mPrizeSmartWifi.setChecked(true);
        }else{
            mPrizeSmartWifi.setChecked(false);
        }
        mPrizeSmartWifi.setOnPreferenceChangeListener(this);
        mPrizeAutoCellular = (SwitchPreference)findPreference(KEY_PRIZE_SUTO_CHANGE_CELLULAR);
        boolean mIsPrizeAutoCellularOpen = Settings.System.getInt(getContentResolver(),"mobile_switch_enabled",0) == 1;
        if(mIsPrizeAutoCellularOpen){
            mPrizeAutoCellular.setChecked(true);
        }else{
            mPrizeAutoCellular.setChecked(false);
        }
        mPrizeSmartWifi.setOnPreferenceChangeListener(this);
        mPrizeAutoCellular.setOnPreferenceChangeListener(this);
        if(!mIsPrizeSmartWifiOpen){
            removePreference(KEY_PRIZE_SUTO_CHANGE_CELLULAR);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean isChecked = (boolean) newValue;
        switch (preference.getKey()){
            case KEY_PRIZE_SMART_SELECT_WIFI:
                if(isChecked){
                    if(!isContainCellularPreference()){
                        getPreferenceScreen().addPreference(mPrizeAutoCellular);
                    }
                    Settings.System.putInt(getContentResolver(),"wifi_switch_enabled",1);
                }else{
                    Settings.System.putInt(getContentResolver(),"wifi_switch_enabled",0);
                    removePreference(KEY_PRIZE_SUTO_CHANGE_CELLULAR);
                }
                break;
            case KEY_PRIZE_SUTO_CHANGE_CELLULAR:
                if(isChecked){
                    Settings.System.putInt(getContentResolver(),"mobile_switch_enabled",1);
                    Settings.System.putInt(getContentResolver(),"switch_mobile_remind_enabled",1);
                }else{
                    Settings.System.putInt(getContentResolver(),"mobile_switch_enabled",0);
                }
                break;
            default:
                break;
        }
        return true;
    }
    private boolean isContainCellularPreference(){
        int prefCount = getPreferenceScreen().getPreferenceCount();
        if(prefCount > 0){
            for(int i = 0;i < prefCount;i++){
                Preference pref = getPreferenceScreen().getPreference(i);
                if(pref != null && pref.getKey().equals(KEY_PRIZE_SUTO_CHANGE_CELLULAR)){
                    return true;
                }
            }
        }
        return false;
    }
}
