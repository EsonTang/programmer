package com.prize.appcenter.ui.util;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.prize.app.util.JLog;
import com.prize.appcenter.R;
import com.prize.appcenter.activity.FolderActivity;

import java.io.ByteArrayOutputStream;

/**
 * 快捷方式的工具方法
 *
 * @author prize
 */
public class ShortcutUtil {
    private static final String[] PROJECTION = {"_id", "title", "icon", "componentname"};

    public static void createShortCut(Context appContext, String shortcutName) {
        Intent intent = new Intent();
//        registerContentObserver(appContext, shortcutName);
//        intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 添加这一项，确保只创建一次

        intent.setAction(Intent.ACTION_MAIN);// Intent.ACTION_MAIN为了在卸载应用的时候同时删除桌面快捷方式
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);// Intent.CATEGORY_LAUNCHER为了在卸载应用的时候同时删除桌面快捷方式
        intent.setClass(appContext, FolderActivity.class);
        // 创建快捷的intent
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        // 不允许重复创建
        shortcutintent.putExtra("duplicate", false);
        // 需要现实的名称
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        // 快捷图片
//        Parcelable icon = Intent.ShortcutIconResource.fromContext(appContext,
//                R.drawable.ic_al_no_app);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON,  resources2Bitmap(appContext));
//        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        // 点击快捷图片，运行的程序主入口
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        // 发送广播
        appContext.sendBroadcast(shortcutintent);
        ToastUtils.showToast("已为您设置快捷键至桌面");
    }

//    private static void registerContentObserver(final Context appContext, final String shortcutName) {
//        // 为uri的数据改变注册监听器
//        String AUTHORITY = "com.android.launcher2.settings";
//        if (android.os.Build.VERSION.SDK_INT < 8) {
//            AUTHORITY = "com.android.launcher.settings";
//        } else if (android.os.Build.VERSION.SDK_INT < 19) {
//            AUTHORITY = "com.android.launcher2.settings";
//        } else {
//            AUTHORITY = "com.android.launcher3.settings";
//        }
//        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
//                + "/favorites?notify=true");
//        appContext.getContentResolver().registerContentObserver(CONTENT_URI, true,
//                new ContentObserver(new Handler()) {
//                    @Override
//                    public void onChange(boolean selfChange, Uri uri) {
//                        JLog.i("MainActivity", "registerContentObserver-onChange");
//                        // 查询发送邮箱中的短息(处于正在发送状态的短信放在发送箱)
//                        if (hasShortcut(appContext)) {
//                            ToastUtils.showToast("已为您设置快捷键至桌面");
//                        }
//                        appContext.getContentResolver().unregisterContentObserver(this);
//                    }
//                });
//    }

    public static boolean hasShortcut(Context appContext) {
        final ContentResolver cr = appContext.getContentResolver();
        String AUTHORITY = "com.android.launcher2.settings";
        if (android.os.Build.VERSION.SDK_INT < 8) {
            AUTHORITY = "com.android.launcher.settings";
        } else if (android.os.Build.VERSION.SDK_INT < 19) {
            AUTHORITY = "com.android.launcher2.settings";
        } else {
            AUTHORITY = "com.android.launcher3.settings";
        }

        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        try {
            ComponentName mComponent = new ComponentName(appContext, FolderActivity.class);
            Cursor cursor = cr.query(CONTENT_URI, PROJECTION, "componentname=?",
                    new String[]{mComponent.toString()}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int conut = cursor.getColumnCount();
                for (int i = 0; i < conut; i++) {
                    JLog.i("MainActivity", "ColumnName[" + i + "]=" + cursor.getColumnName(i));
                    if (i != 2) {
                        JLog.i("MainActivity", cursor.getString(i) == null ? "kong" : cursor.getString(i));
                    }
                }
                cursor.close();
                return true;
            }
        } catch (Exception e) {
            JLog.i("MainActivity", "hasShortcut-e=" + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static void delShortCut(Context appContext, String shortcutName) {
        // intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        // 快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        /* 删除和创建需要对应才能找到快捷方式并成功删除 **/
        Intent intent = new Intent();
//        intent.setClass(appContext, FolderActivity.class);
        ComponentName comp = new ComponentName(appContext,
                FolderActivity.class);
        intent.setComponent(comp);
        intent.setAction(Intent.ACTION_MAIN);// Intent.ACTION_MAIN为了在卸载应用的时候同时删除桌面快捷方式

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        appContext.sendBroadcast(shortcut);
    }

    public static void updateShortCut(Context appContext, String shortcutName) {
        ComponentName mComponent = new ComponentName(appContext, FolderActivity.class);
        final ContentResolver cr = appContext.getContentResolver();
        String AUTHORITY;
        if (android.os.Build.VERSION.SDK_INT < 8) {
            AUTHORITY = "com.android.launcher.settings";
        } else if (android.os.Build.VERSION.SDK_INT < 19) {
            AUTHORITY = "com.android.launcher2.settings";
        } else {
            AUTHORITY = "com.android.launcher3.settings";
        }
        Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
        try {
            Cursor cursor = cr.query(CONTENT_URI, PROJECTION, "title=? and componentname=?",
                    new String[]{shortcutName, mComponent.toString()}, null);
            if (JLog.isDebug) {
                JLog.i("MainActivity", "updateShortCut-cursor=" + (cursor == null) + "--mComponen=" + mComponent.toString());
            }
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getInt(0);
                if (JLog.isDebug) {
                    JLog.i("MainActivity", "index=" + index);
                }
                Uri uri2 = Uri.parse("content://" + AUTHORITY + "/favorites/" + index + "?notify=true");
                ContentValues values = new ContentValues();
                values.put("icon", bitmap2Bytes(appContext));
                values.put("title", "test");
                cr.update(uri2, values, null, null);
                appContext.getContentResolver().notifyChange(CONTENT_URI, null);
                cursor.close();
            }
        } catch (Exception e) {
            JLog.i("MainActivity", "updateShortCut-e=" + e.getMessage());
            e.printStackTrace();
        }

    }

    public static Bitmap resources2Bitmap(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.ic_al_no_app);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bmp.compress(Bitmap.CompressFormat.PNG, 90, baos);
//        return baos.toByteArray();
    }
    public static byte[] bitmap2Bytes(Context context) {
        Resources res = context.getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.ic_al_no_app);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 90, baos);
        return baos.toByteArray();
    }
}
