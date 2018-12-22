/*****************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *文件名称：DayHeaderView.java
 *内容摘要：
 *当前版本：v 1.0
 *作	者：刘栋
 *完成日期：

 *修改记录：
 *修改日期：2015-5-6 下午8:59:42
 *版 本 号：v  1.0
 *修 改 人：刘栋
 *修改内容：
 ********************************************/
package com.android.calendar.horday;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.text.StaticLayout;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.PopupWindow;
import android.widget.ViewSwitcher;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarData;
import com.android.calendar.CalendarUtils;
import com.android.calendar.Event;
import com.android.calendar.EventLoader;
import com.android.calendar.GeneralPreferences;
import com.android.calendar.OtherPreferences;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.animation.CalendarRelativeLayout;
import com.android.calendar.hormonth.MonthUtils;

import static com.android.calendar.Utils.isFirstLunarDay;

@SuppressWarnings("ResourceType")
public class DayHeaderView extends View implements View.OnCreateContextMenuListener,
        ScaleGestureDetector.OnScaleGestureListener, View.OnClickListener, View.OnLongClickListener {

    private Context mContext;

    private ViewSwitcher mViewSwitcher;

    private Time mBaseTime;

    private Time mBaseFristDayTime;

    private Time mCurrentTime;

    private int[] mDayWeek;

    private String[] mLunarWeekString;

    private int[] mDayPreWeek;

    private String[] mLunarPreWeekString;

    private int[] mDayNextWeek;

    private String[] mLunarNextWeekString;

    private int mViewWight;

    private int mViewHeight;

    private int mViewStartX;
    private int mViewStartY;
    private int mMaxViewStartY;

    private Paint mPaintDrawSize = new Paint();

    private Paint mPaintDrawLunar = new Paint();

    private Bitmap mBitmap_Onfouce;

    private static int mTextWeekDayColor;

    /**
     * The selection modes are HIDDEN, PRESSED, SELECTED, and LONGPRESS.
     */
    private static final int SELECTION_HIDDEN = 0;
    private static final int SELECTION_PRESSED = 1; // D-pad down but not up yet
    private static final int SELECTION_SELECTED = 2;
    private static final int SELECTION_LONGPRESS = 3;
    /**
     * The height of the day names/numbers
     */
    private static int DAY_HEADER_HEIGHT = 45;
    private static float MIN_EVENT_HEIGHT = 24.0F; // in pixels
    private static int mMinCellHeight = 32;
    private int mFirstCell;
    private static int ALLDAY_TOP_MARGIN = 1;
    /**
     * This is the minimum size reserved for displaying regular events.
     * The expanded allDay region can't expand into this.
     */
    private static int MIN_HOURS_HEIGHT = 180;
    private static int SINGLE_ALLDAY_HEIGHT = 34;
    /**
     * This is the minimum desired height of a allday event.
     * When unexpanded, allday events will use this height.
     * When expanded allDay events will attempt to grow to fit all
     * events at this height.
     */
    private static float MIN_UNEXPANDED_ALLDAY_EVENT_HEIGHT = 28.0F; // in pixels
    /**
     * The height of an individual allday event during animation
     */
    private int mAnimateDayEventHeight = (int) MIN_UNEXPANDED_ALLDAY_EVENT_HEIGHT;
    private Time mBaseDate;
    protected int mNumDays = 7;
    private int mNumHours = 10;
    private AccessibilityManager mAccessibilityMgr = null;
    private static int mFutureBgColor;
    private static int mFutureBgColorRes;
    private boolean mIs24HourFormat;
    private String[] mHourStrs;
    private int mLastSelectionDayForAccessibility;
    private int mLastSelectionHourForAccessibility;
    private Event mLastSelectedEventForAccessibility;
    private int mSelectionMode = SELECTION_HIDDEN;
    private boolean mIsAccessibilityEnabled = false;
    private boolean mTouchExplorationEnabled = false;
    protected boolean mPaused = true;
    private Handler mHandler;
    private final DayHeaderView.UpdateCurrentTime mUpdateCurrentTime = new DayHeaderView.UpdateCurrentTime();
    private static final int UPDATE_CURRENT_TIME_DELAY = 300000;
    private int mTodayJulianDay;
    private int mSelectionDay;        // Julian day
    private int mSelectionDayForAccessibility;        // Julian day
    private int mSelectionHour;
    private long mLastReloadMillis;
    private Event mSelectedEvent;
    private Event mPrevSelectedEvent;
    private final ArrayList<Event> mSelectedEvents = new ArrayList<Event>();
    private int mFirstJulianDay;
    private int mLoadedFirstJulianDay = -1;
    private int mLastJulianDay;
//    private static ArrayList<Event> mEvents = new ArrayList<Event>();
//    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private static ArrayList<Event> mEvents = Utils.mEvents;
    private ArrayList<Event> mAllDayEvents = new ArrayList<Event>();
    private StaticLayout[] mLayouts = null;
    private StaticLayout[] mAllDayLayouts = null;
    private boolean mRemeasure = true;
    private boolean mComputeSelectedEvents;
    private ObjectAnimator mEventsCrossFadeAnimation;
    // duration for events' cross-fade animation
    private static final int EVENTS_CROSS_FADE_DURATION = 400;
    private int mEventsAlpha = 255;
    private Event mSelectedEventForAccessibility;
    private int[] mEarliestStartHour;    // indexed by the week day offset
    private boolean[] mHasAllDayEvent;   // indexed by the week day offset
    /**
     * Max of all day events in a given day in this view.
     */
    private int mMaxAlldayEvents;
    private int mMonthLength;
    private int mFirstVisibleDate;
    private int mFirstVisibleDayOfWeek;
    /**
     * The number of allDay events at which point we start hiding allDay events.
     */
    private int mMaxUnexpandedAlldayEventCount = 4;
    /**
     * Whether or not to expand the allDay area to fill the screen
     */
    private static boolean mShowAllAllDayEvents = false;
    /**
     * The height of the day names/numbers when viewing a single day
     */
    private static int ONE_DAY_HEADER_HEIGHT = DAY_HEADER_HEIGHT;
    private boolean mIsSelectionFocusShow = false;
    /**
     * First fully visibile hour
     */
    private int mFirstHour = -1;
    /**
     * Distance between the mFirstCell and the top of first fully visible hour.
     */
    private int mFirstHourOffset;
    private PopupWindow mPopup;
    private View mPopupView;
    private long mLastPopupEventID;
    private static final long INVALID_EVENT_ID = -1; //This is used for remembering a null event
    private final DismissPopup mDismissPopup = new DismissPopup();
    private static int mCellHeight = 0; // shared among all DayViews
    private boolean mScrolling = false;
    private Event mClickedEvent;           // The event the user clicked on
    private Event mSavedClickedEvent;
    private int mClickedYLocation;
    boolean mSelectionAllday;
    private int mSelectionHourForAccessibility;
    private boolean mUpdateToast;
    private static final int DAY_GAP = 1;
    private static final int HOUR_GAP = 1;
    private static int MAX_UNEXPANDED_ALLDAY_HEIGHT =
            (int) (MIN_UNEXPANDED_ALLDAY_EVENT_HEIGHT * 4);
    private static int MAX_HEIGHT_OF_ONE_ALLDAY_EVENT = 34;
    /**
     * The height of the area used for allday events
     */
    private int mAlldayHeight;
    private int mGridAreaHeight = -1;
    /**
     * The height of the allday event area used during animation
     */
    private int mAnimateDayHeight = 0;

    Paint mPaint = new Paint();

    @Override
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        super.removeOnAttachStateChangeListener(listener);
    }

    protected final Resources mResources;

    private static int NormalTextSize = 17;
    private static int NormalLunarTextSize = 8;

    private static int mHorizontalSnapBackThreshold = 48;

    private static final int WeekSize = 7;

    private final GestureDetector mGestureDetector;
    // Pixels scrolled
    private float mInitialScrollX;
    private float mInitialScrollY;

    private boolean mStartingScroll = false;

    private static int mScaledPagingTouchSlop = 0;

    private float mAnimationDistance = 0;

    private int mPreviousDirection;

    /**
     * The initial state of the touch mode when we enter this view.
     */
    private static final int TOUCH_MODE_INITIAL_STATE = 0;

    /**
     * Indicates we just received the touch event and we are waiting to see if
     * it is a tap or a scroll gesture.
     */
    private static final int TOUCH_MODE_DOWN = 1;

    /**
     * Indicates the touch gesture is a vertical scroll
     */
    private static final int TOUCH_MODE_VSCROLL = 0x20;

    /**
     * Indicates the touch gesture is a horizontal scroll
     */
    private static final int TOUCH_MODE_HSCROLL = 0x40;

    private static final int TOUCH_MODE_ONCLICK = 0x80;

    private int mTouchMode = TOUCH_MODE_INITIAL_STATE;

    ScaleGestureDetector mScaleGestureDetector;

    private MonthUtils mMonthUtils;

    private int HOURS_LEFT_MARGIN;

    private int HOURS_RIGHT_MARGIN;

    // CHECKSTYLE:OFF
    private static float mScale = 0; // Used for supporting different screen

    private static Bitmap mBitmap;

    private static Bitmap mOtharMonthBitmap;

    private static int sCounter = 0;

    private final CalendarController mController;

    private static int mMonthOnFourceColor;

    private static int mMonthNoOnFourceColor;

    private EventLoader mEventLoader;

    // When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
    protected int mFirstDayOfWeek;

    private Paint mUnderlinePaint;
    private static int mUnderlineColor;
    private static int mUnderLineWidth;
    private static int mUnderLineLength;
    private int mCellSpace;
    private int mCnY;
    private int mEnY;

    private final Runnable mTZUpdater = new Runnable() {
        @Override
        public void run() {
            String tz = Utils.getTimeZone(mContext, this);
            mBaseTime.timezone = tz;
            mBaseTime.normalize(true);
            mCurrentTime.switchTimezone(tz);
            invalidate();
        }
    };


    public DayHeaderView(Context context, CalendarController controller,
                         ViewSwitcher mViewSwitcher, EventLoader eventLoader/*,int flag*/) {

        super(context);
        // TODO Auto-generated constructor stub
        this.mContext = context;

        this.mViewSwitcher = mViewSwitcher;

        this.mController = controller;

        mResources = mContext.getResources();

        mGestureDetector = new GestureDetector(context,
                new WeekGestureListener());

        mScaleGestureDetector = new ScaleGestureDetector(getContext(), this);

        ViewConfiguration vc = ViewConfiguration.get(context);
        mScaledPagingTouchSlop = vc.getScaledPagingTouchSlop();
        init(mContext);
        mMonthUtils = new MonthUtils(mContext);

//		if(flag==2){
        this.mEventLoader = eventLoader;
//		}
    }

    private void init(Context mContext) {
        mTextWeekDayColor = mResources.getColor(android.R.color.black);
        HOURS_LEFT_MARGIN = (int) mResources
                .getDimension(R.dimen.hours_left_margin);
        HOURS_RIGHT_MARGIN = (int) mResources
                .getDimension(R.dimen.hours_right_margin);

        if (mScale == 0) {
            mScale = mResources.getDisplayMetrics().density;

            NormalTextSize *= mScale;

            NormalLunarTextSize *= mScale;
        }

        mMonthOnFourceColor = mResources.getColor(R.color.month_onfource_color);
        mMonthNoOnFourceColor = mResources.getColor(R.color.month_day_number);

        mCurrentTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
        long currentTime = System.currentTimeMillis();
        mCurrentTime.set(currentTime);

        InputStream is = mResources.openRawResource(R.drawable.month_onfoucs);
        mBitmap = BitmapFactory.decodeStream(is);

        InputStream input = getResources().openRawResource(R.drawable.month_other_onfoucs);
//        InputStream input = getResources().openRawResource(R.drawable.month_onfoucs);
        mOtharMonthBitmap = BitmapFactory.decodeStream(input);
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);

        mBaseDate = new Time(Utils.getTimeZone(mContext, mTZUpdater));
        long millis = System.currentTimeMillis();
        mBaseDate.set(millis);

        mEarliestStartHour = new int[mNumDays];

        mUnderlineColor = mContext.getResources().getColor(R.color.underline_color);
        mUnderLineWidth = mContext.getResources().getDimensionPixelSize(R.dimen.prize_underline);
        mCellSpace = Math.min(mViewHeight, mViewWight / 7);
