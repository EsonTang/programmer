/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.dialer.calllog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.telecom.PhoneAccountHandle;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.common.CallUtil;
import com.android.contacts.common.ClipboardUtils;
import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.common.compat.CompatUtils;
import com.android.contacts.common.compat.PhoneNumberUtilsCompat;
import com.android.contacts.common.dialog.CallSubjectDialog;
import com.android.contacts.common.testing.NeededForTesting;
import com.android.contacts.common.util.PhoneNumberHelper;
import com.android.contacts.common.util.UriUtils;
import com.android.dialer.DialtactsActivity;
import com.android.dialer.R;
import com.android.dialer.calllog.calllogcache.CallLogCache;
import com.android.dialer.compat.FilteredNumberCompat;
import com.android.dialer.database.FilteredNumberAsyncQueryHandler;
import com.android.dialer.filterednumber.BlockNumberDialogFragment;
import com.android.dialer.filterednumber.FilteredNumbersUtil;
import com.android.dialer.logging.Logger;
import com.android.dialer.logging.ScreenEvent;
import com.android.dialer.service.ExtendedBlockingButtonRenderer;
import com.android.dialer.util.DialerUtils;
import com.android.dialer.util.PhoneNumberUtil;
import com.android.dialer.voicemail.VoicemailPlaybackLayout;
import com.android.dialer.voicemail.VoicemailPlaybackPresenter;
import com.android.dialerbind.ObjectFactory;

import com.mediatek.dialer.ext.ExtensionManager;
import com.mediatek.dialer.util.DialerFeatureOptions;
import com.mediatek.dialer.util.DialerVolteUtils;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an object containing references to views contained by the call log list item. This
 * improves performance by reducing the frequency with which we need to find views by IDs.
 *
 * This object also contains UI logic pertaining to the view, to isolate it from the CallLogAdapter.
 */
