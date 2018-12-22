package com.android.soundrecorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
/***
 * Tool class 
 * @author fanjunchen
 *
 */
public class PrizeUtils {

	/***
	 * Get the height and width of the image resource 
	 * @param ctx
	 * @param resId
	 * @return int[0]: width; int[1]:height;
	 */
	public static int[] getImgWidthHeight(Context ctx, int resId) {
		int[] rs = null; 
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();  
	        opts.inJustDecodeBounds = true;  
	        opts.inSampleSize = 1;
	        Bitmap mBitmap = BitmapFactory.decodeResource(ctx.getResources(), resId, opts);  
	        int width = opts.outWidth;  
	        int height = opts.outHeight;
	        rs = new int[2];
	        rs[0] = width;
	        rs[1] = height;
	        opts = null;
	        if (mBitmap != null)
	        	mBitmap.recycle();
	        mBitmap = null;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
}
