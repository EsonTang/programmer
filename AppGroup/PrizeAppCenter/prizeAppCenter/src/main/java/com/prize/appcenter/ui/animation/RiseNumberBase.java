package com.prize.appcenter.ui.animation;

 /**
   * Desc: 数字增长动画，2.0积分系统 RiseNumberTextView使用到
   *
   * Created by huangchangguo
   * Date:  2016/8/16 10:32
   */

public interface RiseNumberBase {
	public void start();

	public RiseNumberTextView withNumber(float number);

	public RiseNumberTextView withNumber(float number, boolean flag);

	public RiseNumberTextView withNumber(int number);

	public RiseNumberTextView setDuration(long duration);

	public void setOnEnd(RiseNumberTextView.EndListener callback);
}
