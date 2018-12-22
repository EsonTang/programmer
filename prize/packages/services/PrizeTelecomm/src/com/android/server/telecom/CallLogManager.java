/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright 2014, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.telecom;

import android.annotation.Nullable;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Country;
import android.location.CountryDetector;
import android.location.CountryListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.PersistableBundle;
import android.provider.CallLog.Calls;
import android.provider.CallLog.ConferenceCalls;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.widget.Toast;
import android.text.TextUtils;

// TODO: Needed for move to system service: import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CallerInfo;
import com.mediatek.telecom.ext.ExtensionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Helper class that provides functionality to write information about calls and their associated
 * caller details to the call log. All logging activity will be performed asynchronously in a
 * background thread to avoid blocking on the main thread.
 */
@VisibleForTesting
public final class CallLogManager extends CallsManagerListenerBase {

    public interface LogCallCompletedListener {
        void onLogCompleted(@Nullable Uri uri);
    }

    /**
     * Parameter object to hold the arguments to add a call in the call log DB.
     */
    private static class AddCallArgs {
        /**
         * @param callerInfo Caller details.
         * @param number The phone number to be logged.
         * @param presentation Number presentation of the phone number to be logged.
         * @param callType The type of call (e.g INCOMING_TYPE). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param features The features of the call (e.g. FEATURES_VIDEO). @see
         *     {@link android.provider.CallLog} for the list of values.
         * @param creationDate Time when the call was created (milliseconds since epoch).
         * @param durationInMillis Duration of the call (milliseconds).
         * @param dataUsage Data usage in bytes, or null if not applicable.
         * @param logCallCompletedListener optional callback called after the call is logged.
         * @param conferenceCallLogId The conference call callLog id.
         */
        public AddCallArgs(Context context, CallerInfo callerInfo, String number,
                String postDialDigits, String viaNumber, int presentation, int callType,
                int features, PhoneAccountHandle accountHandle, long creationDate,
                long durationInMillis, Long dataUsage, UserHandle initiatingUser,
                @Nullable LogCallCompletedListener logCallCompletedListener,
                long conferenceCallLogId /* M: For Conference call */) {
            this.context = context;
            this.callerInfo = callerInfo;
            this.number = number;
            this.postDialDigits = postDialDigits;
            this.viaNumber = viaNumber;
            this.presentation = presentation;
            this.callType = callType;
            this.features = features;
            this.accountHandle = accountHandle;
            this.timestamp = creationDate;
            this.durationInSec = (int)(durationInMillis / 1000);
            this.dataUsage = dataUsage;
            this.initiatingUser = initiatingUser;
            this.logCallCompletedListener = logCallCompletedListener;
            /// M: For Volte conference call calllog
            this.conferenceCallLogId = conferenceCallLogId;
        }
        // Since the members are accessed directly, we don't use the
        // mXxxx notation.
        public final Context context;
        public final CallerInfo callerInfo;
        public final String number;
        public final String postDialDigits;
        public final String viaNumber;
        public final int presentation;
        public final int callType;
        public final int features;
        public final PhoneAccountHandle accountHandle;
        public final long timestamp;
        public final int durationInSec;
        public final Long dataUsage;
        public final UserHandle initiatingUser;
        /// M: For Volte conference call calllog
        public final long conferenceCallLogId;

        @Nullable
        public final LogCallCompletedListener logCallCompletedListener;
    }

    private static final String TAG = CallLogManager.class.getSimpleName();

    private final Context mContext;
    private final PhoneAccountRegistrar mPhoneAccountRegistrar;
    private final MissedCallNotifier mMissedCallNotifier;
    private static final String ACTION_CALLS_TABLE_ADD_ENTRY =
                "com.android.server.telecom.intent.action.CALLS_ADD_ENTRY";
    private static final String PERMISSION_PROCESS_CALLLOG_INFO =
                "android.permission.PROCESS_CALLLOG_INFO";
    private static final String CALL_TYPE = "callType";
    private static final String CALL_DURATION = "duration";

    private Object mLock;
    private String mCurrentCountryIso;

