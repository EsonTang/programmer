package com.android.prize;

import java.util.Timer;
import java.util.TimerTask;

import com.android.camera.CameraActivity;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import com.android.camera.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import com.android.camera.R;

public class BokehLongTimeInfo {
	
	private static final String TAG = "BokehLongTimeInfo";

	private static final int LONG_TIME = 3*60;
	private static final int MSG_SHOW_INFO = 0;
	
	private static final int THREAD_STATUE_START = 0;
	private static final int THREAD_STATUE_PAUSE = 1;
	private static final int THREAD_STATUE_STOP = 2;
	
	private int countTime = 0;
	private Thread mThread;
	private int mThreadStatue = THREAD_STATUE_PAUSE;
	private CameraActivity mActivity;
	
	private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
			case MSG_SHOW_INFO:
				showLongTimeInfo();
				break;
			}
        };
    };

    public BokehLongTimeInfo(CameraActivity cameraActivity) {
    	this.mActivity = cameraActivity;
	}
    
    class ThreadShow implements Runnable {

		@Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                	
                	if(mThreadStatue == THREAD_STATUE_START){
                    	Thread.sleep(1000);
                    }else if(mThreadStatue == THREAD_STATUE_PAUSE){
                    	if(mThread != null){
                    		synchronized (mThread) {
                		    	mThread.wait();
                			}
                    	}
                    }else if(mThreadStatue == THREAD_STATUE_STOP){
                    	break;
                    }
                	countTime++;
                    if(countTime == LONG_TIME){
                    	 handler.sendEmptyMessage(MSG_SHOW_INFO);
                    	 countTime = 0;
                    }
                    
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    
                }
            }
        }
    }
	
	public void showLongTimeInfo() {
		// TODO Auto-generated method stub
		Toast toast = new Toast(mActivity);
		View view = LayoutInflater.from(mActivity).inflate(R.layout.double_camera_longtime_toast, null);
		toast.setView(view);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, 70);
		toast.show();
	}
	
	public void startTime(){
		
		if(mThread == null){
			mThread =  new Thread(new ThreadShow());
			mThread.start();
		}
	}
	
	public void resumeTime(){
		mThreadStatue = THREAD_STATUE_START;
		if(mThread != null){
			synchronized (mThread) {
		    	mThread.notify();
			}
    	}
		
	}
	
	
	public void pausTime(){
		mThreadStatue = THREAD_STATUE_PAUSE;
	}
	
	public void stopTime(){
		mThreadStatue = THREAD_STATUE_STOP;
		if(mThread != null){
			synchronized (mThread) {
		    	mThread.notify();
			}
    	}
	}
}