public final class CallLogListItemViewHolder extends RecyclerView.ViewHolder
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*implements View.OnClickListener, MenuItem.OnMenuItemClickListener,*/
        implements MenuItem.OnMenuItemClickListener,
        /*PRIZE-Change-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        com.android.dialer.calllog.prizeexpandrecyclerview.Expandable,
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        View.OnCreateContextMenuListener {

    /** The root view of the call log list item */
    public final View rootView;
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /** The quick contact badge for the contact. */
    /*public final QuickContactBadge quickContactView;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    /** The primary action view of the entry. */
    public final View primaryActionView;
    /** The details of the phone call. */
    public final PhoneCallDetailsViews phoneCallDetailsViews;

    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /** The text of the header for a day grouping. */
    //public final TextView dayGroupHeader;
    /** The view containing the details for the call log row, including the action buttons. */
    //public final CardView callLogEntryView;
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /** The actionable view which places a call to the number corresponding to the call log row. */
    /*public final ImageView primaryActionButtonView;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    /** The view containing call log item actions.  Null until the ViewStub is inflated. */
    //public View actionsView;
    /*PRIZE-remove-yuandailin-2016-3-28-end*/

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /** The button views below are assigned only when the action section is expanded. */
    /*public VoicemailPlaybackLayout voicemailPlaybackView;
    public View callButtonView;
    public View videoCallButtonView;
    public View createNewContactButtonView;
    public View addToExistingContactButtonView;
    public View sendMessageView;
    /// M: [IP Dial] For IP Prefix feature
    public View ipDialButtonView;
    public View callWithNoteButtonView;
    public View detailsButtonView;*/
    /** M: [VoLTE ConfCallLog] the conference call numbers if it was conference call @{ */
    /*public ArrayList<String> confCallNumbers;*/
    /** @} */
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    public ImageView workIconView;

    /*PRIZE-add-yuandailin-2016-3-15-start*/
    public LinearLayout callDetailLayout;
    /*PRIZE-add-yuandailin-2016-3-15-end*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*public ImageView prizeSimIndicater;//PRIZE-add -yuandailin-2016-7-18*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    public LinearLayout primary_action_phone_message;
    public TextView calllog_item_message;
    public TextView calllog_item_phone;
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/

    /**
     * The row Id for the first call associated with the call log entry.  Used as a key for the
     * map used to track which call log entries have the action button section expanded.
     */
    public long rowId;

    /**
     * The call Ids for the calls represented by the current call log entry.  Used when the user
     * deletes a call log entry.
     */
    public long[] callIds;

    /**
     * The callable phone number for the current call log entry.  Cached here as the call back
     * intent is set only when the actions ViewStub is inflated.
     */
    public String number;

    /**
     * The post-dial numbers that are dialed following the phone number.
     */
    public String postDialDigits;

    /**
     * The formatted phone number to display.
     */
    public String displayNumber;

    /**
     * The phone number presentation for the current call log entry.  Cached here as the call back
     * intent is set only when the actions ViewStub is inflated.
     */
    public int numberPresentation;

    /**
     * The type of the phone number (e.g. main, work, etc).
     */
    public String numberType;

    /**
     * The country iso for the call. Cached here as the call back
     * intent is set only when the actions ViewStub is inflated.
     */
    public String countryIso;

    /**
     * The type of call for the current call log entry.  Cached here as the call back
     * intent is set only when the actions ViewStub is inflated.
     */
    public int callType;

    /**
     * ID for blocked numbers database.
     * Set when context menu is created, if the number is blocked.
     */
    public Integer blockId;

    /**
     * The account for the current call log entry.  Cached here as the call back
     * intent is set only when the actions ViewStub is inflated.
     */
    public PhoneAccountHandle accountHandle;

    /**
     * If the call has an associated voicemail message, the URI of the voicemail message for
     * playback.  Cached here as the voicemail intent is only set when the actions ViewStub is
     * inflated.
     */
    public String voicemailUri;

    /**
     * The name or number associated with the call.  Cached here for use when setting content
     * descriptions on buttons in the actions ViewStub when it is inflated.
     */
    public CharSequence nameOrNumber;

    /**
     * The call type or Location associated with the call. Cached here for use when setting text
     * for a voicemail log's call button
     */
    public CharSequence callTypeOrLocation;

    /**
     * Whether this row is for a business or not.
     */
    public boolean isBusiness;

    /**
     * The contact info for the contact displayed in this list item.
     */
    public ContactInfo info;

    /**
     * Whether the current log entry is a blocked number or not. Used in updatePhoto()
     */
    public boolean isBlocked;

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /**
     * Whether this is the archive tab or not.
     */
    /*public final boolean isArchiveTab;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    private final Context mContext;
    private final CallLogCache mCallLogCache;
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*private final CallLogListItemHelper mCallLogListItemHelper;
    private final VoicemailPlaybackPresenter mVoicemailPlaybackPresenter;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
    private final FilteredNumberAsyncQueryHandler mFilteredNumberAsyncQueryHandler;

    private final BlockNumberDialogFragment.Callback mFilteredNumberDialogCallback;

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*private final int mPhotoSize;
    private ViewStub mExtendedBlockingViewStub;
    private final ExtendedBlockingButtonRenderer mExtendedBlockingButtonRenderer;*/

    /*PRIZE-change-yuandailin-2016-3-28-start*/
    //private View.OnClickListener mExpandCollapseListener;
    /*private boolean mVoicemailPrimaryActionButtonClicked;*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    private CallLogListItemViewHolder(
            Context context,
            ExtendedBlockingButtonRenderer.Listener eventListener,
            //View.OnClickListener expandCollapseListener,
            CallLogCache callLogCache,
            CallLogListItemHelper callLogListItemHelper,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            BlockNumberDialogFragment.Callback filteredNumberDialogCallback,
            View rootView,
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*QuickContactBadge quickContactView,*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            View primaryActionView,
            PhoneCallDetailsViews phoneCallDetailsViews,
            //CardView callLogEntryView,
            //TextView dayGroupHeader,
            LinearLayout callDetailLayout,
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
            LinearLayout primary_action_phone_message,
            TextView calllog_item_message,
            TextView calllog_item_phone,
            /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*ImageView prizeSimIndicater,
            ImageView primaryActionButtonView,*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            boolean isArchiveTab) {
        super(rootView);

        mContext = context;
        //mExpandCollapseListener = expandCollapseListener;
        mCallLogCache = callLogCache;
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*mCallLogListItemHelper = callLogListItemHelper;
        mVoicemailPlaybackPresenter = voicemailPlaybackPresenter;*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        mFilteredNumberAsyncQueryHandler = filteredNumberAsyncQueryHandler;
        mFilteredNumberDialogCallback = filteredNumberDialogCallback;

        this.rootView = rootView;
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*this.quickContactView = quickContactView;*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        this.primaryActionView = primaryActionView;
        this.phoneCallDetailsViews = phoneCallDetailsViews;
        //this.callLogEntryView = callLogEntryView;
        //this.dayGroupHeader = dayGroupHeader;
        this.callDetailLayout = callDetailLayout;
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
        this.primary_action_phone_message = primary_action_phone_message;
        this.calllog_item_message = calllog_item_message;
        this.calllog_item_phone = calllog_item_phone;
        /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*this.prizeSimIndicater = prizeSimIndicater;
        this.primaryActionButtonView = primaryActionButtonView;*/
        //this.workIconView = (ImageView) rootView.findViewById(R.id.work_profile_icon);/*PRIZE-Delete-PrizeInDialer_N-wangzhong-2016_10_24*/
        /*this.isArchiveTab = isArchiveTab;
        Resources resources = mContext.getResources();
        mPhotoSize = mContext.getResources().getDimensionPixelSize(R.dimen.contact_photo_size);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        // Set text height to false on the TextViews so they don't have extra padding.
        phoneCallDetailsViews.nameView.setElegantTextHeight(false);
        //phoneCallDetailsViews.callLocationAndDate.setElegantTextHeight(false);

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*quickContactView.setOverlay(null);
        if (CompatUtils.hasPrioritizedMimeType()) {
            quickContactView.setPrioritizedMimeType(Phone.CONTENT_ITEM_TYPE);
        }
        primaryActionButtonView.setOnClickListener(this);
        mExtendedBlockingButtonRenderer =
                ObjectFactory.newExtendedBlockingButtonRenderer(mContext, eventListener);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        //primaryActionView.setOnClickListener(mExpandCollapseListener);
        primaryActionView.setOnCreateContextMenuListener(this);
    }

    public static CallLogListItemViewHolder create(
            View view,
            Context context,
            ExtendedBlockingButtonRenderer.Listener eventListener,
            //View.OnClickListener expandCollapseListener,
            CallLogCache callLogCache,
            CallLogListItemHelper callLogListItemHelper,
            VoicemailPlaybackPresenter voicemailPlaybackPresenter,
            FilteredNumberAsyncQueryHandler filteredNumberAsyncQueryHandler,
            BlockNumberDialogFragment.Callback filteredNumberDialogCallback,
            boolean isArchiveTab) {

        return new CallLogListItemViewHolder(
                context,
                eventListener,
                //expandCollapseListener,
                callLogCache,
                callLogListItemHelper,
                voicemailPlaybackPresenter,
                filteredNumberAsyncQueryHandler,
                filteredNumberDialogCallback,
                view,
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*(QuickContactBadge) view.findViewById(R.id.quick_contact_photo),*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                view.findViewById(R.id.primary_action_view),
                PhoneCallDetailsViews.fromView(view),
                //(CardView) view.findViewById(R.id.call_log_row),
                //(TextView) view.findViewById(R.id.call_log_day_group_label),
                (LinearLayout) view.findViewById(R.id.show_call_detail_layout),
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                (LinearLayout) view.findViewById(R.id.primary_action_phone_message),
                (TextView) view.findViewById(R.id.calllog_item_message),
                (TextView) view.findViewById(R.id.calllog_item_phone),
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*(ImageView) view.findViewById(R.id.prize_sim_indicater),
                (ImageView) view.findViewById(R.id.primary_action_button),*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                isArchiveTab);
    }

    @Override
    public void onCreateContextMenu(
            final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (TextUtils.isEmpty(number)) {
            return;
        }

        if (callType == CallLog.Calls.VOICEMAIL_TYPE) {
            menu.setHeaderTitle(mContext.getResources().getText(R.string.voicemail));
        } else {
            menu.setHeaderTitle(PhoneNumberUtilsCompat.createTtsSpannable(
                    BidiFormatter.getInstance().unicodeWrap(number, TextDirectionHeuristics.LTR)));
        }

        menu.add(ContextMenu.NONE, R.id.context_menu_copy_to_clipboard, ContextMenu.NONE,
                R.string.action_copy_number_text)
                .setOnMenuItemClickListener(this);

        // The edit number before call does not show up if any of the conditions apply:
        // 1) Number cannot be called
        // 2) Number is the voicemail number
        // 3) Number is a SIP address

        if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)
                && !mCallLogCache.isVoicemailNumber(accountHandle, number)
                && !PhoneNumberUtil.isSipNumber(number)) {
            menu.add(ContextMenu.NONE, R.id.context_menu_edit_before_call, ContextMenu.NONE,
                    R.string.action_edit_number_before_call)
                    .setOnMenuItemClickListener(this);
        }

        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*if (callType == CallLog.Calls.VOICEMAIL_TYPE
                && phoneCallDetailsViews.voicemailTranscriptionView.length() > 0) {
            menu.add(ContextMenu.NONE, R.id.context_menu_copy_transcript_to_clipboard,
                    ContextMenu.NONE, R.string.copy_transcript_text)
                    .setOnMenuItemClickListener(this);
        }*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

        if (FilteredNumberCompat.canAttemptBlockOperations(mContext)
                && FilteredNumbersUtil.canBlockNumber(mContext, number, countryIso)) {
            mFilteredNumberAsyncQueryHandler.isBlockedNumber(
                    new FilteredNumberAsyncQueryHandler.OnCheckBlockedListener() {
                        @Override
                        public void onCheckComplete(Integer id) {
                            blockId = id;
                            int blockTitleId = blockId == null ? R.string.action_block_number
                                    : R.string.action_unblock_number;
                            final MenuItem blockItem = menu.add(
                                    ContextMenu.NONE,
                                    R.id.context_menu_block_number,
                                    ContextMenu.NONE,
                                    blockTitleId);
                            blockItem.setOnMenuItemClickListener(
                                    CallLogListItemViewHolder.this);
                        }
                    }, number, countryIso);
        }

        Logger.logScreenView(ScreenEvent.CALL_LOG_CONTEXT_MENU, (Activity) mContext);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int resId = item.getItemId();
        if (resId == R.id.context_menu_block_number) {
            /** M:find parent view for block number dialog.@{ */
            View v = ((Activity) mContext).findViewById(R.id.floating_action_button_container);
            int parentViewId = (v == null) ? rootView.getId()
                    : R.id.floating_action_button_container;
            /**@}*/
            FilteredNumberCompat
                    .showBlockNumberDialogFlow(mContext.getContentResolver(), blockId, number,
                            countryIso, displayNumber,
                            parentViewId/**M:R.id.floating_action_button_container*/,
                            ((Activity) mContext).getFragmentManager(),
                            mFilteredNumberDialogCallback);
            return true;
        } else if (resId == R.id.context_menu_copy_to_clipboard) {
            ClipboardUtils.copyText(mContext, null, number, true);
            return true;
        } else if (resId == R.id.context_menu_copy_transcript_to_clipboard) {
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
            /*ClipboardUtils.copyText(mContext, null,
                    phoneCallDetailsViews.voicemailTranscriptionView.getText(), true);*/
            /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
            return true;
        } else if (resId == R.id.context_menu_edit_before_call) {
            final Intent intent = new Intent(
                    Intent.ACTION_DIAL, CallUtil.getCallUri(number));
            intent.setClass(mContext, DialtactsActivity.class);
            DialerUtils.startActivityWithErrorToast(mContext, intent);
            return true;
        }
        return false;
    }

    /**
     * Configures the action buttons in the expandable actions ViewStub. The ViewStub is not
     * inflated during initial binding, so click handlers, tags and accessibility text must be set
     * here, if necessary.
     */
    /*public void inflateActionViewStub() {
        ViewStub stub = (ViewStub) rootView.findViewById(R.id.call_log_entry_actions_stub);
        if (stub != null) {
            actionsView = stub.inflate();

            voicemailPlaybackView = (VoicemailPlaybackLayout) actionsView
                    .findViewById(R.id.voicemail_playback_layout);
            if (isArchiveTab) {
                voicemailPlaybackView.hideArchiveButton();
            }


            callButtonView = actionsView.findViewById(R.id.call_action);
            callButtonView.setOnClickListener(this);

            videoCallButtonView = actionsView.findViewById(R.id.video_call_action);
            videoCallButtonView.setOnClickListener(this);

            createNewContactButtonView = actionsView.findViewById(R.id.create_new_contact_action);
            createNewContactButtonView.setOnClickListener(this);

            addToExistingContactButtonView =
                    actionsView.findViewById(R.id.add_to_existing_contact_action);
            addToExistingContactButtonView.setOnClickListener(this);

            sendMessageView = actionsView.findViewById(R.id.send_message_action);
            sendMessageView.setOnClickListener(this);

            detailsButtonView = actionsView.findViewById(R.id.details_action);
            detailsButtonView.setOnClickListener(this);*/

            /** M: [IP Dial] add IP Dial @{ */
            /*ipDialButtonView = actionsView.findViewById(R.id.ipdial_action);
            ipDialButtonView.setOnClickListener(this);*/
            /** @} */

            /*callWithNoteButtonView = actionsView.findViewById(R.id.call_with_note_action);
            callWithNoteButtonView.setOnClickListener(this);

            mExtendedBlockingViewStub =
                    (ViewStub) actionsView.findViewById(R.id.extended_blocking_actions_container);
        }

        bindActionButtons();
    }*/
    /*PRIZE-change-yuandailin-2016-3-28-end*/

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*private void updatePrimaryActionButton(boolean isExpanded) {
        if (!TextUtils.isEmpty(voicemailUri)) {
            // Treat as voicemail list item; show play button if not expanded.
            if (!isExpanded) {
                primaryActionButtonView.setImageResource(R.drawable.ic_play_arrow_24dp);
                primaryActionButtonView.setContentDescription(TextUtils.expandTemplate(
                        mContext.getString(R.string.description_voicemail_action),
                        nameOrNumber));
                primaryActionButtonView.setVisibility(View.VISIBLE);
            } else {
                primaryActionButtonView.setVisibility(View.GONE);
            }
        } else {
            // Treat as normal list item; show call button, if possible.
            if (PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation)) {
                boolean isVoicemailNumber =
                        mCallLogCache.isVoicemailNumber(accountHandle, number);
                /// M: [Suggested Account] Supporting suggested account @{
                /// Give all the calls a suggestion first.
                if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                    primaryActionButtonView.setTag(IntentProvider
                            .getSuggestedReturnCallIntentProvider(number + postDialDigits,
                                    accountHandle));
                /// @}
                } else if (isVoicemailNumber) {
                    // Call to generic voicemail number, in case there are multiple accounts.
                    primaryActionButtonView.setTag(
                            IntentProvider.getReturnVoicemailCallIntentProvider());
                } else {
                    primaryActionButtonView.setTag(IntentProvider
                            .getReturnCallIntentProvider(number + postDialDigits));
                }
                *//** M: [IMS Call] For Volte IMS call @{ *//*
                if (DialerFeatureOptions.isImsCallSupport()
                        && PhoneNumberHelper.isUriNumber(number)
                        && PhoneAccountUtils.isSubScriptionAccount(mContext, accountHandle)) {
                    primaryActionButtonView.setTag(
                            IntentProvider.getReturnIMSCallIntentProvider(number + postDialDigits));
                }
                *//** @} *//*

                primaryActionButtonView.setContentDescription(TextUtils.expandTemplate(
                        mContext.getString(R.string.description_call_action),
                        nameOrNumber));
                primaryActionButtonView.setImageResource(R.drawable.ic_call_24dp);
                primaryActionButtonView.setVisibility(View.VISIBLE);
                *//** M: [VoLTE ConfCallLog] For Volte Conference callLog @{ *//*
                if (confCallNumbers != null) {
                    if (DialerVolteUtils.isVolteConfCallEnable(mContext)) {
                        primaryActionButtonView.setTag(IntentProvider
                                .getReturnVolteConfCallIntentProvider(confCallNumbers));
                    } else {
                        primaryActionButtonView.setTag(null);
                        primaryActionButtonView.setVisibility(View.GONE);
                    }
                }
                *//** @} *//*
            } else {
                primaryActionButtonView.setTag(null);
                primaryActionButtonView.setVisibility(View.GONE);
            }
        }
    }*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    /*PRIZE-remove-yuandailin-2016-3-28-start*/
    /**
     * Binds text titles, click handlers and intents to the voicemail, details and callback action
     * buttons.
     */
    /*private void bindActionButtons() {*/
        /** M: [VoLTE ConfCallLog] Only show call detail button for conference callLog @{ */
        /*if (confCallNumbers != null) {
            callButtonView.setVisibility(View.GONE);
            videoCallButtonView.setVisibility(View.GONE);
            voicemailPlaybackView.setVisibility(View.GONE);
            createNewContactButtonView.setVisibility(View.GONE);
            addToExistingContactButtonView.setVisibility(View.GONE);
            sendMessageView.setVisibility(View.GONE);
            ipDialButtonView.setVisibility(View.GONE);
            callWithNoteButtonView.setVisibility(View.GONE);
            detailsButtonView.setVisibility(View.VISIBLE);
            detailsButtonView.setTag(
                    IntentProvider.getCallDetailIntentProvider(
                            rowId, callIds, null, true));
            return;
        } else {
            // Other buttons will change its visible at below code of this
            // method except sendMessageView. So, make it visible.
            sendMessageView.setVisibility(View.VISIBLE);
        }*/
        /** @} */

        /*boolean canPlaceCallToNumber =
                PhoneNumberUtil.canPlaceCallsTo(number, numberPresentation);

        if (!TextUtils.isEmpty(voicemailUri) && canPlaceCallToNumber) {
            /// M: [Suggested Account] Supporting suggested account @{
            if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                callButtonView.setTag(IntentProvider
                        .getSuggestedReturnCallIntentProvider(number,
                                accountHandle));
            } else {
                callButtonView.setTag(IntentProvider
                        .getReturnCallIntentProvider(number));
            }
            /// @}
            ((TextView) callButtonView.findViewById(R.id.call_action_text))
                    .setText(TextUtils.expandTemplate(
                            mContext.getString(R.string.call_log_action_call),
                            nameOrNumber));
            TextView callTypeOrLocationView = ((TextView) callButtonView.findViewById(
                    R.id.call_type_or_location_text));
            if (callType == Calls.VOICEMAIL_TYPE && !TextUtils.isEmpty(callTypeOrLocation)) {
                callTypeOrLocationView.setText(callTypeOrLocation);
                callTypeOrLocationView.setVisibility(View.VISIBLE);
            } else {
                callTypeOrLocationView.setVisibility(View.GONE);
            }
            callButtonView.setVisibility(View.VISIBLE);
        } else {
            callButtonView.setVisibility(View.GONE);
        }

        /// M: RCS fetch video capability information through presence
        /// plugin ask from RCS and update through isVideoCallCapable API
        // If one of the calls had video capabilities, show the video call button.
        ///M: Plug-in to fetch the video capability for Video over Wifi call
        boolean isAnonymous = info != null && UriUtils.isEncodedContactUri(info.lookupUri);
        /// M: For OP01 Vilte, Always show the video call button
        if ((mCallLogCache.isVideoEnabled() && canPlaceCallToNumber &&
                (ExtensionManager.getInstance().getCallLogExtension().showVideoForAllCallLog() ||
                phoneCallDetailsViews.callTypeIcons.isVideoShown() ||
                ExtensionManager.getInstance().getCallLogExtension().
                isViWifiShown(phoneCallDetailsViews.callTypeIcons))) ||
                (ExtensionManager.getInstance().getCallLogCommonPresenceExtension().
                isVideoCallCapable(number, isAnonymous))) {
            /// M: [Suggested Account] Supporting suggested account @{
            if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                videoCallButtonView.setTag(IntentProvider
                        .getSuggestedReturnVideoCallIntentProvider(number,
                                accountHandle));
            } else {
                videoCallButtonView.setTag(IntentProvider
                        .getReturnVideoCallIntentProvider(number));
            }
            /// @}
            videoCallButtonView.setVisibility(View.VISIBLE);
        } else {
            videoCallButtonView.setVisibility(View.GONE);
        }

        // For voicemail calls, show the voicemail playback layout; hide otherwise.
        if (callType == Calls.VOICEMAIL_TYPE && mVoicemailPlaybackPresenter != null
                && !TextUtils.isEmpty(voicemailUri)) {
            voicemailPlaybackView.setVisibility(View.VISIBLE);

            Uri uri = Uri.parse(voicemailUri);
            mVoicemailPlaybackPresenter.setPlaybackView(
                    voicemailPlaybackView, uri, mVoicemailPrimaryActionButtonClicked);
            mVoicemailPrimaryActionButtonClicked = false;
            // Only mark voicemail as read when not in archive tab
            if (!isArchiveTab) {
                CallLogAsyncTaskUtil.markVoicemailAsRead(mContext, uri);
            }
        } else {
            voicemailPlaybackView.setVisibility(View.GONE);
        }

        if (callType == Calls.VOICEMAIL_TYPE) {
            detailsButtonView.setVisibility(View.GONE);
        } else {
            detailsButtonView.setVisibility(View.VISIBLE);
            detailsButtonView.setTag(
                    IntentProvider.getCallDetailIntentProvider(rowId, callIds, null));
        }*/

        //if (info != null && UriUtils.isEncodedContactUri(info.lookupUri)) {
        //    createNewContactButtonView.setTag(IntentProvider.getAddContactIntentProvider(
        //            info.lookupUri, info.name, info.number, info.type, true /* isNewContact */));
        //    createNewContactButtonView.setVisibility(View.VISIBLE);

        //    addToExistingContactButtonView.setTag(IntentProvider.getAddContactIntentProvider(
        //            info.lookupUri, info.name, info.number, info.type, false /* isNewContact */));
        //    addToExistingContactButtonView.setVisibility(View.VISIBLE);
        //} else {
        //    createNewContactButtonView.setVisibility(View.GONE);
        //    addToExistingContactButtonView.setVisibility(View.GONE);
        //}

        /*if (canPlaceCallToNumber) {
            sendMessageView.setTag(IntentProvider.getSendSmsIntentProvider(number));
            sendMessageView.setVisibility(View.VISIBLE);
        } else {
            sendMessageView.setVisibility(View.GONE);
        }*/

        /** M: [IP Dial] Add IP Dial @{ */
        /*if (DialerFeatureOptions.IP_PREFIX && canPlaceCallToNumber
                && !PhoneNumberHelper.isUriNumber(number)) {
            /// M: [Suggested Account] Supporting suggested account @{
            if (DialerFeatureOptions.isSuggestedAccountSupport()) {
                ipDialButtonView.setTag(IntentProvider
                        .getSuggestedIpDialCallIntentProvider(number, accountHandle));
            } else {
                ipDialButtonView.setTag(IntentProvider
                        .getIpDialCallIntentProvider(number));
            }
            /// @}
            ipDialButtonView.setVisibility(View.VISIBLE);
        } else {
            ipDialButtonView.setVisibility(View.GONE);
        }*/
        /** @} */
        /*mCallLogListItemHelper.setActionContentDescriptions(this);

        boolean supportsCallSubject =
                mCallLogCache.doesAccountSupportCallSubject(accountHandle);
        boolean isVoicemailNumber =
                mCallLogCache.isVoicemailNumber(accountHandle, number);
        callWithNoteButtonView.setVisibility(
                supportsCallSubject && !isVoicemailNumber ? View.VISIBLE : View.GONE);

        if(mExtendedBlockingButtonRenderer != null){
            List<View> completeLogListItems = Lists.newArrayList(
                    createNewContactButtonView,
                    addToExistingContactButtonView,
                    sendMessageView,
                    callButtonView,
                    callWithNoteButtonView,
                    detailsButtonView,
                    voicemailPlaybackView);

            List<View> blockedNumberVisibleViews = Lists.newArrayList(detailsButtonView);
            List<View> extendedBlockingVisibleViews = Lists.newArrayList(detailsButtonView);

            ExtendedBlockingButtonRenderer.ViewHolderInfo viewHolderInfo =
                    new ExtendedBlockingButtonRenderer.ViewHolderInfo(
                            completeLogListItems,
                            extendedBlockingVisibleViews,
                            blockedNumberVisibleViews,
                            number,
                            countryIso,
                            nameOrNumber.toString(),
                            displayNumber);
            mExtendedBlockingButtonRenderer.setViewHolderInfo(viewHolderInfo);

            mExtendedBlockingButtonRenderer.render(mExtendedBlockingViewStub);
        }

       ///M: Plug-in call to customize Action buttons
        ExtensionManager.getInstance().getCallLogExtension().
                                           customizeBindActionButtons(this);
    }*/

    /**
     * Show or hide the action views, such as voicemail, details, and add contact.
     *
     * If the action views have never been shown yet for this view, inflate the view stub.
     */
    /*public void showActions(boolean show) {
        showOrHideVoicemailTranscriptionView(show);

        if (show) {
            // Inflate the view stub if necessary, and wire up the event handlers.
            inflateActionViewStub();

            actionsView.setVisibility(View.VISIBLE);
            actionsView.setAlpha(1.0f);
        } else {
            // When recycling a view, it is possible the actionsView ViewStub was previously
            // inflated so we should hide it in this case.
            if (actionsView != null) {
                actionsView.setVisibility(View.GONE);
            }
        }

        updatePrimaryActionButton(show);
    }

    public void showOrHideVoicemailTranscriptionView(boolean isExpanded) {
        if (callType != Calls.VOICEMAIL_TYPE) {
            return;
        }

        final TextView view = phoneCallDetailsViews.voicemailTranscriptionView;
        if (!isExpanded || TextUtils.isEmpty(view.getText())) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
    }*/
    /*PRIZE-remove-yuandailin-2016-3-28-end*/

    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
    /*public void updatePhoto() {
        quickContactView.assignContactUri(info.lookupUri);

        final boolean isVoicemail = mCallLogCache.isVoicemailNumber(accountHandle, number);
        int contactType = ContactPhotoManager.TYPE_DEFAULT;
        if (isVoicemail) {
            contactType = ContactPhotoManager.TYPE_VOICEMAIL;
        } else if (isBusiness) {
            contactType = ContactPhotoManager.TYPE_BUSINESS;
        }
        /// M: [VoLTE ConfCallLog] Show Volte Conference contact icon @{
        final boolean isConference = confCallNumbers != null;
        if (isConference) {
            contactType = ContactPhotoManager.TYPE_CONFERENCE_CALL;
            quickContactView.assignContactUri(null);
        }
        /// @}

        final String lookupKey = isConference ? null : UriUtils.getLookupKeyFromUri(info.lookupUri);
        final String displayName = TextUtils.isEmpty(info.name) ? displayNumber : info.name;
        final DefaultImageRequest request = new DefaultImageRequest(
                displayName, lookupKey, contactType, true *//* isCircular *//*);

        /// M: if this contact is SIM contact,call log need show SIM icon @{
        if (info != null) {
            request.subId = info.contactSimId;
            request.photoId = info.isSdnContact;
        }
        /// @}

        /// M: if is conference call, just show conference contact photo
        if (info.photoId == 0 && info.photoUri != null && !isConference) {
            ContactPhotoManager.getInstance(mContext).loadPhoto(quickContactView, info.photoUri,
                    mPhotoSize, false *//* darkTheme *//*, true *//* isCircular *//*, request);
        } else {
            ContactPhotoManager.getInstance(mContext).loadThumbnail(quickContactView,
                    isConference ? 0 : info.photoId,
                    false *//* darkTheme *//*, true *//* isCircular *//*, request);
        }

        *//*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-start*//*
        *//*if (mExtendedBlockingButtonRenderer != null) {
            mExtendedBlockingButtonRenderer.updatePhotoAndLabelIfNecessary(
                    number,
                    countryIso,
                    quickContactView,
                    phoneCallDetailsViews.callLocationAndDate);
        }*//*
        if (mExtendedBlockingButtonRenderer != null) {
            mExtendedBlockingButtonRenderer.updatePhotoAndLabelIfNecessary(
                    number,
                    countryIso,
                    quickContactView,
                    phoneCallDetailsViews.callLocation);
        }
        *//*PRIZE-Change-PrizeInDialer_N-wangzhong-2016_10_24-end*//*
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.primary_action_button && !TextUtils.isEmpty(voicemailUri)) {
            mVoicemailPrimaryActionButtonClicked = true;
            //mExpandCollapseListener.onClick(primaryActionView);
        } else if (view.getId() == R.id.call_with_note_action) {
            CallSubjectDialog.start(
                    (Activity) mContext,
                    info.photoId,
                    info.photoUri,
                    info.lookupUri,
                    (String) nameOrNumber *//* top line of contact view in call subject dialog *//*,
                    isBusiness,
                    number,
                    TextUtils.isEmpty(info.name) ? null : displayNumber, *//* second line of contact
                                                                           view in dialog. *//*
                    numberType, *//* phone number type (e.g. mobile) in second line of contact view *//*
                    accountHandle);
        } else {
            final IntentProvider intentProvider = (IntentProvider) view.getTag();
            if (intentProvider != null) {
                final Intent intent = intentProvider.getIntent(mContext);
                // See IntentProvider.getCallDetailIntentProvider() for why this may be null.
                if (intent != null) {
                    DialerUtils.startActivityWithErrorToast(mContext, intent);
                }
            }
        }
    }*/
    /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/

    @NeededForTesting
    public static CallLogListItemViewHolder createForTest(Context context) {
        Resources resources = context.getResources();
        CallLogCache callLogCache =
                CallLogCache.getCallLogCache(context);
        PhoneCallDetailsHelper phoneCallDetailsHelper = new PhoneCallDetailsHelper(
                context, resources, callLogCache);

        /*PRIZE-change-yuandailin-2016-3-28-start*/
        CallLogListItemViewHolder viewHolder = new CallLogListItemViewHolder(
                context,
                null,
                //null /* expandCollapseListener */,
                callLogCache,
                new CallLogListItemHelper(phoneCallDetailsHelper, resources, callLogCache),
                null /* voicemailPlaybackPresenter */,
                null /* filteredNumberAsyncQueryHandler */,
                null /* filteredNumberDialogCallback */,
                new View(context),
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*new QuickContactBadge(context),*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                new LinearLayout(context) /*M: primaryActionView */,
                PhoneCallDetailsViews.createForTest(context),
                //new CardView(context),
                //new TextView(context),
                new LinearLayout(context),
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
                new LinearLayout(context),
                new TextView(context),
                new TextView(context),
                /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
                /*new ImageView(context),
                new ImageView(context),*/
                /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
                false);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        /*viewHolder.detailsButtonView = new TextView(context);
        //viewHolder.actionsView = new View(context);
        viewHolder.voicemailPlaybackView = new VoicemailPlaybackLayout(context);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        /*PRIZE-change-yuandailin-2016-3-28-end*/
        viewHolder.workIconView = new ImageButton(context);
        return viewHolder;
    }

    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-start*/
    public View getExpandView() {
        return primary_action_phone_message;
    }
    /*PRIZE-Add-DialerV8-wangzhong-2017_7_19-end*/
}
