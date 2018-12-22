package com.android.systemui.statusbar.phone;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.systemui.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.graphics.Color;
import java.util.Timer;
import java.util.TimerTask;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PorterDuff.Mode;
import android.graphics.PixelFormat;

public class KeyguardChargeAnimationView extends View {

    private final float BATTERY_BG_Y = 0.40f;// 0.1f;// battery_bg Y Point
    private final int STARS_INTERVAL = 450;// star's Interval appears
    // private final int BATTERY_COLOR = 0xff32d125;
    private final float BOTTOM_SPACE_RATE = 0.0258f;

    private final int CREATE_STAR_MSG = 10002;

    private int speedUpFactor = 1;

    private Paint paint;
    private Paint textPaint;
    private Bitmap batteryBg;
    private Bitmap batterySourceBmp;
    private Bitmap batteryBmp;
    private Bitmap[] batteryStar;
    private int width, height;
    private List<Star> starList;
    private int battery;// battery
    private boolean isPause = false;
    private AnimationListener onAnimationListener;
    private ScreenListenReceiver reciver;
    private boolean isScreenOn = true;

    public class Star {
        public int x, y;
        public Bitmap bitmap;
        public boolean isLeft;// weather move to left
        public float verSpeed;// Increase Vertical speed
        public float horSpeed;// Increase horizontal speed
        public int swingDistance;// Swing distance
        public float offx;
    }

    public abstract class AnimationListener {

        public void onAnimationStart() {
        }

        public void onAnimationEnd() {
        }

    }

    public KeyguardChargeAnimationView(Context context) {
        super(context);
        init();
    }

    public KeyguardChargeAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyguardChargeAnimationView(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("NewApi")
    public KeyguardChargeAnimationView(Context context, AttributeSet attrs,
            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        starList = new ArrayList<Star>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setDither(true);
        int size = mContext.getResources().getDimensionPixelSize(R.dimen.keyguard_charge_textsize);
        textPaint.setTextSize(size);
        textPaint.setColor(0xbbffffff);

        batteryBg = BitmapFactory.decodeResource(getResources(),
                R.drawable.batterybg);
        batteryBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.batterybmp);
        batterySourceBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.batterysource);
        batteryStar = new Bitmap[] {
                BitmapFactory.decodeResource(getResources(), R.drawable.star_s),
                BitmapFactory.decodeResource(getResources(), R.drawable.star_m),
                BitmapFactory.decodeResource(getResources(), R.drawable.star_b) };
        
