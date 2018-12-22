
/*******************************************
 *版权所有©2015,深圳市铂睿智恒科技有限公司
 *
 *内容摘要：图片处理工具
 *当前版本：V1.0
 *作	者：朱道鹏
 *完成日期：2015-04-17
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
package com.android.notepad.note.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 **
 * 类描述：图片处理工具类
 * @author 朱道鹏
 * @version V1.0
 */
public class BitmapTools {

	/**
	 * 方法描述：裁剪位图
	 * @param Bitmap  float  float
	 * @return Bitmap
	 * @see BitmapTools#getScaleBitmap
	 */
	public static Bitmap getScaleBitmap(Bitmap bitmap, int imageObjWidth){

		if(bitmap == null){
			return null;
		}
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float ratio = 0;

		ratio = (float)imageObjWidth/width;

		Matrix matrix = new Matrix();
		matrix.postScale(ratio, ratio);
		Bitmap resizeBmp  = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,true);
		return resizeBmp;
	}
}
