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

package com.android.incallui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.telecom.CallAudioState;
import android.telecom.Connection;
import android.telecom.InCallService.VideoCall;
import android.telecom.VideoProfile;
import android.telecom.VideoProfile.CameraCapabilities;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.ImageView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.compat.CompatUtils;
import com.android.dialer.R;
import com.android.incallui.InCallPresenter.InCallDetailsListener;
import com.android.incallui.InCallPresenter.InCallOrientationListener;
import com.android.incallui.InCallPresenter.InCallStateListener;
import com.android.incallui.InCallPresenter.InCallVideoCallListener;
import com.android.incallui.InCallPresenter.IncomingCallListener;
import com.android.incallui.InCallVideoCallCallbackNotifier.SurfaceChangeListener;
import com.android.incallui.InCallVideoCallCallbackNotifier.VideoEventListener;
import com.mediatek.incallui.videocall.VideoDebugInfo;
import com.mediatek.incallui.wrapper.FeatureOptionWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Logic related to the {@link VideoCallFragment} and for managing changes to the video calling
 * surfaces based on other user interface events and incoming events from the
 * {@class VideoCallListener}.
 * <p>
 * When a call's video state changes to bi-directional video, the
 * {@link com.android.incallui.VideoCallPresenter} performs the following negotiation with the
 * telephony layer:
 * <ul>
 *     <li>{@code VideoCallPresenter} creates and informs telephony of the display surface.</li>
 *     <li>{@code VideoCallPresenter} creates the preview surface.</li>
 *     <li>{@code VideoCallPresenter} informs telephony of the currently selected camera.</li>
 *     <li>Telephony layer sends {@link CameraCapabilities}, including the
 *     dimensions of the video for the current camera.</li>
 *     <li>{@code VideoCallPresenter} adjusts size of the preview surface to match the aspect
 *     ratio of the camera.</li>
 *     <li>{@code VideoCallPresenter} informs telephony of the new preview surface.</li>
 * </ul>
 * <p>
 * When downgrading to an audio-only video state, the {@code VideoCallPresenter} nulls both
 * surfaces.
 */
