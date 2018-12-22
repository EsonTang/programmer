package com.prize.appcenter.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;

import com.prize.appcenter.R;

/**
 * ScrollView中使用GridView,使用此widget
 *
 * @author 聂礼刚
 */
public class ScrollLineGridView extends GridView {
    private int lineStyle;
    private int color;

    public ScrollLineGridView(Context context) {
        super(context);
    }

    public ScrollLineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs,
                R.styleable.ScrollLineGridView);
        lineStyle = ta.getInt(R.styleable.ScrollLineGridView_lineStyle, 0);
        color = ta.getColor(R.styleable.ScrollLineGridView_lineColor, Color.BLACK);
        ta.recycle();
    }

    public ScrollLineGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        return ev.getAction() == MotionEvent.ACTION_MOVE || super.dispatchTouchEvent(ev);

//	    if (ev.getAction() == MotionEvent.ACTION_MOVE) {
//	        return true;
//	    }
//	    return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        View localView1 = getChildAt(0);
        if (localView1 == null)
            return;

        if (lineStyle == 0) {
            drawFullLine(canvas, localView1);
        }
        if (lineStyle == 1) {
            drawCrossStyleLine(canvas, localView1);
        } else if (lineStyle == 2) {
            drawVeticalDividerLine(canvas, localView1);
        }
        if (lineStyle == 3) {
            drawMallStyleLine(canvas, localView1);
        }
    }

    private void drawMallStyleLine(Canvas canvas, View view) {
        int column = getNumColumns();//计算出一共有多少列，假设有3列
        int childCount = getChildCount();//子view的总数

        Paint localPaint;//画笔
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(color);//设置画笔的颜色

        if (childCount == 1) {
            canvas.drawLine(view.getRight(), view.getTop(), view.getRight(), view.getBottom(), localPaint); //画最后一个右边
            return;
        }

        for (int i = 0; i < childCount; i++) {//遍历子view
            View cellView = getChildAt(i);//获取子view
            if (i % column != 0) { //如果不是第一列，画左边
                canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
            }
            if (childCount - i > (childCount % column == 0 ? column : childCount % column)) { //如果不是最后一行，就画下边
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            }

            if (childCount > column && childCount % column != 0) { //如果多于一行，且最后一行没有填满
                if (i == childCount - 1) {
                    canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint); //画最后一个右边
                }
            }
        }
    }

    private void drawCrossStyleLine(Canvas canvas, View view) {
        int column = getNumColumns();//计算出一共有多少列，假设有3列
        int childCount = getChildCount();//子view的总数

        Paint localPaint;//画笔
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(color);//设置画笔的颜色

        if (childCount == 1) {
            return;
        }

        for (int i = 0; i < childCount; i++) {//遍历子view
            View cellView = getChildAt(i);//获取子view
            if (i % column != 0) { //如果不是第一列，画左边
                canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
            }

            if (childCount - i > (childCount % column == 0 ? column : childCount % column)) { //如果不是最后一行，就画下边
                canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
            }

            if (childCount > column && childCount % column != 0) { //如果多于一行，且最后一行没有填满
                if (i == childCount - 1) {
                    canvas.drawLine(cellView.getRight(), cellView.getTop(), cellView.getRight(), cellView.getBottom(), localPaint); //画最后一个右边
                }
            }
        }
    }

    private void drawVeticalDividerLine(Canvas canvas, View view) {
        int column = getNumColumns();//列数
        int childCount = getChildCount();//子view的总数

        Paint localPaint;//画笔
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(color);//设置画笔的颜色

        for (int i = 0; i < childCount; i++) {//遍历子view
            View cellView = getChildAt(i);//获取子view
            if (i % column != 0) { //如果=是第一列，画做边
                canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
            }
        }
    }

    private void drawFullLine(Canvas canvas, View view) {
        int column = getNumColumns();//计算出一共有多少列，假设有3列
        int childCount = getChildCount();//子view的总数

        Paint localPaint;//画笔
        localPaint = new Paint();
        localPaint.setStyle(Paint.Style.STROKE);
        localPaint.setColor(color);//设置画笔的颜色

        if (childCount == 1) {
            canvas.drawLine(getLeft(), getTop(), getLeft(), getBottom(), localPaint);
            canvas.drawLine(getLeft(), getTop(), getRight(), getTop(), localPaint);
            canvas.drawLine(getRight() - 1, getTop(), getRight() - 1, getBottom(), localPaint);
            canvas.drawLine(getLeft(), getBottom() - 1, getRight(), getBottom() - 1, localPaint);
            return;
        }

        for (int i = 0; i < childCount; i++) {//遍历子view
            View cellView = getChildAt(i);//获取子view
            if (getHorizontalSpacing() > 0) {
                if (getVerticalSpacing() > 0) {
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
                    canvas.drawLine(cellView.getRight() - 1, cellView.getTop(), cellView.getRight() - 1, cellView.getBottom(), localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getRight(), cellView.getTop(), localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getBottom() - 1, cellView.getRight(), cellView.getBottom() - 1, localPaint);
                } else {
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getRight(), cellView.getTop(), localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
                    canvas.drawLine(cellView.getRight() - 1, cellView.getTop(), cellView.getRight() - 1, cellView.getBottom(), localPaint);

                    if (childCount - i <= column) { //如果下面没有view，就画下边
                        canvas.drawLine(cellView.getLeft(), cellView.getBottom(), cellView.getRight(), cellView.getBottom(), localPaint);
                    }
                }
            } else {
                if (getVerticalSpacing() > 0) {
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getRight(), cellView.getTop(), localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getBottom() - 1, cellView.getRight(), cellView.getBottom() - 1, localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);

                    if ((i + 1) % column == 0 || i == childCount - 1) { //最后一列或者最后一个，画右边
                        canvas.drawLine(cellView.getRight() - 1, cellView.getTop(), cellView.getRight() - 1, cellView.getBottom(), localPaint);
                    }

                } else {
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getLeft(), cellView.getBottom(), localPaint);
                    canvas.drawLine(cellView.getLeft(), cellView.getTop(), cellView.getRight(), cellView.getTop(), localPaint);
                    if (childCount - i <= column) { //如果下面没有view，就画下边
                        canvas.drawLine(cellView.getLeft(), cellView.getBottom() - 1, cellView.getRight(), cellView.getBottom() - 1, localPaint);
                    }
                    if ((i + 1) % column == 0 || i == childCount - 1) { //最后一列或者最后一个，画右边
                        canvas.drawLine(cellView.getRight() - 1, cellView.getTop(), cellView.getRight() - 1, cellView.getBottom(), localPaint);
                    }
                }
            }
        }
    }

}