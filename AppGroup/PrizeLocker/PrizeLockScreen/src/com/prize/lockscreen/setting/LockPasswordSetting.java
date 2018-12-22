package com.prize.lockscreen.setting;

import android.app.Activity;
import android.app.StatusBarManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.prize.lockscreen.interfaces.OnUnlockListener;
import com.prize.lockscreen.utils.SharedPreferencesTool;
import com.prize.lockscreen.widget.ConfirmPatternPwdLayout;
import com.prize.lockscreen.widget.NewPasswordTextView;
import com.prize.lockscreen.widget.PwdSetLayout;
import com.prize.lockscreen.widget.PwdSetLayout.IBottomClick;
import com.prize.prizelockscreen.R;
/***
 * 创建或修改数字or图案密码
 * @author fanjunchen
 *
 */
public class LockPasswordSetting extends Activity implements View.OnClickListener {

	private NewPasswordTextView mPassTxt;
	/** 1表示确认密码，0表示创建密码 ,2 表示解锁*/
	private int isConfirm;
	/**密码输入正确的次数*/
	private int enterRightTime = 0;
	/**数字密码*/
	private String digitalPass = null;
	
	private TextView txtHint;
	
	private PwdSetLayout setLay;
	
	private int pwdStyle = 0;
	
	com.prize.lockscreen.widget.ComplexPwdLayout mComplexPwdLayout;
	
