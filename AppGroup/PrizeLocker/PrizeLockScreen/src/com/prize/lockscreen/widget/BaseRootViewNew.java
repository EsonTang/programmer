package com.prize.lockscreen.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.SensorManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.ext.res.LockConfigBean;
import com.prize.ext.res.ResHelper;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.ColorHelper;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;
/***
 * 支持左右上下滑动的根视图
 * @author fanjunchen
 *
 */
public abstract class BaseRootViewNew extends FrameLayout implements IResetView {

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
	/**辅助速度处理对象*/
	protected VelocityTracker mVelTracker;
	/**可配置的几个控件ID存放位置*/
	private int[] changeIds = null;
	/**解锁回调*/
	protected IUnLockListener mUnlockListener;
	/**要进入哪一个应用 1 SMS， 2 PHONE， 3 CAMERA*/
	protected int mPos = 0;
	/**时间及日期控件*/
	protected TextView mTimeTxt, mDateTxt;
	/**日期格式*/
	protected String mDateFormat;
	/**内容View**/
	protected View contentView;
	/**pin码输入的View**/
	protected LinearLayout pinView;
	/**按下的X,Y坐标*/
	protected int downX, downY;
	
	public BaseRootViewNew(Context context) {
		this(context, null);
	}

	public BaseRootViewNew(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BaseRootViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public BaseRootViewNew(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
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

	protected void setupView() {
		// 获取屏幕分辨率
		WindowManager wm = (WindowManager) (mContext
				.getSystemService(Context.WINDOW_SERVICE));
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenHeigh = dm.heightPixels;
		mScreenWidth = dm.widthPixels;
		// 这里你一定要设置成透明背景,不然会影响你看到底层布局
		this.setBackgroundColor(Color.argb(0, 0, 0, 0));
		mColorHelper = ColorHelper.getIntance(mContext);
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
		
		pinView = (LinearLayout)findViewById(R.id.pin_lay);
		contentView = findViewById(R.id.content_view);
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
	 * 得到控件支持的锁屏类型
	 * @return
	 */
	protected abstract int getLockType();
	/***
	 * 设置内容View是否可以触摸
	 * @param isTouch
	 */
	public abstract void setContentViewTouchable(boolean isTouch);
	/***
	 * 设置背景根据是否有密码来确定<br>
	 * 调用这个方法前先需要调用 {@link #setUnlockListener(IUnLockListener)}
	 */
	public void setBgBitmap() {
		Bitmap bitmap = LockScreenApplication.getBgImg();
		if (null == bitmap || bitmap.isRecycled())
			bitmap = BitmapFactory.decodeResource(mContext.getResources(),
				R.drawable.default_);
		
		mBitmap = bitmap;
		if (mUnlockListener != null) {
			boolean b = mUnlockListener.hasPwd();
			if (b && !(contentView instanceof SlidUpFrameView)) {
				setBackground(new BitmapDrawable(mContext.getResources(), mBitmap));
				if (contentView instanceof PullRightDoorView) {
					((PullRightDoorView)contentView).setBgBitmap(null);
				}
			}
			else if (contentView != null && !(contentView instanceof SlidUpFrameView)) {
				if (!b && (contentView instanceof PullRightDoorView)) {
					((PullRightDoorView)contentView).setBgBitmap(mBitmap);
					setBackground(null);
				}
				else
					contentView.setBackground(new BitmapDrawable(mContext.getResources(), mBitmap));
			}
			else if (contentView != null && (contentView instanceof SlidUpFrameView)) {
				((SlidUpFrameView)contentView).setFontBitmap();
			}
			if (contentView instanceof ColorBubbleView)
				((BaseRootView)contentView).setBgBitmap(mBitmap);
		}
	}
	/***
	 * 设置解锁回调
	 * @param listener
	 */
	public void setUnlockListener(IUnLockListener listener) {
		mUnlockListener = listener;
	}
	
	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		pinView.setVisibility(View.GONE);
		contentView.setVisibility(VISIBLE);
	}
	
	public void setPos(int p) {
		mPos = p;
	}
	
	public int getPos() {
		return mPos;
	}
}
