/**
 * 
 */
package com.prize.foundation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author prize
 * 
 */
public class DefaultPropertiesSupport implements MutablePropertyable, Cloneable {

	private Map<String, Object> properties = new HashMap<String, Object>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public DefaultPropertiesSupport clone() throws CloneNotSupportedException {
		DefaultPropertiesSupport o = (DefaultPropertiesSupport) super.clone();

		o.setProperties(this.properties);
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.skymobi.util.MutablePropertyable#setProperties(java.util.Map)
	 */
	public void setProperties(Map<String, Object> properties) {
		this.properties.clear();

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (null != entry.getValue()) {
				this.properties.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.skymobi.util.MutablePropertyable#setProperty(java.lang.String,
	 * java.lang.Object)
	 */
	public void setProperty(String key, Object value) {
		properties.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.skymobi.util.Propertyable#getProperties()
	 */
	public Map<String, Object> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.skymobi.util.Propertyable#getProperty(java.lang.String)
	 */
	public Object getProperty(String key) {
		return properties.get(key);
	}

	/*
	 * public String toString() {
	 * 
	 * return ToStringBuilder.reflectionToString(this,
	 * ToStringStyle.SHORT_PREFIX_STYLE); }
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((properties == null) ? 0 : properties.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultPropertiesSupport other = (DefaultPropertiesSupport) obj;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		return true;
	}
}