public class VideoCallPresenter extends Presenter<VideoCallPresenter.VideoCallUi> implements
        IncomingCallListener, InCallOrientationListener, InCallStateListener,
        InCallDetailsListener, SurfaceChangeListener, VideoEventListener,
        InCallPresenter.InCallEventListener, InCallPresenter.InCallVideoCallListener {
    public static final String TAG = "VideoCallPresenter";

    public static final boolean DEBUG = true;
    /// M: Store the call id which had disconnected the surface.
    private static final Set<String> sDisconnectedSurfaceCallIds = Collections.newSetFromMap(
            new ConcurrentHashMap<String, Boolean>(8, 0.9f, 1));

    /**
     * Runnable which is posted to schedule automatically entering fullscreen mode.  Will not auto
     * enter fullscreen mode if the dialpad is visible (doing so would make it impossible to exit
     * the dialpad).
     */
    private Runnable mAutoFullscreenRunnable =  new Runnable() {
        @Override
        public void run() {
            if (mAutoFullScreenPending && !InCallPresenter.getInstance().isDialpadVisible()) {
                Log.v(this, "Automatically entering fullscreen mode.");
                InCallPresenter.getInstance().setFullScreen(true);
                mAutoFullScreenPending = false;
                ///M: auto Into FullScreen should also resize@{
                if (FeatureOptionWrapper.isSupportVideoDisplayTrans()) {
                    final VideoCallUi ui = getUi();
                    if (ui == null) {
                        Log.e(this, "autoFull Screen resize ui is null return");
                        return;
                    }
                    ui.resizeDisplaySurface();
                }
                ///@}
            } else {
                Log.v(this, "Skipping scheduled fullscreen mode.");
            }
        }
    };

    /**
     * Defines the state of the preview surface negotiation with the telephony layer.
     */
    private class PreviewSurfaceState {
        /**
         * The camera has not yet been set on the {@link VideoCall}; negotiation has not yet
         * started.
         */
        private static final int NONE = 0;

        /**
         * The camera has been set on the {@link VideoCall}, but camera capabilities have not yet
         * been received.
         */
        private static final int CAMERA_SET = 1;

        /**
         * The camera capabilties have been received from telephony, but the surface has not yet
         * been set on the {@link VideoCall}.
         */
        private static final int CAPABILITIES_RECEIVED = 2;

        /**
         * The surface has been set on the {@link VideoCall}.
         */
        private static final int SURFACE_SET = 3;
    }

    /**
     * The minimum width or height of the preview surface.  Used when re-sizing the preview surface
     * to match the aspect ratio of the currently selected camera.
     */
    private float mMinimumVideoDimension;

    /**
     * The current context.
     */
    private Context mContext;

    /**
     * The call the video surfaces are currently related to
     */
    private Call mPrimaryCall;

    /**
     * The {@link VideoCall} used to inform the video telephony layer of changes to the video
     * surfaces.
     */
    private VideoCall mVideoCall;

    /**
     * Determines if the current UI state represents a video call.
     */
    private int mCurrentVideoState;

    /**
     * Call's current state
     */
    private int mCurrentCallState = Call.State.INVALID;

    /**
     * Determines the device orientation (portrait/lanscape).
     */
    private int mDeviceOrientation = InCallOrientationEventListener.SCREEN_ORIENTATION_0;

    /**
     * Tracks the state of the preview surface negotiation with the telephony layer.
     */
    private int mPreviewSurfaceState = PreviewSurfaceState.NONE;

    private static boolean mIsVideoMode = false;

    /**
     * Contact photo manager to retrieve cached contact photo information.
     */
    private ContactPhotoManager mContactPhotoManager = null;

    /**
     * The URI for the user's profile photo, or {@code null} if not specified.
     */
    private ContactInfoCache.ContactCacheEntry mProfileInfo = null;

    /**
     * UI thread handler used for delayed task execution.
     */
    private Handler mHandler;

    /**
     * Determines whether video calls should automatically enter full screen mode after
     * {@link #mAutoFullscreenTimeoutMillis} milliseconds.
     */
    private boolean mIsAutoFullscreenEnabled = false;

    /**
     * Determines the number of milliseconds after which a video call will automatically enter
     * fullscreen mode.  Requires {@link #mIsAutoFullscreenEnabled} to be {@code true}.
     */
    private int mAutoFullscreenTimeoutMillis = 0;

    /**
     * Determines if the countdown is currently running to automatically enter full screen video
     * mode.
     */
    private boolean mAutoFullScreenPending = false;

    /// M: don't notify disconnecting video provider for more than once.
    private Call mLastDisconnectedCall;

    /**
     * M: judge current call's Held state is changed
     */
    private boolean mCurrentHeldState = false;

   //add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
    private boolean mCurrentVideoRingtoneState = false;
	//end

    /**
     * M: when EngineerMode for package loss on,
     * should new VideoDebugInfo, in order to show package loss info.
     */
    private VideoDebugInfo mDebugInfo = null;

    /**
     * Initializes the presenter.
     *
     * @param context The current context.
     */
    public void init(Context context) {
        mContext = context;
        mMinimumVideoDimension = mContext.getResources().getDimension(
                R.dimen.video_preview_small_dimension);
        mHandler = new Handler(Looper.getMainLooper());
        mIsAutoFullscreenEnabled = mContext.getResources()
                .getBoolean(R.bool.video_call_auto_fullscreen);
        mAutoFullscreenTimeoutMillis = mContext.getResources().getInteger(
                R.integer.video_call_auto_fullscreen_timeout);
    }

    /**
     * Called when the user interface is ready to be used.
     *
     * @param ui The Ui implementation that is now ready to be used.
     */
    @Override
    public void onUiReady(VideoCallUi ui) {
        super.onUiReady(ui);
        Log.d(this, "onUiReady:");

        // Do not register any listeners if video calling is not compatible to safeguard against
        // any accidental calls of video calling code.
        if (!CompatUtils.isVideoCompatible()) {
            return;
        }

        // Register for call state changes last
        InCallPresenter.getInstance().addListener(this);
        InCallPresenter.getInstance().addDetailsListener(this);
        InCallPresenter.getInstance().addIncomingCallListener(this);
        InCallPresenter.getInstance().addOrientationListener(this);
        // To get updates of video call details changes
        /// M: Duplicated invocation, remove it.
//        InCallPresenter.getInstance().addDetailsListener(this);
        InCallPresenter.getInstance().addInCallEventListener(this);
        //M:add listener here to do something with video
        InCallPresenter.getInstance().addInCallVideoCallListener(this);
        // Register for surface and video events from {@link InCallVideoCallListener}s.
        InCallVideoCallCallbackNotifier.getInstance().addSurfaceChangeListener(this);
        InCallVideoCallCallbackNotifier.getInstance().addVideoEventListener(this);
        ///M: when EngineerMode for package loss on, should show package loss info.
        if(VideoDebugInfo.isFeatureOn()){
            mDebugInfo = new VideoDebugInfo();
            InCallVideoCallCallbackNotifier.getInstance().addVideoEventListener(mDebugInfo);
        }
        ///@}

        mCurrentVideoState = VideoProfile.STATE_AUDIO_ONLY;
        mCurrentCallState = Call.State.INVALID;

        /**
         * M: [Video Call] bug fix: The VideoCallPresenter should force trigger onStateChange()
         * to refresh the UI immediately after its initialization. Otherwise the UI might not be
         * refreshed in time if no more InCallPresenter events.
         * It doesn't matter what the old state is, so we pass null. @{
         */
        onStateChange(null, InCallPresenter.getInstance().getInCallState(),
                CallList.getInstance());
        /** @} */

        /// M: fix CR:ALPS02510395,not update video Call UI. @{
        if (mPrimaryCall != null) {
            showVideoUi(mPrimaryCall, mPrimaryCall.getVideoState(), mPrimaryCall.getState());
        }
        /// @}
    }

    /**
     * Called when the user interface is no longer ready to be used.
     *
     * @param ui The Ui implementation that is no longer ready to be used.
     */
    @Override
    public void onUiUnready(VideoCallUi ui) {
        super.onUiUnready(ui);
        Log.d(this, "onUiUnready:");

        if (!CompatUtils.isVideoCompatible()) {
            return;
        }

        InCallPresenter.getInstance().removeListener(this);
        InCallPresenter.getInstance().removeDetailsListener(this);
        InCallPresenter.getInstance().removeIncomingCallListener(this);
        InCallPresenter.getInstance().removeOrientationListener(this);
        InCallPresenter.getInstance().removeInCallEventListener(this);
        /// M: Remove the listener
        InCallPresenter.getInstance().removeInCallVideoCallListener(this);

        InCallVideoCallCallbackNotifier.getInstance().removeSurfaceChangeListener(this);
        InCallVideoCallCallbackNotifier.getInstance().removeVideoEventListener(this);
        ///M: when EngineerMode for package loss on, should show package loss info.
        // so ui is unready also should delete this listener.
        if(VideoDebugInfo.isFeatureOn() && mDebugInfo != null){
            InCallVideoCallCallbackNotifier.getInstance().removeVideoEventListener(mDebugInfo);
        }
        ///@}
    }

    /**
     * Handles the creation of a surface in the {@link VideoCallFragment}.
     *
     * @param surface The surface which was created.
     */
    public void onSurfaceCreated(int surface) {
        Log.d(this, "onSurfaceCreated surface=" + surface + " mVideoCall=" + mVideoCall);
        Log.d(this, "onSurfaceCreated PreviewSurfaceState=" + mPreviewSurfaceState);
        Log.d(this, "onSurfaceCreated presenter=" + this);

        final VideoCallUi ui = getUi();
        if (ui == null || mVideoCall == null) {
            Log.w(this, "onSurfaceCreated: Error bad state VideoCallUi=" + ui + " mVideoCall="
                    + mVideoCall);
            return;
        }

        /// M: if the video call is in hold/held state, just ignore the video operations @{
        if (mPrimaryCall != null &&
                (Call.State.ONHOLD == mPrimaryCall.getState() || mPrimaryCall.isHeld())) {
            Log.w(this, "onSurfaceCreated: primary call " + mPrimaryCall.getId()
                    + " is in hold or held, ignore the video operations");
            return;
        }
        /// @}

        // If the preview surface has just been created and we have already received camera
        // capabilities, but not yet set the surface, we will set the surface now.
        if (surface == VideoCallFragment.SURFACE_PREVIEW ) {
            if (mPreviewSurfaceState == PreviewSurfaceState.CAPABILITIES_RECEIVED) {
                mPreviewSurfaceState = PreviewSurfaceState.SURFACE_SET;
                mVideoCall.setPreviewSurface(ui.getPreviewVideoSurface());
            } else if (mPreviewSurfaceState == PreviewSurfaceState.NONE && isCameraRequired()){
                /// M: force register video call back. @{
                if (mPrimaryCall != null) {
                    mPrimaryCall.registerVideoCallback();
                }
                /// @}
                enableCamera(mVideoCall, true);
            }
        } else if (surface == VideoCallFragment.SURFACE_DISPLAY) {
            mVideoCall.setDisplaySurface(ui.getDisplayVideoSurface());
        }
    }

    /**
     * Handles structural changes (format or size) to a surface.
     *
     * @param surface The surface which changed.
     * @param format The new PixelFormat of the surface.
     * @param width The new width of the surface.
     * @param height The new height of the surface.
     */
    public void onSurfaceChanged(int surface, int format, int width, int height) {
        //Do stuff
    }

    /**
     * Handles the destruction of a surface in the {@link VideoCallFragment}.
     * Note: The surface is being released, that is, it is no longer valid.
     *
     * @param surface The surface which was destroyed.
     */
    public void onSurfaceReleased(int surface) {
        Log.d(this, "onSurfaceReleased: mSurfaceId=" + surface);
        if ( mVideoCall == null) {
            Log.w(this, "onSurfaceReleased: VideoCall is null. mSurfaceId=" +
                    surface);
            return;
        }

        /// M: FIXME: when hangup, the mVideoCall would be set null once the primary call changed.
        /// So this API would never be called. As a result, the VideoProvider wouldn't know when
        /// to disconnect the Surface. We've added another caller in exitVideoMode(), and watching
        /// whether the code here can be removed.
        if (surface == VideoCallFragment.SURFACE_DISPLAY) {
            mVideoCall.setDisplaySurface(null);
        } else if (surface == VideoCallFragment.SURFACE_PREVIEW) {
            mVideoCall.setPreviewSurface(null);
            enableCamera(mVideoCall, false);
        }
    }

    /**
     * Called by {@link VideoCallFragment} when the surface is detached from UI (TextureView).
     * Note: The surface will be cached by {@link VideoCallFragment}, so we don't immediately
     * null out incoming video surface.
     * @see VideoCallPresenter#onSurfaceReleased(int)
     *
     * @param surface The surface which was detached.
     */
    public void onSurfaceDestroyed(int surface) {
        Log.d(this, "onSurfaceDestroyed: mSurfaceId=" + surface);
        if (mVideoCall == null) {
            return;
        }

        final boolean isChangingConfigurations =
                InCallPresenter.getInstance().isChangingConfigurations();
        Log.d(this, "onSurfaceDestroyed: isChangingConfigurations=" + isChangingConfigurations);

        if (surface == VideoCallFragment.SURFACE_PREVIEW) {
            if (!isChangingConfigurations) {
                enableCamera(mVideoCall, false);
            } else {
                Log.w(this, "onSurfaceDestroyed: Activity is being destroyed due "
                        + "to configuration changes. Not closing the camera.");
            }
        }
    }

    /**
     * Handles clicks on the video surfaces by toggling full screen state.
     * Informs the {@link InCallPresenter} of the change so that it can inform the
     * {@link CallCardPresenter} of the change.
     *
     * @param surfaceId The video surface receiving the click.
     */
    public void onSurfaceClick(int surfaceId) {
        /// M: [VideoCall]fixed for ALPS02304060,when only active state
        // can change fullscreen mode @{
        if(mPrimaryCall != null && mPrimaryCall.getState() == Call.State.ACTIVE){
            boolean isFullscreen = InCallPresenter.getInstance().toggleFullscreenMode();
            /// M: needs to resize display TextureView size. @{
            getUi().resizeDisplaySurface();
            /// @}
            Log.v(this, "toggleFullScreen = " + isFullscreen);
        }
        /// @}
    }

    /**
     * Handles incoming calls.
     *
     * @param oldState The old in call state.
     * @param newState The new in call state.
     * @param call The call.
     */
    @Override
    public void onIncomingCall(InCallPresenter.InCallState oldState,
            InCallPresenter.InCallState newState, Call call) {
        // same logic should happen as with onStateChange()
        onStateChange(oldState, newState, CallList.getInstance());
    }

    /**
     * Handles state changes (including incoming calls)
     *
     * @param newState The in call state.
     * @param callList The call list.
     */
    @Override
    public void onStateChange(InCallPresenter.InCallState oldState,
            InCallPresenter.InCallState newState, CallList callList) {
        Log.d(this, "onStateChange oldState" + oldState + " newState=" + newState +
                " isVideoMode=" + isVideoMode());

        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2017_3_8-start*/
        if (isVideoMode()) {
            getUi().updateVideoUIPrize(true);
            if (null != mContext) {
                int isSystemFlashOn = android.provider.Settings.System.getInt(mContext.getContentResolver(), android.provider.Settings.System.PRIZE_FLASH_STATUS, -1);
                if (isSystemFlashOn == 3 || isSystemFlashOn == 1 || isSystemFlashOn == 0) {
                    android.provider.Settings.System.putInt(mContext.getContentResolver(), android.provider.Settings.System.PRIZE_FLASH_STATUS, 2);
                }
            }
        }/* else {
            getUi().updateVideoUIPrize(false);
        }*/
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2017_3_8-end*/

        if (newState == InCallPresenter.InCallState.NO_CALLS) {
            if (isVideoMode()) {
                exitVideoMode();
            }

            cleanupSurfaces();
            /// M: no call exists, clear the stored IDs of disconnected call @{
            if (!sDisconnectedSurfaceCallIds.isEmpty()) {
                sDisconnectedSurfaceCallIds.clear();
                Log.d(this, "there is no call, clear IDs of disconnected call.");
            }
            /// @}
        }

        // Determine the primary active call).
        Call primary = null;

        // Determine the call which is the focus of the user's attention.  In the case of an
        // incoming call waiting call, the primary call is still the active video call, however
        // the determination of whether we should be in fullscreen mode is based on the type of the
        // incoming call, not the active video call.
        Call currentCall = null;
        Call disconnectedCall = null;
        if (newState == InCallPresenter.InCallState.INCOMING) {
            // We don't want to replace active video call (primary call)
            // with a waiting call, since user may choose to ignore/decline the waiting call and
            // this should have no impact on current active video call, that is, we should not
            // change the camera or UI unless the waiting VT call becomes active.
            primary = callList.getActiveCall();
            currentCall = callList.getIncomingCall();
            if (!VideoUtils.isActiveVideoCall(primary)) {
                primary = callList.getIncomingCall();
            }
        } else if (newState == InCallPresenter.InCallState.OUTGOING) {
            currentCall = primary = callList.getOutgoingCall();
        } else if (newState == InCallPresenter.InCallState.PENDING_OUTGOING) {
            currentCall = primary = callList.getPendingOutgoingCall();
        } else if (newState == InCallPresenter.InCallState.INCALL) {
            /// M: add pause controller to display video, when call hold
            //  we should update videoCall not exit VideoMode @{
            //currentCall = primary = callList.getActiveCall();
            currentCall = primary = callList.getActiveOrBackgroundCall();
            disconnectedCall = callList.getDisconnectedCall();
            /// @}
        }
        /// M: when the video call was disconnected, set the
        // preview/display surface to null. @{
        disconnectSurfaces(disconnectedCall, callList);
        /// @}

        final boolean primaryChanged = !Objects.equals(mPrimaryCall, primary);
        Log.d(this, "onStateChange primaryChanged=" + primaryChanged);
        Log.d(this, "onStateChange primary= " + primary);
        Log.d(this, "onStateChange mPrimaryCall = " + mPrimaryCall);
        if (primaryChanged) {
            onPrimaryCallChanged(primary);
        } else if (mPrimaryCall != null) {
            updateVideoCall(primary);
        /// M: locked device portrait if no calls exist. @{
        } else {
            /// false: without screen sensor works.
            InCallPresenter.getInstance().setInCallAllowsOrientationChange(false);
        /// @}
        }
        updateCallCache(primary);

        // If the call context changed, potentially exit fullscreen or schedule auto enter of
        // fullscreen mode.
        // If the current call context is no longer a video call, exit fullscreen mode.
        maybeExitFullscreen(currentCall);
        // Schedule auto-enter of fullscreen mode if the current call context is a video call
        maybeAutoEnterFullscreen(currentCall);
        //M: for ALPS02501750. update hide button for rotation.
        updatePreviewHideStatus();
    }

    /**
     * Handles a change to the fullscreen mode of the app.
     *
     * @param isFullscreenMode {@code true} if the app is now fullscreen, {@code false} otherwise.
     */
    @Override
    public void onFullscreenModeChanged(boolean isFullscreenMode) {
        cancelAutoFullScreen();
    }

    /**
     * Handles changes to the visibility of the secondary caller info bar.
     *
     * @param isVisible {@code true} if the secondary caller info is showing, {@code false}
     *      otherwise.
     * @param height the height of the secondary caller info bar.
     */
    @Override
    public void onSecondaryCallerInfoVisibilityChanged(boolean isVisible, int height) {
        Log.d(this,
                "onSecondaryCallerInfoVisibilityChanged : isVisible = " + isVisible + " height = "
                        + height);
        getUi().adjustPreviewLocation(isVisible /* shiftUp */, height);
    }

    private void checkForVideoStateChange(Call call) {
        final boolean isVideoCall = VideoUtils.isVideoCall(call);
        final boolean hasVideoStateChanged = mCurrentVideoState != call.getVideoState();

        Log.d(this, "checkForVideoStateChange: isVideoCall= " + isVideoCall
                + " hasVideoStateChanged=" + hasVideoStateChanged + " isVideoMode="
                + isVideoMode() + " previousVideoState: " +
                VideoProfile.videoStateToString(mCurrentVideoState) + " newVideoState: "
                + VideoProfile.videoStateToString(call.getVideoState()));

        if (!hasVideoStateChanged) {
            return;
        }

        updateCameraSelection(call);

        if (isVideoCall) {
            enterVideoMode(call);
        } else if (isVideoMode()) {
            exitVideoMode();
        }
    }

    /**
     * M: hide Local Preview according to the state hide.
     *
     */
    @Override
    public void onHidePreviewRequest(boolean hide) {
        final VideoCallUi ui = getUi();
        if (ui == null) {
            Log.w(this, "onHidePreviewRequest");
            return;
        }
        getUi().hidePreview(hide);
        if (mPrimaryCall != null) {
            mPrimaryCall.setHidePreview(hide);
        }
    }

    /**
     * M:FIXME update Local Preview hide or not
     * we should update this in showVideoUi in future.
     */
    public void updatePreviewHideStatus() {
        final VideoCallUi ui = getUi();
        if (ui == null) {
            Log.w(this, "onHidePreviewRequest");
            return;
        }
        if (mPrimaryCall != null && mPrimaryCall.isHidePreview()) {
            getUi().hidePreview(true);
        } else if (mPrimaryCall != null && VideoUtils.isVideoCall(mPrimaryCall)) {
            //FIXME when video call support 1A1H, like that active call hide preview , then
            // hold call become active, when should show local preview.
            getUi().hidePreview(false);
        }
    }
    /**
     * M: hide Local Preview according to the state hide.
     *
     */
    @Override
    public void disbaleFullScreen() {
        if(mAutoFullScreenPending) {
            Log.d(this,"callbuttonfragmet onclick called,cancel autofullscreen");
            mHandler.removeCallbacks(mAutoFullscreenRunnable);
            mAutoFullScreenPending = false;
        }
    }

    private void checkForCallStateChange(Call call) {
        final boolean isVideoCall = VideoUtils.isVideoCall(call);
        final boolean hasCallStateChanged = mCurrentCallState != call.getState();

        Log.d(this, "checkForCallStateChange: isVideoCall= " + isVideoCall
                + " hasCallStateChanged=" +
                hasCallStateChanged + " isVideoMode=" + isVideoMode());

        if (!hasCallStateChanged) {
            return;
        }

        if (isVideoCall) {
            /// M: [ALPS02412506]FIXME: workaround to notify MA that the call has resumed
            /// from Hold state. The MA need to be notified when the video starts to show.
            /// This might not be the right solution. @{
            if (mCurrentCallState == Call.State.ONHOLD && call.getState() == Call.State.ACTIVE) {
                Log.i(this, "[checkForCallStateChange]resume from onhold");
                enterVideoMode(call);
                return;
            }
            /// @}
            final InCallCameraManager cameraManager = InCallPresenter.getInstance().
                    getInCallCameraManager();

            String prevCameraId = cameraManager.getActiveCameraId();
            updateCameraSelection(call);
            String newCameraId = cameraManager.getActiveCameraId();

            if (!Objects.equals(prevCameraId, newCameraId) && VideoUtils.isActiveVideoCall(call)) {
                /// M: force register video call back.
                call.registerVideoCallback();
                enableCamera(call.getVideoCall(), true);
            }
        }

        // Make sure we hide or show the video UI if needed.
        ///M: when conference Video Call , we should hide preview @{
        //showVideoUi(call.getVideoState(), call.getState());
        showVideoUi(call, call.getVideoState(), call.getState());
        ///@}
    }

    private void cleanupSurfaces() {
        final VideoCallUi ui = getUi();
        if (ui == null) {
            Log.w(this, "cleanupSurfaces");
            return;
        }
        ui.cleanupSurfaces();
    }

    private void onPrimaryCallChanged(Call newPrimaryCall) {
        final boolean isVideoCall = VideoUtils.isVideoCall(newPrimaryCall);
        final boolean isVideoMode = isVideoMode();

        Log.d(this, "onPrimaryCallChanged: isVideoCall=" + isVideoCall + " isVideoMode="
                + isVideoMode);

        if (!isVideoCall && isVideoMode) {
            // Terminate video mode if new primary call is not a video call
            // and we are currently in video mode.
            Log.d(this, "onPrimaryCallChanged: Exiting video mode...");
            exitVideoMode();
        } else if (isVideoCall) {
            Log.d(this, "onPrimaryCallChanged: Entering video mode...");

            updateCameraSelection(newPrimaryCall);
            enterVideoMode(newPrimaryCall);
        }
    }

    private boolean isVideoMode() {
        return mIsVideoMode;
    }

    private void updateCallCache(Call call) {
        if (call == null) {
            mCurrentVideoState = VideoProfile.STATE_AUDIO_ONLY;
            mCurrentCallState = Call.State.INVALID;
            mVideoCall = null;
            mPrimaryCall = null;
        } else {
            mCurrentVideoState = call.getVideoState();
            mVideoCall = call.getVideoCall();
            mCurrentCallState = call.getState();
            mPrimaryCall = call;
        }
    }

    /**
     * Handles changes to the details of the call.  The {@link VideoCallPresenter} is interested in
     * changes to the video state.
     *
     * @param call The call for which the details changed.
     * @param details The new call details.
     */
    @Override
    public void onDetailsChanged(Call call, android.telecom.Call.Details details) {
        Log.d(this, " onDetailsChanged call=" + call + " details=" + details + " mPrimaryCall="
                + mPrimaryCall);
        if (call == null) {
            return;
        }
        // If the details change is not for the currently active call no update is required.
        if (!call.equals(mPrimaryCall)) {
            Log.d(this, " onDetailsChanged: Details not for current active call so returning. ");
            return;
        }

        updateVideoCall(call);

        updateCallCache(call);
    }

    private void updateVideoCall(Call call) {
        checkForVideoCallChange(call);
        checkForVideoStateChange(call);
        checkForCallStateChange(call);
        checkForOrientationAllowedChange(call);
        //M:when the local phone is in held, we also should showVideoUi
        checkForVideoHeldStateChange(call);
        //add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
        checkForVideoRingtoneStateChange(call);
        //end
    }

    private void checkForOrientationAllowedChange(Call call) {
        /*PRIZE-Delete-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
        /*InCallPresenter.getInstance().setInCallAllowsOrientationChange(CallUtils.isVideoCall(call)
                /// [Video Call]
                && call.getVideoFeatures().supportsRotation()
                /// M: change device orientation only video call is active
                && call.getState() == Call.State.ACTIVE);*/
        /*PRIZE-Delete-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
    }

    /**
     * Checks for a change to the video call and changes it if required.
     */
    private void checkForVideoCallChange(Call call) {
        final VideoCall videoCall = call.getTelecomCall().getVideoCall();
        Log.d(this, "checkForVideoCallChange: videoCall=" + videoCall + " mVideoCall="
                + mVideoCall);
        if (!Objects.equals(videoCall, mVideoCall)) {
            changeVideoCall(call);
        }
    }

    /**
     * Handles a change to the video call. Sets the surfaces on the previous call to null and sets
     * the surfaces on the new video call accordingly.
     *
     * @param videoCall The new video call.
     */
    private void changeVideoCall(Call call) {
        final VideoCall videoCall = call.getTelecomCall().getVideoCall();
        Log.d(this, "changeVideoCall to videoCall=" + videoCall + " mVideoCall=" + mVideoCall);
        // Null out the surfaces on the previous video call.
        if (mVideoCall != null) {
            // Log.d(this, "Null out the surfaces on the previous video call.");
            // mVideoCall.setDisplaySurface(null);
            // mVideoCall.setPreviewSurface(null);
        }

        final boolean hasChanged = mVideoCall == null && videoCall != null;

        mVideoCall = videoCall;
        if (mVideoCall == null || call == null) {
            Log.d(this, "Video call or primary call is null. Return");
            return;
        }

        if (VideoUtils.isVideoCall(call) && hasChanged) {
            enterVideoMode(call);
        }
    }

    private static boolean isCameraRequired(int videoState) {
        return VideoProfile.isBidirectional(videoState) ||
                VideoProfile.isTransmissionEnabled(videoState);
    }

    private boolean isCameraRequired() {
        return mPrimaryCall != null && isCameraRequired(mPrimaryCall.getVideoState());
    }

    /**
     * Enters video mode by showing the video surfaces and making other adjustments (eg. audio).
     * TODO(vt): Need to adjust size and orientation of preview surface here.
     */
    private void enterVideoMode(Call call) {
        VideoCall videoCall = call.getVideoCall();
        int newVideoState = call.getVideoState();

        Log.d(this, "enterVideoMode videoCall= " + videoCall + " videoState: " + newVideoState);
        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "Error VideoCallUi is null so returning");
            return;
        }
        ///M: when conference Video Call , we should hide preview @{
        //showVideoUi(newVideoState, call.getState());
        showVideoUi(call, newVideoState, call.getState());
        ///@}
        ui.updateVideoUIPrize(true);//PRIZE-add for video call-yuandailin-2016-7-6

        ///M: fix bug for ALPS02681048, when 1A1H, click secondary call and rotate screen
        //at same time, that will cause primary call change, then enter video mode set camera again,
        // cause MA module crash, so when in hold state can't enter video mode again.
        if(Call.State.ONHOLD == call.getState()) {
            Log.d(this, "the call is onhold not set surface and camera");
            mCurrentVideoState = newVideoState;
            maybeAutoEnterFullscreen(call);
            return;
        }
        ///@}

        // Communicate the current camera to telephony and make a request for the camera
        // capabilities.
        if (videoCall != null) {
            if (ui.isDisplayVideoSurfaceCreated()) {
                Log.d(this, "Calling setDisplaySurface with " + ui.getDisplayVideoSurface());
                videoCall.setDisplaySurface(ui.getDisplayVideoSurface());
            }

            videoCall.setDeviceOrientation(mDeviceOrientation);
            /// M: force register video call back.
            call.registerVideoCallback();
            enableCamera(videoCall, isCameraRequired(newVideoState));
        }
        mCurrentVideoState = newVideoState;

        mIsVideoMode = true;

        maybeAutoEnterFullscreen(call);
        /** M: [ALPS02812002] stop voice recording during vt call @{ */
        InCallPresenter presenter = InCallPresenter.getInstance();
        if (presenter.isRecording()) {
            presenter.stopVoiceRecording();
        }
        /** @} */
    }

    private static boolean isSpeakerEnabledForVideoCalls() {
        // TODO: Make this a carrier configurable setting. For now this is always true. b/20090407
        return true;
    }

    private void enableCamera(VideoCall videoCall, boolean isCameraRequired) {
        Log.d(this, "enableCamera: VideoCall=" + videoCall + " enabling=" + isCameraRequired);
        if (videoCall == null) {
            Log.w(this, "enableCamera: VideoCall is null.");
            return;
        }

        if (isCameraRequired) {
            InCallCameraManager cameraManager = InCallPresenter.getInstance().
                    getInCallCameraManager();
            videoCall.setCamera(cameraManager.getActiveCameraId());
            mPreviewSurfaceState = PreviewSurfaceState.CAMERA_SET;

            videoCall.requestCameraCapabilities();
        } else {
            mPreviewSurfaceState = PreviewSurfaceState.NONE;
            videoCall.setCamera(null);
        }
    }

    /**
     * Exits video mode by hiding the video surfaces and making other adjustments (eg. audio).
     */
    private void exitVideoMode() {
        Log.d(this, "exitVideoMode");
        ///M: when conference Video Call , we should hide preview @{
        //showVideoUi(VideoProfile.STATE_AUDIO_ONLY, Call.State.ACTIVE);
        showVideoUi(null, VideoProfile.STATE_AUDIO_ONLY, Call.State.ACTIVE);
        ///@}
        /*PRIZE-add for video call-yuandailin-2016-7-6-start*/
        VideoCallUi ui = getUi();
        if (ui != null) {
            ui.updateVideoUIPrize(false);
        }
        /*PRIZE-add for video call-yuandailin-2016-7-6-end*/
        enableCamera(mVideoCall, false);
        InCallPresenter.getInstance().setFullScreen(false);

        mIsVideoMode = false;
    }

    /**
     * Based on the current video state and call state, show or hide the incoming and
     * outgoing video surfaces.  The outgoing video surface is shown any time video is transmitting.
     * The incoming video surface is shown whenever the video is un-paused and active.
     *
     * @param videoState The video state.
     * @param callState The call state.
     */
    private void showVideoUi(Call call, int videoState, int callState) {
        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "showVideoUi, VideoCallUi is null returning");
            return;
        }
        boolean isPaused = VideoProfile.isPaused(videoState);
        boolean isCallActive = callState == Call.State.ACTIVE;
        /**
         * M: TODO: need review this part in future.
         * In MTK's solution, the Incoming video video should be shown as early as the call
         * launched. So that the Surface can be set to the VideoProvider early enough to setup
         * a video call session successfully. @{
         */
        isCallActive |= callState == Call.State.INCOMING;
        isCallActive |= callState == Call.State.DIALING;
        /** @} */
        ///M: when display surface is visible, we should avoid incoming
        // and dialing state display surface black. @{
        boolean transparent = callState == Call.State.INCOMING ||
		        //add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
		        //callState == Call.State.DIALING;
                (callState == Call.State.DIALING
                 && call != null
                 && call.can(android.telecom.Call.Details.CAPABILITY_VIDEO_RINGTONE) == false);
                //end

        ///M : when call state is hold, we should set the picture
        if (call != null && VideoUtils.isVideoCall(call)
                && (callState == Call.State.ONHOLD
                || (call.getDetails() != null
                && call.isHeld()))) {
            ui.showVideoViews(false, false, transparent);
            loadProfilePhotoAsync();
        } else {
            if (VideoProfile.isBidirectional(videoState)) {
                /// M: when conference call should hide preview@{
                ui.showVideoViews(true, !isPaused && isCallActive, transparent);
                if (call != null
                        && call.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)) {
                    ui.hidePreview(true);
                }
                /// @}
            } else if (VideoProfile.isTransmissionEnabled(videoState)) {
                /// M: add pause controller to display video@{
                ui.showVideoViews(true, false, transparent);
                if (call != null
                        && call.can(android.telecom.Call.Details.CAPABILITY_MANAGE_CONFERENCE)) {
                    ui.hidePreview(true);
                }
                /// @}
            } else if (VideoProfile.isReceptionEnabled(videoState)) {
				//add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
                //ui.showVideoViews(false, !isPaused && isCallActive, transparent);
                //loadProfilePhotoAsync();
                if (callState == Call.State.DIALING) {
                    ui.showVideoViews(true, !isPaused && isCallActive, transparent);
                } else {
                    ui.showVideoViews(false, !isPaused && isCallActive, transparent);
                }
                if (call != null
                        && call.can(android.telecom.Call.Details.CAPABILITY_VIDEO_RINGTONE)) {
                    ui.hidePreview(true);
                    call.setHidePreview(true);
                } else {
                    loadProfilePhotoAsync();
                }
				//end
            } else {
                ui.hideVideoUi();
            }
        }
        /// @}
        /// M: update peer dimension and angle @{
        if (VideoUtils.isVideoCall(call)) {
            Point dimension = call.getPeerDimension();
            if (dimension.x > 0 && dimension.y > 0) {
                ui.updatePeerDimensionAndAngle(dimension.x, dimension.y,
                        call.getPeerRotation());
            }
        }
        /// @}
        InCallPresenter.getInstance().enableScreenTimeout(
                VideoProfile.isAudioOnly(videoState));
    }

    /**
     * Determines if the incoming video surface should be shown based on the current videoState and
     * callState.  The video surface is shown when incoming video is not paused, the call is active,
     * and video reception is enabled.
     *
     * @param videoState The current video state.
     * @param callState The current call state.
     * @return {@code true} if the incoming video surface should be shown, {@code false} otherwise.
     */
    public static boolean showIncomingVideo(int videoState, int callState) {
        if (!CompatUtils.isVideoCompatible()) {
            return false;
        }

        boolean isPaused = VideoProfile.isPaused(videoState);
        boolean isCallActive = callState == Call.State.ACTIVE;

        return !isPaused && isCallActive && VideoProfile.isReceptionEnabled(videoState);
    }

    /**
     * Determines if the outgoing video surface should be shown based on the current videoState.
     * The video surface is shown if video transmission is enabled.
     *
     * @param videoState The current video state.
     * @return {@code true} if the the outgoing video surface should be shown, {@code false}
     *      otherwise.
     */
    public static boolean showOutgoingVideo(int videoState) {
        if (!CompatUtils.isVideoCompatible()) {
            return false;
        }

        return VideoProfile.isTransmissionEnabled(videoState);
    }

    /**
     * Handles peer video pause state changes.
     *
     * @param call The call which paused or un-pausedvideo transmission.
     * @param paused {@code True} when the video transmission is paused, {@code false} when video
     *               transmission resumes.
     */
    @Override
    public void onPeerPauseStateChanged(Call call, boolean paused) {
        if (!call.equals(mPrimaryCall)) {
            return;
        }

        // TODO(vt): Show/hide the peer contact photo.
    }

    /**
     * Handles peer video dimension changes.
     *
     * @param call The call which experienced a peer video dimension change.
     * @param width The new peer video width .
     * @param height The new peer video height.
     */
    @Override
    public void onUpdatePeerDimensions(Call call, int width, int height) {
        Log.d(this, "onUpdatePeerDimensions: width= " + width + " height= " + height);
        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "VideoCallUi is null. Bail out");
            return;
        }
        if (!call.equals(mPrimaryCall)) {
            Log.e(this, "Current call is not equal to primary call. Bail out");
            return;
        }

        // Change size of display surface to match the peer aspect ratio
        if (width > 0 && height > 0) {
            setDisplayVideoSize(width, height);
        }
    }

    /**
     * M: Handles peer video dimension changes.
     *
     * @param call The call which experienced a peer video rotation change.
     * @param width The new peer video width .
     * @param height The new peer video height.
     * @param height The new peer rotation angle.
     */
    @Override
    public void onPeerDimensionsWithAngleChanged(Call call, int width,
                                                 int height, int rotation) {
        /**
         * M: save the dimension and rotation for using later. if the UI isn't
         * ready or the current call is not the primary call, InCallUi would
         * ignore this update. But in some special case, once ignore, it would
         * make some mistakes. For example, there are two calls, one is a
         * conference video call (peer dimension [416, 224]) in background, and
         * the other is a vilte call in foreground (peer dimension [240, 320]),
         * and the screen size is [1080, 1776]. Then switch between these two
         * calls, the video of conference call would be enlarged. So in order to
         * avoid above case, save the dimension and angle to
         * com.android.inallui.call, and then update the dimension and angle
         * when trigger to show video call.
         * VidoeCallFregment.resizeDisplaySurface would be invoked when the view
         * is ready to be re-layout.
         *
         * @{
         */
        call.setPeerDimensionAndAngle(width, height, rotation);
        /** @} */
        Log.d(this, "onPeerDimensionsWithAngleChanged: width= " + width
                + " height= " + height + " rotation" + rotation);
        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "VideoCallUi is null. Bail out");
            return;
        }
        if (!call.equals(mPrimaryCall)) {
            Log.e(this, "Current call is not equal to primary call. Bail out");
            return;
        }

        // Change size of display surface to match the peer aspect ratio
        if (width > 0 && height > 0) {
            //setDisplayVideoSize(width, height);
            getUi().changeDisplayVideoWithAngle(width, height, rotation);
        }
    }

    /**
     * Handles any video quality changes in the call.
     *
     * @param call The call which experienced a video quality change.
     * @param videoQuality The new video call quality.
     */
    @Override
    public void onVideoQualityChanged(Call call, int videoQuality) {
        // No-op
    }

    /**
     * Handles a change to the dimensions of the local camera.  Receiving the camera capabilities
     * triggers the creation of the video
     *
     * @param call The call which experienced the camera dimension change.
     * @param width The new camera video width.
     * @param height The new camera video height.
     */
    @Override
    public void onCameraDimensionsChange(Call call, int width, int height) {
        Log.d(this, "onCameraDimensionsChange call=" + call + " width=" + width + " height="
                + height);
        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "onCameraDimensionsChange ui is null");
            return;
        }

        if (!call.equals(mPrimaryCall)) {
            Log.e(this, "Call is not primary call");
            return;
        }

        mPreviewSurfaceState = PreviewSurfaceState.CAPABILITIES_RECEIVED;
        changePreviewDimensions(width, height);

        // Check if the preview surface is ready yet; if it is, set it on the {@code VideoCall}.
        // If it not yet ready, it will be set when when creation completes.
        if (ui.isPreviewVideoSurfaceCreated()) {
            mPreviewSurfaceState = PreviewSurfaceState.SURFACE_SET;
            /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
            /*mVideoCall.setPreviewSurface(ui.getPreviewVideoSurface());*/
            if (null != mVideoCall) {
                mVideoCall.setPreviewSurface(ui.getPreviewVideoSurface());
            }
            /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/
        }
    }

    /**
     * Changes the dimensions of the preview surface.
     *
     * @param width The new width.
     * @param height The new height.
     */
    private void changePreviewDimensions(int width, int height) {
        VideoCallUi ui = getUi();
        if (ui == null) {
            return;
        }

        // Resize the surface used to display the preview video
        ui.setPreviewSurfaceSize(width, height);

        // Configure the preview surface to the correct aspect ratio.
        float aspectRatio = 1.0f;
        if (width > 0 && height > 0) {
            aspectRatio = (float) width / (float) height;
        }

        // Resize the textureview housing the preview video and rotate it appropriately based on
        // the device orientation
        setPreviewSize(mDeviceOrientation, aspectRatio);
    }

    /**
     * Called when call session event is raised.
     *
     * @param event The call session event.
     */
    @Override
    public void onCallSessionEvent(int event) {
        StringBuilder sb = new StringBuilder();
        sb.append("onCallSessionEvent = ");

        switch (event) {
            case Connection.VideoProvider.SESSION_EVENT_RX_PAUSE:
                sb.append("rx_pause");
                break;
            case Connection.VideoProvider.SESSION_EVENT_RX_RESUME:
                sb.append("rx_resume");
                break;
            case Connection.VideoProvider.SESSION_EVENT_CAMERA_FAILURE:
                sb.append("camera_failure");
                break;
            case Connection.VideoProvider.SESSION_EVENT_CAMERA_READY:
                sb.append("camera_ready");
                break;
            default:
                sb.append("unknown event = ");
                sb.append(event);
                break;
        }
        Log.d(this, sb.toString());
    }

    /**
     * Handles a change to the call data usage
     *
     * @param dataUsage call data usage value
     */
    @Override
    public void onCallDataUsageChange(long dataUsage) {
        Log.d(this, "onCallDataUsageChange dataUsage=" + dataUsage);
    }

    /**
     * Handles changes to the device orientation.
     * @param orientation The screen orientation of the device (one of:
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_0},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_90},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_180},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_270}).
     */
    @Override
    public void onDeviceOrientationChanged(int orientation) {
        mDeviceOrientation = orientation;

        VideoCallUi ui = getUi();
        if (ui == null) {
            Log.e(this, "onDeviceOrientationChanged: VideoCallUi is null");
            return;
        }

        Point previewDimensions = ui.getPreviewSize();
        if (previewDimensions == null) {
            return;
        }
        Log.d(this, "onDeviceOrientationChanged: orientation=" + orientation + " size: "
                + previewDimensions);
        changePreviewDimensions(previewDimensions.x, previewDimensions.y);

        /// M: Do not rotation surface view by InCallUI, MA takes the responsibility
        // ui.setPreviewRotation(mDeviceOrientation);
    }

    /**
     * Sets the preview surface size based on the current device orientation.
     * See: {@link InCallOrientationEventListener#SCREEN_ORIENTATION_0},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_90},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_180},
     * {@link InCallOrientationEventListener#SCREEN_ORIENTATION_270}).
     *
     * @param orientation The device orientation
     * @param aspectRatio The aspect ratio of the camera (width / height).
     */
    private void setPreviewSize(int orientation, float aspectRatio) {
        VideoCallUi ui = getUi();
        if (ui == null) {
            return;
        }
        Log.d(this, "[setPreviewSize]orientation: " + orientation
                + ", aspectRatio: " + aspectRatio
                + ", mMinimumVideoDimension: " + mMinimumVideoDimension);

        int height;
        int width;

        if (orientation == InCallOrientationEventListener.SCREEN_ORIENTATION_90 ||
                orientation == InCallOrientationEventListener.SCREEN_ORIENTATION_270) {
            width = (int) (mMinimumVideoDimension * aspectRatio);
            height = (int) mMinimumVideoDimension;
        } else {
            // Portrait or reverse portrait orientation.
            width = (int) mMinimumVideoDimension;
            height = (int) (mMinimumVideoDimension * aspectRatio);
        }
        ui.setPreviewSize(width, height);
    }

    /**
     * Sets the display video surface size based on peer width and height
     *
     * @param width peer width
     * @param height peer height
     */
    private void setDisplayVideoSize(int width, int height) {
        Log.v(this, "setDisplayVideoSize: Received peer width=" + width + " height=" + height);
        VideoCallUi ui = getUi();
        if (ui == null) {
            return;
        }

        // Get current display size
        Point size = ui.getScreenSize();
        Log.v(this, "setDisplayVideoSize: windowmgr width=" + size.x
                + " windowmgr height=" + size.y);
        if (size.y * width > size.x * height) {
            // current display height is too much. Correct it
            size.y = (int) (size.x * height / width);
        } else if (size.y * width < size.x * height) {
            // current display width is too much. Correct it
            size.x = (int) (size.y * width / height);
        }
        ui.setDisplayVideoSize(size.x, size.y);
    }

    /**
     * Exits fullscreen mode if the current call context has changed to a non-video call.
     *
     * @param call The call.
     */
    protected void maybeExitFullscreen(Call call) {
        if (call == null) {
            return;
        }

        /// M: when call is in held state ,we should exit full screen @{
        if (!VideoUtils.isVideoCall(call) || call.getState() == Call.State.INCOMING ||
              call.isHeld() || (getUi() != null && !getUi().isDisplayVisible())) {
            InCallPresenter.getInstance().setFullScreen(false);
        }
        ///@}
    }

    /**
     * Schedules auto-entering of fullscreen mode.
     * Will not enter full screen mode if any of the following conditions are met:
     * 1. No call
     * 2. Call is not active
     * 3. Call is not video call
     * 4. Already in fullscreen mode
     *
     * @param call The current call.
     */
    protected void maybeAutoEnterFullscreen(Call call) {
        if (!mIsAutoFullscreenEnabled) {
            return;
        }

        /// M: When call is held or peer isn't visible,it shouldn't enter full screen @{
        if (call == null || (
                call != null && (call.getState() != Call.State.ACTIVE ||
                        !VideoUtils.isVideoCall(call) || call.isHeld()) ||
                        InCallPresenter.getInstance().isFullscreen()) ||
                        (getUi() != null && !getUi().isDisplayVisible())) {
            // Ensure any previously scheduled attempt to enter fullscreen is cancelled.
            cancelAutoFullScreen();
            return;
        }
        /// @}

        if (mAutoFullScreenPending) {
            Log.v(this, "maybeAutoEnterFullscreen : already pending.");
            return;
        }
        Log.v(this, "maybeAutoEnterFullscreen : scheduled");
        mAutoFullScreenPending = true;
        mHandler.postDelayed(mAutoFullscreenRunnable, mAutoFullscreenTimeoutMillis);
    }

    /**
     * Cancels pending auto fullscreen mode.
     */
    public void cancelAutoFullScreen() {
        if (!mAutoFullScreenPending) {
            Log.v(this, "cancelAutoFullScreen : none pending.");
            return;
        }
        Log.v(this, "cancelAutoFullScreen : cancelling pending");
        mAutoFullScreenPending = false;
    }

    private static boolean isAudioRouteEnabled(int audioRoute, int audioRouteMask) {
        return ((audioRoute & audioRouteMask) != 0);
    }

    private static void updateCameraSelection(Call call) {
        Log.d(TAG, "updateCameraSelection: call=" + call);
        Log.d(TAG, "updateCameraSelection: call=" + toSimpleString(call));

        final Call activeCall = CallList.getInstance().getActiveCall();
        int cameraDir = Call.VideoSettings.CAMERA_DIRECTION_UNKNOWN;

        // this function should never be called with null call object, however if it happens we
        // should handle it gracefully.
        if (call == null) {
            cameraDir = Call.VideoSettings.CAMERA_DIRECTION_UNKNOWN;
            com.android.incallui.Log.e(TAG, "updateCameraSelection: Call object is null."
                    + " Setting camera direction to default value (CAMERA_DIRECTION_UNKNOWN)");
        }

        // Clear camera direction if this is not a video call.
        else if (VideoUtils.isAudioCall(call)) {
            cameraDir = Call.VideoSettings.CAMERA_DIRECTION_UNKNOWN;
            call.getVideoSettings().setCameraDir(cameraDir);
        }

        // If this is a waiting video call, default to active call's camera,
        // since we don't want to change the current camera for waiting call
        // without user's permission.
        else if (VideoUtils.isVideoCall(activeCall) && VideoUtils.isIncomingVideoCall(call)) {
            cameraDir = activeCall.getVideoSettings().getCameraDir();
        }

        // Infer the camera direction from the video state and store it,
        // if this is an outgoing video call.
        else if (VideoUtils.isOutgoingVideoCall(call) && !isCameraDirectionSet(call) ) {
            cameraDir = toCameraDirection(call.getVideoState());
            call.getVideoSettings().setCameraDir(cameraDir);
        }

        // Use the stored camera dir if this is an outgoing video call for which camera direction
        // is set.
        else if (VideoUtils.isOutgoingVideoCall(call)) {
            cameraDir = call.getVideoSettings().getCameraDir();
        }

        // Infer the camera direction from the video state and store it,
        // if this is an active video call and camera direction is not set.
        else if (VideoUtils.isActiveVideoCall(call) && !isCameraDirectionSet(call)) {
            cameraDir = toCameraDirection(call.getVideoState());
            call.getVideoSettings().setCameraDir(cameraDir);
        }

        // Use the stored camera dir if this is an active video call for which camera direction
        // is set.
        /// M: [ALPS02841616] Camera always switches to front one after resume
        else if (VideoUtils.isActiveVideoCall(call)
                || isCameraDirectionSet(call)) {
            cameraDir = call.getVideoSettings().getCameraDir();
        }

        // For all other cases infer the camera direction but don't store it in the call object.
        else {
            cameraDir = toCameraDirection(call.getVideoState());
        }

        com.android.incallui.Log.d(TAG, "updateCameraSelection: Setting camera direction to " +
                cameraDir + " Call=" + call);
        final InCallCameraManager cameraManager = InCallPresenter.getInstance().
                getInCallCameraManager();
        cameraManager.setUseFrontFacingCamera(cameraDir ==
                Call.VideoSettings.CAMERA_DIRECTION_FRONT_FACING);
    }
    /**
     * M: FIXME: changed google's default behavior.
     * We found a problem that when the call becomes a Tx call, the camera would automatically
     * changed to the rear one. Google might define this behavior for the user privacy.
     * But we think it isn't a good user experience to switch the camera unpredictable.
     * So we use the front one if no camera was specified by the user before.
     * if our customer wanted to follow google default behavior, just unmark the google code. @{
     */

    private static int toCameraDirection(int videoState) {
        /*return VideoProfile.isTransmissionEnabled(videoState) &&
                !VideoProfile.isBidirectional(videoState)
                ? Call.VideoSettings.CAMERA_DIRECTION_BACK_FACING
                : Call.VideoSettings.CAMERA_DIRECTION_FRONT_FACING;
        */
        return Call.VideoSettings.CAMERA_DIRECTION_FRONT_FACING;
    }

    private static boolean isCameraDirectionSet(Call call) {
        return VideoUtils.isVideoCall(call) && call.getVideoSettings().getCameraDir()
                    != Call.VideoSettings.CAMERA_DIRECTION_UNKNOWN;
    }

    private static String toSimpleString(Call call) {
        return call == null ? null : call.toSimpleString();
    }

    /**
     * Starts an asynchronous load of the user's profile photo.
     */
    public void loadProfilePhotoAsync() {
        final VideoCallUi ui = getUi();
        if (ui == null) {
            return;
        }

        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            /**
             * Performs asynchronous load of the user profile information.
             *
             * @param params The parameters of the task.
             *
             * @return {@code null}.
             */
            @Override
            protected Void doInBackground(Void... params) {
                if (mProfileInfo == null) {
                    // Try and read the photo URI from the local profile.
                    mProfileInfo = new ContactInfoCache.ContactCacheEntry();
                    ///M: when click hold button and rotate will crash @{
                    if (mContext == null || (mContext != null
                            && mContext.getContentResolver() == null)) {
                        return null;
                    }
                    /// @}
                    final Cursor cursor = mContext.getContentResolver().query(
                            ContactsContract.Profile.CONTENT_URI, new String[]{
                                    ContactsContract.CommonDataKinds.Phone._ID,
                                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                                    ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_ALTERNATIVE
                            }, null, null, null);
                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                mProfileInfo.lookupKey = cursor.getString(cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));
                                String photoUri = cursor.getString(cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                                mProfileInfo.displayPhotoUri = photoUri == null ? null
                                        : Uri.parse(photoUri);
                                mProfileInfo.namePrimary = cursor.getString(cursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                mProfileInfo.nameAlternative = cursor.getString(
                                        cursor.getColumnIndex(ContactsContract.CommonDataKinds
                                                        .Phone.DISPLAY_NAME_ALTERNATIVE));
                            }
                        } finally {
                            cursor.close();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                // If user profile information was found, issue an async request to load the user's
                // profile photo.
                if (mProfileInfo != null) {
                    if (mContactPhotoManager == null) {
                        mContactPhotoManager = ContactPhotoManager.getInstance(mContext);
                    }
                    ContactPhotoManager.DefaultImageRequest imageRequest = (mProfileInfo != null)
                            ? null :
                            new ContactPhotoManager.DefaultImageRequest(mProfileInfo.namePrimary,
                                    mProfileInfo.lookupKey, false /* isCircularPhoto */);

                    ImageView photoView = ui.getPreviewPhotoView();
                    if (photoView == null) {
                        return;
                    }
                    mContactPhotoManager.loadDirectoryPhoto(photoView,
                                    mProfileInfo.displayPhotoUri,
                                    false /* darkTheme */, false /* isCircular */, imageRequest);
                }
            }
        };

        task.execute();
    }

    /**
     * M: In MTK solution, the VideoProvider should be notified to stop MA by setting the
     * both Surfaces to null.
     * When the video call downgrades or disconnected, it should notify the VideoProvider.
     *
     * @param videoCall the VideoCall instance.
     */
    private void disconnectSurfaces(Call call, CallList callList) {
        if (call == null || call.getVideoCall() == null || callList == null) {
            return;
        }
        if (sDisconnectedSurfaceCallIds.contains(call.getId())) {
            Log.d(this,"disconnectSurfaces, already disconnected, ignore " + call.getId());
            return;
        }
        if (findVideoCallByVideoId(callList, call) == null) {
            call.getVideoCall().setDisplaySurface(null);
            call.getVideoCall().setPreviewSurface(null);
            sDisconnectedSurfaceCallIds.add(call.getId());
            Log.d(this,"disconnectSurfaces, clear surface for " + call.getId());
        } else {
            Log.d(this,"disconnectSurfaces, same VT call exists, ignore " + call.getId());
        }
    }

    /**
    * M:In MTK solution, when local call is held by remote, we should show video
    * as photo , when unhold, we will display as video.
    *
    * @param call
    */
   private void checkForVideoHeldStateChange(Call call) {
       android.telecom.Call.Details details = call.getDetails();
       if (details == null) {
           return;
       }

       boolean isVideoCallHeld = call.isHeld();
       final boolean isCallHeldStateChanged = mCurrentHeldState != isVideoCallHeld;
       if (!isCallHeldStateChanged) {
           return;
       }
       mCurrentHeldState = isVideoCallHeld;
       showVideoUi(call, call.getVideoState(), call.getState());
   }
    //add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
    private void checkForVideoRingtoneStateChange(Call call) {
        android.telecom.Call.Details details = call.getDetails();
        if (details == null) {
            return;
        }
        boolean hasVideoRingtone = details.can(
                android.telecom.Call.Details.CAPABILITY_VIDEO_RINGTONE);
        final boolean isVideoRingtoneStateChanged = mCurrentVideoRingtoneState != hasVideoRingtone;
        Log.d(this,"checkForVideoRingtoneStateChange : hasVideoRingtone = " + hasVideoRingtone +
                " isVideoRingtoneStateChanged = " + isVideoRingtoneStateChanged);
        if (!isVideoRingtoneStateChanged) {
            return;
        }
        if (hasVideoRingtone == false) {
            call.setHidePreview(false);
        }
        mCurrentVideoRingtoneState = hasVideoRingtone;
        showVideoUi(call, call.getVideoState(), call.getState());
    }
	//end
    /**
     * M: [ViLTE]when EngineerMode on we should get the video view to show some
     * debug message, here to get VideoDebugInfo instance.
     */
    public VideoDebugInfo getDebugInfo() {
        return mDebugInfo;
    }

    /**
     * Defines the VideoCallUI interactions.
     */
    public interface VideoCallUi extends Ui {
        //M: add transparent para,avoid incoming and dialing state display surface black
        void showVideoViews(boolean showPreview, boolean showIncoming, boolean transparent);
        void hideVideoUi();
        boolean isDisplayVideoSurfaceCreated();
        boolean isPreviewVideoSurfaceCreated();
        Surface getDisplayVideoSurface();
        Surface getPreviewVideoSurface();
        int getCurrentRotation();
        void setPreviewSize(int width, int height);
        void setPreviewSurfaceSize(int width, int height);
        void setDisplayVideoSize(int width, int height);
        Point getScreenSize();
        Point getPreviewSize();
        void cleanupSurfaces();
        ImageView getPreviewPhotoView();
        void adjustPreviewLocation(boolean shiftUp, int offset);
        void setPreviewRotation(int orientation);
        /// M: changed Display surface according to the rotation.
        /**
         * M: how to show display video when peer angle changed.
         */
        void changeDisplayVideoWithAngle(int width, int height, int rotation);
        /**
         * M: Handles a hide to the local preview.
         * @param hide true will hide local preview.
         */
        void hidePreview(boolean hide);
        /**
         * M: [ViLTE]when the user click videoViews, CallCard layout will
         * change, so should resize display TextureView.
         */
        void resizeDisplaySurface();
        /**
         * M: [ViLTE]when auto enter full screen time come , we should judge weather video
         * dispaly suface is shown.
         */
        boolean isDisplayVisible();

        void updateVideoUIPrize(boolean isUpdateForPrize);//PRIZE-add for video call-yuandailin-2016-7-6

        /**
         * M: update the peer dimension and angle
         */
        void updatePeerDimensionAndAngle(int width, int height, int rotation);
    }
   //add by xiaoyunhui 20180403 for bug 54522 cmcc video ringtone start
    public void resetPreviewCameraIfNeed() {
        if (mPrimaryCall != null
                && mPrimaryCall.isHidePreview()
                && VideoUtils.isVideoCall(mPrimaryCall)) {
            Log.d(this,"resetPreviewCamera for hiden preview. ");
            onSurfaceCreated(VideoCallFragment.SURFACE_PREVIEW);
        }
    }
    //end
    /**
     * M: Find the video call which has the same video call id with the original.
     * @{
     * @param callList CallList
     * @param original the original call
     * @return if found, return the Call, else return null.
     */
    private static String KEY_VIDEO_PROVIDER_ID = "video_provider_id";

    private Call findVideoCallByVideoId(CallList callList, Call original) {
        Bundle ori_extras = original.getTelecomCall().getDetails().getExtras();
        String ori_id = ori_extras != null ? ori_extras.getString(KEY_VIDEO_PROVIDER_ID) : null;
        if (TextUtils.isEmpty(ori_id)) {
            Log.d(this, "findVideoCallByVideoId, no extras or id, incallui "
                    + original.getId());
            return null;
        } else {
            Log.d(this, "findVideoCallByVideoId, vido_provider_id= " + ori_id
                    + ", incallui " + original.getId());
        }

        Collection<android.telecom.Call> list = callList.getCallMap().values();
        Bundle extras = null;
        String id = null;

        for (android.telecom.Call telecomCall : list) {
            if (Objects.equals(original.getTelecomCall(), telecomCall)) {
                continue;
            }
            Call call = callList.getCallByTelecomCall(telecomCall);
            if (call.getState() == Call.State.DISCONNECTING
                    || call.getState() == Call.State.DISCONNECTED) {
                Log.d(this,"findVideoCallByVideoId, ignore the disconnected call " + call.getId());
                continue;
            }
            extras = telecomCall.getDetails().getExtras();
            id = extras != null ? extras.getString(KEY_VIDEO_PROVIDER_ID) : null;
            if (!TextUtils.isEmpty(id) && ori_id.equals(id)) {
                Log.d(this, "findVideoCallByVideoId, found, vido_provider_id="
                        + id + ", call is " + call.getId());
                return call;
            }
        }
        return null;
    }
    /** @} */
}
