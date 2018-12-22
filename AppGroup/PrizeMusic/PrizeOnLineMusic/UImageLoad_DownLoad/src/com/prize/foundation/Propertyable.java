/**
 * 
 */
package com.prize.foundation;

import java.util.Map;

/**
 * @author prize
 * 
 */
public interface Propertyable {
	public Object getProperty(String key);

	public Map<String, Object> getProperties();
}
