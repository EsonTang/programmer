package com.prize.weather.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

import com.prize.weather.R;
import com.prize.weather.util.OptimizeImage;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class MovingPictureView extends View implements Runnable{
	int moveType = 0;
	float downXValue;
	long downTime;
	private float lastTouchX, lastTouchY;
	private boolean hasMoved = false;
	// 用于显示的图�?
	Bitmap bitmap;

	// 图片坐标转化的线程是否运行，false，则停止运行
	public static boolean isRuning = true;

	// 图片的Lfet，Top�?
	int left = 100;
	int top = 20;

	// 用于同步线程
	Handler handler;
	MoveHandler movehandler;
	int sleepSeconds;
	int goBackType;

	// 向量，可以�?过调节此两个变量调节移动速度
	int dx = 1;
	int dy = 1;
	
	public int index;
	public Thread moving;
	public boolean isstarted = false;//是否已启动线�?

	int sandLeft,sandTop;
	
	/**
	 * 
	 * @param context 
	 * @param resource 图片资源
	 * @param left 居左
	 * @param top 居上
	 * @param sleepSeconds 移动时间间隔
	 */
	public MovingPictureView(Context context,int resource,int left,int top,int sleepSeconds) {
		super(context);
		
		this.left = left;
		this.top = top;
		this.sleepSeconds = sleepSeconds;
		
		sandLeft = left;
		sandTop = top;
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
//		bitmap = BitmapFactory.decodeResource(getResources(), resource);
//		
//		Resources r = this.getContext().getResources();
//		bitmap_snows = ((BitmapDrawable) r.getDrawable(resource)).getBitmap();
		bitmap = ((BitmapDrawable)context.getResources().getDrawable(resource)).getBitmap();
//		bitmap = LruCacheUtils.mLruCacheUtils.loadBitmap(context, resource);
//		Bitmap bitmap2 = ((BitmapDrawable)this.getContext().getResources().getDrawable(resource)).getBitmap();
//		bitmap = comp(bitmap2);
//		bitmap2.recycle();
		
		
		
		handler = new Handler();
		System.gc();
		movehandler = new MoveHandler();
		//new Thread(this).start();
	}

	
	public void move(int moveType){
		this.moveType = moveType;
//		if(moveType != -1){
			moving = new Thread(this);
			moving.start();			
//		}
	}	
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(bitmap!=null){
			canvas.drawBitmap(bitmap, left, top, null);
		}
//		if(moveType == -1){
//			bitmap.recycle();
//			return;
//		}
		/*if(bitmap == null){
			bitmap.recycle();			
		}*/
	}

	@Override
	public boolean onTouchEvent(MotionEvent evt) {
		//isRuning = false;//停止
		
		boolean consumed = super.onTouchEvent(evt);
		if (isClickable()) {
			switch (evt.getAction()) {

			case MotionEvent.ACTION_DOWN:
				lastTouchX = evt.getX();
				lastTouchY = evt.getY();
				downXValue = evt.getX();
				downTime = evt.getEventTime();
				hasMoved = false;
				break;

			case MotionEvent.ACTION_MOVE:
				hasMoved = moved(evt);
				break;

			case MotionEvent.ACTION_UP:
				float currentX = evt.getX();
				long currentTime = evt.getEventTime();

				break;
			}

		}
		return consumed || isClickable();
		
		//return true;
	}
	private Random random = new Random();
	@Override
	public void run() {
		isstarted = true;
		while (MovingPictureView.isRuning) {
			// 通过调节向量，来控制方向
//			dx = left < 0 || left > (getWidth() - bitmap.getWidth()) ? -dx : dx;
//			dy = top < 0 || top > (getHeight() - bitmap.getHeight()) ? -dy : dy;
//			left = left + dx;
//			top = top + dy;
			
			//控制循环移动
//			if((bitmap!=null) && (left >= (getWidth()))){
//				left = - bitmap.getWidth();
//			} 			
//			left = left + dx;
			if(moveType == 0){
				
			}else if(moveType == 1){
				if((bitmap!=null) && (left >= (getWidth()))){
					left = - bitmap.getWidth();
				}
				left = left + dx;
			}else if(moveType == 2){
				int tmpx = random.nextInt(5);
				int tmpy = random.nextInt(3);
				left = left +tmpx;
				top = top -tmpy;
				if((bitmap !=null)&&((top<0)||(left >= (getWidth())))){
					left = sandLeft;
					top = sandTop;
				}
			}

//			handler.post(new Runnable() {
//				@Override
//				public void run() {
//					invalidate();
//				}
//			});
			movehandler.sendMessage(handler.obtainMessage());

			try {
				Thread.sleep(sleepSeconds);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-start*/
		if (!MovingPictureView.isRuning) {
			isstarted = false;
		}
		/*PRIZE-Add-PrizeWeather-wangzhong-2016_8_10-end*/

	}
	
	private boolean moved(MotionEvent evt) {

		return hasMoved || Math.abs(evt.getX() - lastTouchX) > 10.0
				|| Math.abs(evt.getY() - lastTouchY) > 10.0;
	}
	
	public class MoveHandler extends Handler{
		@Override
        public void handleMessage(Message msg) {
			MovingPictureView.this.invalidate();
		}
	}
	
	
	/**图片按质量压缩*/
	private Bitmap compressImage(Bitmap image) {  		  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
        int options = 100;  
        while ( baos.toByteArray().length / 1024>100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩         
            baos.reset();//重置baos即清空baos  
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            options -= 10;//每次都减少10  
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
        return bitmap;  
    }  
	
	/**图片按比例压缩*/
	private Bitmap comp(Bitmap image) {  
	      
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();         
	    image.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
	    if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出    
	        baos.reset();//重置baos即清空baos  
	        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中  
	    }  
	    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
	    BitmapFactory.Options newOpts = new BitmapFactory.Options();  
	    //开始读入图片，此时把options.inJustDecodeBounds 设回true了  
	    newOpts.inJustDecodeBounds = true;  
	    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	    newOpts.inJustDecodeBounds = false;  
	    int w = newOpts.outWidth;  
	    int h = newOpts.outHeight;  
	    //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为  
	    float hh = 1280f;//这里设置高度为800f  
	    float ww = 720f;//这里设置宽度为480f  
	    //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
	    int be = 1;//be=1表示不缩放  
	    if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
	        be = (int) (newOpts.outWidth / ww);  
	    } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
	        be = (int) (newOpts.outHeight / hh);  
	    }  
	    if (be <= 0)  
	        be = 1;  
	    newOpts.inSampleSize = be;//设置缩放比例  
	    //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
	    isBm = new ByteArrayInputStream(baos.toByteArray());  
	    bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);  
	    return compressImage(bitmap);//压缩好比例大小后再进行质量压缩  
	}  
}
