package com.android.floatwindow;

import com.android.floatwindow.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button mButton1;
	private Button mButton2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mButton1 = (Button) findViewById(R.id.button1);
		mButton2 = (Button) findViewById(R.id.button2);
		
		mButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent iFloatWindow = new Intent(FloatWindowReceiver.PRIZE_FLOAT_WINDOW);
				MainActivity.this.sendBroadcast(iFloatWindow);
			}
		});
		
		mButton2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent iFloatWindow = new Intent(FloatWindowReceiver.PRIZE_FLOAT_WINDOW);
				MainActivity.this.sendBroadcast(iFloatWindow);
				
			}
		});
		
		
		
	}

	

}
