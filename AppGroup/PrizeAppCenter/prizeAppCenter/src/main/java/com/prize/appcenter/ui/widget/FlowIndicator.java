//package com.prize.appcenter.ui.widget;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Paint;
//import android.graphics.RectF;
//import android.util.AttributeSet;
//import android.view.View;
//
//import com.prize.app.util.JLog;
//import com.prize.appcenter.R;
//
///**
// * 首页轮播图指示器
// */
//public class FlowIndicator extends View {
//    private int count;
//    private float space, radius, relWidth, relHeight;
//    private int point_normal_color, point_seleted_color;
//
//    // 选中
//    private int seleted = 0;
//    public static boolean DEBUG = false;
//
//    // background seleted normal
//
//    public FlowIndicator(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        TypedArray a = context.obtainStyledAttributes(attrs,
//                R.styleable.FlowIndicator);
//
//        count = a.getInteger(R.styleable.FlowIndicator_count, 4);
//        space = a.getDimension(R.styleable.FlowIndicator_space, 9);
//        relWidth = a.getDimension(R.styleable.FlowIndicator_point_width, 20);
//        relHeight = a.getDimension(R.styleable.FlowIndicator_point_height, 4);
//        radius = a.getDimension(R.styleable.FlowIndicator_point_radius, 2);
//
//        point_normal_color = a.getColor(
//                R.styleable.FlowIndicator_point_normal_color, 0x33ffffff);
//        point_seleted_color = a.getColor(
//                R.styleable.FlowIndicator_point_seleted_color, 0xe6691e);
//        int sum = attrs.getAttributeCount();
//        if (DEBUG) {
//            String str = "";
//            for (int i = 0; i < sum; i++) {
//                String name = attrs.getAttributeName(i);
//                String value = attrs.getAttributeValue(i);
//                str += "attr_name :" + name + ": " + value + "\n";
//            }
//            JLog.i("attribute", str);
//        }
//        a.recycle();
//    }
//
//    public void setSeletion(int index) {
//        this.seleted = index;
//        invalidate();
//    }
//
//    public void setCount(int count) {
//        this.count = count;
//        invalidate();
//    }
//
//    public void next() {
//        if (seleted < count - 1)
//            seleted++;
//        else
//            seleted = 0;
//        invalidate();
//    }
//
//    public void previous() {
//        if (seleted > 0)
//            seleted--;
//        else
//            seleted = count - 1;
//        invalidate();
//    }
//
//    private RectF mBackgroundBounds;
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        mBackgroundBounds = new RectF();
//        if (radius == 0) {
//            radius = getMeasuredHeight() / 2;
//        }
//        mBackgroundBounds.top = 0;
//        mBackgroundBounds.bottom = relHeight;
//        float width = (getWidth() - ((relWidth * count) + (space * (count - 1)))) / 2.f;
//        for (int i = 0; i < count; i++) {
//            mBackgroundBounds.left = width + ((space + relWidth) * i);
//            mBackgroundBounds.right = width + relWidth + ((space + relWidth) * i);
//
//
//            if (i == seleted)
//                paint.setColor(point_seleted_color);
//            else
//                paint.setColor(point_normal_color);
//
////            canvas.drawCircle(width + getPaddingLeft() + radius + i
////                    * (space + radius + radius), getHeight() / 2, radius, paint);
//            canvas.drawRoundRect(mBackgroundBounds, radius, radius, paint);
//
//        }
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(measureWidth(widthMeasureSpec),
//                measureHeight(heightMeasureSpec));
//    }
//
//    private int measureWidth(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            result = specSize;
//        } else {
//            result = (int) (getPaddingLeft() + getPaddingRight()
//                    + (count * 2 * radius) + (count - 1) * radius + 1);
//            if (specMode == MeasureSpec.AT_MOST) {
//                result = Math.min(result, specSize);
//            }
//        }
//        return result;
//    }
//
//    private int measureHeight(int measureSpec) {
//        int result = 0;
//        int specMode = MeasureSpec.getMode(measureSpec);
//        int specSize = MeasureSpec.getSize(measureSpec);
//
//        if (specMode == MeasureSpec.EXACTLY) {
//            result = specSize;
//        } else {
//            result = (int) (2 * radius + getPaddingTop() + getPaddingBottom() + 1);
//            if (specMode == MeasureSpec.AT_MOST) {
//                result = Math.min(result, specSize);
//            }
//        }
//        return result;
//    }
//
//}

package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.prize.app.util.JLog;
import com.prize.appcenter.R;

public class FlowIndicator extends View {
    private int count;
    private float space, radius;
    private int point_normal_color, point_seleted_color;

    // 选中
    private int seleted = 0;
    public static boolean DEBUG = false;

    // background seleted normal

    public FlowIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlowIndicator);

        count = a.getInteger(R.styleable.FlowIndicator_count, 4);
        space = a.getDimension(R.styleable.FlowIndicator_space, 9);
        radius = a.getDimension(R.styleable.FlowIndicator_point_radius, 9);

        point_normal_color = a.getColor(
                R.styleable.FlowIndicator_point_normal_color, 0x33ffffff);
        point_seleted_color = a.getColor(
                R.styleable.FlowIndicator_point_seleted_color, 0xe6691e);
        int sum = attrs.getAttributeCount();
        if (DEBUG) {
            String str = "";
            for (int i = 0; i < sum; i++) {
                String name = attrs.getAttributeName(i);
                String value = attrs.getAttributeValue(i);
                str += "attr_name :" + name + ": " + value + "\n";
            }
            JLog.i("attribute", str);
        }
        a.recycle();
    }

    public void setSeletion(int index) {
        this.seleted = index;
        invalidate();
    }

    public void setCount(int count) {
        this.count = count;
        invalidate();
    }

    public void next() {
        if (seleted < count - 1)
            seleted++;
        else
            seleted = 0;
        invalidate();
    }

    public void previous() {
        if (seleted > 0)
            seleted--;
        else
            seleted = count - 1;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        float width = (getWidth() - ((radius * 2 * count) + (space * (count - 1)))) / 2.f;

        for (int i = 0; i < count; i++) {
            if (i == seleted)
                paint.setColor(point_seleted_color);
            else
                paint.setColor(point_normal_color);
            canvas.drawCircle(width + getPaddingLeft() + radius + i
                    * (space + radius + radius), getHeight() / 2, radius, paint);

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (getPaddingLeft() + getPaddingRight()
                    + (count * 2 * radius) + (count - 1) * radius + 1);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = (int) (2 * radius + getPaddingTop() + getPaddingBottom() + 1);
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

}

