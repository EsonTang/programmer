/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settingslib.RestrictedLockUtils;

import java.util.List;
import android.service.persistentdata.PersistentDataBlockManager;
import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

/**
 * Confirm and execute a reset of the device to a clean "just out of the box"
 * state.  Multiple confirmations are required: first, a general "are you sure
 * you want to do this?" prompt, followed by a keyguard pattern trace if the user
 * has defined one, followed by a final strongly-worded "THIS WILL ERASE EVERYTHING
 * ON THE PHONE" prompt.  If at any time the phone is allowed to go to sleep, is
 * locked, et cetera, then the confirmation sequence is abandoned.
 *
 * This is the initial screen.
 */
public class MasterClear extends OptionsMenuFragment {
    private static final String TAG = "MasterClear";

    private static final int KEYGUARD_REQUEST = 55;

    static final String ERASE_EXTERNAL_EXTRA = "erase_sd";

    private View mContentView;
    private Button mInitiateButton;
    private View mExternalStorageContainer;
    private CheckBox mExternalStorage;
    private  CheckBox mPrizeExternalStorage;
    private AlertDialog mAlertDialog;
    private boolean isVisibleSdcard = false;
    private PowerManager pm;
    private ProgressDialog mProgressDialog;
    /**
     * Keyguard validation is run using the standard {@link ConfirmLockPattern}
     * component as a subactivity
     * @param request the request code to be returned once confirmation finishes
     * @return true if confirmation launched
     */
    private boolean runKeyguardConfirmation(int request) {
        Resources res = getActivity().getResources();
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(
                request, res.getText(R.string.master_clear_title));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != KEYGUARD_REQUEST) {
            return;
        }

        // If the user entered a valid keyguard trace, present the final
        // confirmation prompt; otherwise, go back to the initial state.
        if (resultCode == Activity.RESULT_OK) {
          //  showFinalConfirmation();
                showDialog(getActivity());
        } else {
            establishInitialState();
            finish();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         /* prize-modify-by-lijimeng-for bugid 50023-20180302-start*/
        pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
         /* prize-modify-by-lijimeng-for bugid 50023-20180302-end*/

    }
    private void showFinalConfirmation() {
        Bundle args = new Bundle();
        args.putBoolean(ERASE_EXTERNAL_EXTRA, mExternalStorage.isChecked());
        ((SettingsActivity) getActivity()).startPreferencePanel(MasterClearConfirm.class.getName(),
                args, R.string.master_clear_confirm_title, null, null, 0);
    }

