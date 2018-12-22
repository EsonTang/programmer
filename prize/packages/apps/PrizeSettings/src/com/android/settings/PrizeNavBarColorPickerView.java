/*
* Nav bar color customized feature.
* created. prize-zhaojian
*/

package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PrizeNavBarColorPickerView extends View {
	private static final String TAG = "PrizeNavBarColorPickerView";

	private Context mContext;
	private Paint mBottomPaint;
	private int mTotalHeight;
	private int mTotalWidth;
	private int[] mBottomColors;
	private int BOTTOM_HEIGHT;

	private int TOP_WIDTH;
	private Bitmap mTopSlideBitmap;
	private Bitmap mTopSlideBitmap2;
	private Bitmap mBottomSlideBitmap;
	private Bitmap mBottomSlideBitmap2;
	private Paint mTopPaint;
	private final int MARGIN = getContext().getResources().getInteger(R.integer.prize_palette_margin);		
	private boolean downInTop = false;
	private boolean downInBottom = false;
	private PointF mTopSelectPoint;
	private PointF mBottomSelectPoint;
	private OnColorChangedListener mColorChangedListener;

	private boolean mTopMove = false;
	private boolean mBottomMove = false;
	private float mTopSlideBitmapRadius;
	private Bitmap mTopGradualChangeBitmap;
	private int mCallBackColor = Integer.MAX_VALUE;
	private SharedPreferences mColorPositionSp;
	private final int MOVE_COUNT = 300;
	private final int CUSTOM_HEIGHT = getContext().getResources().getInteger(R.integer.prize_custom_height);
	private Paint mTopLinePaint;
	private Paint mBottomLinePaint;
	private final int GRADUAL_BITMAP_WIDTH = getContext().getResources().getInteger(R.integer.prize_gradual_bitmap_width);    // Palette's width

	public PrizeNavBarColorPickerView(Context context) {
		this(context, null);
	}

	public PrizeNavBarColorPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		init();
	}

	public void setOnColorChangedListener(OnColorChangedListener listener) {
		mColorChangedListener = listener;
	}


	private void init() {
		mColorPositionSp = mContext.getSharedPreferences("COLOR_POSITION_SP",Context.MODE_PRIVATE);

		mBottomPaint = new Paint();
		mBottomPaint.setStyle(Paint.Style.FILL);
		mBottomPaint.setStrokeWidth(1);

		mBottomColors = new int[3];
		mBottomColors[0] = Color.BLACK;
		mBottomColors[2] = Color.GRAY;

		mTopPaint = new Paint();

		mTopSlideBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.top_slide);
		mTopSlideBitmap2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.top_slide);
		mTopSlideBitmapRadius = mTopSlideBitmap.getWidth() / 2;
		mTopSelectPoint = new PointF(MARGIN, MARGIN);

		mBottomSlideBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bottom_slide);
		mBottomSlideBitmap2 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.bottom_slide);
		//prize modify bug 39716 zj 20171014 start
		mBottomSelectPoint = new PointF(/*MARGIN, MARGIN*/MARGIN + GRADUAL_BITMAP_WIDTH/2,MARGIN);
		//prize modify bug 39716 zj 20171014 end

		BOTTOM_HEIGHT = getContext().getResources().getInteger(R.integer.prize_bottom_height);

		mTopLinePaint = new Paint();
		mBottomLinePaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int LINE_LEFT_MARGIN = getContext().getResources().getInteger(R.integer.prize_line_left_margin);			
		int LINE_TOP_MARGIN = getContext().getResources().getInteger(R.integer.prize_line_top_margin);			
		int LINE_BOTTOM_MARGIN = getContext().getResources().getInteger(R.integer.prize_line_bottom_margin);

		canvas.drawBitmap(getGradual(), null,new Rect(MARGIN, MARGIN, TOP_WIDTH + MARGIN, mTotalHeight - MARGIN - BOTTOM_HEIGHT * 2), mTopPaint);

		//prize delete bug:when select default color(white,black,pink ...),then enter automize color,
		// and then move topSelectPoint image, it will set twice navbarcolor.    2017929 start
