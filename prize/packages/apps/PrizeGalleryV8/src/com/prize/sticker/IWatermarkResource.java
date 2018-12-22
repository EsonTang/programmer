/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

public interface IWatermarkResource extends Parcelable {
	int TYPE_PHOTO = 1;
	int TYPE_WORD = 2;
	int ALIGN_LEFT = 0;
	int ALIGN_CENTER = 1;
	int ALIGN_RIGHT = 2;
	void drawResource(Canvas canvas, int color, boolean isSave);
	void init(Context context, ISticker sticker);
	void replaceResource(Object object);
	Object getResource(float x, float y);
	RectF getRectF();
	void writeParcelable(Parcel dest, int flags);
	boolean isAddress();
	void replaceAddress(String address, double longitude, double latitude);
}
