package com.prize.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
/*prize-xuchunming-adjust layout at 18:9 project-start*/
import android.widget.TextView;
/*prize-xuchunming-adjust layout at 18:9 project-end*/

import com.android.camera.R;

public class CHListView extends RecyclerView implements OnScrollListener {

	protected static final String TAG = "RecyclerView";
	private int mItemWidth;
	private int mItemCount;
	private int mPadding;
	private OnItemScrollChangeListener mListener;
	
	public interface OnItemScrollChangeListener {
		void onChange();
	}

	public CHListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CHListView);
		mItemWidth = ta.getDimensionPixelSize(R.styleable.CHListView_childWidth, -1);
        ta.recycle();
        init(context);
	}
	
	public CHListView(Context context, AttributeSet attrs, int itemWidth) {
		super(context, attrs);
		setOnScrollListener(this);
		mItemWidth = itemWidth;
        init(context);
	}
	
	private void init(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		mItemCount = width / mItemWidth;
		mPadding = width % mItemWidth / 2;
		if (mItemCount % 2 == 0) {
			mPadding += mItemWidth / 2;
		}
		mPadding = mPadding - mItemWidth;
		Log.i(TAG, "onLayout mWidth=" + width + " mItemCount=" + mItemCount + " mPadding=" + mPadding);
		setPadding(mPadding, 0, mPadding, 0);
	}
	
	public void setOnItemScrollChangeListener(OnItemScrollChangeListener listener) {
		mListener = listener;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		/*prize-xuchunming-20171226-bugid 46184-start*/
		hightFocusView();
		/*prize-xuchunming-20171226-bugid 46184-end*/
	}

	@Override
	public void onScrollStateChanged(int arg0) {
		Log.i(TAG, "onScrollStateChanged arg0=" + arg0);
		/*prize-xuchunming-adjust layout at 18:9 project-start*/
		cancleFocuseView();
		/*prize-xuchunming-adjust layout at 18:9 project-end*/
		if (arg0 == SCROLL_STATE_IDLE) {
			/*prize-xuchunming-20160913-adpater layout RTL-start*/
			if(isLayoutRtl() == true){
				int left = getChildAt(mItemCount).getLeft();
				Log.i(TAG, "onScrollStateChanged left=" + left);
				if (left == (mPadding + mItemWidth)) {
					if (mListener != null) {
						mListener.onChange();
						/*prize-xuchunming-adjust layout at 18:9 project-start*/
						hightFocusView();
						/*prize-xuchunming-adjust layout at 18:9 project-end*/
					}
					return;
				}
				smoothScrollBy(left - (mPadding + mItemWidth), 0);
			}else{
			/*prize-xuchunming-20160913-adpater layout RTL-end*/
				int left = getChildAt(0).getLeft();
				Log.i(TAG, "onScrollStateChanged left=" + left);
				if (left == mPadding) {
					if (mListener != null) {
						mListener.onChange();
						/*prize-xuchunming-adjust layout at 18:9 project-start*/
						hightFocusView();
						/*prize-xuchunming-adjust layout at 18:9 project-end*/
					}
					return;
				}
				smoothScrollBy(left - mPadding, 0);
			}
		}
	}
	
	@Override
	public void onScrolled(int arg0, int arg1) {
		
	}

	@Override
	public boolean fling(int velocityX, int velocityY) {
		return super.fling(velocityX / 4, velocityY);
	}
	
	public void smoothScrollToSelect(int position, int firstVisible) {
		int newVisible = position - (mItemCount + 1) / 2;
		int diff = newVisible - firstVisible;
		/*prize-xuchunming-20160913-adpater layout RTL-start*/
		if(isLayoutRtl() == true){
			diff = -diff;
		}
		/*prize-xuchunming-20160913-adpater layout RTL-end*/
		smoothScrollBy(diff * mItemWidth, 0);
		int left = getChildAt(mItemCount).getLeft();
	}
	
	public void smoothScrollDiff(int diff) {
		smoothScrollBy(diff * mItemWidth, 0);
	}
	
	/*prize-xuchunming-adjust layout at 18:9 project-start*/
	public void hightFocusView() {
		for(int i = 0; i < getLayoutManager().getChildCount(); i++){
			((TextView)(getLayoutManager().getChildAt(i).findViewById(R.id.tv))).setSelected(false);
		}
		if(getLayoutManager().getChildAt(3) != null){
			((TextView)(getLayoutManager().getChildAt(3).findViewById(R.id.tv))).setSelected(true); 
		}
		
	}
	
	public void cancleFocuseView(){
		if(getLayoutManager().getChildAt(3) != null){
			if(((TextView)(getLayoutManager().getChildAt(3).findViewById(R.id.tv))).isSelected() == true){
				((TextView)(getLayoutManager().getChildAt(3).findViewById(R.id.tv))).setSelected(false);
			}
		}
	}
	/*prize-xuchunming-adjust layout at 18:9 project-end*/
}
