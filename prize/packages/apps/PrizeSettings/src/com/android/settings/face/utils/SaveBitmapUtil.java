package com.android.settings.face.utils;

/**
 * Created by Administrator on 2017/10/25.
 */

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveBitmapUtil {

    public static void saveBitmap2Sdcard(String fileName, Bitmap bmp) {

        if (bmp == null || fileName == null) {
            return;
        }

        FileOutputStream fos = null;

        try {
            File dir = new File(Environment.getExternalStorageDirectory().toString() + "/faceidfail/");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File bitmapFile = new File(Environment.getExternalStorageDirectory().toString() + "/faceidfail/" + fileName + ".jpg");
            bitmapFile.createNewFile();
            fos = new FileOutputStream(bitmapFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {

                }
            }
        }

    }
}