//		mUnderLineLength = mCellSpace / 3;
//		mUnderLineLength = 90;
        mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mUnderlinePaint.setStyle(Style.FILL);
        mUnderlinePaint.setTextAlign(Align.CENTER);
        mUnderlinePaint.setAntiAlias(true);
        mUnderlinePaint.setColor(mUnderlineColor);

        mCnY = (int) mContext.getResources().getDimension(R.dimen.ch_y);
        mEnY = (int) mContext.getResources().getDimension(R.dimen.en_y);


        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mContext.getResources().getColor(R.color.prize_background));
    }


    public void setSelected(Time time, boolean ignoreTime, boolean animateToday) {
        mBaseDate.set(time);
        if (this.mBaseTime == null) {
            this.mBaseTime = new Time();
        }
        this.mBaseTime.set(time.monthDay, time.month, time.year);

        mBaseTime.normalize(true);

        int julainday = Utils.getJulianDayInGeneral(mBaseTime, true)
                - (Calendar.DAY_OF_WEEK - mFirstDayOfWeek + mBaseTime.weekDay)
                % Calendar.DAY_OF_WEEK;

        mBaseFristDayTime = new Time();
        mBaseFristDayTime.setJulianDay(julainday);
        mBaseFristDayTime.normalize(true);
        invalidate();
        recalc();    //add by hekeyi for calendar v8.0
    }

    /*
        @SuppressLint("WrongCall")*/
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
//        Log.d("hekeyi","[DayHeaderView]-onDraw..............");
        // offset canvas by the current drag and header position
        canvas.translate(-mViewStartX, 0);
        // clip to everything below the allDay area
        Rect dest = new Rect();
        dest.top = 0;
        dest.bottom = (int) (mViewHeight);
        dest.left = 0;
        dest.right = mViewWight;

        canvas.save();
        canvas.clipRect(dest);
//        markEvent(canvas); //待调试
//        OnDrawBackGroup(canvas);
        OnDrawWeek(canvas);
        canvas.restore();
        if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            DayHeaderView nextView = (DayHeaderView) mViewSwitcher
                    .getNextView();
            if (nextView != this) {
                float xTranslate;
                if (mViewStartX > 0) {
                    xTranslate = mViewWight;
                } else {
                    xTranslate = -mViewWight;
                }
                // Move the canvas around to prep it for the next view
                // specifically, shift it by a screen and undo the
                // yTranslation which will be redone in the nextView's onDraw().
                canvas.translate(xTranslate, 0);

                // Prevent infinite recursive calls to onDraw().
                nextView.mTouchMode = TOUCH_MODE_INITIAL_STATE;
                nextView.onDraw(canvas);
                // Move it back for this view
                canvas.translate(-xTranslate, 0);
            }
        } else {
            // If we drew another view we already translated it back
            // If we didn't draw another view we should be at the edge of the
            // screen
            canvas.translate(mViewStartX, 0);
        }
