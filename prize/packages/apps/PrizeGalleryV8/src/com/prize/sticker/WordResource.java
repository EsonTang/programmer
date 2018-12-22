/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import java.util.ArrayList;

import com.android.gallery3d.R;

import android.R.integer;
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
import android.widget.TextView;

public class WordResource implements IWatermarkResource {
	private float mX;
	private float mY;
	private boolean mIsV;
	private String mWord;
	private int mTextSize;
	
	private ISticker mSticker;
	private RectF mSrcRect = new RectF();
	private Path mWordPath;
	private Paint mWordPaint;
	private int mAlign;
	private float mPx;
	private float mPy;
	private Drawable mRectDrawable;
	private int mLimitLen;
	private FontMetrics fontMetrics;
	private int mTextWidth;
	private int mTextHeight;
	private int mFontHeight;
	private int mVLineWidth;
	private int mLineNum;
	private int mLineSpace;
	private static final String CHANGE_LINE = "\\n";
	private int mPadding;
	private ArrayList<String> mWords = new ArrayList<String>();
	private boolean mIsCanEdit;
	private boolean mIsSingleLine;
	
	public static final Parcelable.Creator<WordResource> CREATOR = new Creator<WordResource>() {

		@Override
		public WordResource createFromParcel(Parcel source) {
			return new WordResource(source);
		}

		@Override
		public WordResource[] newArray(int size) {
			return new WordResource[size];
		}
		
	};
	
	public WordResource(float mX, float mY, boolean mIsV, int mTextSize,
			String mWord) {
		this(ALIGN_LEFT, mX, mY, mIsV, mTextSize, mWord);
	}
	
	public WordResource(int align, float mX, float mY, boolean mIsV, int mTextSize,
			String mWord) {
		this(align, mX, mY, mIsV, mTextSize, mWord, 10);
	}
	
	public WordResource(int align, float mX, float mY, boolean mIsV, int mTextSize,
			String mWord, int limitLen) {
		this(align, mX, mY, mIsV, mTextSize, mWord, limitLen, true, true);
	}
	
	public WordResource(int align, float mX, float mY, boolean mIsV, int mTextSize,
			String mWord, int limitLen, boolean isCanEdit, boolean isSingleLine) {
		mAlign = align;
		this.mX = mX;
		this.mY = mY;
		this.mIsV = mIsV;
		this.mTextSize = mTextSize;
		this.mWord = mWord;
		mLimitLen = limitLen;
		mIsCanEdit = isCanEdit;
		mIsSingleLine = isSingleLine;
	}
	
	public WordResource(Parcel in) {
		mX = in.readFloat();
		mY = in.readFloat();
		
		mIsV = (in.readInt() == 1);
		mTextSize = in.readInt();
		mWord = in.readString();
		mAlign = in.readInt();
		mLimitLen = in.readInt();
		mIsCanEdit = (in.readInt() == 1);
		mIsSingleLine = (in.readInt() == 1);
	}
	
	private void initResource(Context context) {
		mWordPath = new Path();  
		mWordPaint = new Paint();
        mWordPaint.setAntiAlias(true);
        mWordPaint.setStyle(Paint.Style.FILL);
        int pxSize = sp2px(context, mTextSize);
        mWordPaint.setTextSize(pxSize);
        mLineSpace = pxSize / 5;
        mPadding = pxSize / 3;
        mPx = dp2px(context, mX);
        mPy = dp2px(context, mY);
        mRectDrawable = context.getResources().getDrawable(R.drawable.rect_edit_word);
        fontMetrics = mWordPaint.getFontMetrics();
        mFontHeight = (int) (Math.ceil(fontMetrics.bottom - fontMetrics.top));
        float[] widths = new float[1];
        mWordPaint.getTextWidths("正", widths);//获取单个汉字的宽度
		mVLineWidth = (int) Math.ceil(widths[0]);
        getWordRect();
	}
	
	public String getWord() {
		return mWord;
	}

	public int getLimitLen() {
		return mLimitLen;
	}

	public boolean isSingleLine() {
		return mIsSingleLine;
	}

	private int getBaseY(int height) {
		float fontHeight = fontMetrics.bottom - fontMetrics.top;
		int baseY =  (int) (1/2 * height + 1/2 * fontHeight - fontMetrics.bottom);
		return baseY;
	}
	
