package com.prize.uploadappinfo.threads;

import android.content.Context;
import android.os.AsyncTask;

import com.prize.uploadappinfo.utils.CommonUtils;

/***
 * 初始化手机安装的所有3方应用
 * 
 * @author prize
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
		return CommonUtils.inert2DB(ctx);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		isRun = false;
		super.onPostExecute(result);
	}
}