//		canvas.restore();


        isScrolling = false;
        super.onDraw(canvas);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TODO Auto-generated method stub
        this.mViewWight = w;
        this.mViewHeight = h;
        mCellSpace = Math.min(mViewHeight, mViewWight / 7);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @SuppressLint("ResourceAsColor")
    private void OnDrawBackGroup(Canvas canvas) {
        boolean isDrawBackGroup = (Utils.getJulianDayInGeneral(mBaseTime, true) - Utils
                .getJulianDayInGeneral(mCurrentTime, true)) > (6 - mCurrentTime.weekDay);

        if (isDrawBackGroup) {
            Paint p = new Paint();
            Rect r = new Rect();
            r.top = 0;
            r.bottom = mViewHeight;
            r.left = HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN;
            r.right = mViewWight;
            p.setColor(android.R.color.white);
            p.setStyle(Style.FILL);
            p.setAntiAlias(false);
            canvas.drawRect(r, p);
        }
    }

    String lunarDate;
    int eventDay = -1;
    int eventMonth = -1;

    private String SHARED_PREFS_NAME = "com.android.calendar_preferences";
    private String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
    private String mTimeZoneId;
    private boolean mUseHomeTZ = false;
    private void OnDrawWeek(Canvas canvas) {
//        Log.d("hekeyi","[DayHeaderView]-OnDrawWeek............");

        mCurrentTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
        long currentTime = System.currentTimeMillis();
        mCurrentTime.set(currentTime);

        int x, y = 0;
        Paint p = new Paint();
        p.setTextSize(NormalTextSize);
        p.setStyle(Style.FILL);
        p.setTextAlign(Align.CENTER);
        p.setAntiAlias(true);
        p.setColor(mTextWeekDayColor);
        int mMaxMonthDay = mBaseFristDayTime
                .getActualMaximum(mBaseFristDayTime.MONTH_DAY);

        int FistWeekDay = mBaseFristDayTime.monthDay;

        int CurrentWeekMonth = mBaseFristDayTime.month;

        /*for(int day = 0; day < Calendar.DAY_OF_WEEK; day++){
            eventDay = FistWeekDay + day;
            eventMonth = CurrentWeekMonth + 1;
            markEvent(canvas, eventDay, eventMonth);    //待调试
        }*/

        int CurrentWeekYear = mBaseFristDayTime.year;
        int size = Utils.getJulianDayInGeneral(mBaseTime, true)
                - Utils.getJulianDayInGeneral(mBaseFristDayTime, true);

        SharedPreferences prefs = CalendarUtils.getSharedPreferences(mContext,SHARED_PREFS_NAME);
        mUseHomeTZ = prefs.getBoolean(KEY_HOME_TZ_ENABLED,false);
        mTimeZoneId = Utils.getTimeZone(mContext, null);

        /**add by hekeyi for calendar v8.0 begin*/
//        Log.d("hekeyi","[DayHeaderView]-start = "+System.currentTimeMillis());
//        Time todayTime = new Time();
        Time todayTime ;
        if(!mUseHomeTZ){
            todayTime = new Time();
        }else {
            todayTime = new Time(mTimeZoneId);
        }
        todayTime.setToNow();
        int todaySize = Utils.getJulianDayInGeneral(todayTime, true)
                - Utils.getJulianDayInGeneral(mBaseFristDayTime, true);
        int todayX = (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN)
                + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                * (todaySize) / Calendar.DAY_OF_WEEK
                + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                / (2 * Calendar.DAY_OF_WEEK) - mBitmap.getWidth() / 2;
//        Log.d("hekeyi","[DayHeaderView]-end = "+System.currentTimeMillis());

        /**add by hekeyi for calendar v8.0 end*/

        x = (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN)
                + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                * (size) / Calendar.DAY_OF_WEEK
                + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                / (2 * Calendar.DAY_OF_WEEK) - mBitmap.getWidth() / 2;

        y = mViewHeight / 2 - mBitmap.getHeight() / 2;

        if (Utils.getJulianDayInGeneral(mBaseTime, true) == Utils
                .getJulianDayInGeneral(mCurrentTime, true)) {
            canvas.drawBitmap(mBitmap, x, y, p);
        } else {
            canvas.drawBitmap(mOtharMonthBitmap, x, y, p);
            canvas.drawBitmap(mBitmap, todayX, y, p);
        }
        y = (mViewHeight) / 2;

        for (int day = 0; day < Calendar.DAY_OF_WEEK; day++) {
            x = (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN)
                    + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                    * day / Calendar.DAY_OF_WEEK
                    + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                    / (Calendar.DAY_OF_WEEK * 2);
            p.setTextSize(mTextWeekDayColor);


            boolean isCurrent = false;
            isCurrent = (day + mFirstDayOfWeek) % Calendar.DAY_OF_WEEK == mCurrentTime.weekDay;

            if ((FistWeekDay + day) <= mMaxMonthDay) {
                p.setAlpha(255);
                p.setTextSize(NormalTextSize);
                if (isCurrent && (day + FistWeekDay == mCurrentTime.monthDay)
                        && (CurrentWeekYear == mCurrentTime.year) && (CurrentWeekMonth == mCurrentTime.month)) {
                    p.setColor(mMonthOnFourceColor);
                } else {
                    p.setColor(mMonthNoOnFourceColor);
                }
                canvas.drawText("" + (FistWeekDay + day), x, y, p);
                p.setTextSize(NormalLunarTextSize);


                if (mContext.getResources().getConfiguration().locale
                        .getCountry().equals("CN")
                        || mContext.getResources().getConfiguration().locale
                        .getCountry().equals("TW")) {
                    canvas.drawText(
                            ""
                                    + mMonthUtils.getMonthShowLunar(
                                    CurrentWeekYear,
                                    CurrentWeekMonth + 1,
                                    (FistWeekDay + day)), x, y + 24, p);
                }


                eventDay = FistWeekDay + day;
                eventMonth = CurrentWeekMonth + 1;
                markEvent(canvas, eventDay, eventMonth);    //待调试
            } else if ((FistWeekDay + day) > mMaxMonthDay
                    && CurrentWeekMonth < 11) {
                p.setTextSize(NormalTextSize);
                p.setAlpha(255);

                if (isCurrent && (day + FistWeekDay - mMaxMonthDay == mCurrentTime.monthDay)
                        && (CurrentWeekYear == mCurrentTime.year) && (CurrentWeekMonth == mCurrentTime.month - 1)) {
                    p.setColor(mMonthOnFourceColor);
                } else {
                    p.setColor(mMonthNoOnFourceColor);
                }
                canvas.drawText("" + (FistWeekDay + day - mMaxMonthDay), x, y, p);

                p.setTextSize(NormalLunarTextSize);
//				p.setAlpha((int) (255 * 0.3));
                if (mContext.getResources().getConfiguration().locale
                        .getCountry().equals("CN")
                        || mContext.getResources().getConfiguration().locale
                        .getCountry().equals("TW")) {
                    canvas.drawText(
                            ""
                                    + mMonthUtils.getMonthShowLunar(
                                    CurrentWeekYear,
                                    CurrentWeekMonth + 2, (FistWeekDay
                                            + day - mMaxMonthDay)), x,
                            y + 24, p);
                }

                eventDay = FistWeekDay + day - mMaxMonthDay;
                eventMonth = CurrentWeekMonth + 2;
                markEvent(canvas, eventDay, eventMonth); //待调试
            } else if ((FistWeekDay + day) > mMaxMonthDay
                    && CurrentWeekMonth == 11) {
                p.setTextSize(NormalTextSize);
                p.setAlpha(255);
                if (isCurrent && (day + FistWeekDay - mMaxMonthDay == mCurrentTime.monthDay)
                        && (CurrentWeekYear == mCurrentTime.year - 1)) {
                    p.setColor(mMonthOnFourceColor);
                } else {
                    p.setColor(mMonthNoOnFourceColor);
                }
                canvas.drawText("" + (FistWeekDay + day - mMaxMonthDay), x, y, p);

                p.setTextSize(NormalLunarTextSize);
//				p.setAlpha((int) (255 * 0.3));
                if (mContext.getResources().getConfiguration().locale
                        .getCountry().equals("CN")
                        || mContext.getResources().getConfiguration().locale
                        .getCountry().equals("TW")) {
                    canvas.drawText(
                            ""
                                    + mMonthUtils.getMonthShowLunar(
                                    CurrentWeekYear + 1, 1, (FistWeekDay
                                            + day - mMaxMonthDay)), x,
                            y + 24, p);//1月份的第一周
                }
            }

            lunarDate = mMonthUtils.getMonthShowLunar(CurrentWeekYear, CurrentWeekMonth + 1, (FistWeekDay + day));
            drawUndeLine(x, y, lunarDate, canvas);

            eventDay = FistWeekDay + day - mMaxMonthDay;
            eventMonth = CurrentWeekMonth + 2;
            markEvent(canvas,eventDay,eventMonth); //待调试
        }
    }


    private void cancelAnimation() {
        Animation in = mViewSwitcher.getInAnimation();
        if (in != null) {
            // cancel() doesn't terminate cleanly.
            in.scaleCurrentDuration(0);
        }
        Animation out = mViewSwitcher.getOutAnimation();
        if (out != null) {
            // cancel() doesn't terminate cleanly.
            out.scaleCurrentDuration(0);
        }
    }

    private void doSingleTapUp(MotionEvent ev) {
        int PostionX = (int) ev.getX();
        int WeekDay = 0;
        if (PostionX <= (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN + (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                / (Calendar.DAY_OF_WEEK))) {
            WeekDay = 0;
        } else {
            WeekDay = (int) ((ev.getX() - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN))
                    * Calendar.DAY_OF_WEEK / (mViewWight - (HOURS_LEFT_MARGIN + HOURS_RIGHT_MARGIN)));
        }
        int CurrentDay = (WeekSize + mBaseTime.weekDay - mFirstDayOfWeek)
                % WeekSize;
        if (WeekDay == CurrentDay) {
            return;
        } else {

            int mBaseJulianDay = Utils.getJulianDayInGeneral(mBaseTime, true)
                    + (WeekDay - CurrentDay);
            mBaseTime.setJulianDay(mBaseJulianDay);
            mBaseTime.normalize(true);
            invalidate();
            mController.sendEvent(this, EventType.GO_TO, mBaseTime, mBaseTime,
                    null, -1, ViewType.CURRENT,
                    CalendarController.EXTRA_GOTO_DATE, null, null);
        }

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//		Log.d("hekeyi","[DayHeaderView]-onScrollChanged l = "+l+" t="+t+" oldl="+oldl+" oldt="+oldt);
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        super.setOnScrollChangeListener(l);
    }

    public static boolean isScrolling = false;

    private void doScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//		Log.d("hekeyi","[DayHeaderView]-onScrollChanged mTouchMode = "+mTouchMode+" mStartingScroll = "+mStartingScroll);
        /*if(mCalendarRelativeLayout==null){
			mCalendarRelativeLayout = (CalendarRelativeLayout)getRootView().findViewById(R.id.calendar_layout);
		}*/
		/*if(mTouchMode==TOUCH_MODE_DOWN){
			isScrolling = true;
		}else {
			isScrolling = false;
		}*/
