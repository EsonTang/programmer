package com.prize.runoldtest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.prize.runoldtest.util.Const;
import com.prize.runoldtest.util.DataUtil;
import com.prize.runoldtest.util.LogUtil;
import com.prize.runoldtest.util.SystemProperties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

public class EmmcActivity extends Activity {
    private long emmc_time;
    public static boolean createFile;
    private PowerManager.WakeLock wakeLock = null;
    public enum FileUnit {
        KB, MB, GB
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emmc);
        DataUtil.addDestoryActivity(EmmcActivity.this, "EmmcActivity");
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "My TAG");
        wakeLock.acquire();
        LogUtil.e("EmmcActivity OnCreate()");
        Intent intent=getIntent();
        emmc_time = intent.getIntExtra(Const.EXTRA_MESSAGE,0);
    }

    protected void onStart() {
        super.onStart();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (emmc_time-- != 0){
                	LogUtil.e("write file id = " + emmc_time);
                    try {
                        String filePath_KB = "/sdcard/eMMCTest_KB.txt";
                        int fileSize = 10;
                        createFile = createFile(filePath_KB, fileSize, FileUnit.KB);

                        String filePath_MB = "/sdcard/eMMCTest_MB.txt";
                        createFile = createFile(filePath_MB, fileSize, FileUnit.MB);

                        String filePath_GB = "/sdcard/eMMCTest_GB.txt";
                        createFile = createFile(filePath_GB, fileSize, FileUnit.GB);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ManualTestActivity.FlagMem = true;
                        if (!createFile){
                            SystemProperties.set(Const.SYSPROPERTY_RUN_TEST_EMMC, Const.RUNIN_TEST_FAIL + "");
                        }
                        delFile("/sdcard/eMMCTest_KB.txt");
                        delFile("/sdcard/eMMCTest_MB.txt");
                        delFile("/sdcard/eMMCTest_GB.txt");
                        finish();
                    }
                });
            }
        });
        t.start();
    }

    public void onPause() {
        super.onPause();
        wakeLock.release();
    }

    public static void delFile(String fileName){
        File file = new File(fileName);
        if(file.isFile()){
            file.delete();
        }
        file.exists();
    }


    public boolean createFile(String targetFile, long fileLength, FileUnit unit) {
        long KBSIZE = 1024;
        long MBSIZE1 = 1024 * 1024;
        long MBSIZE10 = 1024 * 1024 * 10;
        switch (unit) {
            case KB:
                fileLength = fileLength * 1024;
                break;
            case MB:
                fileLength = fileLength * 1024*1024;
                break;
            case GB:
                fileLength = fileLength * 1024*1024*1024;
                break;

            default:
                break;
        }
        FileOutputStream fos = null;
        File file = new File(targetFile);
        try {

            if (!file.exists()) {
                file.createNewFile();
            }

            long batchSize = 0;
            batchSize = fileLength;
            if (fileLength > KBSIZE) {
                batchSize = KBSIZE;
            }
            if (fileLength > MBSIZE1) {
                batchSize = MBSIZE1;
            }
            if (fileLength > MBSIZE10) {
                batchSize = MBSIZE10;
            }
            long count = fileLength / batchSize;
            long last = fileLength % batchSize;

            fos = new FileOutputStream(file);
            FileChannel fileChannel = fos.getChannel();
            for (int i = 0; i < count; i++) {
                ByteBuffer buffer = ByteBuffer.allocate((int) batchSize);
                fileChannel.write(buffer);

            }
            if (last != 0) {
                ByteBuffer buffer = ByteBuffer.allocate((int) last);
                fileChannel.write(buffer);
            }
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
