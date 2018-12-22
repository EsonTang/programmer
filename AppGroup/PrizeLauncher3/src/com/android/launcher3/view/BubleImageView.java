package com.android.launcher3.view;

import com.android.launcher3.DrawEditIcons;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BubleImageView extends ImageView {

	public BubleImageView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public BubleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public BubleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BubleImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
/*
		Launcher launcher = null;
		if (this.getContext() instanceof Launcher) {
			launcher = (Launcher) this.getContext();

		}
		Drawable d = this.getContext().getDrawable(
				R.drawable.ic_launcher_delete_holo);
		int w = d.getIntrinsicWidth();
		int h = d.getIntrinsicHeight();
		FolderInfo shortcutInfo = (FolderInfo) this.getTag();
		if (launcher.getSpringState() == Launcher.SpringState.BATCH_EDIT_APPS
				&& shortcutInfo != null
				&& shortcutInfo.mItemState == ItemInfo.State.BATCH_SELECT_MODEL) {
			DrawEditIcons.drawStateIconForBatch(canvas, this,
					R.drawable.in_use, w, h, 1f);// 此处为画文件夹编辑小图标
		}*/
	}
	
	

}
