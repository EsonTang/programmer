package com.prize.cloud.helper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

public class PrizeHelper {

	/**
	 * 获取登录后的个人信息
	 * 
	 * @param ctx
	 * @return
	 */
	public static PersonalInfo getPersonalInfo(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.provider.cloud/person");
		Cursor cs = ctx.getContentResolver().query(uri, null, null, null, null);
		PersonalInfo person = null;
		if (cs != null && cs.moveToFirst()) {
			person = new PersonalInfo();
			person.setAvatar(cs.getString(cs.getColumnIndex("avatar")));
			person.setEmail(cs.getString(cs.getColumnIndex("email")));
			person.setPhone(cs.getString(cs.getColumnIndex("phone")));
			person.setRealName(cs.getString(cs.getColumnIndex("realName")));
			person.setUserId(cs.getString(cs.getColumnIndex("userId")));
			person.setSex(cs.getInt(cs.getColumnIndex("sex")));
		}
		if (cs != null) {
			cs.close();
		}
		return person;
	}

	/**
	 * 获取账号信息，获得userId和passport，无法获取到密码
	 * 
	 * @param ctx
	 * @return
	 */
	public static PrizeAccount curAccount(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.provider.cloud/account");
		Cursor cs = ctx.getContentResolver().query(uri, null, null, null, null);
		PrizeAccount account = null;
		if (cs != null && cs.moveToFirst()) {
			account = new PrizeAccount();
			account.setLoginName(cs.getString(cs.getColumnIndex("loginName")));
			account.setPassport(cs.getString(cs.getColumnIndex("passport")));
		}
		if (cs != null) {
			cs.close();
		}
		return account;
	}

	/**
	 * passport过期时调用此方法. passport更新成功后，将发送更新的广播({@link #CloudIntent.ACTION_PASSPORT_GET}
	 * param:"passport")
	 * 
	 * @param ctx
	 */
	public static void doActivate(Context ctx) {
		Intent it = new Intent(CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND);
		ComponentName component = new ComponentName("com.prize.cloud",
				"com.prize.cloud.service.AccountService");
		it.setComponent(component);
		ctx.startService(it);
	}

	/**
	 * 直接启动云账户，自带逻辑判断跳转至何界面
	 */
	public static void startCloud(Context ctx) {
		Intent intent = new Intent();
		PackageManager packageManager = ctx.getPackageManager();
		intent = packageManager.getLaunchIntentForPackage("com.prize.cloud");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// ComponentName comp = new ComponentName("com.prize.cloud",
		// "com.prize.cloud.MainActivity");
		// intent.setComponent(comp);
		ctx.startActivity(intent);
	}

}
