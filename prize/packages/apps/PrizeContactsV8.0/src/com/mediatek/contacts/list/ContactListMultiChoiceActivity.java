/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
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
package com.mediatek.contacts.list;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;

import com.android.contacts.ContactsActivity;
import com.android.contacts.R;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.list.ContactsRequest;

import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.AbstractPickerFragment.OnSelectedContactsCheckBoxChangeListener;
import com.mediatek.contacts.list.DropMenu.DropDownMenu;

import com.mediatek.contacts.util.Log;
/*prize add for bug58893 by zhaojian 20180525 start*/
import java.util.List;
import android.app.ActivityManager;
import com.mediatek.contacts.list.service.MultiChoiceService;
/*prize add for bug58893 by zhaojian 20180525 end*/

/**
 * Displays a list of contacts (or phone numbers or postal addresses) for the
 * purposes of selecting multiple contacts.
 */

public class ContactListMultiChoiceActivity extends ContactsActivity implements
        View.OnCreateContextMenuListener, OnQueryTextListener, OnClickListener, OnCloseListener,
        OnFocusChangeListener {
    private static final String TAG = "ContactListMultiChoiceActivity";

    private static final int SUBACTIVITY_ADD_TO_EXISTING_CONTACT = 0;
    public static final int CONTACTGROUPLISTACTIVITY_RESULT_CODE = 1;

    private static final String KEY_ACTION_CODE = "actionCode";
    private static final int DEFAULT_DIRECTORY_RESULT_LIMIT = 20;

    public static final String RESTRICT_LIST = "restrictlist";

    private ContactsIntentResolverEx mIntentResolverEx;
    protected AbstractPickerFragment mListFragment;

    private int mActionCode = -1;

    private ContactsRequest mRequest;
    private SearchView mSearchView;

    // the dropdown menu with "Select all" and "Deselect all"
    private DropDownMenu mSelectionMenu;
    private boolean mIsSelectedAll = true;
    private boolean mIsSelectedNone = true;
    // if Search Mode now, decide the menu display or not.
    private boolean mIsSearchMode = false;

    // for CT NEW FEATURE
    private int mNumberBalance = 100;

    private enum SelectionMode {
        SearchMode, ListMode
    };

    /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -start */
    private TextView prize_bottom_ok;
    private View prize_bottom_button;

    //prize-add-huangliemin-2016-7-27
    private TextView SelectAllButton;
    private View SelectAllButtonContainer;//prize-add for dido os8.0-hpf-2017-8-21
    private RelativeLayout mBottomLayout;
    private ImageButton mDeleteButton;//prize-add-huangpengfei-2016-9-2
    private TextView mDeleteText;//prize-add-huangpengfei-2016-9-2
    private RelativeLayout mSearchLayout;
    private EditText mSearchEditText;
    private EditText mSearchViewEditText;
    private boolean isSeachMode =false;
    //prize-add-huangliemin-2016-7-27

    // prize add for bug 54802 by zhaojian 20180413 start
    public static boolean sIsActivityActive = false;
    // prize add for bug 54802 by zhaojian 20180413 end

    /*prize add for bug58893 by zhaojian 20180524 start*/
    static final String PRIZE_DISABLE_NAVIGATION = "prize_disable_navigation";
    /*prize add for bug58893 by zhaojian 20180524 end*/
    
    private LinearLayout mDeleteContactsLayout;//prize-add-huangpengfei-2016-8-18

    /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -end */
    public ContactListMultiChoiceActivity() {
        Log.i(TAG, "[ContactListMultiChoiceActivity]new.");
        mIntentResolverEx = new ContactsIntentResolverEx(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof AbstractPickerFragment) {
            mListFragment = (AbstractPickerFragment) fragment;
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.i(TAG,"[onCreate]startPermissionActivity,return.");
            return;
        }
        // for ct new feature
        Intent mmsIntent = this.getIntent();
        Log.i(TAG, "[onCreate]...");
        if (mmsIntent != null) {
            mNumberBalance = mmsIntent.getIntExtra("NUMBER_BALANCE", 100);
            Log.i(TAG, "[onCreate]mNumberBalance from intent = " + mNumberBalance);

        }

        if (savedState != null) {
            mActionCode = savedState.getInt(KEY_ACTION_CODE);
            mNumberBalance = savedState.getInt("NUMBER_BALANCE");
            Log.i(TAG, "[onCreate]mNumberBalance from savedState = " + mNumberBalance);

        }

        // Extract relevant information from the intent
        mRequest = mIntentResolverEx.resolveIntent(getIntent());
        if (!mRequest.isValid()) {
            Log.w(TAG, "[onCreate]Request is invalid!");
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        setContentView(R.layout.contact_picker);

        configureListFragment();

        // Disable Search View in listview
        if (mSearchView != null) {
            mSearchView.setVisibility(View.GONE);
        }

        /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -start */
        prize_bottom_button = findViewById(R.id.prize_bottom_button);
        prize_bottom_ok = (TextView) findViewById(R.id.prize_bottom_ok);
        prize_bottom_ok.setText(R.string.prize_bottom_ok);
        prize_bottom_ok.setOnClickListener(this);
        /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -start */
        /*prize-add-huangliemin-2016-7-27-start*/
        mBottomLayout = (RelativeLayout)findViewById(R.id.prize_bottom_delete_layout);
        mDeleteButton = (ImageButton)findViewById(R.id.delete_contacts_button);
        mDeleteText = (TextView)findViewById(R.id.delet_contacts_text);//prize-add-huangpengfei-2016-9-2
        mDeleteContactsLayout = (LinearLayout)findViewById(R.id.delete_contacts_layout);//prize-add-huangpengfei-2016-8-18
        mDeleteContactsLayout.setEnabled(false);//prize-add for dido os8.0-hpf-2017-9-2
        mSearchLayout = (RelativeLayout)findViewById(R.id.search_edit_layout);
        mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
        mSearchEditText.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					showActionBar(SelectionMode.SearchMode);
				}
				return true;
			}
		});
        //prize-change mDeleteButton to mDeleteContactsLayout -huangpengfei-2016-8-18
        mDeleteContactsLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListFragment.onOptionAction();
			}
		});
        /*prize-add-huangliemin-2016-7-27-end*/
      
        /*prize-add-huangpengfei-2016-9-2-start*/
        
        mListFragment.setSelectedContactsCheckBoxChangeListener(new OnSelectedContactsCheckBoxChangeListener() {
			
			@Override
			public void onSelectedContactsCheckBoxChange() {
				// TODO Auto-generated method stub
				Log.d(TAG,"[onSelectedContactsCheckBoxChange]");
				if (mListFragment.getCheckedItemIds().length == 0) {
					mDeleteContactsLayout.setEnabled(false);
		            
		        }else{
		        	mDeleteContactsLayout.setEnabled(true);
		        }
				if(SelectAllButton != null){
					if(mListFragment.isSelectedAll()){
						SelectAllButton.setText(R.string.prize_unselect_all);
					}else{
						SelectAllButton.setText(R.string.menu_select_all);
					}
				}
			}
		});
        
       
        /*prize-add-huangpengfei-2016-9-2-end*/
        
        showActionBar(SelectionMode.ListMode);
    }

    /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -start */
    public TextView getPrizeOKButton() {
        return prize_bottom_ok;
    }
    /* PRIZE-add some view for delete contacts activity -zhangzhonghao-2015-5-20 -end */

    // prize add for bug 54802 by zhaojian 20180413 start
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "[onStart]");
        sIsActivityActive = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "[onRestart]");
        sIsActivityActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "[onStop]");
        sIsActivityActive = false;
    }
    // prize add for bug 54802 by zhaojian 20180413 end

    /*prize add for bug58893 by zhaojian 20180525 start*/
    @Override
    protected void onResume() {
        super.onResume();
        boolean isServiceWork = isServiceWork(this,"com.mediatek.contacts.list.service.MultiChoiceService");
        Log.i(TAG, "[onResume],isServiceWork = " + isServiceWork);
        if(isServiceWork){
            android.provider.Settings.System.putInt(getContentResolver(), PRIZE_DISABLE_NAVIGATION ,1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "[onPause]");
        android.provider.Settings.System.putInt(getContentResolver(), PRIZE_DISABLE_NAVIGATION ,0);
    }

    private boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }
    /*prize add for bug58893 by zhaojian 20180525 end*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "[onDestroy]");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "[onSaveInstanceState]mActionCode = " + mActionCode + ",mNumberBalance = "
                + mNumberBalance);
        outState.putInt(KEY_ACTION_CODE, mActionCode);
        // for ct new feature
        outState.putInt("NUMBER_BALANCE", mNumberBalance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/*prize-add-huangliemin-2016-7-27-start*/
    	if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS ||
    			mActionCode == ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS ||
    			mActionCode == ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS) { //prize-change-huangliemin-2016-7-28
    		return false;
    	}
    	/*prize-add-huangliemin-2016-7-27-end*/
        super.onCreateOptionsMenu(menu);
        ExtensionManager.getInstance().getContactsPickerExtension()
                .addSearchMenu(ContactListMultiChoiceActivity.this, menu);

       /* MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mtk_list_multichoice, menu);

        MenuItem optionItem = menu.findItem(R.id.search_menu_item);
        optionItem.setTitle(R.string.menu_search);*/

        return true;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "[onClick]v= " + v);
        final int resId = v.getId();
        switch (resId) {
        case R.id.search_menu_item:
            Log.i(TAG, "[onClick]resId = search_menu_item ");
            mListFragment.updateSelectedItemsView();
            showActionBar(SelectionMode.SearchMode);
            closeOptionsMenu();
            break;

        case R.id.menu_option:
            Log.i(TAG, "[onClick]resId = menu_option ");
            if (mListFragment instanceof MultiDuplicationPickerFragment) {
                Log.d(TAG, "[onClick]Send result for copy action");
                setResult(ContactImportExportActivity.RESULT_CODE);
            }
            if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                PhoneAndEmailsPickerFragment fragment =
                        (PhoneAndEmailsPickerFragment) mListFragment;
                fragment.setNumberBalance(mNumberBalance);
                fragment.onOptionAction();
            } else {
                mListFragment.onOptionAction();
            }
            break;
            /* PRIZE-add button function -zhangzhonghao-2015-5-20 -start */
            case R.id.prize_bottom_ok:
                if (mListFragment instanceof MultiDuplicationPickerFragment) {
                    Log.d(TAG, "Send result for copy action");
                    setResult(ContactImportExportActivity.RESULT_CODE);
                }
                if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                    PhoneAndEmailsPickerFragment fragment = (PhoneAndEmailsPickerFragment) mListFragment;
                    fragment.setNumberBalance(mNumberBalance);
                    fragment.onOptionAction();
                } else {
                    mListFragment.onOptionAction();
                }
                break;

            /* PRIZE-add button function -zhangzhonghao-2015-5-20 -end */
        case R.id.select_items:
            Log.i(TAG, "[onClick]resId = select_items ");
            // if the Window of this Activity hasn't been created,
            // don't show Popup. because there is no any window to attach .
            if (getWindow() == null) {
                Log.w(TAG, "[onClick]current Activity dinsow is null");
                return;
            }
            if (mSelectionMenu == null || !mSelectionMenu.isShown()) {
                View parent = (View) v.getParent();
                mSelectionMenu = updateSelectionMenu(parent);
                mSelectionMenu.show();
            } else {
                Log.w(TAG, "[onClick]mSelectionMenu is already showing, ignore this click");
            }
            break;

        default:
            break;
        }
        return;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        Log.i(TAG, "[onMenuItemSelected]itemId = " + itemId);
        // if click the search menu, into the SearchMode and disable the search
        // menu
        if (itemId == R.id.search_menu_item) {
            mListFragment.updateSelectedItemsView();
            mIsSelectedNone = mListFragment.isSelectedNone();
            showActionBar(SelectionMode.SearchMode);
            item.setVisible(false);
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * Creates the fragment based on the current request.
     */
    private void configureListFragment() {
        if (mActionCode == mRequest.getActionCode()) {
            Log.w(TAG, "[configureListFragment]return ,mActionCode = " + mActionCode);
            return;
        }

        Bundle bundle = new Bundle();
        mActionCode = mRequest.getActionCode();
        Log.i(TAG, "[configureListFragment] action code is " + mActionCode);

        switch (mActionCode) {
        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS://Mms,Rcse,Email
        	Log.d(TAG,"[configureListFragment]switch1");
            mListFragment = new MultiBasePickerFragment();
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_VCARD_PICKER://Email
                	Log.d(TAG,"[configureListFragment]switch2");
            mListFragment = new MultiVCardPickerFragment();
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_CONTACTS
                | ContactsIntentResolverEx.MODE_MASK_IMPORT_EXPORT_PICKER:
                	Log.d(TAG,"[configureListFragment]switch3");
            mListFragment = new MultiDuplicationPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_EMAILS:
            mListFragment = new EmailsPickerFragment();
            Log.d(TAG,"[configureListFragment]switch4");
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONES:
            mListFragment = new PhoneNumbersPickerFragment();
            //M:Op01 Rcs get intent data for filter @{
            ExtensionManager.getInstance().getRcsExtension().
                    getIntentData(getIntent(), mListFragment);
            /** @} */
            Log.d(TAG,"[configureListFragment]switch5");
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_DATAS:
            mListFragment = new DataItemsPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            Log.d(TAG,"[configureListFragment]switch6");
            break;

        case ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS:
            mListFragment = new MultiDeletionPickerFragment();
            Log.d(TAG,"[configureListFragment]switch7");
            break;

        case ContactsRequestAction.ACTION_GROUP_MOVE_MULTIPLE_CONTACTS:
            mListFragment = new MultiGroupPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            Log.d(TAG,"[configureListFragment]switch8");
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS:
            mListFragment = new PhoneAndEmailsPickerFragment();
            Log.d(TAG,"[configureListFragment]switch9");
            break;

        case ContactsRequestAction.ACTION_SHARE_MULTIPLE_CONTACTS:
            mListFragment = new MultiSharePickerFragment();
            Log.d(TAG,"[configureListFragment]switch10");
            break;

        case ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS:
            mListFragment = new MultiGroupAddPickerFragment();
            bundle.putParcelable(MultiBasePickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            Log.d(TAG,"[configureListFragment]switch11");
            break;

        case ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONE_IMS_SIP_CALLS:
            mListFragment = new ConferenceCallsPickerFragment();
            bundle.putParcelable(ConferenceCallsPickerFragment.FRAGMENT_ARGS, getIntent());
            mListFragment.setArguments(bundle);
            Log.d(TAG,"[configureListFragment]switch12");
            break;

        default:
        	Log.d(TAG,"[configureListFragment]switch13");
            throw new IllegalStateException("Invalid action code: " + mActionCode);
        }

        mListFragment.setLegacyCompatibilityMode(mRequest.isLegacyCompatibilityMode());
        mListFragment.setQueryString(mRequest.getQueryString(), false);
        mListFragment.setDirectoryResultLimit(DEFAULT_DIRECTORY_RESULT_LIMIT);
        mListFragment.setVisibleScrollbarEnabled(true);

        getFragmentManager().beginTransaction().replace(R.id.list_container, mListFragment)
                .commitAllowingStateLoss();
    }

    public void startActivityAndForwardResult(final Intent intent) {
        Log.i(TAG, "[startActivityAndForwardResult]intent = " + intent);
        intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

        // Forward extras to the new activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intent.putExtras(extras);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mListFragment.startSearch(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onClose() {
        if (mSearchView == null) {
            return false;
        }
        if (!TextUtils.isEmpty(mSearchView.getQuery())) {
            mSearchView.setQuery(null, true);
        }
        showActionBar(SelectionMode.ListMode);
        mListFragment.updateSelectedItemsView();
        return true;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.search_view) {
            if (hasFocus) {
                showInputMethod(mSearchView.findFocus());
            }
        }
        /*prize-add-huangliemin-2016-7-27-start*/
        if(view.getId() == R.id.search_view_edit) {
        	if(hasFocus && mSearchViewEditText!=null && isSeachMode) {
        		showInputMethod(mSearchViewEditText.findFocus());
        	}
        }
        /*prize-add-huangliemin-2016-7-27-end*/
    }

    private void showInputMethod(View view) {
        final InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if (!imm.showSoftInput(view, 0)) {
                Log.w(TAG, "Failed to show soft input method.");
            }
        }
    }

    public void returnPickerResult(Uri data) {
        Intent intent = new Intent();
        intent.setData(data);
        returnPickerResult(intent);
    }

    public void returnPickerResult(Intent intent) {
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "[onActivityResult]requestCode = " + requestCode + ",resultCode = "
                + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBACTIVITY_ADD_TO_EXISTING_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    startActivity(data);
                }
                finish();
            }
        }

        if (resultCode == ContactImportExportActivity.RESULT_CODE) {
            finish();
        }

        if (resultCode == CONTACTGROUPLISTACTIVITY_RESULT_CODE) {
            long[] ids = data.getLongArrayExtra("checkedids");
            if (mListFragment instanceof PhoneAndEmailsPickerFragment) {
                PhoneAndEmailsPickerFragment fragment =
                        (PhoneAndEmailsPickerFragment) mListFragment;
                fragment.markItemsAsSelectedForCheckedGroups(ids);
            }
            // M:OP01 RCS will mark item for selected group in phone numbers
            // list@{
            ExtensionManager.getInstance().getRcsExtension().getGroupListResult(mListFragment, ids);
            /** @} */
        }

    }

    public void onBackPressed() {
        Log.i(TAG, "[onBackPressed]");
        /*prize-change-huangliemin-2016-7-27-start*/
        /*
        if (mSearchView != null && !mSearchView.isFocused()) {
            if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                mSearchView.setQuery(null, true);
            }
            showActionBar(SelectionMode.ListMode);
            mListFragment.updateSelectedItemsView();
            return;
        }
        */
        if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS ||
        		mActionCode == ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS ||
        		mActionCode == ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS) { //prize-change-huangliemin-2016-7-28
        	if(mSearchViewEditText!=null && isSeachMode) {
        		if(!TextUtils.isEmpty(mSearchViewEditText.getText().toString())) {
        			mSearchViewEditText.setText(null);
        		}
        		showActionBar(SelectionMode.ListMode);
        		mListFragment.updateSelectedItemsView();
        		return;
        	}
        } else {
        	if (mSearchView != null && !mSearchView.isFocused()) {
            	if (!TextUtils.isEmpty(mSearchView.getQuery())) {
                	mSearchView.setQuery(null, true);
            	}
            	showActionBar(SelectionMode.ListMode);
            	mListFragment.updateSelectedItemsView();
            	return;
        	}
        }
        /*prize-change-huangliemin-2016-7-27-end*/
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "[onConfigurationChanged] newConfig:" + newConfig);
        super.onConfigurationChanged(newConfig);
        // do nothing
    }

    private void showActionBar(SelectionMode mode) {
        Log.d(TAG, "[showActionBar]mode = " + mode + "mActionCode = " + mActionCode);
        ActionBar actionBar = getActionBar();
        /*prize-change-huangliemin-2016-7-27-start*/
        if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS ||
        		mActionCode == ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS ||
        		mActionCode == ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS) {//prize-change-huangliemin-2016-7-28
        	switch(mode) {
                case ListMode:
                    Log.d(TAG, "[showActionBar] match ActionCode / list mode ");
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    actionBar.setDisplayShowCustomEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayUseLogoEnabled(false);
                    actionBar.setCustomView(R.layout.prize_custom_delete_contacts_actionbar);
                    TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
                    if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS) {
                        TitleText.setText(R.string.prize_delete_contact);
                    } else {
                        TitleText.setText(R.string.contactPickerActivityTitle);
                    }
                    View BackButton = actionBar.getCustomView().findViewById(R.id.back_container);
                    BackButton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                    SelectAllButton = (TextView)actionBar.getCustomView().findViewById(R.id.select_all);
                    SelectAllButtonContainer = actionBar.getCustomView().findViewById(R.id.select_all_container);//prize-add for dido os8.0-hpf-2017-8-21
                    SelectAllButtonContainer.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            updateSelectButton();
                        }
                    });
                    isSeachMode = false;
                    mSearchLayout.setVisibility(View.VISIBLE);
                    break;

                case SearchMode:
                    Log.d(TAG, "[showActionBar] match ActionCode / search mode ");
                    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                    actionBar.setDisplayShowCustomEnabled(true);
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayUseLogoEnabled(false);
                    actionBar.setCustomView(R.layout.prize_search_bar_expanded);
                    ImageButton back_button = (ImageButton)actionBar.getCustomView().findViewById(R.id.search_back_button);
                    back_button.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                    actionBar.getCustomView().setVisibility(View.VISIBLE);
                    mSearchViewEditText = (EditText)actionBar.getCustomView().findViewById(R.id.search_view_edit);
                    mSearchViewEditText.setHint(R.string.prize_search_hint);
                    mSearchViewEditText.addTextChangedListener(new SearchTextWatcher());
                    mSearchViewEditText.setOnFocusChangeListener(this);
                    isSeachMode = true;
                    mSearchViewEditText.requestFocus();
                    mSearchLayout.setVisibility(View.GONE);
                    break;
        	}
        	if(mActionCode == ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS ||
        			mActionCode == ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS) {
        		prize_bottom_button.setVisibility(View.VISIBLE);
        	} else {
        		mBottomLayout.setVisibility(View.VISIBLE);
        	}
        	return;
        }
        /*prize-change-huangliemin-2016-7-27-end*/
        switch (mode) {
        case SearchMode:
        	Log.d(TAG, "[showActionBar] SearchMode");
            mIsSearchMode = true;
            invalidateOptionsMenu();
            final View searchViewContainer = LayoutInflater.from(actionBar.getThemedContext())
                    .inflate(R.layout.mtk_multichoice_custom_action_bar, null);
            // in SearchMode,disable the doneMenu and selectView.
            Button selectView = (Button) searchViewContainer.findViewById(R.id.select_items);
            selectView.setVisibility(View.GONE);

            mSearchView = (SearchView) searchViewContainer.findViewById(R.id.search_view);
            mSearchView.setVisibility(View.VISIBLE);
            mSearchView.setIconifiedByDefault(true);
            mSearchView.setQueryHint(getString(R.string.hint_findContacts));
            mSearchView.setIconified(false);
            mSearchView.setOnQueryTextListener(this);
            mSearchView.setOnCloseListener(this);
            mSearchView.setOnQueryTextFocusChangeListener(this);

            // when no Query String,do not display the "X"
            mSearchView.onActionViewExpanded();

            actionBar.setCustomView(searchViewContainer, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            // display the "OK" button.
            Button optionView = (Button) searchViewContainer.findViewById(R.id.menu_option);
            optionView.setTypeface(Typeface.DEFAULT_BOLD);
            if (mIsSelectedNone) {
                // if there is no item selected, the "OK" button is disable.
                optionView.setEnabled(false);
                optionView.setTextColor(Color.LTGRAY);
            } else {
                optionView.setEnabled(true);
                optionView.setTextColor(Color.WHITE);
            }
            optionView.setOnClickListener(this);
            /* PRIZE-change actionbar item -zhangzhonghao-2015-5-20 -start */
            optionView.setVisibility(View.GONE);
            prize_bottom_button.setVisibility(View.VISIBLE);
            getActionBar().setTitle(null);
            /* PRIZE-change actionbar item -zhangzhonghao-2015-5-20 -start */
            break;

        case ListMode:
        	Log.d(TAG, "[showActionBar] ListMode");
            mIsSearchMode = false;
            /*prize-remove-hpf-2017-12-5-start*/
            /*invalidateOptionsMenu();
            // Inflate a custom action bar that contains the "done" button for
            // multi-choice
            LayoutInflater inflater =
                    (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.mtk_multichoice_custom_action_bar,
                    null);
            // in the listMode,disable the SearchView
            mSearchView = (SearchView) customActionBarView.findViewById(R.id.search_view);
            mSearchView.setVisibility(View.GONE);

            // set dropDown menu on selectItems.
            Button selectItems = (Button) customActionBarView.findViewById(R.id.select_items);
            selectItems.setOnClickListener(this);

            Button menuOption = (Button) customActionBarView.findViewById(R.id.menu_option);
            menuOption.setTypeface(Typeface.DEFAULT_BOLD);
            String optionText = menuOption.getText().toString();
            menuOption.setOnClickListener(this);

            // Show the custom action bar but hide the home icon and title
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_SHOW_CUSTOM
                    | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setCustomView(customActionBarView);
            // in onBackPressed() used. If mSearchView is null,return prePage.
            mSearchView = null;*/
            /*prize-remove-hpf-2017-12-5-end*/
            /*prize-add-hpf-2017-12-5-start*/
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setCustomView(R.layout.prize_custom_delete_contacts_actionbar);
            TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
            if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS) {
                TitleText.setText(R.string.prize_delete_contact);
            } else {
                TitleText.setText(R.string.contactPickerActivityTitle);
            }
            View BackButton = actionBar.getCustomView().findViewById(R.id.back_container);
            BackButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            SelectAllButton = (TextView)actionBar.getCustomView().findViewById(R.id.select_all);
            SelectAllButtonContainer = actionBar.getCustomView().findViewById(R.id.select_all_container);//prize-add for dido os8.0-hpf-2017-8-21
            SelectAllButtonContainer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateSelectButton();
                }
            });
            mSearchLayout.setVisibility(View.GONE);
            prize_bottom_button.setVisibility(View.VISIBLE);
            /*prize-add-hpf-2017-12-5-end*/
            break;

        default:
            break;
        }
    }
    
    /*prize-add-huangliemin-2016-7-27-start*/
    private class SearchTextWatcher implements TextWatcher {

		
		@Override
		public void beforeTextChanged(CharSequence queryString, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		
		@Override
		public void onTextChanged(CharSequence queryString, int start, int before,
				int count) {
			// TODO Auto-generated method stub
			mListFragment.startSearch(""+queryString);
			
		}

		
		@Override
		public void afterTextChanged(Editable queryString) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    public void updateSelectButton() {
    	mListFragment.updateSelectedItemsView();
    	mIsSelectedAll = mListFragment.isSelectedAll();
    	if(!mIsSelectedAll) {
    		//SelectAllButton.setText(R.string.prize_unselect_all);//prize-remove-huangpengfei-2016-11-22
    		mListFragment.onSelectAll();
    	} else {
    		//SelectAllButton.setText(R.string.menu_select_all);//prize-remove-huangpengfei-2016-11-22
    		mListFragment.onClearSelect();
    	}
    }
    /*prize-add-huangliemin-2016-7-27-end*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "[onOptionsItemSelected]");
        if (item.getItemId() == android.R.id.home) {
            hideSoftKeyboard(mSearchView);
            // Fix CR:ALPS01945610
            if (isResumed()) {
                onBackPressed();
            }
            return true;
        }
        if (item.getItemId() == R.id.groups) {
            startActivityForResult(new Intent(ContactListMultiChoiceActivity.this,
                    ContactGroupListActivity.class), CONTACTGROUPLISTACTIVITY_RESULT_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * add dropDown menu on the selectItems.The menu is "Select all" or
     * "Deselect all"
     *
     * @param customActionBarView
     * @return The updated DropDownMenu
     */
    private DropDownMenu updateSelectionMenu(View customActionBarView) {
        DropMenu dropMenu = new DropMenu(this);
        // new and add a menu.
        DropDownMenu selectionMenu = dropMenu.addDropDownMenu(
                (Button) customActionBarView.findViewById(R.id.select_items), R.menu.mtk_selection);

        Button selectView = (Button) customActionBarView.findViewById(R.id.select_items);
        // when click the selectView button, display the dropDown menu.
        selectView.setOnClickListener(this);
        MenuItem item = selectionMenu.findItem(R.id.action_select_all);

        // get mIsSelectedAll from fragment.
        mListFragment.updateSelectedItemsView();
        mIsSelectedAll = mListFragment.isSelectedAll();
        // if select all items, the menu is "Deselect all"; else the menu is
        // "Select all".
        if (mIsSelectedAll) {
            // dropDown menu title is "Deselect all".
            item.setTitle(R.string.menu_select_none);
            // click the menu, deselect all items
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    showActionBar(SelectionMode.ListMode);
                    // clear select all items
                    mListFragment.onClearSelect();
                    return false;
                }
            });
        } else {
            // dropDown Menu title is "Select all"
            item.setTitle(R.string.menu_select_all);
            // click the menu, select all items.
            dropMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    showActionBar(SelectionMode.ListMode);
                    // select all of itmes
                    mListFragment.onSelectAll();
                    return false;
                }
            });
        }
        return selectionMenu;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	/*prize-add-huangliemin-2016-7-27-start*/
    	if(mActionCode == ContactsRequestAction.ACTION_DELETE_MULTIPLE_CONTACTS ||
    			mActionCode == ContactsRequestAction.ACTION_PICK_MULTIPLE_PHONEANDEMAILS ||
    			mActionCode == ContactsRequestAction.ACTION_GROUP_ADD_MULTIPLE_CONTACTS) { //prize-change-huangliemin-2016-7-28
    		return false;
    	}
    	/*prize-add-huangliemin-2016-7-27-end*/

        ExtensionManager.getInstance().getOp01Extension().
                addGroupMenu(this, menu, mListFragment);

  /*
        MenuItem menuItem = menu.findItem(R.id.search_menu_item);
        if (mIsSearchMode) {
            // if SearchMode, search Menu is disable.
            menuItem.setVisible(false);
            return false;
        } else {
            // if ListMode, search Menu is display.
            menuItem.setVisible(true);
            if (mListFragment instanceof MultiPhoneAndEmailsPickerFragment) {
                MenuItem groupsItem = menu.findItem(R.id.groups);
                groupsItem.setVisible(true);
            }
            //M:OP01 RCS will add menu item in list@{
            ExtensionManager.getInstance().getRcsExtension().
                    addListMenuOptions(this, menu, menuItem, mListFragment);
            return super.onPrepareOptionsMenu(menu);
        }

        */
        //M:OP01 RCS will add menu item in list@{
        ExtensionManager.getInstance().getRcsExtension().
                addListMenuOptions(this, menu, null, mListFragment);
        /** @} */
        return ExtensionManager.getInstance().getContactsPickerExtension()
                .enableDisableSearchMenu(mIsSearchMode, menu);
    }

    private void hideSoftKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
