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

package com.android.settings.deviceinfo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.widget.PrizeStorageRectView;

public class PrizeStorageSummaryPreference extends Preference {
	private static final String TAG = "PrizeStorageSummaryPreference";
    private int mPercent = -1;
    private long totalBytes = -1;
    private long freeBytes = -1;
    private long usedBytes = -1;
    private long storage_detail_apps = -1;
    private long storage_detail_images = -1;
    private long storage_detail_videos = -1;
    private long storage_detail_audio = -1;
    private long storage_detail_other = -1;
    private PrizeStorageRectView mPrizeStorageRectView;
	private Context mContext;

    public PrizeStorageSummaryPreference(Context context) {
        super(context);
        mContext = context;
        setLayoutResource(R.layout.prize_storage_summary);
        setEnabled(false);
        Log.e(TAG,"++++++++onConstruct++++++++");	
    }

    public void setPercent(int percent) {
        mPercent = percent;
    }
    
    public void cleanAll(){
    	totalBytes = -1;
        freeBytes = -1;
        usedBytes = -1;
        storage_detail_apps = -1;
        storage_detail_images = -1;
        storage_detail_videos = -1;
        storage_detail_audio = -1;
        storage_detail_other = -1;
    }
    
    public void setVolumeInfo(long total,long free,long used) {
    	Log.d(TAG, "setVolumeInfo() total = "+total+", free = "+free+", used = "+used);
    	totalBytes = total;
    	freeBytes = free;
    	usedBytes = used;
    }
    
    public void setVolumeDetails(String tilte,long size) {
    	float percentage;
    	Log.d(TAG, "setVolumeDetails() tilte = "+tilte);
    	if(mContext.getResources().getString(R.string.storage_detail_apps).equals(tilte)){
    		storage_detail_apps = size;
    		percentage= (float)storage_detail_apps/totalBytes; 
    	} else if(mContext.getResources().getString(R.string.storage_detail_images).equals(tilte)){
    		storage_detail_images = size;
    		percentage= (float)storage_detail_images/totalBytes;  
    	} else if(mContext.getResources().getString(R.string.storage_detail_videos).equals(tilte)){
    		storage_detail_videos = size;
    		percentage= (float)storage_detail_videos/totalBytes; 
    	} else if(mContext.getResources().getString(R.string.storage_detail_audio).equals(tilte)){
    		storage_detail_audio = size;
    		percentage= (float)storage_detail_audio/totalBytes; 
    	} else if(mContext.getResources().getString(R.string.storage_detail_other).equals(tilte)){
    		storage_detail_other = size;
    		percentage= (float)storage_detail_other/totalBytes; 
    	}

    	if(mContext.getResources().getString(R.string.storage_detail_cached).equals(tilte))
    		return;
    	
    	Log.e(TAG,"setVolumeDetails() storage_detail_apps = "+storage_detail_apps+", storage_detail_images = "+storage_detail_images
        		+", storage_detail_videos = "+storage_detail_videos +", storage_detail_audio = "+storage_detail_audio 
        		+", storage_detail_other = "+storage_detail_other);	
    	if(storage_detail_apps!=-1
			&&storage_detail_images!=-1
			&&storage_detail_videos!=-1
			&&storage_detail_audio!=-1
			&&storage_detail_other!=-1
			&&totalBytes!=-1){
    		
        	mPrizeStorageRectView.setPercentage(R.string.storage_detail_apps,(int) ((storage_detail_apps * 100) / totalBytes));
        	mPrizeStorageRectView.setPercentage(R.string.storage_detail_images,(int) ((storage_detail_images * 100) / totalBytes));
        	mPrizeStorageRectView.setPercentage(R.string.storage_detail_videos,(int) ((storage_detail_videos * 100) / totalBytes));
        	mPrizeStorageRectView.setPercentage(R.string.storage_detail_audio,(int) ((storage_detail_audio * 100) / totalBytes));
        	mPrizeStorageRectView.setPercentage(R.string.storage_detail_other,(int) ((storage_detail_other * 100) / totalBytes));
    		
        	Log.d(TAG,"----------postInvalidate------------");
    		mPrizeStorageRectView.postInvalidate();
    	}
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
    	mPrizeStorageRectView = (PrizeStorageRectView) view.findViewById(R.id.percentage);
        if (mPercent != -1) {
        	mPrizeStorageRectView.setVisibility(View.VISIBLE);
            //progress.setProgress(mPercent);
        } else {
        	mPrizeStorageRectView.setVisibility(View.GONE);
        }
        mPrizeStorageRectView.cleanAll();
      
        Log.e(TAG,"++++++++onBindView++++++++");	
        
        final TextView summary = (TextView) view.findViewById(android.R.id.summary);
        summary.setTextColor(Color.parseColor("#8a000000"));

        super.onBindViewHolder(view);
    }
}
