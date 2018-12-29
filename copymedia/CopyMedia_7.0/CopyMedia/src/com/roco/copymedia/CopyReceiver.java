package com.roco.copymedia;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;

public class CopyReceiver extends BroadcastReceiver {

	private String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static final String SRC_S = "/system/extra";
	private static final String SRC_D = "/data/extra";
	private static final String DST_STR = "persist.sys.sd.defaultpath";
    private static final String DST = SystemProperties.get("persist.sys.sd.defaultpath");
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		boolean copyOk = CopyJni.isCopyDown();
		CopyUtil.debug("onReceive action " + action);
		if(action.equals(BOOT_COMPLETED)){
			if(!copyOk){
				// doCopyStuff(context);
				Intent copyIntent = new Intent(context, CopyService.class);
				context.startService(copyIntent);
			}
		}
	}

	public void doCopyStuff(final Context mContext){
		CopyJni.startProcess(mContext, SystemProperties.get("ro.preinstall.canreset", "yes").equals("yes") ?SRC_S:SRC_D, DST);
	}
	
}
