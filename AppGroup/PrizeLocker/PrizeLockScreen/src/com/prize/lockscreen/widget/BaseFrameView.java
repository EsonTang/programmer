package com.prize.lockscreen.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.interfaces.IResetView;
import com.prize.lockscreen.interfaces.IUnLockListener;
import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.prizelockscreen.R;
/***
 * 包括密码与解锁视图 的基础视图
 * @author fanjunchen
 *
 */
public class BaseFrameView extends BaseRootViewNew implements View.OnClickListener {

	private final String TAG = "BaseFrameView";
	
	private NewPasswordTextView mPassTxt;
	
	private LinearLayout.LayoutParams mParams = null;
	/**用来保存VIEW*/
	private SparseArray<View> viewMap = new SparseArray<View>(4);
	/**密码类型*/
	private int pwdType = -1;
	
	private EditText mPwdText = null;
	
	public BaseFrameView(Context context) {
		this(context, null);
	}

	public BaseFrameView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BaseFrameView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public BaseFrameView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
				LinearLayout.LayoutParams.MATCH_PARENT);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		if (mUnlockListener != null)
			setUnlockListener(mUnlockListener);
		
		/*if (mPinChild != null) {// 用于平移时用的
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
		super.setUnlockListener(l);
		if (contentView != null) {
			LogUtil.i(TAG, "==contentView==setUnlockListener");
			if (contentView instanceof ColorBubbleView) {
				((ColorBubbleView)contentView).setUnlockListener(l);
			}
			else if (contentView instanceof CircleRelativeView) {
				((CircleRelativeView)contentView).setUnlockListener(l);
			}
			else if (contentView instanceof BlinkRelativeView) {
				((BlinkRelativeView)contentView).setUnlockListener(l);
			}
			else if (contentView instanceof SlidUpFrameView) {
				((SlidUpFrameView)contentView).setUnlockListener(l);
			}
			else if (contentView instanceof PullRightDoorView) {
				((PullRightDoorView)contentView).setUnlockListener(l);
			}
		}
	}

	@Override
	public void resetView() {
		switch (pwdType) {
			case 1:
				if (mPassTxt != null)
					mPassTxt.reset(false);
				break;
			case 2:
				break;
			case 3:
				if (mPwdText != null)
					mPwdText.setText("");
				View vv = viewMap.get(3);
				if (vv != null && vv instanceof ComplexPwdLayout) {
					((ComplexPwdLayout)vv).hideSoft();
				}
				break;
		}
		
		if (mUnlockListener != null && mUnlockListener.hasPwd()) {
			pinView.setVisibility(View.GONE);
		}
		if (contentView != null) {
			contentView.setVisibility(View.VISIBLE);
			if (contentView instanceof IResetView)
				((IResetView) contentView).resetView();
			if (contentView instanceof PullRightDoorView)
				((PullRightDoorView) contentView).scrollTo(0, 0);
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		/*case R.id.delete_button:
			if (mPassTxt != null)
				mPassTxt.deleteLastChar();
			break;*/
		case R.id.txt_emc:
			Intent it = new Intent("com.android.phone.EmergencyDialer.DIAL"); // 紧急呼叫
			it.addCategory(Intent.CATEGORY_DEFAULT);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			it.putExtra("from", "prizeLock");
			mContext.startActivity(it);
			it = null;
			resetView();
			if (mUnlockListener != null) {
				mUnlockListener.emergencyUnlock();
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
			// launchSms();
			break;
		case 2:
			launchDial();
			break;
		case 3:
			launchCamera();
			break;
		}
		setPos(0);
	}
	
	//启动短信应用
    private void launchSms() {

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
    	setPos(pos);
    	contentView.setVisibility(View.GONE);
    	pinView.setVisibility(View.VISIBLE);
    	pinView.setBackground(null);
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
			if (pinView != null && View.VISIBLE == pinView.getVisibility()) {
				resetView();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}
    
    @Override
	public void updateTime() {
		// TODO Auto-generated method stub mDateTxt
    	if (contentView != null) {
			if (contentView instanceof ColorBubbleView) {
				((ColorBubbleView)contentView).updateTime();
			}
			else if (contentView instanceof CircleRelativeView) {
				((CircleRelativeView)contentView).updateTime();
			}
			else if (contentView instanceof BlinkRelativeView) {
				((BlinkRelativeView)contentView).updateTime();
			}
			else if (contentView instanceof SlidUpFrameView) {
				((SlidUpFrameView)contentView).updateTime();
			}
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
			/*if (null == mRect) {
				mRect = new Rect();
				View v = mPullChild.findViewById(mCameraId);
				if (v!= null)
					v.getHitRect(mRect);
			}*/
			mVelTracker.addMovement(event);	
			
			downX = (int) event.getX();
			downY = (int) event.getY();
			
			/*if (mRect.contains(downX, downY)) {
				LogUtil.i(TAG, "===Camera area down==");
				isCameraDown = true;
			}*/
			// return true;
			break;
		case MotionEvent.ACTION_MOVE:
			mVelTracker.addMovement(event);
			int curY = (int) event.getY();
			int curX = (int) event.getX();
			
			int delX = curX - downX;
			int delY = downY - curY;
			/*if (isCameraDown && delY > 150) {
				slideTo(false, 0, curY);
			}
			// 只准上滑有效
			else if (!isCameraDown && delX > 0) {
				slideTo(true, delX, 0);
			}*/
			
			break;
		case MotionEvent.ACTION_UP:
			curY = (int) event.getY();
			delY = downY - curY;
			
			mVelTracker.computeCurrentVelocity(500);
			
			/*if (isCameraDown && delY >= 0) {
				
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
			}*/
			
			mPos = 0;
			float vX = mVelTracker.getXVelocity();
			
			Log.i(TAG, "===Yvelocity==" + vX);
			/*curX = (int) event.getX();
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
			isCameraDown = false;*/
			break;
		case MotionEvent.ACTION_CANCEL:
			/*isCameraDown = false;*/
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
			//contentView.setTranslationX(x);
			//pinView.setTranslationX(x);
		}
		else
		{
			//contentView.setTranslationY(y);
			//pinView.setTranslationY(y);
		}
	}
	
	/***
	 * 移动到某个地方
	 * @param isX 是否沿X轴方向, true 表示X轴方向, false 表示Y轴方向
	 * @param ds 移动轨迹点
	 */
	private void animTo(boolean isX, float[] ds, int duration) {
		if (isX) {
	    	
	    	ObjectAnimator a = ObjectAnimator.ofFloat(contentView, "translationX", ds);
	    	a.setDuration(duration);
	    	a.start();
		}
		else
		{
			ObjectAnimator a = ObjectAnimator.ofFloat(contentView, "translationY", ds);
			a.setDuration(duration);
	    	a.start();
		}
	}
	
	@Override
	protected int getLockType() {
		if (contentView != null) {
			if (contentView instanceof ColorBubbleView) {
				return ((ColorBubbleView)contentView).getLockType();
			}
			else if (contentView instanceof CircleRelativeView) {
				return ((CircleRelativeView)contentView).getLockType();
			}
			else if (contentView instanceof BlinkRelativeView) {
				return ((BlinkRelativeView)contentView).getLockType();
			}
			else if (contentView instanceof SlidUpFrameView) {
				return ((SlidUpFrameView)contentView).getLockType();
			}
		}
		return 0;
	}
	
	@Override
	public void setContentViewTouchable(boolean isTouch) {
		if (contentView != null) {
			if (contentView instanceof ColorBubbleView) {
				((ColorBubbleView)contentView).setTouchable(isTouch);
			}
			else if (contentView instanceof CircleRelativeView) {
				((CircleRelativeView)contentView).setTouchable(isTouch);
			}
			else if (contentView instanceof BlinkRelativeView) {
				((BlinkRelativeView)contentView).setTouchable(isTouch);
			}
			else if (contentView instanceof SlidUpFrameView) {
				((SlidUpFrameView)contentView).setTouchable(isTouch);
			}
		}
	}
	/***
	 * 根据密码类型设置密码内容
	 * @param type 0无, 1数字, 2 图案, 3 混合
	 */
	public void setPwdContent(int type) {
		pwdType = type;
		switch(type) {
			case 0:
				pinView.removeAllViews();
				setNull();
				break;
			case 1:
				View v = viewMap.get(type);
				if (null == v) {
					v = inflate(mContext, R.layout.pin_all_lay, null);
					viewMap.put(type, v);
				}
				setNull();
				pinView.removeAllViews();
				initView(type, v);
				mParams.height = mContext.getResources().getDimensionPixelSize(R.dimen.pin_lay_height);
				pinView.addView(v, mParams);
				break;
			case 2:
				v = viewMap.get(type);
				if (null == v) {
					v = inflate(mContext, R.layout.patter_all_lay, null);
					viewMap.put(type, v);
				}
				setNull();
				pinView.removeAllViews();
				((ConfirmPatternPwdLayout)v).setConfirm(2);
				((ConfirmPatternPwdLayout)v).setOnUnlockListener(mUnlockLsn);
				initView(type, v);
				mParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
				pinView.addView(v, mParams);
				break;
			case 3:
				v = viewMap.get(type);
				if (null == v) {
					v = inflate(mContext, R.layout.complex_pwd_all_lay, null);
					viewMap.put(type, v);
				}
				setNull();
				pinView.removeAllViews();
				((ComplexPwdLayout)v).setConfirm(2);
				((ComplexPwdLayout)v).setOnUnlockListener(mUnlockLsn);
				initView(type, v);
				mParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
				pinView.addView(v, mParams);
				break;
		}
	}
	/***
	 * 把一些View 对象设置成null
	 */
	private void setNull() {
		mPassTxt = null;
		mPwdText = null;
	}
	/***
	 * 初始化控件及事件
	 * @param type
	 */
	private void initView(int type, View v) {
		switch (type) {
			case 1:// 数字密码
				mPassTxt = (NewPasswordTextView)v.findViewById(R.id.simPinEntry);
				
				PinLayout tk = (PinLayout)v.findViewById(R.id.pin_content_lay);
				if (tk != null) {
					tk.setIValidate(mValideLsn);
				}
				/*View tmp = v.findViewById(R.id.key_enter);
				if (tmp != null)
					tmp.setOnClickListener(this);
				
				tmp = v.findViewById(R.id.delete_button);
				if (tmp != null)
					tmp.setOnClickListener(this);*/
				break;
			case 2:// 图案密码
				/*mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
				mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
				mLockPatternView.setTactileFeedbackEnabled(true);*/
				break;
			case 3: // 复杂密码
				mPwdText = (EditText)v.findViewById(R.id.edit_pwd);
				break;
		}
		
		View tmp = v.findViewById(R.id.txt_emc);
		if (tmp != null)
			tmp.setOnClickListener(this);
	}
	
	private OnUnlockListener mUnlockLsn = new OnUnlockListener() {

		@Override
		public void onUnlock(boolean isConfirm) {
			if (isConfirm && mUnlockListener != null) {
				enterApp(getPos());
				mUnlockListener.trueUnlock();
			}
		}
	};
	/**数字密码校验回调*/
	private PinLayout.IValidate mValideLsn = new PinLayout.IValidate() {

		@Override
		public boolean validate(String pwd) {
			//校验密码的正确性
			if (pwd.equals(mUnlockListener.getPwd())) {
				if (pinView != null) {
					pinView.setVisibility(View.GONE);
				}
				enterApp(getPos());
				mUnlockListener.trueUnlock();
				return true;
			}
			else {
				AnimationUtil.virbate(mContext);
			};
			return false;
		}
	};
}
