package com.android.systemui.usb;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.SystemProperties;

public class UsbConnectedReceiver extends BroadcastReceiver {
	private static boolean flag = false;
	
    @Override
    public void onReceive(Context context, Intent intent) {
		
        String action = intent.getAction();
		
		if (UsbManager.ACTION_USB_STATE.equals(action)) {
			String function = SystemProperties.get("ro.jty.def.usb.function","null");
			if("charging".equals(function) || "null".equals(function)){
				return;
			}
			if(!"mtp".equals(function) && !"ptp".equals(function) && !"midi".equals(function)){
				return;
			}
				
			boolean connected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);

			if(connected && !flag){
				flag = true;
				UsbManager mUsbManager = context.getSystemService(UsbManager.class);
				mUsbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, true);
			}
			if(!connected){
				flag = false;
			}
		} 
    }
}