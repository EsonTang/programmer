/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.RectF;

public class Watermark implements ISticker {

    private boolean mIsFocus;

    private Matrix mMatrix;

    private Paint mEdgePaint;

    private float[] mSrcPoints;

    private float[] mDstPoints = new float[10];

    private float mScale = 1.0f;
    
    private float mWatemarkWidth;
    private float mWatemarkHeight;
    private Context mContext;
    private int mWordColor = Color.BLACK;
    private boolean mIsSymmetric;
    
    private long mID;
    private boolean mIsSticker;
    private boolean mIsColorChange;
    private boolean mIsInvertColor;
    private float mMarginLeft;
    private float mMarginTop;

    private List<IWatermarkResource> mResourceList = new ArrayList<IWatermarkResource>();
    public Watermark(int screenWidth, int screenHeight, List<IWatermarkResource> resources, Context context) {
        this(0, true, false, screenWidth, screenHeight, resources, context);
    }
    
    public Watermark(long id, boolean isSticker, boolean isColorChange, int screenWidth, int screenHeight, List<IWatermarkResource> resources, Context context) {
        
    	this(Color.BLACK, id, isSticker, isColorChange, screenWidth, screenHeight, resources, context);
    }
    
    public Watermark(int wordColor, long id, boolean isSticker, boolean isColorChange, int screenWidth, int screenHeight, List<IWatermarkResource> resources, Context context) {
        mWordColor = wordColor;
    	mID = id;
    	mIsSticker = isSticker;
    	mIsColorChange = isColorChange;
    	mResourceList.addAll(resources);
        mContext = context;
        mEdgePaint = new Paint();
        mEdgePaint.setAntiAlias(true);
        mEdgePaint.setFilterBitmap(true);
        mEdgePaint.setStyle(Paint.Style.FILL);
        mEdgePaint.setStrokeWidth(2.0f);
        mEdgePaint.setColor(Color.WHITE);
        
        firstComputeSize();
        
        mMatrix = new Matrix();
        float mLeft = (screenWidth - mWatemarkWidth) / 2;
        float mTop = (screenHeight - mWatemarkWidth) / 2;
        mMatrix.postTranslate(mLeft, mTop);

    }
    
    private void resetScrPoints() {
    	float px = mWatemarkWidth;
        float py = mWatemarkHeight;
        float x0 = 0;
        float y0 = 0;
        
        float x1 = px;
        float y1 = 0;
        
        float x2 = px;
        float y2 = py;
        
        float x3 = 0;
        float y3 = py;
        
        float centerX = px / 2;
        float centerY = py / 2;
        mSrcPoints = new float[]{x0, y0, x1, y1, x2, y2, x3, y3, centerX, centerY};
    }
    
    private void firstComputeSize() {
    	for (IWatermarkResource resource : mResourceList) {
    		resource.init(mContext, this);
    		RectF rectF = resource.getRectF();
    		if (mWatemarkWidth < rectF.right) {
    			mWatemarkWidth = rectF.right;
    		}
    		if (mWatemarkHeight < rectF.bottom) {
    			mWatemarkHeight = rectF.bottom;
    		}
    		if (rectF.left < mMarginLeft) {
    			mMarginLeft = rectF.left - 1;
    		}
    		
    		if (rectF.top < mMarginTop) {
    			mMarginTop = rectF.top - 1;
    		}
    	}
    	mWatemarkWidth -= mMarginLeft;
    	mWatemarkHeight -= mMarginTop;
    	resetScrPoints();
    }
    
    private void computeSize() {
    	mWatemarkHeight = 0;
    	mWatemarkWidth = 0;
    	for (IWatermarkResource resource : mResourceList) {
    		RectF rectF = resource.getRectF();
    		if (mWatemarkWidth < rectF.right) {
    			mWatemarkWidth = rectF.right;
    		}
    		if (mWatemarkHeight < rectF.bottom) {
    			mWatemarkHeight = rectF.bottom;
    		}
    	}
    	resetScrPoints();
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
    	Bitmap newb = Bitmap.createBitmap((int) mWatemarkWidth, (int) mWatemarkHeight, Config.ARGB_8888);
    	Canvas canvas = new Canvas(newb);
    	canvas.save();
    	canvas.translate(-mMarginLeft, -mMarginTop);
    	for (IWatermarkResource watermarkRes : mResourceList) {
    		watermarkRes.drawResource(canvas, mWordColor, isSave);
    	}
    	canvas.restore();
        return newb;
    }
    
    private int mSelectIndex;
    
    @Override
    public Object getTouchResource(float x, float y) {
    	for (int i = mResourceList.size() - 1; i > -1; i--) {
    		IWatermarkResource watermarkResource = mResourceList.get(i);
    		Object word = watermarkResource.getResource(x, y);
    		if (word != null) {
    			mSelectIndex = i;
    			return word;
    		}
    	}
    	return null;
    }
    
    @Override
    public void replaceWord(String newWord) {
    	mResourceList.get(mSelectIndex).replaceResource(newWord);
    	computeSize();
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
    }

    @Override
    public void setFocus(boolean focusable) {
        this.mIsFocus = focusable;
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
		/*if (mWordColor == Color.BLACK) {
			mWordColor = Color.WHITE;
		} else {
			mWordColor = Color.BLACK;
		}*/
		int A, R, G, B;
		A = Color.alpha(mWordColor);

		R = 255 - Color.red(mWordColor);
		G = 255 - Color.green(mWordColor);
		B = 255 - Color.blue(mWordColor);
		mWordColor = Color.argb(A, R, G, B);
		mIsInvertColor = !mIsInvertColor;
	}

	@Override
	public boolean isBlackColor() {
		if (mWordColor == Color.BLACK) {
			return true;
		}
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
	public boolean isAddr() {
		if (mSelectIndex >= 0 && mSelectIndex < mResourceList.size()) {
			IWatermarkResource watermarkResource = mResourceList.get(mSelectIndex);
			return watermarkResource.isAddress();
		}
		return false;
	}

	@Override
	public void replaceAddress(String address, double longitude, double latitude) {
		for (int i = 0, size = mResourceList.size(); i < size; i++) {
			IWatermarkResource iWatermarkResource = mResourceList.get(i);
			iWatermarkResource.replaceAddress(address, longitude, latitude);
		}
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mID ^ (mID >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Watermark other = (Watermark) obj;
		if (mID != other.mID)
			return false;
		return true;
	}

	@Override
	public boolean isSticker() {
		return mIsSticker;
	}

	@Override
	public boolean isTextColorChange() {
		return mIsColorChange;
	}

	@Override
	public boolean isInvertColor() {
		return mIsInvertColor;
	}
}
