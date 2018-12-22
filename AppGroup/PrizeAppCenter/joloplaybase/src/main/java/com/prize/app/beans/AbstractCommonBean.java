/**
 * 
 */
package com.prize.app.beans;

import java.util.UUID;

import com.prize.foundation.DefaultPropertiesSupport;
import com.prize.foundation.MutableIdentifyable;

/**
 * @author prize
 * 
 */
public class AbstractCommonBean extends DefaultPropertiesSupport implements
		MutableIdentifyable {

	private UUID uuid = UUID.randomUUID();

	private short sourceId = 0;

	public void setIdentification(UUID id) {
		this.uuid = id;
	}

	public UUID getIdentification() {
		return uuid;
	}

	/**
	 * @return the sourceId
	 */
	public short getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId
	 *            the sourceId to set
	 */
	public void setSourceId(short sourceId) {
		this.sourceId = sourceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + sourceId;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractCommonBean other = (AbstractCommonBean) obj;
		if (sourceId != other.sourceId)
			return false;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
