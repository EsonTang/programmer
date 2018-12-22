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
 * limitations under the License
 */

package com.android.contacts.editor;

import android.accounts.Account;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.RawContacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import com.android.contacts.ContactSaveService;
import com.android.contacts.GroupMetaDataLoader;
import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorAccountsChangedActivity;
import com.android.contacts.activities.ContactEditorBaseActivity;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.common.logging.ScreenEvent.ScreenType;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.Contact;
import com.android.contacts.common.model.ContactLoader;
import com.android.contacts.common.model.RawContact;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.RawContactModifier;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.ImplicitIntentsUtil;
import com.android.contacts.common.util.MaterialColorMapUtils;
import com.android.contacts.editor.AggregationSuggestionEngine.Suggestion;
import com.android.contacts.list.UiIntentActions;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.UiClosables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.editor.ContactEditorUtilsEx;
import com.mediatek.contacts.editor.SubscriberAccount;
import com.mediatek.contacts.eventhandler.BaseEventHandlerFragment;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simservice.SimEditProcessor;
import com.mediatek.contacts.simservice.SimProcessorService;
import com.mediatek.contacts.simservice.SimServiceUtils;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.util.ContactsSettingsUtils;
import com.mediatek.contacts.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import android.os.Handler;//prize-add by bxh

/**
 * Base Fragment for contact editors.
 */
