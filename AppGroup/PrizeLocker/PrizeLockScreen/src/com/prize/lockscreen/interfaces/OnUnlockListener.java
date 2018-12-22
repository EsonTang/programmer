package com.prize.lockscreen.interfaces;
/***
 * fanjunchen
 * @author 图案解锁成功回调接口
 *
 */
public interface OnUnlockListener {
	/***
	 * @param isConfirm true 成功
	 */
	public void onUnlock(boolean isConfirm);
}
