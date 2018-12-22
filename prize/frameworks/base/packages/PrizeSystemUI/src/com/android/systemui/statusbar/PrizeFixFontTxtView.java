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

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.android.systemui.statusbar.phone.FeatureOption;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.content.res.Resources;
import android.graphics.Typeface;

import com.android.systemui.R;

public class PrizeFixFontTxtView extends TextView {

    private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/Roboto-Medium.ttf";
    private static Typeface mClockTypeface = null;

    public PrizeFixFontTxtView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setTypeface(getClockTypeface());
    }

    public static Typeface getClockTypeface() {
        if (mClockTypeface == null) {
            mClockTypeface = Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE);
        }
        return mClockTypeface;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setTypeface(getClockTypeface());
    }
}
