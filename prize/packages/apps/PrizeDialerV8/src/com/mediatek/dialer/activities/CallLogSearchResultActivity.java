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
package com.mediatek.dialer.activities;

import com.android.dialer.list.OnListFragmentScrolledListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.dialer.CallDetailActivity;
import com.android.dialer.R;
import com.android.dialer.TransactionSafeActivity;
import com.android.dialer.calllog.CallLogFragment;
import com.android.dialer.calllog.CallLogQueryHandler;

import com.mediatek.dialer.util.DialerConstants;
import com.mediatek.dialer.util.DialerFeatureOptions;

/**
 * M: [Dialer Global Search] Displays a list of call log entries.
 */
public class CallLogSearchResultActivity extends TransactionSafeActivity implements OnListFragmentScrolledListener{//PRIZE-change-yuandailin-2016-9-12
    private static final String TAG = "CallLogSearchResultActivity";
    private static final int MENU_ITEM_DELETE_ALL = 1;

    private ViewGroup mSearchResult;
    private TextView mSearchResultFor;
    private TextView mSearchResultFound;
    private String mData;
    private CallLogFragment mCallLogFragment;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        if (!DialerFeatureOptions.DIALER_GLOBAL_SEARCH) {
            finish();
        }

        // View action, start CallDetailActivity view CallLog.
        final Intent intent = this.getIntent();
        /// M: Put start activity operation in a function
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            viewCallDetail(intent);
        }

        setContentView(R.layout.mtk_call_log_search_activity);
        configureActionBar();

        mSearchResult = (LinearLayout) findViewById(R.id.calllog_search_result);
        mSearchResultFor = (TextView) findViewById(R.id.calllog_search_results_for);
        mSearchResultFound = (TextView) findViewById(R.id.calllog_search_results_found);

        mSearchResult.setVisibility(View.VISIBLE);
        mSearchResultFor.setText(Html.fromHtml(getString(R.string.search_results_for, "<b>" + mData
                + "</b>")));
        mSearchResultFound.setText(getString(R.string.search_results_searching));

        mCallLogFragment = new CallLogFragment(CallLogQueryHandler.CALL_TYPE_ALL, true);
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.calllog_list_fragment, mCallLogFragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onResume() {
        super.onResume();
        /** M: When query data changed, update the search result's display. @{ */
        String tempQueryData = this.getIntent().getStringExtra(SearchManager.USER_QUERY);
        if (mData == null || !mData.equals(tempQueryData)) {
            Log.d(TAG, "[Dialer Global Search] QueryData changed, the query data is "
                    + tempQueryData);
            mData = tempQueryData;
            mCallLogFragment.setQueryData(mData);
            mCallLogFragment.fetchCalls();
        }
        /** @} */
        mSearchResultFor.setText(Html.fromHtml(getString(R.string.search_results_for, "<b>" + mData
                + "</b>")));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        /**
         * Add for the issue: when the activity in the search result state, and
         * the activtiy is not finished, then click an item in the QSB to view a
         * call log, the interface is not call detail activty. @{
         */
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            viewCallDetail(this.getIntent());
        }
        /** @} */
    }

    private void configureActionBar() {
        Log.d(TAG, "configureActionBar()");
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar
                    .setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ITEM_DELETE_ALL, 0, R.string.recentCalls_delete).setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean enable = getAdapterCount() > 0;
        menu.findItem(MENU_ITEM_DELETE_ALL).setEnabled(enable);
        menu.findItem(MENU_ITEM_DELETE_ALL).setVisible(enable);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_DELETE_ALL:
            final Intent intent = new Intent(this, CallLogMultipleDeleteActivity.class);
            intent.putExtra(SearchManager.USER_QUERY, mData);
            intent.putExtra(DialerConstants.BUNDLE_GLOBAL_SEARCH, true);
            this.startActivity(intent);
            return true;
        case android.R.id.home:
            finish();
            return true;
        default:
            break;

        }
        return super.onOptionsItemSelected(item);
    }

    private String getQuantityText(int count, int zeroResourceId, int pluralResourceId) {
        if (count == 0) {
            return getResources().getString(zeroResourceId);
        } else {
            String format = getResources().getQuantityText(pluralResourceId, count).toString();
            return String.format(format, count);
        }
    }

    public void updateSearchResult(int count) {
        Log.d(TAG, "[Dialer Global Search] updateSearchResult: " + count);
        String text = getQuantityText(count, R.string.listFoundAllCalllogZero,
                R.plurals.searchFoundCalllogs);
        mSearchResultFound.setText(text);
    }

    private int getAdapterCount() {
        if (mCallLogFragment != null) {
            return mCallLogFragment.getItemCount();
        } else {
            return 0;
        }
    }

    private void viewCallDetail(Intent intent) {
        Uri uri = intent.getData();
        Intent newIntent = new Intent(this, CallDetailActivity.class);
        newIntent.setData(uri);
        startActivity(newIntent);
        finish();
        Log.d(TAG, "View CallLog, start CallDetailActivity to view " + uri);
    }

    /*PRIZE-add -yuandailin-2016-9-12-start*/
    @Override
    public void onListFragmentScrollStateChange(int scrollState) {

    }

    @Override
    public void onListFragmentScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO: No-op for now. This should eventually show/hide the actionBar based on
        // interactions with the ListsFragments.
    }
    /*PRIZE-add -yuandailin-2016-9-12-end*/

}
