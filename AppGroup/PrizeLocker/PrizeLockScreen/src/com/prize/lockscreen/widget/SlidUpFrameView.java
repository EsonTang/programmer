package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.ICanTouch;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;
/***
 * 模拟百叶窗解锁最外层视图 (framelayout)
 * @author fanjunchen
 *
 */
public class SlidUpFrameView extends FrameLayout implements IResetView, ICanTouch {

	private final String TAG = "SlidUpFrameView";
	
	private IUnLockListener mUnlockListener;
	
	private Context mCtx;
	
	private VelocityTracker mVelTracker;
	
	private int downX, downY;

	private int mCurY, mCurX;
	
	private int mDelY;
	
	/**触摸是否已经失效*/
	private boolean isTouchDis = false;
	/**背景大图*/
	private ImageView imgPreview;
	/**有多少页**/
	private final int PAGE_NUM = 16;
	
	private ShutterView[] mSmallViews = null;
	
	private int pageHeight, pageNums = PAGE_NUM, rol = 0;
	/**冗余是否大于高度的一半*/
	private boolean hasRol = false;
	/**小叶窗容器*/
	private LinearLayout mContent;
	/**飞机控件*/
	private ImageView imgPlane;
	/**是否为plane控件被按下**/
	private boolean isPlaneDown = false;
	/**飞机所在起始位置*/
	private Rect planeRect = null;
	
	protected TextView mTimeTxt, mDateTxt;
	/**日期格式*/
	protected String mDateFormat;
	
	private View mListView;
	
	private AnimatorSet mAnimSet = null;
	
	public SlidUpFrameView(Context context) {
		this(context, null);
	}