    /**
     * If the user clicks to begin the reset sequence, we next require a
     * keyguard confirmation if the user has currently enabled one.  If there
     * is no keyguard available, we simply go to the final confirmation prompt.
     */
    private final Button.OnClickListener mInitiateListener = new Button.OnClickListener() {

        public void onClick(View v) {
            if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
                showFinalConfirmation();
            }
        }
    };

    /**
     * In its initial state, the activity presents a button for the user to
     * click in order to initiate a confirmation sequence.  This method is
     * called from various other points in the code to reset the activity to
     * this base state.
     *
     * <p>Reinflating views from resources is expensive and prevents us from
     * caching widget pointers, so we use a single-inflate pattern:  we lazy-
     * inflate each view, caching all of the widget pointers we'll need at the
     * time, then simply reuse the inflated views directly whenever we need
     * to change contents.
     */
    private void establishInitialState() {
        mInitiateButton = (Button) mContentView.findViewById(R.id.initiate_master_clear);
        mInitiateButton.setOnClickListener(mInitiateListener);
        mExternalStorageContainer = mContentView.findViewById(R.id.erase_external_container);
        mExternalStorage = (CheckBox) mContentView.findViewById(R.id.erase_external);

        /*
         * If the external storage is emulated, it will be erased with a factory
         * reset at any rate. There is no need to have a separate option until
         * we have a factory reset that only erases some directories and not
         * others. Likewise, if it's non-removable storage, it could potentially have been
         * encrypted, and will also need to be wiped.
         */
        boolean isExtStorageEmulated = Environment.isExternalStorageEmulated();
        if (isExtStorageEmulated
                || (!Environment.isExternalStorageRemovable() && isExtStorageEncrypted())) {
            mExternalStorageContainer.setVisibility(View.GONE);

            final View externalOption = mContentView.findViewById(R.id.erase_external_option_text);
            externalOption.setVisibility(View.GONE);

            final View externalAlsoErased = mContentView.findViewById(R.id.also_erases_external);
            externalAlsoErased.setVisibility(View.VISIBLE);

            // If it's not emulated, it is on a separate partition but it means we're doing
            // a force wipe due to encryption.
            mExternalStorage.setChecked(!isExtStorageEmulated);
            isVisibleSdcard = false;
        } else {
            isVisibleSdcard = true;
            mExternalStorageContainer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mExternalStorage.toggle();
                }
            });
        }

        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        loadAccountList(um);
        StringBuffer contentDescription = new StringBuffer();
        View masterClearContainer = mContentView.findViewById(R.id.master_clear_container);
        getContentDescription(masterClearContainer, contentDescription);
        masterClearContainer.setContentDescription(contentDescription);
    }

    private void getContentDescription(View v, StringBuffer description) {
       if (v.getVisibility() != View.VISIBLE) {
           return;
       }
       if (v instanceof ViewGroup) {
           ViewGroup vGroup = (ViewGroup) v;
           for (int i = 0; i < vGroup.getChildCount(); i++) {
               View nextChild = vGroup.getChildAt(i);
               getContentDescription(nextChild, description);
           }
       } else if (v instanceof TextView) {
           TextView vText = (TextView) v;
           description.append(vText.getText());
           description.append(","); // Allow Talkback to pause between sections.
       }
    }

    private boolean isExtStorageEncrypted() {
        String state = SystemProperties.get("vold.decrypt");
        return !"".equals(state);
    }

    private void loadAccountList(final UserManager um) {
        View accountsLabel = mContentView.findViewById(R.id.accounts_label);
        LinearLayout contents = (LinearLayout)mContentView.findViewById(R.id.accounts);
        contents.removeAllViews();

        Context context = getActivity();
        final List<UserInfo> profiles = um.getProfiles(UserHandle.myUserId());
        final int profilesSize = profiles.size();

        AccountManager mgr = AccountManager.get(context);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        int accountsCount = 0;
        for (int profileIndex = 0; profileIndex < profilesSize; profileIndex++) {
            final UserInfo userInfo = profiles.get(profileIndex);
            final int profileId = userInfo.id;
            final UserHandle userHandle = new UserHandle(profileId);
            Account[] accounts = mgr.getAccountsAsUser(profileId);
            final int N = accounts.length;
            if (N == 0) {
                continue;
            }
            accountsCount += N;

            AuthenticatorDescription[] descs = AccountManager.get(context)
                    .getAuthenticatorTypesAsUser(profileId);
            final int M = descs.length;

            View titleView = Utils.inflateCategoryHeader(inflater, contents);
            final TextView titleText = (TextView) titleView.findViewById(android.R.id.title);
            titleText.setText(userInfo.isManagedProfile() ? R.string.category_work
                    : R.string.category_personal);
            contents.addView(titleView);

            for (int i = 0; i < N; i++) {
                Account account = accounts[i];
                AuthenticatorDescription desc = null;
                for (int j = 0; j < M; j++) {
                    if (account.type.equals(descs[j].type)) {
                        desc = descs[j];
                        break;
                    }
                }
                if (desc == null) {
                    Log.w(TAG, "No descriptor for account name=" + account.name
                            + " type=" + account.type);
                    continue;
                }
                Drawable icon = null;
                try {
                    if (desc.iconId != 0) {
                        Context authContext = context.createPackageContextAsUser(desc.packageName,
                                0, userHandle);
                        icon = context.getPackageManager().getUserBadgedIcon(
                                authContext.getDrawable(desc.iconId), userHandle);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Log.w(TAG, "Bad package name for account type " + desc.type);
                } catch (Resources.NotFoundException e) {
                    Log.w(TAG, "Invalid icon id for account type " + desc.type, e);
                }
                if (icon == null) {
                    icon = context.getPackageManager().getDefaultActivityIcon();
                }

                TextView child = (TextView)inflater.inflate(R.layout.master_clear_account,
                        contents, false);
                child.setText(account.name);
                child.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
                contents.addView(child);
            }
        }

        if (accountsCount > 0) {
            accountsLabel.setVisibility(View.VISIBLE);
            contents.setVisibility(View.VISIBLE);
        }
        // Checking for all other users and their profiles if any.
        View otherUsers = mContentView.findViewById(R.id.other_users_present);
        final boolean hasOtherUsers = (um.getUserCount() - profilesSize) > 0;
        otherUsers.setVisibility(hasOtherUsers ? View.VISIBLE : View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(
                getActivity(), UserManager.DISALLOW_FACTORY_RESET, UserHandle.myUserId());
        final UserManager um = UserManager.get(getActivity());
        if (!um.isAdminUser() || RestrictedLockUtils.hasBaseUserRestriction(getActivity(),
                UserManager.DISALLOW_FACTORY_RESET, UserHandle.myUserId())) {
            return inflater.inflate(R.layout.master_clear_disallowed_screen, null);
        } else if (admin != null) {
            View view = inflater.inflate(R.layout.admin_support_details_empty_view, null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(View.VISIBLE);
            return view;
        }

        mContentView = inflater.inflate(R.layout.master_clear, null);
		setMargins(mContentView, inflater);
		container.setBackgroundResource(R.color.settings_layout_background);
        establishInitialState();
        if (!runKeyguardConfirmation(KEYGUARD_REQUEST)) {
            showDialog(getActivity());
        }
        return null;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.MASTER_CLEAR;
    }
	
	public void setMargins(View view,LayoutInflater inflater){
			int left = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_maginleft);
			int top = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.prize_preferencefragment_card_magintop);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(left,top,left,top);
            view.setLayoutParams(layoutParams);
			view.setBackgroundResource(R.drawable.toponepreferencecategory_selector);
    }
	private void showDialog(Context context){
       AlertDialog.Builder builder = new AlertDialog.Builder(context);
       View view = LayoutInflater.from(context).inflate(R.layout.prize_dialog_content, null);
       TextView textView = (TextView)view.findViewById(R.id.prompt);
       String prompt = context.getResources().getString(R.string.master_clear_desc);
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.erase_sdcard);
        mPrizeExternalStorage = (CheckBox)linearLayout.findViewById(R.id.prize_erase_external);
        textView.setText(prompt);
        if(isVisibleSdcard){
            linearLayout.setVisibility(View.VISIBLE);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPrizeExternalStorage.toggle();
                }
            });
        }
       builder.setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               recoveryFactory();
               /* prize-modify-by-lijimeng-for bugid 50023-20180302-start*/
                /*if(pm != null){
                    pm.goToSleep(SystemClock.uptimeMillis());
                }*/
               //finish();
               /* prize-modify-by-lijimeng-for bugid 50023-20180302-end*/
           }
       }).setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
               finish();
           }
       });
       mAlertDialog =  builder.create();
        mAlertDialog.setCanceledOnTouchOutside(false);
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();

   }

    private void recoveryFactory(){
        if (Utils.isMonkeyRunning()) {
            return;
        }

        final PersistentDataBlockManager pdbManager = (PersistentDataBlockManager)
                getActivity().getSystemService(Context.PERSISTENT_DATA_BLOCK_SERVICE);

        if (pdbManager != null && !pdbManager.getOemUnlockEnabled() &&
                Utils.isDeviceProvisioned(getActivity())) {
            // if OEM unlock is enabled, this will be wiped during FR process. If disabled, it
            // will be wiped here, unless the device is still being provisioned, in which case
            // the persistent data block will be preserved.
            new AsyncTask<Void, Void, Void>() {
                int mOldOrientation;
                /* prize-modify-by-lijimeng-for bugid 55056-20180302-start*/
               // ProgressDialog mProgressDialog;
                /* prize-modify-by-lijimeng-for bugid 55056-20180302-end*/
                @Override
                protected Void doInBackground(Void... params) {
                    pdbManager.wipe();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    /* prize-modify-by-lijimeng-for bugid 55056-20180302-start*/
                   // mProgressDialog.hide();
                    /* prize-modify-by-lijimeng-for bugid 55056-20180302-end*/
                    if (getActivity() != null) {
                        getActivity().setRequestedOrientation(mOldOrientation);
                        doMasterClear();
                    }
                }

                @Override
                protected void onPreExecute() {
                    mProgressDialog = getProgressDialog();
                    mProgressDialog.show();

                    // need to prevent orientation changes as we're about to go into
                    // a long IO request, so we won't be able to access inflate resources on flash
                    mOldOrientation = getActivity().getRequestedOrientation();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                }
            }.execute();
        } else {
            doMasterClear();
        }
    }

    private ProgressDialog getProgressDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(
                getActivity().getString(R.string.master_clear_progress_title));
        progressDialog.setMessage(
                getActivity().getString(R.string.master_clear_progress_text));
        return progressDialog;
    }
    private void doMasterClear() {
        Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, mPrizeExternalStorage.isChecked());
        getActivity().sendBroadcast(intent);
        // Intent handling is asynchronous -- assume it will happen soon.
    }
    @Override
    public void onDestroy() {
        /* prize-modify-by-lijimeng-for bugid 55056-20180302-start*/
		super.onDestroy();
        if(mProgressDialog != null){
            mProgressDialog.hide();
        }
        /* prize-modify-by-lijimeng-for bugid 55056-20180302-end*/
    }
}