    public CallLogManager(Context context, PhoneAccountRegistrar phoneAccountRegistrar,
            MissedCallNotifier missedCallNotifier) {
        mContext = context;
        mPhoneAccountRegistrar = phoneAccountRegistrar;
        mMissedCallNotifier = missedCallNotifier;
        mLock = new Object();
    }

    @Override
    public void onCallStateChanged(Call call, int oldState, int newState) {
        int disconnectCause = call.getDisconnectCause().getCode();
        boolean isNewlyDisconnected =
                newState == CallState.DISCONNECTED || newState == CallState.ABORTED;
        boolean isCallCanceled = isNewlyDisconnected && disconnectCause == DisconnectCause.CANCELED;
        /// M: Ignore Conference child call, because its DisconnectCause always is CANCELED @{
        if (call.getConferenceCallLogId() > 0) {
            isCallCanceled = false;
        }
        Log.d(TAG, "onCallStateChanged [" + call.getId()
                + ", " + Log.piiHandle(call.getOriginalHandle())
                + "] isNewlyDisconnected:" + isNewlyDisconnected
                + ", oldState:" + CallState.toString(oldState)
                + ", newState:" + CallState.toString(newState)
                + ", call.isConference():" + call.isConference()
                + ", isCallCanceled:" + isCallCanceled
                + ", hasParent:" + (call.getParentCall() != null)
                + ", ConferenceCallLogId:" + call.getConferenceCallLogId());
        /// @}

        // Log newly disconnected calls only if:
        // 1) It was not in the "choose account" phase when disconnected
        // 2) It is a conference call
        // 3) Call was not explicitly canceled
        // 4) Call is not an external call
        if (isNewlyDisconnected &&
                (oldState != CallState.SELECT_PHONE_ACCOUNT &&
                 !call.isConference() &&
                 !isCallCanceled) &&
                !call.isExternalCall()) {
            int type;
            if (!call.isIncoming()) {
                type = Calls.OUTGOING_TYPE;
            } else if (disconnectCause == DisconnectCause.MISSED) {
                type = Calls.MISSED_TYPE;
            } else if (disconnectCause == DisconnectCause.ANSWERED_ELSEWHERE) {
                type = Calls.ANSWERED_EXTERNALLY_TYPE;
            } else if (disconnectCause == DisconnectCause.REJECTED) {
                type = Calls.REJECTED_TYPE;
            } else {
                type = Calls.INCOMING_TYPE;
            }

            /// M: Show call duration @{
            String disconnectReason = call.getDisconnectCause().getReason() == null ? ""
                    : call.getDisconnectCause().getReason();
            if (oldState != CallState.DIALING && oldState != CallState.RINGING
                    && call.getConferenceCallLogId() <= 0
                    && !disconnectReason.contains(IMS_MERGED_SUCCESSFULLY)) {
                showCallDuration(call);
            }
            /// @}

            logCall(call, type, true /*showNotificationForMissedCall*/);
        }

        /// M: Log the conference unconnected participants (such as connect fail, remote reject)
        /// which need handle specially due to they can not be generated telecom call. @{
        if (isNewlyDisconnected && call.isConferenceDial() &&
            call.isConference() && !isCallCanceled) {
            logConferenceUnconnectedParticipants(call);
        }
        /// @}
    }

    /// M: Log the conference unconnected participants
    void logConferenceUnconnectedParticipants(Call conferenceCall) {
        long confCallLogId = conferenceCall.getConferenceCallLogId();
        if (confCallLogId <= 0) {
            // Use conference create time as the conference temp id
            confCallLogId = conferenceCall.getCreationTimeMillis();
            conferenceCall.setConferenceCallLogId(confCallLogId);
        }
        Log.d(TAG, "logConferenceUnconnectedParticipants confCallLogId=" + confCallLogId);
        for (String number : conferenceCall.getUnconnectedParticipants()) {
            String postDialDigits = number != null
                    ? PhoneNumberUtils.extractPostDialPortion(number) : "";
            /// M: Avoid to log duplicated post dial digits. @{
            if (!TextUtils.isEmpty(postDialDigits)
                    && postDialDigits.equals(PhoneNumberUtils.extractPostDialPortion(number))) {
                number = PhoneNumberUtils.extractNetworkPortionAlt(number);
            }
            /// @}
            int handlePresentation = TelecomManager.PRESENTATION_ALLOWED;
            int type = Calls.OUTGOING_TYPE;
            int callFeatures = getCallFeatures(conferenceCall.getVideoStateHistory(), false);
            PhoneAccountHandle accountHandle = conferenceCall.getTargetPhoneAccount();
            long creationTime = conferenceCall.getCreationTimeMillis();
            boolean isEmergencyNumber = PhoneNumberUtils.isEmergencyNumber(number);
            UserHandle initUser = conferenceCall.getInitiatingUser();
            logCall(null, number, postDialDigits, "",
                    handlePresentation, type, callFeatures, accountHandle,
                    creationTime, 0, null, isEmergencyNumber, initUser,
                    null, confCallLogId);
        }
    }

