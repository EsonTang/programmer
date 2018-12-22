/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：悬浮音乐广播接收
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-05-05
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
package com.prize.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.app.util.JLog;
import com.prize.music.service.FloatWindowService;

/**
 * 类描述：悬浮音乐广播接收类
 * @author 朱道鹏
 * @version V1.0
 */
public class MusicFloatWindowBroadcastReceiver extends BroadcastReceiver {

	private final static String ACTION_MUSIC = "com.prize.music.MusicFloatWindowBroadcastReceiver";	
	private final static String ACTION_SERVICE = "com.prize.music.service.FloatWindowService";
	private String intentAction;	

	@Override
	public void onReceive(Context context, Intent intent) {
		intentAction = intent.getAction();
		JLog.i("MusicFloatWindowBroadcastReceiver", "intentAction="+intentAction);
		Intent serviceIntent = new Intent(context, FloatWindowService.class);
		serviceIntent.setAction(ACTION_SERVICE);
		if (null != intent && ACTION_MUSIC.equals(intentAction)) {
			if(!MusicWindowManager.isWindowShowing()){
				context.startService(serviceIntent);
			}else{
				context.stopService(serviceIntent);
			}
		}else{
			context.stopService(serviceIntent);
		}
	}
}
