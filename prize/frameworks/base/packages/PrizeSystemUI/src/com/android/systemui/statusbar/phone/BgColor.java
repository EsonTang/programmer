package com.android.systemui.statusbar.phone;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class BgColor {
	public static int bindTextColor(Bitmap keyguardBitmap) {
		Log.d("luyy", "lukey2");
		Bitmap srcDst = keyguardBitmap;//锁屏壁纸图
		int left = srcDst.getWidth() / 12;//X起点
		int top = srcDst.getHeight() / 12;//Y起点
		int right = srcDst.getWidth() - left;
		int bottom = srcDst.getHeight() - top;
		int lenth = (right - left) * (bottom - top);
		int[][] srcBuffer = new int[srcDst.getWidth()][srcDst.getHeight()];//容器
		long rc = 0;
		long gc = 0;
		long bc = 0;
		int m = 5;
		for (int x = left; x < right; x += m) {
			for (int y = top; y < bottom; y += m) {
				int color = srcDst.getPixel(x, y);
				srcBuffer[x][y] = color;
				int r = Color.red(color);
				rc += r;
				int g = Color.green(color);
				gc += g;
				int b = Color.blue(color);
				bc += b;
			}
		}
		int l = lenth / m / m;
		float rP = rc / (float) l;
		float gP = gc / (float) l;
		float bP = bc / (float) l;
		Color c = new Color();
		float f = (rP + gP + bP) / 3;
		int s = (int) ((rP + gP + bP) / 3f);
		Log.i("zhouerlong", "RGB:::::rP " + rP + "," + gP + " ," + bP + "  f:"
				+ f);
		int m1 = 255;

		if (f >= 190) {
			if (f < 220) {
				f = 220;
			}
			m1 = 0;//c.rgb(255 - (int) f, 255 - (int) f, 255 - (int) f);
		} else {
			m1 = 1;//c.rgb(255, 255, 255);
		}
		if (m1 >= 255) {
			m1 = 1;
		}
		int  textColor = m1;//去反色之后的值，0--用黑色字，1---用白色字
		int bgColor = s;//背景平均值
        srcBuffer = null;
		return textColor;

	}
}
