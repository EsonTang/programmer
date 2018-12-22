package com.prize.boot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class WizardActivity extends BaseActivity {
	protected static final String TAG = "prize";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("prize", "WizardActivity.onCreate()");
        super.onCreate(savedInstanceState);

        initOOBE();
    }
    /**
     * init oobe, start first activity
     */
    public void initOOBE() {
       Log.d("prize", "WizardActivity.initOOBE()");
       	Intent intent = new Intent(this, OnClikSimService.class);
		this.startService(intent);
		
        Intent oobeMainIntent = new Intent(this, MainActivity.class);
        oobeMainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(oobeMainIntent);
        finish();
    }

}

