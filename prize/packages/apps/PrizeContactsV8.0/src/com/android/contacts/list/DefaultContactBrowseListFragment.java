/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.list.ContactListFilter;
import com.android.contacts.common.list.ContactListFilterController;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.list.ProfileAndContactsLoader;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.common.util.AccountFilterUtil;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.util.ContactsListUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.widget.WaitCursorView;
import com.mediatek.contacts.activities.GroupBrowseActivity;//prize-add-huangliemin-2016-7-13

/**
 * Fragment containing a contact list used for browsing (as compared to
 * picking a contact with one of the PICK intents).
 */
public class DefaultContactBrowseListFragment extends ContactBrowseListFragment {
    private static final String TAG = DefaultContactBrowseListFragment.class.getSimpleName();

    private static final int REQUEST_CODE_ACCOUNT_FILTER = 1;

    private View mSearchHeaderView;
    private View mAccountFilterHeader;
    private FrameLayout mProfileHeaderContainer;
    private View mProfileHeader;
    private Button mProfileMessage;
    private Button mSelectAllButton;//prize-add-huangliemin-2016-6-8
    private Button mManagerGroupsButton;//prize-add-huangliemin-2016-7-13
//    private TextView mProfileTitle; zhangzhonghao remove 20160305
    private View mSearchProgress;
    private TextView mSearchProgressText;

    private class FilterHeaderClickListener implements OnClickListener {
        @Override
        public void onClick(View view) {
            AccountFilterUtil.startAccountFilterActivityForResult(
                        DefaultContactBrowseListFragment.this,
                        REQUEST_CODE_ACCOUNT_FILTER,
                        getFilter());
        }
    }
    private OnClickListener mFilterHeaderClickListener = new FilterHeaderClickListener();

    public DefaultContactBrowseListFragment() {
        setPhotoLoaderEnabled(true);
        // Don't use a QuickContactBadge. Just use a regular ImageView. Using a QuickContactBadge
        // inside the ListView prevents us from using MODE_FULLY_EXPANDED and messes up ripples.
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
    }

    @Override
    public CursorLoader createCursorLoader(Context context) {
        /** M: Bug Fix for ALPS00115673 Descriptions: add wait cursor. @{ */
        Log.d(TAG, "[createCursorLoader]");
        if (mLoadingContainer != null) {
            mLoadingContainer.setVisibility(View.GONE);
        }
        /** @} */

        return new ProfileAndContactsLoader(context);
    }

    @Override
    protected void onItemClick(int position, long id) {
        Log.d(TAG, "[onItemClick][launch]start");
        final Uri uri = getAdapter().getContactUri(position);
        if (uri == null) {
            return;
        }
        if (ExtensionManager.getInstance().getRcsExtension()
                .addRcsProfileEntryListener(uri, false)) {
            return;
        }
        viewContact(uri, getAdapter().isEnterpriseContact(position));
        Log.d(TAG, "[onItemClick][launch]end");
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        DefaultContactListAdapter adapter = new DefaultContactListAdapter(getContext());
        adapter.setSectionHeaderDisplayEnabled(isSectionHeaderDisplayEnabled());
        adapter.setDisplayPhotos(true);
        adapter.setPhotoPosition(
                ContactListItemView.getDefaultPhotoPosition(/* opposite = */ false));
        return adapter;
    }

