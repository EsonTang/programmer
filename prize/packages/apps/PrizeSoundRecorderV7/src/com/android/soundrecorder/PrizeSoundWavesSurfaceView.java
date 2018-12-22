package com.android.soundrecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by wangzhong on 2016/7/25.
 */
public class PrizeSoundWavesSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    public static final String TAG = "SoundWavesSurfaceView";

    private Context mContext;

    private Recorder mRecorder;
    private static final float BASE_NUMBER = 32768;
    private int mAnimSeq = 0;
    private int mCurrentSeq = 0;
    private int mMaxAmplitude;
    private long mCurrentTime;

    private HashMap<Double, Double> mData = new HashMap<Double, Double>();
    private HashMap<Double, Double> mCacheData1 = new HashMap<Double, Double>();
    private HashMap<Double, Double> mCacheData2 = new HashMap<Double, Double>();
    private double mCurrentData;
    private boolean isUseCountTime1 = true;
    private boolean isStartCountTime2 = false;
    private int mCountTime  = 0;
    private int mCountTime1 = 0;
    /**
     * Note:Greater than mCountVoiceprintScale.
     */
    private int mCountTime2 = 0;
    public static final int TIME_INTERVAL = 100;//100ms.

    private SurfaceHolder mHolder;
    private Canvas mCanvas;

    private Paint mPaintVoiceprint;
    private Paint mPaintMidcourt;
    private Paint mPaintScale;
    private int mColorVoiceprint = Color.WHITE;
    private int mColorMidcourt   = Color.RED;
    private int mColorScale      = Color.GRAY;
    private int mWidthVoiceprint = 2;
    private int mWidthMidcourt   = 2;
    private int mWidthScale      = 2;
    private int mTextSizeScale   = 26;

    private static final int VOICEPRINT_DEFAULT_HEIGHT = 4;
    private static final int SCALE_LONG_HEIGHT = 30;
    private static final int SCALE_SHORT_HEIGHT = 15;
    /**
     * 10 seconds.
     */
    private static final int VOICEPRINT_SCALE_1 = 10;
    /**
     * A second into 10 portions.
     */
    private static final int VOICEPRINT_SCALE_2 = 4;
    private int mCountVoiceprintScale;
    private int mCountScaleScale;

    private Thread mThread;
    private boolean flag = true;

    public void setCurrentData(double currentData) {
        mCurrentData = currentData;
    }

    public PrizeSoundWavesSurfaceView(Context context) {
        this(context, null);
    }

    public PrizeSoundWavesSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrizeSoundWavesSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initConfige();
    }

    private void initConfige() {
        mHolder = getHolder();
        setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mHolder.addCallback(this);

        setFocusable(true);

        // Show only the half! [mCountVoiceprintScale = ((VOICEPRINT_SCALE_1 / 2) * VOICEPRINT_SCALE_2) * 2;]
        mCountVoiceprintScale = (VOICEPRINT_SCALE_1 * VOICEPRINT_SCALE_2) * 5;

        mCountScaleScale = VOICEPRINT_SCALE_1 * VOICEPRINT_SCALE_2;
        // Greater than mCountVoiceprintScale.
        /*mCountTime2 = mCountVoiceprintScale * 2;*/
        // Use the counttime1.
        isUseCountTime1 = true;
        // Default stop the counttime2.
        isStartCountTime2 = false;

        mCurrentData = VOICEPRINT_DEFAULT_HEIGHT;

        initPaint();
    }

    private void initPaint() {
        initPaintVoiceprint();
        initPaintMidcourt();
        initPaintScale();
    }

    /**
     * Initialize the voice print paint.
     */
    private void initPaintVoiceprint() {
        mPaintVoiceprint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintVoiceprint.setColor(mColorVoiceprint);
        mPaintVoiceprint.setStrokeWidth(mWidthVoiceprint);
        mPaintVoiceprint.setStyle(Paint.Style.FILL);
    }

    /**
     * Initialize the midcourt paint.
     */
    private void initPaintMidcourt() {
        mPaintMidcourt = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintMidcourt.setColor(mColorMidcourt);
        mPaintMidcourt.setStrokeWidth(mWidthMidcourt);
        mPaintMidcourt.setStyle(Paint.Style.FILL);
    }

    /**
     * Initialize the scale paint.
     */
    private void initPaintScale() {
        mPaintScale = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintScale.setColor(mColorScale);
        mPaintScale.setStrokeWidth(mWidthScale);
        mPaintScale.setStyle(Paint.Style.FILL);
        mPaintScale.setTextSize(mTextSizeScale);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //startSoundWaves();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopSoundWaves();
        mHolder.removeCallback(this);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE) {
            //drawView();
        }
    }

    public void startSoundWaves() {
        flag = true;
        mThread = new Thread(this);
        mThread.start();
    }

    public void stopSoundWaves() {
        flag = false;
    }

    public void setRecorder(Recorder recorder) {
        mRecorder = recorder;
//        getCurrentRecorderData();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
        while (flag) {
            try {
                synchronized (mHolder) {
                    drawView();
                    Thread.sleep(TIME_INTERVAL) ;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void drawView() {
        updateViewData();

        mCanvas = mHolder.lockCanvas();
        if (null != mCanvas) {
            mCanvas.drawColor(Color.BLACK);

            int midCoordinateY = getHeight() / 2;
			/*-prize-change-yangming-2016-9-6-start*/
            //int widthIntervalVoiceprint = getWidth() / mCountVoiceprintScale ;
            int widthIntervalVoiceprint = (int) (getWidth() / (mCountVoiceprintScale*1.5));
            /*-prize-change-yangming-2016-9-6-start*/
            int widthIntervalScale = getWidth() / mCountScaleScale;
            // Voiceprint.
            if (null != mData) {
                int dataSize = mData.size();
                for (int i = 0; i < mCountVoiceprintScale * 2; i++) {
                    float currentCoordinateX = getWidth() / 2 - (float) i * widthIntervalVoiceprint;
					float currentCoordinateXRight = getWidth() / 2 + (float) i * widthIntervalVoiceprint;
                    if (i < dataSize) {
                        double key = (i + 1) * 0.1;
                        //Log.d(TAG, "key ...................................." + key);
                        //Positive sequence. discarded the Reverse order!

                        //Reverse order.
                        currentCoordinateX = getWidth() / 2 - (dataSize - i - 1)  * widthIntervalVoiceprint;

                        double h = mData.get(key);
                        if (h < VOICEPRINT_DEFAULT_HEIGHT) {
                            h = VOICEPRINT_DEFAULT_HEIGHT;
                        }
						mCanvas.drawLine(currentCoordinateX, (float) (midCoordinateY - (h / 2)), currentCoordinateX, (float) (midCoordinateY + (h / 2)), mPaintVoiceprint);
                        //mCanvas.drawLine(currentCoordinateX, (float) (midCoordinateY - (h / 2) + 4), currentCoordinateX, (float) (midCoordinateY + (h / 2) - 4), mPaintVoiceprint);
                    } else {
                        //Log.d(TAG, "is default ......................................................i = " + i);
                        mCanvas.drawLine(currentCoordinateX, (float) (midCoordinateY - (VOICEPRINT_DEFAULT_HEIGHT / 2)), currentCoordinateX, (float) (midCoordinateY + (VOICEPRINT_DEFAULT_HEIGHT / 2)), mPaintVoiceprint);
                    }

                    //right.
                    mCanvas.drawLine(currentCoordinateXRight, (float) (midCoordinateY - (VOICEPRINT_DEFAULT_HEIGHT / 2)), currentCoordinateXRight, (float) (midCoordinateY + (VOICEPRINT_DEFAULT_HEIGHT / 2)), mPaintScale);
                }
            }

            // Scale.
            /*-prize-change-yangming-2016-9-6-start*/
            //int scaleDisplacementX = ((mCountTime - 1) % (VOICEPRINT_SCALE_2 * 6 )) * widthIntervalVoiceprint
            int scaleDisplacementX = ((mCountTime - 1) % (VOICEPRINT_SCALE_2 * 9 )) * widthIntervalVoiceprint; //@see mCountVoiceprintScale = (VOICEPRINT_SCALE_1 * VOICEPRINT_SCALE_2) * 5;    this 5
            /*-prize-change-yangming-2016-9-6-end*/
            for (int i = 0; i < mCountScaleScale + VOICEPRINT_SCALE_2; i++) { 
                //mCanvas.drawLine(0, 1, getWidth(), 1, mPaintScale);
                mCanvas.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1, mPaintScale);
                if (i % VOICEPRINT_SCALE_2 == 0) {
                    //mCanvas.drawLine(i * widthIntervalScale, 0, i * widthIntervalScale, SCALE_LONG_HEIGHT, mPaintScale);
                    mCanvas.drawLine(i * widthIntervalScale - scaleDisplacementX, getHeight() - SCALE_LONG_HEIGHT, i * widthIntervalScale - scaleDisplacementX, getHeight(), mPaintScale);
                    // number.
                    //mCanvas.drawText((i / VOICEPRINT_SCALE_2) + "", i * widthIntervalScale, SCALE_LONG_HEIGHT, mPaintScale);
                } else {
                    //mCanvas.drawLine(i * widthIntervalScale, 0, i * widthIntervalScale, SCALE_SHORT_HEIGHT, mPaintScale);
                    mCanvas.drawLine(i * widthIntervalScale - scaleDisplacementX, getHeight() - SCALE_SHORT_HEIGHT, i * widthIntervalScale - scaleDisplacementX, getHeight(), mPaintScale);
                }
            }

            // Midcourt.
            mCanvas.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight(), mPaintMidcourt);
			//mCanvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, mPaintScale);

            if (null != mHolder && null != mCanvas) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
        /*mCanvas = mHolder.lockCanvas(new Rect(0, 0, 0, 0));
        mHolder.unlockCanvasAndPost(mCanvas);*/
    }

    private void updateViewData() {
        // Add data.
        //mCurrentData = (Math.random() * 500);
        getCurrentRecorderData();

        mCountTime += 1;

        // mCacheData1.
        mCountTime1 += 1;
        if (mCountTime1 > mCountVoiceprintScale) {
            isStartCountTime2 = true;
        }
        if (mCountTime1 > mCountVoiceprintScale * 2) {
            mCountTime1 = 1;
            isUseCountTime1 = false;
            mCacheData1.clear();
        }
        //Log.d(TAG, "mCountTime1 ...................................." + mCountTime1);
        mCacheData1.put(mCountTime1 * 0.1, mCurrentData);

        // mCacheData2.
        if (isStartCountTime2) {
            mCountTime2 += 1;
            if (mCountTime2 > mCountVoiceprintScale * 2) {
                mCountTime2 = 1;
                isUseCountTime1 = true;
                mCacheData2.clear();
            }
            //Log.d(TAG, "mCountTime2 ...................................." + mCountTime2);
            mCacheData2.put(mCountTime2 * 0.1, mCurrentData);
        }

        // default.
        //mCurrentData = VOICEPRINT_DEFAULT_HEIGHT;

        // mData.
        //Log.d(TAG, "isUseCountTime1 ...................................." + isUseCountTime1);
        if (isUseCountTime1) {
            mData = mCacheData1;
        } else {
            mData = mCacheData2;
        }
    }

    private void getCurrentRecorderData() {
        if (null != mRecorder) {
            mMaxAmplitude = mRecorder.getMaxAmplitude();
            mCurrentSeq = (int) ((float) (50 * mMaxAmplitude) / BASE_NUMBER);
            mCurrentSeq = Math.min(mCurrentSeq, 50);

            mCurrentData = mCurrentSeq * getHeight() / 50;
        } else {
            mCurrentData = getHeight() / 50;
        }
    }

    public void clearAllData() {
        mCacheData1.clear();
        mCacheData2.clear();

        isUseCountTime1 = true;
        isStartCountTime2 = false;

        mCountTime = 0;
        mCountTime1 = 0;
        mCountTime2 = 0;
    }

}
