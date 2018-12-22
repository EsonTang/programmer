package com.prize.lockscreen.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;
/***
 * 数字或复杂密码输入布局
 * @author fanjunchen
 *
 */
public class PwdSetLayout extends RelativeLayout {

	private TextView mTxtHint, mBtnCancel, mBtnGoon;
	/**密码显示控件*/
	private EditText mPwdTxt;
	
	private IBottomClick bottomClick;
	/**已经存在的密码*/
	private String passStr = null;
	/**第一次输入的密码*/
	private String enterPwd = null;
	
	private Context mCtx;
	/**当前状态: 0 表示需要输入密码确认, 1 表示输入新的密码, 2表示二次输入新密码但需要与上一次相同**/
	private int state = 0;
	/**默认为数字密码, 大于等于6就是复杂密码*/
	private int maxLen = 4;
	/**是否为确认*/
	private int confirm = 0;
	/**是否为简单密码*/
	private boolean isSimple = true;
	
	private boolean enableChange = false;
	/**用于开启软键盘*/
	private Timer mTimer;
	/**
	 * 0 新录入, 1 确认
	 * @param c
	 */
	public void setConfirm(int c) {
		confirm = c;
	}
	
	public PwdSetLayout(Context context) {
		this(context, null);
	}

	public PwdSetLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PwdSetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public PwdSetLayout(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		mCtx = context;
		init();
	}

	/***
	 * 初始化数据
	 */
	private void init() {
		passStr = SharedPreferencesTool.getNumberPassword(mCtx);
	}
	
	public void setState(int t) {
		state = t;
		if (state == 1) {
			if (maxLen > 4)
				mTxtHint.setText(R.string.enter_pwd_has_alp);
			else
				mTxtHint.setText(R.string.enter_digital_pass);
		}
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mTxtHint = (TextView) findViewById(R.id.txt_hint);
		mPwdTxt = (EditText) findViewById(R.id.edt_pwd);
		
		mBtnCancel = (TextView)findViewById(R.id.try_txt);
		if (mBtnCancel != null)
			mBtnCancel.setOnClickListener(mClickLsn);
		
		mBtnGoon = (TextView)findViewById(R.id.go_on_txt);
		if (mBtnGoon != null)
			mBtnGoon.setOnClickListener(mClickLsn);
		
		View tmp = findViewById(R.id.back);
		if (tmp != null)
			tmp.setOnClickListener(mClickLsn);
		
		mPwdTxt.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String pwd = s.toString();
				if (((isSimple && pwd.length() > 3) ||(!isSimple && isComplexPwd(pwd)))
						&& bottomClick != null) {
					if (confirm == 1) {
						if (!bottomClick.validate(pwd)) {
							shake();
							AnimationUtil.virbate(mCtx);
						}
					}
					else if (state == 1 || state == 2) {
						mBtnCancel.setText(R.string.try_again);
						mBtnGoon.setEnabled(true);
					}
					enableChange = true;
				}
				else if(enableChange && 
						((isSimple && pwd.length() < 4) ||(!isSimple && !isComplexPwd(pwd)))
						) {
					mBtnGoon.setEnabled(false);
					enableChange = false;
				}
			}
			
		});
	}
	
	public void getFocus() {
		mPwdTxt.requestFocus();
		if (null == mTimer)
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(mPwdTxt, 0);
			}
		}, 400);
		//mPwdTxt.onTouchEvent(MotionEvent.);
	}
	
	private boolean isComplexPwd(String str) {
		return str.length() >= 4 && TimeUtil.hasAlpha(str);
	}
	/**
	 * 初始化提示文字
	 */
	private void initHint() {
		if (TextUtils.isEmpty(passStr)) {
			if (maxLen > 4)
				mTxtHint.setText(R.string.enter_pwd);
			else
				mTxtHint.setText(R.string.enter_digital_pass);
		}
		
		if (mPwdTxt != null) {
			InputFilter[] filters = {new InputFilter.LengthFilter(maxLen)};
			mPwdTxt.setFilters(filters);
		}
		if (!isSimple) {
			mPwdTxt.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
			mPwdTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
		}
	}
	/***
	 * 设置简单或复杂密码
	 * @param isSimple
	 */
	public void setIsSimple(boolean isSimple) {
		this.isSimple = isSimple;
		if (isSimple) {
			maxLen = 4;
		}
		else {
			maxLen = 16;
		}
		initHint();
	}
	
	private View.OnClickListener mClickLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.try_txt: // 重试或取消
					if (state == 1 && mPwdTxt.getText().toString().trim().length() < 4) {
						if (bottomClick != null)
							bottomClick.onBack();
						return;
					}
					if (maxLen > 4)
						mTxtHint.setText(R.string.enter_pwd_has_alp);
					else
						mTxtHint.setText(R.string.enter_digital_pass);
					state = 1;
					mBtnGoon.setText(R.string.go_on);
					mBtnGoon.setEnabled(false);
					mBtnCancel.setText(R.string.cancel);
					mPwdTxt.setText("");
					enterPwd = null;
					break;
				case R.id.go_on_txt: // 继续
					if (state == 1) {
						state = 2;
						mTxtHint.setText(R.string.need_to_enter_confirm);
						mBtnGoon.setText(R.string.complete);
						enterPwd = mPwdTxt.getText().toString().trim();
						mPwdTxt.setText("");
					}
					else if (state == 2) {
						// 校验与上一次的密码是否一样, 不一样要重新输入,
						if (mPwdTxt.getText().toString().equals(enterPwd)) {
							bottomClick.complete(enterPwd);
						}
						else {
							mPwdTxt.setText("");
							mTxtHint.setText(R.string.need_to_enter_wrong);
						}
						enterPwd = null;
					}
					break;
				case R.id.back: // 返回
					if (bottomClick != null)
						bottomClick.onBack();
					break;
			}
		}
	}; 
	
	/***
	 * 密码控件在摇动
	 */
	public void shake() {
		mTxtHint.setText(R.string.enter_pwd_error);
		mPwdTxt.startAnimation(AnimationUtil.shakeAnimation(5, new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				mPwdTxt.setText("");
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			}
		)
		);
	}
	
	public void setIBottomClick(IBottomClick l) {
		bottomClick = l;
		
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && 
				event.getAction() == KeyEvent.ACTION_UP) {
			if (bottomClick != null)
				bottomClick.onBack();
		}
		return super.dispatchKeyEvent(event);
	}
	/***
	 * 底部按钮接口
	 * @author fanjunchen
	 *
	 */
	public interface IBottomClick extends OnUnlockListener {
		
		public void onBack();
		
		public void onNext();
		/***
		 * 表示与原密码进行校验
		 * @param pwd
		 */
		public boolean validate(String pwd);
		/***
		 * 表示输入完成且正确
		 * @param pwd
		 */
		public void complete(String pwd);
	}
}
