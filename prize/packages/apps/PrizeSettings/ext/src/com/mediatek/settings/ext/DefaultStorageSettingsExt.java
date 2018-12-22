package com.mediatek.settings.ext;

import android.content.Context;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;

public class DefaultStorageSettingsExt implements IStorageSettingsExt {

    public void initCustomizationStoragePlugin(Context context){
    }

    public void updateCustomizedStorageSettingsPlugin(PreferenceCategory prefcategory){
    }

    public void updateCustomizedPrivateSettingsPlugin(PreferenceScreen screen, VolumeInfo vol){
    }

    public void updateCustomizedPrefDetails(VolumeInfo vol){
    }

    public void updateCustomizedStorageSummary(Object summaryProvider, Object summaryLoader){
    }
}
