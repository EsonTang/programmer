package com.prize.left.page.model;
/***
 * 卡片数据变化通知类,主要是用于第一次卡片数据取不到时处理
 * @author june
 *
 */
public interface ICardNotify {

	/***
	 * 根据卡片类型来处理某卡片的移除与否<br>
	 * 注: 需要在UI线程中运行
	 * @param type
	 * @param hasData 是否有数据, false 为无
	 */
	void notifyUpdate(int type, boolean hasData);
	/***
	 * 需要增加某类型
	 * @param type
	 */
	void addUpdate(int type);
}
