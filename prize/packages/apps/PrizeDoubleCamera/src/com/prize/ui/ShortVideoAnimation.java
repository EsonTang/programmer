package com.prize.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.camera.Log;
import com.android.camera.R;
/**
 * Created by xp on 2017/9/29.
 */
public class ShortVideoAnimation extends SurfaceView implements SurfaceHolder.Callback,Runnable {
    private String TAG = this.getClass().getSimpleName();
    private final SurfaceHolder mHolder;
    private Thread mThread;
    private long mFrameSpaceTime = 84;
    private boolean mIsDraw = true;
    private int mCurrentIndext = 0;
    private LruCache<Integer, Bitmap> lruCache = null;
    private boolean isAnimationStop = false;
    public int mBitmapResourceIds[] = {
            R.drawable.video0,R.drawable.video1,R.drawable.video2,R.drawable.video3,R.drawable.video4,
            R.drawable.video5,R.drawable.video6,R.drawable.video7,R.drawable.video8,R.drawable.video9,
            R.drawable.video10,R.drawable.video11,R.drawable.video12,R.drawable.video13,R.drawable.video14,
            R.drawable.video15,R.drawable.video16,R.drawable.video17,R.drawable.video18,R.drawable.video19,
            R.drawable.video20,R.drawable.video21,R.drawable.video22,R.drawable.video23,R.drawable.video24,
            R.drawable.video25,R.drawable.video26,R.drawable.video27,R.drawable.video28,R.drawable.video29,
            R.drawable.video30,R.drawable.video31,R.drawable.video32,R.drawable.video33,R.drawable.video34,
            R.drawable.video35,R.drawable.video36,R.drawable.video37,R.drawable.video38,R.drawable.video39,
            R.drawable.video40,R.drawable.video41,R.drawable.video42,R.drawable.video43,R.drawable.video44,
            R.drawable.video45,R.drawable.video46,R.drawable.video47,R.drawable.video48,R.drawable.video49,
            R.drawable.video50,R.drawable.video51,R.drawable.video52,R.drawable.video53,R.drawable.video54,
            R.drawable.video55,R.drawable.video56,R.drawable.video57,R.drawable.video58,R.drawable.video59,
            R.drawable.video60,R.drawable.video61,R.drawable.video62,R.drawable.video63,R.drawable.video64,
            R.drawable.video65,R.drawable.video66,R.drawable.video67,R.drawable.video68,R.drawable.video69,
            R.drawable.video70,R.drawable.video71,R.drawable.video72,R.drawable.video73,R.drawable.video74,
            R.drawable.video75,R.drawable.video76,R.drawable.video77,R.drawable.video78,R.drawable.video79,
            R.drawable.video80,R.drawable.video81,R.drawable.video82,R.drawable.video83,R.drawable.video84,
            R.drawable.video85,R.drawable.video86,R.drawable.video87,R.drawable.video88,R.drawable.video89,
            R.drawable.video90,R.drawable.video91,R.drawable.video92,R.drawable.video93,R.drawable.video94,
            R.drawable.video95,R.drawable.video96,R.drawable.video97,R.drawable.video98,R.drawable.video99,
            R.drawable.video100,R.drawable.video101,R.drawable.video102,R.drawable.video103,R.drawable.video104,
            R.drawable.video105,R.drawable.video106,R.drawable.video107,R.drawable.video108,R.drawable.video109,
            R.drawable.video110,R.drawable.video111,R.drawable.video112,R.drawable.video113,R.drawable.video114,
            R.drawable.video115,R.drawable.video116,R.drawable.video117,R.drawable.video118,R.drawable.video119
    };
    private Bitmap mBitmap;

    public ShortVideoAnimation(Context context) {this(context,null);}

    public ShortVideoAnimation(Context context, AttributeSet attrs) {this(context,attrs,0);}

    public ShortVideoAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
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
        if (isAnimationStop) {
            mCurrentIndext = 0;
        }
        isAnimationStop = false;
    }

    public void stop() {
        mIsDraw = false;
        isAnimationStop = true;

    }

    public void pause() {
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

    private void recycle(Bitmap mBitmap) {
        if(mBitmap != null)
            mBitmap.recycle();
    }
}
