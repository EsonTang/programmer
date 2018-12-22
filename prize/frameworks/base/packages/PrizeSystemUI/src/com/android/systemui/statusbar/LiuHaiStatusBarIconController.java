/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import android.view.ViewGroup;
import com.android.systemui.R;
import java.util.ArrayList;
import android.graphics.Point;
import android.widget.TextView;
import android.widget.ImageView;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.text.TextUtils;
import android.os.UserHandle;
import android.graphics.drawable.Icon;
import android.content.res.TypedArray;
import com.android.systemui.statusbar.phone.PrizeStatusBarStyleListener;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import android.content.res.ColorStateList;
import android.app.StatusBarManager;

//prize add by xiarui 2018-06-05 start @{
import android.provider.Settings;
import com.mediatek.common.prizeoption.PrizeOption;
//@}

/**
 * Controls the indications and error messages shown on the Keyguard
 */
public class LiuHaiStatusBarIconController implements PrizeStatusBarStyleListener{

    private static final String TAG = "LiuHaiStatusBarIconController";
    private final int maxIconNums = 5;
    private String mVolteSlot;
    private String mNetworkSpeedSlot;
    private String mHotspotSlot;
    private String mSlotBluetooth;
    private StatusBarIcon mLiuHaiBtConnectedIcon;
    private StatusBarIcon mBtConnectedIcon;

    private final Context mContext;
    private PhoneStatusBar mPhoneStatusBar;
    //left->right
    public interface ShowOnRightListener{
        public void showVolteIcon(int subId, int resid, int simCount);
        public void hideVolteIcon(int subId, int simCount);
        public void hideAllVolteIcon();

        public void refreshNetworkSpeed(int visibility, String text);
    }
    //right->left
    public interface ShowOnLeftListener{
        public void showHotSpotsIcon(View view);
        public void hideHotSpotsIcon(View view);
    }
    //status icon
    public interface StatusIconsListener{
        public void refreshStatusIcon(StatusBarIcon icon, String slot, int index, boolean visibility);
    }

    private ShowOnRightListener mShowOnRightListener;
    public void setShowOnRightListener(ShowOnRightListener listener){
        mShowOnRightListener = listener;
    }
    public ShowOnRightListener getShowOnRightListener(){
        return mShowOnRightListener;
    }

    private ShowOnLeftListener mShowOnLeftListener;
    public void setShowOnLeftListener(ShowOnLeftListener listener){
        mShowOnLeftListener = listener;
    }
    public ShowOnLeftListener getShowOnLeftListener(){
        return mShowOnLeftListener;
    }

    private StatusIconsListener mStatusIconsListener;
    public void setStatusIconsListener(StatusIconsListener listener){
        mStatusIconsListener = listener;
    }
    public StatusIconsListener getStatusIconsListener(){
        return mStatusIconsListener;
    }

    private String[] defaultSlots;//default icons
    private int[] defaultIconResIds;
    private LinearLayout mStatusIconParent;
    private int mIconSpace;
    private ArrayList<LiuHaiStatusBarIconData> iconList = new ArrayList<LiuHaiStatusBarIconData>();

    public LiuHaiStatusBarIconController(Context context, PhoneStatusBar bar) {
        mContext = context;
        mPhoneStatusBar = bar;
        initLiuHaiStatusBarIconData();

        mStatusIconsListener = new StatusIconImpI();
        mShowOnRightListener = new ShowOnRightImpI();
    }

    class StatusIconImpI implements StatusIconsListener{

