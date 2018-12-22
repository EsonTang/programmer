/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：悬浮窗服务
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-05-08
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
package com.prize.music.service;

import java.util.Timer;
import java.util.TimerTask;

import com.prize.music.MusicWindowManager;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

/**
 * 类描述：悬浮窗服务
 * @author 朱道鹏
 * @version V1.0
 */
public class FloatWindowService extends Service {

	/**
	 * 用于在线程中创建或移除悬浮窗。
	 */
	private Handler handler = new Handler();

	/**
	 * 定时器，定时进行检测当前应该创建还是移除悬浮窗。
	 */
	private Timer timer;
	
	private boolean isWindowShowing = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 开启定时器，每隔0.5秒刷新一次
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 300);
		}
		return super.onStartCommand(intent, flags, startId); 
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		// Service被终止的同时也停止定时器继续运行
		timer.cancel();
		timer = null;
		isWindowShowing = false;
		MusicWindowManager.removeBigWindow(getApplicationContext());
	}
	
	class RefreshTask extends TimerTask {

		@Override
		public void run() {
			// 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
			if (!isWindowShowing) {
				isWindowShowing = true;
				handler.post(new Runnable() {
					@Override
					public void run() {
						MusicWindowManager.createBigWindow(getApplicationContext());
					}
				});
			}
		}
	}
}
