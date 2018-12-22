/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts.editor;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Organization;//prize-add-huangliemin-2016-6-6
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.WindowManager;
import android.widget.Button;//prize-add-huangliemin-2016-6-6
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.dataitem.DataKind;
import com.android.contacts.widget.DialogBottomMenu;
import com.android.contacts.widget.DialogItemOnClickListener;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.ValuesDelta;

import com.google.common.base.Objects;

import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.aassne.SimAasEditor;
import com.mediatek.contacts.editor.ContactEditorUtilsEx;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.Log;

import java.util.ArrayList;

/**
 * Custom view that provides all the editor interaction for a specific
 * {@link Contacts} represented through an {@link RawContactDelta}. Callers can
 * reuse this view and quickly rebuild its contents through
 * {@link #setState(RawContactDelta, AccountType, ViewIdGenerator)}.
 * <p>
 * Internal updates are performed against {@link ValuesDelta} so that the
 * source {@link RawContact} can be swapped out. Any state-based changes, such as
 * adding {@link Data} rows or changing {@link EditType}, are performed through
 * {@link RawContactModifier} to ensure that {@link AccountType} are enforced.
 */
public class RawContactEditorView extends BaseRawContactEditorView implements DialogItemOnClickListener {
    private static final String KEY_SUPER_INSTANCE_STATE = "superInstanceState";

    private LayoutInflater mInflater;

    private StructuredNameEditorView mName;
    private PhoneticNameEditorView mPhoneticName;
    //prize-delete-huangliemin-2016-6-6
    //private TextFieldsEditorView mNickName;

    private GroupMembershipView mGroupMembershipView;

    private ViewGroup mFields;

    private View mAccountSelector;
    private TextView mAccountSelectorTypeTextView;
    private TextView mAccountSelectorNameTextView;

    private View mAccountHeader;
    private TextView mAccountHeaderTypeTextView;
    private TextView mAccountHeaderNameTextView;
    private ImageView mAccountIconImageView;

    private long mRawContactId = -1;
    private boolean mAutoAddToDefaultGroup = true;
    private Cursor mGroupMetaData;
    private DataKind mGroupMembershipKind;
    private RawContactDelta mState;
    
    /*prize-add-huangliemin-2016-6-6-start*/
    private View mAddFieldButton;
    //prize-change-huangliemin-2016-7-7-for-change-to-PopupWindow-start
    
    //Prize-add-zhudaopeng-2016-08-04 Start
    private DialogBottomMenu mBottomMenuDialog;
    //    private PopupMenu mPopupMenu;
    //    private PopupWindow mPopupWindow;
    //    private View mPopuWindowView;
    //Prize-add-zhudaopeng-2016-08-04 Start
    
    //prize-change-huangliemin-2016-7-7-for-change-to-PopupWindow-end
    private TextFieldsEditorView mCompany;
    private boolean mPhoneticNameAdded;
    /*prize-add-huangliemin-2016-6-6-end*/
    
    private View prizeDeleteContactOrigin; //zhangzhonghao 20160414

    public RawContactEditorView(Context context) {
        super(context);
    }

    public RawContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        View view = getPhotoEditor();
        if (view != null) {
            view.setEnabled(enabled);
        }

        if (mName != null) {
            mName.setEnabled(enabled);
        }

        if (mPhoneticName != null) {
            mPhoneticName.setEnabled(enabled);
        }

        if (mFields != null) {
            int count = mFields.getChildCount();
            for (int i = 0; i < count; i++) {
                mFields.getChildAt(i).setEnabled(enabled);
            }
        }

        if (mGroupMembershipView != null) {
            mGroupMembershipView.setEnabled(enabled);
        }
        
        /*prize-add-huangliemin-2016-6-6-start*/
        if(mAddFieldButton !=null) {
            mAddFieldButton.setEnabled(enabled);
        }
        /*prize-add-huangliemin-2016-6-6-end*/
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mName = (StructuredNameEditorView)findViewById(R.id.edit_name);
        mName.setDeletable(false);

        mPhoneticName = (PhoneticNameEditorView)findViewById(R.id.edit_phonetic_name);
        mPhoneticName.setDeletable(false);
        //prize-delete-huangliemin-2016-6-6
        //mNickName = (TextFieldsEditorView)findViewById(R.id.edit_nick_name);