    void logCall(Call call, int type, boolean showNotificationForMissedCall) {
        if (type == Calls.MISSED_TYPE && showNotificationForMissedCall) {
            logCall(call, Calls.MISSED_TYPE,
                    new LogCallCompletedListener() {
                        @Override
                        public void onLogCompleted(@Nullable Uri uri) {
                            mMissedCallNotifier.showMissedCallNotification(call);
                        }
                    });
        } else {
            logCall(call, type, null);
        }
    }

    /**
     * Logs a call to the call log based on the {@link Call} object passed in.
     *
     * @param call The call object being logged
     * @param callLogType The type of call log entry to log this call as. See:
     *     {@link android.provider.CallLog.Calls#INCOMING_TYPE}
     *     {@link android.provider.CallLog.Calls#OUTGOING_TYPE}
     *     {@link android.provider.CallLog.Calls#MISSED_TYPE}
     * @param logCallCompletedListener optional callback called after the call is logged.
     */
    void logCall(Call call, int callLogType,
        @Nullable LogCallCompletedListener logCallCompletedListener) {
        final long creationTime = call.getCreationTimeMillis();
        final long age = call.getAgeMillis();

        final String logNumber = getLogNumber(call);
        /// M: ALPS01899538, when dial a empty voice mail number fail, should not log this @{
        if (PhoneAccount.SCHEME_VOICEMAIL.equals(getLogScheme(call))
                && TextUtils.isEmpty(logNumber)) {
            Log.d(TAG, "Empty voice mail logNumber");
            return;
        }
        /// @}

        /// M: Update the CS call into IMS call callLog for conference SRVCC case @{
        if (handleConferenceSrvccCallLog(call, logNumber)) {
            return;
        }
        /// @}

        Log.d(TAG, "logNumber set to: %s", Log.pii(logNumber));

        final PhoneAccountHandle emergencyAccountHandle =
                TelephonyUtil.getDefaultEmergencyPhoneAccount().getAccountHandle();

        String formattedViaNumber = PhoneNumberUtils.formatNumber(call.getViaNumber(),
                getCountryIso());
        formattedViaNumber = (formattedViaNumber != null) ?
                formattedViaNumber : call.getViaNumber();

        PhoneAccountHandle accountHandle = call.getTargetPhoneAccount();
        if (emergencyAccountHandle.equals(accountHandle)) {
            accountHandle = null;
        }

        Long callDataUsage = call.getCallDataUsage() == Call.DATA_USAGE_NOT_SET ? null :
                call.getCallDataUsage();

        int callFeatures = getCallFeatures(call.getVideoStateHistory(),
                call.getDisconnectCause().getCode() == DisconnectCause.CALL_PULLED);

       ///M: Plugin to get call features corresponds to different call type
       callFeatures = ExtensionManager.getCallMgrExt().getCallFeatures(
                call.getConnectionManagerPhoneAccount(), callFeatures);

        logCall(call.getCallerInfo(), logNumber, call.getPostDialDigits(), formattedViaNumber,
                call.getHandlePresentation(), callLogType, callFeatures, accountHandle,
                creationTime, age, callDataUsage, call.isEmergencyCall(), call.getInitiatingUser(),
                logCallCompletedListener,
                call.getConferenceCallLogId() /* M: For Volte Conference call */);
    }

