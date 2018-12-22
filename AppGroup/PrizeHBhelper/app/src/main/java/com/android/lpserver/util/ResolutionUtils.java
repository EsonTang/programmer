package com.android.lpserver.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by prize on 2018/2/2.
 */

public class ResolutionUtils {
    private static final String TAG = "ResolutionUtils";

    private static final String PIXEL_2160_1080 = 2160+"*"+1080;
    private static final String PIXEL_1280_720 = 1280+"*"+720;

    public static int[] getResolutionParams(Context context){
        int[] pixelArray = null;
        String resolution = getDisplay(context);
        switch (resolution){
            case PIXEL_1280_720:
                pixelArray = new int[]{350,720,370,740};
                break;
            case PIXEL_2160_1080:
                pixelArray = new int[]{530,1160,550,1180};
                break;
            // please add pixelArray here when added a new type phone
        }

        return pixelArray;
    }

    private static String getDisplay(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        if(wm != null){
            wm.getDefaultDisplay().getRealMetrics(outMetrics);
            int widthPixels = outMetrics.widthPixels;
            int heightPixels = outMetrics.heightPixels;
            Log.d(TAG,"heightPixels * widthPixels ="+(heightPixels+"*"+widthPixels));
            return heightPixels + "*" + widthPixels;
        }
        return null;
    }
}
