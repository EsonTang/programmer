package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

import com.prize.ext.res.LockConfigBean;
import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.interfaces.LockClickListener;
import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.prizelockscreen.R;
/***
 * 右滑解锁最外层视图
 * @author fanjunchen
 *
 */
public class RightFrameLayout extends BaseRootViewNew implements View.OnClickListener {

	private final String TAG = "RightFrameLayout";
	
	private com.prize.lockscreen.widget.PullRightDoorView mPullChild = null;
	
	private LinearLayout mPinChild = null;
	
	private IUnLockListener mClickListener;
	
	private PasswordTextView mPassTxt;
	/**监控对象的显示区域*/
	private Rect mRect = null;
	/**是否按下Camera*/
	private boolean isCameraDown = false;
	/**监控对象的res ID*/
	private int mCameraId = R.id.img_camera;
	
	public RightFrameLayout(Context context) {
		this(context, null);
	}

	public RightFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RightFrameLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public RightFrameLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setLockClickListener(IUnLockListener l) {
		mClickListener = l;
	}
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		// 获取自己想要的视图
		mPullChild = (com.prize.lockscreen.widget.PullRightDoorView)findViewById(R.id.content_view);
		
		/*if (mPullChild != null) {
			((ICanTouch)mPullChild).setTouchable(false);
		}*/
		
		mPinChild = (LinearLayout) findViewById(R.id.pin_lay);
		
		mPullChild.setUnlockListener(mUnlockListener);
		
		mPassTxt = (PasswordTextView)findViewById(R.id.simPinEntry);
		
		/*View tmp = findViewById(R.id.key_enter);
		if (tmp != null)
			tmp.setOnClickListener(this);*/
		
		View tmp = findViewById(R.id.delete_button);
		if (tmp != null)
			tmp.setOnClickListener(this);
		
		tmp = findViewById(R.id.txt_emc);
		if (tmp != null)
			tmp.setOnClickListener(this);
		
