package com.mediatek.telecom.recording;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;


import com.mediatek.storage.StorageManagerEx;

import java.io.File;

public class RecorderUtils {
    private static final String TAG = "PhoneRecorderUtils";
    public static boolean diskSpaceAvailable(long sizeAvailable) {
        return (getDiskAvailableSize() - sizeAvailable) > 0;
    }

    public static boolean diskSpaceAvailable(String defaultPath, long sizeAvailable) {
        if (null == defaultPath) {
            return diskSpaceAvailable(sizeAvailable);
        } else {
            File sdCardDirectory = new File(defaultPath);
            StatFs statfs;
            try {
                if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                    statfs = new StatFs(sdCardDirectory.getPath());
                } else {
                //    log("-----diskSpaceAvailable: sdCardDirectory is null----");
                    return false;
                }
            } catch (IllegalArgumentException e) {
             //   log("-----diskSpaceAvailable: IllegalArgumentException----");
                return false;
            }
            long blockSize = statfs.getBlockSize();
            long availBlocks = statfs.getAvailableBlocks();
            long totalSize = blockSize * availBlocks;
            return (totalSize - sizeAvailable) > 0;
        }
    }

    public static boolean isExternalStorageMounted(Context context) {
        StorageManager storageManager =
                (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (null == storageManager) {
           // log("-----story manager is null----");
            return false;
        }
        String storageState = storageManager.getVolumeState(StorageManagerEx.getDefaultPath());
        return storageState.equals(Environment.MEDIA_MOUNTED) ? true : false;
    }

    public static String getExternalStorageDefaultPath() {
        return StorageManagerEx.getDefaultPath();
    }

    public static long getDiskAvailableSize() {
        File sdCardDirectory = new File(StorageManagerEx.getDefaultPath());
        StatFs statfs;
        try {
            if (sdCardDirectory.exists() && sdCardDirectory.isDirectory()) {
                statfs = new StatFs(sdCardDirectory.getPath());
            } else {
             //   log("-----diskSpaceAvailable: sdCardDirectory is null----");
                return -1;
            }
        } catch (IllegalArgumentException e) {
         //   log("-----diskSpaceAvailable: IllegalArgumentException----");
            return -1;
        }
        long blockSize = statfs.getBlockSize();
        long availBlocks = statfs.getAvailableBlocks();
        long totalSize = blockSize * availBlocks;
        return totalSize;
    }

    public static boolean isStorageAvailable(Context context) {
        if (!isExternalStorageMounted(context)) {
            Log.e(TAG, "-----Please insert an SD card----");
            return false;
        }

        if (!diskSpaceAvailable(
                PhoneRecorderHandler.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
            Log.e(TAG, "-----SD card storage is full----");
            return false;
        }

        return true;
    }
}
