package com.android.calendar.hormonth;

import java.io.InputStream;
import java.util.ArrayList;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.Lunar.LunarCalendarUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.animation.AnimationUtil;
import com.android.calendar.animation.CalendarRelativeLayout;
import com.android.calendar.animation.MainLinearLayout;

public class CalendarView extends View {

	private static final String TAG = "CalendarView";

	private int _id = 0;

	public static final int MONTH_STYLE = 0;
	public static final int WEEK_STYLE = 1;

	private static final int TOTAL_COL = 7;
	private static final int TOTAL_ROW = 6;

	private Paint mTextPaint;
	private Paint mLinePaint;
	private Paint mUnderlinePaint;
	private Rect mTextRect;
	private int mViewWidth;
	private int mViewHight;
	private int mCellSpace;
	private Row rows[] = new Row[TOTAL_ROW];
	private static CustomDate mShowDate;
	private CustomDate mShowCurrentDate;// 指的是该view对应的CurrentDate
	public static int style = MONTH_STYLE;
	private static final int WEEK = 7;
	private CallBack mCallBack;
	private int touchSlop;
	private boolean callBackCellSpace;

	private Time mCurrentTime = new Time();

	private Time mBaseTime = new Time();

	private Context mContext;

	private LunarCalendarUtil mLunarCalendarUtil;

	private static boolean IsShowOnfauce = false;

	private static Bitmap mBitmap;

	private static Drawable mDrawable;
	
	
	private static Bitmap mOtharMonthBitmap;
	private static Drawable mOtharMonthDrawable;
	
	// CHECKSTYLE:OFF
	private static float mScale = 0; // Used for supporting different screen

	private static float NORMAL_FONT_SIZE = 17f;
	private static float NORMAL_MONTHLUNARCOUNT_SIZE = 8f;

	/* PRIZE-界面调整 wanzhijuan 2015-7-6 start */
	// 调整颜色
	// 80323232
	private static int mOutSideColor;
	// #323232
	private static int mSelectOfMonthColor;
	// #ffffff
	private static int mTodayColor;
	// #e0e0e0
	private static int mLineColor;
	
	/* PRIZE-界面调整 wanzhijuan 2015-7-6 end */
	private static int mUnderlineColor;
	private static int mUnderLineWidth;
	private static int mUnderLineLength;
	
	private int mFirstDayOfWeek; // First day of the week
	private int mCnY;
	private int mEnY;

	private EventLoader mEventLoader;
	
	public interface CallBack {

		void clickDate(CustomDate date); // 点击改变改变焦点的日期

		void onMesureCellHeight(int cellSpace);

		void changeDate(CustomDate date); // 移动改变焦点日期（使用在左右滑动）

		void ChangeOtherMont(CustomDate date, boolean froward);// 移动到下一个月

		void ClearOnFouces(); // 清楚焦点


		void sendFocusToParent();
	}

