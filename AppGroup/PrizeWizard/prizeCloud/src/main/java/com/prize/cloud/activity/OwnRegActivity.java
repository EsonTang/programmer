/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年7月23日
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/
/**/
package com.prize.cloud.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.prize.cloud.R;
import com.prize.cloud.util.AppManager;
import com.prize.cloud.util.Utils;
import com.prize.cloud.vp.IOwnView;
import com.prize.cloud.vp.OwnPresenter;

/**
 * 本机注册
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class OwnRegActivity extends BaseActivity implements IOwnView {
	private EditText mRegistEdit;
	private TextView mNextBtn;
	private TextView mOtherBtn;
	private ImageView mShowPassword;
	private TextView mAgree;
	
	private String mTel;
	private OwnPresenter mPresenter;
		
	private boolean isAgreed = true;
	private PopupWindow pop;
	private boolean mIsPassWordShow = false;
	private TextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		AppManager.getAppManager().addActivity(this);
		mNextBtn = (TextView) findViewById(R.id.regist_next_id);
		mRegistEdit = (EditText) findViewById(R.id.passwordedit_id);
		mShowPassword = (ImageView) findViewById(R.id.password_id);
		mOtherBtn = (TextView) findViewById(R.id.regist_other_phone_id);
		mAgree = (TextView)findViewById(R.id.regist_agree_id);
		
		mNextBtn.setClickable(true);
		mNextBtn.setEnabled(false);
		mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		mTel = Utils.getTel(this);
		mPresenter = new OwnPresenter(this);
		mShowPassword.setOnClickListener(showClickListener);
		mRegistEdit.addTextChangedListener(watcher);
		setAgree();
		mOtherBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(OwnRegActivity.this, RegActivity.class));
				finish();
			}
		});
		mNextBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hideSoft();
				displayPop();
			}
		});
		mTitle = (TextView) findViewById(R.id.title_id);
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * 方法描述：设置酷比条款字体颜色和点击事件
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	private void setAgree() {
		mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cloud_agree, 0, 0, 0);
		SpannableStringBuilder builder = new SpannableStringBuilder(mAgree.getText().toString());
		ForegroundColorSpan prize_default = new ForegroundColorSpan(getResources().getColor(R.color.prize_text_default));  
		builder.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {
				if(isAgreed)
					mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cloud_register_not_agree, 0, 0, 0);
				else
					mAgree.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cloud_agree, 0, 0, 0);
				isAgreed = !isAgreed;
				enableNext();
			}
		}, 0, 0, Spannable.SPAN_INCLUSIVE_INCLUSIVE);  
		builder.setSpan(prize_default, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		builder.setSpan(new ClickableSpan() {
			
			@Override
			public void onClick(View widget) {

				Intent intent = new Intent(OwnRegActivity.this,
						WebviewActivity.class);
				if (AppManager.isThird/*||BaseApplication.isCoosea*/) {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/useragreement.html");
				} else {
					intent.putExtra(WebviewActivity.EXTRA_URL,
							"file:///android_asset/html/koobeeuseragreement.html");
				}
				intent.putExtra(WebviewActivity.EXTRA_TITLE,
						OwnRegActivity.this.getString(R.string.user_agreement));
				startActivity(intent);	
			}
			
			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				 ds.setUnderlineText(false);
			}
		}, 5, 14, Spannable.SPAN_INCLUSIVE_INCLUSIVE); 
		mAgree.setHighlightColor(Color.TRANSPARENT); 
		mAgree.append(builder);
        mAgree.setMovementMethod(LinkMovementMethod.getInstance());
		mAgree.setText(builder);
	}
	
	private OnClickListener showClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (!mIsPassWordShow) {
				mIsPassWordShow = true;
				mShowPassword.setBackgroundResource(R.drawable.cloud_hide_password_selector);
				mRegistEdit.setTransformationMethod(HideReturnsTransformationMethod
			            .getInstance());
			} else {
				mIsPassWordShow = false;
				mShowPassword.setBackgroundResource(R.drawable.cloud_show_password_selector);
				mRegistEdit.setTransformationMethod(PasswordTransformationMethod
			            .getInstance());
			}
			CharSequence text = mRegistEdit.getText();
			if (text instanceof Spannable) {
				Spannable spanText = (Spannable) text;
				Selection.setSelection(spanText, text.length());// 将光标移动到最后
			}		
		}
	};
	
	/**
	 * 下一步按钮是否可用
	 */
	private void enableNext() {
		String text = mRegistEdit.getText().toString();
		if (isAgreed && text.length()>=8 && text.length()<=16) {
			mNextBtn.setBackgroundResource(R.drawable.cloud_next_selector);
			mNextBtn.setEnabled(true);
			mNextBtn.setTextColor(getResources().getColor(R.color.login_color));

		} else {
			mNextBtn.setEnabled(false);
			mNextBtn.setTextColor(getResources().getColor(R.color.login_color_gray));
		}
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			enableNext();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}
	};
	private ProgressDialog mProgress;

	/**
	 * 注册过程中显示加载状态
	 */
	private void showProgress() {
		if (mProgress == null) {
			mProgress = new ProgressDialog(this,
					ProgressDialog.THEME_HOLO_LIGHT);
			mProgress.setMessage(this.getString(R.string.registing));
			mProgress.setCanceledOnTouchOutside(false);
			mProgress.setCancelable(true);
			mProgress.setOnKeyListener(new DialogInterface.OnKeyListener() {
				   @Override
				   public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					   if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
						   return true;
					    } else {
					    	return false; //默认返回 false
					    }
				  }
			});
		}
		mProgress.show();
	}

	private void hideProgress() {
		if (mProgress != null && mProgress.isShowing()) {
			mProgress.dismiss();
		}
	}

	@Override
	public void onPreRegister() {
		showProgress();
	}

	@Override
	public void onError(String msg) {		
		hideProgress();
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onSuccess(String data) {
		hideProgress();
		mPresenter.goRegSuccess(getApplicationContext(), mTel);
	}
	
	public void displayPop(){
		View popView = LayoutInflater.from(this).inflate(R.layout.pop_send_msg, null);
		Button cancle = (Button) popView.findViewById(R.id.cancel_id);
		Button sure = (Button) popView.findViewById(R.id.sure_id);
		cancle.setOnClickListener(popClickListener);
		sure.setOnClickListener(popClickListener);
		
		pop = new PopupWindow(popView,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		pop.setAnimationStyle(R.style.mypopwindow_anim_style);
		pop.setOutsideTouchable(true);
		pop.setBackgroundDrawable(new ColorDrawable(R.color.pop_background));
		pop.showAtLocation(mNextBtn, Gravity.BOTTOM, 0, 0);
	}

	private OnClickListener popClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.sure_id) {
				dismissPop();
				String password = mRegistEdit.getText().toString();
				if (!TextUtils.isEmpty(password))
					mPresenter.doRegister(getApplicationContext(), mTel,
							password);
			} else if (id == R.id.cancel_id) {
				dismissPop();
			}	
		}
	};
	
	public void dismissPop() {
		if (pop != null && pop.isShowing()) {
			pop.dismiss();
		}
	}
	
	public void hideSoft() {
		InputMethodManager manager = (InputMethodManager) 
				this.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(getCurrentFocus().getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
}