		/*if (mPinChild != null) {
			FrameLayout.LayoutParams p = (FrameLayout.LayoutParams)mPinChild.getLayoutParams();
			p.leftMargin = -DisplayUtil.getScreenWidthPixels();
			p.rightMargin = DisplayUtil.getScreenWidthPixels();
			mPinChild.setLayoutParams(p);
			mPinChild.setVisibility(View.VISIBLE);
		}*/
	}
	/**
	 * 设置解锁回调
	 * @param l
	 */
	public void setUnlockListener(IUnLockListener l) {
		mUnlockListener = l;
		if (mPullChild != null) {
			mPullChild.setUnlockListener(mUnlockListener);
		}
	}

	@Override
	public void resetView() {
		// TODO Auto-generated method stub
		mPassTxt.reset(false);
		mPullChild.setVisibility(View.VISIBLE);
		mPinChild.setVisibility(View.GONE);
		mPullChild.scrollTo(0, 0);
		/*mPullChild.setTranslationX(0);
		mPinChild.setTranslationX(0);
		mPullChild.setTranslationY(0);
		mPinChild.setTranslationY(0);*/
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		/*case R.id.key_enter:
			//校验密码的正确性
			String pass = mPassTxt.getText();
			if (pass.equals(mUnlockListener.getPwd())) {
				if (mPinChild != null) {
					mPinChild.setVisibility(View.GONE);
				}
				enterApp(mPullChild.getPos());
				if (mClickListener != null)
					mClickListener.trueUnlock();
			}
			else {
				mPassTxt.startAnimation(AnimationUtil.shakeAnimation(5, new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						mPassTxt.reset(true);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}}));
				// mPassTxt.reset(true);
				AnimationUtil.virbate(mContext);
			}
			break;*/
		case R.id.delete_button:
			if (mPassTxt != null)
				mPassTxt.deleteLastChar();
			break;
		case R.id.txt_emc:
			Intent it = new Intent("com.android.phone.EmergencyDialer.DIAL"); // 紧急呼叫
			it.addCategory(Intent.CATEGORY_DEFAULT);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			mContext.startActivity(it);
			it = null;
			resetView();
			if (mClickListener != null) {
				mClickListener.emergencyUnlock();
			}
			break;
		}
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
		mPullChild.setPos(0);
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
    /***
     * 让数字盘可见
     * @param pos 进入哪一个应用
     */
    public void setPinVisible(int pos) {
    	mPullChild.setPos(pos);
    	mPullChild.setVisibility(View.GONE);
		mPinChild.setVisibility(View.VISIBLE);
		
		pinAnimte();
    }
    
    /**是否有PinCode动画在运行**/
    private boolean isPinAnimRun = false;
    
    private void pinAnimte() {
    	if (isPinAnimRun)
    		return;
    	isPinAnimRun = true;
    	ObjectAnimator y = ObjectAnimator.ofFloat(pinView, "translationY", mScreenHeigh, 0).setDuration(400);
    	y.addListener(mAnimListener);
    	y.start();
    }
    /***
     * 动画监听器
     */
    private AnimatorListener mAnimListener = new AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			isPinAnimRun = false;
			if (LockScreenApplication.getBlurBg() != null &&
					!LockScreenApplication.getBlurBg().isRecycled())
				pinView.setBackground(new BitmapDrawable(getResources(), LockScreenApplication.getBlurBg()));
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			isPinAnimRun = false;
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
		}
    	
    };
    
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (mPinChild != null && View.VISIBLE == mPinChild.getVisibility()) {
				resetView();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
    
    @Override
	public void updateTime() {
		// TODO Auto-generated method stub mDateTxt
    	if (mPullChild != null) {
    		mPullChild.updateTime();
    	}
	}
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (null == mVelTracker) {
				mVelTracker = VelocityTracker.obtain();
			}
			else {
				mVelTracker.clear();
			}
			if (null == mRect) {
				mRect = new Rect();
				View v = mPullChild.findViewById(mCameraId);
				if (v!= null)
					v.getHitRect(mRect);
			}
			mVelTracker.addMovement(event);	
			
			downX = (int) event.getX();
			downY = (int) event.getY();
			
			if (mRect.contains(downX, downY)) {
				LogUtil.i(TAG, "===Camera area down==");
				isCameraDown = true;
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			mVelTracker.addMovement(event);
			int curY = (int) event.getY();
			int curX = (int) event.getX();
			
			int delX = curX - downX;
			int delY = downY - curY;
			if (isCameraDown && delY > 150) {
				slideTo(false, 0, curY);
			}
			// 只准上滑有效
			else if (!isCameraDown && delX > 0) {
				slideTo(true, delX, 0);
			}
			
			break;
		case MotionEvent.ACTION_UP:
			curY = (int) event.getY();
			delY = downY - curY;
			
			mVelTracker.computeCurrentVelocity(500);
			
			if (isCameraDown && delY >= 0) {
				
				float vY = mVelTracker.getYVelocity();
				if (calAndMoveToY(vY, 0, curY)) {
					isCameraDown = false;
					break;
				}
				
				if (Math.abs(delY) > mScreenHeigh / 2) {
					// 向上滑动超过半个屏幕高的时候 开启向上消失动画
					mPos = 3;
					//animTo(false, 0, curY, 0, 0);
				} else {
					// 向上滑动未超过半个屏幕高的时候 开启向下弹动动画
					//animTo(false, 0, curY, 0, mScreenHeigh);
				}
				isCameraDown = false;
				break;
			}
			
			mPos = 0;
			float vX = mVelTracker.getXVelocity();
			
			Log.i(TAG, "===Yvelocity==" + vX);
			curX = (int) event.getX();
			if (calAndMoveTo(vX, curX, 0))
				break;
			delX = curX - downX;
			
			if (delX >= 0) {
				if (Math.abs(delX) > mScreenWidth / 2) {
					// 向右滑动超过半个屏幕高的时候 开启向左消失动画
					animTo(true, new float[]{curX, mScreenWidth}, 500);
					//mCloseFlag = true;
				} else {
					// 向右滑动未超过半个屏幕高的时候 开启向左弹动动画
					animTo(true, new float[]{curX, 0}, 500);
				}
			}
			isCameraDown = false;
			// mVelTracker.recycle();
			break;
		case MotionEvent.ACTION_CANCEL:
			isCameraDown = false;
			resetView();
			break;
		}
		return super.onTouchEvent(event);
	}
    
    /***
	 * 计算时间及距离并滑动到目标 X轴上移动
	 * @param velocity
	 * @param curX
	 * @param curY
	 * @return
	 */
	private boolean calAndMoveTo(float velocityX, int curX, int curY) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocityX));
		int duration = getSplineFlingDuration((int)Math.abs(velocityX));
		
		boolean rs = false;
		
		int startX = curX;
		float[] f = null;
		if (velocityX > 20) {
			int aim = (int)(startX + dis);
			if (aim > mScreenWidth || aim > mScreenWidth / 2 + 10) {
				duration = (int)((duration * (mScreenWidth - startX)) / dis);
				f = new float[]{startX, mScreenWidth};
			}
			else {
				duration = (int)((duration * (startX + 2 * dis)) / dis);
				f = new float[]{startX, startX + dis, 0};
			}
			rs = true;
			if (duration < 120)
				duration = 120;
			else if (duration > 1200)
				duration = 1200;
			animTo(true, f, duration);
		}
		else if (velocityX < -20){
			rs = true;
			int aim = (int)(startX - dis);
			if (aim < 0 || aim < mScreenWidth/2) {
				duration = (int)(duration * startX / dis);
				f = new float[]{startX, 0};
			}
			else {
				duration = (int)(duration * (mScreenWidth - startX + 2 * dis) / dis);
				f = new float[]{startX, startX - dis, mScreenWidth};
			}
			if (duration < 120)
				duration = 120;
			else if (duration > 1200)
				duration = 1200;
			
			animTo(true, f, duration);
		}
		return rs;
	}
	
	/***
	 * 计算时间及距离并滑动到目标
	 * @param velocityY
	 * @param curX
	 * @param curY
	 * @return
	 */
	private boolean calAndMoveToY(float velocityY, int curX, int curY) {
		float dis = (float)getSplineFlingDistance((int)Math.abs(velocityY));
		int duration = getSplineFlingDuration((int)Math.abs(velocityY));
		
		boolean rs = false;
		mPos = 0;
		int startY = curY;
		float[] f = null;
		if (velocityY < -20) {
			int aim = (int)(startY + dis);
			if (aim > mScreenHeigh || aim > mScreenHeigh / 2) {
				duration = (int)((duration * (mScreenHeigh - startY)) / dis);
				f = new float[]{startY, mScreenHeigh};
			}
			else {
				duration = (int)((duration * (startY + 2 * dis)) / dis);
				f = new float[]{startY, startY + dis, 0};
			}
			
			rs = true;
			if (duration < 120)
				duration = 120;
			else if (duration > 1600)
				duration = 1600;
			
			animTo(false, f, duration);
		}
		else if (velocityY > 20){
			int aim = (int)(startY - dis);
			if (aim < mScreenHeigh/2) {
				duration = (int)(duration * startY / dis);
				f = new float[]{startY, 0};
			}
			else {
				duration = (int)((duration * (mScreenHeigh - startY + 2 * dis)) / dis);
				f = new float[]{startY, startY + dis, mScreenHeigh};
			}
			
			rs = true;
			if (duration < 120)
				duration = 120;
			else if (duration > 1600)
				duration = 1600;
			
			animTo(false, f, duration);
		}
		return rs;
	}
	/***
	 * 移动到某个地方
	 * @param isX 是否沿X轴方向, true 表示X轴方向, false 表示Y轴方向
	 * @param x 滑动到X轴的位置
	 * @param y 滑动到Y轴的位置
	 */
	private void slideTo(boolean isX, int x, int y) {
		if (isX) {
			mPullChild.setTranslationX(x);
			//mPinChild.setTranslationX(x);
		}
		else
		{
			mPullChild.setTranslationY(y);
			//mPinChild.setTranslationY(y);
		}
	}
	
	/***
	 * 移动到某个地方
	 * @param isX 是否沿X轴方向, true 表示X轴方向, false 表示Y轴方向
	 * @param ds 移动轨迹点
	 */
	private void animTo(boolean isX, float[] ds, int duration) {
		if (isX) {
			/*AnimatorSet animSet = new AnimatorSet();
	    	ObjectAnimator a = ObjectAnimator.ofFloat(mPullChild, "translationX", ds);
	    	ObjectAnimator b = ObjectAnimator.ofFloat(mPinChild, "translationX", ds);
	    	
	    	animSet.setDuration(duration);
	    	animSet.playTogether(a, b);
	    	animSet.start();*/
	    	
	    	ObjectAnimator a = ObjectAnimator.ofFloat(mPullChild, "translationX", ds);
	    	a.setDuration(duration);
	    	a.start();
		}
		else
		{
			/*AnimatorSet animSet = new AnimatorSet();
			ObjectAnimator a = ObjectAnimator.ofFloat(mPullChild, "translationY", ds);
	    	
	    	ObjectAnimator b = ObjectAnimator.ofFloat(mPinChild, "translationY", ds);
	    	
	    	animSet.setDuration(duration);
	    	animSet.playTogether(a, b);
	    	animSet.start();*/
			
			ObjectAnimator a = ObjectAnimator.ofFloat(mPullChild, "translationY", ds);
			a.setDuration(duration);
	    	a.start();
		}
	}
	
	@Override
	protected int getLockType() {
		return LockConfigBean.DEFAULT_LOCK_TYPE;
	}

	@Override
	public void setContentViewTouchable(boolean isTouch) {
		if (mPullChild != null)
			mPullChild.setTouchable(isTouch);
	}
}
