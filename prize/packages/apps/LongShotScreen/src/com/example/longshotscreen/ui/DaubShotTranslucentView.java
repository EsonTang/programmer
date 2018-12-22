package com.example.longshotscreen.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import com.example.longshotscreen.utils.Log;
import android.view.View;
import java.util.Iterator;
import java.util.Stack;

public class DaubShotTranslucentView extends View
{
	private Paint clearPaint;
	private Stack<Path> daubPaths;
	private boolean isDrawPath = false;
	private Paint mPaint;
	private Path mPath;
	private int mScreenHeight;
	private int mScreenWidth;

	public DaubShotTranslucentView(Context paramContext, AttributeSet paramAttributeSet)
	{
		super(paramContext, paramAttributeSet);
	}

	protected void clearCanvasAndDrawTranslucent()
	{
		this.isDrawPath = false;
		invalidate();
	}

	protected void drawPath(Path paramPath)
	{
		this.isDrawPath = true;
		this.mPath = paramPath;
		invalidate();
	}

	protected void onDraw(Canvas canvas)
	{
		Log.i("DaubShotTranslucentView", "canvas.isHardwareAccelerated() = " + canvas.isHardwareAccelerated());
		if(isDrawPath) {
			canvas.saveLayer(0.0f, 0.0f, (float)mScreenWidth, (float)mScreenHeight, null, 0x1f);
			if((mPath != null) && (mPaint != null)) {
				if(clearPaint != null) {
					canvas.drawColor(Color.parseColor("#67000000"));
				}
				if((daubPaths != null) && (!daubPaths.isEmpty())) {
					for(Path path : daubPaths) {
						canvas.drawPath(path, mPaint);
					}
				}
				canvas.drawPath(mPath, mPaint);
			}
			canvas.restore();
		} else if(clearPaint != null) {
			canvas.drawColor(Color.parseColor("#67000000"));
		}
		super.onDraw(canvas);
	}

	protected void setClearPaint(Paint paramPaint)
	{
		this.clearPaint = paramPaint;
	}

	protected void setDaubPaths(Stack<Path> paramStack)
	{
		this.daubPaths = paramStack;
	}

	protected void setPaint(Paint paramPaint)
	{
		this.mPaint = paramPaint;
	}

	protected void setWidthAndHeight(int paramInt1, int paramInt2)
	{
		this.mScreenWidth = paramInt1;
		this.mScreenHeight = paramInt2;
	}
}
