package com.prize.ui;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.camera.R;
import com.prize.ui.CHListAdapter.OnItemClickLitener;
import com.prize.ui.CHListView.OnItemScrollChangeListener;
import com.prize.setting.LogTools;
import android.support.v7.widget.LinearLayoutManager;

public class CenterHorizontalScroll extends FrameLayout {

	protected static final String TAG = "CenterHorizontalScroll";
	private CHListView mChListView;
	private ImageView mCenterIm;
	private int mCenterImWidth;
	private int mItemCount;
	private CHListAdapter mAdapter;
	private LinearLayoutManager mLinearLayoutManager;
	private static final int SIZE_OFFSET = 10000;
	private int mOffset;
	private int mDataSize;
	private int mDividerSize;
	private OnItemChangeListener mOnItemChangeListener;
	
	public interface OnItemChangeListener {
		void itemChange(int postion);
	}
	
	public CenterHorizontalScroll(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCenterImWidth = context.getResources().getDimensionPixelOffset(R.dimen.center_item_width);
		mDividerSize = 0;//(int) TypedValue.applyDimension(1, TypedValue.COMPLEX_UNIT_DIP, context.getResources().getDisplayMetrics());
		mCenterImWidth += mDividerSize;
		mChListView = new CHListView(context, attrs, mCenterImWidth);
		/*prize-xuchunming-adjust layout at 18:9 project-start*/
		//setBackgroundResource(R.color.shutter_video_background);
		/*prize-xuchunming-adjust layout at 18:9 project-end*/
		mCenterIm = new ImageView(context);
		LayoutParams matchContent =
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		matchContent.gravity = Gravity.CENTER_HORIZONTAL;
		LayoutParams center =
				new LayoutParams(mCenterImWidth - mDividerSize, LayoutParams.MATCH_PARENT);
		center.gravity = Gravity.CENTER_HORIZONTAL;
		/*prize-xuchunming-adjust layout at 18:9 project-start*/
		//mCenterIm.setBackgroundColor(context.getResources().getColor(R.color.setting_tip));
		/*prize-xuchunming-adjust layout at 18:9 project-end*/
		addView(mCenterIm, center);
		addView(mChListView, matchContent);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mItemCount = width / mCenterImWidth;
	}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	super.onLayout(changed, l, t, r, b);
    }
	
	public void initAdapter(List<String> data, int index) {
		initAdapter(data);
		setSelectIndex(index);
	}
	
	public void initAdapter(List<String> data) {
		mDataSize = data.size();
		mLinearLayoutManager = new LinearLayoutManager(getContext());
		mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

		mChListView.setLayoutManager(mLinearLayoutManager);
		mAdapter = new CHListAdapter(getContext(), data);
		mChListView.setAdapter(mAdapter);
		/*mChListView.addItemDecoration(new DividerItemDecoration(
                getContext(), DividerItemDecoration.HORIZONTAL_LIST, getContext().getResources().getColor(R.color.center_view_divider), mDividerSize));*/
		mOffset = SIZE_OFFSET * mDataSize;
		mAdapter.setOnItemClickLitener(new OnItemClickLitener() {
			@Override
			public void onItemClick(View view, int position) {
				int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
				Log.i("RecyclerView", "firstPosition=" + firstPosition + " position=" + position);
				mChListView.smoothScrollToSelect(position, firstPosition);
			}
		});
		mChListView.setOnItemScrollChangeListener(new OnItemScrollChangeListener() {
			
			@Override
			public void onChange() {
				int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
				if (mOnItemChangeListener != null) {
					mOnItemChangeListener.itemChange((firstPosition + (mItemCount + 1) / 2) % mDataSize);
				}
			}
		});
	}
	
	public void toPosition(int index, boolean mirror) {
		/*int firstPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
		int newPosition = mOffset + index - (mItemCount + 1) / 2;
		int diff = (newPosition - firstPosition) % mDataSize;
		Log.i(TAG, "toPosition firstPostion=" + firstPosition + " diff=" + diff + " newPosition=" + newPosition + " index=" + index);
		newPosition = firstPosition + diff;
		if (diff > 0 && diff < mDataSize / 2) {
			newPosition = newPosition - mDataSize;
		}
		Log.i(TAG, "toPosition newPosition=" + newPosition);
		mChListView.scrollToPosition(newPosition);*/
		mLinearLayoutManager.scrollToPositionWithOffset(mOffset + index - (mItemCount + 1) / 2, 0);  
	}
	
	public void setOnItemChangeListener(OnItemChangeListener listener) {
		mOnItemChangeListener = listener;
    }
	
	public void setSelectIndex(int index) {
		LogTools.i(TAG, "setSelectIndex mOffset=" + mOffset);
		mChListView.scrollToPosition(mOffset + index - (mItemCount + 1) / 2);
	}
	
	public void scrollSelect(int diff) {
		LogTools.i(TAG, "setSelectIndex mOffset=" + mOffset);
		mChListView.smoothScrollDiff(diff);
	}
	
	public void handleMotionEvent(MotionEvent event) {
		mChListView.onTouchEvent(event);
	}
	/*prize-xuchunming-adjust layout at 18:9 project-start*/
	public void hightFocusView() {
		mChListView.hightFocusView();
	}
	/*prize-xuchunming-adjust layout at 18:9 project-end*/
}
