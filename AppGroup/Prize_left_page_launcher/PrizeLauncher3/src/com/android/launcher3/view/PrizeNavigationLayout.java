/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：整理功能 导航栏
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Scroller;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.CellLayout;
import com.android.launcher3.Hotseat;
import com.android.launcher3.ImageUtils;
import com.android.launcher3.Launcher;
import com.android.launcher3.Utilities;
import com.android.launcher3.Launcher.SpringState;
import com.android.launcher3.Workspace;

public class PrizeNavigationLayout extends ViewGroup {// Terase_ScrollLyoutDemoActivity
	private static final String TAG = "terase_ScrollLayout";
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private int mCurScreen;
	private int mDefaultScreen = 0;

	private Launcher mLauncher;

	public void setLauncher(Launcher mlauncher) {
		this.mLauncher = mlauncher;
	}

	private static final int TOUCH_STATE_REST = 0; // 表示触摸状态为空闲
													// 即没有触摸或者手指离开了
	private static final int TOUCH_STATE_SCROLLING = 1; // 表示手指正在移动

	private static final int SNAP_VELOCITY = 600; // 默认的滚动速度
													// 之后用于和手指滑动产生的速度比较
													// 获取屏幕滚动的速度

	private int mTouchState = TOUCH_STATE_REST; // 当前手指的事件状态
	private int mTouchSlop; // 手指移动的最小距离的判断标准
							// =ViewConfiguration.get(getContext()).getScaledTouchSlop();
							// 在viewpapper中就是依赖于这个值来判断用户
							// 手指滑动的距离是否达到界面滑动的标准
	private float mLastMotionX; // 手指移动的时候，或者手指离开屏幕的时候记录下的手指的横坐标
	private float mLastMotionY; // 手指移动的时候，或者手指离开屏幕的时候记录下的手指的纵坐标

	public PrizeNavigationLayout(Context context) {
		super(context);
		Log.e(TAG, "----ScrollLyout1---");
	}

	public PrizeNavigationLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		Log.e(TAG, "----ScrollLyout2---");

