
package com.prize.qihoo.cleandroid.sdk.update;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;

import com.prize.qihoo.cleandroid.sdk.SDKEnv;

public class UpdateUtils {
    private final static boolean DEBUG = SDKEnv.DEBUG;

    private final static String TAG = DEBUG ? "UpdateUtils" : UpdateUtils.class.getSimpleName();

    private static final String TIMESTAMP_EXT = ".timestamp";

    // 对于打包的文件，都是放在 assets 目录的，时间戳自然也在 assets 目录
    public static long getBundleTimestamp(Context c, String filename) {
        InputStream fis = null;
        try {
            fis = c.getAssets().open(filename + TIMESTAMP_EXT);
        } catch (Exception e) {
        }

        if (fis != null) {
            return getTimestampFromStream(fis);
        } else {
            return 0;
        }
    }

    private static long getTimestampFromStream(InputStream fis) {

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(fis);
            String s = dis.readLine();
            return Long.parseLong(s);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }

        return 0;
    }

    /**
     * 保存文件的时间戳，升级的时候需要判断
     **/
    public static void setFileTimestamp(Context c, String filename, long timeStamp) {
        FileOutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fos = c.openFileOutput(filename + TIMESTAMP_EXT, Context.MODE_PRIVATE);
            dos = new DataOutputStream(fos);
            dos.writeBytes(String.valueOf(timeStamp));
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }
    }
}
