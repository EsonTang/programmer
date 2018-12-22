package com.prize.qihoo.cleandroid.sdk.plugins;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

import com.qihoo360.mobilesafe.opti.i.plugins.ISharedPreferences;

public class SharedPreferencesImpl implements ISharedPreferences {
    private final Context mContext;

    public SharedPreferencesImpl(Context context) {
        mContext = context;
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return new IpcSharedPreferences(mContext, "shared_pref_clear_sdk");
    }

    @Override
    public SharedPreferences getSharedPreferences(String name) {
        return new IpcSharedPreferences(mContext, name);
    }

    /**
     * 若清理SDK被多个进程调用时，建议实现多进程存储方案，避免多进程导致的数据丢失
     */
    public static class IpcSharedPreferences implements SharedPreferences {

        private final SharedPreferences mSharedPreferences;

        public IpcSharedPreferences(Context context, String name) {
            mSharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        }

        @Override
        public Map<String, ?> getAll() {
            return mSharedPreferences.getAll();
        }

        @Override
        public String getString(String key, String defValue) {
            return mSharedPreferences.getString(key, defValue);
        }

        @Override
        public int getInt(String key, int defValue) {
            return mSharedPreferences.getInt(key, defValue);
        }

        @Override
        public long getLong(String key, long defValue) {
            return mSharedPreferences.getLong(key, defValue);
        }

        @Override
        public float getFloat(String key, float defValue) {
            return mSharedPreferences.getFloat(key, defValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return mSharedPreferences.getBoolean(key, defValue);
        }

        @Override
        public boolean contains(String key) {
            return mSharedPreferences.contains(key);
        }

        @Override
        public Editor edit() {
            return mSharedPreferences.edit();
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public Set<String> getStringSet(String arg0, Set<String> arg1) {
            return mSharedPreferences.getStringSet(arg0, arg1);
        }
    }
}
