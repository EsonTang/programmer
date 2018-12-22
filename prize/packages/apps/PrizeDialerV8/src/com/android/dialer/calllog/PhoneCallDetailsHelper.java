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

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.content.ContextCompat;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.PhoneNumberHelper;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.R;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.util.DialerUtils;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.ICallerInfoExt;
import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.CallLogHighlighter;
import com.mediatek.dialer.util.DialerFeatureOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/*PRIZE-add-yuandailin-2016-3-17-start*/
import java.text.SimpleDateFormat;
import java.util.Date;
import android.graphics.Color;
import android.text.format.Time;
import android.util.Log;
//import android.telephony.SubscriptionManager;
/*PRIZE-add-yuandailin-2016-3-17-end*/

/**
 * Helper class to fill in the views in {@link PhoneCallDetailsViews}.
 */
public class PhoneCallDetailsHelper {

    /** The maximum number of icons will be shown to represent the call types in a group. */
    private static final int MAX_CALL_TYPE_ICONS = 1;//PRIZE-change-yuandailin-2016-7-18

    private final Context mContext;
    private final Resources mResources;
    /** The injected current time in milliseconds since the epoch. Used only by tests. */
    private Long mCurrentTimeMillisForTest;

    private CharSequence mPhoneTypeLabelForTest;

    private final CallLogCache mCallLogCache;

    /** Calendar used to construct dates */
    private final Calendar mCalendar;

    /**
     * List of items to be concatenated together for accessibility descriptions
     */
    private ArrayList<CharSequence> mDescriptionItems = Lists.newArrayList();

    /**
     * Creates a new instance of the helper.
     * <p>
     * Generally you should have a single instance of this helper in any context.
     *
     * @param resources used to look up strings
     */
    public PhoneCallDetailsHelper(
            Context context,
            Resources resources,
            CallLogCache callLogCache) {
        mContext = context;
        mResources = resources;

        /// M: [Dialer Global Search] for CallLogSearch @{
        if (DialerFeatureOptions.DIALER_GLOBAL_SEARCH) {
            initHighlighter();
        }
        /// @}
        mCallLogCache = callLogCache;
        mCalendar = Calendar.getInstance();
    }

