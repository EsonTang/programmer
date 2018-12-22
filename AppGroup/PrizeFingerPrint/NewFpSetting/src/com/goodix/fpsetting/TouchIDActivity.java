package com.goodix.fpsetting;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.goodix.application.FpApplication;
import com.goodix.application.FpApplication.ServiceConnectCallback;
import com.goodix.database.FpDbOperarionImpl;
import com.goodix.model.FpInfo;
import com.goodix.service.FingerAdapter;
import com.goodix.service.FingerprintManager;
import com.goodix.service.FingerprintManager.VerifySession;
import com.goodix.util.ConstantUtil;
import com.goodix.util.DialogUtils;
import com.goodix.util.Fingerprint;
import com.goodix.util.L;
import com.goodix.util.ToastUtil;

public class TouchIDActivity extends BaseActivity implements ServiceConnectCallback{

	public static final String TAG = "TouchIDActivity";
	public static final String FRINGERPRINT_URI = "fp_uri";
	public static final String FRINGERPRINT_INDEX = "fp_uri_index";
	/* Database service */
	private int mEditorIndex = -1;

	private ArrayList<Fingerprint> mDataList = null;

	private VerifySession mSession = null;

	public static TouchIDActivity instance = null;

	private Dialog mOperationDialog;

	private ListView mFingerListView;

	private FingerAdapter mFingerListViewAdapter;

	private TextView mAddFpEntry;
	private FpDbOperarionImpl mDao;

