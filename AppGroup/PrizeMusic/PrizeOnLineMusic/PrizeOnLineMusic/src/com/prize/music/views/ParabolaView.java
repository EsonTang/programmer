package com.prize.music.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

public class ParabolaView extends SurfaceView implements SurfaceHolder.Callback {

	public ParabolaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ParabolaView(Context context) {
		super(context);
		init();
	}

	private SurfaceHolder holder;

	/** 动画图标 */
	private Bitmap bitmap;
	private DrawThread thread;
	private ParabolaView.ParabolaListener listener;
	private boolean isRunAnimal;

	/** 默认未创建，相当于Destory */
	private boolean surfaceDestoryed = true;

	public ParabolaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		holder = getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		surfaceDestoryed = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceDestoryed = true;
		doing = false;
	}

	private Canvas canvas = null;

	public void handleThread() {

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		doing = true;
		isRunAnimal = true;
		if (listener != null) {
			listener.onParabolaStart(this);
		}		
//		int w = bitmap.getWidth();
//		int h = bitmap.getHeight();
		while (doing) {
			try {
				compute();
				canvas = holder.lockCanvas();
				if (canvas != null) {
					// 设置画布的背景为透明
					canvas.drawColor(Color.TRANSPARENT,
							android.graphics.PorterDuff.Mode.CLEAR);
					// bitmap = transBitmap(bitmap);
					if (isFly) {
						int translationY = (int) (progress * detalY);
						int translationX = (int) (progress * detalX);
						canvas.translate(translationX, translationY);

						// int translationY = (int) (progress*progress *detalY);
						// int translationX = (int) (progress *detalX);
						// canvas.translate(translationX,translationY);

						// if(scale>=0.8f){
						// scale = scale-0.007f;
						// canvas.scale(scale, scale, srcX+w/2, srcY+h/2);
						// }
						canvas.drawBitmap(bitmap, srcX, srcY, paint);
					} else {
						/*
						 * if(scale>0.0f){ scale = scale -0.04f;
						 * canvas.scale(scale, scale, offsetX+w/2, offsetY+h/2);
						 * canvas.drawBitmap(bitmap,offsetX, offsetY, paint); }
						 * else {
						 */
						// canvas.scale(0, 0, offsetX+w/2, offsetY+h/2);
						doing = false;
						isRunAnimal = false;
						// }
					}
				}
				//holder.unlockCanvasAndPost(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				//prize-public-bug:18520 monkey test ,music cause system freeze -pengcancan-0160725-start
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
				//prize-public-bug:18520 monkey test ,music cause system freeze -pengcancan-0160725-end
			}
		}
		// 清除屏幕内容
		// 直接�?Home"回桌面，SurfaceView被销毁了，lockCanvas返回为null�?
		if (surfaceDestoryed == false) {
			canvas = holder.lockCanvas();
			if (canvas!=null) {
				canvas.drawColor(Color.TRANSPARENT,
						android.graphics.PorterDuff.Mode.CLEAR);
				holder.unlockCanvasAndPost(canvas);
			}			
		}

		thread.setRunning(false);
		if (listener != null) {
			listener.onParabolaEnd(this);
		}
	}

	private float x = 0, y = 0;
	private long duration = 1000;
	private long deltaTime = 20;
	/** 动画正在进行时�?为true，反之为false�? */
	private boolean doing = false;
	private int detalY, detalX;
	private int srcX, srcY;
	private int offsetX;
	private int offsetY;
	private float progress;
	private boolean isFly;
	private float offestProgress = 0.04f;
	private int count = 0;

	public void compute() {
		if (x == 0) {
			x = srcX;
		}
		if (y == 0) {
			y = srcY;
		}
		if (progress < 1) {
			// x = x + detalX * deltaTime / duration;
			// progress = (float) x / detalX;
			// y = srcY + progress * progress * detalY;
			if (progress <= 0.5) {
				offestProgress = offestProgress + 0.005f;
			} else if (offestProgress >= 0.009) {
				offestProgress = offestProgress - 0.0085f;
			} else if (offestProgress >= 0.002f) {
				offestProgress = offestProgress - 0.001f;
			} else {
				offestProgress = 0.001f;
			}
			count++;
			progress = progress + offestProgress;
			// JLog.e("hu", "progress="+progress +
			// "offestProgress="+offestProgress);
			isFly = true;
		}

		if (progress >= 1) {
			progress = 1;
			isFly = false;
		}
	}

	public void showMovie() {
		if (thread == null) {
			thread = new DrawThread(this);
		} else if (thread.getState() == Thread.State.TERMINATED) {
			thread.setRunning(false);
			thread = new DrawThread(this);
		}
		if (thread.getState() == Thread.State.NEW) {
			thread.start();
		}
	}

	/** 正在播放动画时，返回true；否则返回false�? */
	public boolean isShowMovie() {
		return doing;
	}

	private float scale = 1f;

	int srcHeight;
	int srcWidth;
	int decHeight;
	int decWidth;

	/** 设置动画的X，Y距离 */
	public void setAnimationPara(ImageView srcView, View decView) {
		if (srcView == null || decView == null)
			return;
		progress = 0;
		scale = 1f;
		x = 0;
		y = 0;
		offestProgress = 0.04f;
		count = 0;
		int[] srcLocation = new int[2];
		srcView.getLocationInWindow(srcLocation);
		srcX = (int) srcLocation[0];
		srcY = (int) srcLocation[1];

		int[] decLocation = new int[2];
		decView.getLocationInWindow(decLocation);
		
		srcHeight = srcView.getHeight();
		srcWidth = srcView.getWidth();
		decHeight = decView.getHeight();
		decWidth = decView.getWidth();

		offsetX = decLocation[0] - (srcWidth / 2 - decWidth / 2);
		offsetY = decLocation[1] - (srcHeight / 2 - decHeight / 2);

		detalX = offsetX - srcX;
		detalY = offsetY - srcY;

		// JLog.e("hu", "srcX="+srcX + " srcY="+srcY
		// +" offsetX="+offsetX+" offsetY="+offsetY);
		// JLog.e("hu", "detalY="+detalY + " detalX="+detalX);
		if(srcView!=null){
			bitmap = convertViewToBitmap(srcView);
		}
	}

	/** view转成Bitmap */
	public static Bitmap convertViewToBitmap(ImageView view) {
		Bitmap bitmap = null;
		Bitmap scaledbitmap = null;
		if (view != null) {
			bitmap = ((BitmapDrawable) (view.getDrawable())).getBitmap();
		}
		if (bitmap != null && !bitmap.isRecycled()) {
			scaledbitmap = Bitmap.createScaledBitmap(bitmap, view.getWidth(),
					view.getHeight(), true);
		}
		return scaledbitmap;
	}

	/** 设置抛物线的动画监听器 */
	public void setParabolaListener(ParabolaView.ParabolaListener listener) {
		this.listener = listener;
	}

	public boolean isRunning() {
		return isRunAnimal;
	}

	static interface ParabolaListener {
		public void onParabolaStart(ParabolaView view);

		public void onParabolaEnd(ParabolaView view);
	}
}
