package com.example.framerecorder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{
private Button btn_start,btn_stop;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn_start = (Button) findViewById(R.id.start);
		btn_stop = (Button) findViewById(R.id.stop);
		btn_start.setOnClickListener(this);
		btn_stop.setOnClickListener(this);
	}
	
	private void StartService(){
		Intent startintent = new Intent(getApplicationContext(),TakeScreenRecordService.class);
		startService(startintent);	
	}
	
	private void StopService(){
		Intent stoptintent = new Intent(getApplicationContext(),TakeScreenRecordService.class);
		stopService(stoptintent);	
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.start:
			StartService();
			
			break;
		case R.id.stop:
			StopService();
			break;
		default:
			break;
		}
		
	}
	
}
