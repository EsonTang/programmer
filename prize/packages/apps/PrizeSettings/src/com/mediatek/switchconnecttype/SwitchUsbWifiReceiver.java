package com.mediatek.switchconnecttype;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter;
import android.location.LocationManager;
import android.provider.Settings;

public final class SwitchUsbWifiReceiver extends BroadcastReceiver {

    private static final String TAG = "SECRET_CODE";
    private static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
    private static final String META_CONNECT_TYPE = "persist.meta.connecttype";
    // process *#*#3641122#*#*
    private final Uri WIFI_TO_USB = Uri.parse("android_secret_code://3641122");

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == null) {
            Log.i( TAG, "[ATM] Null action");
            return;
        }
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Uri uri = intent.getData();
            Log.i(TAG, "[ATM] getIntent success uri: "+uri.toString());
            if (uri.equals(WIFI_TO_USB)) {
				//Close WIFI if already on
				WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				if(wifiManager.isWifiEnabled()) {
					Log.i(TAG, "[ATM] WIFI is on");
				    if(wifiManager.setWifiEnabled(false)){
						Log.i(TAG, "[ATM] Close WIFI success");
					}
				}

				//Close BT if already on
				BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
				BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
				if(bluetoothAdapter.isEnabled()) {
					Log.i(TAG, "[ATM] Bluetooth is on");
					if(bluetoothAdapter.disable()){
						Log.i(TAG, "[ATM] Close Bluetooth success");
					}
				}

				//Close GPS if already on
				LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
				if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					Log.i(TAG, "[ATM] GPS is on");
				    Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER, false);
				    Log.i(TAG, "[ATM] Close GPS success");
				}

				SystemProperties.set(META_CONNECT_TYPE,"usb");
				Log.i(TAG, "[ATM] switch communication to usb");
				Toast.makeText(context, "Switch to USB", Toast.LENGTH_SHORT).show();
            }
		}
    }
}
