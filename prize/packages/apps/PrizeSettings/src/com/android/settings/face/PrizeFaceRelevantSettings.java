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

package com.android.settings.face;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.Fingerprint;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Annotation;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.ChooseLockGeneric;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.HelpUtils;
import com.android.settings.PrizeNoRightArrowPreference;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SubSettings;
import com.android.settings.face.utils.FaceXmlData;
import com.android.settings.face.utils.SaveListUtil;
import com.android.settings.face.utils.SpUtil;
import com.android.settings.fingerprint.PrizeFingerprintDetailsActivity;
import com.android.settings.fingerprint.PrizeFingerprintEnrollEnrolling;
import com.android.settings.fingerprint.PrizeFpOperationDialogUtils;
import com.android.settings.fingerprint.PrizeFpOperationInterface;
import com.mediatek.common.prizeoption.PrizeOption;
import android.os.SystemProperties;
import java.util.List;
import android.content.ComponentName;
import android.os.SystemProperties;
/**
 * Settings screen for fingerprints
 */
public class PrizeFaceRelevantSettings extends SubSettings {

    /**
     * Used by the choose fingerprint wizard to indicate the wizard is
     * finished, and each activity in the wizard should finish.
     * <p>
     * Previously, each activity in the wizard would finish itself after
     * starting the next activity. However, this leads to broken 'Back'
     * behavior. So, now an activity does not finish itself until it gets this
     * result.
     */
    protected static final int RESULT_FINISHED = RESULT_FIRST_USER;

    /**
     * Used by the enrolling screen during setup wizard to skip over setting up fingerprint, which
     * will be useful if the user accidentally entered this flow.
     */
    protected static final int RESULT_SKIP = RESULT_FIRST_USER + 1;

    /**
     * Like {@link #RESULT_FINISHED} except this one indicates enrollment failed because the
     * device was left idle. This is used to clear the credential token to require the user to
     * re-enter their pin/pattern/password before continuing.
     */
    protected static final int RESULT_TIMEOUT = RESULT_FIRST_USER + 2;

