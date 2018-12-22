package com.prize.prizethemecenter.ui.widget.indicator;

public interface IconTextPagerAdapter {
	/**
	 * Get icon representing the page at {@code index} in the adapter.
	 */
	int getIconResId(int index);

	int getCount();
}
