/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import libcore.icu.LocaleData;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.util.Log;
import android.widget.TextView;

import com.android.systemui.DemoMode;
import com.android.systemui.R;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;

import com.mediatek.systemui.PluginManager;
import com.mediatek.systemui.ext.ISystemUIStatusBarExt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/*PRIZE-import pkg- liufan-2015-05-19-start*/
import com.android.systemui.statusbar.phone.FeatureOption;
import android.util.Log;
/*PRIZE-import pkg- liufan-2015-05-19-end*/
/*PRIZE-add for liuhai screen-liufan-2018-04-09-start*/
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.PrizeFixFontTxtView;
import android.graphics.Typeface;
import android.content.res.Configuration;
/*PRIZE-add for liuhai screen-liufan-2018-04-09-end*/

/**
 * Digital clock for the status bar.
 */
public class LiuHaiClock extends Clock {

    public LiuHaiClock(Context context) {
        this(context, null);
    }

    public LiuHaiClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiuHaiClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN || PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            setTypeface(PrizeFixFontTxtView.getClockTypeface());
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(PhoneStatusBar.OPEN_LIUHAI_SCREEN || PhoneStatusBar.OPEN_LIUHAI_SCREEN2){
            setTypeface(PrizeFixFontTxtView.getClockTypeface());
        }
    }

}

