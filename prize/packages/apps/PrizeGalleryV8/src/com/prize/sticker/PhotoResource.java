/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

public class PhotoResource implements IWatermarkResource {

	private float mX;
	private float mY;
	private String mPath;
	private int mResId;
	private Bitmap mBitmap;
	private Bitmap mInvertBitmap;
	
	private ISticker mSticker;
	private RectF mSrcRect = new RectF();
	private boolean mIsCanInvert;
	
	public static final Parcelable.Creator<PhotoResource> CREATOR = new Creator<PhotoResource>() {

		@Override
		public PhotoResource createFromParcel(Parcel source) {
			return new PhotoResource(source);
		}

		@Override
		public PhotoResource[] newArray(int size) {
			return new PhotoResource[size];
		}
		
	};
	
	public PhotoResource(float mX, float mY, String mPath) {
		this.mX = mX;
		this.mY = mY;
		this.mPath = mPath;
	}
	
	public PhotoResource(float mX, float mY, int mResId) {
		this.mX = mX;
		this.mY = mY;
		this.mResId = mResId;
	}
	
	private void initResource(Context context) {
		if (mResId > 0) {
			mBitmap = BitmapFactory.decodeResource(context.getResources(), mResId);
			mSrcRect.set(mX, mY, mX + mBitmap.getWidth(), mY + mBitmap.getHeight());
		}
	}
	
	public PhotoResource(Parcel in) {
		mX = in.readFloat();
		mY = in.readFloat();
		
		boolean hasPath = (in.readInt() == 1);
		if (hasPath) {
			mPath = in.readString();
		} else {
			mResId = in.readInt();
		}
	}

	@Override
	public void drawResource(Canvas canvas, int color, boolean isSave) {
		Bitmap draw = mBitmap;
		if (mSticker.isInvertColor() && mIsCanInvert) {
			if (mInvertBitmap == null) {
				mInvertBitmap = invert(mBitmap);
			}
			draw = mInvertBitmap;
		}
		canvas.drawBitmap(draw, mX, mY, null); 
	}

	@Override
	public void init(Context context, ISticker sticker) {
		mSticker = sticker;
		initResource(context);
	}

	@Override
	public void replaceResource(Object object) {
		
	}

	@Override
	public Object getResource(float x, float y) {
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
		
		if (mPath != null) {
			dest.writeInt(1);
			dest.writeString(mPath);
		} else {
			dest.writeInt(0);
			dest.writeInt(mResId);
		}
	}

	@Override
	public RectF getRectF() {
		return mSrcRect;
	}
	
	@Override
	public void writeParcelable(Parcel dest, int flags) {
		dest.writeInt(TYPE_PHOTO);
		dest.writeParcelable(((Parcelable) this), flags);
	}

	@Override
	public boolean isAddress() {
		return false;
	}

	@Override
	public void replaceAddress(String address, double longitude, double latitude) {
	}
	
	public static Bitmap invert(Bitmap src) {
		Bitmap output = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
				src.getConfig());
		int A, R, G, B;
		int pixelColor;
		int height = src.getHeight();
		int width = src.getWidth();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixelColor = src.getPixel(x, y);
				A = Color.alpha(pixelColor);

				R = 255 - Color.red(pixelColor);
				G = 255 - Color.green(pixelColor);
				B = 255 - Color.blue(pixelColor);

				output.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return output;
	}
}
