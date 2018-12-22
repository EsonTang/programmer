package com.android.settings;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.settings.deviceinfo.UsbSettings;

public class UsbSettingsActivity extends Activity
{
  protected void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    getFragmentManager().beginTransaction().replace(android.R.id.content, new UsbSettings()).commit();
    requestWindowFeature(Window.FEATURE_NO_TITLE);
  }
}