//        cancelAnimation();

        if (mStartingScroll) {
            mInitialScrollX = 0;
            mStartingScroll = false;
        }
        mInitialScrollX += distanceX;
        // mInitialScrollY += deltaY;
        int distanceXpostion = (int) mInitialScrollX;
        int deltaX = (int) mInitialScrollX;
        if (mTouchMode == TOUCH_MODE_DOWN) {
            int absDistanceX = Math.abs(deltaX);
            mPreviousDirection = 0;
            int slopFactor = mScaleGestureDetector.isInProgress() ? 20 : 2;
            if (absDistanceX > mScaledPagingTouchSlop * slopFactor) {
                mTouchMode = TOUCH_MODE_HSCROLL;
                mViewStartX = deltaX;
                initNextView(-mViewStartX);
            }

        } else if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
            // We are already scrolling horizontally, so check if we
            // changed the direction of scrolling so that the other week
            // is now visible.
            mViewStartX = distanceXpostion;
            if (distanceX != 0) {
                int direction = (distanceX > 0) ? 1 : -1;
                if (direction != mPreviousDirection) {
                    // The user has switched the direction of scrolling
                    // so re-init the next view
                    initNextView(-mViewStartX);
                    mPreviousDirection = direction;
                }
            }
        }
//        invalidate();    //removed by hekeyi 20171013  reduce zhe count of multi draw
    }

    private void doDown(MotionEvent ev) {
        mTouchMode = TOUCH_MODE_DOWN;
    }

    private void initNextView(int DistanceX) {
        int JulianDay = Utils.getJulianDayInGeneral(mBaseTime, true);
        Time mNextBaseTime = new Time();
        DayHeaderView nextView = (DayHeaderView) mViewSwitcher.getNextView();
        nextView.setSelected(mNextBaseTime, true, true);
        nextView.layout(getLeft(), getTop(), getRight(), getBottom());
        if (DistanceX > 0) {
            JulianDay -= 7;
            mNextBaseTime.setJulianDay(JulianDay);
            mNextBaseTime.normalize(true);
        } else {
            JulianDay += 7;
            mNextBaseTime.setJulianDay(JulianDay);
            mNextBaseTime.normalize(true);
        }
        /*nextView.setSelected(mNextBaseTime, true, true);   //changed by hekeyi 20171013
        nextView.layout(getLeft(), getTop(), getRight(), getBottom());*/
//        nextView.requestLayout();
//        nextView.recalc();
    }

    private float mDownX;
    private float mDownY;
    CalendarRelativeLayout mCalendarRelativeLayout;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
//		Log.d("hekeyi","[DayHeaderView]-onTouchEvent TOUCH_MODE_HSCROLL = "+TOUCH_MODE_HSCROLL+"  mStartingScroll = "+mStartingScroll);

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartingScroll = true;
                mGestureDetector.onTouchEvent(event);
                /**add by hekeyi for calendar v8.0 20170717 begin*/