	private TextWatcher mSLFpsvcFPTextWatcher = new TextWatcher() {
		public void afterTextChanged(Editable s) {
			Fingerprint fp = mDataList.get(mEditorIndex);
			fp.setName(s.toString());
			ContentValues values = new ContentValues();
			values.put(FpInfo.ID, fp.getKey());
			values.put(FpInfo.NAME, fp.getName());
			values.put(FpInfo.DESCRIPTION, fp.getDescription());
			values.put(FpInfo.URI, fp.getUri());
			String[] selectionArgs = new String[]{String.valueOf(fp.getKey())};
			String where = FpInfo.ID + " = ? ";
			mDao.update(ConstantUtil.FP_INFO_TB_NAME,where,selectionArgs,values);
			mFingerListViewAdapter.notifyDataSetChanged();
		}
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}
	};

	private OnClickListener mDeleteClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Fingerprint fp = mDataList.get(mEditorIndex);
			String[] selectionArgs = new String[]{String.valueOf(fp.getKey())};
			String where = FpInfo.ID + " = ? ";
			mDao.delete(ConstantUtil.FP_INFO_TB_NAME,where,selectionArgs);
			try {
				int index = Integer.parseInt(mDataList.get(mEditorIndex).getUri());
				FpApplication.getInstance().getFpServiceManager().delete(index);
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
				e.printStackTrace();
			}
			mDataList.remove(mEditorIndex);
			mFingerListViewAdapter.notifyDataSetChanged();
			mOperationDialog.dismiss();
			if (mDataList.size() == 0) {
				SystemProperties.set("persist.sys.prize_fp_enable", "0");
			} 
		}
	};

	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			mAddFpEntry.setEnabled(true);
			updateFingerprintItemsView();
		};
	};
	private FpApplication mApplication;
	private FingerprintManager mManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSubContentView(R.layout.activity_touchid);
		displayBackButton();
		setTitleHeaderText(getResources().getString(R.string.fingerprint_manager));

		instance = this;

		updateView();
		mDao = FpDbOperarionImpl.getInstance(this);
		mApplication = FpApplication.getInstance();
		
		if(mApplication.isFpServiceManagerEmpty()){
			mApplication.setCallback(this);
			mManager = mApplication.getFpServiceManager();
		}else{
			mManager = mApplication.getFpServiceManager();
			initDate();
		}
	}

	private void updateView() {
		mFingerListView = (ListView) findViewById(R.id.fp_finger_list);
		mFingerListView.setOnItemClickListener(new FpOnItemClickListener());

		TextView headLine = new TextView(this);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
		headLine.setLayoutParams(params);
		headLine.setBackgroundResource(R.drawable.list_view_divier);
		mFingerListView.addHeaderView(headLine);

		mAddFpEntry = (TextView) findViewById(R.id.add_fingerprint_entry);
		mAddFpEntry.setOnClickListener(new OnAddBtnOnClickListener());
		mAddFpEntry.setEnabled(false);
	}

	private class FpOnItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mEditorIndex = position-1;
			mOperationDialog = DialogUtils.createOperationDialog(TouchIDActivity.this, mDataList.get(mEditorIndex).getName(), 
					mSLFpsvcFPTextWatcher, mDeleteClickListener);
			mOperationDialog.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					int count = mDataList.size();
					if(count >= 5) {
						mAddFpEntry.setEnabled(false);
						mAddFpEntry.setTextColor(getResources().getColor(R.color.half_gray_one));
					}else if(count >= 0 && count <= 4){
						mAddFpEntry.setEnabled(true);
						mAddFpEntry.setTextColor(getResources().getColor(R.color.half_apple_blue));
					}
				}
			});

		}
	}

	@Override
	protected void onPause() {
		L.d("TouchiIdACTIVITY : onPause");
		super.onPause();
		ToastUtil.cancelToast();
	}

	@Override
	protected void onResume() {
		L.d("TouchiIdACTIVITY : onResume");
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK: {
			return super.onKeyDown(keyCode, event);
		}
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onStop() {
		Log.v(TAG, "TouchIDActivity : onStop");
		L.d("TouchIDActivity : onStop");
		super.onStop();
		instance = null;
		if (null != mSession) {
			mSession.exit();
		}
	}

	@Override
	public void onActivityResult(int request_code, int resultcode, Intent intent) {
		super.onActivityResult(request_code, resultcode, intent);
		if (resultcode == RESULT_OK)
			onCaptureActivityResult(request_code, resultcode, intent);
	}

	public void onCaptureActivityResult(int request_code, int resultcode,
			Intent intent) {
		if (resultcode == Activity.RESULT_OK) {
			Time time = new Time();
			time.setToNow();

			String name, description, uri;
			int mKey = getKey(mDataList);

			name = TouchIDActivity.this.getString(R.string.list_item) + " " + Integer.toString(mKey);

			description = "Description(" + time.hour + ":" + time.minute + ":" + time.second + ")";
			uri = intent.getStringExtra(TouchIDActivity.FRINGERPRINT_URI);

			Fingerprint fp = new Fingerprint(mKey, name, description, uri);
			ContentValues values = new ContentValues();
			values.put(FpInfo.ID, fp.getKey());
			values.put(FpInfo.NAME, fp.getName());
			values.put(FpInfo.DESCRIPTION, fp.getDescription());
			values.put(FpInfo.URI, fp.getUri());
			if (mDao.insert(ConstantUtil.FP_INFO_TB_NAME, values) == 0) {
				mDataList.add(fp);

				int count = mDataList.size();
				if(count >= 5) {
					mAddFpEntry.setEnabled(false);
					mAddFpEntry.setTextColor(getResources().getColor(R.color.half_gray_one));
				}else if(count >= 0 && count < 5){
					mAddFpEntry.setEnabled(true);
					mAddFpEntry.setTextColor(getResources().getColor(R.color.half_apple_blue));
				}

				if (count > 0) {
					SystemProperties.set("persist.sys.prize_fp_enable", "1");
				} 

				if(null != mFingerListViewAdapter){
					mFingerListViewAdapter.notifyDataSetChanged();
				}else{
					mFingerListViewAdapter = new FingerAdapter(this, mDataList);
					mFingerListView.setAdapter(mFingerListViewAdapter);
				}
			} else {
				ToastUtil.showToast(this,getResources().getString(R.string.addfinger_faied_toast));
			}
		}
	}

	private void setFpAble(int count){
		if (count == 0) {
			SystemProperties.set("persist.sys.prize_fp_enable", "0");
		}else{
			SystemProperties.set("persist.sys.prize_fp_enable", "1");
		}
	}

	private int getKey(ArrayList<Fingerprint> dataList) {
		int mKey = 0;
		for (int i = 0; i < dataList.size(); i++)
			mKey = Math.max(dataList.get(i).getKey(), mKey);
		return ++mKey;
	}

	private void updateFingerprintItemsView() {
		if (null == mDataList) {
			return;
		}
		int count = mDataList.size();
		if(count >= 5) {
			mAddFpEntry.setEnabled(false);
			mAddFpEntry.setTextColor(getResources().getColor(R.color.half_gray_one));
		}else if(count >= 0 && count < 5){
			mAddFpEntry.setEnabled(true);
			mAddFpEntry.setTextColor(getResources().getColor(R.color.half_apple_blue));
		}
		mFingerListViewAdapter = new FingerAdapter(this, mDataList);
		mFingerListView.setAdapter(mFingerListViewAdapter);
		mFingerListViewAdapter.notifyDataSetChanged();

	}

	private class OnAddBtnOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(TouchIDActivity.this,RegisterActivity.class);
			intent.putParcelableArrayListExtra(FRINGERPRINT_INDEX, mDataList);
			startActivityForResult(intent, 3);
		}
	}

	private void initDate(){
		new Thread(){
			public void run() {
				mDataList = new ArrayList<Fingerprint>();
				Cursor cursor = mDao.query(ConstantUtil.FP_INFO_TB_NAME,null,null,null,null);

				Fingerprint fp = null;
				while (null != cursor && cursor.moveToNext()){
					fp = new Fingerprint(cursor.getInt(0), cursor.getString(1),cursor.getString(2), cursor.getString(3));
					mDataList.add(fp);
				};
				if(null != cursor){
					cursor.close();
				}
				mDataList = loadData(mDataList,mManager.query());
				mHandler.sendEmptyMessage(10);
			};
		}.start();
	}

	private ArrayList<Fingerprint> loadData(ArrayList<Fingerprint> dataList,
			int fpFlag) {
		if (null == dataList || (fpFlag >> 16) <= 0) {
			return null;
		}
		ArrayList<Fingerprint> tempList = new ArrayList<Fingerprint>();
		int mKey = getKey(dataList);
		int count = (fpFlag >> 16 & 0xFFFF);
		boolean[] bRegister = new boolean[count];

		for (int i = 0; i < count; i++) {
			bRegister[i] = (((fpFlag >> i) & 0x1) > 0) ? true : false;
			if (bRegister[i] == true) {
				boolean bFind = false;
				for (int j = 0; j < dataList.size(); j++) {
					if (Integer.parseInt(dataList.get(j).getUri()) == i + 1) {
						Fingerprint fp = dataList.remove(j);
						tempList.add(fp);
						bFind = true;
						break;
					}
				}
				if (bFind == false) {
					Fingerprint fp = new Fingerprint(mKey, TouchIDActivity.this.getString(R.string.list_item) + " " + Integer.toString(i + 1),
							TouchIDActivity.this.getString(R.string.list_item), Integer.toString(i + 1));
					mKey++;
					tempList.add(fp);
					ContentValues values = new ContentValues();
					values.put(FpInfo.ID, fp.getKey());
					values.put(FpInfo.NAME, fp.getName());
					values.put(FpInfo.DESCRIPTION, fp.getDescription());
					values.put(FpInfo.URI, fp.getUri());
					mDao.insert(ConstantUtil.FP_INFO_TB_NAME,values);
				}
			}
		}

		for (int i = 0; i < dataList.size(); i++) {
			String[] selectionArgs = new String[]{String.valueOf(dataList.get(i).getKey())};
			String where = FpInfo.ID + " = ? ";
			mDao.delete(ConstantUtil.FP_INFO_TB_NAME,where,selectionArgs);
		}
		int listCount = tempList.size();
		setFpAble(listCount);
		return tempList;
	}

	@Override
	public void serviceConnect() {
		mManager = mApplication.getFpServiceManager();
		initDate();
	}
}
