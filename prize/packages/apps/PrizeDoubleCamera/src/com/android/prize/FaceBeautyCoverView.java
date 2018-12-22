package com.android.prize;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.camera.Log;
import com.android.camera.R;

/**
 * Created by Administrator on 2017/3/18.
 */

public class FaceBeautyCoverView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "FaceBeautyCoverView";
    private SurfaceHolder holder;
    private Canvas canvas;
    private int mCurrentFrame = 0;
    private int mSingleFrameDuration = 10;
    private boolean mSurfaceCreated = false;
    private final int TOTLE_FRAME = 21;
    private Paint paint;
    private boolean isRunning = false;
    private LruCache<Integer, Bitmap> lruCache = null;
    private final Object mLock = new Object();
    /*prize-xuchunming-20180427-bugid:56713-start*/
    private HandlerThread mHandlerThread;
    private Handler mFaceBeautyCoverViewHandler;
    private static final int MSG_START_ANIMATE = 100;
    /*prize-xuchunming-20180427-bugid:56713-end*/
    
	private final static int[] FRAMES = new int[] { R.drawable.face_beauty_waiting_anim_001,
			R.drawable.face_beauty_waiting_anim_003, R.drawable.face_beauty_waiting_anim_005,
			R.drawable.face_beauty_waiting_anim_009, R.drawable.face_beauty_waiting_anim_011,
			R.drawable.face_beauty_waiting_anim_013, R.drawable.face_beauty_waiting_anim_017,
			R.drawable.face_beauty_waiting_anim_019, R.drawable.face_beauty_waiting_anim_023,
			R.drawable.face_beauty_waiting_anim_027, R.drawable.face_beauty_waiting_anim_029,
			R.drawable.face_beauty_waiting_anim_033, R.drawable.face_beauty_waiting_anim_037,
			R.drawable.face_beauty_waiting_anim_039, R.drawable.face_beauty_waiting_anim_041,
			R.drawable.face_beauty_waiting_anim_043, R.drawable.face_beauty_waiting_anim_047,
			R.drawable.face_beauty_waiting_anim_049, R.drawable.face_beauty_waiting_anim_051,
			R.drawable.face_beauty_waiting_anim_053, R.drawable.face_beauty_waiting_anim_057,
			R.drawable.face_beauty_waiting_anim_060 };

    public FaceBeautyCoverView(Context context) {
        this(context, null);
    }


    public FaceBeautyCoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceBeautyCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FaceBeautyCoverView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    	/*prize-xuchunming-20180427-bugid:56713-start*/
    	mHandlerThread = new HandlerThread("FaceBeautyCoverView");
    	mHandlerThread.start();
        mFaceBeautyCoverViewHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case MSG_START_ANIMATE:
                        startAnimate();
                        break;
                }
            }
        };
        /*prize-xuchunming-20180427-bugid:56713-end*/
        holder = getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        paint = new Paint();
        paint.setAntiAlias(true);
        long maxMemory = Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (maxMemory / 8);
        lruCache = new LruCache<Integer, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount();
            }
        };
    }

    public void setSingleFrameDuration(int ms) {
        mSingleFrameDuration = ms;
        mCurrentFrame = 0;
    }

    private boolean isRunning(){
        return isRunning;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceCreated = false;
        mCurrentFrame = 0;
        lruCache.evictAll();
    }

    public void startAnimation() {
    	Log.i(TAG, "startAnimation");
    	/*prize-xuchunming-20180427-bugid:56713-start*/
        //new Thread(mAnimateJob).start();
    	stopAnimation();
        mFaceBeautyCoverViewHandler.removeMessages(MSG_START_ANIMATE);
        mFaceBeautyCoverViewHandler.sendEmptyMessage(MSG_START_ANIMATE);
        /*prize-xuchunming-20180427-bugid:56713-end*/
    }

    public void stopAnimation() {
    	Log.i(TAG, "stopAnimation");
        isRunning = false;
        mCurrentFrame = 0;
    }

    private Runnable mAnimateJob = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                if (mSurfaceCreated) {
                    startAnimate();
                }
            }
        }
    };

    private void startAnimate() {
        isRunning = true;
        while (mCurrentFrame < TOTLE_FRAME && isRunning) {
            canvas = holder.lockCanvas();
            try {
                if (canvas != null) {
                    canvas.drawColor(Color.TRANSPARENT,
                            android.graphics.PorterDuff.Mode.CLEAR);
                }
                Bitmap bitmap = null;
                if (lruCache != null && lruCache.get(FRAMES[mCurrentFrame]) != null){
                    bitmap = lruCache.get(FRAMES[mCurrentFrame]);
                } else {
                    bitmap = BitmapFactory.decodeResource(getResources(),FRAMES[mCurrentFrame]);
                    lruCache.put(FRAMES[mCurrentFrame],bitmap);
                }
                if (bitmap != null){
                    canvas.drawBitmap(bitmap, 0, 0, paint);
                }

            }catch (NullPointerException ex){
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                    canvas = null;
                    break;
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                    canvas = null;
                }
            }
            mCurrentFrame ++;
            try {
                Thread.sleep(mSingleFrameDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mCurrentFrame = 0;
        isRunning = false;
    }
    
    @Override
    public void setVisibility(int visibility) {
    	// TODO Auto-generated method stub
    	Log.d(TAG, "this:"+this+",visibility:"+visibility);
    	super.setVisibility(visibility);
    }
}
