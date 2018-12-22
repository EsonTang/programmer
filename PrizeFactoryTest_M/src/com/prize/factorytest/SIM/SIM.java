package com.prize.factorytest.SIM;

import com.prize.factorytest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import com.mediatek.telephony.TelephonyManagerEx;
import com.prize.factorytest.PrizeFactoryTestListActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.KeyEvent;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

public class SIM extends Activity {
	String TAG1 = "SIM1";
	String TAG2 = "SIM2";
	String resultString = "Failed";
	String simString = "";
	String sim1String = "";
	String sim2String = "";
	TextView mSim;
	private Button buttonPass;
	private SubscriptionManager mSubscriptionManager;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSubscriptionManager = SubscriptionManager.from(this);
		final SubscriptionInfo IMSI1 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
		final SubscriptionInfo IMSI2 = mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);
        if (IMSI1 != null && !IMSI1.equals("")) {
            sim1String = TAG1 + ":pass";
        } 
        else{
        	sim1String = TAG1 + ":fail";
        }
        if (IMSI2 != null && !IMSI2.equals("")) {
            sim2String = TAG2 + ":pass";
        }
        else{
        	sim2String = TAG2 + ":fail";
        }
        simString = sim1String + "\n" + sim2String;
        LinearLayout VersionLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.sim, null);
        setContentView(VersionLayout);
        mSim = (TextView)findViewById(R.id.sim_show);
        mSim.setText(simString);
        buttonPass = (Button)findViewById(R.id.passButton);
		if (sim1String.contains("fail") || sim2String.contains("fail")) {
			buttonPass.setEnabled(false);
		} else {
			buttonPass.setEnabled(true);
		}
		confirmButton();
	}

	public void confirmButton() {
		final Button buttonFail = (Button) findViewById(R.id.failButton);
		buttonPass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_OK);
				finish();
			}
		});
		buttonFail.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (PrizeFactoryTestListActivity.toStartAutoTest == true) {
					PrizeFactoryTestListActivity.itempos++;
				}
				setResult(RESULT_CANCELED);
				finish();

			}

		});
   	}
}
