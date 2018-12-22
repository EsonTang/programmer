package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.android.prize.simple.model.PagedDataModel;
import com.android.prize.simple.table.ItemTable;

public class PackageChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        final String packageName = intent.getData().getSchemeSpecificPart();

        if (packageName == null || packageName.length() == 0) {
            // they sent us a bad intent
            return;
        }
        // prize fanjunchen added {
        String act = intent.getAction();
        if ("android.intent.action.PACKAGE_REMOVED".equals(act)) {
        	if (!isSystemApplication(context, packageName)) {
        		// delete from db
        		try {
        			ItemTable it = LauncherApplication.getDbManager().selector(ItemTable.class).where("pkgName", "=", packageName).findFirst();
        			if (it != null) {
        				if (PagedDataModel.getInstance() != null) {
        					PagedDataModel.getInstance().delItem(it, false);
        				}
        				else { // 没有进入到老人主题
        					PagedDataModel.delItem(it);
        				}
        			}
				} catch (Exception e) {
					e.printStackTrace();
				}
        		Intent it1 = new Intent(PagedDataModel.REC_APP_DEL);
            	it1.putExtra(PagedDataModel.P_PKG, packageName);
            	context.sendBroadcast(it1);
        	}
        }
        else if (Intent.ACTION_PACKAGE_ADDED.equals(act)) {
        	Intent it1 = new Intent(PagedDataModel.REC_APP_ADD);
        	it1.putExtra(PagedDataModel.P_PKG, packageName);
        	context.sendBroadcast(it1);
        	
        	it1 = null;
        }
        // end added }
        // in rare cases the receiver races with the application to set up LauncherAppState
        LauncherAppState.setApplicationContext(context.getApplicationContext());
        LauncherAppState app = LauncherAppState.getInstance();
        WidgetPreviewLoader.removePackageFromDb(app.getWidgetPreviewCacheDb(), packageName);
    }
    /***
     * 判断是否为系统应用
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isSystemApplication(Context context, String packageName){
    	PackageManager manager = context.getPackageManager();
        try {
			PackageInfo packageInfo = manager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
			if((packageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM)!=0){
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
    }
}
