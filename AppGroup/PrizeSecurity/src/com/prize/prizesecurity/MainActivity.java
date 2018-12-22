package com.prize.prizesecurity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn = (Button)findViewById(R.id.testbutton);
		btn.setOnClickListener(new OnClickListener()
		{			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.prize.prizesecurity","com.prize.prizesecurity.ClearBackgroundService"));
				startService(intent);
			}
		});
	}

	
}
