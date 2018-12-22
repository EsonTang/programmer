package com.prize.lockscreen.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.prize.lockscreen.BootActivity;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.prizelockscreen.R;

/***
 * 圆圈解锁中心控件
 * 
 * @author fanjunchen
 * 
 */
public class StarLockView extends ViewGroup {

	private static final boolean DBG = false;
	private Context mContext;
	private Handler mainHandler = null;
	private float mx;
	private float my;
//	private int count = 0;
//	private long firstClick = 0;
//	private long secondClick = 0;

	private int mWidth, mHight;

	private int mScreenHalfWidth;

	private int mAlphaViewWidth, mAlphaViewHeight;

	private int mCenterViewWidth, mCenterViewHeight;

	private int mCenterViewTop, mCenterViewBottom;

	private int mAlphaViewTop, mAlphaViewBottom;

	private int mSmsViewHalfWidth, mSmsViewHalfHeight;

	private ImageView mDialView, mCameraView, mUnLockView; // mSmsView,
	private ImageView mCenterView, mAlphaView;
	private ImageView mUnLockLightView, mCameraLightView, mDialLightView; // mSmsLightView,

	private Rect dialRect, cameraRect, unlockRect; // smsRect,
	private Rect mCenterViewRect = new Rect();

	private AnimatorSet alpha;
	private boolean mTracking = false;

	private static final String TAG = "FxLockView";
	public static final String SHOW_MUSIC = "com.phicomm.hu.action.music";

	/** 中心图标移动的半径 */
	private int moveRadius = 0;

	private IUnLockListener mUnlockListener;

	public void setUnlockListener(IUnLockListener l) {
		mUnlockListener = l;
	}

	public StarLockView(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		mContext = context;
		if (DBG)
			Log.d(TAG, "FxLockView2");
		// connectMediaService();
		initViews(context);
		setViewId();
		// setViewOnClick();
		centerAnimationStart();
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		if (changed) {
			mWidth = r - l;
			mHight = b - t;
			// //mHalfWidth >> 1为向右位移1位，相当于mHalfWidth / 2;
			mScreenHalfWidth = mWidth >> 1;

			getViewMeasure();
			mCenterViewTop = 4 * mHight / 7 - (mCenterViewHeight >> 1);
			mCenterViewBottom = mCenterViewTop + mCenterViewHeight;
			mAlphaViewTop = 4 * mHight / 7 - (mAlphaViewHeight >> 1);
			mAlphaViewBottom = 4 * mHight / 7 + (mAlphaViewHeight >> 1);

			mCenterViewRect.left = mWidth / 2 - mCenterViewWidth / 2;
			mCenterViewRect.top = mCenterViewTop;
			mCenterViewRect.right = mCenterViewRect.left + mCenterViewWidth;
			mCenterViewRect.bottom = mCenterViewBottom;

			setChildViewLayout();
			getChildViewRect();
			setActivatedViewLayout();
		}
	}

