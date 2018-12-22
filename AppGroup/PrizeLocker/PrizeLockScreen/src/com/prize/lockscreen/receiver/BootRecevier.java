
package com.prize.lockscreen.receiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.prize.ext.res.ResHelper;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.service.LockScreenService;
import com.prize.theme.db.Tables;
/***
 * 系统启动广播
 * @author fanjunchen
 *
 */
public class BootRecevier extends BroadcastReceiver {

    final static String TAG = "BootRecevier";
    final static String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
    	String act = intent.getAction();
        if (BOOT_ACTION.equals(act)) {
//			Intent mIntent = new Intent(context, BootActivity.class );
//			mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			mIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED); 
//			context.startActivity(mIntent);
        	Intent itService = new Intent(context, LockScreenService.class);
        	context.startService(itService);
        }
        else if (Tables.BRD_ACTION_APPLY_THEME.equals(act)) {
        	String pkg = intent.getStringExtra(Tables.BRD_EXTRA);
        	ResHelper.getInstance(context).changePkg(pkg);
        }
        else if (Tables.BRD_ACTION_UNINSTALL_DEL.equals(act)) {
        	String pkg = intent.getStringExtra(Tables.BRD_EXTRA);
        	ResHelper.getInstance(context).changeLockConfig(pkg);
        }
        else if (Tables.BRD_ACTION_APPLY_LOCK.equals(act)) {
        	int type = intent.getIntExtra(Tables.BRD_EXTRA_TYPE, 1);
        	String str = intent.getStringExtra(Tables.BRD_EXTRA);
        	new GetImg(type, str, context).execute();
        }
    }
}