    private static final long LOCKOUT_DURATION = 30000; // time we have to wait for fp to reset, ms

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, FaceprintSettingsFragment.class.getName());
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (FaceprintSettingsFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-start*/
        /*CharSequence msg = getText(R.string.security_settings_fingerprint_preference_title);*/
        CharSequence msg = getText(R.string.prize_face_operation_function_manager_title);
        /*PRIZE-Change-M_Fingerprint-wangzhong-2016_6_28-end*/
        setTitle(msg);
    }

    public static class FaceprintSettingsFragment extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener {
        private static final int MAX_RETRY_ATTEMPTS = 20;
        private static final int RESET_HIGHLIGHT_DELAY_MS = 500;

        private static final String TAG = "PrizeFaceRelevantSettings";

        private static final String KEY_FINGERPRINT_ITEM_PREFIX = "key_fingerprint_item";
        private static final String KEY_FINGERPRINT_ADD = "key_fingerprint_add";
        private static final String KEY_APP_LOCK_ADD = "key_app_lock_add";

        private static final String KEY_FINGERPRINT_ENABLE_KEYGUARD_TOGGLE =
                "fingerprint_enable_keyguard_toggle";
        private static final String KEY_LAUNCHED_CONFIRM = "launched_confirm";

        private static final String KEY_FP_FUNCTION_LIST = "fa_function_list";
        private static final String KEY_FP_LIST = "app_list";

        private static final String FP_UNLOCK_SCREEN = "fa_unlock_screen";
        private static final String FA_SHOW_ANIMAL = "face_show_animal";
        private static final String FP_APP_LOCK = "fp_app_lock";
        private static final String PRIZE_FACEID_SWITCH = "prize_faceid_switch";

        private static final int MSG_REFRESH_FINGERPRINT_TEMPLATES = 1000;
        private static final int MSG_FINGER_AUTH_SUCCESS = 1001;
        private static final int MSG_FINGER_AUTH_FAIL = 1002;
        private static final int MSG_FINGER_AUTH_ERROR = 1003;
        private static final int MSG_FINGER_AUTH_HELP = 1004;

        private static final int CONFIRM_REQUEST = 101;
        private static final int CHOOSE_LOCK_GENERIC_REQUEST = 102;

        private static final int ADD_FINGERPRINT_REQUEST = 10;

        protected static final boolean DEBUG = true;

        /* private FingerprintManager mFingerprintManager;
         private CancellationSignal mFingerprintCancel;
         private boolean mInFingerprintLockout;*/
        private byte[] mToken;
        private boolean mLaunchedConfirm;
        private Drawable mHighlightDrawable;

        private PreferenceGroup mFpFunctionListGroup;
        private PreferenceGroup mFpListGroup;

        private SwitchPreference mUnLockScreenSwitchPreference;
        private SwitchPreference mFaceAnimalSwitchPreference;
        private SwitchPreference mAppLockSwitchPreference;

        private static final int DELETE_FACE_UPATE = 0;
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case DELETE_FACE_UPATE:
                        updatePreferences();
                        break;

                }
            }
        };

        /*private AuthenticationCallback mAuthCallback = new AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(AuthenticationResult result) {
                int fingerId = result.getFingerprint().getFingerId();
                mHandler.obtainMessage(MSG_FINGER_AUTH_SUCCESS, fingerId, 0).sendToTarget();
            }

            @Override
            public void onAuthenticationFailed() {
                mHandler.obtainMessage(MSG_FINGER_AUTH_FAIL).sendToTarget();
            }

            ;

            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                mHandler.obtainMessage(MSG_FINGER_AUTH_ERROR, errMsgId, 0, errString)
                        .sendToTarget();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                mHandler.obtainMessage(MSG_FINGER_AUTH_HELP, helpMsgId, 0, helpString)
                        .sendToTarget();
            }
        };*/
        /*private RemovalCallback mRemoveCallback = new RemovalCallback() {

            @Override
            public void onRemovalSucceeded(Fingerprint fingerprint) {
                mHandler.obtainMessage(MSG_REFRESH_FINGERPRINT_TEMPLATES,
                        fingerprint.getFingerId(), 0).sendToTarget();
            }

            @Override
            public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) {
                final Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, errString, Toast.LENGTH_SHORT);
                }
            }
        };*/
        /*private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case MSG_REFRESH_FINGERPRINT_TEMPLATES:
                        removeFingerprintPreference(msg.arg1);
                        updateAddPreference();
                        retryFingerprint();
                        break;
                    case MSG_FINGER_AUTH_SUCCESS:
                        mFingerprintCancel = null;
                        highlightFingerprintItem(msg.arg1);
                        retryFingerprint();
                        break;
                    case MSG_FINGER_AUTH_FAIL:
                        // No action required... fingerprint will allow up to 5 of these
                        break;
                    case MSG_FINGER_AUTH_ERROR:
                        handleError(msg.arg1 *//* errMsgId *//*, (CharSequence) msg.obj *//* errStr *//*);
                        break;
                    case MSG_FINGER_AUTH_HELP: {
                        // Not used
                    }
                    break;
                }
            }

            ;
        };*/

        /*private DialogInterface.OnKeyListener mKeyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    updateAppLockSwitchPreference();
                }
                return false;
            }
        };*/

        /*private void stopFingerprint() {
            if (mFingerprintCancel != null && !mFingerprintCancel.isCanceled()) {
                mFingerprintCancel.cancel();
            }
            mFingerprintCancel = null;
        }*/

        /**
         * @param errMsgId
         */
        /*protected void handleError(int errMsgId, CharSequence msg) {
            mFingerprintCancel = null;
            switch (errMsgId) {
                case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                    return; // Only happens if we get preempted by another activity. Ignored.
                case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                    mInFingerprintLockout = true;
                    // We've been locked out.  Reset after 30s.
                    if (!mHandler.hasCallbacks(mFingerprintLockoutReset)) {
                        mHandler.postDelayed(mFingerprintLockoutReset,
                                LOCKOUT_DURATION);
                    }
                    // Fall through to show message
                default:
                    // Activity can be null on a screen rotation.
                    final Activity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
                    }
                    break;
            }
            retryFingerprint(); // start again
        }*/

        /*private void retryFingerprint() {
            if (!mInFingerprintLockout) {
                mFingerprintCancel = new CancellationSignal();
                mFingerprintManager.authenticate(null, mFingerprintCancel, 0 *//* flags *//*,
                        mAuthCallback, null);
            }
        }*/
        @Override
        protected int getMetricsCategory() {
            return MetricsEvent.FINGERPRINT;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                mToken = savedInstanceState.getByteArray(
                        ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                mLaunchedConfirm = savedInstanceState.getBoolean(
                        KEY_LAUNCHED_CONFIRM, false);
            }

            Activity activity = getActivity();
            if (mToken == null && mLaunchedConfirm == false) {
                mLaunchedConfirm = true;
                launchChooseOrConfirmLock();
            }

            addPreferencesFromResource(R.xml.prize_security_settings_face);
            mFpFunctionListGroup = (PreferenceGroup) findPreference(KEY_FP_FUNCTION_LIST);
            mFaceAnimalSwitchPreference = (SwitchPreference) findPreference(FA_SHOW_ANIMAL);
            mAppLockSwitchPreference = (SwitchPreference) findPreference(FP_APP_LOCK);

            mFaceAnimalSwitchPreference.setOnPreferenceChangeListener(this);

            mAppLockSwitchPreference.setOnPreferenceChangeListener(this);

            if (null != mFpFunctionListGroup && null != mAppLockSwitchPreference) {
                mFpFunctionListGroup.removePreference(mAppLockSwitchPreference);
            }

            if (PrizeOption.PRIZE_FACE_ID_KOOBEE){
                getPreferenceScreen().removePreference(mFpFunctionListGroup);
            }

            mFpListGroup = (PreferenceGroup) findPreference(KEY_FP_LIST);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            /*TextView v = (TextView) LayoutInflater.from(view.getContext()).inflate(
                    R.layout.fingerprint_settings_footer, null);
            v.setText(LearnMoreSpan.linkify(getText(isFingerprintDisabled()
                            ? R.string.security_settings_fingerprint_enroll_disclaimer_lockscreen_disabled
                            : R.string.security_settings_fingerprint_enroll_disclaimer),
                    getString(getHelpResource())));
            v.setMovementMethod(new LinkMovementMethod());*/
//            mFpFunctionListGroup.addFooterView(v);
//            mFpListGroup.addFooterView(v);
//            mFpListGroup.setFooterDividersEnabled(false);
        }

        /*private boolean isFingerprintDisabled() {
            final DevicePolicyManager dpm =
                    (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            return dpm != null && (dpm.getKeyguardDisabledFeatures(null)
                    & DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT) != 0;
        }*/

        /*protected void removeFingerprintPreference(int fingerprintId) {
            String name = genKey(fingerprintId);
            Preference prefToRemove = findPreference(name);
            if (prefToRemove != null) {
                if (!getPreferenceScreen().removePreference(prefToRemove)) {
                    Log.w(TAG, "Failed to remove preference with key " + name);
                }
            } else {
                Log.w(TAG, "Can't find preference to remove: " + name);
            }
        }*/

        /**
         * Important!
         * <p>
         * Don't forget to update the SecuritySearchIndexProvider if you are doing any change in the
         * logic or adding/removing preferences here.
         */
       /* private PreferenceScreen createPreferenceHierarchy() {
//            PreferenceScreen root = getPreferenceScreen();
//            if (root != null) {
//                root.removeAll();
//            }
//            addPreferencesFromResource(R.xml.prize_security_settings_fingerprint);
//            root = getPreferenceScreen();

            addFingerprintItemPreferences(mFpListGroup);
            return null;
        }*/
        private void addAppLockPreferences(PreferenceGroup root) {
            Preference mPreference = root.findPreference(KEY_APP_LOCK_ADD);
            if (mPreference != null) {
                return;
            }
            Preference mAppLockPreference = new Preference(root.getContext());
            mAppLockPreference.setKey(KEY_APP_LOCK_ADD);
            mAppLockPreference.setTitle(R.string.application_lock);
            root.addPreference(mAppLockPreference);
            mAppLockPreference.setOnPreferenceChangeListener(this);
        }

        private void removeAppLockPreferences(PreferenceGroup root) {
            if (root == null) {
                return;
            }
            Preference mAppLockPreference = root.findPreference(KEY_APP_LOCK_ADD);
            if (mAppLockPreference != null) {
                root.removePreference(mAppLockPreference);
            }
        }

        private void addFaceprintItemPreferences(PreferenceGroup root) {
            root.removeAll();
            /*final List<Fingerprint> items = mFingerprintManager.getEnrolledFingerprints();
            final int fingerprintCount = items.size();
            for (int i = 0; i < fingerprintCount; i++) {
                final Fingerprint item = items.get(i);
                FingerprintPreference pref = new FingerprintPreference(root.getContext());
                pref.setKey(genKey(item.getFingerId()));
                pref.setTitle(item.getName());
                pref.setFingerprint(item);
                pref.setPersistent(false);
                root.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
            }*/

            FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
            //List<String> list = SaveListUtil.getList(getActivity());
            //int faceprintCount = list.size();
            //int faceprintCount = faceBean.faceSize;
			int faceprintCount = 0;
            

		    Log.d(TAG,"ishaveface:"+SystemProperties.get("persist.sys.ishavaface", "no").equals("yes"));
			if(SystemProperties.get("persist.sys.ishavaface", "no").equals("yes")){
				faceprintCount = 1;
			}else{
				faceprintCount = 0;
			}


            for (int i = 0; i < faceprintCount; i++) {
                //String muserName = (String) SpUtil.getData(getActivity(), list.get(i), "");
                String muserName = /*faceBean.face_name*/getString(R.string.face_delete);
                FaceprintPreference pref = new FaceprintPreference(root.getContext());
                pref.setTitle(muserName);
                pref.setUserName(muserName);
                pref.setIndex(i);
                pref.setPersistent(false);
                root.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
            }

            /*Preference lock = new Preference(root.getContext());
            lock.setKey("lockaa");
            lock.setTitle("解锁");
            root.addPreference(lock);
            lock.setOnPreferenceChangeListener(this);*/

            PrizeNoRightArrowPreference addPreference = new PrizeNoRightArrowPreference(root.getContext());
            addPreference.setKey(KEY_FINGERPRINT_ADD);
            addPreference.setTitle(R.string.faceprint_add_title);
//            addPreference.setIcon(R.drawable.ic_add_24dp);
            if (faceprintCount == 0) {
                root.addPreference(addPreference);
            } else {
                root.removePreference(addPreference);
            }
            addPreference.setOnPreferenceChangeListener(this);
           /* prize-add-by-lijimeng-20180321-start*/
            SwitchPreference switchPreference = new SwitchPreference(getPrefContext());
            switchPreference.setTitle(R.string.prize_face_switch);
            switchPreference.setSummary(R.string.prize_face_switch_summary);
            switchPreference.setKey(PRIZE_FACEID_SWITCH);//lijimeng
            boolean siOpen = Settings.System.getInt(getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1) == 1;
            if(faceprintCount == 0){
                switchPreference.setEnabled(false);
                switchPreference.setChecked(true);
                Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1);
            }else{
                switchPreference.setEnabled(true);
                if(siOpen){
                    switchPreference.setChecked(siOpen);
                }else{
                    switchPreference.setChecked(siOpen);
                }
            }
            switchPreference.setOnPreferenceChangeListener(this);
            root.addPreference(switchPreference);
            /* prize-add-by-lijimeng-20180321-end*/
            //updateAddPreference();

            //mFaceAnimalSwitchPreference.setChecked((boolean) (SpUtil.getData(getActivity(), "face_show_ani", true)));
            mFaceAnimalSwitchPreference.setChecked(faceBean.face_show_ani);
            if (faceprintCount == 0) {
                updateFaceAniPreference(mFaceAnimalSwitchPreference, true);
            } else {
                mFaceAnimalSwitchPreference.setEnabled(true);
            }

            if (faceprintCount == 0) {
                if (mFaceprintEnrollDialog != null && mFaceprintEnrollDialog.isShowing()) {
                    return;
                }
                //showFaceprintEnrollDialog();
            }
        }

        private void updateFaceAniPreference(SwitchPreference spf, boolean noFace) {
            spf.setChecked(!noFace);
            spf.setEnabled(!noFace);
            //SpUtil.saveData(getActivity(),"face_show_ani", false);
        }

        private void updateAddPreference() {
            /* Disable preference if too many faceprints added */
            final int max = getContext().getResources().getInteger(
                    com.android.internal.R.integer.config_fingerprintMaxTemplatesPerUser);
            boolean tooMany = /*SaveListUtil.getList(getActivity()).size()*/FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH).faceSize >= /*max*/1;
            CharSequence maxSummary = tooMany ?
                    getContext().getString(R.string.faceprint_add_max, /*max*/1) : "";
            PrizeNoRightArrowPreference addPreference = (PrizeNoRightArrowPreference) findPreference(KEY_FINGERPRINT_ADD);
            addPreference.setSummary(maxSummary);
            addPreference.setEnabled(!tooMany);
        }

        private static String genKey(int id) {
            return KEY_FINGERPRINT_ITEM_PREFIX + "_" + id;
        }

        @Override
        public void onResume() {
            super.onResume();
            // Make sure we reload the preference hierarchy since fingerprints may be added,
            // deleted or renamed.
            updatePreferences();
        }

        private void updatePreferences() {
//            createPreferenceHierarchy();
            //addFingerprintItemPreferences(mFpListGroup);
            //retryFingerprint();

            addFaceprintItemPreferences(mFpListGroup);


           /* PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start
            if (!isAddedFingerprint()) {
                updateUnLockScreenData(false);
                updateUnLockScreenSwitchPreference();
                updateAppLockData(false);
                updateAppLockSwitchPreference();
            }
            if (!isSetedAppLock()) {
                updateAppLockData(false);
                updateAppLockSwitchPreference();
            } else {
                removeAppLockPreferences(mFpFunctionListGroup);
                updateAppLockSwitchPreference();
            }
            PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
        }

        @Override
        public void onPause() {
            super.onPause();
            // stopFingerprint();
        }

        @Override
        public void onSaveInstanceState(final Bundle outState) {
            outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN,
                    mToken);
            outState.putBoolean(KEY_LAUNCHED_CONFIRM, mLaunchedConfirm);
        }

        @Override
        public boolean onPreferenceTreeClick(Preference pref) {
            final String key = pref.getKey();
            if (KEY_FINGERPRINT_ADD.equals(key)) {
            	Log.d("xucm", "onPreferenceTreeClick KEY_FINGERPRINT_ADD");
                Intent intent = new Intent();
                intent.setClassName("com.android.settings",
                        PrizeFaceprintEnrollEnrolling.class.getName());
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
                startActivityForResult(intent, ADD_FINGERPRINT_REQUEST);
            } else if (FA_SHOW_ANIMAL.equals(key)) {

                FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
                if (mFaceAnimalSwitchPreference.isChecked()) {
                    faceBean.face_show_ani = true;
                    //SpUtil.saveData(getActivity(), "face_show_ani", true);
                } else {
                    faceBean.face_show_ani = false;
                    //SpUtil.saveData(getActivity(), "face_show_ani", false);
                }
                FaceXmlData.writeCheckRootDataFile(faceBean, FaceXmlData.FACEBEAN_PHTH);
            } else if (FP_APP_LOCK.equals(key)) {
                boolean isChecked = mAppLockSwitchPreference.isChecked();
                if (isChecked) {
                    // TODO: 2016/8/16  Confirm the password
                    startConfirmApplockPassword();
                } else {
                    return true;
                }
            } else if (KEY_APP_LOCK_ADD.equals(key)) {
            } else if (pref instanceof FaceprintPreference) {
                /*Intent intent = new Intent();

                intent.setClassName("com.android.settings",
                        PrizeFaceprintDetailsActivity.class.getName());

                Bundle bundle = new Bundle();
                bundle.putString("face_name", ((FaceprintPreference) pref).getmUserName());
                bundle.putInt("face_index", ((FaceprintPreference) pref).getIndex());

                intent.putExtras(bundle);
                startActivity(intent);*/

                showDeleteDialog();
                return true;
            }
            return true;
        }

        private void showDeleteDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.face_delete));
            builder.setMessage(getString(R.string.face_delete_message));
            builder.setPositiveButton(getString(R.string.prize_fp_operation_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String faceCompany = "sensetime";
                   if(faceCompany.equals("sensetime")){
                       ComponentName  cn = new ComponentName("com.prize.faceunlock", "com.sensetime.faceunlock.service.PrizeFaceDetectService");
                       Intent intent = new Intent();
                       intent.putExtra("face_operation", "deleface");
                       intent.setComponent(cn);
                       getPrefContext().startService(intent);
					   mHandler.removeMessages(DELETE_FACE_UPATE);
                       mHandler.sendEmptyMessageDelayed(DELETE_FACE_UPATE,100);
                   }else{
                       FaceBean faceBean = FaceXmlData.readCheckRootDataFile(FaceXmlData.FACEBEAN_PHTH);
                       faceBean.faceSize = 0;
                       FaceXmlData.writeCheckRootDataFile(faceBean, FaceXmlData.FACEBEAN_PHTH);
                       SystemProperties.set("persist.sys.pafd", "0");
					   updatePreferences();
                   }
                   
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.prize_fp_operation_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        /*private void showRenameDeleteDialog(final Fingerprint fp) {
            RenameDeleteDialog renameDeleteDialog = new RenameDeleteDialog(getContext());
            Bundle args = new Bundle();
            args.putParcelable("fingerprint", fp);
            renameDeleteDialog.setArguments(args);
            renameDeleteDialog.setTargetFragment(this, 0);
            renameDeleteDialog.show(getFragmentManager(), RenameDeleteDialog.class.getName());
        }*/

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            boolean result = true;
            final String key = preference.getKey();
            if (KEY_FINGERPRINT_ENABLE_KEYGUARD_TOGGLE.equals(key)) {
                // TODO
            } else if (PRIZE_FACEID_SWITCH.equals(key)){
               boolean isChecked = (boolean) value;
               if(isChecked){
                   Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 1);
               }else{
                   Settings.System.putInt(getContentResolver(), Settings.System.PRIZE_FACEID_SWITCH , 0);
               }
            }else{
                Log.v(TAG, "Unknown key:" + key);
            }
            return result;
        }

        @Override
        protected int getHelpResource() {
            return R.string.help_url_fingerprint;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CHOOSE_LOCK_GENERIC_REQUEST
                    || requestCode == CONFIRM_REQUEST) {
                if (resultCode == RESULT_FINISHED || resultCode == RESULT_OK) {
                    // The lock pin/pattern/password was set. Start enrolling!
                    if (data != null) {
                        mToken = data.getByteArrayExtra(
                                ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);

                        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
                        /*if (!isAddedFingerprint()) {
                            showFingerprintEnrollDialog();
                        }*/
                        /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
                    }
                }
            } else if (requestCode == ADD_FINGERPRINT_REQUEST) {
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
                /*if (isAppLockFirstOpenFingerprintEnroll) {
                    isAppLockFirstOpenFingerprintEnroll = false;
                    if (!isSetedAppLock()) {
                        showAppLockSetDialog();
                    }
                }*/
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/

                if (resultCode == RESULT_TIMEOUT) {
                    Activity activity = getActivity();
                    activity.setResult(RESULT_TIMEOUT);
                    activity.finish();
                }
            }

            if (mToken == null) {
                // Didn't get an authentication, finishing
                getActivity().finish();
            }
        }

        private Dialog mFaceprintEnrollDialog = null;
        private Dialog mFingerprintEnrollDialog = null;
        private Dialog mAppLockSetDialog = null;
        private Dialog mAppLockCloseDialog = null;
        private ContentResolver mContentResolver = null;
        private boolean isAppLockFirstOpenFingerprintEnroll = false;


        public void showFaceprintEnrollDialog() {
            mFaceprintEnrollDialog = PrizeFpOperationDialogUtils.createDoubleButtonTextDialog(this.getActivity(),
                    getString(R.string.prize_facerprint_management_dialog_title),
                    getString(R.string.prize_faceprint_management_dialog_message),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startFaceprintEnroll();
                            if (null != mFaceprintEnrollDialog)
                                mFaceprintEnrollDialog.dismiss();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (null != mFaceprintEnrollDialog)
                                mFaceprintEnrollDialog.dismiss();
                        }
                    },
                    getString(R.string.prize_fingerprint_management_dialog_confirm),
                    getString(R.string.prize_fingerprint_management_dialog_cancel));
            //mFaceprintEnrollDialog.setOnKeyListener(mKeyListener);
        }


        public void startFaceprintEnroll() {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings",
                    PrizeFaceprintEnrollEnrolling.class.getName());
            //intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
            // intent.putExtra(PrizeFpOperationInterface.FP_ADD_TYPE_KEY, PrizeFpOperationInterface.ADD_FP_IN_FP_INTRRFACE);
            startActivityForResult(intent, ADD_FINGERPRINT_REQUEST);
        }

        private void startConfirmApplockPassword() {

        }


        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        private Drawable getHighlightDrawable() {
            if (mHighlightDrawable == null) {
                final Activity activity = getActivity();
                if (activity != null) {
                    mHighlightDrawable = activity.getDrawable(R.drawable.preference_highlight);
                }
            }
            return mHighlightDrawable;
        }

        private void launchChooseOrConfirmLock() {
            Intent intent = new Intent();
            //long challenge = mFingerprintManager.preEnroll();
            ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(getActivity(), this);
            if (!helper.launchConfirmationActivity(CONFIRM_REQUEST,
                    getString(R.string.faceid),
                    null, null, 0)) {
                intent.setClassName("com.android.settings", ChooseLockGeneric.class.getName());
                intent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.MINIMUM_QUALITY_KEY,
                        DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
                intent.putExtra(ChooseLockGeneric.ChooseLockGenericFragment.HIDE_DISABLED_PREFS,
                        true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-start*/
                if (null != getActivity() && null != getActivity().getIntent() && getActivity().getIntent().getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_IS_FACEPRINT, false)) {
                    intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_FOR_FACEPRINT, true);
                    intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_IS_FACEPRINT, true);
                }
                /*PRIZE-Add-M_Fingerprint-wangzhong-2016_6_28-end*/
                startActivityForResult(intent, CHOOSE_LOCK_GENERIC_REQUEST);
            }
        }



        /*private final Runnable mFingerprintLockoutReset = new Runnable() {
            @Override
            public void run() {
                mInFingerprintLockout = false;
                retryFingerprint();
            }
        };*/

        /*public static class RenameDeleteDialog extends DialogFragment {

            private final Context mContext;
            private Fingerprint mFp;
            private EditText mDialogTextField;
            private String mFingerName;
            private Boolean mTextHadFocus;
            private int mTextSelectionStart;
            private int mTextSelectionEnd;

            public RenameDeleteDialog(Context context) {
                mContext = context;
            }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                mFp = getArguments().getParcelable("fingerprint");
                if (savedInstanceState != null) {
                    mFingerName = savedInstanceState.getString("fingerName");
                    mTextHadFocus = savedInstanceState.getBoolean("textHadFocus");
                    mTextSelectionStart = savedInstanceState.getInt("startSelection");
                    mTextSelectionEnd = savedInstanceState.getInt("endSelection");
                }
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setView(R.layout.fingerprint_rename_dialog)
                        .setPositiveButton(R.string.security_settings_fingerprint_enroll_dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final String newName =
                                                mDialogTextField.getText().toString();
                                        final CharSequence name = mFp.getName();
                                        if (!newName.equals(name)) {
                                            if (DEBUG) {
                                                Log.v(TAG, "rename " + name + " to " + newName);
                                            }
                                            MetricsLogger.action(getContext(),
                                                    MetricsLogger.ACTION_FINGERPRINT_RENAME,
                                                    mFp.getFingerId());
                                            FaceprintSettingsFragment parent
                                                    = (FaceprintSettingsFragment)
                                                    getTargetFragment();
                                            parent.renameFingerPrint(mFp.getFingerId(),
                                                    newName);
                                        }
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(
                                R.string.security_settings_fingerprint_enroll_dialog_delete,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onDeleteClick(dialog);
                                    }
                                })
                        .create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        mDialogTextField = (EditText) alertDialog.findViewById(
                                R.id.fingerprint_rename_field);
                        CharSequence name = mFingerName == null ? mFp.getName() : mFingerName;
                        mDialogTextField.setText(name);
                        if (mTextHadFocus == null) {
                            mDialogTextField.selectAll();
                        } else {
                            mDialogTextField.setSelection(mTextSelectionStart, mTextSelectionEnd);
                        }
                    }
                });
                if (mTextHadFocus == null || mTextHadFocus) {
                    // Request the IME
                    alertDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
                return alertDialog;
            }

            private void onDeleteClick(DialogInterface dialog) {
                if (DEBUG) Log.v(TAG, "Removing fpId=" + mFp.getFingerId());
                MetricsLogger.action(getContext(), MetricsLogger.ACTION_FINGERPRINT_DELETE,
                        mFp.getFingerId());
                FaceprintSettingsFragment parent
                        = (FaceprintSettingsFragment) getTargetFragment();
                if (parent.mFingerprintManager.getEnrolledFingerprints().size() > 1) {
                    parent.deleteFingerPrint(mFp);
                } else {
                    ConfirmLastDeleteDialog lastDeleteDialog = new ConfirmLastDeleteDialog();
                    Bundle args = new Bundle();
                    args.putParcelable("fingerprint", mFp);
                    lastDeleteDialog.setArguments(args);
                    lastDeleteDialog.setTargetFragment(getTargetFragment(), 0);
                    lastDeleteDialog.show(getFragmentManager(),
                            ConfirmLastDeleteDialog.class.getName());
                }
                dialog.dismiss();
            }

            @Override
            public void onSaveInstanceState(Bundle outState) {
                super.onSaveInstanceState(outState);
                if (mDialogTextField != null) {
                    outState.putString("fingerName", mDialogTextField.getText().toString());
                    outState.putBoolean("textHadFocus", mDialogTextField.hasFocus());
                    outState.putInt("startSelection", mDialogTextField.getSelectionStart());
                    outState.putInt("endSelection", mDialogTextField.getSelectionEnd());
                }
            }
        }*/

        /*public static class ConfirmLastDeleteDialog extends DialogFragment {

            private Fingerprint mFp;

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                mFp = getArguments().getParcelable("fingerprint");
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.fingerprint_last_delete_title)
                        .setMessage(R.string.fingerprint_last_delete_message)
                        .setPositiveButton(R.string.fingerprint_last_delete_confirm,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FaceprintSettingsFragment parent
                                                = (FaceprintSettingsFragment) getTargetFragment();
                                        parent.deleteFingerPrint(mFp);
                                        dialog.dismiss();
                                    }
                                })
                        .setNegativeButton(
                                R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .create();
                return alertDialog;
            }
        }*/
    }

    public static class FingerprintPreference extends Preference {
        private Fingerprint mFingerprint;
        private View mView;

        public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public FingerprintPreference(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public FingerprintPreference(Context context, AttributeSet attrs) {
            this(context, attrs, com.android.internal.R.attr.preferenceStyle);
        }

        public FingerprintPreference(Context context) {
            this(context, null);
        }

        public View getView() {
            return mView;
        }

        public void setFingerprint(Fingerprint item) {
            mFingerprint = item;
        }

        public Fingerprint getFingerprint() {
            return mFingerprint;
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            mView = holder.itemView;
        }
    }

    ;

    /*private static class LearnMoreSpan extends URLSpan {

        private static final Typeface TYPEFACE_MEDIUM =
                Typeface.create("sans-serif-medium", Typeface.NORMAL);

        private LearnMoreSpan(String url) {
            super(url);
        }

        @Override
        public void onClick(View widget) {
            Context ctx = widget.getContext();
            Intent intent = HelpUtils.getHelpIntent(ctx, getURL(), ctx.getClass().getName());
            try {
                ((Activity) ctx).startActivityForResult(intent, 0);
            } catch (ActivityNotFoundException e) {
                Log.w(FaceprintSettingsFragment.TAG,
                        "Actvity was not found for intent, " + intent.toString());
            }
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
            ds.setTypeface(TYPEFACE_MEDIUM);
        }

        public static CharSequence linkify(CharSequence rawText, String uri) {
            SpannableString msg = new SpannableString(rawText);
            Annotation[] spans = msg.getSpans(0, msg.length(), Annotation.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(msg);
            for (Annotation annotation : spans) {
                int start = msg.getSpanStart(annotation);
                int end = msg.getSpanEnd(annotation);
                LearnMoreSpan link = new LearnMoreSpan(uri);
                builder.setSpan(link, start, end, msg.getSpanFlags(link));
            }
            return builder;
        }
    }*/

    public static class FaceprintPreference extends PrizeNoRightArrowPreference {
        private String mUserName;
        private View mView;
        private int mIndex;

        /*public FaceprintPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                   int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }*/

        /*public FaceprintPreference(Context context, AttributeSet attrs, int defStyleAttr) {
            this(context, attrs, defStyleAttr, 0);
        }

        public FaceprintPreference(Context context, AttributeSet attrs) {
            this(context, attrs, com.android.internal.R.attr.preferenceStyle);
        }*/

        public FaceprintPreference(Context context) {
            super(context);
        }

        public View getView() {
            return mView;
        }

        public void setUserName(String name) {
            mUserName = name;
        }

        public void setIndex(int index) {
            mIndex = index;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getmUserName() {
            return mUserName;
        }

        @Override
        public void onBindViewHolder(PreferenceViewHolder holder) {
            super.onBindViewHolder(holder);
            mView = holder.itemView;
        }
    }

}