    /**
     * Inserts a call into the call log, based on the parameters passed in.
     *
     * @param callerInfo Caller details.
     * @param number The number the call was made to or from.
     * @param postDialDigits The post-dial digits that were dialed after the number,
     *                       if it was an outgoing call. Otherwise ''.
     * @param presentation
     * @param callType The type of call.
     * @param features The features of the call.
     * @param start The start time of the call, in milliseconds.
     * @param duration The duration of the call, in milliseconds.
     * @param dataUsage The data usage for the call, null if not applicable.
     * @param isEmergency {@code true} if this is an emergency call, {@code false} otherwise.
     * @param logCallCompletedListener optional callback called after the call is logged.
     * @param conferenceCallLogId The conference call callLog id.
     */
    private void logCall(
            CallerInfo callerInfo,
            String number,
            String postDialDigits,
            String viaNumber,
            int presentation,
            int callType,
            int features,
            PhoneAccountHandle accountHandle,
            long start,
            long duration,
            Long dataUsage,
            boolean isEmergency,
            UserHandle initiatingUser,
            @Nullable LogCallCompletedListener logCallCompletedListener,
            long conferenceCallLogId /* M: For Volte Conference call */) {

        // On some devices, to avoid accidental redialing of emergency numbers, we *never* log
        // emergency calls to the Call Log.  (This behavior is set on a per-product basis, based
        // on carrier requirements.)
        boolean okToLogEmergencyNumber = false;
        CarrierConfigManager configManager = (CarrierConfigManager) mContext.getSystemService(
                Context.CARRIER_CONFIG_SERVICE);
        PersistableBundle configBundle = configManager.getConfig();
        if (configBundle != null) {
            okToLogEmergencyNumber = configBundle.getBoolean(
                    CarrierConfigManager.KEY_ALLOW_EMERGENCY_NUMBERS_IN_CALL_LOG_BOOL);
        }

        // Don't log emergency numbers if the device doesn't allow it.
        final boolean isOkToLogThisCall = !isEmergency || okToLogEmergencyNumber;

        sendAddCallBroadcast(callType, duration);

        if (isOkToLogThisCall) {
            Log.d(TAG, "Logging Calllog entry: " + callerInfo + ", "
                    + Log.pii(number) + "," + presentation + ", " + callType
                    + ", " + start + ", " + duration + ", " + conferenceCallLogId);
            AddCallArgs args = new AddCallArgs(mContext, callerInfo, number, postDialDigits,
                    viaNumber, presentation, callType, features, accountHandle, start, duration,
                    dataUsage, initiatingUser, logCallCompletedListener,
                    conferenceCallLogId /* M: For Conference call */);
            logCallAsync(args);
        } else {
          Log.d(TAG, "Not adding emergency call to call log.");
        }
    }

    /**
     * Based on the video state of the call, determines the call features applicable for the call.
     *
     * @param videoState The video state.
     * @param isPulledCall {@code true} if this call was pulled to another device.
     * @return The call features.
     */
    private static int getCallFeatures(int videoState, boolean isPulledCall) {
        int features = 0;
        if (VideoProfile.isVideo(videoState)) {
            features |= Calls.FEATURES_VIDEO;
        }
        if (isPulledCall) {
            features |= Calls.FEATURES_PULLED_EXTERNALLY;
        }
        return features;
    }

    /**
     * Retrieve the phone number from the call, and then process it before returning the
     * actual number that is to be logged.
     *
     * @param call The phone connection.
     * @return the phone number to be logged.
     */
    private String getLogNumber(Call call) {
        Uri handle = call.getOriginalHandle();

        if (handle == null) {
            return null;
        }

        String handleString = handle.getSchemeSpecificPart();
        if (!PhoneNumberUtils.isUriNumber(handleString)) {
            handleString = PhoneNumberUtils.stripSeparators(handleString);
        }
        /// M: ALPS02795957, Avoid to log duplicated post dial digits. @{
        String postDial = call.getPostDialDigits();
        if (!TextUtils.isEmpty(postDial)
                && postDial.equals(PhoneNumberUtils.extractPostDialPortion(handleString))) {
            handleString = PhoneNumberUtils.extractNetworkPortionAlt(handleString);
            Log.d(TAG, "Remove duplicate post dial digits: " + postDial);
        }
        /// @}
        return handleString;
    }