        reciver = new ScreenListenReceiver();
        IntentFilter recevierFilter = new IntentFilter();
        recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
        recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(reciver, recevierFilter);
    }

    /**
     * 方法描述：开始动画
     */
    public void start() {
        if(!isPause && speedUpFactor == 1){
            return;
        }
        if(!isScreenOn){
            return;
        }
        isPause = false;
        speedUpFactor = 1;
        mHandler.removeMessages(CREATE_STAR_MSG);
        createOneStar();
        if (onAnimationListener != null) {
            onAnimationListener.onAnimationStart();
        }
    }

    /**
     * 方法描述：暂停动画
     */
    public void pause() {
        isPause = true;
    }

    /**
     * 方法描述：暂停动画
     */
    public void stop() {
        isPause = true;
        speedUpFactor = 3;
        mHandler.removeMessages(CREATE_STAR_MSG);
        synchronized (starList) {
            starList.clear();
        }
    }
    
    /**
     * 方法描述：创建一个星星
     */
    private void createOneStar() {
        synchronized (starList) {
            Star star = new Star();
            int index = (int) (Math.random() * batteryStar.length);
            star.bitmap = batteryStar[index];
            star.x = (int) ((width - batteryBg.getWidth()) / 2 + Math.random()
                    * (batteryBg.getWidth() - star.bitmap.getWidth()));
            star.y = height;
            star.verSpeed = (float) (2.5 + Math.random() * 2);
            star.horSpeed = (float) (0.5 + Math.random());
            star.isLeft = Math.random() > 0.5f ? false : true;
            int dis = batteryBg.getWidth() / 3;
            star.swingDistance = (int) (dis + Math.random() * dis);
            if(getVisibility() == View.VISIBLE){
                starList.add(star);
            }
            if (!isPause) {
                mHandler.removeMessages(CREATE_STAR_MSG);
                mHandler.sendEmptyMessageDelayed(CREATE_STAR_MSG,
                        STARS_INTERVAL);
                if(starList.size() == 1){
                    invalidate();
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width == 0 || height == 0) {
            WindowManager mWindowManager = (WindowManager) getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            Display mDisplay = mWindowManager.getDefaultDisplay();
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics(mDisplayMetrics);
            width = mDisplayMetrics.widthPixels;
            height = mDisplayMetrics.heightPixels;
        }
		/*prize-modify for rotation-zhangjialong-20150930-start*/
        if(width>height){
			int mExchange =0;
			mExchange =width;
			width = height;
			height = mExchange;          
		}        
		/*prize-modify for rotation-zhangjialong-20150930-end*/
		
        if (width != 0 && height != 0) {
            synchronized (starList) {
                drawBatterySourceBmp(canvas);
                drawBatteryBg(canvas);
                drawStars(canvas);
                drawCharge(canvas);
                invalidate();
            }
        }
        //Log.d("liufan-","KeyguardChargeAnimationView----onDraw--");
    }

    /**
     * 方法描述：画动态的星星
     */
    private void drawStars(Canvas canvas) {
        if(battery == 100){
            return;
        }
        if (starList == null || starList.size() == 0) {
            return;
        }
        boolean isUpdate = true;
        if(updateTime==0){
            updateTime = System.currentTimeMillis();
        }
        long curTime = System.currentTimeMillis();
        if(curTime-updateTime < 15){
            isUpdate = false;
        }
        
        for (int i = 0; i < starList.size(); i++) {
            Star star = starList.get(i);
            if (star.bitmap == null || star.bitmap.isRecycled()) {
                continue;
            }
            canvas.drawBitmap(star.bitmap, star.x + star.offx, star.y, paint);
            if(isUpdate){
                updateTime = curTime;
                boolean isDelete = updateStar(star);
                if (isDelete) {
                    starList.remove(star);
                    if (starList.size() == 0 && onAnimationListener != null) {
                        onAnimationListener.onAnimationEnd();
                    }
                    i--;
                }
            }
        }
    }

    private long updateTime = 0;
    /**
     * 方法描述：刷新星星的位置
     * 
     * @return 返回是否需要删除星星
     */
    private boolean updateStar(Star star) {
        if (star.isLeft) {
            star.offx -= star.horSpeed * speedUpFactor;
            if (star.offx <= -star.swingDistance) {
                star.isLeft = false;
            }
            if (star.x + star.offx <= (width - batteryBg.getWidth()) / 2) {
                star.isLeft = false;
            }
        } else {
            star.offx += star.horSpeed * speedUpFactor;
            if (star.offx >= star.swingDistance) {
                star.isLeft = true;
            }
            if (star.x + star.offx >= (width + batteryBg.getWidth()) / 2
                    - star.bitmap.getWidth()) {
                star.isLeft = true;
            }
        }
        star.y -= star.verSpeed * speedUpFactor;
        if (star.y <= (height * BATTERY_BG_Y + batteryBg.getHeight())) {
            return true;
        }
        return false;
    }
	/**
     * 方法描述：画电量文字
     */
    private void drawCharge(Canvas canvas) {
        if (batteryBmp == null || batteryBmp.isRecycled()) {
            return;
        }
		String text = battery + "%";
        float len = textPaint.measureText(text);
		int x = (int)((width - len) / 2);
		int y = (int)(height * BATTERY_BG_Y + batteryBg.getHeight() * 0.50f);
        canvas.drawText(text, x, y, textPaint);
    }
    
    /**
     * 方法描述：画电量
     */
    private void drawBattery(Canvas canvas, int left, float top, int bgHeight) {
        if (batteryBmp == null || batteryBmp.isRecycled()) {
            return;
        }
        float rate = battery / 100f;
        float space = BOTTOM_SPACE_RATE * bgHeight;
        RectF rf = new RectF();
        rf.left = (width - batteryBmp.getWidth()) / 2;
        rf.right = rf.left + batteryBmp.getWidth();
        rf.bottom = top + bgHeight - space;
        rf.top = rf.bottom - (bgHeight - space * 3) * rate;
        canvas.drawBitmap(batteryBmp, null, rf, paint);
    }

    /**
     * 方法描述：画电池背景
     */
    private void drawBatteryBg(Canvas canvas) {
        if (batteryBg == null || batteryBg.isRecycled()) {
            return;
        }
        int x = (width - batteryBg.getWidth()) / 2;
        float y = height * BATTERY_BG_Y;
        canvas.drawBitmap(batteryBg, x, y, paint);
        drawBattery(canvas, x, y, batteryBg.getHeight());
    }

    /**
     * 方法描述：画动画底部的背景
     */
    private void drawBatterySourceBmp(Canvas canvas) {
        if(battery == 100){
            return;
        }
        if (batterySourceBmp == null || batterySourceBmp.isRecycled()) {
            return;
        }
        int x = (width - batterySourceBmp.getWidth()) / 2;
        int y = height - batterySourceBmp.getHeight();
        canvas.drawBitmap(batterySourceBmp, x, y, paint);
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case CREATE_STAR_MSG:
                createOneStar();
                break;
            }
        }

    };
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(reciver);
    }

    public class ScreenListenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                isScreenOn = true;
                start();
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                isScreenOn = false;
                stop();
            }
        }

    }
    
    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void setOnAnimationListener(AnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }

}
