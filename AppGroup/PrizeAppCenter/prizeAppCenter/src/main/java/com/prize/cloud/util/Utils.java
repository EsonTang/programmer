/*
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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.task.pojo.LoginInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 *
 * @author huangchangguo
 * @version 1.9+
 */
public class Utils {

    /**
     * 保存所有个人信息于数据库
     *
     * @param ctx  Context
     * @param info LoginInfo
     */
    public static void saveInfo(Context ctx, LoginInfo info) {
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_person");
        ctx.getContentResolver().insert(uri, info2Values(info));
    }

    /**
     * 个人信息对象转换为ContentValues
     *
     * @param info  LoginInfo
     * @return  ContentValues
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
     *
     * @param ctx  Context
     * @param values  ContentValues
     */
    public static void saveAccount(Context ctx, ContentValues values) {
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_account");
        ctx.getContentResolver().insert(uri, values);
    }

    /**
     * 获取当前账号
     *
     * @param ctx  Context
     * @return 含有账号信息的对象
     */
    public static CloudAccount curAccount(Context ctx) {
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_account");
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

//    /**
//     * 获取个人信息
//     *
//     * @param ctx
//     * @return
//     */
//    public static Person getPersonalInfo(Context ctx) {
//        Uri uri = Uri
//                .parse("content://com.prize.appcenter.provider.appstore/table_person");
//        Cursor cs = ctx.getContentResolver().query(uri, null, null, null, null);
//        Person person = null;
//        if (cs != null && cs.moveToFirst()) {
//            person = new Person();
//            person.setAvatar(cs.getString(cs.getColumnIndex("avatar")));
//            person.setEmail(cs.getString(cs.getColumnIndex("email")));
//            person.setPhone(cs.getString(cs.getColumnIndex("phone")));
//            person.setRealName(cs.getString(cs.getColumnIndex("realName")));
//            person.setUserId(cs.getString(cs.getColumnIndex("userId")));
//            person.setSex(cs.getInt(cs.getColumnIndex("sex")));
//        }
//        if (cs != null) {
//            cs.close();
//        }
//        return person;
//    }

    /**
     * 仅更新email
     *
     * @param ctx  Context
     * @param email  邮箱
     * @return  更新信息是否成功
     */
    public static int updateEmail(Context ctx, String email) {
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/email");
        ContentValues values = new ContentValues();
        values.put("email", email);
        return ctx.getContentResolver().update(uri, values, null, null);
    }

    /**
     * 注销，将删除数据库中个人信息
     *
     * @param ctx Context
     */
    public static void logout(Context ctx) {
        Uri uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_person");
        ctx.getContentResolver().delete(uri, null, null);
        uri = Uri
                .parse("content://com.prize.appcenter.provider.appstore/table_account");
        ctx.getContentResolver().delete(uri, null, null);
        ctx.sendBroadcast(new Intent(CloudIntent.ACTION_LOGOUT));
    }

    /**
     * md5加密
     *
     * @param val 待加密字符串
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


//    /**
//     * email校验
//     *
//     * @param email
//     * @return
//     */
//    public static boolean isEmail(String email) {
//        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
//        Pattern p = Pattern.compile(str);
//        Matcher m = p.matcher(email);
//        return m.matches();
//    }

    /**
     * 电话号码校验
     *
     * @param phone 电话号码
     * @return  boolean
     */
    public static boolean isPhone(String phone) {
        String str = "^(0|86|17951)?(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(phone);
        return m.matches();
    }



    /**
     * 获取bool值
     *
     * @param ctx  Context
     * @param key  key
     * @return  boolean
     */
    public static boolean getBoolean(Context ctx, String key) {
        return ctx.getSharedPreferences("cloud_prefs", 0)
                .getBoolean(key, false);
    }
//
//    private static boolean isBootActivate(Context ctx) {
//        Context otherCtx;
//        try {
//            otherCtx = ctx.createPackageContext("com.prize.boot",
//                    Context.CONTEXT_IGNORE_SECURITY);
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//            return false;
//        }
//        SharedPreferences sp = otherCtx.getSharedPreferences("boot_prefs",
//                Context.MODE_WORLD_READABLE);
//        return sp.getBoolean("boot", false);
//    }

    /**
     * 验证手机号码格式
     */
    public static boolean isPhoneNum(String mobilesNum) {
        /*
         * 移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
		 * 联通：130、131、132、152、155、156、185、186 电信：133、153、180、189、（1349卫通）
		 * 第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
		 */
        //因为最新号码出现了14**** 17***，因此只判断1开头的11位  modify by:龙宝修 2017/6/12
        String telRegex = "[1]\\d{10}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        return !TextUtils.isEmpty(mobilesNum) && mobilesNum.matches(telRegex);
    }

    /**
     * 防止快速点击
     */
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

//    /**
//     * 注册成功后返回最开始的界面
//     *
//     * @param activity  Activity
//     * @param isRegist  boolean
//     */
//    public static void onComplete(Activity activity, boolean isRegist) {
//
//        boolean booting = Utils.isBootActivate(activity);
//
//        if (!booting && !isRegist) {
//
//            JLog.d("Utils-------", "onComplete--!booting");
//            Intent it = new Intent(activity, LoginActivityNew.class);
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            activity.startActivity(it);
//            AppManager.getAppManager().finishAllActivity();
//        } else {
//            Intent it = new Intent();
//            ComponentName comp = new ComponentName("com.prize.boot",
//                    "com.prize.boot.OtherSetActivity");
//            it.setComponent(comp);
//            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            activity.startActivity(it);
//        }
//        activity.finish();
//    }

}
