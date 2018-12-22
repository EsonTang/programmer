package com.prize.appcenter.service;

import android.content.Context;
import android.os.AsyncTask;

import com.prize.app.util.CommonUtils;
import com.prize.appcenter.R;

/***
 * 初始化安装的应用
 *
 * @author fanjunchen
 *
 */
public class InitInstalledAppTask extends AsyncTask<Void, Void, Boolean> {

    private Context ctx;

    private static boolean isRun = false;

    public InitInstalledAppTask(Context c) {
        ctx = c;
    }

    public static boolean isRun() {
        return isRun;
    }

    @Override
    protected void onPreExecute() {
        isRun = true;
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String sys = ctx.getString(R.string.installed_app);
        return CommonUtils.inert2DB(ctx, sys);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        isRun = false;
        super.onPostExecute(result);
    }
}
