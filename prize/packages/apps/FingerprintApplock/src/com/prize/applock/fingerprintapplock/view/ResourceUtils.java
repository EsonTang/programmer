package com.prize.applock.fingerprintapplock.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

/**
 * @author wangzhong
 */
public class ResourceUtils {

    private ResourceUtils() {
        throw new AssertionError();
    }

    public static int getColor(Context context, @ColorRes int id) {
        return ContextCompat.getColor(context, id);
    }

    public static float getDimensionInPx(Context context, @DimenRes int id) {
        return context.getResources().getDimension(id);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id) {
        return ContextCompat.getDrawable(context, id);
    }
}
