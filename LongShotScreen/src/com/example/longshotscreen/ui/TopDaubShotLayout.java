
package com.example.longshotscreen.ui;

import android.widget.FrameLayout;
import android.os.Handler;
import android.content.Context;
import android.util.AttributeSet;
import android.content.res.Configuration;

public class TopDaubShotLayout extends FrameLayout {
	private Handler mHandler;

	public TopDaubShotLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	private void sendExitMessage() {
		mHandler.sendEmptyMessage(0x45b);
	}

	protected void onConfigurationChanged(Configuration configuration) {
		sendExitMessage();
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}
}
