
package com.prize.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import com.android.gallery3d.R;

import com.android.gallery3d.app.TimeBar;
import com.prize.util.LogTools;

public class TimeSeekBar extends TimeBar {


	private static final int PADDING = 2; // DP

	private static final int V_PADDING = 4; // DP

	private static final int H_PADDING_IN_DP = 8; // DP

	private static final int TIME_MARGIN_IN_DP = 16; // DP
	private static final int TIME_RIGHT_ADJUST_IN_DP = 3; // DP

	private int mHPaddingPx; // PX

	private int mTimeMarginPx; // PX
	private Context mContext;
	private int mAdjustRight;
    public TimeSeekBar(Context context, Listener listener) {
        
        super(context, listener);
        mContext = context;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScrubberPadding = (int) (PADDING * metrics.density);
        mVPaddingInPx = (int) (V_PADDING * metrics.density);
        mPlayedPaint.setColor(0xFF1283CF); 
        mHPaddingPx = (int) (H_PADDING_IN_DP * metrics.density);
        mTimeMarginPx = (int) (TIME_MARGIN_IN_DP * metrics.density);
        mAdjustRight = (int) (TIME_RIGHT_ADJUST_IN_DP * metrics.density);
    }


    @Override
    public int getPreferredHeight() {
        
        return Math.max(mScrubber.getHeight(), mTimeBounds.height()) + mVPaddingInPx + mScrubberPadding;
    }
    
    

    @Override
	protected void setScrubber() {
    	mScrubber = BitmapFactory.decodeResource(getResources(), R.drawable.ic_scrubber_knob);
	}


    @Override
    public int getBarHeight() {
        
        int barHeight = 2 * mScrubberPadding + mScrubber.getHeight() + mVPaddingInPx;
        return barHeight;
    }

    @Override
    protected void layoutSeekBar(boolean changed, int l, int t, int r, int b) {
        
        int w = r - l;
        int h = b - t;
        if (!mShowTimes && !mShowScrubber) {
            mProgressBar.set(0, 0, w, h);
        } else {
            int margin = mScrubber.getWidth() / 3;
            int padding = 0;
        	if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                padding = mTimeMarginPx;
            }
            if (mShowTimes) { // 修改进度条显示的长度，给时间预留位置
                margin += mTimeBounds.width() + mHPaddingPx + padding;
            }
            LogTools.i(TAG, "layoutSeekBar() margin=" + margin + " hPadding=" + mHPaddingPx);
            // margin = mLayoutExt.getProgressMargin(margin);
            int progressY = (h - 4) / 2;
            mScrubberTop = progressY - mScrubber.getHeight() / 2 + 1;
            mProgressBar.set(getPaddingLeft() + margin, progressY, w
                    - getPaddingRight() - margin, progressY + 4);
        }
     }

    @Override
    protected void drawTimes(Canvas canvas) {
    	int padding = 0;
    	if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = mTimeMarginPx;
        }
    	LogTools.i(TAG, "drawTimes() padding=" + padding);
        if (mShowTimes) { 
            canvas.drawText(stringForTime(mCurrentTime), mTimeBounds.width()
                    / 2 + getPaddingLeft() + padding,
                    (mTimeBounds.height() + getHeight()) / 2, mTimeTextPaint);
            canvas.drawText(stringForTime(mTotalTime), getWidth()
                    - getPaddingRight() - mTimeBounds.width() / 2 - padding - mAdjustRight,
                    (mTimeBounds.height() + getHeight()) / 2, mTimeTextPaint);

        }
    }


    @Override
    protected String stringForTime(long millis) {
        
        int totalSeconds = (int) millis / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return String.format("%2d:%02d:%02d", hours, minutes, seconds).toString();
    }
}