    /**
     * Adds the call defined by the parameters in the provided AddCallArgs to the CallLogProvider
     * using an AsyncTask to avoid blocking the main thread.
     *
     * @param args Prepopulated call details.
     * @return A handle to the AsyncTask that will add the call to the call log asynchronously.
     */
    public AsyncTask<AddCallArgs, Void, Uri[]> logCallAsync(AddCallArgs args) {
        return new LogCallAsyncTask().execute(args);
    }

    /**
     * Helper AsyncTask to access the call logs database asynchronously since database operations
     * can take a long time depending on the system's load. Since it extends AsyncTask, it uses
     * its own thread pool.
     */
    private class LogCallAsyncTask extends AsyncTask<AddCallArgs, Void, Uri[]> {
        /// M: For conference SRVCC
        private AddCallArgs[] mAddCallArgs = null;

        private LogCallCompletedListener[] mListeners;

        @Override
        protected Uri[] doInBackground(AddCallArgs... callList) {
            mAddCallArgs = callList;
            int count = callList.length;
            Uri[] result = new Uri[count];
            mListeners = new LogCallCompletedListener[count];
            for (int i = 0; i < count; i++) {
                AddCallArgs c = callList[i];
                mListeners[i] = c.logCallCompletedListener;
                try {
                    // May block.
                    result[i] = addCall(c);
                } catch (Exception e) {
                    // This is very rare but may happen in legitimate cases.
                    // E.g. If the phone is encrypted and thus write request fails, it may cause
                    // some kind of Exception (right now it is IllegalArgumentException, but this
                    // might change).
                    //
                    // We don't want to crash the whole process just because of that, so just log
                    // it instead.
                    Log.e(TAG, e, "Exception raised during adding CallLog entry.");
                    result[i] = null;
                }
            }
            return result;
        }

        private Uri addCall(AddCallArgs c) {
            PhoneAccount phoneAccount = mPhoneAccountRegistrar
                    .getPhoneAccountUnchecked(c.accountHandle);
            if (phoneAccount != null &&
                    phoneAccount.hasCapabilities(PhoneAccount.CAPABILITY_MULTI_USER)) {
                ///M:add log
                Log.i(TAG, "addCallLog, c.initiatingUser=" + c.initiatingUser);
                if (c.initiatingUser != null &&
                        UserUtil.isManagedProfile(mContext, c.initiatingUser)) {
                    return addCall(c, c.initiatingUser);
                } else {
                    return addCall(c, null);
                }
            } else {
                return addCall(c, c.accountHandle == null ? null : c.accountHandle.getUserHandle());
            }
        }

        /**
         * Insert the call to a specific user or all users except managed profile.
         * @param c context
         * @param userToBeInserted user handle of user that the call going be inserted to. null
         *                         if insert to all users except managed profile.
         */
        private Uri addCall(AddCallArgs c, UserHandle userToBeInserted) {
            ///M:add log
            Log.i(TAG, "addCallLog, userToBeInserted=" + userToBeInserted);
            /// M: For Volte conference call calllog
            long conferenceCallLogId = getConferenceCallLogId(c.conferenceCallLogId);
            return Calls.addCall(c.callerInfo, c.context, c.number, c.postDialDigits, c.viaNumber,
                    c.presentation, c.callType, c.features, c.accountHandle, c.timestamp,
                    c.durationInSec, c.dataUsage, userToBeInserted == null,
                    userToBeInserted, false/* M: is read */,
                    conferenceCallLogId/* M: For Volte conference call calllog */);
        }


