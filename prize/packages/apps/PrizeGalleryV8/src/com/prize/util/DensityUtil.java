
 /*******************************************
 *author：wanzhijuan
 *complete data：2015-3-30
*********************************************/

package com.prize.util;

import android.content.Context;
/**
 * 
 **
 * Class description: conversion between PX and dip
 * @author author
 * @version version
 */
public class DensityUtil {
    /**
     * According to the resolution of the cell phone from PX (pixels) to the unit to become DP
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * According to the resolution of the cell phone from PX (pixels) to the unit to become DP
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}