        mFields = (ViewGroup)findViewById(R.id.sect_fields);

        mAccountHeader = findViewById(R.id.account_header_container);
        mAccountHeaderTypeTextView = (TextView) findViewById(R.id.account_type);
        mAccountHeaderNameTextView = (TextView) findViewById(R.id.account_name);
        
//        mAccountIconImageView = (ImageView) findViewById(android.R.id.icon);prize-remove-huangpengfei-2016-10-27

        // The same header is used by both full editor and read-only editor view. The header is
        // left-aligned with read-only editor view but is not aligned well with full editor. So we
        // need to shift the text in the header a little bit for full editor.
        
        /*prize-remove-huangpengfei-2016-10-27-start*/
//        LinearLayout accountInfoView = (LinearLayout) findViewById(R.id.account_info);
//        final int topBottomPaddingDp = (int) getResources().getDimension(R.dimen
//                .editor_account_header_expandable_top_bottom_padding);
//        final int leftPaddingDp = (int) getResources().getDimension(R.dimen
//                .editor_account_header_expandable_left_padding);
//        accountInfoView.setPadding(leftPaddingDp, topBottomPaddingDp, 0, topBottomPaddingDp);
        /*prize-remove-huangpengfei-2016-10-27-end*/

        mAccountSelector = findViewById(R.id.account_selector_container);
        mAccountSelectorTypeTextView = (TextView) findViewById(R.id.account_type_selector);
        mAccountSelectorNameTextView = (TextView) findViewById(R.id.account_name_selector);
        
