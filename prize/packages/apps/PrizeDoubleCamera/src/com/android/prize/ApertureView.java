package com.android.prize;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Xfermode;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.MotionEvent;
import android.view.View;
import com.android.camera.R;

/**
 * 上下滑动可以调节光圈大小； 调用setApertureChangedListener设置光圈值变动监听接口； 绘制的光圈最大直径将填满整个view
 * 
 * @author willhua http://www.cnblogs.com/willhua/
 * 
 */
public class ApertureView extends View {

	public interface ApertureChanged {
		public void onApertureChanged(float newapert);
	}

	private static final float ROTATE_ANGLE = 30;
	private static final String TAG = "ApertureView";
	private static final float COS_30 = 0.866025f;
	private static final int WIDTH = 100; // 当设置为wrap_content时测量大小
	private static final int HEIGHT = 100;
	private int mCircleRadius;
	private int mBladeColor;
	private int mBackgroundColor;
	private int mSpace;
	private float mMaxApert = 1;
	private float mMinApert = 0.15f;
	private float mCurrentApert = 2f;

	// 利用PointF而不是Point可以减少计算误差，以免叶片之间间隔由于计算误差而不均衡
	private PointF[] mPoints = new PointF[6];
	private Bitmap mBlade;
	private Paint mPaint;
	private Path mPath;
	private ApertureChanged mApertureChanged;

	private float mPrevX;
	private float mPrevY;