    /** Fills the call details views with content. */
    public void setPhoneCallDetails(PhoneCallDetailsViews views, PhoneCallDetails details) {
        // Display up to a given number of icons.
        views.callTypeIcons.clear();

        //views.callTypeIcons.setPhoneAccountHandle(details.accountHandle);//PRIZE-remove-yuandailin-2016-9-5
        views.callTypeIcons.setSlotIdAndSimCount(details.slotId, details.mSimCount);//PRIZE-run smoohtlier in callllog -yuandailin-2016-9-21
        int count = details.callTypes.length;
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*boolean isVoicemail = false;*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        for (int index = 0; index < count && index < MAX_CALL_TYPE_ICONS; ++index) {
            views.callTypeIcons.add(details.callTypes[index]);
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*if (index == 0) {
                isVoicemail = details.callTypes[index] == Calls.VOICEMAIL_TYPE;
            }*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        }

        // Show the video icon if the call had video enabled.
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        /*views.callTypeIcons.setShowVideo(
                (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO);*/
        views.callTypeIcons.setShowVideo(false);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        ///M: Plug-in call to show different icons VoLTE, VoWifi, ViWifi in call logs
        ExtensionManager.getInstance().getCallLogExtension().setShowVolteWifi(
                       views.callTypeIcons, details.features);

        views.callTypeIcons.requestLayout();
        views.callTypeIcons.setVisibility(View.VISIBLE);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        views.prize_call_types_icons.setPrimaryCallTypeIcon(false);
        views.prize_call_types_icons.clear();
        views.prize_call_types_icons.setSlotIdAndSimCount(details.slotId, details.mSimCount);
        for (int index = 0; index < count && index < MAX_CALL_TYPE_ICONS; ++index) {
            views.prize_call_types_icons.add(details.callTypes[index]);
        }
        // Show the video icon if the call had video enabled.
        views.prize_call_types_icons.setShowVideo(
                (details.features & Calls.FEATURES_VIDEO) == Calls.FEATURES_VIDEO);
        ///M: Plug-in call to show different icons VoLTE, VoWifi, ViWifi in call logs
        ExtensionManager.getInstance().getCallLogExtension().setShowVolteWifi(
                views.prize_call_types_icons, details.features);
        views.prize_call_types_icons.requestLayout();
        views.prize_call_types_icons.setVisibility(View.VISIBLE);
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        // Show the total call count only if there are more than the maximum number of icons.
        /*PRIZE-change-yuandailin-2016-3-17-start*/
        /*final Integer callCount;
        if (count > MAX_CALL_TYPE_ICONS) {
            callCount = count;
        } else {
            callCount = null;
        }*/
        CharSequence callLocationAndDate = getCallLocationAndDate(details);
        views.callLocation.setText(callLocationAndDate);
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*final Integer callCount = count;
        // Set the call count, location, date and if voicemail, set the duration.
        setDetailText(views, callCount, details);*/
        CharSequence dateText = getCallDate(details);
        if (dateText != null) views.date.setText(dateText);
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        // Set the account label if it exists.
        /*String accountLabel = mCallLogCache.getAccountLabel(details.accountHandle);
        if (!TextUtils.isEmpty(details.viaNumber)) {
            if (!TextUtils.isEmpty(accountLabel)) {
                accountLabel = mResources.getString(R.string.call_log_via_number_phone_account,
                        accountLabel, details.viaNumber);
            } else {
                accountLabel = mResources.getString(R.string.call_log_via_number,
                        details.viaNumber);
            }
        }
        if (!TextUtils.isEmpty(accountLabel)) {
            //views.callAccountLabel.setVisibility(View.VISIBLE);
            views.callAccountLabel.setVisibility(View.GONE);
            views.callAccountLabel.setText(accountLabel);
            *//*int color = mCallLogCache.getAccountColor(details.accountHandle);
            if (color == PhoneAccount.NO_HIGHLIGHT_COLOR) {
                int defaultColor = R.color.dialtacts_secondary_text_color;
                views.callAccountLabel.setTextColor(mContext.getResources().getColor(defaultColor));
            } else {
                views.callAccountLabel.setTextColor(color);
            }*//*
        } else {
            views.callAccountLabel.setVisibility(View.GONE);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        CharSequence nameText;
        final CharSequence displayNumber = details.displayNumber;
        /*PRIZE-add -yuandailin-2016-7-18-start*/
        String countString = "";
        /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-start*/
        /*if (count > 1) {
            countString = "  " + "(" + count + ")";
        }*/
        /*PRIZE-Delete-DialerV8-wangzhong-2017_7_19-end*/
        /*PRIZE-add -yuandailin-2016-7-18-end*/

        if (TextUtils.isEmpty(details.getPreferredName())) {
            //nameText = displayNumber;
            nameText = "";
            // We have a real phone number as "nameView" so make it always LTR
            views.nameView.setTextDirection(View.TEXT_DIRECTION_LTR);
            views.numberView.setText(displayNumber + countString);//PRIZE-change-yuandailin-2016-7-18
        } else {
            //nameText = details.getPreferredName();
            nameText = details.getPreferredName() + countString;/*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24*/
            views.numberView.setText("");
            views.callLocation.setText(displayNumber);//PRIZE-add-yuandailin-2016-7-18
        }

        /// M: [Dialer Global Search]for CallLog Search @{
        if (DialerFeatureOptions.DIALER_GLOBAL_SEARCH && mHighlightString != null
                && mHighlightString.length > 0) {
            boolean onlyNumber = TextUtils.isEmpty(details.getPreferredName());
            nameText = getHightlightedCallLogName(nameText.toString(),
                    mHighlightString, onlyNumber);
        }
        /// @}
        views.nameView.setText(nameText);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        if ((views.callLocation.getText() + "").trim().equals("")) {
            views.callLocation.setText(mContext.getResources().getString(R.string.prize_call_log_list_item_unknown));
        }
        views.nameView.setTextColor(mContext.getResources().getColor(R.color.prize_dialer_call_log_list_item_name_color));
        views.numberView.setTextColor(mContext.getResources().getColor(R.color.prize_dialer_call_log_list_item_name_color));
        if (details.callTypes[0] == com.android.dialer.util.AppCompatConstants.CALLS_MISSED_TYPE
                || details.callTypes[0] == com.android.dialer.util.AppCompatConstants.CALLS_REJECTED_TYPE) {
            views.nameView.setTextColor(mContext.getResources().getColor(R.color.prize_dialer_call_log_list_item_missed_name_color));
            views.numberView.setTextColor(mContext.getResources().getColor(R.color.prize_dialer_call_log_list_item_missed_name_color));
        }
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (isVoicemail) {
            views.voicemailTranscriptionView.setText(TextUtils.isEmpty(details.transcription) ? null
                    : details.transcription);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        // Bold if not read
        /*Typeface typeface = details.isRead ? Typeface.SANS_SERIF : Typeface.DEFAULT_BOLD;
        views.nameView.setTypeface(typeface);
        views.voicemailTranscriptionView.setTypeface(typeface);
        views.callLocationAndDate.setTypeface(typeface);
        views.callLocationAndDate.setTextColor(ContextCompat.getColor(mContext, details.isRead ?
                R.color.call_log_detail_color : R.color.call_log_unread_text_color));*/
        /*PRIZE-change-yuandailin-2016-3-17-end*/
    }

    /**
     * Builds a string containing the call location and date. For voicemail logs only the call date
     * is returned because location information is displayed in the call action button
     *
     * @param details The call details.
     * @return The call location and date string.
     */
    private CharSequence getCallLocationAndDate(PhoneCallDetails details) {
        mDescriptionItems.clear();

        if (details.callTypes[0] != Calls.VOICEMAIL_TYPE) {
            // Get type of call (ie mobile, home, etc) if known, or the caller's location.
            CharSequence callTypeOrLocation = getCallTypeOrLocation(details);

            // Only add the call type or location if its not empty.  It will be empty for unknown
            // callers.
            if (!TextUtils.isEmpty(callTypeOrLocation)) {
                mDescriptionItems.add(callTypeOrLocation);
            }
        }

        // The date of this call
        //mDescriptionItems.add(getCallDate(details));//PRIZE-remove-yuandailin-2016-3-17

        // Create a comma separated list from the call type or location, and call date.
        return DialerUtils.join(mResources, mDescriptionItems);
    }

    /**
     * For a call, if there is an associated contact for the caller, return the known call type
     * (e.g. mobile, home, work).  If there is no associated contact, attempt to use the caller's
     * location if known.
     *
     * @param details Call details to use.
     * @return Type of call (mobile/home) if known, or the location of the caller (if known).
     */
    public CharSequence getCallTypeOrLocation(PhoneCallDetails details) {
        CharSequence numberFormattedLabel = null;
        // Only show a label if the number is shown and it is not a SIP address.
        if (!TextUtils.isEmpty(details.number)
                && !PhoneNumberHelper.isUriNumber(details.number.toString())
                && !mCallLogCache.isVoicemailNumber(details.accountHandle, details.number)) {
            /*PRIZE-remove-yuandailin-2016-3-17-start*/
            //if (TextUtils.isEmpty(details.namePrimary) && !TextUtils.isEmpty(details.geocode)) {
                numberFormattedLabel = details.geocode;
            /*} else if (!(details.numberType == Phone.TYPE_CUSTOM
                    && TextUtils.isEmpty(details.numberLabel))) {
                // Get type label only if it will not be "Custom" because of an empty number label.
                /// M: Using new API for AAS phone number label lookup
                numberFormattedLabel = MoreObjects.firstNonNull(mPhoneTypeLabelForTest,
                        Phone.getTypeLabel(
                                mContext, details.numberType, details.numberLabel));
            }*/
        }

        /*if (!TextUtils.isEmpty(details.namePrimary) && TextUtils.isEmpty(numberFormattedLabel)) {
            numberFormattedLabel = details.displayNumber;
        }*/
        /*PRIZE-remove-yuandailin-2016-3-17-end*/
        return numberFormattedLabel;
    }

    @NeededForTesting
    public void setPhoneTypeLabelForTest(CharSequence phoneTypeLabel) {
        this.mPhoneTypeLabelForTest = phoneTypeLabel;
    }

    /*PRIZE-add-yuandailin-2016-3-17-start*/
    public static String formatTimeStampStringExtend(Context context, long when) {
        Time then = new Time();
        int format_flags =  DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;
        then.set(when);
        Time now = new Time();
        now.setToNow();
        if (then.yearDay == now.yearDay){
            format_flags = DateUtils.FORMAT_SHOW_TIME;
        }else{
            format_flags = DateUtils.FORMAT_SHOW_DATE;
        }
        return DateUtils.formatDateTime(context, when, format_flags);
    }
    /*PRIZE-add-yuandailin-2016-3-17-end*/

    /**
     * Get the call date/time of the call. For the call log this is relative to the current time.
     * e.g. 3 minutes ago. For voicemail, see {@link #getGranularDateTime(PhoneCallDetails)}
     *
     * @param details Call details to use.
     * @return String representing when the call occurred.
     */
    public CharSequence getCallDate(PhoneCallDetails details) {
        if (details.callTypes[0] == Calls.VOICEMAIL_TYPE) {
            return getGranularDateTime(details);
        }

        /*PRIZE-remove-yuandailin-2016-3-17-start*/
        /*return DateUtils.getRelativeTimeSpanString(details.date, getCurrentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);*/
        return formatTimeStampStringExtend(mContext, details.date);
        /*PRIZE-remove-yuandailin-2016-3-17-end*/
    }

    /**
     * Get the granular version of the call date/time of the call. The result is always in the form
     * 'DATE at TIME'. The date value changes based on when the call was created.
     *
     * If created today, DATE is 'Today'
     * If created this year, DATE is 'MMM dd'
     * Otherwise, DATE is 'MMM dd, yyyy'
     *
     * TIME is the localized time format, e.g. 'hh:mm a' or 'HH:mm'
     *
     * @param details Call details to use
     * @return String representing when the call occurred
     */
    public CharSequence getGranularDateTime(PhoneCallDetails details) {
        return mResources.getString(R.string.voicemailCallLogDateTimeFormat,
                getGranularDate(details.date),
                DateUtils.formatDateTime(mContext, details.date, DateUtils.FORMAT_SHOW_TIME));
    }

    /**
     * Get the granular version of the call date. See {@link #getGranularDateTime(PhoneCallDetails)}
     */
    private String getGranularDate(long date) {
        if (DateUtils.isToday(date)) {
            return mResources.getString(R.string.voicemailCallLogToday);
        }
        return DateUtils.formatDateTime(mContext, date, DateUtils.FORMAT_SHOW_DATE
                | DateUtils.FORMAT_ABBREV_MONTH
                | (shouldShowYear(date) ? DateUtils.FORMAT_SHOW_YEAR : DateUtils.FORMAT_NO_YEAR));
    }

    /**
     * Determines whether the year should be shown for the given date
     *
     * @return {@code true} if date is within the current year, {@code false} otherwise
     */
    private boolean shouldShowYear(long date) {
        mCalendar.setTimeInMillis(getCurrentTimeMillis());
        int currentYear = mCalendar.get(Calendar.YEAR);
        mCalendar.setTimeInMillis(date);
        return currentYear != mCalendar.get(Calendar.YEAR);
    }

    /** Sets the text of the header view for the details page of a phone call. */
    @NeededForTesting
    public void setCallDetailsHeader(TextView nameView, PhoneCallDetails details) {
        final CharSequence nameText;
        /*PRIZE-change-yuandailin-2016-3-17-start*/
        if (!TextUtils.isEmpty(details.namePrimary)) {
            nameText = details.namePrimary;
        /*} else if (!TextUtils.isEmpty(details.displayNumber)) {
            nameText = details.displayNumber;*/
        } else {
            //nameText = mResources.getString(R.string.unknown);
            nameText = " ";
        }
        /*PRIZE-change-yuandailin-2016-3-17-end*/
        nameView.setText(nameText);
    }

    @NeededForTesting
    public void setCurrentTimeForTest(long currentTimeMillis) {
        mCurrentTimeMillisForTest = currentTimeMillis;
    }

    /**
     * Returns the current time in milliseconds since the epoch.
     * <p>
     * It can be injected in tests using {@link #setCurrentTimeForTest(long)}.
     */
    private long getCurrentTimeMillis() {
        if (mCurrentTimeMillisForTest == null) {
            return System.currentTimeMillis();
        } else {
            return mCurrentTimeMillisForTest;
        }
    }

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /** Sets the call count, date, and if it is a voicemail, sets the duration. */
    /*private void setDetailText(PhoneCallDetailsViews views, Integer callCount,
                               PhoneCallDetails details) {
        // Combine the count (if present) and the date.
        CharSequence dateText = getCallLocationAndDate(details);
        final CharSequence text;
        if (callCount != null) {
            text = mResources.getString(
                    R.string.call_log_item_count_and_date, callCount.intValue(), dateText);
        } else {
            text = dateText;
        }

        if (details.callTypes[0] == Calls.VOICEMAIL_TYPE && details.duration > 0) {
            views.callLocationAndDate.setText(mResources.getString(
                    R.string.voicemailCallLogDateTimeFormatWithDuration, text,
                    getVoicemailDuration(details)));
        } else {
            views.callLocationAndDate.setText(text);
        }
    }

    private String getVoicemailDuration(PhoneCallDetails details) {
        long minutes = TimeUnit.SECONDS.toMinutes(details.duration);
        long seconds = details.duration - TimeUnit.MINUTES.toSeconds(minutes);
        if (minutes > 99) {
            minutes = 99;
        }
        return mResources.getString(R.string.voicemailDurationFormat, minutes, seconds);
    }*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    /// M: [Dialer Global Search] for CallLog search @{
    private CallLogHighlighter mHighlighter;
    private char[] mHighlightString;

    private void initHighlighter() {
        mHighlighter = new CallLogHighlighter(Color.GREEN);
    }

    public void setHighlightedText(char[] highlightedText) {
        mHighlightString = highlightedText;
    }

    private String getHightlightedCallLogName(String text, char[] highlightText,
            boolean isOnlyNumber) {
        String name = text;
        if (isOnlyNumber) {
            name = mHighlighter.applyNumber(text, highlightText).toString();
        } else {
            name = mHighlighter.applyName(text, highlightText).toString();
        }
        return name;
    }
    /// @}
}
