package com.prize.prizenavigation.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.prize.prizenavigation.bean.ClientInfo;

/**
 * 监听网络变化
 * Created by liukun on 2017/3/13.
 */
public class NavigationNetReceiver extends BroadcastReceiver {

    public NavigationNetReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ClientInfo.getAPNType(context);
    }

//    public interface NetChangeCallback{
//        void
//    }
}
