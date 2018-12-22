package com.gangyun.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.android.camera.Log;

/**
 * 上下滑动可以调节光圈大小；
 * 调用setApertureChangedListener设置光圈值变动监听接口；
 * 绘制的光圈最大直径将填满整个view
 * 
 */
public class ApertureView extends View {

    public interface ApertureChanged {
        public void onApertureChanged(float newapert);
    }

    private static final float ROTATE_ANGLE = 30;
    private static final String TAG = "ApertureView";
    private static final float COS_30 = 0.866025f;
    private static final int WIDTH = 70;
    private static final int HEIGHT = 70;
    private int mCircleRadius;
    private int mBladeColor;
    private int mBackgroundColor;
    private int mSpace;
    private float mMaxApert = 1;
    private float mMinApert = 0.2f;
    private float mCurrentApert = 0.5f;

    private PointF[] mPoints = new PointF[6];
    private Bitmap mBlade;
    private Paint mPaint;
    private Path mPath;
    private ApertureChanged mApertureChanged;

    private float mPrevX;
    private float mPrevY;

    public ApertureView(Context context, AttributeSet attrs) {
        super(context, attrs);
/*
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ApertureView);
        mSpace = (int)array.getDimension(R.styleable.ApertureView_blade_space, 5);
        mBladeColor = array.getColor(R.styleable.ApertureView_blade_color, 0xFF000000);
        mBackgroundColor = array.getColor(R.styleable.ApertureView_background_color, 0xFFFFFFFF);
        array.recycle();
        Log.d("lyh", " init ");
*/
        init();
    }
    
    public ApertureView(Context contexts) {
    	super(contexts);
        mSpace = 5;
        mBladeColor =  0xFFDFDFDF;
        mBackgroundColor =0xFFFFFFFF;
        //array.recycle();
        Log.d("lyh", " init ");

        init();
    }

    private void init() {
        mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        for (int i = 0; i < 6; i++) {
            mPoints[i] = new PointF();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int paddX = getPaddingLeft() + getPaddingRight();
        int paddY = getPaddingTop() + getPaddingBottom();
        mCircleRadius = widthSpecSize - paddX < heightSpecSize - paddY ? (widthSpecSize - paddX) / 2
                : (heightSpecSize - paddY) / 2;
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WIDTH, HEIGHT);
            mCircleRadius = (WIDTH - paddX) / 2;
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WIDTH, heightSpecSize);
            mCircleRadius = WIDTH - paddX < heightSpecSize - paddY ? (WIDTH - paddX) / 2
                    : (heightSpecSize - paddY) / 2;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, HEIGHT);
            mCircleRadius = widthSpecSize - paddX < HEIGHT - paddY ? (widthSpecSize - paddX) / 2
                    : (HEIGHT - paddY) / 2;
        }
        if (mCircleRadius < 1) {
            mCircleRadius = 1;
        }
        mPath = new Path();
        mPath.addCircle(0, 0, mCircleRadius, Path.Direction.CW);
        createBlade();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        calculatePoints();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(ROTATE_ANGLE * (mCurrentApert - mMinApert) / (mMaxApert - mMinApert));
        canvas.clipPath(mPath);
       // canvas.drawColor(mBackgroundColor);

        for (int i = 0; i < 6; i++) {
            canvas.save();
            canvas.translate(mPoints[i].x, mPoints[i].y);
            canvas.rotate(-i * 60);
            canvas.drawBitmap(mBlade, 0, 0, mPaint);
            canvas.restore();
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            return false;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPrevX = event.getX();
            mPrevY = event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            float diffx = Math.abs((event.getX() - mPrevX));
            float diffy = Math.abs((event.getY() - mPrevY));
            if (diffy > diffx) {
                float diff = (float) Math.sqrt(diffx * diffx + diffy * diffy)
                        / mCircleRadius * mMaxApert;
                if (event.getY() > mPrevY) {
                    setCurrentApert(mCurrentApert - diff);
                } else {
                    setCurrentApert(mCurrentApert + diff);
                }
                mPrevX = event.getX();
                mPrevY = event.getY();
            }
            break;
        default:
            break;
        }
        return true;
    }

    private void calculatePoints() {
        if (mCircleRadius - mSpace <= 0) {
            Log.e(TAG, "the size of view is too small and Space is too large");
            return;
        }
        float curRadius = mCurrentApert / mMaxApert * (mCircleRadius - mSpace);
        mPoints[0].x = curRadius / 2;
        mPoints[0].y = -curRadius * COS_30;
        mPoints[1].x = -mPoints[0].x;
        mPoints[1].y = mPoints[0].y;
        mPoints[2].x = -curRadius;
        mPoints[2].y = 0;
        mPoints[3].x = mPoints[1].x;
        mPoints[3].y = -mPoints[1].y;
        mPoints[4].x = -mPoints[3].x;
        mPoints[4].y = mPoints[3].y;
        mPoints[5].x = curRadius;
        mPoints[5].y = 0;
    }

    private void createBlade() {
        mBlade = Bitmap.createBitmap(mCircleRadius,
                (int) (mCircleRadius * 2 * COS_30), Config.ARGB_8888);
        Path path = new Path();
        Canvas canvas = new Canvas(mBlade);
        path.moveTo(mSpace / 2 / COS_30, mSpace);
        path.lineTo(mBlade.getWidth(), mBlade.getHeight());
        path.lineTo(mBlade.getWidth(), mSpace);
        path.close();
        canvas.clipPath(path);
        canvas.drawColor(mBladeColor);
    }

    /**
     * 设置光圈片的颜色
     * @param bladeColor
     */
    public void setBladeColor(int bladeColor) {
        mBladeColor = bladeColor;
    }

    /**
     * 设置光圈背景色
     */
    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }

    /**
     * 设置光圈片之间的间隔
     * @param space
     */
    public void setSpace(int space) {
        mSpace = space;
    }

    /**
     * 设置光圈最大值
     * @param maxApert
     */
    public void setMaxApert(float maxApert) {
        mMaxApert = maxApert;
    }

    /**
     * 设置光圈最小值
     * @param mMinApert
     */
    public void setMinApert(float mMinApert) {
        this.mMinApert = mMinApert;
    }

    public float getCurrentApert() {
        return mCurrentApert;
    }

    public void setCurrentApert(float currentApert) {
        if (currentApert > mMaxApert) {
            currentApert = mMaxApert;
        }
        if (currentApert < mMinApert) {
            currentApert = mMinApert;
        }
        if (mCurrentApert == currentApert) {
            return;
        }
        mCurrentApert = currentApert;
        invalidate();
        if (mApertureChanged != null) {
            mApertureChanged.onApertureChanged(currentApert);
        }
    }

    public void setApertureChangedListener(ApertureChanged listener) {
        mApertureChanged = listener;
    }

}