        public void refreshStatusIcon(StatusBarIcon icon, String slot, int index, boolean visibility){
            PhoneStatusBar.debugLiuHai("refreshStatusIcon slot = "+ slot + ", visibility = " + visibility);
            if(!isContainTheSlot(slot)){
                PhoneStatusBar.debugLiuHai(slot + " is not in liuhai statusbar list.");
                return ;
            }
            if(mStatusIconParent != null){
                if(visibility){
                    LiuHaiStatusBarIconData data = getSlotData(slot);
                    if(data != null){
                        if(data.view == null){
                            data.view = createLiuHaiStatusBarView(data, icon, slot);
                        }
                        if(icon!= null && mSlotBluetooth.equals(slot)){
                            if(data.view instanceof StatusBarIconView){
                                StatusBarIconView sbiv = (StatusBarIconView)data.view;
                                if(sbiv.equalIcons(mBtConnectedIcon.icon, icon.icon)){
                                    PhoneStatusBar.debugLiuHai("mbluetooth is connected");
                                    sbiv.set(mLiuHaiBtConnectedIcon);
                                } else {
                                    PhoneStatusBar.debugLiuHai("mbluetooth is not connected");
                                    android.util.Log.d("mbluetooth",android.util.Log.getStackTraceString(new Throwable()));
                                    sbiv.set(data.icon);
                                }
                            }
                        } else {
                            if(icon!= null && data.view instanceof StatusBarIconView){
                                StatusBarIconView sview = (StatusBarIconView)data.view;
                                StatusBarIcon liuhai_icon = useLiuHaiIcon(slot, icon);
                                data.icon = liuhai_icon;
                                sview.set(liuhai_icon);
                            }
                        }

                        //hotspot, to show on left
                        if(mHotspotSlot.equals(slot)){
                            if(mShowOnLeftListener != null){
                                mShowOnLeftListener.showHotSpotsIcon(data.view);
                                data.visibility = true;
                            }
                            inverseView(data);
                            return ;
                        }

                        if(data.visibility && !data.isTempHide){
                            PhoneStatusBar.debugLiuHai("refreshStatusIcon "+ slot + " view is show");
                            inverseView(data);
                            return ;
                        }
                        Point point = getViewPoint(data);
                        if(point.x >= maxIconNums){
                            addTheOverMaxNumsIcon(data);
                        } else {
                            if(data.view.getParent() == mStatusIconParent){
                                mStatusIconParent.removeView(data.view);
                            }
                            mStatusIconParent.addView(data.view, point.y);
                            data.visibility = true;
                            inverseView(data);
                        }
                    }
                }else {
                    LiuHaiStatusBarIconData data = getSlotData(slot);
                    //hotspot, to show on left
                    if(mHotspotSlot.equals(slot)){
                        if(mShowOnLeftListener != null){
                            mShowOnLeftListener.hideHotSpotsIcon(data.view);
                            data.visibility = false;
                        }
                        return ;
                    }

                    if(data.visibility && data.view != null && data.view.getParent() == mStatusIconParent){
                        mStatusIconParent.removeView(data.view);
                        if(!data.isTempHide) data.visibility = false;
                    }

                    //find temp hide data, and to show it
                    showTempHideIcon();
                }
            }
        }

    }

    class LiuHaiStatusBarIconData{
        public String slot;
        public int index;
        public View view;
        public boolean visibility;
        public boolean isCanBeHide;
        public boolean isTempHide;
        public StatusBarIcon icon;

        public TextView speedTxt;
        public TextView unitTxt;
    }
    
