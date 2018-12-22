/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.android.calendar.R;

/**
 * A custom view to draw the day of the month in the today button in the options menu
 */

public class DayOfMonthDrawable extends Drawable {

    private String mDayOfMonth = "1";
    private final Paint mPaint;
    private final Rect mTextBounds = new Rect();
    private static float mTextSize = 14;
    private Context mContext;

    public DayOfMonthDrawable(Context c) {
    	mContext = c;
        mTextSize = c.getResources().getDimension(R.dimen.today_icon_text_size);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(255);
        /** prize-jugda Today UI-wanzhijuan-2015-7-14-start */
        mPaint.setColor(Color.DKGRAY);
        /** prize-jugda Today UI-wanzhijuan-2015-7-14-end */
        //mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.getTextBounds(mDayOfMonth, 0, mDayOfMonth.length(), mTextBounds);
        int textHeight = mTextBounds.bottom - mTextBounds.top;
        Rect bounds = getBounds();
        /** prize-jugda Today UI-wanzhijuan-2015-7-14-start */
        canvas.drawText(mDayOfMonth, bounds.right / 2, ((float) bounds.bottom + textHeight + mContext.getResources().getDimension(R.dimen.today_padding_top)) / 2,
                mPaint);
        /** prize-jugda Today UI-wanzhijuan-2015-7-14-end */
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Ignore
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setDayOfMonth(int day) {
        mDayOfMonth = Integer.toString(day);
        invalidateSelf();
    }
}