	public CalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Time time = new Time();
		time.setToNow();
		time.normalize(true);
		init(context, time);
		this.mContext = context;
	}

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Time time = new Time();
		time.setToNow();
		time.normalize(true);
		init(context, time);
		this.mContext = context;
	}

	public CalendarView(Context context) {
		super(context);
		Time time = new Time();
		time.setToNow();
		time.normalize(true);
		init(context, time);
		this.mContext = context;
	}

	public CalendarView(Context context, int style, CallBack mCallBack,
			Time mTime, int _id , EventLoader mEventLoader) {
		super(context);
		CalendarView.style = style;
		this.mCallBack = mCallBack;
		init(context, mTime);
		this._id = _id;
		this.mContext = context;
		
		this.mEventLoader = mEventLoader;
	}

	
	
	
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub

		super.onAttachedToWindow();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.d("hekeyi","[CalendarView]-onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	Canvas canvas2;
	@Override
	protected void onDraw(Canvas canvas) {
//		Log.d("hekeyi","[CalendarView]-onDraw");
		super.onDraw(canvas);
		this.canvas2 = canvas;
		int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
		for (int i = 0; i < count; i++) {
			int y = mViewHight / count * (i + 1);
			mLinePaint.setColor(mLineColor);
			/*PRIZE-界面调整 wanzhijuan 2015-7-3 start */
			/*canvas.drawLine(0mViewWidth / (4 * Calendar.DAY_OF_WEEK), y - 1, mViewWidth - mViewWidth
					/ (4 * Calendar.DAY_OF_WEEK)mViewWidth, y, mLinePaint);*/// 画线
			/*PRIZE-界面调整 wanzhijuan 2015-7-3 end */
			if (rows[i] != null)
				rows[i].drawCells(canvas);
		}

		/*PRIZE-获取本月的事件-lixing- 2015-8-21- start */
		markEvent(canvas);
		/*PRIZE-获取本月的事件-lixing- 2015-8-21- end */
		slideDerection = 0;
	}

	private void init(Context context, Time mShowTime) {

		mCnY = (int) context.getResources().getDimension(R.dimen.ch_y);
		mEnY = (int) context.getResources().getDimension(R.dimen.en_y);
		mSelectOfMonthColor = context.getResources().getColor(
				R.color.mouth_up_unselect);
		mLineColor = context.getResources().getColor(
				R.color.mouth_divide);
		mUnderlineColor = context.getResources().getColor(
				R.color.underline_color);
		mUnderLineWidth = context.getResources().getDimensionPixelSize(R.dimen.prize_underline);
		
		mTodayColor = context.getResources().getColor(
				R.color.mouth_up_select);
		
		mOutSideColor = getResources().getColor(R.color.mouth_down_unselect);

		InputStream is = context.getResources().openRawResource(
				R.drawable.month_onfoucs);
		mBitmap = BitmapFactory.decodeStream(is);

		mDrawable = context.getResources().getDrawable(R.drawable.month_onfoucs);
		
		InputStream input = context.getResources().openRawResource(	R.drawable.month_other_onfoucs);
		mOtharMonthBitmap = BitmapFactory.decodeStream(input);
		
		mOtharMonthDrawable = context.getResources().getDrawable(R.drawable.month_other_onfoucs);
		

		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mLunarCalendarUtil = LunarCalendarUtil.getInstance(context);
		initDate(mShowTime);
		mCurrentTime.setToNow();
		mCurrentTime.normalize(true);

		if (mScale == 0) {
			mScale = context.getResources().getDisplayMetrics().density;

			NORMAL_FONT_SIZE *= mScale;
			NORMAL_MONTHLUNARCOUNT_SIZE *= mScale;
		}

		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setStyle(Style.FILL);
		mLinePaint.setTextAlign(Align.CENTER);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setColor(mLineColor);
		
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextSize(NORMAL_FONT_SIZE);
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setColor(mOutSideColor);
		
		mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mUnderlinePaint.setStyle(Style.FILL);
		mUnderlinePaint.setTextAlign(Align.CENTER);
		mUnderlinePaint.setAntiAlias(true);
		mUnderlinePaint.setColor(mUnderlineColor);
		mTextRect = new Rect();

		mFirstDayOfWeek = Utils.getFirstDayOfWeek(context);
	}

	private void initDate(Time mShowTime) {
		if (style == MONTH_STYLE) {
			mShowDate = new CustomDate(mShowTime.year, mShowTime.month + 1,
					mShowTime.monthDay);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHight = h;
		int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
		mCellSpace = Math.min(mViewHight / count, mViewWidth / TOTAL_COL);
//		mCellSpace = Math.min(mViewHight / TOTAL_ROW, mViewWidth / TOTAL_COL);
		mUnderLineLength = mCellSpace / 3;
		if (!callBackCellSpace) {
			mCallBack.onMesureCellHeight(mCellSpace);
			callBackCellSpace = true;
		}
		mTextPaint.setTextSize(mCellSpace / 3);
	}

	private Cell mClickCell;
	private float mDownX;
	private float mDownY;
	private CalendarRelativeLayout calendarLayout;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		Log.d("hekeyi","[CalendarView]-onTouchEvent getLayout = "+getRootView());

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
//			Log.d("hekeyi","[CalendarView]-onTouchEvent MOVE_DOWN");
//			IsShowOnfauce = false;   //modified by hekeyi for when ACTION_DOWN,the grey focused disappear,then appear again.  20170908
			mCallBack.ClearOnFouces();
//			invalidate();              //modified by hekeyi for when ACTION_DOWN,the grey focused disappear,then appear again.  20170817
			mDownX = event.getX();
			mDownY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
//			Log.d("hekeyi","[CalendarView]-onTouchEvent ACTION_UP getParent = "+getParent());
			float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			/*if(Math.abs(disX)>10){
				getParent().requestDisallowInterceptTouchEvent(false);
			}*/
			if(Math.abs(disY)>30){
				mCallBack.sendFocusToParent();
			}else {
				if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
					int col = (int) (mDownX * TOTAL_COL / mViewWidth);
					int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
					int row = (int) (mDownY * count / mViewHight);
					measureClickCell(col, row);
//					mCallBack.clickDate(null);
				}
			}

			break;
		}
		return true;
	}


	private void measureClickCell(int col, int row) {
		if (col >= TOTAL_COL || row >= TOTAL_ROW)
			return;
		if (rows[row] != null) {
			mClickCell = new Cell(rows[row].cells[col].date,
					rows[row].cells[col].state, rows[row].cells[col].i,
					rows[row].cells[col].j);
			CustomDate date = rows[row].cells[col].date;
			date.week = col;

			mShowDate = new CustomDate(mShowDate.year, mShowDate.month,
					date.day);
			if (row < 1 && date.day > 7) {
				mCallBack.ChangeOtherMont(date, false);
				return ;
			} else if (row > 3 && date.day <= 14) {
				mCallBack.ChangeOtherMont(date, true);
				return;
			}

			if (mShowDate.month == date.month && mShowDate.year == date.year) {
				mCallBack.clickDate(date);
			}
		}
	}

	class Row {
		public int j;

		Row(int j) {
			this.j = j;
		}

		public Cell[] cells = new Cell[TOTAL_COL];

		public void drawCells(Canvas canvas) {
			mCurrentTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
			long currentTime = System.currentTimeMillis();
			mCurrentTime.set(currentTime);
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null)
					cells[i].drawSelf(canvas);
//				markEvent(canvas,i,j);
			}
		}
	}

	class Cell {
		public CustomDate date;
		public State state;
		public int i;
		public int j;

		public Cell(CustomDate date, State state, int i, int j) {
			super();
			this.date = date;
			this.state = state;
			this.i = i;
			this.j = j;
		}

		public void drawSelf(Canvas canvas) {
//			Log.d("hekeyi","[CalendarView]-markToday state = "+state+" mShowDate.day = "+mShowDate.day);
			switch (state) {
			case CLICK_DAY:
				if (date.day == mShowDate.day && date.month == mShowDate.month && IsShowOnfauce) {
					int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
					int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2);
					int y1 = mViewHight * j / (count) + mViewHight / (2 * count) - mDrawable.getIntrinsicHeight()/2;
					
					int x2 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) + mDrawable.getIntrinsicWidth()/2);
					int y2 = mViewHight * j / (count) + mViewHight / (2 * count) + mDrawable.getIntrinsicHeight()/2;
					
					canvas.drawBitmap(mOtharMonthBitmap, x1, y1, mTextPaint); //左上顶点处

//					Log.d("draw","[CalendarView]- x1 = "+x1+" y1 = "+y1+"  x2 = "+x2+" y2 = "+y2);
//					mDrawable.setBounds(x1, y1, x2, y2);
//					mDrawable.draw(canvas);
											
//					mTextPaint.setColor(mTodayColor);
				}
				mTextPaint.setColor(mSelectOfMonthColor);
