package com.android.contacts.preference;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.contacts.R;

import com.mediatek.contacts.activities.ContactImportExportActivity;
import com.mediatek.contacts.list.ContactsIntentResolverEx;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.android.contacts.common.model.account.AccountWithDataSet;
import com.android.contacts.common.model.AccountTypeManager;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.android.contacts.common.model.account.AccountType;
import com.mediatek.storage.StorageManagerEx;
import android.os.storage.StorageVolume;
import android.os.UserHandle;
import com.android.contacts.common.util.AccountSelectionUtil;
import com.android.contacts.common.vcard.VCardCommonArguments;
import com.mediatek.contacts.util.ContactsIntent;
import com.android.contacts.common.model.AccountTypeManager;
import com.mediatek.contacts.util.AccountTypeUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;

public class PrizeContactsManagerPreferenceActivity extends PreferenceActivity {
	
	private final String IMPORT_FROM_SD_CARD_KEY = "import_from_sd_card";
	//private final String IMPORT_FROM_OTHER_PHONE_KEY = "import_from_other_phone";
	private final String EXPORT_CONTACTS_KEY = "export_contacts";
	private final String IMPORT_FROM_SIM_KEY = "import_form_sim";
	private static final String TAG = "PrizeContactsManagerPreferenceActivity";
	