//		mBottomColors[1] = mBottomPaint.getColor();
		//prize-delete-bug-39912 zhaojian 2017929 end
		mBottomPaint.setShader(new LinearGradient(MARGIN, mTotalHeight - MARGIN - BOTTOM_HEIGHT / 2,TOP_WIDTH + MARGIN,mTotalHeight - MARGIN - BOTTOM_HEIGHT / 2, mBottomColors, null, Shader.TileMode.MIRROR));
		canvas.drawRect(new Rect(MARGIN, mTotalHeight - MARGIN - BOTTOM_HEIGHT, TOP_WIDTH + MARGIN, mTotalHeight - MARGIN), mBottomPaint);

		if (mTopMove) {
			canvas.drawBitmap(mTopSlideBitmap, mTopSelectPoint.x - mTopSlideBitmapRadius,
					mTopSelectPoint.y - mTopSlideBitmapRadius, mTopPaint);
		} else {
			canvas.drawBitmap(mTopSlideBitmap2, mTopSelectPoint.x - mTopSlideBitmapRadius,
					mTopSelectPoint.y - mTopSlideBitmapRadius, mTopPaint);
		}

		if (mBottomMove) {
			canvas.drawBitmap(mBottomSlideBitmap, mBottomSelectPoint.x - mTopSlideBitmapRadius , mTotalHeight - MARGIN - mTopSlideBitmapRadius-BOTTOM_HEIGHT/2-1, mTopPaint);
		} else {
			canvas.drawBitmap(mBottomSlideBitmap2, mBottomSelectPoint.x - mTopSlideBitmapRadius , mTotalHeight - MARGIN - mTopSlideBitmapRadius-BOTTOM_HEIGHT/2-1, mTopPaint);
		}

		mTopLinePaint.setColor(getResources().getColor(R.color.buttonPressGray));
		mTopLinePaint.setStrokeWidth(1);

		canvas.drawLine(LINE_LEFT_MARGIN, LINE_TOP_MARGIN,MARGIN+TOP_WIDTH+ LINE_LEFT_MARGIN, LINE_TOP_MARGIN,mTopLinePaint);
		

		mBottomLinePaint.setColor(getResources().getColor(R.color.buttonPressGray));
		mBottomLinePaint.setStrokeWidth(1);

		canvas.drawLine(LINE_LEFT_MARGIN,CUSTOM_HEIGHT- LINE_BOTTOM_MARGIN,MARGIN+TOP_WIDTH+ LINE_LEFT_MARGIN,CUSTOM_HEIGHT- LINE_BOTTOM_MARGIN,mBottomLinePaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		if (widthMode == MeasureSpec.EXACTLY) {
			mTotalWidth = width;
		} else {
			mTotalWidth = getContext().getResources().getInteger(R.integer.prize_total_width);
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			mTotalHeight = height;
		} else {
			mTotalHeight = getContext().getResources().getInteger(R.integer.prize_total_height);
		}

		TOP_WIDTH = mTotalWidth - MARGIN * 2;
		setMeasuredDimension(mTotalWidth, mTotalHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x = event.getX() ;
		float y = event.getY() ;
		int topColor;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				downInTop = inTopPanel(x, y);
				downInBottom = inBottomPanel(x, y);
				if (downInTop) {
					mTopMove = true;
					proofTop(x, y);

					topColor = getTopColor(mTopSelectPoint.x - MARGIN, mTopSelectPoint.y - MARGIN );
					mBottomPaint.setColor(topColor);

					//prize add bug:when select default color(white,black,pink ...),then enter automize color,
					// and then move topSelectPoint image, it will set twice navbarcolor.    2017929 start
					mBottomColors[1] = mBottomPaint.getColor();
					Log.d(TAG,"onDraw     mBottomColors[1] = " + mBottomColors[1]);
					//prize-add-bug-39912 zhaojian 2017929 end

				} else if (downInBottom) {
					mBottomMove = true;
					proofBottom(x, y);
				}

				invalidate();

				int bottomColor = getBottomColor(mBottomSelectPoint.x - MARGIN);
				if (mCallBackColor == Integer.MAX_VALUE || mCallBackColor != bottomColor) {
					mCallBackColor = bottomColor;
				} else {
					break;
				}
				if (mColorChangedListener != null) {
					mColorChangedListener.onColorChanged(mCallBackColor,mTopSelectPoint,mBottomSelectPoint);
				}

				break;
			case MotionEvent.ACTION_UP:
				if (downInTop) {
					downInTop = false;
				} else if (downInBottom) {
					downInBottom = false;
				}
				mTopMove = false;
				mBottomMove = false;
				invalidate();
				Log.d(TAG,"getBottomColor = " + getBottomColor(mBottomSelectPoint.x - MARGIN) + ",mBottomSelectPoint.x  = " +mBottomSelectPoint.x );
				if (mColorChangedListener != null) {
					mColorChangedListener.onColorChanged(getBottomColor(mBottomSelectPoint.x - MARGIN),mTopSelectPoint,mBottomSelectPoint);
				}
		}
		return true;
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mTopGradualChangeBitmap != null && !mTopGradualChangeBitmap.isRecycled()) {
			mTopGradualChangeBitmap.recycle();
		}
		if (mTopSlideBitmap != null && !mTopSlideBitmap.isRecycled()) {
			mTopSlideBitmap.recycle();
		}
		if (mTopSlideBitmap2 != null && !mTopSlideBitmap2.isRecycled()) {
			mTopSlideBitmap2.recycle();
		}
		if (mBottomSlideBitmap != null && !mBottomSlideBitmap.isRecycled()) {
			mBottomSlideBitmap.recycle();
		}
		if (mBottomSlideBitmap2 != null && !mBottomSlideBitmap2.isRecycled()) {
			mBottomSlideBitmap2.recycle();
		}
		super.onDetachedFromWindow();
	}

	private Bitmap getGradual() {
		if (mTopGradualChangeBitmap == null) {
			Paint topPaint = new Paint();
			topPaint.setStrokeWidth(1);

			mTopGradualChangeBitmap = Bitmap.createBitmap(GRADUAL_BITMAP_WIDTH, CUSTOM_HEIGHT - 2 * MARGIN - BOTTOM_HEIGHT * 2, Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(mTopGradualChangeBitmap);
			int bitmapWidth = mTopGradualChangeBitmap.getWidth();
			TOP_WIDTH = bitmapWidth;
			int bitmapHeight = mTopGradualChangeBitmap.getHeight();
			int[] topColors = new int[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
			Shader topShader = new LinearGradient(0, bitmapHeight / 2, bitmapWidth, bitmapHeight / 2, topColors, null, Shader.TileMode.REPEAT);
			LinearGradient shadowShader = new LinearGradient(bitmapWidth / 2, 0, bitmapWidth / 2, bitmapHeight,
					Color.BLACK, Color.WHITE,Shader.TileMode.CLAMP);
			ComposeShader shader = new ComposeShader(topShader, shadowShader, PorterDuff.Mode.SCREEN);
			topPaint.setShader(shader);
			canvas.drawRect(0, 0, bitmapWidth, bitmapHeight, topPaint);
		}
		return mTopGradualChangeBitmap;
	}

    private boolean inTopPanel(float x, float y) {
		return (0 < x && x < MARGIN + TOP_WIDTH + MARGIN / 2 && 0 < y &&
				y < mTotalHeight - MARGIN - BOTTOM_HEIGHT*2);
    }

	private boolean inBottomPanel(float x, float y) {
		return (mTotalHeight - MARGIN - BOTTOM_HEIGHT - MARGIN / 2 < y &&
				y < mTotalHeight && 0 < x && x < TOP_WIDTH + MARGIN);
	}

	// Correct area to prevent out of bounds
	private void proofTop(float x, float y) {
		if (x < MARGIN) {
			mTopSelectPoint.x = MARGIN;
		} else if (x > (MARGIN + TOP_WIDTH)) {
			mTopSelectPoint.x = MARGIN + TOP_WIDTH;
		} else {
			mTopSelectPoint.x = x;
		}
		if (y < MARGIN) {
			mTopSelectPoint.y = MARGIN;
		} else if (y > (mTotalHeight - MARGIN - BOTTOM_HEIGHT * 2)) {
			mTopSelectPoint.y = mTotalHeight - MARGIN - BOTTOM_HEIGHT * 2;
		} else {
			mTopSelectPoint.y = y;
		}
	}

	private void proofBottom(float x, float y) {
		if (x < MARGIN) {
			mBottomSelectPoint.x = MARGIN;
		} else if (x > (MARGIN + TOP_WIDTH)) {
			mBottomSelectPoint.x = MARGIN + TOP_WIDTH;
		} else {
			mBottomSelectPoint.x = x;
		}
		if (y < mTotalHeight - MARGIN - BOTTOM_HEIGHT) {
			mBottomSelectPoint.y = mTotalHeight - MARGIN - BOTTOM_HEIGHT;
		} else if (y > (mTotalHeight - MARGIN)) {
			mBottomSelectPoint.y = mTotalHeight - MARGIN;
		} else {
			mBottomSelectPoint.y = y;
		}

	}

	private int getTopColor(float x, float y) {
		Bitmap temp = getGradual();
		// in order to prevent out of bound
		int intX = (int) x;
		int intY = (int) y;
		if (intX >= temp.getWidth()) {
			intX = temp.getWidth() - 1;
		}
		if (intY >= temp.getHeight()) {
			intY = temp.getHeight() - 1;
		}
		return temp.getPixel(intX, intY);
	}

	private int getBottomColor(float x) {
		int a, r, g, b, so, dst;
		float p;

		float bottomHalfWidth = (mTotalWidth - (float) MARGIN * 2) / 2;
		Log.d(TAG,"bottomHalfWidth = " + bottomHalfWidth + "x = " + x);
		if (x < bottomHalfWidth) {
			so = mBottomColors[0];
			dst = mBottomColors[1];
			p = x / bottomHalfWidth;
			Log.d(TAG,"1   so = " + so + ",dst = " + dst + ",p = " + p);
		} else {
			so = mBottomColors[1];
			dst = mBottomColors[2];
			p = (x - bottomHalfWidth) / bottomHalfWidth;
			Log.d(TAG,"2   so = " + so + "dst = " + dst + ",p = " + p);
		}

		a = ave(Color.alpha(so), Color.alpha(dst), p);
		r = ave(Color.red(so), Color.red(dst), p);
		g = ave(Color.green(so), Color.green(dst), p);
		b = ave(Color.blue(so), Color.blue(dst), p);
		Log.d(TAG,"a = " + a + "r = " + r + ",g = " + g + ",b = " + b);
		return Color.argb(a, r, g, b);
	}

	private int ave(int s, int d, float p) {
		return s + Math.round(p * (d - s));
	}

	public interface OnColorChangedListener {
		void onColorChanged(int color,PointF topSelectPoint,PointF bottomSelectPoint);
	}

	public void recentColorPosition(float topX,float topY,float bottomX,float bottomY){
		mTopSelectPoint = new PointF(topX,topY);
		mBottomSelectPoint = new PointF(bottomX,bottomY);
		mBottomPaint.setColor(getTopColor(mTopSelectPoint.x - MARGIN, mTopSelectPoint.y - MARGIN ));
		//prize add bug:when select default color(white,black,pink ...),then enter automize color,
		// and then move topSelectPoint image, it will set twice navbarcolor.    2017929 start
		mBottomColors[1] = mBottomPaint.getColor();
		//prize 2017929 end
		invalidate();
	}

	public void moveRecentPosition(final float topX, final float topY, final float bottomX, final float bottomY/*,ExecutorService executorService*/){
		final float originalTopX = mColorPositionSp.getFloat("recent_top_x",0);
		final float originalTopY = mColorPositionSp.getFloat("recent_top_y",0);
		final float originalBottomX = mColorPositionSp.getFloat("recent_bottom_x",0);
		final float originalBottomY = mColorPositionSp.getFloat("recent_bottom_y",0);
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < MOVE_COUNT; i++) {
				
					if(topX >= originalTopX && topY >= originalTopY){
						mTopSelectPoint = new PointF(originalTopX + (topX-originalTopX)/MOVE_COUNT*i,originalTopY + (topY-originalTopY)/MOVE_COUNT*i);
					}

					if(topX <= originalTopX && topY <= originalTopY){
						mTopSelectPoint = new PointF(originalTopX - (originalTopX-topX)/MOVE_COUNT*i,originalTopY - (originalTopY-topY)/MOVE_COUNT*i);

					}

					if(topX <= originalTopX && topY >= originalTopY){
						mTopSelectPoint = new PointF(originalTopX - (originalTopX-topX)/MOVE_COUNT*i,originalTopY + (topY-originalTopY)/MOVE_COUNT*i);
					}

					if(topX >= originalTopX && topY <= originalTopY){
						mTopSelectPoint = new PointF(originalTopX + (topX-originalTopX)/MOVE_COUNT*i,originalTopY - (originalTopY-topY)/MOVE_COUNT*i);
					}

					
					if(bottomX >= originalBottomX){
						mBottomSelectPoint = new PointF(originalBottomX + (bottomX-originalBottomX)/MOVE_COUNT*i,bottomY);
					}else if(bottomX < originalBottomX){
						mBottomSelectPoint = new PointF(originalBottomX - (originalBottomX-bottomX)/MOVE_COUNT*i,bottomY);
					}

					SystemClock.sleep(1);
					postInvalidate();
				}
			}
		}).start();
	}

}