	public ApertureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		// 读取自定义布局属性
		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ApertureView);
		mSpace = (int) array.getDimension(R.styleable.ApertureView_blade_space,
				3);
		mBladeColor = array.getColor(R.styleable.ApertureView_blade_color,
				0xFFcfcecc);
		mBackgroundColor = array.getColor(
				R.styleable.ApertureView_background_color, 0x00FFFFFF);
		array.recycle();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		for (int i = 0; i < 6; i++) {
			mPoints[i] = new PointF();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
		int paddX = getPaddingLeft() + getPaddingRight();
		int paddY = getPaddingTop() + getPaddingBottom();
		// 光圈的大小要考虑减去view的padding值
		mCircleRadius = widthSpecSize - paddX < heightSpecSize - paddY ? (widthSpecSize - paddX) / 2
				: (heightSpecSize - paddY) / 2;
		// 对布局参数为wrap_content时的处理
		if (widthSpecMode == MeasureSpec.AT_MOST
				&& heightSpecMode == MeasureSpec.AT_MOST) {
			setMeasuredDimension(WIDTH, HEIGHT);
			mCircleRadius = (WIDTH - paddX) / 2;
		} else if (widthSpecMode == MeasureSpec.AT_MOST) {
			setMeasuredDimension(WIDTH, heightSpecSize);
			mCircleRadius = WIDTH - paddX < heightSpecSize - paddY ? (WIDTH - paddX) / 2
					: (heightSpecSize - paddY) / 2;
		} else if (heightSpecMode == MeasureSpec.AT_MOST) {
			setMeasuredDimension(widthSpecSize, HEIGHT);
			mCircleRadius = widthSpecSize - paddX < HEIGHT - paddY ? (widthSpecSize - paddX) / 2
					: (HEIGHT - paddY) / 2;
		}
		if (mCircleRadius < 1) {
			mCircleRadius = 1;
		}
		// measure之后才能知道所需要绘制的光圈大小
		mPath = new Path();
		mPath.addCircle(0, 0, mCircleRadius, Path.Direction.CW);
		createBlade();
	}

	@Override
	public void onDraw(Canvas canvas) {

		
		calculatePoints();
		canvas.drawBitmap(getApertureView(getWidth(), getHeight()), 0, 0, mPaint);
	}
	
	
	public Bitmap getApertureView(int w, int h) {
		Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bm);
		
		Rect srcRect  = new Rect(0, 0, w, h);
      
        Bitmap dstBitmap = getDSTBitmap(w, h);
        Bitmap srcBitmap = getSRCBitmap(w, h);
        
        c.drawBitmap(dstBitmap, 0, 0, mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        c.drawBitmap(srcBitmap, srcRect, srcRect, mPaint);
      
        mPaint.setXfermode(null);
		return bm;
	}
	
	 public Bitmap getDSTBitmap(int w, int h) {
	        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        Canvas c = new Canvas(bm);
	        c.drawCircle(getWidth() / 2, getHeight() / 2, mCircleRadius, mPaint);
	        return bm;
	 }
	
	 public  Bitmap getSRCBitmap(int w, int h) {
		 Bitmap bm = Bitmap.createBitmap(getWidth(),getHeight(), Config.ARGB_8888);
	     Canvas c = new Canvas(bm);
	     c.translate(getWidth() / 2, getHeight() / 2);
	     c.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG)); 
		 for (int i = 0; i < 6; i++) {
			c.save();
			c.translate(mPoints[i].x, mPoints[i].y);
			c.rotate(-i * 60);
			c.drawBitmap(mBlade, 0, 0, mPaint);
			c.restore();
		}
		return bm;
	}

	 public Bitmap getRectBitmap(int w, int h) {
	        Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        Canvas c = new Canvas(bm);
	        Path path = new Path();
			
			path.moveTo(mSpace / 2 / COS_30, mSpace);
			path.lineTo(w, h);
			path.lineTo(w, mSpace);
			path.close();
			mPaint.setColor(mBladeColor);
			c.drawPath(path, mPaint);
	        return bm;
	 }
	 
	private void calculatePoints() {
		if (mCircleRadius - mSpace <= 0) {
			Log.e(TAG, "the size of view is too small and Space is too large");
			return;
		}
		// mCircleRadius - mSpace可以保证内嵌六边形在光圈内
		float curRadius = mCurrentApert / mMaxApert * (mCircleRadius - mSpace);
		// 利用对称关系，减少计算
		mPoints[0].x = curRadius / 2;
		mPoints[0].y = -curRadius * COS_30;
		mPoints[1].x = -mPoints[0].x;
		mPoints[1].y = mPoints[0].y;
		mPoints[2].x = -curRadius;
		mPoints[2].y = 0;
		mPoints[3].x = mPoints[1].x;
		mPoints[3].y = -mPoints[1].y;
		mPoints[4].x = -mPoints[3].x;
		mPoints[4].y = mPoints[3].y;
		mPoints[5].x = curRadius;
		mPoints[5].y = 0;
	}

	// 创建光圈叶片，让美工MM提供更好
	private void createBlade() {
		mBlade = Bitmap.createBitmap(mCircleRadius,
				(int) (mCircleRadius * 2 * COS_30), Config.ARGB_8888);
		Canvas canvas = new Canvas(mBlade);
		mPaint.setAntiAlias(true);
		Bitmap srcBitmap = getRectBitmap(mBlade.getWidth(), mBlade.getHeight());
		canvas.drawRect(0, 0, mBlade.getWidth(), mBlade.getHeight(), mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(srcBitmap, 0, 0, mPaint);
		mPaint.setXfermode(null);

	}

	/**
	 * 设置光圈片的颜色
	 * 
	 * @param bladeColor
	 */
	public void setBladeColor(int bladeColor) {
		mBladeColor = bladeColor;
	}

	/**
	 * 设置光圈背景色
	 */
	public void setBackgroundColor(int backgroundColor) {
		mBackgroundColor = backgroundColor;
	}

	/**
	 * 设置光圈片之间的间隔
	 * 
	 * @param space
	 */
	public void setSpace(int space) {
		mSpace = space;
	}

	/**
	 * 设置光圈最大值
	 * 
	 * @param maxApert
	 */
	public void setMaxApert(float maxApert) {
		mMaxApert = maxApert;
	}

	/**
	 * 设置光圈最小值
	 * 
	 * @param mMinApert
	 */
	public void setMinApert(float mMinApert) {
		this.mMinApert = mMinApert;
	}

	public float getCurrentApert() {
		return mCurrentApert;
	}

	public void setCurrentApert(float currentApert) {
		if (currentApert > mMaxApert) {
			currentApert = mMaxApert;
		}
		if (currentApert < mMinApert) {
			currentApert = mMinApert;
		}
		if (mCurrentApert == currentApert) {
			return;
		}
		mCurrentApert = currentApert;
		invalidate();
		if (mApertureChanged != null) {
			mApertureChanged.onApertureChanged(currentApert);
		}
	}

	/**
	 * 设置光圈值变动的监听
	 * 
	 * @param listener
	 */
	public void setApertureChangedListener(ApertureChanged listener) {
		mApertureChanged = listener;
	}

	public float getMinApert() {
		return mMinApert;
	}

	public void hide() {
		mCurrentApert = 2;
		invalidate();
	}
}