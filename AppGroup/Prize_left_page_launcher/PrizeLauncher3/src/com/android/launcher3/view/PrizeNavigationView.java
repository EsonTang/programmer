/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：整理功能 导航栏 截图控件
 *当前版本：V1.0
 *作	者：zhouerlong
 *完成日期：2015-9-2
 *********************************************/
package com.android.launcher3.view;

import java.util.HashMap;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.gallery3d.util.LogUtils;
import com.android.launcher3.CellAndSpan;
import com.android.launcher3.CellLayout;
import com.android.launcher3.ItemConfiguration;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutAndWidgetContainer;
import com.android.launcher3.Launcher.SpringState;

public class PrizeNavigationView extends ImageView {

	private int mHeight;

	private int mWidth;

	public PrizeNavigationView(Context arg0, AttributeSet arg1, int arg2,
			int arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	Drawable mSelectDrawable = null;
	Drawable mNormalDrawable = null;
	public boolean select = false;
	private Drawable mChildDrawable;
	
	private Drawable  mSelectChild;
	private Drawable mNormalChild;
	private Drawable  mSelectChild_s;
	private Drawable mNormalChild_s;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawRectStroke(canvas, mWidth, mHeight);
		onDrawCell(canvas, mWidth, mHeight);

	}

	private int mDragCellX;
	private int mDragCellY;
	private int mDragspanX;
	private int mDragspanY;
	private int mDragOldX;
	private int mDragOldY;
	private View mDragView;
	private float mDragUpdate;

	public void cellToPosition(int cellX, int cellY, int spanx, int spany,
			View v, float value, int oldCellX, int oldCellY) {
		mDragCellX = cellX;
		mDragCellY = cellY;
		mDragspanX = spanx;
		mDragspanY = spany;
		mDragOldX = oldCellX;
		mDragOldY = oldCellY;
		if (v != null) {
			mDragView = v;
		}
		mDragUpdate = value;
		this.requestLayout();
	}

	CellLayout mCellLayout;

	public void drawRectStroke(Canvas canvas, int w, int h) {
		canvas.save();
		Paint paint = new Paint();
		paint.setStyle(Style.STROKE);// 空心矩形框
		if (select != false) {
			paint.setColor(Color.CYAN);
			paint.setStrokeWidth(8);
//			canvas.drawRect(new RectF(0, 0, w, h), paint);
			mSelectDrawable.setBounds(0, 0, w, h);
			mSelectDrawable.draw(canvas);
		} else {

			paint.setStrokeWidth(3);
			mNormalDrawable.setBounds(0, 0, w, h);
			mNormalDrawable.draw(canvas);
			paint.setColor(Color.WHITE);
//			canvas.drawRect(new RectF(0, 0, w, h), paint);
		}
		canvas.restore();
	}

	public void Ondrop() {
		mDragView = null;
		this.invalidate();
		LogUtils.i("zhouerlong", "mDragView:----Ondrop" + mDragView);
	}

	// private ItemConfiguration solution;

	public void animateChildToPosition(ItemConfiguration finalSolution) {

	}

	class ItemSolution {
		public int newCellX;
		public int newCellY;
		public int oldCellX;
		public int oldCellY;
		public ItemInfo info;
		public float update;

		public ItemSolution(int newCellX, int newCellY, int oldCellX,
				int oldCellY, ItemInfo info) {
			super();
			this.newCellX = newCellX;
			this.newCellY = newCellY;
			this.oldCellX = oldCellX;
			this.oldCellY = oldCellY;
			this.info = info;
		}

	}

	HashMap<View, ItemSolution> solutions = new HashMap<>();

	public void onAnimateChildToPosition(int newCellX, int newCellY,
			int oldCellX, int oldCellY, View child, ItemInfo info) {
		solutions.put(child, new ItemSolution(newCellX, newCellY, oldCellX,
				oldCellY, info));
	}

	private void onDrawAnimationChildToPosition(Canvas canvas, Paint p,
			int cellW, int cellH, int grap, ItemSolution solution) {
		Log.i("zhouerlong", "update:solution.update " + solution.update
				+ " mUpdate:" + mUpdate);
		if (solution != null) {
			// p.setColor(Color.RED);
			int newX = solution.newCellX;
			int newY = solution.newCellY;
			int oldX = solution.oldCellX;
			int oldY = solution.oldCellY;

			int destLeft = newX * cellW + (newX + 1) * grap;
			int destTop = newY * cellH + (newY + 1) * grap;
			int l = oldX * cellW + (oldX + 1) * grap;
			int t = oldY * cellH + (oldY + 1) * grap;
			int targetX = (int) (l + (destLeft - l) * solution.update);
			int targetY = (int) (t + (destTop - t) * solution.update);
			// if (solution.update>0.9f)
			// Log.i("zhouerlong", "update:solution.update"+solution.update);
			canvas.translate(targetX, targetY);
		}
	}

