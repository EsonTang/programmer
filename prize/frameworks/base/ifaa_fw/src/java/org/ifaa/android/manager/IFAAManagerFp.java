/**
 * @author yangzh6
 * 
 * Reference: <<IFAA标准-REE系统框架部分.pdf>>
 */

package org.ifaa.android.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.util.Log;

public class IFAAManagerFp extends IFAAManager {
	private static final String TAG = IFAAManagerFp.class.getSimpleName();

	/** Internal Attributes */
	private static final int VERSION = 1;

	// Overrides APIs
	// ------------------------------------------------------------------------
	@Override
	public int getVersion() {
		return VERSION;
	}

	@Override
	public int startBIOManager(Context context, int authType) {
		if ((null == context) || (authType != IFAAUtils.BIO_TYPE_FINGERPRINT)) {
			Log.e(TAG, "context = " + context + ", authType = " + authType);
			return IFAAUtils.COMMAND_FAIL;
		}
		if(IFAAUtils.BIO_TYPE_FINGERPRINT == authType){
    		Intent intent = new Intent();
    		intent.setAction(IFAAUtils.ACTION_FP_SETTINGS);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		PackageManager  pm =  context.getPackageManager();
    		if(pm.resolveActivity(intent, 0) != null){
    			Log.i(TAG,"start prize  BIOManager ....");
    			context.startActivity(intent);
    		}else{
    			final String pkgName = context.getPackageName();
    			Log.i(TAG,"pkgName = " + pkgName +", guofan authType = " +authType);
    			Intent intent_a = new Intent("android.settings.SECURITY_SETTINGS");
    			if(pm.resolveActivity(intent_a, 0) != null){
    				Log.i(TAG,"start android  BIOManager ....");
        			context.startActivity(intent_a);
    			}else{
    				return IFAAUtils.COMMAND_FAIL;
    			}
    			
    		}
    		return IFAAUtils.COMMAND_OK;    		
    	} else{
    		return IFAAUtils.COMMAND_FAIL;
    	}
	}

}
