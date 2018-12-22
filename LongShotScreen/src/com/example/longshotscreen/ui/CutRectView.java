package com.example.longshotscreen.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import com.example.longshotscreen.utils.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.example.longshotscreen.R;

public class CutRectView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String[] mColors;
	private int HINT_TEXT_BELOW_HINT_IMAGE;
	private float HINT_TEXT_SIZE;
	private float RADIUS;
	private int RECT_MIN_LENGTH;
	private int RECT_STROKE_WIDTH;
	private float TOUCH_DISTANCE;
	private Bitmap hintBitmap;
	private Canvas mCanvas;
	private Context mContext;
	private int mDownHeight;
	private Point mDownPoint = new Point();
	private int mDownRectBottom;
	private int mDownRectLeft;
	private int mDownRectRight;
	private int mDownRectTop;
	private int mDownWidth;
	private FunnyShotView mFunnyShotView;
	private Handler mHandler;
	private Rect mHintRect;
	private boolean mIsMulPointer = false;
	private Point mMovePoint = new Point();
	private Paint mPaint;
	private Point[] mPoints = new Point[8];
	private Rect mRect;
	private int mRectColor;
	private SurfaceHolder mSurfaceHolder;
	private int pressPointPosition;
	private int screenHeight;
	private int screenWidth;

	//syc add strat
	public boolean isActionUp = false;
	public boolean canGetPosition = false;
	public boolean atpoint = false;
	//syc add end
	static {
		String[] arrayOfString = new String[6];
		arrayOfString[0] = "#55000000";
		arrayOfString[1] = "#44000000";
		arrayOfString[2] = "#33000000";
		arrayOfString[3] = "#22000000";
		arrayOfString[4] = "#11000000";
		arrayOfString[5] = "#00000000";
		mColors = arrayOfString;
	}

	public CutRectView(Context paramContext, Rect paramRect, int paramInt1,
			int paramInt2, Handler paramHandler) {
		super(paramContext);
		this.mContext = paramContext;
		this.mRectColor = paramContext.getResources().getColor(
				R.color.rect_line_color);
		this.mFunnyShotView = FunnyShotView.mInstance;
		this.mRect = paramRect;
		this.screenWidth = paramInt1;
		this.screenHeight = paramInt2;
		this.RADIUS = 10.0F;
		this.RECT_MIN_LENGTH = 25;
		this.HINT_TEXT_SIZE = 40.0F;
		this.RECT_STROKE_WIDTH = 3;
		this.HINT_TEXT_BELOW_HINT_IMAGE = 50;
		this.TOUCH_DISTANCE = 50.0F; //syc M
		Log.i("CutRectView", "enter CutRectView... mRect = " + this.screenWidth
				+ this.screenHeight);
		this.mHandler = paramHandler;
		this.hintBitmap = BitmapFactory.decodeResource(
				paramContext.getResources(), R.drawable.rectshot_hint);
		this.mHintRect = new Rect((paramInt1 - this.hintBitmap.getWidth()) / 2,
				(paramInt2 - this.hintBitmap.getHeight()) / 2,
				(paramInt1 + this.hintBitmap.getWidth()) / 2,
				(paramInt2 + this.hintBitmap.getHeight()) / 2);
		this.mSurfaceHolder = getHolder();
		setZOrderOnTop(true);
		this.mSurfaceHolder.setFormat(-3);
		this.mSurfaceHolder.addCallback(this);
	}

	private void animationEnterDrawSelectRect() {
		// if (!this.mFunnyShotView.getIsStartRectShot())
		// return;
		for (int i = 10; i > 0; --i)
			drawSelectRect(i - 1);
	}

	private void animationExitDrawSelectRect() {
		for (int i = 0; i < 10; ++i){
			drawSelectRect(i);
		}
	}

	private void drawHint() {
		this.mPaint = new Paint();
		this.mCanvas = this.mSurfaceHolder.lockCanvas();
		this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		this.mCanvas.drawPaint(this.mPaint);
		this.mCanvas.drawColor(Color.parseColor("#55000000"));
		this.mCanvas.drawBitmap(
				this.hintBitmap,
				new Rect(0, 0, this.hintBitmap.getWidth(), this.hintBitmap
						.getHeight()), this.mHintRect, null);
		Paint localPaint = new Paint();
		localPaint.setTextSize(this.HINT_TEXT_SIZE);
		localPaint.setColor(-1);
		localPaint.setAntiAlias(true);
		localPaint.setTextAlign(Paint.Align.CENTER);
		this.mCanvas.drawText(
				this.mContext.getString(R.string.drag_to_select_capture_area),
				this.screenWidth / 2, this.mHintRect.bottom
				+ this.HINT_TEXT_BELOW_HINT_IMAGE, localPaint);
		this.mSurfaceHolder.unlockCanvasAndPost(this.mCanvas);
	}

	private void drawRect(int paramInt) {
		this.mPaint = new Paint();
		this.mCanvas = this.mSurfaceHolder.lockCanvas();
		this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
		this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		this.mCanvas.drawPaint(this.mPaint);
		this.mCanvas.drawColor(Color.parseColor(mColors[paramInt]));
		this.mPaint.setStyle(Paint.Style.FILL);
		if (this.mFunnyShotView.getIsStartRectShot())
			this.mCanvas.drawRect(this.mRect, this.mPaint);
		this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		this.mPaint.setColor(this.mRectColor);
		this.mPaint.setStyle(Paint.Style.STROKE);
		this.mPaint.setStrokeWidth(this.RECT_STROKE_WIDTH);
		if (this.mFunnyShotView.getIsStartRectShot()) {
			this.mCanvas.drawRect(this.mRect, this.mPaint);
			this.mPaint.setStyle(Paint.Style.FILL);
			this.mPaint.setAntiAlias(true);
			this.mCanvas.drawCircle(this.mRect.left, this.mRect.top,
					this.RADIUS, this.mPaint);
			this.mCanvas.drawCircle((this.mRect.left + this.mRect.right) / 2,
					this.mRect.top, this.RADIUS, this.mPaint);
			this.mCanvas.drawCircle(this.mRect.right, this.mRect.top,
					this.RADIUS, this.mPaint);
			this.mCanvas.drawCircle(this.mRect.left,
					(this.mRect.top + this.mRect.bottom) / 2, this.RADIUS,
					this.mPaint);
			this.mCanvas.drawCircle(this.mRect.right,
					(this.mRect.top + this.mRect.bottom) / 2, this.RADIUS,
					this.mPaint);
			this.mCanvas.drawCircle(this.mRect.left, this.mRect.bottom,
					this.RADIUS, this.mPaint);
			this.mCanvas.drawCircle((this.mRect.left + this.mRect.right) / 2,
					this.mRect.bottom, this.RADIUS, this.mPaint);
			this.mCanvas.drawCircle(this.mRect.right, this.mRect.bottom,
					this.RADIUS, this.mPaint);
		}
		this.mSurfaceHolder.unlockCanvasAndPost(this.mCanvas);
	}

	private void drawSelectRect(int paramInt) {
		this.mPaint = new Paint();
		this.mCanvas = this.mSurfaceHolder.lockCanvas();
		this.mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
		this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		this.mCanvas.drawPaint(this.mPaint);
		this.mCanvas.drawColor(Color.parseColor("#55000000"));
		this.mPaint.setStyle(Paint.Style.FILL);
		this.mCanvas.drawRect(this.mRect, this.mPaint);
		this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		this.mPaint.setColor(this.mRectColor);
		this.mPaint.setStyle(Paint.Style.STROKE);
		this.mPaint.setStrokeWidth(this.RECT_STROKE_WIDTH);
		this.mCanvas.drawRect(this.mRect, this.mPaint);
		this.mPaint.setStyle(Paint.Style.FILL);
		this.mPaint.setAntiAlias(true);
		this.mCanvas.drawCircle(this.mRect.left, this.mRect.top, this.RADIUS,
				this.mPaint);
		this.mCanvas.drawCircle((this.mRect.left + this.mRect.right) / 2,
				this.mRect.top, this.RADIUS, this.mPaint);
		this.mCanvas.drawCircle(this.mRect.right, this.mRect.top, this.RADIUS,
				this.mPaint);
		this.mCanvas.drawCircle(this.mRect.left,
				(this.mRect.top + this.mRect.bottom) / 2, this.RADIUS,
				this.mPaint);
		this.mCanvas.drawCircle(this.mRect.right,
				(this.mRect.top + this.mRect.bottom) / 2, this.RADIUS,
				this.mPaint);
		this.mCanvas.drawCircle(this.mRect.left, this.mRect.bottom,
				this.RADIUS, this.mPaint);
		this.mCanvas.drawCircle((this.mRect.left + this.mRect.right) / 2,
				this.mRect.bottom, this.RADIUS, this.mPaint);
		this.mCanvas.drawCircle(this.mRect.right, this.mRect.bottom,
				this.RADIUS, this.mPaint);
		this.mSurfaceHolder.unlockCanvasAndPost(this.mCanvas);
	}

	private static int getDistence(Point paramPoint1, Point paramPoint2) {
		return (int) Math.sqrt(Math.pow(paramPoint1.x - paramPoint2.x, 2.0D) + Math.pow(paramPoint1.y - paramPoint2.y, 2.0D));
	}

	private int getPosition(Point point) {
		int pos = -1;
		if ((pressPointPosition > 0) && (pressPointPosition < 9)) {
			pos = pressPointPosition;
		}
		Log.i("CutRectView", "getPosition...");
		setPointsValue();
		for (int i = 0; i < mPoints.length; i = i + 1) {
			if(!atpoint){
			if ((point.x > mRect.left) && (point.x < mRect.right) && (point.y > mRect.top) && (point.y < mRect.bottom)) {
				Log.i("CutRectView", "inside...");
				pos = 0;
			}
			}
			if ((float) getDistence(mPoints[i], point) < TOUCH_DISTANCE) {
				atpoint = true;
				pos = i + 1;
				return pos;
			}
		}
		return pos;
	}

	private void sendExitMessage() {
		this.mHandler.sendEmptyMessage(1113);
	}

	private void setPointsValue() {
		this.mPoints[0] = new Point(this.mRect.left, this.mRect.top);
		this.mPoints[1] = new Point((this.mRect.left + this.mRect.right) / 2, this.mRect.top);
		this.mPoints[2] = new Point(this.mRect.right, this.mRect.top);
		this.mPoints[3] = new Point(this.mRect.left, (this.mRect.top + this.mRect.bottom) / 2);
		this.mPoints[4] = new Point(this.mRect.right, (this.mRect.top + this.mRect.bottom) / 2);
		this.mPoints[5] = new Point(this.mRect.left, this.mRect.bottom);
		this.mPoints[6] = new Point((this.mRect.left + this.mRect.right) / 2, this.mRect.bottom);
		this.mPoints[7] = new Point(this.mRect.right, this.mRect.bottom);
	}

	protected void animationEnterDraw() {
		for (int i = 6; i > 0; --i)
			drawRect(i - 1);
		animationEnterDrawSelectRect();
		drawHint();
	}

	protected void animationExitDraw() {
		animationExitDrawSelectRect();
		for (int i = 0; i < 6; ++i){
			drawRect(i);
		}
	}

	protected void drawRect() {
		Log.i("CutRectView", "enter drawRect... mRect = " + this.mRect);
		mPaint = new Paint();
		mCanvas = this.mSurfaceHolder.lockCanvas();
		mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, 3));
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		mCanvas.drawPaint(this.mPaint);
		mCanvas.drawColor(Color.parseColor("#55000000"));
		mPaint.setStyle(Paint.Style.FILL);
		mCanvas.drawRect(this.mRect, this.mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
		mPaint.setColor(this.mRectColor);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(this.RECT_STROKE_WIDTH);
		mCanvas.drawRect(this.mRect, this.mPaint);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
		mCanvas.drawCircle(mRect.left, mRect.top, RADIUS,mPaint);
		mCanvas.drawCircle((mRect.left + mRect.right) / 2,mRect.top, RADIUS, mPaint);
		mCanvas.drawCircle(mRect.right, mRect.top, RADIUS,mPaint);
		mCanvas.drawCircle(mRect.left,(mRect.top + mRect.bottom) / 2, RADIUS,mPaint);
		mCanvas.drawCircle(mRect.right,(mRect.top + mRect.bottom) / 2, RADIUS,mPaint);
		mCanvas.drawCircle(mRect.left, mRect.bottom, RADIUS, mPaint);
		mCanvas.drawCircle((mRect.left + mRect.right) / 2,mRect.bottom, RADIUS, mPaint);
		mCanvas.drawCircle(mRect.right, mRect.bottom, RADIUS, mPaint);
		mSurfaceHolder.unlockCanvasAndPost(mCanvas);
	}

	protected void onConfigurationChanged(Configuration paramConfiguration) {
		sendExitMessage();
	}

	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//syc add start
			 this.mHandler.sendEmptyMessage(0x11);
			 this.mDownPoint.x = (int)event.getX();
		     this.mDownPoint.y = (int)event.getY();
		     this.mMovePoint.x = this.mDownPoint.x;
		     this.mMovePoint.y = this.mDownPoint.y;
			//syc add end
			
			//this.mDownPoint.x = (int) event.getX();
			//this.mDownPoint.y = (int) event.getY();
		if (this.mFunnyShotView.getIsStartRectShot()){
			this.mDownRectLeft = this.mRect.left;
			this.mDownRectTop = this.mRect.top;
			this.mDownRectRight = this.mRect.right;
			this.mDownRectBottom = this.mRect.bottom;
			this.mDownHeight = this.mRect.height();
			this.mDownWidth = this.mRect.width();
		}
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i("CutRectView", "enter event.ACTION_DOWN...");
			//this.mHandler.sendEmptyMessage(0x11);
			this.mMovePoint.x = (int) event.getX();
			this.mMovePoint.y = (int) event.getY();
			Log.i("CutRectView", "enter event.ACTION_MOVE...");

			if (!this.mFunnyShotView.getIsStartRectShot()) {

				if ((this.mMovePoint.x < this.mDownPoint.x) && (this.mMovePoint.y < this.mDownPoint.y)) {
					if (this.mDownPoint.x - this.mMovePoint.x < this.RECT_MIN_LENGTH)
						this.mMovePoint.x = (this.mDownPoint.x - this.RECT_MIN_LENGTH);
					if (this.mDownPoint.y - this.mMovePoint.y < this.RECT_MIN_LENGTH)
						this.mMovePoint.y = (this.mDownPoint.y - this.RECT_MIN_LENGTH);
					if (this.mMovePoint.x <= 0)
						this.mMovePoint.x = 0;
					if (this.mDownPoint.x >= -1 + this.screenWidth)
						this.mDownPoint.x = (-1 + this.screenWidth);
					if (this.mMovePoint.y <= 0)
						this.mMovePoint.y = 0;
					if (this.mDownPoint.y >= -1 + this.screenHeight)
						this.mDownPoint.y = (-1 + this.screenHeight);
					this.mRect.left = this.mMovePoint.x;
					this.mRect.top = this.mMovePoint.y;
					this.mRect.right = this.mDownPoint.x;
					this.mRect.bottom = this.mDownPoint.y;
					drawRect();
					this.mIsMulPointer = true;
				}
				if ((this.mMovePoint.x > this.mDownPoint.x) && (this.mMovePoint.y < this.mDownPoint.y)) {
					if (this.mMovePoint.x - this.mDownPoint.x < this.RECT_MIN_LENGTH)
						this.mMovePoint.x = (this.mDownPoint.x + this.RECT_MIN_LENGTH);
					if (this.mDownPoint.y - this.mMovePoint.y < this.RECT_MIN_LENGTH)
						this.mMovePoint.y = (this.mDownPoint.y - this.RECT_MIN_LENGTH);
					if (this.mDownPoint.x <= 0)
						this.mDownPoint.x = 0;
					if (this.mMovePoint.x >= -1 + this.screenWidth)
						this.mMovePoint.x = (-1 + this.screenWidth);
					if (this.mMovePoint.y <= 0)
						this.mMovePoint.y = 0;
					if (this.mDownPoint.y >= -1 + this.screenHeight)
						this.mDownPoint.y = (-1 + this.screenHeight);
					this.mRect.left = this.mDownPoint.x;
					this.mRect.top = this.mMovePoint.y;
					this.mRect.right = this.mMovePoint.x;
					this.mRect.bottom = this.mDownPoint.y;
					drawRect();
					this.mIsMulPointer = true;
				}
				if ((this.mMovePoint.x < this.mDownPoint.x) && (this.mMovePoint.y > this.mDownPoint.y)) {
					if (this.mDownPoint.x - this.mMovePoint.x < this.RECT_MIN_LENGTH)
						this.mMovePoint.x = (this.mDownPoint.x - this.RECT_MIN_LENGTH);
					if (this.mMovePoint.y - this.mDownPoint.y < this.RECT_MIN_LENGTH)
						this.mMovePoint.y = (this.mDownPoint.y + this.RECT_MIN_LENGTH);
					if (this.mMovePoint.x <= 0)
						this.mMovePoint.x = 0;
					if (this.mDownPoint.x >= -1 + this.screenWidth)
						this.mDownPoint.x = (-1 + this.screenWidth);
					if (this.mDownPoint.y <= 0)
						this.mDownPoint.y = 0;
					if (this.mMovePoint.y >= -1 + this.screenHeight)
						this.mMovePoint.y = (-1 + this.screenHeight);
					this.mRect.left = this.mMovePoint.x;
					this.mRect.top = this.mDownPoint.y;
					this.mRect.right = this.mDownPoint.x;
					this.mRect.bottom = this.mMovePoint.y;
					drawRect();
					this.mIsMulPointer = true;
				}
				if ((this.mMovePoint.x > this.mDownPoint.x) && (this.mMovePoint.y > this.mDownPoint.y)) {
					if (this.mMovePoint.x - this.mDownPoint.x < this.RECT_MIN_LENGTH)
						this.mMovePoint.x = (this.mDownPoint.x + this.RECT_MIN_LENGTH);
					if (this.mMovePoint.y - this.mDownPoint.y < this.RECT_MIN_LENGTH)
						this.mMovePoint.y = (this.mDownPoint.y + this.RECT_MIN_LENGTH);
					if (this.mDownPoint.x <= 0)
						this.mDownPoint.x = 0;
					if (this.mMovePoint.x >= -1 + this.screenWidth)
						this.mMovePoint.x = (-1 + this.screenWidth);
					if (this.mDownPoint.y <= 0)
						this.mDownPoint.y = 0;
					if (this.mMovePoint.y >= -1 + this.screenHeight)
						this.mMovePoint.y = (-1 + this.screenHeight);
					this.mRect.left = this.mDownPoint.x;
					this.mRect.top = this.mDownPoint.y;
					this.mRect.right = this.mMovePoint.x;
					this.mRect.bottom = this.mMovePoint.y;
					drawRect();
					this.mIsMulPointer = true;
				}
			}
			if(canGetPosition){
			switch (getPosition(this.mMovePoint)) {
			case 0:
				int i = this.mMovePoint.x - this.mDownPoint.x;
				int j = this.mMovePoint.y - this.mDownPoint.y;
				this.mRect.left = (i + this.mDownRectLeft);
				if (this.mRect.left < 0)
					this.mRect.left = 0;
				if (this.mRect.left > -1 + (this.screenWidth - this.mDownWidth))
					this.mRect.left = (-1 + (this.screenWidth - this.mDownWidth));
				this.mRect.top = (j + this.mDownRectTop);
				if (this.mRect.top < 0)
					this.mRect.top = 0;
				if (this.mRect.top > -1
						+ (this.screenHeight - this.mDownHeight))
					this.mRect.top = (-1 + (this.screenHeight - this.mDownHeight));
				this.mRect.right = (i + this.mDownRectRight);
				if (this.mRect.right < -1 + this.mDownWidth)
					this.mRect.right = this.mDownWidth;
				if (this.mRect.right > -1 + this.screenWidth)
					this.mRect.right = (-1 + this.screenWidth);
				this.mRect.bottom = (j + this.mDownRectBottom);
				if (this.mRect.bottom < -1 + this.mDownHeight)
					this.mRect.bottom = this.mDownHeight;
				if (this.mRect.bottom > -1 + this.screenHeight)
					this.mRect.bottom = (-1 + this.screenHeight);
				drawRect();
				this.pressPointPosition = 0;
				break;
			case 1:
				this.mRect.left = this.mMovePoint.x;
				this.mRect.top = this.mMovePoint.y;
				if (this.mDownRectRight - this.mMovePoint.x < this.RECT_MIN_LENGTH)
					this.mRect.left = (this.mDownRectRight - this.RECT_MIN_LENGTH);
				if (this.mDownRectBottom - this.mMovePoint.y < this.RECT_MIN_LENGTH)
					this.mRect.top = (this.mDownRectBottom - this.RECT_MIN_LENGTH);
				if (this.mRect.left < 0)
					this.mRect.left = 0;
				if (this.mRect.top < 0)
					this.mRect.top = 0;
				drawRect();
				this.pressPointPosition = 1;
				break;
			case 2:
				this.mRect.top = this.mMovePoint.y;
				if (this.mDownRectBottom - this.mMovePoint.y < this.RECT_MIN_LENGTH)
					this.mRect.top = (this.mDownRectBottom - this.RECT_MIN_LENGTH);
				if (this.mRect.top < 0)
					this.mRect.top = 0;
				drawRect();
				this.pressPointPosition = 2;
				break;
			case 3:
				this.mRect.top = this.mMovePoint.y;
				this.mRect.right = this.mMovePoint.x;
				if (this.mDownRectBottom - this.mMovePoint.y < this.RECT_MIN_LENGTH)
				{
					this.mRect.top = (this.mDownRectBottom - this.RECT_MIN_LENGTH);
				}
				if (this.mMovePoint.x - this.mDownRectLeft < this.RECT_MIN_LENGTH)
				{
					this.mRect.right = (this.mDownRectLeft + this.RECT_MIN_LENGTH);
				}
				if (this.mRect.top < 0)
					this.mRect.top = 0;
				if (this.mRect.right > this.screenWidth - 1)
					this.mRect.right = (this.screenWidth - 1);
				drawRect();
				this.pressPointPosition = 3;
				break;
			case 4:
				this.mRect.left = this.mMovePoint.x;
				if (this.mDownRectRight - this.mMovePoint.x < this.RECT_MIN_LENGTH)
					this.mRect.left = (this.mDownRectRight - this.RECT_MIN_LENGTH);
				if (this.mRect.left < 0)
					this.mRect.left = 0;
				drawRect();
				this.pressPointPosition = 4;
				break;
			case 5:
				this.mRect.right = this.mMovePoint.x;
				if (this.mMovePoint.x - this.mDownRectLeft < this.RECT_MIN_LENGTH)
					this.mRect.right = (this.mDownRectLeft + this.RECT_MIN_LENGTH);
				if (this.mRect.right > this.screenWidth - 1 - this.RECT_STROKE_WIDTH - this.RADIUS)
					this.mRect.right = (this.screenWidth - 1);
				drawRect();
				this.pressPointPosition = 5;
				break;
			case 6:
				this.mRect.left = this.mMovePoint.x;
				this.mRect.bottom = this.mMovePoint.y;
				if (this.mDownRectRight - this.mMovePoint.x < this.RECT_MIN_LENGTH)
					this.mRect.left = (this.mDownRectRight - this.RECT_MIN_LENGTH);
				if (this.mMovePoint.y - this.mDownRectTop < this.RECT_MIN_LENGTH)
					this.mRect.bottom = (this.mDownRectTop + this.RECT_MIN_LENGTH);
				if (this.mRect.left < 0)
					this.mRect.left = 0;
				if (this.mRect.bottom > this.screenHeight - 1)
					this.mRect.bottom = (this.screenHeight - 1);
				drawRect();
				this.pressPointPosition = 6;
				break;
			case 7:
				this.mRect.bottom = this.mMovePoint.y;
				if (this.mMovePoint.y - this.mDownRectTop < this.RECT_MIN_LENGTH)
					this.mRect.bottom = (this.mDownRectTop + this.RECT_MIN_LENGTH);
				if (this.mRect.bottom > this.screenHeight - 1)
					this.mRect.bottom = (this.screenHeight - 1);
				drawRect();
				this.pressPointPosition = 7;
				break;
			case 8:
				this.mRect.right = this.mMovePoint.x;
				this.mRect.bottom = this.mMovePoint.y;
				if (this.mMovePoint.x - this.mDownRectLeft < this.RECT_MIN_LENGTH)
				{
					this.mRect.right = (this.mDownRectLeft + this.RECT_MIN_LENGTH);
				}
				if (this.mMovePoint.y - this.mDownRectTop < this.RECT_MIN_LENGTH)
				{
					this.mRect.bottom = (this.mDownRectTop + this.RECT_MIN_LENGTH);	
				}
				if (this.mRect.bottom > this.screenHeight - 1)
					this.mRect.bottom = (this.screenHeight - 1);
				if (this.mRect.right > this.screenWidth - 1)
					this.mRect.right = (this.screenWidth - 1);
				this.pressPointPosition = 8;
				drawRect();
			}
			}
			break;
		case MotionEvent.ACTION_UP:
			atpoint = false;
			//syc add start
			isActionUp = true;
			if(isActionUp && mIsMulPointer){
				canGetPosition = true;
				
			}
			//syc add end
			if ((!this.mFunnyShotView.getIsStartRectShot())
					&& (this.mMovePoint.x != this.mDownPoint.x)
					&& (this.mMovePoint.y != this.mDownPoint.y)) {
				this.mFunnyShotView.setIsStartRectShot(true);
			    this.mHandler.sendEmptyMessage(18);
			}
			this.pressPointPosition = 0;
		    if (this.mFunnyShotView.getIsStartRectShot()){
			this.mHandler.sendEmptyMessage(18);
			animationEnterDrawSelectRect();	
		    }
			break;
		default:
			break;
		}

		return true;
	}

	public void surfaceChanged(SurfaceHolder paramSurfaceHolder, int paramInt1,
			int paramInt2, int paramInt3) {
	}

	public void surfaceCreated(SurfaceHolder paramSurfaceHolder) {
	}

	public void surfaceDestroyed(SurfaceHolder paramSurfaceHolder) {
	}
}
