/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/* //device/content/providers/media/src/com/android/providers/media/MediaScannerService.java
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**     http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/

package com.android.providers.media;

import static android.media.MediaInserter.*;
import static com.android.providers.media.MediaUtils.*;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.IntentFilter;
import android.database.Cursor;

import android.media.IMediaScannerListener;
import android.media.IMediaScannerService;
import android.media.MediaScanner;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.UserManager;
import android.os.storage.StorageManager;

import android.os.storage.StorageVolume;
import android.os.SystemProperties;
import android.provider.MediaStore;

import com.android.internal.util.ArrayUtils;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;

public class MediaScannerService extends Service implements Runnable {
    private static final String TAG = "MediaScannerService";
    private static final boolean LOG = true;

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private PowerManager.WakeLock mWakeLock;
    private String[] mExternalStoragePaths;

    /**M: Added for SD hot-plug performance optimization.@{**/
    private final ArrayList<PrescanTask> mPrescanTaskList = new ArrayList<PrescanTask> ();

    /**
        * This asynctask invoked while:
        * -The external SD card mounted.
        *
        * It's work flow as:
        * -1.SD card mounted;
        * -2.Do prescan at first, deleted all the media db entities that files not existed
        *     on the mounted SD card, all the jobs runned in the worker thread;
        * -3.Prescan done, then scan the mounted external SD card and insert or update
        *     the media db if their has new or updated files existed on the mounted
        *     external SD card;
        */
    class PrescanTask extends AsyncTask<Void, Void, Void> {
        private Bundle mBundle = null;
        private Handler mHandler = null;
        private int mStartId = -1;
        private String mVolumn = null;

        public PrescanTask(Bundle bundle, Handler handler, int startId) {
            mBundle = bundle;
            mHandler = handler;
            mStartId = startId;
            Bundle tmpBundle = (Bundle)bundle.clone();
            mVolumn = tmpBundle.getString("volume");
        }

        protected String getPrescanVolume () {
            return mVolumn;
        }

        @Override
        protected Void doInBackground(Void... args){
            // do prescan operation in the worker thread
            MtkLog.d(TAG,"PrescanTask.doInBackground() mVolumn = " + mVolumn);
            if(mVolumn!= null && mVolumn.equals(MediaProvider.EXTERNAL_VOLUME)) {
                prescanSdCardRelated(MediaProvider.EXTERNAL_VOLUME);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            // start MediaScanner instance to scan mounted external SD card
            int what = MSG_SCAN_DIRECTORY;
            Message msg = mServiceHandler.obtainMessage(what, mStartId, -1, mBundle);
            mServiceHandler.sendMessage(msg);
            synchronized (MediaScannerService.this) {
                mPrescanTaskList.remove(this);
            }
            super.onPostExecute(arg);
        }
    }

    // Process the prescan operation for the mounted external SD card.
    private void prescanSdCardRelated(String volumn) {
        try {
            Uri uri = MediaStore.Files.getContentUri(MediaProvider.EXTERNAL_VOLUME);
            this.getContentResolver().call(uri, MediaUtils.ACTION_PRESCAN_STARTED, null, null);
            try (MediaScanner scanner = new MediaScanner(this, volumn)) {
                scanner.preScanAll(volumn);
            }
        } catch (Exception e) {
            MtkLog.e(TAG, "Exception in prescanSdCardRelated", e);
        } finally {
            // Notify the MediaProvider that prescan done
            Uri uri = MediaStore.Files.getContentUri(MediaProvider.EXTERNAL_VOLUME);
            this.getContentResolver().call(uri, MediaUtils.ACTION_PRESCAN_DONE, null, null);
            if (MediaUtils.LOG_SCAN) {
                MtkLog.d(TAG,"prescanSdCardRelated()");
            }
        }
    }
    /**@}**/

    private void openDatabase(String volumeName) {
        try {
            ContentValues values = new ContentValues();
            values.put("name", volumeName);
            getContentResolver().insert(Uri.parse("content://media/"), values);
        } catch (IllegalArgumentException ex) {
            MtkLog.w(TAG, "failed to open media database");
        }
    }

    private void scan(String[] directories, String volumeName) {
        Uri uri = Uri.parse("file://" + directories[0]);
        // don't sleep while scanning
        mWakeLock.acquire();

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MEDIA_SCANNER_VOLUME, volumeName);
            Uri scanUri = getContentResolver().insert(MediaStore.getMediaScannerUri(), values);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_STARTED, uri));

            try {
                if (volumeName.equals(MediaProvider.EXTERNAL_VOLUME)) {
                    openDatabase(volumeName);
                }

                try (MediaScanner scanner = new MediaScanner(this, volumeName)) {
                    scanner.scanDirectories(directories);
                }
            } catch (Exception e) {
                MtkLog.e(TAG, "exception in MediaScanner.scan()", e);
            }

            getContentResolver().delete(scanUri, null, null);

        } catch (Exception ex) {
            MtkLog.e(TAG, "exception in MediaScanner.scan()", ex);
        } finally {
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_FINISHED, uri));
            mWakeLock.release();
            if (MediaUtils.LOG_SCAN) {
                MtkLog.d(TAG, "scan(): volumeName = " + volumeName
                    + ", directories = " + Arrays.toString(directories));
            }
        }
    }

    /**
     * M: Scan given folder without do prescan.
     *
     * @param folders
     * @param volumeName
     */
    private void scanFolder(String[] folders, String volumeName) {
        /// don't sleep while scanning
        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        try {
            if (volumeName.equals(MediaProvider.EXTERNAL_VOLUME)) {
                openDatabase(volumeName);
            }
            try (MediaScanner scanner = new MediaScanner(this, volumeName)) {
                    scanner.scanFolders(folders, volumeName, false);
            }
        } catch (Exception e) {
            MtkLog.e(TAG, "exception in scanFolder", e);
        } finally {
            if (mWakeLock != null && mWakeLock.isHeld() && mMediaScannerThreadPool == null) {
                mWakeLock.release();
            }
            if (MediaUtils.LOG_SCAN) {
                MtkLog.d(TAG, "scanFolder(): volumeName = " + volumeName
                    + ", folders = " + Arrays.toString(folders));
            }
        }
    }

    @Override
    public void onCreate() {
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        mExternalStoragePaths = storageManager.getVolumePaths();

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Thread thr = new Thread(null, this, "MediaScannerService");
        thr.start();

        /// M: Register a unmount receiver to make sure pre-scan again when sdcard unmount at scanning.
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        filter.setPriority(100);
        registerReceiver(mUnmountReceiver, filter);

        mIsThreadPoolEnable = getCpuCoreNum() >= 4 && !isLowRamDevice();
        MtkLog.d(TAG, "onCreate: CpuCoreNum = " + getCpuCoreNum()
            + ", isLowRamDevice = " + isLowRamDevice()
            + ", mIsThreadPoolEnable = " + mIsThreadPoolEnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        while (mServiceHandler == null) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    MtkLog.e(TAG, "onStartCommand: InterruptedException!");
                }
            }
        }

        if (intent == null) {
            MtkLog.e(TAG, "Intent is null in onStartCommand: ",
                new NullPointerException());
            return Service.START_NOT_STICKY;
        }
        /**M:ALPS02342602-reinitial mExternalStoragePaths.@{**/
        StorageManager storageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        mExternalStoragePaths = storageManager.getVolumePaths();
        /**@}**/

        /// M: deliver different message for scan single file and directory
        Bundle arguments = intent.getExtras();
        int what;
        /**M: Added for SD hot-plug performance optimization.@{**/
        if (arguments.getString("filepath") != null) {
            what = MSG_SCAN_SINGLE_FILE;
            Message msg = mServiceHandler.obtainMessage(what, startId, -1, arguments);
            mServiceHandler.sendMessage(msg);
        } else {
            // Cancel un-finished task
            synchronized (MediaScannerService.this) {
                    Bundle tmpBundle = (Bundle)arguments.clone();
                    String curVolumn = tmpBundle.getString("volume");
                ArrayList<PrescanTask> cancelList = new ArrayList<PrescanTask> ();
                for (PrescanTask task : mPrescanTaskList) {
                    String taskVolumn = task.getPrescanVolume();
                    if (taskVolumn != null
                       && curVolumn != null
                       && taskVolumn.equals(curVolumn)) {
                        task.cancel(true);
                        cancelList.add(task);
                    }
                }
                mPrescanTaskList.remove(cancelList);
                PrescanTask prescanTask = new PrescanTask(arguments,mServiceHandler,startId);
                mPrescanTaskList.add(prescanTask);
                prescanTask.execute();
            }
        }
        /**@}**/

        // Try again later if we are killed before we can finish scanning.
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        MtkLog.d(TAG, "onDestroy");
        // Make sure thread has started before telling it to quit.
        while (mServiceLooper == null) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    MtkLog.e(TAG, "onDestroy: InterruptedException!");
                }
            }
        }
        mServiceLooper.quit();

        /// M: MediaScanner Performance turning {@
        /// If service has destroyed, we need release wakelock.
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        /// @}
        /// M: register at onCreate and unregister at onDestory
        unregisterReceiver(mUnmountReceiver);

        /**M: Added for SD hot-plug performance optimization.@{**/
        // release PrescanTask instance
        synchronized (MediaScannerService.this) {
            for (PrescanTask task : mPrescanTaskList) {
                task.cancel(true);
            }
            mPrescanTaskList.clear();
        }
        /**@}**/
    }

    @Override
    public void run() {
        // reduce priority below other background threads to avoid interfering
        // with other services at boot time.
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND +
                Process.THREAD_PRIORITY_LESS_FAVORABLE);
        Looper.prepare();

        mServiceLooper = Looper.myLooper();
        mServiceHandler = new ServiceHandler();
        /// M: reduce thread priority after ServiceHandler have been created to avoid cpu starvation
        /// which may cause ANR because create service handler too slow.
        // reduce priority below other background threads to avoid interfering
        // with other services at boot time.
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE);

        Looper.loop();
    }

    private Uri scanFile(String path, String mimeType) {
        String volumeName = MediaProvider.EXTERNAL_VOLUME;

        try (MediaScanner scanner = new MediaScanner(this, volumeName)) {
            // make sure the file path is in canonical form
            String canonicalPath = new File(path).getCanonicalPath();
            return scanner.scanSingleFile(canonicalPath, mimeType);
        } catch (Exception e) {
            MtkLog.e(TAG, "bad path " + path + " in scanFile()", e);
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    
    private final IMediaScannerService.Stub mBinder = 
            new IMediaScannerService.Stub() {
        public void requestScanFile(String path, String mimeType, IMediaScannerListener listener) {
            if (MediaUtils.LOG_SCAN) {
                MtkLog.d(TAG, "IMediaScannerService.scanFile: " + path
                    + " mimeType: " + mimeType);
            }
            Bundle args = new Bundle();
            args.putString("filepath", path);
            args.putString("mimetype", mimeType);
            if (listener != null) {
                args.putIBinder("listener", listener.asBinder());
            }
            startService(new Intent(MediaScannerService.this,
                    MediaScannerService.class).putExtras(args));
        }

        public void scanFile(String path, String mimeType) {
            requestScanFile(path, mimeType, null);
        }
    };

    private final class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            /// M: MediaScanner Performance turning {@
            /// Add two message for shutdown threadpool
            /// and handle scan finish request.
            if (MediaUtils.LOG_SCAN) {
                MtkLog.v(TAG, "handleMessage: what = " + msg.what
                    + ", startId = " + msg.arg1
                    + ", arguments = " + msg.obj);
            }
            switch (msg.what) {
                case MSG_SCAN_SINGLE_FILE:
                    handleScanSingleFile(msg);
                    break;

                case MSG_SCAN_DIRECTORY:
                    handleScanDirectory(msg);
                    break;

                case MSG_SHUTDOWN_THREADPOOL:
                    handleShutdownThreadpool();
                    break;

                case MSG_SCAN_FINISH_WITH_THREADPOOL:
                    handleScanFinish();
                    break;

                default:
                    MtkLog.w(TAG, "unsupport message " + msg.what);
                    break;
            }
            /// @}
        }
    };

    private void handleScanSingleFile(Message msg) {
        Bundle arguments = (Bundle) msg.obj;
        String filePath = arguments.getString("filepath");
        try {
            IBinder binder = arguments.getIBinder("listener");
            IMediaScannerListener listener =
                (binder == null ? null : IMediaScannerListener.Stub.asInterface(binder));
            Uri uri = null;
            try {
                /// M: If file path is a directory we need scan the folder, else just scan single file.{@
                File file = new File(filePath);
                if (file.isDirectory()) {
                    scanFolder(new String[] {filePath}, MediaProvider.EXTERNAL_VOLUME);
                } else {
                    uri = scanFile(filePath, arguments.getString("mimetype"));
                }
                /// @}
            } catch (Exception e) {
                MtkLog.e(TAG, "Exception scanning single file " + filePath, e);
            }
            if (listener != null) {
                listener.scanCompleted(filePath, uri);
            }
        } catch (Exception e) {
            MtkLog.e(TAG, "Exception in handleScanSingleFile", e);
        }

        /// M: MediaScanner Performance turning {@
        /// Only stop service when thread pool terminate
        if (mStartId != -1) {
            stopSelfResult(mStartId);
            mStartId = msg.arg1;
        } else {
            stopSelf(msg.arg1);
        }
        /// @}
    }

    private void handleScanDirectory(Message msg) {
        Bundle arguments = (Bundle) msg.obj;
        try {
            String volume = arguments.getString("volume");
            String[] directories = null;

            if (MediaProvider.INTERNAL_VOLUME.equals(volume)) {
                // scan internal media storage
                directories = new String[] {
                        Environment.getRootDirectory() + "/media",
                                Environment.getOemDirectory() + "/media",
                };
            } else if (MediaProvider.EXTERNAL_VOLUME.equals(volume)) {
                // scan external storage volumes
                        // scan external storage volumes
                        if (getSystemService(UserManager.class).isDemoUser()) {
                            directories = ArrayUtils.appendElement(String.class,
                                    mExternalStoragePaths,
                                    Environment.getDataPreloadsMediaDirectory().getAbsolutePath());
                        } else {
                            directories = mExternalStoragePaths;
                        }
                /// M: MediaScanner Performance turning {@
                /// Thread pool enable, use threadpool to scan.
                if (mIsThreadPoolEnable) {
                    mStartId = msg.arg1;
                    if (mMediaScannerThreadPool == null) {
                        scanWithThreadPool(directories, volume);
                    }
                    return;
                }
                /// @}
            }

            if (directories != null) {
                scan(directories, volume);
            }
        } catch (Exception e) {
            MtkLog.e(TAG, "Exception in handleScanDirectory", e);
        }

        /// M: MediaScanner Performance turning {@
        /// Only stop service when thread pool terminate
        if (mStartId != -1) {
            stopSelfResult(mStartId);
            mStartId = msg.arg1;
        } else {
            stopSelf(msg.arg1);
        }
        /// @}
    }

    /// M: MediaScanner Performance turning {@

    private MediaScannerThreadPool mMediaScannerThreadPool;
    private MediaScannerInserter mMediaScannerInserter;
    /// M: Only when device is not low ram device and it's cpu core num big than 4 need enable thread pool to scan.
    private boolean mIsThreadPoolEnable = false;
    /// M: Start mediascanner service id, when scan finish with thread pool we need stop
    /// service with this id.
    private int mStartId = -1;
    /// M: use them to restore scan times.
    private long mScanStartTime;
    private long mPreScanFinishTime;
    private long mScanFinishTime;
    private long mPostScanFinishTime;

    /// M: Stop scan and do scan later to avoid unmount sdcard fail when user unmount sdcard while scanning.
    private BroadcastReceiver mUnmountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized (this) {
                MtkLog.v(TAG, "onReceive()-"+intent.getAction());
                while (mServiceHandler == null) {
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                        MtkLog.e(TAG, "onStartCommand: InterruptedException!");
                    }
                }
                if(mMediaScannerThreadPool != null && mIsThreadPoolEnable){
                    mMediaScannerThreadPool.stopScan();
                }
                int startId = mStartId;
                mStartId = -1;
                /// remove all scan directory message and send delay message to scan later
                mServiceHandler.removeMessages(MSG_SCAN_DIRECTORY);
            }
        }
    };

    /**
     * M: Scan given directories with thread pool.
     *
     * @param directories need scan directories.
     * @param volume external or internal.
     */
    private void scanWithThreadPool(String[] directories, String volume) {
        mScanStartTime = System.currentTimeMillis();
        /// 1. Remove old scan directory message.
        mServiceHandler.removeMessages(MSG_SCAN_DIRECTORY);

        /// 2.Acquire wakelock to avoid sleep while scanning
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }

        /// 3.Prepare down provider to save scan out data
        ContentValues values = new ContentValues();
        values.put(MediaStore.MEDIA_SCANNER_VOLUME, volume);
        getContentResolver().insert(MediaStore.getMediaScannerUri(), values);
        openDatabase(volume);

        /// 4.Initialize thread pool
        initializeThreadPool(directories, volume);

        /// 5.First pre scan all objects before scan all folders(post scan them when scan finish).
        /// then parse out all task and execute them to thread pool.
        mPreScanFinishTime = System.currentTimeMillis();

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_STARTED,
                                 Uri.parse("file://" + mExternalStoragePaths[0])));
        mMediaScannerThreadPool.parseScanTask();
        if (MediaUtils.LOG_SCAN) {
            MtkLog.v(TAG, "scanWithThreadPool() " + Arrays.toString(directories));
        }
    }

    /**
     * M: initialize thread pool parameter
     * @param directories
     * @param volume
     */
    private void initializeThreadPool(String[] directories, String volume) {
        if (mMediaScannerThreadPool == null) {
            if (MediaUtils.LOG_SCAN) {
                MtkLog.v(TAG, "initializeThreadPool() with creating new one");
            }
            mMediaScannerInserter = new MediaScannerInserter(this, mServiceHandler);
            mMediaScannerThreadPool = new MediaScannerThreadPool(this, directories, mServiceHandler,
                    mMediaScannerInserter.getInsertHandler());
        }
    }

    private void releaseThreadPool() {
        synchronized (this) {
            mMediaScannerInserter.release();
            mMediaScannerThreadPool = null;
            mMediaScannerInserter = null;
            if (MediaUtils.LOG_SCAN) {
                MtkLog.v(TAG, "releaseThreadPool()");
            }
        }
    }

    private void handleShutdownThreadpool() {
        if (mMediaScannerThreadPool != null && !mMediaScannerThreadPool.isShutdown()) {
            if (MediaUtils.LOG_SCAN) {
                MtkLog.v(TAG, "handleShutdownThreadpool()");
            }
            mMediaScannerThreadPool.shutdown();
        }
    }

    /**
     * M: Scan finish with thread pool, do pre scan again if need and post scan with preScanner, then update
     * provider and send broadcast to notify scan finish. If there is a scan request coming during scanning,
     * check right now and scan all files again if need, if no new scan request, release thread pool.
     */
    private void handleScanFinish() {
        /// 1.Scan finish, preScan if need, post scan to generate playlist files. Then send broadcast to
        /// to notify app and release wakelock.
        try {
            mScanFinishTime = System.currentTimeMillis();
            /// After scan finish we need postscan.
            try (MediaScanner scanner = new MediaScanner(this, "external")) {
                scanner.postScanAll(mMediaScannerThreadPool.getPlaylistFilePaths());
            }
            getContentResolver().delete(MediaStore.getMediaScannerUri(), null, null);
        } catch (Exception e) {
            MtkLog.e(TAG, "Exception in handleScanFinish", e);
        } finally {
            if (MediaUtils.LOG_SCAN) {
                MtkLog.v(TAG, "handleScanFinish()");
            }
        }
        mPostScanFinishTime = System.currentTimeMillis();
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_FINISHED, Uri.parse("file://" + mExternalStoragePaths[0])));
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        releaseThreadPool();

        /// 2.release thread pool and stop service.
        stopSelfResult(mStartId);
        mStartId = -1;
    }

    private int getCpuCoreNum() {
        return Runtime.getRuntime().availableProcessors();
    }

    private boolean isLowRamDevice() {
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        return am.isLowRamDevice();
    }
}
