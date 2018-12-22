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

package com.mediatek.dialer.ext;

import java.util.List;
import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v13.app.FragmentPagerAdapter;
import android.telecom.PhoneAccountHandle;
import android.database.Cursor;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ListView;

public class DefaultCallLogExtension implements ICallLogExtension {
    private static final String TAG = "DefaultCallLogExtension";

    /**
     * for OP09
     * set account for call log list
     *
     * @param Context context
     * @param View view
     * @param PhoneAccountHandle phoneAccountHandle
     */
     @Override
     public void setCallAccountForCallLogList(Context context, View view,
             PhoneAccountHandle phoneAccountHandle) {
         log("setCallAccountForCallLogList");
     }

    /**
     * for op01
     * @param typeFiler current query type
     * @param builder the query selection Stringbuilder
     * @param selectionArgs the query selection args, modify to change query selection
     */
    @Override
    public void appendQuerySelection(int typeFiler, StringBuilder builder,
            List<String> selectionArgs) {
        log("appendQuerySelection");
    }


    /**
     * for op01
     * called when home button in actionbar clicked
     * @param activity the current activity
     * @param pagerAdapter the view pager adapter used in activity
     * @param menu the optionsmenu itmes
     * @return true if do not need further operation in host
     */
    @Override
    public boolean onHomeButtonClick(Activity activity,
            FragmentPagerAdapter pagerAdapter, MenuItem menu) {
        log("onHomeButtonClick");
        return false;
    }

    /**
     * for op01
     * called when host create menu, to add plug-in own menu here
     * @param activity the current activity
     * @param menu
     * @param tabs the ViewPagerTabs used in activity
     * @param callLogAction callback plug-in need if things need to be done by host
     */
    @Override
    public void createCallLogMenu(Activity activity, Menu menu, HorizontalScrollView tabs,
            ICallLogAction callLogAction) {
        log("createCallLogMenu");
    }

    /**
     * for op01
     * called when host prepare menu, prepare plug-in own menu here
     * @param activity the current activity
     * @param menu the Menu Created
     * @param fragment the current fragment
     * @param itemDeleteAll the optionsmenu delete all item
     * @param adapterCount adapterCount
     */
    @Override
    public void prepareCallLogMenu(Activity activity, Menu menu,
            Fragment fragment, MenuItem itemDeleteAll, int adapterCount) {
        log("prepareCallLogMenu");
    }

    /**
     * for op01
     * called when updating tab count
     * @param activity the current activity
     * @param count count
     * @return tab count
     */
    @Override
    public int getTabCount(Activity activity, int count) {
       log("getTabCount");
       return count;
    }

    /**
     * for op01
     * @param context the current context
     * @param pagerAdapter the view pager adapter used in activity
     * @param tabs the ViewPagerTabs used in activity
     */
    @Override
    public void restoreFragments(Context context,
            FragmentPagerAdapter pagerAdapter, HorizontalScrollView tabs) {
        log("restoreFragments");
    }

    /**
     * for op01
     * @param activity the current activity
     * @param outState save state
     */
    @Override
    public void onSaveInstanceState(Activity activity, Bundle outState) {
        log("onSaveInstanceState");
    }

    /**.
     * for op01
     * @param activity the current activity
     * @param pagerAdapter the view pager adapter used in activity
     * @param callLogAction callback plug-in need if things need to be done by host
     */
    @Override
    public void onBackPressed(Activity activity, FragmentPagerAdapter pagerAdapter,
            ICallLogAction callLogAction) {
        log("prepareCallLogMenu");
        callLogAction.processBackPressed();
    }

    private void log(String msg) {
//        Log.d(TAG, msg + " default");
    }

    /**
     * for op01
     * plug-in set position
     * @param position to set
     */
    @Override
    public void setPosition(int position) {
        //default do nothing
    }

    /**
     * for op01
     * plug-in get current position
     * @param position position
     * @return get the position
     */
    @Override
    public int getPosition(int position) {
        return position;
    }

    /**.
     * for op01
     * plug-in manage the state and unregister receiver
     * @param activity the current activity
     */
    @Override
    public void onDestroy(Activity activity) {
        //default do nothing
    }

