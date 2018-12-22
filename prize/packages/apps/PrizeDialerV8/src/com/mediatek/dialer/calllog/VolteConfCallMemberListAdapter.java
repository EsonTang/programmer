/* Copyright Statement:
*
* This software/firmware and related documentation ("MediaTek Software") are
* protected under relevant copyright laws. The information contained herein
* is confidential and proprietary to MediaTek Inc. and/or its licensors.
* Without the prior written permission of MediaTek inc. and/or its licensors,
* any reproduction, modification, use or disclosure of MediaTek Software,
* and information contained herein, in whole or in part, shall be strictly prohibited.
*
* MediaTek Inc. (C) 2014. All rights reserved.
*
* BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
* THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
* RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
* AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
* NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
* SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
* SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
* THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
* THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
* CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
* SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
* STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
* CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
* AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
* OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
* MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
*/
package com.mediatek.dialer.calllog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.contacts.common.ContactPhotoManager;
import com.android.contacts.common.ContactPhotoManager.DefaultImageRequest;
import com.android.dialer.PhoneCallDetails;
import com.android.dialer.R;
import com.android.dialer.calllog.CallDetailHistoryAdapter;
import com.android.dialer.calllog.CallLogAdapter;
import com.android.dialer.calllog.CallLogListItemViewHolder;
import com.android.dialer.calllog.CallLogQuery;
import com.android.dialer.calllog.ContactInfoHelper;
import com.android.dialer.calllog.IntentProvider;
import com.android.dialer.calllog.PhoneAccountUtils;
import com.android.dialer.calllog.CallLogAdapter.CallFetcher;
import com.android.dialer.calllog.CallLogGroupBuilder;
import com.android.dialer.util.DialerUtils;
import com.mediatek.dialer.util.DialerVolteUtils;

import java.util.ArrayList;

/**
 * M: [VoLTE ConfCall] The Volte Conference call member list adapter
 */
public class VolteConfCallMemberListAdapter extends CallLogAdapter {
    private final static String TAG = "VolteConfCallMemberListAdapter";
    private final static int VIEW_TYPE_CALL_HISTORY_LIST_ITEM_HEADER = 50;
    private final static int VIEW_TYPE_CALL_HISTORY_LIST_ITEM = 51;
    private final static int VIEW_TYPE_CALL_DETAIL_HEADER = 52;

    private CallDetailHistoryAdapter mCallDetailHistoryAdapter;
    private PhoneCallDetails[] mConferenceCallDetails;
    private ArrayList<String> mConfNumbers;

    public VolteConfCallMemberListAdapter(Context context,
            ContactInfoHelper contactInfoHelper) {
        super(context, new CallFetcher() {
            @Override
            public void fetchCalls() {
                // Do nothings
            }
        }, contactInfoHelper, null, ACTIVITY_TYPE_CALL_LOG);
        setIsConfCallMemberList(true);
    }

    public void setConferenceCallDetails(PhoneCallDetails[] callDetails) {
        mConferenceCallDetails = callDetails;
        mConfNumbers = new ArrayList<String>();
        for (int i = 0; i < callDetails.length; i++) {
            if (!TextUtils.isEmpty(callDetails[i].number)) {
                mConfNumbers.add(callDetails[i].number.toString());
            }
        }
    }

    /**
     * For show the call history list item views. Only one conference call history item
     * @param adapter the CallDetailHistoryAdapter
     */
    public void setCallDetailHistoryAdapter(CallDetailHistoryAdapter adapter) {
        mCallDetailHistoryAdapter = adapter;
    }

    @Override
    protected void addGroups(Cursor cursor) {
        if (cursor.getCount() == 0) {
            return;
        }
        // Clear any previous day grouping information.
        clearDayGroups();
        // Reset cursor to start before the first row
        cursor.moveToPosition(-1);
        // Create an individual group for each calllog
        while (cursor.moveToNext()) {
            addGroup(cursor.getPosition(), 1);
            setDayGroup(cursor.getLong(CallLogQuery.ID),
                    CallLogGroupBuilder.DAY_GROUP_TODAY);
        }
    }

