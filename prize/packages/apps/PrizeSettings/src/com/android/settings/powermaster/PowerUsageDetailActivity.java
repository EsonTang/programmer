package com.android.settings.powermaster;

import com.android.settings.SettingsActivity;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;

public class PowerUsageDetailActivity extends SettingsActivity {
	private ActionBar mActionBar;
	private RelativeLayout mActionBarView;
	private TextView mBackView;
	private TextView mLeftTitle;
	private TextView mMidTitle;
	private TextView mOperationButton;
	private RelativeLayout mBackClickRl;
	private RelativeLayout mOperationClickRl;
	
	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.back_click_rl:
				mBackView.setPressed(true);
				finish();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("");
		setActionBarView();
	}

	private void setActionBarView(){
		mActionBar = getActionBar();
		mActionBar.setCustomView(null);
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
		
		mMidTitle.setVisibility(View.GONE);
		mOperationClickRl.setVisibility(View.GONE);
		
		mLeftTitle.setText(getResources().getString(R.string.power_use_details_title));
	}
	
	@Override
	public boolean onNavigateUp() {
		finish();
		return true;
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		Log.d("PowerUsageDetailActivity", "Launching fragment " + fragmentName);
		return true;
	}
}
