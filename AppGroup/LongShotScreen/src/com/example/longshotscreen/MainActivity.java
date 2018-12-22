package com.example.longshotscreen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RemoteViews;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import java.text.SimpleDateFormat;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.net.Uri;

public class MainActivity extends Activity {
	private Button btn_shot;
	private NotificationManager mNotificationManager;
	private Notification.Builder mNotificationBuilder;
	private int mNotificationId= 720;
	private long mBaseTime = 0;
	private String mToggleCordinate = "";
	private String mFilePath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent("com.freeme.supershot.MainFloatMenu"));
		finish();

		btn_shot = (Button) findViewById(R.id.shot);
		final GlobalScreenshot mscreenshot = new GlobalScreenshot(this);
		btn_shot.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				//ScreenShot.shoot(MainActivity.this);
				mscreenshot.takeScreenshot(getWindow().getDecorView(), new Runnable() {
					@Override
					public void run() {

					}
				}, true, true);
				//updateRecordingNotification();
			}
		});

	}


}