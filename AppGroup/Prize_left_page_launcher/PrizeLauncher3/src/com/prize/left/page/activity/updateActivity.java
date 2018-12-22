package com.prize.left.page.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.android.launcher3.R;
import com.prize.left.page.bean.AppInfoBean;
import com.prize.left.page.model.LeftModel;
import com.prize.left.page.ui.UpdateSelfDialog;
import com.prize.left.page.util.ClientInfo;
import com.prize.left.page.util.IConstants;
import com.prize.left.page.util.PreferencesUtils;

public class updateActivity extends Activity implements OnClickListener {

	private UpdateSelfDialog mUpdateSelfDialog;

	private AppInfoBean bean;
	/**延迟刷新的时间间隔 2小时*/
	private final long BETWEEN_TIME = 1000 * 60 * 60 * 24;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.update_activity);
		bean = LeftModel.getInstance().getUpgradApp();
		if (mUpdateSelfDialog == null && bean != null) {
			/*mUpdateSelfDialog = new UpdateSelfDialog(this, R.style.add_dialog,
					ClientInfo.getInstance(this).appVersionCode, this
							.getResources()
							.getString(R.string.new_version_name,
									bean.versionName), bean.updateInfo);*/
			mUpdateSelfDialog = UpdateSelfDialog.getInstance(this, R.style.add_dialog,
					ClientInfo.getInstance(this).appVersionCode, this
							.getResources()
							.getString(R.string.new_version_name,
									bean.versionname), bean.updateinfo);
			mUpdateSelfDialog.setBean(bean);

		}
		if (mUpdateSelfDialog != null && !mUpdateSelfDialog.isShowing()) {
			mUpdateSelfDialog.show();
			mUpdateSelfDialog.findViewById(R.id.add_neg).setOnClickListener(
					this);
			mUpdateSelfDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == 4) {
						mUpdateSelfDialog.dismiss();
						finish();
						return true;
					}
					return false;
				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.add_neg:
			if (mUpdateSelfDialog != null && mUpdateSelfDialog.isShowing()) {
				mUpdateSelfDialog.dismiss();
				finish();
				PreferencesUtils.putLong(this, IConstants.KEY_DELAY_CHECK_TIME, System.currentTimeMillis()+BETWEEN_TIME);
			}
			break;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
}
