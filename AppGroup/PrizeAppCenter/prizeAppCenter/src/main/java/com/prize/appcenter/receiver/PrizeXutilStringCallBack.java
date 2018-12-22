package com.prize.appcenter.receiver;


/**
 * longbaoxiu
 * 2017/7/8.11:13
 */

public abstract class PrizeXutilStringCallBack<String> implements org.xutils.common.Callback.CommonCallback<String> {
    @Override
    public abstract void onSuccess(String result);


    @Override
    public abstract void onError(Throwable ex, boolean isOnCallback);


    @Override
    public void onCancelled(CancelledException cex) {

    }

    @Override
    public void onFinished() {

    }
}
