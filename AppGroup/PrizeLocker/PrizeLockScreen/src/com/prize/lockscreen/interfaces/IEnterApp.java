package com.prize.lockscreen.interfaces;
/***
 * 进入到应用的接口
 * @author fanjunchen
 *
 */
public interface IEnterApp {
	/***
	 * 进入到哪个应用
	 * @param which 1 主页(暂不做操作), 2 电话, 3相机
	 */
	public void enterApp(int which);
}
