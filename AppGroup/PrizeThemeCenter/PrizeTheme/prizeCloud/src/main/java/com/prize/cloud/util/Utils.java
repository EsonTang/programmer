/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.prize.cloud.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.bean.Person;
import com.prize.cloud.task.pojo.LoginInfo;

/**
 * 工具类
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class Utils {

	/**
	 * 保存所有个人信息于数据库
	 * @param ctx
	 * @param info
	 */
	public static void saveInfo(Context ctx, LoginInfo info) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
		ctx.getContentResolver().insert(uri, info2Values(info));
	}

	/**
	 * 个人信息对象转换为ContentValues
	 * @param info
	 * @return
	 */
	private static ContentValues info2Values(LoginInfo info) {
		ContentValues values = new ContentValues();
		try {
			values.put("avatar", info.getAvatar());
			values.put("email", info.getEmail());
			values.put("realName", info.getRealName());
			values.put("userId", info.getUserId());
			values.put("phone", info.getPhone());
			values.put("sex", info.getGender());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return values;
	}

	/**
	 * 保存账号信息
	 * @param ctx
	 * @param values
	 */
	public static void saveAccount(Context ctx, ContentValues values) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_account");
		ctx.getContentResolver().insert(uri, values);
	}

	/**
	 * 获取当前账号
	 * @param ctx
	 * @return 含有账号信息的对象
	 */
	public static CloudAccount curAccount(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_account");
		Cursor cs = ctx.getContentResolver().query(uri, null, null, null, null);
		CloudAccount account = null;
		if (cs != null && cs.moveToFirst()) {
			account = new CloudAccount();
			account.setLoginName(cs.getString(cs.getColumnIndex("loginName")));
			account.setPassword(cs.getString(cs.getColumnIndex("password")));
			account.setPassport(cs.getString(cs.getColumnIndex("passport")));
		}
		if (cs != null) {
			cs.close();
			cs = null;
		}
		return account;
	}

	/**
	 * 获取个人信息
	 * @param ctx
	 * @return
	 */
	public static Person getPersonalInfo(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
		Cursor cs = ctx.getContentResolver().query(uri, null, null, null, null);
		Person person = null;
		if (cs != null && cs.moveToFirst()) {
			person = new Person();
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
	 * 仅更新email
	 * @param ctx
	 * @param email
	 * @return
	 */
	public static int updateEmail(Context ctx, String email) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/email");
		ContentValues values = new ContentValues();
		values.put("email", email);
		return ctx.getContentResolver().update(uri, values, null, null);
	}

	/**
	 * 注销，将删除数据库中个人信息
	 * @param ctx
	 */
	public static void logout(Context ctx) {
		Uri uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_person");
		ctx.getContentResolver().delete(uri, null, null);
		uri = Uri.parse("content://com.prize.appcenter.provider.appstore/table_account");
		ctx.getContentResolver().delete(uri, null, null);
		ctx.sendBroadcast(new Intent(CloudIntent.ACTION_LOGOUT));
	}

	/**
	 * md5加密
	 * @param val待加密字符串
	 * @return 加密后字符串
	 */
	public static String getMD5(String val) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			return val;
		}
		md5.update(val.getBytes());
		byte[] m = md5.digest();
		return getString(m);
	}

	private static String getString(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(b[i]);
		}
		return sb.toString();
	}

	/**
	 * 判断网络连接
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * email校验
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email) {
		String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(email);
		return m.matches();
	}

	/**
	 * 电话号码校验
	 * @param phone
	 * @return
	 */
	public static boolean isPhone(String phone) {
		String str = "^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$";
		Pattern p = Pattern.compile(str);
		Matcher m = p.matcher(phone);
		return m.matches();
	}

	/**
	 * 获取本机号码
	 * @param ctx
	 * @return
	 */
	public static String getTel(Context ctx) {
		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		String tel;
		try {
			tel = tm.getLine1Number();
			String subTel = null;
			if (!TextUtils.isEmpty(tel)){
				if (tel.length()>11){
					subTel = tel.substring(3, tel.length());  //去掉电话号码前的+86国际区号
					return subTel;
				}
			}
			return tel;
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	/**
	 * 方法描述：获取sim卡状态是否可用
	 * @param ctx
	 * @return boolean
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static boolean getSimState(Context ctx){
		TelephonyManager mTelephonyManager=(TelephonyManager) ctx.getSystemService(Service.TELEPHONY_SERVICE);
		if(mTelephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {//SIM卡有没有就绪
			return true;
		}
		return false;
	}
	
	/**
	 * 方法描述：获得唯一串
	 * @param ctx
	 * @return String
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static String getUUID(Context ctx) {
		String imei;
		long currentTime = System.currentTimeMillis();
		UUID uuid = new DeviceUuidFactory(ctx).getDeviceUuid();
		try {
			TelephonyManager mTelephonyManager=(TelephonyManager) ctx.getSystemService(Service.TELEPHONY_SERVICE);
			imei = mTelephonyManager.getDeviceId();						
			if (imei != null) {
				return getMD5(imei + currentTime + uuid.toString());
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getMD5(currentTime + uuid.toString());
	}
	
	/**
	 * 保存bool于SharedPreferences
	 * @param ctx
	 * @param key
	 * @param value
	 */
	public static void savePref(Context ctx, String key, boolean value) {
		SharedPreferences settings = ctx.getSharedPreferences("cloud_prefs", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 获取bool值
	 * @param ctx
	 * @param key
	 * @return
	 */
	public static boolean getBoolean(Context ctx, String key) {
		return ctx.getSharedPreferences("cloud_prefs", 0)
				.getBoolean(key, false);
	}

	public static boolean isBootActivate(Context ctx) {
		Context otherCtx;
		try {
			otherCtx = ctx.createPackageContext("com.prize.boot",
					Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		SharedPreferences sp = otherCtx.getSharedPreferences("boot_prefs",
				Context.MODE_WORLD_READABLE);
		return sp.getBoolean("boot", false);
	}
	
}
