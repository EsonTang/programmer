/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.contacts.common.list;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
//import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.contacts.common.R;
import com.android.contacts.common.model.AccountTypeManager;
import com.android.contacts.common.model.account.AccountType;
import com.android.contacts.common.model.account.AccountWithDataSet;

import com.mediatek.contacts.GlobalEnv;
import com.mediatek.contacts.model.AccountWithDataSetEx;
import com.mediatek.contacts.simcontact.SubInfoUtils;
import com.mediatek.contacts.util.ContactsCommonListUtils;
import com.mediatek.contacts.util.Log;
/**
 * Contact list filter parameters.
 */
public class ContactListFilterView extends LinearLayout {

    private static final String TAG = ContactListFilterView.class.getSimpleName();

    private ImageView mIcon;
    private TextView mAccountType;
    private TextView mAccountUserName;
    private TextView mSubTextView; //prize-add-huangliemin-2016-7-26
    private RadioButton mRadioButton;
    private ContactListFilter mFilter;
    private boolean mSingleAccount;

    public ContactListFilterView(Context context) {
        super(context);
    }

    public ContactListFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContactListFilter(ContactListFilter filter) {
        mFilter = filter;
    }

    public ContactListFilter getContactListFilter() {
        return mFilter;
    }

    public void setSingleAccount(boolean flag) {
        this.mSingleAccount = flag;
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        if (mRadioButton != null) {
            mRadioButton.setChecked(activated);
        } else {
            // We're guarding against null-pointer exceptions,
            // but otherwise this code is not expected to work
            // properly if the button hasn't been initialized.
            Log.wtf(TAG, "radio-button cannot be activated because it is null");
        }
        setContentDescription(generateContentDescription());
    }

    public boolean isChecked() {
        return mRadioButton.isChecked();
    }

