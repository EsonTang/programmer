package com.prize.lockscreen.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.setting.Lists;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.lockscreen.widget.LockPatternView.Cell;
import com.prize.lockscreen.widget.LockPatternView.DisplayMode;
import com.prize.prizelockscreen.R;
/***
 * 图案密码操作布局
 * @author fanjunchen
 *
 */
public class ConfirmPatternPwdLayout extends LinearLayout {

	private static final int ID_EMPTY_MESSAGE = -1;
	// how long we wait to clear a wrong pattern
	private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;

	public ConfirmPatternPwdLayout(Context context) {
		super(context);
		init();
	}

	public ConfirmPatternPwdLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ConfirmPatternPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ConfirmPatternPwdLayout(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private OnUnlockListener onUnlockListener;
	/**0 新录入, 1 确认, 2 绘制解锁*/
	private int isConfirm = 0;
	private LockPatternView mLockPatternView;
	protected TextView mHeaderText;
	protected TextView mFooterText;
	protected List<LockPatternView.Cell> mChosenPattern = null, mTmpPattern;
	private Stage mUiStage = Stage.Introduction;
	
	private TextView mBtnCancel, mBtnGoon;
	
	private View mBack;
	/**
	 * The patten used during the help screen to show how to draw a pattern.
	 */
	private final List<LockPatternView.Cell> mAnimatePattern = Collections
			.unmodifiableList(Lists.newArrayList(LockPatternView.Cell.of(0, 0),
					LockPatternView.Cell.of(0, 1),
					LockPatternView.Cell.of(1, 1),
					LockPatternView.Cell.of(2, 1)));

	public void setOnUnlockListener(OnUnlockListener onUnlockListener) {
		this.onUnlockListener = onUnlockListener;
	}

	/**
	 * 0 新录入, 1 确认, 2 绘制解锁
	 * @param type
	 */
	public void setConfirm(int type) {
		this.isConfirm = type;
	}

	private void init() {
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mLockPatternView = (LockPatternView) findViewById(R.id.lockPattern);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);

		mHeaderText = (TextView) findViewById(R.id.headerText);
		mFooterText = (TextView) findViewById(R.id.footerText);
		
		mBack = findViewById(R.id.back);
		
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if(isConfirm == 1) {
					mHeaderText.setText(getContext().getResources().getString(R.string.lockpattern_need_to_unlock));
				}
				else if(isConfirm == 2) {
					mHeaderText.setText(getContext().getResources().getString(R.string.lockpattern_recording_intro_header));
				}
				else {
					updateStage(mUiStage);
				}
				getViewTreeObserver().removeOnPreDrawListener(this);
				return false;
			}
		});
		
		mBtnCancel = (TextView)findViewById(R.id.try_txt);
		if (mBtnCancel != null)
			mBtnCancel.setOnClickListener(mClickLsn);
		
		mBtnGoon = (TextView)findViewById(R.id.go_on_txt);
		if (mBtnGoon != null) {
			mBtnGoon.setOnClickListener(mClickLsn);
			mBtnGoon.setEnabled(false);
		}
	}

	private View.OnClickListener mClickLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.try_txt: // 重试或取消
					if (mTmpPattern != null) {
						mTmpPattern = null;
						mBtnGoon.setText(R.string.go_on);
						mBtnGoon.setEnabled(false);
						mBtnCancel.setText(R.string.cancel);
						updateStage(Stage.Introduction);
						//postClearPatternRunnable();
					}
					else if (mBack != null)
						mBack.performClick();
					break;
				case R.id.go_on_txt: // 继续
					if (mChosenPattern == null && mTmpPattern != null) {
						mChosenPattern = mTmpPattern;
						postDelayed(new Runnable() {
							@Override
							public void run() {
								updateStage(Stage.NeedToConfirm);				
							}
						}, 600);
						mBtnGoon.setText(R.string.complete);
						mBtnGoon.setEnabled(false);
						//postClearPatternRunnable();
					}
					else if (mChosenPattern != null) {
						SharedPreferencesTool.setPatternPassword(getContext(),LockPatternView.patternToString(mChosenPattern));
						SharedPreferencesTool.setLockPwdType(getContext(),SharedPreferencesTool.LOCK_STYLE_PATTERN_PASSWORD);
						updateStage(Stage.ChoiceConfirmed);
						if(onUnlockListener != null){
							onUnlockListener.onUnlock(false);
						}
					}
					break;
			}
		}
	}; 
	
	private Runnable mClearPatternRunnable = new Runnable() {
		public void run() {
			mLockPatternView.clearPattern();
		}
	};
	/**
	 * The pattern listener that responds according to a user choosing a new
	 * lock pattern.
	 */
	protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			patternInProgress();
		}

		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			/*if (mUiStage == Stage.NeedToConfirm
					|| mUiStage == Stage.ConfirmWrong) {
				if (mChosenPattern == null)
					throw new IllegalStateException(
							"null chosen pattern in stage 'need to confirm");
				if (mChosenPattern.equals(pattern)) {
					updateStage(Stage.ChoiceConfirmed);
				} else {
					updateStage(Stage.ConfirmWrong);
				}
			} else if (mUiStage == Stage.Introduction
					|| mUiStage == Stage.ChoiceTooShort) {
				if (pattern.size() < 4) {// LockPatternUtils.MIN_LOCK_PATTERN_SIZE
					updateStage(Stage.ChoiceTooShort);
				} else {
					mChosenPattern = new ArrayList<LockPatternView.Cell>(
							pattern);
					updateStage(Stage.FirstChoiceValid);
				}
			} else {
				throw new IllegalStateException("Unexpected stage " + mUiStage
						+ " when " + "entering the pattern.");
			}*/
			if(isConfirm == 1){
				mChosenPattern = LockPatternView.stringToPattern(SharedPreferencesTool.getPatternPassword(getContext()));
				if (mChosenPattern.equals(pattern) || mChosenPattern.size()==0) {
					if(onUnlockListener != null) {
						onUnlockListener.onUnlock(true);
					}
				}
				else {
					updateStage(Stage.UnlockWrong);
				}
			} 
			else if(isConfirm == 2){
				mChosenPattern = LockPatternView.stringToPattern(SharedPreferencesTool.getPatternPassword(getContext()));
				if (mChosenPattern.equals(pattern) || mChosenPattern.size()==0) {
					updateStage(Stage.Introduction);
					if(onUnlockListener != null){
						onUnlockListener.onUnlock(true);
					}
				}
				else {
					updateStage(Stage.UnlockWrong);
				}
			} 
			else {
				if (pattern.size() < 4) {// LockPatternUtils.MIN_LOCK_PATTERN_SIZE
					updateStage(Stage.ChoiceTooShort);
				} 
				else if(mTmpPattern == null) { // 增加回调
					mTmpPattern = new ArrayList<LockPatternView.Cell>(
							pattern);
					updateStage(Stage.FirstChoiceValid);
					mBtnCancel.setText(R.string.try_again);
					mBtnGoon.setEnabled(true);
					/*postDelayed(new Runnable() {
						@Override
						public void run() {
							updateStage(Stage.NeedToConfirm);				
						}
					}, 800);*/
				}
				else {
					if (mChosenPattern.equals(pattern)) {
						mTmpPattern = pattern;
						updateStage(Stage.ChoiceConfirmed);
						mBtnGoon.setEnabled(true);
					}
					else {
						updateStage(Stage.ConfirmWrong);
					}
				}
			}
		}

		public void onPatternCellAdded(List<Cell> pattern) {

		}

		private void patternInProgress() {
			mHeaderText.setText(R.string.lockpattern_recording_inprogress);
			if (mFooterText != null)
				mFooterText.setText("");
		}
	};

	/**
	 * Keep track internally of where the user is in choosing a pattern.
	 */
	protected enum Stage {

				Introduction(R.string.lockpattern_recording_intro_header,
				ID_EMPTY_MESSAGE, true), 
				HelpScreen(R.string.lockpattern_settings_help_how_to_record,
				ID_EMPTY_MESSAGE, false), 
				ChoiceTooShort(R.string.lockpattern_recording_incorrect_too_short,
				ID_EMPTY_MESSAGE, true), 
				FirstChoiceValid(R.string.lockpattern_pattern_entered_header, ID_EMPTY_MESSAGE,
				false), 
				NeedToConfirm(R.string.lockpattern_need_to_confirm,
				ID_EMPTY_MESSAGE, true), 
				ConfirmWrong(R.string.lockpattern_need_to_unlock_wrong, ID_EMPTY_MESSAGE,
				true), 
				UnlockWrong(
						R.string.unlockpattern_error, ID_EMPTY_MESSAGE,
						true),
				ChoiceConfirmed(R.string.lockpattern_pattern_confirmed_header,
				ID_EMPTY_MESSAGE, false);

		/**
		 * @param headerMessage
		 *            The message displayed at the top.
		 * @param leftMode
		 *            The mode of the left button.
		 * @param rightMode
		 *            The mode of the right button.
		 * @param footerMessage
		 *            The footer message.
		 * @param patternEnabled
		 *            Whether the pattern widget is enabled.
		 */
		Stage(int headerMessage, int footerMessage, boolean patternEnabled) {
			this.headerMessage = headerMessage;
			this.footerMessage = footerMessage;
			this.patternEnabled = patternEnabled;
		}

		final int headerMessage;
		final int footerMessage;
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
		if (stage == Stage.ChoiceTooShort) {
			mHeaderText.setText(getResources()
					.getString(stage.headerMessage, 4));// LockPatternUtils.MIN_LOCK_PATTERN_SIZE
		} else {
			mHeaderText.setText(stage.headerMessage);
		}
		if (stage.footerMessage == ID_EMPTY_MESSAGE && mFooterText != null) {
			mFooterText.setText("");
		} else if (mFooterText != null){
			mFooterText.setText(stage.footerMessage);
		}

		// same for whether the patten is enabled
		if (stage.patternEnabled) {
			mLockPatternView.enableInput();
		} else {
			mLockPatternView.disableInput();
		}

		// the rest of the stuff varies enough that it is easier just to handle
		// on a case by case basis.
		mLockPatternView.setDisplayMode(DisplayMode.Correct);

		switch (mUiStage) {
		case Introduction:
			mLockPatternView.clearPattern();
			break;
		case HelpScreen:
			mLockPatternView.setPattern(DisplayMode.Animate, mAnimatePattern);
			break;
		case ChoiceTooShort:
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			postClearPatternRunnable();
			break;
		case FirstChoiceValid:
			break;
		case NeedToConfirm:
			mLockPatternView.clearPattern();
			break;
		case ConfirmWrong:
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			postClearPatternRunnable();
			break;
		case UnlockWrong:
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			postClearPatternRunnable();
			break;
		case ChoiceConfirmed:
			break;
		}

		// If the stage changed, announce the header for accessibility. This
		// is a no-op when accessibility is disabled.
		if (previousStage != stage) {
			mHeaderText.announceForAccessibility(mHeaderText.getText());
		}
	}

	// clear the wrong pattern unless they have started a new one
	// already
	private void postClearPatternRunnable() {
		mLockPatternView.removeCallbacks(mClearPatternRunnable);
		mLockPatternView.postDelayed(mClearPatternRunnable,
				WRONG_PATTERN_CLEAR_TIMEOUT_MS);
	}
}
