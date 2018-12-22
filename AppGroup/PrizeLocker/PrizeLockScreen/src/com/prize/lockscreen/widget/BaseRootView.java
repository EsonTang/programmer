package com.prize.lockscreen.widget;

import com.prize.ext.res.LockConfigBean;
import com.prize.ext.res.ResHelper;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.ColorHelper;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
/***
 * 所有布局的root view基类, 提供了时间与日期的显示位置控制<br>
 * 需要注意的是:时间与日期控件均为TextView且id默认分别为R.id.text_time 和 R.id.text_date，
 * <br>还有一个是滑动提示的TextView控件，id默认为R.id.tv_hint
 * @author fanjunchen
 *
 */
public abstract class BaseRootView extends RelativeLayout {

	/**重力加速度有关， 是为减速的*/
	private static final float INFLEXION = 0.35f; // Tension lines cross at (INFLEXION, 1)
	private static float PHYSICAL_COEF;
	private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));
	/**Fling friction*/
    private float mFlingFriction = ViewConfiguration.getScrollFriction();
    
    protected Context mContext;
    /**高度与宽度*/
    protected int mScreenHeigh = 0, mScreenWidth = 0;
    /**取颜色类*/
	protected static ColorHelper mColorHelper;
	/**背景图片*/
	protected Bitmap mBitmap;
	/**辅助滑动对象*/
	protected Scroller mScroller;
	/**辅助速度处理对象*/
	protected VelocityTracker mVelTracker;
	/**可配置的几个控件ID存放位置*/
	private int[] changeIds = null;
	/**解锁回调*/
	protected IUnLockListener mUnlockListener;
	/**要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA*/
	protected int mPos = 0;
	
	protected TextView mTimeTxt, mDateTxt;
	/**日期格式*/
	protected String mDateFormat;
    
	public void setUnlockListener(IUnLockListener listener) {
		mUnlockListener = listener;
	}
	
	public BaseRootView(Context context) {
		this(context, null, 0, 0);
	}

	public BaseRootView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public BaseRootView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public BaseRootView(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		mContext = context;
		mDateFormat = mContext.getString(R.string.month_day_year_week);
		initFromContext(context);
		setupView();
		changeIds = getChangeIds();
		if (null == changeIds) {
			changeIds = new int[] {
				R.id.text_time	
				, R.id.text_date
				, R.id.tv_hint
			};
		}
	}
	/***
	 * 得到可配置控件的id,是在布局文件中的id;<br>
	 * 若是默认的则可以不设置或设置为null.
	 * <br>该方法不可显示调用即在子类中不需要调用,
	 * <br>该方法返回的数据为int[3]长度的数据，index=0时为时间控件id,
	 * <br>index=1时为日期控件id,index=2时为滑动提示控件id。
	 * @return
	 */
	protected int[] getChangeIds() {
		return null;
	}
	/***
	 * 得到控件支持的锁屏类型
	 * @return
	 */
	protected abstract int getLockType();
	
	@SuppressLint("NewApi")
	protected void setupView() {
		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (mContext
				.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenHeigh = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		// 这里你一定要设置成透明背景,不然会影响你看到底层布局
		// this.setBackgroundColor(Color.argb(0, 0, 0, 0));
		mColorHelper = ColorHelper.getIntance(mContext);
		
		/*Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap)
			bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.default_background);
		setBackground(bitmap);*/
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
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		if (null == mTimeTxt) {
			mTimeTxt = (TextView)findViewById(changeIds[0]);
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (null == mDateTxt) {
			mDateTxt = (TextView)findViewById(changeIds[1]);
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
		}
		
		getClockTypeface();
		if (mClockTypeface != null) {
			/*if (mDateTxt != null) {
				mDateTxt.setTypeface(mClockTypeface);
			}*/
			if (mTimeTxt != null) {
				mTimeTxt.setTypeface(mClockTypeface);
			}
		}
	}
	
	private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";
    private static Typeface mClockTypeface = null;
    
	private Typeface getClockTypeface() {
        if (mClockTypeface == null) {
            mClockTypeface = Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE);
        }
        return mClockTypeface;
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
    
	/***
	 * 把图片缩放到与本View同样大小
	 * @param bitmap
	 */
	public void setBgBitmap(Bitmap bitmap){
		mBitmap = bitmap;
		mColorHelper.setBitmap(mBitmap);
//		BitmapDrawable d = new BitmapDrawable(mContext.getResources(), mBitmap);
//		setBackground(d);
	}
	
	public Bitmap getBgBitmap() {
		return mBitmap;
	}

	@Override
	protected void onLayout(boolean isChanged, int l, int t, int r, int b) {
		
		LockConfigBean bean = ResHelper.getInstance(mContext).getLockConfig();
		if (bean != null && bean.lockType == getLockType()) {
		
			mTimeTxt = (TextView)findViewById(changeIds[0]);
			if (mTimeTxt != null) {
				mTimeTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, bean.timeSz);
				mTimeTxt.setTextColor(Color.parseColor(bean.timeColor));
			}
			
			mDateTxt = (TextView)findViewById(changeIds[1]);
			if (mDateTxt != null) {
				mDateTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, bean.dateSz);
				mDateTxt.setTextColor(Color.parseColor(bean.dateColor));
			}
			
			TextView tmp = (TextView)findViewById(changeIds[2]);
			if (tmp != null && !TextUtils.isEmpty(bean.hint)) 
				tmp.setText(bean.hint);
		}
		super.onLayout(isChanged, l, t, r, b);
		
		if (bean != null && bean.lockType == getLockType()) {
			
			if (mTimeTxt != null) {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mTimeTxt.getLayoutParams();
				if (bean.timeX != -1) {
					params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
					params.leftMargin = bean.timeX;
					params.topMargin = bean.timeY;
				}
			}
			
			mDateTxt = (TextView)findViewById(changeIds[1]);
			if (mDateTxt != null) {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mDateTxt.getLayoutParams();
				if (bean.timeX != -1) {
					params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
					params.leftMargin = bean.timeX;
					params.topMargin = 0;//bean.timeY;
				}
			}
		}
	}
	
	@Override
	protected void finalize() {
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private AnimatorSet mAnimateSet;
	
	private AnimatorListener mAnimatorListener= new AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animator) {
		}
	
		@Override
		public void onAnimationRepeat(Animator animator) {
		}
	
		@Override
		public void onAnimationEnd(Animator animator) {
			setVisibility(View.GONE);
			if (mUnlockListener != null && !mUnlockListener.checkPwd(mPos)) {
				enterApp();
				mUnlockListener.onUnlockFinish();
			}
		}
	
		@Override
		public void onAnimationCancel(Animator animator) {
		}
	};
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
	/***
	 * 向上移动动画，动画结束后退出应用
	 * @param pos 要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA
	 */
	protected void moveUp(int pos) {
		mPos = pos;
		mAnimateSet = new AnimatorSet();
		//设置动画
		mAnimateSet.playTogether(
	
		 ObjectAnimator.ofFloat(this, "alpha", 1, 0.5f),
		 // ObjectAnimator.ofFloat(this, "translationX", 0f,400f,0f),
		 ObjectAnimator.ofFloat(this, "translationY", 0f, -mScreenHeigh)
		 //ObjectAnimator.ofFloat(this, "rotation", 0,180,360)
		);
	
		//设置动画时间
		mAnimateSet.setDuration(500);
		//设置动画监听
		mAnimateSet.addListener(mAnimatorListener);
		
		mAnimateSet.start();
	}
	
	/***
	 * 向下移动动画，动画结束后退出应用
	 * @param pos 要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA, 无 传0即可
	 */
	protected void moveDown(int pos) {
		mPos = pos;
		mAnimateSet = new AnimatorSet();
		//设置动画
		mAnimateSet.playTogether(
	
		 ObjectAnimator.ofFloat(this, "alpha", 1, 0.5f),
		 // ObjectAnimator.ofFloat(this, "translationX", 0f,400f,0f),
		 ObjectAnimator.ofFloat(this, "translationY", 0f, mScreenHeigh)
		 //ObjectAnimator.ofFloat(this, "rotation", 0,180,360)
		);
	
		//设置动画时间
		mAnimateSet.setDuration(500);
		//设置动画监听
		mAnimateSet.addListener(mAnimatorListener);
		
		mAnimateSet.start();
	}
	
	/***
	 * 向右移动动画，动画结束后退出应用
	 * @param pos 要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA, 无 传0即可
	 */
	protected void moveRight(int pos) {
		mPos = pos;
		mAnimateSet = new AnimatorSet();
		//设置动画
		mAnimateSet.playTogether(
	
		 ObjectAnimator.ofFloat(this, "alpha", 1, 0.5f),
		 // ObjectAnimator.ofFloat(this, "translationX", 0f,400f,0f),
		 ObjectAnimator.ofFloat(this, "translationX", 0f, mScreenWidth)
		 //ObjectAnimator.ofFloat(this, "rotation", 0,180,360)
		);
	
		//设置动画时间
		mAnimateSet.setDuration(320);
		//设置动画监听
		mAnimateSet.addListener(mAnimatorListener);
		
		mAnimateSet.start();
	}
	
	/***
	 * 向左移动动画，动画结束后退出应用
	 * @param pos 要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA, 无 传0即可, 无 传0即可
	 */
	protected void moveLeft(int pos) {
		mPos = pos;
		mAnimateSet = new AnimatorSet();
		//设置动画
		mAnimateSet.playTogether(
	
		 ObjectAnimator.ofFloat(this, "alpha", 1, 0.5f),
		 // ObjectAnimator.ofFloat(this, "translationX", 0f,400f,0f),
		 ObjectAnimator.ofFloat(this, "translationX", 0f, - mScreenWidth)
		 //ObjectAnimator.ofFloat(this, "rotation", 0,180,360)
		);
	
		//设置动画时间
		mAnimateSet.setDuration(320);
		//设置动画监听
		mAnimateSet.addListener(mAnimatorListener);
		
		mAnimateSet.start();
	}
	
	/***
	 * 慢慢变透明消失，动画结束后退出应用
	 */
	protected void transparent() {
		
//		mAnimateSet = new AnimatorSet();
//		//设置动画
//		mAnimateSet.playTogether(
//		 ObjectAnimator.ofFloat(this, "alpha", 1, 0.0f)
//		);
	
		ObjectAnimator a = ObjectAnimator.ofFloat(this, "alpha", 1, 0.0f);
		a.addListener(mAnimatorListener);
		a.setDuration(1200);
		a.start();
//		//设置动画时间
//		mAnimateSet.setDuration(1200);
//		//设置动画监听
//		mAnimateSet.addListener(mAnimatorListener);
//		
//		mAnimateSet.start();
	}
	/***
	 * 解锁完成后进入到哪个APP
	 * @param pos
	 */
	private void enterApp() {
		switch (mPos) {
		case 1:
			launchSms();
			break;
		case 2:
			launchDial();
			break;
		case 3:
			launchCamera();
			break;
		}
		mPos = 0;
	}
	
	//启动短信应用
    private void launchSms() {

		//mFocusView.setVisibility(View.GONE);
		Intent intent = new Intent();
		ComponentName comp = new ComponentName("com.android.mms",
				"com.android.mms.ui.ConversationList");
		intent.setComponent(comp);
		intent.setAction("android.intent.action.VIEW");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}
    
    //启动拨号应用
    private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}
    
    //启动相机应用
    private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}
}
