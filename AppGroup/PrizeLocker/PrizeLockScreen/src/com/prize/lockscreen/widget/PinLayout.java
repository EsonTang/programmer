package com.prize.lockscreen.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.prize.lockscreen.utils.AnimationUtil;
import com.prize.prizelockscreen.R;
/***
 * 数字密码输入布局
 * @author fanjunchen
 *
 */
public class PinLayout extends LinearLayout {

	private TextView mTxtHint;
	/**密码显示控件*/
	private NewPasswordTextView mPwdTxt;
	/**密码校验*/
	private IValidate mValidateLsn;
	
	public PinLayout(Context context) {
		this(context, null);
	}

	public PinLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PinLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public PinLayout(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		
		init();
	}

	/***
	 * 初始化数据
	 */
	private void init() {
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
		mTxtHint = (TextView) findViewById(R.id.txt_hint);
		mPwdTxt = (NewPasswordTextView) findViewById(R.id.simPinEntry);
		
		mPwdTxt.setOnChangeListener(mChangeLsn);
		
		View tmp = findViewById(R.id.delete_button);
		if (tmp != null)
			tmp.setOnClickListener(mClickLsn);
	}
	
	public void setIValidate(IValidate l) {
		mValidateLsn = l;
	}
	
	public void setTriggerLen(int len) {
		if (mPwdTxt != null)
			mPwdTxt.setTriggerLen(len);
	}
	
	private View.OnClickListener mClickLsn = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
				case R.id.delete_button:
					if (mPwdTxt != null)
						mPwdTxt.deleteLastChar();
					break;
			}
		}
	}; 
	
	private NewPasswordTextView.OnContentChange mChangeLsn = new NewPasswordTextView.OnContentChange() {
		@Override
		public void onChange(String str) {
			// 判断是否相等,若相等则怎么处理
			if (null == mValidateLsn)
				return;
			
			if (!mValidateLsn.validate(str)) {
				mTxtHint.setText(R.string.enter_pwd_error);
				shake();
			}
			else 
				mTxtHint.setText(R.string.enter_pwd);
		}
	};
	/***
	 * 密码控件在摇动
	 */
	public void shake() {
		mPwdTxt.startAnimation(AnimationUtil.shakeAnimation(5, new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				mPwdTxt.reset(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			}
		)
		);
	}
	/***
	 * 校验接口
	 * @author june
	 *
	 */
	public interface IValidate {
		/***
		 * 校验是否通过
		 * @param pwd
		 * @return
		 */
		boolean validate(String pwd);
	}
}
