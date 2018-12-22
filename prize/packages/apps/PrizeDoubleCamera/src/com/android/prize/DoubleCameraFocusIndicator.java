package com.android.prize;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import com.android.camera.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.camera.R;
import com.android.camera.CameraActivity;
import com.android.camera.Util;
import com.android.camera.ui.FocusIndicatorRotateLayout;
import com.android.camera.ui.RotateLayout;
import com.mediatek.camera.setting.SettingConstants;

public class DoubleCameraFocusIndicator extends RotateLayout implements OnSeekBarChangeListener{

	private static final String TAG = "DoubleCameraFocusIndicator";

	private static final int DELAY_TIME_HIDE = 2000;
	private static final int DELAY_TIME_GRAY = 2000;
	private static final int DELAY_START_SHOW = 300;
	private static final int MSG_HIDE = 0;
	private static final int MSG_GRAY = 1;
	private static final int MSG_START_SHOW = 2;

	private static final int AUTO_FOCUSING = 0;
	private static final int AUTO_FOCUSED = 1;
	private static final int MV_FOCUSING = 2;
	private static final int MV_FOCUSED = 3;
	
	private SeekBar mSeekBar;
	
	private ApertureView mApertureView;
	
	private int focusStatue = -1;
	
	private int cnt = 0;
	//PRIZE-modify-log last focus point-20170303-pengcancan-start
	private int mLastFocusPointX;

	private int mLastFocusPointY;
	//PRIZE-modify-log last focus point-20170303-pengcancan-end
	
	private int screenwight = 0;
	
	private int screenheight = 0;
	
	private Integer mApertureValue;
	
	private  int mSeekBarOrignalMarginTop;  
	
	public interface OnSeekBarChangeListener {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) ;
		public void onStartTrackingTouch(SeekBar seekBar);

