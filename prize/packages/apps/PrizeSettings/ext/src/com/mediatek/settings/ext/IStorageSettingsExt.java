package com.mediatek.settings.ext;

import android.content.Context;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;

public interface IStorageSettingsExt {

    /**
    * Carry the host activity context to plugin
    * @param AppContext host application context
    * @internal
    */
    void initCustomizationStoragePlugin(Context context);

    /**
    * update the summery prefferences of storage setting fragment
    * @param prefcategory internal PreferenceCategory of storage setting fragment
    * @internal
    */
    void updateCustomizedStorageSettingsPlugin(PreferenceCategory prefcategory);

    /**
    * update the system memory, summery prefrence and related settings of private storage
    * @param summeryPref PreferenceScreen of private volume
    * @param vol current volume info to check update is done only for internal volume
    * @internal
    */
    void updateCustomizedPrivateSettingsPlugin(PreferenceScreen screen, VolumeInfo vol);

    /**
    * update the custiomizes added pref details
    * @param vol current volume info to check update is done only for internal volume
    * @internal
    */
    void updateCustomizedPrefDetails(VolumeInfo vol);

    /**
    * update the storage details on Settings home screen
    * @param summaryProvider SummaryProvider object used in storage settings
    * @param summeryLoader SummeryLoader object received in StorageSetting SummeryProvider
    * @internal
    */
    void updateCustomizedStorageSummary(Object summaryProvider, Object summaryLoader);
}