	public SlidUpFrameView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidUpFrameView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public SlidUpFrameView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		mCtx = context;
		initFromContext(mCtx);
		mDateFormat = mCtx.getString(R.string.month_day_year_week);
		init();
	}
	/***
	 * 初始化变量值
	 */
	private void init() {
		
		mScreenHeigh = DisplayUtil.getScreenHeightPixels();
		mScreenWidth = DisplayUtil.getScreenWidthPixels();
		
		pageHeight = mScreenHeigh / PAGE_NUM;
		rol = mScreenHeigh % PAGE_NUM;
		if (rol * 2 > pageHeight) {
			pageNums += 1;
			hasRol = true;
		}
	}
	
	/**
	 * 设置解锁回调
	 * @param l
	 */
	public void setUnlockListener(IUnLockListener l) {
		mUnlockListener = l;
	}

	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		setShutterVisible(false);
		imgPlane.setTranslationY(0);
    	
    	for(ShutterView v : mSmallViews) {
    		if (v != null)
    			v.animToStart();
    	}
	}
	
    /***
     * 让数字盘可见
     * @param pos 进入哪一个应用
     */
    public void setPinVisible(int pos) {
    }
    
    @Override
	public void updateTime() {
		// TODO Auto-generated method
    	if (mTimeTxt != null) {
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (mDateTxt != null) {
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		}
	}
    
    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// 处理添加控件的事情, 且需要为每个控件设备背景
		imgPreview = (ImageView)findViewById(R.id.img_bg);
		imgPlane = (ImageView)findViewById(R.id.img_plane);
		
		mDateTxt = (TextView) findViewById(R.id.text_date);
		mTimeTxt = (TextView) findViewById(R.id.text_time);
		getClockTypeface();
		if (mClockTypeface != null) {
			/*if (mDateTxt != null) {
				mDateTxt.setTypeface(mClockTypeface);
			}*/
			if (mTimeTxt != null) {
				mTimeTxt.setTypeface(mClockTypeface);
			}
		}
		
		mListView = findViewById(R.id.notice_list);
		updateTime();
		
		Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap)
			bitmap = BitmapFactory.decodeResource(mCtx.getResources(),
				R.drawable.default_);
		
		if (imgPreview != null && bitmap != null) {
			imgPreview.setImageBitmap(bitmap);
		}
		
		mContent = (LinearLayout)findViewById(R.id.imgs_content);
		if (mContent != null) {
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT, pageHeight);
			mSmallViews = new ShutterView[pageNums];
			for (int i=0; i<pageNums; i++) {
				Rect rect = new Rect();
				int top = i * pageHeight;
				int bottom = top + pageHeight;
				if (i == pageNums -1) {
					if (hasRol) {
						params = new android.widget.LinearLayout.LayoutParams(
								android.widget.LinearLayout.LayoutParams.MATCH_PARENT, rol);
						bottom = top + rol;
					}
					else if(rol != 0) {
						params = new android.widget.LinearLayout.LayoutParams(
								android.widget.LinearLayout.LayoutParams.MATCH_PARENT, pageHeight + rol);
						bottom = top + pageHeight + rol;
					}
				}
				
				ShutterView v = new ShutterView(mCtx);
				v.setBitmap(bitmap);
				rect.set(0, top, DisplayUtil.getScreenWidthPixels(), bottom);
				v.setSrc(rect);
				mContent.addView(v, params);
				mSmallViews[i] = v;
			}
		}
		
		mHandler.sendEmptyMessage(MSG_PLANE_START);
	}
	
    public void setFontBitmap() {
    	Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap || bitmap.isRecycled())
			bitmap = BitmapFactory.decodeResource(mCtx.getResources(),
				R.drawable.default_);
		
		if (imgPreview != null && bitmap != null) {
			imgPreview.setImageBitmap(bitmap);
		}
		
		for(ShutterView v : mSmallViews) {
    		if (v != null)
    			v.setBitmap(bitmap);
    	}
    }
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!isTouchable)
			return false;
		if (isTouchDis)
			return super.onTouchEvent(event);
		
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (null == planeRect) {
				planeRect = new Rect();
				int[] loc = new int[2];
				imgPlane.getLocationInWindow(loc);
				planeRect.set(loc[0], loc[1], loc[0] + imgPlane.getWidth(), loc[1] + imgPlane.getHeight());
			}
			downX = (int)event.getX();
			downY = (int)event.getY();
			
			if (!planeRect.contains(downX, downY)) {
				isPlaneDown = false;
				return super.onTouchEvent(event);
			}
			
			isPlaneDown = true;
			if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			setShutterVisible(true);
			return true;
		case MotionEvent.ACTION_MOVE:
			if (isPlaneDown) {
				mVelTracker.addMovement(event);
				mCurY = (int) event.getY();
				mCurX = (int) event.getX();
				exeAnim(mCurY);
			}
			break;
		case MotionEvent.ACTION_UP:
			
			if (isPlaneDown) {
				
				mVelTracker.computeCurrentVelocity(300);
				float vY = mVelTracker.getYVelocity();
				mCurY = (int) event.getY();
//				float dis = (float)getSplineFlingDistance((int)Math.abs(vY));
				LogUtil.i(TAG, "===DYvelocity==" + vY);
				if (calAndMoveTo(vY)) {
					releaseTrack();
					return true;
				}
				
				mDelY = mCurY - downY;
				if (mDelY <= 0 && mCurY < mScreenHeigh/2) {
					// 执行往上飞的动作
					verticalRun(new int[]{mCurY, 0}, 500);
				}
				else {
					// 执行下降动作
					verticalRun(new int[]{mCurY, mScreenHeigh}, 500);
				}
			}
			isPlaneDown = false;
			releaseTrack();
			break;
		case MotionEvent.ACTION_CANCEL:
			resetView();
			isTouchDis = false;
			isPlaneDown = false;
			releaseTrack();
			break;
		}
		return super.onTouchEvent(event);
	}
	
	/***
	 * 释放速度计算类
	 */
	protected void releaseTrack() {
		if (mVelTracker != null) {
			mVelTracker.clear();
			mVelTracker.recycle();
			mVelTracker = null;
		}
	}
	/**
	 * 垂直滑落或向上滑
	 * @param from 开始位置 
	 * @param to 结束位置
	 */
	private void verticalRun(final int[] ds, int duration)
	{
		isTouchDis = true;
		ValueAnimator animator = ValueAnimator.ofInt(ds);
		animator.addUpdateListener(new AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int f = (int)animation.getAnimatedValue();
				exeAnim(f);
			}
		});
		
		animator.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				isTouchDis = false;
				if (ds[ds.length - 1] != 0)
					setShutterVisible(false);
				else if (mUnlockListener != null && !mUnlockListener.checkPwd(1))
					mUnlockListener.onUnlockFinish();
			}
			@Override
			public void onAnimationCancel(Animator animation) {
				
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
				
			}
		});
		//animator.setTarget(imgPlane);
		animator.setDuration(duration).start();
	}
	
	/***
	 * 设置几个小图片的可见性
	 * @param isVisible
	 */
	private void setShutterVisible(boolean isVisible) {
		if (isVisible) {
			if (mContent != null) {
				mContent.setVisibility(View.VISIBLE);
				imgPreview.setVisibility(View.GONE);
			}
			if (mDateTxt != null)
				mDateTxt.setVisibility(View.GONE);
			if (mTimeTxt != null)
				mTimeTxt.setVisibility(View.GONE);
			if (mListView != null)
				mListView.setVisibility(View.GONE);
			
			mHandler.sendEmptyMessage(MSG_PLANE_STOP);
		}
		else {
			if (mContent != null) {
				mContent.setVisibility(View.GONE);
				imgPreview.setVisibility(View.VISIBLE);
			}
			if (mDateTxt != null)
				mDateTxt.setVisibility(View.VISIBLE);
			if (mTimeTxt != null)
				mTimeTxt.setVisibility(View.VISIBLE);
			if (mListView != null)
				mListView.setVisibility(View.VISIBLE);
			
			mHandler.sendEmptyMessage(MSG_PLANE_START);
		}
	}
	
	/***
	 * 计算时间及距离并滑动到目标
	 * @param velocity
	 * @return
	 */
	private boolean calAndMoveTo(float velocity) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocity));
		int duration = getSplineFlingDuration((int)Math.abs(velocity));
		
		boolean rs = false;
		
		int startY = mCurY;
		
		int[] ss;
		
		if (velocity < -100) { // 往上
			rs = true;
			int aim = (int)(startY - dis);
			if (aim < mScreenHeigh/2) {
				ss = new int[2];
				ss[0] = startY;
				ss[1] = 0;
				duration = (int)(500 * startY / (0.0 + mScreenHeigh/2));
			}
			else {
				ss = new int[3];
				ss[0] = startY;
				ss[1] = aim;
				ss[2] = mScreenHeigh;
				duration = (int)(500 * 2 * (mScreenHeigh - startY + dis) / (0.0 + mScreenHeigh/2));
			}
			if (duration < 100)
				duration = 100;
			verticalRun(ss, duration);
		}
		else if (velocity > 100) {// 往下
			rs = true;
			int aim = (int)(startY + dis);
			if (aim > mScreenHeigh/2) {
				ss = new int[2];
				ss[0] = startY;
				ss[1] = mScreenHeigh;
				duration = (int)(500 * (mScreenHeigh - startY) / (0.0 + mScreenHeigh/2));
			}
			else {
				ss = new int[3];
				ss[0] = startY;
				ss[1] = aim;
				ss[2] = 0;
				duration = (int)(500 * 2 * (startY + dis) / (0.0 + mScreenHeigh/2));
			}
			if (duration < 100)
				duration = 100;
			verticalRun(ss, duration);
		}
		return rs;
	}
	
	/***
	 * 初始化加速度因子
	 * @param context
	 */
	protected static void initFromContext(Context context) {
        final float ppi = context.getResources().getDisplayMetrics().density * 160.0f;
        PHYSICAL_COEF = SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * ppi
                * 0.84f; // look and feel tuning
    }
	/***
	 * 滑动的加速度
	 * @param velocity
	 * @return
	 */
	protected double getSplineDeceleration(int velocity) {
        return Math.log(INFLEXION * Math.abs(velocity) / (mFlingFriction * PHYSICAL_COEF));
    }
	/***
	 * 初速度计算滑行距离
	 * @param velocity
	 * @return
	 */
	protected double getSplineFlingDistance(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return mFlingFriction * PHYSICAL_COEF * Math.exp(DECELERATION_RATE / decelMinusOne * l);
    }

	/***
	 * 初速度计算滑行时间， 以毫秒为单位
	 * @param velocity
	 * @return
	 */
	protected int getSplineFlingDuration(int velocity) {
        final double l = getSplineDeceleration(velocity);
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        return (int) (1000.0 * Math.exp(l / decelMinusOne));
    }
	
	/**重力加速度有关， 是为减速的*/
	private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
	private static float PHYSICAL_COEF;
	private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
	/**Fling friction*/
    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    
    /**高度与宽度*/
    protected int mScreenHeigh = 0, mScreenWidth = 0;
    
    /***
     * 执行百叶窗翻转动作
     * @param y 坐标
     */
    private void exeAnim(int y) {
    	if (null == mSmallViews || mSmallViews.length < 1)
    		return;
    	
    	int dy = y - downY;
    	if (dy > 0)
    		dy = 0;
    	imgPlane.setTranslationY(dy);
    	
    	for(ShutterView v : mSmallViews) {
    		Rect r = v.getSrc();
    		if (v.getVisibility() == View.INVISIBLE && (dy == 0 || r.bottom < y)) {
    			v.animToStart();
    		}
    		else if (r.bottom > y) {
    			v.animToEnd();
    		}
    	}
    }
    
    private void setPlaneAnim() {
    	if (imgPlane == null)
    		return;
    	if(null == mAnimSet) {
    		mAnimSet = new AnimatorSet();
	    	
	    	ObjectAnimator a = ObjectAnimator.ofFloat(imgPlane, "translationY", 0f, 40f, 0f, -40f);
	    	a.setRepeatMode(Animation.REVERSE);
	    	a.setRepeatCount(Animation.INFINITE);
	    	
	    	ObjectAnimator b = ObjectAnimator.ofFloat(imgPlane, "alpha", 1f, 0.5f, 1f, .5f);
	    	b.setRepeatMode(Animation.REVERSE);
	    	b.setRepeatCount(Animation.INFINITE);
	    	
	    	mAnimSet.setDuration(2500);
	    	mAnimSet.playTogether(a, b);
    	}
    	mAnimSet.start();
    }
    
    private void planeStopAnim() {
    	if (imgPlane == null)
    		return;
    	mAnimSet.cancel();
    }
    
    private final int MSG_PLANE_START = 22;
    
    private final int MSG_PLANE_STOP = MSG_PLANE_START + 1;
    /***
     * 在同一线程中处理事件
     */
    private Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MSG_PLANE_START:
    			setPlaneAnim();
    			break;
    		case MSG_PLANE_STOP:
    			planeStopAnim();
    			break;
    		}
    	}
    };
    
    public int getLockType() {
    	return LockConfigBean.FLY_LOCK_TYPE;
    }
    
    private boolean isTouchable = true;
	@Override
	public void setTouchable(boolean isTouch) {
		isTouchable = isTouch;
	}
	
	private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";
    private static Typeface mClockTypeface = null;
    /***
     * 时间字体
     * @return
     */
	private Typeface getClockTypeface() {
        if (mClockTypeface == null) {
            mClockTypeface = Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE);
        }
        return mClockTypeface;
    }
}