		// 初始化基本数据
		mScroller = new Scroller(context);
		mCurScreen = mDefaultScreen;
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop(); // 使用系统默认的值
	}

	public PrizeNavigationLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		Log.e(TAG, "----ScrollLyout3---");
	}

	public void removeNavigationView(int index) {
		this.removeView(this.getChildAt(index));
	}

	private int scaleh;

	private int scalew;

	public Drawable getContentDrawable(View v) {
		synchronized (PrizeBubbleTextView.class) {
			try {

				int h = v.getMeasuredHeight();
				int w = v.getMeasuredWidth();
				Bitmap m = ImageUtils.convertViewToBitmap(v, w, h);
				Bitmap ms = ImageUtils.resize(m, scalew, scaleh);
				if (m != ms && !m.isRecycled()) {
					m.recycle();
				}

				ViewGroup p = (ViewGroup) v.getParent();
				Drawable d = ImageUtils.bitmapToDrawable(ms);
				d.setBounds(0, 0, scalew, scaleh);
				return d;
			} catch (Exception e) {
				return null;
			}

		}
	}

	// private PrizeNavigationView mChildView = null;

	public void addNavigationView(CellLayout cell) {
		final PrizeNavigationView view = new PrizeNavigationView(
				this.getContext(), cell, scalew, scaleh);

		LayoutParams layout = new LayoutParams(scalew, scaleh);
		view.setLayoutParams(layout);
		view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = PrizeNavigationLayout.this.indexOfChild(view);
				mLauncher.getworkspace().snapToPage(index, 1000);
				updateSelectState(index);
			}
		});
		this.addView(view);

	}

	public void addNavigationView(View v) {

		addNavigationView(v);

	}

	public void OnDragEnd(int id, Workspace workspace) {
		View child = workspace.getChildAt(id);
		PrizeNavigationView navigationChild = (PrizeNavigationView) this
				.getChildAt(id);
		if (navigationChild != null) {

			// navigationChild.setImageDrawable(navigationChild.getContentDrawable(child));
		}
	}

	public void OnDragOver(int x, int y, Rect r) {
		if (mLauncher.getSpringState() == SpringState.BATCH_EDIT_APPS) {
			int id = pointInSelfOverChildForIndex(x, y, r);
			if (mLastpage != id && id != -1) {
				mLauncher.getworkspace().snapToPage(id, 1000);
				mLastpage = id;
			}
		}
	}

	int mLastpage = -1;

	public int pointInSelfOverChildForIndex(int x, int y, Rect r) {
		int index = -1;
		if (r == null) {
			r = new Rect();
		}
		int mTempPt[] = new int[2];
		mTempPt[0] = x;
		mTempPt[1] = y;
		mLauncher.getDragLayer().getDescendantCoordRelativeToSelf(
				mLauncher.getworkspace(), mTempPt, true);

		int[] myPoint = this.getLocationOnScreen();
		r.left = myPoint[0];
		r.right = myPoint[0] + this.getWidth();
		r.top = myPoint[1];
		r.bottom = myPoint[1] + this.getHeight();
		if (!r.contains(mTempPt[0], mTempPt[1])) {
			return -1;
		}
		LogUtils.i("zhouerlong", "mTempPt:" + "[" + mTempPt[0] + "," + mTempPt[0]
				+ "]");
		for (int i = 0; i < this.getChildCount(); i++) {
			PrizeNavigationView child = (PrizeNavigationView) this
					.getChildAt(i);
			int[] childPoint = child.getLocationOnScreen();
			r.left = childPoint[0];
			r.right = childPoint[0] + child.getWidth();
			r.top = childPoint[1];
			r.bottom = childPoint[1] + child.getHeight();
			child.select = false;
			if (r.contains(mTempPt[0], mTempPt[1])) {

				index = i;
				child.select = true;
			}
			child.requestLayout();
		}
		return index;
	}

	/**
	 * 实时更新选中项
	 * 
	 * @param index
	 */
	public void updateSelectState(int index) {
		for (int i = 0; i < this.getChildCount(); i++) {
			PrizeNavigationView child = (PrizeNavigationView) this
					.getChildAt(i);
			if (index == i) {
				child.select = true;
			} else {
				child.select = false;
			}
			child.Ondrop();
			child.requestLayout();
			
		}
		int nextPage = index/6%6;
		if(nextPage != mCurScreen) {
			snap2DestScreen(nextPage);
		}

	}

	
	
	public void enterSprindLoadMode(boolean isWorkspaceToSpringLoad,
			Workspace workspace,int defPage) {
		if (isWorkspaceToSpringLoad) {
			this.removeAllViews();
			List<View> list = new ArrayList<>();
			for (int i = 0; i < workspace.getChildCount(); i++) {
				View v = workspace.getChildAt(i);
				if(v instanceof CellLayout) {
					CellLayout child = (CellLayout) workspace.getChildAt(i);
				if (workspace.getIdForScreen(child) != Workspace.EXTRA_EMPTY_SCREEN_ID) {

					list.add(child);

					int h = child.getMeasuredHeight();
					int w = child.getMeasuredWidth();
					scalew = (int) (Utilities.sIconTextureWidth/1.1f);
					float percent = scalew / (float) w;
					scaleh = (int) (h * percent);
					PrizeNavigationView view = new PrizeNavigationView(
							mContext, child, scalew, scaleh);
					LayoutParams layout = new LayoutParams(scalew, scaleh);
					view.setLayoutParams(layout);
					view.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							int index = PrizeNavigationLayout.this
									.indexOfChild(v);
							mLauncher.getworkspace().snapToPage(index, 1000);
							updateSelectState(index);
						}
					});
					this.addView(view);
				}
				}
			}

			updateSelectState(defPage);
			/*
			 * load = new LoadTask(); load.execute(list);
			 */
		}
	}

	

	public void updateChildContent(int id, Workspace workspace) {
		View v = (PrizeNavigationView) getChildAt(id);
		if (v != null) {
			v.requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int count = getChildCount();
		// 为每一个孩子设置它们的大小为ScrollLayout的大小
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
		scrollTo(mCurScreen * width, 0);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.e(TAG, "----onLayout----");
		// 为每一个孩子设置它们的位置
		if (true) {
			int p = (int) (12*Launcher.scale);
			int childCount = getChildCount();
			int width = this.getMeasuredWidth();
			int childleft = 0;
			for (int i = 0; i < childCount; i++) {
				final View childView = getChildAt(i);
				int height = childView.getLayoutParams().height;
				final int childWidth = (int) (50*Launcher.scale);//childView.getLayoutParams().width;
				int left1 = childWidth * childCount + p * (childCount - 1);
				if (childView.getVisibility() != View.GONE) {
					int left=0;
					int right=0;
					int pageNum =0;
					// 此处获取到的宽度就是在onMeasure中设置的值
					if(childCount>5) {
						pageNum = i/6;
						left = ((childWidth+p)*i - pageNum*p);
					}else {
						 left = (int) (width / 2f - left1 / 2f) + i
								* (childWidth + p);
					}
					 right = left + childWidth;

					// 为每一个子View布局
					childView.layout(left, 30, right, height+30);
					/*
					 * childView.layout(childleft, 0,childleft+childWidth,
					 * childView.getMeasuredHeight()); childleft +=childWidth;
					 */

				}
			}
		}
	}

	/**
	 * 让界面跟着手指移动到手指移动的地点
	 */
	public void snapToDestination() {

		Log.e(TAG, "----snapToDestination---");
		final int screenWidth = getWidth(); // 子view的宽度，此例中为他适配的父view的宽度
		Log.e(TAG, "screenWidth = " + screenWidth);
		final int destScreen = (getScrollX() + screenWidth / 2) / (screenWidth-(int)(12*Launcher.scale)); // 某个算法吧，
		Log.e(TAG, "[destScreen] : " + destScreen); // 我计算了一下的确是能够准确算出目标view
		// getScroolX()值为
		snap2DestScreen(destScreen);
	}

	public int getcustChildCount() {
		// TODO Auto-generated method stub
		// return super.getChildCount();
		int count = this.getChildCount() / 6;
		int pagex = this.getChildCount() % 6;
		if (pagex > 0) {
			return count + 1;
		} else {
			return count;
		}

	}

	/**
	 * 滚动到指定screen
	 * 
	 * @param destScreen
	 */
	private void snap2DestScreen(int destScreen) {
		Log.e(TAG, "----snap2DestScreen----");
		Log.e(TAG,
				"Math.min(destScreen, getChildCount() - 1) = "
						+ (Math.min(destScreen, getcustChildCount() - 1)));
		destScreen = Math.max(0, Math.min(destScreen, getcustChildCount() - 1));// 获取要滚动到的目标screen
		Log.e(TAG, "whichScreen = " + destScreen);
		if (getScrollX() != (getWidth() * destScreen)) {
			final int delta = destScreen * getWidth() - getScrollX(); // 获取屏幕移到目的view还需要移动多少距离
			Log.e(TAG, "[getScrollX()] : " + getScrollX());
			Log.e(TAG, "[delta] : " + delta);
			Log.e(TAG, "[getScrollX要走到的位置为] : " + (getScrollX() + delta));
			mScroller.startScroll(getScrollX(), 0, delta, 0,
					Math.abs(delta) * 2);// 使用Scroller辅助滚动，让滚动变得更平滑
			mCurScreen = destScreen;
			invalidate();// 重绘界面
		}
	}

	@Override
	public void computeScroll() {
		Log.e(TAG, "----computeScroll----");
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e(TAG, "----onTouchEvent----");
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:// 1,终止滚动2,获取最后一次事件的x值
			Log.e(TAG, "----ACTION_DOWN----");
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:// 1,获取最后一次事件的x值2,滚动到指定位置

			int deltaX = (int) (mLastMotionX - x);
			mLastMotionX = x;
			scrollBy(deltaX, 0);
			Log.e(TAG, "----ACTION_MOVE----+X:" + x + " ,getScrollX:"
					+ getScrollX() + " deltaX:" + deltaX);
			break;
		case MotionEvent.ACTION_UP:// 1,计算手指移动的速度并得出我们需要的速度2,选择不同情况下滚动到哪个 screen
			Log.e(TAG, "----ACTION_UP----");
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000); // 设置属性为计算1秒运行多少个像素
			// computeCurrentVelocity(int
			// units, float
			// maxVelocity)上面的1000即为此处的units。
			// maxVelocity必须为正，表示当计算出的速率大于maxVelocity时为maxVelocity
			// 小于maxVelocity就为计算出的速率
			int velocityX = (int) velocityTracker.getXVelocity();
			Log.e(TAG, "[velocityX] : " + velocityX);
			if (velocityX > SNAP_VELOCITY && mCurScreen > 0) {// 如果速度为正，则表示向右滑动。需要指定mCurScreen大于0，才能滑，不然就不准确啦
				Log.e(TAG, "速度为正且-->：当前mCurScreen = " + mCurScreen);
				Log.e(TAG, "要走到的：mCurScreen = " + (mCurScreen - 1));
				snap2DestScreen(mCurScreen - 1);

			} else if (velocityX < -SNAP_VELOCITY
					&& mCurScreen < (getChildCount() - 1)) {// 如果速度为负，则表示手指向左滑动。需要指定mCurScreen小于最后一个子view的id，才能滑，不然就不准确啦
				Log.e(TAG, "速度为fu且《--：当前mCurScreen = " + mCurScreen);
				Log.e(TAG, "要走到的：mCurScreen = " + (mCurScreen + 1));
				snap2DestScreen(mCurScreen + 1);
			} else { // 速度小于我们规定的达标速度，那么就让界面跟着手指滑动显示。最后显示哪个screen再做计算（方法中有计算）
				Log.e(TAG, "速度的绝对值小于规定速度,走snapToDestination方法");
				snapToDestination();
			}
			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST; // 为什么这里要设置？？？
			break;
		case MotionEvent.ACTION_CANCEL:// 1,设置触摸事件状态为空闲
			Log.e(TAG, "----ACTION_CANCEL----");
			mTouchState = TOUCH_STATE_REST;
			break;
		}
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.e(TAG, "----onInterceptTouchEvent----");
		final int action = ev.getAction();
		// 如果
		if ((action == MotionEvent.ACTION_MOVE)
				&& mTouchState != TOUCH_STATE_REST) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {

		case MotionEvent.ACTION_DOWN:// 判断滚动是否停止
			Log.e(TAG, "----ACTION_DOWN----");
			mLastMotionX = x;
			mLastMotionY = y;
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;

			break;
		case MotionEvent.ACTION_MOVE:// 判断是否达成滚动条件
			Log.e(TAG, "----ACTION_MOVE----");
			final int xDiff = (int) Math.abs(mLastMotionX - x);
			if (xDiff > mTouchSlop) {// 如果该值大于我们规定的最小移动距离则表示界面在滚动
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:// 把状态调整为空闲
			Log.e(TAG, "----ACTION_UP----");
			mTouchState = TOUCH_STATE_REST;
			break;

		}
		// 如果屏幕没有在滚动那么就不消耗这个touch事件
		return mTouchState != TOUCH_STATE_REST;
	}
}