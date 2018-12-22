package com.android.settings.powermaster;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.settings.R;

import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Window;
import android.view.WindowManager;
import android.view.KeyEvent;
public class PrizePowerMasterActivity extends Activity {

	private static final String TAG = "PrizePowerMasterActivity";
	private ActionBar mActionBar;
	private RelativeLayout mActionBarView;
	private TextView mBackView;
	private TextView mLeftTitle;
	private TextView mMidTitle;
	private TextView mOperationButton;
	private RelativeLayout mBackClickRl;
	private RelativeLayout mOperationClickRl;
	private PowerMasterMainFragment mFragment;
	
	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	setActionBarView();
		initSateBar(this);
		setContentView(R.layout.prize_power_master_activity);

		mFragment = new PowerMasterMainFragment();
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.content_fl, mFragment);

		transaction.commitAllowingStateLoss();
		getFragmentManager().executePendingTransactions();
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

		mMidTitle.setVisibility(View.GONE);
		mOperationClickRl.setVisibility(View.GONE);
		
		mBackClickRl.setOnClickListener(mClickListener);
		mLeftTitle.setText(getResources().getString(R.string.power_save_manager_title));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		mFragment.onWindowFocusChanged(hasFocus);
		super.onWindowFocusChanged(hasFocus);
	}
	
	public void initSateBar(Activity a) {
		Window window = a.getWindow();
		window.requestFeature(Window.FEATURE_NO_TITLE);
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
			window = a.getWindow();
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
		}
	}
}
