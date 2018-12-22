/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
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
 * limitations under the License
 */

package com.android.contacts.activities;

import android.app.ActionBar;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.group.GroupDetailDisplayUtils;
import com.android.contacts.group.GroupDetailFragment;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;

//import com.mediatek.contacts.activities.GroupBrowseActivity.AccountCategoryInfo;
import com.android.contacts.prize.PrizeGroupBrowseListFragment.AccountCategoryInfo;//prize-change-huangliemin-2016-7-5
import com.mediatek.contacts.group.SimGroupUtils;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.util.Log;

public class GroupDetailActivity extends ContactsActivity {

    private static final String TAG = "GroupDetailActivity";

    private boolean mShowGroupSourceInActionBar;

    private String mAccountTypeString;
    private String mDataSet;

    private GroupDetailFragment mFragment;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        // TODO: Create Intent Resolver to handle the different ways users can get to this list.
        // TODO: Handle search or key down

        setContentView(R.layout.group_detail_activity);

        mShowGroupSourceInActionBar = getResources().getBoolean(
                R.bool.config_show_group_action_in_action_bar);

        mFragment = (GroupDetailFragment) getFragmentManager().findFragmentById(
                R.id.group_detail_fragment);
        mFragment.setListener(mFragmentListener);
        mFragment.setShowGroupSourceInActionBar(mShowGroupSourceInActionBar);
        /// M: marked google default code. @{
        //mFragment.loadGroup(getIntent().getData());
        //mFragment.closeActivityAfterDelete(true);
        /// @}

        /** M: New feature @{  */
        setAccountCategoryInfo();
        /** @} */
        // We want the UP affordance but no app icon.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME);
        }
    }

    private final GroupDetailFragment.Listener mFragmentListener =
            new GroupDetailFragment.Listener() {

        @Override
        public void onGroupNotFound() {
            /// M:
            finish();
        }

        @Override
        public void onGroupSizeUpdated(String size) {
            getActionBar().setSubtitle(size);
        }

        @Override
        public void onGroupTitleUpdated(String title) {
            getActionBar().setTitle(title);
        }

        @Override
        public void onAccountTypeUpdated(String accountTypeString, String dataSet) {
            mAccountTypeString = accountTypeString;
            mDataSet = dataSet;
            invalidateOptionsMenu();
        }

        @Override
        public void onEditRequested(Uri groupUri) {
            final Intent intent = new Intent(GroupDetailActivity.this, GroupEditorActivity.class);
            /** M: Bug Fix CR ID :ALPS000116203 @{ */
            mSubId = Integer.parseInt(groupUri.getLastPathSegment().toString());
            String grpId = groupUri.getPathSegments().get(1).toString();
            Uri uri = Uri.parse("content://com.android.contacts/groups").buildUpon()
                    .appendPath(grpId).build();
            Log.d(TAG, "[onEditRequested] uri: " + uri + ", grpId: " + grpId);
            intent.setData(uri);
            intent.setAction(Intent.ACTION_EDIT);
            intent.putExtra("SIM_ID", mSubId);
            /// M: For feature move to other groups.
            intent.putExtra("GROUP_NUMS", mAccountGroupMemberCount);
            startActivityForResult(intent, SUBACTIVITY_EDIT_GROUP);
            /** @} */

        }

        @Override
        public void onContactSelected(Uri contactUri) {
            Intent intent = new Intent(Intent.ACTION_VIEW, contactUri);
            ImplicitIntentsUtil.startActivityInApp(GroupDetailActivity.this, intent);
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mShowGroupSourceInActionBar) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.group_source, menu);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mShowGroupSourceInActionBar) {
            return false;
        }
        MenuItem groupSourceMenuItem = menu.findItem(R.id.menu_group_source);
        if (groupSourceMenuItem == null) {
            return false;
        }
        final AccountTypeManager manager = AccountTypeManager.getInstance(this);
        final AccountType accountType =
                manager.getAccountType(mAccountTypeString, mDataSet);
        if (TextUtils.isEmpty(mAccountTypeString)
                || TextUtils.isEmpty(accountType.getViewGroupActivity())) {
            groupSourceMenuItem.setVisible(false);
            return false;
        }
        View groupSourceView = GroupDetailDisplayUtils.getNewGroupSourceView(this);
        GroupDetailDisplayUtils.bindGroupSourceView(this, groupSourceView,
                mAccountTypeString, mDataSet);
        groupSourceView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Uri uri = ContentUris.withAppendedId(Groups.CONTENT_URI,
                        mFragment.getGroupId());
                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setClassName(accountType.syncAdapterPackageName,
                        accountType.getViewGroupActivity());
                ImplicitIntentsUtil.startActivityInApp(GroupDetailActivity.this, intent);
            }
        });
        groupSourceMenuItem.setActionView(groupSourceView);
        groupSourceMenuItem.setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                /// M: In L, return the prior activity. KK will return home activity.
                onBackPressed();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /** M: @{ */

    /// M: New feature @{
    private void setAccountCategoryInfo() {
        Bundle intentExtras;
        String category = null;
        String accountName = null;

        intentExtras = this.getIntent().getExtras();
        final AccountCategoryInfo accountCategoryInfo = intentExtras == null ? null
                : (AccountCategoryInfo) intentExtras
                        .getParcelable(SimGroupUtils.KEY_ACCOUNT_CATEGORY);
        if (accountCategoryInfo != null) {
            category = accountCategoryInfo.mAccountCategory;
            mSubId = accountCategoryInfo.mSubId;
            //accountName = accountCategoryInfo.mAccountName;
            /// M:For Feature move to other groups
            if (accountCategoryInfo.mAccountGroupMemberCount <= 0) {
                /// M: Fix CR ALPS02801328 Conn't show move_menu when new a group.
                mAccountGroupMemberCount = getAccountGroupMemberCount(accountName);
            } else {
                mAccountGroupMemberCount = accountCategoryInfo.mAccountGroupMemberCount;
            }
        }
        Log.d(TAG, "[setAccountCategoryInfo] mSubId: " + mSubId + ",accountName: " + accountName +
                ",mAccountGroupMemberCount: " + mAccountGroupMemberCount);
        mFragment.loadExtras(category, mSubId, accountName, mAccountGroupMemberCount);

        String callBackIntent = getIntent().getStringExtra("callBackIntent");
        Log.d(TAG, "[setAccountCategoryInfo] callBackIntent: " + callBackIntent);
        if (null != callBackIntent) {
            int subId = getIntent().getIntExtra("mSubId", -1);
            mFragment.loadExtras(subId);
            Log.d(TAG, "[setAccountCategoryInfo] subId: " + subId);
        }

        mFragment.loadGroup(getIntent().getData());
        mFragment.closeActivityAfterDelete(false);
    }
    /// @}

    /// M: Fix CR ALPS02801328 Conn't show move_menu when new a group. @{
    private int getAccountGroupMemberCount(String accountName) {
        final Cursor cursor = getContentResolver().query(
                Groups.CONTENT_URI,
                new String[] {
                        Groups._ID, Groups.TITLE
                },
                Groups.DELETED + "=0 "
                        + "AND " + Groups.ACCOUNT_NAME + "= '" + accountName
                        + "'", null, null);
        if (cursor == null) {
            return 0;
        }
        int count = 0;
        try {
            if (cursor.moveToFirst()) {
                count = cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }
    /// @}

    private int mSubId = SubInfoUtils.getInvalidSubId();
    private static final int SUBACTIVITY_EDIT_GROUP = 1;
    private int mAccountGroupMemberCount;
    /** @} */
}
