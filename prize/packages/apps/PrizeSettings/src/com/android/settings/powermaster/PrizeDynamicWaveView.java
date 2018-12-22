package com.android.settings.powermaster;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.graphics.Rect;
import com.android.settings.R;
import android.os.Handler;
import android.graphics.PixelFormat;
public class PrizeDynamicWaveView extends SurfaceView implements SurfaceHolder.Callback {

	private static final String TAG = "PrizeDynamicWaveView";

	/**
	 * Wave color
	 */
	private static final int WAVE_PAINT_COLOR = 0xFF63D91C;

	private static final int WAVE_VIEW_BG_COLOR = 0xFF50B116;
	/**
	 * y = Asin(wx + b) + h
	 */
	private static final float STRETCH_FACTOR_A = 10;
	private static final int OFFSET_Y = 0;
	/**
	 * Wave speed<br>
	 * <b>V = S / t</b>
	 */
	private static final int TRANSLATE_X_SPEED_ONE = 7;
	private static final int TRANSLATE_X_SPEED_TWO = 5;
	private float mCycleFactorW;

	private int mTotalWidth, mTotalHeight;
	private float[] mYPositions;
	private float[] mResetOneYPositions;
	private float[] mResetTwoYPositions;

	private int mXOffsetSpeedOne;
	private int mXOffsetSpeedTwo;

	private int mPowerHeight;

	private int mXOneOffset;
	private int mXTwoOffset;

	private int mYWave = 50;

	private SurfaceHolder mSurfaceHolder;

	private Canvas mCanvas;

	private Paint mWavePaint;

	private PaintFlagsDrawFilter mDrawFilter;

	private Timer mTimer;

	private TimerTask mTask;

	private Context mContext;
	/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
	private Paint mHeadPaint;
	private String mBatteryNunber;
    private String mElectricity;
    private String mBatteryPercentage ;
    private Paint mBatteryNunberP;
    private Paint mElectricityP;
    private Paint mBatteryPercentageP;
    private Rect rect;
	/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/

	public PrizeDynamicWaveView(Context context) {
		this(context,null);
	}

