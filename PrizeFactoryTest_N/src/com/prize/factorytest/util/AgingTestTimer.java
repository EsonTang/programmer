package com.prize.factorytest.util;

import com.prize.factorytest.AgingTestActivity;

import android.os.CountDownTimer;

public class AgingTestTimer{
	
	private static final String TAG = "AgingTestTimer";
	
	private TickCallBack mTickCallBack;
	private TimeUpCallBack mTimeUpCallBack;
	private CountDownTimer mCountDownTimer;
	
	private long gDuration =0;
	private long gStartTime =0;
	private int id = -1;
	
	public interface TickCallBack{
		public void showElapseTime(int id,long start);
	}
	public interface TimeUpCallBack{
		public void goToNextItem(int id);
	}
	
	public AgingTestTimer(long duration,TickCallBack tickCallback,TimeUpCallBack timeupCallback) {
		// TODO Auto-generated constructor stub
		gDuration = duration;		
		mTickCallBack = tickCallback;
		mTimeUpCallBack = timeupCallback;
	}
	
	public void start(int id,long start){
		this.id=id;
		gStartTime = start;
		if(mCountDownTimer!=null){
			mCountDownTimer.cancel();
			mCountDownTimer=null;
		}			
		mCountDownTimer = new CountDownTimer(gDuration*1000, 1000) {
			@Override
			public void onFinish() {
				mTimeUpCallBack.goToNextItem(getCurrent());
			}
			@Override
			public void onTick(long arg0) {				
				mTickCallBack.showElapseTime(getCurrent(),getStartTime());
			}
		}.start();
	}
	public void cancel(){
		if(mCountDownTimer!=null){
			mCountDownTimer.cancel();
			mCountDownTimer=null;
		}			
	}

	public int getCurrent(){
		return id;
	}
	public long getStartTime(){
		return gStartTime;
	}
}
