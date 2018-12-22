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
 * limitations under the License
 */

package com.android.incallui;

import android.os.Bundle;
import android.telecom.VideoProfile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.dialer.R;
// M: add for plugin. @{
import com.mediatek.incallui.ext.ExtensionManager;
/// @}


public class GlowPadAnswerFragment extends AnswerFragment {

    /*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24-start*/
    /*private GlowPadWrapper mGlowpad;

    public GlowPadAnswerFragment() {
    }*/
    /*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24-end*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-start*/
        /*mGlowpad = (GlowPadWrapper) inflater.inflate(R.layout.answer_fragment,
                container, false);
        Log.d(this, "Creating view for answer fragment ", this);
        Log.d(this, "Created from activity", getActivity());
        mGlowpad.setAnswerFragment(this);

        ExtensionManager.getVilteAutoTestHelperExt().registerReceiverForAcceptAndRejectUpgrade(
                getActivity(),InCallPresenter.getInstance().getAnswerPresenter());
        return mGlowpad;*/
        ExtensionManager.getVilteAutoTestHelperExt().registerReceiverForAcceptAndRejectUpgrade(
                getActivity(),InCallPresenter.getInstance().getAnswerPresenter());
        return super.onCreateView(inflater, container, savedInstanceState);
        /*PRIZE-Change-PrizeInCallUI_N-wangzhong-2016_10_24-end*/
    }

    @Override
    public void onResume() {
        super.onResume();
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mGlowpad.requestFocus();*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    }

    @Override
    public void onDestroyView() {
        Log.d(this, "onDestroyView");
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (mGlowpad != null) {
            mGlowpad.stopPing();
            mGlowpad = null;
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        ExtensionManager.getVilteAutoTestHelperExt().unregisterReceiverForAcceptAndRejectUpgrade();
        super.onDestroyView();
    }

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*@Override
    *//*public void onShowAnswerUi(boolean shown) {*//*
    public void onShowAnswerUi(boolean shown, boolean isVideoCall) {
        super.onShowAnswerUi(shown, isVideoCall);
        Log.d(this, "Show answer UI: " + shown);
        if (shown) {
            mGlowpad.startPing();
        } else {
            mGlowpad.stopPing();
        }
    }*/

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    /*public void showTargets(int targetSet) {
        showTargets(targetSet, VideoProfile.STATE_BIDIRECTIONAL);
    }*/

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     *
     * @param targetSet Integer identifying the set of targets to use.
     */
    /*@Override
    public void showTargets(int targetSet, int videoState) {
        final int targetResourceId;
        final int targetDescriptionsResourceId;
        final int directionDescriptionsResourceId;
        final int handleDrawableResourceId;
        mGlowpad.setVideoState(videoState);

        switch (targetSet) {
            case TARGET_SET_FOR_AUDIO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_audio_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS:
                targetResourceId = R.array.incoming_call_widget_video_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_video_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST:
                targetResourceId =
                        R.array.incoming_call_widget_video_request_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_request_target_descriptions;
                directionDescriptionsResourceId = R.array
                        .incoming_call_widget_video_request_target_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            *//**
            * M: [video call]3G Video call doesn't support answer as audio, and reject via SMS. @{
            *//*
            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS_AUDIO:
                targetResourceId = R.array.mtk_incoming_call_widget_video_without_sms_audio_targets;
                targetDescriptionsResourceId =
                        R.array.
                            mtk_incoming_call_widget_video_without_sms_audio_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.
                            mtk_incoming_call_widget_video_without_sms_audio_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            *//** @}*//*
            case TARGET_SET_FOR_AUDIO_WITHOUT_SMS:
            default:
                targetResourceId = R.array.incoming_call_widget_audio_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
        }

        if (targetResourceId != mGlowpad.getTargetResourceId()) {
            mGlowpad.setTargetResources(targetResourceId);
            mGlowpad.setTargetDescriptionsResourceId(targetDescriptionsResourceId);
            mGlowpad.setDirectionDescriptionsResourceId(directionDescriptionsResourceId);
            mGlowpad.setHandleDrawable(handleDrawableResourceId);
            mGlowpad.reset(false);
            /// M: Force layout to avoid UI abnormally.
            mGlowpad.requestLayout();
        }
    }

    @Override
    protected void onMessageDialogCancel() {
        if (mGlowpad != null) {
            mGlowpad.startPing();
        }
    }*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
}
