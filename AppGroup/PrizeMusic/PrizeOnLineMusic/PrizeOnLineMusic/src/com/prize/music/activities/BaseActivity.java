/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：
 *作	者：
 *完成日期：
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

package com.prize.music.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.prize.app.util.SDKUtil;
import com.prize.app.xiami.RequestManager;
import com.umeng.analytics.MobclickAgent;
import com.xiami.sdk.XiamiSDK;

public class BaseActivity extends FragmentActivity {
	/**
	 * XiamiSDK
	 */
	protected XiamiSDK xiamiSDK;
	protected RequestManager requestManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//41518 【本地音乐】进入音乐按home键退出更改字体大小再进入各音乐列表时界面显示异常（100%，附视频）liukun
		if(null!=savedInstanceState) {
			savedInstanceState=null;
		}
		xiamiSDK = new XiamiSDK(getApplicationContext(), SDKUtil.KEY,
				SDKUtil.SECRET);
		requestManager = RequestManager.getInstance();
		super.onCreate(savedInstanceState);
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
}
