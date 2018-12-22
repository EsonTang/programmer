package com.prize.lockscreen.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.interfaces.LockClickListener;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.prizelockscreen.R;
/***
 * 模拟眨眼解锁最外层视图 (framelayout)
 * @author fanjunchen
 *
 */
public class BlinkFrameLayout extends FrameLayout implements IResetView {

	private final String TAG = "BlinkFrameLayout";
	
	private IUnLockListener mUnlockListener;
	
	private LockClickListener mClickListener;
	
	private Context mCtx;
	
	private VelocityTracker mVelTracker;
	
	private int mLastY = 0;

	private int mCurY, mCurX;
	
	private int mDelY;
	
	/**哪个控件被按下, 1 home, 2 phone, 3 camera, 0 表示无*/
	private int whichDown = 0;
	/**触摸是否已经失效*/
	private boolean isTouchDis = false;
	/**几个可以移动的大图*/
	private ImageView imgHome, imgHomeTop, imgCamera, imgCameraTop, imgPhoe, imgPhoneTop;
	/**点击区域*/
	private Rect homeRect, cameraRect, phoneRect;
	/**几个象征意义的小图*/
	private ImageView icoHome, icoCamera, icoPhone;
	
	private int leftCorner, rightCorner;
	
	private double tanLeft, tanRight;
	
	public BlinkFrameLayout(Context context) {
		this(context, null);
	}

	public BlinkFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BlinkFrameLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public BlinkFrameLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		mCtx = context;
		
