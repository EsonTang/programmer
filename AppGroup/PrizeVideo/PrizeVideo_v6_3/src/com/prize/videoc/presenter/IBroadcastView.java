package com.prize.videoc.presenter;

import android.content.Intent;

/**
 * Created by yiyi on 2015/6/10.
 */
public interface IBroadcastView {

	void onReceive(Intent intent);

	void onChange();
}