	// 获取各个图标的宽、高
	private void getViewMeasure() {
		mAlphaView.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		mAlphaViewWidth = mAlphaView.getMeasuredWidth();
		mAlphaViewHeight = mAlphaView.getMeasuredHeight();

		mCenterView.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		mCenterViewWidth = mCenterView.getMeasuredWidth();
		mCenterViewHeight = mCenterView.getMeasuredHeight();

		/*
		 * mSmsView.measure(View.MeasureSpec.makeMeasureSpec(0,
		 * View.MeasureSpec.UNSPECIFIED), View.MeasureSpec .makeMeasureSpec(0,
		 * View.MeasureSpec.UNSPECIFIED)); mSmsViewHalfWidth =
		 * (mSmsView.getMeasuredWidth()) >> 1; mSmsViewHalfHeight =
		 * (mSmsView.getMeasuredHeight()) >> 1;
		 */

		mDialView.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
		mSmsViewHalfWidth = (mDialView.getMeasuredWidth()) >> 1;
		mCameraView.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		mUnLockView.measure(View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
				.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

		/*
		 * mSmsLightView.measure(View.MeasureSpec.makeMeasureSpec(0,
		 * View.MeasureSpec.UNSPECIFIED), View.MeasureSpec .makeMeasureSpec(0,
		 * View.MeasureSpec.UNSPECIFIED));
		 */
	}

	// 设置高亮图标的布局
	private void setActivatedViewLayout() {

		// mSmsLightView.layout(smsRect.left, smsRect.top, smsRect.right,
		// smsRect.bottom);

		mUnLockLightView.layout(unlockRect.left, unlockRect.top,
				unlockRect.right, unlockRect.bottom);

		mDialLightView.layout(dialRect.left, dialRect.top, dialRect.right,
				dialRect.bottom);

		mCameraLightView.layout(cameraRect.left, cameraRect.top,
				cameraRect.right, cameraRect.bottom);
	}

	// 设置各图标在FxLockView中的布局
	private void setChildViewLayout() {
		// mCenterView.layout(mScreenHalfWidth - mCenterViewWidth / 2,
		// mCenterViewTop,
		// mScreenHalfWidth + mCenterViewWidth / 2, mCenterViewBottom);
		mCenterView.layout(mCenterViewRect.left, mCenterViewRect.top,
				mCenterViewRect.right, mCenterViewRect.bottom);

		int cx = mCenterViewRect.centerX();
		int cy = mCenterViewRect.centerY();

		int aLeft = cx - mAlphaViewWidth / 2;
		int aTop = cy - mAlphaViewHeight / 2;
		mAlphaView.layout(aLeft, aTop, aLeft + mAlphaViewWidth, aTop
				+ mAlphaViewHeight);

		int centerX = cx > mHight / 2 ? mHight / 2 : cx;

		int distance = Math
				.abs(centerX - getPaddingRight() - mSmsViewHalfWidth);
		moveRadius = distance;

		// layoutChild(mSmsView, distance, getPaddingRight(), 3);
		layoutChild(mDialView, distance, getPaddingLeft(), 1);
		layoutChild(mCameraView, distance, getPaddingTop(), 2);
		layoutChild(mUnLockView, distance, getPaddingBottom(), 4);

	}

	/***
	 * 布局某一子控件 fanjunchen
	 * 
	 * @param childView
	 * @param distance
	 *            与中心控件的距离
	 * @param padding
	 * @param direct
	 *            1 左， 2右， 3 上， 4下
	 */
	private void layoutChild(View childView, int distance, int padding,
			int direct) {

		int left = 0, top = 0, right = 0, bottom = 0;

		switch (direct) {
		case 1:
			left = mCenterViewRect.centerX()
					- (distance + childView.getMeasuredWidth() / 2);
			top = mCenterViewRect.centerY() - childView.getMeasuredHeight() / 2;
			break;
		case 2:
			left = mCenterViewRect.centerX() + distance
					- childView.getMeasuredWidth() / 2;
			top = mCenterViewRect.centerY() - childView.getMeasuredHeight() / 2;
			break;
		case 3:
			top = mCenterViewRect.centerY() - distance
					- childView.getMeasuredHeight() / 2;
			left = mCenterViewRect.centerX() - childView.getMeasuredWidth() / 2;
			break;
		case 4:
			top = mCenterViewRect.centerY() + distance
					- childView.getMeasuredHeight() / 2;
			left = mCenterViewRect.centerX() - childView.getMeasuredWidth() / 2;
			break;
		}

		right = left + childView.getMeasuredWidth();
		bottom = top + childView.getMeasuredHeight();

		if (bottom > mHight) {
			bottom = mHight;
			top = bottom - childView.getMeasuredHeight();
		}

		childView.layout(left, top, right, bottom);
	}

	// 创建各图标位置对应的Rect
	private void getChildViewRect() {
		/*
		 * smsRect = new Rect(); mSmsView.getHitRect(smsRect);
		 */

		dialRect = new Rect();
		mDialView.getHitRect(dialRect);

		cameraRect = new Rect();
		mCameraView.getHitRect(cameraRect);

		unlockRect = new Rect();
		mUnLockView.getHitRect(unlockRect);

	}

	// 获取图标，将获取的图标添加入FxLockView，设置图标的可见性
	private void initViews(Context context) {
		mAlphaView = new ImageView(context);
		mAlphaView.setImageResource(R.drawable.more_circle);
		setViewsLayout(mAlphaView);
		mAlphaView.setVisibility(View.INVISIBLE);

		mCenterView = new ImageView(context);
		mCenterView.setImageResource(R.drawable.centure1);
		setViewsLayout(mCenterView);
		mCenterView.setVisibility(View.VISIBLE);

		/*
		 * mSmsView = new ImageView(context);
		 * mSmsView.setImageResource(R.drawable.sms); setViewsLayout(mSmsView);
		 * mSmsView.setVisibility(View.VISIBLE);
		 */

		mDialView = new ImageView(context);
		mDialView.setImageResource(R.drawable.dial);
		setViewsLayout(mDialView);
		mDialView.setVisibility(View.INVISIBLE);

		mCameraView = new ImageView(context);
		mCameraView.setImageResource(R.drawable.camera);
		setViewsLayout(mCameraView);
		mCameraView.setVisibility(View.INVISIBLE);

		mUnLockView = new ImageView(context);
		mUnLockView.setImageResource(R.drawable.home);
		setViewsLayout(mUnLockView);
		mUnLockView.setVisibility(View.VISIBLE);

		/*
		 * mSmsLightView= new ImageView(context);
		 * setLightDrawable(mSmsLightView); setViewsLayout(mSmsLightView);
		 * mSmsLightView.setVisibility(INVISIBLE);
		 */

		mUnLockLightView = new ImageView(context);
		setLightDrawable(mUnLockLightView);
		setViewsLayout(mUnLockLightView);
		mUnLockLightView.setVisibility(INVISIBLE);

		mCameraLightView = new ImageView(context);
		setLightDrawable(mCameraLightView);
		setViewsLayout(mCameraLightView);
		mCameraLightView.setVisibility(INVISIBLE);

		mDialLightView = new ImageView(context);
		setLightDrawable(mDialLightView);
		setViewsLayout(mDialLightView);
		mDialLightView.setVisibility(INVISIBLE);
	}

	private void setLightDrawable(ImageView img) {
		img.setImageResource(R.drawable.light);
	}

	// 设置获取图标的参数，并添加到FxLockView
	private void setViewsLayout(ImageView image) {
		image.setScaleType(ScaleType.CENTER);
		image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		addView(image);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// 手指点在中心图标范围区域内
			if (mCenterViewRect.contains((int) x, (int) y)) {
				mTracking = true;
				// onAnimationEnd();
				mAlphaView.setVisibility(View.INVISIBLE);
				return true;
			}
			break;

		default:
			break;
		}
		if (DBG)
			Log.d(TAG, "onInterceptTouchEvent()");
		// 此处返回false，onClick事件才能监听的到
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*
		 * mTracking为true时，说明中心图标被点击移动 即只有在中心图标被点击移动的情况下，onTouchEvent 事件才会触发。
		 */
		if (mTracking) {
			final int action = event.getAction();
			final float nx = event.getX();
			final float ny = event.getY();

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				setAimVisible(true);
				break;
			case MotionEvent.ACTION_MOVE:
				setTargetViewVisible(nx, ny);
				// 中心图标移动
				handleMoveView(nx, ny);
				break;
			case MotionEvent.ACTION_UP:
				mTracking = false;
				doTriggerEvent(mx, my);
				resetMoveView();
				setAimVisible(false);
				break;
			case MotionEvent.ACTION_CANCEL:
				mTracking = false;
				doTriggerEvent(mx, my);
				resetMoveView();
				setAimVisible(false);
				break;
			}
		}
		if (DBG)
			Log.d(TAG, "onTouchEvent()");
		return mTracking || super.onTouchEvent(event);
	}

	// 平方和计算
	private float dist2(float dx, float dy) {
		return dx * dx + dy * dy;
	}

	private void handleMoveView(float x, float y) {

		int mHalfCenterViewWidth = mCenterViewWidth >> 1;

		/*
		 * 若用户手指移动的点与中心点的距离长度大于Radius，则中心图标坐标位置限定在移动区域范围圆弧上。
		 * 一般是用户拖动中心图标，手指移动到限定圆范围区域外。
		 */
		if (Math.sqrt(dist2(x - mScreenHalfWidth, y
				- (mCenterView.getTop() + mCenterViewWidth / 2))) > moveRadius) {
			// 原理为x1 / x = r1 / r
			x = (float) ((moveRadius / (Math.sqrt(dist2(x - mScreenHalfWidth, y
					- (mCenterView.getTop() + mHalfCenterViewWidth)))))
					* (x - mScreenHalfWidth) + mScreenHalfWidth);

			y = (float) ((moveRadius / (Math.sqrt(dist2(x - mScreenHalfWidth, y
					- (mCenterView.getTop() + mHalfCenterViewWidth)))))
					* (y - (mCenterView.getTop() + mHalfCenterViewWidth))
					+ mCenterView.getTop() + mHalfCenterViewWidth);
		}

		mx = x;
		my = y;
		/*
		 * 图形的坐标是以左上角为基准的， 所以，为了使手指所在的坐标和图标的中心位置一致， 中心坐标要减去宽度和高度的一半。
		 */
		mCenterView.setX((int) x - mCenterView.getWidth() / 2);
		mCenterView.setY((int) y - mCenterView.getHeight() / 2);
		showLightView(x, y);
		invalidate();
	}

	// 监听解锁、启动拨号、相机、短信应用
	private void doTriggerEvent(float a, float b) {
		/*
		 * if (smsRect.contains((int)a, (int) b)) { //stopViewAnimation();
		 * onAnimationEnd(); //setTargetViewInvisible(mSmsView); virbate();
		 * //发送消息到MainActivity类中的mHandler出来
		 * mainHandler.obtainMessage(BootActivity
		 * .MSG_LAUNCH_SMS).sendToTarget(); } else
		 */
		if (dialRect.contains((int) a, (int) b)) {
			// onAnimationEnd();
			// setTargetViewInvisible(mDialView);
			AnimationUtil.virbate(mContext);
			if (mainHandler != null)
				mainHandler.obtainMessage(BootActivity.MSG_LAUNCH_DIAL)
						.sendToTarget();
			//else
			//	launchDial();
			if (mUnlockListener != null && !mUnlockListener.checkPwd(2))
				mUnlockListener.onUnlockFinish();
		} else if (cameraRect.contains((int) a, (int) b)) {
			// onAnimationEnd();
			// setTargetViewInvisible(mCameraView);
			AnimationUtil.virbate(mContext);
			if (mainHandler != null)
				mainHandler.obtainMessage(BootActivity.MSG_LAUNCH_CAMERA)
						.sendToTarget();
			//else
			//	launchCamera();
			if (mUnlockListener != null && !mUnlockListener.checkPwd(3))
				mUnlockListener.onUnlockFinish();
		} else if (unlockRect.contains((int) a, (int) b)) {
			// onAnimationEnd();
			// setTargetViewInvisible(mUnLockView);
			AnimationUtil.virbate(mContext);
			if (mainHandler != null)
				mainHandler.obtainMessage(BootActivity.MSG_LAUNCH_HOME)
						.sendToTarget();
			if (mUnlockListener != null && !mUnlockListener.checkPwd(0))
				mUnlockListener.onUnlockFinish();
		}
		mx = my = -1;
	}

	// 中心图标拖动到指定区域时显示高亮图标
	private void showLightView(float a, float b) {
		if (unlockRect.contains((int) a, (int) b)) {
			setLightVisible(mUnLockLightView);
		}
		/*
		 * else if (smsRect.contains((int)a, (int) b)) {
		 * setLightVisible(mSmsLightView); }
		 */
		else if (dialRect.contains((int) a, (int) b)) {
			setLightVisible(mDialLightView);
		} else if (cameraRect.contains((int) a, (int) b)) {
			setLightVisible(mCameraLightView);
		} else {
			setLightInvisible();
		}
	}

	private void setLightVisible(ImageView view) {
		view.setVisibility(View.VISIBLE);
		mCenterView.setVisibility(View.INVISIBLE);
	}

	// 隐藏高亮图标
	private void setLightInvisible() {
		final View mActivatedViews[] = { mUnLockLightView, // mSmsLightView,
				mDialLightView, mCameraLightView };
		for (View view : mActivatedViews) {
			view.setVisibility(View.INVISIBLE);
		}

		mCenterView.setVisibility(View.VISIBLE);
	}

	private void setTargetViewInvisible(ImageView img) {
		img.setVisibility(View.INVISIBLE);
	}

	private void setTargetViewVisible(float x, float y) {
		/*
		 * if(Math.sqrt(dist2(x - mScreenHalfWidth, y - (mCenterView.getTop() +
		 * mCenterViewWidth / 2) )) > mAlphaViewHeight / 4) { return; }
		 */
	}

	private void setTargetViewVisible() {
		if (DBG)
			Log.d(TAG, "setTargetViewVisible()");
		final View mTargetViews[] = { // mSmsView,
		mDialView, mUnLockView, mCameraView };
		for (View view : mTargetViews) {
			view.setVisibility(View.VISIBLE);
		}
	}

	/***
	 * 设置可到达的view可见性
	 */
	private void setAimVisible(boolean isVisible) {
		final View mTargetViews[] = { // mSmsView,
		mDialView, mCameraView };
		for (View view : mTargetViews) {
			if (isVisible)
				view.setVisibility(View.VISIBLE);
			else
				view.setVisibility(View.INVISIBLE);
		}
	}

	// 重置中心图标，回到原位置
	private void resetMoveView() {
		mCenterView.setX(mWidth / 2 - mCenterViewWidth / 2);
		mCenterView.setY((mCenterView.getTop() + mCenterViewHeight / 2)
				- mCenterViewHeight / 2);
		mCenterView.setVisibility(View.VISIBLE);
		// onAnimationStart();
		centerAnimationStart();
		invalidate();
	}

	public void setMainHandler(Handler handler) {
		mainHandler = handler;
	}

	private void setViewId() {
		if (DBG)
			Log.d(TAG, "setViewId()");
	}

	// 停止中心图标动画
	@Override
	protected void onAnimationEnd() {
		// TODO Auto-generated method stub
		super.onAnimationEnd();
		if (alpha != null) {
			alpha = null;
		}
		mAlphaView.setAnimation(null);
	}

	// 显示中心图标动画
	@Override
	protected void onAnimationStart() {
		// TODO Auto-generated method stub
		super.onAnimationStart();
		mAlphaView.setVisibility(View.VISIBLE);

		if (alpha == null) {

			alpha = new AnimatorSet();
			ObjectAnimator a = ObjectAnimator.ofFloat(mAlphaView, "alpha",
					0.0f, 1.0f);
			ObjectAnimator sx = ObjectAnimator.ofFloat(mAlphaView, "scaleX",
					0.5f, 1.2f);
			ObjectAnimator sy = ObjectAnimator.ofFloat(mAlphaView, "scaleY",
					0.5f, 1.2f);
			a.setRepeatCount(Animation.INFINITE);
			sx.setRepeatCount(Animation.INFINITE);
			sy.setRepeatCount(Animation.INFINITE);

			alpha.playTogether(a, sx, sy);
			// alpha = new AnimationSet(false);
			// AlphaAnimation a = new AlphaAnimation(0.0f, 1.0f);
			// ScaleAnimation s = new ScaleAnimation(0.5f, 1.2f, 0.5f, 1.2f,
			// 0.5f, 0.5f);
			// a.setRepeatCount(Animation.INFINITE);
			// s.setRepeatCount(Animation.INFINITE);
			// alpha.addAnimation(a);
			// alpha.addAnimation(s);
			alpha.setDuration(2000);
			alpha.start();
		}
		// alpha.setRepeatCount(Animation.INFINITE);
		// mAlphaView.startAnimation(alpha);
	}

	// 显示中心图标动画
	protected void centerAnimationStart() {
		mAlphaView.setVisibility(View.VISIBLE);

		if (alpha == null) {

			alpha = new AnimatorSet();
			ObjectAnimator a = ObjectAnimator.ofFloat(mAlphaView, "alpha",
					0.5f, 1f, 0.0f);
			ObjectAnimator sx = ObjectAnimator.ofFloat(mAlphaView, "scaleX",
					0.3f, 1.6f);
			ObjectAnimator sy = ObjectAnimator.ofFloat(mAlphaView, "scaleY",
					0.3f, 1.6f);
			a.setRepeatCount(Animation.INFINITE);
			sx.setRepeatCount(Animation.INFINITE);
			sy.setRepeatCount(Animation.INFINITE);

			alpha.playTogether(a, sx, sy);
			alpha.setDuration(2000);
			alpha.start();
		}
	}

	// 启动拨号应用
	private void launchDial() {
		Intent intent = new Intent(Intent.ACTION_DIAL);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}

	// 启动相机应用
	private void launchCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		mContext.startActivity(intent);
	}

	/***
	 * 重置几个light view
	 */
	public void resetView() {
		final View mActivatedViews[] = { mUnLockLightView,// mSmsLightView,
				mDialLightView, mCameraLightView };
		for (View view : mActivatedViews) {
			view.setVisibility(View.INVISIBLE);
		}
	}
}