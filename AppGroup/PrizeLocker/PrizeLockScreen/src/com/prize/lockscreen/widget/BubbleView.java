package com.prize.lockscreen.widget;

import java.util.Random;

import com.prize.lockscreen.application.LockScreenApplication;
import com.prize.lockscreen.utils.DisplayUtil;
import com.prize.lockscreen.utils.LogUtil;
import com.prize.prizelockscreen.R;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;

public class BubbleView extends ImageView {
	
	private final static String TAG = BubbleView.class.getName();
	
	private int currentX;
	private int currentY;
	private int direction;
	private int bubbleSize;
	private int radius;
	private boolean isMove;
	private int speed;
	
	private int bubble_type;
	private boolean unlock;
	private int velocityX;
	private int velocityY;
	
	public BubbleView(Context context) {
		super(context);
		init(context);
	}
	
	public BubbleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}
	
	public void init(Context context) {
		bubbleSize = context.getResources().getDimensionPixelSize(
				R.dimen.bubble_bitmap_size);
		DisplayMetrics dm = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getMetrics(dm);
		Random rand = new Random();
		currentX = rand.nextInt(dm.widthPixels-bubbleSize);
		currentY = rand.nextInt(dm.heightPixels-bubbleSize);
		direction = DisplayUtil.getDirection();
		this.radius = bubbleSize / 2;
		this.isMove = true;
		this.unlock=false;
		this.speed = 2;
		this.velocityX = 0;
		this.velocityY = 0;
	}
		
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int radius_x = LockScreenApplication.getContext().getResources().getDimensionPixelSize(R.dimen.bubble_width) / 2;
		final int radius_y = LockScreenApplication.getContext().getResources().getDimensionPixelSize(R.dimen.bubble_height) / 2;
		final int x = (int) event.getRawX() - radius_x;
		final int y = (int) event.getRawY() - radius_y;
		LogUtil.d(TAG, "----->onTouchEvent x = "+x+" ,y = "+y);
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			return true;
		case MotionEvent.ACTION_MOVE:
			this.setCurrentLocation(x, y);
			this.moveBubble(x, y);
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return super.onTouchEvent(event);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	
	public int getCurrentX() {
		return currentX;
	}

	public void setCurrentX(int currentX) {
		this.currentX = currentX;
	}

	public int getCurrentY() {
		return currentY;
	}

	public void setCurrentY(int currentY) {
		this.currentY = currentY;
	}

	public void setCurrentLocation(int x, int y) {
		this.currentX = x;
		this.currentY = y;
	}
	
	/**
	 * 移动Bubble
	 * @param x
	 * @param y
	 */
	public void moveBubble(int x, int y){
		this.setFrame(x, y - this.getHeight(), x + this.getWidth(), y); 
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getBubbleSize() {
		return bubbleSize;
	}

	public void setBubbleSize(int bubbleSize) {
		this.bubbleSize = bubbleSize;
	}

	public boolean getIsMove() {
		return isMove;
	}

	public void setIsMove(boolean isMove) {
		this.isMove = isMove;
	}

	public int getBubbleType() {
		return bubble_type;
	}

	public void setBubbleType(int bubble_type) {
		this.bubble_type = bubble_type;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public boolean isUnlock() {
		return unlock;
	}

	public void setUnlock(boolean unlock) {
		this.unlock = unlock;
	}

	public int getVelocityX() {
		return velocityX;
	}

	public void setVelocityX(int velocityX) {
		this.velocityX = velocityX;
	}

	public int getVelocityY() {
		return velocityY;
	}

	public void setVelocityY(int velocityY) {
		this.velocityY = velocityY;
	}
	
	public void setVelocity(int velocityX,int velocityY){
		this.velocityX = velocityX;
		this.currentY = velocityY;
	}
	
}
