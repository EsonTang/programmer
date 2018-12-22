package com.prize.appcenter.receiver;


import android.annotation.NonNull;
import android.view.View;
import android.widget.AbsListView;

/**
 * longbaoxiu
 * 2017/10/30.21:54
 */

public abstract class AbsListViewScrollDetector implements AbsListView.OnScrollListener {
    private int mLastScrollY; //第一个可视的item的顶部坐标
    private int mPreviousFirstVisibleItem; //上一次滑动的第一个可视item的索引值
    private AbsListView mListView;//列表控件，如ListView
    /**
     * 滑动距离响应的临界值，这个值可根据需要自己指定
     * 只有只有滑动距离大于mScrollThreshold，才会响应滑动动作
     */
    private int mScrollThreshold;

    public AbsListViewScrollDetector() {
    }

    //当认为ListView向上滑动时会被调用，由子类去定义的。
    public abstract void onScrollUp();

    //当认为ListView下滑动时会被调用，由子类去定义的。
    public abstract void onScrollDown();

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    //核心方法，该方法封装了滑动方向判断的逻辑，但ListView产生滑动之后，该方法会被调用。
//1.首先，判断滑动后第一个可视的item和滑动前是否同一个，如果是同一个，进入第2步，否则进入第3步
//2.则这次滑动距离小于一个Item的高度，比较第一个可视的item的顶部坐标在滑动前后的差值，就知道了滑动的距离
//3.这个好办，直接比较滑动前后firstVisibleItem的值就可以判断滑动方向了。
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (totalItemCount != 0) {
            // 滑动距离：不超过一个item的高度
            if (this.isSameRow(firstVisibleItem)) {
                int newScrollY = this.getTopItemScrollY();
                //判断滑动距离是否大于 mScrollThreshold
                boolean isSignificantDelta = Math.abs(this.mLastScrollY - newScrollY) > this.mScrollThreshold;
                if (isSignificantDelta) {
                    //对于第一个可视的item，根据其前后两次的顶部坐标判断滑动方向
                    if (this.mLastScrollY > newScrollY) {
                        this.onScrollUp();
                    } else {
                        this.onScrollDown();
                    }
                }
                this.mLastScrollY = newScrollY;
            } else {//根据第一个可视Item的索引值不同，判断滑动方向
                if (firstVisibleItem > this.mPreviousFirstVisibleItem) {
                    this.onScrollUp();
                } else {
                    this.onScrollDown();
                }
                this.mLastScrollY = this.getTopItemScrollY();
                this.mPreviousFirstVisibleItem = firstVisibleItem;
            }
        }
    }

    public void setScrollThreshold(int scrollThreshold) {
        this.mScrollThreshold = scrollThreshold;
    }

    public void setListView(@NonNull AbsListView listView) {
        this.mListView = listView;
    }

    private boolean isSameRow(int firstVisibleItem) {
        return firstVisibleItem == this.mPreviousFirstVisibleItem;
    }

    private int getTopItemScrollY() {
        if (this.mListView != null && this.mListView.getChildAt(0) != null) {
            View topChild = this.mListView.getChildAt(0);
            return topChild.getTop();
        } else {
            return 0;
        }
    }
}
