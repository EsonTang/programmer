package com.android.launcher3;

import com.android.launcher3.DropTarget.DragObject;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class FrameLayoutDropTarget extends FrameLayout implements DropTarget, DragController.DragListener,DragSource{

	private Launcher mLauncher;

	public FrameLayoutDropTarget(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FrameLayoutDropTarget(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FrameLayoutDropTarget(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDragStart(DragSource source, Object info, int dragAction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragEnd() {
		// TODO Auto-generated method stub
		
	}
	
	public void setup(Launcher launcher, DragController dragController) {
        dragController.addDragListener(this);
        dragController.addDropTarget(this);
        mLauncher=launcher;
    }
	
	@Override
	public boolean isDropEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragEnter(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragOver(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragExit(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFlingToDelete(DragObject dragObject, int x, int y, PointF vec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean acceptDrop(DragObject dragObject) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void getHitRectRelativeToDragLayer(Rect outRect) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getLocationInDragLayer(int[] loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsFlingToDelete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onFlingToDeleteCompleted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDropCompleted(View target, DragObject d,
			boolean isFlingToDelete, boolean success) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFristEnter(DragObject dragObject) {
		// TODO Auto-generated method stub
		
	}

	@Override

	public void onStartMuitipleDrag(View child, DragObject dragobject,
			Runnable exitSpringLoadedRunnable,View dragChild) {		// TODO Auto-generated method stub
		
	}

}
