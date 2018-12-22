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

package com.android.settings.notification;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.RestrictedCheckBox;
import com.android.settings.RestrictedRadioButton;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;

import static android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS;
import static android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class RedactionInterstitial extends SettingsActivity {

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, RedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return RedactionInterstitialFragment.class.getName().equals(fragmentName);
    }

    /**
     * Create an intent for launching RedactionInterstitial.
     * @return An intent to launch the activity is if is available, @null if the activity is not
     * available to be launched.
     */
    public static Intent createStartIntent(Context ctx, int userId) {
            return new Intent(ctx, RedactionInterstitial.class)
                    /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-start*/
                    /*.putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true)*/
                    .putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, false)
                    /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-end*/
                    .putExtra(EXTRA_PREFS_SET_BACK_TEXT, (String) null)
                    .putExtra(EXTRA_PREFS_SET_NEXT_TEXT, ctx.getString(
                            R.string.app_notifications_dialog_done))
                    .putExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID,
                        Utils.isManagedProfile(UserManager.get(ctx), userId)
                            ? R.string.lock_screen_notifications_interstitial_title_profile
                            : R.string.lock_screen_notifications_interstitial_title)
                .putExtra(Intent.EXTRA_USER_ID, userId);
    }

    public static class RedactionInterstitialFragment extends SettingsPreferenceFragment
            implements RadioGroup.OnCheckedChangeListener {

        private RadioGroup mRadioGroup;
        private RestrictedRadioButton mShowAllButton;
        private RestrictedRadioButton mRedactSensitiveButton;
        private int mUserId;

        @Override
        protected int getMetricsCategory() {
            return MetricsEvent.NOTIFICATION_REDACTION;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.redaction_interstitial, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
            mShowAllButton = (RestrictedRadioButton) view.findViewById(R.id.show_all);
            mRedactSensitiveButton =
                    (RestrictedRadioButton) view.findViewById(R.id.redact_sensitive);

            mRadioGroup.setOnCheckedChangeListener(this);
            mUserId = Utils.getUserIdFromBundle(
                    getContext(), getActivity().getIntent().getExtras());
            if (Utils.isManagedProfile(UserManager.get(getContext()), mUserId)) {
                ((TextView) view.findViewById(R.id.message))
                    .setText(R.string.lock_screen_notifications_interstitial_message_profile);
                mShowAllButton.setText(R.string.lock_screen_notifications_summary_show_profile);
                mRedactSensitiveButton
                    .setText(R.string.lock_screen_notifications_summary_hide_profile);
                ((RadioButton) view.findViewById(R.id.hide_all))
                    .setText(R.string.lock_screen_notifications_summary_disable_profile);
            }

			 /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
            android.widget.Button completed_button = (android.widget.Button) view.findViewById(R.id.completed_button);
            completed_button.setOnClickListener(new android.view.View.OnClickListener() {

                public void onClick(View v) {
                    finish();
                }
            });
            /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        }

        @Override
        public void onResume() {
            super.onResume();
            // Disable buttons according to policy.

            checkNotificationFeaturesAndSetDisabled(mShowAllButton,
                    KEYGUARD_DISABLE_SECURE_NOTIFICATIONS |
                    KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS);
            checkNotificationFeaturesAndSetDisabled(mRedactSensitiveButton,
                    KEYGUARD_DISABLE_SECURE_NOTIFICATIONS);
            loadFromSettings();
        }

        private void checkNotificationFeaturesAndSetDisabled(RestrictedRadioButton button,
                int keyguardNotifications) {
            EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(
                    getActivity(), keyguardNotifications, mUserId);
            button.setDisabledByAdmin(admin);
        }

        private void loadFromSettings() {
            final boolean enabled = Settings.Secure.getIntForUser(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, 0, mUserId) != 0;
            final boolean show = Settings.Secure.getIntForUser(getContentResolver(),
                        Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, 1, mUserId) != 0;

            int checkedButtonId = R.id.hide_all;
            if (enabled) {
                if (show && !mShowAllButton.isDisabledByAdmin()) {
                    checkedButtonId = R.id.show_all;
                } else if (!mRedactSensitiveButton.isDisabledByAdmin()) {
                    checkedButtonId = R.id.redact_sensitive;
                }
            }

            mRadioGroup.check(checkedButtonId);
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            final boolean show = (checkedId == R.id.show_all);
            final boolean enabled = (checkedId != R.id.hide_all);

            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, show ? 1 : 0, mUserId);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, enabled ? 1 : 0, mUserId);

        }
    }
}
