package com.android.lpserver;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {
    public BaseActivity(){
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QHBApplication.activityCreateStatistics(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        QHBApplication.activityResumeStatistics(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        QHBApplication.activityPauseStatistics(this);
    }
}
