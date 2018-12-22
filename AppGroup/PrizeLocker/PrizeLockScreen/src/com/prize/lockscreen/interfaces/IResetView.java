package com.prize.lockscreen.interfaces;
/***
 * 使VIEW重现及统一设置时间的接口
 * @author fanjunchen
 *
 */
public interface IResetView {

	/***
	 * 让自己重现即之前是怎么消失的, 这个方法就是反过来让其重现
	 */
	public void resetView();
	/***
	 * 更新时间
	 */
	public void updateTime();
}