//				markToday(canvas, j, i);
				break;
			case CURRENT_MONTH_DAY:
				if (date.day == mShowDate.day && IsShowOnfauce && mShowDate.month == date.month) {
					mTextPaint.setColor(mSelectOfMonthColor);
//					int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mBitmap.getWidth() / 2);
//					int y1 = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) - mBitmap.getHeight() / 2;
					
					/*int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2);
					int y1 = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) - mDrawable.getIntrinsicHeight()/2;
					
					int x2 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) + mDrawable.getIntrinsicWidth()/2);
					int y2 = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) + mDrawable.getIntrinsicHeight()/2;*/

					int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
					int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2);
					int y1 = mViewHight * j / (count) + mViewHight / (2 * count) - mDrawable.getIntrinsicHeight()/2;

					int x2 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) + mDrawable.getIntrinsicWidth()/2);
					int y2 = mViewHight * j / (count) + mViewHight / (2 * count) + mDrawable.getIntrinsicHeight()/2;
					
					canvas.drawBitmap(mOtharMonthBitmap, x1, y1, mTextPaint);
//					mOtharMonthDrawable.setBounds(x1+2, y1+2, x2+1, y2+1);
//					mOtharMonthDrawable.draw(canvas);
				}
				if (mCurrentTime.monthDay == date.day
						&& date.day == mShowDate.day) {
					mTextPaint.setColor(mSelectOfMonthColor);
				} else {
					mTextPaint.setColor(mSelectOfMonthColor);
				}
				break;
			case NEXT_MONTH_DAY:
			case PAST_MONTH_DAY:
