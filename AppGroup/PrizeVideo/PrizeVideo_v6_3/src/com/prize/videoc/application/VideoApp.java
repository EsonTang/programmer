package com.prize.videoc.application;

import com.prize.videoc.db.DbManager;

import android.app.Application;

public class VideoApp extends Application{
	
	@Override
	public void onCreate() {
		super.onCreate();
		DbManager.getInstance().createDb(getApplicationContext());
	}

}
