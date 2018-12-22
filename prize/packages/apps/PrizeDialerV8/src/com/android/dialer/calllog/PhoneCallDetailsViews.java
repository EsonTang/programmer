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

package com.android.dialer.calllog;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dialer.R;

/**
 * Encapsulates the views that are used to display the details of a phone call in the call log.
 */
public final class PhoneCallDetailsViews {
    public final TextView nameView;
    /*PRIZE-change-yuandailin-2016-3-17-start*/
    //public final View callTypeView;
    public final CallTypeIconsView callTypeIcons;
    //public final TextView callLocationAndDate;
    public final TextView numberView;
    public final TextView callLocation;
    public final TextView date;
    /*PRIZE-change-yuandailin-2016-3-17-end*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*public final TextView voicemailTranscriptionView;
    public final TextView callAccountLabel;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
    public final TextView prize_call_mark_tag;
    /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    public final CallTypeIconsView prize_call_types_icons;
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    private PhoneCallDetailsViews(TextView nameView, /*View callTypeView*/TextView numberView,
            CallTypeIconsView callTypeIcons, /*TextView callLocationAndDate*/TextView callLocation, TextView date,
            /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
            TextView prize_call_mark_tag,
            /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            CallTypeIconsView prize_call_types_icons) {
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*TextView voicemailTranscriptionView, TextView callAccountLabel) {*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        this.nameView = nameView;
        /*PRIZE-change-yuandailin-2016-3-17-start*/
        this.numberView = numberView;
        //this.callTypeView = callTypeView;
        this.callTypeIcons = callTypeIcons;
        //this.callLocationAndDate = callLocationAndDate;
        this.callLocation = callLocation;
        this.date = date;
        /*PRIZE-change-yuandailin-2016-3-17-end*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*this.voicemailTranscriptionView = voicemailTranscriptionView;
        this.callAccountLabel = callAccountLabel;*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
        this.prize_call_mark_tag = prize_call_mark_tag;
        /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        this.prize_call_types_icons = prize_call_types_icons;
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
    }

    /**
     * Create a new instance by extracting the elements from the given view.
     * <p>
     * The view should contain three text views with identifiers {@code R.id.name},
     * {@code R.id.date}, and {@code R.id.number}, and a linear layout with identifier
     * {@code R.id.call_types}.
     */
    public static PhoneCallDetailsViews fromView(View view) {
        return new PhoneCallDetailsViews((TextView) view.findViewById(R.id.name),
                /*PRIZE-change-yuandailin-2016-3-17-start*/
                //view.findViewById(R.id.call_type),
                (TextView) view.findViewById(R.id.item_number),
                (CallTypeIconsView) view.findViewById(R.id.call_type_icons),
                //(TextView) view.findViewById(R.id.call_location_and_date),
                (TextView) view.findViewById(R.id.call_location),
                (TextView) view.findViewById(R.id.call_date),

                /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
                (TextView) view.findViewById(R.id.prize_call_mark_tag),
                /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                (CallTypeIconsView) view.findViewById(R.id.prize_call_types_icons));
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*(TextView) view.findViewById(R.id.voicemail_transcription),
                (TextView) view.findViewById(R.id.call_account_label));*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                /*PRIZE-change-yuandailin-2016-3-17-end*/
    }

    public static PhoneCallDetailsViews createForTest(Context context) {
        return new PhoneCallDetailsViews(
                new TextView(context),
                /*PRIZE-change-yuandailin-2016-3-17-start*/					
                //new View(context),
                new TextView(context),
                new CallTypeIconsView(context),
                new TextView(context),
                new TextView(context),

                /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-start*/
                new TextView(context),
                /*PRIZE-Add-TMSDK_Call_Mark-wangzhong-2017_5_5-end*/
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                new CallTypeIconsView(context));
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*new TextView(context),
                new TextView(context));*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                /*PRIZE-change-yuandailin-2016-3-17-end*/					
    }
}
