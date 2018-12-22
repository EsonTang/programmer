package com.android.lpserver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by prize on 2016/11/28.
 */
public class AutoStartReceiver extends BroadcastReceiver {
    private Context context;
    private static final String TAG = "AutoStartReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences("auto_switch",Context.MODE_PRIVATE);
        Log.d(TAG,"sp.getInt() = " + sharedPreferences.getInt("auto_swithc_number",0));

       if(sharedPreferences.getInt("auto_swithc_number",0) != 2) {
           SharedPreferences.Editor edit = sharedPreferences.edit();
           edit.putInt("auto_swithc_number",2);
           edit.commit();
           updateAccessibility();
       }
    }


    static final char ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR = ':';
    private static final String SERVICE_NAME = "com.android.lpserver/com.android.lpserver.QiangHongBaoService";
    final static TextUtils.SimpleStringSplitter sStringColonSplitter =
            new TextUtils.SimpleStringSplitter(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
    private void updateAccessibility() {
        // Parse the enabled services.
        Set<ComponentName> enabledServices = getEnabledServicesFromSettings(context);
        if(null == enabledServices) {
            return;
        }
        // Determine enabled services and accessibility state.
        ComponentName toggledService = ComponentName.unflattenFromString(SERVICE_NAME);
        final boolean accessibilityEnabled;
        // Enabling at least one service enables accessibility.
        accessibilityEnabled = true;
        enabledServices.add(toggledService);
        // Update the enabled services setting.
        StringBuilder enabledServicesBuilder = new StringBuilder();
        // Keep the enabled services even if they are not installed since we
        // have no way to know whether the application restore process has
        // completed. In general the system should be responsible for the
        // clean up not settings.
        for (ComponentName enabledService : enabledServices) {
            enabledServicesBuilder.append(enabledService.flattenToString());
            enabledServicesBuilder.append(ENABLED_ACCESSIBILITY_SERVICES_SEPARATOR);
        }
        final int enabledServicesBuilderLength = enabledServicesBuilder.length();
        if (enabledServicesBuilderLength > 0) {
            enabledServicesBuilder.deleteCharAt(enabledServicesBuilderLength - 1);
        }
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                enabledServicesBuilder.toString());
        Log.d(TAG,"enabledServicesBuilder = " + enabledServicesBuilder.toString());

        // Update accessibility enabled.
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, accessibilityEnabled ? 1 : 0);
    }



    private static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            enabledServicesSetting = "";
        }
        Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        TextUtils.SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);
        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                if(enabledService.flattenToString().equals(SERVICE_NAME)) {
                    return null;
                }
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }
}
