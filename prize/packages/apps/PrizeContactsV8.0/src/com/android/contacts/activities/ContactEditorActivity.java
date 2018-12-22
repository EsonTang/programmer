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
 * limitations under the License
 */

package com.android.contacts.activities;

import com.android.contacts.R;
import com.android.contacts.activities.ContactEditorBaseActivity.ContactEditor;
import com.android.contacts.common.activity.RequestPermissionsActivity;
import com.android.contacts.editor.CompactContactEditorFragment;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.util.DialogManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.mediatek.contacts.activities.ActivitiesUtils;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.Log;
import com.mediatek.contacts.simservice.SimEditProcessor;



/**M: Add SIMEditProcessor.Listener*/
/**
 * Contact editor with all fields displayed.
 */
public class ContactEditorActivity extends ContactEditorBaseActivity
        implements DialogManager.DialogShowingViewActivity {
    private static final String TAG = "ContactEditorActivity";
    /** M: @{ */
    public static final String KEY_ACTION = "key_action";
    private String mAction;
    /** @} */

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d(TAG, "[onCreate]");

        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            Log.w(TAG, "[onCreate]no permission,return.");
            return;
        }

        /// M: Descriptions: can not add contact when in delete processing. @{
        if (ActivitiesUtils.isDeleteingContact(this)) {
            Log.w(TAG, "[onCreate]deleting contact,return.");
            return;
        }
        /// @}

        setContentView(R.layout.contact_editor_activity);

        mFragment = (ContactEditorFragment) getFragmentManager().findFragmentById(
                R.id.contact_editor_fragment);
        mFragment.setListener(mFragmentListener);

        /**
         * M: [Google Issue][ALPS03249437]: save root cause with ALPS03154332
         * do not update URI while re-create Activity, otherwise uri will
         * change from lookup uri to raw uri wrongly. @{ */
        if (savedState == null) {
        /** @} */
            final String action = getIntent().getAction();
            final Uri uri = ContactEditorBaseActivity.ACTION_EDIT.equals(action)
                    || Intent.ACTION_EDIT.equals(action) ? getIntent().getData() : null;
            mFragment.load(action, uri, getIntent().getExtras());
        /** @{ */
        }
        /** @} */
    }

    /** M: Bug Fix CR ID: ALPS00251666 @{
     * Description:Can't open the Join Contact Activity when change screen orientation.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(mAction)) {
            outState.putString(KEY_ACTION, mAction);
        }
        super.onSaveInstanceState(outState);
    }
    /** @} */
    
  //prize-add-huangliemin-2016-7-19-start
    @Override
    public void onSavePressed() {
    	if(mFragment!=null) {
    		((ContactEditorFragment)(mFragment)).onSavePressed();
    	}
    }
  //prize-add-huangliemin-2016-7-19-end

    /*prize-add-hpf-2017-12-4-start*/
    public boolean mIsPersonalInformation = false; 
    public void onChange(){
        boolean isEmpty = ((ContactEditorFragment)(mFragment)).prizeAreAllEditorFieldsEmpty();
        updateActionbarSaveTextEnable(isEmpty);
    }
    /*prize-add-hpf-2017-12-4-end*/
}