        prizeDeleteContactOrigin = findViewById(R.id.prize_editor_delete); //zhangzhonghao
        /*prize-add-huangliemin-2016-6-6-start*/
        mCompany = (TextFieldsEditorView)findViewById(R.id.edit_company);
        mCompany.setDeletable(false);
        mAddFieldButton = findViewById(R.id.button_add_field);
        if(mAddFieldButton!=null) {
          mAddFieldButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAddInformationPopupWindow();
                }
            });
        }
        /*prize-add-huangliemin-2016-6-6-end*/
        mCompany.setPrizeFieldsEditorBtnEnable(false);//prize-add for dido os 8.0-hpf-2017-7-19
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        // super implementation of onSaveInstanceState returns null
        bundle.putParcelable(KEY_SUPER_INSTANCE_STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable(KEY_SUPER_INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * Set the internal state for this view, given a current
     * {@link RawContactDelta} state and the {@link AccountType} that
     * apply to that state.
     */
    @Override
    public void setState(RawContactDelta state, AccountType type, ViewIdGenerator vig,
            boolean isProfile) {

        Log.d(TAG, "[setState]state: " + state + ", AccountType: " + type + ", isProfile: "
                + isProfile);
        mState = state;

        // Remove any existing sections
        mFields.removeAllViews();

        // Bail if invalid state or account type
        if (state == null || type == null) return;

        setId(vig.getId(state, null, null, ViewIdGenerator.NO_VIEW_INDEX));

        // Make sure we have a StructuredName
        RawContactModifier.ensureKindExists(state, type, StructuredName.CONTENT_ITEM_TYPE);
        /*prize-add-huangliemin-2016-6-6 start*/
        RawContactModifier.ensureKindExists(state, type, Organization.CONTENT_ITEM_TYPE);
        /*prize-add-huangliemin-2016-6-6 end*/

        mRawContactId = state.getRawContactId();

        /// M: For displaying SIM name. @{
        String accountName = state.getValues().getAsString(RawContacts.ACCOUNT_NAME);
        if (type.isIccCardAccount()) {
            mSubId = AccountTypeUtils.getSubIdBySimAccountName(mContext, accountName);
            Log.d(TAG, "[setState]subId: " + mSubId + ",AccountName: " + accountName);
            if (SubInfoUtils.getDisplaynameUsingSubId(mSubId) != null) {
                accountName = SubInfoUtils.getDisplaynameUsingSubId(mSubId);
                Log.d(TAG, "[setState] accountName: " + accountName);
            }
            /// M: OP09 icon replace.
            ContactEditorUtilsEx.setDefaultIconForEditor(mSubId, getPhotoEditor());
            /// M: Bug fix ALPS01413181
            if (ContactEditorUtilsEx.finishActivityIfInvalidSubId(getContext(), mSubId)) {
                Log.d(TAG, "[setState]finishActivityIfInvalidSubId,return.");
                return;
            }
        }
        /// @}

        ///M:
        int activatedSubInfoCount = SubInfoUtils.getActivatedSubInfoCount();
        Log.d(TAG,"[setState]activatedSubInfoCount = " + activatedSubInfoCount
                + ",accountType = " + type.accountType+"   accountName = "+accountName);
        // Fill in the account info
        final Pair<String,String> accountInfo = isProfile
                ? EditorUiUtils.getLocalAccountInfo(getContext(), state.getAccountName(), type)
                : EditorUiUtils.getAccountInfo(getContext(), state.getAccountName(), type);
        if (accountInfo.first == null) {
            // Hide this view so the other text view will be centered vertically
            mAccountHeaderNameTextView.setVisibility(View.GONE);
            /*prize-add for dido os 8.0-hpf-2017-8-4-start*/
            mAccountHeaderTypeTextView.setText(mContext.getString(R.string.prize_phone_info_lable));
            ((com.android.contacts.activities.ContactEditorActivity)mContext).mIsPersonalInformation = true;
            /*prize-add for dido os 8.0-hpf-2017-8-4-end*/
        } else {
            mAccountHeaderNameTextView.setVisibility(View.GONE);//prize-change-huangliemin-2016-6-30
            mAccountHeaderNameTextView.setText(accountInfo.first);

            /// M: Modify for SIM indicator feature. @{
            if (AccountTypeUtils.isAccountTypeIccCard(type.accountType)) {
                if(activatedSubInfoCount <= 1){
                    mAccountHeaderNameTextView.setVisibility(View.GONE);
                } else if (accountInfo.first != null) {
                    int subId = AccountTypeUtils.getSubIdBySimAccountName(getContext(),
                            accountInfo.first);
                    String account_name = SubInfoUtils.getDisplaynameUsingSubId(subId);
                    mAccountHeaderNameTextView.setText(account_name);
                }
            }
            // @}

            /// M: Bug fix ALPS00453091, if local phone account, set string to "Phone contact" @{
            if (AccountWithDataSetEx.isLocalPhone(type.accountType)) {
                CharSequence accountType = type.getDisplayLabel(mContext);
                if (TextUtils.isEmpty(accountType)) {
                    accountType = mContext.getString(R.string.account_phone_only);
                }
                /*prize-change for dido os 8.0-hpf-2017-7-19-start*/
                mAccountHeaderTypeTextView.setText(/*accountType*/mContext.getString(R.string.account_type_format, accountType));
                /*prize-change for dido os 8.0-hpf-2017-7-19-end*/
            } else {
                mAccountHeaderTypeTextView.setText(accountInfo.second);
            }
            // @}
        }

        updateAccountHeaderContentDescription();

        // The account selector and header are both used to display the same information.
        mAccountSelectorTypeTextView.setText(mAccountHeaderTypeTextView.getText());
        mAccountSelectorTypeTextView.setVisibility(mAccountHeaderTypeTextView.getVisibility());
        mAccountSelectorNameTextView.setText(mAccountHeaderNameTextView.getText());

        /// M: Modify for SIM indicator feature. @{
        if (AccountTypeUtils.isAccountTypeIccCard(type.accountType)) {
            if(activatedSubInfoCount <= 1){
            mAccountSelectorNameTextView.setVisibility(View.GONE);
        } else {
                mAccountSelectorNameTextView.setVisibility(
                        mAccountHeaderNameTextView.getVisibility());
            }
        }
        /// @}

        // Showing the account header at the same time as the account selector drop down is
        // confusing. They should be mutually exclusive.
        mAccountHeader.setVisibility(mAccountSelector.getVisibility() == View.GONE
                ? View.VISIBLE : View.GONE);

        /* prize-remove-huangpengfei-2016-10-27-start*/
//        mAccountIconImageView.setImageDrawable(state.getRawContactAccountType(getContext())
//                .getDisplayIcon(getContext()));
        /*prize-remove-huangpengfei-2016-10-27-end*/

        // Show photo editor when supported
        RawContactModifier.ensureKindExists(state, type, Photo.CONTENT_ITEM_TYPE);
        setHasPhotoEditor((type.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null));
        getPhotoEditor().setEnabled(isEnabled());
        mName.setEnabled(isEnabled());
        /*prize-add-huangliemin-2016-6-6 start*/
        mCompany.setEnabled(isEnabled());
        /*prize-add-huangliemin-2016-6-6 end*/

        mPhoneticName.setEnabled(isEnabled());

        // Show and hide the appropriate views
        mFields.setVisibility(View.VISIBLE);
        mName.setVisibility(View.VISIBLE);
        mPhoneticName.setVisibility(View.VISIBLE);

        /// M:AAS[COMMD_FOR_AAS] mPhoneticName -> GONE.
        GlobalEnv.getAasExtension().updateView(state, mPhoneticName, null,
                SimAasEditor.VIEW_UPDATE_VISIBILITY);

        mGroupMembershipKind = type.getKindForMimetype(GroupMembership.CONTENT_ITEM_TYPE);
        if (mGroupMembershipKind != null) {
			/*PRIZE-contact_info-xiaxuefeng-2015-4-21-start*/
			//mGroupMembershipView = (GroupMembershipView)mInflater.inflate(
            //        R.layout.item_group_membership, mFields, false);
            mGroupMembershipView = (GroupMembershipView)mInflater.inflate(
                    R.layout.item_group_membership_prize_xiaxuefeng_2015_4_21, mFields, false);
			/*PRIZE-contact_info-xiaxuefeng-2015-4-21-end*/
            mGroupMembershipView.setKind(mGroupMembershipKind);
            mGroupMembershipView.setEnabled(isEnabled());
        }

        // Create editor sections for each possible data kind
        for (DataKind kind : type.getSortedDataKinds()) {
            // Skip kind of not editable
            if (!kind.editable) continue;

            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for structured name
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                mName.prizeSetEditTextLengthFilter(50);
                mName.setValues(
                        type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME),
                        primary, state, false, vig);
                mPhoneticName.prizeSetEditTextLengthFilter(50);
                mPhoneticName.setValues(
                        type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME),
                        primary, state, false, vig);
                // It is useful to use Nickname outside of a KindSectionView so that we can treat it
                // as a part of StructuredName's fake KindSectionView, even though it uses a
                // different CP2 mime-type. We do a bit of extra work below to make this possible.
                //prize-delete-huangliemin-2016-6-6
                /*
                final DataKind nickNameKind = type.getKindForMimetype(Nickname.CONTENT_ITEM_TYPE);
                if (nickNameKind != null) {
                    ValuesDelta primaryNickNameEntry = state.getPrimaryEntry(nickNameKind.mimeType);
                    if (primaryNickNameEntry == null) {
                        primaryNickNameEntry = RawContactModifier.insertChild(state, nickNameKind);
                    }
                    mNickName.setValues(nickNameKind, primaryNickNameEntry, state, false, vig);
                    mNickName.setDeletable(false);
                } else {
                    mPhoneticName.setPadding(0, 0, 0, (int) getResources().getDimension(
                            R.dimen.editor_padding_between_editor_views));
                    mNickName.setVisibility(View.GONE);
                }
                */
                //prize-delete-huangliemin-2016-6-6
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Handle special case editor for photos
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                getPhotoEditor().setValues(kind, primary, state, false, vig);
                Log.d(TAG, "set photo, primary: " + primary);
            } else if(Organization.CONTENT_ITEM_TYPE.equals(mimeType)) {
                /*prize-add-huangliemin-2016-6-6 start*/
            	mCompany.prizeSetEditTextLengthFilter(50);
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                mCompany.setValues(
                  type.getKindForMimetype(Organization.CONTENT_ITEM_TYPE),
                  primary, state, false, vig);
                /*prize-add-huangliemin-2016-6-6 end*/
            } else if (GroupMembership.CONTENT_ITEM_TYPE.equals(mimeType)) {
                if (mGroupMembershipView != null) {
                    mGroupMembershipView.setState(state);
                    /// M: Bug Fix for ALPS00440157, add isProfile check.
                    if (!isProfile) {
                        mFields.addView(mGroupMembershipView);
                    }
                }
            /// M: Bug fix ALPS00566570,some USIM card do not support storing Email address. @{
            } else if (Email.CONTENT_ITEM_TYPE.equals(mimeType) && type.isUSIMAccountType()
                    && SimCardUtils.getIccCardEmailCount(mSubId) <= 0) {
                Log.d(TAG, "[setState] It's USIM account and no Email field in subId: " + mSubId);
                /// M: Bug fix ALPS01583209, the state may already have email entry when switch
                //  from AccountSwitcher, here need to clear email address entry.
                if (state.hasMimeEntries(Email.CONTENT_ITEM_TYPE)) {
                    state.removeEntry(Email.CONTENT_ITEM_TYPE);
                }
                continue;
            /// @}
            }/* else if (DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME.equals(mimeType)
                    || DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(mimeType)
                    || Nickname.CONTENT_ITEM_TYPE.equals(mimeType)) {
                // Don't create fields for each of these mime-types. They are handled specially.
                continue;
            }*/ else {
                // Otherwise use generic section-based editors
                if (kind.fieldList == null) continue;
                final KindSectionView section = (KindSectionView)mInflater.inflate(
                        R.layout.item_kind_section, mFields, false);
                section.setEnabled(isEnabled());
                section.setState(kind, state, /* readOnly =*/ false, vig);
                mFields.addView(section);
            }
        }
        Log.d(TAG, "[setState]addToDefaultGroupIfNeeded. ");
        /*prize-add-huangliemin-2016-6-6 start*/
        if (type.getKindForMimetype(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME) != null) {
            updatePhoneticNameVisibility();
        }
        /*prize-add-huangliemin-2016-6-6 end*/
        addToDefaultGroupIfNeeded();
        
        /*prize-add-huangliemin-2016-6-6-start*/
        final int sectionCount = getSectionViewsWithoutFields().size();
        if (mAddFieldButton != null) {
            mAddFieldButton.setVisibility(sectionCount > 0 ? View.VISIBLE : View.GONE);
            mAddFieldButton.setEnabled(isEnabled());
        }
        /*prize-add-huangliemin-2016-6-6-end*/
        
        addPhoneNumItem(state);//prize-add-fix bug[54190] -huangpengfei-2016-10-19
    }

    @Override
    public void setGroupMetaData(Cursor groupMetaData) {
        mGroupMetaData = groupMetaData;
        addToDefaultGroupIfNeeded();
        if (mGroupMembershipView != null) {
            mGroupMembershipView.setGroupMetaData(groupMetaData);
        }
    }

    /**
     * M: For sim/usim contact.
     */
    @Override
    public void setSubId(int subId) {
        if (mGroupMembershipView != null) {
            mGroupMembershipView.setSubId(subId);
        }
    }

    public void setAutoAddToDefaultGroup(boolean flag) {
        this.mAutoAddToDefaultGroup = flag;
    }

    /**
     * If automatic addition to the default group was requested (see
     * {@link #setAutoAddToDefaultGroup}, checks if the raw contact is in any
     * group and if it is not adds it to the default group (in case of Google
     * contacts that's "My Contacts").
     */
    private void addToDefaultGroupIfNeeded() {
        if (!mAutoAddToDefaultGroup || mGroupMetaData == null || mGroupMetaData.isClosed()
                || mState == null) {
            return;
        }

        boolean hasGroupMembership = false;
        ArrayList<ValuesDelta> entries = mState.getMimeEntries(GroupMembership.CONTENT_ITEM_TYPE);
        if (entries != null) {
            for (ValuesDelta values : entries) {
                Long id = values.getGroupRowId();
                if (id != null && id.longValue() != 0) {
                    hasGroupMembership = true;
                    break;
                }
            }
        }

        if (!hasGroupMembership) {
            long defaultGroupId = getDefaultGroupId();
            if (defaultGroupId != -1) {
                ValuesDelta entry = RawContactModifier.insertChild(mState, mGroupMembershipKind);
                if (entry != null) {
                    entry.setGroupRowId(defaultGroupId);
                }
            }
        }
    }

    /**
     * Returns the default group (e.g. "My Contacts") for the current raw contact's
     * account.  Returns -1 if there is no such group.
     */
    private long getDefaultGroupId() {
        String accountType = mState.getAccountType();
        String accountName = mState.getAccountName();
        String accountDataSet = mState.getDataSet();
        mGroupMetaData.moveToPosition(-1);
        while (mGroupMetaData.moveToNext()) {
            String name = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_NAME);
            String type = mGroupMetaData.getString(GroupMetaDataLoader.ACCOUNT_TYPE);
            String dataSet = mGroupMetaData.getString(GroupMetaDataLoader.DATA_SET);
            if (name.equals(accountName) && type.equals(accountType)
                    && Objects.equal(dataSet, accountDataSet)) {
                long groupId = mGroupMetaData.getLong(GroupMetaDataLoader.GROUP_ID);
                if (!mGroupMetaData.isNull(GroupMetaDataLoader.AUTO_ADD)
                            && mGroupMetaData.getInt(GroupMetaDataLoader.AUTO_ADD) != 0) {
                    return groupId;
                }
            }
        }
        return -1;
    }

    public StructuredNameEditorView getNameEditor() {
        return mName;
    }

    public TextFieldsEditorView getPhoneticNameEditor() {
        return mPhoneticName;
    }

    //prize-delete-huangliemin-2016-6-6
    /*
    public TextFieldsEditorView getNickNameEditor() {
        return mNickName;
    }
    */
    //prize-delete-huangliemin-2016-6-6

    @Override
    public long getRawContactId() {
        return mRawContactId;
    }

    /** M: For sim/usim contact and bug fix. @{ */
    private static final String TAG = "RawContactEditorView";
    private int mSubId = SubInfoUtils.getInvalidSubId();
    /** @} **/

    /* prize zhangzhonghao get delete view start */
    /**
     * get this view in fragment
     * 
     * @return
     */
    public View getDeleteView(){
        return prizeDeleteContactOrigin;
    }
    /* prize zhangzhonghao get delete view end */
    
    /*prize-add-huangliemin-2016-6-6-start*/
    /*prize-change-huangliemin-for-show-menu-on-bottom-to-top-2016-7-7-start*/
    /*
    private void showAddInformationPopupWindow() {
    	final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();
    	if(mPopupMenu == null) {
    		mPopupMenu = new PopupMenu(getContext(), mAddFieldButton);
    	}
    	
    	final Menu menu = mPopupMenu.getMenu();
    	menu.clear();
    	for(int i=0;i<fields.size();i++) {
    		menu.add(Menu.NONE, i, Menu.NONE, fields.get(i).getTitle());
    	}
    	
    	mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// TODO Auto-generated method stub
				final KindSectionView view = fields.get(item.getItemId());
				if(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
					mPhoneticNameAdded = true;
          updatePhoneticNameVisibility();
          mPhoneticName.requestFocus();
				} else {
					view.addItem();
				}
				
				if(fields.size() == 1) {
					mAddFieldButton.setVisibility(View.GONE);
				}
				return true;
			}
		});
    	
    	mPopupMenu.show();
    }
    */
    
    
    private void showAddInformationPopupWindow() {
    	Log.d(TAG,"[showAddInformationPopupWindow]");
    	//Prize-change-zhudaopeng-2016-08-04 Start
    	final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();
    	ArrayList<String> mMenuItemIds = new ArrayList<String>();
    	for(KindSectionView field:fields){
    		mMenuItemIds.add(field.getTitle());
    	}
    	Resources mRes = getContext().getResources();
    	mBottomMenuDialog = new DialogBottomMenu(getContext(),mRes.getString(R.string.prize_add_other_item),
    			R.layout.dialog_bottom_menu_item);
    	mBottomMenuDialog.setMenuItem(mMenuItemIds);
    	mBottomMenuDialog.setMenuItemOnClickListener(this);
    	mBottomMenuDialog.show();
    	
//    	if(mPopuWindowView==null){
//    		mPopuWindowView = View.inflate(getContext(), R.layout.prize_popupwindow, null);
//    	}
//    	final ViewGroup mContent = (ViewGroup)mPopuWindowView.findViewById(R.id.prize_popup_content);
//
//    	if(mPopupWindow == null) {
//    		mPopupWindow = new PopupWindow(mPopuWindowView, 
//    				WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//    		//mPopupWindow.setAnimationStyle(R.style.PrizePopupWindowStyle);
//    		mPopupWindow.setFocusable(true);
//    		mPopupWindow.setOutsideTouchable(false);
//    		//mPopupWindow.update();
//    	}
//    	
//    	TextView TitleItem = (TextView)mPopuWindowView.findViewById(R.id.content_main_title);
//    	View TitleDivider = mPopuWindowView.findViewById(R.id.title_divider);//prize-add-huangliemin-2016-7-29
//    	TitleItem.setText(getResources().getString(R.string.prize_add_other_item));
//    	TitleDivider.setVisibility(View.VISIBLE);//prize-add-huangliemin-2016-7-29
//    	TitleItem.setVisibility(View.VISIBLE);
//    	
//    	mContent.removeAllViews();
    	/*
    	TextView TitleItem = (TextView)View.inflate(getContext(), R.layout.prize_popmenu_text_huangliemin_2016_7_8, null);
    	TitleItem.setGravity(Gravity.CENTER);
    	TitleItem.setText(getResources().getString(R.string.prize_add_other_item));
    	mContent.addView(TitleItem);
    	*/
    	
//    	for(int i=0;i<fields.size();i++) {
//    		TextView menuItem = (TextView)View.inflate(getContext(), R.layout.prize_popmenu_text_huangliemin_2016_7_8, null);
//    		menuItem.setText(fields.get(i).getTitle());
//    		mContent.addView(menuItem);
//    		menuItem.setOnClickListener(new PopuItemClickListener(i,fields));
//    		
//    		
//    		View Divider = new View(getContext());
//        	Divider.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
//        			1));
//        	Divider.setBackgroundColor(getResources().getColor(R.color.divider_line_color_light));
//        	mContent.addView(Divider);
//    	}
//    	/*prize-add-huangliemin-2016-7-29-start*/
//    	final Activity mActivity = ((Activity)(getContext()));
//    	WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
//    	params.alpha=0.7f;
//    	mActivity.getWindow().setAttributes(params);
//    	mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//			
//			@Override
//			public void onDismiss() {
//				// TODO Auto-generated method stub
//				WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
//				params.alpha=1f;
//				mActivity.getWindow().setAttributes(params);
//			}
//		});
//    	/*prize-add-huangliemin-2016-7-29-end*/
//    	mPopupWindow.showAtLocation(mAddFieldButton, Gravity.BOTTOM, 0, 0);
    	//Prize-change-zhudaopeng-2016-08-04 End
    }
    
    class PopuItemClickListener implements View.OnClickListener {
    	private int Position;
    	ArrayList<KindSectionView> fields;

		public PopuItemClickListener(int position, ArrayList<KindSectionView> list) {
			Position = position;
			fields = list;
		}
    		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final KindSectionView view = fields.get(Position);
			if(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
				mPhoneticNameAdded = true;
                updatePhoneticNameVisibility();
                mPhoneticName.requestFocus();
			} else {
				view.addItem();
			}
			
			if(fields.size() == 1) {
				mAddFieldButton.setVisibility(View.GONE);
			}
//			if(mPopupWindow!=null && mPopupWindow.isShowing()) {
//				mPopupWindow.dismiss();
//			}
		}
    	
    }
    
    //Prize-add-zhudaopeng-2016-08-04 Start
	@Override
	public void onClickMenuItem(View v, int item_index, String item) {
		Log.d(TAG,"[onClickMenuItem] item_index="+item_index);
		final ArrayList<KindSectionView> fields = getSectionViewsWithoutFields();
		final KindSectionView view = fields.get(item_index);
		if(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(view.getKind().mimeType)) {
			mPhoneticNameAdded = true;
            updatePhoneticNameVisibility();
            mPhoneticName.requestFocus();
		} else {
			view.addItem();
		}
		
		if(fields.size() == 1) {
			mAddFieldButton.setVisibility(View.GONE);
		}
	}
	//Prize-add-zhudaopeng-2016-08-04 End
    /*prize-change-huangliemin-for-show-menu-on-bottom-to-top-2016-7-7-start*/
    
    private ArrayList<KindSectionView> getSectionViewsWithoutFields(){
    	final ArrayList<KindSectionView> fields =
    			new ArrayList<KindSectionView>(mFields.getChildCount());
    	for(int i=0;i<mFields.getChildCount();i++){
    		View child = mFields.getChildAt(i);
    		if(child instanceof KindSectionView) {
    			final KindSectionView sectionView = (KindSectionView) child;
    			if(sectionView.getEditorCount() >0){
    				continue;
    			}
    			DataKind kind = sectionView.getKind();
    			if((kind.typeOverallMax == 1) && sectionView.getEditorCount()!=0) {
    				continue;
    			}
    			if(DataKind.PSEUDO_MIME_TYPE_DISPLAY_NAME.equals(kind.mimeType)){
    				continue;
    			}
    			if(DataKind.PSEUDO_MIME_TYPE_PHONETIC_NAME.equals(kind.mimeType)
                        && mPhoneticName.getVisibility() == View.VISIBLE) {
    				continue;
    			}
    			fields.add(sectionView);
    			
    		}
    	}
    	return fields;
    }
    
    private void updatePhoneticNameVisibility() {
        boolean showByDefault =
                getContext().getResources().getBoolean(R.bool.config_editor_include_phonetic_name);

        if (showByDefault || mPhoneticName.hasData() || mPhoneticNameAdded) {
            mPhoneticName.setVisibility(View.VISIBLE);
        } else {
            mPhoneticName.setVisibility(View.GONE);
        }
    }
    /*prize-add-huangliemin-2016-6-6-end*/
    
    /*prize-add-huangpengfei-2016-10-19-start*/
    
    /**
     * Check the contact editor layout and find the KindSectionView about phone number item.
     * phone number item will be added when the contact has one phone number. 
     * 
     */
    private String itemType_phone = "vnd.android.cursor.item/phone_v2"; 

    public void addPhoneNumItem(RawContactDelta state){
    	Log.d(TAG,"[addPhoneNumItem]");
    	String saveLocation = state.getAccountName();
    	boolean isSaveForSIMCard = false;  
        if(saveLocation != null){
	        String reg=".*SIM.*"; 
	        if(saveLocation.matches(reg)){
	        	isSaveForSIMCard = true;
	        }else{
	        	isSaveForSIMCard = false;
	        }
        }
    	KindSectionView sectionView = null;
    	String mimeType = null;
    	for(int i=0;i<mFields.getChildCount();i++){
    		View child = mFields.getChildAt(i);
    		if(child instanceof KindSectionView) {
    			sectionView = (KindSectionView) child;
    			if(sectionView == null){
    				continue;
    			}
    			mimeType = sectionView.getKind().mimeType;
    			//check item type whether the Phone number item
    			if(itemType_phone.equals(mimeType)){
    				int emptyEditCount = sectionView.getEmptyEditorCount();
    		    	Log.d(TAG,"[addPhoneNumItem]   emptyEditCount = "+emptyEditCount);
    		    	
    				//If this contact is saved SIM card and item count more than 1 .
    		    	//Can not to add item about phone number item.
    				if(emptyEditCount >= 1) break;
    				if(isSaveForSIMCard) break;
    				sectionView.addItem();
    				break;
        		}
    		}
    	}	
    }
    /*prize-add-huangpengfei-2016-10-19-end*/
    
    /*prize-add-hpf-2017-10-30-start*/
    public boolean prizeAreAllEditorFieldsEmpty(){
    	int childCount = mFields.getChildCount();
    	boolean isAllEmpty = false;
    	boolean isNameEmpty = false;
    	boolean isPhoneticNameEmpty = false;
    	boolean isCompanyEmpty = false;
    	for(int i = 0;i < childCount;i++){
    		View v = mFields.getChildAt(i);
    		 if(v instanceof KindSectionView){
    			KindSectionView ksv = (KindSectionView)v;
                 isAllEmpty = ksv.areAllEditorsEmpty();
    			if(!isAllEmpty) break;
    		}

    		/*prize add by for bug 41594 by zhaojian 20171115 start*/
            if(v instanceof GroupMembershipView){
                TextView groupListTextView = (TextView) v.findViewById(R.id.group_list);
                if(groupListTextView != null && groupListTextView.getText() != null){
                    if(!mContext.getResources().getString(R.string.group_edit_field_hint_text)
                            .equals(groupListTextView.getText().toString())) {
                        return false;
                    }
                }
            }
            /*prize add by for bug 41594 by zhaojian 20171115 end*/
    	}
        isNameEmpty = mName.isEmpty();
        isPhoneticNameEmpty = mPhoneticName.isEmpty();
        isCompanyEmpty = mCompany.isEmpty();
    	
    	return isAllEmpty & isNameEmpty & isPhoneticNameEmpty & isCompanyEmpty & mPhoto.isEmpty();
    }

    /*prize-add-hpf-2017-10-30-end*/
    
}
