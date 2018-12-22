package com.goodix.fpsetting;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.goodix.database.FpDbOperarionImpl;
import com.goodix.model.AppLockInfo;
import com.goodix.util.ConstantUtil;
import com.goodix.util.DialogUtils;

import java.util.Timer;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class AppLockPswManagerActivity extends BaseActivity {

	private ListView mOperationListView;
	private OperationListViewAdapter mOperationListViewAdapter;
	private final List<String> mOperationNameList = new ArrayList<String>();

	private static final int APP_PSW_SETTING_INDEX = 0;
	private static final int APP_PSW_DELETE_INDEX = 1;
	private static final int SHOW_DELAY_ONE_SECOND = 1000;
	private Timer mTimer = new Timer();
	private Dialog mDialog;
	private String mInPutContext;
	private FpDbOperarionImpl mDao;
	private AppLockInfo mAppLockInfo;
	private String mInPutOriginalPsw;
	private String mWrongPswPrompt;
	private TextView mDialogPromptView;
	private TextView mInPutEdit;

	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			mDialogPromptView.setText(mInPutOriginalPsw);
			mInPutEdit.getEditableText().clear();
		};
	};

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			mHandler.sendEmptyMessage(0);
		}
	};

	private TextWatcher mWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			mInPutContext = s.toString().trim();
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};

	private OnClickListener mConfirmClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mInPutContext == null || mInPutContext.length() == 0){
				mDialogPromptView.setText(mWrongPswPrompt);
				mHandler.postDelayed(mRunnable, SHOW_DELAY_ONE_SECOND);
			}else{
				if(mInPutContext.equals(mAppLockInfo.getInfo())){
					mDao.deleteAllData(ConstantUtil.APP_LOCK_INFO_TB_NAME);
					mDialog.dismiss();
				}else{
					mDialogPromptView.setText(mWrongPswPrompt);
					mHandler.postDelayed(mRunnable, SHOW_DELAY_ONE_SECOND);
				}
			}
		}
	};

	private OnClickListener mCancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mDialog.dismiss();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.app_lock_psw_manager);
		displayBackButton();
		hideRightClickArea();
		setTitleHeaderText(getResources().getString(R.string.app_lock_password_manage));
		mInPutOriginalPsw = getResources().getString(R.string.please_enter_original_password);
		mWrongPswPrompt = getResources().getString(R.string.wrong_password);;

		initData();
		initView();
	}

	private void initData() {
		String managerName = getResources().getString(R.string.set_password);
		String settingName = getResources().getString(R.string.delete_password);
		mOperationNameList.add(managerName);
		mOperationNameList.add(settingName);	
	}

	private void initView() {
		mOperationListView = (ListView) findViewById(R.id.list_view);
		mOperationListViewAdapter = new OperationListViewAdapter(this, mOperationNameList);
		mOperationListView.setAdapter(mOperationListViewAdapter);
		mOperationListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case APP_PSW_SETTING_INDEX:
					Intent settingIntent= new Intent(AppLockPswManagerActivity.this, AppLockPswSettingActivity.class);
					startActivity(settingIntent);
					break;
				case APP_PSW_DELETE_INDEX:
					// 弹出带输入框的验证密码删除框
					mDao = FpDbOperarionImpl.getInstance(AppLockPswManagerActivity.this);
					Cursor cursor = mDao.query(ConstantUtil.APP_LOCK_INFO_TB_NAME, null, null, null, null);
					if(null != cursor && cursor.moveToNext()){
						int dbId = cursor.getInt(cursor.getColumnIndex(AppLockInfo.ID));
						String info = cursor.getString(cursor.getColumnIndex(AppLockInfo.INFO));
						mAppLockInfo = new AppLockInfo(dbId, info);
					}
					if(null != cursor){
						cursor.close();
					}
					String mTitleName = getResources().getString(R.string.delete_app_lock_psw);
					mDialog = DialogUtils.createOperationDialog(AppLockPswManagerActivity.this, mTitleName, mInPutOriginalPsw, mWatcher, mConfirmClickListener, mCancelClickListener);
					mDialogPromptView = (TextView)mDialog.findViewById(R.id.prompt_title);
					mInPutEdit = (TextView)mDialog.findViewById(R.id.content_text_edit);
					mTimer.schedule(new TimerTask(){
						public void run(){
							InputMethodManager inputManager =
									(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							inputManager.showSoftInput(mInPutEdit, 0);
						}
					},200);
					break;
				}
			}
		});	
	}
}