	private OnUnlockListener onUnlockListener = new OnUnlockListener() {
		@Override
		public void onUnlock(boolean isConfirmSucces) {
			if (isConfirmSucces) {
				setResult(RESULT_OK);
			}
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			Window window = getWindow();
			/*window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
					| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN                                                                             
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION                                                                                                  
                    ); *///| View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			int color = getResources().getColor(R.color.color_title);
			window.setStatusBarColor(color);
		}
		
		isConfirm = getIntent().getIntExtra("isConfirm", 0);
		pwdStyle = getIntent().getIntExtra("style", 0);
		
		if (pwdStyle == 0) {
			setResult(RESULT_CANCELED);
			this.finish();
			return;
			
		} 
		else if (pwdStyle == 1 || pwdStyle == 3) {
			
//			setContentView(R.layout.confirm_number_password);
//			ConfirmNumberPwdLayout mConfirmNumberPwdLayout = (ConfirmNumberPwdLayout) findViewById(R.id.confirm_number_password_layout);
//			mConfirmNumberPwdLayout.setOnUnlockListener(onUnlockListener);
//			mConfirmNumberPwdLayout.setConfirm(isConfirm);
			
			digitalPass = SharedPreferencesTool.getNumberPassword(this);
			/*setContentView(R.layout.pin_set_lay);
			
			LinearLayout linView = (LinearLayout)findViewById(R.id.pin_content_lay);
			
			int color = getResources().getColor(R.color.pink);
			linView.setBackgroundColor(color);*/
			
			setContentView(R.layout.digital_pwd_set);
			
			setLay = (PwdSetLayout)findViewById(R.id.pwd_set_lay);
			setLay.setIBottomClick(mBottomClick);
			
			
			txtHint = (TextView)findViewById(R.id.txt_hint);
			TextView title = (TextView)setLay.findViewById(R.id.title);
			if (pwdStyle == 1) {
				setLay.setIsSimple(true);
				if (isConfirm == 1) {
					txtHint.setText(R.string.enter_pwd);
					setLay.findViewById(R.id.bottom_area).setVisibility(View.GONE);
					setLay.setState(0);
					setLay.setConfirm(1);
					title.setText(R.string.enter_pwd);
				} else {
					txtHint.setText(R.string.enter_digital_pass);
					setLay.setState(1);
					title.setText(R.string.title_digital_pwd);
				}
			}
			else {
				setLay.setIsSimple(false);
				if (isConfirm == 1) {
					txtHint.setText(R.string.enter_pwd);
					setLay.findViewById(R.id.bottom_area).setVisibility(View.GONE);
					setLay.setState(0);
					setLay.setConfirm(1);
					title.setText(R.string.enter_pwd);
				} else {
					txtHint.setText(R.string.enter_pwd_has_alp);
					setLay.setState(1);
					title.setText(R.string.title_complex_pwd);
				}
			}
			
			
			txtHint.setTextColor(getResources().getColor(R.color.black));
			
			/*View v = findViewById(R.id.txt_hint);
			if (v != null)
				v.setVisibility(View.GONE);*/
			
			// mPassTxt = (NewPasswordTextView)findViewById(R.id.simPinEntry);
			
			// ((PinLayout)linView).setIValidate(mValideLsn);
			
		} else if (pwdStyle == 2) {
			
			setContentView(R.layout.confirm_pattern_password);
			ConfirmPatternPwdLayout mConfirmIconPwdLayout = (ConfirmPatternPwdLayout) findViewById(R.id.confirm_icon_password_layout);
			mConfirmIconPwdLayout.setOnUnlockListener(onUnlockListener);
			mConfirmIconPwdLayout.setConfirm(isConfirm);
			
			TextView title = (TextView)mConfirmIconPwdLayout.findViewById(R.id.title);
			title.setText(R.string.title_pattern_pwd);
			
			TextView tv = (TextView) findViewById(R.id.headerText);
			tv.setTextColor(getResources().getColor(R.color.black));
			tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.font_size_15));
			mConfirmIconPwdLayout.findViewById(R.id.back).setOnClickListener(this);
			if (isConfirm == 1) {
				mConfirmIconPwdLayout.findViewById(R.id.bottom_area).setVisibility(View.INVISIBLE);
				title.setText(R.string.lockpattern_recording_intro_header);
			}
		}
		/*else if (pwdStyle == 3) {
			setContentView(R.layout.complex_pwd_lay);
			mComplexPwdLayout = 
					(com.prize.lockscreen.widget.ComplexPwdLayout) findViewById(R.id.complex_pwd_lay);
			mComplexPwdLayout.setOnUnlockListener(onUnlockListener);
			mComplexPwdLayout.setConfirm(isConfirm);
		}*/
		// 状态栏反色
		WindowManager.LayoutParams lp= getWindow().getAttributes();
        lp.statusBarInverse = StatusBarManager.STATUS_BAR_INVERSE_GRAY;
        getWindow().setAttributes(lp);
	}
	@Override
	public void onResume() {
		super.onResume();
		if (mComplexPwdLayout != null)
			mComplexPwdLayout.onResume();
		
		if (setLay != null)
			setLay.getFocus();
	}
	@Override
	public void onPause() {
		super.onPause();
		if (mComplexPwdLayout != null)
			mComplexPwdLayout.onPause();
	}
	/***
	 * 点击事件处理
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			
			case R.id.delete_button:
				if (mPassTxt != null)
					mPassTxt.deleteLastChar();
				break;
			case R.id.back:
				finish();
				break;
		}
	}
	
	/**回调接口*/
	private IBottomClick mBottomClick = new IBottomClick() {

		@Override
		public void onUnlock(boolean isConfirm) {
			if (isConfirm) {
				setResult(RESULT_OK);
			}
			finish();
		}

		@Override
		public void onBack() {
			finish();
		}

		@Override
		public void onNext() {
			
		}

		@Override
		public boolean validate(String pwd) {
			// TODO Auto-generated method stub
			if (!TextUtils.isEmpty(pwd)) {
				if (pwd.equals(digitalPass)) {
					onUnlock(true);
					return true;
				}
			}
			return false;
		}

		@Override
		public void complete(String pwd) {
			// 密码输入完成
			SharedPreferencesTool.setNumberPassword(LockPasswordSetting.this, pwd);
			if (pwdStyle == 1)
				SharedPreferencesTool.setLockPwdType(LockPasswordSetting.this, SharedPreferencesTool.LOCK_STYLE_NUMBER_PASSWORD);
			else
				SharedPreferencesTool.setLockPwdType(LockPasswordSetting.this, SharedPreferencesTool.LOCK_STYLE_COMPLEX_PASSWORD);
			onUnlock(false);
		}
	};
	@Override
	public void onBackPressed() {
		finish();
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyUp(keyCode, event);
	}
}