	public PrizeDynamicWaveView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public PrizeDynamicWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr,0);
	}



	public PrizeDynamicWaveView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		Log.d(TAG, "WaveView()");
		mContext = context;
		initPaint(context);
		mSurfaceHolder = this.getHolder();
		mSurfaceHolder.addCallback(this);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
		setZOrderOnTop(true);
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
		mXOffsetSpeedOne = (int) android.util.TypedValue.applyDimension(
				android.util.TypedValue.COMPLEX_UNIT_DIP, TRANSLATE_X_SPEED_ONE, context.getResources().getDisplayMetrics());
		mXOffsetSpeedTwo = (int) android.util.TypedValue.applyDimension(
				android.util.TypedValue.COMPLEX_UNIT_DIP, TRANSLATE_X_SPEED_TWO, context.getResources().getDisplayMetrics());

		mWavePaint = new Paint();
		mWavePaint.setAntiAlias(true);
		mWavePaint.setStyle(Style.FILL);
		mWavePaint.setColor(mContext.getResources().getColor(R.color.prize_power_master_color));

		mHeadPaint = new Paint();
		mHeadPaint.setAntiAlias(true);
		mHeadPaint.setStyle(Style.FILL);
		mHeadPaint.setColor(mContext.getResources().getColor(R.color.prize_power_master_canvas_color));

		mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	}

	public void setYWave(int y) {
		mYWave = y;
		mBatteryNunber = String.valueOf(mYWave);
        mBatteryNunberP.getTextBounds(mBatteryNunber, 0, mBatteryNunber.length(), rect);
	}

	private void resetPositonY() {
		int yOneInterval = mYPositions.length - mXOneOffset;
		System.arraycopy(mYPositions, mXOneOffset, mResetOneYPositions, 0, yOneInterval);
		System.arraycopy(mYPositions, 0, mResetOneYPositions, yOneInterval, mXOneOffset);

		int yTwoInterval = mYPositions.length - mXTwoOffset;
		System.arraycopy(mYPositions, mXTwoOffset, mResetTwoYPositions, 0, yTwoInterval);
		System.arraycopy(mYPositions, 0, mResetTwoYPositions, yTwoInterval, mXTwoOffset);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.d(TAG, "WaveView.surfaceChanged()");
		setWaveDate(width, height);
	}

	private void setWaveDate(int width, int height){
		mTotalWidth = width;
		mTotalHeight = height;

		mPowerHeight = (int)((mYWave/100F)*mTotalHeight);

		mYPositions = new float[mTotalWidth];
		mResetOneYPositions = new float[mTotalWidth];
		mResetTwoYPositions = new float[mTotalWidth];

		mCycleFactorW = (float) (2 * Math.PI / mTotalWidth);

		for (int i = 0; i < mTotalWidth; i++) {
			mYPositions[i] = (float) (STRETCH_FACTOR_A * Math.sin(mCycleFactorW * i) + OFFSET_Y);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "WaveView.surfaceCreated()");
		if(mTimer == null){
			mTimer = new Timer();
			mTask = new TimerTask() {
				@Override
				public void run() {
					Log.d(TAG, "WaveView.run()");
					if(mSurfaceHolder != null){
						mCanvas = mSurfaceHolder.lockCanvas(); 
						if(mCanvas != null){
							mCanvas.drawColor(mContext.getResources().getColor(R.color.prize_power_master_canvas_color)); 
							mCanvas.setDrawFilter(mDrawFilter);
							resetPositonY();
							float mOneStatY = 0;
							float mTwoStatY = 0;
							float mHeadEndY = 0;
							for (int i = 0; i < mTotalWidth; i++) {
								mOneStatY = mTotalHeight - mResetOneYPositions[i] -  mPowerHeight;
								mTwoStatY = mTotalHeight - mResetTwoYPositions[i] -  mPowerHeight;

								mHeadEndY = mOneStatY > mTwoStatY?mTwoStatY:mOneStatY;
								mCanvas.drawLine(i, 0, i, mHeadEndY, mHeadPaint);

								mCanvas.drawLine(i, mOneStatY, i, mTotalHeight, mWavePaint);
								mCanvas.drawLine(i, mTwoStatY, i, mTotalHeight, mWavePaint);
							}
							setWaveDate(mTotalWidth, mTotalHeight);
							/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
							mCanvas.drawText(mBatteryNunber, getWidth() / 2, getHeight() / 2 + rect.height() / 2, mBatteryNunberP);
							mCanvas.drawText(mBatteryPercentage,getWidth()/2+rect.width()/2+10,getHeight()/2-rect.height()/3,mBatteryPercentageP);
							mCanvas.drawText(mElectricity,getWidth()/2+rect.width()/2+10,getHeight()/2,mElectricityP);
							/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
							mXOneOffset += mXOffsetSpeedOne;
							mXTwoOffset += mXOffsetSpeedTwo;
							if (mXOneOffset >= mTotalWidth) {
								mXOneOffset = 0;
							}
							if (mXTwoOffset > mTotalWidth) {
								mXTwoOffset = 0;
							}
							mSurfaceHolder.unlockCanvasAndPost(mCanvas); 
						}
					}
				}
			};
		}
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
		Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	if(mTimer != null){
            		mTimer.scheduleAtFixedRate(mTask, 30, 30);
            	}
            }
        },100);
		//mTimer.scheduleAtFixedRate(mTask, 30, 30);
		/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "WaveView.surfaceDestroyed()");
		if(mTask !=null){
			mTask.cancel();
			mTask = null;
		}
		if(mTimer !=null){
			mTimer.cancel();
			mTimer = null;
		}
	}
	/* prize-modify-by-lijimeng-for bugid 43724-20171213-start*/
	 private void initPaint(Context context){
		mBatteryNunber = String.valueOf(mYWave);
        mElectricity = context.getResources().getString(R.string.battery_percentage_bottom);
        mBatteryPercentage = context.getResources().getString(R.string.battery_percentage_top);
		mBatteryNunberP = new Paint();
        mBatteryNunberP.setTextAlign(Paint.Align.CENTER);
        mBatteryNunberP.setTextSize(200);
        rect = new Rect();
        mBatteryNunberP.getTextBounds(mBatteryNunber, 0, mBatteryNunber.length(), rect);
        mBatteryNunberP.setColor(context.getResources().getColor(R.color.white));
        mBatteryPercentageP = new Paint();
        mBatteryPercentageP.setTextSize(32);
        mBatteryPercentageP.setTextAlign(Paint.Align.LEFT);
        mBatteryPercentageP.setColor(context.getResources().getColor(R.color.white));

        mElectricityP = new Paint();
        mElectricityP.setTextSize(30);
        mElectricityP.setTextAlign(Paint.Align.LEFT);
        mElectricityP.setColor(context.getResources().getColor(R.color.white));
    }
	/* prize-modify-by-lijimeng-for bugid 43724-20171213-end*/
}
