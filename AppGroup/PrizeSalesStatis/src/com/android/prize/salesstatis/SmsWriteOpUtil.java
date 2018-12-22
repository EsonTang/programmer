package com.android.prize.salesstatis;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public final class SmsWriteOpUtil {
	private static final String TAG = "PrizeSalesStatis";
    private static final int OP_WRITE_SMS = 15;

    public static boolean setWriteEnabled(Context context, boolean enabled) {
        int uid = getUid(context);
        int mode = enabled ? AppOpsManager.MODE_ALLOWED
                : AppOpsManager.MODE_IGNORED;
        Log.v(TAG, "oooMMS AppOpsManager setWriteEnabled  uid = "+ uid + ", mode = " + mode);
        return setMode(context, OP_WRITE_SMS, uid, mode);
    }

    private static boolean setMode(Context context, int code, int uid, int mode) {
        AppOpsManager appOpsManager = (AppOpsManager) context
                .getSystemService(Context.APP_OPS_SERVICE);
        try {
        	Log.v(TAG, "oooMMS AppOpsManager setWriteEnabled setMode() is start! ");
			appOpsManager.setMode(OP_WRITE_SMS, uid, context.getPackageName(), AppOpsManager.MODE_ALLOWED);
			Log.v(TAG, "oooMMS AppOpsManager setWriteEnabled setMode() is end! ");
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v(TAG, "oooMMS AppOpsManager setWriteEnabled setMode() is error! ");
			e.printStackTrace();
		}
        return false;
    }

    private static int getUid(Context context) {
        try {
            int uid = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_SERVICES).uid;
            Log.v(TAG, "oooMMS AppOpsManager getUid() = " + uid + ", PackageName = " + context.getPackageName());
            return uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

