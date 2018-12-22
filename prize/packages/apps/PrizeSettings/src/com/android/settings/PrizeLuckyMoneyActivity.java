package com.android.settings;

import android.util.Log;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

public class PrizeLuckyMoneyActivity extends Activity implements
		Preference.OnPreferenceClickListener, OnPreferenceChangeListener {
	private String notice_sound_name_default = "system/media/audio/luckymoney/GXFC.ogg";
	private String[] notice_sound_name = {"GXFC.ogg","HBLL.ogg","JBDL.ogg","LXHG.ogg","BZBP.ogg","chord.ogg","popcorn.ogg","XFML.ogg"};
	//private String[] notice_sound_name = {"Castor.ogg","HBLL.ogg","JBDL.ogg","LXHG.ogg"};
	private int[] notice_sound_id = {R.string.lucky_money_notice_sound_gxfc,R.string.lucky_money_notice_sound_hbll,
								  R.string.lucky_money_notice_sound_jbdl,R.string.lucky_money_notice_sound_lxhd,
								  R.string.lucky_money_notice_sound_bzbp,R.string.lucky_money_notice_sound_chord,
								  R.string.lucky_money_notice_sound_popcorn,R.string.lucky_money_notice_sound_xfml};
	
	private Intent service = null;
	private Switch actionBarSwitch;
	private TextView tv_no_lucky_money_helper_solution;
	private TextView tv_lucky_money_notice_sound_default;
	private RelativeLayout rl_lucky_money_notice_sound;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_lucky_money_use_activity);
		addActionBar();
		rl_lucky_money_notice_sound = (RelativeLayout)findViewById(R.id.rl_lucky_money_notice_sound);
		rl_lucky_money_notice_sound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("prize_zwl", "-----+++++++++--->");
				Intent i = new Intent(PrizeLuckyMoneyActivity.this, PrizeLuckyMoneyNoticeSoundActivity.class);  
                startActivityForResult(i, 1);  
			}
		});
		
		String noticeSounddefault = Settings.System.getString(getContentResolver(),
				Settings.System.PRIZE_LUCKY_MONEY_NOTICE_SOUND_DEFAULT);
		tv_lucky_money_notice_sound_default = (TextView)findViewById(R.id.tv_lucky_money_notice_sound_default);
		if(noticeSounddefault == null || noticeSounddefault.length() < 20){
			Settings.System.putString(getContentResolver(), Settings.System.PRIZE_LUCKY_MONEY_NOTICE_SOUND_DEFAULT, notice_sound_name_default);
			noticeSounddefault = notice_sound_name_default;
		}
		tv_lucky_money_notice_sound_default.setText(formartSound(noticeSounddefault));
		
		tv_lucky_money_notice_sound_default = (TextView)findViewById(R.id.tv_lucky_money_notice_sound_default);
		tv_no_lucky_money_helper_solution = (TextView)findViewById(R.id.tv_no_lucky_money_helper_solution);
		tv_no_lucky_money_helper_solution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("prize_zwl", "------------>");
				Intent i = new Intent(PrizeLuckyMoneyActivity.this, PrizeLuckyMoneyDialog.class);  
                startActivityForResult(i, 0);  
			}
		});
	}
	private String formartSound(String name){
		if(name == null){
			return null;
		}
		String[] temp = name.split("/");
		Log.v("prize_zwl", "formartSound--111--->" + temp[temp.length-1]);
		String[] names = temp[temp.length-1].split("\\.");
		Log.v("prize_zwl", "formartSound--222--->" + names[0]);
		
		for(int i=0;i < notice_sound_name.length;i++){
			if(notice_sound_name[i].equals(temp[temp.length-1])){
				return getResources().getString(notice_sound_id[i]);
			}
		}
		Log.v("prize_zwl", "formartSound---333-->" + names[0]);
		return names[0];
	}
	
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		if(data == null){
			Log.v("prize_zwl", "onActivityResult------------>data = null");
			return;
		}
        String notice_sonud = data.getStringExtra("notice_sonud"); 
        Log.v("prize_zwl", "onActivityResult------------>notice_sonud = " + notice_sonud);
        if(notice_sonud == null || "0".equals(notice_sonud)){
        	return;
        }
        switch (requestCode) {  
        case 1:  
        	tv_lucky_money_notice_sound_default.setText(notice_sonud);  
            break;  
        default:  
            break;  
        }  
    }  
	
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void addActionBar(){
		service = new Intent("prize.luckymoney.service");
		service.setPackage("com.prize.luckymonkeyhelper");
		
		actionBarSwitch = new Switch(this);
		boolean lmenable = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_LUCKY_MONEY_HELPER_ENABLE, 0) == 1;
		if(lmenable){
			actionBarSwitch.setChecked(true);
		}else{
			actionBarSwitch.setChecked(false);
		}
		final int padding = this.getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
		this.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,ActionBar.DISPLAY_SHOW_CUSTOM);
		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
			ActionBar.LayoutParams.WRAP_CONTENT,Gravity.CENTER_VERTICAL | Gravity.END);
		lp.setMargins(0, 0, padding, 0);
		this.getActionBar().setCustomView(actionBarSwitch, lp);
		
		actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if(isChecked){
					Log.v("prize_zwl", "lucky money enable is true !");
					startService(service);
					Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_LUCKY_MONEY_HELPER_ENABLE, 1);
				}else{
					Log.v("prize_zwl", "lucky money enable is false !");
					stopService(service);
					Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_LUCKY_MONEY_HELPER_ENABLE, 0);
				}
			}		
		});
	}	
}
