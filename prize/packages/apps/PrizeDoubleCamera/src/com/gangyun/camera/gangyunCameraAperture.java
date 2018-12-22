package com.gangyun.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.SeekBar;
import android.view.View;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class gangyunCameraAperture extends LinearLayout implements OnSeekBarChangeListener {
        private static final String TAG = "gangyunCameraAperture";
	private ApertureView mApertureView;
	   private FilterSeekBar mFilterSeekBar;
        private Handler mHandler;
        private OnGyProgressChangedListener mListener;
        private final static int ON_HIDE_VIEW = 0;
        
	    public gangyunCameraAperture(Context context, AttributeSet attrs) {
		super(context, attrs);
		mApertureView = new ApertureView(context);
		mFilterSeekBar = new FilterSeekBar(context);
		mFilterSeekBar.setOnSeekBarChangeListener(this);
		
		LinearLayout.LayoutParams ps = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,  LinearLayout.LayoutParams.MATCH_PARENT);
		LinearLayout.LayoutParams ps1 = new LinearLayout.LayoutParams(140, LinearLayout.LayoutParams.MATCH_PARENT);
		addView(mApertureView, ps1);
		addView(mFilterSeekBar, ps);
        mHandler = new MainHandler(context.getMainLooper());
		
	   }
	    
	   public gangyunCameraAperture(Context context) {
            super(context);
       }

   
       public gangyunCameraAperture(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

	    public interface OnGyProgressChangedListener {
	        void onGyProgressChanged(int arg1);
	    }
	    
	    public void setOnGyProgressChangedListener(OnGyProgressChangedListener listener) {
	        mListener = listener;
	    }

	    public void setBokehValue(int max,int value) {
			if(mFilterSeekBar != null){
				mFilterSeekBar.setMax(max);
				mFilterSeekBar.setProgress(value);
			}

	    }
				
       private class MainHandler extends Handler {

        public MainHandler(Looper looper) {
               super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
           switch (msg.what) {
            // this msg just used for VFB,so if you want use cFB,please be
            // careful
              case ON_HIDE_VIEW:
                   gyViewHide();
                   break;

               default:
                   break;
            }
        }
      }

       public void gyViewHide(){
           this.setVisibility(View.GONE);
       }
       public void gyShowView(){
           mHandler.removeMessages(ON_HIDE_VIEW);
	       this.setVisibility(View.VISIBLE);
           mHandler.sendEmptyMessageDelayed(ON_HIDE_VIEW,  3000);
       }


	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		mApertureView.setCurrentApert((1f/100)*arg1);
		mListener.onGyProgressChanged(arg1);
         if (mHandler != null) {
             mHandler.removeMessages(ON_HIDE_VIEW);
         }
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
              if (mHandler != null) {
                  mHandler.sendEmptyMessageDelayed(ON_HIDE_VIEW,  3000);
              }		
	}
	
	private int mXPos, mYPost;
	public void setPos(int x, int y){
		mXPos = x;
		mYPost = y;
	}

}
