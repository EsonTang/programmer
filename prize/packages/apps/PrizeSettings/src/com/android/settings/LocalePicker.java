/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.android.settings.SettingsPreferenceFragment.SettingsDialogFragment;
import java.util.Locale;
import android.widget.FrameLayout;

public class LocalePicker extends com.android.internal.app.LocalePicker
        implements com.android.internal.app.LocalePicker.LocaleSelectionListener,
        DialogCreatable {

    private static final String TAG = "LocalePicker";

    private SettingsDialogFragment mDialogFragment;
    private static final int DLG_SHOW_GLOBAL_WARNING = 1;
    private static final String SAVE_TARGET_LOCALE = "locale";

    private Locale mTargetLocale;

    public LocalePicker() {
        super();
        setLocaleSelectionListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(SAVE_TARGET_LOCALE)) {
            mTargetLocale = new Locale(savedInstanceState.getString(SAVE_TARGET_LOCALE));
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        final ListView list = (ListView) view.findViewById(android.R.id.list);

        /* Add by zhudaopeng at 2016-12-12 Start */
        list.setDivider(list.getContext().getResources().getDrawable(R.drawable.list_divider, null));
        list.setDividerHeight(1);
        /* Add by zhudaopeng at 2016-12-12 End */
		setMargins(list,inflater);
		container.setBackgroundResource(R.color.settings_layout_background);
        Utils.forcePrepareCustomPreferencesList(container, view, list, false);
        return view;
    }

    @Override
    public void onLocaleSelected(final Locale locale) {
        if (Utils.hasMultipleUsers(getActivity())) {
            mTargetLocale = locale;
            showDialog(DLG_SHOW_GLOBAL_WARNING);
        } else {
            getActivity().onBackPressed();
            LocalePicker.updateLocale(locale);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mTargetLocale != null) {
            outState.putString(SAVE_TARGET_LOCALE, mTargetLocale.toString());
        }
    }

    protected void showDialog(int dialogId) {
        if (mDialogFragment != null) {
            Log.e(TAG, "Old dialog fragment not null!");
        }
        mDialogFragment = new SettingsDialogFragment(this, dialogId);
        mDialogFragment.show(getActivity().getFragmentManager(), Integer.toString(dialogId));
    }

    public Dialog onCreateDialog(final int dialogId) {
        return Utils.buildGlobalChangeWarningDialog(getActivity(),
                R.string.global_locale_change_title,
                new Runnable() {
                    public void run() {
                        removeDialog(dialogId);
                        getActivity().onBackPressed();
                        LocalePicker.updateLocale(mTargetLocale);
                    }
                }
        );
    }

    protected void removeDialog(int dialogId) {
        // mDialogFragment may not be visible yet in parent fragment's onResume().
        // To be able to dismiss dialog at that time, don't check
        // mDialogFragment.isVisible().
        if (mDialogFragment != null && mDialogFragment.getDialogId() == dialogId) {
            mDialogFragment.dismiss();
        }
        mDialogFragment = null;
    }
	
	public void setMargins(View view,LayoutInflater inflater){
		if(view.getLayoutParams() instanceof FrameLayout.LayoutParams){
			int left = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_maginleft);
			int top = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_magintop);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(left,top,left,top);
            view.setLayoutParams(layoutParams);
			view.setBackgroundResource(R.drawable.toponelistpreferencecategory_selector);
		}	
    }
}
