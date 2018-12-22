/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：
 *当前版本：1.0.0
 *作	者：yiyi
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
package com.prize.cloud;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 应用启动页，在这里判断是否是应用第一次启动，然后进行相应跳转
 * 
 * @author yiyi
 * @version 1.0.0
 */
public class StartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}
}
