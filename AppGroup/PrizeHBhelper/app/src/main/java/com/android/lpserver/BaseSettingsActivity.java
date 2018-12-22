package com.android.lpserver;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;


public abstract class BaseSettingsActivity extends BaseActivity {

    public BaseSettingsActivity(){
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getFragmentManager().beginTransaction().add(R.id.container, getSettingsFragment()).commitAllowingStateLoss();

//        if(isShowBack()) {
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
//            }
//        }
    }

    protected boolean isShowBack() {
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public abstract Fragment getSettingsFragment();

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
