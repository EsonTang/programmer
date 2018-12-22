package com.prize.appcenter.receiver;


import android.content.Context;
import android.content.Intent;

import com.prize.app.BaseApplication;
import com.prize.app.util.JLog;
import com.prize.appcenter.service.PrizeAppCenterService;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

/**
 * longbaoxiu
 * 2016/11/4.11:41
 */

public class MyXGReceiver extends XGPushBaseReceiver {
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {

    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        if (JLog.isDebug) {
            JLog.i("MainActivity", "MyXGReceiver-xgPushTextMessage.getContent()=" + xgPushTextMessage.getContent());
        }
        Intent intent = new Intent("com.prize.appcenter.service.PrizeAppCenterService");
        intent.setClassName(BaseApplication.curContext,
                "com.prize.appcenter.service.PrizeAppCenterService");
        intent.putExtra("content", xgPushTextMessage.getContent());
        intent.putExtra(PrizeAppCenterService.OPT_TYPE, 7);
        context.startService(intent);

    }

    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {

    }
}
