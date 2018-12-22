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
package com.prize.cloud.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.prize.cloud.R;
import com.prize.cloud.bean.CloudAccount;
import com.prize.cloud.bean.Person;
import com.prize.cloud.util.Utils;
import com.prize.cloud.widgets.CircleImageView;
import com.prize.cloud.widgets.OutLoginDialog;

/**
 * 已登录页
 * @author huanglingjun
 * @version 1.0.0
 */
public class LogonActivity extends BaseActivity {
	private TextView mNameText;
	private TextView mIdText;
	private CircleImageView mHead_img;
	private ImageView head_img_back_id;
	private ImageView mainBackgroud;
	private TextView mTitle;
	
	private OutLoginDialog outLoginDialog;

	private static final int CANCEL = 0;
	private static final int SURE= 1;
	private static final String OUTLOGIN_ACTION = "com.prize.cloud.outlogin";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*getWindow().setStatusBarColor(
				getResources().getColor(android.R.color.transparent));*/
		setContentView(R.layout.koobee_main);	
		findViewById();
		// initData();
		mTitle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		CloudAccount cloudAccount = Utils.curAccount(this);
	}

	private void findViewById() {
		mNameText = (TextView) findViewById(R.id.nickname_id);
		mIdText = (TextView) findViewById(R.id.kb_id);
		mHead_img = (CircleImageView) findViewById(R.id.head_img_id);
		head_img_back_id = (ImageView) findViewById(R.id.head_img_back_id);
		mainBackgroud = (ImageView) findViewById(R.id.kb_main_backgroud_id);
		mTitle = (TextView) findViewById(R.id.title_id);
	}
	
	public void logout(View v) {
		CloudAccount cloudAccount = Utils.curAccount(this);
		if (cloudAccount == null) {
			return;
		}
		/*outLoginDialog = new OutLoginDialog(this,R.style.add_dialog,cloudAccount.getLoginName());
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
					
					onBackPressed();
					break;

				default:
					break;
				}
			}
		});
		outLoginDialog.show();*/
	}
	
	public void headClick(View v) {
		Intent intent = new Intent(LogonActivity.this, PersonActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
			if (!TextUtils.isEmpty(person.getAvatar())) {
				head_img_back_id
						.setBackgroundResource(R.drawable.cloud_logon_head_img);
				// x.image().bind(mHead_img, person.getAvatar());

				DisplayImageOptions options = new DisplayImageOptions.Builder()
						.cacheInMemory(true).cacheOnDisk(true).build();
				ImageLoader.getInstance().displayImage(person.getAvatar(),
						mHead_img, options);
			} else {
				head_img_back_id.setBackgroundResource(Color.TRANSPARENT);
				if((person.getSex()) == 0){
					mHead_img.setImageResource(R.drawable.cloud_woman_big);
				}else if ((person.getSex()) == 1){
					mHead_img.setImageResource(R.drawable.cloud_man);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();	
		}
	}
	
/*	@Override
	public void onBackPressed() {
		Intent intent = new Intent(getApplicationContext(), PersonalCenterActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
		super.onBackPressed();
	}*/
	
	@Override
	protected void onResume() {
		super.onResume();
		initData();
	}
}
