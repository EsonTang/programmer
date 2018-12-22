package com.android.soundrecorder;

import android.widget.BaseAdapter;
public abstract class BaseExtAdapter extends BaseAdapter {

	/**Variable maximum height */
	public final int DLT_HEIGHT = 40;
	/***
	 * Set the current item height 
	 */
	public abstract void setItemHeight(int h);
	/***
	 * Get the current item height 
	 * @return
	 */
	public abstract int getCurrentItemHeight();
	/***
	 * Maximum height of acquisition 
	 * @return
	 */
	public abstract int getMaxItemHeight();
	/***
	 * Minimum height of acquisition 
	 * @return
	 */
	public abstract int getMinItemHeight();
	/***
	 * Gets the normal height of the item 
	 * @return
	 */
	public abstract int getNormalItemHeight();

}
