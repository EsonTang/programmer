package com.viewpagerindicator;

import android.view.View;

public interface InnerViewPagerAdapter {
    /**
     * Get View representing the page at {@code index} in the adapter.
     */
    View getInnerView(int index);

    // From PagerAdapter
    int getCount();
}
