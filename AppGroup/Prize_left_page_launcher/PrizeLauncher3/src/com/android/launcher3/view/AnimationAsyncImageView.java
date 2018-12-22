package com.android.launcher3.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.AsyncImageView;
import com.android.launcher3.R;

public class AnimationAsyncImageView extends AsyncImageView {

	public AnimationAsyncImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 画边框 暂时去除小边框
		Drawable unreadBgNinePatchDrawable = (Drawable) getContext()
				.getDrawable(R.drawable.in_use);
		int w = unreadBgNinePatchDrawable.getIntrinsicWidth() / 4;
		int h = unreadBgNinePatchDrawable.getIntrinsicHeight() / 4;
		drawSelect(canvas, this, w, h);
	}

	static void drawSelect(Canvas canvas, View icon, int w, int h) {
		if (icon.isSelected()) {
			Resources res = icon.getContext().getResources();

			Drawable unreadBgNinePatchDrawable = (Drawable) res
					.getDrawable(R.drawable.in_use);
			int unreadBgWidth = w;// (int)
									// (unreadBgNinePatchDrawable.getIntrinsicWidth()/1.8f);
			int unreadBgHeight = h;// (int)
									// (unreadBgNinePatchDrawable.getIntrinsicHeight()/1.8f);
			Rect unreadBgBounds = new Rect(0, 0, unreadBgWidth, unreadBgHeight);
			unreadBgNinePatchDrawable.setBounds(unreadBgBounds);

			int unreadMarginTop = unreadBgWidth ;
			int unreadMarginRight = unreadBgWidth /2;

			int unreadBgPosX = icon.getScrollX() + icon.getWidth()
					- unreadBgWidth - unreadMarginRight;
			int unreadBgPosY = icon.getScrollY() + unreadMarginTop;

			canvas.save();
			canvas.translate(unreadBgPosX, unreadBgPosY);

			unreadBgNinePatchDrawable.draw(canvas);
			canvas.restore();
		}
	}

}
