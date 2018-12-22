package com.prize.music;

/**
 * activity与Fragment通信接口
 * 
 * @author longbaoxiu
 *
 */
public interface IfragToActivityLister {
	/**
	 * 获取选中的条目数量
	 * 
	 * @param count
	 *            条目数量
	 * @return void
	 */
	void countNum(int count);

	/**
	 * 处理不同的action
	 * 
	 * @param action
	 *            操作String Action
	 * @return void
	 */
	void processAction(String action);

}
