package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.ICanTouch;
import com.prize.lockscreen.interfaces.IEnterApp;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;

/**
 * 移动类似眨眼 解锁view (RelativeLayout)
 * @author fanjunchen
 * <br>created 2015-08-28
 */
public class BlinkRelativeView extends BaseRootView implements IResetView, ICanTouch {

	private final static String TAG = "BlinkRelativeView";

	private int mLastY = 0, mLastX = 0;

	private int mCurY, mCurX;

	private int mDelY;
	/**哪个控件被按下, 1 home, 2 phone, 3 camera, 0 表示无*/
	private int whichDown = 0;
	/**触摸是否已经失效*/
	private boolean isTouchDis = false;
	/**几个可以移动的大图*/
	private ImageView imgHome, imgHomeTop, imgCamera, imgCameraTop, imgPhone, imgPhoneTop;
	/**点击区域*/
	private Rect homeRect, cameraRect, phoneRect;
	/**几个象征意义的小图*/
	private ImageView icoHome, icoCamera, icoPhone;
	/**左右三角形的顶角角度*/
	private int leftCorner, rightCorner;
	/**三角函数计算出来的tan 左右的数值*/
	private double tanLeft, tanRight;
	/**电话多边形能往上移动的最大高度  上移为负值, 下移为正值*/
	private int phoneMaxTranslateY = 0;
	/**主页多边形能往上移动的最大高度  上移为负值, 下移为正值*/
	private int homeMaxTranslateY = 0;
	/**相机多边形能往上移动的最大高度  上移为负值, 下移为正值*/
	private int cameraMaxTranslateY = 0;
	/**哪个控件被move, 1 home, 2 phone, 3 camera, 0 表示无*/
	private int whichState = 0;
	/**是否已经处于解锁**/
	private boolean isUnlock = false;
	/**控件移动的高度 负值表示往上移动了这么高, 正值表示往下移动了这么高**/
	private int tmpTranlateY = 0;
	/**用来处理进入到哪个应用的接口*/
	private IEnterApp iEnter;
	/**左右中间露出三角形的高度**/
	private int leftTrainHeight, rightTrainHeight, midTrainHeight;
	
	/**上部控件在动画执行结束时还仍有露出的高度**/
	private int topRyHeight;
	
	private AnimatorSet mAnimateSet = null;
	
	private AnimatorSet mMoveSet = null;
	
	public BlinkRelativeView(Context context) {
		this(context, null);
	}
	
