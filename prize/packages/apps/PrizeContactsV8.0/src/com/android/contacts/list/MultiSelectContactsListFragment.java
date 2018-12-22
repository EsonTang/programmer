/*
 * Copyright (C) 2015 The Android Open Source Project
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

import com.android.contacts.common.list.ContactListAdapter;
import com.android.contacts.common.list.ContactListItemView;
import com.android.contacts.common.list.DefaultContactListAdapter;
import com.android.contacts.common.logging.SearchState;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.list.MultiSelectEntryContactListAdapter.SelectedContactsListener;
import com.android.contacts.common.logging.Logger;
import com.android.contacts.R;

import com.mediatek.contacts.util.Log;

import android.database.Cursor;
import android.R.integer;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mediatek.contacts.util.ContactsPortableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
/*prize add for bug 58893 zhaojian 20180523 start*/
import com.mediatek.contacts.list.service.MultiChoiceService;
/*prize add for bug 58893 zhaojian 20180523 end*/

/**
 * Fragment containing a contact list used for browsing contacts and optionally selecting
 * multiple contacts via checkboxes.
 */
public class MultiSelectContactsListFragment extends DefaultContactBrowseListFragment
        implements SelectedContactsListener {
    private static final String TAG = "MultiSelectContactsListFragment";

    public interface OnCheckBoxListActionListener {
        void onStartDisplayingCheckBoxes();
        void onSelectedContactIdsChanged();
        void onStopDisplayingCheckBoxes();
    }

    private static final String EXTRA_KEY_SELECTED_CONTACTS = "selected_contacts";
    /// M: If it need to show checkbox, after screen rotation.
    private static final String EXTRA_KEY_SHOW_CHECKBOX = "show_checkbox";

    private static final String KEY_SEARCH_RESULT_CLICKED = "search_result_clicked";

    private OnCheckBoxListActionListener mCheckBoxListListener;

    private boolean mSearchResultClicked;

    // M: DefaultMode default loaderId
    private static final int DEFAULTMODE_LOADERID = 0;

    public void setCheckBoxListListener(OnCheckBoxListActionListener checkBoxListListener) {
        mCheckBoxListListener = checkBoxListListener;
    }

    /**
     * Whether a search result was clicked by the user. Tracked so that we can distinguish
     * between exiting the search mode after a result was clicked from existing w/o clicking
     * any search result.
     */
    public boolean wasSearchResultClicked() {
        return mSearchResultClicked;
    }

    /**
     * Resets whether a search result was clicked by the user to false.
     */
    public void resetSearchResultClicked() {
        mSearchResultClicked = false;
    }
        /* prize add to get listener zhangzhonghao 20160314 start */
    public OnCheckBoxListActionListener getCheckBoxListener(){
        return mCheckBoxListListener;
    }
    /* prize add to get listener zhangzhonghao 20160314 end */

    @Override
    public void onSelectedContactsChanged() {
        Log.d(TAG, "[onSelectedContactsChanged]");
        if (mCheckBoxListListener != null) {
            mCheckBoxListListener.onSelectedContactIdsChanged();
        }
    }

    @Override
    public void onSelectedContactsChangedViaCheckBox() {
        Log.d(TAG, "[onSelectedContactsChangedViaCheckBox]");
        if (getAdapter().getSelectedContactIds().size() == 0) {
            // Last checkbox has been unchecked. So we should stop displaying checkboxes.
//            mCheckBoxListListener.onStopDisplayingCheckBoxes(); zhangzhonghao 20160309
            onSelectedContactsChanged(); //zhangzhonghao 20160314
        } else {
            onSelectedContactsChanged();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            final TreeSet<Long> selectedContactIds = (TreeSet<Long>)
                    savedInstanceState.getSerializable(EXTRA_KEY_SELECTED_CONTACTS);
            /// M: If it need to show checkbox, after screen rotation.
            boolean showCheckBoxes = savedInstanceState.getBoolean(EXTRA_KEY_SHOW_CHECKBOX);
            getAdapter().setDisplayCheckBoxes(showCheckBoxes);
            getAdapter().setSelectedContactIds(selectedContactIds);
            if (mCheckBoxListListener != null) {
                mCheckBoxListListener.onSelectedContactIdsChanged();
            }
            mSearchResultClicked = savedInstanceState.getBoolean(KEY_SEARCH_RESULT_CLICKED);
        }
    }

    public TreeSet<Long> getSelectedContactIds() {
        final MultiSelectEntryContactListAdapter adapter = getAdapter();
        /*prize-add-huangliemin-2016-6-13-start*/
        updateSelectedItemsView();
        if(mIsSelectedAll && isAdded()) {//prize-add  " && isAdded() " -huangpengfei-2016-9-2
        	setSelectAllButtonTitle(R.string.menu_select_none);
        } else if(isAdded()){//prize-add  "if(isAdded())" -huangpengfei-2016-9-28 
        	
        	setSelectAllButtonTitle(R.string.menu_select_all);
        }
        /*prize-add-huangliemin-2016-6-13-end*/
        return adapter.getSelectedContactIds();
    }

    @Override
    public MultiSelectEntryContactListAdapter getAdapter() {
        return (MultiSelectEntryContactListAdapter) super.getAdapter();
    }

    @Override
    protected void configureAdapter() {
        super.configureAdapter();
        getAdapter().setSelectedContactsListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(EXTRA_KEY_SELECTED_CONTACTS, getSelectedContactIds());
        outState.putBoolean(KEY_SEARCH_RESULT_CLICKED, mSearchResultClicked);

        /// M: If it need to show checkbox, after screen rotation.
        outState.putBoolean(EXTRA_KEY_SHOW_CHECKBOX,
                getSelectedContactIds().size() > 0 ? true : false);
    }

    public void displayCheckBoxes(boolean displayCheckBoxes) {
        getAdapter().setDisplayCheckBoxes(displayCheckBoxes);
        if (!displayCheckBoxes) {
            clearCheckBoxes();
        }
    }

    public void clearCheckBoxes() {
        getAdapter().setSelectedContactIds(new TreeSet<Long>());
    }

    @Override
    protected boolean onItemLongClick(int position, long id) {
    	/*prize-add-huangliemin-2016-7-27*/
    	if(true) {
    		return true;
    	}
    	/*prize-add-huangliemin-2016-7-27*/
        Log.d(TAG, "[onItemLongClick]position = " + position + ",id = " + id);
        final int previouslySelectedCount = getAdapter().getSelectedContactIds().size();
        final Uri uri = getAdapter().getContactUri(position);

        /// M: If is SDN number, can't do long click to multiSelect
        boolean isSupportMultiSelect = true;
        final int partition = getAdapter().getPartitionForPosition(position);
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            isSupportMultiSelect = uri != null && (partition == ContactsContract.Directory.DEFAULT
                    && (position > 0 || !getAdapter().hasProfile()))
                    && !getAdapter().isSdnNumber(position);
        } else {
            isSupportMultiSelect = uri != null && (partition == ContactsContract.Directory.DEFAULT
                    && (position > 0 || !getAdapter().hasProfile()));
        }
        if (isSupportMultiSelect) {
            final String contactId = uri.getLastPathSegment();
            if (!TextUtils.isEmpty(contactId)) {
                if (mCheckBoxListListener != null) {
                    mCheckBoxListListener.onStartDisplayingCheckBoxes();
                }
                getAdapter().toggleSelectionOfContactId(Long.valueOf(contactId));
                // Manually send clicked event if there is a checkbox.
                // See b/24098561.  TalkBack will not read it otherwise.
                final int index = position + getListView().getHeaderViewsCount() - getListView()
                        .getFirstVisiblePosition();
                if (index >= 0 && index < getListView().getChildCount()) {
                    getListView().getChildAt(index).sendAccessibilityEvent(AccessibilityEvent
                            .TYPE_VIEW_CLICKED);
                }
            }
        }
        final int nowSelectedCount = getAdapter().getSelectedContactIds().size();
        if (mCheckBoxListListener != null
                && previouslySelectedCount != 0 && nowSelectedCount == 0) {
            // Last checkbox has been unchecked. So we should stop displaying checkboxes.
            Log.d(TAG, "[onItemLongClick]onStopDisplayingCheckBoxes");
//            mCheckBoxListListener.onStopDisplayingCheckBoxes(); zhangzhonghao 20160309
        }
        return true;
    }

    @Override
    protected void onItemClick(int position, long id) {
        Log.d(TAG, "[onItemClick]position = " + position + ",id = " + id);
        final Uri uri = getAdapter().getContactUri(position);
        if (uri == null) {
            Log.w(TAG, "[onItemClick]uri is null,return.");
            return;
        }
        /// M: If is SDN number, can't do click on multiSelection
        boolean canMultiSelect = true;
        if (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT) {
            canMultiSelect = getAdapter().isDisplayingCheckBoxes()
                    && !getAdapter().isSdnNumber(position);
        } else {
            canMultiSelect = getAdapter().isDisplayingCheckBoxes();
        }
        if (canMultiSelect) {
            final String contactId = uri.getLastPathSegment();
            if (!TextUtils.isEmpty(contactId)) {
                getAdapter().toggleSelectionOfContactId(Long.valueOf(contactId));
            }
        } else {
            if (isSearchMode()) {
                mSearchResultClicked = true;
                Logger.logSearchEvent(createSearchStateForSearchResultClick(position));
            }
            super.onItemClick(position, id);
        }
        if (mCheckBoxListListener != null && getAdapter().getSelectedContactIds().size() == 0) {
//            mCheckBoxListListener.onStopDisplayingCheckBoxes(); //zhangzhonghao 20160309
        }
    }

    /**
     * Returns the state of the search results currently presented to the user.
     */
    public SearchState createSearchState() {
        return createSearchState(/* selectedPosition */ -1);
    }

    /**
     * Returns the state of the search results presented to the user
     * at the time the result in the given position was clicked.
     */
    public SearchState createSearchStateForSearchResultClick(int selectedPosition) {
        return createSearchState(selectedPosition);
    }

    private SearchState createSearchState(int selectedPosition) {
        final MultiSelectEntryContactListAdapter adapter = getAdapter();
        if (adapter == null) {
            return null;
        }
        final SearchState searchState = new SearchState();
        searchState.queryLength = adapter.getQueryString() == null
                ? 0 : adapter.getQueryString().length();
        searchState.numPartitions = adapter.getPartitionCount();

        // Set the number of results displayed to the user.  Note that the adapter.getCount(),
        // value does not always match the number of results actually displayed to the user,
        // which is why we calculate it manually.
        final List<Integer> numResultsInEachPartition = new ArrayList<>();
        for (int i = 0; i < adapter.getPartitionCount(); i++) {
            final Cursor cursor = adapter.getCursor(i);
            if (cursor == null || cursor.isClosed()) {
                // Something went wrong, abort.
                numResultsInEachPartition.clear();
                break;
            }
            numResultsInEachPartition.add(cursor.getCount());
        }
        if (!numResultsInEachPartition.isEmpty()) {
            int numResults = 0;
            for (int i = 0; i < numResultsInEachPartition.size(); i++) {
                numResults += numResultsInEachPartition.get(i);
            }
            searchState.numResults = numResults;
        }

        // If a selection was made, set additional search state
        if (selectedPosition >= 0) {
            searchState.selectedPartition = adapter.getPartitionForPosition(selectedPosition);
            searchState.selectedIndexInPartition = adapter.getOffsetInPartition(selectedPosition);
            final Cursor cursor = adapter.getCursor(searchState.selectedPartition);
            searchState.numResultsInSelectedPartition =
                    cursor == null || cursor.isClosed() ? -1 : cursor.getCount();

            // Calculate the index across all partitions
            if (!numResultsInEachPartition.isEmpty()) {
                int selectedIndex = 0;
                for (int i = 0; i < searchState.selectedPartition; i++) {
                    selectedIndex += numResultsInEachPartition.get(i);
                }
                selectedIndex += searchState.selectedIndexInPartition;
                searchState.selectedIndex = selectedIndex;
            }
        }
        return searchState;
    }

    @Override
    protected ContactListAdapter createListAdapter() {
        Log.d(TAG, "[createListAdapter]");
        DefaultContactListAdapter adapter = new MultiSelectEntryContactListAdapter(getContext());
        adapter.setSectionHeaderDisplayEnabled(isSectionHeaderDisplayEnabled());
        adapter.setDisplayPhotos(true);
        adapter.setPhotoPosition(
                ContactListItemView.getDefaultPhotoPosition(/* opposite = */ false));
        adapter.setIsPeopleActivity(mIsPeopleActivity);//prize add for dido os8.0-hpf-2018-1-2
        return adapter;
    }

    /// M: Add for SelectAll/DeSelectAll Feature. @{
    // SelectAll or DeselectAll items.
    private boolean mIsSelectedAll = false;

    public void updateSelectedItemsView() {
        int count = getAdapter().getCount();
        Log.d(TAG, "[updateSelectedItemsView] count = " + count + ",isSearchMode: "
                + isSearchMode() + ",hasProfile: " + getAdapter().hasProfile()
                + ",sdnNumber: " + getAdapter().getSdnNumber());
        /// M: we should ingore head "all contacts" title in search mode
        if (isSearchMode()) {
            count--;
        }
        if (getAdapter().hasProfile()) {
            count--;
        }
        if (getAdapter().getSdnNumber() > 0) {
            count -= getAdapter().getSdnNumber();
        }
        int checkCount = getAdapter().getSelectedContactIds().size();
        Log.d(TAG, "[updateSelectedItemsView]count = " + count + ",checkcount: " + checkCount);
        // Add consideration of "0" case
        if (count != 0 && count == checkCount) {
            mIsSelectedAll = true;
        } else {
            mIsSelectedAll = false;
        }
    }

    /**
     * @return mIsSelectedAll
     */
    public boolean isSelectedAll() {
        return mIsSelectedAll;
    }

    public void updateCheckBoxState(boolean checked) {
        int position = 0;
        final int count = getAdapter().getCount();

        /// M: we should ingore head "all contacts" title in search mode
        if (isSearchMode()) {
            position++;
        }

        Log.d(TAG, "[updateCheckBoxState]checked = " + checked + ",count = " + count + ",postion: "
                + position + ",isSearchMode: " + isSearchMode());
        long contactId = -1;
        if (checked) {
            for (; position < count; position++) {
                if (getAdapter().isUserProfile(position)
                        || (ContactsPortableUtils.MTK_PHONE_BOOK_SUPPORT && getAdapter()
                                .isSdnNumber(position))) {
                    continue;
                }
                if (!getListView().isItemChecked(position)) {
                    getListView().setItemChecked(position, checked);
                    contactId = getAdapter().getContactId(position);
                    getSelectedContactIds().add(contactId);
                }
            }
        } else {
            getSelectedContactIds().clear();
        }
    }
    /*@}*/

    /// M: Fix CR ALPS02273774: Change airplane mode when select contacts. @{
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "[onLoadFinished]");
        super.onLoadFinished(loader, data);

        long dataId = -1;
        int position = 0;
        Set<Long> newDataSet = new HashSet<Long>();
        if (data != null && !data.isClosed() && data.getCount() != 0) {//prize-add " && !cursor.isClosed()"fix bug[52385]-hpf-2018-3-15
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                dataId = -1;
                Log.d(TAG, "data.getLong(0) = " + data.getLong(0));   //zj
                dataId = data.getLong(0);
                if (dataId != -1) {
                    newDataSet.add(dataId);
                }
            }
        }
        int sizeBefore = getAdapter().getSelectedContactIds().size();
        // M: fix ALPS02459978,remove selected should judge loaderiId caused by search mode
        // will start other loader,every loader finished,will start this follow.@{
        int loaderId = loader.getId();
        Log.d(TAG,"[onLoadFinished]sizeBefore = " + sizeBefore + ",loader =" + loader +
                ",loaderId = " + loaderId + ",newDataSet: " + newDataSet.toString() +
                ",currentSelected: " + getAdapter().getSelectedContactIds().toString());
        for (Iterator<Long> it = getAdapter().getSelectedContactIds().iterator(); it.hasNext();) {
            Long id = it.next();
            if ((loaderId == DEFAULTMODE_LOADERID) && !newDataSet.contains(id)) {
                Log.d(TAG, "[onLoadFinished] selected removeId = " + id);
            // @}
                it.remove();
            }
        }
        int sizeAfter = getAdapter().getSelectedContactIds().size();
        Log.d(TAG,"[onLoadFinished]sizeAfter = " + sizeAfter);
        if (data != null && !data.isClosed() && data.getCount() != 0) {//prize-add " && !cursor.isClosed()"fix bug[52385]-hpf-2018-3-15
            data.moveToPosition(-1);
            while (data.moveToNext()) {
                dataId = -1;
                dataId = data.getLong(0);
                if (getAdapter().getSelectedContactIds().contains(dataId)) {
                    getListView().setItemChecked(position, true);
                }
                ++position;
            }
        }

        if (sizeAfter > 0) {
            onSelectedContactsChanged();
            updateSelectedItemsView();
        } else if (sizeBefore > 0 || getAdapter().getCount() == 0) {
            if (mCheckBoxListListener != null) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mCheckBoxListListener.onStopDisplayingCheckBoxes();//exit the select state
                    }
                });
            }
        }
    }
    /// @}
    /*prize-add-huangliemin-2016-6-8-start*/
    @Override
    protected void updateSelectAll(){
    	if(mIsSelectedAll) {
    		updateCheckBoxState(false);
        	displayCheckBoxes(true);
    	} else {
    		updateCheckBoxState(true);
        	displayCheckBoxes(true);
    	}
    }
    /*prize-add-huangliemin-2016-6-8-end*/

    /* prize add for dido os8.0-hpf-2018-1-2-start */
    private boolean mIsPeopleActivity = false;
    public MultiSelectContactsListFragment() {
		super();
	}
	public MultiSelectContactsListFragment(boolean isPeopleActivity) {
		super();
		mIsPeopleActivity = isPeopleActivity;
	}
	/* prize add for dido os8.0-hpf-2018-1-2-end */

//    /*prize-add-fix bug [55320] -hpf-2018-05-17-start*/
//    //The parent method in ContactEntryListFragment
//    @Override
//    public boolean isDeletionProcessorRun(){
//        boolean isProcessing = MultiChoiceService.isProcessing(MultiChoiceService.TYPE_DELETE);
//        Log.d(TAG, "[isDeletionProcessorRun]  isProcessing = " + isProcessing);
//        return isProcessing;
//    }
//    /*prize-add-fix bug [55320] -hpf-2018-05-17-end*/
    
}
