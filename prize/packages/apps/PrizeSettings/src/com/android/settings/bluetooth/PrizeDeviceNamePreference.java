/*
 * Copyright (C) 2011 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.settings.bluetooth;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;
public class PrizeDeviceNamePreference extends Preference {
    private TextView deviceName;
    private String phoneName;
    public PrizeDeviceNamePreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_widget_device_name);
    }
    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
		// TODO Auto-generated method stub
		deviceName = (TextView)view.findViewById(R.id.bluetooth_device_name);
		if(deviceName!=null)
			deviceName.setText(phoneName);
		Log.d("lwq","PrizeDeviceNamePreference "+","+deviceName);
		super.onBindViewHolder(view);
	}
	
	public void setDeviceName(String device){
		Log.d("lwq","PrizeDeviceNamePreference "+device+","+deviceName);
		phoneName = device;
		if(deviceName!=null)
			deviceName.setText(device);
	}
}