    @Override
    public int getItemCount() {
        // If there was no calllog items, do not shown headers
        if (super.getItemCount() == 0) {
            return 0;
        }
        // Add conference call detail header, history list header and item views
        return super.getItemCount() + 3;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_CALL_DETAIL_HEADER;
        } else if (position == getItemCount() - 1) {
            return VIEW_TYPE_CALL_HISTORY_LIST_ITEM;
        } else if (position == getItemCount() - 2) {
            return VIEW_TYPE_CALL_HISTORY_LIST_ITEM_HEADER;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CALL_DETAIL_HEADER) {
            return CallDetailHeaderViewHolder.create(mContext, parent);
        } else if (viewType == VIEW_TYPE_CALL_HISTORY_LIST_ITEM_HEADER) {
            return CallHistoryViewHolder.createHeader(mContext, parent);
        } else if (viewType == VIEW_TYPE_CALL_HISTORY_LIST_ITEM) {
            return CallHistoryViewHolder.create(mContext, parent);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void bindCallLogListViewHolder(ViewHolder viewHolder, int position) {
        Log.d(TAG, "bindCallLogListViewHolder(), viewHolder = " + viewHolder
                + " position = " + position);
        // Conference call detail header, history list header and item views
        if (getItemViewType(position) == VIEW_TYPE_CALL_DETAIL_HEADER
                && mConferenceCallDetails != null) {
            CallDetailHeaderViewHolder holder = (CallDetailHeaderViewHolder)viewHolder;
            bindCallDetailHeader(holder);
            return;
        } else if (getItemViewType(position) == VIEW_TYPE_CALL_HISTORY_LIST_ITEM_HEADER) {
            return;
        } else if (getItemViewType(position) == VIEW_TYPE_CALL_HISTORY_LIST_ITEM
                && mCallDetailHistoryAdapter != null) {
            CallHistoryViewHolder holder = (CallHistoryViewHolder)viewHolder;
            mCallDetailHistoryAdapter.getView(0, holder.view, null);
            return;
        } else {
            // The first position is call detail header
            position = position - 1;
            bindMemberList(viewHolder, position);
        }
    }

    private void bindMemberList(ViewHolder viewHolder, int position) {
        super.bindCallLogListViewHolder(viewHolder, position);
        Cursor c = (Cursor) getItem(position);
        if (c == null) {
            return;
        }
        CallLogListItemViewHolder views = (CallLogListItemViewHolder) viewHolder;
        // Conference member list title
        /*PRIZE-remove-yuandailin-2016-3-19-start*/
        /*if (position == 0) {
            views.dayGroupHeader.setVisibility(View.VISIBLE);
            views.dayGroupHeader.setText(R.string.conf_call_member_list);
        } else {
            views.dayGroupHeader.setVisibility(View.GONE);
        }*/
        /*PRIZE-remove-yuandailin-2016-3-19-end*/
        long duration = c.getLong(CallLogQuery.DURATION);
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-start*/
        // Hide the account label
        /*views.phoneCallDetailsViews.callAccountLabel.setVisibility(View.GONE);*/
        /*PRIZE-Delete-Optimize_Dialer-wangzhong-2018_3_5-end*/
        // Add the "Missed" or "Answered"
        ArrayList<CharSequence> texts = new ArrayList<CharSequence>();
        //texts.add(views.phoneCallDetailsViews.callLocationAndDate.getText());//PRIZE-remove-yuandailin-2016-3-19
        texts.add(mContext
                .getText(duration > 0 ? R.string.conf_call_participant_answered
                        : R.string.conf_call_participant_missed));
        /*PRIZE-remove-yuandailin-2016-3-19-start*/
        /*views.phoneCallDetailsViews.callLocationAndDate.setText(DialerUtils
                .join(mContext.getResources(), texts));*/
        /*PRIZE-remove-yuandailin-2016-3-19-end*/
    }

    private void bindCallDetailHeader(CallDetailHeaderViewHolder viewHolder) {
        String confCallTile = mContext.getString(R.string.confCall);
        viewHolder.callerName.setText(confCallTile);
        viewHolder.callerNumber.setVisibility(View.GONE);
        viewHolder.quickContactBadge.setImageResource(R.drawable.ic_group_white_24dp);

        ///M: [Volte ConfCall] Support launch volte conference call @{
        if (DialerVolteUtils.isVolteConfCallEnable(mContext)) {
            viewHolder.callButton.setVisibility(View.VISIBLE);
            viewHolder.callButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = IntentProvider
                            .getReturnVolteConfCallIntentProvider(
                                    mConfNumbers).getIntent(mContext);
                    DialerUtils.startActivityWithErrorToast(mContext, intent);
                }
            });
        } else {
            viewHolder.callButton.setVisibility(View.GONE);
        }
        /// @}

        String accountLabel = PhoneAccountUtils.getAccountLabel(mContext,
                mConferenceCallDetails[0].accountHandle);
        if (!TextUtils.isEmpty(accountLabel)) {
            viewHolder.accountLabel.setText(accountLabel);
            viewHolder.accountLabel.setVisibility(View.VISIBLE);
        } else {
            viewHolder.accountLabel.setVisibility(View.GONE);
        }
        final DefaultImageRequest request = new DefaultImageRequest(confCallTile, null,
                ContactPhotoManager.TYPE_CONFERENCE_CALL, true /* isCircular */);
        viewHolder.quickContactBadge.assignContactUri(null);
        viewHolder.quickContactBadge.setOverlay(null);
        ContactPhotoManager.getInstance(mContext).loadThumbnail(viewHolder.quickContactBadge, 0,
                false /* darkTheme */, true /* isCircular */, request);
    }

    // Call history list item view holder
    static class CallHistoryViewHolder extends RecyclerView.ViewHolder {
        public View view;

        private CallHistoryViewHolder(final Context context, View view) {
            super(view);
            this.view = view;
        }

        public static CallHistoryViewHolder create(Context context, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.call_detail_history_item, parent, false);
            return new CallHistoryViewHolder(context, view);
        }

        public static CallHistoryViewHolder createHeader(Context context, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.call_detail_history_header, parent, false);
            return new CallHistoryViewHolder(context, view);
        }
    }

    // Call detail header view holder
    static class CallDetailHeaderViewHolder extends RecyclerView.ViewHolder {
        public QuickContactBadge quickContactBadge;
        public TextView callerName;
        public TextView callerNumber;
        public TextView accountLabel;
        public View callButton;

        private CallDetailHeaderViewHolder(final Context context, View view) {
            super(view);
            callerName = (TextView) view.findViewById(R.id.caller_name);
            callerNumber = (TextView) view.findViewById(R.id.caller_number);
            accountLabel = (TextView) view.findViewById(R.id.phone_account_label);
            quickContactBadge = (QuickContactBadge) view.findViewById(R.id.quick_contact_photo);
            quickContactBadge.setOverlay(null);
            callButton = view.findViewById(R.id.call_back_button);
        }

        public static CallDetailHeaderViewHolder create(Context context, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.call_detail_header, parent, false);
            return new CallDetailHeaderViewHolder(context, view);
        }
    }
}
