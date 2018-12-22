package com.prize.lockscreen.interfaces;

import android.view.View;


public interface BubbleMoveListener {
	public void move(View view);

	public void stop(View view, boolean isMove);

	public void start(View view, boolean isMove);
}
