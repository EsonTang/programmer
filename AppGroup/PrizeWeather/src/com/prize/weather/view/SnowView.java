package com.prize.weather.view;

import java.util.Random;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class SnowView extends View implements Runnable{
	int MAX_SNOW_COUNT = 10;

	Bitmap bitmap_snows = null;

	private final Paint mPaint = new Paint();

	private static final Random random = new Random();

	private Snow[] snows = new Snow[MAX_SNOW_COUNT];

	int view_height = 0;
	int view_width = 0;
	int MAX_SPEED = 55;

	Handler handler;
	int sleepSeconds;
	public SnowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SnowView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}
	
	private int type;
	public SnowView(Context context,int resource,int width,int height,int sleepSeconds,int count,int type){
		super(context);
		this.type = type;
		Log.d("move","count = "+count);
//		this.MAX_SNOW_COUNT = count;
		LoadSnowImage(context,resource);
		SetView(height, width);
		this.sleepSeconds = sleepSeconds;
		handler = new Handler();
		mRedrawHandler = new RefreshHandler();
	}

	private RefreshHandler mRedrawHandler;;

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			//snow.addRandomSnow();
			SnowView.this.invalidate();
			sleep(sleepSeconds);
		}
		public void sleep(long delayMillis) {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), delayMillis);
		}
	};
	/**
	 * ������Ůɢ���Ļ�ͼƬ���ڴ���
	 * 
	 */
	public void LoadSnowImage(Context context,int resource) {
		Resources r = this.getContext().getResources();
		bitmap_snows = ((BitmapDrawable) r.getDrawable(resource)).getBitmap();
//		bitmap_snows = LruCacheUtils.mLruCacheUtils.loadBitmap(context,resource);   //20150901
	}

	public void SetView(int height, int width) {
		view_height = height ;
		view_width = width ;
	}

	public void addRandomSnow() {
		Log.d("move","MAX_SNOW_COUNT  = "+MAX_SNOW_COUNT);
		for(int i =0; i< MAX_SNOW_COUNT;i++){
			snows[i] = new Snow(random.nextInt(view_width), 0,random.nextInt(MAX_SPEED));
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < MAX_SNOW_COUNT; i += 1) {
//			Log.d("move","snows["+i+"] = "+snows[i]);
			if(null != snows[i]){
//				Log.d("move","snows["+i+"].coordinate = "+snows[i].coordinate);
//				Log.d("move","snows["+i+"].coordinate.x = "+snows[i].coordinate.x);
				if (snows[i].coordinate.x >= view_width || snows[i].coordinate.y >= view_height) {
					snows[i].coordinate.y = 0;
					snows[i].coordinate.x = random.nextInt(view_width);
				}
				// 雪花下落的速度
				snows[i].coordinate.y += snows[i].speed + 15;
				//雪花飘动的效果

				if(type == 1){
//					snows[i].coordinate.x = random.nextInt(view_width+50);
					// 随机产生一个数字，让雪花有水平移动的效果
//					MAX_SPEED = 40;
					int tmp = MAX_SPEED/2 - (random.nextInt(MAX_SPEED));
					//为了动画的自然性，如果水平的速度大于雪花的下落速度，那么水平的速度我们取下落的速度。
					snows[i].coordinate.x += snows[i].speed < tmp ? snows[i].speed : tmp;		
					
				}
				
			/*	if(type == 2){
					if(snows[i].coordinate.x >= view_width || snows[i].coordinate.y >= view_height){
						snows[i].coordinate.y = view_height + 800-random.nextInt(view_height);
						snows[i].coordinate.x = 0;						
					}
					snows[i].coordinate.x += (random.nextInt(5));
					snows[i].coordinate.y += (random.nextInt(5));
					
				}*/
				canvas.drawBitmap(bitmap_snows, snows[i].coordinate.x,//((float) snows[i].coordinate.x)
						((float) snows[i].coordinate.y) - 140, mPaint);
			}
			
		}

	}
	
	public Thread moving;
	public void update(){
		moving = new Thread( this);
		moving.start();
	}
	
	@Override
	public void run() {
		addRandomSnow();
		mRedrawHandler.sleep(600);
		
	}

	
	public class Coordinate {
		public int x;
		public int y;

		public Coordinate(int newX, int newY) {
			x = newX;
			y = newY;
		}

		// public boolean equals(Coordinate other) {
		// if (x == other.x && y == other.y) {
		// return true;
		// }
		// return false;
		// }

		@Override
		public String toString() {
			return "Coordinate: [" + x + "," + y + "]";
		}
	}
	
	public class Snow {
		Coordinate coordinate;
		int speed;
		
		public Snow(int x, int y, int speed){
			coordinate = new Coordinate(x, y);
			System.out.println("Speed:"+speed);
			this.speed = speed;
			if(this.speed == 0) {
				this.speed =1;
			}
		}
		
	}
}