    /**.
     * for op01
     * plug-in reset the reject mode in the host
     * @param activity the current activity
     * @param bundle bundle
     */
    @Override
    public void onCreate(Activity activity, Bundle bundle) {
        //default do nothing
    }

    /**.
     * for op01
     * plug-in reset the reject mode in the host
     * @param activity the current activity
     */
    @Override
    public void resetRejectMode(Activity activity) {
        //default do nothing
    }

    /**.
     * for op01
     * plug-in get the auto reject icon
     * @return return the auto reject icon
     */
    @Override
    public Drawable getAutoRejectDrawable() {
        //default return null
        return null;
    }

    /**
     * for op01.
     * plug-in whether is auto reject mode
     * @return call log show state
     */
    public boolean isAutoRejectMode() {
        //default return false
        return false;
    }

    /**
     * for op01.
     * plug-in insert auto reject icon resource for dialer search
     * @param callTypeDrawable callTypeDrawable
     */
    public void addResourceForDialerSearch(HashMap<Integer, Drawable>
            callTypeDrawable) {
        //default do nothing
    }

    /**
     * for op09.
     * plug-in whether show sim label account or not
     * @return Account null or not
     */
    public boolean shouldReturnAccountNull() {
        return true;
    }

     /**
     * for op01.
     * plug-in always show video call back button
     * @return true in op01
     */
    @Override
    public boolean showVideoForAllCallLog() {
        //default return false
        return false;
    }

    @Override
    public boolean isVideoButtonEnabled(boolean hostVideoEnabled, Object...params) {
        Boolean canPlaceCall =
                (Boolean)params[ICallLogExtension.VIDEO_BUTTON_PARAMS_INDEX_PLACE_CALL];
        Boolean isVideoShown =
                (Boolean)params[ICallLogExtension.VIDEO_BUTTON_PARAMS_INDEX_IS_VIDEO_SHOWN];
        return (hostVideoEnabled && canPlaceCall && isVideoShown);
    }

    /**
     * for OP18
     * plug-for Draw VoWifi & VoVolte Call Icon
     * @param int scaledHeight
     */
    @Override
    public void drawWifiVolteCallIcon(int scaledHeight) {
        log("drawWifiVolteCallIcon");
    }

    /**
     * for OP18
     * plug-for Draw VoWifi & VoVolte Canvas
     * @param Object resourceObj
     * @param int left
     * @param Canvas canvas
     * @Object callTypeIconViewObj
     */
    @Override
    public void drawWifiVolteCanvas(int left, Canvas canvas,
                     Object callTypeIconViewObj) {
        log("drawWifiVolteCanvas");
    }

    /**
     * for OP18
     * plug-for Show VoWifi & VoVolte Call Icon
     * @param Object object
     * @param int features
     */
    @Override
    public void setShowVolteWifi(Object object, int features) {
        log("setShowVolteWifi");
    }

  /**
     * for OP18
     * plug-in to check ViWifi shown or not
     * @param Object object
     * @return boolean true or false
     */
     @Override
     public boolean isViWifiShown(Object object) {
         log("isViWifiShown");
         return false;
    }

    /**
     * for OP18
     * plug-in to group Call log according to number and Call Feature
     * @param Cursor cursor
     * @return boolean true
     */
    @Override
    public boolean sameCallFeature(Cursor cursor) {
        log("sameCallFeature");
        return true;
    }

    /**
     * for op01.
     * plug-in we do not use google default blocked number features
     * @return false in op01
     */
    @Override
    public boolean shouldUseBlockedNumberFeature() {
        return true;
    }

   /**
    * for OP18
    * Customize Bind action buttons
    * @param Object object
    */
    @Override
    public void customizeBindActionButtons(Object object) {
        log("customizeBindActionButtons");
    }

    /**
     * Request capability when tap call log item
     */
    @Override
    public void onExpandViewHolderActions(String number) {

    }

    /**
     * for OP01
     * called when click expand view in calllist item view holder
     * @param obj the host calllist item view holder
     * @param show whether show action
     */
    @Override
    public void showActions(Object obj, boolean show) {

    }

}
