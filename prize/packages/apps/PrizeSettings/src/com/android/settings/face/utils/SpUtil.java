package com.android.settings.face.utils;

/**
 * Created by Administrator on 2017/10/25.
 */

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import java.io.File;
import java.lang.reflect.Field;

public class SpUtil {
    //存储的sharedpreferences文件名
    private static final String FILE_NAME = "faceid_sp";

    /**
     * 保存数据到文件
     */
    public static void saveData(Context context, String key, Object data) {

        try {

            /*Field field;
            field = ContextWrapper.class.getDeclaredField("mBase");
            field.setAccessible(true);
            Object obj = field.get(context);
            field = obj.getClass().getDeclaredField("mPreferencesDir");
            field.setAccessible(true);
            File file = new File("/data/system/users/faceid/");
            field.set(obj, file);*/

            String type = data.getClass().getSimpleName();
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if ("Integer".equals(type)) {
                editor.putInt(key, (Integer) data);
            } else if ("Boolean".equals(type)) {
                editor.putBoolean(key, (Boolean) data);
            } else if ("String".equals(type)) {
                editor.putString(key, (String) data);
            } else if ("Float".equals(type)) {
                editor.putFloat(key, (Float) data);
            } else if ("Long".equals(type)) {
                editor.putLong(key, (Long) data);
            }

            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 从文件中读取数据
     */
    public static Object getData(Context context, String key, Object defValue) {

        try {
            /*Field field;
            field = ContextWrapper.class.getDeclaredField("mBase");
            field.setAccessible(true);
            Object obj = field.get(context);
            field = obj.getClass().getDeclaredField("mPreferencesDir");
            field.setAccessible(true);
            File file = new File("/data/system/users/faceid/");
            field.set(obj, file);*/

            String type = defValue.getClass().getSimpleName();
            SharedPreferences sharedPreferences =
                    context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

            //defValue为为默认值，如果当前获取不到数据就返回它
            if ("Integer".equals(type)) {
                return sharedPreferences.getInt(key, (Integer) defValue);
            } else if ("Boolean".equals(type)) {
                return sharedPreferences.getBoolean(key, (Boolean) defValue);
            } else if ("String".equals(type)) {
                return sharedPreferences.getString(key, (String) defValue);
            } else if ("Float".equals(type)) {
                return sharedPreferences.getFloat(key, (Float) defValue);
            } else if ("Long".equals(type)) {
                return sharedPreferences.getLong(key, (Long) defValue);
            }

            return null;
        } catch (Exception e) {
            return defValue;
        }
    }
}
