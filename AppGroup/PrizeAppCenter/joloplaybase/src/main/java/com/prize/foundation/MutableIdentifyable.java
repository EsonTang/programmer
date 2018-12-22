/**
 * 
 */
package com.prize.foundation;

import java.util.UUID;

/**
 * @author prize
 * 
 */
public interface MutableIdentifyable extends Identifyable {
	public void setIdentification(UUID id);
}
