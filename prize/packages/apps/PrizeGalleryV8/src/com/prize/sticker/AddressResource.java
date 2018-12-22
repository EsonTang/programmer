/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import com.android.gallery3d.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AddressResource implements IWatermarkResource {
	private float mX;
	private float mY;
	private boolean mIsV;
	private String mWord;
	private int mTextSize;
	private boolean mIsAddrReplace;
	private double mLongitude;
	private double mLatitude;
	
	private ISticker mSticker;
	private RectF mSrcRect = new RectF();
	private Path mWordPath;
	private Paint mWordPaint;
	private int mAlign;
	private float mPx;
	private float mPy;
	private Drawable mRectDrawable;
	private FontMetrics fontMetrics;
	private int mTextWidth;
	private int mTextHeight;
	private int mFontHeight;
	private int mVLineWidth;
	private int mPadding;
	
	public static final Parcelable.Creator<AddressResource> CREATOR = new Creator<AddressResource>() {

		@Override
		public AddressResource createFromParcel(Parcel source) {
			return new AddressResource(source);
		}

		@Override
		public AddressResource[] newArray(int size) {
			return new AddressResource[size];
		}
		
	};
	
	public AddressResource(float mX, float mY, boolean mIsV, int mTextSize,
			String mWord) {
		this(ALIGN_LEFT, mX, mY, mIsV, mTextSize, mWord);
	}
	
	public AddressResource(int align, float mX, float mY, boolean mIsV, int mTextSize,
			String mWord) {
		mAlign = align;
		this.mX = mX;
		this.mY = mY;
		this.mIsV = mIsV;
		this.mTextSize = mTextSize;
		this.mWord = mWord;
	}
	
	public AddressResource(Parcel in) {
		mX = in.readFloat();
		mY = in.readFloat();
		
		mIsV = (in.readInt() == 1);
		mTextSize = in.readInt();
		mWord = in.readString();
		mLongitude = in.readDouble();
		mLatitude = in.readDouble();
		mAlign = in.readInt();
	}
	
	private void initResource(Context context) {
		mWordPath = new Path();  
		mWordPaint = new Paint();
        mWordPaint.setAntiAlias(true);
        mWordPaint.setStyle(Paint.Style.FILL);
        int pxSize = sp2px(context, mTextSize);
        mWordPaint.setTextSize(pxSize);
        mPadding = pxSize / 3;
        mPx = dp2px(context, mX);
        mPy = dp2px(context, mY);
        mRectDrawable = context.getResources().getDrawable(R.drawable.rect_edit_word);
        fontMetrics = mWordPaint.getFontMetrics();
        mFontHeight = (int) (Math.ceil(fontMetrics.bottom - fontMetrics.top));
        float[] widths = new float[1];
        mWordPaint.getTextWidths("正", widths);
		mVLineWidth = (int) Math.ceil(widths[0]);
        getWordRect();
	}
	
	public static int sp2px(Context context, float spValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (spValue * fontScale + 0.5f); 
    } 
	
	public static int dp2px(Context context, float spValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().density; 
        return (int) (spValue * fontScale + 0.5f); 
    } 

	private void getWordRect() {
		Rect rect = new Rect();
		int width = 0, height = 0;
		mWordPaint.getTextBounds(mWord, 0, mWord.length(), rect);
		if (mIsV) { // right->left
			height = mFontHeight * mWord.length();
			width = mVLineWidth; 
			if (mAlign == ALIGN_CENTER) {
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - height / 2 - mPadding, mPx + mVLineWidth + mPadding, mPy + height / 2 + mPadding);
			} else if (mAlign == ALIGN_RIGHT) {
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - height - mPadding, mPx + mVLineWidth + mPadding, mPy + mPadding);
			} else { // left
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - mPadding, mPx + mVLineWidth + mPadding, mPy + height + mPadding);
			}
		} else {
			height = mFontHeight;
			width = rect.width(); 
			if (mAlign == ALIGN_CENTER) {
				mSrcRect.set(mPx - width / 2 - mPadding, mPy - mPadding, mPx + width / 2 + mPadding, mPy + height + mPadding);
			} else if (mAlign == ALIGN_RIGHT) {
				mSrcRect.set(mPx - width - mPadding, mPy - mPadding, mPx + mPadding, mPy + height + mPadding);
			} else { // left
				mSrcRect.set(mPx - mPadding, mPy - mPadding, mPx + width + mPadding, mPy + height + mPadding);
			}
		}
		mTextWidth = width;
		mTextHeight = height;
	}

	@Override
	public void drawResource(Canvas canvas, int color, boolean isSave) {
		
		mWordPaint.setColor(color);
		mWordPaint.setStyle(Paint.Style.FILL);
		if (mIsV) {
			drawVText(canvas);
		} else {
			drawHText(canvas);
		}
//		mWordPaint.setStyle(Paint.Style.STROKE);
		if (mSticker.isFocus() && !isSave) {
			int left = (int) mSrcRect.left;//(mSrcRect.left - fontMetrics.bottom);
			int top = (int) mSrcRect.top;//(int) (mPy + mSrcRect.height() + fontMetrics.top);//(int)Math.floor(mSrcRect.top) - 2;
			int right = (int) mSrcRect.right;//(int) (mSrcRect.right + fontMetrics.bottom + 0.95);
			int bottom = (int) mSrcRect.bottom;//(int) (mPy + mSrcRect.height() + fontMetrics.bottom + 0.95);//(int)Math.ceil(mSrcRect.bottom) + 4;
			mRectDrawable.setBounds(left, top, right, bottom);
			mRectDrawable.draw(canvas);
		}
//		canvas.drawRect(mSrcRect, mWordPaint);
	}
	
	private void drawHText(Canvas canvas) {
		float drawX, baseY;
		if (mAlign == ALIGN_CENTER) {
			mWordPaint.setTextAlign(Align.CENTER);
			drawX = mPx;
		} else if (mAlign == ALIGN_RIGHT) {
			drawX = mPx;
			mWordPaint.setTextAlign(Align.RIGHT);
		} else { // left
			drawX = mPx;
			mWordPaint.setTextAlign(Align.LEFT);
		}
        baseY = mPy + mFontHeight - fontMetrics.bottom;
		canvas.drawText(mWord, drawX, baseY, mWordPaint);
	}
	
	private void drawVText(Canvas canvas) {
		mWordPaint.setTextAlign(Align.LEFT);
		int len = mWord.length();
		float drawY, drawX, baseY;
		if (mAlign == ALIGN_CENTER) {
			drawY = mPy - len * mFontHeight / 2;
		} else if (mAlign == ALIGN_RIGHT) {
			drawY = mPy - len * mFontHeight;
		} else { // left
			drawY = mPy;
		}
		drawX = mPx;
		baseY = drawY;
		for (int j = 0; j < len; j++) {
			char c = mWord.charAt(j);
//			baseY = drawY + mFontHeight * (j + 1) - fontMetrics.bottom;
			if (isChinese(c)) {
				baseY = baseY + mFontHeight;
				canvas.drawText(String.valueOf(c), drawX, baseY - fontMetrics.bottom, mWordPaint);
			} else {
				float[] widths = new float[1];
				mWordPaint.getTextWidths(String.valueOf(c), widths);
				mWordPath.reset();
				mWordPath.moveTo(drawX + fontMetrics.bottom, baseY);
				mWordPath.lineTo(drawX + fontMetrics.bottom, baseY + mVLineWidth);
				canvas.drawTextOnPath(String.valueOf(c), mWordPath, 0, 0,
						mWordPaint);
				baseY = baseY + widths[0];
			}
		}	
	}
	
	private boolean isChinese(char c) {  
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
             || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
            return true;  
        }  
        return false;  
    }

	@Override
	public void init(Context context, ISticker sticker) {
		mSticker = sticker;
		initResource(context);
	}

	@Override
	public void replaceResource(Object word) {
		if (word instanceof String) {
			mWord = (String) word;
			getWordRect();
		}
	}

	@Override
	public Object getResource(float x, float y) {
		if (mWordPaint == null || (mWord == null)) {
			return null;
		}
		RectF dest = new RectF(mSrcRect);
		mSticker.getMatrix().mapRect(dest);
		if (dest.contains(x, y) && mIsAddrReplace) {
			return new PointD(mLongitude, mLatitude);
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(mX);
		dest.writeFloat(mY);
		dest.writeInt(mIsV ? 1 : 0);
		dest.writeInt(mTextSize);
		dest.writeString(mWord);
		dest.writeDouble(mLongitude);
		dest.writeDouble(mLatitude);
		dest.writeInt(mAlign);
	}

	@Override
	public RectF getRectF() {
		return mSrcRect;
	}

	@Override
	public void writeParcelable(Parcel dest, int flags) {
		dest.writeInt(TYPE_WORD);
		dest.writeParcelable(((Parcelable) this), flags);
	}

	@Override
	public boolean isAddress() {
		return true;
	}

	@Override
	public void replaceAddress(String address, double longitude, double latitude) {
		if (!mIsAddrReplace) {
			replaceResource(address);
			mLongitude = longitude;
			mLatitude = latitude;
			mIsAddrReplace = true;
		}
	}

}
