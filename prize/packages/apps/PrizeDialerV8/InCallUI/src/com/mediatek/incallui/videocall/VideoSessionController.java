package com.mediatek.incallui.videocall;

import android.os.SystemClock;

import android.telecom.VideoProfile;
import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.CallTimer;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallVideoCallCallbackNotifier;
import com.android.incallui.Log;

import com.google.common.base.Preconditions;
import com.mediatek.incallui.ext.ExtensionManager;

/**
 * M: [Video Call] A helper to downgrade video call if necessary.
 * Especially downgrade when UI in background or quit.
 */
public class VideoSessionController implements InCallPresenter.InCallStateListener,
        InCallPresenter.IncomingCallListener,
        InCallVideoCallCallbackNotifier.SessionModificationListener {
    private static final boolean DEBUG = true;
    private static final int DEFAULT_COUNT_DOWN_SECONDS = 20;
    ///M:add for upgrade recevied timeout
    private static final int COUNT_DOWN_SECONDS_FOR_RECEVICE_UPGRADE_WITH_PRECONDITION = 15;
    private static final long MILLIS_PER_SECOND = 1000;
    private static VideoSessionController sInstance;
    private InCallPresenter mInCallPresenter;
    private Call mPrimaryCall;
    private AutoDeclineTimer mAutoDeclineTimer = new AutoDeclineTimer();

    //M:the event to start timer of cancel upgrade
    public static final int SESSION_EVENT_NOTIFY_START_TIMER_20S = 1013;

    private VideoSessionController() {
        // do nothing
    }

    /**
     * M: get the VideoSessionController instance.
     * @return the instance.
     */
    public static VideoSessionController getInstance() {
        if (sInstance == null) {
            sInstance = new VideoSessionController();
        }
        return sInstance;
    }

    /**
     * M: setup when InCallPresenter setUp.
     * @param inCallPresenter the InCallPresenter instance.
     */
    public void setUp(InCallPresenter inCallPresenter) {
        logd("setUp");
        mInCallPresenter = Preconditions.checkNotNull(inCallPresenter);
        mInCallPresenter.addListener(this);
        mInCallPresenter.addIncomingCallListener(this);

        //register session modification listener for local.
        InCallVideoCallCallbackNotifier.getInstance().addSessionModificationListener(this);
    }

    /**
     * M: tearDown when InCallPresenter tearDown.
     */
    public void tearDown() {
        logd("tearDown...");
        mInCallPresenter.removeListener(this);
        mInCallPresenter.removeIncomingCallListener(this);

        //unregister session modification listener.
        InCallVideoCallCallbackNotifier.getInstance().removeSessionModificationListener(this);

        clear();
    }

    /**
     * M: get the countdown second number.
     * @return countdown number.
     */
    public long getAutoDeclineCountdownSeconds() {
        return mAutoDeclineTimer.getAutoDeclineCountdown();
    }

    @Override
    public void onStateChange(InCallPresenter.InCallState oldState,
                              InCallPresenter.InCallState newState, CallList callList) {
        Call call;
        if (newState == InCallPresenter.InCallState.INCOMING) {
            call = callList.getIncomingCall();
        } else if (newState == InCallPresenter.InCallState.WAITING_FOR_ACCOUNT) {
            call = callList.getWaitingForAccountCall();
        } else if (newState == InCallPresenter.InCallState.PENDING_OUTGOING) {
            call = callList.getPendingOutgoingCall();
        } else if (newState == InCallPresenter.InCallState.OUTGOING) {
            call = callList.getOutgoingCall();
        } else {
            call = callList.getActiveOrBackgroundCall();
        }

        if (!Call.areSame(call, mPrimaryCall)) {
            onPrimaryCallChanged(call);
        }
        /// M: ALPS03366927.when incallactivity on destory,the callbutton presenter will remove
        /// listner.so when video state changes,the callbuttonpresenter will not be notified by
        /// incallpresenter.so the sessiomModificationState can't be clear.when upgrade request
        /// comes,it will be declined.so remove this function from callbuttonpresenter to here.@
        updateVideoCallSessionState(call);
        /// @}
    }

    @Override
    public void onIncomingCall(InCallPresenter.InCallState oldState,
                               InCallPresenter.InCallState newState, Call call) {
        if (!Call.areSame(call, mPrimaryCall)) {
            onPrimaryCallChanged(call);
        }
    }

    /**
     * M: When upgrade request received, start timing.
     * @param call the call upgrading.
     */
    public void startTimingForAutoDecline(Call call) {
        logi("[startTimingForAutoDecline] for call: " + getId(call));
        if (!Call.areSame(call, mPrimaryCall)) {
            Log.e(this, "[startTimingForAutoDecline]Abnormal case for a non-primary call " +
                    "receiving upgrade request.");
            onPrimaryCallChanged(call);
        }
        mAutoDeclineTimer.startTiming();
    }

    /**
     * M: stop timing when the request accepted or declined.
     */
    public void stopTiming() {
        mAutoDeclineTimer.stopTiming();
    }

    private void onPrimaryCallChanged(Call call) {
        logi("[onPrimaryCallChanged] " + getId(mPrimaryCall) + " -> " + getId(call));
        if (call != null && mPrimaryCall != null && mPrimaryCall.getSessionModificationState()
                == Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST) {
            /**
             * force decline upgrade request if primary call changed.
             */
            mInCallPresenter.declineUpgradeRequest(mInCallPresenter.getContext());
        }
        mPrimaryCall = call;
    }

    private void clear() {
        mInCallPresenter = null;
        // when mInCallPresenter is null ,eg peer disconnect call,
        // local should stop timer.
        stopTiming();
    }

    private void logd(String msg) {
        if (DEBUG) {
            Log.d(this, msg);
        }
    }

    private void logw(String msg) {
        if (DEBUG) {
            Log.w(this, msg);
        }
    }

    private void logi(String msg) {
        Log.i(this, msg);
    }

    private static String getId(Call call) {
        return call == null ? "null" : call.getId();
    }

    @Override
    public void onUpgradeToVideoRequest(Call call, int videoState) {
        logd("onUpgradeToVideoRequest callId = " + getId(call) + " new video state = "
                + videoState);
        if (mPrimaryCall == null || !Call.areSame(mPrimaryCall, call)) {
            logw("UpgradeToVideoRequest received for non-primary call");
        }

        if (call == null) {
            logw("UpgradeToVideoRequest the current call is null");
            return;
        }

        call.setRequestedVideoState(videoState);
    }

    @Override
    public void onUpgradeToVideoSuccess(Call call) {
        logd("onUpgradeToVideoSuccess callId=" + getId(call));
        if (mPrimaryCall == null || !Call.areSame(mPrimaryCall, call)) {
            logw("onUpgradeToVideoSuccess received for non-primary call");
        }

        if (call == null) {
            logw("onUpgradeToVideoSuccess the current call is null");
            return;
        }

        /// fix ALPS02681041,show message only when upgrade to video from voice call successfully.
        /// M: [ALPS02671613] Changed to one-way state from 2-ways, nothing would happen, neither.
        /// TODO: Currently no well-support to one-way video call, so change from/to
        /// one-way video call success/fail would have no prompt.
        /// M: fix CR:ALPS02707358,shouldn't show "failed to switch video call" toast
        /// when call is disconnecting or call has been disconnected. @{
        if (VideoProfile.isAudioOnly(call.getModifyVideoStateFrom()) &&
                Call.State.isConnectingOrConnected(call.getState())) {
            InCallPresenter.getInstance().showMessage(
                    com.android.incallui.R.string.video_call_upgrade_to_video_call);
        } else {
            logd("onUpgradeToVideoSuccess call is disconnecting or call has been disconnected");
        }
        /// @}
        ///fix ALPS02497928,stop recording if switch to video call
        // requested by local.@{
        if (InCallPresenter.getInstance().isRecording()) {
            InCallPresenter.getInstance().stopVoiceRecording();
        }
        /// @}
    }

    @Override
    public void onUpgradeToVideoFail(int status, Call call) {
        logd("onUpgradeToVideoFail callId=" + getId(call));
        if (mPrimaryCall == null || !Call.areSame(mPrimaryCall, call)) {
            logw("onUpgradeToVideoFail received for non-primary call");
        }

        if (call == null) {
            logw("onUpgradeToVideoFail the current call is nul");
            return;
        }

        /// M: show message when upgrade to video fail
        /// TODO: Currently no well-support to one-way video call, so change from/to
        /// one-way video call success/fail would have no prompt.
        /// Note: there's no way to change from audio to one-way. So no prompt for "pause fail"
        /// neither. ref: [ALPS02704527]
        if (VideoProfile.isAudioOnly(call.getModifyVideoStateFrom())) {
            //show message when upgrade to video fail
            InCallPresenter.getInstance().showMessage(
                    com.android.incallui.R.string.video_call_upgrade_to_video_call_failed);
        }
    }

    @Override
    public void onDowngradeToAudio(Call call) {
        logd("[onDowngradeToAudio]for callId: " + getId(call));
        if (call == null) {
            logw("onDowngradeToAudio the current call is nul");
            return;
        }
        //reset hide preview flag
        call.setHidePreview(false);

        //Google source code
        call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);

        //show message when downgrade to voice
        // move this toast to Framework layer to show for cover more cases of video call
        //downgrade on OP01 project.ex:SRVCC,Change to voice call in call dailing,etc. @{
        if (ExtensionManager.getVideoCallExt().showToastForDowngrade()) {
            InCallPresenter.getInstance().showMessage(
                com.android.incallui.R.string.video_call_downgrade_to_voice_call);
        } else {
            logd("[onDowngradeToAudio] not show downgrade toast in here");
        }
        /// @}
        ExtensionManager.getInCallExt().maybeDismissBatteryDialog();
    }


     /**
     * M: when ony way remote side set SessionModificationState,
     * we change to default according to videostate
     *
     * @param call
     */
    private void updateVideoCallSessionState(Call call) {
        logd("[updateVideoCallSessionState] call=" + call);
        if (call == null) {
            return;
        }
        if (call.getSessionModificationState()
                == Call.SessionModificationState.WAITING_FOR_DOWNGRADE_RESPONSE) {
            if (call.getVideoState() == VideoProfile.STATE_AUDIO_ONLY) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        } else if (call.getSessionModificationState()
                == Call.SessionModificationState.WAITING_FOR_PAUSE_VIDEO_RESPONSE) {
            if (call.getVideoState() == VideoProfile.STATE_TX_ENABLED) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        } else if (call.getSessionModificationState()
                == Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST_ONE_WAY) {
            if (call.getVideoState() == VideoProfile.STATE_BIDIRECTIONAL) {
                call.setSessionModificationState(Call.SessionModificationState.NO_REQUEST);
            }
        }
    }

    /**
     * M: Timer to countdown.
     */
    private class AutoDeclineTimer {
        private int mCountdownSeconds;
        private CallTimer mTimer;
        private long mTimingStartMillis;
        private long mRemainSecondsBeforeDecline = -1;

        AutoDeclineTimer() {
            mTimer = new CallTimer(new Runnable() {
                @Override
                public void run() {
                    updateCountdown();
                }
            });
        }

        public void startTiming() {
            //TODO: customer might need some other value for this.
            ///M: change timer value for AT&T
            mCountdownSeconds = ExtensionManager.getVideoCallExt().getDeclineTimer();
            /// M: CMCC upgrade with precondition needs differenct timer.the timer of UE that sends
            /// upgrade request is 20 seconds.the timer of UE that receives upgrade request is 15
            ///seconds. @{
            if (mPrimaryCall != null && mPrimaryCall.getSessionModificationState() ==
                    Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST
                    && mPrimaryCall.getVideoFeatures().supportsCancelUpgradeVideo()) {
                mCountdownSeconds = COUNT_DOWN_SECONDS_FOR_RECEVICE_UPGRADE_WITH_PRECONDITION;
            }
            ///@}
            mRemainSecondsBeforeDecline = mCountdownSeconds;
            mTimingStartMillis = SystemClock.uptimeMillis();
            mTimer.start(MILLIS_PER_SECOND);
        }

        public long getAutoDeclineCountdown() {
            return mRemainSecondsBeforeDecline;
        }

        public void stopTiming() {
            mTimer.cancel();
            mRemainSecondsBeforeDecline = -1;
        }

        private void updateCountdown() {
            long currentMillis = SystemClock.uptimeMillis();
            long elapsedSeconds = (currentMillis - mTimingStartMillis) / MILLIS_PER_SECOND;
            if (elapsedSeconds > mCountdownSeconds) {
                if(mInCallPresenter == null) {
                    logd("[updateCountdown]mInCallPresenter is null return");
                    return;
                }

                /// M: When call is in cancel progress, the timeout requires send cancel
                /// upgrade request out. @{
                 if (mPrimaryCall != null && mPrimaryCall.getSessionModificationState() ==
                         Call.SessionModificationState.WAITING_FOR_UPGRADE_RESPONSE ) {
                      mInCallPresenter.cancelUpgradeRequest(mInCallPresenter.getContext());
               ///@}
                 } else {
                mInCallPresenter.declineUpgradeRequest(mInCallPresenter.getContext());
                 }
            } else {
                mRemainSecondsBeforeDecline = mCountdownSeconds - elapsedSeconds;
                    /// M: When call is in cancel progress, it doesn't need to update UI. @{
                    if (mPrimaryCall != null &&
                            mPrimaryCall.getSessionModificationState() !=
                            Call.SessionModificationState.RECEIVED_UPGRADE_TO_VIDEO_REQUEST ) {
                        logd("[updateCountdown]it didn't need show updateUI" );
                        return;
                    }
                    ///@}
                updateRelatedUi();
            }
        }

        private void updateRelatedUi() {
            logd("[updateRelatedUi]remain seconds: " + mRemainSecondsBeforeDecline);
            if(mInCallPresenter == null) {
                logd("[updateRelatedUi]mInCallPresenter is null return");
                return;
            }
            mInCallPresenter.onAutoDeclineCountdownChanged();
        }
    }

}
