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
package com.prize.cloud.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

import com.prize.cloud.db.DbManager;
import com.prize.cloud.receiver.PollMgr;
import com.prize.cloud.service.AccountService;
import com.prize.cloud.util.CloudIntent;

/**
 * 应用唯一App类，在这里进行初始化工作
 * @author yiyi
 * @version 1.0.0
 */
public class CloudApp extends Application {

	private static CloudApp instance;

	private static ArrayList<Activity> activityList = new ArrayList<Activity>();
	
    public static CloudApp getInstance() {
        return instance;
    }
	@Override
	public void onCreate() {
		super.onCreate();
		DbManager.getInstance().createDb(this);
		/*PollMgr.startPollingService(this, PollMgr.IntervalMillis,
				AccountService.class, CloudIntent.ACTION_ACTIVATE_IN_BACKGROUND);*/
		instance = this;
	}
}
