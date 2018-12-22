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
package com.prize.cloud.widgets;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.prize.cloud.R;
import com.prize.cloud.activity.LostPswdActivity;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.util.Utils;

public class OutLoginDialog extends Dialog implements android.view.View.OnClickListener{
	public EditText mEdit;
	private Button mCancel;
	private Button mSure;
	private Context context;
	private ImageView mShowPassword;
	private TextView mForgetPassword;
	private TextView mAccount;
	private String account;
	private boolean mIsPassWordShow = false;
	
	private static final int CANCEL = 0;
	private static final int SURE= 1;
	
	private String mPassword;
	protected OutLoginDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		
		super(context, cancelable, cancelListener);
		this.context = context;
		// TODO Auto-generated constructor stub
		
	}

	public OutLoginDialog(Context context) {
		
		
		super(context);
		this.context = context;
		// TODO Auto-generated constructor stub
		
	}

	public OutLoginDialog(Context context, int theme) {
		
		super(context, theme);
		this.context = context;
		// TODO Auto-generated constructor stub
		
	}

	/*public OutLoginDialog(Context context, int theme,String account) {
		super(context, theme);
		this.context = context;
		this.account = account;
		//必须在setcontentView之前不然软键盘弹出被对话框遮挡
		//View view = LayoutInflater.from(context).inflate(R.layout.dialog_log_out, null);
		//setView(view,0,0,0,0);
	}*/

	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
		 super.onCreate(savedInstanceState); 
		 CloudAccount cloudAccount = Utils.curAccount(context);
			if (cloudAccount == null) {
				Toast.makeText(context,this.context.getResources().getString(R.string.unknwon_error) ,
						Toast.LENGTH_SHORT).show();
				Utils.logout(context);
				return;
			}
		 this.account = cloudAccount.getLoginName();
		 setContentView(R.layout.dialog_log_out);  
		 mSure = (Button) findViewById(R.id.sure_id);
		 mCancel = (Button) findViewById(R.id.cancel_id);
		 mEdit = (EditText) findViewById(R.id.passwordedit_id);
		 mShowPassword = (ImageView) findViewById(R.id.password_id);
		 mForgetPassword = (TextView) findViewById(R.id.forget_password_id);
		 mAccount = (TextView) findViewById(R.id.account_id);
		
		 
		 mSure.setOnClickListener(this);
		 mCancel.setOnClickListener(this);
		 mShowPassword.setOnClickListener(this);
		 mForgetPassword.setOnClickListener(this);
		 
		 if(account != null)
			 mPassword = cloudAccount.getPassword();
		 	mAccount.setText(account);
	} 

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (id == R.id.sure_id) {
			String password = mEdit.getText().toString().trim();
			if(TextUtils.isEmpty(password)){
				Toast.makeText(context,context.getString(R.string.null_password), Toast.LENGTH_SHORT).show();
			}else if(password.length()<8){
				Toast.makeText(context,context.getString(R.string.not_enough_password), Toast.LENGTH_SHORT).show();
			}else if(!TextUtils.isEmpty(mPassword) && password.equalsIgnoreCase(mPassword)){
				Utils.logout(context);
				this.dismiss();
				//outLoginInfo.onClick(SURE,password);
			}else{
				Toast.makeText(context,context.getString(R.string.wrong_pswd), Toast.LENGTH_SHORT).show();
			}
		} else if (id == R.id.cancel_id) {
			this.dismiss();
			//outLoginInfo.onClick(CANCEL,null);
		} else if (id == R.id.password_id) {
			if (!mIsPassWordShow) {
				mIsPassWordShow = true;
				mShowPassword.setImageResource(R.drawable.cloud_hide_password_selector);
				mEdit.setTransformationMethod(HideReturnsTransformationMethod
			            .getInstance());
			} else {
				mIsPassWordShow = false;
				mShowPassword.setImageResource(R.drawable.cloud_show_password_selector);
				mEdit.setTransformationMethod(PasswordTransformationMethod
			            .getInstance());
			}
			CharSequence text = mEdit.getText();
			if (text instanceof Spannable) {
				Spannable spanText = (Spannable) text;
				Selection.setSelection(spanText, text.length());// 将光标移动到最后
			}
		} else if (id == R.id.forget_password_id) {
			Intent intent = new Intent(context, LostPswdActivity.class);
			context.startActivity(intent);
			dismiss();
		}
		
	}
	
   public static interface OutLoginInfo{
	   void onClick(int which,String password);
   }
   
   public OutLoginInfo outLoginInfo;
   
	public void setOutLoginInfo(OutLoginInfo outLoginInfo) {
		this.outLoginInfo = outLoginInfo;
	}
  
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		mEdit.requestFocus();
		Timer timer=new Timer();
		timer.schedule(new TimerTask() {
					
			@Override
			public void run() {
				InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
				inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 300);
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		InputMethodManager inputmanger = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
		super.dismiss();
	}
}