//                mDownX = event.getX();
//                mDownY = event.getY();
                /**add by hekeyi for calendar v8.0 20170717 end*/
                return true;
            case MotionEvent.ACTION_MOVE:
                /**add by hekeyi for calendar v8.0 20170717 begin*/
			/*float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			if(Math.abs(disX)>10){
				Log.d("hekeyi","DayHeaderView   getRootView = "+getRootView());
				getParent().requestDisallowInterceptTouchEvent(true);
			}else if(Math.abs(disX)==0&&Math.abs(disY)>50 ){
				getParent().requestDisallowInterceptTouchEvent(false);
			}*/
                /**add by hekeyi for calendar v8.0 20170717 end*/
                mGestureDetector.onTouchEvent(event);
                return true;
            case MotionEvent.ACTION_UP:
//			getParent().requestDisallowInterceptTouchEvent(false); //add by hekeyi for calendar v8.0 20170717
                /**add by hekeyi for calendar v8.0 20170717 begin*/
			/*float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			if(Math.abs(disX)>10){
				Log.d("hekeyi","DayHeaderView   getRootView = "+getRootView());
				getParent().requestDisallowInterceptTouchEvent(true);
			}else if(Math.abs(disX)==0&&Math.abs(disY)>50 ){
				getParent().requestDisallowInterceptTouchEvent(false);
			}*/
			/*if(mCalendarRelativeLayout==null){
				mCalendarRelativeLayout = (CalendarRelativeLayout)getRootView().findViewById(R.id.calendar_layout);
			}
			float disX = event.getX() - mDownX;
			if(Math.abs(disX)>10){
				mCalendarRelativeLayout.setScrollable(false);
			}else {
				mCalendarRelativeLayout.setScrollable(true);
			}*/
                float disX = event.getX() - mDownX;
                if (Math.abs(disX) > 10) {
                    isScrolling = true;
                } else {
                    isScrolling = false;
                }
                /**add by hekeyi for calendar v8.0 20170717 end*/
                mGestureDetector.onTouchEvent(event);
                if ((mTouchMode & TOUCH_MODE_HSCROLL) != 0) {
                    // mTouchMode = TOUCH_MODE_INITIAL_STATE;
                    if (Math.abs(mViewStartX) > mHorizontalSnapBackThreshold) {
                        // The user has gone beyond the threshold so switch views
//                        invalidate();
                        switchViews(mViewStartX > 0, mViewStartX, mViewWight, 0,
                                false);
                        // mViewStartX = 0;
                        return true;
                    } else {
                        // Not beyond the threshold so invalidate which will cause
                        // the view to snap back. Also call recalc() to ensure
                        // that we have the correct starting date and title.
                        mViewStartX = 0;
                        invalidate();

                        return true;
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                mGestureDetector.onTouchEvent(event);
                return true;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private View switchViews(boolean forward, float xOffSet, float width, float velocity, boolean isMaxforward) {
        mAnimationDistance = width - xOffSet;
//        Log.d("hekeyi","[DayHeaderView]-switchViews...............");
        float progress = Math.abs(xOffSet) / width;
        if (progress > 1.0f) {
            progress = 1.0f;
        }

        float inFromXValue = 0, inToXValue = 0;
        float outFromXValue = 0, outToXValue = 0;
		/*Time startTime = new Time();
		Time endTime = new Time();
		Time newSelectTime = new Time();*/
        if (forward) {
            inFromXValue = 1.0f - progress;
            inToXValue = 0.0f;
            outFromXValue = -progress;
            outToXValue = -1.0f;
			/*startTime.setJulianDay(getNextWeek(
					Utils.getJulianDayInGeneral(mBaseTime, true), 1));
			newSelectTime.set(startTime);
			endTime.set(startTime);*/
        } else {

            inFromXValue = progress - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = progress;
            outToXValue = 1.0f;
			/*startTime.setJulianDay(getNextWeek(
					Utils.getJulianDayInGeneral(mBaseTime, true), -1));
			newSelectTime.set(startTime);
			endTime.set(startTime);*/
        }

        final Time start = new Time(mBaseDate.timezone);
        start.set(mController.getTime());
        if (forward) {
            start.monthDay += mNumDays;
        } else {
            start.monthDay -= mNumDays;
        }
        mController.setTime(start.normalize(true));

        Time newSelected = start;

        if (mNumDays == 7) {
            newSelected = new Time(start);
            adjustToBeginningOfWeek(start);
        }

        final Time end = new Time(start);
        end.monthDay += mNumDays - 1;

		/*newSelectTime.normalize(true);
		endTime.normalize(true);
		startTime.normalize(true);*/

        TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue, Animation.ABSOLUTE,
                0.0f, Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue, Animation.ABSOLUTE,
                0.0f, Animation.ABSOLUTE, 0.0f);

        inAnimation.setDuration(400);

        outAnimation.setDuration(400);
//		outAnimation.setAnimationListener(new GotoBroadcaster(startTime,endTime));

        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
        this.mViewStartX = 0;
		mViewSwitcher.showNext();
        DayHeaderView view = (DayHeaderView) mViewSwitcher.getCurrentView();
        view.cleanup();
        mViewSwitcher.showNext();
        view = (DayHeaderView) mViewSwitcher.getCurrentView();
        view.setSelected(newSelected, true, false);
//        view.requestFocus();
        view.reloadEvents();
//        view.updateTitle();
//        view.restartCurrentTimeUpdates();

        mController.sendEvent(this, EventType.UPDATE_AGENDA_SHOW_TIME, mBaseTime, mBaseTime,
                null, -1, ViewType.CURRENT,
                CalendarController.EXTRA_GOTO_DATE, null, null);
        isScrolling = false;

        return view;
    }

    public void setSelectGoTo(Time SelectTime) {
        float inFromXValue = 0.0f, inToXValue = 0.0f;

        float outFromXValue = 0.0f, outToXValue = 0.0f;

        int SelectTimeJulianDay = Utils.getJulianDayInGeneral(SelectTime, true);

        int FristShowTimeJulianDay = Utils.getJulianDayInGeneral(mBaseTime,
                true)
                - (WeekSize - mFirstDayOfWeek + mBaseTime.weekDay)
                % WeekSize;

        int EndShowTimeJulianDay = FristShowTimeJulianDay + WeekSize - 1;

        if (SelectTimeJulianDay >= FristShowTimeJulianDay
                && SelectTimeJulianDay <= EndShowTimeJulianDay) {
            mBaseTime.set(SelectTime);
            mBaseTime.normalize(true);
            invalidate();
            return;
        } else if (SelectTimeJulianDay < FristShowTimeJulianDay) {
            inFromXValue = 0 - 1.0f;
            inToXValue = 0.0f;
            outFromXValue = 0;
            outToXValue = 1.0f;
        } else if (SelectTimeJulianDay > EndShowTimeJulianDay) {
            inFromXValue = 1.0f - 0;
            inToXValue = 0.0f;
            outFromXValue = -0;
            outToXValue = -1.0f;
        }
        TranslateAnimation inAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, inFromXValue,
                Animation.RELATIVE_TO_SELF, inToXValue, Animation.ABSOLUTE,
                0.0f, Animation.ABSOLUTE, 0.0f);

        TranslateAnimation outAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, outFromXValue,
                Animation.RELATIVE_TO_SELF, outToXValue, Animation.ABSOLUTE,
                0.0f, Animation.ABSOLUTE, 0.0f);

        inAnimation.setDuration(400);

        outAnimation.setDuration(400);
        DayHeaderView mNextDayHeaderView = (DayHeaderView) mViewSwitcher
                .getNextView();
        mNextDayHeaderView.setSelected(SelectTime, true, true);
        mViewSwitcher.setInAnimation(inAnimation);
        mViewSwitcher.setOutAnimation(outAnimation);
        this.mViewStartX = 0;
        mViewSwitcher.showNext();
        DayHeaderView view = (DayHeaderView) mViewSwitcher.getCurrentView();

        SelectTime.normalize(true);
        view.setSelected(SelectTime, true, true);
        isScrolling = false;

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    private class GotoBroadcaster implements Animation.AnimationListener {
        private final int mCounter;
        private final Time mStart;
        private final Time mEnd;

        public GotoBroadcaster(Time start, Time end) {
            mCounter = ++sCounter;
            mStart = start;
            mEnd = end;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            DayHeaderView view = (DayHeaderView) mViewSwitcher.getCurrentView();
            view.mViewStartX = 0;
            view = (DayHeaderView) mViewSwitcher.getNextView();
            view.mViewStartX = 0;

            if (mCounter == sCounter) {
                mController.sendEvent(this, EventType.GO_TO, mStart, mEnd,
                        null, -1, ViewType.CURRENT,
                        CalendarController.EXTRA_GOTO_DATE, null, null);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    private int getNextWeek(int SelectJuLianDay, int size) {
        // TODO Auto-generated method stub
        return SelectJuLianDay + size * Calendar.DAY_OF_WEEK;
    }

    public class WeekGestureListener extends
            GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            // TODO Auto-generated method stub
            DayHeaderView.this.doScroll(e1, e2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // TODO Auto-generated method stub
            DayHeaderView.this.doDown(e);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // TODO Auto-generated method stub
            DayHeaderView.this.doSingleTapUp(e);
            return super.onSingleTapUp(e);
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector arg0) {
        // TODO Auto-generated method stub

    }

    public void updateTitle() {
        Time start = new Time(mBaseDate);
        /*if(!mUseHomeTZ){
            start = new Time(mBaseDate);
        }else {
            start = new Time(mTimeZoneId);
        }*/
        start.normalize(true);
        Time end = new Time(start);
        end.monthDay += mNumDays - 1;
        // Move it forward one minute so the formatter doesn't lose a day

//		end.minute += 1;    //move by hekeyi for mark event bug
        end.normalize(true);
        long formatFlags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        if (mNumDays != 1) {
            // Don't show day of the month if for multi-day view
            formatFlags |= DateUtils.FORMAT_NO_MONTH_DAY;

            // Abbreviate the month if showing multiple months
            if (start.month != end.month) {
                formatFlags |= DateUtils.FORMAT_ABBREV_MONTH;
            }
        }

        mController.sendEvent(this, EventType.UPDATE_TITLE, start, end, null, -1, ViewType.CURRENT,
                formatFlags, null, null);
    }

    public void handleOnResume() {
        initAccessibilityVariables();
        if (Utils.getSharedPreference(mContext, OtherPreferences.KEY_OTHER_1, false)) {
            mFutureBgColor = 0;
        } else {
            mFutureBgColor = mFutureBgColorRes;
        }
        mIs24HourFormat = DateFormat.is24HourFormat(mContext);
        mHourStrs = mIs24HourFormat ? CalendarData.s24Hours : CalendarData.s12HoursNoAmPm;
        mFirstDayOfWeek = Utils.getFirstDayOfWeek(mContext);
        mLastSelectionDayForAccessibility = 0;
        mLastSelectionHourForAccessibility = 0;
        mLastSelectedEventForAccessibility = null;
        mSelectionMode = SELECTION_HIDDEN;

    }

    private void initAccessibilityVariables() {
        mAccessibilityMgr = (AccessibilityManager) mContext
                .getSystemService(Service.ACCESSIBILITY_SERVICE);
        mIsAccessibilityEnabled = mAccessibilityMgr != null && mAccessibilityMgr.isEnabled();
//		mTouchExplorationEnabled = isTouchExplorationEnabled();
    }

    /**
     * Restart the update timer
     */
    public void restartCurrentTimeUpdates() {
        mPaused = false;
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCurrentTime);
            mHandler.post(mUpdateCurrentTime);
        }
    }

    class UpdateCurrentTime implements Runnable {

        public void run() {
            long currentTime = System.currentTimeMillis();
            mCurrentTime.set(currentTime);
            //% causes update to occur on 5 minute marks (11:10, 11:15, 11:20, etc.)
            if (!DayHeaderView.this.mPaused) {
                mHandler.postDelayed(mUpdateCurrentTime, UPDATE_CURRENT_TIME_DELAY
                        - (currentTime % UPDATE_CURRENT_TIME_DELAY));
            }
            mTodayJulianDay = Time.getJulianDay(currentTime, mCurrentTime.gmtoff);
            invalidate();
        }
    }

    /**
     * Returns the start of the selected time in milliseconds since the epoch.
     *
     * @return selected time in UTC milliseconds since the epoch.
     */
    public long getSelectedTimeInMillis() {
        Time time = new Time(mBaseDate);
        /// M: @{
        Utils.setJulianDayInGeneral(time, mSelectionDay);
        /// @}
        time.hour = mSelectionHour;

        // We ignore the "isDst" field because we want normalize() to figure
        // out the correct DST value and not adjust the selected time based
        // on the current setting of DST.
        return time.normalize(true /* ignore isDst */);
    }

    public void clearCachedEvents() {
        mLastReloadMillis = 0;
    }

    public void reloadEvents() {

        // Protect against this being called before this view has been
        // initialized.
//        if (mContext == null) {
//            return;
//        }

        // Make sure our time zones are up to date
        mTZUpdater.run();

        setSelectedEvent(null);
        mPrevSelectedEvent = null;
        mSelectedEvents.clear();

        // The start date is the beginning of the week at 12am
        Time weekStart = new Time(Utils.getTimeZone(mContext, mTZUpdater));
        weekStart.set(mBaseDate);
        weekStart.hour = 0;
        weekStart.minute = 0;
        weekStart.second = 0;
        long millis = weekStart.normalize(true /* ignore isDst */);

        // Avoid reloading events unnecessarily.
        if (millis == mLastReloadMillis) {
            return;
        }
        mLastReloadMillis = millis;

        // load events in the background
//        mContext.startProgressSpinner();
        final ArrayList<Event> events = new ArrayList<Event>();
        mEventLoader.loadEventsInBackground(mNumDays, events, mFirstJulianDay, new Runnable() {

            public void run() {
                boolean fadeinEvents = mFirstJulianDay != mLoadedFirstJulianDay;
//                mEvents.clear();
//                mEvents = events;

                if (mEvents == null) {
                    mEvents = new ArrayList<Event>();
                } else {
                    mEvents.clear();
                }

                // Create a shorter array for all day events
                for (Event e : events) {
                    mEvents.add(e);
                }
                mLoadedFirstJulianDay = mFirstJulianDay;
                if (mAllDayEvents == null) {
                    mAllDayEvents = new ArrayList<Event>();
                } else {
                    mAllDayEvents.clear();
                }

                // Create a shorter array for all day events
                for (Event e : events) {
                    if (e.drawAsAllday()) {
                        mAllDayEvents.add(e);
                    }
                }

                // New events, new layouts
                if (mLayouts == null || mLayouts.length < events.size()) {
                    mLayouts = new StaticLayout[events.size()];
                } else {
                    Arrays.fill(mLayouts, null);
                }

                if (mAllDayLayouts == null || mAllDayLayouts.length < mAllDayEvents.size()) {
                    mAllDayLayouts = new StaticLayout[events.size()];
                } else {
                    Arrays.fill(mAllDayLayouts, null);
                }

                computeEventRelations();

                mRemeasure = true;
                mComputeSelectedEvents = true;
                recalc();

                Time time = new Time();
                time.set(mController.getTime());
                if (AllInOneActivity.viewTypeFlag == 4) return;
                /*mController.sendEvent(this, EventType.GO_TO, mBaseTime, mBaseTime,
                        null, -1, ViewType.CURRENT,
                        CalendarController.EXTRA_GOTO_DATE, null, null);*/   //add by hekeyi for mark event v8.0 ,it brings a bug
                mController.sendEvent(this, EventType.GO_TO, mBaseTime, mBaseTime, -1, ViewType.CURRENT);
                /*if(!mUseHomeTZ){
                    if (AllInOneActivity.viewTypeFlag == 4) return;
                    mController.sendEvent(this, EventType.GO_TO, mBaseTime, mBaseTime,
                            null, -1, ViewType.CURRENT,
                            CalendarController.EXTRA_GOTO_DATE, null, null);   //add by hekeyi for mark event v8.0 ,it brings a bug
                }*/

                /// M: if no events it is unnecessary to do animation
                if (events.isEmpty()) {
                    invalidate();
                    return;
                }
                // Start animation to cross fade the events
                if (fadeinEvents) {
                    if (mEventsCrossFadeAnimation == null) {
                        mEventsCrossFadeAnimation =
                                ObjectAnimator.ofInt(DayHeaderView.this, "EventsAlpha", 0, 255);
                        mEventsCrossFadeAnimation.setDuration(EVENTS_CROSS_FADE_DURATION);
                    }
                    mEventsCrossFadeAnimation.start();
                } else {
                    invalidate();
                }
//                invalidate();
            }

        }, mCancelCallback);
        if (AllInOneActivity.viewTypeFlag == 4) return;

    }


    private void setSelectedEvent(Event e) {
        mSelectedEvent = e;
        mSelectedEventForAccessibility = e;
    }

    private void computeEventRelations() {

        // Compute the layout relation between each event before measuring cell
        // width, as the cell width should be adjusted along with the relation.
        //
        // Examples: A (1:00pm - 1:01pm), B (1:02pm - 2:00pm)
        // We should mark them as "overwapped". Though they are not overwapped logically, but
        // minimum cell height implicitly expands the cell height of A and it should look like
        // (1:00pm - 1:15pm) after the cell height adjustment.

        // Compute the space needed for the all-day events, if any.
        // Make a pass over all the events, and keep track of the maximum
        // number of all-day events in any one day.  Also, keep track of
        // the earliest event in each day.
        int maxAllDayEvents = 0;
        final ArrayList<Event> events = mEvents;
        final int len = events.size();
        // Num of all-day-events on each day.
        final int eventsCount[] = new int[mLastJulianDay - mFirstJulianDay + 1];
        Arrays.fill(eventsCount, 0);
        for (int ii = 0; ii < len; ii++) {
            Event event = events.get(ii);
            if (event.startDay > mLastJulianDay || event.endDay < mFirstJulianDay) {
                continue;
            }
            if (event.drawAsAllday()) {
                // Count all the events being drawn as allDay events
                final int firstDay = Math.max(event.startDay, mFirstJulianDay);
                final int lastDay = Math.min(event.endDay, mLastJulianDay);
                for (int day = firstDay; day <= lastDay; day++) {
                    final int count = ++eventsCount[day - mFirstJulianDay];
                    if (maxAllDayEvents < count) {
                        maxAllDayEvents = count;
                    }
                }

                int daynum = event.startDay - mFirstJulianDay;
                int durationDays = event.endDay - event.startDay + 1;
                if (daynum < 0) {
                    durationDays += daynum;
                    daynum = 0;
                }
                if (daynum + durationDays > mNumDays) {
                    durationDays = mNumDays - daynum;
                }
                for (int day = daynum; durationDays > 0; day++, durationDays--) {
                    if (mHasAllDayEvent != null) {
                        mHasAllDayEvent[day] = true;
                    }
                }
            } else {
                int daynum = event.startDay - mFirstJulianDay;
                int hour = event.startTime / 60;
                if (daynum >= 0 && hour < mEarliestStartHour[daynum]) {
                    mEarliestStartHour[daynum] = hour;
                }

                // Also check the end hour in case the event spans more than
                // one day.
                daynum = event.endDay - mFirstJulianDay;
                hour = event.endTime / 60;
                if (daynum < mNumDays && hour < mEarliestStartHour[daynum]) {
                    mEarliestStartHour[daynum] = hour;
                }
            }
        }
        mMaxAlldayEvents = maxAllDayEvents;
        initAllDayHeights();
    }

    private void recalc() {
        /// M: normalize time, or the weekday/monthday part maybe incorrect @{
        mBaseDate.normalize(true);
        /// @}
        // Set the base date to the beginning of the week if we are displaying
        // 7 days at a time.
        if (mNumDays == 7) {
            adjustToBeginningOfWeek(mBaseDate);
        }

        /// M: use getJulianDayInGeneral to solve the problems which happens before 1970-1-1 @{
        mFirstJulianDay = Utils.getJulianDayInGeneral(mBaseDate, false);
        /// @}
        mLastJulianDay = mFirstJulianDay + mNumDays - 1;

        mMonthLength = mBaseDate.getActualMaximum(Time.MONTH_DAY);
        mFirstVisibleDate = mBaseDate.monthDay;
        mFirstVisibleDayOfWeek = mBaseDate.weekDay;
    }

    public void setEventsAlpha(int alpha) {
        mEventsAlpha = alpha;
        invalidate();
    }

    private final Runnable mCancelCallback = new Runnable() {
        public void run() {
            clearCachedEvents();
        }
    };

    /**
     * Figures out the initial heights for allDay events and space when
     * a view is being set up.
     */
    public void initAllDayHeights() {
        if (mMaxAlldayEvents <= mMaxUnexpandedAlldayEventCount) {
            return;
        }
        if (mShowAllAllDayEvents) {
            int maxADHeight = mViewHeight - DAY_HEADER_HEIGHT - MIN_HOURS_HEIGHT;
            maxADHeight = Math.min(maxADHeight,
                    (int) (mMaxAlldayEvents * MIN_UNEXPANDED_ALLDAY_EVENT_HEIGHT));
            mAnimateDayEventHeight = maxADHeight / mMaxAlldayEvents;
        } else {
            mAnimateDayEventHeight = (int) MIN_UNEXPANDED_ALLDAY_EVENT_HEIGHT;
        }
    }

    private void adjustToBeginningOfWeek(Time time) {
        int dayOfWeek = time.weekDay;
        int diff = dayOfWeek - mFirstDayOfWeek;
        if (diff != 0) {
            if (diff < 0) {
                diff += 7;
            }
            time.monthDay -= diff;
            time.normalize(true /* ignore isDst */);
        }
    }

    public void selectionFocusShow(boolean isFocusShow) {
        mIsSelectionFocusShow = isFocusShow;
    }

    /**
     * return a negative number if "time" is comes before the visible time
     * range, a positive number if "time" is after the visible time range, and 0
     * if it is in the visible time range.
     */
    public int compareToVisibleTimeRange(Time time) {

        int savedHour = mBaseDate.hour;
        int savedMinute = mBaseDate.minute;
        int savedSec = mBaseDate.second;

        mBaseDate.hour = 0;
        mBaseDate.minute = 0;
        mBaseDate.second = 0;


        //add by hekeyi for calendar v8.0
        mController.sendEvent(mContext,
                EventType.UPDATE_AGENDA_SHOW_TIME, time, time, -1,
                ViewType.CURRENT, CalendarController.EXTRA_GOTO_DATE, null,
                null);

        // Compare beginning of range
        int diff = Time.compare(time, mBaseDate);
        if (diff > 0) {
            // Compare end of range
            mBaseDate.monthDay += mNumDays;
            mBaseDate.normalize(true);
            diff = Time.compare(time, mBaseDate);


            mBaseDate.monthDay -= mNumDays;
            mBaseDate.normalize(true);
            if (diff < 0) {
                // in visible time
                diff = 0;
            } else if (diff == 0) {
                // Midnight of following day
                diff = 1;
            }
        }

        mBaseDate.hour = savedHour;
        mBaseDate.minute = savedMinute;
        mBaseDate.second = savedSec;
        return diff;
    }

    public int getFirstVisibleHour() {
        return mFirstHour;
    }

    public void setFirstVisibleHour(int firstHour) {
        mFirstHour = firstHour;
        mFirstHourOffset = 0;
    }

    /**
     * Cleanup the pop-up and timers.
     */
    public void cleanup() {
        // Protect against null-pointer exceptions
        if (mPopup != null) {
            mPopup.dismiss();
        }
        mPaused = true;
        mLastPopupEventID = INVALID_EVENT_ID;
        if (mHandler != null) {
            mHandler.removeCallbacks(mDismissPopup);
            mHandler.removeCallbacks(mUpdateCurrentTime);
        }

        Utils.setSharedPreference(mContext, GeneralPreferences.KEY_DEFAULT_CELL_HEIGHT,
                mCellHeight);
        // Clear all click animations
        eventClickCleanup();
        // Turn off redraw
        mRemeasure = false;
        // Turn off scrolling to make sure the view is in the correct state if we fling back to it
        mScrolling = false;
    }

    class DismissPopup implements Runnable {

        public void run() {
            // Protect against null-pointer exceptions
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    }

    private void eventClickCleanup() {
        this.removeCallbacks(mClearClick);
        this.removeCallbacks(mSetClick);
        mClickedEvent = null;
        mSavedClickedEvent = null;
    }

    // Clears the "clicked" color from the clicked event and launch the event
    private final Runnable mClearClick = new Runnable() {
        @Override
        public void run() {
            if (mClickedEvent != null) {
                mController.sendEventRelatedEvent(this, EventType.VIEW_EVENT, mClickedEvent.id,
                        mClickedEvent.startMillis, mClickedEvent.endMillis,
                        DayHeaderView.this.getWidth() / 2, mClickedYLocation,
                        getSelectedTimeInMillis());
            }
            mClickedEvent = null;
            DayHeaderView.this.invalidate();
        }
    };

    // Sets the "clicked" color from the clicked event
    private final Runnable mSetClick = new Runnable() {
        @Override
        public void run() {
            mClickedEvent = mSavedClickedEvent;
            mSavedClickedEvent = null;
            DayHeaderView.this.invalidate();
        }
    };


    public void stopEventsAnimation() {
        if (mEventsCrossFadeAnimation != null) {
            mEventsCrossFadeAnimation.cancel();
        }
        mEventsAlpha = 255;
    }


    int marked = 0;
    Time oldTime ;
    private void markEvent(Canvas canvas) {
//        Log.d("hekeyi","[DayHeaderView]-markEvent viewTypeFlag = "+AllInOneActivity.viewTypeFlag +"marked = "+marked);
        /*if(AllInOneActivity.viewTypeFlag== ViewType.MONTH){
            return;
        }*/
        oldTime = null;
        if (mEvents.size() != 0 && AllInOneActivity.viewTypeFlag!= ViewType.MONTH /*&& marked!=0*/) {
            for (Event e : mEvents) {
                Time startTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
                startTime.set(e.startMillis);
                if(oldTime!=null){
                    if(oldTime.toMillis(false)==startTime.toMillis(false)) continue;
                }

                oldTime = startTime;
                int dayOff = -1;
                if (mFirstDayOfWeek == 0 || mFirstDayOfWeek == 1) {
                    dayOff = mFirstDayOfWeek;
                } else {
                    dayOff = mFirstDayOfWeek - 7;
                }
                int x1 = (int) (mViewWight * (startTime.weekDay - dayOff + 0.5) / 7);
                int y1 = mViewHeight - (mViewHeight / 2 - mBitmap.getHeight() / 2) + 7;
                canvas.drawCircle(x1, y1, mBitmap.getHeight() / 15, mPaint);
            }
        }
        marked++;
    }

    private void markEvent(Canvas canvas,int day,int month) {
//        Log.d("hekeyi","[DayHeaderView]-markEvent mEvents.size() = "+mEvents.size());
        /*if(AllInOneActivity.viewTypeFlag== ViewType.MONTH){
            return;
        }*/
        oldTime = null;
        if (mEvents.size() != 0 /*&& AllInOneActivity.viewTypeFlag!= ViewType.MONTH && marked!=0*/) {
            for (Event e : mEvents) {
                Time startTime = new Time(Utils.getTimeZone(mContext, mTZUpdater));
                startTime.set(e.startMillis);

//                Log.d("hekeyi","[DayHeaderView]-markEvent day = "+day+" month = "+month);
//                Log.d("hekeyi","[DayHeaderView]-markEvent e.Day = "+startTime.monthDay+" e.month = "+(startTime.month+1));
//                if(day!=startTime.monthDay && month !=(startTime.month+1)) continue;
                if(month==startTime.month+1 && (startTime.monthDay<day || startTime.monthDay>day)) continue;
                if(startTime.month+1<month && startTime.monthDay>day) continue;
                if(startTime.month+1>month && startTime.monthDay<day) continue;
                if(startTime.month+1<month) continue;
                if(oldTime!=null ){
                    if(oldTime.toMillis(false)==startTime.toMillis(false)) continue;
                }

                oldTime = startTime;
                int dayOff = -1;
                if (mFirstDayOfWeek == 0 || mFirstDayOfWeek == 1) {
                    dayOff = mFirstDayOfWeek;
                } else {
                    dayOff = mFirstDayOfWeek - 7;
                }
                int x1 = (int) (mViewWight * (startTime.weekDay - dayOff + 0.5) / 7);
                int y1 = mViewHeight - (mViewHeight / 2 - mBitmap.getHeight() / 2) + 7;
                canvas.drawCircle(x1, y1, mBitmap.getHeight() / 15, mPaint);
            }
        }
        marked++;
    }

    private void drawUndeLine(int x, int y, String lunardate, Canvas canvas) {
        if (isFirstLunarDay(lunardate)) {
            mUnderlinePaint.setStrokeWidth(mUnderLineWidth);
            mUnderLineLength = 30;
//			canvas.drawLine(x - mUnderLineLength/2, y + mCnY*1.8f, x + mUnderLineLength/2, y + mCnY*1.8f, mUnderlinePaint);
            canvas.drawLine(x - mUnderLineLength / 2, y + mCnY * 1.4f, x + mUnderLineLength / 2, y + mCnY * 1.4f, mUnderlinePaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
    }

}