//				Log.d("hekeyi","[CalendarView]-drawself set mOutSideColor date = "+date+" mCurrentTime = "+mCurrentTime);
				mTextPaint.setColor(mOutSideColor);
				break;
			}

			if(mCurrentTime.monthDay==date.day && mCurrentTime.month==date.month-1 && mCurrentTime.year==date.year
					&& state!=State.NEXT_MONTH_DAY && state!=State.PAST_MONTH_DAY){
				mTextPaint.setColor(mTodayColor);
				markToday(canvas, j, i);
			}

			/*prize-Always show today's date icon on the calendar-lixing-2015-8-21 -start*/
//			markToday(canvas, j, i);
			/*prize-Always show today's date icon on the calendar-lixing-2015-8-21 -end*/

			int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
			String content = date.day + "";
			int x = mViewWidth * i / TOTAL_COL + mViewWidth / (TOTAL_COL * 2);
			if(mContext.getResources().getConfiguration().locale.getCountry().equals("CN")
					||mContext.getResources().getConfiguration().locale.getCountry().equals("TW")) {
//				int y = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW);
				int y = mViewHight * j / (count) + mViewHight / (2 * count);
				mTextPaint.setTextSize(NORMAL_FONT_SIZE);
				canvas.drawText(content, x, y, mTextPaint);
				/*PRIZE-The main interface of Chinese lunar calendar, font color wanzhijuan 2015-7-14 start */
                /*if (state == State.CLICK_DAY || state == State.CURRENT_MONTH_DAY) {
                	mTextPaint.setAlpha((int) (255 * 0.5));
                }*/
				/*PRIZE-The main interface of Chinese lunar calendar, font color wanzhijuan 2015-7-14 end */
				mTextPaint.setTextSize(NORMAL_MONTHLUNARCOUNT_SIZE);
				canvas.drawText(date.LinearString, x, y + mCnY, mTextPaint);
				mTextPaint.getTextBounds(date.LinearString, 0, date.LinearString.length(), mTextRect);
				switch (date.geLunarMDType()) {
				case 2://new year
					mUnderlinePaint.setStrokeWidth(mUnderLineWidth/**2*/);
//					canvas.drawLine(x - mUnderLineLength/2, y + mCnY*1.8f, x + mUnderLineLength/2, y + mCnY*1.8f, mUnderlinePaint);
					canvas.drawLine(x - mUnderLineLength/2, y + mCnY*1.4f, x + mUnderLineLength/2, y + mCnY*1.4f, mUnderlinePaint);
					break;
				case 1://day one of the month
					mUnderlinePaint.setStrokeWidth(mUnderLineWidth);
//					canvas.drawLine(x - mUnderLineLength/2, y + mCnY*1.8f, x + mUnderLineLength/2, y + mCnY*1.8f, mUnderlinePaint);
					canvas.drawLine(x - mUnderLineLength/2, y + mCnY*1.4f, x + mUnderLineLength/2, y + mCnY*1.4f, mUnderlinePaint);
					break;
				default:
					break;
				}
				/*PRIZE-The main interface of Chinese lunar calendar, font color wanzhijuan 2015-7-14 start */
//				mTextPaint.setAlpha(255);
				/*PRIZE-The main interface of Chinese lunar calendar, font color wanzhijuan 2015-7-14 end */
			} else {
//				int y = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) + mEnY;
				int y = mViewHight * j / (count) + mViewHight / (2 * count) + mEnY;
				mTextPaint.setTextSize(NORMAL_FONT_SIZE);
				canvas.drawText(content, x, y, mTextPaint);
			}
		}
	}

	enum State {
		CURRENT_MONTH_DAY, PAST_MONTH_DAY, NEXT_MONTH_DAY, CLICK_DAY;
	}

	public void setIsShowFouce(boolean IsShowOnfauce) {
		this.IsShowOnfauce = IsShowOnfauce;
		this.invalidate();
	}

	private void fillDate() {

		fillMonthDate();

		// mCallBack.changeDate(mShowDate);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
	}

	private void fillMonthDate() {
		/*int countOffset= caculateOffset();

		if(slideDerection!=0){
			monthChangeAnimation(countOffset);
		}*/
		String timeZone = Utils.getTimeZone(mContext, mTZUpdater);
		
		int monthDay = mDateUtil.getCurrentMonthDay(timeZone);
		int lastMonthDays = mDateUtil.getMonthDays(mShowCurrentDate.year,
				mShowCurrentDate.month - 1);
		int currentMonthDays = mDateUtil.getMonthDays(mShowCurrentDate.year,
				mShowCurrentDate.month);
		int firstDayWeek = mDateUtil.getWeekDayFromDate(mShowCurrentDate.year,
				mShowCurrentDate.month);
		
		firstDayWeek = (firstDayWeek + TOTAL_COL - mFirstDayOfWeek) % TOTAL_COL;
		
		boolean isCurrentMonth = false;
		if (mDateUtil.isCurrentMonth(mShowCurrentDate)) {
			isCurrentMonth = true;
		}
		int day = 0;
		int count = mDateUtil.setRows(mShowCurrentDate.year,mShowCurrentDate.month,mContext);
//		for (int j = 0; j < TOTAL_ROW; j++) {
		for (int j = 0; j < count; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int postion = i + j * TOTAL_COL ;
//				Log.d("hekeyi","[CalendarView]-fillDateMode  position = "+postion+" firstDayWeek = "+firstDayWeek);
				if (postion >= firstDayWeek
						&& postion < firstDayWeek + currentMonthDays) {
					day++;
					if (isCurrentMonth && day == monthDay) {
						CustomDate date = CustomDate.modifiDayForObject(
								mShowCurrentDate, day);
						date.setLinear(mLunarCalendarUtil
								.getFestivalChineseString(date.getYear(),
										date.getMonth(), date.getDay()));
						mClickCell = new Cell(date, State.CLICK_DAY, i, j);
						date.week = i;

						// mCallBack.clickDate(date);
						rows[j].cells[i] = new Cell(date, State.CLICK_DAY, i, j);
						continue;
					}else{
						rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(
								mShowCurrentDate, day), State.CURRENT_MONTH_DAY, i,
								j);
						rows[j].cells[i].date.setLinear(mLunarCalendarUtil
								.getFestivalChineseString(
										rows[j].cells[i].date.getYear(),
										rows[j].cells[i].date.getMonth(),
										rows[j].cells[i].date.getDay()));

					}
				} else if (postion < firstDayWeek) {
					rows[j].cells[i] = new Cell(new CustomDate(
							mShowCurrentDate.year, mShowCurrentDate.month - 1,
							lastMonthDays - (firstDayWeek - postion - 1)),
							State.PAST_MONTH_DAY, i, j);
					rows[j].cells[i].date.setLinear(mLunarCalendarUtil
							.getFestivalChineseString(
									rows[j].cells[i].date.getYear(),
									rows[j].cells[i].date.getMonth(),
									rows[j].cells[i].date.getDay()));
				} else if (postion >= firstDayWeek + currentMonthDays) {
					rows[j].cells[i] = new Cell((new CustomDate(
							mShowCurrentDate.year, mShowCurrentDate.month + 1,
							postion - firstDayWeek - currentMonthDays + 1)),
							State.NEXT_MONTH_DAY, i, j);
					rows[j].cells[i].date.setLinear(mLunarCalendarUtil
							.getFestivalChineseString(
									rows[j].cells[i].date.getYear(),
									rows[j].cells[i].date.getMonth(),
									rows[j].cells[i].date.getDay()));
//					Log.d("hekeyi","[CalendarView]-date = "+rows[j].cells[i].date+" state "+State.NEXT_MONTH_DAY);
				}
				rows[j].cells[i].date.setLunarMDType(mLunarCalendarUtil.getLunarMDStatus(rows[j].cells[i].date.getYear(),
						rows[j].cells[i].date.getMonth(),
						rows[j].cells[i].date.getDay()));
			}
		}
	}


	// public void update() {
	// fillDate();
	// invalidate();
	// }
	//
	// public void switchStyle(int style) {
	// CalendarView.style = style;
	// if (style == MONTH_STYLE) {
	// update();
	// }
	// }

	public CustomDate getShowDate() {
		return mShowDate;
	}

	public void setShowDate(CustomDate mShowDate) {
		this.mShowDate = mShowDate;
	}

	public void setViewCurrentDate(CustomDate mShowCurrentDate) {
//		Log.d("hekeyi","[MonthChangeAnimation] setViewCurrentDate = "+System.currentTimeMillis());
//		int countOffset= caculateOffset();

		/*if(slideDerection!=0){
			monthChangeAnimation();
		}*/
		this.mShowCurrentDate = mShowCurrentDate;
		this.fillDate();

	}

	public CustomDate getCurrentShowDate() {
		return this.mShowCurrentDate;
	}

	public void setShowDate(CustomDate mShowDate, boolean OnDraw) {
		this.mShowDate = mShowDate;
		if (OnDraw) {
			this.invalidate();
		}
	}

	MainLinearLayout main_layout;
	LinearLayout second_layout;
	public static int slideDerection ;  //left:1 , right:2, default 0
	public void rightSilde() {
//		Log.d("hekeyi","[MonthChangeAnimation] rightSilde = "+System.currentTimeMillis());
		/*if(mShowDate.month!=mCurrentTime.month){
			setPaneHeight();
		}*/
		if(mShowDate.year>=2037 && mShowDate.month>=12) return;
		slideDerection = 2;
		if (mShowDate.month == 12) {
			mShowDate.month = 1;
			mShowDate.year += 1;
		} else {
			mShowDate.month += 1;
		}
		if (mDateUtil.getMonthDays(mShowDate.year, mShowDate.month) < mShowDate.day) {
			mShowDate.day = mDateUtil.getMonthDays(mShowDate.year,mShowDate.month);
		}
		setPaneHeight();
	}

	public void leftSilde() {
		if(mShowDate.year<1970) return;
		slideDerection = 1;
		if (mShowDate.month == 1) {
			mShowDate.month = 12;
			mShowDate.year -= 1;
		} else {
			mShowDate.month -= 1;
		}


		if (mDateUtil.getMonthDays(mShowDate.year, mShowDate.month) < mShowDate.day) {
			mShowDate.day = mDateUtil.getMonthDays(mShowDate.year,
					mShowDate.month);
		}
		setPaneHeight();
	}
	
	
	
	void clearCachedEvents() {
		mLastReloadMillis = 0;

	}

	private final Runnable mCancelCallback = new Runnable() {
		public void run() {
			clearCachedEvents();
		}
	};
	
	
	private final Runnable mTZUpdater = new Runnable() {
		@Override
		public void run() {
			String tz = Utils.getTimeZone(mContext, this);
			if(mNewBaseDate!=null){
				mNewBaseDate.timezone = tz;
				mNewBaseDate.normalize(true);
				mCurrentTime.switchTimezone(tz);
			}

			invalidate();
		}
	};
	
	
	
	/* package */
	/**
	 * @see:根据当前时间获取事件信息，并存入全局变量List中，刷新View
	 * @author lixing
	 * 2015-8-17
	 */
	long mLastReloadMillis = 0;
	
	int mFirstJulianDay = 0;
	
	Time mNewBaseDate;
	
	
	
