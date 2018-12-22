/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：oppo风格整理图标底部控件
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HideAppsView extends FrameLayout  {
	private static final String TAG = "EditAppsHotseat";

	private CellLayout mContent;

	private Alarm mReorderAlarm = new Alarm();
	private Launcher mLauncher;

	public static final int DRAG_BITMAP_PADDING = 2;
	private IconCache mIconCache;
	private static final int REORDER_ANIMATION_DURATION = 230;
	private static final int REORDER_DELAY = 250;

	private boolean mTransposeLayoutWithOrientation;
	private boolean mIsLandscape;
	protected float mLastMotionY;

	protected int mTouchSlop;// 使让桌面滑动起来的最低限度值

	protected static final int INVALID_POINTER = -1;

	// add by zhouerlong

	public HideAppsView(Context context) {
		this(context, null);
	}

	public HideAppsView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private int getContentAreawidth() {
		LauncherAppState app = LauncherAppState.getInstance();
		DeviceProfile grid = app.getDynamicGrid().getDeviceProfile();
		Rect workspacePadding = grid
				.getWorkspacePadding(grid.isLandscape ? CellLayout.LANDSCAPE
						: CellLayout.PORTRAIT);
		int maxContentAreawidth = grid.availableWidthPx - 1 * grid.edgeMarginPx
				- getPaddingLeft() - getPaddingRight();
		return Math.min(maxContentAreawidth, mContent.getDesiredWidth());

	}
	
	

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int height = getPaddingTop() + getPaddingBottom()
				+ mContent.getDesiredHeight();
		int width = mContent.getDesiredWidth();
		int contentAreaHeightSpec = MeasureSpec.makeMeasureSpec(height,
				MeasureSpec.EXACTLY);
		int contentAreaWidthSpec = MeasureSpec.makeMeasureSpec(width,
				MeasureSpec.EXACTLY);
		
		mContent.setFixedSize(width, height);
		mScrollView.measure(contentAreaWidthSpec, heightMeasureSpec);
		setMeasuredDimension(widthSize, heightSize);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}

	public HideAppsView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Resources r = context.getResources();
		mTransposeLayoutWithOrientation = r
				.getBoolean(R.bool.hotseat_transpose_layout_with_orientation);
		mIsLandscape = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledPagingTouchSlop();

		LauncherAppState app = LauncherAppState.getInstance();
		mIconCache = app.getIconCache();

	}

	public void setup(Launcher launcher) {
		mLauncher = launcher;
		// setOnKeyListener(new HotseatIconKeyEventListener());
	}

	CellLayout getLayout() {
		return mContent;
	}

	public void onDrop(final Object dragInfo, boolean pullOrPush) {

		ItemInfo info = (ItemInfo) dragInfo;
		mReorderAlarm.cancelAlarm();
		mTargetItemInfo = info;
		mPushOrPull = pullOrPush;
		mReorderAlarm.setOnAlarmListener(mReorderAlarmListener);
		mReorderAlarm.setAlarm(REORDER_DELAY);
	}

	ItemInfo mTargetItemInfo;
	boolean mPushOrPull = false;
	OnAlarmListener mReorderAlarmListener = new OnAlarmListener() {
		public void onAlarm(Alarm alarm) {
			realTimeReorder(mTargetItemInfo, mPushOrPull);
		}
	};
	private boolean successfulDrop = true;

	private int[] mTargetCell = new int[] { 0, 0 };

	private ScrollView mScrollView;

	private DeviceProfile mgrid;

	public void onDropShortExternal(final Object dargInfo, View v) {
		onDropShortExternal(dargInfo, mContent, false, 500, v);
	}
	

	public void onDropShortExternal(final Object dragInfo,
			final CellLayout cellLayout, boolean insertAtFirst, int duration,
			View v) {
		final Runnable exitSpringLoadedRunnable = new Runnable() {
			@Override
			public void run() {
				successfulDrop = true;
			}
		};

		// This is for other drag/drop cases, like dragging from All Apps
		View shotview = null;

		ItemInfo info = (ItemInfo) dragInfo;
		// onDrop(info, true);
		if (successfulDrop == false) {
			return;
		}
		successfulDrop = false;
		switch (info.itemType) {
		case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
		case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
			if (info.container == NO_ID && info instanceof AppInfo) {
				// Came from all apps -- make a copy
				info = new ShortcutInfo((AppInfo) info);
			}
			shotview = mLauncher.createShortcut(R.layout.application,
					cellLayout, (ShortcutInfo) info);
			break;
		case LauncherSettings.Favorites.ITEM_TYPE_FOLDER:
			shotview = FolderIcon.fromXml(R.layout.folder_icon, mLauncher,
					cellLayout, (FolderInfo) info, mIconCache);
			break;
		default:
			throw new IllegalStateException("Unknown item type: "
					+ info.itemType);
		}
		mTargetCell[0] = 0;
		mTargetCell[1] = 0;
		int spanX = info.spanX;
		int spanY = info.spanY;
		int x = 0;
		int y = 0;
		ViewGroup.LayoutParams genericLp = v.getLayoutParams();
		CellLayout.LayoutParams lp;
		if (genericLp == null
				|| !(genericLp instanceof CellLayout.LayoutParams)) {
			lp = new CellLayout.LayoutParams(x, y, spanX, spanY);
		} else {
			lp = (CellLayout.LayoutParams) genericLp;
			lp.cellX = mTargetCell[0];
			lp.cellY = mTargetCell[1];
			lp.cellHSpan = spanX;
			lp.cellVSpan = spanY;
		}
		ShortcutInfo shotviewInfo = (ShortcutInfo) shotview.getTag();
		shotviewInfo.cellX = lp.cellX;
		shotviewInfo.cellY = lp.cellY;
		shotviewInfo.spanX = lp.cellHSpan;
		shotviewInfo.spanY = lp.cellVSpan;
		realTimeReorder(info, true);

		mContent.addViewToCellLayout(shotview, -1, -1, lp, true);
		cellLayout.onDropChild(shotview);
		CellLayout.LayoutParams lps = (CellLayout.LayoutParams) shotview
				.getLayoutParams();
		cellLayout.getShortcutsAndWidgets().measureChild(shotview);
		/*
		 * LauncherModel.addOrMoveItemInDatabase(mLauncher, info, container,
		 * screenId, lp.cellX, lp.cellY);
		 */
		DragView dragView = createDragView(v);
		if (v instanceof PagedViewIcon) {
			dragView = this.createDragView(v);
			// 修改这里表示如果是编辑模式的app 的话就将编辑界面 apps 选项的位置提供 否则应该是文件夹分解 如果不加这里 会发现
			// 如果文件夹分解的时候会出现文件夹图标向下面移动 而不是 分解后的app icons
		}
		dragView.showNoAnimation(dragView.getDragRegionLeft(),
				dragView.getDragRegionTop());
		if (dragView != null) {
			// We wrap the animation call in the temporary set and reset of
			// the current
			// cellLayout to its final transform -- this means we animate
			// the drag view to
			// the correct final location.
			mLauncher.getDragLayer().animateViewIntoPosition(dragView,
					shotview, exitSpringLoadedRunnable, -1, duration, true,
					null);// widget or apps
		}

	}

	/**
	 * Returns a new bitmap to show when the given View is being dragged around.
	 * Responsibility for the bitmap is transferred to the caller.
	 */
	public Bitmap createDragBitmap(View v, Canvas canvas, int padding) {
		Bitmap b;

		if (v instanceof TextView) {
			Drawable d = ((TextView) v).getCompoundDrawables()[1];
			/*
			 * b = Bitmap.createBitmap(d.getIntrinsicWidth() + padding,
			 * d.getIntrinsicHeight() + padding, Bitmap.Config.ARGB_8888);
			 */

			b = Utilities.createIconBitmap(d, mContext);
		} else {
			b = Bitmap.createBitmap(v.getWidth() + padding, v.getHeight()
					+ padding, Bitmap.Config.ARGB_8888);
		}

		canvas.setBitmap(b);
		drawDragView(v, canvas, padding, true);
		canvas.setBitmap(null);

		return b;
	}

	/**
	 * Draw the View v into the given Canvas.
	 * 
	 * @param v
	 *            the view to draw
	 * @param destCanvas
	 *            the canvas to draw on
	 * @param padding
	 *            the horizontal and vertical padding to use when drawing
	 */
	private void drawDragView(View v, Canvas destCanvas, int padding,
			boolean pruneToDrawable) {
		final Rect clipRect = new Rect();
		v.getDrawingRect(clipRect);

		boolean textVisible = false;

		destCanvas.save();
		if (v instanceof TextView && pruneToDrawable) {
			Drawable d = ((TextView) v).getCompoundDrawables()[1];
			clipRect.set(0, 0, d.getIntrinsicWidth() + padding,
					d.getIntrinsicHeight() + padding);
			destCanvas.translate(padding / 2, padding / 2);
			d.draw(destCanvas);
		} else {
			if (v instanceof FolderIcon) {
				// For FolderIcons the text can bleed into the icon area, and so
				// we need to
				// hide the text completely (which can't be achieved by
				// clipping).
				if (((FolderIcon) v).getTextVisible()) {
					((FolderIcon) v).setTextVisible(false);
					textVisible = true;
				}
			} else if (v instanceof BubbleTextView) {
				final BubbleTextView tv = (BubbleTextView) v;
				clipRect.bottom = tv.getExtendedPaddingTop()
						- (int) BubbleTextView.PADDING_V
						+ tv.getLayout().getLineTop(0);
			} else if (v instanceof TextView) {
				final TextView tv = (TextView) v;
				clipRect.bottom = tv.getExtendedPaddingTop()
						- tv.getCompoundDrawablePadding()
						+ tv.getLayout().getLineTop(0);
			}
			destCanvas.translate(-v.getScrollX() + padding / 2, -v.getScrollY()
					+ padding / 2);
			destCanvas.clipRect(clipRect, Op.REPLACE);
			v.draw(destCanvas);

			// Restore text visibility of FolderIcon if necessary
			if (textVisible) {
				((FolderIcon) v).setTextVisible(true);
			}
		}
		destCanvas.restore();
	}

	public DragView createDragView(View child) {

		final Bitmap b = createDragBitmap(child, new Canvas(),
				DRAG_BITMAP_PADDING);
		int left = 0;
		int top = 0;
		int loc[] = new int[2];
		mLauncher.getDragLayer().getLocationInDragLayer(child, loc);
		int dragLeft = loc[0];
		int dragTop = loc[1];
		return new DragView(mLauncher, b, dragLeft, dragTop, left, top,
				b.getWidth(), b.getHeight(), 1.0f);// 修改启动点位置
	}

	public void realTimeReorder(ItemInfo info, boolean pushOrPull) {
		int startX = pushOrPull ? 0 : info.cellX + 1;
		int temp = pushOrPull ? 1 : -1;
		int targetCell[] = new int[2];
		int destCell[] = new int[2];
		int endX = mContent.getShortcutsAndWidgets().getChildCount() - 1;
		if (pushOrPull) {
			for (int x = endX; x >= startX; x--) {
				View child = mContent.getChildAt(x, 0);
				ItemInfo iteminfo = (ItemInfo) child.getTag();
				if (iteminfo.equals(info)) {
					continue;
				}
				destCell[0] = iteminfo.cellX + temp;
				destCell[1] = iteminfo.cellY;
				mContent.animateChildToPosition(child, destCell[0],
						destCell[1], REORDER_ANIMATION_DURATION, 0, true, true,null);

			}
		} else {
			for (int x = startX; x <= endX; x++) {
				View child = mContent.getChildAt(x, 0);
				ItemInfo itemInfo = (ItemInfo) child.getTag();
				destCell[0] = itemInfo.cellX + temp;
				destCell[1] = itemInfo.cellY;
				mContent.animateChildToPosition(child, destCell[0],
						destCell[1], REORDER_ANIMATION_DURATION, 0, true, true,null);
			}
		}
	}

	/**
	 * Registers the specified listener on the cell layout of the hotseat.
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mContent.setOnLongClickListener(l);
	}

	private boolean hasVerticalHotseat() {
		return (mIsLandscape && mTransposeLayoutWithOrientation);
	}

	/*
	 * Get the orientation invariant order of the item in the hotseat for
	 * persistence.
	 */
	int getOrderInHotseat(int x, int y) {
		return hasVerticalHotseat() ? (mContent.getCountY() - y - 1) : x;
	}

	/*
	 * Get the orientation specific coordinates given an invariant order in the
	 * hotseat.
	 */
	int getCellXFromOrder(int rank) {
		return hasVerticalHotseat() ? 0 : rank;
	}

	int getCellYFromOrder(int rank) {
		return hasVerticalHotseat() ? (mContent.getCountY() - (rank + 1)) : 0;
	}

	/**
	 * This returns the coordinates of an app in a given cell, relative to the
	 * DragLayer
	 */
	Rect getCellCoordinates(int cellX, int cellY) {
		Rect coords = new Rect();
		mContent.cellToRect(cellX, cellY, 1, 1, coords);
		int[] hotseatInParent = new int[2];
		Utilities.getDescendantCoordRelativeToParent(this,
				mLauncher.getDragLayer(), hotseatInParent, false);
		coords.offset(hotseatInParent[0], hotseatInParent[1]);

		// Center the icon
		int cWidth = mContent.getShortcutsAndWidgets().getCellContentWidth();
		int cHeight = mContent.getShortcutsAndWidgets().getCellContentHeight();
		int cellPaddingX = (int) Math.max(0, ((coords.width() - cWidth) / 2f));
		int cellPaddingY = (int) Math
				.max(0, ((coords.height() - cHeight) / 2f));
		coords.offset(cellPaddingX, cellPaddingY);

		return coords;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LauncherAppState app = LauncherAppState.getInstance();
		mgrid = app.getDynamicGrid().getDeviceProfile();
		mScrollView = (ScrollView) findViewById(R.id.scroll_view);
		mContent = (CellLayout) findViewById(R.id.layout);
		int widthSize = this.getWidth();
		int heightSize = this.getHeight();
		mContent.setCellDimensions(mgrid.folderCellWidthPx,
				mgrid.folderCellHeightPx);
		if (mgrid.isLandscape && !mgrid.isLargeTablet()) {
			mContent.setGridSize(1, (int) mgrid.numHotseatIcons);
		} else {
			mContent.setGridSize(4, 20);
		}
		mContent.setIsHotseat(true);

	}

}
