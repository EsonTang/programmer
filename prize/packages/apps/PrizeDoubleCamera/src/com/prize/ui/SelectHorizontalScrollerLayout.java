package com.prize.ui;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;

import com.android.camera.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SelectHorizontalScrollerLayout extends HorizontalScrollView {

	protected static final String TAG = "HorizontalScrollerLayout";

    private int mSelectIndex = -1;
    private HorizontalScrollLayoutAdapter mAdapter;
    private OnItemClickListener mOnItemClickListener;
    private Map<View, Integer> mViewPos = new HashMap<View, Integer>();
    protected final HorizontalScrollStrip mScrollStrip;
    
    private boolean isNeedReload = true;
    private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int index = mViewPos.get(v);
			Log.i(TAG, "onClick index=" + index + " mSelectIndex=" + mSelectIndex);
			if (index != mSelectIndex) {
				scrollToCenter(index);
			}
		}
	};

    public interface OnItemClickListener {
		void onItemClick(int pos);
	}
    
    public SelectHorizontalScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setBackgroundColor(Color.TRANSPARENT);
     // Disable the Scroll Bar
     	setHorizontalScrollBarEnabled(false);
     	setOverScrollMode(OVER_SCROLL_NEVER);
		this.mScrollStrip = new HorizontalScrollStrip(context, attrs, false);
		setFillViewport(false);
		addView(mScrollStrip, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		ViewConfiguration configuration = ViewConfiguration.get(context);
		mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
		Log.i(TAG, "SelectHorizontalScrollerLayout mTouchSlop=" + mTouchSlop);
    }
    
    public SelectHorizontalScrollerLayout(Context context, AttributeSet attrs, HorizontalScrollLayoutAdapter adapter) {
        this(context, attrs);
        setAdapter(adapter);
    }
    
    public void setAdapter(HorizontalScrollLayoutAdapter adapter) {
    	mAdapter = adapter;
    	initView();
    }
    
    private void initView() {
    	int count = mAdapter.getCount();
    	mScrollStrip.removeAllViews();
    	mSelectIndex = -1;
    	mViewPos.clear();
    	isNeedReload = true;
    	//smoothScrollTo(0, 0);
    	for (int i = 0; i < count; i++) {
    		View childView = mAdapter.getView(i, null, this);
    		mViewPos.put(childView, i);
    		childView.setOnClickListener(mClickListener);
    		mScrollStrip.addView(childView);
    	}
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
    	mOnItemClickListener = listener;
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
        if ((changed || isNeedReload) && mSelectIndex != -1) {
        	isNeedReload = false;
        	scrollToCenter(mSelectIndex);
        }
    }
    
	private void scrollToCenter(int tabIndex) {
		final int tabStripChildCount = mScrollStrip.getChildCount();
		Log.i(TAG, "scrollToCenter tabStripChildCount=" + tabStripChildCount);
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
		Log.i(TAG, "scrollToCenter Utils.getStart(selectedTab) = "+x+",getScrollX()="+getScrollX());
		x -= (first - selected) / 2;
		Log.i(TAG, "scrollToCenter tabIndex=" + tabIndex + " x=" + x +",first="+first+",selected="+selected);
		if (mSelectIndex != tabIndex) {
			mSelectIndex = tabIndex;
			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(mSelectIndex);
			}
		}
		setSelect(mSelectIndex);
//		scrollTo(x, 0);
		smoothScrollTo(x, 0);
		/*prize-modify-adjust roactive text spacing-xiaoping-20171012-start*/
/*		if (tabStripChildCount == 4) {
			smoothScrollTo(x-24, 0);
		} else {
			smoothScrollTo(x, 0);
		}*/
		/*prize-modify-adjust roactive text spacing-xiaoping-20171012-end*/
		refreshView(mSelectIndex);
	}
	
	public void setSelect(int index) {
		for (int i = 0, count = mScrollStrip.getChildCount(); i < count; i++) {
			View childView = mScrollStrip.getChildAt(i);
			if (i == index) {
				childView.setSelected(true);
			} else {
				childView.setSelected(false);
			}
		}
	}
	
	private boolean mIsChange;
	private float mDownX;
	private float mXMove;
	private int mTouchSlop;
	
	@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*prize-xuchunming-20180522-bugid:58575-start*/
		/*if(isEnabled() == false){
			return true;
		}*/
		/*prize-xuchunming-20180522-bugid:58575-end*/
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	mDownX = ev.getRawX();
            	
            	Log.i(TAG, "onInterceptTouchEvent mDownX=" + mDownX);
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mDownX);
                Log.i(TAG, "onInterceptTouchEvent diff=" + diff + " mTouchSlop=" + mTouchSlop);
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(isEnabled() == false){
    		return true;
		}
    	float currentX = event.getRawX();
        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		mIsChange = false;
        		mDownX = event.getRawX();
        		Log.i(TAG, "onTouchEvent down");
        		break;
        	case MotionEvent.ACTION_MOVE:
        		if (!mIsChange && mScrollStrip.getChildAt(mSelectIndex) != null) {
        			int width = mScrollStrip.getChildAt(mSelectIndex).getWidth();
            		if ((mSelectIndex < mScrollStrip.getChildCount() - 1) && (mDownX - currentX > (width / 2))) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex + 1);
            		} else if ((mSelectIndex > 0) && (currentX - mDownX > (width / 2))) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex - 1);
            		}
            		Log.i(TAG, "onTouchEvent move mSelectIndex=" + mSelectIndex + " mDownX=" + mDownX + " currentX=" + currentX + " width=" + width);
        		}
        		break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            	Log.i(TAG, "up onTouchEvent mSelectIndex=" + mSelectIndex + " mDownX=" + mDownX + " currentX=" + currentX + " mIsChange=" + mIsChange);
                if (!mIsChange) {
                	if ((mSelectIndex < mScrollStrip.getChildCount() - 1) && (mDownX - currentX > 10)) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex + 1);
            		} else if ((mSelectIndex > 0) && (currentX - mDownX > 10)) {
            			mIsChange = true;
            			scrollToCenter(mSelectIndex - 1);
            		}
                }
                mIsChange = false;
            	break;
        }
        return true;
    }
    
    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);
    }

	public void setSelectIndex(int index) {
		// TODO Auto-generated method stub
		Log.i(TAG, "setSelectIndex index=" + index + " mSelectIndex=" + mSelectIndex);
		if (index != mSelectIndex) {
			scrollToCenter(index);
		}
	}
	
	public int getCount(){
		if(mAdapter != null ){
			return mAdapter.getCount();
		}
		return -1;
	}

	public void refreshView(int index) {
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View view = mScrollStrip.getChildAt(i);
			if (mAdapter.getCount() ==3 && index == 0) {
				Log.d(TAG,"left: "+view.getLeft()+",right: "+view.getRight()+",bottom: "+view.getBottom()+",top: "+view.getTop());
				/*prize-modify-adjust roactive text spacing-xiaoping-20171012-start*/
				mScrollStrip.setPaddingRelative(24,0,0,0);
				/*prize-modify-adjust proactive text spacing-xiaoping-20171012-end*/
			}
			view.setScaleX((float) (1-(Math.abs(i-index)) *(0.07)));
			requestLayout();
		}
	}
	
	/*prize-xuchunming-20180522-bugid:58575-start*/
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		for (Entry<View, Integer> entry : mViewPos.entrySet()) {  
			entry.getKey().setEnabled(enabled); 
		}  
		super.setEnabled(enabled);
	}
	/*prize-xuchunming-20180522-bugid:58575-end*/
}