//	private static ArrayList<Event> mEvents = new ArrayList<Event>();
	private static ArrayList<Event> mEvents = Utils.mEvents;

	void reloadEvents() {
		mNewBaseDate = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		
		mNewBaseDate.setToNow();
//		CustomDate date = rows[0].cells[0].date;
//		Log.d("CalendarView","CalendarView.reloadEvents data is" + date.toString());
				
		mNewBaseDate.monthDay = 1;
//		if(mShowCurrentDate != null)
		mNewBaseDate.month = getShowDate().month - 1;
		mNewBaseDate.year = getShowDate().year;
		
		
		mFirstJulianDay = Utils.getJulianDayInGeneral(mNewBaseDate, false);


		// Make sure our time zones are up to date
		mTZUpdater.run();


		// Avoid reloading events unnecessarily.
//		if (millis == mLastReloadMillis) {
//			return;
//		}
//		mLastReloadMillis = millis;

		// load events in the background
		// mContext.startProgressSpinner();
//		2440588   2457020
		
		int currentMonthDays = mDateUtil.getMonthDays(getShowDate().year,
				getShowDate().month);
		
//		Log.d("hekeyi","[CalendarView]-reloadEvents getShowDate = "+getShowDate()+" getCurrentDate = "+getCurrentShowDate());
//		Toast.makeText(mContext, "currentMonthDays is:" + currentMonthDays, 200).show();
		final ArrayList<Event> events = new ArrayList<Event>();
		mEventLoader.loadEventsInBackground(currentMonthDays, events, mFirstJulianDay,
				new Runnable() {
					public void run() {
						if (mEvents == null) {
							mEvents = new ArrayList<Event>();
						} else {
							mEvents.clear();
						}
						// Create a shorter array for all day events
						for (Event e : events) {
							mEvents.add(e);
						}
						invalidate();
					}
				}, mCancelCallback);
	}
	
	/**
	 * @see :Based on the event information stored in View, the List is marked on the
	 * @author lixing
	 * @param canvas
	 * 2015-8-17
	 */
	Paint mPaint = new Paint();
	private void markEvent(Canvas canvas) {
		// TODO Auto-generated method stub
//		Log.d("hekeyi","[CalendarView]-markEvent");
		if(mEvents.size()!=0){
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(mContext.getResources().getColor(R.color.prize_background));
		for(Event e:mEvents){
		Time startTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		startTime.set(e.startMillis);
			int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
		for (int j = 0; j < count; j++) {
			for (int i = 0; i < TOTAL_COL; i++) {
				if (rows[j]==null) return;
				if(rows[j].cells[i].date.day == startTime.monthDay && rows[j].cells[i].date.month -1 == startTime.month){
				    /*PRIZE-modifi dot position of event remind-xiaoping-2016-12-1-start*/
                    int x1 = (int) (mViewWidth * (i+0.5) / TOTAL_COL /*(mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2)*/);
//                    int y1 = mViewHight * (j+1) / (TOTAL_ROW) -( mViewHight / (2 * TOTAL_ROW) - mDrawable.getIntrinsicHeight()/2)+7;
					int y1 = mViewHight * (j+1) / (count) -( mViewHight / (2 * count) - mDrawable.getIntrinsicHeight()/2)+7;
                    /*PRIZE-modifi dot position of event remind-xiaoping-2016-12-1-end*/
                    canvas.drawCircle(x1, y1, mDrawable.getIntrinsicWidth()/15, mPaint);
				}	
			}
		}
		}
		}
	}

	private void markEvent(Canvas canvas,int i,int j){
		mPaint.setColor(mContext.getResources().getColor(R.color.prize_background));
		int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
		if(mEvents.size()!=0){
			for(Event e:mEvents){
				Time startTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
				startTime.set(e.startMillis);
				if(rows[j].cells[i].date.day == startTime.monthDay && rows[j].cells[i].date.month -1 == startTime.month){
					int x1 = (int) (mViewWidth * (i+0.5) / TOTAL_COL);
					int y1 = mViewHight * (j+1) / (count) -( mViewHight / (2 * count) - mDrawable.getIntrinsicHeight()/2)+7;
					canvas.drawCircle(x1, y1,mBitmap.getHeight()/15, mPaint);
				}
			}
		}
	}
	
	
	/**
	 * @see :Always display the date icon on the View
	 * @author lixing
	 * 2015-8-21
	 */
	private void markToday(Canvas canvas,int j ,int i){
		
		Time today = new Time(Utils.getTimeZone(mContext, mTZUpdater));
		today.setToNow();
//		Log.d("hekeyi","[CalendarView]-markToday day = "+today.monthDay);
//		if(rows[j].cells[i].date.day == today.monthDay && rows[j].cells[i].date.month -1 == today.month 
//				&& rows[j].cells[i].date.year == today.year){
		int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
//		if(rows[j].cells[i].state == State.CLICK_DAY ){
					/*int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2);
					int y1 = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) - mDrawable.getIntrinsicHeight()/2;
					
					int x2 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) + mDrawable.getIntrinsicWidth()/2);
					int y2 = mViewHight * j / (TOTAL_ROW) + mViewHight / (2 * TOTAL_ROW) + mDrawable.getIntrinsicHeight()/2;*/
			int x1 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) - mDrawable.getIntrinsicWidth()/2);
			int y1 = mViewHight * j / (count) + mViewHight / (2 * count) - mDrawable.getIntrinsicHeight()/2;

			int x2 = mViewWidth * i / TOTAL_COL + (mViewWidth / (2 * TOTAL_COL) + mDrawable.getIntrinsicWidth()/2);
			int y2 = mViewHight * j / (count) + mViewHight / (2 * count) + mDrawable.getIntrinsicHeight()/2;