        @Override
        protected void onPostExecute(Uri[] result) {
            for (int i = 0; i < result.length; i++) {
                Uri uri = result[i];
                /*
                 Performs a simple sanity check to make sure the call was written in the database.
                 Typically there is only one result per call so it is easy to identify which one
                 failed.
                 */
                if (uri == null) {
                    Log.w(TAG, "Failed to write call to the log.");
                }
                if (mListeners[i] != null) {
                    mListeners[i].onLogCompleted(uri);
                }
            }
            /// M: If it was conference child, we record the Uri used for
            /// conference SRVCC case. @{
            for (int i = 0; i < mAddCallArgs.length; i++) {
                AddCallArgs c = mAddCallArgs[i];
                if (c.conferenceCallLogId > 0 && result[i] != null) {
                    updateSrvccConferenceCallLogs(c.conferenceCallLogId, c.number, result[i]);
                }
            }
            /// @}
        }
    }

    private void sendAddCallBroadcast(int callType, long duration) {
        Intent callAddIntent = new Intent(ACTION_CALLS_TABLE_ADD_ENTRY);
        callAddIntent.putExtra(CALL_TYPE, callType);
        callAddIntent.putExtra(CALL_DURATION, duration);
        mContext.sendBroadcast(callAddIntent, PERMISSION_PROCESS_CALLLOG_INFO);
    }

    private String getCountryIsoFromCountry(Country country) {
        if(country == null) {
            // Fallback to Locale if there are issues with CountryDetector
            Log.w(TAG, "Value for country was null. Falling back to Locale.");
            return Locale.getDefault().getCountry();
        }

        return country.getCountryIso();
    }

    /**
     * Get the current country code
     *
     * @return the ISO 3166-1 two letters country code of current country.
     */
    public String getCountryIso() {
        synchronized (mLock) {
            if (mCurrentCountryIso == null) {
                Log.i(TAG, "Country cache is null. Detecting Country and Setting Cache...");
                final CountryDetector countryDetector =
                        (CountryDetector) mContext.getSystemService(Context.COUNTRY_DETECTOR);
                Country country = null;
                if (countryDetector != null) {
                    country = countryDetector.detectCountry();

                    countryDetector.addCountryListener((newCountry) -> {
                        Log.startSession("CLM.oCD");
                        try {
                            synchronized (mLock) {
                                Log.i(TAG, "Country ISO changed. Retrieving new ISO...");
                                mCurrentCountryIso = getCountryIsoFromCountry(newCountry);
                            }
                        } finally {
                            Log.endSession();
                        }
                    }, Looper.getMainLooper());
                }
                mCurrentCountryIso = getCountryIsoFromCountry(country);
            }
            return mCurrentCountryIso;
        }
    }

    /**
     * M: ALPS01899538, add for getting scheme from call.
     * @param call
     * @return the phone number scheme to be logged.
     */
    private String getLogScheme(Call call) {
        Uri handle = call.getOriginalHandle();

        if (handle == null) {
            return null;
        }
        String scheme = handle.getScheme();
        return scheme;
    }

