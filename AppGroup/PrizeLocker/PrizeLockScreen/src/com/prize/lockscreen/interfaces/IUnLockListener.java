package com.prize.lockscreen.interfaces;

public interface IUnLockListener extends LockClickListener {
	/***
	 * 解锁完成
	 */
	public void onUnlockFinish();
	/***
	 * 检查是否有密码， 若有密码则要打开密码输入页
	 * @param which 点击哪一个,用来打开相机或电话之类的应用的
	 * @return true表示有
	 */
	public boolean checkPwd(int which);
	/***
	 * 是否有密码
	 * @return
	 */
	public boolean hasPwd();
	/***
	 * 获取密码串
	 * @return
	 */
	public String getPwd();
	/***
	 * 紧急电话解锁
	 */
	public void emergencyUnlock();
}