    @Override
    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.contact_list_content, null);
    }

    @Override
    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        /// Add for ALPS02377518, should prevent accessing SubInfo if has no basic permissions. @{
        if (!RequestPermissionsActivity.hasBasicPermissions(getContext())) {
            Log.i(TAG, "[onCreateView] has no basic permissions");
            return;
        }
        /// @}
        mAccountFilterHeader = getView().findViewById(R.id.account_filter_header_container);
        mAccountFilterHeader.setOnClickListener(mFilterHeaderClickListener);

        // Create an entry for public account and show it from now
        ExtensionManager.getInstance().getRcsExtension()
            .createEntryView(getListView(), getActivity());

        // Create an empty user profile header and hide it for now (it will be visible if the
        // contacts list will have no user profile).
        addEmptyUserProfileHeader(inflater);
        showEmptyUserProfile(false);
        /** M: Bug Fix for ALPS00115673 Descriptions: add wait cursor */
       mWaitCursorView = ContactsListUtils.initLoadingView(this.getContext(),
                getView(), mLoadingContainer, mLoadingContact, mProgress);

        // Putting the header view inside a container will allow us to make
        // it invisible later. See checkHeaderViewVisibility()
        FrameLayout headerContainer = new FrameLayout(inflater.getContext());
        mSearchHeaderView = inflater.inflate(R.layout.search_header, null, false);
        headerContainer.addView(mSearchHeaderView);
        getListView().addHeaderView(headerContainer, null, false);
        checkHeaderViewVisibility();

        mSearchProgress = getView().findViewById(R.id.search_progress);
        mSearchProgressText = (TextView) mSearchHeaderView.findViewById(R.id.totalContactsText);
    }

    @Override
    protected void setSearchMode(boolean flag) {
        super.setSearchMode(flag);
        checkHeaderViewVisibility();
        // prize delete progressbar by zhaojian 20171121 start
        //if (!flag) showSearchProgress(false);
        // prize delete progressbar by zhaojian 20171121 end
    }

    /** Show or hide the directory-search progress spinner. */
    private void showSearchProgress(boolean show) {
        if (mSearchProgress != null) {
            mSearchProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void checkHeaderViewVisibility() {
        updateFilterHeaderView();

        // Hide the search header by default.
        if (mSearchHeaderView != null) {
            mSearchHeaderView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setFilter(ContactListFilter filter) {
        super.setFilter(filter);
        updateFilterHeaderView();
    }

    private void updateFilterHeaderView() {
        if (mAccountFilterHeader == null) {
            return; // Before onCreateView -- just ignore it.
        }
        final ContactListFilter filter = getFilter();
        if (filter != null && !isSearchMode()) {
            final boolean shouldShowHeader = AccountFilterUtil.updateAccountFilterTitleForPeople(
                    mAccountFilterHeader, filter, false);
            mAccountFilterHeader.setVisibility(/*shouldShowHeader ? View.VISIBLE : */View.GONE);//prize-change-huangliemin-2016-7-26
        } else {
            mAccountFilterHeader.setVisibility(View.GONE);
        }
    }

    @Override
    public void setProfileHeader() {
        mUserProfileExists = false;/*getAdapter().hasProfile();*///prize-change-huangliemin-2016-7-26
        showEmptyUserProfile(!mUserProfileExists && !isSearchMode());

        if (isSearchMode()) {
            ContactListAdapter adapter = getAdapter();
            if (adapter == null) {
                return;
            }

            // In search mode we only display the header if there is nothing found
            if (TextUtils.isEmpty(getQueryString()) || !adapter.areAllPartitionsEmpty()) {
                mSearchHeaderView.setVisibility(View.GONE);
                // prize delete progressbar by zhaojian 20171121 start
                //showSearchProgress(false);
                // prize delete progressbar by zhaojian 20171121 end
            } else {
                mSearchHeaderView.setVisibility(View.VISIBLE);
                if (adapter.isLoading()) {
                    mSearchProgressText.setText(R.string.search_results_searching);         
                    // prize delete progressbar by zhaojian 20171121 start
                    //showSearchProgress(true);
                    // prize delete progressbar by zhaojian 20171121 end
                } else {
                    mSearchProgressText.setText(R.string.listFoundAllContactsZero);             
                    mSearchProgressText.sendAccessibilityEvent(
                            AccessibilityEvent.TYPE_VIEW_SELECTED);
                    // prize delete progressbar by zhaojian 20171121 start
                    //showSearchProgress(false);
                    // prize delete progressbar by zhaojian 20171121 end
                }
            }
            showEmptyUserProfile(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACCOUNT_FILTER) {
            if (getActivity() != null) {
                AccountFilterUtil.handleAccountFilterResult(
                        ContactListFilterController.getInstance(getActivity()), resultCode, data);
            } else {
                Log.e(TAG, "[onActivityResult]getActivity() returns null during Fragment");
            }
        }
    }

    private void showEmptyUserProfile(boolean show) {
        // Changing visibility of just the mProfileHeader doesn't do anything unless
        // you change visibility of its children, hence the call to mCounterHeaderView
        // and mProfileTitle
        Log.d(TAG, "[showEmptyUserProfile] show : " + show);
        mProfileHeaderContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        mProfileHeader.setVisibility(show ? View.VISIBLE : View.GONE);
//        mProfileTitle.setVisibility(show ? View.VISIBLE : View.GONE); zhangzhonghao remove 20160305
        //mProfileMessage.setVisibility(show ? View.VISIBLE : View.GONE);//prize-remove-huangliemin-2016-6-8
    }

    /**
     * This method creates a pseudo user profile contact. When the returned query doesn't have
     * a profile, this methods creates 2 views that are inserted as headers to the listview:
     * 1. A header view with the "ME" title and the contacts count.
     * 2. A button that prompts the user to create a local profile
     */
    private void addEmptyUserProfileHeader(LayoutInflater inflater) {
        ListView list = getListView();
        // Add a header with the "ME" name. The view is embedded in a frame view since you cannot
        // change the visibility of a view in a ListView without having a parent view.
        mProfileHeader = inflater.inflate(R.layout.user_profile_header, null, false);
//        mProfileTitle = (TextView) mProfileHeader.findViewById(R.id.profile_title); zhangzhonghao remove 20160305
        mProfileHeaderContainer = new FrameLayout(inflater.getContext());
        mProfileHeaderContainer.addView(mProfileHeader);
        list.addHeaderView(mProfileHeaderContainer, null, false);

        // Add a button with a message inviting the user to create a local profile
        mProfileMessage = (Button) mProfileHeader.findViewById(R.id.user_profile_button);
        mProfileMessage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ExtensionManager.getInstance().getRcsExtension()
                       .addRcsProfileEntryListener(null, true)) {
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_INSERT, Contacts.CONTENT_URI);
                intent.putExtra(ContactEditorFragment.INTENT_EXTRA_NEW_LOCAL_PROFILE, true);
                ImplicitIntentsUtil.startActivityInApp(getActivity(), intent);
            }
        });
        /*prize-add-huangliemin-2016-6-8-start*/
        mSelectAllButton = (Button) mProfileHeader.findViewById(R.id.select_all);
        mSelectAllButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				updateSelectAll();
			}
		});
        /*prize-add-huangliemin-2016-6-8-end*/
        /*prize-add-huangliemin-2016-7-13-start*/
        mManagerGroupsButton = (Button) mProfileHeader.findViewById(R.id.manager_groups);
        mManagerGroupsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ManagerGroups();
			}
		});
        /*prize-add-huangliemin-2016-7-13-end*/
    }
    
    /*prize-add-manager-groups-huangliemin-2016-7-13-start*/
    public void ManagerGroups() {
    	Intent intent = new Intent(getActivity(),GroupBrowseActivity.class);
    	getActivity().startActivity(intent);
    }
    
    public void setManagerGroupsButtonVisibility(boolean flag) {
    	if(mManagerGroupsButton!=null && mProfileMessage!=null) {
    		if(flag) {
    			mManagerGroupsButton.setVisibility(View.VISIBLE);
    			mProfileMessage.setVisibility(View.GONE);
    		} else {
    			mManagerGroupsButton.setVisibility(View.GONE);
    		}
    	}
    }
    /*prize-add-manager-groups-huangliemin-2016-7-13-end*/
    
    /*prize-add-huangliemin-2016-6-8-start*/
    public void setSelectAllButtonTitle(int ResId) {
    	if(mSelectAllButton!=null) {
    		mSelectAllButton.setText(getResources().getString(ResId));
    	}
    }
    
    protected void updateSelectAll() {
    	
    }
    
    public void setSelectAllButtonVisibility(boolean flag) {
    	if(mSelectAllButton!=null && mProfileMessage!=null) {
    		if(flag) {
    			mSelectAllButton.setVisibility(View.VISIBLE);
    			mProfileMessage.setVisibility(View.GONE);
    		} else {
    			mSelectAllButton.setVisibility(View.GONE);
    			mProfileMessage.setVisibility(View.VISIBLE);
    		}
    	}
    }
    /*prize-add-huangliemin-2016-6-8-end*/

    /** M: Bug Fix For ALPS00115673. @{*/
    private ProgressBar mProgress;
    private View mLoadingContainer;
    private WaitCursorView mWaitCursorView;
    private TextView mLoadingContact;
    /** @} */

    /**
     * M: Bug Fix CR ID: ALPS00279111.
     */
    public void closeWaitCursor() {
        // TODO Auto-generated method stub
        Log.d(TAG, "[closeWaitCursor] DefaultContactBrowseListFragment");
        mWaitCursorView.stopWaitCursor();
    }

    /**
     * M: for ALPS01766595.
     */
    private int getContactCount() {
        int count = isSearchMode() ? 0 : getAdapter().getCount();
        if (mUserProfileExists) {
            count -= PROFILE_NUM;
        }
        return count;
    }
}