		public void onStopTrackingTouch(SeekBar seekBar) ;
	}
	
	private OnSeekBarChangeListener mOnSeekBarChangeListener;
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_HIDE:
					setVisibility(View.GONE);
				break;
				case MSG_GRAY:
					setViewGray();
					mHandler.removeMessages(MSG_HIDE);
					mHandler.sendEmptyMessageDelayed(MSG_HIDE, DELAY_TIME_HIDE);
				break;
				case MSG_START_SHOW:
					String tempValue = ((CameraActivity)getContext()).getListPreference(SettingConstants.KEY_APERTURE).getValue();
					mApertureValue = Integer.valueOf(tempValue);
					mSeekBar.setProgress(mApertureValue);
					/*prize-add-the location of cameraFocusIndicator displayed in the middle of the preview area-20170926-xiaoping-start*/
					mLastFocusPointY = (int)mContext.getResources().getDimension(R.dimen.camerafocus_indicator_default_height);
					/*prize-add-the location of cameraFocusIndicator displayed in the middle of the preview area-20170926-xiaoping-start*/
					onSinlgeTapUp(null, mLastFocusPointX, mLastFocusPointY);
					onAutoFocused();
				break;
				
			}
		}
		
	};
	
	private void setViewGray() {
		// TODO Auto-generated method stub
		mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg_gray);
		mSeekBar.setThumb(getResources().getDrawable(R.drawable.doublecamera_seakbar_thumb_gray));
		mSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.doublecamera_seakbar_pro_gray));
	}
	
	private void setViewNormal() {
		// TODO Auto-generated method stub
		mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg);
		mSeekBar.setThumb(getResources().getDrawable(R.drawable.doublecamera_seakbar_thumb));
		mSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.doublecamera_seakbar_pro));
	}
	
	public void updateApertureView(int size){
		 mApertureView.setCurrentApert(mApertureView.getMinApert()+size*(1-mApertureView.getMinApert())/ 100f);
         mApertureView.invalidate();
	}
	
	public DoubleCameraFocusIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		getScreenWH();
	}
	
	@Override
	protected void onLayout(boolean change, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(change, left, top, right, bottom);
		
		
	}
	
	public void initUi(){
		if(mApertureView == null){
			mApertureView = (ApertureView)findViewById(R.id.aperture);
		}
		if(mSeekBar == null){
			mSeekBar = (SeekBar)findViewById(R.id.double_camera_seekbar);
			mSeekBar.setOnSeekBarChangeListener(this);
			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)mSeekBar.getLayoutParams();
			mSeekBarOrignalMarginTop = p.topMargin;
		}

	}
	public void onSinlgeTapUp(View view, int x, int y) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onSinlgeTapUp x = "+x+", y = "+y);

		//PRIZE-modify-log last focus point-20170303-pengcancan-start
		mLastFocusPointX = x;
		mLastFocusPointY = y;
		//PRIZE-modify-log last focus point-20170303-pengcancan-end
		
		setSeekBarVisible(View.INVISIBLE);
		setApertureHide();
		
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)getLayoutParams();
	    int left = 0;
	    int top = 0;
		//PRIZE-add-invalidate seekbar layoutparam-20170303-start
	    int focusWidth = getWidth();
        int focusHeight = getHeight();
        int previewWidth = ((CameraActivity)getContext()).getPreviewFrameWidth();
        int previewHeight = ((CameraActivity)getContext()).getPreviewFrameHeight();
	    left = Util.clamp(x - focusWidth / 2, (- focusWidth / 2 + mApertureView.getWidth()/2), (previewWidth - focusWidth/2 - mApertureView.getWidth()/2));
	    top = Util.clamp(y - focusHeight / 2, 0, previewHeight - focusHeight);
	    Log.d(TAG, "left:"+left+"top:"+"x:"+x+",y:"+y+",focusWidth:"+focusWidth+",focusHeight:"+focusHeight);
	    onSeekbarLayout(x, previewWidth, focusWidth);
	    onFocuseLayout(left, top, previewWidth, focusWidth);
		//PRIZE-add-invalidate seekbar layoutparam-20170303-end
	    if(getVisibility() != View.VISIBLE){
	    	setVisibility(View.VISIBLE);
	    }
	}

	private void setApertureHide() {
		// TODO Auto-generated method stub
		if(mApertureView != null){
			mApertureView.hide();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		mApertureValue = progress;
	    updateApertureView(progress);
	    if(mOnSeekBarChangeListener != null){
	    	mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
	    }
	    mHandler.removeMessages(MSG_HIDE);
	    mHandler.removeMessages(MSG_GRAY);
	    mHandler.sendEmptyMessageDelayed(MSG_GRAY, DELAY_TIME_GRAY);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if(mOnSeekBarChangeListener != null){
	    	mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
	    }
		setViewNormal();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		((CameraActivity)getContext()).getListPreference(SettingConstants.KEY_APERTURE).setValue(String.valueOf(mApertureValue));
		if(mOnSeekBarChangeListener != null){
	    	mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
	    }
	}
	
	
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener mOnSeekBarChangeListener){
		this.mOnSeekBarChangeListener = mOnSeekBarChangeListener;
	}
	
	public void setSeekBarVisible(int visible){
		if(mSeekBar != null){
			mSeekBar.setVisibility(visible);
		}
	}
	
	public void onFocusStart(){
		//mApertureView.setBackground(background);
	}
	
	public void onFocusViewEnd(){
		if(mApertureView != null){
			mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg);
		}
	}

	public void onAutoFocusing() {
		// TODO Auto-generated method stub
		focusStatue = AUTO_FOCUSING;
		if(mApertureView != null){
			mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg_white);
			Animation mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.aperture_alpha);
			mApertureView.startAnimation(mAnimation);
		}
	}

	public void onAutoFocused() {
		// TODO Auto-generated method stub
		focusStatue = AUTO_FOCUSED;
		if(mApertureView != null){
			mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg);
		}
		
		if(mSeekBar != null){
			mSeekBar.setVisibility(View.VISIBLE);
			mSeekBar.setThumb(getResources().getDrawable(R.drawable.doublecamera_seakbar_thumb_gray));
			mSeekBar.setProgressDrawable(getResources().getDrawable(R.drawable.doublecamera_seakbar_pro_gray));
		}
		mHandler.removeMessages(MSG_HIDE);
	    mHandler.removeMessages(MSG_GRAY);
	    mHandler.sendEmptyMessageDelayed(MSG_GRAY, DELAY_TIME_GRAY);
	}
	
	public void onMvFocusing() {
		// TODO Auto-generated method stub
		if(getVisibility() == View.VISIBLE || mApertureView == null){
			return;
		}
		focusStatue = MV_FOCUSING;
		onSinlgeTapUp(null, mLastFocusPointX, mLastFocusPointY);
		mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg_white);
		Animation mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.aperture_alpha);
		mApertureView.startAnimation(mAnimation);
	}
	
	public void onMvFocused() {
		// TODO Auto-generated method stub
		if(focusStatue != MV_FOCUSING){
			return;
		}
		focusStatue = MV_FOCUSED;
		if(mApertureView != null){
			mApertureView.setBackgroundResource(R.drawable.aperture_view_focusing_bg);
			mHandler.removeMessages(MSG_HIDE);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE, 200);
		}
		
	}
	
	public void startShow(){
		mLastFocusPointX = screenwight / 2;
		mLastFocusPointY = screenheight / 2 - ((FrameLayout.LayoutParams)((CameraActivity)getContext()).getPreviewSurfaceView().getLayoutParams()).bottomMargin;
		Log.d(TAG, "mLastFocusPointX:"+ mLastFocusPointX +",mLastFocusPointY:"+ mLastFocusPointY);
		mHandler.removeMessages(MSG_START_SHOW);
		mHandler.sendEmptyMessage(MSG_START_SHOW);
	}
	
	/**
	 * 获取屏幕宽高
	 */
	private void getScreenWH() {
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		screenwight = wm.getDefaultDisplay().getWidth();
		screenheight = wm.getDefaultDisplay().getHeight();
	}
	
	public void hide(){
		setVisibility(View.GONE);
	}

	public void show() {
		// TODO Auto-generated method stub
		setVisibility(View.VISIBLE);
	}

	//PRIZE-add-invalidate seekbar layoutparam-20170303-start
	public void onSeekbarLayout(int x, int previewWidth, int focusWidth){
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)mSeekBar.getLayoutParams();
		if(x >= (previewWidth - mApertureView.getWidth()/2 - mSeekBarOrignalMarginTop - mSeekBar.getHeight())){
			p.topMargin = -(mSeekBarOrignalMarginTop + mApertureView.getWidth() + mSeekBar.getHeight());
		}else{
			p.topMargin = mSeekBarOrignalMarginTop;
		}
		mSeekBar.setLayoutParams(p);
	}
	//PRIZE-add-invalidate seekbar layoutparam-20170303-end
	
	public void onFocuseLayout(int left, int top, int previewWidth, int focusWidth){
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)getLayoutParams();
		if (p.getLayoutDirection() != View.LAYOUT_DIRECTION_RTL) {
	        p.setMargins(left, top, 0, 0);
	    } else {
	            // since in RTL language, framework will use marginRight as
	            // standard.
	        int right = previewWidth - (left + focusWidth);
	        p.setMargins(0, top, right, 0);
	    }
		setLayoutParams(p);
	}
	
}
