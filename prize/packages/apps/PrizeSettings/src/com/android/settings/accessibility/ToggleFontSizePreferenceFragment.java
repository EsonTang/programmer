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

package com.android.settings.accessibility;

import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.R;

/**
 * Preference fragment used to control font size.
 */
public class ToggleFontSizePreferenceFragment extends PreviewSeekBarPreferenceFragment {
    private final static String TAG = "ToggleFontSize";
    private float[] mValues;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivityLayoutResId = R.layout.font_size_activity;
        mPreviewSampleResIds = new int[]{R.layout.font_size_preview};

        Resources res = getContext().getResources();
        final ContentResolver resolver = getContext().getContentResolver();
        // Mark the appropriate item in the preferences list.
        /* prize-modify-by-lijimeng-for fontsize-20180523-start*/
        mEntries = res.getStringArray(R.array.custom_entries_font_size);
        final String[] strEntryValues = res.getStringArray(R.array.custom_entryvalues_font_size);
         /* prize-modify-by-lijimeng-for fontsize-20180523-end*/
        final float currentScale =
                Settings.System.getFloat(resolver, Settings.System.FONT_SCALE, 1.0f);
        mInitialIndex = fontSizeValueToIndex(currentScale, strEntryValues);
        mValues = new float[strEntryValues.length];
        for (int i = 0; i < strEntryValues.length; ++i) {
            mValues[i] = Float.parseFloat(strEntryValues[i]);
        }
    }

    @Override
    protected Configuration createConfig(Configuration origConfig, int index) {
        // Populate the sample layouts.
        final Configuration config = new Configuration(origConfig);
        config.fontScale = mValues[index];
        return config;
    }

    /**
     * Persists the selected font size.
     */
    @Override
    protected void commit() {
        if (getContext() == null) return;
        final ContentResolver resolver = getContext().getContentResolver();
        /* prize-add-by-lijimeng-for fontsize-20180522-start*/
        if(mValues[mCurrentIndex] == 1.30001f){
            SystemProperties.set("persist.sys.prize.fontsize","true");
            Toast.makeText(getContext(),R.string.prize_large_font_prompt,Toast.LENGTH_SHORT).show();
        }else{
            SystemProperties.set("persist.sys.prize.fontsize","false");
        }
        Log.d(TAG,"from settings fontsize values "+mValues[mCurrentIndex]);
        /* prize-add-by-lijimeng-for fontsize-20180522-end*/
        Settings.System.putFloat(resolver, Settings.System.FONT_SCALE, mValues[mCurrentIndex]);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.ACCESSIBILITY_FONT_SIZE;
    }

    /**
     *  Utility function that returns the index in a string array with which the represented value is
     *  the closest to a given float value.
     */
    public static int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }

}
