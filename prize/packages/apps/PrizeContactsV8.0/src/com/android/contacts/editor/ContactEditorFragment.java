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
 * limitations under the License
 */

package com.android.contacts.editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.contacts.ContactSaveService;
import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorActivity;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.RawContactDelta;
import com.android.contacts.common.model.RawContactDeltaList;
import com.android.contacts.common.model.ValuesDelta;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.util.AccountsListAdapter;
import com.android.contacts.common.util.AccountsListAdapter.AccountListFilter;
import com.android.contacts.detail.PhotoSelectionHandler;
import com.android.contacts.editor.Editor.EditorListener;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.UiClosables;

import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.util.Log;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

//prize-add-huangliemin-2016-7-8-start
import com.mediatek.contacts.util.AccountsListAdapterUtils;
import java.util.ArrayList;
//prize-add-huangliemin-2016-7-8-end
//prize-add-huangpengfei-2016-11-15-start
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
//prize-add-huangpengfei-2016-11-15-end
import com.android.contacts.prize.PrizeToastUtils;//prize-add-hpf-2017-10-30

/**
 * Contact editor with all fields displayed.
 */
public class ContactEditorFragment extends ContactEditorBaseFragment implements
        RawContactReadOnlyEditorView.Listener {
    private static final String TAG = "ContactEditorFragment";

    private static final String KEY_EXPANDED_EDITORS = "expandedEditors";

    private static final String KEY_RAW_CONTACT_ID_REQUESTING_PHOTO = "photorequester";
    private static final String KEY_CURRENT_PHOTO_URI = "currentphotouri";
    private static final String KEY_UPDATED_PHOTOS = "updatedPhotos";

    // Used to store which raw contact editors have been expanded. Keyed on raw contact ids.
    private HashMap<Long, Boolean> mExpandedEditors = new HashMap<Long, Boolean>();

    /**
     * The raw contact for which we started "take photo" or "choose photo from gallery" most
     * recently.  Used to restore {@link #mCurrentPhotoHandler} after orientation change.
     */
    private long mRawContactIdRequestingPhoto;

    /**
     * The {@link PhotoHandler} for the photo editor for the {@link #mRawContactIdRequestingPhoto}
     * raw contact.
     *
     * A {@link PhotoHandler} is created for each photo editor in {@link #bindPhotoHandler}, but
     * the only "active" one should get the activity result.  This member represents the active
     * one.
     */
    private PhotoHandler mCurrentPhotoHandler;
    private Uri mCurrentPhotoUri;
    private View mMainView;//prize-add-huangliemin-2016-6-29
    private Bundle mUpdatedPhotos = new Bundle();
    private Handler mHandler;//prize-add-huangliemin-2016-6-29
    /*prize-add-for-popupwindow-on-bottom-huangliemin-2016-7-8-start*/
    private View mPopuWindowView;
    private PopupWindow mPopupWindow;
    /*prize-add-for-popupwindow-on-bottom-huangliemin-2016-7-8-end*/
    private HomeClickBroadCastReceiver mReceiver;//prize-add-hpf-2018-2-6

    public ContactEditorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        Log.i(TAG, "[onCreateView].");
        final View view = inflater.inflate(R.layout.contact_editor_fragment, container, false);
        /*prize-add-huangliemin-2016-6-29-start*/
        mMainView = view;
        mHandler = new Handler();
        /*prize-add-huangliemin-2016-6-29-end*/

        mContent = (LinearLayout) view.findViewById(R.id.editors);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.i(TAG, "[onCreate].");
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.prize.home.click");
        mReceiver = new HomeClickBroadCastReceiver();
        mContext.registerReceiver(mReceiver, filter);
        
        if (savedState != null) {
            mExpandedEditors = (HashMap<Long, Boolean>)
                    savedState.getSerializable(KEY_EXPANDED_EDITORS);
            mRawContactIdRequestingPhoto = savedState.getLong(
                    KEY_RAW_CONTACT_ID_REQUESTING_PHOTO);
            mCurrentPhotoUri = savedState.getParcelable(KEY_CURRENT_PHOTO_URI);
            mUpdatedPhotos = savedState.getParcelable(KEY_UPDATED_PHOTOS);
            mRawContactIdToDisplayAlone = savedState.getLong(
                    ContactEditorBaseFragment.INTENT_EXTRA_RAW_CONTACT_ID_TO_DISPLAY_ALONE, -1);
        }
    }

    @Override
    public void load(String action, Uri lookupUri, Bundle intentExtras) {
        super.load(action, lookupUri, intentExtras);
        if (intentExtras != null) {
            mRawContactIdToDisplayAlone = intentExtras.getLong(
                    ContactEditorBaseFragment.INTENT_EXTRA_RAW_CONTACT_ID_TO_DISPLAY_ALONE, -1);
        }
    }

    @Override
    public void onStart() {
        getLoaderManager().initLoader(LOADER_GROUPS, null, mGroupsLoaderListener);
        super.onStart();
        Log.d(TAG, "[onStart] ");
    }

    @Override
    public void onExternalEditorRequest(AccountWithDataSet account, Uri uri) {
        if (mListener != null) {
            mListener.onCustomEditContactActivityRequested(account, uri, null, false);
        }
    }

    @Override
    public void onEditorExpansionChanged() {
        Log.d(TAG, "[onEditorExpansionChanged]");
        updatedExpandedEditorsMap();
    }

    @Override
    protected void setGroupMetaData() {
        if (mGroupMetaData == null) {
            return;
        }
        int editorCount = mContent.getChildCount();
        Log.d(TAG,"editorCount = " + editorCount);
        for (int i = 0; i < editorCount; i++) {
            BaseRawContactEditorView editor = (BaseRawContactEditorView) mContent.getChildAt(i);
            editor.setGroupMetaData(mGroupMetaData);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return revert();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void bindEditors() {
        Log.d(TAG, "[bindEditors]");
        // bindEditors() can only bind views if there is data in mState, so immediately return
        // if mState is null
        if (mState.isEmpty()) {
            Log.w(TAG, "[bindEditors]mState is empty,return.");
            return;
        }

        // Check if delta list is ready.  Delta list is populated from existing data and when
        // editing an read-only contact, it's also populated with newly created data for the
        // blank form.  When the data is not ready, skip. This method will be called multiple times.
        if ((mIsEdit && !mExistingContactDataReady) || (mHasNewContact && !mNewContactDataReady)) {
            Log.w(TAG, "[bindEditors], delta list is not ready,return.");
            return;
        }

        // Sort the editors
        Collections.sort(mState, mComparator);

        // Remove any existing editors and rebuild any visible
        mContent.removeAllViews();

        final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(mContext);
        int numRawContacts = mState.size();

        for (int i = 0; i < numRawContacts; i++) {
            // TODO ensure proper ordering of entities in the list
            final RawContactDelta rawContactDelta = mState.get(i);
            if (!rawContactDelta.isVisible()) continue;

            final AccountType type = rawContactDelta.getAccountType(accountTypes);
            final long rawContactId = rawContactDelta.getRawContactId();

            if (mRawContactIdToDisplayAlone != -1 && mRawContactIdToDisplayAlone != rawContactId) {
                continue;
            }

            final BaseRawContactEditorView editor;
            if (!type.areContactsWritable()) {
                editor = (BaseRawContactEditorView) inflater.inflate(
                        R.layout.raw_contact_readonly_editor_view, mContent, false);
            } else {
              /*prize-change-huangliemin-2016-6-7-start*/
                /*editor = (RawContactEditorView) inflater.inflate(R.layout.raw_contact_editor_view,
                        mContent, false);*/
                editor = (RawContactEditorView) inflater.inflate(R.layout.raw_contact_editor_view_prize_huangliemin_2016_6_7,
                        mContent, false);
              /*prize-change-huangliemin-2016-6-7-end*/
				/* prize zhangzhonghao add delete button start */
                RawContactEditorView prizeEditor = (RawContactEditorView) editor;
                prizeEditor.getDeleteView().setVisibility(prizeIsShowDeleteView() ? View.VISIBLE : View.GONE);
                prizeEditor.getDeleteView().setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                    	prizeDeleteContact();
                    }
                });
				/* prize zhangzhonghao add delete button end */
            }
            /// M: If sim type, disable photo editor's triangle affordance.
            mSubsciberAccount.disableTriangleAffordance(editor, mState);

            editor.setListener(this);
            final List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(mContext)
                    .getAccounts(true);
            android.util.Log.d(TAG, "[bindEditors]  accounts = " + accounts);//prize-add log-hpf-2018-3-6
            /// M: need use isEditingUserProfile() instead of mNewLocalProfile
            if (mHasNewContact && !isEditingUserProfile() && accounts.size() > 1) {
                addAccountSwitcher(mState.get(0), editor);
            }

            editor.setEnabled(isEnabled());

            if (mRawContactIdToDisplayAlone != -1) {
                editor.setCollapsed(false);
            } else if (mExpandedEditors.containsKey(rawContactId)) {
                editor.setCollapsed(mExpandedEditors.get(rawContactId));
            } else {
                // By default, only the first editor will be expanded.
                editor.setCollapsed(i != 0);
            }

            /** M: AAS&SNE ensure phone kind updated and exists @{ */
            GlobalEnv.getAasExtension().ensurePhoneKindForEditor(type,
                    mSubsciberAccount.getSubId(), rawContactDelta);
            GlobalEnv.getSneExtension().onEditorBindEditors(rawContactDelta,
                    type, mSubsciberAccount.getSubId());
            /** @} */

            mContent.addView(editor);

            editor.setState(rawContactDelta, type, mViewIdGenerator, isEditingUserProfile());
            if (mRawContactIdToDisplayAlone != -1) {
                editor.setCollapsible(false);
            } else {
            	/*prize-change for dido os8.0-hpf-2017-8-31-start*/
                //editor.setCollapsible(numRawContacts > 1);
            	editor.setCollapsible(false);
            	/*prize-change for dido os8.0-hpf-2017-8-31-end*/
            }

            // Set up the photo handler.
            bindPhotoHandler(editor, type, mState);

            // If a new photo was chosen but not yet saved, we need to update the UI to
            // reflect this.
            final Uri photoUri = updatedPhotoUriForRawContact(rawContactId);
            if (photoUri != null) editor.setFullSizedPhoto(photoUri);

            if (editor instanceof RawContactEditorView) {
                final Activity activity = getActivity();
                final RawContactEditorView rawContactEditor = (RawContactEditorView) editor;
                final ValuesDelta nameValuesDelta = rawContactEditor.getNameEditor().getValues();
                final EditorListener structuredNameListener = new EditorListener() {

                    @Override
                    public void onRequest(int request) {
                        // Make sure the activity is running
                        if (activity.isFinishing()) {
                            return;
                        }
                        if (!isEditingUserProfile()) {
                            if (request == EditorListener.FIELD_CHANGED) {
                                if (!nameValuesDelta.isSuperPrimary()) {
                                    unsetSuperPrimaryForAllNameEditors();
                                    nameValuesDelta.setSuperPrimary(true);
                                }
                                acquireAggregationSuggestions(activity,
                                        rawContactEditor.getNameEditor().getRawContactId(),
                                        rawContactEditor.getNameEditor().getValues());
                            } else if (request == EditorListener.FIELD_TURNED_EMPTY) {
                                if (nameValuesDelta.isSuperPrimary()) {
                                    nameValuesDelta.setSuperPrimary(false);
                                }
                            }
                        }
                    }

                    @Override
                    public void onDeleteRequested(Editor removedEditor) {
                    }
                };

                final StructuredNameEditorView nameEditor = rawContactEditor.getNameEditor();
                nameEditor.setEditorListener(structuredNameListener);

                rawContactEditor.setAutoAddToDefaultGroup(mAutoAddToDefaultGroup);

                if (!isEditingUserProfile() && isAggregationSuggestionRawContactId(rawContactId)) {
                    acquireAggregationSuggestions(activity,
                            rawContactEditor.getNameEditor().getRawContactId(),
                            rawContactEditor.getNameEditor().getValues());
                }
            }
        }

        setGroupMetaData();

        // Show editor now that we've loaded state
        mContent.setVisibility(View.VISIBLE);

        // Refresh Action Bar as the visibility of the join command
        // Activity can be null if we have been detached from the Activity
        invalidateOptionsMenu();

        updatedExpandedEditorsMap();
    }

    private void unsetSuperPrimaryForAllNameEditors() {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View view = mContent.getChildAt(i);
            if (view instanceof RawContactEditorView) {
                final RawContactEditorView rawContactEditorView = (RawContactEditorView) view;
                final StructuredNameEditorView nameEditorView =
                        rawContactEditorView.getNameEditor();
                if (nameEditorView != null) {
                    final ValuesDelta valuesDelta = nameEditorView.getValues();
                    if (valuesDelta != null) {
                        valuesDelta.setSuperPrimary(false);
                    }
                }
            }
        }
    }

    /**
     * Update the values in {@link #mExpandedEditors}.
     */
    private void updatedExpandedEditorsMap() {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                BaseRawContactEditorView childEditor = (BaseRawContactEditorView) childView;
                mExpandedEditors.put(childEditor.getRawContactId(), childEditor.isCollapsed());
            }
        }
    }

    /**
     * If we've stashed a temporary file containing a contact's new photo, return its URI.
     * @param rawContactId identifies the raw-contact whose Bitmap we'll try to return.
     * @return Uru of photo for specified raw-contact, or null
     */
    private Uri updatedPhotoUriForRawContact(long rawContactId) {
        return (Uri) mUpdatedPhotos.get(String.valueOf(rawContactId));
    }

    private void bindPhotoHandler(BaseRawContactEditorView editor, AccountType type,
            RawContactDeltaList state) {
        final int mode;
        boolean showIsPrimaryOption;
        if (type.areContactsWritable()) {
            if (editor.hasSetPhoto()) {
                mode = PhotoActionPopup.Modes.WRITE_ABLE_PHOTO;
                showIsPrimaryOption = hasMoreThanOnePhoto();
            } else {
                mode = PhotoActionPopup.Modes.NO_PHOTO;
                showIsPrimaryOption = false;
            }
        } else if (editor.hasSetPhoto() && hasMoreThanOnePhoto()) {
            mode = PhotoActionPopup.Modes.READ_ONLY_PHOTO;
            showIsPrimaryOption = true;
        } else {
            // Read-only and either no photo or the only photo ==> no options
            editor.getPhotoEditor().setEditorListener(null);
            editor.getPhotoEditor().setShowPrimary(false);
            return;
        }
        if (mRawContactIdToDisplayAlone != -1) {
            showIsPrimaryOption = false;
        }
        final PhotoHandler photoHandler = new PhotoHandler(mContext, editor, mode, state);
        editor.getPhotoEditor().setEditorListener(
                (PhotoHandler.PhotoEditorListener) photoHandler.getListener());
        editor.getPhotoEditor().setShowPrimary(showIsPrimaryOption);

        // Note a newly created raw contact gets some random negative ID, so any value is valid
        // here. (i.e. don't check against -1 or anything.)
        if (mRawContactIdRequestingPhoto == editor.getRawContactId()) {
            mCurrentPhotoHandler = photoHandler;
        }
    }

    private void addAccountSwitcher(
            final RawContactDelta currentState, BaseRawContactEditorView editor) {
        /// M: Change AccountSwitcher: add for ICCAccountType. @{
        final AccountWithDataSet currentAccount;
        if (mSubsciberAccount.isIccAccountType(mState)) {
            currentAccount = new AccountWithDataSetEx(currentState.getAccountName(),
                    currentState.getAccountType(), currentState.getDataSet(),
                    mSubsciberAccount.getSubId());
        } else {
            currentAccount = new AccountWithDataSet(currentState.getAccountName(),
                    currentState.getAccountType(), currentState.getDataSet());
        }
        // @}
        final View accountView = editor.findViewById(R.id.account);
        final View anchorView = editor.findViewById(R.id.account_selector_container);
        if (accountView == null) {
            Log.w(TAG, "[addAccountSwitcher]accountView is null,return!");
            return;
        }
        anchorView.setVisibility(View.VISIBLE);
        /*prize-change-accountView-to-anchorView-huangliemin-2016-6-7*/
        anchorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	/*prize-change-to-popupwindow-huangliemin-2016-7-8-start*/
            	/*
                final ListPopupWindow popup = new ListPopupWindow(mContext, null);
                final AccountsListAdapter adapter =
                        new AccountsListAdapter(mContext,
                        AccountListFilter.ACCOUNTS_CONTACT_WRITABLE, currentAccount);
                popup.setWidth(anchorView.getWidth());
                popup.setAnchorView(anchorView);
                popup.setAdapter(adapter);
                popup.setModal(true);
                popup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NOT_NEEDED);
                popup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
                        UiClosables.closeQuietly(popup);
                        AccountWithDataSet newAccount = adapter.getItem(position);
                        if (!newAccount.equals(currentAccount)) {
                            /// M: Change feature: AccountSwitcher. @{
                            // If the new account is sim account, set the sim info firstly.
                            // Or need to clear sim info firstly.
                            if (mSubsciberAccount.setAccountSimInfo(currentState, newAccount,
                                    mCurrentPhotoHandler, mContext)) {
                                return;
                            }
                            // @}
                            mNewContactAccountChanged = true;
                            rebindEditorsForNewContact(currentState, currentAccount, newAccount);
                        }
                    }
                });
                popup.show();
                */
            	final List<AccountWithDataSet> mAccounts = getAccounts(AccountListFilter.ACCOUNTS_CONTACT_WRITABLE);
            	AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(mContext);
            	
            	if(currentAccount !=null
            			&& !mAccounts.isEmpty()
            			&& !mAccounts.get(0).equals(currentAccount)
            			&& mAccounts.remove(currentAccount)) {
            		mAccounts.add(0,currentAccount);
            	}
            	android.util.Log.d(TAG, "[addAccountSwitcher]  mAccounts.size = " + mAccounts.size());//prize-add log-hpf-2018-3-6
            	
            	/*PRIZE-add for dido os8.0-hpf-2017-8-26-start*/
                InputMethodManager im = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
                im.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                /*PRIZE-add for dido os8.0-hpf-2017-8-26-end*/
            	
            	if(mPopuWindowView == null) {
            		mPopuWindowView = View.inflate(mContext, R.layout.prize_popupwindow, null);
            		/*prize-add for dido os8.0 -hpf-2017-7-28-start*/
            		View bottomButton = mPopuWindowView.findViewById(R.id.prize_popup_bottom_button_container);
            		View titleDivider = mPopuWindowView.findViewById(R.id.prize_popup_title_divider);
            		TextView cancelBtn = (TextView)mPopuWindowView.findViewById(R.id.cancel_btn);
            		cancelBtn.setVisibility(View.VISIBLE);
            		cancelBtn.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if(mPopupWindow != null) {
			            		mPopupWindow.dismiss();
							}
						}
					});
            		titleDivider.setVisibility(View.GONE);
            		bottomButton.setVisibility(View.GONE);
            		/*prize-add for dido os8.0 -hpf-2017-7-28-end*/
            	}
            	final ViewGroup mMenuContent = (ViewGroup)mPopuWindowView.findViewById(R.id.prize_popup_content);
            	
            	if(mPopupWindow == null) {
            		mPopupWindow = new PopupWindow(mPopuWindowView, 
            				WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            		//mPopupWindow.setAnimationStyle(R.style.PrizePopupWindowStyle);
            		mPopupWindow.setFocusable(true);
            		mPopupWindow.setOutsideTouchable(true);
            		mPopupWindow.setAnimationStyle(R.style.GetDialogBottomMenuAnimation);
            		//mPopupWindow.update();
            	}
            	
            	mMenuContent.removeAllViews();
            	
            	for(int i=1;i<mAccounts.size();i++) {
            		TextView menuItem = (TextView)View.inflate(getContext(), R.layout.prize_popmenu_text_huangliemin_2016_7_8, null);
            		menuItem.setPadding(0, 0, 0, 0);
            		menuItem.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
            				mContext.getResources().getDimensionPixelOffset(R.dimen.dialog_bottom_menu_item_height)));
            		
            		AccountWithDataSet account = mAccounts.get(i);
            		AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
            		//menuItem.setText(mAccounts.get(i).name);
            		//menuItem.setText(accountType.getDisplayLabel(mContext));
            		AccountsListAdapterUtils.getViewForName(mContext, account, accountType, menuItem);
            		mMenuContent.addView(menuItem);
            		menuItem.setOnClickListener(new PopuItemClickListener(i,mAccounts,currentAccount,currentState));
            		
            		if(i<mAccounts.size()-1){
            			View Divider = new View(getContext());
                		Divider.setLayoutParams(new LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,1));
                		Divider.setBackgroundColor(getResources().getColor(R.color.divider_line_color_light));
                		mMenuContent.addView(Divider);
            		}
            	}
            	/*prize-add-huangliemin-2016-7-29-start*/
            	WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
            	params.alpha=0.7f;
            	getActivity().getWindow().setAttributes(params);
            	mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
					
					@Override
					public void onDismiss() {
						// TODO Auto-generated method stub
						WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
						params.alpha=1f;
						getActivity().getWindow().setAttributes(params);
					}
				});
            	/*prize-add-huangliemin-2016-7-29-end*/
            	mPopupWindow.showAtLocation(anchorView, Gravity.BOTTOM, 0, 0);
            	
            	
                /*prize-change-to-popupwindow-huangliemin-2016-7-8-start*/
            }
        });
    }
    
    /*prize-add-huangliemin-2016-7-8-start*/
    public List<AccountWithDataSet> getAccounts(AccountListFilter accountListFilter) {
    	AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(mContext);
    	if (accountListFilter == AccountListFilter.ACCOUNTS_GROUP_WRITABLE) {
            /// M: add for sim account
            return AccountsListAdapterUtils.getGroupAccount(mAccountTypes);
        }

        /** M: For MTK multiuser in 3gdatasms @ {  */
        ArrayList<AccountWithDataSet> multiAccountList = AccountsListAdapterUtils.
                getAccountForMultiUser(mAccountTypes, accountListFilter);
        if (multiAccountList != null && multiAccountList.size() > 0) {
            return multiAccountList;
        }
        /** @ } */

        return new ArrayList<AccountWithDataSet>(mAccountTypes.getAccounts(
                accountListFilter == AccountListFilter.ACCOUNTS_CONTACT_WRITABLE));
    }
    
    class PopuItemClickListener implements View.OnClickListener {
    	private int Position;
    	List<AccountWithDataSet> accountList;
    	AccountWithDataSet currentAccount;
    	RawContactDelta currentState;

		public PopuItemClickListener(int position, List<AccountWithDataSet> list, AccountWithDataSet account
				, RawContactDelta state) {
			Position = position;
			accountList = list;
			currentAccount = account;
			currentState = state;
		}
    		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(mPopupWindow!=null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			}
			
			AccountWithDataSet newAccount = accountList.get(Position);
			
			if (!newAccount.equals(currentAccount)) {
                /// M: Change feature: AccountSwitcher. @{
                // If the new account is sim account, set the sim info firstly.
                // Or need to clear sim info firstly.
                if (mSubsciberAccount.setAccountSimInfo(currentState, newAccount,
                        mCurrentPhotoHandler, mContext)) {
                    return;
                }
                // @}
                mNewContactAccountChanged = true;
                rebindEditorsForNewContact(currentState, currentAccount, newAccount);

			}
    	
		}
    }
    /*prize-add-huangliemin-2016-7-8-end*/

    @Override
    protected boolean doSaveAction(int saveMode, Long joinContactId) {
        final Intent intent = ContactSaveService.createSaveContactIntent(mContext, mState,
                SAVE_MODE_EXTRA_KEY, saveMode, isEditingUserProfile(),
                ((Activity) mContext).getClass(), ContactEditorActivity.ACTION_SAVE_COMPLETED,
                mUpdatedPhotos, JOIN_CONTACT_ID_EXTRA_KEY, joinContactId);
        return startSaveService(mContext, intent, saveMode);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(KEY_EXPANDED_EDITORS, mExpandedEditors);
        outState.putLong(KEY_RAW_CONTACT_ID_REQUESTING_PHOTO, mRawContactIdRequestingPhoto);
        outState.putParcelable(KEY_CURRENT_PHOTO_URI, mCurrentPhotoUri);
        outState.putParcelable(KEY_UPDATED_PHOTOS, mUpdatedPhotos);
        outState.putLong(ContactEditorBaseFragment.INTENT_EXTRA_RAW_CONTACT_ID_TO_DISPLAY_ALONE,
                mRawContactIdToDisplayAlone);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mStatus == Status.SUB_ACTIVITY) {
            Log.d(TAG, "[onActivityResult]mStatus changed as Status.EDITING,ori is SUB_ACTIVITY");
            mStatus = Status.EDITING;
        }

        // See if the photo selection handler handles this result.
        if (mCurrentPhotoHandler != null && mCurrentPhotoHandler.handlePhotoActivityResult(
                requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void joinAggregate(final long contactId) {
        Log.d(TAG, "[joinAggregate] start ContactSaveService,contactId = " + contactId);
        final Intent intent = ContactSaveService.createJoinContactsIntent(
                mContext, mContactIdForJoin, contactId, ContactEditorActivity.class,
                ContactEditorActivity.ACTION_JOIN_COMPLETED);
        mContext.startService(intent);
    }

    /**
     * Sets the photo stored in mPhoto and writes it to the RawContact with the given id
     */
    private void setPhoto(long rawContact, Bitmap photo, Uri photoUri) {
        BaseRawContactEditorView requestingEditor = getRawContactEditorView(rawContact);

        if (photo == null || photo.getHeight() <= 0 || photo.getWidth() <= 0) {
            // This is unexpected.
            Log.w(TAG, "Invalid bitmap passed to setPhoto()");
            return;
        }

        if (requestingEditor != null) {
            requestingEditor.setPhotoEntry(photo);
            // Immediately set all other photos as non-primary. Otherwise the UI can display
            // multiple photos as "Primary photo".
            for (int i = 0; i < mContent.getChildCount(); i++) {
                final View childView = mContent.getChildAt(i);
                if (childView instanceof BaseRawContactEditorView
                        && childView != requestingEditor) {
                    final BaseRawContactEditorView rawContactEditor
                            = (BaseRawContactEditorView) childView;
                    rawContactEditor.getPhotoEditor().setSuperPrimary(false);
                }
            }
        } else {
            Log.w(TAG, "The contact that requested the photo is no longer present.");
        }

        mUpdatedPhotos.putParcelable(String.valueOf(rawContact), photoUri);
    }

    /**
     * Finds raw contact editor view for the given rawContactId.
     */
    @Override
    protected View getAggregationAnchorView(long rawContactId) {
        BaseRawContactEditorView editorView = getRawContactEditorView(rawContactId);
        return editorView == null ? null : editorView.findViewById(R.id.anchor_view);
    }

    public BaseRawContactEditorView getRawContactEditorView(long rawContactId) {
        for (int i = 0; i < mContent.getChildCount(); i++) {
            final View childView = mContent.getChildAt(i);
            if (childView instanceof BaseRawContactEditorView) {
                final BaseRawContactEditorView editor = (BaseRawContactEditorView) childView;
                if (editor.getRawContactId() == rawContactId) {
                    return editor;
                }
            }
        }
        return null;
    }

    /**
     * Returns true if there is currently more than one photo on screen.
     */
    private boolean hasMoreThanOnePhoto() {
        int countWithPicture = 0;
        final int numEntities = mState.size();
        for (int i = 0; i < numEntities; i++) {
            final RawContactDelta entity = mState.get(i);
            if (entity.isVisible()) {
                final ValuesDelta primary = entity.getPrimaryEntry(Photo.CONTENT_ITEM_TYPE);
                if (primary != null && primary.getPhoto() != null) {
                    countWithPicture++;
                } else {
                    final long rawContactId = entity.getRawContactId();
                    final Uri uri = mUpdatedPhotos.getParcelable(String.valueOf(rawContactId));
                    if (uri != null) {
                        try {
                            mContext.getContentResolver().openInputStream(uri);
                            countWithPicture++;
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "[hasMoreThanOnePhoto]e = " + e);
                        }
                    }
                }

                if (countWithPicture > 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Custom photo handler for the editor.  The inner listener that this creates also has a
     * reference to the editor and acts as an {@link EditorListener}, and uses that editor to hold
     * state information in several of the listener methods.
     */
    public final class PhotoHandler extends PhotoSelectionHandler {

        final long mRawContactId;
        private final BaseRawContactEditorView mEditor;
        private final PhotoActionListener mPhotoEditorListener;

        public PhotoHandler(Context context, BaseRawContactEditorView editor, int photoMode,
                RawContactDeltaList state) {
            super(context, editor.getPhotoEditor().getChangeAnchorView(), photoMode, false, state);
            mEditor = editor;
            mRawContactId = editor.getRawContactId();
            mPhotoEditorListener = new PhotoEditorListener();
        }

        @Override
        public PhotoActionListener getListener() {
            return mPhotoEditorListener;
        }

        @Override
        public void startPhotoActivity(Intent intent, int requestCode, Uri photoUri) {
            mRawContactIdRequestingPhoto = mEditor.getRawContactId();
            mCurrentPhotoHandler = this;
            /*prize-change-fix bug[46466]-hpf-2017-12-26-start*/
            boolean isSuperPowerMode = android.os.PowerManager.isSuperSaverMode();
        	if(!isSuperPowerMode) {
        		Log.d(TAG, "[startPhotoActivity]status changed as SUB_ACTIVITY,mStatus:" + mStatus);
                mStatus = Status.SUB_ACTIVITY;
        	}
        	/*prize-change-fix bug[46466]-hpf-2017-12-26-end*/
            mCurrentPhotoUri = photoUri;
            ContactEditorFragment.this.startActivityForResult(intent, requestCode);
        }

        // M: Remove contacts photo when switch account to SIM type.
        public void removePictureChosen() {
            mEditor.setFullSizedPhoto(null);
            mUpdatedPhotos.clear();
            Log.d(TAG, "[removePictureChosen]");
        }
        // @}

        private final class PhotoEditorListener extends PhotoSelectionHandler.PhotoActionListener
                implements EditorListener {

            @Override
            public void onRequest(int request) {
                if (!hasValidState()) return;

                if (request == EditorListener.REQUEST_PICK_PHOTO) {
                    onClick(mEditor.getPhotoEditor());
                }
                if (request == EditorListener.REQUEST_PICK_PRIMARY_PHOTO) {
                    useAsPrimaryChosen();
                }
            }

            @Override
            public void onDeleteRequested(Editor removedEditor) {
                // The picture cannot be deleted, it can only be removed, which is handled by
                // onRemovePictureChosen()
            }

            /**
             * User has chosen to set the selected photo as the (super) primary photo
             */
            public void useAsPrimaryChosen() {
                // Set the IsSuperPrimary for each editor
                int count = mContent.getChildCount();
                for (int i = 0; i < count; i++) {
                    final View childView = mContent.getChildAt(i);
                    if (childView instanceof BaseRawContactEditorView) {
                        final BaseRawContactEditorView editor =
                                (BaseRawContactEditorView) childView;
                        final PhotoEditorView photoEditor = editor.getPhotoEditor();
                        photoEditor.setSuperPrimary(editor == mEditor);
                    }
                }
                bindEditors();
            }

            /**
             * User has chosen to remove a picture
             */
            @Override
            public void onRemovePictureChosen() {
                mEditor.setPhotoEntry(null);

                // Prevent bitmap from being restored if rotate the device.
                // (only if we first chose a new photo before removing it)
                mUpdatedPhotos.remove(String.valueOf(mRawContactId));
                bindEditors();
            }

            @Override
            public void onPhotoSelected(Uri uri) throws FileNotFoundException {
                final Bitmap bitmap = ContactPhotoUtils.getBitmapFromUri(mContext, uri);
                setPhoto(mRawContactId, bitmap, uri);
                mCurrentPhotoHandler = null;
                bindEditors();
            }

            @Override
            public Uri getCurrentPhotoUri() {
                return mCurrentPhotoUri;
            }

            @Override
            public void onPhotoSelectionDismissed() {
                // Nothing to do.
            }
        }
    }

    /// M: Add for SIM contacts feature @{
    @Override
    protected boolean doSaveSIMContactAction(int saveMode) {
        Log.d(TAG, "[doSaveSIMContactAction] saveMode = " + saveMode);
        saveToIccCard(mState, saveMode, ((Activity) mContext).getClass());
        return true;
    }
    /// @}


    /*prize-add-hpf-2017-12-4-start*/
    public boolean prizeAreAllEditorFieldsEmpty(){
        View v = mContent.getChildAt(0);
        if(v instanceof RawContactEditorView) {
            RawContactEditorView rcev = (RawContactEditorView) v;
            return rcev.prizeAreAllEditorFieldsEmpty();
        }
        return false;
    }
    
    public class HomeClickBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
        	if (!ContactEditorFragment.this.getActivity().isChangingConfigurations() && mStatus == Status.EDITING) {
                save(SaveMode.CLOSE);
            }
        }
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
        Log.d(TAG, "[onDestroy]");
        mContext.unregisterReceiver(mReceiver);
        TextFieldsEditorView.mShowFinish = false;
    }
    /*prize-add-hpf-2017-12-4-end*/
    
    
}
