package com.android.settings.applock;

import com.android.settings.SubSettings;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.settings.R;

public class PrizeAppLockManagerActivity extends SubSettings{
	@Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, PrizeAppLockManagerSettings.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (PrizeAppLockManagerSettings.class.getName().equals(fragmentName)) return true;
        return false;
    }    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.application_lock);
        setTitle(msg);
    }
}
