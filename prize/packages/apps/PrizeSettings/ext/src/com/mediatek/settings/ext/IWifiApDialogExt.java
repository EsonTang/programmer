package com.mediatek.settings.ext;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.View;
import android.widget.Spinner;

public interface IWifiApDialogExt {
    /**
     * set adapter for wifi access point security spinner
     * @param context The parent context
     * @internal
     */
    void setAdapter(Context context, Spinner spinner, int arrayId);
    /**
     * Customize Wifi ap dialog view to add apChannel selection option.
     * @param context The parent context
     * @param view parent layout view
     * @param config wificonfiguration object
     */
    void customizeView(Context context, View view, WifiConfiguration config);

    /**
     * Update wifiConfiguration with selected apChannel information.
     * @param config wificonfiguration object
     */
    void updateConfig(WifiConfiguration config);

    /**
     * Set ApChannel spinner when band is changed.
     * @param apBand selected AP band
     * @param needToSet this is to check if different band is selected
     */
    void setApChannel(int apBand, boolean needToSet);
}
