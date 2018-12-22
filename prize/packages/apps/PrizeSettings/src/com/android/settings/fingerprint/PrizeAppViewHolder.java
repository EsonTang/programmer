/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.settings.fingerprint;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.applications.ManageApplications;
import com.android.settings.widget.ToggleSwitch;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settings.applock.PrizeAppLockMetaData;
// View Holder used when displaying views
public class PrizeAppViewHolder {
    public ApplicationsState.AppEntry entry;
    public View rootView;
    public TextView appName;
    public ImageView appIcon;
    public ToggleSwitch mAppLockSwitch;
//    public TextView disabled;
    
    public OnCheckedChangeListener mCheckedListener = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			ContentResolver mResolver= buttonView.getContext().getContentResolver();
        	ContentValues values = new ContentValues();
        	String where = null;
        	String[] selectionArgs = null;
        	if(isChecked){
        		values.put(PrizeAppLockMetaData.LOCK_STATUS, 1);
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
                /*values.put(PrizeAppLockMetaData.LOCK_STATUS_SETTINGS, 1);*/
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        	} else {
        		values.put(PrizeAppLockMetaData.LOCK_STATUS, 0);
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
                /*values.put(PrizeAppLockMetaData.LOCK_STATUS_SETTINGS, 0);*/
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        	}
        	where = PrizeAppLockMetaData.PKG_NAME + " =?";
        	selectionArgs = new String[]{entry.info.packageName};
        	mResolver.update(PrizeAppLockMetaData.CONTENT_URI, values, where, selectionArgs);
		}
    };

    static public PrizeAppViewHolder createOrRecycle(LayoutInflater inflater, View convertView) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.prize_app_lock_item, null);
//            inflater.inflate(R.layout.widget_text_views,(ViewGroup) convertView.findViewById(android.R.id.widget_frame));

            PrizeAppViewHolder holder = new PrizeAppViewHolder();
            holder.rootView = convertView;
            holder.appName = (TextView) convertView.findViewById(android.R.id.title);
            holder.appIcon = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.mAppLockSwitch = (ToggleSwitch) convertView.findViewById(R.id.app_lock_switch);
//            holder.summary = (TextView) convertView.findViewById(R.id.widget_text1);
//            holder.summary.setVisibility(View.GONE);
//            holder.disabled = (TextView) convertView.findViewById(R.id.widget_text2);
//            holder.disabled.setVisibility(View.GONE);
            convertView.setTag(holder);
            return holder;
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            return (PrizeAppViewHolder)convertView.getTag();
        }
    }

    public void updateSizeText(CharSequence invalidSizeStr, int whichSize) {
        if (entry.sizeStr != null) {
            switch (whichSize) {
                case ManageApplications.SIZE_INTERNAL:
//                    summary.setText(entry.internalSizeStr);
                    break;
                case ManageApplications.SIZE_EXTERNAL:
//                    summary.setText(entry.externalSizeStr);
                    break;
                default:
//                    summary.setText(entry.sizeStr);
                    break;
            }
        } else if (entry.size == ApplicationsState.SIZE_INVALID) {
//            summary.setText(invalidSizeStr);
        }
    }
}