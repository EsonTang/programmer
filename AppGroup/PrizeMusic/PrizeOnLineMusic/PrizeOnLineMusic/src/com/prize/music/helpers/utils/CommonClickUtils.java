/** 
 *CommonClickUtils.java 
 * classes : com.dld.util.CommonClickUtils 
 * @author cdj
 * V 1.0.0 * Create at 2014-8-22 上午9:40:31 
 */
package com.prize.music.helpers.utils;

/**
 * @ClassName:[CommonClickUtils]
 * @Description:[]
 * @CreateDate:[2014-8-22 上午9:40:31]
 * @UpdateUser: UpdateUser
 * @UpdateDate: [2014-8-22 上午9:40:31]
 * @UpdateRemark: [说明本次修改内容]
 * @version [V1.0]
 */

public class CommonClickUtils {
	private static long lastClickTime;

	/**
	 * @Description:[如果是连续点击则返回true]
	 * @return
	 */
	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 800) {
			return true;
		}
		lastClickTime = time;
		return false;
	}
}
