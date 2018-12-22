/**
 * Copyright (C) 2015 ogaclejapan
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.prize.ui;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import com.android.camera.R;

/**
 * <p>
 * Forked from Google Samples &gt; SlidingTabsBasic &gt; <a href=
 * "https://developer.android.com/samples/SlidingTabsBasic/src/com.example.android.common/view/SlidingTabLayout.html"
 * >SlidingTabStrip</a>
 */
class HorizontalScrollStrip extends LinearLayout {

	protected static final String TAG = "HorizontalScrollStrip";


	private Paint dividerPaint;
	private boolean mIsDivider = true;

	HorizontalScrollStrip(Context context, AttributeSet attrs) {
		super(context);
		setWillNotDraw(false);
		dividerPaint = new Paint(/*Paint.ANTI_ALIAS_FLAG*/);
	    dividerPaint.setStrokeWidth(1);
	    dividerPaint.setColor(context.getResources().getColor(R.color.center_view_divider));
	    setBackgroundColor(Color.TRANSPARENT);
	}
	
	HorizontalScrollStrip(Context context, AttributeSet attrs, boolean divider) {
		super(context);
		setWillNotDraw(false);
		mIsDivider = divider;
	    setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mIsDivider) {
			drawDecoration(canvas);
		}
	}

	private void drawDecoration(Canvas canvas) {
		final int height = getHeight();
		final int tabCount = getChildCount();
		// Vertical separators between the titles
		drawSeparator(canvas, height, tabCount);

	}

	private void drawSeparator(Canvas canvas, int height, int tabCount) {

		// Vertical separators between the titles
		final int separatorTop = 0;
		final int separatorBottom = separatorTop + height;

		for (int i = 0; i < tabCount; i++) {
			View child = getChildAt(i);
			int end = Utils.getEnd(child);
			int separatorX = end;
			if (i == 0) {
				int start = Utils.getStart(child);
				canvas.drawLine(start, separatorTop, start,
						separatorBottom, dividerPaint);
			}
			canvas.drawLine(separatorX - 1, separatorTop, separatorX - 1,
					separatorBottom, dividerPaint);
		}
	}

}