    public void bindView(AccountTypeManager accountTypes) {
        if (mAccountType == null) {
            mIcon = (ImageView) findViewById(R.id.icon);
            mAccountType = (TextView) findViewById(R.id.accountType);
            mAccountUserName = (TextView) findViewById(R.id.accountUserName);
            mSubTextView= (TextView)findViewById(R.id.subtitle);
            mRadioButton = (RadioButton) findViewById(R.id.radioButton);
            mRadioButton.setChecked(isActivated());
        }

        if (mFilter == null) {
            mAccountType.setText(R.string.contactsList);
            return;
        }

        mAccountUserName.setVisibility(View.GONE);
        switch (mFilter.filterType) {
            case ContactListFilter.FILTER_TYPE_ALL_ACCOUNTS: {
            	//prize-change-huangliemin-2016-7-26
                bindView(0, R.string.prize_show_all_contacts/*list_filter_all_accounts*/);
                mSubTextView.setText(R.string.prize_show_all_sub_text);
                mSubTextView.setVisibility(View.VISIBLE);
                //prize-change-huangliemin-2016-7-26
                break;
            }
            case ContactListFilter.FILTER_TYPE_STARRED: {
                bindView(R.drawable.mtk_ic_menu_star_holo_light, R.string.list_filter_all_starred);
                break;
            }
            case ContactListFilter.FILTER_TYPE_CUSTOM: {
            	//prize-change for dido os8.0-hpf-2017-8-3
                bindView(/*R.drawable.ic_menu_settings_holo_light*/0, R.string.list_filter_customize);
                break;
            }
            case ContactListFilter.FILTER_TYPE_WITH_PHONE_NUMBERS_ONLY: {
                bindView(0, R.string.list_filter_phones);
                break;
            }
            case ContactListFilter.FILTER_TYPE_SINGLE_CONTACT: {
                bindView(0, R.string.list_filter_single);
                break;
            }
            case ContactListFilter.FILTER_TYPE_ACCOUNT: {
                mAccountUserName.setVisibility(View.VISIBLE);
                mIcon.setVisibility(/*View.VISIBLE*/View.GONE);//prize-change-huangliemin-2016-7-26
                if (mFilter.icon != null) {
                    mIcon.setImageDrawable(mFilter.icon);
                } else {
                    mIcon.setImageResource(R.drawable.unknown_source);
                }
                final AccountType accountType =
                        accountTypes.getAccountType(mFilter.accountType, mFilter.dataSet);
                /** M: Change Feature ALPS00406553 @{ */
                if (accountType.isIccCardAccount()) {
                    mAccountUserName.setText(accountType.getDisplayLabel(mContext));
                } else {
                    mAccountUserName.setText(mFilter.accountName);
                }
                /** @} */
                mAccountType.setText(accountType.getDisplayLabel(getContext()));
                /// M: Add SIM Indicator feature for Android M. @{
                if (SubInfoUtils.getActivatedSubInfoCount() == 1 &&
                        accountType.isIccCardAccount()) {
                    mAccountType.setVisibility(View.GONE);
                    /*prize-change-huangliemin-2016-7-26-start*/
                    /*
                    mAccountUserName.setTextAppearance(mContext,
                            android.R.attr.textAppearanceMedium);
                    mAccountUserName.setTextSize(18);
                    */
                    mAccountUserName.setText(R.string.prize_sim_account);
                    /*prize-change-huangliemin-2016-7-26-end*/
                } else {
                    mAccountType.setVisibility(View.VISIBLE);
                }
                /// @}
                /*prize-add-huangliemin-2016-7-26-start*/
                if(SubInfoUtils.getActivatedSubInfoCount() >=0 &&
                        accountType.isIccCardAccount()) {
                	int simIndex = getAccountSimIndex(mFilter.accountType, mFilter.accountName);
                	if(simIndex == 1) {
                		mSubTextView.setText(R.string.prize_sim_1);
                	} else if(simIndex == 2) {
                		mSubTextView.setText(R.string.prize_sim_2);
                	}
                	
                	if(simIndex!=-1) {
                		mSubTextView.setVisibility(View.VISIBLE);
                	}
                	if(SubInfoUtils.getActivatedSubInfoCount() == 2) {
                		mAccountUserName.setVisibility(View.GONE);
                	}
                	
                }
                
                if(AccountWithDataSetEx.isLocalPhone(accountType.accountType)) {
                	mSubTextView.setText(R.string.prize_phone_account_sub_text);
                	mSubTextView.setVisibility(View.VISIBLE);
                }
                /*prize-add-huangliemin-2016-7-26-end*/

                /// M: modify
                ContactsCommonListUtils.setAccountTypeText(getContext(), accountType,
                        mAccountType, mAccountUserName, mFilter);
                break;
            }
        }
        setContentDescription(generateContentDescription());
        
        /* prize-add-fix bug[48154]-hpf-2018-1-13-start */
        if(View.GONE == mAccountType.getVisibility() && View.VISIBLE == mAccountUserName.getVisibility()){
        	mAccountUserName.setTextColor(mContext.getResources().getColor(R.color.prize_content_title_color));
        	mAccountUserName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,mContext.getResources().getDimension(R.dimen.prize_single_content_size));
        }
        /* prize-add-fix bug[48154]-hpf-2018-1-13-end */
    }
    
    
    /*prize-add-huangliemin-2016-7-26-start*/
    private int getAccountSimIndex(String accountType, String accountName) {
    	int simIndex = -1;
    	if(null == accountType || null == accountName) {
    		return -1;
    	}
    	
    	Context context = GlobalEnv.getApplicationContext();
    	if(null == context) {
    		return -1;
    	}
    	
    	List<AccountWithDataSet> accounts = AccountTypeManager.getInstance(context).getAccounts(true);
    	
    	if(null == accounts) {
    		return -1;
    	}
    	
    	for(AccountWithDataSet ads : accounts) {
    		if(ads instanceof AccountWithDataSetEx) {
    			if(accountType.equals(ads.type) && accountName.equals(ads.name)) {
    				simIndex = ((AccountWithDataSetEx)ads).getSubId();
    				simIndex = SubInfoUtils.getSlotIdUsingSubId(simIndex)+1;
    			}
    		}
    	}
    	
    	return simIndex;
    }

    private void bindView(int iconResource, int textResource) {
        if (iconResource != 0) {
            mIcon.setVisibility(View.VISIBLE);
            mIcon.setImageResource(iconResource);
        } else {
            mIcon.setVisibility(View.GONE);
        }

        mAccountType.setText(textResource);
    }

    String generateContentDescription() {
        final StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(mAccountType.getText())) {
            sb.append(mAccountType.getText());
        }
        if (!TextUtils.isEmpty(mAccountUserName.getText())) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(mAccountUserName.getText());
        }
        return getContext().getString(isActivated() ? R.string.account_filter_view_checked :
                R.string.account_filter_view_not_checked, sb.toString());
    }
}
