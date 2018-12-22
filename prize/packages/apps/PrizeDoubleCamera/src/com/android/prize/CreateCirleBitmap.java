
 /*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：把矩形图片裁剪为圆形图片
 *当前版本：V1.0
 *作	者：徐春明
 *完成日期：2015-4-9
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
 ...
 *修改记录：
 *修改日期：
 *版 本 号：
 *修 改 人：
 *修改内容：
*********************************************/

package com.android.prize;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

public class CreateCirleBitmap {

	
	
	 /**
	 * 方法描述：把矩形图片裁剪为圆形图片
	 * @param bitmap 原始矩形图片
	 * @return Bitmap 圆形图片
	 * @see 类名/完整类名/完整类名#方法名
	 */
	public static Bitmap getCircleBitmap(Bitmap bitmap, int cirration) {
		
		/** 比较矩形图片的宽高，得到最小的长度作为圆形的直径 */
		int bw = bitmap.getWidth();
		
		int bh = bitmap.getHeight();
		
	//	int cirRation = bw < bh ? bw : bh;
		
		int cirRation = cirration;
		/** 创建正方形图片，内容为填充颜色 */
		Bitmap output = Bitmap.createBitmap(cirRation,cirRation, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(output);
		
	    int color = 0xff424242;
		
	    Paint paint = new Paint();
		
	    Rect des = new Rect(0, 0, cirRation, cirRation);
	    
	    Rect src = null;
	    
	    /** 选择原矩形图片中间以cirRation的正方形区域 */
		if(bw < bh){
			src = new Rect(0, (bh-bw)/2, bw, (bh+bw)/2);
		}else if(bitmap.getWidth() >= bitmap.getHeight()){
			src = new Rect((bw-bh)/2, 0, (bw+bh)/2, bh);
		}
		
		
		/** 避免据此*/
		paint.setAntiAlias(true);
		
		canvas.drawARGB(0, 0, 0, 0);
		
		paint.setColor(color);
		
		/** 画半径为cirRation/2的圆*/
		canvas.drawCircle(cirRation /2, cirRation / 2, cirRation / 2, paint);
		
		/** 设置两张图片叠加类型，这是重点：Mode.SRC_IN类型是选择两张图片叠加区域并且该区域一定输入后景图片区域*/
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		
		/** 画原始图片*/
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