	/*prize-add-huangliemin-for-import-and-export-huangliemin-2016-7-11-start*/
	private static final String STORAGE_ACCOUNT_TYPE = "_STORAGE_ACCOUNT";
	ArrayList<AccountWithDataSet> items = new ArrayList<AccountWithDataSet>();
	ArrayList<AccountWithDataSet> items2 = new ArrayList<AccountWithDataSet>();
	private AccountWithDataSet mAccount1 = null;
	private AccountWithDataSet mAccount2 = null;
	/*prize-add-huangliemin-for-import-and-export-huangliemin-2016-7-11-end*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prize_preferencce_contacts_manager_options);
		//add by wangyunhe 20160805 statrt
		View listView = findViewById(android.R.id.list);
        if(null != listView)
        {
            listView.setPadding( 0, listView.getPaddingTop(), 0, listView.getPaddingBottom());
            getListView().setDivider(getDrawable(R.drawable.list_divider));
            getListView().setDividerHeight(1);
        }
	    //add by wangyunhe 20160805 end
		ActionBar actionBar = getActionBar();
		if(actionBar != null) {
			/*prize-change-huangliemin-2016-7-22-start*/
			//actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP);
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        	actionBar.setDisplayShowCustomEnabled(true);
        	actionBar.setDisplayHomeAsUpEnabled(false);
        	actionBar.setDisplayShowTitleEnabled(false);
        	actionBar.setDisplayUseLogoEnabled(false);
        	actionBar.setCustomView(R.layout.prize_custom_center_actionbar_2016_7_21);
        	TextView TitleText = (TextView)actionBar.getCustomView().findViewById(R.id.title);
        	TitleText.setText(R.string.prize_contacts_manager_title);
        	ImageButton BackButton = (ImageButton)actionBar.getCustomView().findViewById(R.id.back_button);
        	BackButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					onBackPressed();
				}
			});
			/*prize-change-huangliemin-2016-7-22-end*/
			
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
			case android.R.id.home: {
			    onBackPressed();
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onPreferenceTreeClick(android.preference.PreferenceScreen, android.preference.Preference)
	 */
	@Override
	@Deprecated
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		String key_str = preference.getKey();
		List<AccountWithDataSetEx> accountslist = loadAccountFilters();
		Dialog mDialog = null;
		switch(key_str){
			case IMPORT_FROM_SD_CARD_KEY:
				doImportOrExportSDCard(true);
				
				break;
			/*
			case IMPORT_FROM_OTHER_PHONE_KEY:
				break;
			*/
			
			case IMPORT_FROM_SIM_KEY:
				String titleSelectSIM = getString(R.string.prize_select_sim);
				mDialog = getSelectAccount1Dialog(accountslist,titleSelectSIM);
				if(mDialog!=null) {
				   mDialog.show();
				} else {
					Toast.makeText(this, R.string.prize_sim_invalid, Toast.LENGTH_LONG).show();
				}
				break;
				
				
			case EXPORT_CONTACTS_KEY:
				//doImportOrExportSDCard(false);
				accountslist.addAll(getStorageAccounts());
				/*PRIZE-add -yuandailin-2016-8-8-start*/
				StorageManager storageManager = (StorageManager) getApplicationContext().getSystemService(STORAGE_SERVICE);
				StorageVolume[] storageVolumeList = storageManager.getVolumeList();
				String titleSelectLocation = getString(R.string.prize_contacts_storage_location);
				mDialog = getSelectOtherAccountDialog(accountslist,storageVolumeList,titleSelectLocation);
				/*PRIZE-add -yuandailin-2016-8-8-end*/
				if(mDialog!=null) {
					mDialog.show();
				} else {
					Toast.makeText(this, R.string.prize_no_other_account, Toast.LENGTH_LONG).show();
				}
				break;
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	//prize-add-huangliemin-for-import-and-export-in2016-7-11-start
	private void CheckSimReady(AccountWithDataSet account) {
		if(AccountTypeUtils.isAccountTypeIccCard(account.type)) {
			int subId = ((AccountWithDataSetEx)account).getSubId();
			if(!SimCardUtils.isPhoneBookReady(subId)) {
				Toast.makeText(this, R.string.icc_phone_book_invalid, Toast.LENGTH_LONG).show();
				finish();
				Log.i(TAG, "[doImportExport] phb is not ready");
			}
		}
	}
	
	private void doImportOrExportSDCard(boolean isImprot) {
		List<AccountWithDataSetEx> accountslist = loadAccountFilters();
		accountslist.addAll(getStorageAccounts());
		AccountWithDataSetEx StorageAccount = null;
		AccountWithDataSetEx PhoneAccount = null;
		
		for(int i=0;i<accountslist.size();i++) {
			if(isStorageAccount(accountslist.get(i))) {
				if(!checkSDCardAvaliable(accountslist.get(i).dataSet)) {
					new AlertDialog.Builder(this).setMessage(R.string.no_sdcard_message)
					.setTitle(R.string.no_sdcard_title)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            
                        }
                    }).show();
				} else {
					StorageAccount = accountslist.get(i);
				}
			} else if(AccountWithDataSetEx.isLocalPhone(accountslist.get(i).type)) {
				PhoneAccount = accountslist.get(i);
			}
		}
		
		if(StorageAccount !=null && PhoneAccount !=null) {
			if(isImprot) {
			    AccountSelectionUtil.doImportFromSdCard(this, StorageAccount.dataSet,
					    PhoneAccount);
			} else {
				if(isSDCardFull(StorageAccount.dataSet)) {
					Log.i(TAG, "[handleImportExportAction] isSDCardFull");
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.storage_full)
                            .setTitle(R.string.storage_full)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            
                                        }
                                    }).show();
				} else {
					Intent intent = new Intent(this,
	                        com.mediatek.contacts.list.ContactListMultiChoiceActivity.class)
	                        .setAction(ContactsIntent.LIST.ACTION_PICK_MULTI_CONTACTS)
	                        .putExtra("request_type",
	                                ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER)
	                        .putExtra("toSDCard", true).putExtra("fromaccount", PhoneAccount)
	                        .putExtra("toaccount", StorageAccount)
	                        .putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY, PrizeContactsManagerPreferenceActivity.class.getName());
	                startActivityForResult(intent, ContactImportExportActivity.REQUEST_CODE);
				}
			}
		}
		
	}
	
	private boolean isSDCardFull(final String path) {
        if (TextUtils.isEmpty(path)) {
            Log.w(TAG, "[isSDCardFull]path is null!");
            return false;
        }
        Log.d(TAG, "[isSDCardFull] storage path is " + path);
        if (checkSDCardAvaliable(path)) {
            StatFs sf = null;
            try {
                sf = new StatFs(path);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "[isSDCardFull]catch exception:");
                e.printStackTrace();
                return false;
            }
            long availCount = sf.getAvailableBlocks();
            return !(availCount > 0);
        }

        return true;
    }
	
	private List<AccountWithDataSetEx> loadAccountFilters() {
		List<AccountWithDataSetEx> accountsEx = new ArrayList<AccountWithDataSetEx>();
        final AccountTypeManager accountTypes = AccountTypeManager.getInstance(this);
        List<AccountWithDataSet> accounts = accountTypes.getAccounts(true);
        
        for (AccountWithDataSet account : accounts) {
            AccountType accountType = accountTypes.getAccountType(account.type, account.dataSet);
            Log.d(TAG, "[loadAccountFilters]account.type = " + account.type
                    + ",account.name =" + account.name);
            if (accountType.isExtension() && !account.hasData(this)) {
                Log.d(TAG, "[loadAccountFilters]continue.");
                // Hide extensions with no raw_contacts.
                continue;
            }
            int subId = SubInfoUtils.getInvalidSubId();
            if (account instanceof AccountWithDataSetEx) {
                subId = ((AccountWithDataSetEx) account).getSubId();
            }
            Log.d(TAG, "[loadAccountFilters]subId = " + subId);
            accountsEx.add(new AccountWithDataSetEx(account.name, account.type, subId));
        }
        
        //accountsEx.addAll(getStorageAccounts());

        return accountsEx;
        
	}
	
	private boolean isStorageAccount(Account account) {
		if(account!=null) {
			return STORAGE_ACCOUNT_TYPE.equalsIgnoreCase(account.type);
		}
		return false;
	}
	
	private boolean checkSDCardAvaliable(String path) {
		if (TextUtils.isEmpty(path)) {
            Log.w(TAG, "[checkSDCardAvaliable]path is null!");
            return false;
        }
        StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
            Log.d(TAG, "-----story manager is null----");
             return false;
        }
        String storageState = storageManager.getVolumeState(path);
        Log.d(TAG, "[checkSDCardAvaliable]path = " + path + ",storageState = " + storageState);
        return storageState.equals(Environment.MEDIA_MOUNTED);
	}
	
	public List<AccountWithDataSetEx> getStorageAccounts() {
        List<AccountWithDataSetEx> storageAccounts = new ArrayList<AccountWithDataSetEx>();
        StorageManager storageManager = (StorageManager) getApplicationContext().getSystemService(
                STORAGE_SERVICE);
        if (null == storageManager) {
            Log.w(TAG, "[getStorageAccounts]storageManager is null!");
            return storageAccounts;
        }
        String defaultStoragePath = StorageManagerEx.getDefaultPath();
        if (!storageManager.getVolumeState(defaultStoragePath).equals(Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "[getStorageAccounts]State is  not MEDIA_MOUNTED!");
            return storageAccounts;
        }

        // change for ALPS02390380, different user can use different storage, so change the API
        // to user related API.
        StorageVolume volumes[] = StorageManager.getVolumeList(UserHandle.myUserId(),
                StorageManager.FLAG_FOR_WRITE);
        if (volumes != null) {
            Log.d(TAG, "[getStorageAccounts]volumes are: " + volumes);
            for (StorageVolume volume : volumes) {
                String path = volume.getPath();
                //if (!Environment.MEDIA_MOUNTED.equals(path)) {
                //        continue;
               // }
                storageAccounts.add(new AccountWithDataSetEx(volume.getDescription(this),
                        STORAGE_ACCOUNT_TYPE, path));
            }
        }
        return storageAccounts;
    }
	
	private Dialog getSelectAccount1Dialog(List<AccountWithDataSetEx> list,String title) {
		Log.d(TAG,"[getSelectAccount1Dialog]");
		DialogInterface.OnClickListener listener = new AccountTypeSelectedListener(this, 0);
		AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(title)
		.setPositiveButton(android.R.string.ok,listener)
		.setNegativeButton(android.R.string.cancel, null);
		
		items.clear();
		for(int i=0;i<list.size();i++) {
			AccountWithDataSet account = list.get(i);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			if(accountType.isIccCardAccount()) {
				items.add(account/*getAccountName(account,accountType)*/);
			}
		}
		String[] itemsChoices = new String[items.size()];
		for(int j=0;j<items.size();j++) {
			AccountWithDataSet account = items.get(j);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			itemsChoices[j] = getAccountName(account,accountType);
		}
		builder.setSingleChoiceItems(itemsChoices, 0, listener);
		if(itemsChoices.length > 0) {
			return builder.create();
		}
		return null;
	}
	
	private Dialog getSelectAccount2Dialog(List<AccountWithDataSetEx> list) {
		Log.d(TAG,"[getSelectAccount2Dialog]");
		DialogInterface.OnClickListener listener = new AccountTypeSelectedListener(this, 1);
		AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setPositiveButton(android.R.string.ok,listener)
		.setNegativeButton(android.R.string.cancel, null);
		
		items2.clear();
		for(int i=0;i<list.size();i++) {
			AccountWithDataSet account = list.get(i);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			if(!account.equals(mAccount1)) {
				items2.add(account);
			}
		}
		String[] itemsChoices = new String[items2.size()];
		for(int j=0;j<items2.size();j++) {
			AccountWithDataSet account = items2.get(j);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			itemsChoices[j] = getAccountName(account,accountType);
		}
		builder.setSingleChoiceItems(itemsChoices, 0, listener);
		if(itemsChoices.length > 0) {
			return builder.create();
		}
		return null;
	}
	
	private Dialog getSelectOtherAccountDialog(List<AccountWithDataSetEx> list,StorageVolume[] storageVolumeList,String title) {
		Log.d(TAG,"[getSelectOtherAccountDialog]");
		DialogInterface.OnClickListener listener = new AccountTypeSelectedListener(this, 2);
		AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(this);
		AlertDialog.Builder builder = new AlertDialog.Builder(this)
		.setTitle(title)
		.setPositiveButton(android.R.string.ok,listener)
		.setNegativeButton(android.R.string.cancel, null);
		
		items.clear();
		for(int i=0;i<list.size();i++) {
			AccountWithDataSet account = list.get(i);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			if(!AccountWithDataSetEx.isLocalPhone(account.type)) {
				items.add(account);
			} else {
				mAccount1 = account;
			}
		}
		String[] itemsChoices = new String[items.size()];
		for(int j=0;j<items.size();j++) {
			AccountWithDataSet account = items.get(j);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			if(isStorageAccount(account)) {	
				/*PRIZE-change -yuandailin-2016-8-10-start*/
				int length = storageVolumeList.length;
				if(length>1 && storageVolumeList[1].isRemovable() && j==2){
				   itemsChoices[j] = getResources().getString(R.string.prize_outside_sd_card);
				}else {
				   itemsChoices[j] = getResources().getString(R.string.prize_sd_card);
				}
				/*PRIZE-change -yuandailin-2016-8-10-end*/
			} else {
				itemsChoices[j] = getAccountName(account,accountType);
			}
		}
		builder.setSingleChoiceItems(itemsChoices, 0, listener);
		if(itemsChoices.length > 0) {
			return builder.create();
		}
		return null;
	}
	
	private String getAccountName(AccountWithDataSet account, AccountType accountType) {
		int subId = -1;
		if(account instanceof AccountWithDataSetEx) {
			subId = ((AccountWithDataSetEx)account).getSubId();
			String displayName = ((AccountWithDataSetEx)account).getDisplayName();
			if(TextUtils.isEmpty(displayName)) {
				displayName = account.name;
			}
			
			int activtedSubInfoCount = SubInfoUtils.getActivatedSubInfoCount();
			if(activtedSubInfoCount >1 ){
				int simindex = SubInfoUtils.getSlotIdUsingSubId(subId)+1;
				if(simindex == 1) {
					displayName = accountType.getDisplayLabel(this)+"1 ("+displayName+")";
				} else if(simindex == 2) {
					displayName = accountType.getDisplayLabel(this)+"2 ("+displayName+")";
				}
				
				/*prize-add-huangliemin-2016-7-27-start*/
				if(AccountWithDataSetEx.isLocalPhone(account.type)) {
					displayName = ""+accountType.getDisplayLabel(this);
				}
				/*prize-add-huangliemin-2016-7-27-end*/
			} else {
				displayName = ""+accountType.getDisplayLabel(this);
			}
			return displayName;
		} else {
			return ""+accountType.getDisplayLabel(this);
		}
	}
	
	private class AccountTypeSelectedListener implements DialogInterface.OnClickListener {
		
		private int mCurrentIndex;
		private int type;
		private Context DialogContext;
		
		public AccountTypeSelectedListener(Context context, int type) {
			this.type = type;
			DialogContext = context;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			Log.i("logtest", "which: "+which);
			if(type == 0) {
				//for account1
				mAccount1 = null;
				if(which == DialogInterface.BUTTON_POSITIVE) {
					if(items!=null) {
						Log.d(TAG,"[AccountTypeSelectedListener]    type == 0    mCurrentIndex="+mCurrentIndex);
						mAccount1 = items.get(mCurrentIndex);
						//CheckSimReady(mAccount1);
						List<AccountWithDataSetEx> accountslist = loadAccountFilters();
						
						/*prize add-for huangpengfei 2016-8-18-start*/
						setItem2Data(accountslist);
						//for account2
						mAccount2 = null;
						
						if(items2!=null) {
							mAccount2 = items2.get(mCurrentIndex);
							//CheckSimReady(mAccount2);
							startExport(DialogContext);
						}
						/*prize add-for huangpengfei 2016-8-18-end*/
						
						
						/*prize remove-for huangpengfei 2016-8-17-start*/
						/*Dialog mDialog = getSelectAccount2Dialog(accountslist);
						if(mDialog != null) {
							mDialog.show(); 
						} else {
							Toast.makeText(DialogContext, R.string.prize_no_other_account, Toast.LENGTH_LONG).show();
						}*/
						/*prize remove-for huangpengfei 2016-8-17-end*/
					}
				} else if(which == DialogInterface.BUTTON_NEGATIVE) {
					finish();
				} else {
					mCurrentIndex = which;
				}
				
			} /*prizr remove-huangpengfei-2016-8-18-start*/
			/*else if(type == 1) {
				//for account2
				mAccount2 = null;
				if(which == DialogInterface.BUTTON_POSITIVE) {
					if(items2!=null) {
						Log.d(TAG,"[AccountTypeSelectedListener]    type == 1    mCurrentIndex="+mCurrentIndex);
						mAccount2 = items2.get(mCurrentIndex);
						//CheckSimReady(mAccount2);
						startExport(DialogContext);
					}
				} else if(which == DialogInterface.BUTTON_NEGATIVE) {
					finish();
				} else {
					mCurrentIndex = which;
				}
			}*/ 
			/*prizr remove-huangpengfei-2016-8-18-end*/
			else if(type == 2) {
				mAccount2 = null;
				if(which == DialogInterface.BUTTON_POSITIVE) {
					if(items!=null) {
						Log.d(TAG,"[AccountTypeSelectedListener]    type == 2    mCurrentIndex="+mCurrentIndex);
						mAccount2 = items.get(mCurrentIndex);
						if(isStorageAccount(mAccount2)) {
							doImportOrExportSDCard(false);
						} else {
							startExport(DialogContext);
						}

					}
				} else if(which == DialogInterface.BUTTON_NEGATIVE) {
					finish();
				} else {
					mCurrentIndex = which;
				}
			}
			
			
		}
		
		private void startExport(Context context) {
			Log.i(TAG, "startExport");
			Intent intent = new Intent(context,
                    com.mediatek.contacts.list.ContactListMultiChoiceActivity.class)
                    .setAction(ContactsIntent.LIST.ACTION_PICK_MULTI_CONTACTS)
                    .putExtra("request_type",
                            ContactsIntentResolverEx.REQ_TYPE_IMPORT_EXPORT_PICKER)
                    .putExtra("toSDCard", false).putExtra("fromaccount", mAccount1)
                    .putExtra("toaccount", mAccount2)
                    .putExtra(VCardCommonArguments.ARG_CALLING_ACTIVITY, PrizeContactsManagerPreferenceActivity.class.getName());
			context.startActivity(intent);
		}
		
	}
	//prize-add-huangliemin-for-import-and-export-in2016-7-11-end
	
	/*prize add-huangpengfei-2016-8-18-start*/
	private void setItem2Data(List<AccountWithDataSetEx> list) {
		AccountTypeManager mAccountTypes = AccountTypeManager.getInstance(this);
		
		items2.clear();
		for(int i=0;i<list.size();i++) {
			AccountWithDataSet account = list.get(i);
			AccountType accountType = mAccountTypes.getAccountType(account.type, account.dataSet);
			if(!account.equals(mAccount1)) {
				items2.add(account);
			}
		}
	}
	
	/*prize add-huangpengfei-2016-8-18-end*/
	
	
}
