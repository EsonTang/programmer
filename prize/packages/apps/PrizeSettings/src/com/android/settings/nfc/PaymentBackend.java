/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.settings.nfc;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.nfc.cardemulation.ApduServiceInfo;
import android.nfc.cardemulation.CardEmulation;
import android.os.Handler;
import android.os.Message;

/// M: Patch for GSMA TS27 Test Item 15.7.3.4.2
import android.os.RemoteException;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.internal.content.PackageMonitor;

import java.util.ArrayList;
import java.util.List;
/// M: Patch for GSMA TS27 Test Item 15.7.3.4.2 @{
import java.util.Iterator;

import com.mediatek.nfcgsma_extras.INfcAdapterGsmaExtras;
import com.mediatek.nfcgsma_extras.GSMAOffHostAppInfo;
import com.mediatek.settings.FeatureOption;
/// @}

public class PaymentBackend {
    public static final String TAG = "Settings.PaymentBackend";

    public interface Callback {
        void onPaymentAppsChanged();
    }

    public static class PaymentAppInfo {
        CharSequence label;
        CharSequence description;
        Drawable banner;
        boolean isDefault;
        public ComponentName componentName;
        public ComponentName settingsComponent;
    }

    private final Context mContext;
    private final NfcAdapter mAdapter;
    private final CardEmulation mCardEmuManager;
    private final PackageMonitor mSettingsPackageMonitor = new SettingsPackageMonitor();
    /// M: Patch for GSMA TS27 Test Item 15.7.3.4.2
    private final INfcAdapterGsmaExtras mGsmaEx;
    // Fields below only modified on UI thread
    private ArrayList<PaymentAppInfo> mAppInfos;
    private PaymentAppInfo mDefaultAppInfo;
    private ArrayList<Callback> mCallbacks = new ArrayList<Callback>();

    public PaymentBackend(Context context) {
        mContext = context;

        mAdapter = NfcAdapter.getDefaultAdapter(context);
        mCardEmuManager = CardEmulation.getInstance(mAdapter);
           /// M: Patch for GSMA TS27 Test Item 15.7.3.4.2
        mGsmaEx = mAdapter.getNfcAdapterGsmaExtrasInterface();
        refresh();
    }

    public void onPause() {
        mSettingsPackageMonitor.unregister();
    }

    public void onResume() {
        mSettingsPackageMonitor.register(mContext, mContext.getMainLooper(), false);
    }

    public void refresh() {
        PackageManager pm = mContext.getPackageManager();
        List<ApduServiceInfo> serviceInfos =
                mCardEmuManager.getServices(CardEmulation.CATEGORY_PAYMENT);
        ArrayList<PaymentAppInfo> appInfos = new ArrayList<PaymentAppInfo>();

        if (serviceInfos == null) {
            makeCallbacks();
            return;
        }

        ComponentName defaultAppName = getDefaultPaymentApp();
        PaymentAppInfo foundDefaultApp = null;
        for (ApduServiceInfo service : serviceInfos) {
            PaymentAppInfo appInfo = new PaymentAppInfo();
            appInfo.label = service.loadLabel(pm);
            if (appInfo.label == null) {
                appInfo.label = service.loadAppLabel(pm);
            }
            appInfo.isDefault = service.getComponent().equals(defaultAppName);
            if (appInfo.isDefault) {
                foundDefaultApp = appInfo;
            }
            appInfo.componentName = service.getComponent();
            String settingsActivity = service.getSettingsActivityName();
            if (settingsActivity != null) {
                appInfo.settingsComponent = new ComponentName(appInfo.componentName.getPackageName(),
                        settingsActivity);
            } else {
                appInfo.settingsComponent = null;
            }
            appInfo.description = service.getDescription();
            appInfo.banner = service.loadBanner(pm);
            appInfos.add(appInfo);
        }
        /// M: Patch for GSMA TS27 Test Item 15.7.3.4.2 @{
        if (FeatureOption.MTK_NFC_GSMA_SUPPORT) {
            try {
                List<GSMAOffHostAppInfo> GSMAOffHostAppInfos = mGsmaEx.getGSMAOffHostAppInfos();
                Iterator itr = GSMAOffHostAppInfos.iterator();
                while (itr.hasNext()) {
                    GSMAOffHostAppInfo offHostAppInfo = (GSMAOffHostAppInfo) itr.next();
                    PaymentAppInfo gsmaPaymentAppInfo = new PaymentAppInfo();
                    gsmaPaymentAppInfo.label = offHostAppInfo.getLabel();
                    gsmaPaymentAppInfo.description = offHostAppInfo.getDescription();
                    gsmaPaymentAppInfo.banner = offHostAppInfo.getBanner();
                    gsmaPaymentAppInfo.componentName = offHostAppInfo.getComponentName();
                    gsmaPaymentAppInfo.isDefault =
                        gsmaPaymentAppInfo.componentName.equals(defaultAppName);
                    if (gsmaPaymentAppInfo.isDefault) {
                        foundDefaultApp = gsmaPaymentAppInfo;
                    }
                    gsmaPaymentAppInfo.settingsComponent = null;
                    appInfos.add(gsmaPaymentAppInfo);
                }
            } catch (RemoteException e) {
                android.util.Log.e(TAG, "Fail: GSMAOffHostAppInfo - " + e);
            }
        }
        /// @}
        mAppInfos = appInfos;
        mDefaultAppInfo = foundDefaultApp;
        makeCallbacks();
    }

    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    public List<PaymentAppInfo> getPaymentAppInfos() {
        return mAppInfos;
    }

    public PaymentAppInfo getDefaultApp() {
        return mDefaultAppInfo;
    }

    void makeCallbacks() {
        for (Callback callback : mCallbacks) {
            callback.onPaymentAppsChanged();
        }
    }

    Drawable loadDrawableForPackage(String pkgName, int drawableResId) {
        PackageManager pm = mContext.getPackageManager();
        try {
            Resources res = pm.getResourcesForApplication(pkgName);
            Drawable banner = res.getDrawable(drawableResId);
            return banner;
        } catch (Resources.NotFoundException e) {
            return null;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    boolean isForegroundMode() {
        try {
            return Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.NFC_PAYMENT_FOREGROUND) != 0;
        } catch (SettingNotFoundException e) {
            return false;
        }
    }

    void setForegroundMode(boolean foreground) {
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_FOREGROUND, foreground ? 1 : 0) ;
    }

    ComponentName getDefaultPaymentApp() {
        String componentString = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT);
        if (componentString != null) {
            return ComponentName.unflattenFromString(componentString);
        } else {
            return null;
        }
    }

    public void setDefaultPaymentApp(ComponentName app) {
        Settings.Secure.putString(mContext.getContentResolver(),
                Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT,
                app != null ? app.flattenToString() : null);
        refresh();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            refresh();
        }
    };

    private class SettingsPackageMonitor extends PackageMonitor {
        @Override
        public void onPackageAdded(String packageName, int uid) {
            mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageAppeared(String packageName, int reason) {
            mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageDisappeared(String packageName, int reason) {
            mHandler.obtainMessage().sendToTarget();
        }

        @Override
        public void onPackageRemoved(String packageName, int uid) {
            mHandler.obtainMessage().sendToTarget();
        }
    }
}