//					mDrawable.setBounds(x1, y1, x2, y2);
//					mDrawable.draw(canvas);

			canvas.drawBitmap(mBitmap, x1, y1, mTextPaint); //左上顶点处
					
					mTextPaint.setColor(mTodayColor);
//		}
	}



	private float mAnimationX;
	private float mAnimationY;
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}

	private OnTouchEventChangeListener mOnTouchEventChangeListener;
	public interface OnTouchEventChangeListener {
		void  onChange();
	}

	public void setOnTouchEventChangeListener(OnTouchEventChangeListener mOnTouchEventChangeListener) {
		this.mOnTouchEventChangeListener = mOnTouchEventChangeListener;
	}

	DateUtil mDateUtil = DateUtil.dateUtil;
	private int caculateOffset(){
		int countSlideAfter = -1;
		int countSlideBefore = -1;
		int countOffset;
		switch ( slideDerection ){
			case 0:
				break;
			case 1:
				countSlideBefore = mDateUtil.setRows(mShowDate.year,mShowDate.month+1,mContext);
				countSlideAfter = mDateUtil.setRows(mShowDate.year,mShowDate.month+1-1,mContext);
				break;
			case 2:
				countSlideBefore = mDateUtil.setRows(mShowDate.year,mShowDate.month-1,mContext);
				countSlideAfter = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
				break;
		}
		countOffset = countSlideAfter - countSlideBefore;
		return countOffset;
	}

	long start;
	public void monthChangeAnimation() {
		if(second_layout==null){
			second_layout = (LinearLayout)getRootView().findViewById(R.id.secondary_layout);
		}
		int offset= caculateOffset();
		if (offset==0){
			return;
		}
		slideDerection = 0;
		final int viewHeight =(int) getResources().getDimension(R.dimen.prize_week_height);
		TranslateAnimation mListviewAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.ABSOLUTE,-offset*viewHeight, Animation.ABSOLUTE,0);

		mListviewAction.setDuration(200);
		if(second_layout!=null){
			second_layout.startAnimation(mListviewAction);
		}
	}

	LinearLayout barImage;
	public void setPaneHeight(){
		if(AllInOneActivity.viewTypeFlag== CalendarController.ViewType.WEEK) return;
		if(main_layout==null){
			main_layout = (MainLinearLayout)getRootView().findViewById(R.id.main_pane);
		}
		int count = mDateUtil.setRows(mShowDate.year,mShowDate.month,mContext);
		if(main_layout!=null){
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)main_layout.getLayoutParams();
			layoutParams.height = count*(int)getResources().getDimension(R.dimen.prize_week_height);
//		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);// 宽不变, 确定值, match_parent
//		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(layoutParams.height, MeasureSpec.AT_MOST);
//		main_layout.measure(widthMeasureSpec,heightMeasureSpec);
			main_layout.setLayoutParams(layoutParams);
			monthChangeAnimation();

		}


		if(barImage==null){
			barImage = (LinearLayout) getRootView().findViewById(R.id.main_title_layout);
		}
//		AnimationUtil.setMainTitleBackground(mContext, mShowDate.month-1, barImage);
	}



}
	
	
	
	
	
	

