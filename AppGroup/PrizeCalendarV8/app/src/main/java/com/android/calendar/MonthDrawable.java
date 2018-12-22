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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.android.calendar.R;

/**
 * A custom view to draw the day of the month in the today button in the options menu
 */

public class MonthDrawable extends Drawable {

    private String mMonth = "1";
    private final Paint mPaint;
    private final Rect mTextBounds = new Rect();
    private static float mTextSize = 14;
    private Resources mRes;
    private String[] mMonths;

    public MonthDrawable(Context c) {
    	mRes = c.getResources();
        mTextSize = mRes.getDimension(R.dimen.month_title_icon_text_size);
        mMonths = mRes.getStringArray(R.array.prize_gerge_month_array);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(255);
        mPaint.setColor(mRes.getColor(R.color.prize_black_3));
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.RGBA_8888;
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.getTextBounds(mMonth, 0, mMonth.length(), mTextBounds);
        int textHeight = mTextBounds.bottom - mTextBounds.top;
        Rect bounds = getBounds();
        canvas.drawText(mMonth, bounds.width() / 2, ((float) bounds.height() + textHeight - mRes.getDimension(R.dimen.month_title_padding_top)) / 2,
                mPaint);
    }

    public void setMonth(int month) {
    	mMonth = mMonths[month];
        invalidateSelf();
    }
	
	/*private final String text;
    private final Paint paint;

    public MonthDrawable(String text) {

        this.text = text;

        this.paint = new Paint();
        paint.setColor(Color.parseColor("#323232"));
        paint.setTextSize(22f);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawText(text, 0, 0, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }*/
}
