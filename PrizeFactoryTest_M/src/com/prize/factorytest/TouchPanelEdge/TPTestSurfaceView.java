package com.prize.factorytest.TouchPanelEdge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class TPTestSurfaceView extends SurfaceView implements Callback {
	
	private SurfaceHolder mHolder;
	
	public boolean bLinearity1 = false;
	public boolean bLinearity2 = false;
	public boolean bAccuracy = false;
	public boolean bSensitivity = false;
	public boolean bDeazone = false;	//DeadZone
	public boolean bZoom = false;		//Zoom
	public boolean bLinearityHorizontal = false;	//LinearityHorizontal
	public boolean bLinearityVertical = false;		//arrayLinearityVerticalX
	public boolean bFree = false;		//Free

	
	private final static int PAINT_COLOR_RED 	= 0;
	private final static int PAINT_COLOR_GREEN 	= 1;
	private final static int PAINT_COLOR_WHITE 	= 2;
	private Paint mRedPaint		= null;//0 PAINT_COLOR_RED
	private Paint mRedPaint2	= null;//0 PAINT_COLOR_RED
	private Paint mRedPaint3	= null;//0 PAINT_COLOR_RED
	private Paint mGreenPaint	= null;//1 PAINT_COLOR_GREEN
	private Paint mGreenPaint2	= null;//1 PAINT_COLOR_GREEN
	private Paint mGreenPaint3	= null;//1 PAINT_COLOR_GREEN
	private Paint mWhitePaint	= null;//2 PAINT_COLOR_WHITE
	private Paint mWhitePaint2	= null;//2 PAINT_COLOR_WHITE
	private Paint mBluePaint	= null;//3 PAINT_COLOR_BLUE
	private Paint mBluePaint2	= null;//3 PAINT_COLOR_BLUE
	
	// 1./2. Linearity
	private Path mTouchPath		= null;
	private Rect mInvalidRect	= new Rect();
	private boolean isDrawing = false;
	private boolean isReady = false;
	private float mPreviousX;
	private float mPreviousY;
	private float mCurveEndX;
	private float mCurveEndY;
	
	// 3. Accuracy
	private int[][] arrayAccuracyCircles;
	
	// 4. Sensitivity
	private int[][] arraySensitivityPoints;
	int mWidthSensitivity;
	private int mBorderType = SENSITIVITY_BORDER_1;
	private final static int SENSITIVITY_BORDER_1 = 1;
	private final static int SENSITIVITY_BORDER_2 = 2;
	private final static int SENSITIVITY_BORDER_3 = 3;
	private final static int SENSITIVITY_BORDER_4 = 4;
	private final static int SENSITIVITY_BORDER_5 = 5;
	private final static int SENSITIVITY_BORDER_6 = 6;
	private final static int SENSITIVITY_BORDER_7 = 7;
	private final static int SENSITIVITY_BORDER_8 = 8;
	private final static int SENSITIVITY_BORDER_9 = 9;
	private final static int SENSITIVITY_BORDER_10 = 10;
	private final static int SENSITIVITY_BORDER_11 = 11;
	
	// 6. Zoom
	private int[][] arrayZoomCircles;
	private double mPreviousDistance;
	
	// 7. Free
	private String mCoordinate = "x : 0  y : 0";
	
	
	private final int BORDER_DEFAULT= -1;
	private final int BORDER_LEFT 	= 1;
	private final int BORDER_RIGHT 	= 2;
	private final int BORDER_TOP 	= 3;
	private final int BORDER_BOTTOM = 4;
	private int mLinearityBorderType = BORDER_DEFAULT;
	// 8. LinearityHorizontal
	private int[][] arrayLinearityHorizontalY;
	private int mCurrentLinearityHorizontalPosition = 0;
	private int mLinearityHorizontalCount;
	
	// 9. LinearityVertical
	private int[][] arrayLinearityVerticalX;
	private int mCurrentLinearityVerticalPosition = 0;
	private int mLinearityVerticalCount;
	
	
	private final static int PAINT_WIDTH_1 = 2;
	private static int mPaintWidth2 = 0;		//shifty width.
	private static int mPaintWidth2_border = 0;	//shifty width.
	private final static int PAINT_WIDTH_3 = 10;

	private final static int LINEARITY_WIDTH = 70; 
	
	
	public final int TP_TEST_LINEARITY1 	= 1;
	public final int TP_TEST_LINEARITY2 	= 2;
	public final int TP_TEST_ACCURACY 		= 3;
	public final int TP_TEST_SENSITIVITY 	= 4; 
	public final int TP_TEST_DEADZONE 		= 5;//DeadZone
	public final int TP_TEST_ZOOM 			= 6;//Zoom
	public final int TP_TEST_LINEARITY_HORIZONTAL 	= 7;//LinearityHorizontal
	public final int TP_TEST_LINEARITY_VERTICAL 	= 8;//LinearityVertical
	public final int TP_TEST_FREE 			= 9;//Free

	
	public int mModeType = TP_TEST_LINEARITY_HORIZONTAL;//First view.

	public TPTestSurfaceView(Context context) {
		super(context);
		// Add callback to this SurfaceView,
		mHolder = getHolder();
		mHolder.addCallback(this);
		init();
		setFocusable(true);
	}

	private void setupAccuracy() {
		int width = getWidth();
		int height = getHeight();
		
		int WIDTH_ACCURACY_RADIUS1 = width / 8;
		int WIDTH_ACCURACY_RADIUS2 = width / 10;
		
		arrayAccuracyCircles = new int[][]{
				new int[]{WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width / 2, WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width - WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width / 4, height / 4, WIDTH_ACCURACY_RADIUS2, PAINT_COLOR_RED},
				new int[]{width * 3 / 4, height / 4, WIDTH_ACCURACY_RADIUS2, PAINT_COLOR_RED},
				new int[]{WIDTH_ACCURACY_RADIUS1, height / 2, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width / 2, height / 2, WIDTH_ACCURACY_RADIUS2, PAINT_COLOR_RED},
				new int[]{width - WIDTH_ACCURACY_RADIUS1, height / 2, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width / 4, height * 3 / 4, WIDTH_ACCURACY_RADIUS2, PAINT_COLOR_RED},
				new int[]{width * 3 / 4, height * 3 / 4, WIDTH_ACCURACY_RADIUS2, PAINT_COLOR_RED},
				new int[]{WIDTH_ACCURACY_RADIUS1, height - WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width / 2, height - WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
				new int[]{width - WIDTH_ACCURACY_RADIUS1, height - WIDTH_ACCURACY_RADIUS1, WIDTH_ACCURACY_RADIUS1, PAINT_COLOR_RED},
		};
	}

	private void setupSensitivity() {
		int width = getWidth();
		int height = getHeight();
		
		int widthSensitivity = mWidthSensitivity = width / 6;
		
		arraySensitivityPoints = new int[][]{
				new int[]{widthSensitivity, height},
				new int[]{widthSensitivity, widthSensitivity},
				new int[]{width - widthSensitivity, widthSensitivity},
				new int[]{width - widthSensitivity, height - widthSensitivity},
				new int[]{widthSensitivity * 2, height - widthSensitivity},
				new int[]{widthSensitivity * 2, widthSensitivity * 2},
				new int[]{width - widthSensitivity * 2, widthSensitivity * 2},
				new int[]{width - widthSensitivity * 2, height - widthSensitivity * 2},
				new int[]{width / 2, height - widthSensitivity * 2},
				new int[]{width / 2, widthSensitivity * 3}
		};
	}

	private void setupZoom() {
		int width = getWidth();
		int height = getHeight();
		
		arrayZoomCircles = new int[][]{
				new int[]{width / 2, height / 2, width / 2, PAINT_COLOR_RED},
				new int[]{width / 2, height / 2, width * 4 / 10, PAINT_COLOR_WHITE},
				new int[]{width / 2, height / 2, width / 10, PAINT_COLOR_RED}
		};
	}

	private void setupLinearityHorizontal() {
		int height = getHeight();
		
		int scale = 2;
		int heightLinearityHorizontalYBorder = height / (10  * scale);
		int middleHeight = height - (heightLinearityHorizontalYBorder * 2 * scale);
		int heightLinearityHorizontalY = middleHeight / (5 * scale);
		
		arrayLinearityHorizontalY = new int[][]{
				new int[]{heightLinearityHorizontalYBorder * 1 + heightLinearityHorizontalY * 0, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 2 + heightLinearityHorizontalY * 1, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 2 + heightLinearityHorizontalY * 3, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 2 + heightLinearityHorizontalY * 5, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 2 + heightLinearityHorizontalY * 7, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 2 + heightLinearityHorizontalY * 9, PAINT_COLOR_GREEN},
				new int[]{heightLinearityHorizontalYBorder * 3 + heightLinearityHorizontalY * 10, PAINT_COLOR_GREEN}
		};
		mPaintWidth2_border = heightLinearityHorizontalYBorder * 2;
		mPaintWidth2 = heightLinearityHorizontalY * 2;
		resetGreenPaint2();
		resetGreenPaint3();
		resetRedPaint2();
		resetRedPaint3();
	}

	private void setupLinearityVertical() {
		int width = getWidth();
		
		int scale = 2;
		int widthLinearityVerticalX = width / (6 * scale);
		
		arrayLinearityVerticalX = new int[][]{
				new int[]{widthLinearityVerticalX * 1, PAINT_COLOR_GREEN},
				new int[]{widthLinearityVerticalX * 3, PAINT_COLOR_GREEN},
				new int[]{widthLinearityVerticalX * 5, PAINT_COLOR_GREEN},
				new int[]{widthLinearityVerticalX * 7, PAINT_COLOR_GREEN},
				new int[]{widthLinearityVerticalX * 9, PAINT_COLOR_GREEN},
				new int[]{widthLinearityVerticalX * 11, PAINT_COLOR_GREEN}
		};
		mPaintWidth2 = widthLinearityVerticalX * 2;
		resetGreenPaint2();
		resetRedPaint2();
	}

	private void init() {
		initRedPaint();
		initRedPaint2();
		initRedPaint3();
		initGreenPaint();
		initGreenPaint2();
		initGreenPaint3();
		initWhitePaint();
		initWhitePaint2();
		initBluePaint();
		initBluePaint2();
		initTouchPath();
	}

	private void initRedPaint() {
		if (null == mRedPaint) {
			mRedPaint = new Paint();
			setPaintBaseAttribute(mRedPaint, Color.RED, PAINT_WIDTH_1);
		}
	}

	private void initRedPaint2() {
		if (null == mRedPaint2) {
			mRedPaint2 = new Paint();
			int red = Color.red(Color.RED);
			int green = Color.green(Color.RED);
			int blue = Color.blue(Color.RED);
			setPaintBaseAttribute(mRedPaint2, Color.argb(80, red, green, blue), mPaintWidth2);
		}
	}

	private void initRedPaint3() {
		if (null == mRedPaint3) {
			mRedPaint3 = new Paint();
			int red = Color.red(Color.RED);
			int green = Color.green(Color.RED);
			int blue = Color.blue(Color.RED);
			setPaintBaseAttribute(mRedPaint3, Color.argb(80, red, green, blue), mPaintWidth2_border);
		}
	}

	private void initGreenPaint() {
		if (null == mGreenPaint) {
			mGreenPaint = new Paint();
			setPaintBaseAttribute(mGreenPaint, Color.GREEN, PAINT_WIDTH_1);
		}
	}

	private void initGreenPaint2() {
		if (null == mGreenPaint2) {
			mGreenPaint2 = new Paint();
			int red = Color.red(Color.GREEN);
			int green = Color.green(Color.GREEN);
			int blue = Color.blue(Color.GREEN);
			setPaintBaseAttribute(mGreenPaint2, Color.argb(80, red, green, blue), mPaintWidth2);
		}
	}

	private void initGreenPaint3() {
		if (null == mGreenPaint3) {
			mGreenPaint3 = new Paint();
			int red = Color.red(Color.GREEN);
			int green = Color.green(Color.GREEN);
			int blue = Color.blue(Color.GREEN);
			setPaintBaseAttribute(mGreenPaint3, Color.argb(80, red, green, blue), mPaintWidth2_border);
		}
	}

	private void initWhitePaint() {
		if (null == mWhitePaint) {
			mWhitePaint = new Paint();
			setPaintBaseAttribute(mWhitePaint, Color.WHITE, PAINT_WIDTH_1);
			mWhitePaint.setTextSize(25);
		}
	}

	private void initWhitePaint2() {
		if (null == mWhitePaint2) {
			mWhitePaint2 = new Paint();
			setPaintBaseAttribute(mWhitePaint2, Color.WHITE, PAINT_WIDTH_3);
			mWhitePaint2.setTextSize(25);
		}
	}

	private void initBluePaint() {
		if (null == mBluePaint) {
			mBluePaint = new Paint();
			setPaintBaseAttribute(mBluePaint, Color.BLUE, PAINT_WIDTH_3);
			mBluePaint.setTextSize(25);
		}
	}

	private void initBluePaint2() {
		if (null == mBluePaint2) {
			mBluePaint2 = new Paint();
			int red = Color.red(Color.BLUE);
			int green = Color.green(Color.BLUE);
			int blue = Color.blue(Color.BLUE);
			setPaintBaseAttribute(mBluePaint2, Color.argb(150, red, green, blue), PAINT_WIDTH_3);
			mBluePaint2.setTextSize(25);
		}
	}

	private void setPaintBaseAttribute(Paint paint, int color, int width) {
		//Anti-aliasing.
		paint.setAntiAlias(true);	//Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setDither(true);		//Paint paint = new Paint(Paint.DITHER_FLAG);
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(width);
	}

	private void initTouchPath() {
		if (null == mTouchPath) {
			mTouchPath = new Path();
		}
	}

	private void resetRedPaint2() {
		mRedPaint2 = null;
		initRedPaint2();
	}

	private void resetRedPaint3() {
		mRedPaint3 = null;
		initRedPaint3();
	}

	private void resetGreenPaint2() {
		mGreenPaint2 = null;
		initGreenPaint2();
	}

	private void resetGreenPaint3() {
		mGreenPaint3 = null;
		initGreenPaint3();
	}

	private void resetWhitePaint() {
		mWhitePaint = null;
		initWhitePaint();
	}

	private void resetTouchPath() {
		if (null != mTouchPath) {
			mTouchPath.reset();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawView();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		 mHolder.removeCallback(this);
	}

	public void toNext() {
		isDrawing = false;
		isReady = false;
		switch (mModeType) {
		case TP_TEST_LINEARITY1:
			mModeType = TP_TEST_LINEARITY2;
			break;
		case TP_TEST_LINEARITY2:
			mModeType = TP_TEST_ACCURACY;
			break;
		case TP_TEST_ACCURACY:
			mModeType = TP_TEST_SENSITIVITY;
			break;
		case TP_TEST_SENSITIVITY:
			mModeType = TP_TEST_ZOOM;
			break;
		case TP_TEST_DEADZONE:
			
			break;
		case TP_TEST_ZOOM:
			mModeType = TP_TEST_FREE;
			break;
		case TP_TEST_FREE:
			
			break;
		case TP_TEST_LINEARITY_HORIZONTAL:
			mModeType = TP_TEST_LINEARITY_VERTICAL;
			break;
		case TP_TEST_LINEARITY_VERTICAL:
			mModeType = TP_TEST_FREE;
			break;

		default:
			break;
		}
		clearTouchPath();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (mModeType) {
		case TP_TEST_LINEARITY1:
			handleEventLinearity(event, TP_TEST_LINEARITY1);
			break;
		case TP_TEST_LINEARITY2:
			handleEventLinearity(event, TP_TEST_LINEARITY2);
			break;
		case TP_TEST_ACCURACY:
			handleEventAccuracy(event);
			break;
		case TP_TEST_SENSITIVITY:
			handleEventSensitivity(event);
			break;
		case TP_TEST_DEADZONE:
			
			break;
		case TP_TEST_ZOOM:
			handleEventZoom(event);
			break;
		case TP_TEST_FREE:
			handleEventFree(event);
			break;
		case TP_TEST_LINEARITY_HORIZONTAL:
			handleEventLinearityHorizontal(event);
			break;
		case TP_TEST_LINEARITY_VERTICAL:
			handleEventLinearityVertical(event);
			break;

		default:
			break;
		}
		return true;
	}

	private void handleEventLinearity(MotionEvent event, int i) {
		float currentX = event.getX();
		float currentY = event.getY();
		int marginX = 30;
		
		switch (i) {
		case TP_TEST_LINEARITY1:
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				clearTouchPath();
				if (currentX <= LINEARITY_WIDTH && currentY <= LINEARITY_WIDTH) {
					drawTouchDown(currentX, currentY);
				}
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (isDrawing) {
					float calculatedX = currentY * (getWidth() - marginX) / getHeight();
					if (currentX < calculatedX + LINEARITY_WIDTH && currentX > calculatedX - LINEARITY_WIDTH + 20) {
						if (currentX >= getWidth() - LINEARITY_WIDTH && currentY >= getHeight() - LINEARITY_WIDTH) {
							bLinearity1 = true;
							toNext();
						} else {
							drawTouchMove(currentX, currentY);
						}
					} else {
						isDrawing = false;
						clearTouchPath();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				drawTouchUp();
				break;
			default:
				break;
			}
			
			break;
		case TP_TEST_LINEARITY2:
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				clearTouchPath();
				if (currentX >= getWidth() - LINEARITY_WIDTH && currentY <= LINEARITY_WIDTH) {
					drawTouchDown(currentX, currentY);
				}
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (isDrawing) {
					float calculatedX = getWidth() - (currentY * (getWidth() - marginX) / getHeight()) - marginX;
					if (currentX < calculatedX + LINEARITY_WIDTH && currentX > calculatedX - LINEARITY_WIDTH + 20) {
						if (currentX <= LINEARITY_WIDTH && currentY >= getHeight() - LINEARITY_WIDTH) {
							bLinearity2 = true;
							toNext();
						} else {
							drawTouchMove(currentX, currentY);
						}
					} else {
						isDrawing = false;
						clearTouchPath();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				drawTouchUp();
				break;
			default:
				break;
			}
			
			break;

		default:
			break;
		}
	}

	private void handleEventAccuracy(MotionEvent event) {
		float currentX = event.getX();
		float currentY = event.getY();
		
		boolean isInCircle = false;
		boolean isAllAccuracy = false;
		
		if (null != arrayAccuracyCircles && arrayAccuracyCircles.length > 0) {
			int count = arrayAccuracyCircles.length;
			for (int i = 0; i < count; i++) {
				int centerX = arrayAccuracyCircles[i][0];
				int centerY = arrayAccuracyCircles[i][1];
				int radius = arrayAccuracyCircles[i][2];
				double distance = Math.sqrt(Math.abs(currentX - centerX) * Math.abs(currentX - centerX) + Math.abs(currentY - centerY) * Math.abs(currentY - centerY));
				if (distance <= radius) {
					arrayAccuracyCircles[i][3] = PAINT_COLOR_GREEN;
					isInCircle = true;
					drawView();
					
					for (int j = 0; j < count; j++) {
						if (arrayAccuracyCircles[j][3] == PAINT_COLOR_RED) {
							isAllAccuracy = false;
							break;
						} else {
							isAllAccuracy = true;
						}
					}
					if (isAllAccuracy) {
						bAccuracy = true;
						toNext();
					}
					break;
				} else {
					isInCircle = false;
				}
			}
			if (!isInCircle) {
				setupAccuracy();
				drawView();
			}
		}
	}

	private void handleEventSensitivity(MotionEvent event) {
		float currentX = event.getX();
		float currentY = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clearTouchPath();
			if (currentX <= mWidthSensitivity && currentY >= getHeight() - mWidthSensitivity) {
				drawTouchDown(currentX, currentY);
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDrawing) {
				boolean isAllowCrossBorder = false;
				
				switch (mBorderType) {
				case SENSITIVITY_BORDER_1:
					if (currentX <= arraySensitivityPoints[1][0]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX <= arraySensitivityPoints[1][0] && currentY <= arraySensitivityPoints[1][1]) {
						mBorderType = SENSITIVITY_BORDER_2;
					}
					break;
				case SENSITIVITY_BORDER_2:
					if (currentY <= arraySensitivityPoints[2][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[2][0] && currentY <= arraySensitivityPoints[2][1]) {
						mBorderType = SENSITIVITY_BORDER_3;
					}
					break;
				case SENSITIVITY_BORDER_3:
					if (currentX >= arraySensitivityPoints[3][0]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[3][0] && currentY >= arraySensitivityPoints[3][1]) {
						mBorderType = SENSITIVITY_BORDER_4;
					}
					break;
				case SENSITIVITY_BORDER_4://point 1 and point 5
					if (currentX >= arraySensitivityPoints[0][0] && currentY >= arraySensitivityPoints[4][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[0][0] && currentX <= arraySensitivityPoints[4][0] && currentY >= arraySensitivityPoints[4][1]) {
						mBorderType = SENSITIVITY_BORDER_5;
					}
					break;
				case SENSITIVITY_BORDER_5://point 2 and point 6
					if (currentX >= arraySensitivityPoints[1][0] && currentX <= arraySensitivityPoints[5][0] && currentY >= arraySensitivityPoints[1][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[1][0] && currentX <= arraySensitivityPoints[5][0] && currentY >= arraySensitivityPoints[1][1] && currentY <= arraySensitivityPoints[5][1]) {
						mBorderType = SENSITIVITY_BORDER_6;
					}
					break;
				case SENSITIVITY_BORDER_6://point 3 and point 7
					if (currentX >= arraySensitivityPoints[1][0] && currentX <= arraySensitivityPoints[2][0] && currentY >= arraySensitivityPoints[2][1] && currentY <= arraySensitivityPoints[6][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[6][0] && currentX <= arraySensitivityPoints[2][0] && currentY >= arraySensitivityPoints[2][1] && currentY <= arraySensitivityPoints[6][1]) {
						mBorderType = SENSITIVITY_BORDER_7;
					}
					break;
				case SENSITIVITY_BORDER_7://point 4 and point 8
					if (currentX >= arraySensitivityPoints[7][0] && currentX <= arraySensitivityPoints[3][0] && currentY >= arraySensitivityPoints[2][1] && currentY <= arraySensitivityPoints[3][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[7][0] && currentX <= arraySensitivityPoints[3][0] && currentY >= arraySensitivityPoints[7][1] && currentY <= arraySensitivityPoints[3][1]) {
						mBorderType = SENSITIVITY_BORDER_8;
					}
					break;
				case SENSITIVITY_BORDER_8://point 5 and point 9
					if (currentX >= arraySensitivityPoints[4][0] && currentX <= arraySensitivityPoints[3][0] && currentY >= arraySensitivityPoints[8][1] && currentY <= arraySensitivityPoints[4][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[4][0] && currentX <= arraySensitivityPoints[8][0] && currentY >= arraySensitivityPoints[8][1] && currentY <= arraySensitivityPoints[4][1]) {
						mBorderType = SENSITIVITY_BORDER_9;
					}
					break;
				case SENSITIVITY_BORDER_9://point 6 and point 10
					if (currentX >= arraySensitivityPoints[5][0] && currentX <= arraySensitivityPoints[9][0] && currentY >= arraySensitivityPoints[5][1] && currentY <= arraySensitivityPoints[4][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[5][0] && currentX <= arraySensitivityPoints[9][0] && currentY >= arraySensitivityPoints[5][1] && currentY <= arraySensitivityPoints[9][1]) {
						mBorderType = SENSITIVITY_BORDER_10;
					}
					break;
				case SENSITIVITY_BORDER_10://point 7 and point 10
					if (currentX >= arraySensitivityPoints[5][0] && currentX <= arraySensitivityPoints[6][0] && currentY >= arraySensitivityPoints[6][1] && currentY <= arraySensitivityPoints[9][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[9][0] && currentX <= arraySensitivityPoints[6][0] && currentY >= arraySensitivityPoints[6][1] && currentY <= arraySensitivityPoints[9][1]) {
						mBorderType = SENSITIVITY_BORDER_11;
					}
					break;
				case SENSITIVITY_BORDER_11://point 8 or point 9
					if (currentX >= arraySensitivityPoints[8][0] && currentX <= arraySensitivityPoints[7][0] && currentY >= arraySensitivityPoints[6][1] && currentY <= arraySensitivityPoints[7][1]) {
						isAllowCrossBorder = true;
					} else {
						isAllowCrossBorder = false;
					}
					if (currentX >= arraySensitivityPoints[8][0] && currentX <= arraySensitivityPoints[7][0] && currentY >= arraySensitivityPoints[7][1] - mWidthSensitivity && currentY <= arraySensitivityPoints[7][1]) {
						isAllowCrossBorder = false;
						bSensitivity = true;
						toNext();
					}
					break;
				default:
					break;
				}
				
				if (isAllowCrossBorder) {
					drawTouchMove(currentX, currentY);
				} else {
					mBorderType = SENSITIVITY_BORDER_1;
					isDrawing = false;
					clearTouchPath();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			drawTouchUp();
			break;

		default:
			break;
		}
		
	}

	private void handleEventZoom(MotionEvent event) {
		if (event.getPointerCount() == 2) {
			float currentX1 = event.getX(0);
			float currentY1 = event.getY(0);
			float currentX2 = event.getX(1);
			float currentY2 = event.getY(1);
			
			double distance = Math.sqrt(Math.abs(currentX1 - currentX2) * Math.abs(currentX1 - currentX2) + Math.abs(currentY1 - currentY2) * Math.abs(currentY1 - currentY2));
			
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_POINTER_DOWN:
				mPreviousDistance = distance;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mPreviousDistance != 0) {
					if (arrayZoomCircles[1][2] >= 0) {
						arrayZoomCircles[1][2] += (distance - mPreviousDistance) * 5;
					}else{
						arrayZoomCircles[1][2] = 0;
					}
					mPreviousDistance = distance;
					
					if (arrayZoomCircles[1][2] >= arrayZoomCircles[0][2]) {
						arrayZoomCircles[0][3] = PAINT_COLOR_GREEN;
					}
					if (arrayZoomCircles[1][2] <= arrayZoomCircles[2][2]) {
						arrayZoomCircles[2][3] = PAINT_COLOR_GREEN;
					}
					drawView();
					
					// to next.
					if (arrayZoomCircles[0][3] == PAINT_COLOR_GREEN && arrayZoomCircles[2][3] == PAINT_COLOR_GREEN) {
						bZoom = true;
						toNext();
					}
				} else {
					mPreviousDistance = distance;
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mPreviousDistance = 0;
				break;

			default:
				break;
			}
		}
	}

	private void handleEventFree(MotionEvent event) {
		float currentX = event.getX();
		float currentY = event.getY();
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			drawTouchDown(currentX, currentY);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDrawing) {
				mCoordinate = "x : " + currentX + "  y : " + currentY;
				drawTouchMove(currentX, currentY);
			}
			break;
		case MotionEvent.ACTION_UP:
			drawTouchUp();
			break;
		default:
			break;
		}
	}

	private void handleEventLinearityHorizontal(MotionEvent event) {
		float currentX = event.getX();
		float currentY = event.getY();

		int measureWidth = 0;
		if (mCurrentLinearityHorizontalPosition == 0 || mCurrentLinearityHorizontalPosition == mLinearityHorizontalCount - 1) {
			measureWidth = mPaintWidth2_border;
		} else {
			measureWidth = mPaintWidth2;
		}
		int currentPositionY = arrayLinearityHorizontalY[mCurrentLinearityHorizontalPosition][0];
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clearTouchPath();
			if (currentY <= currentPositionY + measureWidth / 2 && currentY >= currentPositionY - measureWidth / 2) {
				if (currentX <= measureWidth / 2) {
					mLinearityBorderType = BORDER_LEFT;
					drawTouchDown(currentX, currentY);
				} else if (currentX >= getWidth() - measureWidth / 2) {
					mLinearityBorderType = BORDER_RIGHT;
					drawTouchDown(currentX, currentY);
				}
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDrawing) {
				if (currentY <= currentPositionY + measureWidth / 2 && currentY >= currentPositionY - measureWidth / 2) {
					switch (mLinearityBorderType) {
					case BORDER_LEFT:
						if (currentX >= getWidth() - measureWidth / 2) {
							arrayLinearityHorizontalY[mCurrentLinearityHorizontalPosition][1] = PAINT_COLOR_WHITE;
							if (mCurrentLinearityHorizontalPosition == mLinearityHorizontalCount - 1) {
								bLinearityHorizontal = true;
								toNext();
								return;
							}
							isDrawing = false;
							clearTouchPath();
						} else {
							drawTouchMove(currentX, currentY);
						}
						break;
					case BORDER_RIGHT:
						if (currentX <= measureWidth / 2) {
							arrayLinearityHorizontalY[mCurrentLinearityHorizontalPosition][1] = PAINT_COLOR_WHITE;
							if (mCurrentLinearityHorizontalPosition == mLinearityHorizontalCount - 1) {
								bLinearityHorizontal = true;
								toNext();
								return;
							}
							isDrawing = false;
							clearTouchPath();
						} else {
							drawTouchMove(currentX, currentY);
						}
						break;

					default:
						break;
					}

				} else {
					isDrawing = false;
					arrayLinearityHorizontalY[mCurrentLinearityHorizontalPosition][1] = PAINT_COLOR_RED;
					clearTouchPath();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isDrawing) {
				arrayLinearityHorizontalY[mCurrentLinearityHorizontalPosition][1] = PAINT_COLOR_RED;
				clearTouchPath();
			}
			drawTouchUp();
			break;
		default:
			break;
		}
	}

	private void handleEventLinearityVertical(MotionEvent event) {//Vertical
		float currentX = event.getX();
		float currentY = event.getY();
		
		int currentPositionX = arrayLinearityVerticalX[mCurrentLinearityVerticalPosition][0];
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			clearTouchPath();
			if (currentX <= currentPositionX + mPaintWidth2 / 2 && currentX >= currentPositionX - mPaintWidth2 / 2) {
				if (currentY <= mPaintWidth2 / 2) {
					mLinearityBorderType = BORDER_TOP;
					drawTouchDown(currentX, currentY);
				} else if (currentY >= getHeight() - mPaintWidth2 / 2) {
					mLinearityBorderType = BORDER_BOTTOM;
					drawTouchDown(currentX, currentY);
				}
			}
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDrawing) {
				if (currentX <= currentPositionX + mPaintWidth2 / 2 && currentX >= currentPositionX - mPaintWidth2 / 2) {
					switch (mLinearityBorderType) {
					case BORDER_TOP:
						if (currentY >= getHeight() - mPaintWidth2 / 2) {
							arrayLinearityVerticalX[mCurrentLinearityVerticalPosition][1] = PAINT_COLOR_WHITE;
							if (mCurrentLinearityVerticalPosition == mLinearityVerticalCount - 1) {
								bLinearityVertical = true;
								toNext();
								return;
							}
							isDrawing = false;
							clearTouchPath();
						} else {
							drawTouchMove(currentX, currentY);
						}
						break;
					case BORDER_BOTTOM:
						if (currentY <= mPaintWidth2 / 2) {
							arrayLinearityVerticalX[mCurrentLinearityVerticalPosition][1] = PAINT_COLOR_WHITE;
							if (mCurrentLinearityVerticalPosition == mLinearityVerticalCount - 1) {
								bLinearityVertical = true;
								toNext();
								return;
							}
							isDrawing = false;
							clearTouchPath();
						} else {
							drawTouchMove(currentX, currentY);
						}
						break;

					default:
						break;
					}

				} else {
					isDrawing = false;
					arrayLinearityVerticalX[mCurrentLinearityVerticalPosition][1] = PAINT_COLOR_RED;
					clearTouchPath();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isDrawing) {
				arrayLinearityVerticalX[mCurrentLinearityVerticalPosition][1] = PAINT_COLOR_RED;
				clearTouchPath();
			}
			drawTouchUp();
			break;
		default:
			break;
		}
	}

	private void drawTouchDown(float currentX, float currentY) {
		isDrawing = true;
		
		mPreviousX = currentX;
		mPreviousY = currentY;
		mTouchPath.moveTo(currentX, currentY);
		
		mInvalidRect.set((int) currentX, (int) currentY, (int) currentX , (int) currentY);
		mCurveEndX = currentX;
		mCurveEndY = currentY;
	}

	private void drawTouchMove(float currentX, float currentY) {
        final float dx = Math.abs(currentX - mPreviousX);
        final float dy = Math.abs(currentY - mPreviousY);
        Rect areaToRefresh = null;
        if (dx >= 3 || dy >= 3) {
			areaToRefresh = mInvalidRect;
			areaToRefresh.set((int) mCurveEndX, (int) mCurveEndY, (int) mCurveEndX, (int) mCurveEndY);
			//璁剧疆璐濆灏旀洸绾跨殑鎿嶄綔鐐逛负璧风偣鍜岀粓鐐圭殑涓�崐
			mCurveEndX = (currentX + mPreviousX) / 2;
			mCurveEndY = (currentY + mPreviousY) / 2;
			mTouchPath.quadTo(mPreviousX, mPreviousY, mCurveEndX, mCurveEndY);
			
			areaToRefresh.union((int) mPreviousX, (int) mPreviousY, (int) mPreviousX, (int) mPreviousY);
			areaToRefresh.union((int) mCurveEndX, (int) mCurveEndY, (int) mCurveEndX, (int) mCurveEndY);
			
			mPreviousX = currentX;
			mPreviousY = currentY;
			drawView();
        }
		if (null != areaToRefresh) {
			invalidate(areaToRefresh);
		}
	}

	private void drawTouchUp() {
		if (isDrawing) {
			isDrawing = false;
			invalidate();
		}
	}

	private void drawView() {
		if (null == mHolder) {
			mHolder = getHolder();
		}
		Canvas canvas = mHolder.lockCanvas();
		canvas.drawColor(Color.BLACK);
		//clearCanvas(canvas);
		switch (mModeType) {
		case TP_TEST_LINEARITY1:
			drawLinearity(canvas, TP_TEST_LINEARITY1);
			break;
		case TP_TEST_LINEARITY2:
			drawLinearity(canvas, TP_TEST_LINEARITY2);
			break;
		case TP_TEST_ACCURACY:
			drawAccuracy(canvas);
			break;
		case TP_TEST_SENSITIVITY:
			drawSensitivity(canvas);
			break;
		case TP_TEST_DEADZONE:
			
			break;
		case TP_TEST_ZOOM:
			drawZoom(canvas);
			break;
		case TP_TEST_FREE:
			drawFree(canvas);
			break;
		case TP_TEST_LINEARITY_HORIZONTAL:
			drawLinearityHorizontal(canvas);
			break;
		case TP_TEST_LINEARITY_VERTICAL:
			drawLinearityVertical(canvas);
			break;

		default:
			break;
		}
		canvas.drawPath(mTouchPath, mWhitePaint2);
		mHolder.unlockCanvasAndPost(canvas);
	}

	private void clearTouchPath() {
		resetTouchPath();
		drawView();
	}

	private void drawLinearity(Canvas canvas, int type) {
		int width = getWidth();
		int height = getHeight();
		if (type == TP_TEST_LINEARITY1) {
			canvas.drawLine((float) LINEARITY_WIDTH, 0.0f, (float) width, (float) (height - LINEARITY_WIDTH), mRedPaint);
			canvas.drawLine(0.0f, (float) LINEARITY_WIDTH, (float) (width - LINEARITY_WIDTH), (float) height, mRedPaint);
			return;
		} else if (type == TP_TEST_LINEARITY2) {
	        canvas.drawLine((float) (width - LINEARITY_WIDTH), 0.0f, 0.0f, (float) (height - LINEARITY_WIDTH), mRedPaint);
	        canvas.drawLine((float) width, (float) LINEARITY_WIDTH, (float) LINEARITY_WIDTH, (float) height, mRedPaint);
		}
	}

	private void drawAccuracy(Canvas canvas) {
		if (!isReady) {
			setupAccuracy();
			isReady = true;
		}
		int count = arrayAccuracyCircles.length;
		for (int i = 0; i < count; i++) {
			canvas.drawCircle(arrayAccuracyCircles[i][0], arrayAccuracyCircles[i][1], arrayAccuracyCircles[i][2], arrayAccuracyCircles[i][3] == PAINT_COLOR_RED ? mRedPaint : mGreenPaint);
		}
	}

	private void drawSensitivity(Canvas canvas) {
		setupSensitivity();
		int count = arraySensitivityPoints.length;
		for (int i = 0; i < count - 1; i++) {
			canvas.drawLine(arraySensitivityPoints[i][0], arraySensitivityPoints[i][1], arraySensitivityPoints[i + 1][0], arraySensitivityPoints[i + 1][1], mRedPaint);
		}
	}

	private void drawZoom(Canvas canvas) {
		if (!isReady) {
			setupZoom();
			isReady = true;
		}
		int count = arrayZoomCircles.length;
		for (int i = 0; i < count; i++) {
			Paint p = mRedPaint;
			switch (arrayZoomCircles[i][3]) {
			case PAINT_COLOR_RED:
				p = mRedPaint;
				break;
			case PAINT_COLOR_GREEN:
				p = mGreenPaint;
				break;
			case PAINT_COLOR_WHITE:
				p = mWhitePaint;
				break;

			default:
				break;
			}
			canvas.drawCircle(arrayZoomCircles[i][0], arrayZoomCircles[i][1], arrayZoomCircles[i][2], p);
		}
	}

	private void drawFree(Canvas canvas) {
		canvas.drawText(mCoordinate, 30, 30, mWhitePaint);
		canvas.drawLine(0, 40, getWidth(), 40, mWhitePaint);
		resetWhitePaint();
	}

	private void drawLinearityHorizontal(Canvas canvas) {
		if (!isReady) {
			setupLinearityHorizontal();
			isReady = true;
		}
		int count = mLinearityHorizontalCount = arrayLinearityHorizontalY.length;
		for (int i = 0; i < count; i++) {
			int color = arrayLinearityHorizontalY[i][1];
			if (color != PAINT_COLOR_WHITE) {
				int y = arrayLinearityHorizontalY[i][0];
				switch (color) {
				case PAINT_COLOR_GREEN:
					if (i == 0 || i == count - 1) {
						canvas.drawLine(0, y, getWidth(), y, mGreenPaint3);
					} else {
						canvas.drawLine(0, y, getWidth(), y, mGreenPaint2);
					}
					break;
				case PAINT_COLOR_RED:
					if (i == 0 || i == count - 1) {
						canvas.drawLine(0, y, getWidth(), y, mRedPaint3);
					} else {
						canvas.drawLine(0, y, getWidth(), y, mRedPaint2);
					}
					break;

				default:
					break;
				}
				canvas.drawLine(0, y, getWidth(), y, mBluePaint2);
				mCurrentLinearityHorizontalPosition = i;
				break;
			}
		}
	}

	private void drawLinearityVertical(Canvas canvas) {
		if (!isReady) {
			setupLinearityVertical();
			isReady = true;
		}
		int count = mLinearityVerticalCount = arrayLinearityVerticalX.length;
		for (int i = 0; i < count; i++) {
			int color = arrayLinearityVerticalX[i][1];
			if (color != PAINT_COLOR_WHITE) {
				int x = arrayLinearityVerticalX[i][0];
				switch (color) {
				case PAINT_COLOR_GREEN:
					canvas.drawLine(x, 0, x, getHeight(), mGreenPaint2);
					break;
				case PAINT_COLOR_RED:
					canvas.drawLine(x, 0, x, getHeight(), mRedPaint2);
					break;

				default:
					break;
				}
				canvas.drawLine(x, 0, x, getHeight(), mBluePaint2);
				mCurrentLinearityVerticalPosition = i;
				break;
			}
		}
	}

}
