package com.android.settings.powermaster;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;

public class CPUModeDetailsActivity extends Activity {

	private TextView mCPUNotice;
	private ListView mListView;
	private ActionBar mActionBar;
	private TextView mBackView;
	private TextView mLeftTitle;
	private TextView mMidTitle;
	private TextView mOperationButton;
	private RelativeLayout mActionBarView;

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mBackView.setPressed(true);
			CPUModeDetailsActivity.this.finish();
		}
	};
	private RelativeLayout mBackClickRl;
	private RelativeLayout mOperationClickRl;
	private CPUModeDetailsAdapter mAdapter;
	private int mModeType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prize_power_mastercpu_modedetails_activity);
		getWindow().setStatusBarColor(getResources().getColor(R.color.settings_layout_background));
		initViews();
		setActionBarView();
	}

	private void setActionBarView(){
		mActionBar = getActionBar();
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.power_master_action_bar_layout);
		mActionBarView = (RelativeLayout) mActionBar.getCustomView();

		mBackClickRl = (RelativeLayout) mActionBarView.findViewById(R.id.back_click_rl);
		mOperationClickRl = (RelativeLayout) mActionBarView.findViewById(R.id.operation_click_rl);

		mBackView = (TextView) mActionBarView.findViewById(R.id.back_button);
		mLeftTitle = (TextView) mActionBarView.findViewById(R.id.left_title);
		mMidTitle = (TextView) mActionBarView.findViewById(R.id.mid_title);
		mOperationButton = (TextView) mActionBarView.findViewById(R.id.operation_button);

		mBackClickRl.setOnClickListener(mClickListener);

		//mMidTitle.setVisibility(View.GONE);
		mLeftTitle.setVisibility(View.GONE);
		mOperationClickRl.setVisibility(View.GONE);

		mModeType = getIntent().getIntExtra(PowerMasterUtil.CPU_MODE_TYPE, 1);
		String mTitle = null;
		switch (mModeType) {
		case 0:
			mTitle = getResources().getString(R.string.fast_function_title);
			break;
		case 1:
			mTitle = getResources().getString(R.string.normal_mode_title);
			break;
		case 2:
			mTitle = getResources().getString(R.string.noopsych_save_title);
			break;
		}
		if(mTitle == null){
			finish();
		} else {
			//mLeftTitle.setText(mTitle);
			mMidTitle.setText(mTitle);
		}
	}

	private void initViews() {
		mCPUNotice = (TextView) findViewById(R.id.cpu_mode_notice);
		mListView = (ListView) findViewById(R.id.cpu_mode_listview);

		mCPUNotice.setText(getResources().getString(R.string.return_to_normal_mode_optimization));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(hasFocus){
			mModeType = getIntent().getIntExtra(PowerMasterUtil.CPU_MODE_TYPE, 1);

			String[] mModeNameArr = getResources().getStringArray(R.array.mode_name);
			String[] mModeStatusArr = CPUModeUtils.getModeState(this,mModeType);
			
			int count = mModeStatusArr.length;
			
			String[] mModeName = new String[count-1];
			String[] mModeStatus = new String[count-1];
			for(int i = 1;i < count;i++){
				mModeName[i-1] = mModeNameArr[i];
				mModeStatus[i-1] = mModeStatusArr[i];
			}
			
			
			mAdapter = new CPUModeDetailsAdapter(this, mModeType, mModeName, mModeStatus);
			mListView.setAdapter(mAdapter);
		}
		super.onWindowFocusChanged(hasFocus);
	}


}
