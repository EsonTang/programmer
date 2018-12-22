package com.android.launcher3;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class PrizeIcon extends TextView implements Iicon<IconInfo> {

	@Override
	protected void onDraw(Canvas c) {
		super.onDraw(c);

		Drawable unreadBgNinePatchDrawable = (Drawable) getContext()
				.getDrawable(R.drawable.in_use);
		int w = unreadBgNinePatchDrawable.getIntrinsicWidth() / 4;
		int h = unreadBgNinePatchDrawable.getIntrinsicHeight() / 4;
		DrawEditIcons.drawSelect(c, this, w, h);
	}

	public PrizeIcon(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public PrizeIcon(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public PrizeIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public PrizeIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void applyIconInfo(IconInfo info) {
		setText(info.title);
		setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
		setTag(info);

	}

}
