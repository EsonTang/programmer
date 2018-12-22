/**
 * 
 */
package com.prize.foundation;

import java.util.Map;

/**
 * @author prize
 * 
 */
public interface MutablePropertyable extends Propertyable {
	public void setProperty(String key, Object value);

	public void setProperties(Map<String, Object> properties);
}
