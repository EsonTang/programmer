/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;

/**
 * modify dengli 20181203 for home button switch feature
 */
public class HomeButtonSwitchReceiver extends BroadcastReceiver {

    private static final String TAG = "HomeButtonSwitchReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
		Log.d(TAG, action + " dengli ");
		Bundle bundle = intent.getExtras();
        if ("com.jty.homebutton_close".equals(action)) {
           Settings.System.putInt(context.getContentResolver(), Settings.System.HOME_BUTTON_SWITCH, 0);
        }else if ("com.jty.homebutton_open".equals(action)) {
           Settings.System.putInt(context.getContentResolver(), Settings.System.HOME_BUTTON_SWITCH, 1);
        }else if ("com.jty.homebutton_switch".equals(action)) {
			processCustomMessage(context, bundle);
        }
    }
		
		private void processCustomMessage(Context context, Bundle bundle) {
				int action = Settings.System.getInt(context.getContentResolver(), Settings.System.HOME_BUTTON_SWITCH, 0);
				Intent mIntent=new Intent("com.jty.homebutton_switch_t");
				mIntent.putExtra("message", ""+action);
				Log.d(TAG, action + " dengli message");
				context.sendBroadcast(mIntent);
				
		}


}
