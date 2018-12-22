/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
 *完成日期：2015年8月3日
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
package com.prize.boot;

//import com.prize.cloud.helper.PrizeAccount;
//import com.prize.cloud.helper.PrizeHelper;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class OtherSetActivity extends AbstractGuideActivity {
	/** Broadcast intent action when the location mode is about to change. */
    private static final String MODE_CHANGING_ACTION =
            "com.android.settings.location.MODE_CHANGING";
    private static final String CURRENT_MODE_KEY = "CURRENT_MODE";
    private static final String NEW_MODE_KEY = "NEW_MODE";
	/**位置服务**/
	private Switch mLocationSwitch;
	/** 用户体验**/
	private Switch mUeSwitch;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_other);
		findViews();
		setGuideTitle(R.drawable.set_icon, R.string.other_setting);
	}
	
	private void findViews() {
		mLocationSwitch = (Switch) findViewById(R.id.st_location);
		mUeSwitch = (Switch) findViewById(R.id.st_ue);
		mUeSwitch.setVisibility(View.GONE);
		
		
		int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
		if(mode == Settings.Secure.LOCATION_MODE_OFF){
			mLocationSwitch.setChecked(false);
		}else{
			mLocationSwitch.setChecked(true);
		}

		mLocationSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
				}else{
					Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
				}
			}
		});
		
	}
	
	public void onClick(View v) {
		if (v.getId() == R.id.next_btn) {
//			startActivity(new Intent(this, SetOverActivity.class));
//			finish();
			nextStep(true);
		} else if (v.getId() == R.id.im_back) {
			nextStep(false);
		}
	}
}
