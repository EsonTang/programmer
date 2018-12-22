package com.prize.lockscreen.bean;

import android.graphics.Bitmap;

public class CircleBean {

	private int mColor;

	private int mAlpha;

	private float mCircleRadius;

	private Bitmap mBitmap;

	private float mCurrentPostionX;

	private float mCurrentPostionY; 
	//弧度
	private float mAngle;

	private float mMoveSize ;
	
	public CircleBean() {

	}
	
	public void setAngle(float mAngle){
		this.mAngle = mAngle;
	}
	
	public float getAngle(){
		return this.mAngle;
	}
	
	public void setMoveSize(float mMoveSize){
		 this.mMoveSize=mMoveSize;
	}
	
	public float getMoveSize(){
		return this.mMoveSize;
	}

	public void setCurrentPostionX(float mCurrentPostionX) {
		this.mCurrentPostionX = mCurrentPostionX;
	}

	public float getCurrentPostionX() {
		return this.mCurrentPostionX;
	}

	public void setCurrentPostionY(float mCurrentPostionY) {
		this.mCurrentPostionY = mCurrentPostionY;
	}

	public float getCurrentPostionY() {
		return this.mCurrentPostionY;
	}

	public void setColor(int mColor) {
		this.mColor = mColor;
	}

	public int getColor() {
		return this.mColor;
	}

	public void setAlpha(int mAlpha) {
		this.mAlpha = mAlpha;
	}

	public int getAlpha() {
		return this.mAlpha;
	}

	public void setCircleRadius(float mCircleRadius) {
		this.mCircleRadius = mCircleRadius;
	}

	public float getCircleRadius() {
		return this.mCircleRadius;
	}

	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public Bitmap getBitmap() {
		return this.mBitmap;
	}

	
	public String toString() {
		String result = "mColor = " + mColor + " ,mAlpha = " + mAlpha
				+ " ,mCurrentPostionX = " + mCurrentPostionX
				+ " ,mCurrentPostionY = " + mCurrentPostionY
				+ " ,mCircleRadius = " + mCircleRadius;
		return result;
	}
}
