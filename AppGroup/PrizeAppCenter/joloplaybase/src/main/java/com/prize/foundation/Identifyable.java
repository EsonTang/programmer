/**
 * 
 */
package com.prize.foundation;

import java.util.UUID;

/**
 * 定义具有唯一标识号接口
 * 
 * @author prize
 * 
 */
public interface Identifyable {
	/**
	 * 获取标识�?
	 * 
	 * @return 唯一标记�?
	 */
	public UUID getIdentification();
}