	public void onDrawCell(Canvas canvas, int w, int h) {
		ShortcutAndWidgetContainer shortcutParent = mCellLayout
				.getShortcutsAndWidgets();

		int columns = mCellLayout.getCountX();
		int nums = mCellLayout.getCountY();
		
		Drawable childDrawble;
		childDrawble =mChildDrawable;
		int grap = 8;
		int cellW = (w - grap * (columns + 1)) / columns;
		int cellH = (h - grap * (nums + 1)) / nums;
		for (int i = 0; i < shortcutParent.getChildCount(); i++) {
			canvas.save();
			View child = shortcutParent.getChildAt(i);
			ItemInfo info = (ItemInfo) child.getTag();
			int l = info.cellX * cellW + (info.cellX + 1) * grap;
			int t = info.cellY * cellH + (info.cellY + 1) * grap;
			int spany = info.spanY * cellW + (info.spanY - 1) * grap;
			int spanx = info.spanX * cellW + (info.spanX - 1) * grap;
			
			if(info.spanY*info.spanX>1) {
				mSelectChild = mSelectChild_s;
				mNormalChild = mNormalChild_s;
			}
			mChildDrawable.setBounds(0, 0, cellW, cellH);
			Paint p = new Paint();
			ItemSolution solution = solutions.get(child);

			Launcher launcher = (Launcher) this.getContext();
			if (launcher.getSpringState() == SpringState.BATCH_EDIT_APPS) {
				if (info.mItemState == ItemInfo.State.BATCH_SELECT_MODEL) {
					p.setColor(Color.CYAN);
					childDrawble = mSelectChild;
				} else {
					p.setColor(Color.WHITE);
					childDrawble = mNormalChild;
				}
			} else {

				p.setColor(Color.WHITE);
				childDrawble = mNormalChild;
			}
			LogUtils.i("zhouerlong", "mDragView:" + mDragView);
			if (child == mDragView) {
				p.setColor(Color.CYAN);
				childDrawble = mSelectChild;
				int destLeft = mDragCellX * cellW + (mDragCellX + 1) * grap;
				int destTop = mDragCellY * cellH + (mDragCellY + 1) * grap;
				l = mDragOldX * cellW + (mDragOldX + 1) * grap;
				t = mDragOldY * cellH + (mDragOldY + 1) * grap;
				int targetX = (int) (l + (destLeft - l) * mDragUpdate);
				int targetY = (int) (t + (destTop - t) * mDragUpdate);
				canvas.translate(targetX, targetY);
			} else if (!solutions.isEmpty() && solution != null) {

				onDrawAnimationChildToPosition(canvas, p, cellW, cellH, grap,
						solution);
			} else {
				canvas.translate(l, t);
			}
			Rect rs = new Rect(0, 0, spanx, spany);
			childDrawble.setBounds(0, 0, spanx, spany);
			if (launcher.getSpringState() == SpringState.BATCH_EDIT_APPS
					&& info.mItemState == ItemInfo.State.BATCH_SELECT_MODEL
					&& mCellLayout.isDragging()) {

			} else {
//				canvas.drawRect(rs, p);
				childDrawble.draw(canvas);
				
			}
			canvas.restore();

		}

	}

	public PrizeNavigationView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PrizeNavigationView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PrizeNavigationView(Context context, View cell, int width, int height) {

		super(context);

		mSelectDrawable = context
				.getDrawable(R.drawable.rect_strok_select);
		mNormalDrawable = context
				.getDrawable(R.drawable.rect_strok);
		mChildDrawable = context
				.getDrawable(R.drawable.launcher_screen_manage_thumbnail_bg_normal);
		mSelectChild = context
				.getDrawable(R.drawable.edit_select_icon);
		mNormalChild = context
				.getDrawable(R.drawable.edit_select_normal);
		mSelectChild_s = context.getDrawable(R.drawable.edit_select_icon_s);
		mNormalChild_s = context.getDrawable(R.drawable.edit_select_normal_s);
		mCellLayout = (CellLayout) cell;
		mCellLayout.setPrizeNavigationView(this);
		mWidth = width;
		mHeight = height;

	}

	public void clearItems() {

		solutions.clear();
	}

	class CellAndSpanUpdateInfo {
		public int cellX;
		public int cellY;
		public int spanX;
		public int spanY;
		public int targetCellX;
		public int targetCellY;
		public float update;
	}

	float mUpdate;
	HashMap<CellAndSpanUpdateInfo, ItemInfo> items = new HashMap<>();

	public void onAnimationUpdate(float value, View child) {
		ItemSolution s = solutions.get(child);
		if (s != null) {
			// if (value>0.9f)
			LogUtils.i("zhouerlong", "onAnimationUpdate----:solution.update" + value);
			s.update = value;
			mUpdate = value;
			this.requestLayout();
		}

	}

}