	private int getBottomY(int height) {
		float fontHeight = fontMetrics.bottom - fontMetrics.top;
		float baseY =  (int) (1/2 * height + 1/2 * fontHeight - fontMetrics.bottom);
		int bottomY = (int) (baseY + fontMetrics.bottom);
		return bottomY;
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
		String[] slipWords = mWord.split(CHANGE_LINE);
		mLineNum = slipWords.length;
		mWords.clear();
		int width = 0, height = 0;
		for (int i = 0; i < mLineNum; i++) {
			String slipWord = slipWords[i];
			mWords.add(slipWord);
			if (mIsV) {
				height = Math.max(height, mFontHeight * slipWord.length());
			} else {
				mWordPaint.getTextBounds(slipWord, 0, slipWord.length(), rect);
				width = Math.max(width, rect.width());
			}
		}
		if (mIsV) { // right->left
			width = (mVLineWidth + mLineSpace) * mLineNum - mLineSpace; 
			if (mAlign == ALIGN_CENTER) {
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - height / 2 - mPadding, mPx + mVLineWidth + mPadding, mPy + height / 2 + mPadding);
			} else if (mAlign == ALIGN_RIGHT) {
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - height - mPadding, mPx + mVLineWidth + mPadding, mPy + mPadding);
			} else { // left
				mSrcRect.set(mPx - width + mVLineWidth - mPadding, mPy - mPadding, mPx + mVLineWidth + mPadding, mPy + height + mPadding);
			}
		} else {
			height = (mFontHeight + mLineSpace) * mLineNum - mLineSpace; 
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
		/*mWordPaint.getTextBounds(mWord, 0, mWord.length(), rect);
		RectF dest;
		if (mIsV) {
 			if (mAlign == ALIGN_CENTER) {
				dest = new RectF(mPx, mPy - rect.width() / 2, mPx + rect.height(), mPy + rect.width() / 2);
			} else if (mAlign == ALIGN_RIGHT) {
				dest = new RectF(mPx, mPy - rect.width(), mPx + rect.height(), mPy);
			} else { // left
				dest = new RectF(mPx, mPy, mPx + rect.height(), mPy + rect.width());
			}
		} else {
			if (mAlign == ALIGN_CENTER) {
				dest = new RectF(mPx - rect.width() / 2, mPy, mPx + rect.width() / 2, mPy + rect.height());
			} else if (mAlign == ALIGN_RIGHT) {
				dest = new RectF(mPx - rect.width(), mPy, mPx, mPy + rect.height());
			} else { // left
				dest = new RectF(mPx, mPy, mPx + rect.width(), mPy + rect.height());
			}
		}
		mSrcRect.set(dest);*/
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
		if (mSticker.isFocus() && !isSave && mIsCanEdit) {
			int left = (int) mSrcRect.left;//(mSrcRect.left - fontMetrics.bottom);
			int top = (int) mSrcRect.top;//(int) (mPy + mSrcRect.height() + fontMetrics.top);//(int)Math.floor(mSrcRect.top) - 2;
			int right = (int) mSrcRect.right;//(int) (mSrcRect.right + fontMetrics.bottom + 0.95);
			int bottom = (int) mSrcRect.bottom;//(int) (mPy + mSrcRect.height() + fontMetrics.bottom + 0.95);//(int)Math.ceil(mSrcRect.bottom) + 4;
			mRectDrawable.setBounds(left, top, right, bottom);
			mRectDrawable.draw(canvas);
		}
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
		for (int i = 0, size = mWords.size(); i < size; i++) {
			String word = mWords.get(i);
			baseY = mPy + mFontHeight * (i + 1) - fontMetrics.bottom;
			canvas.drawText(word, drawX, baseY, mWordPaint);
		}
	}
	
	private void drawVText(Canvas canvas) {
		/*float w;
		final int len = mWord.length();
		float py = 0 + mPy;
		if (mAlign == ALIGN_CENTER) {
			py = mPy - mSrcRect.height() / 2;
		} else if (mAlign == ALIGN_RIGHT) {
			py = mPy - mSrcRect.height();
		} else { // left
			py = mPy;
		}
		for (int i = 0; i < len; i++) {
			char c = mWord.charAt(i);
			w = mWordPaint.measureText(mWord, i, i + 1);
			if (isChinese(c)) {
				py += w;
				canvas.drawText(String.valueOf(c), mPx, py, mWordPaint);
			} else {
				mWordPath.reset();
				mWordPath.moveTo(mPx + 5, py);
				mWordPath.lineTo(mPx + 5, py + w);
				canvas.drawTextOnPath(String.valueOf(c), mWordPath, 0, 0,
						mWordPaint);
				py += w;
			}
		}*/
		mWordPaint.setTextAlign(Align.LEFT);
		for (int i = 0, size = mWords.size(); i < size; i++) {
			String word = mWords.get(i);
			int len = word.length();
			float drawY, drawX, baseY;
			if (mAlign == ALIGN_CENTER) {
				drawY = mPy - len * mFontHeight / 2;
			} else if (mAlign == ALIGN_RIGHT) {
				drawY = mPy - len * mFontHeight;
			} else { // left
				drawY = mPy;
			}
			drawX = mPx - mVLineWidth * i - i * mLineSpace;
			baseY = drawY;
			for (int j = 0; j < len; j++) {
				char c = word.charAt(j);
//				baseY = drawY + mFontHeight * (j + 1) - fontMetrics.bottom;
				if (isChinese(c)) {
					baseY = baseY + mFontHeight;
					canvas.drawText(String.valueOf(c), drawX, baseY - fontMetrics.bottom, mWordPaint);
				} else {
					float[] widths = new float[1];
					mWordPaint.getTextWidths(String.valueOf(c), widths);//获取单个汉字的宽度
					mWordPath.reset();
					mWordPath.moveTo(drawX + fontMetrics.bottom, baseY);
					mWordPath.lineTo(drawX + fontMetrics.bottom, baseY + mVLineWidth);
					canvas.drawTextOnPath(String.valueOf(c), mWordPath, 0, 0,
							mWordPaint);
					baseY = baseY + widths[0];
				}
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
			String newWord = (String) word;
			if (!newWord.equals(mWord)) {
				mWord = newWord;
				getWordRect();
			}
		}
	}

	@Override
	public Object getResource(float x, float y) {
		if (mWordPaint == null || (mWord == null) || !mIsCanEdit) {
			return null;
		}
		RectF dest = new RectF(mSrcRect);
		mSticker.getMatrix().mapRect(dest);
		if (dest.contains(x, y)) {
			return this;
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
		dest.writeInt(mAlign);
		dest.writeInt(mLimitLen);
		dest.writeInt(mIsCanEdit ? 1 : 0);
		dest.writeInt(mIsSingleLine ? 1 : 0);
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
		return false;
	}

	@Override
	public void replaceAddress(String address, double longitude, double latitude) {
	}

}
