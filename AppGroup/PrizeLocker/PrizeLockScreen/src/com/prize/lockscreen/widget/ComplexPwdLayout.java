package com.prize.lockscreen.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.lockscreen.utils.TimeUtil;
import com.prize.prizelockscreen.R;
/***
 * 复杂密码操作布局
 * @author fanjunchen
 *
 */
public class ComplexPwdLayout extends LinearLayout {

	// how long we wait to clear a wrong pattern
	private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 4000;
	
	private final int TIME_SHOW_HIDE_INPUT = 600;

	public ComplexPwdLayout(Context context) {
		this(context, null);
	}

	public ComplexPwdLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

	public ComplexPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public ComplexPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private OnUnlockListener onUnlockListener;
	/**0 新录入, 1 确认, 2 解锁*/
	private int pwdType = 0;
	
	protected TextView mHeaderText;
	
	private EditText pwdEdit;
	
	private Stage mUiStage = Stage.Introduction;
	
	private InputMethodManager mImm;
	/**首次输入的密码*/
	private String firstPwd = null;
	
	/**用于开启软键盘*/
	private Timer mTimer;

	public void setOnUnlockListener(OnUnlockListener onUnlockListener) {
		this.onUnlockListener = onUnlockListener;
	}

	/**
	 * 0 新录入, 1 确认, 2 绘制解锁
	 * @param type
	 */
	public void setConfirm(int type) {
		this.pwdType = type;
	}

	private void init(Context context) {
		mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.linear_out_slow_in);
        mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(
                context, android.R.interpolator.fast_out_linear_in);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// mLockPatternView.setTactileFeedbackEnabled(true);
		mImm = (InputMethodManager) getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
		
