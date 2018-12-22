/***
 * function:watermark
 * author: wanzhijuan
 * date:   2016-1-21
 */
package com.prize.sticker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;

/**
 * Created by yangchen on 15-9-10.
 */
public class Utils {

    private static Point point = null;

    public static Point getDisplayWidthPixels(Context context) {
        if (point != null) {
            return point;
        }
        WindowManager wm = ((Activity)context).getWindowManager();
        point = new Point();
        wm.getDefaultDisplay().getSize(point);
        return point;
    }

    /**
     * 计算两点之间的距离
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double lineSpace(double x1, double y1, double x2, double y2) {
        double lineLength = 0;
        double x, y;
        x = x1 - x2;
        y = y1 - y2;
        lineLength = Math.sqrt(x * x + y * y);
        return lineLength;
    }

    /**
     * 获取线段中点坐标
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static PointD getMidpointCoordinate(double x1, double y1, double x2, double y2) {
        PointD midpoint = new PointD();
        midpoint.set((x1 + x2) / 2, (y1 + y2) / 2);
        return midpoint;
    }
}
