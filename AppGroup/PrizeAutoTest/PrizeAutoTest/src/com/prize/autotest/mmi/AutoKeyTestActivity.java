package com.prize.autotest.mmi;

import com.prize.autotest.AutoConstant;
import com.prize.autotest.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class AutoKeyTestActivity extends Activity {

	private boolean bKeyHome = false;
	private boolean bKeyMenu = false;
	private boolean bKeyBack = false;
	private boolean bKeyVoUp = false;
	private boolean bKeyVoDn = false;
	private String cmdOrder = null;
	private BroadcastReceiver mBroadcast = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.key);
		if (SystemProperties.get("ro.support_hiding_navbar").equals("1")) {
			TextView mKeyMenu = (TextView) findViewById(R.id.menu);
			mKeyMenu.setVisibility(View.GONE);
			TextView mKeyHome = (TextView) findViewById(R.id.home);
			mKeyHome.setVisibility(View.GONE);
			TextView mKeyBack = (TextView) findViewById(R.id.back);
			mKeyBack.setVisibility(View.GONE);
		}
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AutoConstant.ACTION_UI);
		registerReceiver(mBroadcast, filter);

		Intent intent = getIntent();
		cmdOrder = intent.getStringExtra("back");
		if (cmdOrder != null) {
			new Handler().post(new Runnable() {
				public void run() {
					runCmdOrder();
				}
			});
		}		
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				cmdOrder = intent.getStringExtra("back");
				runCmdOrder();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	private void runCmdOrder() {
		if (cmdOrder == null || cmdOrder.length() < 1) {
			return;
		}

		String temp = cmdOrder.substring(1);
		//Toast.makeText(this, temp + "", Toast.LENGTH_SHORT).show();
		if (temp.startsWith(AutoConstant.CMD_MMI_KEY_RESULT)) {
			if (SystemProperties.get("ro.support_hiding_navbar").equals("1")) {
				if (bKeyVoUp && bKeyVoDn) {
					AutoConstant.writeFile("key : PASS" + "\n");
					AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
							this);
				} else {
					AutoConstant.writeFile("key : FAIL" + "\n");
					AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
							this);
				}
			} else {
				if (bKeyVoUp && bKeyVoDn && bKeyBack && bKeyHome && bKeyMenu) {
					AutoConstant.writeFile("key : PASS" + "\n");
					AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
							this);
				} else {
					AutoConstant.writeFile("key : FAIL" + "\n");
					AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
							this);
				}
			}
			finish();
		}
		AutoConstant.SendDataToService(AutoConstant.RESULT_SUCCUSS,
				this);

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		TextView keyText = null;
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			keyText = (TextView) findViewById(R.id.volume_up);
			bKeyVoUp = true;
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			keyText = (TextView) findViewById(R.id.volume_down);
			if(bKeyVoUp){
				bKeyVoDn = true;
			}else{
				AutoConstant.writeFile("key : FAIL" + "\n" + "VoUp VoDn error! \n");
				AutoConstant.SendDataToService(AutoConstant.RESULT_FAIL,
						this);
			}
			break;
		case KeyEvent.KEYCODE_MENU:
			keyText = (TextView) findViewById(R.id.menu);
			bKeyMenu = true;
			break;
		case KeyEvent.KEYCODE_HOME:
			keyText = (TextView) findViewById(R.id.home);
			bKeyHome = true;
			break;
		case KeyEvent.KEYCODE_BACK:
			keyText = (TextView) findViewById(R.id.back);
			bKeyBack = true;
			break;
		}
		if (null != keyText) {
			keyText.setBackgroundResource(R.color.green);

		}
		return true;
	}

}
