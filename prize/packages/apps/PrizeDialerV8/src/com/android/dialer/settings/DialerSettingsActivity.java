/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.dialer.settings;

import com.google.common.collect.Lists;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.os.BuildCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.TelephonyManagerCompat;
import com.android.dialer.R;
import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.compat.SettingsCompat;
import com.android.dialer.compat.UserManagerCompat;

import com.mediatek.dialer.ext.ExtensionManager;

import java.util.List;

public class DialerSettingsActivity extends AppCompatPreferenceActivity {
    protected SharedPreferences mPreferences;
    private boolean migrationStatusOnBuildHeaders;
    private HeaderAdapter mHeaderAdapter;//prize-add-huangliemin-2016-6-15

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        getActionBar().setElevation(this.getResources().getDimensionPixelOffset(R.dimen.prize_elevation_top));
        getListView().setBackgroundColor(getResources().getColor(R.color.prize_preferences_lowest_bg));
        getListView().setDivider(null);
        android.graphics.drawable.ColorDrawable drawable = new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT);
        getListView().setSelector(drawable);
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * The headers need to be recreated if the migration status changed between when the headers
         * were created and now.
         */
        if (migrationStatusOnBuildHeaders != FilteredNumberCompat.hasMigratedToNewBlocking()) {
            invalidateHeaders();
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        /// M: [ALPS02841164] Disable display order for CJK locales as well
        /*PRIZE-change-yuandailin-2016-6-2-start*/
        /*if (getResources().getBoolean(R.bool.config_sort_order_user_changeable)) {
            Header displayOptionsHeader = new Header();
            displayOptionsHeader.titleRes = R.string.display_options_title;
            displayOptionsHeader.fragment = DisplayOptionsSettingsFragment.class.getName();
            target.add(displayOptionsHeader);
        }

        Header soundSettingsHeader = new Header();
        soundSettingsHeader.titleRes = R.string.sounds_and_vibration_title;
        soundSettingsHeader.fragment = SoundSettingsFragment.class.getName();
        soundSettingsHeader.id = R.id.settings_header_sounds_and_vibration;
        target.add(soundSettingsHeader);

        if (CompatUtils.isMarshmallowCompatible()) {
            Header quickResponseSettingsHeader = new Header();
            Intent quickResponseSettingsIntent =
                    new Intent(TelecomManager.ACTION_SHOW_RESPOND_VIA_SMS_SETTINGS);
            quickResponseSettingsHeader.titleRes = R.string.respond_via_sms_setting_title;
            quickResponseSettingsHeader.intent = quickResponseSettingsIntent;
            target.add(quickResponseSettingsHeader);
        }*/

        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        Header prizeHeader = new Header();
        prizeHeader.titleRes = R.string.activity_title_settings;
        target.add(prizeHeader);
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

        final Header generalSettingsHeader = new Header();
        generalSettingsHeader.titleRes = R.string.general_settings_label;
        generalSettingsHeader.fragment = PrizeGeneralSettingsFragment.class.getName();
        target.add(generalSettingsHeader);

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // "Call Settings" (full settings) is shown if the current user is primary user and there
        // is only one SIM. Before N, "Calling accounts" setting is shown if the current user is
        // primary user and there are multiple SIMs. In N+, "Calling accounts" is shown whenever
        // "Call Settings" is not shown.
        boolean isPrimaryUser = isPrimaryUser();
        if (isPrimaryUser
                && TelephonyManagerCompat.getPhoneCount(telephonyManager) <= 1) {
            Header callSettingsHeader = new Header();
            Intent callSettingsIntent = new Intent(TelecomManager.ACTION_SHOW_CALL_SETTINGS);
            callSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            callSettingsHeader.titleRes = R.string.call_settings_label;
            callSettingsHeader.intent = callSettingsIntent;
            target.add(callSettingsHeader);
        } else if (BuildCompat.isAtLeastN() || isPrimaryUser) {
            Header phoneAccountSettingsHeader = new Header();
            Intent phoneAccountSettingsIntent =
                    new Intent(TelecomManager.ACTION_CHANGE_PHONE_ACCOUNTS);
            phoneAccountSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            phoneAccountSettingsHeader.titleRes = R.string.phone_account_settings_label;
            phoneAccountSettingsHeader.intent = phoneAccountSettingsIntent;
            target.add(phoneAccountSettingsHeader);
        }
        
        /*prize-remove-huangpengfei-2016-11-15-start*/
        /*if (FilteredNumberCompat.canCurrentUserOpenBlockSettings(this) &&
            /// M: For OP01, do not use BlockedNumberProvider @{
            ExtensionManager.getInstance().getCallLogExtension()
                .shouldUseBlockedNumberFeature()) {
            /// @}
            Header blockedCallsHeader = new Header();
            blockedCallsHeader.titleRes = R.string.manage_blocked_numbers_label;
            blockedCallsHeader.intent = FilteredNumberCompat.createManageBlockedNumbersIntent(this);
            target.add(blockedCallsHeader);
            migrationStatusOnBuildHeaders = FilteredNumberCompat.hasMigratedToNewBlocking();
        }*/
        /*prize-remove-huangpengfei-2016-11-15-end*/
        
        /*if (isPrimaryUser
                && (TelephonyManagerCompat.isTtyModeSupported(telephonyManager)
                || TelephonyManagerCompat.isHearingAidCompatibilitySupported(telephonyManager))) {
            Header accessibilitySettingsHeader = new Header();
            Intent accessibilitySettingsIntent =
                    new Intent(TelecomManager.ACTION_SHOW_CALL_ACCESSIBILITY_SETTINGS);
            accessibilitySettingsHeader.titleRes = R.string.accessibility_settings_title;
            accessibilitySettingsHeader.intent = accessibilitySettingsIntent;
            target.add(accessibilitySettingsHeader);
        }*/
    }

    /**
    * Returns {@code true} or {@code false} based on whether the display options setting should be
    * shown. For languages such as Chinese, Japanese, or Korean, display options aren't useful
    * since contacts are sorted and displayed family name first by default.
    *
    * @return {@code true} if the display options should be shown, {@code false} otherwise.
    */
    private boolean showDisplayOptions() {
        return getResources().getBoolean(R.bool.config_display_order_user_changeable)
                && getResources().getBoolean(R.bool.config_sort_order_user_changeable);
    }

    /*@Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.settings_header_sounds_and_vibration) {
            // If we don't have the permission to write to system settings, go to system sound
            // settings instead. Otherwise, perform the super implementation (which launches our
            // own preference fragment.
            if (!SettingsCompat.System.canWrite(this)) {
                Toast.makeText(
                        this,
                        getResources().getString(R.string.toast_cannot_write_system_settings),
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
                return;
            }
        }
        super.onHeaderClick(header, position);
    }*/
    /*PRIZE-change-yuandailin-2016-6-2-end*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!isSafeToCommitTransactions()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    /*prize-add-huangliemin-2016-6-15-start*/
    @Override
    public void setListAdapter(ListAdapter adapter) {
        if (adapter == null) {
            super.setListAdapter(null);
        } else {
            // We don't have access to the hidden getHeaders() method, so grab the headers from
            // the intended adapter and then replace it with our own.
            int headerCount = adapter.getCount();
            List<Header> headers = Lists.newArrayList();
            for (int i = 0; i < headerCount; i++) {
                headers.add((Header) adapter.getItem(i));
            }
            mHeaderAdapter = new HeaderAdapter(this, headers);
            super.setListAdapter(mHeaderAdapter);
        }
    }
    /*prize-add-huangliemin-2016-6-15-end*/

    /**
     * @return Whether the current user is the primary user.
     */
    private boolean isPrimaryUser() {
        return UserManagerCompat.isSystemUser((UserManager) getSystemService(Context.USER_SERVICE));
    }

    /*prize-add-huangliemin-2016-6-15-start*/
    /**
     * This custom {@code ArrayAdapter} is mostly identical to the equivalent one in
     * {@code PreferenceActivity}, except with a local layout resource.
     */
    private static class HeaderAdapter extends ArrayAdapter<Header> {
        static class HeaderViewHolder {
            TextView title;
            TextView summary;
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            TextView preference_category;
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        }

        private LayoutInflater mInflater;

        public HeaderAdapter(Context context, List<Header> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            View view;

            if (convertView == null) {
                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
                /*view = mInflater.inflate(R.layout.dialer_preferences, parent, false);*/
                view = mInflater.inflate(R.layout.prize_dialer_preferences, parent, false);
                /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
                holder = new HeaderViewHolder();
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                holder.preference_category = (TextView) view.findViewById(R.id.preference_category);
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
                holder.title = (TextView) view.findViewById(R.id.title);
                holder.summary = (TextView) view.findViewById(R.id.summary);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (HeaderViewHolder) view.getTag();
            }

            // All view fields must be updated every time, because the view may be recycled
            Header header = getItem(position);
            holder.title.setText(header.getTitle(getContext().getResources()));
            CharSequence summary = header.getSummary(getContext().getResources());
            if (!TextUtils.isEmpty(summary)) {
                holder.summary.setVisibility(View.VISIBLE);
                holder.summary.setText(summary);
            } else {
                holder.summary.setVisibility(View.GONE);
            }
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            if (0 == position) {
                view.setBackgroundDrawable(mInflater.getContext().getResources().getDrawable(R.drawable.prize_preferences_category_bg_selector));
                //holder.title.setTextColor(mInflater.getContext().getResources().getColor(R.color.prize_preferences_text_color));
                android.widget.AbsListView.LayoutParams absListParams =(android.widget.AbsListView.LayoutParams) view.getLayoutParams();
                absListParams.height = mInflater.getContext().getResources().getDimensionPixelOffset(R.dimen.prize_preferences_bg_height3);
                view.setLayoutParams(absListParams);

                holder.preference_category.setText(header.getTitle(getContext().getResources()));
                holder.preference_category.setVisibility(View.VISIBLE);
                holder.title.setVisibility(View.GONE);
            } else if (position == getCount() - 1) {
                view.setBackgroundDrawable(mInflater.getContext().getResources().getDrawable(R.drawable.prize_preferences_bottom_bg_selector));
                holder.title.setVisibility(View.VISIBLE);
            } else {
                view.setBackgroundDrawable(mInflater.getContext().getResources().getDrawable(R.drawable.prize_preferences_mid_bg_selector));
                holder.title.setVisibility(View.VISIBLE);
            }
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
            return view;
        }
    }
    /*prize-add-huangliemin-2016-6-15-end*/
}
