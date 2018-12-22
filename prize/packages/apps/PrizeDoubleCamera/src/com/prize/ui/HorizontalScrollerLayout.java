package com.prize.ui;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class HorizontalScrollerLayout extends HorizontalScrollView {

	protected static final String TAG = "HorizontalScrollerLayout";

    private int mSelectIndex = -1;
    private HorizontalScrollLayoutAdapter mAdapter;
    private OnItemChangeListener mOnItemClickListener;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    protected final HorizontalScrollStrip mScrollStrip;
    private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int index = mViewPos.get(v);
			if (index != mSelectIndex) {
				scrollToCenter(index);
			}
		}
	};

    public interface OnItemChangeListener {
		void onItemSelect(int pos);
	}
    
    public void setOnItemChangeListener(OnItemChangeListener listener) {
    	mOnItemClickListener = listener;
    }
    
    public HorizontalScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
     // Disable the Scroll Bar
     	setHorizontalScrollBarEnabled(false);
     	setOverScrollMode(OVER_SCROLL_NEVER);
		this.mScrollStrip = new HorizontalScrollStrip(context, attrs);
		setFillViewport(false);
		addView(mScrollStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }
    
    public HorizontalScrollerLayout(Context context, AttributeSet attrs, HorizontalScrollLayoutAdapter adapter) {
        this(context, attrs);
        setAdapter(adapter);
    }
    
    public HorizontalScrollerLayout(Context context, AttributeSet attrs, HorizontalScrollLayoutAdapter adapter, int index) {
        this(context, attrs);
        setAdapter(adapter);
        scrollToCenter(index);
    }
    
    public void setAdapter(HorizontalScrollLayoutAdapter adapter) {
    	mAdapter = adapter;
    	initView();
    }
    
    public void setAdapter(HorizontalScrollLayoutAdapter adapter, int index) {
    	mAdapter = adapter;
    	initView();
    	scrollToCenter(index);
    }
    
    private void initView() {
    	int count = mAdapter.getCount();
    	Log.i(TAG, "initView count=" + count);
    	for (int i = 0; i < count; i++) {
    		View childView = mAdapter.getView(i, null, mScrollStrip);
    		mViewPos.put(childView, i);
    		childView.setOnClickListener(mClickListener);
    		mScrollStrip.addView(childView);
    	}
    }
    
    @Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScrollStrip.getChildCount() > 0) {
			View firstTab = mScrollStrip.getChildAt(0);
			View lastTab = mScrollStrip.getChildAt(mScrollStrip.getChildCount() - 1);
			int start = (w - Utils.getMeasuredWidth(firstTab)) / 2;
//					- Utils.getMarginStart(firstTab);
			int end = (w - Utils.getMeasuredWidth(lastTab)) / 2;
//					- Utils.getMarginEnd(lastTab);
			mScrollStrip.setMinimumWidth(mScrollStrip.getMeasuredWidth());
			setPaddingRelative(start, getPaddingTop(), end, getPaddingBottom());
//			ViewCompat.setPaddingRelative(this, start, getPaddingTop(), end,
//					getPaddingBottom());
			setClipToPadding(false);
			Log.i(TAG, "onSizeChanged w=" + w + " start=" + start + " end=" + end);
		}
	}

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	Log.i(TAG, "onLayout changed=" + changed);
    	super.onLayout(changed, l, t, r, b);
    	if (changed && mSelectIndex != -1) {
        	scrollToCenter(mSelectIndex);
        }
    }
    
    public void setSelectIndex(int index) {
    	if (index != mSelectIndex) {
    		scrollToCenter(index, false);
    	}
    }
    
    private void ajustScorllX() {
    	int scrollX = getScrollX();
    	int width = 0;
    	int postion = 0;
		for (int i = 0, count = mScrollStrip.getChildCount(); i < count; i++) {
			View childView = mScrollStrip.getChildAt(i);
			int childViewWidth = childView.getWidth();
			int ajustWidth = width + childViewWidth / 2;
			width += childViewWidth;
			Log.i(TAG, "ajustScorllX scrollX=" + scrollX + " ajustWidth=" + ajustWidth);
			if (scrollX < ajustWidth) {
				postion = i;
				break;
			}
		}
		Log.i(TAG, "ajustScorllX postion=" + postion);
		scrollToCenter(postion);
	}
    
    private void scrollToCenter(int tabIndex, boolean back) {
		final int tabStripChildCount = mScrollStrip.getChildCount();
		if (tabStripChildCount == 0 || tabIndex < 0
				|| tabIndex >= tabStripChildCount) {
			return;
		}
		Log.i(TAG, "scrollToTab tabIndex=" + tabIndex + " scrollX=" + getScrollX());
		View selectedTab = mScrollStrip.getChildAt(tabIndex);
		View firstTab = mScrollStrip.getChildAt(0);
		int first = Utils.getWidth(firstTab);
//				+ Utils.getMarginStart(firstTab);
		int selected = Utils.getWidth(selectedTab);
//				+ Utils.getMarginStart(selectedTab);
		int x = Utils.getStart(selectedTab);
//				- Utils.getMarginStart(selectedTab);
		x -= (first - selected) / 2;
		Log.i(TAG, "scrollToCenter tabIndex=" + tabIndex + " x=" + x);
		if (mSelectIndex != tabIndex) {
			mSelectIndex = tabIndex;
			if (mOnItemClickListener != null && back) {
				mOnItemClickListener.onItemSelect(mSelectIndex);
			}
			mScrollStrip.invalidate();
		}
//		scrollTo(x, 0);
		smoothScrollTo(x, 0);
	}
    
	private void scrollToCenter(int tabIndex) {
		scrollToCenter(tabIndex, true);
	}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	Log.i(TAG, "onTouchEvent event=" + event.getAction() + " getScrollX()=" + getScrollX());
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	ajustScorllX();
                return true;
//            	break;
        }
        return super.onTouchEvent(event);
    }
    
    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);
    }

}
