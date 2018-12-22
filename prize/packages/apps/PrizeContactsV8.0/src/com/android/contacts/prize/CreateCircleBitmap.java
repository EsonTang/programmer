/**
 * CreateCircleBitmap.java [V 1.0.0]
 * classes : com.android.contacts.prize.CreateCircleBitmap
 * huangliemin Create at 2016-7-18 6:00:01
 */
package com.android.contacts.prize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;

/**
 * com.android.contacts.prize.CreateCircleBitmap
 * @author huangliemin <br/>
 * create at 2016-7-18 6:00:01
 */
public class CreateCircleBitmap {

	public Bitmap getCircleBitmap(Bitmap bitmap, int cirration) {
   		int bw = bitmap.getWidth();
   		
   		int bh = bitmap.getHeight();
   		
   	//	int cirRation = bw < bh ? bw : bh;
   		
   		int cirRation = cirration;
   		Bitmap output = Bitmap.createBitmap(cirRation,cirRation, Config.ARGB_8888);
   		
   		Canvas canvas = new Canvas(output);
   		
   	    int color = 0xff424242;
   		
   	    Paint paint = new Paint();
   		
   	    Rect des = new Rect(0, 0, cirRation, cirRation);
   	    
   	    Rect src = null;
   	    
   		if(bw < bh){
   			src = new Rect(0, (bh-bw)/2, bw, (bh+bw)/2);
   		}else if(bitmap.getWidth() >= bitmap.getHeight()){
   			src = new Rect((bw-bh)/2, 0, (bw+bh)/2, bh);
   		}
   		
   		
   		paint.setAntiAlias(true);
   		
   		canvas.drawARGB(0, 0, 0, 0);
   		
   		paint.setColor(color);
   		
   		canvas.drawCircle(cirRation /2, cirRation / 2, cirRation / 2, paint);
   		
   		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
   		
   		canvas.drawBitmap(bitmap, src, des, paint);
   		
   		/**Prize-Add white circle to the thumb image-20160419-start*/
   		paint.setColor(Color.WHITE);
   		paint.setStrokeWidth(2.0f);
   		paint.setDither(true);
   		paint.setStyle(Style.STROKE);
   		paint.setXfermode(null);
   		canvas.drawCircle(cirRation /2, cirRation / 2, (cirRation / 2 - 1.0f), paint);
   		/**Prize-Add white circle to the thumb image-20160419-end*/
   		return output;
   	}
}