    private void initLiuHaiStatusBarIconData(){
        mVolteSlot = mContext.getString(R.string.volte);
        mNetworkSpeedSlot = mContext.getString(R.string.network_speed);
        mHotspotSlot = mContext.getString(com.android.internal.R.string.status_bar_hotspot);
        mIconSpace = mContext.getResources().getDimensionPixelSize(R.dimen.liuhai_status_bar_space);
        mSlotBluetooth = mContext.getString(com.android.internal.R.string.status_bar_bluetooth);
        mLiuHaiBtConnectedIcon = new StatusBarIcon(UserHandle.SYSTEM, mContext.getPackageName(),
                    Icon.createWithResource(mContext, R.drawable.liuhai_bluetooth_connected), 0, 0, null);
        mBtConnectedIcon = new StatusBarIcon(UserHandle.SYSTEM, mContext.getPackageName(),
                    Icon.createWithResource(mContext, R.drawable.stat_sys_data_bluetooth_connected), 0, 0, null);

        TypedArray ar = mContext.getResources().obtainTypedArray(R.array.config_liuhai_statusBarIcons_res);
        final int len = ar.length();
        defaultIconResIds = new int[len];
        for (int i = 0; i < len; i++){
            defaultIconResIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        defaultSlots = mContext.getResources().getStringArray(R.array.config_liuhai_statusBarIcons);
        for(int i = 0; i < defaultSlots.length; i++){
            String slot = defaultSlots[i];
            PhoneStatusBar.debugLiuHai("init [" + i + "] = " + slot);
            LiuHaiStatusBarIconData data = new LiuHaiStatusBarIconData();
            data.slot = slot;
            data.index = i;
            if(mVolteSlot.equals(slot)){
                data.view = createLiuHaiStatusBarView(data, null, slot);
            } else if(mNetworkSpeedSlot.equals(slot)){
                data.view = createLiuHaiStatusBarView(data, null, slot);
                data.speedTxt = (TextView)data.view.findViewById(R.id.liuhai_speed_txt);
                data.unitTxt = (TextView)data.view.findViewById(R.id.liuhai_speed_unit_txt);
            }
            if(i == 1 || i == 4 || i == 5){//can be hide icon : volte, alarm_clock, location
                data.isCanBeHide = true;
            }
            iconList.add(data);
        }
    }

    public boolean isContainTheSlot(String slot){
        for(int i = 0; i < defaultSlots.length; i++){
            String s = defaultSlots[i];
            if(s.equals(slot)){
                return true;
            }
        }
        return false;
    }
    
    public LiuHaiStatusBarIconData getSlotData(String slot){
        for(int i = 0; i < iconList.size(); i++){
            LiuHaiStatusBarIconData data = iconList.get(i);
            if(data != null && data.slot.equals(slot)){
                return data;
            }
        }
        return null;
    }

    /*
    * Point x = icon sum; y = added view index;
    */
    public Point getViewPoint(LiuHaiStatusBarIconData data){
        int sum = 0;
        int count = 0;
        for(int i = 0; i < iconList.size(); i++){
            LiuHaiStatusBarIconData d = iconList.get(i);
            if(mHotspotSlot.equals(d.slot)){
                continue ;
            }
            if(d.visibility) {
                if(d.isCanBeHide){
                    if(!d.isTempHide){
                        if(i < data.index){
                            count++;
                        }
                        sum++;
                    }
                } else {
                    if(i < data.index){
                        count++;
                    }
                    sum++;
                }
            }
        }
        PhoneStatusBar.debugLiuHai(data.slot + " view Point = (" + sum + ", " + count + ")");
        return new Point(sum, count);
    }

    public View createLiuHaiStatusBarView(LiuHaiStatusBarIconData data, StatusBarIcon icon, String slot){
        View view = null;
        if(slot.equals(mVolteSlot)){
            view = new ImageView(mContext);
        } else if(slot.equals(mNetworkSpeedSlot)){
            view = (ViewGroup) LayoutInflater.from(mContext)
                            .inflate(R.layout.statusbar_network_speed_liuhai_prize, null);
        } else {
            StatusBarIconView iconView = new StatusBarIconView(mContext, slot, null, false);
            StatusBarIcon liuhai_icon = useLiuHaiIcon(slot, icon);
            data.icon = liuhai_icon;
            iconView.set(liuhai_icon);
            view = iconView;
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(mHotspotSlot.equals(slot)){
            lp.setMargins(mIconSpace, 0, 0, 0);
        } else {
            lp.setMargins(0, 0, mIconSpace, 0);
        }
        view.setLayoutParams(lp);
        PhoneStatusBar.debugLiuHai("create slot(" + slot + ") view ");
        return view;
    }
    
    public StatusBarIcon useLiuHaiIcon(String slot, StatusBarIcon icon){
        if(slot == null) return icon;
        int resourceId = 0;
        LiuHaiStatusBarIconData data = getSlotData(slot);
        if(data.index >= 0 && data.index < defaultIconResIds.length ){
            resourceId = defaultIconResIds[data.index];
        }
        if(resourceId != 0 ){
            //prize add by xiarui 2018-06-02 @{
            if (PrizeOption.PRIZE_VIBRATE_CONTROL) {
                if (resourceId == R.drawable.liuhai_volume) {
                    boolean isVibratorInSilent = Settings.System.getInt(mContext.getContentResolver(), Settings.System.PRIZE_VIBRATE_IN_SILENT, 0) != 0;
                    if (isVibratorInSilent) {
                        resourceId = R.drawable.liuhai_volume_vibrator2;
                    }
                }
            }
            //@}
            StatusBarIcon sbIcon = new StatusBarIcon(UserHandle.SYSTEM, mContext.getPackageName(),
                    Icon.createWithResource(mContext, resourceId), 0, 0, null);
            return sbIcon;
        }
        return icon;
    }

    public void addTheOverMaxNumsIcon(LiuHaiStatusBarIconData data){
        PhoneStatusBar.debugLiuHai("addTheOverMaxNumsIcon start");
        LiuHaiStatusBarIconData tempData = null;
        for(int i = 0; i < iconList.size(); i++){
            LiuHaiStatusBarIconData d = iconList.get(i);
            if(d.isCanBeHide && !d.isTempHide){
                d.isTempHide = true;
                tempData = d;
                break ;
            }
        }
        if(tempData != null){
            //mStatusIconsListener.refreshStatusIcon(null, tempData.slot, 0, false);
            if(tempData.visibility && tempData.view != null && tempData.view.getParent() == mStatusIconParent){
                PhoneStatusBar.debugLiuHai("addTheOverMaxNumsIcon temp hide slot = " + tempData.slot);
                mStatusIconParent.removeView(tempData.view);
            }
            mStatusIconsListener.refreshStatusIcon(null, data.slot, 0, true);
        } else {
            PhoneStatusBar.debugLiuHai("addTheOverMaxNumsIcon all canbe hide icon had hide.");
        }
    }

    public void showTempHideIcon(){
        PhoneStatusBar.debugLiuHai("showTempHideIcon find temp hide icon, and show it.");
        for(int i = iconList.size() - 1; i >= 0; i--){
            LiuHaiStatusBarIconData d = iconList.get(i);
            if(d.isTempHide){
                mStatusIconsListener.refreshStatusIcon(null, d.slot, 0, true);
                d.isTempHide = false;
                PhoneStatusBar.debugLiuHai("showTempHideIcon show temp hide slot = " + d.slot);
                break ;
            }
        }
    }

    private int firstSubId = -1;
    private int secondSubId = -1;
    private int mSimId = 0;//0->no volte icon, 1->volte 1, 2->volte 2, 3->volte 1&2;
    private int mSimCount = 0;
    class ShowOnRightImpI implements ShowOnRightListener{

        public void showVolteIcon(int subId, int resid, int simCount){
            subId = SubscriptionManager.getSlotId(subId) + 1;//get slot id
            int simId = addSubId(subId);
            if(mSimId == simId && mSimCount == simCount){
                return ;
            }
            mSimId = simId;
            mSimCount = simCount;
            PhoneStatusBar.debugLiuHai("showVolteIcon subId = " + subId + ", mSimId = " + mSimId
                    + ", firstSubId = " + firstSubId + ", secondSubId = " + secondSubId);
            refreshVolteIcon();
        }

        public void hideVolteIcon(int subId, int simCount){
            subId = SubscriptionManager.getSlotId(subId) + 1;//get slot id
            int simId = deleteSubId(subId);
            if(mSimId == simId && mSimCount == simCount){
                return ;
            }
            mSimId = simId;
            mSimCount = simCount;
            PhoneStatusBar.debugLiuHai("hideVolteIcon subId = " + subId + ", mSimId = " + mSimId
                    + ", firstSubId = " + firstSubId + ", secondSubId = " + secondSubId);
            refreshVolteIcon();
        }

        public void hideAllVolteIcon(){
            PhoneStatusBar.debugLiuHai("hideAllVolteIcon, firstSubId = " + firstSubId
                    + ", secondSubId = " + secondSubId);
            firstSubId = -1;
            secondSubId = -1;
            mSimId = 0;
            mSimCount = 0;
            refreshVolteIcon();
        }

        public void refreshNetworkSpeed(int visibility, String text){
            PhoneStatusBar.debugLiuHai("refreshNetworkSpeed, visibility = " + visibility
                    + ", text = " + text);
            String[] arr = text.split(" ");
            if(visibility == View.VISIBLE && !TextUtils.isEmpty(text) && arr.length >= 2){
                LiuHaiStatusBarIconData data = getSlotData(mNetworkSpeedSlot);
                mStatusIconsListener.refreshStatusIcon(null, mNetworkSpeedSlot, 0, true);
                data.speedTxt.setText(arr[0]);
                data.unitTxt.setText(arr[1]);
            } else {
                mStatusIconsListener.refreshStatusIcon(null, mNetworkSpeedSlot, 0, false);
            }
        }
    }

    public void refreshVolteIcon(){
        int curSubResId = getVolteResId(mSimId);
        LiuHaiStatusBarIconData data = getSlotData(mVolteSlot);
        if(data.view instanceof ImageView){
            if(curSubResId != 0){
                mStatusIconsListener.refreshStatusIcon(null, mVolteSlot, 0, true);
                ImageView iv = (ImageView)data.view;
                iv.setImageResource(curSubResId);
            } else {
                mStatusIconsListener.refreshStatusIcon(null, mVolteSlot, 0, false);
                return ;
            }
        }
    }

    private int addSubId(int subId){
        int simId = 0;
        if(firstSubId == -1 && secondSubId == -1){
            firstSubId = subId;
            simId = subId;
        } else if(firstSubId != -1 && secondSubId == -1){
            if(firstSubId == subId){
                simId = firstSubId;
            } else {
                secondSubId = subId;
                simId = 3;
            }
        } else if(firstSubId == -1 && secondSubId != -1){
            if(secondSubId == subId){
                simId = secondSubId;
            } else {
                firstSubId = subId;
                simId = 3;
            }
        } else if(firstSubId != -1 && secondSubId != -1){
            if(firstSubId == subId || secondSubId == subId){
                simId = 3;
            } else {
                simId = 0;
            }
        }
        return simId;
    }

    private int deleteSubId(int subId){
        int simId = 0;
        if(firstSubId == -1 && secondSubId == -1){
            simId = 0;
        } else if(firstSubId != -1 && secondSubId == -1){
            if(firstSubId == subId){
                firstSubId = -1;
                simId = 0;
            } else {
                simId = firstSubId;
            }
        } else if(firstSubId == -1 && secondSubId != -1){
            if(secondSubId == subId){
                secondSubId = -1;
                simId = 0;
            } else {
                simId = secondSubId;
            }
        } else if(firstSubId != -1 && secondSubId != -1){
            if(firstSubId == subId){
                firstSubId = -1;
                simId = secondSubId;
            } else if(secondSubId == subId){
                secondSubId = -1;
                simId = firstSubId;
            } else {
                firstSubId = -1;
                secondSubId = -1;
                simId = 0;
            }
        }
        return simId;
    }

    public int getVolteResId(int simId){
        int resid = 0;
        if(mSimCount == 1){
            if(simId == 1 || simId == 2){
                resid = R.drawable.liuhai_volte0;
            } else {
                resid = 0;
            }
        } else {
            if(simId == 1){
                resid = R.drawable.liuhai_volte1;
            } else if(simId == 2){
                resid = R.drawable.liuhai_volte2;
            } else if(simId == 3){
                resid = R.drawable.liuhai_volte3;
            } else {
                resid = 0;
            }
        }
        return resid;
    }

    public void setStatusIconParent(LinearLayout parent){
        mStatusIconParent = parent;
    }

    public void setStatusBarIconController(StatusBarIconController controller){
        mStatusBarIconController = controller;
    }

    private StatusBarIconController mStatusBarIconController;
    private int mCurStatusBarStyle = StatusBarManager.STATUS_BAR_INVERSE_DEFALUT;
    @Override
    public void onStatusBarStyleChanged(int style) {
        PhoneStatusBar.debugLiuHai("onStatusBarStyleChanged(). curStyle=" + mCurStatusBarStyle + ", newStyle=" + style);
        if(mCurStatusBarStyle != style) {
            mCurStatusBarStyle = style;
            refreshAllIcons();
        }
    }

    public void refreshAllIcons(){
        for(int i = 0; i < iconList.size(); i++){
            LiuHaiStatusBarIconData d = iconList.get(i);
            if(d.visibility && !d.isTempHide){
                mStatusIconsListener.refreshStatusIcon(null, d.slot, 0, true);
            }
        }
    }

    public void inverseView(LiuHaiStatusBarIconData data){
        if(mStatusBarIconController != null){
            PhoneStatusBar.debugLiuHai("inverseView data.slot = " + data.slot + ", mCurStatusBarStyle = " + mCurStatusBarStyle);
            if(data.slot.equals(mVolteSlot)){
                ImageView iv = (ImageView)data.view;
                int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
                if(mCurStatusBarStyle != 0){
                    color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
                }
                iv.setImageTintList(ColorStateList.valueOf(color));
            } else if(data.slot.equals(mNetworkSpeedSlot)){
                if(data.speedTxt != null && data.unitTxt != null){
                    int color = StatusBarManager.STATUS_BAR_COLOR_WHITE;
                    if(mCurStatusBarStyle != 0){
                        color = StatusBarManager.STATUS_BAR_COLOR_GRAY;
                    }
                    data.speedTxt.setTextColor(color);
                    data.unitTxt.setTextColor(color);
                }
            } else {
                if(data.view instanceof StatusBarIconView){
                    StatusBarIconView view = (StatusBarIconView)data.view;
                    if(isKeyguardController){
                        mStatusBarIconController.setKeyguardStatusIconForStyle(view, true, true);
                    } else {
                        mStatusBarIconController.setStatusIconForStyle(view, true, true);
                    }
                }
            }
        }
    }

    private boolean isKeyguardController = false;
    public void setKeyguardController(boolean value){
        isKeyguardController = true;
    }
}
