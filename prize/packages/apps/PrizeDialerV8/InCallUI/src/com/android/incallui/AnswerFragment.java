/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telecom.VideoProfile;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import com.android.dialer.R;

import java.util.ArrayList;
import java.util.List;
//prize  start pyx 2016-07-19   7.0UI
import android.app.KeyguardManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.media.AudioManager;
import android.view.Window;
import android.view.Gravity;
import com.android.incallui.PrizeAnswerLayout.IPrizeAnswerLayoutResponseListener;
//prize  end pyx 2016-07-19   7.0UI
import com.android.dialer.DialerApplication;//PRIZE-add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25

/**
 * Provides only common interface and functions. Should be derived to implement the actual UI.
 */
public abstract class AnswerFragment extends BaseFragment<AnswerPresenter, AnswerPresenter.AnswerUi>
        implements AnswerPresenter.AnswerUi, IPrizeAnswerLayoutResponseListener {

    public static final int TARGET_SET_FOR_AUDIO_WITHOUT_SMS = 0;
    public static final int TARGET_SET_FOR_AUDIO_WITH_SMS = 1;
    public static final int TARGET_SET_FOR_VIDEO_WITHOUT_SMS = 2;
    public static final int TARGET_SET_FOR_VIDEO_WITH_SMS = 3;
    public static final int TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST = 4;

    /**
     * M: [video call]3G VT call doesn't support answer as voice.
     */
    public static final int TARGET_SET_FOR_VIDEO_WITHOUT_SMS_AUDIO = 10;

    /**
     * This fragment implement no UI at all. Derived class should do it.
     */
    /*@Override
    public abstract View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState);*/

    /**
     * The popup showing the list of canned responses.
     *
     * This is an AlertDialog containing a ListView showing the possible choices.  This may be null
     * if the InCallScreen hasn't ever called showRespondViaSmsPopup() yet, or if the popup was
     * visible once but then got dismissed.
     */
    /*PRIZE-change message style to show -yuandailin-2016-8-16-start*/
    //private Dialog mCannedResponsePopup = null;
    private PopupWindow mCannedResponsePopup;
    /*PRIZE-change message style to show -yuandailin-2016-8-16-end*/

    /**
     * The popup showing a text field for users to type in their custom message.
     */
    private AlertDialog mCustomMessagePopup = null;

    private ArrayAdapter<String> mSmsResponsesAdapter;

    private final List<String> mSmsResponses = new ArrayList<>();

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*private GlowPadWrapper mGlowpad;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    //prize  start pyx 2016-07-19   7.0UI
    private View view, viewnolock;
    /*PRIZE-change message style to show -yuandailin-2016-8-17-start*/
    private TextView mReject, mAnswer, mSlient, mMessage, mRejectText, mAnswerText, mSlientText, mMessageText;
    private PrizeAnswerLayout prizeAnswerLayout;
    /*PRIZE-change message style to show -yuandailin-2016-8-17-end*/
    /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
    private int mVideoState = VideoProfile.STATE_AUDIO_ONLY; //android.telecom.VideoProfile.STATE_BIDIRECTIONAL //fix video state. prize-linkh-20170303
    /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/

    /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/

    // prize-add for video to audio call-by xiekui-20180816-start
    private RelativeLayout mPrizeToAudioCallRoot;
    private ImageView mPrizeToAudioCallImg;
    // prize-add for video to audio call-by xiekui-20180816-start

    @Override
    public void prizeIncallAnswer(int videoState) {
        getPresenter().onAnswer(videoState, getActivity());
    }

    @Override
    public void prizeIncallReject() {
        getPresenter().onDecline(getActivity());
    }

    @Override
    public void prizeIncallMessage() {
        prizeShowMessagePopupWindow();
    }
    /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/

    @Override
    public AnswerPresenter createPresenter() {
        return InCallPresenter.getInstance().getAnswerPresenter();
    }

    @Override
    public AnswerPresenter.AnswerUi getUi() {
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //prize  start pyx 2016-07-19   7.0UI

        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-start*/
        //mGlowpad = (GlowPadWrapper) inflater.inflate(R.layout.answer_fragment, container, false);
        /*view = (View) inflater.inflate(R.layout.answer_fragment, container, false);
        viewnolock = (View) inflater.inflate(R.layout.answer_fragment_nolock, container, false);*/
        view = (View) inflater.inflate(R.layout.prize_answer_fragment, container, false);
        viewnolock = (View) inflater.inflate(R.layout.prize_answer_fragment_nolock, container, false);
        /*PRIZE-Change-DialerV8-wangzhong-2017_7_19-end*/

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mGlowpad = (GlowPadWrapper) view.findViewById(R.id.glow_pad_view);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        prizeAnswerLayout = (PrizeAnswerLayout) view.findViewById(R.id.prize_answer_layout);
        prizeAnswerLayout.setIPrizeAnswerLayoutResponseListener(this);

        mReject = (TextView) viewnolock.findViewById(R.id.reject_button);
        mAnswer = (TextView) viewnolock.findViewById(R.id.answer_button);
        mSlient = (TextView) viewnolock.findViewById(R.id.silent_button);
        mMessage = (TextView) viewnolock.findViewById(R.id.message_button);
        /*PRIZE-change message style to show -yuandailin-2016-8-17-start*/
        mRejectText = (TextView) viewnolock.findViewById(R.id.reject_text);
        mAnswerText = (TextView) viewnolock.findViewById(R.id.answer_text);
        mSlientText = (TextView) viewnolock.findViewById(R.id.silent_text);
        mMessageText = (TextView) viewnolock.findViewById(R.id.message_text);
        /*PRIZE-change message style to show -yuandailin-2016-8-17-end*/

        // prize-add for video to audio call-by xiekui-20180816-start
        mPrizeToAudioCallImg = (ImageView) viewnolock.findViewById(R.id.prize_img_to_audio_call_nolock);
        mPrizeToAudioCallRoot = (RelativeLayout) viewnolock.findViewById(R.id.prize_tv_to_audio_call_nolock_root);
        mPrizeToAudioCallImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().onAnswer(VideoProfile.STATE_AUDIO_ONLY, getActivity());
            }
        });
        // prize-add for video to audio call-by xiekui-20180816-end

        mReject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getPresenter().onDecline(getActivity());
            }
        });

        mAnswer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
                /*getPresenter().onAnswer(VideoProfile.STATE_AUDIO_ONLY, getActivity());*/
                getPresenter().onAnswer(mVideoState, getActivity());
                /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
            }
        });

        mSlient.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*PRIZE-change -yuandailin-2016-8-11-start*/
                TelecomManager telecomManager =(TelecomManager) getActivity().getSystemService(Context.TELECOM_SERVICE);
                if (telecomManager.isRinging()) {
                    telecomManager.silenceRinger();
                    mSlient.setSelected(true);
                }
                /*PRIZE-change -yuandailin-2016-8-11-end*/
                DialerApplication.stopSpeaking();//PRIZE-add-SIMPLE_LAUNCHER_TTS-hpf-2017_11_25
            }
        });

        mMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //showMessageDialog();
                prizeShowMessagePopupWindow();
            }
        });

        KeyguardManager mKeyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
            return view;
        }

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mGlowpad.setAnswerFragment(this);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        return viewnolock;
        ///prize  end pyx 2016-07-19   7.0UI
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != prizeAnswerLayout) {
            prizeAnswerLayout.resetViewState();
        }

        // prize-add for video to audio call-by xiekui-20180816-start
        if (VideoProfile.isBidirectional(mVideoState)) {
            mPrizeToAudioCallRoot.setVisibility(View.VISIBLE);
            mAnswer.setBackgroundResource(R.drawable.prize_answer_video_selector);
        }
        // prize-add for video to audio call-by xiekui-20180816-end
    }

    /*PRIZE-change message style to show -yuandailin-2016-8-17-start*/
    /*@Override
    public void showMessageDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_DEVICE_DEFAULT_DARK);

        mSmsResponsesAdapter = new ArrayAdapter<>(builder.getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, mSmsResponses);

        final ListView lv = new ListView(getActivity());
        lv.setAdapter(mSmsResponsesAdapter);
        lv.setOnItemClickListener(new RespondViaSmsItemClickListener());

        builder.setCancelable(true).setView(lv).setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (mGlowpad != null) {
                            mGlowpad.startPing();
                        }
                        dismissCannedResponsePopup();
                        getPresenter().onDismissDialog();
                    }
                });
        mCannedResponsePopup = builder.create();
        mCannedResponsePopup.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        Window window = mCannedResponsePopup.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.1f;
        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        window.setAttributes(lp);
        mCannedResponsePopup.show();
    }*/

    @Override
    public void prizeShowMessagePopupWindow() {
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.prize_show_messages_pop_window, null);
        mCannedResponsePopup = new PopupWindow(contentView,android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mCannedResponsePopup.setOutsideTouchable(false);
        ListView prizeListView = (ListView) contentView.findViewById(R.id.prize_incallui_message_list);
        /*PRIZE-Change-PrizeInCallUI-wangzhong-2016_10_24-start*/
        /*mSmsResponsesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, mSmsResponses);*/
        if (null != mSmsResponses && mSmsResponses.size() == 0) {
            getPresenter().initAnswerTargetsForSms();
        }
        mSmsResponsesAdapter = new ArrayAdapter(getActivity(), R.layout.prize_incall_response_message_item, R.id.msg, mSmsResponses);
        /*PRIZE-Change-PrizeInCallUI-wangzhong-2016_10_24-end*/
        prizeListView.setAdapter(mSmsResponsesAdapter);
        prizeListView.setOnItemClickListener(new RespondViaSmsItemClickListener());

        mCannedResponsePopup.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                getPresenter().onDismissDialog();
                prizeAnswerLayout.setVisibility(View.VISIBLE);
                mReject.setVisibility(View.VISIBLE);
                mMessage.setVisibility(View.VISIBLE);
                mAnswer.setVisibility(View.VISIBLE);
                mSlient.setVisibility(View.VISIBLE);
                mRejectText.setVisibility(View.VISIBLE);
                mMessageText.setVisibility(View.VISIBLE);
                mAnswerText.setVisibility(View.VISIBLE);
                mSlientText.setVisibility(View.VISIBLE);
                mCannedResponsePopup =null;
            }
        });
        ImageButton prizeShutdownBotton = (ImageButton) contentView.findViewById(R.id.prize_incallui_message_popup_shutdown_button);
        prizeShutdownBotton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCannedResponsePopup.dismiss();
            }
        });
        prizeAnswerLayout.setVisibility(View.INVISIBLE);
        mReject.setVisibility(View.INVISIBLE);
        mMessage.setVisibility(View.INVISIBLE);
        mAnswer.setVisibility(View.INVISIBLE);
        mSlient.setVisibility(View.INVISIBLE);
        mRejectText.setVisibility(View.INVISIBLE);
        mMessageText.setVisibility(View.INVISIBLE);
        mAnswerText.setVisibility(View.INVISIBLE);
        mSlientText.setVisibility(View.INVISIBLE);
        mCannedResponsePopup.showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
    }
    /*PRIZE-change message style to show -yuandailin-2016-8-17-end*/

    private boolean isCannedResponsePopupShowing() {
        if (mCannedResponsePopup != null) {
            return mCannedResponsePopup.isShowing();
        }
        return false;
    }

    private boolean isCustomMessagePopupShowing() {
        if (mCustomMessagePopup != null) {
            return mCustomMessagePopup.isShowing();
        }
        return false;
    }

    /**
     * Dismiss the canned response list popup.
     *
     * This is safe to call even if the popup is already dismissed, and even if you never called
     * showRespondViaSmsPopup() in the first place.
     */
    protected void dismissCannedResponsePopup() {
        if (mCannedResponsePopup != null) {
            mCannedResponsePopup.dismiss();  // safe even if already dismissed
            mCannedResponsePopup = null;
        }
    }

    /**
     * Dismiss the custom compose message popup.
     */
    private void dismissCustomMessagePopup() {
        if (mCustomMessagePopup != null) {
            mCustomMessagePopup.dismiss();
            mCustomMessagePopup = null;
        }
    }

    /// M: override dismissPendingDialogs in AnswerUi. @{
    @Override
    public void dismissPendingDialogs() {
    /// @}
        if (isCannedResponsePopupShowing()) {
            dismissCannedResponsePopup();
        }

        if (isCustomMessagePopupShowing()) {
            dismissCustomMessagePopup();
        }
    }

    public boolean hasPendingDialogs() {
        return !(mCannedResponsePopup == null && mCustomMessagePopup == null);
    }

    /*PRIZE-change message style to show -yuandailin-2016-8-17-start*/
    /**
     * Shows the custom message entry dialog.
     */
    /*public void showCustomMessageDialog() {
        // Create an alert dialog containing an EditText
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(builder.getContext());
        builder.setCancelable(true).setView(et)
                .setPositiveButton(R.string.custom_message_send,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The order is arranged in a way that the popup will be destroyed when the
                        // InCallActivity is about to finish.
                        final String textMessage = et.getText().toString().trim();
                        dismissCustomMessagePopup();
                        getPresenter().rejectCallWithMessage(textMessage);
                    }
                })
                .setNegativeButton(R.string.custom_message_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /// M: ALPS01856137, need to start ping when back from dialog.
                        if (mGlowpad != null) {
                            mGlowpad.startPing();
                        }
                        /// @}
                        dismissCustomMessagePopup();
                        getPresenter().onDismissDialog();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        /// M: ALPS01856137, need to start ping when back from dialog.
                        if (mGlowpad != null) {
                            mGlowpad.startPing();
                        }
                        /// @}
                        dismissCustomMessagePopup();
                        getPresenter().onDismissDialog();
                    }
                })
                .setTitle(R.string.respond_via_sms_custom_message);
        mCustomMessagePopup = builder.create();

        // Enable/disable the send button based on whether there is a message in the EditText
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /// M: For ALPS02035613. Asynchronous callback
                // mCustomMessagePopup maybe null. @{
                if (mCustomMessagePopup == null) {
                    Log.e(this, "afterTextChanged, mCustomMessagePopup is null");
                    return;
                }
                /// @}
                final Button sendButton = mCustomMessagePopup.getButton(
                        DialogInterface.BUTTON_POSITIVE);
                sendButton.setEnabled(s != null && s.toString().trim().length() != 0);
            }
        });

        // Keyboard up, show the dialog
        mCustomMessagePopup.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mCustomMessagePopup.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mCustomMessagePopup.show();

        // Send button starts out disabled
        final Button sendButton = mCustomMessagePopup.getButton(DialogInterface.BUTTON_POSITIVE);
        sendButton.setEnabled(false);
    }*/

    public void showPrizeCustomMessageDialog() {
        // Create an alert dialog containing an EditText
    	mCustomMessagePopup= new AlertDialog.Builder(getActivity()).create();
    	mCustomMessagePopup.show();
        Window window = mCustomMessagePopup.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // Keyboard up, show the dialog
        //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setGravity(Gravity.BOTTOM);
        window.setContentView(R.layout.prize_incallui_edit_mesaage_to_send);
        TextView prizeEditMessageTitle= (TextView)window.findViewById(R.id.prize_edit_message_title);
        String sendMessageTo = getActivity().getResources().getString(R.string.prize_incallui_send_message_to_string);
        String number =getPresenter().getCallNumber();
        prizeEditMessageTitle.setText(sendMessageTo+""+number);
        final EditText messageEdittext =(EditText)window.findViewById(R.id.prize_message_edittext);
        final ImageButton sendMessageButton = (ImageButton) window.findViewById(R.id.prize_send_message_button);
        sendMessageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String textMessage = messageEdittext.getText().toString().trim();
                dismissCustomMessagePopup();
                getPresenter().rejectCallWithMessage(textMessage);

                /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
                if (VideoProfile.isVideo(mVideoState)) {
                    InCallPresenter.getInstance().declineUpgradeRequest(getContext());
                }
                /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
            }
        });
        mCustomMessagePopup.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*if (mGlowpad != null) {
                    mGlowpad.startPing();
                }*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                dismissCustomMessagePopup();
                getPresenter().onDismissDialog();
            }
        });
        
        messageEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                /// M: For ALPS02035613. Asynchronous callback
                // mCustomMessagePopup maybe null. @{
                if (mCustomMessagePopup == null) {
                    Log.e(this, "afterTextChanged, mCustomMessagePopup is null");
                    return;
                }  
                sendMessageButton.setEnabled(s != null && s.toString().trim().length() != 0);
            }
        });    
        // Send button starts out disabled
        sendMessageButton.setEnabled(false);
    }
    /*PRIZE-change message style to show -yuandailin-2016-8-17-end*/
    
    @Override
    public void configureMessageDialog(List<String> textResponses) {
        mSmsResponses.clear();
        mSmsResponses.addAll(textResponses);
        mSmsResponses.add(getResources().getString(
                R.string.respond_via_sms_custom_message));
        if (mSmsResponsesAdapter != null) {
            mSmsResponsesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    public void onAnswer(int videoState, Context context) {
        Log.d(this, "onAnswer videoState=" + videoState + " context=" + context);
        getPresenter().onAnswer(videoState, context);
    }

    public void onDecline(Context context) {
        getPresenter().onDecline(context);
    }

    public void onDeclineUpgradeRequest(Context context) {
        InCallPresenter.getInstance().declineUpgradeRequest(context);
    }

    public void onText() {
        getPresenter().onText();
    }

    /**
     * OnItemClickListener for the "Respond via SMS" popup.
     */
    public class RespondViaSmsItemClickListener implements AdapterView.OnItemClickListener {

        /**
         * Handles the user selecting an item from the popup.
         */
        @Override
        public void onItemClick(AdapterView<?> parent,  // The ListView
                View view,  // The TextView that was clicked
                int position, long id) {
            Log.d(this, "RespondViaSmsItemClickListener.onItemClick(" + position + ")...");
            final String message = (String) parent.getItemAtPosition(position);
            Log.v(this, "- message: '" + message + "'");
            dismissCannedResponsePopup();

            // The "Custom" choice is a special case.
            // (For now, it's guaranteed to be the last item.)
            if (position == (parent.getCount() - 1)) {
                // Show the custom message dialog
                /*PRIZE-change message style to show -yuandailin-2016-8-17-start*/
                //showCustomMessageDialog();
                showPrizeCustomMessageDialog();
                /*PRIZE-change message style to show -yuandailin-2016-8-17-end*/
            } else {
                getPresenter().rejectCallWithMessage(message);
                /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
                if (VideoProfile.isVideo(mVideoState)) {
                    InCallPresenter.getInstance().declineUpgradeRequest(getContext());
                }
                /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
            }
        }
    }

    @Override
    /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
    /*public void onShowAnswerUi(boolean shown) {*/
    public void onShowAnswerUi(boolean shown, boolean isVideoCall) {
    /*PRIZE-Change-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
        Log.d(this, "Show answer UI: " + shown + ",  isVideoCall : " + isVideoCall + ",  prizeAnswerLayout : " + prizeAnswerLayout);

        /*PRIZE-Add-Dismiss_Message_Pop-wangzhong-2017_3_15-start*/
        if (!shown) dismissPendingDialogs();
        /*PRIZE-Add-Dismiss_Message_Pop-wangzhong-2017_3_15-end*/
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2017_3_8-start*/
        if (null != mMessage && null != mMessageText) {
            mMessage.setEnabled(true);
            mMessageText.setEnabled(true);
            if (null != mSmsResponses && mSmsResponses.size() == 0) {
                getPresenter().initAnswerTargetsForSms();
                if (mSmsResponses.size() == 0) {
                    mMessage.setEnabled(false);
                    mMessageText.setEnabled(false);
                }
            }
        }

        if (null != prizeAnswerLayout) {
            //prizeAnswerLayout.resetViewState();
            prizeAnswerLayout.initLayoutResponseStatus();
            prizeAnswerLayout.handleActionUpEvent();
        }
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2017_3_8-end*/
    }

    @Override
    public void showTargets(int targetSet) {
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
        showTargets(targetSet, VideoProfile.STATE_BIDIRECTIONAL);
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
        // Do Nothing
    }

    @Override
    public void showTargets(int targetSet, int videoState) {
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-start*/
        mVideoState = videoState;
        prizeAnswerLayout.setVideoState(videoState);
        /*PRIZE-Add-InCallUI_VideoCall-wangzhong-2016_12_26-end*/
        // Do Nothing
    }

    protected void onMessageDialogCancel() {
        // Do nothing.
    }

}
