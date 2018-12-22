package com.prize.app.syncsongs.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.prize.app.BaseApplication;
import com.prize.app.database.dao.GameDAO;
import com.prize.app.download.DownloadHelper;
import com.prize.app.syncsongs.Executor;
import com.prize.app.util.FileUtils;
import com.prize.app.util.JLog;
import com.prize.onlinemusibean.SongDetailInfo;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.MediaStore;

/**
 * This singleton class will make sure that each interactor operation gets a background thread.
 * <p/>
 */
public class ThreadExecutor implements Executor {

    // This is a singleton
    private static volatile ThreadExecutor sThreadExecutor;

    private static final int                     CORE_POOL_SIZE  = 3;
    private static final int                     MAX_POOL_SIZE   = 5;
    private static final int                     KEEP_ALIVE_TIME = 120;
    private static final TimeUnit                TIME_UNIT       = TimeUnit.SECONDS;
    private static final BlockingQueue<Runnable> WORK_QUEUE      = new LinkedBlockingQueue<Runnable>();

	protected static final String TAG = "ThreadExecutor";

    private ThreadPoolExecutor mThreadPoolExecutor;
    
    String[] mCursorCols = new String[] { "audio._id AS _id",
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
			MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
			MediaStore.Audio.Media.ARTIST_ID,
			MediaStore.Audio.Media.IS_PODCAST, MediaStore.Audio.Media.BOOKMARK };

    private ThreadExecutor() {
        long keepAlive = KEEP_ALIVE_TIME;
        mThreadPoolExecutor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                keepAlive,
                TIME_UNIT,
                WORK_QUEUE);
    }

    @Override
    public void execute(final int song_id) {
        mThreadPoolExecutor.submit(new Runnable() {
            @Override
            public void run() {
            	SongDetailInfo info = GameDAO.getInstance().getSongById(song_id);
            	String path = FileUtils.getDownMusicFilePath(DownloadHelper
    					.getFileName(info));
    			String selection = "_DATA=?";
    			long mUriId = -1;
    			Cursor cursor = null;
    			try {
    				while(!(cursor != null && cursor.moveToFirst()&&cursor.getCount()>0)){
    					if (cursor!=null&&!cursor.isClosed()) {
							cursor.close();
						}
    					cursor = BaseApplication.curContext.getContentResolver().query(
    							MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
    							/* mProjection */mCursorCols, selection,
    							new String[] { path }, null);
    				}
					mUriId = cursor.getLong(0);
					GameDAO.getInstance().backupLocalId(song_id, mUriId);
					JLog.i(TAG, "mUriId = " + mUriId);
					ContentValues values = new ContentValues();
					values.put(MediaStore.Audio.Media.TITLE, info.song_name);
					BaseApplication.curContext.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, 
							selection,new String[] { path });
    			} catch (Exception e) {
    				e.printStackTrace();
    			} finally {
    				if (cursor != null && !cursor.isClosed()) {
    					cursor.close();
    				}
    				cursor = null;
    			}
            }
        });
    }

    /**
     * Returns a singleton instance of this executor. If the executor is not initialized then it initializes it and returns
     * the instance.
     */
    public static Executor getInstance() {
        if (sThreadExecutor == null) {
            sThreadExecutor = new ThreadExecutor();
        }

        return sThreadExecutor;
    }
}
