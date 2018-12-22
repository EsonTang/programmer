package com.mediatek.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.android.settings.bluetooth.DeviceProfilesSettings;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;

import com.mediatek.settingslib.bluetooth.DunServerProfile;

public class DeviceProfilesSettingsExt {
    private static final String TAG = "DeviceProfilesSettingsExt";
    private Activity mActivity;
    private CachedBluetoothDevice mDevice;
    private DeviceProfilesSettings mDeviceProfilesSettings;

    public DeviceProfilesSettingsExt(Activity activity,
            DeviceProfilesSettings deviceProfileSettings, CachedBluetoothDevice device) {
        Log.d(TAG , "DeviceProfilesSettingsExt");
        mActivity = activity;
        mDeviceProfilesSettings = deviceProfileSettings;
        mDevice = device;
    }

    /**
     * Add a checkbox preference to the ViewGroup. The key will be
     * the profile's name.
     *
     * @param viewgroup The viewgroup that checkbox will be added.
     * @param device The device whose service will be shown.
     *
     */
    public void addPreferencesForProfiles(ViewGroup viewgroup,
            CachedBluetoothDevice device) {
        Log.d(TAG , "addPreferencesForProfiles");
        if (viewgroup != null && device != null) {
            for (LocalBluetoothProfile profile : device.getConnectableProfiles()) {
                Log.d(TAG , "profile.toString()=" + profile.toString());
                if ((profile instanceof DunServerProfile)) {
                    CheckBox pref = createProfilePreference(profile, device);
                    viewgroup.addView(pref);
                }
            }
        }
    }

    /**
     * Creates a checkbox preference for the particular profile. The key will be
     * the profile's name.
     *
     * @param profile The profile for which the preference controls.
     * @return A preference that allows the user to choose whether this profile
     *         will be connected to.
     */
    private CheckBox createProfilePreference(LocalBluetoothProfile profile,
            CachedBluetoothDevice device) {
        Log.d(TAG , "createProfilePreference");
        CheckBox pref = new CheckBox(mActivity);
        pref.setTag(profile.toString());
        pref.setText(profile.getNameResource(device.getDevice()));
        pref.setOnClickListener(mDeviceProfilesSettings);

        refreshProfilePreference(pref, profile);

        return pref;
    }

    private void refreshProfilePreference(CheckBox profilePref,
            LocalBluetoothProfile profile) {
        Log.d(TAG , "refreshProfilePreference");
        BluetoothDevice device = mDevice.getDevice();

        if (profile instanceof DunServerProfile) {
            Log.d(TAG , "DunProfile=" + (profile.getConnectionStatus(device) ==
                    BluetoothProfile.STATE_CONNECTED));
            profilePref.setChecked(profile.getConnectionStatus(device) ==
                    BluetoothProfile.STATE_CONNECTED);
        }
    }
}
