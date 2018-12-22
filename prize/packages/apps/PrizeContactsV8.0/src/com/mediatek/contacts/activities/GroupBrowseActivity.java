/*
* This software/firmware and related documentation ("MediaTek Software") are
* protected under relevant copyright laws. The information contained herein
* is confidential and proprietary to MediaTek Inc. and/or its licensors.
* Without the prior written permission of MediaTek inc. and/or its licensors,
* any reproduction, modification, use or disclosure of MediaTek Software,
* and information contained herein, in whole or in part, shall be strictly prohibited.
*/
/* MediaTek Inc. (C) 2011. All rights reserved.
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
*
* The following software/firmware and/or related documentation ("MediaTek Software")
* have been modified by MediaTek Inc. All revisions are subject to any receiver's
* applicable license agreements with MediaTek Inc.
*/

package com.mediatek.contacts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.activities.GroupDetailActivity;
import com.android.contacts.activities.GroupEditorActivity;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupBrowseListFragment.OnGroupBrowserActionListener;
import com.android.contacts.prize.PrizeGroupBrowseListFragment;
import com.android.contacts.prize.PrizeGroupEditorActivity;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.group.SimGroupUtils;
import com.mediatek.contacts.simcontact.SubInfoUtils;

import java.util.List;

public class GroupBrowseActivity extends Activity {
    private static final String TAG = "GroupBrowseActivity";

    private static final int SUBACTIVITY_NEW_GROUP = 4;

    private PrizeGroupBrowseListFragment mGroupsFragment;//prize-change-GroupBrowseListFragment-huangliemin-2016-7-5

    private final class GroupBrowserActionListener implements OnGroupBrowserActionListener {

        GroupBrowserActionListener() {}

        @Override
        public void onViewGroupAction(Uri groupUri) {
                int simId = -1;
                int subId = SubInfoUtils.getInvalidSubId();
        ///M: For move to other group feature.
                int count = /*mGroupsFragment.getAccountGroupMemberCount();*/0;
                String accountType = "";
                String accountName = "";
                Log.i(TAG, "[onViewGroupAction] groupUri" + groupUri.toString());
                List uriList = groupUri.getPathSegments();
                Uri newGroupUri = ContactsContract.AUTHORITY_URI.buildUpon()
                        .appendPath(uriList.get(0).toString())
                        .appendPath(uriList.get(1).toString()).build();
                if (uriList.size() > 2) {
                    subId = Integer.parseInt(uriList.get(2).toString());
                    Log.i(TAG, "[onViewGroupAction] subId:" + subId);
                }
                if (uriList.size() > 3) {
                    accountType = uriList.get(3).toString();
                }
                if (uriList.size() > 4) {
                    accountName = uriList.get(4).toString();
                }
                Log.i(TAG, "[onViewGroupAction] newGroupUri: " + newGroupUri + ",accountType: " +
                        accountType + ",accountName:" + accountName);
                Intent intent = new Intent(GroupBrowseActivity.this, GroupDetailActivity.class);
                intent.setData(newGroupUri);
                intent.putExtra(SimGroupUtils.KEY_ACCOUNT_CATEGORY, new AccountCategoryInfo(
                        accountType, subId, accountName, count));
                startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mtk_group_browse_activity);

        mGroupsFragment = (PrizeGroupBrowseListFragment) getFragmentManager().//prize-change-huangliemin-2016-7-5
                findFragmentById(R.id.groups_fragment);
        //mGroupsFragment.setListener(new GroupBrowserActionListener());

        // We want the UP affordance but no app icon.
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
        	/*prize-change-huangliemin-2016-7-29-start*/
        	/*
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                    ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME);
           */
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    		actionBar.setDisplayShowCustomEnabled(true);
    		actionBar.setDisplayHomeAsUpEnabled(false);
    		actionBar.setDisplayShowTitleEnabled(false);
    		actionBar.setDisplayUseLogoEnabled(false);
    		actionBar.setCustomView(R.layout.prize_custom_delete_contacts_actionbar);
    		TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
    		TitleText.setText(R.string.prize_contacts_manager_title);
    		View backButton = actionBar.getCustomView().findViewById(R.id.back_container);
    		TextView backTv = (TextView)actionBar.getCustomView().findViewById(R.id.back);
    		backTv.setText(R.string.back_button_label);
    		backButton.setOnClickListener(new OnClickListener() {
			
    			@Override
    			public void onClick(View v) {
    				onBackPressed();
    			}
    		});
    		TextView EditButton = (TextView)actionBar.getCustomView().findViewById(R.id.select_all);
    		View EditButtonContainer = actionBar.getCustomView().findViewById(R.id.select_all_container);//prize-add-for dido os8.0-hpf-2017-8-21
    		EditButton.setText(R.string.prize_group_edit);
    		EditButtonContainer.setOnClickListener(new OnClickListener() {
			
    			@Override
    			public void onClick(View v) {
    				final Intent intent = new Intent(GroupBrowseActivity.this, PrizeGroupEditorActivity.class);
    	        	startActivity(intent);
    			}
    		});
            /*prize-change-huangliemin-2016-7-29-end*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/*prize-add-huangliemin-don't-show-menu-2016-7-29-start*/
    	if(true) {
    		return false;
    	}
    	/*prize-add-huangliemin-don't-show-menu-2016-7-29-end*/
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mtk_group_browse_options, menu);
        //M:OP01 RCS will add group menu item @{
        ExtensionManager.getInstance().getRcsExtension().addGroupMenuOptions(menu, this);
        /** @} */

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add_group:
        	//prize-change-huangliemin-2016-7-15-start
        	/*
            final Intent intent = new Intent(this, GroupEditorActivity.class);
            intent.setAction(Intent.ACTION_INSERT);
            startActivityForResult(intent, SUBACTIVITY_NEW_GROUP);
            */
        	final Intent intent = new Intent(this, PrizeGroupEditorActivity.class);
        	startActivity(intent);
        	
            
            //prize-change-huangliemin-2016-7-15-end
            return true;
        case android.R.id.home:
            Intent homeIntent = new Intent(this, PeopleActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
            finish();
            return true;
        }
        return false;
    }

    public static class AccountCategoryInfo implements Parcelable {

        public String mAccountCategory;
        public int mSubId;
        public String mAccountName;
        ///M: For move to other group feature.
        public int mAccountGroupMemberCount;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(mAccountCategory);
            out.writeInt(mSubId);
            out.writeString(mAccountName);
            ///M: For move to other group feature.
            out.writeInt(mAccountGroupMemberCount);
        }

        public static final Parcelable.Creator<AccountCategoryInfo> CREATOR =
                new Parcelable.Creator<AccountCategoryInfo>() {
            public AccountCategoryInfo createFromParcel(Parcel in) {
                return new AccountCategoryInfo(in);
            }

            public AccountCategoryInfo[] newArray(int size) {
                return new AccountCategoryInfo[size];
            }
        };

        private AccountCategoryInfo(Parcel in) {
            mAccountCategory = in.readString();
            mSubId = in.readInt();
            mAccountName = in.readString();
            ///M: For move to other group feature.
            mAccountGroupMemberCount = in.readInt();
        }

        public AccountCategoryInfo(String accountCategory, int subId,
                String accountName, int count) {
            mAccountCategory = accountCategory;
            mSubId = subId;
            mAccountName = accountName;
            ///M: For move to other group feature.
            mAccountGroupMemberCount = count;
        }
    }
}