abstract public class ContactEditorBaseFragment extends BaseEventHandlerFragment implements
        ContactEditor, SplitContactConfirmationDialogFragment.Listener,
        JoinContactConfirmationDialogFragment.Listener,
        AggregationSuggestionEngine.Listener, AggregationSuggestionView.Listener,
        CancelEditDialogFragment.Listener {

    static final String TAG = "ContactEditorBaseFragment";

    protected static final int LOADER_CONTACT = 1;
    protected static final int LOADER_GROUPS = 2;

    private static final List<String> VALID_INTENT_ACTIONS = new ArrayList<String>() {{
        add(Intent.ACTION_EDIT);
        add(Intent.ACTION_INSERT);
        add(ContactEditorBaseActivity.ACTION_EDIT);
        add(ContactEditorBaseActivity.ACTION_INSERT);
        add(ContactEditorBaseActivity.ACTION_SAVE_COMPLETED);
    }};

    private static final String KEY_ACTION = "action";
    private static final String KEY_URI = "uri";
    private static final String KEY_AUTO_ADD_TO_DEFAULT_GROUP = "autoAddToDefaultGroup";
    private static final String KEY_DISABLE_DELETE_MENU_OPTION = "disableDeleteMenuOption";
    private static final String KEY_NEW_LOCAL_PROFILE = "newLocalProfile";
    private static final String KEY_MATERIAL_PALETTE = "materialPalette";
    private static final String KEY_PHOTO_ID = "photoId";

    private static final String KEY_VIEW_ID_GENERATOR = "viewidgenerator";

    private static final String KEY_RAW_CONTACTS = "rawContacts";

    private static final String KEY_EDIT_STATE = "state";
    private static final String KEY_STATUS = "status";

    private static final String KEY_HAS_NEW_CONTACT = "hasNewContact";
    private static final String KEY_NEW_CONTACT_READY = "newContactDataReady";

    private static final String KEY_IS_EDIT = "isEdit";
    private static final String KEY_EXISTING_CONTACT_READY = "existingContactDataReady";

    private static final String KEY_RAW_CONTACT_DISPLAY_ALONE_IS_READ_ONLY = "isReadOnly";

    // Phone option menus
    private static final String KEY_SEND_TO_VOICE_MAIL_STATE = "sendToVoicemailState";
    private static final String KEY_ARE_PHONE_OPTIONS_CHANGEABLE = "arePhoneOptionsChangable";
    private static final String KEY_CUSTOM_RINGTONE = "customRingtone";

    private static final String KEY_IS_USER_PROFILE = "isUserProfile";
    private static final String KEY_NEED_QUIT_EDIT = "isQuitEdit";

    private static final String KEY_ENABLED = "enabled";

    // Aggregation PopupWindow
    private static final String KEY_AGGREGATION_SUGGESTIONS_RAW_CONTACT_ID =
            "aggregationSuggestionsRawContactId";

    // Join Activity
    private static final String KEY_CONTACT_ID_FOR_JOIN = "contactidforjoin";
    /// M: IccCard account no need show join menu
    private static final String KEY_NO_NEED_SHOW_JOIN = "noneedshowjoin";

    private static final String KEY_READ_ONLY_DISPLAY_NAME = "readOnlyDisplayName";

    protected static final int REQUEST_CODE_JOIN = 0;
    protected static final int REQUEST_CODE_ACCOUNTS_CHANGED = 1;
    protected static final int REQUEST_CODE_PICK_RINGTONE = 2;

    private static final int CURRENT_API_VERSION = android.os.Build.VERSION.SDK_INT;

    /// M: Sim related info.
    protected SubscriberAccount mSubsciberAccount = new SubscriberAccount();

    /**
     * An intent extra that forces the editor to add the edited contact
     * to the default group (e.g. "My Contacts").
     */
    public static final String INTENT_EXTRA_ADD_TO_DEFAULT_DIRECTORY = "addToDefaultDirectory";

    public static final String INTENT_EXTRA_NEW_LOCAL_PROFILE = "newLocalProfile";

    public static final String INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION =
            "disableDeleteMenuOption";

    /**
     * Intent key to pass the photo palette primary color calculated by
     * {@link com.android.contacts.quickcontact.QuickContactActivity} to the editor and between
     * the compact and fully expanded editors.
     */
    public static final String INTENT_EXTRA_MATERIAL_PALETTE_PRIMARY_COLOR =
            "material_palette_primary_color";

    /**
     * Intent key to pass the photo palette secondary color calculated by
     * {@link com.android.contacts.quickcontact.QuickContactActivity} to the editor and between
     * the compact and fully expanded editors.
     */
    public static final String INTENT_EXTRA_MATERIAL_PALETTE_SECONDARY_COLOR =
            "material_palette_secondary_color";

    /**
     * Intent key to pass the ID of the photo to display on the editor.
     */
    public static final String INTENT_EXTRA_PHOTO_ID = "photo_id";

    /**
     * Intent key to pass the ID of the raw contact id that should be displayed in the full editor
     * by itself.
     */
    public static final String INTENT_EXTRA_RAW_CONTACT_ID_TO_DISPLAY_ALONE =
            "raw_contact_id_to_display_alone";

    /**
     * Intent key to pass the boolean value of if the raw contact id that should be displayed
     * in the full editor by itself is read-only.
     */
    public static final String INTENT_EXTRA_RAW_CONTACT_DISPLAY_ALONE_IS_READ_ONLY =
            "raw_contact_display_alone_is_read_only";

    /**
     * Intent extra to specify a {@link ContactEditor.SaveMode}.
     */
    public static final String SAVE_MODE_EXTRA_KEY = "saveMode";

    /**
     * Intent extra key for the contact ID to join the current contact to after saving.
     */
    public static final String JOIN_CONTACT_ID_EXTRA_KEY = "joinContactId";

    /**
     * Callbacks for Activities that host contact editors Fragments.
     */
    public interface Listener {

        /**
         * Contact was not found, so somehow close this fragment. This is raised after a contact
         * is removed via Menu/Delete
         */
        void onContactNotFound();

        /**
         * Contact was split, so we can close now.
         *
         * @param newLookupUri The lookup uri of the new contact that should be shown to the user.
         *                     The editor tries best to chose the most natural contact here.
         */
        void onContactSplit(Uri newLookupUri);

        /**
         * User has tapped Revert, close the fragment now.
         */
        void onReverted();

        /**
         * Contact was saved and the Fragment can now be closed safely.
         */
        void onSaveFinished(Intent resultIntent);

        /**
         * User switched to editing a different contact (a suggestion from the
         * aggregation engine).
         */
        void onEditOtherContactRequested(Uri contactLookupUri,
                ArrayList<ContentValues> contentValues);

        /**
         * Contact is being created for an external account that provides its own
         * new contact activity.
         */
        void onCustomCreateContactActivityRequested(AccountWithDataSet account,
                Bundle intentExtras);

        /**
         * The edited raw contact belongs to an external account that provides
         * its own edit activity.
         *
         * @param redirect indicates that the current editor should be closed
         *                 before the custom editor is shown.
         */
        void onCustomEditContactActivityRequested(AccountWithDataSet account, Uri rawContactUri,
                Bundle intentExtras, boolean redirect);

        /**
         * User has requested that contact be deleted.
         */
        void onDeleteRequested(Uri contactUri);
    }

    /**
     * Adapter for aggregation suggestions displayed in a PopupWindow when
     * editor fields change.
     */
    protected static final class AggregationSuggestionAdapter extends BaseAdapter {
        private final LayoutInflater mLayoutInflater;
        private final boolean mSetNewContact;
        private final AggregationSuggestionView.Listener mListener;
        private final List<AggregationSuggestionEngine.Suggestion> mSuggestions;

        public AggregationSuggestionAdapter(Activity activity, boolean setNewContact,
                AggregationSuggestionView.Listener listener, List<Suggestion> suggestions) {
            mLayoutInflater = activity.getLayoutInflater();
            mSetNewContact = setNewContact;
            mListener = listener;
            mSuggestions = suggestions;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Suggestion suggestion = (Suggestion) getItem(position);
            final AggregationSuggestionView suggestionView =
                    (AggregationSuggestionView) mLayoutInflater.inflate(
                            R.layout.aggregation_suggestions_item, null);
            suggestionView.setNewContact(mSetNewContact);
            suggestionView.setListener(mListener);
            suggestionView.bindSuggestion(suggestion);
            return suggestionView;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return mSuggestions.get(position);
        }

        @Override
        public int getCount() {
            return mSuggestions.size();
        }
    }

    protected Context mContext;
    protected Listener mListener;

    //
    // Views
    //
    protected LinearLayout mContent;
    protected View mAggregationSuggestionView;
    protected ListPopupWindow mAggregationSuggestionPopup;

    //
    // Parameters passed in on {@link #load}
    //
    protected String mAction;
    protected Uri mLookupUri;
    protected Bundle mIntentExtras;
    protected boolean mAutoAddToDefaultGroup;
    protected boolean mDisableDeleteMenuOption;
    protected boolean mNewLocalProfile;
    protected MaterialColorMapUtils.MaterialPalette mMaterialPalette;
    protected long mPhotoId = -1;

    //
    // Helpers
    //
    protected ContactEditorUtils mEditorUtils;
    protected RawContactDeltaComparator mComparator;
    protected ViewIdGenerator mViewIdGenerator;
    private AggregationSuggestionEngine mAggregationSuggestionEngine;

    //
    // Loaded data
    //
    // Used to store existing contact data so it can be re-applied during a rebind call,
    // i.e. account switch.  Only used in {@link ContactEditorFragment}.
    protected ImmutableList<RawContact> mRawContacts;
    protected Cursor mGroupMetaData;

    //
    // Editor state
    //
    protected RawContactDeltaList mState;
    protected int mStatus;
    protected long mRawContactIdToDisplayAlone = -1;
    protected boolean mRawContactDisplayAloneIsReadOnly = false;

    // Whether to show the new contact blank form and if it's corresponding delta is ready.
    protected boolean mHasNewContact;
    protected AccountWithDataSet mAccountWithDataSet;
    protected boolean mNewContactDataReady;
    protected boolean mNewContactAccountChanged;

    // Whether it's an edit of existing contact and if it's corresponding delta is ready.
    protected boolean mIsEdit;
    protected boolean mExistingContactDataReady;

    // Whether we are editing the "me" profile
    protected boolean mIsUserProfile;

    // Phone specific option menu items
    private boolean mSendToVoicemailState;
    private boolean mArePhoneOptionsChangable;
    private String mCustomRingtone;

    // Whether editor views and options menu items should be enabled
    private boolean mEnabled = true;

    // Aggregation PopupWindow
    private long mAggregationSuggestionsRawContactId;

    // Join Activity
    protected long mContactIdForJoin;

    // Used to pre-populate the editor with a display name when a user edits a read-only contact.
    protected String mReadOnlyDisplayName;

    //
    // Not saved/restored on rotates
    //

    // The name editor view for the new raw contact that was created so that the user can
    // edit a read-only contact (to which the new raw contact was joined)
    protected StructuredNameEditorView mReadOnlyNameEditorView;

    /**
     * The contact data loader listener.
     */
    protected final LoaderManager.LoaderCallbacks<Contact> mContactLoaderListener =
            new LoaderManager.LoaderCallbacks<Contact>() {

                protected long mLoaderStartTime;

                @Override
                public Loader<Contact> onCreateLoader(int id, Bundle args) {
                    Log.d(TAG, "[onCreateLoader]mLookupUri = " + mLookupUri + ",id = " + id);
                    mLoaderStartTime = SystemClock.elapsedRealtime();
                    return new ContactLoader(mContext, mLookupUri, true);
                }

                @Override
                public void onLoadFinished(Loader<Contact> loader, Contact contact) {
                    final long loaderCurrentTime = SystemClock.elapsedRealtime();
                    Log.v(TAG, "Time needed for loading: " + (loaderCurrentTime-mLoaderStartTime));
                    if (!contact.isLoaded()) {
                        // Item has been deleted. Close activity without saving again.
                        Log.i(TAG, "[onLoadFinished]No contact found. Closing activity");
                        mStatus = Status.CLOSING;
                        if (mListener != null) mListener.onContactNotFound();
                        return;
                    }

                    /** M: New Feature @{ */
                    if (mSubsciberAccount.getNeedFinish()) {
                        Log.d(TAG, "[onLoadFinished] mNeedFinish is true,Cancle execute.");
                        mSubsciberAccount.setNeedFinish(false);
                        return;
                    }
                    /** @} */
                    Log.i(TAG, "[onLoadFinished]change state is  Status.EDITING");
                    mStatus = Status.EDITING;
                    mLookupUri = contact.getLookupUri();

                    /** M: ALPS02763652. should set right simIndex for editing state. @{ */
                    mSubsciberAccount.setIndicatePhoneOrSimContact(contact.getIndicate());
                    mSubsciberAccount.setSimIndex(contact.getSimIndex());
                    /** @} */

                    final long setDataStartTime = SystemClock.elapsedRealtime();
                    setState(contact);
                    setStateForPhoneMenuItems(contact);
                    final long setDataEndTime = SystemClock.elapsedRealtime();

                    Log.v(TAG, "Time needed for setting UI: " + (setDataEndTime - setDataStartTime));
                }

                @Override
                public void onLoaderReset(Loader<Contact> loader) {
                }
            };

    /**
     * The groups meta data loader listener.
     */
    protected final LoaderManager.LoaderCallbacks<Cursor> mGroupsLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public CursorLoader onCreateLoader(int id, Bundle args) {
                    return new GroupMetaDataLoader(mContext, ContactsContract.Groups.CONTENT_URI);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    mGroupMetaData = data;
                    setGroupMetaData();
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                }
            };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.i(TAG, "[onAttach]");
        mContext = activity;
        mEditorUtils = ContactEditorUtils.getInstance(mContext);
        mComparator = new RawContactDeltaComparator(mContext);

        //M:OP01 RCS will set currnet editor fragment and it's manager.@{
        ExtensionManager.getInstance().getRcsExtension().setEditorFragment(this,
                getFragmentManager());
        /** @} */
    }

    @Override
    public void onCreate(Bundle savedState) {
        Log.i(TAG, "[onCreate]");
        if (savedState != null) {
            // Restore mUri before calling super.onCreate so that onInitializeLoaders
            // would already have a uri and an action to work with
            mAction = savedState.getString(KEY_ACTION);
            mLookupUri = savedState.getParcelable(KEY_URI);
        }

        super.onCreate(savedState);

        if (savedState == null) {
            mViewIdGenerator = new ViewIdGenerator();
        } else {
            mViewIdGenerator = savedState.getParcelable(KEY_VIEW_ID_GENERATOR);

            mAutoAddToDefaultGroup = savedState.getBoolean(KEY_AUTO_ADD_TO_DEFAULT_GROUP);
            mDisableDeleteMenuOption = savedState.getBoolean(KEY_DISABLE_DELETE_MENU_OPTION);
            mNewLocalProfile = savedState.getBoolean(KEY_NEW_LOCAL_PROFILE);
            mMaterialPalette = savedState.getParcelable(KEY_MATERIAL_PALETTE);
            mPhotoId = savedState.getLong(KEY_PHOTO_ID);

            mRawContacts = ImmutableList.copyOf(savedState.<RawContact>getParcelableArrayList(
                    KEY_RAW_CONTACTS));
            // NOTE: mGroupMetaData is not saved/restored

            // Read state from savedState. No loading involved here
            mState = savedState.<RawContactDeltaList> getParcelable(KEY_EDIT_STATE);
            mStatus = savedState.getInt(KEY_STATUS);
            mRawContactDisplayAloneIsReadOnly = savedState.getBoolean(
                    KEY_RAW_CONTACT_DISPLAY_ALONE_IS_READ_ONLY);

            mHasNewContact = savedState.getBoolean(KEY_HAS_NEW_CONTACT);
            mNewContactDataReady = savedState.getBoolean(KEY_NEW_CONTACT_READY);

            mIsEdit = savedState.getBoolean(KEY_IS_EDIT);
            mExistingContactDataReady = savedState.getBoolean(KEY_EXISTING_CONTACT_READY);

            mIsUserProfile = savedState.getBoolean(KEY_IS_USER_PROFILE);

            // Phone specific options menus
            mSendToVoicemailState = savedState.getBoolean(KEY_SEND_TO_VOICE_MAIL_STATE);
            mArePhoneOptionsChangable = savedState.getBoolean(KEY_ARE_PHONE_OPTIONS_CHANGEABLE);
            mCustomRingtone = savedState.getString(KEY_CUSTOM_RINGTONE);

            mEnabled = savedState.getBoolean(KEY_ENABLED);

            // Aggregation PopupWindow
            mAggregationSuggestionsRawContactId = savedState.getLong(
                    KEY_AGGREGATION_SUGGESTIONS_RAW_CONTACT_ID);

            // Join Activity
            mContactIdForJoin = savedState.getLong(KEY_CONTACT_ID_FOR_JOIN);

            mReadOnlyDisplayName = savedState.getString(KEY_READ_ONLY_DISPLAY_NAME);

            /// M: Bug Fix Descriptions: add simid and slotid
            mSubsciberAccount.restoreSimAndSubId(savedState);
        }

        // mState can still be null because it may not have have finished loading before
        // onSaveInstanceState was called.
        if (mState == null) {
            Log.d(TAG, "onCreate mState == null");
            mState = new RawContactDeltaList();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "[onActivityCreated]mAction = " + mAction);
        validateAction(mAction);

        if (mState.isEmpty()) {
            // The delta list may not have finished loading before orientation change happens.
            // In this case, there will be a saved state but deltas will be missing.  Reload from
            // database.
            if (Intent.ACTION_EDIT.equals(mAction) ||
                    ContactEditorBaseActivity.ACTION_EDIT.equals(mAction)) {
                // Either
                // 1) orientation change but load never finished.
                // 2) not an orientation change so data needs to be loaded for first time.
                Log.d(TAG, "[onActivityCreated]initLoader data.");
                getLoaderManager().initLoader(LOADER_CONTACT, null, mContactLoaderListener);
                getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupsLoaderListener);
            }
        } else {
            // Orientation change, we already have mState, it was loaded by onCreate
            bindEditors();
        }

        // Handle initial actions only when existing state missing
        if (savedInstanceState == null) {
            final Account account = mIntentExtras == null ? null :
                    (Account) mIntentExtras.getParcelable(Intents.Insert.EXTRA_ACCOUNT);
            final String dataSet = mIntentExtras == null ? null :
                    mIntentExtras.getString(Intents.Insert.EXTRA_DATA_SET);
            if (account != null) {
                mAccountWithDataSet = new AccountWithDataSet(account.name, account.type, dataSet);
            }

            if (Intent.ACTION_EDIT.equals(mAction) ||
                    ContactEditorBaseActivity.ACTION_EDIT.equals(mAction)) {
                mIsEdit = true;
            } else if (Intent.ACTION_INSERT.equals(mAction) ||
                    ContactEditorBaseActivity.ACTION_INSERT.equals(mAction)) {
                mHasNewContact = true;
                if (mAccountWithDataSet != null) {
                    createContact(mAccountWithDataSet);
                } else {
                    // No Account specified. Let the user choose
                    // Load Accounts async so that we can present them
                    selectAccountAndCreateContact();
                }
            }
        }
    }

    /**
     * Checks if the requested action is valid.
     *
     * @param action The action to test.
     * @throws IllegalArgumentException when the action is invalid.
     */
    private static void validateAction(String action) {
        if (VALID_INTENT_ACTIONS.contains(action)) {
            return;
        }
        throw new IllegalArgumentException(
                "Unknown action " + action + "; Supported actions: " + VALID_INTENT_ACTIONS);
    }

    @Override
    public void onStart() {
        super.onStart();
        /// M: add for AAS, it can't refresh aas label when aas label changed
        // if you call it in onActivityCreated.
        ContactEditorUtilsEx.updateAasView(mContext, mState, mContent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "[onSaveInstanceState]");
        outState.putString(KEY_ACTION, mAction);
        outState.putParcelable(KEY_URI, mLookupUri);
        outState.putBoolean(KEY_AUTO_ADD_TO_DEFAULT_GROUP, mAutoAddToDefaultGroup);
        outState.putBoolean(KEY_DISABLE_DELETE_MENU_OPTION, mDisableDeleteMenuOption);
        outState.putBoolean(KEY_NEW_LOCAL_PROFILE, mNewLocalProfile);
        if (mMaterialPalette != null) {
            outState.putParcelable(KEY_MATERIAL_PALETTE, mMaterialPalette);
        }
        outState.putLong(KEY_PHOTO_ID, mPhotoId);

        outState.putParcelable(KEY_VIEW_ID_GENERATOR, mViewIdGenerator);

        outState.putParcelableArrayList(KEY_RAW_CONTACTS, mRawContacts == null ?
                Lists.<RawContact>newArrayList() : Lists.newArrayList(mRawContacts));
        // NOTE: mGroupMetaData is not saved

        if (hasValidState()) {
            Log.d(TAG, "hasValidState, save mState");
            // Store entities with modifications
            outState.putParcelable(KEY_EDIT_STATE, mState);
        }

        outState.putInt(KEY_STATUS, mStatus);
        outState.putBoolean(KEY_HAS_NEW_CONTACT, mHasNewContact);
        outState.putBoolean(KEY_NEW_CONTACT_READY, mNewContactDataReady);
        outState.putBoolean(KEY_IS_EDIT, mIsEdit);
        outState.putBoolean(KEY_EXISTING_CONTACT_READY, mExistingContactDataReady);
        outState.putBoolean(KEY_RAW_CONTACT_DISPLAY_ALONE_IS_READ_ONLY,
                mRawContactDisplayAloneIsReadOnly);

        outState.putBoolean(KEY_IS_USER_PROFILE, mIsUserProfile);

        // Phone specific options
        outState.putBoolean(KEY_SEND_TO_VOICE_MAIL_STATE, mSendToVoicemailState);
        outState.putBoolean(KEY_ARE_PHONE_OPTIONS_CHANGEABLE, mArePhoneOptionsChangable);
        outState.putString(KEY_CUSTOM_RINGTONE, mCustomRingtone);

        outState.putBoolean(KEY_ENABLED, mEnabled);

        // Aggregation PopupWindow
        outState.putLong(KEY_AGGREGATION_SUGGESTIONS_RAW_CONTACT_ID,
                mAggregationSuggestionsRawContactId);

        // Join Activity
        outState.putLong(KEY_CONTACT_ID_FOR_JOIN, mContactIdForJoin);

        outState.putString(KEY_READ_ONLY_DISPLAY_NAME, mReadOnlyDisplayName);

        /// M: add simid , slotid , savemode for sim
        mSubsciberAccount.onSaveInstanceStateSim(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "[onStop]");
        UiClosables.closeQuietly(mAggregationSuggestionPopup);

        //M:OP01 RCS will close listener of phone number text change.@{
        ExtensionManager.getInstance().getRcsExtension().closeTextChangedListener(false);
        /** @} */
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[onDestroy]");
        if (mAggregationSuggestionEngine != null) {
            mAggregationSuggestionEngine.quit();
        }

        //M:OP01 RCS will close listener of phone number text change.@{
        ExtensionManager.getInstance().getRcsExtension().closeTextChangedListener(true);
        /** @} */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "[onActivityResult]requestCode = " + requestCode
                + ",resultCode = " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_JOIN: {
                // Ignore failed requests
                if (resultCode != Activity.RESULT_OK) return;
                if (data != null) {
                    final long contactId = ContentUris.parseId(data.getData());
                    if (hasPendingChanges()) {
                        // Ask the user if they want to save changes before doing the join
                        JoinContactConfirmationDialogFragment.show(this, contactId);
                    } else {
                        // Do the join immediately
                        joinAggregate(contactId);
                    }
                }
                break;
            }
            case REQUEST_CODE_ACCOUNTS_CHANGED: {
                // Bail if the account selector was not successful.
                if (resultCode != Activity.RESULT_OK) {
                    if (mListener != null) {
                        mListener.onReverted();
                    }
                    return;
                }
                // If there's an account specified, use it.
                if (data != null) {
                    /// M: For create sim/usim contact
                    mSubsciberAccount.setAccountChangedSim(data, mContext);

                    AccountWithDataSet account = data.getParcelableExtra(
                            Intents.Insert.EXTRA_ACCOUNT);
                    if (account != null) {
                        createContact(account);
                        return;
                    }
                }
                // If there isn't an account specified, then this is likely a phone-local
                // contact, so we should continue setting up the editor by automatically selecting
                // the most appropriate account.
                createContact();
                break;
            }
            case REQUEST_CODE_PICK_RINGTONE: {
                if (data != null) {
                    final Uri pickedUri = data.getParcelableExtra(
                            RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    onRingtonePicked(pickedUri);
                }
                break;
            }
        }
    }

    private void onRingtonePicked(Uri pickedUri) {
        mCustomRingtone = EditorUiUtils.getRingtoneStringFromUri(pickedUri, CURRENT_API_VERSION);
        Intent intent = ContactSaveService.createSetRingtone(
                mContext, mLookupUri, mCustomRingtone);
        Log.d(TAG, "[onRingtonePicked]start ContactSaveService,intent = " + intent);
        mContext.startService(intent);
    }

    //
    // Options menu
    //

    private void setStateForPhoneMenuItems(Contact contact) {
        if (contact != null) {
            mSendToVoicemailState = contact.isSendToVoicemail();
            mCustomRingtone = contact.getCustomRingtone();
            mArePhoneOptionsChangable = !contact.isDirectoryEntry()
                    && PhoneCapabilityTester.isPhone(mContext);
        }
    }

    /**
     * Invalidates the options menu if we are still associated with an Activity.
     */
    protected void invalidateOptionsMenu() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, final MenuInflater inflater) {
    	//prize-add-huangliemin-2016-7-19
    	if(true) {
    		return;
    	}
        inflater.inflate(R.menu.edit_contact, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	//prize-change-huangliemin-2016-7-19
    	if(true) {
    		return;
    	}
        Log.d(TAG, "[onPrepareOptionsMenu]");
        // This supports the keyboard shortcut to save changes to a contact but shouldn't be visible
        // because the custom action bar contains the "save" button now (not the overflow menu).
        // TODO: Find a better way to handle shortcuts, i.e. onKeyDown()?
        final MenuItem saveMenu = menu.findItem(R.id.menu_save);
        final MenuItem splitMenu = menu.findItem(R.id.menu_split);
        final MenuItem joinMenu = menu.findItem(R.id.menu_join);
        final MenuItem helpMenu = menu.findItem(R.id.menu_help);
        final MenuItem sendToVoiceMailMenu = menu.findItem(R.id.menu_send_to_voicemail);
        final MenuItem ringToneMenu = menu.findItem(R.id.menu_set_ringtone);
        final MenuItem deleteMenu = menu.findItem(R.id.menu_delete);

        // Set visibility of menus

        // help menu depending on whether this is inserting or editing
		/* prize zhangzhonghao add start */
        /*if (isInsert(mAction)) {
            HelpUtils.prepareHelpMenuItem(mContext, helpMenu, R.string.help_url_people_add);
            discardMenu.setVisible(false);
            splitMenu.setVisible(false);
            joinMenu.setVisible(false);
            deleteMenu.setVisible(false);
        } else if (isEdit(mAction)) {
            HelpUtils.prepareHelpMenuItem(mContext, helpMenu, R.string.help_url_people_edit);
            // Split only if there is more than one raw contact, it is not a user-profile, and
            // splitting won't result in an empty contact. For the empty contact case, we only guard
            // against this when there is a single read-only contact in the aggregate.  If the user
            // has joined >1 read-only contacts together, we allow them to split it,
            // even if they have never added their own information and splitting will create a
            // name only contact.
            final boolean isSingleReadOnlyContact = mHasNewContact && mState.size() == 2;
            splitMenu.setVisible(mState.size() > 1 && !isEditingUserProfile()
                    && !isSingleReadOnlyContact);
            // Cannot join a user profile
            /// M: if it is IccCardAccount, User can't do join method
            joinMenu.setVisible(!isEditingUserProfile() && mSubsciberAccount.getSubId() < 0);

            deleteMenu.setVisible(!mDisableDeleteMenuOption);
        } else {
            // something else, so don't show the help menu
            helpMenu.setVisible(false);
        }*/
		/* prize zhangzhonghao add end */
        // Save menu is invisible when there's only one read only contact in the editor.
        saveMenu.setVisible(!mRawContactDisplayAloneIsReadOnly);

        if (mRawContactIdToDisplayAlone != -1 || mIsUserProfile) {
            sendToVoiceMailMenu.setVisible(false);
            ringToneMenu.setVisible(false);
        } else {
            // Hide telephony-related settings (ringtone, send to voicemail)
            // if we don't have a telephone or are editing a new contact.
            sendToVoiceMailMenu.setChecked(mSendToVoicemailState);
            /// M: disable sendToVoiceMailMenu&ringToneMenu for SIM account @{
            sendToVoiceMailMenu.setVisible(mArePhoneOptionsChangable &&
                    mSubsciberAccount.getSubId() < 0);
            ringToneMenu.setVisible(mArePhoneOptionsChangable &&
                    mSubsciberAccount.getSubId() < 0);
            /// @}
        }

        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setEnabled(mEnabled);
        }

        //M:OP01 RCS will add editor menu item @{
        ExtensionManager.getInstance().getRcsExtension().
                addEditorMenuOptions(this, menu, Intent.ACTION_INSERT.equals(mAction));
        /** @} */
        
        /* prize zhangzhonghao delete all menu start */
        //discardMenu.setVisible(false);
        splitMenu.setVisible(false);
        joinMenu.setVisible(false);
        deleteMenu.setVisible(false);
        ringToneMenu.setVisible(false);
        helpMenu.setVisible(false);
        sendToVoiceMailMenu.setVisible(false);
        /* prize zhangzhonghao delete all menu end */
        
    }
    
    /*prize-add-huangliemin-2016-7-19-start*/
    public void onSavePressed() {
    	Log.d(TAG, "[onSavePressed]");
    	save(SaveMode.CLOSE);
    }
    /*prize-add-huangliemin-2016-7-19-end*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            // If we no longer are attached to a running activity want to
            // drain this event.
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_save:
                Log.d(TAG, "[onOptionsItemSelected]save");
                return save(SaveMode.CLOSE);
            case R.id.menu_delete:
                if (mListener != null) mListener.onDeleteRequested(mLookupUri);
                return true;
            case R.id.menu_split:
                return doSplitContactAction();
            case R.id.menu_join:
                return doJoinContactAction();
            case R.id.menu_set_ringtone:
                doPickRingtone();
                return true;
            case R.id.menu_send_to_voicemail:
                Log.d(TAG, "[onOptionsItemSelected]send to voicemail.");
                // Update state and save
                mSendToVoicemailState = !mSendToVoicemailState;
                item.setChecked(mSendToVoicemailState);
                final Intent intent = ContactSaveService.createSetSendToVoicemail(
                        mContext, mLookupUri, mSendToVoicemailState);
                mContext.startService(intent);
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean revert() {
        Log.d(TAG, "[revert]    isEmpty = " + mState.isEmpty() + "!hasPendingChanges = " + !hasPendingChanges());
        if (mState.isEmpty() || !hasPendingChanges()) {
            onCancelEditConfirmed();
        } else {
            CancelEditDialogFragment.show(this);
        }
        return true;
    }

    @Override
    public void onCancelEditConfirmed() {
        // When this Fragment is closed we don't want it to auto-save
        Log.d(TAG, "[onCancelEditConfirmed,change status as Status.CLOSING");
        mStatus = Status.CLOSING;
        if (mListener != null) {
            mListener.onReverted();
        }
    }

    @Override
    public void onSplitContactConfirmed(boolean hasPendingChanges) {
        if (mState.isEmpty()) {
            // This may happen when this Fragment is recreated by the system during users
            // confirming the split action (and thus this method is called just before onCreate()),
            // for example.
            Log.e(TAG, "mState became null during the user's confirming split action. " +
                    "Cannot perform the save action.");
            return;
        }

        if (!hasPendingChanges && mHasNewContact) {
            // If the user didn't add anything new, we don't want to split out the newly created
            // raw contact into a name-only contact so remove them.
            final Iterator<RawContactDelta> iterator = mState.iterator();
            while (iterator.hasNext()) {
                final RawContactDelta rawContactDelta = iterator.next();
                if (rawContactDelta.getRawContactId() < 0) {
                    iterator.remove();
                }
            }
        }
        Log.d(TAG, "[onSplitContactConfirmed],set SaveMode.SPLIT");
        mState.markRawContactsForSplitting();
        save(SaveMode.SPLIT);
    }

    private boolean doSplitContactAction() {
        Log.d(TAG, "[doSplitContactAction]");
        if (!hasValidState()) return false;

        SplitContactConfirmationDialogFragment.show(this, hasPendingChanges());
        return true;
    }

    private boolean doJoinContactAction() {
        Log.d(TAG, "[doJoinContactAction]");
        if (!hasValidState() || mLookupUri == null) {
            Log.w(TAG, "[doJoinContactAction]hasValidState is false, return.");
            return false;
        }

        // If we just started creating a new contact and haven't added any data, it's too
        // early to do a join
        if (mState.size() == 1 && mState.get(0).isContactInsert()
                && !hasPendingChanges()) {
            Toast.makeText(mContext, R.string.toast_join_with_empty_contact,
                    Toast.LENGTH_LONG).show();
            return true;
        }
        showJoinAggregateActivity(mLookupUri);
        return true;
    }

    @Override
    public void onJoinContactConfirmed(long joinContactId) {
        doSaveAction(SaveMode.JOIN, joinContactId);
        Log.d(TAG, "[doJoinContactAction],set SaveMode.JOIN");
    }

    private void doPickRingtone() {
        Log.d(TAG, "[doPickRingtone]");
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        // Allow user to pick 'Default'
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        // Show only ringtones
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        // Allow the user to pick a silent ringtone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);

        final Uri ringtoneUri = EditorUiUtils.getRingtoneUriFromString(mCustomRingtone,
                CURRENT_API_VERSION);

        // Put checkmark next to the current ringtone for this contact
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);

        // Launch!
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_RINGTONE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(mContext, R.string.missing_app, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean save(int saveMode) {
        Log.i(TAG, "[save]saveMode = " + saveMode);
        if (!hasValidState() || mStatus != Status.EDITING) {
            Log.w(TAG, "[save]return,mStatus = " + mStatus);
            return false;
        }

        /// M:Show Progress Dialog here.save contact for save action or Editor jump to
        // CompactEditor then will call onSaveComplete to dismiss Dialog@{
        if (ContactEditorBaseActivity.sIsClickedSaveText && (saveMode == SaveMode.CLOSE || saveMode == SaveMode.COMPACT)) { // prize add sIsClickedSaveText by zhaojian 20180316
            mSubsciberAccount.getProgressHandler().showDialog(getFragmentManager());
            Log.d(TAG, "[save]saveMode == CLOSE or COMPACT,show ProgressDialog");
        }
        /// @}

        // If we are about to close the editor - there is no need to refresh the data
        if (saveMode == SaveMode.CLOSE || saveMode == SaveMode.COMPACT
                || saveMode == SaveMode.SPLIT) {
            getLoaderManager().destroyLoader(LOADER_CONTACT);
        }

        Log.i(TAG, "[save]change status as Status.SAVING");
        mStatus = Status.SAVING;
        if (!hasPendingChanges()) {
            if (mLookupUri == null && saveMode == SaveMode.RELOAD) {
                // We don't have anything to save and there isn't even an existing contact yet.
                // Nothing to do, simply go back to editing mode
                mStatus = Status.EDITING;
                Log.i(TAG, "[save]change mStatus as EDITING");
                return true;
            }
            Log.i(TAG, "[save]onSaveCompleted");
            /// M: New Feature Descriptions: create sim/usim contact
            if (mSubsciberAccount.isAccountTypeIccCard(mState)) {
                Intent intent = new Intent(SimEditProcessor.EDIT_SIM_ACTION);
                intent.putExtra(SimEditProcessor.RESULT, SimEditProcessor.RESULT_OK);
                intent.putExtra(ContactEditorFragment.SAVE_MODE_EXTRA_KEY, saveMode);
                intent.setData(mLookupUri);
                onSaveSIMContactCompleted(false, intent);
                return true;
            }
            onSaveCompleted(/* hadChanges =*/ false, saveMode,
                    /* saveSucceeded =*/ mLookupUri != null, mLookupUri, /* joinContactId =*/ null);
            return true;
        }

        setEnabled(false);

        /// M: New Feature Descriptions: create sim/usim contact
        Log.i(TAG, "[save]doSaveAction");
        if (mSubsciberAccount.isAccountTypeIccCard(mState)) {
            return doSaveSIMContactAction(saveMode);
        }

        return doSaveAction(saveMode, /* joinContactId */ null);
    }

    /**
     * Persist the accumulated editor deltas.
     *
     * @param joinContactId the raw contact ID to join the contact being saved to after the save,
     *         may be null.
     */
    abstract protected boolean doSaveAction(int saveMode, Long joinContactId);

    protected boolean startSaveService(Context context, Intent intent, int saveMode) {
        final boolean result = ContactSaveService.startService(
                context, intent, saveMode);
        if (!result) {
            onCancelEditConfirmed();
        }
        return result;
    }

    //
    // State accessor methods
    //

    /**
     * Check if our internal {@link #mState} is valid, usually checked before
     * performing user actions.
     */
    protected boolean hasValidState() {
        return mState.size() > 0;
    }

    protected boolean isEditingUserProfile() {
        return mNewLocalProfile || mIsUserProfile;
    }

    /**
     * Whether the contact being edited spans multiple raw contacts.
     * The may also span multiple accounts.
     */
    public boolean isEditingMultipleRawContacts() {
        return mState.size() > 1;
    }

    /**
     * Whether the contact being edited is composed of a single read-only raw contact
     * aggregated with a newly created writable raw contact.
     */
    protected boolean isEditingReadOnlyRawContactWithNewContact() {
        return mHasNewContact && mState.size() == 2;
    }

    /**
     * Return true if there are any edits to the current contact which need to
     * be saved.
     */
    protected boolean hasPendingRawContactChanges(Set<String> excludedMimeTypes) {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        return RawContactModifier.hasChanges(mState, accountTypes, excludedMimeTypes);
    }

    /**
     * We allow unlinking only if there is more than one raw contact, it is not a user-profile,
     * and unlinking won't result in an empty contact.  For the empty contact case, we only guard
     * against this when there is a single read-only contact in the aggregate.  If the user
     * has joined >1 read-only contacts together, we allow them to unlink it, even if they have
     * never added their own information and unlinking will create a name only contact.
     */
    protected boolean canUnlinkRawContacts() {
        return isEditingMultipleRawContacts()
                && !isEditingUserProfile()
                && !isEditingReadOnlyRawContactWithNewContact();
    }

    /**
     * Determines if changes were made in the editor that need to be saved, while taking into
     * account that name changes are not real for read-only contacts.
     * See go/editing-read-only-contacts
     */
    protected boolean hasPendingChanges() {
        if (mReadOnlyNameEditorView != null && mReadOnlyDisplayName != null) {
            // We created a new raw contact delta with a default display name.
            // We must test for pending changes while ignoring the default display name.
            final String displayName = mReadOnlyNameEditorView.getDisplayName();
            if (mReadOnlyDisplayName.equals(displayName)) {
                final Set<String> excludedMimeTypes = new HashSet<>();
                excludedMimeTypes.add(StructuredName.CONTENT_ITEM_TYPE);
                return hasPendingRawContactChanges(excludedMimeTypes);
            }
            return true;
        }
        return hasPendingRawContactChanges(/* excludedMimeTypes =*/ null);
    }

    /**
     * Whether editor inputs and the options menu should be enabled.
     */
    protected boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Returns the palette extra that was passed in.
     */
    protected MaterialColorMapUtils.MaterialPalette getMaterialPalette() {
        return mMaterialPalette;
    }

    //
    // Account creation
    //

    private void selectAccountAndCreateContact() {
        Log.d(TAG, "[selectAccountAndCreateContact]");
        // If this is a local profile, then skip the logic about showing the accounts changed
        // activity and create a phone-local contact.
        /// M: need use isEditingUserProfile() instead of mNewLocalProfile
        if (isEditingUserProfile()) {
            Log.i(TAG, "[selectAccountAndCreateContact]isEditingUserProfile,return.");
            createContact(null);
            return;
        }

        // If there is no default account or the accounts have changed such that we need to
        // prompt the user again, then launch the account prompt.
        if (mEditorUtils.shouldShowAccountChangedNotification()) {
            Intent intent = new Intent(mContext, ContactEditorAccountsChangedActivity.class);
            // Prevent a second instance from being started on rotates
            /// M: Add account type for handling special case when add new contactor
            /*
             *  M: ALPS03621760, if the current editing contacts include a RDONLY and a new contact
             *  filter the IccCard accounts for IccCard does not aggregate with others.
             *  @{1/4
             */
            if(mHasNewContact && mRawContacts != null && mRawContacts.size() >= 1)
            {
                Log.d(TAG, "[selectAccountAndCreateContact]ACCOUNT_TYPE=NONE_SIM_TYPE_ACCOUNT");
                intent.putExtra(ContactsSettingsUtils.ACCOUNT_TYPE,
                        ContactsSettingsUtils.NONE_SIM_TYPE_ACCOUNT);
                ////test
                Log.d(TAG, "[selectAccountAndCreateContact] extra = " + intent.getExtras());
            } else {
            //1/4@}
                intent.putExtra(ContactsSettingsUtils.ACCOUNT_TYPE,
                        getActivity().getIntent().getIntExtra(
                        ContactsSettingsUtils.ACCOUNT_TYPE,
                        ContactsSettingsUtils.ALL_TYPE_ACCOUNT));
            // @{2/4
            }
            // 2/4@}
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.d(TAG, "[selectAccountAndCreateContact]change status as Status.SUB_ACTIVITY");
            mStatus = Status.SUB_ACTIVITY;
            startActivityForResult(intent, REQUEST_CODE_ACCOUNTS_CHANGED);
        } else {
            // Otherwise, there should be a default account. Then either create a local contact
            // (if default account is null) or create a contact with the specified account.
            AccountWithDataSet defaultAccount = mEditorUtils.getDefaultAccountEx();
            // @{3/4
            if (mHasNewContact && mRawContacts != null && mRawContacts.size() >= 1 &&
                    AccountTypeUtils.isAccountTypeIccCard(defaultAccount.type)) {
                /// start activity
                Intent intent = new Intent(mContext, ContactEditorAccountsChangedActivity.class);
                intent.putExtra(ContactsSettingsUtils.ACCOUNT_TYPE,
                        ContactsSettingsUtils.NONE_SIM_TYPE_ACCOUNT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Log.d(TAG, "[selectAccountAndCreateContact]default account is SIM");
                mStatus = Status.SUB_ACTIVITY;
                startActivityForResult(intent, REQUEST_CODE_ACCOUNTS_CHANGED);
            } else {
            // 3/4@}
                /*M: Change feature: AccountSwitcher. If the default account is sim account,
                 * and PHB is ready, set the corresponding sim info firstly.@{*/
                if (defaultAccount instanceof AccountWithDataSetEx) {
                   mSubsciberAccount.setSimInfo((AccountWithDataSetEx) defaultAccount);
                }
                /*@}*/
                createContact(defaultAccount);
            // @{4/4
            }
            // 4/4@}
        }
    }

    /**
     * Create a contact by automatically selecting the first account. If there's no available
     * account, a device-local contact should be created.
     */
    protected void createContact() {
        final List<AccountWithDataSet> accounts =
                AccountTypeManager.getInstance(mContext).getAccounts(true);
        // No Accounts available. Create a phone-local contact.
        if (accounts.isEmpty()) {
            createContact(null);
            return;
        }

        // We have an account switcher in "create-account" screen, so don't need to ask a user to
        // select an account here.
        createContact(accounts.get(0));
    }

    /**
     * Shows account creation screen associated with a given account.
     *
     * @param account may be null to signal a device-local contact should be created.
     */
    protected void createContact(AccountWithDataSet account) {
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        final AccountType accountType = accountTypes.getAccountTypeForAccount(account);

        if (accountType.getCreateContactActivityClassName() != null) {
            if (mListener != null) {
                mListener.onCustomCreateContactActivityRequested(account, mIntentExtras);
            }
        } else {
            setStateForNewContact(account, accountType, isEditingUserProfile());
        }
    }

    //
    // Data binding
    //

    private void setState(Contact contact) {
        // If we have already loaded data, we do not want to change it here to not confuse the user
        if (!mState.isEmpty()) {
            Log.w(TAG, "Ignoring background change. This will have to be rebased later");
            return;
        }

        // See if this edit operation needs to be redirected to a custom editor
        mRawContacts = contact.getRawContacts();

        /// M: op01 remove RCS account suite
        mRawContacts = ExtensionManager.getInstance()
                .getRcsExtension().rcsConfigureRawContacts(mRawContacts, contact.isUserProfile());

        if (mRawContacts.size() == 1) {
            RawContact rawContact = mRawContacts.get(0);
            String type = rawContact.getAccountTypeString();
            String dataSet = rawContact.getDataSet();
            AccountType accountType = rawContact.getAccountType(mContext);
            if (accountType.getEditContactActivityClassName() != null &&
                    !accountType.areContactsWritable()) {
                if (mListener != null) {
                    String name = rawContact.getAccountName();
                    long rawContactId = rawContact.getId();
                    mListener.onCustomEditContactActivityRequested(
                            new AccountWithDataSet(name, type, dataSet),
                            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                            mIntentExtras, true);
                }
                return;
            }
        }

        String readOnlyDisplayName = null;
        // Check for writable raw contacts.  If there are none, then we need to create one so user
        // can edit.  For the user profile case, there is already an editable contact.
        if (!contact.isUserProfile() && !contact.isWritableContact(mContext)) {
            mHasNewContact = true;

            // This is potentially an asynchronous call and will add deltas to list.
            selectAccountAndCreateContact();

            readOnlyDisplayName = contact.getDisplayName();
        } else {
            mHasNewContact = false;
        }

        //M: init the fields for IccCard related features
        mSubsciberAccount.initIccCard(contact);

        // This also adds deltas to list.  If readOnlyDisplayName is null at this point it is
        // simply ignored later on by the editor.
        setStateForExistingContact(readOnlyDisplayName, contact.isUserProfile(), mRawContacts);
    }

    /**
     * Prepare {@link #mState} for a newly created phone-local contact.
     */
    private void setStateForNewContact(AccountWithDataSet account, AccountType accountType,
            boolean isUserProfile) {
        setStateForNewContact(account, accountType, /* oldState =*/ null,
                /* oldAccountType =*/ null, isUserProfile);
    }

    /**
     * Prepare {@link #mState} for a newly created phone-local contact, migrating the state
     * specified by oldState and oldAccountType.
     */
    protected void setStateForNewContact(AccountWithDataSet account, AccountType accountType,
            RawContactDelta oldState, AccountType oldAccountType, boolean isUserProfile) {
        mStatus = Status.EDITING;
        mState.add(createNewRawContactDelta(account, accountType, oldState, oldAccountType));
        mIsUserProfile = isUserProfile;
        mNewContactDataReady = true;
        bindEditors();
    }

    /**
     * Returns a {@link RawContactDelta} for a new contact suitable for addition into
     * {@link #mState}.
     *
     * If oldState and oldAccountType are specified, the state specified by those parameters
     * is migrated to the result {@link RawContactDelta}.
     */
    private RawContactDelta createNewRawContactDelta(AccountWithDataSet account,
            AccountType accountType, RawContactDelta oldState, AccountType oldAccountType) {

        /// M: New Feature Descriptions: insert data to SIM/USIM.
        mSubsciberAccount.setSimSaveMode(accountType);

        final RawContact rawContact = new RawContact();
        if (account != null) {
            rawContact.setAccount(account);
        } else {
            rawContact.setAccountToLocal();
        }

        final RawContactDelta result = new RawContactDelta(
                ValuesDelta.fromAfter(rawContact.getValues()));
        if (oldState == null) {
            // Parse any values from incoming intent
            RawContactModifier.parseExtras(mContext, accountType, result, mIntentExtras);
            /// M: Bug Fix Descriptions: add toast when insert sip to sim/usim and finish activity
            ContactEditorUtilsEx.showSimSipTip(mContext);
        } else {
            RawContactModifier.migrateStateForNewContact(
                    mContext, oldState, result, oldAccountType, accountType);
        }

        /// M: set sim card data kind max count first
        ContactEditorUtilsEx.setSimDataKindCountMax(accountType, mSubsciberAccount.getSubId());

        // Ensure we have some default fields (if the account type does not support a field,
        // ensureKind will not add it, so it is safe to add e.g. Event)
        RawContactModifier.ensureKindExists(result, accountType, Phone.CONTENT_ITEM_TYPE);
        RawContactModifier.ensureKindExists(result, accountType, Email.CONTENT_ITEM_TYPE);
        /*prize-delete-huangliemin-2016-6-6 start*/
        /*
        RawContactModifier.ensureKindExists(result, accountType, Organization.CONTENT_ITEM_TYPE);
        RawContactModifier.ensureKindExists(result, accountType, Event.CONTENT_ITEM_TYPE);
        RawContactModifier.ensureKindExists(result, accountType,
                StructuredPostal.CONTENT_ITEM_TYPE);
        */
        /*prize-delete-huangliemin-2016-6-6 end*/

        // Set the correct URI for saving the contact as a profile
        /// M: need use isEditingUserProfile() instead of mNewLocalProfile
        if (isEditingUserProfile()) {
            result.setProfileQueryUri();
        }

        return result;
    }

    /**
     * Prepare {@link #mState} for an existing contact.
     */
    protected void setStateForExistingContact(String readOnlyDisplayName, boolean isUserProfile,
            ImmutableList<RawContact> rawContacts) {
        Log.d(TAG, "[setStateForExistingContact]readOnlyDisplayName = " + readOnlyDisplayName
                + ",isUserProfile = " + isUserProfile);
        setEnabled(true);

        //M: init MTK fields for IccCard related features
        mSubsciberAccount.insertRawDataToSim(rawContacts);

        mReadOnlyDisplayName = readOnlyDisplayName;

        mState.addAll(rawContacts.iterator());

        /** M: Set for existing contact is ICC account, and it need show join menu or not @{*/
        mSubsciberAccount.getIccAccountType(mState);
        /* @}*/

        setIntentExtras(mIntentExtras);
        mIntentExtras = null;

        // For user profile, change the contacts query URI
        mIsUserProfile = isUserProfile;
        boolean localProfileExists = false;

        if (mIsUserProfile) {
            for (RawContactDelta rawContactDelta : mState) {
                // For profile contacts, we need a different query URI
                rawContactDelta.setProfileQueryUri();
                // Try to find a local profile contact
                if (rawContactDelta.getValues().getAsString(RawContacts.ACCOUNT_TYPE) == null) {
                    localProfileExists = true;
                }
            }
            // Editor should always present a local profile for editing
            if (!localProfileExists) {
                mState.add(createLocalRawContactDelta());
            }
        }
        mExistingContactDataReady = true;
        bindEditors();
    }

    /**
     * Returns a {@link RawContactDelta} for a local contact suitable for addition into
     * {@link #mState}.
     */
    private static RawContactDelta createLocalRawContactDelta() {
        final RawContact rawContact = new RawContact();
        rawContact.setAccountToLocal();

        final RawContactDelta result = new RawContactDelta(
                ValuesDelta.fromAfter(rawContact.getValues()));
        result.setProfileQueryUri();

        return result;
    }

    /**
     * Sets group metadata on all bound editors.
     */
    abstract protected void setGroupMetaData();

    /**
     * Bind editors using {@link #mState} and other members initialized from the loaded (or new)
     * Contact.
     */
    abstract protected void bindEditors();

    /**
     * Set the enabled state of editors.
     */
    private void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;

            // Enable/disable editors
            if (mContent != null) {
                int count = mContent.getChildCount();
                for (int i = 0; i < count; i++) {
                    mContent.getChildAt(i).setEnabled(enabled);
                }
            }

            // Enable/disable aggregation suggestion vies
            if (mAggregationSuggestionView != null) {
                LinearLayout itemList = (LinearLayout) mAggregationSuggestionView.findViewById(
                        R.id.aggregation_suggestions);
                int count = itemList.getChildCount();
                for (int i = 0; i < count; i++) {
                    itemList.getChildAt(i).setEnabled(enabled);
                }
            }

            // Maybe invalidate the options menu
            final Activity activity = getActivity();
            if (activity != null) activity.invalidateOptionsMenu();
        }
    }

    /**
     * Removes a current editor ({@link #mState}) and rebinds new editor for a new account.
     * Some of old data are reused with new restriction enforced by the new account.
     *
     * @param oldState Old data being edited.
     * @param oldAccount Old account associated with oldState.
     * @param newAccount New account to be used.
     */
    protected void rebindEditorsForNewContact(
            RawContactDelta oldState, AccountWithDataSet oldAccount,
            AccountWithDataSet newAccount) {
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        AccountType oldAccountType = accountTypes.getAccountTypeForAccount(oldAccount);
        AccountType newAccountType = accountTypes.getAccountTypeForAccount(newAccount);

        /// M: Fix CR: ALPS02990454. Set sim card data kind max count first.
        ContactEditorUtilsEx.setSimDataKindCountMax(newAccountType, mSubsciberAccount.getSubId());
        /// @}

        if (newAccountType.getCreateContactActivityClassName() != null) {
            Log.w(TAG, "external activity called in rebind situation");
            if (mListener != null) {
                mListener.onCustomCreateContactActivityRequested(newAccount, mIntentExtras);
            }
        } else {
            mExistingContactDataReady = false;
            mNewContactDataReady = false;
            mState = new RawContactDeltaList();
            setStateForNewContact(newAccount, newAccountType, oldState, oldAccountType,
                    isEditingUserProfile());
            if (mIsEdit) {
                setStateForExistingContact(mReadOnlyDisplayName, isEditingUserProfile(),
                        mRawContacts);
            }
        }
    }

    //
    // ContactEditor
    //

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void load(String action, Uri lookupUri, Bundle intentExtras) {
        Log.d(TAG, "[load]action = " + action + ",lookupUri = " + lookupUri);
        mAction = action;
        mLookupUri = lookupUri;
        mIntentExtras = intentExtras;

        if (mIntentExtras != null) {
            mAutoAddToDefaultGroup =
                    mIntentExtras.containsKey(INTENT_EXTRA_ADD_TO_DEFAULT_DIRECTORY);
            mNewLocalProfile =
                    mIntentExtras.getBoolean(INTENT_EXTRA_NEW_LOCAL_PROFILE);
            mDisableDeleteMenuOption =
                    mIntentExtras.getBoolean(INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION);
            if (mIntentExtras.containsKey(INTENT_EXTRA_MATERIAL_PALETTE_PRIMARY_COLOR)
                    && mIntentExtras.containsKey(INTENT_EXTRA_MATERIAL_PALETTE_SECONDARY_COLOR)) {
                mMaterialPalette = new MaterialColorMapUtils.MaterialPalette(
                        mIntentExtras.getInt(INTENT_EXTRA_MATERIAL_PALETTE_PRIMARY_COLOR),
                        mIntentExtras.getInt(INTENT_EXTRA_MATERIAL_PALETTE_SECONDARY_COLOR));
            }
            // If the user selected a different photo, don't restore the one from the Intent
            if (mPhotoId < 0) {
                mPhotoId = mIntentExtras.getLong(INTENT_EXTRA_PHOTO_ID);
            }
            mRawContactIdToDisplayAlone = mIntentExtras.getLong(
                    INTENT_EXTRA_RAW_CONTACT_ID_TO_DISPLAY_ALONE, -1);
            mRawContactDisplayAloneIsReadOnly = mIntentExtras.getBoolean(
                    INTENT_EXTRA_RAW_CONTACT_DISPLAY_ALONE_IS_READ_ONLY);
        }
    }

    @Override
    public void setIntentExtras(Bundle extras) {
        if (extras == null || extras.size() == 0) {
            Log.w(TAG, "[setIntentExtras]extras error,value = " + extras);
            return;
        }

        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        for (RawContactDelta state : mState) {
            final AccountType type = state.getAccountType(accountTypes);
            if (type.areContactsWritable()) {
                // Apply extras to the first writable raw contact only
                RawContactModifier.parseExtras(mContext, type, state, extras);
                break;
            }
        }
    }

    @Override
    public void onJoinCompleted(Uri uri) {
        Log.d(TAG, "[onJoinCompleted],uri = " + uri);
        onSaveCompleted(false, SaveMode.RELOAD, uri != null, uri, /* joinContactId */ null);
    }

    @Override
    public void onSaveCompleted(boolean hadChanges, int saveMode, boolean saveSucceeded,
            Uri contactLookupUri, Long joinContactId) {
    	Log.d(TAG, "[onSaveCompleted]");
        ///M :dismiss Progress Dialog here.
        mSubsciberAccount.getProgressHandler().dismissDialog(getFragmentManager());

        if (hadChanges) {
            if (saveSucceeded) {
                switch (saveMode) {
                    case SaveMode.JOIN:
                        break;
                    case SaveMode.SPLIT:
                        Toast.makeText(mContext, R.string.contactUnlinkedToast, Toast.LENGTH_SHORT)
                                .show();
                        break;
                    default:
                        //prize add if-judge for bug 54159 by zhaojian 20180404 start
                        if(contactLookupUri != null){
                            Toast.makeText(mContext, R.string.contactSavedToast, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        //prize add if-judge for bug 54159 by zhaojian 20180404 end
                }

            } else {
                Toast.makeText(mContext, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
            }
        }
        switch (saveMode) {
            case SaveMode.CLOSE: {
                final Intent resultIntent;
                if (saveSucceeded && contactLookupUri != null && ContactEditorBaseActivity.sIsClickedSaveText) { // prize add sIsClickedSaveText for bug 52850 by zhaojian 20180316
                    // prize add for bug 52850 by zhaojian 20180316 start
                    ContactEditorBaseActivity.sIsClickedSaveText = false;
                    // prize add for bug 52850 by zhaojian 20180316 end
                    final Uri lookupUri = maybeConvertToLegacyLookupUri(
                            mContext, contactLookupUri, mLookupUri);
                    //M:OP01 RCS load rich call screen from server when new/edit contact. @{
                    ExtensionManager.getInstance().getRcsRichUiExtension()
                            .loadRichScrnByContactUri(lookupUri, getActivity());
                    /** @} */
                    resultIntent = ImplicitIntentsUtil.composeQuickContactIntent(lookupUri,
                            QuickContactActivity.MODE_FULLY_EXPANDED);
                    resultIntent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                            ScreenType.EDITOR);
                    /*prize-add-hpf-2018-1-9-start*/
                    if(((com.android.contacts.activities.ContactEditorActivity)mContext).mIsPersonalInformation){
                    	resultIntent.putExtra("isPersonalInformation", true);
                    }
                    /*prize-add-hpf-2018-1-9-end*/
                    /**  prize add by bxh  start */
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (mLookupUri != null) {
                                Intent intent = new Intent();
                                intent.setAction("prize_update_contact");
                                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, QuickContactActivity.getDisplayName());
                                intent.putExtra("ContactUri", mLookupUri.toString());
                                intent.putExtra("LookupContactUri", lookupUri.toString());
                                mContext.sendBroadcast(intent);
                            }
                        }
                    }, 1000);
                    /**  prize add by bxh  end */
                } else {
                    resultIntent = null;
                }
                // It is already saved, so prevent it from being saved again
                Log.d(TAG, "[onSaveCompleted]SaveMode.CLOSE,change status as Status.CLOSING");
                mStatus = Status.CLOSING;
                if (mListener != null) mListener.onSaveFinished(resultIntent);
              
                break;
            }
            case SaveMode.COMPACT: {
                // It is already saved, so prevent it from being saved again
                mStatus = Status.CLOSING;
                if (mListener != null) mListener.onSaveFinished(/* resultIntent= */ null);
               
                break;
            }
            case SaveMode.JOIN:
                if (saveSucceeded && contactLookupUri != null && joinContactId != null) {
                    joinAggregate(joinContactId);
                }
                break;
            case SaveMode.RELOAD:
                if (saveSucceeded && contactLookupUri != null) {
                    // If this was in INSERT, we are changing into an EDIT now.
                    // If it already was an EDIT, we are changing to the new Uri now
                    mState = new RawContactDeltaList();
                    load(Intent.ACTION_EDIT, contactLookupUri, null);
                    mStatus = Status.LOADING;
                    getLoaderManager().restartLoader(LOADER_CONTACT, null, mContactLoaderListener);
                }
                break;

            case SaveMode.SPLIT:
                Log.d(TAG, "[onSaveCompleted]SaveMode.SPLIT,change status as Status.CLOSING");
                mStatus = Status.CLOSING;
                if (mListener != null) {
                    mListener.onContactSplit(contactLookupUri);
                } else {
                    Log.d(TAG, "No listener registered, can not call onSplitFinished");
                }
                break;
            default:
                break;
        }
    }

    /**
     * Shows a list of aggregates that can be joined into the currently viewed aggregate.
     *
     * @param contactLookupUri the fresh URI for the currently edited contact (after saving it)
     */
    private void showJoinAggregateActivity(Uri contactLookupUri) {
        if (contactLookupUri == null || !isAdded()) {
            Log.w(TAG, "[showJoinAggregateActivity]error,contactLookupUri = " + contactLookupUri);
            return;
        }
        Log.d(TAG, "[showJoinAggregateActivity]");
        mContactIdForJoin = ContentUris.parseId(contactLookupUri);
        final Intent intent = new Intent(UiIntentActions.PICK_JOIN_CONTACT_ACTION);
        intent.putExtra(UiIntentActions.TARGET_CONTACT_ID_EXTRA_KEY, mContactIdForJoin);
        startActivityForResult(intent, REQUEST_CODE_JOIN);
    }

    //
    // Aggregation PopupWindow
    //

    /**
     * Triggers an asynchronous search for aggregation suggestions.
     */
    protected void acquireAggregationSuggestions(Context context,
            long rawContactId, ValuesDelta valuesDelta) {
        /**
         * M:Bug Fix for CR ALPS02334596
         * Description: To disable the aggregation function for SIM editor. @{
         */
        if (mSubsciberAccount.isIccAccountType(mState)) {
            return;
        }
        /** @} */

        if (mAggregationSuggestionsRawContactId != rawContactId
                && mAggregationSuggestionView != null) {
            mAggregationSuggestionView.setVisibility(View.GONE);
            mAggregationSuggestionView = null;
            mAggregationSuggestionEngine.reset();
        }

        mAggregationSuggestionsRawContactId = rawContactId;

        if (mAggregationSuggestionEngine == null) {
            mAggregationSuggestionEngine = new AggregationSuggestionEngine(context);
            mAggregationSuggestionEngine.setListener(this);
            mAggregationSuggestionEngine.start();
        }

        mAggregationSuggestionEngine.setContactId(getContactId());

        mAggregationSuggestionEngine.onNameChange(valuesDelta);
    }

    /**
     * Returns the contact ID for the currently edited contact or 0 if the contact is new.
     */
    private long getContactId() {
        for (RawContactDelta rawContact : mState) {
            Long contactId = rawContact.getValues().getAsLong(RawContacts.CONTACT_ID);
            if (contactId != null) {
                return contactId;
            }
        }
        return 0;
    }

    @Override
    public void onAggregationSuggestionChange() {
        final Activity activity = getActivity();
        Log.d(TAG, "[onAggregationSuggestionChange]mStatus = " + mStatus);
        if ((activity != null && activity.isFinishing())
                || !isVisible() ||  mState.isEmpty() || mStatus != Status.EDITING) {
            Log.w(TAG, "[onAggregationSuggestionChange]invalid status,return. ");
            return;
        }

        UiClosables.closeQuietly(mAggregationSuggestionPopup);

        if (mAggregationSuggestionEngine.getSuggestedContactCount() == 0) {
            Log.w(TAG, "[onAggregationSuggestionChange]count = 0,return. ");
            return;
        }

        final View anchorView = getAggregationAnchorView(mAggregationSuggestionsRawContactId);
        if (anchorView == null) {
            Log.w(TAG, "[onAggregationSuggestionChange]anchorView = null,return.");
            return; // Raw contact deleted?
        }
        mAggregationSuggestionPopup = new ListPopupWindow(mContext, null);
        mAggregationSuggestionPopup.setAnchorView(anchorView);
        mAggregationSuggestionPopup.setWidth(anchorView.getWidth());
        mAggregationSuggestionPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
        mAggregationSuggestionPopup.setAdapter(
                new AggregationSuggestionAdapter(
                        getActivity(),
                        mState.size() == 1 && mState.get(0).isContactInsert(),
                        /* listener =*/ this,
                        mAggregationSuggestionEngine.getSuggestions()));
        mAggregationSuggestionPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AggregationSuggestionView suggestionView = (AggregationSuggestionView) view;
                suggestionView.handleItemClickEvent();
                UiClosables.closeQuietly(mAggregationSuggestionPopup);
                mAggregationSuggestionPopup = null;
            }
        });
        mAggregationSuggestionPopup.show();
    }

    /**
     * Returns the raw contact editor view for the given rawContactId that should be used as the
     * anchor for aggregation suggestions.
     */
    abstract protected View getAggregationAnchorView(long rawContactId);

    /**
     * Whether the given raw contact ID matches the one used to last load aggregation
     * suggestions.
     */
    protected boolean isAggregationSuggestionRawContactId(long rawContactId) {
        return mAggregationSuggestionsRawContactId == rawContactId;
    }

    @Override
    public void onJoinAction(long contactId, List<Long> rawContactIdList) {
        final long rawContactIds[] = new long[rawContactIdList.size()];
        for (int i = 0; i < rawContactIds.length; i++) {
            rawContactIds[i] = rawContactIdList.get(i);
        }
        try {
            JoinSuggestedContactDialogFragment.show(this, rawContactIds);
        } catch (Exception ignored) {
            Log.e(TAG, "[onJoinAction]ignored = " + ignored);
            // No problem - the activity is no longer available to display the dialog
        }
    }

    /**
     * Joins the suggested contact (specified by the id's of constituent raw
     * contacts), save all changes, and stay in the editor.
     */
    protected void doJoinSuggestedContact(long[] rawContactIds) {
        Log.d(TAG, "[doJoinSuggestedContact]mStatus = " + mStatus);
        if (!hasValidState() || mStatus != Status.EDITING) {
            return;
        }

        mState.setJoinWithRawContacts(rawContactIds);
        save(SaveMode.RELOAD);
    }

    @Override
    public void onEditAction(Uri contactLookupUri) {
        SuggestionEditConfirmationDialogFragment.show(this, contactLookupUri);
    }

    /**
     * Abandons the currently edited contact and switches to editing the suggested
     * one, transferring all the data there
     */
    protected void doEditSuggestedContact(Uri contactUri) {
        if (mListener != null) {
            // make sure we don't save this contact when closing down
            Log.d(TAG, "[doEditSuggestedContact]change status is Status.CLOSING");
            mStatus = Status.CLOSING;
            mListener.onEditOtherContactRequested(
                    contactUri, mState.get(0).getContentValues());
        }
    }

    //
    // Join Activity
    //

    /**
     * Performs aggregation with the contact selected by the user from suggestions or A-Z list.
     */
    abstract protected void joinAggregate(long contactId);

    //
    // Utility methods
    //

    /**
     * Returns a legacy version of the given contactLookupUri if a legacy Uri was originally
     * passed to the contact editor.
     *
     * @param contactLookupUri The Uri to possibly convert to legacy format.
     * @param requestLookupUri The lookup Uri originally passed to the contact editor
     *                         (via Intent data), may be null.
     */
    protected static Uri maybeConvertToLegacyLookupUri(Context context, Uri contactLookupUri,
            Uri requestLookupUri) {
        final String legacyAuthority = "contacts";
        final String requestAuthority = requestLookupUri == null
                ? null : requestLookupUri.getAuthority();
        if (legacyAuthority.equals(requestAuthority)) {
            // Build a legacy Uri if that is what was requested by caller
            final long contactId = ContentUris.parseId(Contacts.lookupContact(
                    context.getContentResolver(), contactLookupUri));
            final Uri legacyContentUri = Uri.parse("content://contacts/people");
            return ContentUris.withAppendedId(legacyContentUri, contactId);
        }
        // Otherwise pass back a lookup-style Uri
        return contactLookupUri;
    }

    /**
     * Whether the argument Intent requested a contact insert action or not.
     */
    protected static boolean isInsert(Intent intent) {
        return intent == null ? false : isInsert(intent.getAction());
    }

    protected static boolean isInsert(String action) {
        return Intent.ACTION_INSERT.equals(action)
                || ContactEditorBaseActivity.ACTION_INSERT.equals(action);
    }

    protected static boolean isEdit(String action) {
        return Intent.ACTION_EDIT.equals(action)
                || ContactEditorBaseActivity.ACTION_EDIT.equals(action);
    }

    /**
     * Persist the accumulated editor deltas.
     */
    abstract protected boolean doSaveSIMContactAction(int saveMode);
    /// As below mediatek code
    /** M: Add for SIM Service refactory @{ */
    public void onSaveSIMContactCompleted(boolean hadChanges, Intent intent) {

        if (intent == null) {
            Log.w(TAG, "[onSaveSIMContactCompleted] data is null.");
            return;
        }

        mSubsciberAccount.getProgressHandler().dismissDialog(getFragmentManager());
        Log.i(TAG, "[onSaveSIMContactCompleted] mStatus = " + mStatus);
        if (mStatus == Status.SUB_ACTIVITY) {
            Log.d(TAG, "[onSaveSIMContactCompleted]status changed as EDITING,ori is SUB_ACTIVITY");
            mStatus = Status.EDITING;
        }

        int result = intent.getIntExtra(SimEditProcessor.RESULT, -2);
        Log.d(TAG, "[onSaveSIMContactCompleted] result = " + result);
        if (result == SimEditProcessor.RESULT_CANCELED) {
            mStatus = Status.EDITING;
            Log.d(TAG, "[onSaveSIMContactCompleted]change status is Status.EDITING 2");
            if (intent != null) {
                boolean quitEdit = intent.getBooleanExtra(KEY_NEED_QUIT_EDIT, false);
                Log.d(TAG, "[onSaveSIMContactCompleted] isQuitEdit : " + quitEdit);
                if (quitEdit) {
                    if (getActivity() != null) {
                        getActivity().finish();
                        /// Add for ALPS02384743, return if finishing activity, no need to
                        // refresh activity and bind editor. @{
                        Log.d(TAG, "[onSaveSIMContactCompleted] finish activity.");
                        return;
                        /// @}
                    }
                }
                ArrayList<RawContactDelta> simData =
                        intent.getParcelableArrayListExtra(SimServiceUtils.KEY_SIM_DATA);
                Log.d(TAG, "[onSaveSIMContactCompleted] simData : " + simData);
                mState = (RawContactDeltaList) simData;
                mAggregationSuggestionsRawContactId = 0;
                setEnabled(true);
                Log.d(TAG, "[onSaveSIMContactCompleted] setEnabletrue, and bindEditors");
                bindEditors();
                mSubsciberAccount.setIsSaveToSim(true);
                return;
            }
        } else if (result == SimEditProcessor.RESULT_OK) {
            int saveMode = intent.getIntExtra(
                    ContactEditorFragment.SAVE_MODE_EXTRA_KEY, ContactEditor.SaveMode.CLOSE);
            Uri contactLookupUri = intent.getData();
            Log.d(TAG, "[onSaveSIMContactCompleted] result: RESULT_OK,mIsEdit = " + mIsEdit
                    + ",lookupUri = " + contactLookupUri);
            switch (saveMode) {
                case SaveMode.CLOSE: {
                    final Intent resultIntent;
                    if (contactLookupUri != null && ContactEditorBaseActivity.sIsClickedSaveText) { // prize add sIsClickedSaveText for bug 52850 by zhaojian 20180323
                        // prize add for bug 52850 by zhaojian 20180323 start
                        ContactEditorBaseActivity.sIsClickedSaveText = false;
                        // prize add for bug 52850 by zhaojian 20180323 end
                        final Uri lookupUri = maybeConvertToLegacyLookupUri(
                                mContext, contactLookupUri, mLookupUri);
                        //M:OP01 RCS load rich call screen from server when new/edit contact. @{
                        ExtensionManager.getInstance().getRcsRichUiExtension()
                                .loadRichScrnByContactUri(lookupUri, getActivity());
                        /** @} */
                        resultIntent = ImplicitIntentsUtil.composeQuickContactIntent(lookupUri,
                                QuickContactActivity.MODE_FULLY_EXPANDED);
                        // same with onSaveContactCompleted function
                        resultIntent.putExtra(QuickContactActivity.EXTRA_PREVIOUS_SCREEN_TYPE,
                                ScreenType.EDITOR);
                    } else {
                        resultIntent = null;
                    }
                    mStatus = Status.CLOSING;
                    Log.d(TAG, "[onSaveSIMContactCompleted]change status is Status.CLOSING");
                    if (mListener != null) {
                        mListener.onSaveFinished(resultIntent);
                    }
                    Log.d(TAG, "Status.CLOSING onSaveFinished");
                    break;
                }

                case SaveMode.RELOAD:
                    Log.d(TAG, "[onSaveCompleted]SaveMode.RELOAD2, reloadFullEditor");
                    if (contactLookupUri != null) {
                        // If this was in INSERT, we are changing into an EDIT now.
                        // If it already was an EDIT, we are changing to the new Uri now
                        mState = new RawContactDeltaList();
                        load(Intent.ACTION_EDIT, contactLookupUri, null);
                        mStatus = Status.LOADING;
                        getLoaderManager().restartLoader(LOADER_CONTACT, null,
                                mContactLoaderListener);
                    }
                    break;
                default:
                    break;
            }
        } else if (result == SimEditProcessor.RESULT_NO_DATA) {
            mStatus = Status.EDITING;
            if (intent != null) {
                boolean quitEdit = intent.getBooleanExtra(KEY_NEED_QUIT_EDIT, false);
                Log.d(TAG, "[onSaveSIMContactCompleted] isQuitEdit : " + quitEdit);
                if (quitEdit) {
                    if (getActivity() != null) {
                        getActivity().finish();
                        return;
                    }
                }
                setEnabled(true);
                bindEditors();
                Log.d(TAG, "[onSaveSIMContactCompleted] setEnabletrue, and bindEditors");
                mSubsciberAccount.setIsSaveToSim(true);
                return;
            }
        }
    }


    /** M:
     * @param hadChanges
     * @param saveSucceeded
     * @param contactLookupUri
     */
    private void setStatus(boolean hadChanges, boolean saveSucceeded, Uri contactLookupUri) {
        if (contactLookupUri == null && !hadChanges && !saveSucceeded) {
            Log.d(TAG, "[setStatus] change status as Status.EDITING");
            mStatus = Status.EDITING;
            // getActivity().finish();
        }
        /** M: Bug Fix for CR ALPS00318983 @{ */
        else if (contactLookupUri == null && hadChanges && saveSucceeded) {
            mSubsciberAccount.setNeedFinish(true);
            Log.d(TAG, "[setStatus] the contact is deleted,start PeopleActivity");
            Intent intent = new Intent(mContext, PeopleActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
    /** @} */

    /**
     * M: New Feature by Mediatek Begin. Original Android's code:
     *  Descriptions: create sim/usim contact
     */
    protected boolean saveToIccCard(RawContactDeltaList state, int saveMode,
            Class<? extends Activity> callbackActivity) {
        Log.d(TAG, "[saveToIccCard]saveMode = " + saveMode);
        if (!preSavetoSim(saveMode)) {
            Log.i(TAG, "[saveToIccCard]fail,saveMode = " + saveMode);
            return false;
        }
        ContactEditorUtilsEx.showLogContactState(state);

        setEnabled(false);

        Intent serviceIntent = new Intent(mContext, SimProcessorService.class);

        serviceIntent.putParcelableArrayListExtra(SimServiceUtils.KEY_SIM_DATA, state);
        serviceIntent.putParcelableArrayListExtra(SimServiceUtils.KEY_OLD_SIM_DATA,
                mSubsciberAccount.getOldState());
        ContactEditorUtilsEx.showLogContactState(mSubsciberAccount.getOldState());
        mSubsciberAccount.processSaveToSim(serviceIntent, mLookupUri);

        Log.d(TAG, "[saveToIccCard]set setEnabled false,the mLookupUri is = " + mLookupUri);
        ContactEditorUtilsEx.processGroupMetadataToSim(state, serviceIntent, mGroupMetaData);

        /** M: Add for SIM Service refactory @{ */
        serviceIntent.putExtra(SimServiceUtils.SERVICE_SUBSCRIPTION_KEY,
                mSubsciberAccount.getSubId());
        serviceIntent.putExtra(SimServiceUtils.SERVICE_WORK_TYPE,
                SimServiceUtils.SERVICE_WORK_EDIT);
        if (callbackActivity != null) {
            // Callback intent will be invoked by the service once the contact is
            // saved.  The service will put the URI of the new contact as "data" on
            // the callback intent.
            Intent callbackIntent = new Intent(mContext, callbackActivity);
            callbackIntent.putExtra(SAVE_MODE_EXTRA_KEY, saveMode);
            callbackIntent.setAction(SimEditProcessor.EDIT_SIM_ACTION);
            serviceIntent.putExtra(SimProcessorService.EXTRA_CALLBACK_INTENT, callbackIntent);
        }
        mContext.startService(serviceIntent);
        /** @} */

        mSubsciberAccount.setIsSaveToSim(true);

        return true;
    }
    /** @} */

    /**
     * @param saveMode
     */
    protected boolean preSavetoSim(int saveMode) {
        Log.i(TAG, "[preSavetoSim]saveMode = " + saveMode);
        if (!hasValidState() || mStatus != Status.SAVING) {
            Log.i(TAG, "[preSavetoSim]return false,mStatus = " + mStatus);
            return false;
        }

        if (!hasPendingChanges()) {
            Log.i(TAG, "[preSavetoSim] hasPendingChanges is false");
            /// M: Fix CR: ALPS02635893 There is no response in contact editor.
            setEnabled(true);
            onSaveCompleted(/* hadChanges =*/ false, saveMode,
                    /* saveSucceeded =*/ mLookupUri != null, mLookupUri, /* joinContactId =*/ null);
            return false;
        }

        return true;
    }

    public boolean isSimType() {
       return mSubsciberAccount.isIccAccountType(mState);
    }
    /** @}*/

    /** M: Bug Fix for ALPS00416628 @{ */
    @Override
    public void onPause() {
        Log.d(TAG, "[onPause]");
        if (null != mAggregationSuggestionPopup) {
            mAggregationSuggestionPopup.dismiss();
            mAggregationSuggestionPopup = null;
        }
        /** M:OP01 RCS will close listener of phone number text change.@{*/
        ExtensionManager.getInstance().getRcsExtension().closeTextChangedListener(false);
        /** @} */
        super.onPause();
    }
    /** @} */

    public void doDiscard() {
        Log.d(TAG, "[doDiscard],call revert()");
        revert();
    }

    /* prize zhangzhonghao 20160414 start */
    
    /**
     * ensure show delete view 
     * 
     * @return
     */
    public Boolean prizeIsShowDeleteView(){
        if(isInsert(mAction)){
            return false;
        }else if(isEdit(mAction)){
            return true;
        }else{
            return false;
        }
    }
    /**
     * delete this contact
     */
    public void prizeDeleteContact(){
        if (mListener != null) mListener.onDeleteRequested(mLookupUri);
    }
    /* prize zhangzhonghao 20160414 end */
    

}
