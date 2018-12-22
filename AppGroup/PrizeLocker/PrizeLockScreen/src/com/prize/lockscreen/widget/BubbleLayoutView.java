package com.prize.lockscreen.widget;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.prize.lockscreen.Constant;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.prizelockscreen.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class BubbleLayoutView extends RelativeLayout {

	private final static String TAG = BubbleLayoutView.class.getName();
	private Context mContext;
	private List<BubbleView> mBubbleArray;
	private boolean isExit = false;
	
	public BubbleLayoutView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public BubbleLayoutView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public BubbleLayoutView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}
	
	private void init(){
		mBubbleArray = new ArrayList<BubbleView>();
		
		Bitmap bitmap_calculator = BitmapFactory.decodeResource(getResources(),
				R.drawable.calculator_icon);
		Bitmap bit_music = BitmapFactory.decodeResource(getResources(),
				R.drawable.music_icon);
		BubbleView calculatorBean = new BubbleView(mContext);
		calculatorBean.setImageBitmap(bitmap_calculator);
		calculatorBean.setBubbleType(Constant.BUBBLE_TYPE_CALCULATOR);
		
		BubbleView musicBean = new BubbleView(mContext);
		musicBean.setImageBitmap(bit_music);
		musicBean.setBubbleType(Constant.BUBBLE_TYPE_MUSIC);
		
		LayoutParams calcLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		calcLayoutParams.leftMargin = calculatorBean.getCurrentX();
		calcLayoutParams.topMargin = calculatorBean.getCurrentY();
		LayoutParams musicLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		musicLayoutParams.leftMargin = musicBean.getCurrentX();
		musicLayoutParams.topMargin = musicBean.getCurrentY();
		addView(calculatorBean, calcLayoutParams);
		addView(musicBean,musicLayoutParams);
		mBubbleArray.add(calculatorBean);
		mBubbleArray.add(musicBean);
//		new Thread(new MoveRunnable()).start();
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_MOVE;
		mHandler.sendMessageDelayed(msg, delayMillis);
	}
	
/*	*//**
	 * 设置背景
	 * @param bitmap
	 *//*
	public void setBackground(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int newWidth = DisplayUtil.getScreenWidthPixels();
		int newHeight = DisplayUtil.getScreenHeightPixels();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		mBackGroundBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
	}*/
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	/**
	 * 碰撞检测
	 * 
	 * @param bean
	 */
	private void crashDetection(BubbleView bean) {
		for (int i = 0; i < mBubbleArray.size(); i++) {
			BubbleView temp = mBubbleArray.get(i);
			if (temp.getBubbleType() == bean.getBubbleType()) {
				continue;
			}
			// 如果两个圆心的距离小于半径之和，则已经碰撞,方向变为原来的相反方向
			int distance = (int) Math.sqrt(Math.pow(
					temp.getCurrentX() - bean.getCurrentX(), 2)
					+ Math.pow(temp.getCurrentY() - bean.getCurrentY(), 2));
			if (distance <= bean.getBubbleSize()) {
				temp.setDirection(temp.getDirection() + 180);
				bean.setDirection(bean.getDirection() + 180);
			}

		}
	}
	
	private final static int delayMillis = 10;
	private final static int MSG_MOVE = 0X0002;
	
	private Handler mHandler = new Handler() {
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case MSG_MOVE:
				mHandler.removeMessages(MSG_MOVE);
				moveBubble();
				mHandler.sendEmptyMessageDelayed(MSG_MOVE, delayMillis);
				break;
			default:
				break;
			}
		}
	};
	
	private void moveBubble() {
		int x = 0;
		int y = 0;
		int direction = 0;
		int bubble_size = 0;
		for (int i = 0; i < mBubbleArray.size(); i++) {
			BubbleView bean = mBubbleArray.get(i);
			crashDetection(bean);
			x = bean.getCurrentX();
			y = bean.getCurrentY();
			direction = bean.getDirection();
			bubble_size = bean.getBubbleSize();

			if (x <= 0) {
				x = 0;
				direction = DisplayUtil.getDirection();
			}
			if (y <= 0) {
				y = 0;
				direction = DisplayUtil.getDirection();
			}
			if (x >= (DisplayUtil.getScreenWidthPixels() - bubble_size)) {
				x = DisplayUtil.getScreenWidthPixels() - bubble_size;
				direction = DisplayUtil.getDirection();
			}
			if (y >= (DisplayUtil.getScreenHeightPixels() - bubble_size)) {
				y = DisplayUtil.getScreenHeightPixels() - bubble_size;
				direction = DisplayUtil.getDirection();
			}
			// 四舍五入
			x = x
					+ new BigDecimal(Math.sin(direction) * bean.getSpeed())
							.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			y = y
					+ new BigDecimal(Math.cos(direction) * bean.getSpeed())
							.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
			bean.setDirection(direction);
			bean.setCurrentLocation(x, y);
			bean.moveBubble(x, y);
		}
	}
	

}
