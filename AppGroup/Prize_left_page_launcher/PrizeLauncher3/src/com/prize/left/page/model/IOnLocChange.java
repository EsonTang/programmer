package com.prize.left.page.model;

import com.baidu.location.BDLocation;
/***
 * 当位置信息发生变化时接口
 * @author fanjunchen
 *
 */
public interface IOnLocChange {
	/***
	 * 位置变化回调
	 * @param loc
	 * @param isSuccess 0 成功且有网络, 1 成功但没有地址因没有网络, -1 失败 
	 */
	void onLocChange(BDLocation loc, int isSuccess);
}