		init();
	}

	private void init() {
		
		Resources res = mCtx.getResources();
		leftCorner = res.getInteger(R.integer.left_corner);
		rightCorner = res.getInteger(R.integer.right_corner);
		
		tanLeft = Math.tan(leftCorner*Math.PI/180);
		tanRight = Math.tan(rightCorner*Math.PI/180);
		
		calLeftRect(res);
		
		calRightRect(res);
		
		calMidRect(res);
		
	}
	
	public void setLockClickListener (LockClickListener l) {
		mClickListener = l;
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
	}
	
	/***
	 * 解锁完成后进入到哪个APP
	 * @param pos
	 */
	private void enterApp(int pos) {
		switch (pos) {
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
		mCtx.startActivity(intent);
	}
    
    //启动拨号应用
    private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mCtx.startActivity(intent);
	}
    
    //启动相机应用
    private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mCtx.startActivity(intent);
	}
    /***
     * 让数字盘可见
     * @param pos 进入哪一个应用
     */
    public void setPinVisible(int pos) {
    }
    
    @Override
	public void updateTime() {
		// TODO Auto-generated method stub mDateTxt
	}
    
    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		imgHome = (ImageView)findViewById(R.id.img_home); 
		imgHomeTop = (ImageView)findViewById(R.id.img_home_top);
		imgCamera = (ImageView)findViewById(R.id.img_camera);
		imgCameraTop = (ImageView)findViewById(R.id.img_camera_top);
		imgPhoe = (ImageView)findViewById(R.id.img_phone);
		imgPhoneTop = (ImageView)findViewById(R.id.img_phone_top);
		
		icoHome = (ImageView)findViewById(R.id.ico_home);
		icoCamera = (ImageView)findViewById(R.id.ico_camera);
		icoPhone = (ImageView)findViewById(R.id.ico_phone);
	}
	/***
	 * 获取多边形的高度
	 * @param trainHeight
	 * @return
	 */
	private int getMulSlidHeight(int trainHeight) {
		int h = (DisplayUtil.getScreenHeightPixels() + trainHeight) / 2;
		return h;
	}
	
	private void calLeftRect(Resources res ) {
		int bottom = res.getDimensionPixelSize(R.dimen.phone_margin_bottom);
		// 露出三角形的高度
		int h = getMulSlidHeight(res.getDimensionPixelSize(R.dimen.left_train_height)) + bottom;
		
		int s = (int)(h * tanLeft);
		phoneRect = new Rect();
		
		phoneRect.left = 0;
		phoneRect.right = s;
		phoneRect.top = DisplayUtil.getScreenHeightPixels() - h;
		phoneRect.bottom = DisplayUtil.getScreenHeightPixels();
	}
	
	private void calRightRect(Resources res ) {
		int bottom = res.getDimensionPixelSize(R.dimen.camera_margin_bottom);
		// 露出三角形的高度
		int h = getMulSlidHeight(res.getDimensionPixelSize(R.dimen.right_train_height)) + bottom;
		
		int s = (int)(h * tanRight);
		cameraRect = new Rect();
		
		cameraRect.left = DisplayUtil.getScreenWidthPixels() - s;
		cameraRect.right = DisplayUtil.getScreenWidthPixels();
		cameraRect.top = DisplayUtil.getScreenHeightPixels() - h;
		cameraRect.bottom = DisplayUtil.getScreenHeightPixels();
	}
	
	private void calMidRect(Resources res ) {
		int bottom = res.getDimensionPixelSize(R.dimen.home_margin_bottom);
		// 露出三角形的高度
		int h = getMulSlidHeight(0) + bottom;
		
		homeRect = new Rect();
		
		homeRect.left = 0;
		homeRect.right = DisplayUtil.getScreenWidthPixels();
		homeRect.top = DisplayUtil.getScreenHeightPixels() - h;
		homeRect.bottom = DisplayUtil.getScreenHeightPixels();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (isTouchDis)
			return super.onTouchEvent(event);
		
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			judgeWhichDown(event.getX(), event.getY());
			
			if (whichDown != 0) {
				setIconVisible(false);
				mVelTracker.addMovement(event);
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (whichDown != 0)
				mVelTracker.addMovement(event);
			mCurY = (int) event.getY();
			mCurX = (int) event.getX();
			
			moveViews(mCurX, mCurY);
			break;
		case MotionEvent.ACTION_UP:
			float vY = mVelTracker.getYVelocity();
			
			LogUtil.i(TAG, "===Yvelocity==" + vY);
			if (calAndMoveTo(vY))
				break;
			mCurY = (int) event.getY();
			mDelY = mCurY - mLastY;
			if (mDelY <= 0) {
			}
			whichDown = 0;
			break;
		case MotionEvent.ACTION_CANCEL:
			whichDown = 0;
			break;
		}
		return super.onTouchEvent(event);
	}
	
	/***
	 * 判断点击在哪个view身上
	 * @param x
	 * @param y
	 */
	private void judgeWhichDown(float x, float y) {
		if (phoneRect.contains((int)x, (int)y) && DisplayUtil.getScreenHeightPixels() - y > 0 
				&& tanLeft < (phoneRect.width() - x) / (DisplayUtil.getScreenHeightPixels() - y)) {
			whichDown = 2;
		}
		else if (cameraRect.contains((int)x, (int)y) && DisplayUtil.getScreenHeightPixels() - y > 0 
				&& tanRight < (cameraRect.width() - DisplayUtil.getScreenWidthPixels() + x) / (DisplayUtil.getScreenHeightPixels() - y)) {
			whichDown = 3;
		}
		else if (homeRect.contains((int)x, (int)y)) {
			whichDown = 1;
		}
		else
			whichDown = 0;
	}
	
	private void moveViews(int x, int y) {
		LogUtil.i("aaa", "====yyy===" + y);
		switch (whichDown) {
		case 1:
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)imgHome.getLayoutParams();
			lp.bottomMargin -= 1; 
			break;
		case 2:
			// imgPhoe.scrollTo(0, y);
			lp = (FrameLayout.LayoutParams)imgPhoe.getLayoutParams();
			lp.bottomMargin -= 1; 
			imgPhoe.setLayoutParams(lp);
			break;
		case 3:
			imgCamera.scrollTo(0, y);
			break;
		}
	}
	
	/***
	 * 设置几个小图片的可见性
	 * @param isVisible
	 */
	private void setIconVisible(boolean isVisible) {
		if (isVisible) {
			icoHome.setVisibility(View.VISIBLE);
			icoCamera.setVisibility(View.VISIBLE);
			icoPhone.setVisibility(View.VISIBLE);
		}
		else {
			icoHome.setVisibility(View.GONE);
			icoCamera.setVisibility(View.GONE);
			icoPhone.setVisibility(View.GONE);
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
		
		int startY = getScrollY();
		if (velocity < 0) {
			int aim = (int)(startY + dis);
			if (aim > mScreenHeigh || aim > mScreenHeigh/2 + 20) {
				duration = (int)((duration * (mScreenHeigh - startY)) / dis);
			}
			if (aim > (mScreenHeigh >> 1) + 20 
					&& duration < 1000) {
			}
		}
		else {
			int aim = (int)(startY - dis);
			if (aim < 0 || aim < mScreenHeigh/2 - 20) {
				duration = (int)((duration * startY) / dis);
				if (duration < 220)
					duration = 220;
				else if (duration > 1200)
					duration = 1200;
				rs = true;
			}
		}
		return rs;
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
}
