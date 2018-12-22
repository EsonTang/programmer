package com.prize.appcenter.service;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.prize.app.BaseApplication;
import com.prize.app.download.AppManagerCenter;
import com.prize.app.util.CommonUtils;
import com.prize.app.util.DataStoreUtils;
import com.prize.app.util.JLog;
import com.prize.statistics.PrizeStatService;

import java.util.Properties;


/***
 * 初始化安装的应用
 *
 * @author fanjunchen
 *
 */
public class InitActivitedAppTask extends AsyncTask<String, Void, String> {


    public InitActivitedAppTask() {
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String hasUpLoadApp = DataStoreUtils.readLocalInfo("hasUpLoadApp");
        String activitedApp = params[0];
        StringBuilder upLoadActivitedApp = new StringBuilder();
        String[] activitedAppArray = activitedApp.split("#");
        for (String packageName : activitedAppArray) {
            if (AppManagerCenter.isAppExist(packageName)) {
                if (!CommonUtils.isAppActivated(packageName))
                    continue;
                if (!TextUtils.isEmpty(hasUpLoadApp)) {
                    if (hasUpLoadApp.contains(packageName)) {
                        continue;
                    }
                }
                upLoadActivitedApp.append(packageName).append(",");
            }

        }
        if (upLoadActivitedApp.length() <= 0) {
            return null;
        }
        if (JLog.isDebug) {
            JLog.i("InitActivitedAppTask", "upLoadActivitedApp=" + upLoadActivitedApp);
        }
        String result = upLoadActivitedApp.substring(0, upLoadActivitedApp.length() - 1);
        JLog.i("InitActivitedAppTask", "result=" + result);
        DataStoreUtils.saveLocalInfo("hasUpLoadApp", result);
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result == null)
            return;
        Properties prop = new Properties();
        prop.setProperty("packageNames", result);
        PrizeStatService.trackCustomKVEvent(BaseApplication.curContext, "app_actives", prop,false);
    }
}
