/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

public class Sticker implements ISticker {

    private Bitmap mBitmap;

    private boolean mIsFocus;

    private Matrix mMatrix;

    private Paint mEdgePaint;

    private float[] mSrcPoints;

    private float[] mDstPoints = new float[10];

    private float mScale = 1.0f;
    private boolean mIsSymmetric;
    
    public Sticker(Bitmap bitmap, int bgWidth, int bgHeight, int margin) {
        this.mBitmap = bitmap;

        mEdgePaint = new Paint();
        mEdgePaint.setAntiAlias(true);
        mEdgePaint.setFilterBitmap(true);
        mEdgePaint.setStyle(Paint.Style.STROKE);
        mEdgePaint.setStrokeWidth(2.0f);
        mEdgePaint.setColor(Color.WHITE);

        mMatrix = new Matrix();
        float mLeft = (bgWidth - bitmap.getWidth()) / 2;
        float mTop = (bgHeight - bitmap.getHeight()) / 2;
        mMatrix.postTranslate(mLeft, mTop);

        margin = 0;
        float px = bitmap.getWidth();
        float py = bitmap.getHeight();
        float x0 = margin;
        float y0 = margin;
        
        float x1 = px + margin;
        float y1 = margin;
        
        float x2 = px + margin;
        float y2 = py + margin;
        
        float x3 = margin;
        float y3 = py + margin;
        
        float centerX = px / 2 + margin;
        float centerY = py / 2 + margin;
        mSrcPoints = new float[]{x0, y0, x1, y1, x2, y2, x3, y3, centerX, centerY};
    }
    
    @Override
    public void setScale(float scaleSize) {
        this.mScale = scaleSize;
    }

    @Override
    public float[] getDstPoints() {
        return mDstPoints;
    }

    @Override
    public float getScale() {
        return mScale;
    }

    @Override
    public Bitmap getBitmap(boolean isSave) {
        return mBitmap;
    }

    @Override
    public boolean isFocus() {
        return mIsFocus;
    }

    @Override
    public Matrix getMatrix() {
        return mMatrix;
    }

    @Override
    public Paint getEdgePaint() {
        return mEdgePaint;
    }

    @Override
    public float[] getSrcPoints() {
        return mSrcPoints;
    }

    @Override
    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public void setFocus(boolean focusable) {
        this.mIsFocus = focusable;
    }

	@Override
	public Object getTouchResource(float x, float y) {
		return null;
	}

	@Override
	public void replaceWord(String x) {
		
	}

	@Override
	public boolean isInWatemark(float x, float y) {
		Matrix matrix = new Matrix(mMatrix);
        matrix.invert(matrix);
        float[] vertexs = mSrcPoints;  
        RectF rectF = new RectF(vertexs[0], vertexs[1], vertexs[4], vertexs[5]);
        float[] xy = new float[2];
        matrix.mapPoints(xy, new float[]{x, y});
        if (rectF.contains(xy[0], xy[1])) {
        	return true;
        }
        return false;
	}
	
	@Override
	public void invertColor() {
	}

	@Override
	public boolean isBlackColor() {
		return false;
	}

	@Override
	public void doSymmetric() {
		Matrix matrix = new Matrix();
		matrix.postScale(-1, 1);
		matrix.postTranslate(mSrcPoints[0] + mSrcPoints[2], 0);
		matrix.postConcat(mMatrix);
		mMatrix.set(matrix);
		mIsSymmetric = !mIsSymmetric;
	}

	@Override
	public boolean isSymmetric() {
		return mIsSymmetric;
	}

	@Override
	public void replaceAddress(String address, double longitude, double latitude) {
		
	}

	@Override
	public boolean isAddr() {
		return false;
	}

	@Override
	public boolean isSticker() {
		return true;
	}

	@Override
	public boolean isTextColorChange() {
		return false;
	}

	@Override
	public boolean isInvertColor() {
		// TODO Auto-generated method stub
		return false;
	}

}
