/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：用戶手冊主界面
 *当前版本：v1.0
 *作	者：
 *完成日期：
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 *********************************************/
package com.freeme.operationManual.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.freeme.operationManual.R;
import com.freeme.operationManual.folder.Folder;

/**
 **
 * 用戶手冊主界面
 * 
 * @author longbaoxiu
 * @version V1.0
 */
public class MainOperationManualActivity extends Activity {
	// private static final String IS_START = "is_start";
	private RelativeLayout entranceView;
	private ListView mColumnListView;
	private List<String> mColumns = null;
	// private ImageView[] mCoverImgViews;
	private Animation mFadeOutAnimation;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message paramMessage) {
			super.handleMessage(paramMessage);
			switch (paramMessage.what) {
			default:
				MainOperationManualActivity.this.entranceView
						.startAnimation(MainOperationManualActivity.this.mFadeOutAnimation);
				sendEmptyMessageDelayed(2, 400L);
				return;
			case 2:
				MainOperationManualActivity.this.entranceView.setVisibility(8);
				return;
			case 3:
			}
			MainOperationManualActivity.this.mProgressImgView
					.startAnimation(MainOperationManualActivity.this.mRotateClockAnimation);
		}
	};
	
	private boolean mIsStart;
	private final AdapterView.OnItemClickListener mItemListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> paramAdapterView,
				View paramView, int paramInt, long paramLong) {
			String str = MainOperationManualActivity.this.mColumns
					.get(paramInt);
			if (paramInt == -1
					+ MainOperationManualActivity.this.mColumns.size()) {
				Intent localIntent1 = new Intent(
						MainOperationManualActivity.this, SearchActivity.class);
				MainOperationManualActivity.this.startActivity(localIntent1);
				return;
			}
			Intent localIntent2 = new Intent(MainOperationManualActivity.this,
					ManualItemActivity.class);
			localIntent2.putExtra("column", str);
			MainOperationManualActivity.this.startActivity(localIntent2);
		}
	};
	private final Runnable mLoadDataRunnable = new Runnable() {
		@Override
		public void run() {
			MainOperationManualActivity.this.mHandler.sendEmptyMessageDelayed(
					3, 50L);
			int i = 1000;
			long l = System.currentTimeMillis();
			new Folder(MainOperationManualActivity.this.getBaseContext())
					.unZip();
			if (System.currentTimeMillis() - l > 700L)
				i = 0;
			MainOperationManualActivity.this.mHandler.sendEmptyMessageDelayed(
					1, i);
		}
	};
	private ImageView mProgressImgView;
	private Animation mRotateClockAnimation;

	private ArrayList<String> getColumns() {
		ArrayList localArrayList = new ArrayList();
		String[] arrayOfString = getItems();
		if (arrayOfString != null)
			for (int i = 0; i < arrayOfString.length; i++)
				localArrayList.add(arrayOfString[i]);
		return localArrayList;
	}

	private List<Map<String, Object>> getData(List<String> paramList,
			int[] paramArrayOfInt) {
		ArrayList localArrayList = new ArrayList();
		for (int i = 0; i < paramList.size(); i++) {
			HashMap localHashMap = new HashMap();
			localHashMap.put("img", Integer.valueOf(paramArrayOfInt[i]));
			localHashMap.put("title", paramList.get(i));
			localArrayList.add(localHashMap);
		}
		return localArrayList;
	}

	private String[] getItems() {
		return getResources().getStringArray(2131034112);
	}

	private void initColumns() {
		this.mColumnListView = ((ListView) findViewById(R.id.columnListView));
		this.mColumns = getColumns();
		int[] arrayOfInt = new int[5];
		arrayOfInt[0] = R.drawable.quick_guide;
		arrayOfInt[1] = R.drawable.total_guide;
		arrayOfInt[2] = R.drawable.product_info;
		arrayOfInt[3] = R.drawable.search;
		SimpleAdapter localSimpleAdapter = new SimpleAdapter(this, getData(
				this.mColumns, arrayOfInt),
				R.layout.main_operation_manual_list_item, new String[] { "img",
						"title" }, new int[] { R.id.column_img,
						R.id.column_title });
		this.mColumnListView.setAdapter(localSimpleAdapter);
		this.mColumnListView.setOnItemClickListener(this.mItemListener);
	}

	private void initEntrance() {
		this.entranceView = ((RelativeLayout) findViewById(R.id.animationView));
		this.mRotateClockAnimation = AnimationUtils.loadAnimation(this,
				2130968577);
		this.mFadeOutAnimation = AnimationUtils.loadAnimation(this, 2130968576);
		this.mProgressImgView = ((ImageView) findViewById(R.id.loading_progress));
		if (!this.mIsStart)
			this.entranceView.setVisibility(View.GONE);
	}

	private void initView() {
		initEntrance();
		initColumns();
	}

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.main_operation_manual);
		this.mIsStart = getIntent().getBooleanExtra("is_start", true);
		initView();
		new Thread(this.mLoadDataRunnable).start();
	}

}