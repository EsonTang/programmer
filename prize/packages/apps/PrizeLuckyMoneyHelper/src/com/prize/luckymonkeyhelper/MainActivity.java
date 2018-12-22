package com.prize.luckymonkeyhelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {

	private static final String TAG = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startOrStopService(true);
			}
		});
		
		findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startOrStopService(false);
			}
		});		
		
	}

	private void startOrStopService(boolean start) {
		Intent service = new Intent();
		service.setClass(this, LuckyMoneyHelperService.class);
		
		ComponentName cn = null;
		boolean sucessfully = false;
		
		if(start) {
			cn = startService(service);
			Log.d(TAG, "startOrStopService(). start. cn=" + cn);
		} else {
			sucessfully = stopService(service);
			Log.d(TAG, "startOrStopService(). stop. sucessfully=" + sucessfully);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