	public BlinkRelativeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public BlinkRelativeView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}
	/***
	 * 设置进入到哪个APP
	 * @param enter
	 */
	public void setEnterApp(IEnterApp enter) {
		iEnter = enter;
	}
	/***
	 * 初始化值
	 */
	private void init() {
		
		Resources res = mContext.getResources();
		leftCorner = res.getInteger(R.integer.left_corner);
		rightCorner = res.getInteger(R.integer.right_corner);
		
		topRyHeight = res.getDimensionPixelSize(R.dimen.top_ry_height);
		
		tanLeft = Math.tan(leftCorner * Math.PI/180);
		tanRight = Math.tan(rightCorner * Math.PI/180);
		
		calLeftRect(res);
		
		calRightRect(res);
		
		calMidRect(res);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
			if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			judgeWhichDown(event.getX(), event.getY());
			
			if (whichDown != 0) {
				mVelTracker.addMovement(event);
				mLastY = (int)event.getY();
				setIconVisible(false);
				setPoloyVisible(whichDown);
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (whichDown != 0) {
				mVelTracker.addMovement(event);
				mVelTracker.computeCurrentVelocity(500);
				mCurY = (int) event.getY();
				mCurX = (int) event.getX();
				moveViews(mCurX, mCurY);
			}
			break;
		case MotionEvent.ACTION_UP:
			mVelTracker.addMovement(event);
			float vY = mVelTracker.getYVelocity();
			
			LogUtil.i(TAG, "===Yvelocity==" + vY);
			if (calAndMoveTo(vY)) {
				releaseTrack();
				break;
			}
			if (whichState != 0 && isUnlock) {
				if (iEnter != null)
					iEnter.enterApp(whichState);
				if (mUnlockListener != null)
					mUnlockListener.onUnlockFinish();
				
				whichState = 0;
				isUnlock = false;
				releaseTrack();
				break;
			}
			
			mCurY = (int) event.getY();
			mDelY = mCurY - mLastY;
			if (mDelY <= 0 && whichDown != 0) {
				resetViews(whichDown, calTranslateY(whichDown, mCurY));
			}
			mLastY = 0;
			whichDown = 0;
			releaseTrack();
			break;
		case MotionEvent.ACTION_CANCEL:
			mDelY = mCurY - mLastY;
			if (mDelY <= 0 && whichDown != 0) {
				resetViews(whichDown, calTranslateY(whichDown, mCurY));
			}
			mLastY = 0;
			whichDown = 0;
			releaseTrack();
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
	/***
	 * 处理移动
	 * @param x
	 * @param y
	 */
	private void moveViews(int x, int y) {
		LogUtil.i(TAG, "====yyy===" + y);
		switch (whichDown) {
		case 1:
			// imgHome.scrollTo(0, -y);
			// int moveH = -(homeRect.top - y);
			int moveH = -(mLastY - y);
			if (moveH < homeMaxTranslateY) {
				moveH = homeMaxTranslateY;
			}
			if (moveH == homeMaxTranslateY) {
				isUnlock = true;
			}
			else {
				isUnlock = false;
			}
			whichState = 1;
			imgHome.setTranslationY(moveH);
			imgHomeTop.setTranslationY(-moveH);
			break;
		case 2:
			//moveH = -(phoneRect.top - y);
			whichState = 2;
			moveH = -(mLastY - y);
			if (moveH < phoneMaxTranslateY) {
				moveH = phoneMaxTranslateY;
			}
			if (moveH == phoneMaxTranslateY) {
				isUnlock = true;
			}
			else {
				isUnlock = false;
			}
			imgPhone.setTranslationY(moveH);
			imgPhoneTop.setTranslationY(-moveH);
			//imgPhone.scrollTo(0, -y);
			break;
		case 3:
			whichState = 3;
			//moveH = -(cameraRect.top - y);
			moveH = -(mLastY - y);
			if (moveH < cameraMaxTranslateY) {
				moveH = cameraMaxTranslateY;
			}
			if (moveH == cameraMaxTranslateY) {
				isUnlock = true;
			}
			else {
				isUnlock = false;
			}
			imgCamera.setTranslationY(moveH);
			imgCameraTop.setTranslationY(-moveH);
			//imgCamera.scrollTo(0, -y);
			break;
		}
	}
	/***
	 * 计算移动距离
	 * @param which
	 * @param y
	 * @return 返回相对往上移动的距离
	 */
	private int calTranslateY (int which, int y) {
		int a = 0;
		switch (which) {
			case 1:
				a = -(homeRect.top - y);
				if (a < homeMaxTranslateY) {
					a = homeMaxTranslateY;
				}
				break;
			case 2:
				a = -(phoneRect.top - y);
				if (a < phoneMaxTranslateY) {
					a = phoneMaxTranslateY;
				}
				break;
			case 3:
				a = -(cameraRect.top - y);
				if (a < cameraMaxTranslateY) {
					a = cameraMaxTranslateY;
				}
				break;
		}
		return a;
	}
	/***
	 * 若移动不成功则回来原来的位置
	 * @param which 哪一个控件
	 * @param y 移动的距离
	 */
	private void resetViews(int which, int y) {
		
		int a = 0, b = 0, c = 0;
		isTouchDis = true;
		switch (which) {
		case 1:
			a = y;
			mAnimateSet = new AnimatorSet();
			mAnimateSet.playSequentially(ObjectAnimator.ofFloat(imgHome, "translationY", a, 0f, 100, 0), ObjectAnimator.ofFloat(imgCamera, "translationY", c, 0f, 100, 0),
					ObjectAnimator.ofFloat(imgPhone, "translationY", b, 0f, 100, 0));
			
			mAnimateSet.playTogether(ObjectAnimator.ofFloat(imgHomeTop, "translationY", -a, - midTrainHeight - topRyHeight));
			break;
		case 2:
			b = y;
			mAnimateSet = new AnimatorSet();
			mAnimateSet.playSequentially(ObjectAnimator.ofFloat(imgPhone, "translationY", b, 0f, 100, 0), ObjectAnimator.ofFloat(imgHome, "translationY", a, 0f, 100, 0), 
					ObjectAnimator.ofFloat(imgCamera, "translationY", c, 0f, 100, 0));
			mAnimateSet.playTogether(ObjectAnimator.ofFloat(imgPhoneTop, "translationY", -b, - leftTrainHeight - topRyHeight));
			break;
		case 3:
			c = y;
			mAnimateSet = new AnimatorSet();
			mAnimateSet.playSequentially(ObjectAnimator.ofFloat(imgCamera, "translationY", c, 0f, 100, 0), ObjectAnimator.ofFloat(imgHome, "translationY", a, 0f, 100, 0), 
					ObjectAnimator.ofFloat(imgPhone, "translationY", b, 0f, 100, 0));
			mAnimateSet.playTogether(ObjectAnimator.ofFloat(imgCameraTop, "translationY", -c , - rightTrainHeight - topRyHeight));
			break;
		case 0:
			isTouchDis = false;
			return;
		}
		imgPhone.setVisibility(View.VISIBLE);
		imgHome.setVisibility(View.VISIBLE);
		imgCamera.setVisibility(View.VISIBLE);
		//设置动画时间
		mAnimateSet.setDuration(1000);
		mAnimateSet.addListener(mAnimatorResetListener);
		mAnimateSet.start();
		mAnimateSet = null;
	}
	/***
	 * 计算时间及距离并移动
	 * @param velocity
	 * @return
	 */
	private boolean calAndMoveTo(float velocity) {
		// 能移动的距离
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocity));
		
		if (dis < 1)
			return false;
		isTouchDis = true;
		// 移动以上距离所需要的时间
		int duration = getSplineFlingDuration((int)Math.abs(velocity));
		
		if (duration <100)
			duration = 100;
		else if (duration > 800) 
			duration = 800;
		
		boolean rs = false;
		
		int a = 0, b = 0, c = 0;
		
		int startY = 0;
		switch (whichDown) {
		case 1:
			startY = (int)imgHome.getTranslationY();
			a = (int)(startY - dis);
			if (a < homeMaxTranslateY) {
				a = homeMaxTranslateY;
			}
			
			if (a == homeMaxTranslateY) {
				isUnlock = true;
			}
			mMoveSet = null;
			mMoveSet = new AnimatorSet();
			whichState = 1;
			tmpTranlateY = a;
			ObjectAnimator anim = ObjectAnimator.ofFloat(imgHome, "translationY", startY, a);
			rs = true;
			
			mMoveSet.playTogether(anim, ObjectAnimator.ofFloat(imgHomeTop, "translationY", imgHomeTop.getTranslationY(), -a));
			mMoveSet.setDuration(duration);
			mMoveSet.addListener(mAnimatorListener);
			mMoveSet.start();
			break;
		case 2:
			startY = (int)imgPhone.getTranslationY();
			b = (int)(startY - dis);
			if (b < phoneMaxTranslateY) {
				b = phoneMaxTranslateY;
			}
			if (b == phoneMaxTranslateY) {
				isUnlock = true;
			}
			whichState = 2;
			tmpTranlateY = b;
			
			rs = true;
			anim = ObjectAnimator.ofFloat(imgPhone, "translationY", startY, b);
			mMoveSet = null;
			mMoveSet = new AnimatorSet();
			mMoveSet.playTogether(anim, ObjectAnimator.ofFloat(imgPhoneTop, "translationY", imgPhoneTop.getTranslationY(), -b));
			mMoveSet.setDuration(duration);
			mMoveSet.addListener(mAnimatorListener);
			mMoveSet.start();
			break;
		case 3:
			startY = (int)imgCamera.getTranslationY();
			c = (int)(startY - dis);
			if (c < cameraMaxTranslateY) {
				c = cameraMaxTranslateY;
			}
			
			if (c == cameraMaxTranslateY) {
				isUnlock = true;
			}
			whichState = 3;
			tmpTranlateY = c;
			anim = ObjectAnimator.ofFloat(imgCamera, "translationY", startY, c);
			mMoveSet = null;
			mMoveSet = new AnimatorSet();
			mMoveSet.playTogether(anim, ObjectAnimator.ofFloat(imgCameraTop, "translationY", imgCameraTop.getTranslationY(), -c));
			mMoveSet.setDuration(duration);
			mMoveSet.addListener(mAnimatorListener);
			mMoveSet.start();
			rs = true;
			break;
		case 0:
			rs = false;
			isTouchDis = false;
			break;
		}
		return rs;
	}
    /**cal and move 动画监听*/
	private AnimatorListener mAnimatorListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			if (whichState != 0 && isUnlock) {
//				if (iEnter != null)
//					iEnter.enterApp(whichState);
				if (mUnlockListener != null && !mUnlockListener.checkPwd(whichState))
					mUnlockListener.onUnlockFinish();
			}
			else {
				resetViews(whichState, tmpTranlateY);
			}
			isTouchDis = false;
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
			isUnlock = false;
			whichState = 0;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
	};
	/**回弹动画 */
	private AnimatorListener mAnimatorResetListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// TODO Auto-generated method stub
			setIconVisible(true);
			setTopVisible(false);
			isTouchDis = false;
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
	};
	
	@Override
	public void computeScroll() {
		super.computeScroll();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (null == mBitmap || mBitmap.isRecycled())
			setBgBitmap(LockScreenApplication.getBgImg());
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}
	
	private final static int MSG_PRODUCE = 0X0001;
	private final static int MSG_MOVE = MSG_PRODUCE + 1;
	/**没有移动但是为按下未抬起状态**/
	private final static int MSG_NO_MOV = MSG_MOVE + 1;
	/**可以触摸**/
	private final static int MSG_CAN_TOUCH = MSG_NO_MOV + 1;
	
	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_CAN_TOUCH:
				isTouchDis = false;
				break;
				default:
					break;
			}
		}
	};
	
	@Override
	protected void finalize() {
		mHandler = null;
		try {
			super.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected int getLockType() {
		return LockConfigBean.CLOSE_LOCK_TYPE;
	}
	@Override
	public void resetView() {
		
		whichState = 0;
		isUnlock = false;
		tmpTranlateY = 0;
		isTouchDis = false;
		
//		setAlpha(1f);
//		setVisibility(View.VISIBLE);
		
		imgCamera.setTranslationY(0);
		imgCameraTop.setTranslationY(0);
		
		imgHome.setTranslationY(0);
		imgHomeTop.setTranslationY(0);
		
		imgPhone.setTranslationY(0);
		imgPhoneTop.setTranslationY(0);
		
		imgHomeTop.setVisibility(View.INVISIBLE);
		imgHome.setVisibility(View.VISIBLE);
		
		imgPhoneTop.setVisibility(View.INVISIBLE);
		imgPhone.setVisibility(View.VISIBLE);
		
		imgCameraTop.setVisibility(View.INVISIBLE);
		imgCamera.setVisibility(View.VISIBLE);
		
		setIconVisible(true);
	}
	
	@Override
	public void updateTime() {
		// TODO Auto-generated method stub mDateTxt
		if (mTimeTxt != null) {
			mTimeTxt.setText(TimeUtil.getCurrentTime());
		}
		if (mDateTxt != null) {
			mDateTxt.setText(DateFormat.format(mDateFormat, TimeUtil.getTime()));
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
	 * 设置上部分图片的可见性
	 * @param isVisible
	 */
	private void setTopVisible(boolean isVisible) {
		if (isVisible) {
			imgHomeTop.setVisibility(View.VISIBLE);
			imgCameraTop.setVisibility(View.VISIBLE);
			imgPhoneTop.setVisibility(View.VISIBLE);
		}
		else {
			imgHomeTop.setVisibility(View.INVISIBLE);
			imgCameraTop.setVisibility(View.INVISIBLE);
			imgPhoneTop.setVisibility(View.INVISIBLE);
		}
	}
	/***
	 * 设置多边形的可见性
	 * @param which
	 */
	private void setPoloyVisible(int which) {
		switch (which) {
			case 1:
				imgHomeTop.setVisibility(View.VISIBLE);
				imgHome.setVisibility(View.VISIBLE);
				
				imgPhoneTop.setVisibility(View.INVISIBLE);
				imgPhone.setVisibility(View.INVISIBLE);
				
				imgCameraTop.setVisibility(View.INVISIBLE);
				imgCamera.setVisibility(View.INVISIBLE);
				break;
			case 2:
				imgHomeTop.setVisibility(View.INVISIBLE);
				imgHome.setVisibility(View.INVISIBLE);
				
				imgPhoneTop.setVisibility(View.VISIBLE);
				imgPhone.setVisibility(View.VISIBLE);
				
				imgCameraTop.setVisibility(View.INVISIBLE);
				imgCamera.setVisibility(View.INVISIBLE);
				break;
			case 3:
				imgHomeTop.setVisibility(View.INVISIBLE);
				imgHome.setVisibility(View.INVISIBLE);
				
				imgPhoneTop.setVisibility(View.INVISIBLE);
				imgPhone.setVisibility(View.INVISIBLE);
				
				imgCameraTop.setVisibility(View.VISIBLE);
				imgCamera.setVisibility(View.VISIBLE);
				break;
			case 0:
				imgHomeTop.setVisibility(View.INVISIBLE);
				imgHome.setVisibility(View.VISIBLE);
				
				imgPhoneTop.setVisibility(View.INVISIBLE);
				imgPhone.setVisibility(View.VISIBLE);
				
				imgCameraTop.setVisibility(View.INVISIBLE);
				imgCamera.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		imgHome = (ImageView)findViewById(R.id.img_home); 
		imgHomeTop = (ImageView)findViewById(R.id.img_home_top);
		imgCamera = (ImageView)findViewById(R.id.img_camera);
		imgCameraTop = (ImageView)findViewById(R.id.img_camera_top);
		imgPhone = (ImageView)findViewById(R.id.img_phone);
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
	/***
	 * 计算左边多边形的布局
	 * @param res
	 */
	private void calLeftRect(Resources res ) {
		phoneMaxTranslateY = res.getDimensionPixelSize(R.dimen.phone_margin_bottom);
		// 露出三角形的高度
		leftTrainHeight = getMulSlidHeight(res.getDimensionPixelSize(R.dimen.left_train_height)) + phoneMaxTranslateY;
		// 下边长
		int s = (int)(leftTrainHeight * tanLeft);
		phoneRect = new Rect();
		
		phoneRect.left = 0;
		phoneRect.right = s;
		phoneRect.top = DisplayUtil.getScreenHeightPixels() - leftTrainHeight;
		phoneRect.bottom = DisplayUtil.getScreenHeightPixels();
	}
	/***
	 * 计算右边多边形的布局
	 * @param res
	 */
	private void calRightRect(Resources res ) {
		cameraMaxTranslateY = res.getDimensionPixelSize(R.dimen.camera_margin_bottom);
		// 露出三角形的高度
		rightTrainHeight = getMulSlidHeight(res.getDimensionPixelSize(R.dimen.right_train_height)) + cameraMaxTranslateY;
		int s = (int)(rightTrainHeight * tanRight);
		
		cameraRect = new Rect();
		
		cameraRect.left = DisplayUtil.getScreenWidthPixels() - s;
		cameraRect.right = DisplayUtil.getScreenWidthPixels();
		cameraRect.top = DisplayUtil.getScreenHeightPixels() - rightTrainHeight;
		cameraRect.bottom = DisplayUtil.getScreenHeightPixels();
	}
	/***
	 * 计算中间多边形的布局
	 * @param res
	 */
	private void calMidRect(Resources res ) {
		homeMaxTranslateY = res.getDimensionPixelSize(R.dimen.home_margin_bottom);
		// 露出三角形的高度
		midTrainHeight = getMulSlidHeight(0) + homeMaxTranslateY;
		
		homeRect = new Rect();
		
		homeRect.left = 0;
		homeRect.right = DisplayUtil.getScreenWidthPixels();
		homeRect.top = DisplayUtil.getScreenHeightPixels() - midTrainHeight;
		homeRect.bottom = DisplayUtil.getScreenHeightPixels();
	}
	
	private boolean isTouchable = true;
	@Override
	public void setTouchable(boolean isTouch) {
		isTouchable = isTouch;
	}
}
