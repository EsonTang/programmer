package com.prize.appcenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.prize.appcenter.ui.util.ShortcutUtil;
import com.prize.appcenter.ui.util.ToastUtils;

public class UserBroadcast extends BroadcastReceiver {

    private static final String ACTION = "net_error";
    private static final String ACTION_PUSH = "action_push";
    public final static String SHORTCUT_ACTION = "prize_shortCut_build";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (intent.getAction().equals(SHORTCUT_ACTION)){
                if (!ShortcutUtil.hasShortcut(context.getApplicationContext())) {
                    ShortcutUtil.createShortCut(context.getApplicationContext(), "必备软件");
                } else {
                    ToastUtils.showToast("该快捷键已经创建");
                }
            }
        }
        // NotificationService.startNotification(MainApplication.curContext,
        // NotificationService.ACT_NOTIFY_GAMES_INFO);
        // // 提示平台消息
        // NotificationService.startNotification(MainApplication.curContext,
        // NotificationService.ACT_NOTIFY_NEWS);
//		if (ACTION.equals(intent.getAction())) {
//			String pushAppPackgeName = DataStoreUtils
//					.readLocalInfo(DataStoreUtils.PUSH_APP);
//			String downloadPkgName = intent.getStringExtra("downloadPkgName");
////			Log.e("UserBroadcast", "pushAppPackgeName=" + pushAppPackgeName
////					+ " packageName=" + downloadPkgName);
////			if (!TextUtils.isEmpty(pushAppPackgeName)) {
////				if (downloadPkgName.equals(pushAppPackgeName)) {
////					new PushNotification().processDownLoadData(
////							BaseApplication.curContext, GameDAO.getInstance()
////									.querySingle(pushAppPackgeName), true);
////				}
////			}
//		} else if (ACTION_PUSH.equals(intent.getAction())) {
//			Log.e("UserBroadcast", "action_push");
////			Intent intent2 = new Intent(BaseApplication.curContext,
////					PrizeAppCenterService.class);
////			intent2.putExtra(PrizeAppCenterService.OPT_TYPE, 6);
////			BaseApplication.curContext.startService(intent2);
//		}
    }
}