    /// M: Show call duration @{
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private void showCallDuration(Call call) {
        final long callDuration = call.getAgeMillis();

        Log.d(TAG, "showCallDuration: " + callDuration);

        if (callDuration / 1000 != 0) {
            // Must post to main thread because this method called in binder call thread
            mHandler.post(new java.lang.Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, getFormateDuration((int) (callDuration / 1000)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getFormateDuration(long duration) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

        if (duration >= 3600) {
            hours = duration / 3600;
            minutes = (duration - hours * 3600) / 60;
            seconds = duration - hours * 3600 - minutes * 60;
        } else if (duration >= 60) {
            minutes = duration / 60;
            seconds = duration - minutes * 60;
        } else {
            seconds = duration;
        }

        String duration_title = mContext.getResources().getString(R.string.call_duration_title);
        String duration_content = mContext.getResources().getString(R.string.call_duration_format, hours, minutes, seconds);
        return  duration_title + " (" + duration_content + ")";
    }
    /// @}

    /// M: For Volte conference call calllog @{
    // For skip the duration toast of IMS merged calls
    private static final String IMS_MERGED_SUCCESSFULLY = "IMS_MERGED_SUCCESSFULLY";

    private HashMap<Long, Long> mConferenceCallLogIdMap = new HashMap<Long, Long>();

    private synchronized long getConferenceCallLogId(long tempConferenceId) {
        if (tempConferenceId <= 0) {
            return 0;
        }
        Long confCallLogId = mConferenceCallLogIdMap.get(tempConferenceId);
        if (confCallLogId == null || confCallLogId <= 0) {
            // Conference has not save calllog into database, save it now
            ContentValues values = new ContentValues();
            // The temp conference id is conference creating time
            values.put(ConferenceCalls.CONFERENCE_DATE, tempConferenceId);
            Uri confUri = null;
            try {
                confUri = mContext.getContentResolver().insert(
                        ConferenceCalls.CONTENT_URI, values);
            } catch (Exception e) {
                Log.e(TAG, e, "Exception raised during adding conference entry.");
                return 0;
            }
            try {
                confCallLogId = ContentUris.parseId(confUri);
            } catch (Exception ex) {
                Log.e(TAG, ex, "Failed to parse conference saved uri:" + confUri);
                return 0;
            }
            if (confCallLogId == null || confCallLogId <= 0) {
                Log.d(TAG, "Invalid saved conference Id: " + confCallLogId);
                return 0;
            }
            mConferenceCallLogIdMap.put(tempConferenceId, confCallLogId);
            Log.d(TAG, "Temp conference Id: " + tempConferenceId + " Map to " + confCallLogId);
        }
        return confCallLogId;
    }

    /**
     * M: In order to save the relationships between the conference and its
     * participants into callLog. We put the conference callLog id into the the
     * participant call while this call be changed to a participant of the
     * conference. And if the conference has no conference callLog id, we save
     * the conference into database first to get its callLog id.
     */
    @Override
    public void onIsConferencedChanged(Call call) {
        Log.d(TAG, "onIsConferencedChanged Call: " + call);
        // If the call is conference call child
        Call confCall = call.getParentCall();
        if (confCall != null) {
            // It is conference call child
            long confCallLogId = confCall.getConferenceCallLogId();
            if (confCallLogId <= 0) {
                // Use conference create time as the conference temp id
                confCallLogId = confCall.getCreationTimeMillis();
                confCall.setConferenceCallLogId(confCallLogId);
                Log.d(TAG, "New conference, set a new temp id: " + confCallLogId);
                /// M: For Volte conference SRVCC case. Now the phone only support
                /// one conference call existing simultaneously. So, Clear the
                /// previous residual info while new conference setup. @{
                Log.d(TAG, "Clear mSrvccConferenceCallLogs");
                synchronized (mSrvccConferenceCallLogs) {
                    mSrvccConferenceCallLogs.clear();
                }
                /// @}
            }
            Log.d(TAG, "Set temp conference Id:" + confCallLogId + " to Call:" + call.getId());
            call.setConferenceCallLogId(confCallLogId);
        }
    }
    /// @}

    /// M: For Volte conference SRVCC case. @{
    /**
     * The Volte conference would not be disconnected while SRVCC occurred. But
     * the children IMS calls would be disconnected and changed to new CS calls.
     * If use the normal logging method a child SRVCC call would be logged as
     * two callLogs. It is bad user experiences. So, the child IMS call and CS
     * call should merge into one callLog. Here is the logic to implement this
     * feature. First we record the IMS call numbers and their callLog Uris into
     * a memory map. Then find the corresponding Uri from the map according the
     * call number while the CS call disconnecting. Finally, use the Uri to
     * update the callLog info, such as duration.
     */
    private HashMap<Long, HashMap<String, Uri>> mSrvccConferenceCallLogs =
            new HashMap<Long, HashMap<String, Uri>>();

    private void addSrvccConferenceCallLogs(long conferenceCallLogId,
            String logNumber, Uri callLogUri) {
        Log.d(TAG, "addSrvccConferenceCallLogs confId: " + conferenceCallLogId
                + ", logNumber:" + logNumber + ", callLogUri:" + callLogUri);
        if (TextUtils.isEmpty(logNumber)) {
            return;
        }
        synchronized (mSrvccConferenceCallLogs) {
            HashMap<String, Uri> callLogs = mSrvccConferenceCallLogs.get(conferenceCallLogId);
            if (callLogs == null) {
                callLogs = new HashMap<String, Uri>();
                mSrvccConferenceCallLogs.put(conferenceCallLogId, callLogs);
            }
            callLogs.put(logNumber, callLogUri);
        }
    }

    private void updateSrvccConferenceCallLogs(long conferenceCallLogId, String logNumber,
            Uri callLogUri) {
        Log.d(TAG, "updateSrvccConferenceCallLogs confId: " + conferenceCallLogId
                + ", logNumber:" + logNumber + ", callLogUri:" + callLogUri);
        if (TextUtils.isEmpty(logNumber)) {
            return;
        }
        synchronized (mSrvccConferenceCallLogs) {
            HashMap<String, Uri> callLogs = mSrvccConferenceCallLogs.get(conferenceCallLogId);
            if (callLogs != null) {
                callLogs.put(logNumber, callLogUri);
            }
        }
    }

    private Uri removeSrvccConferenceCallLogs(
            long conferenceCallLogId, String logNumber) {
        Log.d(TAG, "removeSrvccConferenceCallLogs confId: " + conferenceCallLogId
                + ", logNumber:" + logNumber);
        synchronized (mSrvccConferenceCallLogs) {
            HashMap<String, Uri> callLogs = mSrvccConferenceCallLogs.get(conferenceCallLogId);
            if (callLogs == null || TextUtils.isEmpty(logNumber)) {
                return null;
            }
            String removedCallLogNumber = null;
            for (String number : callLogs.keySet()) {
                if (logNumber.equals(number)
                        || logNumber.equals(PhoneNumberUtils
                                .getUsernameFromUriNumber(number))) {
                    removedCallLogNumber = number;
                    break;
                }
            }
            if (removedCallLogNumber != null) {
                Log.d(TAG, "removeSrvccConferenceCallLogs"
                        + " removedCallLogNumber:" + removedCallLogNumber);
                return callLogs.remove(removedCallLogNumber);
            }
        }
        return null;
    }

    /**
     * M: Handle the Volte conference SRVCC case.
     * @param childCall the conference child call.
     * @param childCall the call number to be logged.
     * @return true if the its conference SRVCC CS child call and be updated into callLog.
     */
    private boolean handleConferenceSrvccCallLog(Call childCall, String logNumber) {
        if (childCall.getConferenceCallLogId() <= 0) {
            return false;
        }
        if (childCall.hasProperty(Connection.PROPERTY_VOLTE)) {
            // It is IMS call and it may be changed to be CS call at SRVCC case.
            // So, temporarily record it.
            addSrvccConferenceCallLogs(childCall.getConferenceCallLogId(),
                    logNumber, null);
            return false;
        }
        if (!childCall.hasProperty(Connection.PROPERTY_VOLTE)) {
            // It is CS call. Find the previous relative IMS call, if we found
            // update the Call data into IMS call data.
            Uri callLogUri = removeSrvccConferenceCallLogs(
                    childCall.getConferenceCallLogId(), logNumber);
            if (callLogUri != null) {
                new UpdateConferenceCallLogAsyncTask(childCall.getContext(),
                        childCall, callLogUri).execute();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * The AsyncTask to update the conference callLog info. Now we only update the duration.
     */
    private class UpdateConferenceCallLogAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Call mChildCall;
        private final Context mContext;
        private final Uri mCallLogUri;

        UpdateConferenceCallLogAsyncTask(Context context, Call newChildCall, Uri callLogUri) {
            mContext = context;
            mChildCall = newChildCall;
            mCallLogUri = callLogUri;
        }

        @Override
        protected Void doInBackground(Void... args) {
            Cursor c = null;
            try {
                c = mContext.getContentResolver().query(mCallLogUri,
                        new String[] { Calls.DURATION }, null, null, null);
                c.moveToFirst();
                // call duration in seconds
                long duration = c.getLong(0) + (mChildCall.getAgeMillis() / 1000);
                ContentValues values = new ContentValues();
                values.put(Calls.DURATION, duration);
                Log.d(TAG, "Update " + mCallLogUri + " with duration=" + duration);
                mContext.getContentResolver().update(mCallLogUri, values, null, null);
            } catch (Exception e) {
                Log.e(TAG, e, "Exception raised during update conference CallLog.");
            } finally {
                if (c != null) {
                    c.close();
                }
            }
            return null;
        }
    }
    /// @}
}
