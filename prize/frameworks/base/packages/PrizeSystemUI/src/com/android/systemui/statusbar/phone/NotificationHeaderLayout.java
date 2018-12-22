/*****************************************
*版权所有©2015,深圳市铂睿智恒科技有限公司
*
*内容摘要：通知栏头StatusBarHeaderView的背景布局
*当前版本：V1.0
*作  者：liufan
*完成日期：2015-6-16
*修改记录：
*修改日期：
*版 本 号：
*修 改 人：
*修改内容：
********************************************/
package com.android.systemui.statusbar.phone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.Display;
import android.util.DisplayMetrics;
import android.util.Log;

/**
* 类描述：通知栏头StatusBarHeaderView的背景布局
* @author liufan
* @version V1.0
*/
@SuppressLint("NewApi")
public class NotificationHeaderLayout extends View {
    private Bitmap bitmap;
    private Paint paint;
    private Display mDisplay;

    public NotificationHeaderLayout(Context context) {
        super(context);
        init();
    }

    public NotificationHeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationHeaderLayout(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NotificationHeaderLayout(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        
        WindowManager mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
        * 由于模糊算法采用的缩小图片模糊法，所以这里再将图片放大
        */
        if (this.bitmap != null && !bitmap.isRecycled()) {
            int w = getWidth();
            int h = getHeight();
            if(w == 0 || h == 0){
                invalidate();
                return;
            }
            canvas.save();
            Rect rect = new Rect(0,0,w,h);
            canvas.clipRect(rect);
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics(mDisplayMetrics);
            int screenWidth = mDisplayMetrics.widthPixels ;
            int screenHeight = mDisplayMetrics.heightPixels;
            int rw = Math.min(screenWidth, w);
            int rh = (int)(rw / (float)bitmap.getWidth() * bitmap.getHeight());
            Rect r = new Rect(0, 0, rw, rh);
            canvas.drawBitmap(bitmap, null, r, paint);
		    canvas.restore();
        }
    }

    /**
    * 方法描述：设置背景，可以为null
    */
    public void setBg(Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }
}