		mHeaderText = (TextView) findViewById(R.id.txt_hint);
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if(pwdType == 1) {
					mHeaderText.setText(getContext().getResources().getString(R.string.enter_pwd));
					mHeaderText.removeCallbacks(clearRun);
					mHeaderText.postDelayed(clearRun, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
				}
				else if(pwdType == 2) {
					mHeaderText.setText(getContext().getResources().getString(R.string.enter_pwd));
					mHeaderText.removeCallbacks(clearRun);
					mHeaderText.postDelayed(clearRun, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
				}
				else {
					updateStage(mUiStage);
				}
				getViewTreeObserver().removeOnPreDrawListener(this);
				return false;
			}
		});
		
		pwdEdit = (EditText)findViewById(R.id.edit_pwd);
		
		pwdEdit.setOnEditorActionListener(editorActionListener);
		/*pwdEdit.setOnKeyListener(new View.OnKeyListener() {  
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if (KeyEvent.KEYCODE_ENTER == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {  
					asdfadf;
					return true;  
				}
				return false;
			}  
		});*/ 
		
		mHeaderText.postDelayed(clearRun, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
		pwdEdit.requestFocus();
		// mImm.showSoftInput(pwdEdit, InputMethodManager.SHOW_IMPLICIT);
	}

	public void onResume() {
		if (mImm != null) {
			
			if (null == mTimer)
				mTimer = new Timer();
			mTimer.schedule(new TimerTask() {
				public void run() {
					InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputManager.showSoftInput(pwdEdit, 0);
				}
			}, 400);
			/*pwdEdit.setFocusable(true);
			pwdEdit.setFocusableInTouchMode(true);
			pwdEdit.requestFocus();
			pwdEdit.postDelayed(showOrHideInput, TIME_SHOW_HIDE_INPUT);*/
		}
	}
	
	public void onPause() {
		pwdEdit.postDelayed(showOrHideInput, TIME_SHOW_HIDE_INPUT);
	}
	
	private Runnable clearRun = new Runnable() {
		public void run() {
			mHeaderText.setText("");
		}
	};
	/**输入法是否弹出*/
	private boolean isInputShow = false;
	/***
	 * 弹出或隐藏输入框
	 */
	private Runnable showOrHideInput = new Runnable() {
		public void run() {
			if (!isInputShow) {
				isInputShow = true;
				if (mImm != null) {
					mImm.showSoftInput(pwdEdit, InputMethodManager.SHOW_IMPLICIT);// InputMethodManager.SHOW_IMPLICIT
					mImm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
				}
			}
			else {
				if (mImm != null)
					mImm.hideSoftInputFromWindow(pwdEdit.getWindowToken(), 0);
				isInputShow = false;
			}
		}
	};
	
	private final EditText.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == KeyEvent.ACTION_DOWN
					|| actionId == EditorInfo.IME_ACTION_DONE) {
				// 业务代码
				String str = v.getText().toString();
				if (pwdType == 2) {
					if (str.equals(SharedPreferencesTool.getNumberPassword(getContext()))) {
						updateStage(Stage.Introduction);
						if (onUnlockListener != null)
							onUnlockListener.onUnlock(true);
					}
					else {
						updateStage(Stage.UnlockWrong);
						v.setText("");
						return true;
					}
					v.setText("");
				}
				else if (pwdType == 1) {
					if (str.equals(SharedPreferencesTool.getNumberPassword(getContext()))) {
						updateStage(Stage.Introduction);
						if (onUnlockListener != null)
							onUnlockListener.onUnlock(true);
					}
					else {
						updateStage(Stage.UnlockWrong);
						v.setText("");
						return true;
					}
				}
				else {
					// 判断长度与字符是否符合条件
					if (str.length() < 6 || !TimeUtil.hasAlpha(str)) {
						updateStage(Stage.EnterTooShort);
						return true;
					}
					if (TextUtils.isEmpty(firstPwd)) {
						firstPwd = str;
						updateStage(Stage.NeedToConfirm);
						v.setText("");
						return true;
					}
					// 若符合条件则判断与上次的是否相等
					else if (str.equals(firstPwd)) {
						SharedPreferencesTool.setNumberPassword(getContext(), str);
						SharedPreferencesTool.setLockPwdType(getContext(), SharedPreferencesTool.LOCK_STYLE_COMPLEX_PASSWORD);
						updateStage(Stage.Introduction);
						v.setText("");
						if(onUnlockListener != null){
							onUnlockListener.onUnlock(true);
						}
					}
					else {
						updateStage(Stage.ConfirmWrong);
						//v.setText("");
						return true;
					}
				}
				
				hideSoft();
			}
			return true;
		}
	};
	/***
	 * 隐藏输入法
	 */
	public void hideSoft() {
		if (mImm != null)
			mImm.hideSoftInputFromWindow(getWindowToken(), 0);
	}
	/**
	 * Keep track internally of where the user is in choosing a pattern.
	 */
	protected enum Stage {

				Introduction(R.string.enter_pwd,
						true), 
				HelpScreen(R.string.lockpattern_settings_help_how_to_record, false), 
				EnterTooShort(R.string.enter_too_short, true), 
				FirstChoiceValid(R.string.pwd_entered_record, false), 
				NeedToConfirm(R.string.need_to_enter_confirm, true), 
				ConfirmWrong(R.string.need_to_enter_wrong,	true), 
				UnlockWrong(R.string.enter_pwd_error, true);

		/**
		 * @param headerMessage
		 *            The message displayed at the top.
		 * @param patternEnabled
		 *            Whether the pattern widget is enabled.
		 */
		Stage(int headerMessage, boolean patternEnabled) {
			this.headerMessage = headerMessage;
			this.patternEnabled = patternEnabled;
		}

		final int headerMessage;
		final boolean patternEnabled;
	}

	/**
	 * Updates the messages and buttons appropriate to what stage the user is at
	 * in choosing a view. This doesn't handle clearing out the pattern; the
	 * pattern is expected to be in the right state.
	 * 
	 * @param stage
	 */
	protected void updateStage(Stage stage) {
		final Stage previousStage = mUiStage;

		mUiStage = stage;

		// header text, footer text, visibility and
		// enabled state all known from the stage
		if (stage == Stage.EnterTooShort) {
			mHeaderText.setText(getResources()
					.getString(stage.headerMessage, 6));
			mHeaderText.removeCallbacks(clearRun);
			mHeaderText.postDelayed(clearRun, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
		} else {
			mHeaderText.setText(stage.headerMessage);
			mHeaderText.removeCallbacks(clearRun);
			mHeaderText.postDelayed(clearRun, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
		}



		switch (mUiStage) {
		case Introduction:
			break;
		case HelpScreen:
			break;
		case EnterTooShort:
			break;
		case FirstChoiceValid:
			break;
		case NeedToConfirm:
			break;
		case ConfirmWrong:
			break;
		case UnlockWrong:
			break;
		}

		// If the stage changed, announce the header for accessibility. This
		// is a no-op when accessibility is disabled.
		if (previousStage != stage) {
			mHeaderText.announceForAccessibility(mHeaderText.getText());
		}
	}
	
	// animate relative
	private Interpolator mLinearOutSlowInInterpolator;
    private Interpolator mFastOutLinearInInterpolator;
    
	public void startAppearAnimation() {
        setAlpha(0f);
        setTranslationY(0f);
        animate()
                .alpha(1)
                .withLayer()
                .setDuration(300)
                .setInterpolator(mLinearOutSlowInInterpolator);
    }

    public boolean startDisappearAnimation(Runnable finishRunnable) {
        animate()
                .alpha(0f)
                .translationY(-DisplayUtil.getScreenHeightPixels())
                .setInterpolator(mFastOutLinearInInterpolator)
                .setDuration(100)
                .withEndAction(finishRunnable);
        return true;
    }
}
