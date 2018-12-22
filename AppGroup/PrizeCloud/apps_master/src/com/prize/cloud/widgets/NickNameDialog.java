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

import com.prize.cloud.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NickNameDialog extends AlertDialog implements android.view.View.OnClickListener{
	public EditText mEdit;
	private Button mCancel;
	private Button mSure;
	private String oldNickName;
	private Context context;
	private static final int CANCEL = 0;
	private static final int SURE= 1;
	
	public NickNameDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public NickNameDialog(Context context, int theme,String oldNickName) {
		super(context, theme);
		this.oldNickName = oldNickName;
		this.context = context;
		//必须在setcontentView之前不然软键盘弹出被对话框遮挡
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_nickname, null);
		setView(view,0,0,0,0);
	}

	@Override  
	protected void onCreate(Bundle savedInstanceState) {  
		 super.onCreate(savedInstanceState);  
		 setContentView(R.layout.dialog_nickname);  
		 mSure = (Button) findViewById(R.id.sure_id);
		 mCancel = (Button) findViewById(R.id.cancel_id);
		 mEdit = (EditText) findViewById(R.id.edit_nickname_id);
		 
		 mEdit.setText(oldNickName);
		 mSure.setOnClickListener(this);
		 mCancel.setOnClickListener(this);
	} 
	
	public void setEditText(String oldNickName){
		mEdit.setText(oldNickName);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.sure_id:
				String newNickName = mEdit.getText().toString().trim();
				if (!TextUtils.isEmpty(newNickName)) {
					nameInfo.onClick(SURE,newNickName);
				} else {
					Toast.makeText(context, context.getString(R.string.null_nickname), Toast.LENGTH_SHORT).show();
				}
				break;
				
			case R.id.cancel_id:
				nameInfo.onClick(CANCEL,null);
				break;
				
			default:
				break;
		}
		
	}
	
   public static interface NickNameInfo{
	   void onClick(int which,String newNickName);
   }
   
   public NickNameInfo nameInfo;
   
	public void setNickNameInfo(NickNameInfo nameInfo) {
		this.nameInfo = nameInfo;
	}
  
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		mEdit.selectAll();
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
		InputMethodManager inputmanger2 = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger2.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
		super.dismiss();
	}
}
