package com.prize.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.util.LruCache;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.android.camera.R;

/**
 * Created by xp on 2017/9/29.
 */
public class CaptureAnimation extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    private String TAG = this.getClass().getSimpleName();
    private final SurfaceHolder mHolder;
    private Thread mThread;
    private long mFrameSpaceTime = 35;
    private boolean mIsDraw = true;
    private int mCurrentIndext = 0;
    private LruCache<Integer, Bitmap> lruCache = null;

    public int mBitmapResourceIds[] = {
            R.drawable.quan_000,R.drawable.quan_001,R.drawable.quan_002,R.drawable.quan_003,R.drawable.quan_004,
            R.drawable.quan_005,R.drawable.quan_006,R.drawable.quan_007,R.drawable.quan_008,R.drawable.quan_009,
            R.drawable.quan_010,R.drawable.quan_011,R.drawable.quan_012,R.drawable.quan_013,R.drawable.quan_014,
            R.drawable.quan_015,R.drawable.quan_016,R.drawable.quan_017,R.drawable.quan_018,R.drawable.quan_019,
            R.drawable.quan_020,R.drawable.quan_021,R.drawable.quan_022,R.drawable.quan_023,R.drawable.quan_024,
            R.drawable.quan_025,R.drawable.quan_026,R.drawable.quan_027,R.drawable.quan_028,R.drawable.quan_029,
            R.drawable.quan_030,R.drawable.quan_031,R.drawable.quan_032,R.drawable.quan_033,R.drawable.quan_034,
            R.drawable.quan_035,R.drawable.quan_036,R.drawable.quan_037,R.drawable.quan_038,R.drawable.quan_039,
            R.drawable.quan_040,R.drawable.quan_041,R.drawable.quan_042,R.drawable.quan_043,R.drawable.quan_044,
            R.drawable.quan_045,R.drawable.quan_046,R.drawable.quan_047,R.drawable.quan_048,R.drawable.quan_049,
            R.drawable.quan_050

    };
    private Bitmap mBitmap;

    public CaptureAnimation(Context context) {this(context,null);}

    public CaptureAnimation(Context context, AttributeSet attrs) {this(context,attrs,0);}

    public CaptureAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHolder = this.getHolder();
        mHolder.addCallback(this);

        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        lruCache = new LruCache<Integer, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");

    }

    public void start() {
        if(mBitmapResourceIds == null){
            return;
        }
        mThread = new Thread(this);
        mThread.start();
        mIsDraw = true;
    }

    public void stop() {
        mIsDraw = false;

    }
    public void setmBitmapResourceIds(int[] mBitmapResourceIds) {
        this.mBitmapResourceIds = mBitmapResourceIds;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDraw = false;
        try {
            Thread.sleep(mFrameSpaceTime);
            Log.d(TAG, "surfaceDestroyed: Thread " + mThread.getState());
            lruCache.evictAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        synchronized (mHolder) {
            long time = 0;
            while (mIsDraw) {
                try {
                    time = System.currentTimeMillis();
                    drawView();
                    Thread.sleep(mFrameSpaceTime);
                    //Thread.sleep(mFrameSpaceTime - (System.currentTimeMillis() - time) > 0 ? mFrameSpaceTime - (System.currentTimeMillis() - time) : 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            drawStopView();

        }
    }
    
    private void drawView() {
        Log.i(TAG, "drawView: ");
        Canvas mCanvas = mHolder.lockCanvas();
        try {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//            mBitmap = BitmapFactory.decodeResource(getResources(), mBitmapResourceIds[mCurrentIndext]);
            if (lruCache != null && lruCache.get(mBitmapResourceIds[mCurrentIndext]) != null){
                mBitmap = lruCache.get(mBitmapResourceIds[mCurrentIndext]);
            } else {
                mBitmap = BitmapFactory.decodeResource(getResources(),mBitmapResourceIds[mCurrentIndext]);
                lruCache.put(mBitmapResourceIds[mCurrentIndext],mBitmap);
            }
            mCanvas.drawBitmap(mBitmap, 0, 0, null);
            if (mCurrentIndext == mBitmapResourceIds.length - 1) {
                mCurrentIndext = 0;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            mCurrentIndext++;
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
                mCanvas = null;
            }
//            recycle(mBitmap);
        }
    }
    private void drawStopView() {
        Log.i(TAG, "xucm-debug1 drawStopView: ");
        Canvas mCanvas = mHolder.lockCanvas();
        try {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mBitmap = mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_shutter_photo);
            mCanvas.drawBitmap(mBitmap, 0, 0, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
                mCanvas = null;
            }
        }
    }

    private void recycle(Bitmap mBitmap) {
        if(mBitmap != null)
            mBitmap.recycle();
    }
}
