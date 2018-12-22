package com.prize.runoldtest;


import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.LogToFile;
import com.prize.runoldtest.util.SharedPreferencesHelper;

import android.app.Application;

public class PrizeRunOldTestApplication extends Application{
    private SharedPreferencesHelper shared_pref;
    @Override
    public void onCreate() {
        super.onCreate();
        shared_pref=new SharedPreferencesHelper(this, "com.prize.runoldtest");
        loadRebootTimes();
        LogToFile.init(getApplicationContext());
    }

    private void loadRebootTimes(){
    	if(shared_pref.getValue(Const.SHARE_PREF_REBOOT_SELECTE)==null){
            shared_pref.putValue(Const.SHARE_PREF_REBOOT_SELECTE, "0");
        }
        if(shared_pref.getValue(Const.SHARE_PREF_REBOOT_CURTIMES)==null){
            shared_pref.putValue(Const.SHARE_PREF_REBOOT_CURTIMES, "0");
        }
        if(shared_pref.getValue(Const.SHARE_PREF_REBOOT_TOTALTIMES)==null){
            shared_pref.putValue(Const.SHARE_PREF_REBOOT_TOTALTIMES, "0");
        }
        
        if(shared_pref.getValue(Const.SHARE_PREF_DDR_TEST)==null){
            shared_pref.putValue(Const.SHARE_PREF_DDR_TEST, "0");
        }
        if(shared_pref.getValue(Const.SHARE_PREF_DDR_TEST_CIRCLES)==null){
            shared_pref.putValue(Const.SHARE_PREF_DDR_TEST_CIRCLES, "0");
        }
    }
	
	public SharedPreferencesHelper getSharePref(){
        return shared_pref;
    }

}
