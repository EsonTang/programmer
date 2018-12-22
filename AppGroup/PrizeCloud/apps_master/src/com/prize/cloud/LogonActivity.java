/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：huanglingjun
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
package com.prize.cloud;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.bean.Person;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.CircleImageView;
import com.prize.cloud.widgets.OutLoginDialog;
import com.prize.cloud.widgets.OutLoginDialog.OutLoginInfo;

/**
 * 已登录页
 * @author huanglingjun
 * @version 1.0.0
 */
public class LogonActivity extends BaseActivity {
	@ViewInject(R.id.nickname_id)
	private TextView mNameText;
	@ViewInject(R.id.kb_id)
	private TextView mIdText;
	@ViewInject(R.id.head_img_id)
	private CircleImageView mHead_img;
	@ViewInject(R.id.head_img_back_id)
	private ImageView head_img_back_id;
	@ViewInject(R.id.kb_main_backgroud_id)
	private ImageView mainBackgroud;
	@ViewInject(R.id.title_id)
	private TextView mTitle;
	
	private OutLoginDialog outLoginDialog;

	private static final int CANCEL = 0;
	private static final int SURE= 1;
	public static final String OUTLOGIN_ACTION = "com.prize.cloud.outlogin";
	public static final String RELOGIN_ACTION = "com.prize.cloud.relogin";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setStatusBarColor(
				getResources().getColor(android.R.color.transparent));
		setContentView(R.layout.koobee_main);	
		ViewUtils.inject(this);
		initData();
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		CloudAccount cloudAccount = Utils.curAccount(this);
	}

	@OnClick(R.id.back_loginbtn_id)
	public void logout(View v) {
		CloudAccount cloudAccount = Utils.curAccount(this);
		if (cloudAccount == null) {
			startActivity(new Intent(LogonActivity.this, MainActivity.class));
			return;
		}
		outLoginDialog = new OutLoginDialog(this,R.style.add_dialog,cloudAccount.getLoginName());
		outLoginDialog.setOutLoginInfo(new OutLoginInfo() {
			
			@Override
			public void onClick(int which, String password) {
				switch (which) {
				case CANCEL:
					dismissDialog();
					break;
					
				case SURE:	
					dismissDialog();
					LocalBroadcastManager.getInstance(getApplicationContext())
						.sendBroadcast(new Intent(EXITAPP));
					Utils.logout(LogonActivity.this);
					
					//sendBroadCast();
					Intent broadIntent = new Intent(OUTLOGIN_ACTION);
					sendBroadcast(broadIntent);
					String type  = getIntent().getStringExtra("OnceTask");
					if ((!TextUtils.isEmpty(type)) && type.equals("OnceTask")) {
						finish();
						return;
					}
					
					startActivity(new Intent(LogonActivity.this, MainActivity.class));
					break;

				default:
					break;
				}
			}
		});
		outLoginDialog.show();
	}

	/**
	 * 方法描述：发送退出广播
	 */
	public void sendBroadCast (){
		Intent broadIntent = new Intent(OUTLOGIN_ACTION);
		sendBroadcast(broadIntent);
		String type  = getIntent().getStringExtra("OnceTask");
		if ((!TextUtils.isEmpty(type)) && type.equals("OnceTask")) {
			onDestroy();
		}
	}
	
	@OnClick(R.id.kb_main_backgroud_id)
	public void headClick(View v) {
		Intent intent = new Intent(LogonActivity.this, PersonActivity.class);
		this.startActivityForResult(intent, 1000);
	}
	
	private void dismissDialog(){
		if (outLoginDialog != null && outLoginDialog.isShowing()){
			outLoginDialog.dismiss();
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == 1001) {
        	initData();
        }
    }
	/**
	 * 方法描述：初始化数据
	 * @return void
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public void initData() {
		Person person = Utils.getPersonalInfo(this);
		if (person == null) return;
		try {
			if(TextUtils.isEmpty(person.getRealName())){
				mNameText.setText(person.getUserId());
			} else {
				mNameText.setText(person.getRealName());
			} 
			mIdText.setText(person.getUserId());
			if(!TextUtils.isEmpty(person.getAvatar())){
				head_img_back_id.setBackgroundResource(R.drawable.logon_head_img);
				BitmapUtils bitmapUtils = new BitmapUtils(this);
				bitmapUtils.display(mHead_img, person.getAvatar());
			}else{
				head_img_back_id.setBackgroundResource(Color.TRANSPARENT);
				if((person.getSex()) == 0){
					mHead_img.setImageResource(R.drawable.woman_big);
				}else if ((person.getSex()) == 1){
					mHead_img.setImageResource(R.drawable.man);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
}
