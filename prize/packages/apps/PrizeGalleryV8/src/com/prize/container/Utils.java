package com.prize.container;

import android.graphics.Bitmap;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;

public final class Utils {

    public static int getMeasuredWidth(View v) {
        return (v == null) ? 0 : v.getMeasuredWidth();
    }

    public static int getWidth(View v) {
        return (v == null) ? 0 : v.getWidth();
    }

    public static int getWidthWithMargin(View v) {
        return getWidth(v) + getMarginHorizontally(v);
    }

    public static int getStart(View v) {
        return getStart(v, false);
    }

    public static int getStart(View v, boolean withoutPadding) {
        if (v == null) {
            return 0;
        }
        return (withoutPadding) ? v.getLeft() + getPaddingStart(v) : v.getLeft();
    }

    public static int getEnd(View v) {
        return getEnd(v, false);
    }

    public static int getEnd(View v, boolean withoutPadding) {
        if (v == null) {
            return 0;
        }
        if (isLayoutRtl(v)) {
            return (withoutPadding) ? v.getLeft() + getPaddingEnd(v) : v.getLeft();
        } else {
            return (withoutPadding) ? v.getRight() - getPaddingEnd(v) : v.getRight();
        }
    }

    public static int getPaddingStart(View v) {
        if (v == null) {
            return 0;
        }
        return ViewCompat.getPaddingStart(v);
    }

    public static int getPaddingEnd(View v) {
        if (v == null) {
            return 0;
        }
        return ViewCompat.getPaddingEnd(v);
    }

    public static int getPaddingHorizontally(View v) {
        if (v == null) {
            return 0;
        }
        return v.getPaddingLeft() + v.getPaddingRight();
    }

    public static int getMarginStart(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(lp);
    }

    public static int getMarginEnd(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginEnd(lp);
    }

    public static int getMarginHorizontally(View v) {
        if (v == null) {
            return 0;
        }
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        return MarginLayoutParamsCompat.getMarginStart(lp) + MarginLayoutParamsCompat.getMarginEnd(lp);
    }

    public static boolean isLayoutRtl(View v) {
        return ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

    public static Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength) {
        if (null == bitmap || edgeLength <= 0) {
            return null;
        }

        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();

        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            int longerEdge = (int) (edgeLength * Math.max(widthOrg, heightOrg) / Math.min(widthOrg, heightOrg));
            int scaledWidth = widthOrg > heightOrg ? longerEdge : edgeLength;
            int scaledHeight = widthOrg > heightOrg ? edgeLength : longerEdge;
            Bitmap scaledBitmap;

            try {
                scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
            } catch (Exception e) {
                return null;
            }

            int xTopLeft = (scaledWidth - edgeLength) / 2;
            int yTopLeft = (scaledHeight - edgeLength) / 2;

            try {
                result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength);
                scaledBitmap.recycle();
            } catch (Exception e) {
                return null;
            }
        }

        return result;
    }

    private Utils() { }

}
