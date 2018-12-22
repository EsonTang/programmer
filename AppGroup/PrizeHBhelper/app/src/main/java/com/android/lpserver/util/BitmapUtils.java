package com.android.lpserver.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public final class BitmapUtils {

    private BitmapUtils() {}

    public static boolean saveBitmap(Context context, File output, Bitmap bitmap) {
        if(output.exists()) {
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            insertMedia(context, output, "image/jpeg");
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if(fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {}

            }
        }


    }

    /** insert into the system gallery*/
    private static void insertMedia(Context context, File output, String mime) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DATA, output.getAbsolutePath());
            values.put(MediaStore.Video.Media.MIME_TYPE, mime);
            context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));
        } catch (Exception e){}
    }
}
